package cauCapstone.openCanvas.rdb.dto;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import cauCapstone.openCanvas.rdb.entity.Comment;
import cauCapstone.openCanvas.rdb.entity.Content;
import cauCapstone.openCanvas.rdb.entity.User;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

// 백엔드에서 응답용 CommentDto
@Getter 
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "응답용 댓글dto,"
		+ "댓글id, 전체 글 관련 dto(contentDto), 유저 dto(userDto), 댓글 좋아요 관련 dto(commentLikeDtos), 댓글내용, 타임스탬프로 응담함")
public class ResCommentDto {
	@Schema(description = "댓글 id")
	private Long id;
	@Schema(description = "전체 글 관련 dto")
	private ContentDto contentDto;
	@Schema(description = "유저 dto")
	private UserDto userDto;
	@Schema(description = "댓글 좋아요 관련 dto")
	private List<CommentLikeDto> commentLikeDtos = new ArrayList<>();
	@Schema(description = "댓글내용")
	private String body;
	@Schema(description = "타임스탬프")
	private LocalDateTime time;
	
	public static ResCommentDto fromEntity(Comment comment) {
		ContentDto contentDto = ContentDto.fromEntity(comment.getContent());
		UserDto userDto = UserDto.fromEntity(comment.getUser());
		
    	List<CommentLikeDto> commentLikeDtos = comment.getCommentLikes().stream()
    			.map((commentLike) -> CommentLikeDto.fromEntity(commentLike)).toList();
    	
    	return new ResCommentDto(comment.getId(), contentDto, userDto, commentLikeDtos, comment.getBody(), comment.getTime());
	}
}
