package cauCapstone.openCanvas.websocket;

import java.util.Set;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

// SubscribeRegistryService에서 순환구조 때문에 분리함. 주의.
@Slf4j
@RequiredArgsConstructor
@Service
public class RemoveEditorSubjectService {
    private final RedisTemplate<String, String> redisTemplate;
    private static final String SESSION_PREFIX = "ws:subscribe:";
	private final RedisPublisher redisPublisher;
	private final RemoveChatRoomRepository chatRoomRepository;
	
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
}
