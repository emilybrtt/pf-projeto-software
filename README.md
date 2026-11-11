# PROVA FINAL - PROJETO DE SOFTWARE
Desenvolver um sistema de gerenciamento de tarefas de um projeto.

## Descrição: 
Um sistema onde os usuários podem criar e gerenciar tarefas de um projeto.

## Rotas:
- POST /tarefa: Cria uma nova tarefa dentro de um projeto com os dados: título, descrição, prioridade da tarefa e e-mail do usuário que criou a tarefa.
- DELETE /tarefa: Deleta uma tarefa
- GET /tarefa: Lista todas as tarefas.

Todas as requisições nessa aplicação devem ser autenticadas usando o Auth0.
Usar um banco de dados Postgres.
Além das rotas, desenvolver também um front com as funcionalidades de criar, deletar e listar as tarefas. Por último, fazer deploy da aplicação do Back-End na sua máquina da AWS e o front na Vercel com o proxy para a chamada do back.

## Avaliação:
Back-End (2 pontos)
Front-End (2 pontos)
Autenticação. (2 pontos)
Deploy na AWS. (2 pontos)
Deploy na Vercel. (2 pontos)

Enviar os links para os repositórios dos projetos e o link para a aplicação no vercel.