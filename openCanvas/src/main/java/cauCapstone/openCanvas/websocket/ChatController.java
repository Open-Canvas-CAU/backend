package cauCapstone.openCanvas.websocket;

import java.util.List;

import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.stereotype.Controller;

import lombok.RequiredArgsConstructor;

// 클라이언트가 /pub/chat/message로 메시지 발행 요청을 한 것을 /sub/chat/room/{roomId}로 보낸다.
@RequiredArgsConstructor
@Controller
public class ChatController {

    private final SimpMessageSendingOperations messagingTemplate;

    // @MessageMapping을 통해서 웹소켓으로 들어오는 메시지 발행을 처리한다.
    // 클라이언트가 메시지 발행 요청을 할 때에는 prefix를 붙여서 /pub/chat/message로 해야한다.
    @MessageMapping("/chat/message")
    public void message(ChatMessage message) {
        if (ChatMessage.MessageType.JOIN.equals(message.getType()))
            message.setMessage(message.getSender() + "님이 입장하셨습니다.");
        
        // 발행요청한 메시지를 /sub/chat/room/{roomId}로 보낸다.
        // 클라이언트는 /sub/chat/room/{roomId}를 구독하고 있으면 메시지를 전달받는다.
        // /sub/chat/room/{roomId}는 토픽이다.
        messagingTemplate.convertAndSend("/sub/chat/room/" + message.getRoomId(), message);
    }
}