from langchain_core.tools import tool
from browser.manager import get_browser
import asyncio

@tool
async def scroll_down() -> str:
    """
    Scrolls the current page down by 300 pixels.

    Use when:
    - Content is below the visible area
    - You need to reveal more of the page
    - A button or form field is not visible yet

    Returns:
    - Confirmation of scroll action
    """
    try:
        browser = await get_browser()
        page = browser.page
        await page.evaluate("window.scrollBy(0, 300)")
        await asyncio.sleep(0.3)
        return "Scrolled down by 300 pixels."
    except Exception as e:
        return f"Failed to scroll down. Error: {str(e)}"


@tool
async def scroll_up() -> str:
    """
    Scrolls the current page up by 300 pixels.

    Use when:
    - You need to go back up on the page
    - You scrolled too far down
    - You need to reach content at the top

    Returns:
    - Confirmation of scroll action
    """
    try:
        browser = await get_browser()
        page = browser.page
        await page.evaluate("window.scrollBy(0, -300)")
        await asyncio.sleep(0.3)
        return "Scrolled up by 300 pixels."
    except Exception as e:
        return f"Failed to scroll up. Error: {str(e)}"


@tool
async def scroll_to_top() -> str:
    """
    Scrolls all the way to the top of the page instantly.

    Use when:
    - You need to start from the beginning of the page
    - You want to reset scroll position

    Returns:
    - Confirmation of scroll action
    """
    try:
        browser = await get_browser()
        page = browser.page
        await page.evaluate("window.scrollTo(0, 0)")
        await asyncio.sleep(0.3)
        return "Scrolled to the top of the page."
    except Exception as e:
        return f"Failed to scroll to top. Error: {str(e)}"


@tool
async def scroll_to_bottom() -> str:
    """
    Scrolls all the way to the bottom of the page instantly.

    Use when:
    - You need to reach content at the very bottom
    - You want to find a submit button at the end of a form
    - You need to load lazy-loaded content

    Returns:
    - Confirmation of scroll action
    """
    try:
        browser = await get_browser()
        page = browser.page
        await page.evaluate("window.scrollTo(0, document.body.scrollHeight)")
        await asyncio.sleep(0.3)
        return "Scrolled to the bottom of the page."
    except Exception as e:
        return f"Failed to scroll to bottom. Error: {str(e)}"