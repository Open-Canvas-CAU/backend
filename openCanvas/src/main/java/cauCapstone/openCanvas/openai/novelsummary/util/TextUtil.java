package cauCapstone.openCanvas.openai.novelsummary.util;

import java.nio.charset.StandardCharsets;
import org.springframework.stereotype.Component;

@Component
public class TextUtil {

    public int getUtf8BytesLength(String text) {
        return text.getBytes(StandardCharsets.UTF_8).length;
    }

    public String truncateToMaxBytes(String text, int maxBytes) {
        byte[] bytes = text.getBytes(StandardCharsets.UTF_8);
        if (bytes.length <= maxBytes) return text;

        int end = maxBytes;
        while (end > 0 && (bytes[end] & 0xC0) == 0x80) {
            end--; // UTF-8 문자 깨짐 방지
        }
        return new String(bytes, 0, end, StandardCharsets.UTF_8);
    }
}
