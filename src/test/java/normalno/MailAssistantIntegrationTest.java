package normalno;

import normalno.service.MailService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class MailAssistantIntegrationTest {

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    @MockBean
    private MailService mailService;

    @Test
    void contextLoads() {
        // Проверяем, что контекст Spring успешно загружается
    }

    @Test
    void getMailsEndpoint_shouldReturnEmails() {
        // Arrange
        EmailMessage email1 = new EmailMessage();
        email1.setFrom("sender1@example.com");
        email1.setTo("receiver@example.com");
        email1.setSubject("Integration Test Email 1");
        email1.setBody("Test body 1");
        email1.setAiAnalysis("{\"priority\":\"high\"}");

        EmailMessage email2 = new EmailMessage();
        email2.setFrom("sender2@example.com");
        email2.setTo("receiver@example.com");
        email2.setSubject("Integration Test Email 2");
        email2.setBody("Test body 2");
        email2.setAiAnalysis("{\"priority\":\"medium\"}");

        List<EmailMessage> expectedEmails = Arrays.asList(email1, email2);
        when(mailService.fetchEmails()).thenReturn(expectedEmails);

        // Act
        ResponseEntity<List<EmailMessage>> response = restTemplate.exchange(
            "http://localhost:" + port + "/mails",
            HttpMethod.GET,
            null,
            new ParameterizedTypeReference<List<EmailMessage>>() {}
        );

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody()).hasSize(2);
        assertThat(response.getBody().get(0).getSubject()).isEqualTo("Integration Test Email 1");
        assertThat(response.getBody().get(1).getSubject()).isEqualTo("Integration Test Email 2");
    }

    @Test
    void getMailsEndpoint_shouldReturnEmptyListWhenNoEmails() {
        // Arrange
        when(mailService.fetchEmails()).thenReturn(List.of());

        // Act
        ResponseEntity<List<EmailMessage>> response = restTemplate.exchange(
            "http://localhost:" + port + "/mails",
            HttpMethod.GET,
            null,
            new ParameterizedTypeReference<List<EmailMessage>>() {}
        );

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody()).isEmpty();
    }

    @Test
    void application_shouldHaveCorrectBeans() {
        // Проверяем, что все необходимые бины созданы
        assertThat(restTemplate).isNotNull();
        assertThat(mailService).isNotNull();
    }
}