from langchain_core.tools import tool

@tool
def form_filler(url: str, goal: str) -> str:
    """
    Use this agent to navigate to a specific URL and fill out forms, 
    registrations, or applications based on the user's factual memory
    or on-going conversation data.
    """
    return f"[Form Filler Agent] Successfully filled out the form at {url} with the goal: {goal}."

@tool
def job_scraper(criteria: str) -> str:
    """
    Use this agent to search the web for jobs. It will autonomously 
    find job boards, search, and extract listings matching the criteria.
    """
    return f"[Job Scraper Agent] Found job listings matching criteria: {criteria}."

@tool
def data_extractor(query: str, url: str = None) -> str:
    """
    Use this agent to scrape, read, or extract specific data. 
    If you don't know the URL, leave it blank and the agent will search for it.
    """
    target = url if url else "relevant sources"
    return f"[Data Extractor Agent] Extracted data for query: '{query}' from {target}."

# memory tools here 