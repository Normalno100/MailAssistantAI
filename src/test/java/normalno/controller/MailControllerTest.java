package normalno.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import normalno.EmailMessage;
import normalno.service.AiService;
import normalno.service.MailService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(MailController.class)
class MailControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private MailService mailService;

    @MockBean
    private AiService aiService; // Добавляем мок для AiService

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void getMails_shouldReturnListOfEmails_whenEmailsExist() throws Exception {
        // Arrange
        EmailMessage email1 = new EmailMessage();
        email1.setFrom("sender1@example.com");
        email1.setTo("receiver@example.com");
        email1.setSubject("Subject 1");
        email1.setBody("Body 1");
        email1.setAiAnalysis("{\"priority\":\"high\"}");

        EmailMessage email2 = new EmailMessage();
        email2.setFrom("sender2@example.com");
        email2.setTo("receiver@example.com");
        email2.setSubject("Subject 2");
        email2.setBody("Body 2");
        email2.setAiAnalysis("{\"priority\":\"low\"}");

        List<EmailMessage> emails = Arrays.asList(email1, email2);
        when(mailService.fetchEmails()).thenReturn(emails);

        // Act & Assert
        mockMvc.perform(get("/mails")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].from", is("sender1@example.com")))
                .andExpect(jsonPath("$[0].subject", is("Subject 1")))
                .andExpect(jsonPath("$[0].body", is("Body 1")))
                .andExpect(jsonPath("$[0].aiAnalysis", is("{\"priority\":\"high\"}")))
                .andExpect(jsonPath("$[1].from", is("sender2@example.com")))
                .andExpect(jsonPath("$[1].subject", is("Subject 2")));

        verify(mailService, times(1)).fetchEmails();
    }

    @Test
    void getMails_shouldReturnEmptyList_whenNoEmailsExist() throws Exception {
        // Arrange
        when(mailService.fetchEmails()).thenReturn(Collections.emptyList());

        // Act & Assert
        mockMvc.perform(get("/mails")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(0)));

        verify(mailService, times(1)).fetchEmails();
    }

    @Test
    void getMails_shouldHandleServiceException() throws Exception {
        // Arrange
        when(mailService.fetchEmails()).thenThrow(new RuntimeException("Service error"));

        // Act & Assert
        mockMvc.perform(get("/mails")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.message").value("Service error"))
                .andExpect(jsonPath("$.error").value("Internal Server Error"));

        verify(mailService, times(1)).fetchEmails();
    }

    @Test
    void getMails_shouldReturnEmailWithNullFields() throws Exception {
        // Arrange
        EmailMessage email = new EmailMessage();
        // Оставляем поля null

        List<EmailMessage> emails = Collections.singletonList(email);
        when(mailService.fetchEmails()).thenReturn(emails);

        // Act & Assert
        mockMvc.perform(get("/mails")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)));

        verify(mailService, times(1)).fetchEmails();
    }

    @Test
    void getMails_shouldHandleMultipleRequests() throws Exception {
        // Arrange
        EmailMessage email = new EmailMessage();
        email.setFrom("sender@example.com");
        email.setSubject("Test");

        List<EmailMessage> emails = Collections.singletonList(email);
        when(mailService.fetchEmails()).thenReturn(emails);

        // Act & Assert - первый запрос
        mockMvc.perform(get("/mails"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)));

        // Act & Assert - второй запрос
        mockMvc.perform(get("/mails"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)));

        verify(mailService, times(2)).fetchEmails();
    }
}