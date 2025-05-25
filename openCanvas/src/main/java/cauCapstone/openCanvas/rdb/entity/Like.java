package cauCapstone.openCanvas.rdb.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@Setter
@Getter
@Entity
@Table(name = "likes")
public class Like {
	@Id 
	@GeneratedValue
	private Long id;
	
	// 좋아요는 cover가 아닌 content에만 다대일로 연결되있다.
	@ManyToOne
	@JoinColumn(name = "content_id")
	private Content content;
	
	@ManyToOne
	@JoinColumn(name = "user_id")
	private User user;
	
	//LIKE는 좋아요 DISLIKE는 싫어요
    @Enumerated(EnumType.STRING)
	private LikeType liketype;
}
