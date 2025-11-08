package normalno;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class EmailMessageTest {

    @Test
    void emailMessage_shouldCreateWithAllFields() {

        EmailMessage email = new EmailMessage();
        email.setFrom("sender@example.com");
        email.setTo("receiver@example.com");
        email.setSubject("Test Subject");
        email.setBody("Test Body");
        email.setAiAnalysis("{\"priority\":\"high\"}");

        assertThat(email.getFrom()).isEqualTo("sender@example.com");
        assertThat(email.getTo()).isEqualTo("receiver@example.com");
        assertThat(email.getSubject()).isEqualTo("Test Subject");
        assertThat(email.getBody()).isEqualTo("Test Body");
        assertThat(email.getAiAnalysis()).isEqualTo("{\"priority\":\"high\"}");
    }

    @Test
    void emailMessage_shouldHaveNullFieldsByDefault() {

        EmailMessage email = new EmailMessage();

        assertThat(email.getFrom()).isNull();
        assertThat(email.getTo()).isNull();
        assertThat(email.getSubject()).isNull();
        assertThat(email.getBody()).isNull();
        assertThat(email.getAiAnalysis()).isNull();
    }

    @Test
    void emailMessage_shouldSupportEqualsAndHashCode() {

        EmailMessage email1 = new EmailMessage();
        email1.setFrom("sender@example.com");
        email1.setSubject("Test");

        EmailMessage email2 = new EmailMessage();
        email2.setFrom("sender@example.com");
        email2.setSubject("Test");

        assertThat(email1).isEqualTo(email2);
        assertThat(email1.hashCode()).isEqualTo(email2.hashCode());
    }

    @Test
    void emailMessage_shouldSupportToString() {

        EmailMessage email = new EmailMessage();
        email.setFrom("sender@example.com");
        email.setSubject("Test Subject");

        String toString = email.toString();

        assertThat(toString).contains("sender@example.com");
        assertThat(toString).contains("Test Subject");
    }

    @Test
    void emailMessage_shouldAllowModification() {

        EmailMessage email = new EmailMessage();
        email.setSubject("Original Subject");

        email.setSubject("Modified Subject");

        assertThat(email.getSubject()).isEqualTo("Modified Subject");
    }

    @Test
    void emailMessage_shouldHandleEmptyStrings() {

        EmailMessage email = new EmailMessage();
        email.setFrom("");
        email.setTo("");
        email.setSubject("");
        email.setBody("");
        email.setAiAnalysis("");

        assertThat(email.getFrom()).isEmpty();
        assertThat(email.getTo()).isEmpty();
        assertThat(email.getSubject()).isEmpty();
        assertThat(email.getBody()).isEmpty();
        assertThat(email.getAiAnalysis()).isEmpty();
    }

    @Test
    void emailMessage_shouldHandleLongContent() {

        String longBody = "A".repeat(10000);
        EmailMessage email = new EmailMessage();

        email.setBody(longBody);

        assertThat(email.getBody()).hasSize(10000);
    }

    @Test
    void emailMessage_shouldHandleSpecialCharacters() {

        EmailMessage email = new EmailMessage();
        email.setSubject("Test: Специальные символы & <HTML>");
        email.setBody("Body with\nnewlines\tand\ttabs");

        assertThat(email.getSubject()).contains("Специальные символы");
        assertThat(email.getBody()).contains("\n");
        assertThat(email.getBody()).contains("\t");
    }
}