package normalno.service;

import jakarta.mail.*;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import normalno.EmailMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.Properties;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MailServiceTest {

    @Mock
    private AiService aiService;

    @Mock
    private Store store;

    @Mock
    private Folder inbox;

    @Mock
    private Session session;

    private MailService mailService;

    @BeforeEach
    void setUp() {
        mailService = new MailService(aiService);

        // Устанавливаем тестовые значения через рефлексию
        ReflectionTestUtils.setField(mailService, "username", "test@example.com");
        ReflectionTestUtils.setField(mailService, "password", "password");
        ReflectionTestUtils.setField(mailService, "host", "imap.example.com");
        ReflectionTestUtils.setField(mailService, "port", "993");
        ReflectionTestUtils.setField(mailService, "store", store);
    }

    @Test
    void fetchEmails_shouldReturnEmptyList_whenStoreIsNotConnected() throws Exception {
        // Arrange
        when(store.isConnected()).thenReturn(false);

        // Act
        List<EmailMessage> result = mailService.fetchEmails();

        // Assert
        assertThat(result).isEmpty();
    }

    @Test
    void fetchEmails_shouldReturnEmailList_whenStoreIsConnected() throws Exception {
        // Arrange
        when(store.isConnected()).thenReturn(true);
        when(store.getFolder("INBOX")).thenReturn(inbox);
        doNothing().when(inbox).open(Folder.READ_ONLY);

        // Создаём мок-сообщения
        MimeMessage mockMessage = mock(MimeMessage.class);
        when(mockMessage.getFrom()).thenReturn(new Address[]{new InternetAddress("sender@example.com")});
        when(mockMessage.getRecipients(Message.RecipientType.TO))
                .thenReturn(new Address[]{new InternetAddress("receiver@example.com")});
        when(mockMessage.getSubject()).thenReturn("Test Subject");
        when(mockMessage.getContent()).thenReturn("Test email body");

        Message[] messages = new Message[]{mockMessage};
        when(inbox.getMessages()).thenReturn(messages);

        when(aiService.analyzeEmail(any(EmailMessage.class)))
                .thenReturn("{\"summary\":\"Test\",\"priority\":\"medium\"}");

        // Act
        List<EmailMessage> result = mailService.fetchEmails();

        // Assert
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getFrom()).contains("sender@example.com");
        assertThat(result.get(0).getTo()).contains("receiver@example.com");
        assertThat(result.get(0).getSubject()).isEqualTo("Test Subject");
        assertThat(result.get(0).getBody()).isEqualTo("Test email body");
        assertThat(result.get(0).getAiAnalysis()).isNotNull();

        verify(inbox).close(false);
    }

    @Test
    void fetchEmails_shouldFetchLast10Emails_whenMoreThan10Exist() throws Exception {
        // Arrange
        when(store.isConnected()).thenReturn(true);
        when(store.getFolder("INBOX")).thenReturn(inbox);
        doNothing().when(inbox).open(Folder.READ_ONLY);

        // Создаём 15 мок-сообщений, но настраиваем только те, которые будут использоваться (последние 10)
        Message[] messages = new Message[15];

        // Первые 5 сообщений не будут обрабатываться, поэтому не мокаем их детально
        for (int i = 0; i < 5; i++) {
            messages[i] = mock(MimeMessage.class);
        }

        // Последние 10 сообщений (индексы 5-14) будут обработаны
        for (int i = 5; i < 15; i++) {
            MimeMessage mockMessage = mock(MimeMessage.class);
            when(mockMessage.getFrom()).thenReturn(new Address[]{new InternetAddress("sender" + i + "@example.com")});
            when(mockMessage.getRecipients(Message.RecipientType.TO))
                    .thenReturn(new Address[]{new InternetAddress("receiver@example.com")});
            when(mockMessage.getSubject()).thenReturn("Subject " + i);
            when(mockMessage.getContent()).thenReturn("Body " + i);
            messages[i] = mockMessage;
        }

        when(inbox.getMessages()).thenReturn(messages);
        when(aiService.analyzeEmail(any(EmailMessage.class)))
                .thenReturn("{\"summary\":\"Test\"}");

        // Act
        List<EmailMessage> result = mailService.fetchEmails();

        // Assert
        assertThat(result).hasSize(10);
        // Проверяем, что это последние 10 писем
        assertThat(result.get(0).getSubject()).isEqualTo("Subject 5");
        assertThat(result.get(9).getSubject()).isEqualTo("Subject 14");

        verify(inbox).close(false);
    }

    @Test
    void fetchEmails_shouldHandleMultipartContent() throws Exception {
        // Arrange
        when(store.isConnected()).thenReturn(true);
        when(store.getFolder("INBOX")).thenReturn(inbox);
        doNothing().when(inbox).open(Folder.READ_ONLY);

        MimeMessage mockMessage = mock(MimeMessage.class);
        Multipart multipart = mock(Multipart.class);
        BodyPart bodyPart = mock(BodyPart.class);

        when(mockMessage.getFrom()).thenReturn(new Address[]{new InternetAddress("sender@example.com")});
        when(mockMessage.getRecipients(Message.RecipientType.TO))
                .thenReturn(new Address[]{new InternetAddress("receiver@example.com")});
        when(mockMessage.getSubject()).thenReturn("Multipart Email");
        when(mockMessage.getContent()).thenReturn(multipart);

        when(multipart.getCount()).thenReturn(1);
        when(multipart.getBodyPart(0)).thenReturn(bodyPart);
        when(bodyPart.getContentType()).thenReturn("text/plain; charset=UTF-8");
        when(bodyPart.getContent()).thenReturn("Plain text body");

        Message[] messages = new Message[]{mockMessage};
        when(inbox.getMessages()).thenReturn(messages);

        when(aiService.analyzeEmail(any(EmailMessage.class)))
                .thenReturn("{\"summary\":\"Test\"}");

        // Act
        List<EmailMessage> result = mailService.fetchEmails();

        // Assert
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getBody()).isEqualTo("Plain text body");
    }

    @Test
    void fetchEmails_shouldHandleEmailsWithNullSender() throws Exception {
        // Arrange
        when(store.isConnected()).thenReturn(true);
        when(store.getFolder("INBOX")).thenReturn(inbox);
        doNothing().when(inbox).open(Folder.READ_ONLY);

        MimeMessage mockMessage = mock(MimeMessage.class);
        when(mockMessage.getFrom()).thenReturn(null);
        when(mockMessage.getRecipients(Message.RecipientType.TO))
                .thenReturn(new Address[]{new InternetAddress("receiver@example.com")});
        when(mockMessage.getSubject()).thenReturn("No Sender");
        when(mockMessage.getContent()).thenReturn("Body");

        Message[] messages = new Message[]{mockMessage};
        when(inbox.getMessages()).thenReturn(messages);

        when(aiService.analyzeEmail(any(EmailMessage.class)))
                .thenReturn("{\"summary\":\"Test\"}");

        // Act
        List<EmailMessage> result = mailService.fetchEmails();

        // Assert
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getFrom()).isEqualTo("Неизвестный отправитель");
    }

    @Test
    void fetchEmails_shouldSetErrorAnalysis_whenAiServiceFails() throws Exception {
        // Arrange
        when(store.isConnected()).thenReturn(true);
        when(store.getFolder("INBOX")).thenReturn(inbox);
        doNothing().when(inbox).open(Folder.READ_ONLY);

        MimeMessage mockMessage = mock(MimeMessage.class);
        when(mockMessage.getFrom()).thenReturn(new Address[]{new InternetAddress("sender@example.com")});
        when(mockMessage.getRecipients(Message.RecipientType.TO))
                .thenReturn(new Address[]{new InternetAddress("receiver@example.com")});
        when(mockMessage.getSubject()).thenReturn("Test");
        when(mockMessage.getContent()).thenReturn("Body");

        Message[] messages = new Message[]{mockMessage};
        when(inbox.getMessages()).thenReturn(messages);

        when(aiService.analyzeEmail(any(EmailMessage.class)))
                .thenThrow(new RuntimeException("AI Service Error"));

        // Act
        List<EmailMessage> result = mailService.fetchEmails();

        // Assert
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getAiAnalysis()).contains("Ошибка анализа письма");
        assertThat(result.get(0).getAiAnalysis()).contains("AI Service Error");
    }

    @Test
    void fetchEmails_shouldHandleEmptyInbox() throws Exception {
        // Arrange
        when(store.isConnected()).thenReturn(true);
        when(store.getFolder("INBOX")).thenReturn(inbox);
        doNothing().when(inbox).open(Folder.READ_ONLY);

        Message[] messages = new Message[0];
        when(inbox.getMessages()).thenReturn(messages);

        // Act
        List<EmailMessage> result = mailService.fetchEmails();

        // Assert
        assertThat(result).isEmpty();
        verify(inbox).close(false);
    }
}