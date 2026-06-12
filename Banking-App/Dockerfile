# 1. Базовый образ с JDK 25
FROM openjdk:25-jdk-slim

# 2. Информация о maintainer
LABEL maintainer="Valep Vinreo"
LABEL description="Banking Application"

# 3. Рабочая директория внутри контейнера
WORKDIR /app

# 4. Копируем JAR файл из собранного проекта
COPY target/*.jar app.jar

# 5. Открываем порт, на котором работает приложение
EXPOSE 8080

# 6. Точка входа - команда для запуска приложения
ENTRYPOINT ["java", "-jar", "app.jar"]