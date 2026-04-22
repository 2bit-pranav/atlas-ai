from fastapi import FastAPI
from langserve import add_routes
from langchain_core.messages import HumanMessage
from langchain_core.runnables import chain, RunnableConfig
from pydantic import BaseModel, Field
from typing import Any
import uuid
from contextlib import asynccontextmanager
from browser.manager import BrowserManager

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

@asynccontextmanager
async def lifespan(app: FastAPI):
    # startup: do nothing

    yield
    
    # gracefully shutdown the active browser instance on exit
    if BrowserManager._instance is not None:
        print("Saving browser state and closing Playwright...")
        await BrowserManager._instance.close()

@chain
async def atlas_api(inputs: AtlasInput, config: RunnableConfig):
    state = prepare_state(inputs)
    atlas_graph = await get_atlas_graph()

    config["configurable"] = {"thread_id": SESSION_ID}

    async for event in atlas_graph.astream(state, config=config, stream_mode="values"):
        clean_messages = []
        for m in event.get("messages", []):
            content_str = m.content
            if isinstance(content_str, list):
                content_str = "".join(
                    b.get("text", "") if isinstance(b, dict) else str(b) 
                    for b in content_str
                )
            
            clean_messages.append({
                "type": m.type,
                "content": content_str,
                "id": m.id if hasattr(m, "id") else ""
            })

        yield {
            "messages": clean_messages,
            "logs": event.get("logs", [])
        }

atlas_api = atlas_api.with_types(input_type=AtlasInput)

app = FastAPI(
    title="Atlas Browser Agent",
    version="0.1",
    description="The local LangGraph backend for Atlas.",
    lifespan=lifespan
)

add_routes(app, atlas_api, path="/atlas")

if __name__ == "__main__":
    import uvicorn
    import asyncio
    import sys

    if sys.platform == "win32":
        asyncio.set_event_loop_policy(asyncio.WindowsProactorEventLoopPolicy())

    print("Booting Atlas Dev Server")
    
    uvicorn.run(
        "main:app", 
        host="127.0.0.1", 
        port=8000, 
        loop="asyncio" 
    )
