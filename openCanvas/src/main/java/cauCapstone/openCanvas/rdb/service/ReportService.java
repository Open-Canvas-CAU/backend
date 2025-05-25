package cauCapstone.openCanvas.rdb.service;

import java.util.List;

import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import cauCapstone.openCanvas.rdb.dto.ReportDto;
import cauCapstone.openCanvas.rdb.entity.Report;
import cauCapstone.openCanvas.rdb.entity.Role;
import cauCapstone.openCanvas.rdb.entity.User;
import cauCapstone.openCanvas.rdb.entity.Writing;
import cauCapstone.openCanvas.rdb.repository.ReportRepository;
import cauCapstone.openCanvas.rdb.repository.UserRepository;
import cauCapstone.openCanvas.rdb.repository.WritingRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class ReportService {
    private final WritingRepository writingRepository;
    private final UserRepository userRepository;
    private final ReportRepository reportRepository;
    
    private final JavaMailSender mailSender;
	
    // 신고내용을 담아서 메일로 보내는 메소드.
    // TODO: 신고내용은 추후에 생각하기.
    @Transactional
    public void report(ReportDto reportDto) {
        Writing target = writingRepository. findByDepthAndSiblingIndexAndContent_Title(
                reportDto.getDepth(), reportDto.getSiblingIndex(), reportDto.getTitle()
            ).orElseThrow(() -> new IllegalArgumentException("신고할 글이 없음"));
        
        // 신고내용 db에 저장
        Report report = new Report(reportDto.getBody(), target);
        reportRepository.save(report);
    	
    	List<User> admins = userRepository.findAllByRole(Role.ADMIN);
        for (User admin : admins) {
           sendEmailToAdmin(admin.getEmail(), report);
        }
    }
    
    // 메일보내기메소드.
    private void sendEmailToAdmin(String adminEmail, Report report) {
        SimpleMailMessage message = new SimpleMailMessage();

        try {
            message.setFrom("your_email@gmail.com");	// 여기고치기.
            message.setTo(adminEmail);
            message.setSubject("[신고 접수] 새로운 신고가 도착했습니다.");
            message.setText("""
                대상 글 제목: %s
                내용: %s
                버전 정보: depth=%d, siblingIndex=%d
                """.formatted(
                    report.getWriting().getContent().getTitle(),
                    report.getBody(),
                    report.getWriting().getDepth(),
                    report.getWriting().getSiblingIndex()
                )
            );
            mailSender.send(message);
        }catch (Exception e) {
                log.info("메일 발송 실패!");
                throw new RuntimeException(e);
        }
    }
}
