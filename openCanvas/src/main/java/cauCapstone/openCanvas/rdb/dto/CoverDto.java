package cauCapstone.openCanvas.rdb.dto;

import java.time.LocalDateTime;


import cauCapstone.openCanvas.rdb.entity.Cover;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter 
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "표지 정보에 관한 dto 응답용,"
		+ "제목 이미지 타임스탬프 조회수 좋아요개수 응답함")
public class CoverDto {
	@Schema(description = "글 제목")
	private String title;
	@Schema(description = "이미지url")
	private String coverImageUrl;
	@Schema(description = "전체 글 관련 dto")
	private ContentDto contentDto;
	@Schema(description = "타임스탬프")
	private LocalDateTime time;
	
	// 조회수
	private int view;
	// 좋아요 개수
	private Long likeNum;	// TODO: 나중에 잘 안되면 (int) 해서 타입바꾸기.
	
	public CoverDto(String title, String coverImageUrl, ContentDto contentDto, LocalDateTime time) {
		this.title = title;
		this.coverImageUrl = coverImageUrl;
		this.contentDto = contentDto;
		this.time = time;
	}
	
	public CoverDto(String title, String coverImageUrl, LocalDateTime time, int view, Long likeNum) {
		this.title= title;
		this.coverImageUrl = coverImageUrl;
		this.time = time;
		this.view = view;
		this.likeNum = likeNum;
	}
	
	public static CoverDto fromEntity(Cover cover) {
		ContentDto contentDto = ContentDto.fromEntity(cover.getContent());
		
		return new CoverDto(cover.getTitle(), cover.getCoverImageUrl(), contentDto, cover.getTime());
	}
	
	/*
	// 커버 화면에 좋아요 수까지 보임, content는 가져오지 않음.
	public static CoverDto fromEntityWithLike(Cover cover, int likeNum) {
		ContentDto contentDto = ContentDto.fromEntity(cover.getContent());
		
		return new CoverDto(cover.getTitle(), cover.getCoverImageUrl(), contentDto, cover.getTime(), likeNum);
	}
	*/
	
	public Cover toEntity() {
		return new Cover(title, coverImageUrl, time);
	}	// 좋아요 개수
}
