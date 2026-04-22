from tools.memory.memory_tools import save_to_knowledge_base, search_knowledge_base 
from tools.navigation.navigation_tools import navigate_to_url
from tools.scrolling.scroll_tools import scroll_down, scroll_up, scroll_to_top, scroll_to_bottom
from tools.scraping.scraper_tools import (
    scrape_form_fields,
    scrape_page_text,
    get_current_url,
    get_all_links,
    check_element_exists
)

MEMORY_TOOLS = [
    save_to_knowledge_base,
    search_knowledge_base
]

NAVIGATION_TOOLS = [
    navigate_to_url
]

SCROLL_TOOLS = [
    scroll_down,
    scroll_up,
    scroll_to_top,
    scroll_to_bottom
]

SCRAPER_TOOLS = [
    scrape_form_fields,
    scrape_page_text,
    get_current_url,
    get_all_links,
    check_element_exists
]

ALL_TOOLS = MEMORY_TOOLS + NAVIGATION_TOOLS + SCROLL_TOOLS + SCRAPER_TOOLS

TOOL_GROUPS = {
    "MEMORY": MEMORY_TOOLS,
    "NAV": NAVIGATION_TOOLS,
    "SCROLL": SCROLL_TOOLS,
    "SCRAPE": SCRAPER_TOOLS,
    "GENERAL": ALL_TOOLS
}