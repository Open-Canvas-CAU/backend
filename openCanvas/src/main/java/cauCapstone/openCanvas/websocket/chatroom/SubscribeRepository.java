package cauCapstone.openCanvas.websocket.chatroom;

import java.time.Duration;
import java.util.Set;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

// 문서방을 구독할 때(SUBSCRIBE) sessionId -> roomId와 roomId -> subject를 저장한다.
// 문서방을 구독하는 동안 웹소켓이 재연결된다면 CONNECT -> SUBSCRIBE가 된다. 
@Slf4j
@RequiredArgsConstructor
@Service
public class SubscribeRepository {	
	
    private final RedisTemplate<String, String> redisTemplate;
    private static final String SESSION_PREFIX = "ws:subscribe:";
    private static final String DISCONNECT_PREFIX = "disconnect";
    private static final String LOCK_PREFIX = "lock:document:";
    
    // ChatRoomRepository에서 문서방을 만들 때, roomId, editorSubject를 같이 저장해둔다.
    public void registerEditorSubject(String roomId, String subject) {
    	String key = SESSION_PREFIX + "room:" +roomId + ":editorSubject";
    	
        redisTemplate.opsForValue().set(key, subject, Duration.ofDays(1));
    }
    
    // 변경사항: sessionId -> roomId대신 subject -> roomId로 변경
    public void registerSubscribe(String roomId, String sessionId, String subject) {
    	String key1 = SESSION_PREFIX + "room:" + roomId + ":subject";
    	String key2 = SESSION_PREFIX + "subject:" + subject + ":roomId";
    	
        redisTemplate.opsForSet().add(key1, subject);
        redisTemplate.expire(key1, Duration.ofDays(1));
        redisTemplate.opsForValue().set(key2, roomId, Duration.ofDays(1));
    }
    
    // roomId에 있는 유저정보들을(subjects) 반환한다.
    public Set<String> getSubjectsByRoomId(String roomId) {
    	String key= SESSION_PREFIX + "room:" + roomId + ":subject";
        return redisTemplate.opsForSet().members(key);
    }
    
    public String getRoomIdBySubject(String subject) {
    	String key = SESSION_PREFIX + "subject:" + subject + ":roomId";
        return redisTemplate.opsForValue().get(key);
    }
    
    public String getEditorSubjectByRoomId(String roomId) {
    	String key = SESSION_PREFIX +"room:"+ roomId + ":editorSubject";
    	return redisTemplate.opsForValue().get(key);
    }
    
    // DISCONNECT하거나 UNSUSCRIBE할때 호출해서 세션ID, 유저정보를 삭제한다.
    // 내가 편집자라면 '문서방을 닫을 때만' roomId로 된 키에 해당하는 유저정보 셋도 삭제해야한다, 단순 연결이 끊기고 재연결하는 상황에서는 셋을 삭제하면 안된다.
    public void removeSuscribe(String subject) {
    	String key1 = SESSION_PREFIX + "subject:" + subject + ":roomId";
    	String roomId = redisTemplate.opsForValue().get(key1);
    	String key2 = SESSION_PREFIX + "room:" + roomId + ":subject";
    	
        redisTemplate.delete(key1);
        // roomId가 있을 때만, roomId → subject 셋에서 제거
        if (roomId != null) {
            redisTemplate.opsForSet().remove(key2, subject);
        }
    }
    
    public void removeEditorSubjectKey(String roomId) {
        String key = SESSION_PREFIX + "room:" + roomId + ":editorSubject";
        redisTemplate.delete(key);
    }
    
    // disconnect(3분 ttl) 관련 키
    // disconnect 상태 기록 (3분 TTL)
    // 3분동안 안들어오면(DISCONNECT) 기본적으로 의도적으로 나갔다고 판단하고, 
    // 클라이언트도 정상적이지 않다는 것을 인지하는 상황이라 프론트에 알리진 않는다(해당 유저에대한 메시지 보내지않음). 
    
    // disconnect 키 생성 메소드
    private String getDisconnectKey(String roomId, String subject) {
        return DISCONNECT_PREFIX + ":" + roomId + ":" + subject;
    }
    
    // disconnect 키 저장 메소드
    public void makeDisconnectKey(String roomId, String subject) {
        String key = getDisconnectKey(roomId, subject);
        redisTemplate.opsForValue().set(key, "pending", Duration.ofMinutes(3));
    }
    
    // disconnect 키 삭제 메소드
    public void removeDisconnectKey(String roomId, String subject) {
        String key = getDisconnectKey(roomId, subject);
        redisTemplate.delete(key);
    }
    
    // 락 관련 키
    // 편집자가 처음 메시지를 보내면 락이 걸리게 되있다.
    
 // 락 키 생성 메소드
    private String getLockKey(String roomId) {
        return LOCK_PREFIX + roomId;
    }

    // 락 소유자 조회
    public String getLockOwner(String roomId) {
        return redisTemplate.opsForValue().get(getLockKey(roomId));
    }

    // 락 설정 (편집자 subject 기준)
    public void setLock(String roomId, String subject) {
        redisTemplate.opsForValue().set(getLockKey(roomId), subject, Duration.ofMinutes(30));
    }

    // 락 TTL 갱신
    public void extendLock(String roomId) {
        redisTemplate.expire(getLockKey(roomId), Duration.ofMinutes(10));
    }
    

    // 락 삭제
    public void removeLockKey(String roomId) {
        redisTemplate.delete(getLockKey(roomId));
    }
    
}
