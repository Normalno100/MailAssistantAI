package normalno.controller;

import normalno.EmailMessage;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.List;

@RestController
public class MailController {

    @GetMapping("/mails")
    public List<EmailMessage> getMails(){

        // 1. Создаем фейковое письмо
        EmailMessage fakeEmail = new EmailMessage();
        fakeEmail.setFrom("boss@company.com");
        fakeEmail.setTo("you@gmail.com");
        fakeEmail.setSubject("Ваша зарплата");
        fakeEmail.setBody("Поздравляю, вам повысили зарплату в 10 раз!");

        // 2. Создаем второе фейковое письмо
        EmailMessage anotherFakeEmail = new EmailMessage();
        anotherFakeEmail.setFrom("newsletter@cooltech.com");
        anotherFakeEmail.setTo("you@gmail.com");
        anotherFakeEmail.setSubject("Новые технологии в Java");
        anotherFakeEmail.setBody("Узнайте о новом фреймворке Spring AI в нашей новой статье!");

        return List.of(fakeEmail, anotherFakeEmail);
    }
}
