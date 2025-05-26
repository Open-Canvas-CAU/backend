package cauCapstone.openCanvas.websocket.chatmessage;

// TODO: 5.26 여기서 스냅샷을 찍고있는데 수정하기

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.ObjectMapper;

import cauCapstone.openCanvas.websocket.chatroom.ChatRoomRedisEntity;
import cauCapstone.openCanvas.websocket.chatroom.ChatRoomRepository;
import cauCapstone.openCanvas.websocket.chatroom.SessionRepository;

import java.time.Duration;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

// 채팅방에서 메시지를 작성하면 해당 메시지를 topic에 발행한다.
// 메시지를 발행하면 대기하고 있던 redis 구독 서비스가 메시지를 처리한다.
// 5.4에 세션 id를 파라미터에서 지웠는데 필요없는지 확인해야한다.
@RequiredArgsConstructor
@Service
@Slf4j
public class RedisEditPublisher {
	private final RedisTemplate<String, Object> redisTemplate;
	private final ChatRoomRepository chatRoomRepository;
	private final SessionRepository sessionRegistryService;
	private final ObjectMapper objectMapper;
	
	// 문서편집(chat) 메시지를 publish할때 쓴다.
	// createRoom할때 저장했던 편집자 subject를 roomId를 통해 꺼내고, 
	// 현재 메시지를 보내는 사람의 subject를 sessionId를 통해 가져온다.
	// 락이없다면 편집자 subject와 메시지 발행자 subject를 비교하고 락을 걸고 메시지를 보내고,
	// 락이 있다면 메시지 발행자 subject 검증을 하고 메시지를 보낸다.
	public void editPublish(ChannelTopic topic, ChatMessage message, String sessionId) {
		
        String subject = sessionRegistryService.getSubjectBySessionId(sessionId);
        String roomId = message.getRoomId();
        String lockKey = "lock:document:" + roomId;
        String DELTA_LIST_PREFIX = "delta:";
        
        String lockOwner = (String) redisTemplate.opsForValue().get(lockKey);
		
        if (lockOwner == null) {
            // 락이 없으면, 편집자 검증 후 락 생성
            ChatRoomRedisEntity room = chatRoomRepository.findRoomById(roomId);
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
        
        try {
        	// 여기서 timestamp를 찍음.
        	message.setTimestamp(System.currentTimeMillis());
            // 메시지 발행
            redisTemplate.convertAndSend(topic.getTopic(), message);
            
            // 델타 저장: zadd로 저장한다. {roomId}를 키로, json을 맴버로 timestamp를 스코어로 저장한다.
            // TODO: 델타의 형식에 따라 다르게 파싱해서 저장해야할 수도 있음.
            String key = DELTA_LIST_PREFIX + message.getRoomId();
            if (message.getDelta() != null) {
                String json = objectMapper.writeValueAsString(message.getDelta());
                redisTemplate.opsForZSet().add(key, json, message.getTimestamp());
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to publish or store delta", e);
        }
    }
	
	/*
	// TODO: 테스트용. 삭제하기
	public boolean testEditPublish(ChannelTopic topic, ChatMessage message) {
		String tempEditor = "b";
		
        String roomId = message.getRoomId();
        log.info("message roomId: "+ roomId);
        String key= "ws:subscribe:room:1234:editorSubject";
        
        /*
        Object value = redisTemplate.opsForValue().get(key);
        if (value instanceof String) {
            String editorSubject = (String) value;
            log.info("editorSubject: " + editorSubject);
        } else {
            log.warn("Value is not a String"+value);
        } 
        
        redisTemplate.opsForValue().set(key, "b");
        
        String editorSubject = (String) redisTemplate.opsForValue().get(key);

        log.info("editorSubject: "+editorSubject);
        
        if (!tempEditor.equals(editorSubject)) {
            return false;
        }
        
        return true;
	}
	*/
}