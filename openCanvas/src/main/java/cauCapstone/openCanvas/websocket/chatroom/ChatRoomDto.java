package cauCapstone.openCanvas.websocket.chatroom;

import java.util.List;

import cauCapstone.openCanvas.rdb.dto.WritingDto;
import cauCapstone.openCanvas.websocket.chatmessage.ChatMessage;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter 
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "문서 작성방 관련 dto, 응답용")
public class ChatRoomDto {
	@Schema(description = "방id")
    private String roomId;
	@Schema(description = "글의 제목")
    private String name;	// content title 넣으면됨
	@Schema(description = "편집자 email")
    private String subject;	// 편집자임
	@Schema(description = "string 화된 버전 버전 2.2.1 이면 2.2.1 응답받음")
    private String version;		// 현재 Writing 버전을 string화 해서 넣음. TODO: .으로 구분해서 파싱해야함
    
	@Schema(description = "여태 썼던 글 모음")
    private List<WritingDto> writings;
    
	@Schema(description = "편집자가 작성중인 글의 내용과, 블럭번호를 실어서 보냄")
    private List<ChatMessage> snapshots;
	
	public ChatRoomDto (String roomId, String name, String subject, String version, List<WritingDto> writings){
		this.roomId = roomId;
		this.name = name;
		this.subject = subject;
		this.version = version;
		this.writings = writings;
	}
    
    public static ChatRoomDto fromEntity(ChatRoomRedisEntity crre, List<WritingDto> w){
    	
    	return new ChatRoomDto(crre.getRoomId(), crre.getName(), crre.getSubject(), crre.getVersion(), w);
    }
    
    public static ChatRoomDto fromEntity(ChatRoomRedisEntity crre, List<WritingDto> w, List<ChatMessage> messages){
    	
    	return new ChatRoomDto(crre.getRoomId(), crre.getName(), crre.getSubject(), crre.getVersion(), w,
    			messages);
    }
}
