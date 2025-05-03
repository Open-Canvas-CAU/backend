package cauCapstone.openCanvas.websocket;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

import org.springframework.data.redis.core.RedisTemplate;
import java.time.Duration;

// 3분동안 DISCONNECT 상태를 3분 유지한다면 그 유저는 문서방을 나간 것으로 간주함.
// 그 때 문서방을 나간 유저가 편집자인지, 일반 유저인지 따라 달라짐.
// 이 클래스는 오직 리스너를 통해 DISCONNECT 상태를 감지하면 3분동안 TTL을 주는 키를 레디스에 저장하는 역할만 한다.
// TODO: 문서방에 다시 재구독을 했을때(StompHandler의 SUBSCRIBE) 키를 삭제해줘야함.
@Slf4j
@RequiredArgsConstructor
@Component
public class WebSocketEventListener {

    private final RedisTemplate<String, String> redisTemplate;
    private final SessionRegistryService sessionRegistryService;
    private final SubscribeRegistryService subscribeRegistryService;

    @EventListener
    public void handleSessionDisconnect(SessionDisconnectEvent event) {
        String sessionId = event.getSessionId();
        
        // TODO: SubscribeRegistryService 관련코드 여기다가 작성하기.
        // TODO: 리퍼지토리에서 문서방 삭제하기

        // 세션 ID로부터 roomId와 userId(subject)를 알아낸다.
        String subject = sessionRegistryService.getSubjectBySessionId(sessionId);
        String roomId =  subscribeRegistryService.getRoomIdBySubject(subject);

        if (roomId != null && subject != null) {
            log.info("DISCONNECT 발생: session={}, room={}, user={}", sessionId, roomId, subject);

            // disconnect 상태 기록 (3분 TTL)
            // TODO: 이것도 registryService 클래스로 등록하는게 나을수도있다.
            // 3분동안 안들어오면(DISCONNECT) 기본적으로 의도적으로 나갔다고 판단하고, 클라이언트도 정상적이지 않다는 것을 인지하는 상황이라 프론트에 알리진 않는다(해당 유저에대한 메시지 보내지않음).        
            String disconnectKey = "disconnect:" + roomId + ":" + subject;
            redisTemplate.opsForValue().set(disconnectKey, "pending", Duration.ofMinutes(3));
            
        }
        
    }
}
