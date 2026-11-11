package br.insper.cursos.courses.queue;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

// Quem produz (publica) mensagens na fila. Chamado pelo CourseService depois de
// criar ou excluir (soft delete) um curso. Quem consome é o CourseEventConsumer
// (dentro deste mesmo projeto, no padrão do professor seria um worker separado).
//
// DESATIVADO: fila de Redis desligada (dava erro de conexão na EC2, não há
// Redis acessível lá). @Service comentado para o Spring não instanciar esta
// classe nem tentar injetar o StringRedisTemplate (cujo bean também está
// desativado em RedisConfig). Ver QUEUE_EC2_SETUP.md para reativar na EC2.
// @Service
public class CourseEventProducer {

    private static final Logger logger = LoggerFactory.getLogger(CourseEventProducer.class);

    // Nome da "fila": na prática é só uma lista do Redis. Não existe conceito de
    // fila nativo no Redis puro — simula-se uma fila FIFO empilhando numa ponta
    // (rightPush, aqui) e desempilhando na outra (leftPop, no consumer).
    private static final String QUEUE_NAME = "course-events";

    private final StringRedisTemplate stringRedisTemplate;
    private final ObjectMapper objectMapper;

    public CourseEventProducer(StringRedisTemplate stringRedisTemplate, ObjectMapper objectMapper) {
        this.stringRedisTemplate = stringRedisTemplate;
        this.objectMapper = objectMapper;
    }

    public void publish(Long courseId, String code, CourseEventType eventType) {
        try {
            // Monta a mensagem e serializa pra JSON, porque o Redis guarda a lista
            // como Strings — não dá pra empurrar um objeto Java direto.
            CourseQueueMessage message = new CourseQueueMessage(courseId, code, eventType, LocalDateTime.now());
            String json = objectMapper.writeValueAsString(message);

            // rightPush = insere no final da lista ("entra na fila")
            stringRedisTemplate.opsForList().rightPush(QUEUE_NAME, json);
            logger.info("Evento de curso enviado para fila {}: {}", QUEUE_NAME, json);
        } catch (Exception e) {
            // Se falhar em publicar o evento, deixa a exceção subir: quem chamou
            // (CourseService) decide se isso deve impedir a operação principal ou não.
            logger.error("Erro ao publicar evento de curso na fila Redis", e);
            throw new RuntimeException("Erro ao publicar evento de curso na fila Redis", e);
        }
    }
}
