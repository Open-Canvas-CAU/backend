package cauCapstone.openCanvas.rdb.dto;

import java.util.ArrayList;
import java.util.List;

import cauCapstone.openCanvas.rdb.entity.Like;
import cauCapstone.openCanvas.rdb.entity.User;
import cauCapstone.openCanvas.rdb.entity.Writing;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter 
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UserDto {
	private Long id;
	private String nickname;
	private String email;
	private String color;
	
	private List<LikeDto> likeDtos = new ArrayList<>();
	private List<WritingDto> writingDtos = new ArrayList<>();

	public UserDto(Long id, String nickname, String email, String color) {
		this.id = id;
		this.nickname = nickname;
		this.email = email;
		this.color = color;
	}
	
	// 새로운 유저 저장용
    public User toEntity() {
        return new User(nickname, email, color);
    }
    
    // 좋아요 했던 것과 글썼던 목록도 보기위한 UserDto
    public static UserDto fromEntity(User user) {
    	List<LikeDto> likeDtos = user.getLikes().stream()
    			.map((like) -> LikeDto.fromEntity(like)).toList();
    	
    	List<WritingDto> writingDtos = user.getWritings().stream()
    			.map((writing) -> WritingDto.fromEntity(writing)).toList();
    	
    	return new UserDto(user.getId(), user.getNickname(), user.getEmail(), user.getColor(), likeDtos, writingDtos);
    }
}
