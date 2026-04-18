from fastapi import FastAPI
from langserve import add_routes
from langchain_core.messages import HumanMessage
from langchain_core.runnables import chain, RunnableConfig
from pydantic import BaseModel, Field
from typing import Any
import uuid

from agent import get_atlas_graph

class AtlasInput(BaseModel):
    message: str = Field(..., description="The message you want to send to Atlas.")


def _normalize_inputs(inputs: AtlasInput | dict[str, Any]) -> AtlasInput:
    if isinstance(inputs, AtlasInput):
        return inputs

    payload = inputs.get("input", inputs) if isinstance(inputs, dict) else {}
    if isinstance(payload, dict):
        return AtlasInput(**payload)

    raise TypeError("Unsupported input payload for Atlas")

def prepare_state(inputs: AtlasInput | dict[str, Any]) -> dict:
    inputs = _normalize_inputs(inputs)
    return {
        "messages": [HumanMessage(content=inputs.message)],
    }

SESSION_ID = str(uuid.uuid4())

@chain
async def atlas_api(inputs: AtlasInput, config: RunnableConfig):
    state = prepare_state(inputs)
    atlas_graph = await get_atlas_graph()

    config["configurable"] = {"thread_id": SESSION_ID}

    async for event in atlas_graph.astream(state, config=config, stream_mode="values"):
        yield event

atlas_api = atlas_api.with_types(input_type=AtlasInput)

app = FastAPI(
    title="Atlas Browser Agent",
    version="0.1",
    description="The local LangGraph backend for Atlas.",
)

add_routes(app, atlas_api, path="/atlas")

if __name__ == "__main__":
    import uvicorn

    print("Booting Atlas Dev Server")
    uvicorn.run(app, host="127.0.0.1", port=8000)
