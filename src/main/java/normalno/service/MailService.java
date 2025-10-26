package normalno.service;

import normalno.EmailMessage;

import jakarta.annotation.PostConstruct;
import jakarta.mail.*;
import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

@Service
public class MailService {

    private final AiService aiService;

    @Value("${mail.user}")
    private String username;

    @Value("${mail.password}")
    private String password;

    @Value("${mail.imap.host}")
    private String host;

    @Value("${mail.imap.port}")
    private String port;

    private Store store;

    public MailService(AiService aiService){
        this.aiService = aiService;
    }

    @PostConstruct
    public void init(){
        System.out.println("Пытаюсь поключиться к почтовому серверу...");
        connect();
    }

    private void connect(){
        Properties properties = new Properties();
        properties.put("mail.store.protocol", "imaps");
        properties.put("mail.imap.host", host);
        properties.put("mail.imap.port", port);
        properties.put("mail.imap.ssl.enable", "true");
        properties.put("mail.imap.auth", "true");
        properties.put("mail.imap.timeout", "5000");
        properties.put("mail.imap.connectiontimeout", "5000");

        try {
            Session session = Session.getInstance(properties, null);
            store = session.getStore("imaps");
            store.connect(host, username, password);
            System.out.println("Успешно подключился к почтовому серверу!");
        } catch (Exception e) {
            System.err.println("Ошибка подключения: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public List<EmailMessage> fetchEmails(){
        List<EmailMessage> emails = new ArrayList<>();

        if (store == null || !store.isConnected()){
            System.out.println("Нет подключения к почте. Переподключаюсь...");
            connect();
            if (store == null || !store.isConnected()){
                System.out.println("Не удалось подключиться. Возвращаю пустой список.");
                return emails;
            }
        }

        try {
            // Открываем папку INBOX (входящие) только для чтения
            Folder inbox = store.getFolder("INBOX");
            inbox.open(Folder.READ_ONLY);

            //Получаем последние 10 писем
            Message[] messages = inbox.getMessages();
            int start = Math.max(0, messages.length - 10);
            int end = messages.length;

            for (int i = start; i < end; i++) {
                Message message = messages[i];
                if (message instanceof MimeMessage mimeMessage){

                    //Создаем наш объект EmailMessage из письма
                    EmailMessage email = new EmailMessage();

                    //Преобразуем адреса в строки
                    Address[] fromAddresses = mimeMessage.getFrom();
                    if (fromAddresses != null && fromAddresses.length > 0){
                        email.setFrom(fromAddresses[0].toString());
                    }else {
                        email.setFrom("Неизвестный отправитель");
                    }

                    Address[] toAddresses = mimeMessage.getRecipients(Message.RecipientType.TO);
                    if (toAddresses != null && toAddresses.length > 0){
                        email.setTo(toAddresses[0].toString());
                    }else {
                        email.setTo("Неизвестный получатель");
                    }

                    email.setSubject(mimeMessage.getSubject());

                    //Получаем текст письма
                    Object content = mimeMessage.getContent();
                    if (content instanceof String){
                        email.setBody((String) content);
                    } else if(content instanceof Multipart multipart){
                        //Если письмо состоит из нескольких частей(текст + вложения)
                        for (int j = 0; j < multipart.getCount(); j++) {
                            BodyPart bodyPart = multipart.getBodyPart(j);
                            if (bodyPart.getContentType().toLowerCase().contains("text/plain")){
                                email.setBody(bodyPart.getContent().toString());
                                break;
                            }
                        }
                    }

                    try {
                        String analysis = aiService.analyzeEmail(email);
                        email.setAiAnalysis(analysis);
                    } catch (Exception e) {
                        email.setAiAnalysis("Ошибка анализа письма: " + e.getMessage());
                        e.printStackTrace();
                    }

                    emails.add(email);
                }
            }

            inbox.close(false);//закрываем папку без удаления писем
            System.out.println("Получено писем: " + emails.size());
        } catch (Exception e) {
            System.err.println("Ошибка при получении писем: " + e.getMessage());
            e.printStackTrace();
        }

        return emails;
    }
}
