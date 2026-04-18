import os
from langchain_ollama import ChatOllama, OllamaEmbeddings
from langchain_core.messages import SystemMessage, AIMessage
from langgraph.graph import StateGraph, START, END
from langgraph.prebuilt import ToolNode
from langgraph.checkpoint.sqlite.aio import AsyncSqliteSaver
import asyncio
import aiosqlite
import json
from pathlib import Path

from state import AgentState
from memory import factual, semantic, episodic
from tools.tool_registry import ALL_TOOLS

executor_llm = ChatOllama(model="gemma4:e2b", temperature=0)
executor_llm_with_tools = executor_llm.bind_tools(ALL_TOOLS)
embeddings = OllamaEmbeddings(model="nomic-embed-text")


def memfetch_node(state: AgentState):
    """Fetches from local FAISS if missing"""
    print(f"[Memory] Searching memory...")
    user_query = state["messages"][-1].content

    facts = factual.get_profile()
    semantic_context = semantic.search_knowledge(user_query)
    episodic_context = episodic.search_past_experiences(user_query)

    return {
        "factual_memory": facts,
        "semantic_memory": semantic_context,
        "episodic_memory": episodic_context,
        "step_count": 0
    }


def planner_node(state: AgentState):
    """Generates a strict execution plan to avoid hallucinations"""
    print(f"[Planner] Drafting execution steps...")
    prompt = "Look at the user's request and write a 3-step action plan. Do not execute it, just list the steps."
    plan_text = executor_llm.invoke(
        state["messages"] + [SystemMessage(content=prompt)],
        think=False,
    ).content
    return {"plan": plan_text}


def executor_node(state: AgentState):
    """Reasoning node that decides on next steps"""
    print(f"[Atlas] Evaluating next steps...")

    facts = state.get("factual_memory", {})

    sys_prompt = f"""You are Atlas, an autonomous web execution agent.
    Current User: {facts.get('name', 'Pranav')}
    Semantic Context: {state.get('semantic_memory', 'None')}
    
    If you lack crucial information to use a tool, ask the user.
    """

    msgs = [SystemMessage(content=sys_prompt)] + state["messages"]
    response = executor_llm_with_tools.invoke(msgs, think=False)

    return {"messages": [response]}


def speaker_node(state: AgentState):
    """Node that generates conversational responses"""
    last_msg = state["messages"][-1]
    last_user_msg = next((m for m in reversed(state["messages"]) if m.type == "human"), "")

    if last_msg.tool_calls:
        tool_call = last_msg.tool_calls[0]
        tool_name = tool_call["name"]
        tool_args = tool_call["args"]

        print(f"[Atlas] Calling tool: {tool_name}")

        prompt = f"""You are Atlas, a conversational AI agent. 
        The user just said: "{last_user_msg}".
        To help them, you are about to execute the tool '{tool_name}' with these arguments: {tool_args}.
        
        Write exactly ONE short, conversational sentence telling the user what you are doing right now. 
        Make it sound natural, like a human pair-programmer.
        
        GOOD EXAMPLES: 
        - "Give me a second to search the web for backend jobs in Mumbai."
        - "I'll file that preference away in your permanent memory."
        
        DO NOT output markdown, JSON, or any other filler. Just the one sentence.
        """
        ui_text = executor_llm.invoke(prompt, think=False).content
        ui_text = ui_text.replace('"', '').strip()
        last_msg.content = ui_text

    return {"messages": [last_msg]}


def memwrite_node(state: AgentState):
    """Runs before END and updates memory"""
    print(f"[Memory] Reviewing conversation for updates...")

    # factual write
    last_user_msg = next((m for m in reversed(state["messages"]) if m.type == "human"), None)

    if last_user_msg:
        extraction_prompt = f"""Extract any permanent personal facts the user stated about themselves in this message. 
        Return ONLY a valid JSON object. Do not use markdown formatting.
        Example: {{"name": "Pranav", "location": "Mumbai"}}
        If no facts are present, return {{"None": "None"}}.
        Message: "{last_user_msg.content}"
        """

        try:
            extraction_text = executor_llm.invoke(extraction_prompt, think=False).content

            if "```json" in extraction_text:
                extraction_text = extraction_text.split("```json")[1].split("```")[0].strip()
            elif "```" in extraction_text:
                extraction_text = extraction_text.replace("```", "").strip()

            new_facts = json.loads(extraction_text)

            if "None" not in new_facts:
                factual.update_facts(new_facts)
                print(f"[Memory] Updated factual memory with: {new_facts}")

        except json.JSONDecodeError:
            print(f"[Memory] Failed to parse factual extraction into JSON")
        except Exception as e:
            print(f"[Memory] Error during factual extraction: {e}")

    # episodic write
    if state.get("plan"):
        user_query = state["messages"][0].content
        episodic.log_experience(user_query, state["plan"])

    return {}


def executor_router(state: AgentState):
    last_msg = state["messages"][-1]

    if last_msg.tool_calls:
        return "speaker_node"

    return "memwrite_node"


def guard_router(state: AgentState):
    """Failsafe for infinite loops"""
    count = state.get("step_count", 0) + 1

    if count > 5:
        print(f"[Guard] Too many steps, ending workflow.")
        return "memwrite_node"

    state["step_count"] = count
    return "executor_node"


workflow = StateGraph(AgentState)

# define nodes
workflow.add_node("memfetch_node", memfetch_node)
workflow.add_node("planner_node", planner_node)
workflow.add_node("executor_node", executor_node)
workflow.add_node("speaker_node", speaker_node)
workflow.add_node("tools_node", ToolNode(ALL_TOOLS))
workflow.add_node("memwrite_node", memwrite_node)

# core flow
workflow.add_edge(START, "memfetch_node")
workflow.add_edge("memfetch_node", "planner_node")
workflow.add_edge("planner_node", "executor_node")

# executor branch
workflow.add_conditional_edges(
    "executor_node",
    executor_router,
    {
        "speaker_node": "speaker_node",
        "memwrite_node": "memwrite_node",
    },
)

# tool execution loop
workflow.add_edge("speaker_node", "tools_node")
workflow.add_conditional_edges(
    "tools_node",
    guard_router,
    {
        "executor_node": "executor_node",
        "memwrite_node": "memwrite_node",
    },
)

# exit
workflow.add_edge("memwrite_node", END)

_atlas_graph = None
_memory_conn = None
_memory_saver = None
_graph_init_lock = asyncio.Lock()

async def get_atlas_graph():
    global _atlas_graph, _memory_conn, _memory_saver

    if _atlas_graph is not None:
        return _atlas_graph

    async with _graph_init_lock:
        if _atlas_graph is not None:
            return _atlas_graph

        checkpoint_path = Path("checkpoints") / "atlas_memory.db"
        checkpoint_path.parent.mkdir(parents=True, exist_ok=True)

        if _memory_saver is None:
            _memory_conn = await aiosqlite.connect(str(checkpoint_path))
            _memory_saver = AsyncSqliteSaver(_memory_conn)

        _atlas_graph = workflow.compile(checkpointer=_memory_saver)

    return _atlas_graph

# png_bytes = atlas_graph.get_graph().draw_mermaid_png(max_retries=5, retry_delay=2.0)
# output_path = Path(__file__).with_name("atlas_graph.png")
# output_path.write_bytes(png_bytes)