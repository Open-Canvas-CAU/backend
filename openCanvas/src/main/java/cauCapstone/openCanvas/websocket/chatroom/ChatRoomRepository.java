package cauCapstone.openCanvas.websocket.chatroom;

import java.util.List;

import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Repository
public class ChatRoomRepository {
 
    private static final String CHAT_ROOMS = "CHAT_ROOM";
    private static final String LOCK_PREFIX = "lock:doucument:";
    
    private final RedisTemplate<String, Object> redisTemplate; // Redis 데이터 입출력을 위한 template
    private HashOperations<String, String, ChatRoomRedisEntity> opsHashChatRoom; // Redis와 Hash 타입을 다루기위한 객체

    // 빈 생성 후 HashOperations 객체와 topic Map 초기화를 한다.
    @PostConstruct
    private void init() {
        opsHashChatRoom = redisTemplate.opsForHash();
    }

    // Reids의 Hash에서 "CHAT_ROOMS"라는 키에 해당하는 값들을 가져온다.
    // 채팅방 객체는 Redis Hash에 저장된다.
    public List<ChatRoomRedisEntity> findAllRoom() {
        return opsHashChatRoom.values(CHAT_ROOMS);
    }

    public ChatRoomRedisEntity findRoomById(String id) {
        return opsHashChatRoom.get(CHAT_ROOMS, id);
    }
    

    public void createRoom(ChatRoomRedisEntity room) {
        opsHashChatRoom.put(CHAT_ROOMS, room.getRoomId(), room);
    }
    
    public void deleteRoom(String roomId) {
        opsHashChatRoom.delete(CHAT_ROOMS, roomId);
    }
    
    public void deleteLock(String roomId) {
        redisTemplate.delete(LOCK_PREFIX + roomId);
    }
    
    // 5.26 위치 Service로 옮김
    /*
    // 채팅방 객체는 Redis Hash에 "CHAT_ROOMS"라는 키로 저장된다.
    public ChatRoomRedisEntity createChatRoom(String name, String subject) {
        ChatRoomRedisEntity chatRoom = ChatRoomRedisEntity.create(name, subject);
    
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
    */
}