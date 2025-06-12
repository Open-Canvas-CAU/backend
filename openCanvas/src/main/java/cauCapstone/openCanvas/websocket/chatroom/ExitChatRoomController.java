package cauCapstone.openCanvas.websocket.chatroom;

import cauCapstone.openCanvas.websocket.snapshot.SnapshotService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/rooms")
public class ExitChatRoomController {

    private final SubscribeRepository subscribeRepository;
    private final RemoveEditorService removeEditorService;
    private final SnapshotService snapshotService;
    private final RemoveChatRoomService removeChatRoomService;

    @PostMapping("/exit")
    @Operation(
    	    summary = "문서방 나가기",
    	    description = "문서방을 나갈 때 웹소켓을 끊고나서 이 컨트롤러를 호출하면 됩니다. "
    	    		+ "구독하고 있는 방의id가 필요합니다."
    	    		+ "편집자라면 문서방 삭제 과정을 거치고 ROOMOUT메시지를 보냅니다."
    	    		+ "편집자가 아니라면 아무일도 일어나지 않습니다."
    	 
    	)
    public ResponseEntity<String> exitRoom(@RequestParam(name = "roomId") String roomId) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            return ResponseEntity.status(401).body("인증되지 않은 사용자입니다.");
        }

        String subject = (String) auth.getPrincipal(); // JWT 인증 기반에서 subject (이메일 등) 추출

        String editorSubject = subscribeRepository.getEditorSubjectByRoomId(roomId);
        
        removeEditorService.removeEditorSubject(subject);
        
        if (editorSubject == null || !subject.equals(editorSubject)) {
            log.info("편집자가 아닌 유저가 문서방 나가기 시도함. subject={}, editorSubject={}", subject, editorSubject);
            // 2. ROOMOUT + 상태 제거
            return ResponseEntity.ok("편집자가 아니므로 아무 작업도 하지 않음.");
        }

        log.info("편집자 {}가 자발적으로 문서방 {} 나가기 요청", subject, roomId);

        // 1. disconnect 키 삭제
        subscribeRepository.removeDisconnectKey(roomId, subject);

        // 3. 스냅샷 DB 저장
        snapshotService.saveSnapshotToDB(roomId);

        // 4. 문서방 제거 (리스너 해제 포함, 메시지 발행 포함)
        new Thread(() -> {
            try { Thread.sleep(1000); } catch (InterruptedException ignored) {}
            removeChatRoomService.removeChatRoom(roomId);
        }).start();

        return ResponseEntity.ok("문서방 종료 처리 완료");
    }
    
    /*
    @PostMapping("/force-delete")
    public ResponseEntity<String> forceDelete() {
    	removeEditorService.forceR("loghelix223@gmail.com", "b0b1d61e-176e-43d1-80ef-a7c05574d810");
        return ResponseEntity.ok("강제 삭제 시도");
    }
    */
}