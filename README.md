# Projeto Level Up API

## Instruções para Execução

Para resolver o problema de classe principal duplicada:

1. Certifique-se de que a classe principal está no pacote correto:
   - O pacote deve ser `com.projeto.levelupapi.projeto_levelupapi`
   - O arquivo `ProjetoLevelupapiApplication.java` deve ter a declaração de pacote correta

2. Execute a aplicação usando o comando:
```
.\mvnw spring-boot:run -Dspring-boot.run.main-class=com.projeto.levelupapi.projeto_levelupapi.ProjetoLevelupapiApplication
```

3. Ou limpe e recompile o projeto, e depois execute:
```
.\mvnw clean compile
.\mvnw spring-boot:run
```
