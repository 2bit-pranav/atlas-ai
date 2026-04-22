from langchain_core.tools import tool
from pydantic import BaseModel, Field
from browser.manager import get_browser
import urllib.parse

class NavigateArgs(BaseModel):
    query: str = Field(
        ..., 
        description="The website name, search term, or URL to navigate to (e.g., 'vesit college' or 'github.com')."
    )

@tool(args_schema=NavigateArgs)
async def navigate_to_url(query: str):
    """
    Navigates to a website. It attempts a direct URL first. If that fails, it queries DuckDuckGo and clicks the best organic result.
    """
    manager = await get_browser()
    target = query.strip()
    
    if "." in target and " " not in target:
        if not target.startswith("http"):
            target = "https://" + target
        try:
            print(f"[Browser] Attempting direct navigation to {target}...")
            await manager.page.goto(target, timeout=8000) 
            return f"Successfully navigated to {manager.page.url}"
        except Exception:
            print(f"[Browser] Direct URL failed. Falling back to DuckDuckGo...")
            pass

    try:
        print(f"[Browser] Searching DuckDuckGo for: {query}")
        safe_query = urllib.parse.quote(query)
        # Using the lightweight HTML version of DDG
        search_url = f"https://html.duckduckgo.com/html/?q={safe_query}"
        
        await manager.page.goto(search_url, timeout=10000)
        
        # DuckDuckGo HTML organic results use the class .result__snippet
        await manager.page.wait_for_selector(".result__snippet", timeout=10000)
        first_result = await manager.page.query_selector(".result__snippet")
        
        if first_result:
            await first_result.click()
            await manager.page.wait_for_load_state("domcontentloaded", timeout=10000)
            return f"Successfully navigated to: {manager.page.url}"
        else:
            return f"Searched for '{query}' but found no clickable links."
            
    except Exception as e:
        return f"CRITICAL FAILURE during navigation. Error: {str(e)}"