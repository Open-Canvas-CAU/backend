package cauCapstone.openCanvas.rdb.dto;

import java.util.List;

import cauCapstone.openCanvas.rdb.entity.Like;
import cauCapstone.openCanvas.rdb.entity.LikeType;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter 
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class LikeDto {
	
	private ContentDto contentDto;
	private UserDto userDto;
	private LikeType likeType;
	
	public static LikeDto fromEntity(Like like) {
		ContentDto contentDto = ContentDto.fromEntity(like.getContent());
		
		UserDto userDto = UserDto.fromEntity(like.getUser());
    	
    	return new LikeDto(contentDto, userDto, like.getLiketype());
	}
}
