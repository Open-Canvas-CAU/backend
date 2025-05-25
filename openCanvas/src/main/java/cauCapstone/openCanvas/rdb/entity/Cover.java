package cauCapstone.openCanvas.rdb.entity;

import java.time.LocalDateTime;


import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

// 캔버스 표지에 대한 엔티티
@NoArgsConstructor
@Setter
@Getter
@Entity
@Table(name = "covers")
public class Cover {
	@Id 
	@GeneratedValue
	private Long id;
	
	@Column(unique = true)
	private String title;
	private String coverImageUrl;
	
	// 부모가 삭제되면 자식도 같이 삭제됨.
	// 내용
	// 조회수는 Content 엔티티에서 참조해서쓴다.
	@OneToOne(mappedBy = "cover", cascade = CascadeType.ALL)
	private Content content;
	
	// 만든 시간
	private LocalDateTime time;
	
	public Cover(String title, String coverImageUrl, LocalDateTime time) {
		this.title = title;
		this.coverImageUrl = coverImageUrl;
		this.time = time;
	}
}
