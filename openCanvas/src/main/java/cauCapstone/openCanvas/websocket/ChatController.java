package cauCapstone.openCanvas.websocket;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Controller;
import org.springframework.messaging.Message;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

// 클라이언트가 websocket으로 /pub/chat/message로 메시지 발행을 하고, 메시지를 redis로 발행한다.
@RequiredArgsConstructor
@Controller
@Slf4j
public class ChatController {

	private final RedisEditPublisher redisPublisher;
	private final RedisPublisher tempPublisher;
	
	// TODO: 테스트용 지워야함.
    private final RedisMessageListenerContainer redisMessageListener; 
    private final RedisSubscriber redisSubscriber; // 구독 처리 서비스

    // @MessageMapping을 통해서 웹소켓으로 들어오는 메시지 발행을 처리한다.
    // 클라이언트가 메시지 발행 요청을 할 때에는 prefix를 붙여서 /pub/chat/message로 해야한다.
    @MessageMapping("/chat/message")
    public void message(Message<?> rawMessage, ChatMessage message) {
    	
    	// 메시지를 발행할때 메시지를 발행하는 유저의 세션id를 얻는다.
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(rawMessage);
        String sessionId = accessor.getSessionId();

        if(ChatMessage.MessageType.EDIT.equals(message.getType())) {
            // 문서편집, 작성을 할 때는 messageType를 EDIT으로 지정해야한
            // 발행요청한 메시지를 /sub/chat/room/{roomId}로 보낸다.
            // 클라이언트는 /sub/chat/room/{roomId}를 구독하고 있으면 메시지를 전달받는다.
            // /sub/chat/room/{roomId}는 토픽이다.
            // redis에서는 session id를 통해 메시지를 보내는 사람이 편집권한이 있는지 확인한다.
            redisPublisher.editPublish(new ChannelTopic(message.getRoomId()), message, sessionId);
        }else {
        	log.info("메시지타입확인후 else실행.");
            // 클라이언트가 보낸 메시지인데 EDIT가 아니면 위조 의심 → 차단
        	// subscribe/ unsubscribe 상황에서는 ChatController에서 메시지를 보내는 것이 아닌 직접 보내는거라 EDIT 외엔 오류라고 판단한다.
        	
        	/*
        	// TODO: 테스트용. 지워야함 
        	
        	boolean isEditor = redisPublisher.testEditPublish(new ChannelTopic(message.getRoomId()), message);
        	
        	log.info("isEditor: "+isEditor);
        	
        	if(isEditor) {
                redisMessageListener.addMessageListener(redisSubscriber, new ChannelTopic(message.getRoomId()));  
            	tempPublisher.publish(new ChannelTopic(message.getRoomId()), message);
        	}
        	*/
        	
        	// TODO: 테스트용. 주석을 지우기.
            throw new IllegalArgumentException("허용되지 않은 메시지 타입");
        }
     
    }
}