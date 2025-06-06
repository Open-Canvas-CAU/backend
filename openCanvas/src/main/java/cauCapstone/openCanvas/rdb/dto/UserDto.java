package cauCapstone.openCanvas.rdb.dto;

import java.util.ArrayList;
import java.util.List;

import cauCapstone.openCanvas.rdb.entity.Like;
import cauCapstone.openCanvas.rdb.entity.Role;
import cauCapstone.openCanvas.rdb.entity.User;
import cauCapstone.openCanvas.rdb.entity.Writing;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter 
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "유저정보 dto, 요청할때는 안씀(토큰씀) 응답할때 넘기는용도,"
		+ "(현재 썼던 댓글은 따로 리턴안함.)")
public class UserDto {
	
	@Schema(description = "인조키(거의 필수 아님)")
	private Long id;
	@Schema (description = "현재는 여기다가 이메일을 중복으로 담음")
	private String nickname;
	@Schema (description = "이메일")
	private String email;
	@Schema (description = "글씨 색상")
	private String color;
	@Schema (description = "역할(ADMIN 빼고 다 USER)")
	private Role role;
	
	private List<LikeDto> likeDtos = new ArrayList<>();
	private List<WritingDto> writingDtos = new ArrayList<>();

	public UserDto(String nickname, String email, String color) {
		this.nickname = nickname;
		this.email = email;
		this.color = color;
	}
	
	public UserDto(String nickname, String email, String color, Role role) {
		this.nickname = nickname;
		this.email = email;
		this.color = color;
		this.role = role;
	}
	
	public User toEntity() {
		return new User(nickname, email, role);
	}
	
	// 유저의 색상을 정해야할때 
    public User toEntityColor() {
        return new User(nickname, email, color, role);
    }
    
    // 좋아요 했던 것과 글썼던 목록도 보기위한 UserDto
    public static UserDto fromEntity(User user) {
    	List<LikeDto> likeDtos = user.getLikes().stream()
    			.map((like) -> LikeDto.fromEntity(like)).toList();
    	
    	List<WritingDto> writingDtos = user.getWritings().stream()
    			.map(writing -> {
    				String title = writing.getContent().getCover().getTitle(); // 또는 content.getTitle() 커버 방식에 맞게
    				return WritingDto.fromEntity(writing, title);
    			})
    			.toList();
    	
    	return new UserDto(user.getId(), user.getNickname(), user.getEmail(), user.getColor(), user.getRole(), likeDtos, writingDtos);
    }
}
