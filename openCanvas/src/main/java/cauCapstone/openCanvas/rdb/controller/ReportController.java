package cauCapstone.openCanvas.rdb.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import cauCapstone.openCanvas.rdb.dto.ReportDto;
import cauCapstone.openCanvas.rdb.service.ReportService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/reports")
public class ReportController {

    private final ReportService reportService;

    @PostMapping
    @Operation(
        summary = "글 신고",
        description = """
        특정 글을 신고합니다. 신고자는 로그인한 사용자여야 하며, 
        신고된 글 정보와 본문 내용을 통해 관리자 모두한테 이메일이 전송됩니다,
        ReportDto 필요함
        """
    )
    public ResponseEntity<?> reportWriting(@RequestBody ReportDto reportDto) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("로그인되지 않음");
        }

        try {
            reportService.report(reportDto);
            return ResponseEntity.ok("신고가 접수되었습니다.");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("잘못된 신고 요청: " + e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("신고 처리 중 오류 발생");
        }
    }
}