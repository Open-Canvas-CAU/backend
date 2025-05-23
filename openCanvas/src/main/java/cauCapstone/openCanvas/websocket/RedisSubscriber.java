package cauCapstone.openCanvas.websocket;

import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

// redis에 저장된 메시지를 가져와서 채팅방을 구독한 클라이언트에게 전송한다.
@Slf4j
@RequiredArgsConstructor
@Service
public class RedisSubscriber implements MessageListener {

    private final ObjectMapper objectMapper;
    private final RedisTemplate<String, Object> redisTemplate;
    private final SimpMessageSendingOperations messagingTemplate;


    @Override
    public void onMessage(Message message, byte[] pattern) {
        try {
            log.info("redissubscriber 실행");
            // redisTemplate에서 메시지를 얻고, 역직렬화 한다.
            String publishMessage = (String) redisTemplate.getStringSerializer().deserialize(message.getBody());
            // publishMessage를 ChatMessage 클래스로 매핑한다.
            ChatMessage roomMessage = objectMapper.readValue(publishMessage, ChatMessage.class);
            // ChatMessage 클래스 객체인 roomMessage를 채팅방을 구독자 즉 /sub/chat/room/{roomId}로 전송한다.
            // /sub/chat/room/{roomId}는 토픽이다.
            log.info("Sending message to room: " + roomMessage.getRoomId());
            messagingTemplate.convertAndSend("/sub/chat/room/" + roomMessage.getRoomId(), roomMessage);
        } catch (Exception e) {	
            log.error(e.getMessage());
        }
    }
}
