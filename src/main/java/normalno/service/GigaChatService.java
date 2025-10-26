package normalno.service;

import normalno.EmailMessage;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.gigachat.GigaChatModel;
import org.springframework.stereotype.Service;

@Service
public class GigaChatService {

    private final GigaChatModel chatModel;

    public GigaChatService(GigaChatModel chatModel) {
        this.chatModel = chatModel;
    }

    public String askQuestion(String question) {
        try {
            Prompt prompt = new Prompt(question);
            ChatResponse response = chatModel.call(prompt);
            return response.getResult().getOutput().getText();
        } catch (Exception e) {
            e.printStackTrace();
            return "Ошибка при запросе к GigaChat: " + e.getMessage();
        }
    }

    // пример интеграции с Email
    public String analyzeEmail(EmailMessage email) {
        String text = String.format("""
                Проанализируй письмо и ответь кратко.
                Тема: %s
                Отправитель: %s
                Текст: %s
                """, email.getSubject(), email.getFrom(), email.getBody());
        return askQuestion(text);
    }
}
