package cauCapstone.openCanvas.websocket;

import java.util.List;

import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.stereotype.Controller;

import lombok.RequiredArgsConstructor;

// 클라이언트가 websocket으로 /pub/chat/message로 메시지 발행을 하고, 메시지를 redis로 발행한다.
@RequiredArgsConstructor
@Controller
public class ChatController {

	private final RedisPublisher redisPublisher;
	private final ChatRoomRepository chatRoomRepository;

    // @MessageMapping을 통해서 웹소켓으로 들어오는 메시지 발행을 처리한다.
    // 클라이언트가 메시지 발행 요청을 할 때에는 prefix를 붙여서 /pub/chat/message로 해야한다.
    @MessageMapping("/chat/message")
    public void message(ChatMessage message) {
        if (ChatMessage.MessageType.ENTER.equals(message.getType())) {
            chatRoomRepository.enterChatRoom(message.getRoomId());
            message.setMessage(message.getSender() + "님이 입장하셨습니다.");
        }
        
        //redis로 발행한다.
        redisPublisher.publish(chatRoomRepository.getTopic(message.getRoomId()), message);
    }
}