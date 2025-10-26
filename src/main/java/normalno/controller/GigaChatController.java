package normalno.controller;

import normalno.service.GigaChatService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/giga")
public class GigaChatController {

    private final GigaChatService gigaChatService;

    public GigaChatController(GigaChatService gigaChatService){
        this.gigaChatService = gigaChatService;
    }

    @PostMapping("/ask")
    public String ask(@RequestParam String question){
        return gigaChatService.askQuestion(question);
    }
}