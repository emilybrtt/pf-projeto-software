package br.insper.cursos.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

// Configuração de conexão com o Redis. Usado hoje só pela fila em
// br.insper.cursos.courses.queue (CourseEventProducer/Consumer).
// Se remover a fila, pode remover esta classe também (ver nota no fim do arquivo).
@Configuration
public class RedisConfig {

    // Lidos de application.properties: redis.host / redis.port
    // (que por sua vez leem as env vars REDIS_HOST / REDIS_PORT, com fallback pra localhost:6379)
    @Value("${redis.host:localhost}")
    private String redisHost;

    @Value("${redis.port:6379}")
    private int redisPort;

    // Fábrica de conexões com o Redis (Lettuce é o driver usado pelo Spring Data Redis).
    // Os outros beans (RedisTemplate, StringRedisTemplate) dependem desta conexão.
    @Bean
    public RedisConnectionFactory redisConnectionFactory() {
        RedisStandaloneConfiguration config = new RedisStandaloneConfiguration();
        config.setHostName(redisHost);
        config.setPort(redisPort);
        return new LettuceConnectionFactory(config);
    }

    // RedisTemplate genérico (chave String, valor Object serializado como JSON).
    // Não é usado diretamente pela fila hoje (ela usa o StringRedisTemplate abaixo),
    // mas é o bean "padrão" caso precise guardar objetos/cache no Redis no futuro.
    @Bean
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory connectionFactory) {
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);

        StringRedisSerializer stringSerializer = new StringRedisSerializer();
        GenericJackson2JsonRedisSerializer jsonSerializer = new GenericJackson2JsonRedisSerializer();

        template.setKeySerializer(stringSerializer);
        template.setValueSerializer(jsonSerializer);
        template.setHashKeySerializer(stringSerializer);
        template.setHashValueSerializer(jsonSerializer);

        template.afterPropertiesSet();
        return template;
    }

    // Versão simplificada (chave e valor são Strings "cruas").
    // É esse bean que o CourseEventProducer/Consumer usam para empilhar/desempilhar
    // mensagens JSON (já serializadas manualmente como String) na lista do Redis.
    @Bean
    public StringRedisTemplate stringRedisTemplate(RedisConnectionFactory connectionFactory) {
        return new StringRedisTemplate(connectionFactory);
    }

    // ObjectMapper do Jackson 2 (com.fasterxml.jackson), usado pelo producer/consumer
    // pra converter CourseQueueMessage <-> JSON. Precisa ser esse (não o ObjectMapper
    // padrão do Spring Boot, que no Boot 4 é Jackson 3 / pacote tools.jackson) porque
    // o GenericJackson2JsonRedisSerializer acima também é baseado em Jackson 2.
    @Bean
    public ObjectMapper objectMapper() {
        return new ObjectMapper();
    }
}

// ---- Se decidir que NÃO vai precisar de fila/Redis na prova ----
// Pode remover junto: esta classe, o pacote br.insper.cursos.courses.queue inteiro,
// as chamadas a eventProducer.publish(...) no CourseService (e o parâmetro/injeção
// de CourseEventProducer no construtor), a dependência spring-boot-starter-data-redis
// e jackson-databind no pom.xml, o @EnableScheduling em ProvaFinalApplication,
// o serviço "redis" no docker-compose.yml, e as linhas redis.host/redis.port
// no application.properties.
