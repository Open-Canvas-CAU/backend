import os

from dotenv import load_dotenv

# 환경 변수 설정
load_dotenv()

PORT = int(os.getenv("PORT"))

NEO4J_URI = os.getenv("NEO4J_URI")
NEO4J_USER = os.getenv("NEO4J_USER")
NEO4J_PASSWORD = os.getenv("NEO4J_PASSWORD")

EMBEDDING_MODEL_NAME = os.getenv("EMBEDDING_MODEL_NAME")

TAG_WEIGHT = float(os.getenv("TAG_WEIGHT"))
LIKE_WEIGHT = float(os.getenv("LIKE_WEIGHT"))
VIEW_WEIGHT = float(os.getenv("VIEW_WEIGHT"))
