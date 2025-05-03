package cauCapstone.openCanvas.websocket;

import java.util.List;

import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.stereotype.Repository;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;

// TODO: 문서방을 만들 때 원래 있던 문서를 가져오는 과정이 필요하다.
// TODO: 문서방을 만들 때 동시적으로 여러 유저가 문서방을 만드는 것을 막는 락이 필요하다.

@RequiredArgsConstructor
@Repository
public class ChatRoomRepository {
 
    private final RedisMessageListenerContainer redisMessageListener; // 채팅방(topic)에 발행되는 메시지를 수신하는 리스너
    private final RedisSubscriber redisSubscriber; // 구독 처리 서비스
    private static final String CHAT_ROOMS = "CHAT_ROOM";
    private final RedisTemplate<String, Object> redisTemplate; // Redis 데이터 입출력을 위한 template
    private HashOperations<String, String, ChatRoom> opsHashChatRoom; // Redis와 Hash 타입을 다루기위한 객체
    private final SubscribeRegistryService subscribeRegistryService;
    private final SessionRegistryService sessionRegistryService;

    // 빈 생성 후 HashOperations 객체와 topic Map 초기화를 한다.
    @PostConstruct
    private void init() {
        opsHashChatRoom = redisTemplate.opsForHash();
    }

    // Reids의 Hash에서 "CHAT_ROOMS"라는 키에 해당하는 값들을 가져온다.
    // 채팅방 객체는 Redis Hash에 저장된다.
    public List<ChatRoom> findAllRoom() {
        return opsHashChatRoom.values(CHAT_ROOMS);
    }

    public ChatRoom findRoomById(String id) {
        return opsHashChatRoom.get(CHAT_ROOMS, id);
    }

    // 채팅방 객체는 Redis Hash에 "CHAT_ROOMS"라는 키로 저장된다.
    public ChatRoom createChatRoom(String name, String subject) {
        ChatRoom chatRoom = ChatRoom.create(name, subject);
    
        opsHashChatRoom.put(CHAT_ROOMS, chatRoom.getRoomId(), chatRoom);
        
        // 레디스에 roomId, editorSubject 저장.
        subscribeRegistryService.registerEditorSubject(chatRoom.getRoomId(), subject);
        
        // roomId별 토픽을 Redis에 등록하고, 리스너를 통해 메시지를 수신하도록 설정한다.
        // 리스너는 채팅방마다 1개가 필요하다고 한다(사용자별 1개가 아님).
        ChannelTopic topic = new ChannelTopic(chatRoom.getRoomId());
        redisMessageListener.addMessageListener(redisSubscriber, topic);
        
        // 문서 편집 락 설정
        // 문서방이 만들어지고나서 문서 편집 락을 거는 것이다.
        // TODO: 락을 RedisPublisher에서 거는 것같은데 참조하기.
        String lockKey = "lock:doucument:" + chatRoom.getRoomId();
        
        return chatRoom;
    }
    
    // 문서방을 삭제하는 메소드이다. 
    // 문서방은 두가지 조건에서 삭제된다. 1. 클라이언트가 문서방 닫기 버튼을 누르면(프론트), 2. 클라이언트가 연결이 끊기고 3분동안 재연결이 없을 때(백)
    // 관련 메소드, 클래스: WebSocketListener, SubscribeRegistryService.removeEditorSubject
    public void removeChatRoom(String roomId) {
        // 1. Redis Hash에서 채팅방 정보 제거
        opsHashChatRoom.delete(CHAT_ROOMS, roomId);

        // 2. 메시지 리스너에서 구독 해제
        ChannelTopic topic = new ChannelTopic(roomId);
        redisMessageListener.removeMessageListener(redisSubscriber, topic);

        // 3. Redis 락 정보 제거
        String lockKey = "lock:doucument:" + roomId;
        redisTemplate.delete(lockKey);
    }
}