package cauCapstone.openCanvas.websocket;

import java.time.Duration;
import java.util.Set;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

// 문서방을 구독할 때(SUBSCRIBE) sessionId -> roomId와 roomId -> subject를 저장한다.
// 문서방을 구독하는 동안 웹소켓이 재연결된다면 CONNECT -> SUBSCRIBE가 된다. 
@Slf4j
@RequiredArgsConstructor
@Service
public class SubscribeRegistryService {
	
    private final RedisTemplate<String, String> redisTemplate;
    private final SessionRegistryService sessionRegistryService;
    private static final String SESSION_PREFIX = "ws:subscribe:";
	private final RedisPublisher redisPublisher;
	private final ChatRoomRepository chatRoomRepository;
    
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
    // TODO: 내가 편집자라면 '문서방을 닫을 때만' roomId로 된 키에 해당하는 유저정보 셋도 삭제해야한다, 단순 연결이 끊기고 재연결하는 상황에서는 셋을 삭제하면 안된다.
    public void removeSuscribe(String subject) {
    	String key1 = SESSION_PREFIX + "subject:" + subject + ":roomId";
    	String roomId = redisTemplate.opsForValue().get(key1);
    	String key2 = SESSION_PREFIX + "room:" + roomId + ":subject";
    	
        redisTemplate.delete(key1);
        redisTemplate.opsForSet().remove(key2, subject);
    }
    
    // 단순 disconnect상태가 아닌 문서편집자가 문서방을 나갈때 호출해야한다.
    // 유저들은 문서방이 닫혀도 SUSCRIBE 상태를 유지하기 때문에 유저들의 상태를 UNSUSCRIBE로 바꾸라는 메시지를 프론트에 보낸다.
    // 문서방 관련 정보(subject-> roomId, roomId -> subjects, roomId -> editorSubject) 삭제
    public void removeEditorSubject(String subject) {
    	
    	String roomId = getRoomIdBySubject(subject);
    	
    	if(roomId != null) {
        	String editorSubject = getEditorSubjectByRoomId(roomId);
        
        	if(subject.equals(editorSubject)) {
            	Set<String> subjects = getSubjectsByRoomId(roomId);
            	
            	if(subjects!=null) {
                	for(String sub : subjects) {
                    	ChatMessage roomOutMessage = new ChatMessage();
                    	roomOutMessage.setType(ChatMessage.MessageType.ROOMOUT);
                    	roomOutMessage.setRoomId(roomId);
                    	roomOutMessage.setSubject(sub);
                    	roomOutMessage.setMessage(sub+" 유저를 "+roomId+"를 UNSUBSCRIBE 해야함.");

                    	redisPublisher.publish(new ChannelTopic(roomId), roomOutMessage);
                	}
            	}
            	
            	String key1 = SESSION_PREFIX + "subject:" + subject + ":roomId";
            	String key2 = SESSION_PREFIX + "room:" + roomId + ":subject";
            	String key3 = SESSION_PREFIX + "room:" +roomId + ":editorSubject";
            	
                redisTemplate.opsForSet().remove(key2, subject);
                redisTemplate.delete(key3);
                redisTemplate.delete(key1);
            	
                // 채팅방을 ChatRoomRepository에서 삭제한다.
                chatRoomRepository.removeChatRoom(roomId);
        	}else {
        		log.info("subject 인증 실패. editor가 아님.");
        	}
    	}else {
            log.info("subject 인증 실패. roomId없음.");
    	}
    	
    }
}
