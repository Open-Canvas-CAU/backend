package cauCapstone.openCanvas.rdb.entity;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

// 댓글에 관한 엔티티.
// TODO: Writing 파트를 추가해야할 수도 있음(어느 문단에 대해 얘기하고 있는지).
@NoArgsConstructor
@Setter
@Getter
@Entity
@Table(name = "comments")
public class Comment {
	@Id 
	@GeneratedValue
	private Long id;
	
	@ManyToOne
	@JoinColumn(name = "content_id")
	private Content content;
	
	@ManyToOne
	@JoinColumn(name = "user_id")
	private User user;
	
	@OneToMany(mappedBy = "comment", cascade = CascadeType.REMOVE)
	private List<CommentLike> commentLikes = new ArrayList<>();
	
	// 댓글 본문
	private String body;
	
	// 댓글 달린 시간
	private LocalDateTime time;
	
	public Comment(Content content, User user, String body, LocalDateTime time) {
		this.content = content;
		this.user = user;
		this.body = body;
		this.time = time;
	}
}
