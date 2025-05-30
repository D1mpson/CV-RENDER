# Використовуємо офіційний образ з Java 17
FROM maven:3.9.6-eclipse-temurin-17 AS build

# Робоча директорія
WORKDIR /app

# Копіюємо pom.xml для кешування залежностей
COPY pom.xml .

# Завантажуємо залежності (кешується якщо pom.xml не змінюється)
RUN mvn dependency:go-offline -B

# Копіюємо весь проект
COPY . .

# Збираємо додаток
RUN mvn clean package -DskipTests

# Фінальний образ з тільки Java runtime
FROM eclipse-temurin:17-jre-jammy

# Робоча директорія
WORKDIR /app

# Копіюємо JAR файл з build stage
COPY --from=build /app/target/cv-generator-0.0.1-SNAPSHOT.jar app.jar

# Відкриваємо порт
EXPOSE 8080

# Запускаємо додаток
ENTRYPOINT ["java", "-jar", "app.jar"]