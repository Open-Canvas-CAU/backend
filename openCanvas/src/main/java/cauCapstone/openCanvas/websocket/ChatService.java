package cauCapstone.openCanvas.websocket;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.TextMessage;

import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

// 채팅방을 생성하고, 조회하고, 하나의 세션에 메세지를 발송하는 서비스이다.
@Slf4j
@RequiredArgsConstructor
@Service
public class ChatService {

    private final ObjectMapper objectMapper;
    private Map<String, ChatRoom> chatRooms;

    // TODO: 채팅방을 현재 hashMap에 저장했는데, 나중에 외부 저장소에 저장해야 한다.
    @PostConstruct // 생성자가 호출되면 바로 이 메소드가 실행된다.
    private void init() {
        chatRooms = new LinkedHashMap<>();
    }

    // 모든 방을 조회 한다.
    public List<ChatRoom> findAllRoom() {
        return new ArrayList<>(chatRooms.values());
    }

    // 채팅 메세지에 담긴 roomId로 메세지를 보내야하는 채팅방을 알아야할 때 쓴다.
    public ChatRoom findRoomById(String roomId) {
        return chatRooms.get(roomId);
    }
    
    // 랜덤한 UUID와 입력한 이름으로 채팅방을 만든다.
    public ChatRoom createRoom(String name) {
        String randomId = UUID.randomUUID().toString();
        ChatRoom chatRoom = ChatRoom.builder()
                .roomId(randomId)
                .name(name)
                .build();
        chatRooms.put(randomId, chatRoom);
        return chatRoom;
    }
    
    // 세션과 메세지를 입력해서 입력한 세션에 메세지를 보낸다.
    public <T> void sendMessage(WebSocketSession session, T message) {
        try {
            session.sendMessage(new TextMessage(objectMapper.writeValueAsString(message)));
        } catch (IOException e) {
            log.error(e.getMessage(), e);
        }
    }
}
