package cauCapstone.openCanvas.openai.coverimage.controller;

import cauCapstone.openCanvas.openai.coverimage.service.CoverImageService;
import cauCapstone.openCanvas.openai.imagegenerator.dto.ImageGenerateSaveRequestDto;
import cauCapstone.openCanvas.openai.imagegenerator.dto.ImageGeneratorRequestDto;
import cauCapstone.openCanvas.openai.imagegenerator.prompt.ImageGeneratorPromptBuilder;
import cauCapstone.openCanvas.openai.imagegenerator.service.ImageGeneratorService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/image")
public class CoverImageController {

    private final CoverImageService coverImageService;

    public CoverImageController(CoverImageService coverImageService) {
        this.coverImageService = coverImageService;
    }

    @PostMapping("/generate-and-save")  // 테스트용
    public ResponseEntity<String> generateAndSaveImage(@RequestBody ImageGenerateSaveRequestDto dto) {
        coverImageService.makeImageAndSave(
                dto.getPostId(),
                dto.getTitle(),
                dto.getGenres(),
                dto.getContent()
        );

        return ResponseEntity.ok("이미지 저장 완료");
    }
}
