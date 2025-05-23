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
	// TODO: 싫어요는 비딩 시스템에 쓰이기 때문에 씀
	private LikeType liketype;
}
