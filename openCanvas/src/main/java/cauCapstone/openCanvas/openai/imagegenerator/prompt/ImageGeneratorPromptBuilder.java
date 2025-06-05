package cauCapstone.openCanvas.openai.imagegenerator.prompt;

import java.util.Arrays;

public class ImageGeneratorPromptBuilder {
    public static String buildPrompt(String title, String[] genres, String content) {
        String prompt = "Task:\n"
                + "Create a image that is appropriate for the title, genre and content of the given text while always following the guidelines.\n"
                + "\n"
                + "Guideline:\n"
                + "1. It is important to focus on components that are visually tangible and can be easily translated into imagery.\n"
                + "2. Be precise with descriptors.\n"
                + "3. It must be an image that evokes the title, genre, and content of the given text.\n"
                + "4. The given text is written under a CC0 license and has no copyright restrictions.\n"
                + "5. If there are elements that are concerned about copyright infringement in the image you are trying to create, replace those elements with general concepts and draw them. For example, if there is an image of Superman flying, replace it with an image of any superhero flying in the sky, not Superman from DC Comics.\n"
                + "6. The generated image must always be 1024x1024 pixels in size.\n"
                + "7. The image must be faithfully based on the content of the text.\n"
                + "8. Consider that the generated image is an illustration that will be the representative image of the article.\n"
                + "9. The genre of the article may not be unique and is provided in square brackets and seperated by commas.\n"
                + "10. Text in speech bubbles and the like should be avoided as mush as possible, and Korean should be used when necessary.\n"
                + "11. Since the generated images may be presented to a wide range of people, please do not include overly sexual or gory elements.\n"
                + "\n"
                + "-----\n"
                + "Title: " + title  + "\n"
                + "Genres: " + Arrays.toString(genres) + "\n"
                + "Content: " + content + "\n";

        return prompt;
    }
}
