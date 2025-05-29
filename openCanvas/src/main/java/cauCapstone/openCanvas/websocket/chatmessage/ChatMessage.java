package cauCapstone.openCanvas.websocket.chatmessage;

import java.time.LocalDateTime;

import lombok.Getter;
import lombok.Setter;

// 채팅 메세지를 주고받기 위한 dto
// 통상적인 문서작성에선 프론트에서 type, roomId, subject, message를 지정해서 전달해줘야한다.
// 백엔드에서 메시지를 보내야할때는(subscribe/unsubscribe와 같은 방에있는 유저들의 변경) 지정해주고 보내야한다.
@Getter
@Setter
public class ChatMessage {
    // 메시지 타입 : UPDATE(유저변경),EDIT(문서작성), ROOMOUT(편집자가 나가서 기존 유저도 나가야함)
    public enum MessageType {
        UPDATE, EDIT, ROOMOUT
    }
    
    private MessageType type; // 메시지 타입
    private String roomId; // 방번호
    private String subject; // 메시지 보낸사람
    private String message; // 일반 String 메시지, String 메시지가 필요하다면 이것을 사용한다.
    
    private long timestamp;
}

// subscribe/ unsubscribe에서 흐름은 백엔드에서 메시지를 보냄 -> 메시지타입을 프론트에서 받음 -> 
// 유저정보목록 호출 -> 백엔드에서 유저정보 목록을 가져옴.