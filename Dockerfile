# ----------- Build Stage ----------- #
FROM maven:3.9.6-eclipse-temurin-21 AS build

# Install Node.js 14 (für Vaadin)
RUN curl -sL https://deb.nodesource.com/setup_14.x | bash - && \
    apt-get update && apt-get install -y --no-install-recommends nodejs && \
    rm -rf /var/lib/apt/lists/*

RUN useradd -m myuser
USER myuser
WORKDIR /home/myuser/app

# Projektdateien einfügen und bauen
COPY --chown=myuser pom.xml ./ 
RUN mvn dependency:go-offline -B -Pproduction

COPY --chown=myuser src ./src
COPY --chown=myuser src/main/frontend frontend

# Hier deinen Vaadin Lizenzschlüssel ggf. aktualisieren
RUN mvn clean package -B -DskipTests -Pproduction -Dvaadin.offlineKey=DEIN_KEY_HIER

# ----------- Runtime Stage ----------- #
FROM eclipse-temurin:21-jre

# Cloud SQL Auth Proxy hinzufügen
ADD https://dl.google.com/cloudsql/cloud_sql_proxy.linux.amd64 /cloud_sql_proxy
RUN chmod +x /cloud_sql_proxy && useradd -m myuser

USER myuser
WORKDIR /home/myuser/app

# App und Proxy einfügen
COPY --from=build /home/myuser/app/target/*-SNAPSHOT.jar app.jar

# ENV: Setze deine Instanzverbindung
ENV CLOUD_SQL_INSTANCE=foodorderbuddy:europe-west1:foodorderbuddy-sql

# Port freigeben
EXPOSE 8080

# Startscript: erst SQL Proxy, dann Spring Boot
ENTRYPOINT ["/bin/bash", "-c", "./cloud_sql_proxy -instances=$CLOUD_SQL_INSTANCE=tcp:5432 & sleep 5 && java -jar app.jar"]
