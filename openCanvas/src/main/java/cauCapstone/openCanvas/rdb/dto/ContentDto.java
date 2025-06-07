package cauCapstone.openCanvas.rdb.dto;

import java.util.ArrayList;
import java.util.List;

import cauCapstone.openCanvas.rdb.entity.Content;
import cauCapstone.openCanvas.rdb.entity.Cover;
import cauCapstone.openCanvas.rdb.entity.LikeType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

// TODO: 추가한 필드 title 때문에 문제생기나 확인
@Getter 
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "전체 글 관련 dto, 응답용"
		+ "요청할때는 대부분 coverId로 요청함"
		+ "댓글, 글조각 목록, 좋아요, 제목, 커버dto, 좋아요갯수, 유저가 그 글에 좋아요/싫어요를 눌렀는지 리턴함")
public class ContentDto {
	@Schema(description = "id")
	private Long id;
	@Schema(description = "조회수")
	private int view;
	
	@Schema(description = "댓글dto")
	private List<ResCommentDto> commentDtos = new ArrayList<>();
	@Schema(description = "조각글들을 모두 리턴함, 글 내용이 아직 필요없을 때는 "
			+ "depth, siblingIndex, title, username로 만든 dto가 리턴됨")
	private List<WritingDto> writingDtos = new ArrayList<>();
	@Schema(description = "글에 있는 좋아요 정보를 리턴")
	private List<LikeDto> likeDtos = new ArrayList<>();
	
	@Schema(description = "커버dto")
	private CoverDto coverDto;
	
	@Schema(description = "좋아요수")
	private int likeNum;
	@Schema(description = "내가 좋아요/ 싫어요를 눌렀는지")
	private LikeType likeType;
	
	@Schema(description = "제목")
	private String title;	// title 항목 추가
	
	private String official;
	
	public Content toEntity(Cover cover) {
		return new Content(cover);
	}
	
	public ContentDto(Long id, int view, List<ResCommentDto> commentDtos, List<WritingDto> writingDtos, 
			CoverDto coverDto, int likeNum, LikeType likeType, String title) {
		this.id = id;
		this.view = view;
		this.commentDtos = commentDtos;
		this.writingDtos = writingDtos;
		this.coverDto = coverDto;
		this.likeNum = likeNum;
		this.likeType = likeType;
		this.title = title;
	}
	
	public ContentDto (Long id, int view, List<ResCommentDto> commentDtos, List<WritingDto> writingDtos, 
			List<LikeDto> likeDtos, CoverDto coverDto) {
		this.id = id;
		this.view = view;
		this.commentDtos = commentDtos;
		this.writingDtos = writingDtos;
		this.likeDtos = likeDtos;
		this.coverDto = coverDto;
		this.title = coverDto.getTitle();
	}
	
	public static ContentDto fromEntityWithLike(List<ResCommentDto> commentDtos, Content content, int likeNum, LikeType likeType) {
    	
    	List<WritingDto> writingDtos = content.getWritings().stream()
    			.map(writing -> {
    				String title = writing.getContent().getCover().getTitle(); // 또는 content.getTitle() 커버 방식에 맞게
    				return WritingDto.fromEntity(writing, title);
    			}).toList();
		
		CoverDto coverDto = CoverDto.fromEntity(content.getCover(), content.getId());
		
		String title = coverDto.getTitle();
    	
    	return new ContentDto(content.getId(), content.getView(), commentDtos, writingDtos, coverDto, likeNum, likeType, title);
	}
	
}
