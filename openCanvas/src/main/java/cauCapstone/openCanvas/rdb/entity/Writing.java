package cauCapstone.openCanvas.rdb.entity;

import java.time.LocalDateTime;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

// Content안의 글. 이어쓰기단위 
@NoArgsConstructor
@AllArgsConstructor
@Setter
@Getter
@Entity
public class Writing {
	@Id
	@GeneratedValue
	private Long id;
	
	private int depth; //현재 이어쓰기 번째
	private int siblingIndex; // 현재 이어쓰기 단계에서의 순번(2 이하로 하기)
	
	//FetchType.EAGER은 한단계의 연관관계만 보여주기 때문에 LAZY가 더 나음.
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "parent_id")
	private Writing parent;
	
	private String body;	// 그냥 문장을 저장함.
	private LocalDateTime time;
	
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "content_id")
	private Content content;
	
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "user_id")
	private User user;
	
	public Writing(int depth, int siblingIndex, Writing parent, String body, LocalDateTime time, Content content, User user) {
		this.depth = depth;
		this.siblingIndex = siblingIndex;
		this.parent = parent;
		this.body = body;
		this.time = time;
		this.content = content;
		this.user = user;
	}
}
