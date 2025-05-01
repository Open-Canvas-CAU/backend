package cauCapstone.openCanvas.websocket;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.stereotype.Component;

import cauCapstone.openCanvas.jwt.JwtTokenizer;
import io.jsonwebtoken.Claims;

@Slf4j
@RequiredArgsConstructor
@Component
public class StompHandler implements ChannelInterceptor{
	
	private final JwtTokenizer jwtTokenizer;
    private final SessionRegistryService sessionRegistryService;
    private final SubscribeRegistryService subscribeRegistryService;
	private final RedisPublisher redisPublisher;
	
	@Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(message);
        
        String base64EncodedSecretKey = jwtTokenizer.encodeBase64SecretKey();
        
        // websocket 연결시(Stompcommand.CONNECT) 헤더의 jwt token 검증
        // 토큰을 꺼내서 redis에 sessionId, subject(유저 정보) 저장.
        // 토큰은 connect 상태에서(웹소켓 연결 상태)만 꺼내고 나중에는 유저정보는 sessionId를 이용해 redis에서 참조하도록함.
        if (StompCommand.CONNECT == accessor.getCommand()) {
            Claims claims = jwtTokenizer.verifySignature(accessor.getFirstNativeHeader("token"), base64EncodedSecretKey);
            
            String subject = claims.getSubject();
            
            String sessionId = accessor.getSessionId();
            sessionRegistryService.registerSession(sessionId, subject);
        
        // 문서방 구독시(Stompcommand.SUBSCRIBE) roomId를 추출하고, sessionId, subject, roomId를 저장.
        }else if (StompCommand.SUBSCRIBE == accessor.getCommand()) {
        	String destination = accessor.getDestination();
            String roomId = extractRoomId(destination);
        	
        	String sessionId = accessor.getSessionId();
        	String subject = sessionRegistryService.getSubjectBySessionId(sessionId);
        	
        	subscribeRegistryService.registerSubscribe(roomId, sessionId, subject);
        	
        	// TODO: 메시지 부분은 Service클래스를 따로 만들어서 관리할 수도있다.
        	// StompCommand같은 경우는 StompCommand.SEND가 아니면 메시지가 발행이 안되기 때문에 그 외의 경우엔 메시지 발행을 따로 해준다.
        	ChatMessage updateMessage = new ChatMessage();
        	updateMessage.setType(ChatMessage.MessageType.UPDATE);
        	updateMessage.setRoomId(roomId);
        	updateMessage.setSubject(subject);
        	updateMessage.setMessage(subject+" 유저가 "+roomId+"를 구독함.");

        	redisPublisher.publish(new ChannelTopic(roomId), updateMessage, sessionId);
        	
        	
        // 문서방 구독해제시(StompCommand.UNSUBSCRIBE) sessionId, subject와 연결한 roomId 정보를 삭제.
        }else if (StompCommand.UNSUBSCRIBE == accessor.getCommand()) {
        	String sessionId = accessor.getSessionId();
        	
        	String roomId = subscribeRegistryService.getRoomIdBySessionId(sessionId);
        	String subject = sessionRegistryService.getSubjectBySessionId(sessionId);
        	
        	if(roomId != null && subject != null) {
            	subscribeRegistryService.removeSuscribe(sessionId);
            	
            	// TODO: 메시지 부분은 Service클래스를 따로 만들어서 관리할 수도있다.
            	// StompCommand같은 경우는 StompCommand.SEND가 아니면 메시지가 발행이 안되기 때문에 그 외의 경우엔 메시지 발행을 따로 해준다.
            	ChatMessage updateMessage = new ChatMessage();
            	updateMessage.setType(ChatMessage.MessageType.UPDATE);
            	updateMessage.setRoomId(roomId);
            	updateMessage.setSubject(subject);
            	updateMessage.setMessage(subject+" 유저가 "+roomId+"를 구독 해제함.");

            	redisPublisher.publish(new ChannelTopic(roomId), updateMessage, sessionId);
        	}
        
        	
        // 웹소켓 연결해제시(StompCommand.DISCONNECT)  sessionId, subject와 연결한 roomId 정보를 삭제하고 sessionId와 subject도 삭제.
        }else if (StompCommand.DISCONNECT == accessor.getCommand()) {
        	String sessionId = accessor.getSessionId();
        	
        	String roomId = subscribeRegistryService.getRoomIdBySessionId(sessionId);
        	String subject = sessionRegistryService.getSubjectBySessionId(sessionId);
        	
        	if(roomId != null && subject != null) {
            	subscribeRegistryService.removeSuscribe(sessionId);
            	
            	// TODO: 메시지 부분은 Service클래스를 따로 만들어서 관리할 수도있다.
            	// StompCommand같은 경우는 StompCommand.SEND가 아니면 메시지가 발행이 안되기 때문에 그 외의 경우엔 메시지 발행을 따로 해준다.
            	ChatMessage updateMessage = new ChatMessage();
            	updateMessage.setType(ChatMessage.MessageType.UPDATE);
            	updateMessage.setRoomId(roomId);
            	updateMessage.setSubject(subject);
            	updateMessage.setMessage(subject+" 유저가 "+roomId+"를 구독 해제함.");

            	redisPublisher.publish(new ChannelTopic(roomId), updateMessage, sessionId);
        	}
        	
            sessionRegistryService.removeSession(sessionId);
        }
        
        return message;
        
    }
	
    // /sub/chat/room/{roomId}는 토픽이기 때문에 {roomId}를 추출함.
	private String extractRoomId(String destination) {
	    // 예: /sub/chat/room/1234
	    if (destination == null) return null;

	    String[] parts = destination.split("/");
	    if (parts.length >= 4) {
	        return parts[4]; // roomId 위치
	    }
	    return null;
	}
}
