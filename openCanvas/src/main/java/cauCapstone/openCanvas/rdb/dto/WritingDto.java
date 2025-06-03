package cauCapstone.openCanvas.rdb.dto;

import java.time.LocalDateTime;

import cauCapstone.openCanvas.rdb.entity.Content;
import cauCapstone.openCanvas.rdb.entity.User;
import cauCapstone.openCanvas.rdb.entity.Writing;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

// TODO: color 추가해야할듯
@Getter 
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "글조각(이어쓰기 단위)이 필요할때 넘기는 용도로 쓰임,"
		+ "요청할때는 대부분 depth, siblingIndex, title으로 간단하게 정보를 요청함."
		+ "글 조각의 내용이 필요없을 때는 depth, siblingIndex, title, username로 간략하게 정보를 응답 받는 경우가 있음,"
		+ "원래는 depth, siblingIndex, parentIndex, body, tiem, username, title, color까지 응답받을 수 있음.")
public class WritingDto {
	@Schema(description = "현재 몇번째로 이어쓰고 있나")
	private int depth;
	@Schema(description = "현재 이어쓰기 번째에서 몇번째 글인가 (최대2)")
	private int siblingIndex;
	@Schema (description = "부모는 몇번째 글인가(최대2)")
	private Integer parentSiblingIndex;
	@Schema (description = "글내용")
	private String body;
	@Schema (description = "타임스탬프")
	private LocalDateTime time;
	@Schema (description = "이메일")
	private String username;	// 이메일로 바꿔보내도 상관없음, 백엔드에서 응답용
	@Schema (description = "전체 글의 제목")
	private String title;	//content에서 꺼내오는 제목, 백엔드에서 응답용
	private Long userId;	// TODO: 토큰화, 프론트에서 요청용
	@Schema(description = "전체 글의 id, 제목으로 대체될 때는 안씀")
	private Long contentId;	// 프론트에서 요청용
	@Schema(description = "유저의 색상")
	private String color;
	
	public WritingDto(int depth, int siblingIndex, Integer parentSiblingIndex, String body, LocalDateTime time, 
			String username, String title) {
		this.depth = depth;
		this.siblingIndex = siblingIndex;
		this.parentSiblingIndex = parentSiblingIndex;
		this.body = body;
		this.time = time;
		this.username = username;
		this.title = title;
	}
	
	public WritingDto(int depth, int siblingIndex, Integer parentSiblingIndex, String body, LocalDateTime time, 
			String username, String title, String color) {
		this.depth = depth;
		this.siblingIndex = siblingIndex;
		this.parentSiblingIndex = parentSiblingIndex;
		this.body = body;
		this.time = time;
		this.username = username;
		this.title = title;
		this.color = color;
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
	public static WritingDto fromEntity(Writing writing, String title) {
	    Integer parentSiblingIndex = null;

	    if (writing.getDepth() > 1) {  // depth가 1이면 루트니까 부모 불필요
	        Writing parent = writing.getParent();
	        if (parent != null) {
	            parentSiblingIndex = parent.getSiblingIndex();
	        }
	    }

	    String username = writing.getUser().getNickname();
	    
	    String color = writing.getUser().getColor();
	
		return new WritingDto(writing.getDepth(), writing.getSiblingIndex(), parentSiblingIndex, writing.getBody(), 
				writing.getTime(), username, title, color);
	}
	
	public Writing toEntity(User user, Content content, Writing parent) {
		return new Writing(depth, siblingIndex, parent, body, time, content, user);
	}
}
