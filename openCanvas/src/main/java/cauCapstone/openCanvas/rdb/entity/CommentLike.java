package cauCapstone.openCanvas.rdb.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@Setter
@Getter
@Entity
public class CommentLike {
	@Id 
	@GeneratedValue
	private Long id;
	
	@ManyToOne
	@JoinColumn(name = "comment_id")
	private Comment comment;
	
	@ManyToOne
	@JoinColumn(name = "user_id")
	private User user;
	
	// true면 좋아요, false면 싫어요
	private LikeType likeType;
}
