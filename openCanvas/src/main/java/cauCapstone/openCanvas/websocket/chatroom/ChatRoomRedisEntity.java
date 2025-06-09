package cauCapstone.openCanvas.websocket.chatroom;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import org.springframework.web.socket.WebSocketSession;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

// 채팅방의 해시를 위한 Entity
// stomp는 메시지 타입 구분과 세션 관리를 하지 않아도 된다.
// stomp는 메시지의 주소(경로)로 메시지를 구분한다.
// 편집자의 유저정보도 저장해서 권한있는 사람을 알기쉽게한다.
@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
public class ChatRoomRedisEntity implements Serializable {
	
	// Redis에 저장하려면 Serializable해야한다.
	// serialVersionUID를 설정한다.

    private static final long serialVersionUID = 6494678977089006639L;

    private String roomId;
    private String name;
    private String subject;
    private String version;		// 현재 Writing 버전을 string화 해서 넣음. TODO: .으로 구분해서 파싱해야함

    public static ChatRoomRedisEntity create(String name, String subject, String version) {
        ChatRoomRedisEntity chatRoom = new ChatRoomRedisEntity();
        chatRoom.roomId = UUID.randomUUID().toString();
        chatRoom.name = name;
        chatRoom.subject = subject;
        chatRoom.version = version;
        return chatRoom;
    }
}