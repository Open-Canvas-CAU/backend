package cauCapstone.openCanvas.rdb.dto;


import java.time.LocalDateTime;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter 
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "신고내용을 담아서 이 dto로 요청함,"
		+ "신고할 내용과 신고할 글을 지정해줘서 보내면됨")
public class ReportDto {
	@Schema(description = "신고내용")
	String body;	// 신고내용
	@Schema(description = "타임스탬프")
	LocalDateTime time;	// 신고 시간
	
	// 신고할 글의 제목과 버전
	@Schema(description = "글 조각이 몇번째 이어쓰기인지")
	private int depth;
	@Schema(description = "같은 이어쓰기 순서에서 몇번째 글인지(최대2)")
	private int siblingIndex;
	@Schema(description = "전체 글(content) 제목")
	private String title;
}
