package cauCapstone.openCanvas.rdb.dto;

import java.time.LocalDateTime;
import java.util.List;

import cauCapstone.openCanvas.rdb.entity.CommentLike;
import cauCapstone.openCanvas.rdb.entity.LikeType;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter 
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CommentLikeDto {
	private ResCommentDto commentDto;
	private UserDto userDto;
	
	private LikeType likeType;
	
	public static CommentLikeDto fromEntity(CommentLike commentLike) {
		ResCommentDto commentDto = ResCommentDto.fromEntity(commentLike.getComment());
		UserDto userDto = UserDto.fromEntity(commentLike.getUser());
		
		return new CommentLikeDto(commentDto, userDto, commentLike.getLikeType());
	}
}
