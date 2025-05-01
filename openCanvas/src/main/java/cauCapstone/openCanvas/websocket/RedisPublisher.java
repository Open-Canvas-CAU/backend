package cauCapstone.openCanvas.websocket;


import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import java.time.Duration;

import lombok.RequiredArgsConstructor;

// 채팅방에서 메시지를 작성하면 해당 메시지를 topic에 발행한다.
// 메시지를 발행하면 대기하고 있던 redis 구독 서비스가 메시지를 처리한다.
@RequiredArgsConstructor
@Service
public class RedisPublisher {
	private final RedisTemplate<String, Object> redisTemplate;
	private final ChatRoomRepository chatRoomRepository;
	private final SessionRegistryService sessionRegistryService;
 
	// 문서편집(chat) 메시지를 publish할때 쓴다.
	// createRoom할때 저장했던 편집자 subject를 roomId를 통해 꺼내고, 
	// 현재 메시지를 보내는 사람의 subject를 sessionId를 통해 가져온다.
	// 락이없다면 편집자 subject와 메시지 발행자 subject를 비교하고 락을 걸고 메시지를 보내고,
	// 락이 있다면 메시지 발행자 subject 검증을 하고 메시지를 보낸다.
	public void editPublish(ChannelTopic topic, ChatMessage message, String sessionId) {
		
        String subject = sessionRegistryService.getSubjectBySessionId(sessionId);
        String roomId = message.getRoomId();
        String lockKey = "lock:document:" + roomId;
        
        String lockOwner = (String) redisTemplate.opsForValue().get(lockKey);
		
        if (lockOwner == null) {
            // 락이 없으면, 편집자 검증 후 락 생성
            ChatRoom room = chatRoomRepository.findRoomById(roomId);
            String editorSubject = room.getSubject();

            if (!subject.equals(editorSubject)) {
                throw new AccessDeniedException("편집자만 락을 생성할 수 있습니다.");
            }

            // 락 설정 (30분 TTL)
            redisTemplate.opsForValue().set(lockKey, subject);
            redisTemplate.expire(lockKey, Duration.ofMinutes(30));

        } else {
            // 락이 이미 있을 경우, 본인이 편집자인지 확인
            if (!lockOwner.equals(subject)) {
                throw new AccessDeniedException("다른 사용자가 현재 편집 중입니다.");
            }

            // TTL 연장 (편집 중)
            redisTemplate.expire(lockKey, Duration.ofMinutes(5));
        }

        // 메시지 발행
        redisTemplate.convertAndSend(topic.getTopic(), message);
    }
	
	public void updatePublish(ChannelTopic topic, ChatMessage message, String sessionId) {
        redisTemplate.convertAndSend(topic.getTopic(), message);
	}
	
	public void publish(ChannelTopic topic, ChatMessage message, String sessionId) {
        redisTemplate.convertAndSend(topic.getTopic(), message);
	}
	
}