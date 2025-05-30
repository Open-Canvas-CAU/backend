package cauCapstone.openCanvas.rdb.dto;

import java.time.LocalDateTime;
import java.util.List;

import cauCapstone.openCanvas.rdb.entity.CommentLike;
import cauCapstone.openCanvas.rdb.entity.LikeType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter 
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "댓글에 달린 좋아요, 응답용")
public class CommentLikeDto {
	@Schema(description = "댓글dto")
	private ResCommentDto commentDto;
	@Schema(description = "유저dto")
	private UserDto userDto;
	
	@Schema(description = "내가 댓글에 좋아요눌렀는지 싫어요 눌렀는지")
	private LikeType likeType;
	
	public static CommentLikeDto fromEntity(CommentLike commentLike) {
		ResCommentDto commentDto = ResCommentDto.fromEntity(commentLike.getComment());
		UserDto userDto = UserDto.fromEntity(commentLike.getUser());
		
		return new CommentLikeDto(commentDto, userDto, commentLike.getLikeType());
	}
}
