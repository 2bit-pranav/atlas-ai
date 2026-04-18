from typing import TypedDict, Annotated, NotRequired
from langchain_core.messages import BaseMessage
from langgraph.graph.message import add_messages


class AgentState(TypedDict):
    messages: Annotated[list[BaseMessage], add_messages]

    factual_memory: NotRequired[dict]
    episodic_memory: NotRequired[dict]
    semantic_memory: NotRequired[str]

    plan: NotRequired[str]
    step_count: NotRequired[int]
    active_url: NotRequired[str]
    missing_info: NotRequired[str]
