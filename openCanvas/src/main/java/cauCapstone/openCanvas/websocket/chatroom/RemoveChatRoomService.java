package cauCapstone.openCanvas.websocket.chatroom;

import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.stereotype.Repository;

import cauCapstone.openCanvas.rdb.entity.Cover;
import cauCapstone.openCanvas.rdb.entity.RoomType;
import cauCapstone.openCanvas.rdb.repository.CoverRepository;
import cauCapstone.openCanvas.websocket.chatmessage.RedisSubscriber;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Repository
public class RemoveChatRoomService {
    private final ChatRoomRepository chatRoomRepository;
    private final RedisMessageListenerContainer redisMessageListener; // 채팅방(topic)에 발행되는 메시지를 수신하는 리스너
    private final RedisSubscriber redisSubscriber; // 구독 처리 서비스
    private final CoverRepository coverRepository;
	
    // 문서방을 삭제하는 메소드이다. 먼저 RemoveEditorService의 메소드가 실행되야함.
    // 문서방은 두가지 조건에서 삭제된다. 1. 클라이언트가 문서방 닫기 버튼을 누르면(프론트), 2. 클라이언트가 연결이 끊기고 3분동안 재연결이 없을 때(백)
    public void removeChatRoom(String roomId) {
    	
    	if(roomId != null) {
        	
            // 1. Redis Hash에서 채팅방 정보 제거
            chatRoomRepository.deleteRoom(roomId);

            // 2. 메시지 리스너에서 구독 해제
            ChannelTopic topic = new ChannelTopic(roomId);
            redisMessageListener.removeMessageListener(redisSubscriber, topic);	
            
            // 5. Cover 존재 확인
            Cover cover = coverRepository.findByRoomId(roomId)
                .orElseThrow(() -> new IllegalArgumentException("해당 제목의 Cover가 존재하지 않습니다."));

            // 6. roomType, roomId 설정 후 저장
            cover.setRoomType(RoomType.AVAILABLE); // RoomType이 enum이면 EDITING으로 설정
            cover.setRoomId(null);
            coverRepository.save(cover);
    	}
    }
}