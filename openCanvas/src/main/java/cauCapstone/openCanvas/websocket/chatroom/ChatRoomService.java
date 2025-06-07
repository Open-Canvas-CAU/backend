package cauCapstone.openCanvas.websocket.chatroom;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.stereotype.Service;

import cauCapstone.openCanvas.websocket.chatmessage.RedisSubscriber;
import lombok.RequiredArgsConstructor;

//TODO: 문서방을 만들 때 원래 있던 문서를 가져오는 과정이 필요하다.
@RequiredArgsConstructor
@Service
public class ChatRoomService {
    private final RedisMessageListenerContainer redisMessageListener; // 채팅방(topic)에 발행되는 메시지를 수신하는 리스너
    private final RedisSubscriber redisSubscriber; // 구독 처리 서비스
    private final SubscribeRepository subscribeRegistryService;
    private final ChatRoomRepository chatRoomRepository;
    
    // 채팅방 객체는 Redis Hash에 "CHAT_ROOMS"라는 키로 저장된다.
    public ChatRoomRedisEntity createChatRoom(String title, String subject, String version) {
    	
        ChatRoomRedisEntity chatRoom = ChatRoomRedisEntity.create(title, subject, version);
    
    	chatRoomRepository.createRoom(chatRoom);
        
        // 레디스에 roomId, editorSubject 저장.
        subscribeRegistryService.registerEditorSubject(chatRoom.getRoomId(), subject);
        
        // roomId별 토픽을 Redis에 등록하고, 리스너를 통해 메시지를 수신하도록 설정한다.
        // 리스너는 채팅방마다 1개가 필요하다고 한다(사용자별 1개가 아님).
        ChannelTopic topic = new ChannelTopic(chatRoom.getRoomId());
        redisMessageListener.addMessageListener(redisSubscriber, topic);
        
        // 문서방이 만들어지고나서 문서 편집 락을 걸기 때문에, 여기서 락을 걸지 않는다.
        
        return chatRoom;
    }
    
    // 버전을 String화 한다. 2 1 2 이렇게 리스트에다가 싣으면 2.1.2로 리턴한다.
    public String getStringVersion(List<Integer> versionList) {
        return versionList.stream()
                .map(String::valueOf)
                .collect(Collectors.joining("."));
    }
    
    
    public ChatRoomRedisEntity findRoomById(String roomId) {
    	return chatRoomRepository.findRoomById(roomId);
    }
}
