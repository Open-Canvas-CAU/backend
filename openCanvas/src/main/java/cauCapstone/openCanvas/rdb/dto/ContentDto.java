package cauCapstone.openCanvas.rdb.dto;

import java.util.ArrayList;
import java.util.List;

import cauCapstone.openCanvas.rdb.entity.Content;
import cauCapstone.openCanvas.rdb.entity.Cover;
import cauCapstone.openCanvas.rdb.entity.LikeType;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter 
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ContentDto {
	private Long id;
	private int view;
	
	private List<ResCommentDto> commentDtos = new ArrayList<>();
	private List<WritingDto> writingDtos = new ArrayList<>();
	private List<LikeDto> likeDtos = new ArrayList<>();
	
	private CoverDto coverDto;
	
	private int likeNum;
	private LikeType likeType;
	
	public Content toEntity(Cover cover) {
		return new Content(cover);
	}
	
	public ContentDto(Long id, int view, List<ResCommentDto> commentDtos, List<WritingDto> writingDtos, 
			CoverDto coverDto, int likeNum, LikeType likeType) {
		this.id = id;
		this.view = view;
		this.commentDtos = commentDtos;
		this.writingDtos = writingDtos;
		this.coverDto = coverDto;
		this.likeNum = likeNum;
		this.likeType = likeType;
	}
	
	public ContentDto (Long id, int view, List<ResCommentDto> commentDtos, List<WritingDto> writingDtos, 
			List<LikeDto> likeDtos, CoverDto coverDto) {
		this.id = id;
		this.view = view;
		this.commentDtos = commentDtos;
		this.writingDtos = writingDtos;
		this.likeDtos = likeDtos;
		this.coverDto = coverDto;
	}
	
	public static ContentDto fromEntityWithLike(Content content, int likeNum, LikeType likeType) {
    	List<ResCommentDto> commentDtos = content.getComments().stream()
    			.map((comment) -> ResCommentDto.fromEntity(comment)).toList();
    	
    	List<WritingDto> writingDtos = content.getWritings().stream()
    			.map((writing) -> WritingDto.fromEntity(writing)).toList();
		
		CoverDto coverDto = CoverDto.fromEntity(content.getCover());
    	
    	return new ContentDto(content.getId(), content.getView(), commentDtos, writingDtos, coverDto, likeNum, likeType);
	}
	
	public static ContentDto fromEntity(Content content) {
    	List<ResCommentDto> commentDtos = content.getComments().stream()
    			.map((comment) -> ResCommentDto.fromEntity(comment)).toList();
    	
    	List<WritingDto> writingDtos = content.getWritings().stream()
    			.map((writing) -> WritingDto.fromEntity(writing)).toList();
		
    	List<LikeDto> likeDtos = content.getLikes().stream()
    			.map((like) -> LikeDto.fromEntity(like)).toList();
		
		CoverDto coverDto = CoverDto.fromEntity(content.getCover());
		
		return new ContentDto(content.getId(), content.getView(), commentDtos, writingDtos, likeDtos, coverDto);
	}
}
