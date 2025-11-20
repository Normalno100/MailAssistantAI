package normalno.controller;

import normalno.EmailMessage;
import normalno.service.MailService;
import normalno.util.AiAnalysisFormatter;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

@Controller
public class WebMailController {

    private final MailService mailService;
    private final AiAnalysisFormatter analysisFormatter;

    public WebMailController(MailService mailService, AiAnalysisFormatter analysisFormatter) {
        this.mailService = mailService;
        this.analysisFormatter = analysisFormatter;
    }

    @GetMapping("/")
    public String index(Model model) {
        List<EmailMessage> emails = mailService.fetchEmails();

        // Форматируем анализ для каждого письма
        emails.forEach(email -> {
            if (email.getAiAnalysis() != null) {
                String formatted = analysisFormatter.formatAnalysis(email.getAiAnalysis());
                email.setAiAnalysis(formatted);
            }
        });

        model.addAttribute("emails", emails);
        model.addAttribute("totalEmails", emails.size());
        return "index";
    }
}