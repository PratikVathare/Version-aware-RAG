import os
from typing import List
from fastapi import FastAPI, HTTPException
from pydantic import BaseModel, Field
from sentence_transformers import SentenceTransformer
import uvicorn

MODEL_NAME = os.getenv("EMBEDDING_MODEL_NAME", "sentence-transformers/all-MiniLM-L6-v2")
PORT = int(os.getenv("PORT", "8000"))
HOST = os.getenv("HOST", "0.0.0.0")

app = FastAPI(
    title="Local Embedding Microservice",
    description=f"Sentence-Transformers embedding generator using {MODEL_NAME}",
    version="1.0.0"
)

_model = None

def get_model():
    global _model
    if _model is None:
        print(f"Loading embedding model: {MODEL_NAME}...")
        try:
            _model = SentenceTransformer(MODEL_NAME)
            print(f"Model loaded successfully! Embedding dimension: {_model.get_sentence_embedding_dimension()}")
        except Exception as e:
            print(f"Error loading model from HuggingFace: {e}")
            raise e
    return _model


class EmbedRequest(BaseModel):
    texts: List[str] = Field(..., description="List of chunk text strings to embed", min_length=1)


class EmbedResponse(BaseModel):
    model: str
    dimension: int
    embeddings: List[List[float]]


@app.get("/health")
def health_check():
    return {
        "status": "healthy",
        "model": MODEL_NAME,
        "dimension": 384
    }


@app.post("/embed", response_model=EmbedResponse)
def generate_embeddings(request: EmbedRequest):
    if not request.texts:
        raise HTTPException(status_code=400, detail="Text list cannot be empty")

    try:
        model = get_model()
        embeddings = model.encode(
            request.texts,
            convert_to_numpy=True,
            normalize_embeddings=True,
            show_progress_bar=False
        )

        return EmbedResponse(
            model=MODEL_NAME,
            dimension=model.get_sentence_embedding_dimension(),
            embeddings=embeddings.tolist()
        )
    except Exception as e:
        raise HTTPException(status_code=500, detail=f"Failed to generate embeddings: {str(e)}")


if __name__ == "__main__":
    uvicorn.run("app:app", host=HOST, port=PORT, reload=False)
