package cauCapstone.openCanvas.websocket.snapshot;

import java.util.List;

import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;

//SnapshotEntity를 redis에 저장하는 리파지토리
//문서 편집중에 일정 시간마다 snapshot을 저장하게된다.
@RequiredArgsConstructor
@Repository
public class SnapshotRepository {

    private static final String SNAPSHOT_KEY = "SNAPSHOT:";

    private final RedisTemplate<String, Object> redisTemplate;
    private HashOperations<String, String, SnapshotEntity> hashOps;

    @PostConstruct
    private void init() {
        hashOps = redisTemplate.opsForHash();
    }

    // 블록 하나 저장: "SNAPSHOT:{roomId}" -> num -> snapshot
    public void saveSnapshot(String roomId, SnapshotEntity snapshot) {
        hashOps.put(SNAPSHOT_KEY + roomId, snapshot.getNum(), snapshot);
    }
    
    // 블록 하나 조회
    public SnapshotEntity getSnapshot(String roomId, String num) {
        return hashOps.get(SNAPSHOT_KEY + roomId, num);
    }

    // 해당 문서방(roomId)의 전체 블록 조회
    public List<SnapshotEntity> getAllSnapshots(String roomId) {
        return hashOps.values(SNAPSHOT_KEY + roomId);
    }

    // 특정 블록 삭제
    public void deleteSnapshot(String roomId, String num) {
        hashOps.delete(SNAPSHOT_KEY + roomId, num);
    }


    // 문서방의 모든 스냅샷 삭제
    public void deleteAllSnapshots(String roomId) {
        redisTemplate.delete(SNAPSHOT_KEY + roomId);
    }
}