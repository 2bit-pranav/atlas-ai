import os
import json
from typing import Any

FACTS_FILE = "saves/atlas_factual_memory.json"

def _load_facts() -> dict:
    if not os.path.exists(FACTS_FILE):
        return {}
    with open(FACTS_FILE, "r") as f:
        return json.load(f)
    
def _save_facts(facts: dict):
    with open(FACTS_FILE, "w") as f:
        json.dump(facts, f, indent=4)

def get_profile() -> dict:
    return _load_facts()

def update_facts(new_facts: dict):
    current_facts = _load_facts()
    for key, value in new_facts.items():
        if isinstance(value, list) and key in current_facts and isinstance(current_facts[key], list):
            current_facts[key] = list(set(current_facts[key] + value))
        else:
            current_facts[key] = value

    _save_facts(current_facts)
    print(f"[Memory] Updated factual memory with {list(new_facts.keys())}")