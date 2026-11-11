# Variantes para a prova

Guia rápido pra trocar entre as implementações alternativas deixadas no
código, caso a prova peça algo diferente do que está ativo por padrão hoje.

Padrão atual do projeto: **Postgres + JPA**, **soft delete simples (só
ADMIN)** e **fila com Redis** (producer + consumer dentro do próprio app).

---

## 1. Delete simples vs. delete com aprovação do admin

**Hoje está ativo:** soft delete em uma etapa (`CourseService.softDelete`),
chamado por `DELETE /courses/{id}` (só ADMIN), que muda o status pra
`EXCLUIDO`.

**Existe comentado:** o fluxo em duas etapas — qualquer autenticado solicita
(`DELETE /courses/{id}` → status `EXCLUSAO_PENDENTE`), e só o ADMIN aprova de
fato (`POST /courses/{id}/approve-delete` → exclui do banco, ou 409 se não
estiver pendente).

### Como trocar

Em cada um dos 3 arquivos abaixo: comente o bloco "ativo" e descomente o
bloco "alternativo" (estão marcados com esses comentários no código):

1. `src/main/java/br/insper/cursos/courses/CourseService.java`
   — comentar `softDelete(...)`, descomentar `requestDelete(...)` e
   `approveDelete(...)`.
2. `src/main/java/br/insper/cursos/courses/CourseController.java`
   — comentar o método `delete(...)` (mapeado em `DELETE /{id}`), descomentar
   `requestDelete(...)` e `approveDelete(...)`.
3. `src/test/java/br/insper/cursos/courses/CourseServiceTests.java`
   — comentar os 2 testes de soft delete, descomentar os 5 testes do fluxo em
   duas etapas.

Na `Cursos.postman_collection.json` já existe uma pasta **"Alternativa -
Delete em 2 etapas"** com as requests prontas (`Request Delete Course` e
`Approve Delete Course (ADMIN)`); a request solta `Delete Course (ADMIN)` é a
do fluxo simples.

`CourseStatus.EXCLUSAO_PENDENTE` já existe no enum nos dois cenários, não
precisa tocar nele.

---

## 2. Remover a fila (Redis)

**Hoje está ativo:** `CourseService.create` e `.softDelete` publicam um
evento (`CREATED`/`DELETED`) numa lista Redis (`CourseEventProducer`), e um
`@Scheduled` (`CourseEventConsumer`) consome e só loga a mensagem a cada 5s.

Se a prova não pedir fila, ou der algum problema pra subir o Redis na hora,
dá pra tirar tudo com esse checklist:

1. Apagar `src/main/java/br/insper/cursos/config/RedisConfig.java`.
2. Apagar o pacote inteiro `src/main/java/br/insper/cursos/courses/queue/`
   (`CourseEventType`, `CourseQueueMessage`, `CourseEventProducer`,
   `CourseEventConsumer`).
3. Em `CourseService.java`: remover o campo `eventProducer`, o parâmetro dele
   no construtor, os imports de `CourseEventProducer`/`CourseEventType`, e as
   duas chamadas `eventProducer.publish(...)` (em `create` e `softDelete`).
4. Em `CourseServiceTests.java`: remover o `@Mock CourseEventProducer
   eventProducer` e o import correspondente.
5. `pom.xml`: remover as dependências `spring-boot-starter-data-redis` e
   `jackson-databind`.
6. `src/main/resources/application.properties`: remover as linhas
   `redis.host` / `redis.port`.
7. `docker-compose.yml`: remover o serviço `redis`.
8. `ProvaFinalApplication.java`: remover `@EnableScheduling` (só é usado pelo
   consumidor da fila).

---

## 3. Postgres vs. Mongo

**Hoje está ativo:** Postgres via `spring-boot-starter-data-jpa`
(`@Entity`, `JpaRepository<Course, Long>`, id `Long`).

