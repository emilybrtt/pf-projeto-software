# Fila (Redis) — desativada temporariamente

A fila de eventos de curso (Redis) foi **comentada**, não removida, porque estava
dando erro na EC2 (a aplicação tentava conectar em `REDIS_HOST:REDIS_PORT` e não
havia nenhum Redis acessível lá, gerando `connection refused` no startup / a cada
poll de 5s do consumer).

## O que foi comentado

| Arquivo | O que mudou |
|---|---|
| `src/main/java/br/insper/cursos/config/RedisConfig.java` | `@Configuration` comentado — nenhum bean de Redis é criado |
| `src/main/java/br/insper/cursos/courses/queue/CourseEventProducer.java` | `@Service` comentado — não é mais injetado |
| `src/main/java/br/insper/cursos/courses/queue/CourseEventConsumer.java` | `@Component` comentado — polling `@Scheduled` para de rodar |
| `src/main/java/br/insper/cursos/ProvaFinalApplication.java` | `@EnableScheduling` comentado (só servia para o consumer) |
| `src/main/java/br/insper/cursos/courses/CourseService.java` | campo/import `CourseEventProducer` e as duas chamadas `eventProducer.publish(...)` comentados; construtor passou a receber só `CourseRepository` |
| `src/test/java/br/insper/cursos/courses/CourseServiceTests.java` | `@Mock CourseEventProducer` comentado (não é mais usado no construtor) |
| `src/main/resources/application.properties` | `redis.host` / `redis.port` comentados |
| `docker-compose.yml` | serviço `redis` comentado |

Nada foi deletado: dar `Ctrl+F` por `Redis`/fila nesses arquivos e descomentar
devolve o comportamento original. A dependência `spring-boot-starter-data-redis`
continua no `pom.xml` (não precisa reinstalar nada para reativar).

## Como implementar isso na EC2 (quando quiser reativar)

Tem duas formas de ter um Redis acessível pela aplicação na EC2. A mais simples
para uma instância única é rodar o Redis **na própria EC2** via Docker.

### Opção 1 — Redis via Docker na mesma EC2 (mais simples)

1. Garanta que a EC2 tem Docker instalado:
   ```bash
   sudo yum install -y docker      # Amazon Linux 2/2023
   sudo systemctl enable --now docker
   sudo usermod -aG docker ec2-user   # relogue a sessão SSH depois disso
   ```

2. Suba um container Redis (persistindo dados num volume, já com restart automático):
   ```bash
   docker run -d \
     --name redis-pf \
     --restart unless-stopped \
     -p 6379:6379 \
     redis:latest
   ```
   Se a aplicação também rodar em container (via `docker-compose.yml` deste
   repo), descomente o serviço `redis` no `docker-compose.yml` e suba tudo
   junto com `docker compose up -d` — nesse caso use `REDIS_HOST=redis`
   (nome do serviço), não `localhost`.

3. **Importante — Security Group:** a porta `6379` NÃO deve ficar aberta para
   `0.0.0.0/0`. Se app e Redis estão na mesma instância, nem precisa abrir a
   porta no Security Group (comunicação é via `localhost`/rede interna do
   Docker). Só libere `6379` para outras instâncias se o Redis for acessado
   de fora dessa EC2, e mesmo assim restrinja a origem ao Security Group da
   aplicação, nunca ao mundo.

4. Defina as variáveis de ambiente que a aplicação já espera
   (`application.properties` já lê `REDIS_HOST`/`REDIS_PORT`):
   ```bash
   export REDIS_HOST=localhost   # ou "redis" se for o nome do serviço no compose
   export REDIS_PORT=6379
   ```
   Ou, se o processo roda via systemd, adicione ao `Environment=` do unit file,
   ou num `.env` lido pelo `docker-compose.yml`.

5. Teste a conexão antes de depender da aplicação:
   ```bash
   docker exec -it redis-pf redis-cli ping   # deve responder PONG
   ```

### Opção 2 — Amazon ElastiCache for Redis (gerenciado, melhor para produção)

Mais robusto que rodar Redis na própria EC2 (sem gerenciar backup/updates
manualmente), mas dá mais passos de rede:

1. Crie um cluster Redis no ElastiCache (console AWS → ElastiCache → Redis OSS
   cache → "Create").
2. Coloque o cluster na **mesma VPC** da EC2 (idealmente nas mesmas subnets
   privadas).
3. Crie/ajuste o Security Group do cluster para permitir a porta `6379` apenas
   a partir do Security Group da EC2 da aplicação (não libere para a internet).
4. Use o **endpoint primário** do cluster como `REDIS_HOST` e `6379` como
   `REDIS_PORT`:
   ```bash
   export REDIS_HOST=<nome-do-cluster>.xxxxxx.ng.0001.<regiao>.cache.amazonaws.com
   export REDIS_PORT=6379
   ```
5. ElastiCache não é acessível publicamente por padrão — por isso a EC2
   precisa estar na mesma VPC/subnets com rota para o cluster.

### Depois de ter o Redis acessível (qualquer opção)

1. Descomente no código (reverter exatamente o que está listado na tabela
   acima): `@Configuration` em `RedisConfig`, `@Service` em
   `CourseEventProducer`, `@Component` em `CourseEventConsumer`,
   `@EnableScheduling` em `ProvaFinalApplication`, o campo/import/chamadas de
   `eventProducer` em `CourseService`, o `@Mock` em `CourseServiceTests`, as
   linhas `redis.host`/`redis.port` em `application.properties` e o serviço
   `redis` no `docker-compose.yml` (se for usar esse compose na EC2).
2. Confirme que `REDIS_HOST`/`REDIS_PORT` estão definidos no ambiente onde a
   aplicação roda (variável de ambiente do processo, unit do systemd, ou
   `environment:` do serviço da app no `docker-compose.yml`).
3. Suba a aplicação e acompanhe o log: deve aparecer
   `Evento de curso enviado para fila course-events: ...` ao criar/excluir um
   curso, e `Evento de curso recebido da fila course-events: ...` ~5s depois
   (o polling do `CourseEventConsumer`).
4. Se der erro de conexão de novo, primeiro valide à parte, de dentro da
   própria EC2: `redis-cli -h $REDIS_HOST -p $REDIS_PORT ping` deve responder
   `PONG` antes de tentar pela aplicação — isso isola se o problema é
   rede/Security Group ou é a aplicação.
