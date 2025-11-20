package normalno.util;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class AiAnalysisFormatterTest {

    private AiAnalysisFormatter formatter;

    @BeforeEach
    void setUp() {
        formatter = new AiAnalysisFormatter();
    }

    @Test
    void formatAnalysis_shouldFormatValidJson() {
        // Arrange
        String json = """
                {
                  "summary": "Это тестовое письмо",
                  "intent": "request",
                  "tone": "friendly",
                  "priority": "high",
                  "action": "respond immediately"
                }
                """;

        // Act
        String result = formatter.formatAnalysis(json);

        // Assert
        assertThat(result).contains("📝 Краткое содержание");
        assertThat(result).contains("Это тестовое письмо");
        assertThat(result).contains("🎯 Цель письма");
        assertThat(result).contains("request");
        assertThat(result).contains("😊 Тональность");
        assertThat(result).contains("friendly");
        assertThat(result).contains("⚡ Приоритет");
        assertThat(result).contains("high");
        assertThat(result).contains("✅ Рекомендация");
        assertThat(result).contains("respond immediately");
    }

    @Test
    void formatAnalysis_shouldReturnOriginal_whenInvalidJson() {
        // Arrange
        String invalidJson = "This is not JSON";

        // Act
        String result = formatter.formatAnalysis(invalidJson);

        // Assert
        assertThat(result).isEqualTo(invalidJson);
    }

    @Test
    void formatAnalysis_shouldReturnDefault_whenNull() {
        // Act
        String result = formatter.formatAnalysis(null);

        // Assert
        assertThat(result).isEqualTo("Анализ недоступен");
    }

    @Test
    void formatAnalysis_shouldReturnDefault_whenEmpty() {
        // Act
        String result = formatter.formatAnalysis("");

        // Assert
        assertThat(result).isEqualTo("Анализ недоступен");
    }

    @Test
    void formatAnalysis_shouldHandlePartialJson() {
        // Arrange
        String json = """
                {
                  "summary": "Partial data",
                  "priority": "medium"
                }
                """;

        // Act
        String result = formatter.formatAnalysis(json);

        // Assert
        assertThat(result).contains("📝 Краткое содержание");
        assertThat(result).contains("Partial data");
        assertThat(result).contains("⚡ Приоритет");
        assertThat(result).contains("medium");
        assertThat(result).doesNotContain("🎯 Цель письма");
    }

    @Test
    void extractPriority_shouldExtractFromValidJson() {
        // Arrange
        String json = """
                {
                  "summary": "Test",
                  "priority": "HIGH"
                }
                """;

        // Act
        String result = formatter.extractPriority(json);

        // Assert
        assertThat(result).isEqualTo("high");
    }

    @Test
    void extractPriority_shouldReturnUnknown_whenNoPriority() {
        // Arrange
        String json = """
                {
                  "summary": "Test"
                }
                """;

        // Act
        String result = formatter.extractPriority(json);

        // Assert
        assertThat(result).isEqualTo("unknown");
    }

    @Test
    void extractPriority_shouldReturnUnknown_whenInvalidJson() {
        // Act
        String result = formatter.extractPriority("invalid json");

        // Assert
        assertThat(result).isEqualTo("unknown");
    }

    @Test
    void extractPriority_shouldReturnUnknown_whenNull() {
        // Act
        String result = formatter.extractPriority(null);

        // Assert
        assertThat(result).isEqualTo("unknown");
    }

    @Test
    void formatAnalysis_shouldHandleEmptyFields() {
        // Arrange
        String json = """
                {
                  "summary": "",
                  "intent": "test",
                  "tone": "",
                  "priority": "low"
                }
                """;

        // Act
        String result = formatter.formatAnalysis(json);

        // Assert
        assertThat(result).contains("🎯 Цель письма");
        assertThat(result).contains("test");
        assertThat(result).contains("⚡ Приоритет");
        assertThat(result).contains("low");
        // Пустые поля не должны отображаться
        assertThat(result).doesNotContain("📝 Краткое содержание:\n\n");
    }

    @Test
    void formatAnalysis_shouldHandleCyrillicContent() {
        // Arrange
        String json = """
                {
                  "summary": "Письмо содержит важную информацию о проекте",
                  "intent": "информирование",
                  "tone": "деловая",
                  "priority": "средний",
                  "action": "прочитать и ответить"
                }
                """;

        // Act
        String result = formatter.formatAnalysis(json);

        // Assert
        assertThat(result).contains("Письмо содержит важную информацию о проекте");
        assertThat(result).contains("информирование");
        assertThat(result).contains("деловая");
        assertThat(result).contains("средний");
        assertThat(result).contains("прочитать и ответить");
    }
}