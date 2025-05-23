package cauCapstone.openCanvas.rdb.dto;

import java.time.LocalDateTime;


import cauCapstone.openCanvas.rdb.entity.Cover;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter 
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CoverDto {
	private String title;
	private String coverImageUrl;
	private ContentDto contentDto;	// 여기서 조회수를 가져옴.
	private LocalDateTime time;
	
	// 조회수
	private int view;
	// 좋아요 개수
	private int likeNum;
	
	public CoverDto(String title, String coverImageUrl, ContentDto contentDto, LocalDateTime time) {
		this.title = title;
		this.coverImageUrl = coverImageUrl;
		this.contentDto = contentDto;
		this.time = time;
	}
	
	public CoverDto(String title, String coverImageUrl, LocalDateTime time, int view, int likeNum) {
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
