from contextlib import asynccontextmanager
from typing import List
import logging

from fastapi import FastAPI, HTTPException
from pydantic import BaseModel

import config
from database import Database
from embedder import Embedder

# 로깅 설정
logging.basicConfig(
    level=logging.INFO,
    format='%(asctime)s - %(levelname)s - %(message)s'
)
logger = logging.getLogger(__name__)


class ItemCreateRequest(BaseModel):
    id: int
    title: str
    text: str
    tags: List[int] = []


class RecommendResponse(BaseModel):
    items: List[int] = []


db = None
embedder = None


@asynccontextmanager
async def lifespan(app: FastAPI):
    global db, embedder
    # start
    db = Database(config.NEO4J_URI, config.NEO4J_USER, config.NEO4J_PASSWORD)
    embedder = Embedder(config.EMBEDDING_MODEL_NAME)

    # run
    yield

    # shutdown
    db.close()


app = FastAPI(lifespan=lifespan)


@app.post("/user/{user_id}", status_code=204)
def create_user(user_id: int):
    logger.info(f"[CREATE_USER] user_id: {user_id}")
    global db
    db.create_user(user_id)


@app.delete("/user/{user_id}", status_code=204)
def delete_user(user_id: int):
    logger.info(f"[DELETE_USER] user_id: {user_id}")
    global db
    db.delete_user(user_id)


@app.post("/tag/{tag_id}", status_code=204)
def create_tag(tag_id: int):
    logger.info(f"[CREATE_TAG] tag_id: {tag_id}")
    global db
    db.create_tag(tag_id)


@app.delete("/tag/{tag_id}", status_code=204)
def delete_tag(tag_id: int):
    logger.info(f"[DELETE_TAG] tag_id: {tag_id}")
    global db
    db.delete_tag(tag_id)


@app.post("/item", status_code=204)
def create_item(item: ItemCreateRequest):
    logger.info(
        f"[CREATE_ITEM] item_id: {item.id}, title: {item.title}, tags: {item.tags}")
    global db, embedder
    embedding = embedder.encode(item.text)
    db.create_item(item_id=item.id, title=item.title,
                   embedding=embedding, tags=item.tags)


@app.delete("/item/{item_id}", status_code=204)
def delete_item(item_id: int):
    logger.info(f"[DELETE_ITEM] item_id: {item_id}")
    global db
    db.delete_item(item_id)


@app.post("/user/{user_id}/view/{item_id}", status_code=204)
def create_user_view(user_id: int, item_id: int):
    logger.info(f"[CREATE_USER_VIEW] user_id: {user_id}, item_id: {item_id}")
    global db
    db.create_user_view(user_id, item_id)


@app.delete("/user/{user_id}/view/{item_id}", status_code=204)
def delete_user_view(user_id: int, item_id: int):
    logger.info(f"[DELETE_USER_VIEW] user_id: {user_id}, item_id: {item_id}")
    global db
    db.delete_user_view(user_id, item_id)


@app.post("/user/{user_id}/like/{item_id}", status_code=204)
def create_user_like(user_id: int, item_id: int):
    logger.info(f"[CREATE_USER_LIKE] user_id: {user_id}, item_id: {item_id}")
    global db
    db.create_user_like(user_id, item_id)


@app.delete("/user/{user_id}/like/{item_id}", status_code=204)
def delete_user_like(user_id: int, item_id: int):
    logger.info(f"[DELETE_USER_LIKE] user_id: {user_id}, item_id: {item_id}")
    global db
    db.delete_user_like(user_id, item_id)


@app.get("/recommend_item/{item_id}", response_model=RecommendResponse)
def get_recommend_by_item(item_id: int, top_n: int = 5):
    logger.info(f"[RECOMMEND_BY_ITEM] item_id: {item_id}, top_n: {top_n}")
    global db
    items = db.recommend_by_item(item_id=item_id, top_n=top_n, viewW=config.VIEW_WEIGHT,
                                 likeW=config.LIKE_WEIGHT, tagW=config.TAG_WEIGHT)
    return RecommendResponse(
        items=items
    )


@app.get("/recommend_user/{user_id}", response_model=RecommendResponse)
def get_recommend_by_user(user_id: int, top_n: int = 5):
    logger.info(f"[RECOMMEND_BY_USER] user_id: {user_id}, top_n: {top_n}")
    global db
    items = db.recommend_by_user(user_id=user_id, top_n=top_n, user_limit=10,
                                 viewW=config.VIEW_WEIGHT, likeW=config.LIKE_WEIGHT)
    return RecommendResponse(
        items=items
    )


if __name__ == "__main__":
    import uvicorn
    uvicorn.run("main:app", host="0.0.0.0", port=config.PORT, reload=True)
