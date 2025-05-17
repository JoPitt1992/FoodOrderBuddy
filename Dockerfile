# ----------- Build Stage ----------- #
FROM maven:3.9.6-eclipse-temurin-21 AS build

# Installiere Node.js 14 (für Vaadin)
RUN curl -sL https://deb.nodesource.com/setup_14.x | bash - && \
    apt-get update && apt-get install -y --no-install-recommends nodejs && \
    rm -rf /var/lib/apt/lists/*

# Erstelle Benutzer & setze Arbeitsverzeichnis
RUN useradd -m myuser
USER myuser
WORKDIR /home/myuser/app

# Abhängigkeiten laden
COPY --chown=myuser pom.xml ./
RUN mvn dependency:go-offline -B -Pproduction

# Quellcode kopieren & bauen
COPY --chown=myuser src ./src
COPY --chown=myuser src/main/frontend frontend

# Kompiliere mit Lizenzschlüssel (falls nötig)
RUN mvn clean package -B -DskipTests -Pproduction -Dvaadin.offlineKey="eyJraWQiOiI1NDI3NjRlNzAwMDkwOGU2NWRjM2ZjMWRhYmY0ZTJjZDI4OTY2NzU4IiwidHlwIjoiSldUIiwiYWxnIjoiRVM1MTIifQ.eyJhbGxvd2VkUHJvZHVjdHMiOlsidmFhZGluLWNoYXJ0cyIsInZhYWRpbi10ZXN0YmVuY2giLCJ2YWFkaW4tZGVzaWduZXIiLCJ2YWFkaW4tY2hhcnQiLCJ2YWFkaW4tYm9hcmQiLCJ2YWFkaW4tY29uZmlybS1kaWFsb2ciLCJ2YWFkaW4tY29va2llLWNvbnNlbnQiLCJ2YWFkaW4tcmljaC10ZXh0LWVkaXRvciIsInZhYWRpbi1ncmlkLXBybyIsInZhYWRpbi1tYXAiLCJ2YWFkaW4tc3ByZWFkc2hlZXQtZmxvdyIsInZhYWRpbi1jcnVkIiwidmFhZGluLWNvcGlsb3QiLCJ2YWFkaW4tZGFzaGJvYXJkIl0sInN1YiI6Ijg1ZWJhMzBiLWQ4ZjgtNDBmMC05OTE0LTYyODU3MjAyNGM5OSIsInZlciI6MSwiaXNzIjoiVmFhZGluIiwiYWxsb3dlZEZlYXR1cmVzIjpbImNlcnRpZmljYXRpb25zIiwic3ByZWFkc2hlZXQiLCJ0ZXN0YmVuY2giLCJkZXNpZ25lciIsImNoYXJ0cyIsImJvYXJkIiwiYXBwc3RhcnRlciIsInZpZGVvdHJhaW5pbmciLCJwcm8tcHJvZHVjdHMtMjAyMjEwIl0sIm1hY2hpbmVfaWQiOiJtaWQtNDhhZDNjZTMtMjY2ZDg1MTMiLCJzdWJzY3JpcHRpb24iOiJWYWFkaW4gUHJvIiwic3Vic2NyaXB0aW9uS2V5IjpudWxsLCJuYW1lIjoiSm9oYW5uZXMgUGl0dGVybGUiLCJidWlsZF90eXBlcyI6WyJkZXZlbG9wbWVudCIsInByb2R1Y3Rpb24iXSwiZXhwIjoxNzUwNDY0MDAwLCJpYXQiOjE3NDYzNDQ2MjMsImFjY291bnQiOiJNYW5hZ2VtZW50IENlbnRlciBJbm5zYnJ1Y2sifQ.ABv_OxA25Cdmr8tAmIXkJbcbxSHW0FYD3DvsGxJq0MN02-A_RhQYkGCJsjtOIrvSXonNx-nBcOTdQxy5heO7-KKhASlvHEpFfWFNSwz_UI7U5PRREssqu9raJTCLfMAGaRpkoPrnM0NAATuWZLUitsuk8a9k4wuL100B-v0FFyFhIipm"

# ----------- Runtime Stage ----------- #
FROM eclipse-temurin:21-jre

# Cloud SQL Auth Proxy hinzufügen
ADD https://dl.google.com/cloudsql/cloud_sql_proxy.linux.amd64 /cloud_sql_proxy
RUN chmod +x /cloud_sql_proxy && useradd -m myuser

USER myuser
WORKDIR /home/myuser/app

# App und Proxy einfügen
COPY --from=build /home/myuser/app/target/*-SNAPSHOT.jar app.jar

# Cloud SQL Instanz konfigurieren
ENV CLOUD_SQL_INSTANCE=foodorderbuddy:europe-west1:foodorderbuddy-db

# Cloud Run erfordert Port 8080
EXPOSE 8080

# App + Proxy starten (Cloud SQL Proxy zuerst, dann Spring Boot)
CMD sh -c "echo Starting app on PORT=\$PORT && ./cloud_sql_proxy -instances=${CLOUD_SQL_INSTANCE}=tcp:5432 & sleep 5 && java -Dserver.port=\$PORT -jar app.jar"

