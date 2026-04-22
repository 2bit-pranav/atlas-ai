from langchain_core.tools import tool
from memory import semantic

@tool
def save_to_knowledge_base(text_chunk: str, source: str) -> str:
    """
    Saves important information, long articles, or documentation to the permanent semantic memory.
    Use this actively when the user asks you to "study", "remember", or "save" a specific 
    document, URL content, or tutorial for future reference.
    """
    semantic.embed_document(text_chunk, source)
    return f"Saved new information to knowledge base from source: {source}."

@tool
def search_knowledge_base(query: str) -> str:
    """
    Searches the permanent semantic memory for specific concepts, past knowledge, or documentation.
    Use this if you are missing information to complete a task, and you think it might be 
    stored in the user's permanent knowledge base.
    """
    results = semantic.search_knowledge(query)
    if results:
        return results
    return "No relevant information found in knowledge base"