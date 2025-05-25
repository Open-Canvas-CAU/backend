package cauCapstone.openCanvas.rdb.dto;


import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter 
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ReportDto {
	String body;	// 신고내용
	LocalDateTime time;	// 신고 시간
	
	// 신고할 글의 제목과 버전
	private int depth;
	private int siblingIndex;
	private String title;
}
