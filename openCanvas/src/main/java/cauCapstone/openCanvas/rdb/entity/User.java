package cauCapstone.openCanvas.rdb.entity;

import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@Setter
@Getter
@Entity
public class User {
	
	@Id
	@GeneratedValue
	private Long id;
	
	private String nickname;
	private String email;
	private String color;
	
	public User(String nickname, String email, String color) {
		this.nickname = nickname;
		this.email = email;
		this.color = color;
	}
	
	@OneToMany(mappedBy = "user", cascade = CascadeType.REMOVE)
	private List<Like> likes = new ArrayList<>();
	
	
	//TODO: 글 쓴 목록도 추가시켜야함.
	@OneToMany(mappedBy = "user", cascade = CascadeType.REMOVE)
	private List<Writing> writings = new ArrayList<>();
}
