package normalno.service;

import normalno.EmailMessage;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.stereotype.Service;

@Service
class AiService {

    private final OpenAiChatModel chatModel;

    public AiService(OpenAiChatModel chatModel) {
        this.chatModel = chatModel;
    }

    public String analyzeEmail(EmailMessage email) {
        // Формируем текст запроса (prompt)
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

        // Создаём объект Prompt
        Prompt prompt = new Prompt(text);

        // Отправляем запрос в модель
        ChatResponse response = chatModel.call(prompt);

        // Возвращаем текст ответа
        return response.getResult().getOutput().getText();
    }
}
