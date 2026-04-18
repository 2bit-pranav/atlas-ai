# 🛑 ATLAS INTEGRATION GUIDE & COPILOT RULES

**ATTENTION AI ASSISTANTS AND DEVELOPERS:**
You are contributing to **Atlas**, an autonomous LangGraph/FastAPI web agent.

The architecture is intentionally opinionated to prevent:

* spaghetti code
* LLM hallucinations
* redundant tool calls
* duplicated browser logic
* chaotic integrations

**Strictly follow the rules below.**

---

# 1. Project Architecture Constraints

The project is strictly modular.

**Do NOT combine responsibilities or break boundaries.**

## Core Files

### `agent.py`

Contains **ONLY** the LangGraph topology:

* nodes
* edges
* routing logic

Do **not** place tool execution logic here.

---

### `main.py`

Contains:

* FastAPI server setup
* LangServe routes
* streaming endpoints
* API bootstrapping

---

### `memory/`

Handles the memory system.

Do not modify unless explicitly requested.

---

### `tools/`

Contains all active agent capabilities.

Must remain granular and organized by domain.

Example:

```text
tools/
├── browser/
├── form-input-tools/
├── scraping/
├── navigation/
└── utilities/
```

---

# 2. The Playwright Singleton Rule (CRITICAL)

## 🚨 Never instantiate a new Playwright browser inside a tool.

Launching multiple browser instances will destroy system memory and create unstable behavior.

We use a **Singleton Browser Manager**.

## Required Rule

If writing a web automation tool:

* import the global active browser/page context from `browser_manager.py`
* assume browser session already exists
* reuse current page/tab/session

## Valid Tool Behavior

Use:

```python
page.goto(...)
page.fill(...)
page.click(...)
page.locator(...)
```

## Forbidden Behavior

```python
async_playwright().start()
browser = playwright.chromium.launch()
```

inside individual tools.

---

# 3. Tool Creation Protocol (Mandatory 3-Step Process)

If adding a new capability (example: form filler tool), follow this exact process.

---

## Step A: Create Dedicated Logic File

Create a new file inside the proper subfolder.

Example:

```text
tools/form-input-tools/fill_text.py
```

Keep tool logic isolated.

One file = one responsibility.

---

## Step B: Wrap with `@tool` + Pydantic Schema

Every tool must:

* use LangChain `@tool`
* define arguments using Pydantic `BaseModel`
* include detailed docstrings

Example:

```python
from langchain_core.tools import tool
from pydantic import BaseModel, Field


class FillTextArgs(BaseModel):
    selector: str = Field(
        ...,
        description="CSS selector for the input field."
    )
    text: str = Field(
        ...,
        description="Text to inject into the field."
    )


@tool(args_schema=FillTextArgs)
def fill_text_field(selector: str, text: str):
    """
    Fill a text input field using a CSS selector.

    Use when:
    - a visible text input exists
    - a user-provided value must be entered

    Returns:
    - success/failure status
    """

    # Use global Playwright page instance here
```

## Why This Matters

Pydantic schemas reduce bad tool arguments and invalid tool calls.

Docstrings help the model understand when to use the tool.

---

## Step C: Register Tool in the Registry

Open:

```text
tools/tool_registry.py
```

Then:

* import the new tool
* add it to the correct tool group

Example:

```python
FORM_TOOLS = [
    fill_text_field,
    select_dropdown,
    submit_form
]
```

## Strict Rule

**Never bind tools directly inside `agent.py`.**

Tool registration belongs in the registry.

---

# 4. LLM Role Separation

Keep responsibilities clean.

---

## `executor_llm`

Primary reasoning model.

Responsible for:

* planning
* deciding actions
* selecting tools
* interpreting results
* conversation logic

This is the system brain.

---

## `speaker_node`

Responsible for user-facing transition messages.

Examples:

* “Let me fill out that form for you.”
* “Checking the page structure now.”
* “Submitting the application.”

## Important Rule

Do **not** print user-facing messages from tools.

Tools execute actions.

`speaker_node` communicates progress.

---

# 5. Collaboration Boundaries

To avoid duplicate work and random architecture drift:

---

## DOM Manipulators

Work inside `tools/`

Responsible for:

* Playwright logic
* selectors
* iframes
* shadow DOM
* waits/timeouts
* retries

Pure execution layer.

---

## Agent Integrators

Responsible for:

* wrapping logic with Pydantic schemas
* writing tool docstrings
* updating registry
* prompts
* orchestration logic

---

## Frontend / Middleware

Responsible for:

* WebSocket streaming
* handling LangServe chunked output
* progress events
* UI task state
* frontend/backend communication

---

# 6. Global Non-Negotiable Rules

## Never Duplicate Tools

If a click tool exists, reuse it.

Do not create:

* `click_button.py`
* `smart_click.py`
* `better_click_final.py`

One tool. Improve it.

---

## Never Hide Logic in Random Files

If code matters, it belongs in the architecture.

Not in:

```text
temp.py
new_test.py
final_real.py
```

---

## Always Prefer Reuse Over Reinvention

Before writing a tool:

1. Check registry
2. Check existing folders
3. Extend shared modules first

---

# 7. Final Principle

Atlas should feel like:

* one coherent system
* shared infrastructure
* modular intelligence

Not:

* five side projects zip-tied together before demo day.

Act accordingly.
