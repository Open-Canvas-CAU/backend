package cauCapstone.openCanvas.websocket;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import org.springframework.web.socket.WebSocketSession;

import lombok.Builder;
import lombok.Getter;

// 채팅방을 위한 dto
// stomp는 메시지 타입 구분과 세션 관리를 하지 않아도 된다.
// stomp는 메시지의 주소(경로)로 메시지를 구분한다.
@Getter
public class ChatRoom {
    private String roomId;
    private String name;

    public static ChatRoom create(String name) {
        ChatRoom chatRoom = new ChatRoom();
        chatRoom.roomId = UUID.randomUUID().toString();
        chatRoom.name = name;
        return chatRoom;
    }
}
