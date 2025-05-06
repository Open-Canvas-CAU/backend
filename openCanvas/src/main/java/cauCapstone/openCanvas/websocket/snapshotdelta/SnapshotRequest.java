package cauCapstone.openCanvas.websocket.snapshotdelta;

import java.util.Map;
import lombok.Data;

// 문서의 델타함수들의 snapshot을 저장할 dto
// roomId, snapshot객체, timestamp를 전달받아야한다.
@Data
public class SnapshotRequest {
    private String roomId;
    private Map<String, Object> snapshot; 
    private Long timestamp;
}