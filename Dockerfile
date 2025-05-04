
# ----------- Build Stage ----------- #
FROM maven:3.9.6-eclipse-temurin-21 AS build

# Install Node.js 14 (Vaadin benötigt das)
RUN curl -sL https://deb.nodesource.com/setup_14.x | bash - && \
    apt-get update && apt-get install -y --no-install-recommends nodejs && \
    rm -rf /var/lib/apt/lists/*

# Erstelle einen Nutzer (nicht root)
RUN useradd -m myuser
USER myuser
WORKDIR /home/myuser/app

# Kopiere Maven-Projekt & lade Dependencies vorab
COPY --chown=myuser pom.xml ./
RUN mvn dependency:go-offline -B -Pproduction

# Kopiere den Rest & baue das Projekt
COPY --chown=myuser src ./src
COPY --chown=myuser frontend ./frontend
RUN mvn clean package -B -DskipTests -Pproduction -Dvaadin.offlineKey=eyJraWQiOiI1NDI3NjRlNzAwMDkwOGU2NWRjM2ZjMWRhYmY0ZTJjZDI4OTY2NzU4IiwidHlwIjoiSldUIiwiYWxnIjoiRVM1MTIifQ.eyJhbGxvd2VkUHJvZHVjdHMiOlsidmFhZGluLWNoYXJ0cyIsInZhYWRpbi10ZXN0YmVuY2giLCJ2YWFkaW4tZGVzaWduZXIiLCJ2YWFkaW4tY2hhcnQiLCJ2YWFkaW4tYm9hcmQiLCJ2YWFkaW4tY29uZmlybS1kaWFsb2ciLCJ2YWFkaW4tY29va2llLWNvbnNlbnQiLCJ2YWFkaW4tcmljaC10ZXh0LWVkaXRvciIsInZhYWRpbi1ncmlkLXBybyIsInZhYWRpbi1tYXAiLCJ2YWFkaW4tc3ByZWFkc2hlZXQtZmxvdyIsInZhYWRpbi1jcnVkIiwidmFhZGluLWNvcGlsb3QiLCJ2YWFkaW4tZGFzaGJvYXJkIl0sInN1YiI6IjE4NzQwNDRhLTVmYWEtNDE2NC1iY2EyLWNlZDkxMzE3ZjczYyIsInZlciI6MSwiaXNzIjoiVmFhZGluIiwiYWxsb3dlZEZlYXR1cmVzIjpbImNlcnRpZmljYXRpb25zIiwic3ByZWFkc2hlZXQiLCJ0ZXN0YmVuY2giLCJkZXNpZ25lciIsImNoYXJ0cyIsImJvYXJkIiwiYXBwc3RhcnRlciIsInZpZGVvdHJhaW5pbmciLCJwcm8tcHJvZHVjdHMtMjAyMjEwIl0sIm1hY2hpbmVfaWQiOm51bGwsInN1YnNjcmlwdGlvbiI6IlZhYWRpbiBQcm8iLCJzdWJzY3JpcHRpb25LZXkiOm51bGwsIm5hbWUiOiJTdWdhbnRoaSBNYW5vaGFyYW4iLCJidWlsZF90eXBlcyI6WyJwcm9kdWN0aW9uIl0sImV4cCI6MTc1MDQ2NDAwMCwiaWF0IjoxNzQ1NTA0NDk3LCJhY2NvdW50IjoiTWFuYWdlbWVudCBDZW50ZXIgSW5uc2JydWNrIn0.AFyQZwoitcBcvxGnFu0wrMo3oDV2Z31zHAYa83YRsC1i31xBcdJAkIHEkYDFa-azCHrWhmHT_5g1pKnmX7mrEFfyAdmjb6qHD9O4zhCtlV5mmjVCKZIdwWXPI2FIUFoCEOdIDS7URw9Xy0KdBya1OnO8rY75o8BrMbIdYFWwJvp4d9jC

# ----------- Runtime Stage ----------- #
FROM eclipse-temurin:21-jre

# Nutze denselben Benutzer
RUN useradd -m myuser
USER myuser

# Kopiere das fertige JAR
COPY --from=build /home/myuser/app/target/*-SNAPSHOT.jar /home/myuser/app/app.jar

WORKDIR /home/myuser/app
EXPOSE 8080

CMD ["java", "-jar", "app.jar"]