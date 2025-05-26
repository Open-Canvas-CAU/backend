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

    private static final String SNAPSHOT_KEY = "SNAPSHOT";

    private final RedisTemplate<String, Object> redisTemplate;
    private HashOperations<String, String, SnapshotEntity> hashOps;

    @PostConstruct
    private void init() {
        hashOps = redisTemplate.opsForHash();
    }

    // 스냅샷 저장
    public void saveSnapshot(String roomId, SnapshotEntity snapshot) {
        hashOps.put(SNAPSHOT_KEY, roomId, snapshot);
    }

    // 스냅샷 조회
    public SnapshotEntity getSnapshot(String roomId) {
        return hashOps.get(SNAPSHOT_KEY, roomId);
    }

    // 스냅샷 삭제
    public void deleteSnapshot(String roomId) {
        hashOps.delete(SNAPSHOT_KEY, roomId);
    }

    // 전체 스냅샷 조회 (필요할 경우)
    public List<SnapshotEntity> findAllSnapshots() {
        return hashOps.values(SNAPSHOT_KEY);
    }
}