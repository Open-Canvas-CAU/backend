package cauCapstone.openCanvas.rdb.entity;

import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@Setter
@Getter
@Entity
@Table(name = "users")
public class User {
	
	@Id
	@GeneratedValue
	private Long id;
	
	private String nickname;	// 이메일과 똑같은 값으로 써도 괜찮음.
	private String email;
	private String color;
	
    @Enumerated(EnumType.STRING)
	private Role role;
	
	public User(String nickname, String email, String color, Role role) {
		this.nickname = nickname;
		this.email = email;
		this.color = color;
		this.role = role;
	}
	
	public User(String nickname, String email, Role role) {
		this.nickname = nickname;
		this.email = email;
		this.role = role;
	}
	
	@OneToMany(mappedBy = "user", cascade = CascadeType.REMOVE)
	private List<Like> likes = new ArrayList<>();
	
	
	//TODO: 글 쓴 목록도 추가시켜야함.
	@OneToMany(mappedBy = "user", cascade = CascadeType.REMOVE)
	private List<Writing> writings = new ArrayList<>();
}
