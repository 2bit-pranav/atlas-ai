import asyncio
import json
from pathlib import Path
from playwright.async_api import async_playwright, Browser, BrowserContext, Page

STATE_FILE = Path("saves/browser_state.json")

class BrowserManager:
    _instance = None

    def __init__(self):
        self.playwright = None
        self.browser: Browser = None
        self.context: BrowserContext = None
        self.page: Page = None

    @classmethod
    async def get_instance(cls):
        if cls._instance is None:
            cls._instance = cls()
            await cls._instance.start()
        return cls._instance

    async def start(self):
        print("[Browser] Booting Playwright...")
        self.playwright = await async_playwright().start()
        self.browser = await self.playwright.chromium.launch(headless=False) # Keep False for MVP demo

        # Check if we have saved auth tokens/cookies
        if STATE_FILE.exists():
            print(f"[Browser] Loading existing session from {STATE_FILE}...")
            self.context = await self.browser.new_context(storage_state=str(STATE_FILE))
        else:
            print("[Browser] No saved session found. Starting fresh.")
            self.context = await self.browser.new_context()

        self.page = await self.context.new_page()

    async def save_session(self):
        """Call this after a successful login or at the end of a run"""
        if self.context:
            # Ensure the directory exists
            STATE_FILE.parent.mkdir(parents=True, exist_ok=True)
            await self.context.storage_state(path=str(STATE_FILE))
            print(f"[Browser] Session state securely saved to {STATE_FILE}")

    async def close(self):
        if self.context:
            await self.save_session() # Auto-save on shutdown
        if self.browser:
            await self.browser.close()
        if self.playwright:
            await self.playwright.stop()

# Export a single global instance getter
async def get_browser():
    return await BrowserManager.get_instance()

# if __name__ == "__main__":
#     import asyncio

#     async def test_browser():
#         print("🚀 Starting independent browser test...")
        
#         # 1. Boot the manager (this should open the browser)
#         manager = await get_browser()
        
#         # 2. Navigate to a test page
#         print("🌐 Navigating to a test page...")
#         await manager.page.goto("https://github.com/login")
        
#         # 3. Wait a few seconds so you can visually confirm it worked
#         print("⏳ Waiting 5 seconds. (Try typing something into the login box!)")
#         await asyncio.sleep(5)
        
#         # 4. Trigger the shutdown and save sequence
#         print("💾 Closing browser and saving state...")
#         await manager.close()
        
#         print("✅ Test complete! Check your 'saves' folder for browser_state.json.")

#     # Run the async test loop
#     asyncio.run(test_browser())