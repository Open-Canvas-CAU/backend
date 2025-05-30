package cauCapstone.openCanvas.rdb.dto;

import java.time.LocalDateTime;
import java.util.List;

import cauCapstone.openCanvas.rdb.entity.Comment;
import cauCapstone.openCanvas.rdb.entity.Content;
import cauCapstone.openCanvas.rdb.entity.User;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

// 프론트에서 요청용 CommentDto
@Getter 
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "댓글 요청하는 dto")
public class ReqCommentDto {
	@Schema(description = "전체 글(content) id")
	private Long contentId;
	@Schema(description = "댓글 내용")
	private String body;
	@Schema(description = "타임스탬프 프론트에서 찍고 넘겨줘야함")
	private LocalDateTime time;
	
	// 프론트에서는 엑세스토큰(User관련)과 contentId를 주고 백엔드에서 이걸 엔티티로 만들어서 to Entity의 매개변수로 넣는다.
    public Comment toEntity(Content content, User user) {
        return new Comment(content, user, body, time);
    }
    
}
