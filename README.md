# SIMULADO — REDAÇÃO

# Sistema de Cursos

Uma API de cursos em uma universidade. Todas as rotas são autenticadas pelo Auth0, existem dois papeis na aplicação, ADMIN e USER,

## Rotas

- POST /courses - cadastrar curso (ADMIN)
- GET /courses - listar cursos (ADMIN e USER)
- DELETE /courses/{id} - Deletar um curso (apenas ADMIN)

## Entidade

- Dados
  - id
  - código do curso
  - nome do curso
  - nome do instrutor
  - data cadastro
  - status (DISPONIVEL, CANCELADO) -> Usar Enum
  - e-mail do ADMIN que cadastrou o curso

## Front-End

- Implemente um Front-end que lista todos os cursos e permite o cadastro e a exclusão de cursos.
- Se o usuário tiver o papel de USER, ele apenas pode ver os cursos.
- Se o usuário tiver o papel de ADMIN, ele pode ver os cursos, cadastrar novos ou deletar cursos.

Envie os repositórios do Front e do Back