import os
import re
import json

import config
from database import Database
from embedder import Embedder

db = Database(config.NEO4J_URI, config.NEO4J_USER, config.NEO4J_PASSWORD)
embedder = Embedder(config.EMBEDDING_MODEL_NAME)

folder_path = "test_data/"

new_tags = set()
old_tags = {}

with open("tag.json", "r", encoding="utf-8") as f:
    old_tags = json.load(f)

for filename in os.listdir(folder_path):
    if os.path.isfile(os.path.join(folder_path, filename)):
        match = re.search(r'\[([^\]]+)\]', filename)
        if match:
            tags = match.group(1).split(",")
            for tag in tags:
                if tag in old_tags:
                    pass
                else:
                    new_tags.add(tag)

idx = len(old_tags)
for tag in new_tags:
    idx += 1
    db.create_tag(idx)
    old_tags[tag] = idx
            
with open("tag.json", "w", encoding="utf-8") as f:
    json.dump(old_tags, f, indent=2, ensure_ascii=False)

idx = db.getMaxItemId()

for filename in os.listdir(folder_path):
    file_path = os.path.join(folder_path, filename)
    if os.path.isfile(file_path) and filename.lower().endswith(".txt"):
        match = re.search(r'\[([^\]]+)\]', filename)
        tags = []
        if match:
            tag_strs = match.group(1).split(",")
            for tag_str in tag_strs:
                if tag_str in old_tags:
                    tags.append(old_tags[tag_str])

        title = ""
        text = ""

        with open(file_path, "r", encoding="utf-8") as f:
            title = f.readline().rstrip("\n")
            pos = title.find('-')
            if pos != -1:
                title = title[:pos]
            title = title.strip(" \t\n#")
            text = f.read()
            text = text.strip(" \t\n")

        if len(title) == 0 or len(text) == 0:
            continue

        idx += 1
        embedding = embedder.encode(text)
        db.create_item(idx, title, embedding, tags)
