package cauCapstone.openCanvas.websocket.chatroom;

import java.util.List;

import cauCapstone.openCanvas.rdb.dto.WritingDto;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter 
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ChatRoomDto {
    private String roomId;
    private String name;	// content title 넣으면됨
    private String subject;	// 편집자임
    private String version;		// 현재 Writing 버전을 string화 해서 넣음. TODO: .으로 구분해서 파싱해야함
    
    private List<WritingDto> writings;
    
    public static ChatRoomDto fromEntity(ChatRoomRedisEntity crre, List<WritingDto> w){
    	
    	return new ChatRoomDto(crre.getRoomId(), crre.getName(), crre.getSubject(), crre.getVersion(), w);
    }
}
