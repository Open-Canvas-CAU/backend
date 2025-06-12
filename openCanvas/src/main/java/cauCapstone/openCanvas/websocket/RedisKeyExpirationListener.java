package cauCapstone.openCanvas.websocket;


import lombok.extern.slf4j.Slf4j;

import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.listener.KeyExpirationEventMessageListener;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.stereotype.Component;

import cauCapstone.openCanvas.websocket.chatroom.RemoveChatRoomService;
import cauCapstone.openCanvas.websocket.chatroom.RemoveEditorService;
import cauCapstone.openCanvas.websocket.chatroom.SubscribeRepository;
import cauCapstone.openCanvas.websocket.snapshot.SnapshotService;

// WebSocketEventListener가 문서방을 나갔다가 3분안에 안들어온 유저가 있으면 실행한다.
@Slf4j
@Component
public class RedisKeyExpirationListener extends KeyExpirationEventMessageListener {

    private final SubscribeRepository subscribeRepository;
    private final RemoveEditorService removeEditorService;
    private final SnapshotService snapshotService;
    private final RemoveChatRoomService removeChatRoomService;

    public RedisKeyExpirationListener(RedisMessageListenerContainer listenerContainer,
                                      SubscribeRepository subscribeRepository,
                                      RemoveEditorService removeEditorService,
                                      SnapshotService snapshotService,
                                      RemoveChatRoomService removeChatRoomService) {
        super(listenerContainer);
        this.subscribeRepository = subscribeRepository;
        this.removeEditorService = removeEditorService;
        this.snapshotService = snapshotService;
        this.removeChatRoomService = removeChatRoomService;
    }

    @Override
    public void onMessage(Message message, byte[] pattern) {
        String expiredKey = message.toString();

        if (expiredKey.startsWith("disconnect:")) {
            String[] parts = expiredKey.split(":");
            if (parts.length < 3) return;

            String roomId = parts[1];
            String subject = parts[2];

            String editorSubject = subscribeRepository.getEditorSubjectByRoomId(roomId);
            if (editorSubject == null) {
                log.warn("editorSubject 정보 없음. roomId: {}", roomId);
                return;
            }
            
            // 2. ROOMOUT + 상태 제거
            removeEditorService.removeEditorSubject(subject);

            if (subject.equals(editorSubject)) {
                log.info("편집자 {}가 3분간 재연결하지 않아 문서방 {} 제거 시작", subject, roomId);

                // 2. 스냅샷 DB 저장
                snapshotService.saveSnapshotToDB(roomId);

                // 3. 문서방 제거
                // 메시지 전송 후 잠시 기다렸다가 리스너 제거
                new Thread(() -> {
                    try { Thread.sleep(1000); } catch (InterruptedException ignored) {}
                    removeChatRoomService.removeChatRoom(roomId);
                }).start();

            } else {
                // 편집자가 아닌 경우, 아무것도 할 필요가 없음.
            }
        }else if(expiredKey.startsWith("lock:document:")) {
            // 예시 키: lock:document:roomId:subject
            String[] parts = expiredKey.split(":");
            if (parts.length < 3) {
                log.warn("lock 키 포맷 이상함: {}", expiredKey);
                return;
            }

            String roomId = parts[2];

            String editorSubject = subscribeRepository.getEditorSubjectByRoomId(roomId);
            if (editorSubject == null) {
                log.warn("락 만료 감지됨 - editor 정보 없음. roomId: {}", roomId);
                return;
            }
            
            subscribeRepository.makeDisconnectKey2(roomId, editorSubject);
            log.info("락 만료로 인해 disconnect 키 [{}] 강제로 만듦 → 삭제 결과: {}", editorSubject);
        }
    }
}
