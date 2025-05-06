package cauCapstone.openCanvas.websocket.snapshotdelta;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.RequiredArgsConstructor;

// SnapshotDto(SnapshotRequest)를 redis에 저장하는 서비스
// 문서 편집중에 일정 시간마다 snapshot을 저장하게된다.
@Service
@RequiredArgsConstructor
public class SnapshotRedisService {

    private final RedisTemplate<String, String> redisTemplate;
    private final ObjectMapper objectMapper;

    // snapshot: zadd로 저장한다. {roomId}를 키로, json을 맴버로 timestamp를 스코어로 저장한다.
    // snapshot을 최신 것 1개만 저장하기 위해 roomId가 같은 zset을 비우고 새 snapshot을 저장한다.
    public void saveSnapshotToRedis(SnapshotRequest request) {
        try {
            String key = "snapshot:" + request.getRoomId();
            String json = objectMapper.writeValueAsString(request.getSnapshot());
            redisTemplate.opsForZSet().remove(key);
            redisTemplate.opsForZSet().add(key, json, request.getTimestamp());
            
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Snapshot serialization failed", e);
        }
    }

    public void deleteSnapshot(String roomId) {
        redisTemplate.opsForZSet().remove("snapshot:" + roomId);
    }
    
    public void deleteDeltas(String roomId) {
        redisTemplate.opsForZSet().remove("delta:" + roomId);
    }
    
    // 스냅샷은 1개만 저장되기 때문에 첫번째 원소만 리턴한다.
    public String getSnapshotFromRedis(String roomId) {
        String key = "snapshot:" + roomId;
        return redisTemplate.opsForZSet().range(key, 0, 0).iterator().next();
    }
    
    public void removeOldDeltas(String roomId, long snapshotTimestamp) {
        String deltaKey = "delta:" + roomId;


        // 델타 리스트에서 snapshotTimestamp 이전의 델타들 삭제
        redisTemplate.opsForZSet().removeRangeByScore(deltaKey, Double.MIN_VALUE, snapshotTimestamp);
    }
}
