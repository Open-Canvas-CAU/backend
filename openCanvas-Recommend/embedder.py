import torch
from sentence_transformers import SentenceTransformer
from sklearn.metrics.pairwise import cosine_similarity


class Embedder:
    def __init__(self, model_name):
        device = "cuda" if torch.cuda.is_available() else "cpu"
        self.model = SentenceTransformer(model_name, device=device)

    def encode(self, text: str):
        encoded = self.model.encode([text])[0].astype(float).tolist()
        return encoded
