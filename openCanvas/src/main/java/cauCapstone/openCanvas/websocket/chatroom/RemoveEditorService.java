package cauCapstone.openCanvas.websocket.chatroom;

import java.util.Set;

import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.stereotype.Service;

import cauCapstone.openCanvas.websocket.chatmessage.ChatMessage;
import cauCapstone.openCanvas.websocket.chatmessage.RedisPublisher;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

// SubscribeRegistryService에서 순환구조 때문에 분리함. 주의.
@Slf4j
@RequiredArgsConstructor
@Service
public class RemoveEditorService {
	
	private final RedisPublisher redisPublisher;
	private final SubscribeRepository subscribeRepository;
	
	// 이걸로 편집자인지 판단하고 sanpshotService.saveSnapshotToDB() 하고 removeEditorSubject()하면됨.
    public boolean isEditor(String subject) {
    	String roomId = subscribeRepository.getRoomIdBySubject(subject);
    	
    	if(roomId != null) {
        	String editorSubject = subscribeRepository.getEditorSubjectByRoomId(roomId);
        
        	if(subject.equals(editorSubject)) {
        		return true;
        		}
        	
        	}
    	
    	return false;
    }
	
    // 단순 disconnect상태가 아닌 문서편집자가 문서방을 나갈때 호출해야한다.
    // 유저들은 문서방이 닫혀도 SUSCRIBE 상태를 유지하기 때문에 유저들의 상태를 UNSUSCRIBE로 바꾸라는 메시지를 프론트에 보낸다.
    // 문서방 관련 정보(subject-> roomId, roomId -> subjects, roomId -> editorSubject) 삭제
    public String removeEditorSubject(String subject) {
    	
    	String roomId = subscribeRepository.getRoomIdBySubject(subject);
    	
    	if(roomId != null) {
        	String editorSubject = subscribeRepository.getEditorSubjectByRoomId(roomId);
        
        	if(subject.equals(editorSubject)) {
            	Set<String> subjects = subscribeRepository.getSubjectsByRoomId(roomId);
            	
                sendROOMOUTmessage(subjects, roomId);
            	
                // 구독 정보 삭제
                subscribeRepository.removeSuscribe(subject); // subject -> roomId, roomId -> subject
                subscribeRepository.removeEditorSubjectKey(roomId); // roomId -> editorSubject
                
                // 락키를 삭제한다.
            	subscribeRepository.removeLockKey(roomId);
            	
            	return roomId;
        	}else {
        		log.info("subject 인증 실패. editor가 아님.");
       
        	}
    	}else {
            log.info("subject 인증 실패. roomId없음.");

    	}
    	
    	return null;
    }
    
    @Schema(description = "프론트에서는 messageType.ROOMOUT인 메시지가 온다면 구독 해제, 웹소켓 연결을 끊으면 됩니다.")
    // messageType가 ROOMOUT인 메시지를 보내고 받은 유저는 문서방이 닫혔으므로 구독 해제, 웹소켓 연결을 끊어야함.
    public void sendROOMOUTmessage(Set<String> subjects, String roomId) {
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
    }
    
}
