package cauCapstone.openCanvas.rdb.dto;

import java.util.List;

import cauCapstone.openCanvas.rdb.entity.Writing;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter 
@Setter
@NoArgsConstructor
public class WritingDto {
	public static WritingDto fromEntity(Writing writing) {
		return new WritingDto();
	}
}
