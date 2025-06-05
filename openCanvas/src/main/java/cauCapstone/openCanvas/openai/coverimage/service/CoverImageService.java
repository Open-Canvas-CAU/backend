package cauCapstone.openCanvas.openai.coverimage.service;

import cauCapstone.openCanvas.openai.coverimage.config.CoverImageProperties;
import cauCapstone.openCanvas.openai.imagegenerator.prompt.ImageGeneratorPromptBuilder;
import cauCapstone.openCanvas.openai.imagegenerator.service.ImageGeneratorService;
import cauCapstone.openCanvas.openai.novelsummary.service.NovelSummaryService;
import cauCapstone.openCanvas.openai.novelsummary.util.TextUtil;
import java.io.File;
import java.io.InputStream;
import java.net.URL;
import org.apache.commons.io.FileUtils;
import org.springframework.stereotype.Service;

@Service
public class CoverImageService {

    private static final int MAX_PROMPT_BYTE_LENGTH = 4000;

    private final ImageGeneratorService imageGeneratorService;
    private final NovelSummaryService novelSummaryService;
    private final TextUtil textUtil;
    private final CoverImageProperties properties;

    public CoverImageService(ImageGeneratorService imageGeneratorService,
                             NovelSummaryService novelSummaryService,
                             TextUtil textUtil,
                             CoverImageProperties properties) {
        this.imageGeneratorService = imageGeneratorService;
        this.novelSummaryService = novelSummaryService;
        this.textUtil = textUtil;
        this.properties = properties;
    }

    public String generateCoverImage(String title, String[] genres, String content) {
        String prompt = ImageGeneratorPromptBuilder.buildPrompt(title, genres, content);

        if (textUtil.getUtf8BytesLength(prompt) > MAX_PROMPT_BYTE_LENGTH) {
            String summarizedContent = novelSummaryService.summarizeIfExceeds(title, genres, content);
            prompt = ImageGeneratorPromptBuilder.buildPrompt(title, genres, summarizedContent);
        }

        return imageGeneratorService.generateImage(prompt);
    }

    public void makeImageAndSave(String postId, String title, String[] genres, String text) {
        String imageUrl = generateCoverImage(title, genres, text);

        String basePath = properties.getOutputPath(); // yml에 정의된 저장 디렉토리
        String fileName = postId + "_cover.png";
        File outputFile = new File(basePath, fileName);

        try (InputStream in = new URL(imageUrl).openStream()) {
            FileUtils.copyInputStreamToFile(in, outputFile);
        } catch (Exception e) {
            throw new RuntimeException("이미지 저장 실패", e);
        }
    }
}