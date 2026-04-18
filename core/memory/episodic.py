import os
from langchain_community.vectorstores import FAISS
from langchain_ollama import OllamaEmbeddings

EPISODIC_INDEX_FILE = "saves/atlas_episodic_memory"
embeddings = OllamaEmbeddings(model="nomic-embed-text")

def _get_db():
    if os.path.exists(EPISODIC_INDEX_FILE):
        return FAISS.load_local(EPISODIC_INDEX_FILE, embeddings, allow_dangerous_deserialization=True)
    else:
        db = FAISS.from_texts(["[System] Episodic Memory Initialized."], embeddings)
        db.save_local(EPISODIC_INDEX_FILE)
        return db
    
def search_past_experiences(user_intent: str) -> str:
    db = _get_db()
    docs = db.similarity_search(user_intent, k=1)

    if docs and "System" not in docs[0].page_content:
        return f"Past experience: {docs[0].page_content}"
    return ""

def log_experience(user_request: str, successful_plan: str):
    db = _get_db()
    experience_text = f"Request: {user_request}\nExecuted Plan: {successful_plan}"
    db.add_texts([experience_text])
    db.save_local(EPISODIC_INDEX_FILE)
    print(f"[Memory] Logged experience in episodic memory")