package cauCapstone.openCanvas.websocket.snapshotdelta;

import java.util.Set;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class SnapshotRDBService {

    private final SnapshotRepository snapshotRepository;
    private final SnapshotRedisService snapshotRedisService;
    private final RedisTemplate<String, String> redisTemplate;

    public void saveSnapshotToDB(String roomId) {
    	// 스냅샷은 1개만 저장하기 때문에 1개가 불러와진다.
        String snapshotJson = snapshotRedisService.getSnapshotFromRedis(roomId);
        if (snapshotJson == null) {
            return;
        }
        
        Long snapshotTimestamp = System.currentTimeMillis();
        
        // TODO: 스냅샷과 델타를 db에 저장할 다른방법 찾기.
        // 일단은 스냅샷과 델타를 RDB에 저장해두고 불러올 땐 같이 불러오도록 한다.
        // 스냅샷과 델타의 포맷은 같고, 스냅샷이 가장 빠른 시간에 저장된 것이므로 시간순으로 불러오도록 한다.
        
        SnapshotEntity snapshot = new SnapshotEntity();
        snapshot.setDocumentId(roomId);
        snapshot.setSnapshotJson(snapshotJson);
        snapshot.setTimestamp(snapshotTimestamp);
        snapshotRepository.save(snapshot);
        
        String deltaKey = "delta:" + roomId;
        Set<ZSetOperations.TypedTuple<String>> allDeltas = redisTemplate.opsForZSet().rangeWithScores(deltaKey, 0, -1);
        
        for (ZSetOperations.TypedTuple<String> delta : allDeltas) {
            String value = delta.getValue();  
            Double score = delta.getScore();  
            
            SnapshotEntity deltaEntity = new SnapshotEntity();
            deltaEntity.setDocumentId(roomId);
            deltaEntity.setSnapshotJson(value);
            deltaEntity.setTimestamp(score.longValue());
            snapshotRepository.save(deltaEntity);
        }
        
        snapshotRedisService.deleteSnapshot(roomId);
        snapshotRedisService.deleteDeltas(roomId);
    }
}
