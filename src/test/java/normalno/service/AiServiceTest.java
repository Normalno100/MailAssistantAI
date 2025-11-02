package normalno.service;

import normalno.EmailMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ai.chat.client.ChatClient;


import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AiServiceTest {

    @Mock
    private ChatClient.Builder chatClientBuilder;

    @Mock
    private ChatClient chatClient;

    @Mock
    private ChatClient.ChatClientRequestSpec requestSpec;

    @Mock
    private ChatClient.CallResponseSpec callResponseSpec;

    private AiService aiService;

    @BeforeEach
    void setUp() {
        when(chatClientBuilder.build()).thenReturn(chatClient);
        aiService = new AiService(chatClientBuilder);
    }

    @Test
    void analyzeEmail_shouldReturnJsonAnalysis_whenEmailIsValid() {
        // Arrange
        EmailMessage email = new EmailMessage();
        email.setFrom("sender@example.com");
        email.setTo("receiver@example.com");
        email.setSubject("Test Subject");
        email.setBody("This is a test email body");

        String expectedResponse = """
                {
                  "summary": "Test email summary",
                  "intent": "request",
                  "tone": "neutral",
                  "priority": "medium",
                  "action": "respond"
                }
                """;

        when(chatClient.prompt(anyString())).thenReturn(requestSpec);
        when(requestSpec.call()).thenReturn(callResponseSpec);
        when(callResponseSpec.content()).thenReturn(expectedResponse);

        String result = aiService.analyzeEmail(email);

        assertThat(result).isNotNull();
        assertThat(result).contains("summary");
        assertThat(result).contains("intent");
        assertThat(result).contains("tone");
        assertThat(result).contains("priority");
        assertThat(result).contains("action");

        verify(chatClient, times(1)).prompt(anyString());
    }

    @Test
    void analyzeEmail_shouldReturnErrorMessage_whenExceptionOccurs() {
        // Arrange
        EmailMessage email = new EmailMessage();
        email.setFrom("sender@example.com");
        email.setTo("receiver@example.com");
        email.setSubject("Test Subject");
        email.setBody("Test body");

        when(chatClient.prompt(anyString())).thenThrow(new RuntimeException("API Error"));

        // Act
        String result = aiService.analyzeEmail(email);

        // Assert
        assertThat(result).contains("Ошибка при анализе письма");
        assertThat(result).contains("API Error");
    }

    @Test
    void analyzeEmail_shouldHandleNullFields() {
        // Arrange
        EmailMessage email = new EmailMessage();
        // Оставляем поля null

        String expectedResponse = """
                {
                  "summary": "Empty email",
                  "intent": "unknown",
                  "tone": "neutral",
                  "priority": "low",
                  "action": "ignore"
                }
                """;

        when(chatClient.prompt(anyString())).thenReturn(requestSpec);
        when(requestSpec.call()).thenReturn(callResponseSpec);
        when(callResponseSpec.content()).thenReturn(expectedResponse);

        // Act
        String result = aiService.analyzeEmail(email);

        // Assert
        assertThat(result).isNotNull();
        verify(chatClient, times(1)).prompt(anyString());
    }

    @Test
    void analyzeEmail_shouldFormatPromptCorrectly() {

        EmailMessage email = new EmailMessage();
        email.setFrom("test@example.com");
        email.setTo("recipient@example.com");
        email.setSubject("Important Matter");
        email.setBody("Please review this urgent request");

        when(chatClient.prompt(anyString())).thenReturn(requestSpec);
        when(requestSpec.call()).thenReturn(callResponseSpec);
        when(callResponseSpec.content()).thenReturn("{}");

        // Act
        aiService.analyzeEmail(email);

        // Assert
        verify(chatClient).prompt(argThat((String prompt) ->
                prompt.contains("test@example.com") &&
                        prompt.contains("recipient@example.com") &&
                        prompt.contains("Important Matter") &&
                        prompt.contains("Please review this urgent request")
        ));
    }
}