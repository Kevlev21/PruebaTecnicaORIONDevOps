package reception.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import reception.jpa.ReceptionData;
import reception.services.ReceptionService;

@RestController
@RequestMapping("/api/v1/messages")
public class MessageController {

    private final ReceptionService service;

    public MessageController(ReceptionService service) {
        this.service = service;
    }

    @GetMapping
    public ResponseEntity<List<ReceptionData>> getMessages() {
        List<ReceptionData> messages = service.getMessages();
        return ResponseEntity.ok(messages);
    }
}
