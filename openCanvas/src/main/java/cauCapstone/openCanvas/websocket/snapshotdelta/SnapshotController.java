package cauCapstone.openCanvas.websocket.snapshotdelta;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;

// 문서편집중에 스냅샷을 redis에 저장할 때 쓰는 컨트롤러.
// 프론트에서 주기적으로 호출한다.
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/snapshot")
public class SnapshotController {

    private final SnapshotRedisService snapshotService;

    @PostMapping
    public ResponseEntity<Void> saveSnapshot(@RequestBody SnapshotRequest request) {
    	
        snapshotService.saveSnapshotToRedis(request);
        
        snapshotService.removeOldDeltas(request.getRoomId(), request.getTimestamp());
        return ResponseEntity.ok().build();
    }
}