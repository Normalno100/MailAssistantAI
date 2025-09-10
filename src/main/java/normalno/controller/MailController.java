package normalno.controller;

import lombok.RequiredArgsConstructor;
import normalno.EmailMessage;

import normalno.service.MailService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.List;

@RestController
public class MailController {

    private final MailService mailService;

    public MailController(MailService mailService) {
        this.mailService = mailService;
    }

    @GetMapping("/mails")
    public List<EmailMessage> getMails(){
        System.out.println("Получаю реальные письма...");
        return mailService.fetchEmails();
    }
}
