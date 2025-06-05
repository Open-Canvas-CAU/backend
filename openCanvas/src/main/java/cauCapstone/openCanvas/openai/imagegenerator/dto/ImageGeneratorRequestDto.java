package cauCapstone.openCanvas.openai.imagegenerator.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter 
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ImageGeneratorRequestDto {
	private String title;
    private String[] genres;
    private String content;
}
