package cauCapstone.openCanvas.rdb.dto;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import cauCapstone.openCanvas.rdb.entity.Comment;
import cauCapstone.openCanvas.rdb.entity.Content;
import cauCapstone.openCanvas.rdb.entity.LikeType;
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
		+ "댓글id, 전체 글 관련 id(contentId), 유저 id(userId), 댓글 좋아요 관련 dto(commentLikeDtos), 댓글내용, 타임스탬프,"
		+ "댓글의 좋아요수, 댓글의 싫어요수, 내가 댓글에 좋아요/싫어요를 눌렀는지를 필드에 담아서 응답함.")
public class ResCommentDto {
	@Schema(description = "댓글 id")
	private Long id;
	@Schema(description = "전체 글(content) Id")
	private Long contentId;
	@Schema(description = "유저 id")
	private Long userId;
	@Schema(description = "댓글 좋아요 관련 dto")
	private List<CommentLikeDto> commentLikeDtos = new ArrayList<>();
	@Schema(description = "댓글내용")
	private String body;
	@Schema(description = "타임스탬프")
	private LocalDateTime time;
	@Schema(description = "좋아요수")
	private int likeNum;
	@Schema(description = "싫어요수")
	private int disLikeNum;
	@Schema(description = "내가 좋아요/ 싫어요를 눌렀는지")
	private LikeType likeType;
	
	public static ResCommentDto fromEntity(Comment comment, int likeNum, int disLikeNum, LikeType likeType) {
		Long contentId = comment.getContent().getId();
		Long userId = comment.getUser().getId();
		
    	List<CommentLikeDto> commentLikeDtos = comment.getCommentLikes().stream()
    			.map((commentLike) -> CommentLikeDto.fromEntity(commentLike)).toList();
    	
    	return new ResCommentDto(comment.getId(), contentId, userId, commentLikeDtos, comment.getBody(), 
    			comment.getTime(), likeNum, disLikeNum, likeType);
	}
}
