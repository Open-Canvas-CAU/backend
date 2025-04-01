package cauCapstone.openCanvas.websocket;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import org.springframework.web.socket.WebSocketSession;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

// 채팅방을 위한 dto
// stomp는 메시지 타입 구분과 세션 관리를 하지 않아도 된다.
// stomp는 메시지의 주소(경로)로 메시지를 구분한다.
@Getter
@Setter
public class ChatRoom implements Serializable {
	
	// Redis에 저장하려면 Serializable해야한다.
	// serialVersionUID를 설정한다.

    private static final long serialVersionUID = 6494678977089006639L;

    private String roomId;
    private String name;

    public static ChatRoom create(String name) {
        ChatRoom chatRoom = new ChatRoom();
        chatRoom.roomId = UUID.randomUUID().toString();
        chatRoom.name = name;
        return chatRoom;
    }
}