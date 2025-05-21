# Etapa 1: Build
FROM maven:3.8.5-eclipse-temurin-17 AS build
WORKDIR /app
# Copia o arquivo pom.xml e as dependências para o cache
COPY pom.xml .
RUN mvn dependency:go-offline -B
# Copia o código-fonte da aplicação
COPY src ./src
# Compila a aplicação
RUN mvn clean package -DskipTests

# Etapa 2: Execução
FROM eclipse-temurin:17-jre
WORKDIR /app
# Copia o JAR gerado na etapa de build
COPY --from=build /app/target/*.jar app.jar
# Define o comando para executar a aplicação com configurações de produção
ENTRYPOINT ["java", "-Dspring.profiles.active=prod", "-jar", "app.jar"]