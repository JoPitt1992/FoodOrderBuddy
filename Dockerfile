# ----------- Build Stage ----------- #
FROM maven:3.9.6-eclipse-temurin-21 AS build

#RUN useradd -m myuser
#USER myuser
WORKDIR /home/myuser/app

COPY --chown=myuser pom.xml ./
RUN mvn dependency:go-offline -B -Pproduction

COPY --chown=myuser src ./src
COPY --chown=myuser src/main/frontend frontend

RUN mvn clean package -B -DskipTests -Pproduction

# ----------- Runtime Stage ----------- #
FROM eclipse-temurin:21-jre

#RUN useradd -m myuser
#USER myuser
WORKDIR /home/myuser/app

COPY --from=build /home/myuser/app/target/*-SNAPSHOT.jar app.jar

EXPOSE 8080

CMD java -Dserver.port=8080 -jar app.jar
#ENTRYPOINT /bin/bash