from typing import List, Dict
import logging
import math

from neo4j import GraphDatabase

logging.getLogger("neo4j").setLevel(logging.ERROR)
logging.getLogger("neo4j.bolt").setLevel(logging.ERROR)


class Database():
    def __init__(self, uri: str, user: str, password: str):
        self.driver = GraphDatabase.driver(uri, auth=(user, password))

        try:
            with self.driver.session() as session:
                session.run(
                    """
                    CREATE CONSTRAINT IF NOT EXISTS
                    FOR (u:User)
                    REQUIRE u.user_id IS UNIQUE
                    """
                )

                session.run(
                    """
                    CREATE CONSTRAINT IF NOT EXISTS
                    FOR (t:Tag)
                    REQUIRE t.tag_id IS UNIQUE
                    """
                )

                session.run(
                    """
                    CREATE CONSTRAINT IF NOT EXISTS
                    FOR (i:Item)
                    REQUIRE i.item_id IS UNIQUE
                    """
                )
        except Exception:
            pass

    def close(self):
        if self.driver:
            self.driver.close()

    def reset(self):
        with self.driver.session() as session:
            constraints = session.run("SHOW CONSTRAINTS")
            for record in constraints:
                constraint_name = record.get("name")
                if constraint_name:
                    session.run(f"DROP CONSTRAINT {constraint_name} IF EXISTS")

            indexes = session.run("SHOW INDEXES")
            for record in indexes:
                index_name = record.get("name")
                if index_name:
                    session.run(f"DROP INDEX {index_name} IF EXISTS")

            session.run("MATCH (n) DETACH DELETE n")

    def getMaxItemId(self):
        with self.driver.session() as session:
            result = session.run(
                "MATCH (i:Item) "
                "RETURN max(i.item_id) AS maxId"
            )
            record = result.single()
            max_id = record["maxId"] if record and record["maxId"] is not None else 0
            return int(max_id)

    def create_user(self, user_id: int):
        with self.driver.session() as session:
            session.run(
                "MERGE (u:User {user_id: $id}) ",
                id=user_id,
            )

    def delete_user(self, user_id: int):
        with self.driver.session() as session:
            session.run(
                "MATCH (u:User {user_id: $id})-[r]-() DELETE r",
                id=user_id,
            )
            session.run(
                "MATCH (u:User {user_id: $id}) DELETE u",
                id=user_id,
            )

    def create_tag(self, tag_id: int):
        with self.driver.session() as session:
            session.run(
                "MERGE (t:Tag {tag_id: $id})",
                id=tag_id,
            )

    def delete_tag(self, tag_id: int):
        with self.driver.session() as session:
            session.run(
                "MATCH (t:Tag {tag_id: $id})-[r]-() DELETE r",
                id=tag_id,
            )
            session.run(
                "MATCH (t:Tag {tag_id: $id}) DELETE t",
                id=tag_id,
            )

    def create_item(self, item_id: int, title: str, embedding: List[float], tags: List[int]):
        with self.driver.session() as session:
            session.run(
                "MERGE (i:Item {item_id: $iid}) "
                "SET i.title = $title, i.embedding = $embedding",
                iid=item_id,
                title=title,
                embedding=embedding,
            )

            for tag_id in tags:
                session.run(
                    "MATCH (i:Item {item_id: $iid}), (t:Tag {tag_id: $tid}) "
                    "MERGE (i)-[:IN_TAG]->(t)",
                    iid=item_id,
                    tid=tag_id,
                )

    def delete_item(self, item_id: int):
        with self.driver.session() as session:
            session.run(
                "MATCH (i:Item {item_id: $iid})-[r]-() DELETE r",
                iid=item_id,
            )
            session.run(
                "MATCH (i:Item {item_id: $iid}) DELETE i",
                iid=item_id,
            )

    def create_user_view(self, user_id: int, item_id: int):
        with self.driver.session() as session:
            session.run(
                "MATCH (u:User {user_id: $uid}), (i:Item {item_id: $iid}) "
                "MERGE (u)-[:VIEWED]->(i)",
                uid=user_id,
                iid=item_id,
            )

    def delete_user_view(self, user_id: int, item_id: int):
        with self.driver.session() as session:
            session.run(
                "MATCH (u:User {user_id: $uid})-[r:VIEWED]->(i:Item {item_id: $iid}) "
                "DELETE r",
                uid=user_id,
                iid=item_id,
            )

    def create_user_like(self, user_id: int, item_id: int):
        with self.driver.session() as session:
            session.run(
                "MATCH (u:User {user_id: $uid}), (i:Item {item_id: $iid}) "
                "MERGE (u)-[:LIKED]->(i)",
                uid=user_id,
                iid=item_id,
            )

    def delete_user_like(self, user_id: int, item_id: int):
        with self.driver.session() as session:
            session.run(
                "MATCH (u:User {user_id: $uid})-[r:LIKED]->(i:Item {item_id: $iid}) "
                "DELETE r",
                uid=user_id,
                iid=item_id,
            )

    def recommend_by_item2(self, item_id: int, top_n: int, viewW: float, likeW: float, tagW: float, embW: float):
        res = []
        res_scores = {}
        with self.driver.session() as session:
            result = session.run(
                "MATCH (i:Item {item_id: $iid}) RETURN i.embedding AS emb",
                iid=item_id
            )
            record = result.single()
            if not record or record['emb'] is None:
                return []
            target_emb: List[float] = record['emb']
            target_norm = math.sqrt(sum(x*x for x in target_emb))

            co_results = session.run(
                """
                MATCH (u:User)-[:VIEWED]->(i:Item {item_id: $iid})
                MATCH (u)-[rel]->(r:Item)
                WHERE r.item_id <> $iid AND type(rel) IN [\"VIEWED\", \"LIKED\"]
                WITH r, sum(
                  CASE WHEN type(rel) = \"VIEWED\" THEN $viewW
                       WHEN type(rel) = \"LIKED\"  THEN $likeW
                       ELSE 0 END
                ) AS act_score
                OPTIONAL MATCH (r)-[:IN_TAG]->(tag:Tag)<-[:IN_TAG]-(orig:Item {item_id: $iid})
                WITH r, act_score, count(DISTINCT tag) AS shared_tags
                RETURN r.item_id AS id, act_score AS act_score, shared_tags
                """,
                iid=item_id, viewW=viewW, likeW=likeW
            )

            for rec in co_results:
                rid = int(rec['id'])
                act_score = rec['act_score']
                shared = rec['shared_tags']
                tag_score = (shared * shared) * tagW

                emb_res = session.run(
                    "MATCH (r:Item {item_id: $rid}) RETURN r.embedding AS emb",
                    rid=rid
                ).single()
                emb = emb_res['emb'] if emb_res and emb_res['emb'] else []

                # cosine 유사도
                if emb and target_norm > 0:
                    norm_r = math.sqrt(sum(x*x for x in emb))
                    cos = sum(a*b for a, b in zip(target_emb, emb)) / (target_norm * norm_r) if norm_r>0 else 0
                else:
                    cos = 0
                emb_score = cos * embW

                res_scores[rid] = act_score + tag_score + emb_score

            ranked = sorted(res_scores.items(), key=lambda x: x[1], reverse=True)
            res = [iid for iid, _ in ranked[:top_n]]

        
        return [iid for iid, _ in ranked[:top_n]]


    def recommend_by_item(self, item_id: int, top_n: int, viewW: float, likeW: float, tagW: float, embW: float):
        res = []

        # 1차 해당 작품을 본 유저들이 많이 본 다른 작품
        #
        # 1) Co-activity + 태그 겹침 제곱 가중치 (shared_tags^2 × tagW)
        # 2) 동일 태그 기반 조회 + 좋아요 가중치
        with self.driver.session() as session:
            co_results = session.run(
                """
                MATCH (u:User)-[:VIEWED]->(i:Item {item_id: $iid})
                MATCH (u)-[rel]->(r:Item)
                WHERE r.item_id <> $iid
                  AND type(rel) IN ["VIEWED", "LIKED"]
                WITH r, sum(
                  CASE WHEN type(rel) = "VIEWED" THEN $viewW
                       WHEN type(rel) = "LIKED"  THEN $likeW
                       ELSE 0
                  END
                ) AS act_score
                OPTIONAL MATCH (r)-[:IN_TAG]->(tag:Tag)<-[:IN_TAG]-(orig:Item {item_id: $iid})
                WITH r, act_score, count(DISTINCT tag) AS shared_tags
                WITH r.item_id AS id,
                     (act_score + (shared_tags * shared_tags) * $tagW) AS total_score
                RETURN id, total_score AS score
                ORDER BY score DESC
                LIMIT $limit
                """,
                iid=item_id,
                viewW=viewW,
                likeW=likeW,
                tagW=tagW,
                limit=top_n
            )

            for record in co_results:
                res.append(int(record.get("id")))

            # 2차 (유사 장르의 인기 작품)
            #
            # 1) 1차 추천 결과가 부족하면 동일 태그 기반 조회/좋아요 보충
            # 2) 동일하게 공유 태그 개수에 가중치 제공
            if len(res) < top_n:
                needed = top_n - len(res)
                exclude_ids = res + [item_id]

                fallback_results = session.run(
                    """
                    MATCH (orig:Item {item_id: $iid})-[:IN_TAG]->(tag:Tag)<-[:IN_TAG]-(r:Item)
                    WHERE NOT r.item_id IN $exclude
                    WITH r, count(DISTINCT tag) AS shared_tags
                    OPTIONAL MATCH (u1:User)-[:VIEWED]->(r)
                    WITH r, shared_tags, count(u1) AS vcount
                    OPTIONAL MATCH (u2:User)-[:LIKED]->(r)
                    WITH r, shared_tags, vcount, count(u2) AS lcount
                    RETURN r.item_id AS id,
                           (vcount * $viewW + lcount * $likeW + (shared_tags * shared_tags) * $tagW) AS score
                    ORDER BY score DESC
                    LIMIT $limit
                    """,
                    iid=item_id,
                    exclude=exclude_ids,
                    viewW=viewW,
                    likeW=likeW,
                    tagW=tagW,
                    limit=needed
                )

                for record in fallback_results:
                    res.append(int(record["id"]))

        return res

    def recommend_by_user(self, user_id: int, top_n: int, user_limit: int, viewW: float, likeW: float):
        res = []

        # 1차 "나"와 유사한 취향을 가진 유저들이 본 작품 추천
        #
        # 1) 대상 유저가 본 아이템(seen_ids) 수집
        # 2) 공통 좋아요·조회 기반 유사 유저(sim_users) 상위 user_limit 추출
        # 3) sim_users가 본 아이템 중 unseen 아이템을 simScore * 가중치(viewW/likeW)로 집계
        # 4) 점수를 기준으로 상위 top_n 아이템 선별
        with self.driver.session() as session:
            exclude_ids = session.run(
                """
                MATCH (u:User {user_id: $uid})-[r:VIEWED|LIKED]->(i:Item)
                RETURN collect(DISTINCT i.item_id) AS seenIds
                """,
                uid=user_id
            )
            record = exclude_ids.single()
            seen_ids = record["seenIds"] if record and record["seenIds"] is not None else [
            ]

            sim_records = session.run(
                """
                MATCH (u:User {user_id: $uid})
                OPTIONAL MATCH (u)-[:LIKED]->(i1:Item)<-[:LIKED]-(other:User)
                WITH other, count(i1) AS commonLikes
                OPTIONAL MATCH (u)-[:VIEWED]->(i2:Item)<-[:VIEWED]-(other)
                WITH other, commonLikes, count(i2) AS commonViews
                WITH other, (commonLikes * $likeW + commonViews * $viewW) AS simScore
                WHERE other.user_id <> $uid AND simScore > 0
                RETURN other.user_id AS otherId, simScore
                ORDER BY simScore DESC
                LIMIT $uLimit
                """,
                uid=user_id,
                likeW=likeW,
                viewW=viewW,
                uLimit=user_limit
            )

            sim_users: Dict[int, float] = {}
            for rec in sim_records:
                sim_users[int(rec["otherId"])] = float(rec["simScore"])

        item_scores: Dict[int, float] = {}
        with self.driver.session() as session:
            for other_id, sim_score in sim_users.items():
                viewed_recs = session.run(
                    """
                    MATCH (o:User {user_id: $oid})-[r:VIEWED]->(i:Item)
                    WHERE NOT i.item_id IN $seen
                    RETURN i.item_id AS id
                    """,
                    oid=other_id,
                    seen=seen_ids
                )
                for rec in viewed_recs:
                    iid = int(rec["id"])
                    item_scores[iid] = item_scores.get(
                        iid, 0.0) + sim_score * viewW

                liked_recs = session.run(
                    """
                    MATCH (o:User {user_id: $oid})-[r:LIKED]->(i:Item)
                    WHERE NOT i.item_id IN $seen
                    RETURN i.item_id AS id
                    """,
                    oid=other_id,
                    seen=seen_ids
                )
                for rec in liked_recs:
                    iid = int(rec["id"])
                    item_scores[iid] = item_scores.get(
                        iid, 0.0) + sim_score * likeW

        ranked = sorted(item_scores.items(), key=lambda x: x[1], reverse=True)
        res = [iid for iid, _ in ranked[:top_n]]

        # 2차 (전체 인기 작품)
        #
        # 1) 1차 추천 결과가 부족하면 전체 작품에서 인기 작품 추천
        if len(res) < top_n:
            needed = top_n - len(res)
            exclude_ids = res + seen_ids

            with self.driver.session() as session:
                pop_results = session.run(
                    """
                    MATCH (i:Item)
                    OPTIONAL MATCH (u1:User)-[:VIEWED]->(i)
                    WITH i, count(u1) AS vcount
                    OPTIONAL MATCH (u2:User)-[:LIKED]->(i)
                    WITH i, vcount, count(u2) AS lcount
                    WHERE NOT i.item_id IN $exclude
                    RETURN i.item_id AS id,
                           (vcount * $viewW + lcount * $likeW) AS score
                    ORDER BY score DESC
                    LIMIT $limit
                    """,
                    exclude=exclude_ids,
                    viewW=viewW,
                    likeW=likeW,
                    limit=needed
                )

                for rec in pop_results:
                    res.append(int(rec["id"]))

        return res
