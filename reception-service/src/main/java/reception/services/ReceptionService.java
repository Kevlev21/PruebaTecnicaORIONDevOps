package reception.services;

import java.util.List;
import java.util.UUID;

import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.JsonNode;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import reception.dto.MessageDto;
import reception.jpa.MessageRepository;
import reception.jpa.ReceptionData;

@Service
@RequiredArgsConstructor
@Slf4j
public class ReceptionService {

    private final MessageRepository messageRepository;

    @RabbitListener(queues = "${connection.rabbit.consumer:events_queue}")
    public void handleMessage(JsonNode message) {
        log.info("Mensaje recibido de RabbitMQ: {}", message);
        try {
            MessageDto<String> dto = getMessage(message);

            ReceptionData entity = new ReceptionData();
            
            // Intenta parsear como UUID o genera uno si falla
            try {
                entity.setId(UUID.fromString(dto.getId()));
            } catch (IllegalArgumentException e) {
                log.warn("ID recibido ('{}') no es un UUID válido. Generando UUID aleatorio.", dto.getId());
                entity.setId(UUID.randomUUID());
            }

            entity.setName(dto.getMessage());

            messageRepository.save(entity);
            log.info("Mensaje guardado correctamente en la base de datos con ID: {}", entity.getId());
        } catch (Exception e) {
            log.error("Error al procesar el mensaje: {}", e.getMessage(), e);
        }
    }

    public List<ReceptionData> getMessages() {
        return messageRepository.findAll();
    }

    private MessageDto<String> getMessage(JsonNode jsonNode) {
        MessageDto<String> dto = new MessageDto<>();
        
        if (jsonNode != null && jsonNode.hasNonNull("id")) {
            dto.setId(jsonNode.get("id").asText());
        }
        if (jsonNode != null && jsonNode.hasNonNull("message")) {
            dto.setMessage(jsonNode.get("message").asText());
        }
        
        return dto;
    }
}