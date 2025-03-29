package cauCapstone.openCanvas.websocket;

import java.util.HashSet;
import java.util.Set;

import org.springframework.web.socket.WebSocketSession;

import lombok.Builder;
import lombok.Getter;

// 채팅방을 위한 dto
@Getter
public class ChatRoom {
    private String roomId;
    private String name;
    // 클라이언트들의 정보를 담는 session들의 set인 sessions
    private Set<WebSocketSession> sessions = new HashSet<>();

    @Builder
    public ChatRoom(String roomId, String name) {
        this.roomId = roomId;
        this.name = name;
    }
    
    // messageType로 채팅방 입장(ENTER) 인지 대화하기(TALK)인지 구분해서 처리한다.
    public void handleActions(WebSocketSession session, ChatMessage chatMessage, ChatService chatService) {
    	// messageType가 ENTER면 session을 sessions에 추가하고, 입장메세지를 모든 session에 보낸다.
        if (chatMessage.getType().equals(ChatMessage.MessageType.ENTER)) {
            sessions.add(session);
            chatMessage.setMessage(chatMessage.getSender() + "님이 입장했습니다.");
        }
        sendMessage(chatMessage, chatService);
    }
    
    // 채팅창의 모든 session에 메세지를 발송한다.
    public <T> void sendMessage(T message, ChatService chatService) {
        sessions.parallelStream().forEach(session -> chatService.sendMessage(session, message));
    }
}
