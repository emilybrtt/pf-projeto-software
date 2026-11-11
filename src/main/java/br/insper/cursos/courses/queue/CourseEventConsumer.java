package br.insper.cursos.courses.queue;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

// Quem consome (lê e processa) as mensagens publicadas pelo CourseEventProducer.
// Aqui só loga a mensagem recebida — não existe nenhuma regra de negócio real
// acoplada a isso ainda. Se precisar fazer algo de fato (enviar email, notificar
// outro serviço, etc.), é dentro do método consume() que entraria essa lógica.
//
// DESATIVADO junto com CourseEventProducer/RedisConfig (erro de conexão com
// Redis na EC2). @Component comentado para o Spring não criar este bean nem
// agendar o polling. Ver QUEUE_EC2_SETUP.md para reativar na EC2.
// @Component
public class CourseEventConsumer {

    private static final Logger logger = LoggerFactory.getLogger(CourseEventConsumer.class);
    private static final String QUEUE_NAME = "course-events";

    private final StringRedisTemplate stringRedisTemplate;
    private final ObjectMapper objectMapper;

    public CourseEventConsumer(StringRedisTemplate stringRedisTemplate, ObjectMapper objectMapper) {
        this.stringRedisTemplate = stringRedisTemplate;
        this.objectMapper = objectMapper;
    }

    // @Scheduled com fixedDelay roda esse método de novo a cada 5s (sempre esperando
    // 5s depois do fim da execução anterior). É isso que faz o "polling" na fila —
    // precisa do @EnableScheduling em ProvaFinalApplication pra funcionar.
    @Scheduled(fixedDelay = 5000)
    public void consume() {
        // leftPop = remove e retorna o primeiro item da lista ("sai da fila").
        // Junto com o rightPush do producer, isso implementa FIFO (quem entrou
        // primeiro, sai primeiro). Se a lista estiver vazia, volta null.
        String json = stringRedisTemplate.opsForList().leftPop(QUEUE_NAME);
        if (json == null) {
            return;
        }

        try {
            CourseQueueMessage message = objectMapper.readValue(json, CourseQueueMessage.class);
            logger.info("Evento de curso recebido da fila {}: {}", QUEUE_NAME, message);
        } catch (Exception e) {
            // Mensagem malformada ou erro de processamento: só loga e segue,
            // pra não travar o polling por causa de uma mensagem ruim.
            logger.error("Erro ao processar evento de curso da fila Redis: {}", json, e);
        }
    }
}
