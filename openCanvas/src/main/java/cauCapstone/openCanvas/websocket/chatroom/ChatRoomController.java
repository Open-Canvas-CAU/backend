package cauCapstone.openCanvas.websocket.chatroom;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import cauCapstone.openCanvas.jwt.JwtTokenizer;
import io.jsonwebtoken.Claims;
import lombok.RequiredArgsConstructor;

// 채팅방 화면을 위한 컨트롤러
@RequiredArgsConstructor
@Controller
@RequestMapping("/chat")
public class ChatRoomController {
	private final ChatRoomService chatRoomService;
	private final ChatRoomRepository chatRoomRepository;
	private final JwtTokenizer jwtTokenizer;
	private final SubscribeRepository subscribeRegistryService;

	// 채팅 리스트 화면
	@GetMapping("/room")
	public String rooms(Model model) {
	    return "/chat/room";
	}
	
	// 모든 채팅방 목록 반환
	@GetMapping("/rooms")
	@ResponseBody
	public List<ChatRoomRedisEntity> room() {
	    return chatRoomRepository.findAllRoom();
	}
	
	// 채팅방 생성
	// 문서방을 생성할 때 redis에 roomId, name(방 이름), subject(유저 정보)가 저장된다.
	// TODO: 프론트에서 토큰을 header에 받아와야함.
	// TODO: version 필요함.
	@PostMapping("/room")
	@ResponseBody
	public ChatRoomRedisEntity createRoom(@RequestParam String name, @RequestHeader("Authorization") String token) {
		
		// 1. 토큰파싱: Bearer 붙어있으면 제거, 아니면 그대로 사용
		String rawToken = token != null && token.startsWith("Bearer ") ? token.substring(7) : token;
		
		// 2. 시크릿키로 토큰검증 및 Claims 추출
		String base64Key = jwtTokenizer.encodeBase64SecretKey();
		Claims claims = jwtTokenizer.verifySignature(rawToken, base64Key);
		
		// 3. subject를 유저 식별자로 활용
		String subject = claims.getSubject();
		
	    return chatRoomService.createChatRoom(name, subject, subject);
	}
	 
	 
	// TODO: 문서방에서 작성한 문서를 저장하는 역할이 필요함.
	// TODO: 여기 작성 해야함.
	// 프론트 쪽에서 편집자만 문서방 종료 버튼이 보이게 만들어야함.
	@DeleteMapping("/room/{roomId}")
	public ResponseEntity<Void> deleteRoom(@PathVariable String roomId, @RequestHeader("Authorization") String token){
		// 1. 토큰파싱: Bearer 붙어있으면 제거, 아니면 그대로 사용
		String rawToken = token != null && token.startsWith("Bearer ") ? token.substring(7) : token;
		
		// 2. 시크릿키로 토큰검증 및 Claims 추출
		String base64Key = jwtTokenizer.encodeBase64SecretKey();
		Claims claims = jwtTokenizer.verifySignature(rawToken, base64Key);
		
		// 3. subject를 유저 식별자로 활용
		String subject = claims.getSubject();
		
		// subscribeRegistryService.registerEditorSubject 실행. 검증 및 ChatRoom의 삭제까지 함.
		subscribeRegistryService.registerEditorSubject(roomId, subject);
		
		return ResponseEntity.noContent().build();
	}
	 
	// 채팅방 입장 화면
	// TODO: 여기 코드 맞나 확인해보기
	@GetMapping("/room/enter/{roomId}")
	public String roomDetail(Model model, @PathVariable String roomId) {
	    model.addAttribute("roomId", roomId);
	    return "/chat/roomdetail";
	}
	// 특정 채팅방 조회
	@GetMapping("/room/{roomId}")
	@ResponseBody
	public ChatRoomRedisEntity roomInfo(@PathVariable String roomId) {
	    return chatRoomRepository.findRoomById(roomId);
	}
}