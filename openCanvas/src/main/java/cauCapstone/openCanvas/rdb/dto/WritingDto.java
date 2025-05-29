package cauCapstone.openCanvas.rdb.dto;

import java.time.LocalDateTime;

import cauCapstone.openCanvas.rdb.entity.Content;
import cauCapstone.openCanvas.rdb.entity.User;
import cauCapstone.openCanvas.rdb.entity.Writing;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

// TODO: color 추가해야할듯
@Getter 
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class WritingDto {
	private int depth;
	private int siblingIndex;
	
	private int parentSiblingIndex;
	private String body;
	private LocalDateTime time;
	
	private String username;	// 이메일로 바꿔보내도 상관없음, 백엔드에서 응답용
	private String title;	//content에서 꺼내오는 제목, 백엔드에서 응답용
	
	private Long userId;	// TODO: 토큰화, 프론트에서 요청용
	private Long contentId;	// 프론트에서 요청용
	
	public WritingDto(int depth, int siblingIndex, int parentSiblingIndex, String body, LocalDateTime time, 
			String username, String title) {
		this.depth = depth;
		this.siblingIndex = siblingIndex;
		this.parentSiblingIndex = parentSiblingIndex;
		this.body = body;
		this.time = time;
		this.username = username;
		this.title = title;
	}
	
	public WritingDto(int depth, int siblingIndex, LocalDateTime time, String username) {
		this.depth = depth;
		this.siblingIndex = siblingIndex;
		this.time = time;
		this.username = username;
	}
	
	public WritingDto(int depth, int siblingIndex, String title) {
		this.depth = depth;
		this.siblingIndex = siblingIndex;
		this.title = title;
	}
	
	public WritingDto(String title) {
		this.title = title;
		
	}
	
	// TODO: Transactional 붙이고 실제 FetchType.LAZY인 항목이 잘 전달되는지 테스트해보기.
	public static WritingDto fromEntity(Writing writing) {
		int parentSiblingIndex = writing.getParent().getSiblingIndex();
		String username = writing.getUser().getNickname();
		String title = writing.getContent().getTitle();
	
		return new WritingDto(writing.getDepth(), writing.getSiblingIndex(), parentSiblingIndex, writing.getBody(), 
				writing.getTime(), username, title);
	}
	
	public Writing toEntity(User user, Content content, Writing parent) {
		return new Writing(depth, siblingIndex, parent, body, time, content, user);
	}
}
