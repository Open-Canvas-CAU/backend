package cauCapstone.openCanvas.rdb.dto;

import java.time.LocalDateTime;
import java.util.List;

import cauCapstone.openCanvas.rdb.entity.Comment;
import cauCapstone.openCanvas.rdb.entity.Content;
import cauCapstone.openCanvas.rdb.entity.User;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

// 프론트에서 요청용 CommentDto
@Getter 
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ReqCommentDto {
	private Long contentId;
	private Long userId;	// TODO: 토큰을 이용하기 때문에 나중에 없애야 한다.
	private String body;
	private LocalDateTime time;
	
	// 프론트에서는 엑세스토큰(User관련)과 contentId를 주고 백엔드에서 이걸 엔티티로 만들어서 to Entity의 매개변수로 넣는다.
    public Comment toEntity(Content content, User user) {
        return new Comment(content, user, body, time);
    }
    
}
