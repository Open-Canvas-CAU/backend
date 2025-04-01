package cauCapstone.openCanvas.websocket;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.stereotype.Repository;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Repository
public class ChatRoomRepository {
 
    private final RedisMessageListenerContainer redisMessageListener; // 채팅방(topic)에 발행되는 메시지를 수신하는 리스너
    private final RedisSubscriber redisSubscriber; // 구독 처리 서비스
    private static final String CHAT_ROOMS = "CHAT_ROOM";
    private final RedisTemplate<String, Object> redisTemplate; // Redis 데이터 입출력을 위한 template
    private HashOperations<String, String, ChatRoom> opsHashChatRoom; // Redis와 Hash 타입을 다루기위한 객체
    private Map<String, ChannelTopic> topics; // roomId별 Redis pub/sub topic을 저장하는 Map

    // 빈 생성 후 HashOperations 객체와 topic Map 초기화를 한다.
    @PostConstruct
    private void init() {
        opsHashChatRoom = redisTemplate.opsForHash();
        topics = new HashMap<>();
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
    public ChatRoom createChatRoom(String name) {
        ChatRoom chatRoom = ChatRoom.create(name);
        opsHashChatRoom.put(CHAT_ROOMS, chatRoom.getRoomId(), chatRoom);
        return chatRoom;
    }

    // 사용자가 채팅방에 입장할 때 호출한다.
    // roomId별 토픽을 Redis에 등록하고, 리스너를 통해 메시지를 수신하도록 설정한다.
    public void enterChatRoom(String roomId) {
        ChannelTopic topic = topics.get(roomId);
        if (topic == null) {
            topic = new ChannelTopic(roomId);
            redisMessageListener.addMessageListener(redisSubscriber, topic);
            topics.put(roomId, topic);
        }
    }

    public ChannelTopic getTopic(String roomId) {
        return topics.get(roomId);
    }
}