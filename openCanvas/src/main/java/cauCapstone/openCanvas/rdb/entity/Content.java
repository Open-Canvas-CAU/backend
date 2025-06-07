package cauCapstone.openCanvas.rdb.entity;

import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

// 캔버스 내용에 관한 엔티티. 글 내용을 리스트 형태로 갖는다.
@NoArgsConstructor
@AllArgsConstructor
@Setter
@Getter
@Entity
@Table(name = "contents")
public class Content {
	@Id 
	@GeneratedValue
	private Long id;
	
	// 조회수
	private int view;
	
	// 댓글
	// 부모가 삭제되면 자식도 같이 삭제됨.
	@OneToMany(mappedBy = "content", cascade = CascadeType.REMOVE)
	private List<Comment> comments = new ArrayList<>();
	
	
	// content 안의 글 내용을 트리구조
	@OneToMany(mappedBy = "content", cascade = CascadeType.REMOVE)
	private List<Writing> writings = new ArrayList<>();
	
	// 좋아요갯수
	@OneToMany(mappedBy = "content", cascade = CascadeType.REMOVE)
	private List<Like> likes = new ArrayList<>();
	
	@OneToOne
	@JoinColumn(name = "cover_id")
	private Cover cover;
	
	@Column(unique = true)
	private String title;
	
	@Column
	private String official;
	
	public Content(Cover cover) {
		this.view = 1;
		this.cover = cover;
		this.title = cover.getTitle();
		this.official = "1.1";
	}
}
