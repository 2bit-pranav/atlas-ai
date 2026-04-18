from tools.memory_tools import save_to_knowledge_base, search_knowledge_base 
from tools.web_tools import form_filler, job_scraper, data_extractor

MEMORY_TOOLS = [
    save_to_knowledge_base,
    search_knowledge_base
]

WEB_TOOLS = [
    form_filler,
    job_scraper,
    data_extractor
]

ALL_TOOLS = MEMORY_TOOLS + WEB_TOOLS

TOOL_GROUPS = {
    "memory_management": MEMORY_TOOLS,
    "web_execution": WEB_TOOLS,
    "general": ALL_TOOLS
}
