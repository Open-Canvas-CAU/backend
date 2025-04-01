package cauCapstone.openCanvas.websocket;

import lombok.Getter;
import lombok.Setter;

// 채팅 메세지를 주고받기 위한 dto
@Getter
@Setter
public class ChatMessage {
    // 메시지 타입 : JOIN(채팅방 입장),TALK(대화하기)
    public enum MessageType {
        ENTER, TALK
    }
    private MessageType type; // 메시지 타입
    private String roomId; // 방번호
    private String sender; // 메시지 보낸사람
    private String message; // 메시지
}