package cauCapstone.openCanvas.websocket;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.stereotype.Component;

import cauCapstone.openCanvas.jwt.JwtTokenizer;
import cauCapstone.openCanvas.jwt.SessionRegistryService;
import io.jsonwebtoken.Claims;

@Slf4j
@RequiredArgsConstructor
@Component
public class StompHandler implements ChannelInterceptor{
	
	private final JwtTokenizer jwtTokenizer;
    private final SessionRegistryService sessionRegistryService;
	
	@Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(message);
        
        String base64EncodedSecretKey = jwtTokenizer.encodeBase64SecretKey();
        
        // websocket 연결시(Stompcommand.CONNECT) 헤더의 jwt token 검증
        if (StompCommand.CONNECT == accessor.getCommand()) {
            Claims claims = jwtTokenizer.verifySignature(accessor.getFirstNativeHeader("token"), base64EncodedSecretKey);
            
            String subject = claims.getSubject();
            
            String sessionId = accessor.getSessionId();
            sessionRegistryService.registerSession(sessionId, subject);
            
        }
        return message;
        
        // TODO: accessor에다가 유저 정보를 넣는 것도 가능하다고한다.
    }
}
