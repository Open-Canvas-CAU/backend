package cauCapstone.openCanvas.rdb.dto;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import cauCapstone.openCanvas.rdb.entity.Comment;
import cauCapstone.openCanvas.rdb.entity.Content;
import cauCapstone.openCanvas.rdb.entity.User;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

// 백엔드에서 응답용 CommentDto
@Getter 
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ResCommentDto {
	
	private Long id;
	private ContentDto contentDto;
	private UserDto userDto;
	private List<CommentLikeDto> commentLikeDtos = new ArrayList<>();
	private String body;
	private LocalDateTime time;
	
	public static ResCommentDto fromEntity(Comment comment) {
		ContentDto contentDto = ContentDto.fromEntity(comment.getContent());
		UserDto userDto = UserDto.fromEntity(comment.getUser());
		
    	List<CommentLikeDto> commentLikeDtos = comment.getCommentLikes().stream()
    			.map((commentLike) -> CommentLikeDto.fromEntity(commentLike)).toList();
    	
    	return new ResCommentDto(comment.getId(), contentDto, userDto, commentLikeDtos, comment.getBody(), comment.getTime());
	}
}
