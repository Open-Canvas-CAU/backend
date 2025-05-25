package cauCapstone.openCanvas.rdb.entity;

import java.time.LocalDateTime;
import java.util.List;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

// 신고 내용을 저장하는 엔티티
@NoArgsConstructor
@AllArgsConstructor
@Setter
@Getter
@Entity
@Table(name = "reports")
public class Report {
	
	@Id 
	@GeneratedValue
	private Long id;
	
	private String body;	// 신고내용
    private LocalDateTime time;
	
    @ManyToOne
	@JoinColumn(name = "writing_id")
	private Writing writing;	// 신고받은 writing
    
    public Report(String body, Writing writing) {
        this.body = body;
        this.writing = writing;
        this.time = LocalDateTime.now();
    }
	
}
