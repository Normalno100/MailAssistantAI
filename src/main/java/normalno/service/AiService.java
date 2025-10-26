package normalno.service;

import normalno.EmailMessage;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.stereotype.Service;

@Service
public class AiService {

    private final ChatModel chatModel;

    public AiService(ChatModel chatModel) {
        this.chatModel = chatModel;
    }

    public String analyzeEmail(EmailMessage email) {
        String text = String.format(
                """
                Ты — интеллектуальный ассистент, анализирующий входящие письма.

                Проанализируй следующее письмо и сделай выводы по пунктам:
                1. Краткое содержание письма (в 2–3 предложениях)
                2. Основная цель письма (запрос, жалоба, предложение, реклама и т.д.)
                3. Тональность письма (дружелюбная, нейтральная, раздражённая и т.д.)
                4. Приоритет (высокий, средний, низкий)
                5. Рекомендуемое действие (ответить, переслать, проигнорировать и т.п.)

                Информация о письме:
                - Отправитель: %s
                - Получатель: %s
                - Тема: %s
                - Текст письма:
                %s

                Ответь строго в формате JSON:
                {
                  "summary": "...",
                  "intent": "...",
                  "tone": "...",
                  "priority": "...",
                  "action": "..."
                }
                """,
                email.getFrom(), email.getTo(), email.getSubject(), email.getBody()
        );

        try {
            Prompt prompt = new Prompt(text);
            ChatResponse response = chatModel.call(prompt);
            return response.getResult().getOutput().getText();
        } catch (Exception e) {
            e.printStackTrace();
            return "Ошибка при анализе письма: " + e.getMessage();
        }
    }
}

