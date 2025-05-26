package cauCapstone.openCanvas.websocket;

import lombok.extern.slf4j.Slf4j;

import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.listener.KeyExpirationEventMessageListener;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.stereotype.Component;

import cauCapstone.openCanvas.websocket.chatroom.RemoveChatRoomService;
import cauCapstone.openCanvas.websocket.chatroom.SubscribeRepository;

// WebSocketEventListener가 저장한 3분 TTL키가 클라이언트가 재구독을 해서 지워진게아닌 만료됬을 때(= 재구독을 안했을 때)를 감지하는 리스너이다.
@Slf4j
@Component
public class RedisKeyExpirationListener extends KeyExpirationEventMessageListener {

    private final SubscribeRepository subscribeRegistryService;
    private final RemoveChatRoomService chatRoomRepository;

    public RedisKeyExpirationListener(RedisMessageListenerContainer listenerContainer,
                                      SubscribeRepository subscribeRegistryService,
                                      RemoveChatRoomService chatRoomRepository) {
        super(listenerContainer);
        this.subscribeRegistryService = subscribeRegistryService;
        this.chatRoomRepository = chatRoomRepository;
    }

    @Override
    public void onMessage(Message message, byte[] pattern) {
        String expiredKey = message.toString();

        if (expiredKey.startsWith("disconnect:")) {
            String[] parts = expiredKey.split(":");
            if (parts.length < 3) return;

            String roomId = parts[1];
            String subject = parts[2];

            String editorSubject = subscribeRegistryService.getEditorSubjectByRoomId(roomId);
            
            // 연결을 끊은 사람이 그 방의 편집자일 때
            if (subject.equals(editorSubject)) {
                chatRoomRepository.removeChatRoom(roomId);
                
            } 
            // 연결을 끊은 사람이 편집자가 아닐 때
            else {
                subscribeRegistryService.registerEditorSubject(roomId, editorSubject);
            }
        }
    }
}
