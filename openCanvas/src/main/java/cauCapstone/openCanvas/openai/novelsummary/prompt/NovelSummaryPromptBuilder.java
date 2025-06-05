package cauCapstone.openCanvas.openai.novelsummary.prompt;

import java.util.Arrays;

public class NovelSummaryPromptBuilder {
    public static String buildPrompt(String title, String[] genres, String content) {
        String prompt = "Task:\n"
                + "Summarize the main content of the novel, while always following the guidelines.\n\n"
                + "Guideline:\n"
                + "1. The maximum number of characters in the summary result does not exceed 2700 bytes.\n"
                + "2. Write simply and concisely, excluding unnecessary formats.\n"
                + "3. Considering that it is a literary work, explain the background and characters of the work, especially with regard to the main character.\n"
                + "4. The purpose of the summary is to explain the text in order to generate illustrations that fit the piece, using the Dall-e-3 model.\n"
                + "5. The results are based on Korean, with other languages used only when absolutely necessary.\n"
                + "-----\n"
                + "Title: " + title + "\n"
                + "Genres: " + Arrays.toString(genres) + "\n"
                + "Content: " + content;

        return prompt;
    }
}
