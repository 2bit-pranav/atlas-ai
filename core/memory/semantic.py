import os
from langchain_community.vectorstores import FAISS
from langchain_ollama import OllamaEmbeddings

SEMANTIC_INDEX_FILE = "saves/atlas_semantic_memory"
embeddings = OllamaEmbeddings(model="nomic-embed-text")

def _get_db():
    if os.path.exists(SEMANTIC_INDEX_FILE):
        return FAISS.load_local(SEMANTIC_INDEX_FILE, embeddings, allow_dangerous_deserialization=True)
    else:
        db = FAISS.from_texts(["[Memory] Semantic Memory Initialized."], embeddings)
        db.save_local(SEMANTIC_INDEX_FILE)
        return db
    
def search_knowledge(query: str, top_k: int = 3) -> str:
    db = _get_db()
    docs = db.similarity_search(query, k=top_k)

    if not docs:
        return ""
    
    results = "\n---\n".join([doc.page_content for doc in docs])
    return f"Relevant knowledge base entries: \n{results}"

def embed_document(text_chunk: str, source: str):
    db = _get_db()
    db.add_texts(texts=[text_chunk], metadatas=[{"source": source}])
    db.save_local(SEMANTIC_INDEX_FILE)
    print(f"[Memory] Added new document to semantic memory from source: {source}")