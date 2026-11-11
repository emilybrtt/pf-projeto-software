package br.insper.cursos;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

// @EnableScheduling comentado: só era usado pelo polling do CourseEventConsumer
// (fila Redis), que está desativado por dar erro de conexão na EC2.
// Ver QUEUE_EC2_SETUP.md para reativar.
@SpringBootApplication
// @EnableScheduling
public class ProvaFinalApplication {

	public static void main(String[] args) {
		SpringApplication.run(ProvaFinalApplication.class, args);
	}

}
