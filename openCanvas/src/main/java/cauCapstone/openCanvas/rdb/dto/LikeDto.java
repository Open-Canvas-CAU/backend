package cauCapstone.openCanvas.rdb.dto;

import java.util.List;

import cauCapstone.openCanvas.rdb.entity.Like;
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
@Schema(description = "전체 글(content) 좋아요 dto,"
		+ "유저가 좋아요를 누른 글을 가져오는데 쓰임, 응답용"
		+ "좋아요 싫어요 둘다 있지만 좋아요한 글만 가져옴")
public class LikeDto {
    @Schema(description = "좋아요한 글 제목")
    private String contentTitle;
	@Schema(description = "좋아요 누른 이메일")
	private String email;
	@Schema(description = "enum LIKE, DISLIKE 두가지있음")
	private LikeType likeType;
	
	public static LikeDto fromEntity(Like like) {
        String title = like.getContent().getTitle();  // 또는 content.getTitle()
		
		String email = like.getUser().getEmail();
    	
    	return new LikeDto(title, email, like.getLiketype());
	}
}