**Existe uma branch `mongo`** com a variante usando Spring Data MongoDB, mas
ela é anterior a tudo que foi implementado depois (GetById, soft delete,
exception handler, OpenAPI, fila, testes, Postman) — ou seja, não dá pra só
dar `git checkout mongo` ou merge direto sem perder/conflitar com esse
trabalho. O caminho mais seguro é reaplicar manualmente, em cima do código
atual, os mesmos ajustes que a branch `mongo` fez. É isso que está descrito
abaixo (baseado nos commits dessa branch).

### Passo a passo

1. **`pom.xml`**: trocar
   ```xml
   <dependency>
       <groupId>org.springframework.boot</groupId>
       <artifactId>spring-boot-starter-data-jpa</artifactId>
   </dependency>
   <dependency>
       <groupId>org.postgresql</groupId>
       <artifactId>postgresql</artifactId>
       <scope>runtime</scope>
   </dependency>
   ```
   por
   ```xml
   <dependency>
       <groupId>org.springframework.boot</groupId>
       <artifactId>spring-boot-starter-data-mongodb</artifactId>
   </dependency>
   ```

2. **`application.properties`**: trocar
   ```properties
   spring.datasource.url=jdbc:postgresql://${DB_HOST:localhost}:5432/pf
   spring.datasource.username=${DB_USER:appuser}
   spring.datasource.password=${DB_PASSWORD:apppass}
   spring.jpa.hibernate.ddl-auto=update
   ```
   por
   ```properties
   spring.mongodb.uri=mongodb://${DB_HOST:localhost}:27017/pf
   ```
   (o serviço `mongo` já existe no `docker-compose.yml`, não precisa criar.)

3. **`Course.java`**: trocar as anotações JPA pelas do Mongo, e o tipo do id
   de `Long` pra `String` (Mongo usa ObjectId/String, não autoincremento):
   ```java
   // trocar
   import jakarta.persistence.*;
   @Entity
   @Table(name = "courses")
   public class Course {
       @Id
       @GeneratedValue(strategy = GenerationType.IDENTITY)
       private Long id;

   // por
   import org.springframework.data.annotation.Id;
   import org.springframework.data.mongodb.core.mapping.Document;
   @Document(collection = "courses")
   public class Course {
       @Id
       private String id;
   ```
   E o getter `getId()` passa a retornar `String`.

4. **`CourseRepository.java`**: trocar
   ```java
   public interface CourseRepository extends JpaRepository<Course, Long> {}
   ```
   por
   ```java
   public interface CourseRepository extends MongoRepository<Course, String> {}
   ```

5. **Trocar `Long` por `String` em todo lugar que referencia o id do curso**
   (arquivo:linha no estado atual do código, pra não esquecer nenhum):
   - `CourseService.java`: `getById(Long id)`, `softDelete(Long id)` (e, se
     estiver usando a versão alternativa do item 1, `requestDelete`/
     `approveDelete` também).
   - `CourseController.java`: `getById(@PathVariable Long id)`,
     `delete(@PathVariable Long id)` (e `requestDelete`/`approveDelete` se
     aplicável).
   - `CourseResponseDTO.java`: campo `Long id` → `String id`.
   - Pacote `courses/queue/` (fila): `CourseQueueMessage` (campo `Long
     courseId`) e `CourseEventProducer.publish(Long courseId, ...)`.

6. **`.github/workflows/deploy.yml`**: trocar
   ```yaml
   -e DB_HOST=postgres-pf
   ```
   por
   ```yaml
   -e DB_HOST=mongo-pf
   ```

7. **Testes** (`CourseServiceTests.java`): os `Mockito.when(courseRepository
   .findById(1L))` usam `Long` (`1L`) — trocar pra `String` (ex: `"1"`).

8. **Postman** (`Cursos.postman_collection.json` /
   `Cursos.postman_environment.json`): a variável `course_id` deixa de ser
   numérica — usar o id real gerado pelo Mongo (ObjectId em formato string)
   depois de criar um curso.

> Nenhuma dessas mudanças foi aplicada no código — isso é só o roteiro. A
> branch `mongo` em si está desatualizada (é de antes de todas as
> funcionalidades acima) e fica pra resolver depois, como você comentou.
