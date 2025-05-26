package cauCapstone.openCanvas.websocket;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.stereotype.Component;

import cauCapstone.openCanvas.jwt.JwtTokenizer;
import cauCapstone.openCanvas.websocket.chatmessage.ChatMessage;
import cauCapstone.openCanvas.websocket.chatmessage.RedisPublisher;
import cauCapstone.openCanvas.websocket.chatroom.SessionRepository;
import cauCapstone.openCanvas.websocket.chatroom.SubscribeRepository;
import io.jsonwebtoken.Claims;

@Slf4j
@RequiredArgsConstructor
@Component
public class StompHandler implements ChannelInterceptor{
	
	private final JwtTokenizer jwtTokenizer;
    private final SessionRepository sessionRegistryService;
    private final SubscribeRepository subscribeRegistryService;
	private final RedisPublisher redisPublisher;
	
	@Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(message);
        
        String base64EncodedSecretKey = jwtTokenizer.encodeBase64SecretKey();
        
        // websocket 연결시(Stompcommand.CONNECT) 헤더의 jwt token 검증
        // 토큰을 꺼내서 redis에 sessionId, subject(유저 정보) 저장.
        // 토큰은 connect 상태에서(웹소켓 연결 상태)만 꺼내고 나중에는 유저정보는 sessionId를 이용해 redis에서 참조하도록함.
        if (StompCommand.CONNECT == accessor.getCommand()) {
        	
        	// TODO: 프론트에서는 bearer 붙이지 않고 전달한다.
            try {
                Claims claims = jwtTokenizer.verifySignature(accessor.getFirstNativeHeader("token"), base64EncodedSecretKey);
                String subject = claims.getSubject();
                String sessionId = accessor.getSessionId();
                sessionRegistryService.registerSession(sessionId, subject);
            } catch (Exception e) {
                log.error("Error while verifying token", e);
            }

        
        // 문서방 구독시(Stompcommand.SUBSCRIBE) roomId를 추출하고, sessionId, subject, roomId를 저장.
        }else if (StompCommand.SUBSCRIBE == accessor.getCommand()) {
        	
        	String destination = accessor.getDestination();
            String roomId = extractRoomId(destination);
        	
        	String sessionId = accessor.getSessionId();
        	String subject = sessionRegistryService.getSubjectBySessionId(sessionId);
        	
        	subscribeRegistryService.registerSubscribe(roomId, sessionId, subject);
        	
        	// StompCommand같은 경우는 StompCommand.SEND가 아니면 메시지가 발행이 안되기 때문에 그 외의 경우엔 메시지 발행을 따로 해준다.
        	ChatMessage updateMessage = new ChatMessage();
        	updateMessage.setType(ChatMessage.MessageType.UPDATE);
        	updateMessage.setRoomId(roomId);
        	updateMessage.setSubject(subject);
        	updateMessage.setMessage(subject+" 유저가 "+roomId+"를 구독함.");
        
        	// 3분 TTL 키에 해당하는 부분을 실제 키가 있던 없던 지우는 동작을 한다.
        	subscribeRegistryService.removeDisconnectKey(roomId, subject);

            log.debug(updateMessage.getMessage()+" 메시지 전송을 시도함.");
            redisPublisher.publish(new ChannelTopic(roomId), updateMessage);

        	
        // 문서방 구독해제시(StompCommand.UNSUBSCRIBE) sessionId, subject와 연결한 roomId 정보를 삭제.
        // TODO: UNSUSCRIBE, DISCONNECT 둘다 문서방을 명시적으로 나간거라 로직이 같아야할 수도 있음.
        }else if (StompCommand.UNSUBSCRIBE == accessor.getCommand()) {
        	String sessionId = accessor.getSessionId();
        	
        	String subject = sessionRegistryService.getSubjectBySessionId(sessionId);
        	String roomId = subscribeRegistryService.getRoomIdBySubject(subject);
        	
        	if(roomId != null && subject != null) {
            	subscribeRegistryService.removeSuscribe(subject);
            	
            	// StompCommand같은 경우는 StompCommand.SEND가 아니면 메시지가 발행이 안되기 때문에 그 외의 경우엔 메시지 발행을 따로 해준다.
            	ChatMessage updateMessage = new ChatMessage();
            	updateMessage.setType(ChatMessage.MessageType.UPDATE);
            	updateMessage.setRoomId(roomId);
            	updateMessage.setSubject(subject);
            	updateMessage.setMessage(subject+" 유저가 "+roomId+"를 구독 해제함.");

            	redisPublisher.publish(new ChannelTopic(roomId), updateMessage);
        	}else {
        		// 문서방이 사라졌을때 동작을 적어야한다.
        		if(subject != null) {
            		subscribeRegistryService.removeSuscribe(subject);
        		}
        	}
        	
            sessionRegistryService.removeSession(sessionId);
        
        	
        // 웹소켓 연결해제시(StompCommand.DISCONNECT)  sessionId, subject와 연결한 roomId 정보를 삭제하고 sessionId와 subject도 삭제.
        // 만약 연결만 끊긴다면 DISCONNECT가 되고 SUBSCRIBE를 다시 해야하지만 UNSUBSCRIBE 상태는 아니다.
        // 이 경우는 프론트에서 DISCONNECT 프레임을 보냈을 때 이다(예기치 않게 끊긴 경우가 포함되지 않음).
        }else if (StompCommand.DISCONNECT == accessor.getCommand()) {
        	String sessionId = accessor.getSessionId();
        	
        	String subject = sessionRegistryService.getSubjectBySessionId(sessionId);
        	String roomId = subscribeRegistryService.getRoomIdBySubject(subject);
        	
        	if(roomId != null && subject != null) {
                // TODO: 여기 이상있나 테스트해보기
                // 세션id가 바뀔거기때문에 subscribeRegistryService 관련 내용도 삭제한다.
            	subscribeRegistryService.removeSuscribe(subject);
            	
            	// StompCommand같은 경우는 StompCommand.SEND가 아니면 메시지가 발행이 안되기 때문에 그 외의 경우엔 메시지 발행을 따로 해준다.
            	ChatMessage updateMessage = new ChatMessage();
            	updateMessage.setType(ChatMessage.MessageType.UPDATE);
            	updateMessage.setRoomId(roomId);
            	updateMessage.setSubject(subject);
            	updateMessage.setMessage(subject+" 유저가 "+roomId+"를 구독 해제함.");

            	redisPublisher.publish(new ChannelTopic(roomId), updateMessage);
        	}else {
        		// 문서방이 사라졌을때 동작을 적어야한다.
        		if(subject != null) {
            		subscribeRegistryService.removeSuscribe(subject);
        		}
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
