package cauCapstone.openCanvas.websocket;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Repository;

import jakarta.annotation.PostConstruct;

// 채팅방을 만들고, 조회한다.
@Repository
public class ChatRoomRepository {

    private Map<String, ChatRoom> chatRoomMap;

    // TODO: 채팅방을 현재 hashMap에 저장했는데, 나중에 외부 저장소에 저장해야 한다.
    @PostConstruct // 생성자가 호출되면 바로 이 메소드가 실행된다.ㄴ
    private void init() {
        chatRoomMap = new LinkedHashMap<>();
    }

    // 채팅방을 최근 순서대로 반환한다.
    public List<ChatRoom> findAllRoom() {
        List chatRooms = new ArrayList<>(chatRoomMap.values());
        Collections.reverse(chatRooms);
        return chatRooms;
    }

    public ChatRoom findRoomById(String id) {
        return chatRoomMap.get(id);
    }

    public ChatRoom createChatRoom(String name) {
        ChatRoom chatRoom = ChatRoom.create(name);
        chatRoomMap.put(chatRoom.getRoomId(), chatRoom);
        return chatRoom;
    }
}