package cauCapstone.openCanvas.websocket.chatmessage;

import java.time.LocalDateTime;

import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

// 채팅 메세지를 주고받기 위한 dto
// 백엔드에서 메시지를 보내야할때는(subscribe/unsubscribe와 같은 방에있는 유저들의 변경) 지정해주고 보내야한다.
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Tag(name = "WebSocket 메시지 포맷 dto", description = """
		프론트에서 메시지를 전송할 때는 type, roomId, message(메시지 내용), block(현재 블록)을 지정해주세요. 
		type = "EDIT"으로 해주세요.
		
		서버에서 프론트로 메시지를 응답할 때는
		type, roomId, message, subject(해당 유저 email)이 전송됩니다.
		
[서버 → 클라이언트 메시지 설명]

{
  "type": "UPDATE | EDIT | ROOMOUT",
  "roomId": "문서방 ID",
  "subject": "보낸 사람",
  "message": "내용"
  "block": "현재 문서작성하고 있는 블럭"
}

		type = "ROOMOUT"인 경우는 편집자가 문서방을 나갔을 때를 의미합니다, 
		이 메시지를 받은 유저는 웹소켓 구독 해제를 하고, 연결을 끊으면 됩니다. 그리고 /api/room/exit로 컨트롤러를 호출해주세요.
""")
public class ChatMessage {
    // 메시지 타입 : UPDATE(유저변경),EDIT(문서작성), ROOMOUT(편집자가 나가서 기존 유저도 나가야함)
    public enum MessageType {
        UPDATE, EDIT, ROOMOUT
    }
    
    private MessageType type; // 메시지 타입
    private String roomId; // 방번호
    private String subject; // 메시지 보낸사람
    @Schema(description = "해당 블럭 내용")
    private String message; // 일반 String 메시지, String 메시지가 필요하다면 이것을 사용한다
    @Schema(description = "문서 내 블럭 번호", example = "3")
    private String num;	// 블럭넘버
    @Schema(description = "메시지 전송 시간인데 안씀")
    private long timestamp;
    
    public ChatMessage(String roomId, MessageType type, String message) {
        this.roomId = roomId;
        this.type = type;
        this.message = message;
    }
    
    public ChatMessage(String roomId, MessageType type, String message, long timestamp) {
        this.roomId = roomId;
        this.type = type;
        this.message = message;
        this.timestamp = timestamp;
    }
    
    public ChatMessage(String roomId, MessageType type, String subject, String num) {
        this.roomId = roomId;
        this.type = type;
        this.subject = subject;
        this.message = num;
    }
}

// subscribe/ unsubscribe에서 흐름은 백엔드에서 메시지를 보냄 -> 메시지타입을 프론트에서 받음 -> 
// 유저정보목록 호출 -> 백엔드에서 유저정보 목록을 가져옴.