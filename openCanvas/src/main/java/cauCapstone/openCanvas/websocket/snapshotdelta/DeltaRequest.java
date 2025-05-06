package cauCapstone.openCanvas.websocket.snapshotdelta;

import java.util.Map;
import lombok.Data;

// redis에서 delta를 가져올 때 쓰는 dto.
@Data
public class DeltaRequest {
	
    private Long timestamp;
    private Map<String, Object> delta;
    
}