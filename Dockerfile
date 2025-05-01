#FROM eclipse-temurin:21-jre
#COPY target/*.jar app.jar
#EXPOSE 8080
#ENTRYPOINT ["java", "-jar", "/app.jar"]

FROM maven:3.8.1-openjdk-17-slim as build
RUN curl -sL https://deb.nodesource.com/setup_14.x | bash -
RUN apt-get update -qq && apt-get install -qq --no-install-recommends nodejs

RUN useradd -m myuser
WORKDIR /usr/src/app/
RUN chown myuser:myuser /usr/src/app/
USER myuser

COPY --chown=myuser pom.xml ./
RUN mvn dependency:go-offline -Pproduction

COPY --chown=myuser:myuser src src
COPY --chown=myuser:myuser fronted fronted

RUN mvn clean package -DskipTests -Pproduction key

FROM maven:3.8.1-openjdk-17-slim
RUN curl -sL https://deb.nodesource.com/setup_14.x | bash -
RUN apt-get update -qq && apt-get install -qq --no-install-recommends nodejs

COPY --from=build /usr/src/app/target/*.jar /usr/app/app.jar
RUN useradd -m myuser
USER myuser
EXPOSE 8080
CMD java -jar /usr/app/app.jar