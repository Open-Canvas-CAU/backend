package cauCapstone.openCanvas.websocket.chatroom;

import cauCapstone.openCanvas.rdb.dto.WritingDto;
import cauCapstone.openCanvas.rdb.service.WritingService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/rooms")
@RequiredArgsConstructor
public class ChatRoomController {

    private final WritingService writingService;
    private final ChatRoomService chatRoomService;

    @PostMapping("/create")
    @Operation(
    	    summary = "문서방 생성 및 입장",
    	    description = "편집자가 문서방을 생성하고 바로 입장합니다. "
    	    		+ "이전 글들도 불러오고 입장하고나면 웹소켓 연결을 해야한다."
    	    		+ "WritingDto를 받는데 최초 작성시 title만 지정해주고 버전은 1.1.0이라고 설정함, "
    	    		+ "이어쓰는 경우에는 WritingDto에 부모의 depth, siblingIndex가 필요함,"
    	    		+ "ChatRoomDto를 반환함."
    	)
    public ResponseEntity<?> createRoomAndEnter(@RequestBody WritingDto writingDto) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
    	
        if (auth == null || !auth.isAuthenticated()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("로그인되지 않음");
        }

        String email = (String) auth.getPrincipal();
        
        String version;
        
        if(writingDto.getDepth() == 0) {
        	version = "1.1";
        }else {
            int siblingIndex = writingService.checkWriting(
            		writingDto.getDepth(),
            		writingDto.getSiblingIndex(),
            		writingDto.getTitle()
            );

            version = (writingDto.getDepth() + 1) + "." + siblingIndex + "." + writingDto.getSiblingIndex();
        }

        ChatRoomRedisEntity chatRoom = chatRoomService.createChatRoom(
            writingDto.getTitle(),
            email,
            version
        );

        List<WritingDto> history = writingService.getWritingsWithRoomId(chatRoom.getRoomId());

        ChatRoomDto chatRoomDto = ChatRoomDto.fromEntity(chatRoom, history);

        return ResponseEntity.ok(chatRoomDto);
    }
    
    @GetMapping("/{roomId}")
    @Operation(
        summary = "문서방 참여 (구독자)",
        description = "roomId를 통해 기존 문서방에 구독자로 참여합니다. "
                    + "이전 글들의 히스토리를 받아옵니다. 웹소켓 연결 전 조회용으로 사용됩니다,"
                    + "roomId를 받고, ChatRoomDto를 리턴한다."
    )
    public ResponseEntity<?> enterRoomAsSubscriber(@PathVariable(name = "roomId") String roomId) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        if (auth == null || !auth.isAuthenticated()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("로그인되지 않음");
        }

        ChatRoomRedisEntity chatRoom = chatRoomService.findRoomById(roomId);
        
        if (chatRoom == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("존재하지 않는 roomId입니다.");
        }

        List<WritingDto> history = writingService.getWritingsWithRoomId(roomId);

        ChatRoomDto chatRoomDto = ChatRoomDto.fromEntity(chatRoom, history); 

        return ResponseEntity.ok(chatRoomDto);
    }
}