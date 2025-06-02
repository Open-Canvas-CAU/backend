package cauCapstone.openCanvas.websocket.snapshot;

import java.io.Serializable;
import java.time.LocalDateTime;

import lombok.Getter;
import lombok.Setter;

// 편집자가 메시지를 보낼때마다 스냅샷을 저장하는데 필요한 엔티티
@Getter
@Setter
public class SnapshotEntity implements Serializable {

	// Redis에 저장하려면 Serializable해야한다.
	// serialVersionUID를 설정한다.

    private static final long serialVersionUID = 6494678977089006630L;
    
    private String roomId;
    private String body;
    private String num;
    private long time;
    
    public static SnapshotEntity makeSnapshot(String roomId, String body, String num, long time) {
        SnapshotEntity snapshot = new SnapshotEntity();
        snapshot.setRoomId(roomId);
        snapshot.setBody(body);
        snapshot.setNum(num);
        snapshot.setTime(time);
        return snapshot;
    }
}
