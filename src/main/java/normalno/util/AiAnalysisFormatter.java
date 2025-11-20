package normalno.util;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;

@Component
public class AiAnalysisFormatter {

    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * Форматирует JSON-ответ от AI в читаемый вид
     */
    public String formatAnalysis(String jsonAnalysis) {
        if (jsonAnalysis == null || jsonAnalysis.isBlank()) {
            return "Анализ недоступен";
        }

        try {
            JsonNode root = objectMapper.readTree(jsonAnalysis);

            StringBuilder formatted = new StringBuilder();

            addField(formatted, "📝 Краткое содержание", root, "summary");
            addField(formatted, "🎯 Цель письма", root, "intent");
            addField(formatted, "😊 Тональность", root, "tone");
            addField(formatted, "⚡ Приоритет", root, "priority");
            addField(formatted, "✅ Рекомендация", root, "action");

            return formatted.toString();

        } catch (Exception e) {
            // Если не удалось распарсить JSON, возвращаем как есть
            return jsonAnalysis;
        }
    }

    private void addField(StringBuilder sb, String label, JsonNode root, String fieldName) {
        if (root.has(fieldName)) {
            String value = root.get(fieldName).asText();
            if (!value.isBlank()) {
                if (sb.length() > 0) {
                    sb.append("\n\n");
                }
                sb.append(label).append(":\n").append(value);
            }
        }
    }

    /**
     * Извлекает приоритет из JSON для отображения бейджа
     */
    public String extractPriority(String jsonAnalysis) {
        if (jsonAnalysis == null || jsonAnalysis.isBlank()) {
            return "unknown";
        }

        try {
            JsonNode root = objectMapper.readTree(jsonAnalysis);
            if (root.has("priority")) {
                return root.get("priority").asText().toLowerCase();
            }
        } catch (Exception ignored) {
        }

        return "unknown";
    }
}