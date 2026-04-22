from langchain_core.tools import tool
from browser.manager import get_browser

@tool
async def scrape_form_fields() -> str:
    """
    Scans the current page and extracts all form fields including their
    IDs, types, labels, placeholders, and option text for radio/checkboxes.

    Use when:
    - You are about to fill a form and need to understand its structure
    - You need to know what fields exist before deciding what to fill
    - Always call this BEFORE using any form filling tools

    Returns:
    - A structured list of all form fields found on the page
    """
    try:
        browser = await get_browser()
        page = browser.page
        fields = await page.evaluate("""
            () => {
                const form = document.querySelector('form');
                if (!form) return [];
                const inputs = form.querySelectorAll('input, select, textarea');
                const results = [];

                inputs.forEach(el => {
                    const id = el.id;
                    if (!id) return;

                    const type = el.type || el.tagName.toLowerCase();
                    const placeholder = el.placeholder || '';
                    const value = el.value || '';

                    let labelText = '';
                    const label = document.querySelector(`label[for="${id}"]`);
                    if (label) labelText = label.innerText.trim();

                    let siblingText = '';
                    if (el.parentElement) {
                        siblingText = el.parentElement.innerText.trim();
                    }

                    results.push({
                        id, type, placeholder, value, labelText, siblingText
                    });
                });
                return results;
            }
        """)

        if not fields:
            return "No form fields found on the current page."

        output = f"Found {len(fields)} form fields:\n\n"
        for f in fields:
            line = f"ID: #{f['id']} | Type: {f['type']}"
            if f['placeholder']:
                line += f" | Placeholder: {f['placeholder']}"
            if f['labelText']:
                line += f" | Label: {f['labelText']}"
            if f['siblingText']:
                line += f" | OptionText: {f['siblingText']}"
            output += line + "\n"

        return output

    except Exception as e:
        return f"Failed to scrape form fields. Error: {str(e)}"


@tool
async def scrape_page_text() -> str:
    """
    Extracts all visible text content from the current page.

    Use when:
    - You need to read the content of a page
    - You want to extract information from an article or result page
    - You need to confirm what is shown on screen after an action

    Returns:
    - The visible text content of the page trimmed to 3000 characters
    """
    try:
        browser = await get_browser()
        page = browser.page
        text = await page.locator("body").inner_text()
        if len(text) > 3000:
            text = text[:3000] + "\n...[truncated]"
        return f"Page text content:\n{text}"
    except Exception as e:
        return f"Failed to scrape page text. Error: {str(e)}"


@tool
async def get_current_url() -> str:
    """
    Returns the URL of the page currently open in the browser.

    Use when:
    - You need to confirm which page you are on
    - You want to verify navigation worked correctly
    - You need the current URL to pass to another tool

    Returns:
    - The current page URL as a string
    """
    try:
        browser = await get_browser()
        page = browser.page
        return f"Current URL: {page.url}"
    except Exception as e:
        return f"Failed to get current URL. Error: {str(e)}"


@tool
async def get_all_links() -> str:
    """
    Extracts all clickable links from the current page.

    Use when:
    - You need to find a specific link to navigate to
    - You are exploring a page to find where to go next
    - You need to list available navigation options

    Returns:
    - A list of link text and their URLs capped at 30 results
    """
    try:
        browser = await get_browser()
        page = browser.page
        links = await page.evaluate("""
            () => {
                const anchors = document.querySelectorAll('a[href]');
                return Array.from(anchors).map(a => ({
                    text: a.innerText.trim(),
                    href: a.href
                })).filter(l => l.text && l.href);
            }
        """)

        if not links:
            return "No links found on current page."

        output = f"Found {len(links)} links:\n"
        for l in links[:30]:
            output += f"- {l['text']}: {l['href']}\n"
        if len(links) > 30:
            output += f"...and {len(links) - 30} more."
        return output

    except Exception as e:
        return f"Failed to get links. Error: {str(e)}"


@tool
async def check_element_exists(selector: str) -> str:
    """
    Checks whether a specific element exists on the current page.

    Use when:
    - You need to confirm a button or field is present before clicking
    - You want to verify a form loaded correctly
    - You are unsure if a selector is valid before using it

    Returns:
    - Whether the element was found and how many matches exist
    """
    try:
        browser = await get_browser()
        page = browser.page
        count = await page.locator(selector).count()
        if count > 0:
            return f"Element '{selector}' exists on the page. Found {count} match(es)."
        return f"Element '{selector}' was NOT found on the page."
    except Exception as e:
        return f"Error checking element '{selector}'. Error: {str(e)}"