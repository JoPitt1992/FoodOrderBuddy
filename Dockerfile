# Stage that builds the application, a prerequisite for the running stage
FROM maven:3.8.1-openjdk-17-slim as build
RUN curl -sL https://deb.nodesource.com/setup_14.x | bash -
RUN apt-get update -qq && apt-get install -qq --no-install-recommends nodejs

# Stop running as root at this point
RUN useradd -m myuser
WORKDIR /usr/src/app/
RUN chown myuser:myuser /usr/src/app/
USER myuser

# Copy pom.xml and prefetch dependencies so a repeated build can continue from the next step with existing dependencies
COPY --chown=myuser pom.xml ./
RUN mvn dependency:go-offline -Pproduction

# Copy all needed project files to a folder
COPY --chown=myuser:myuser src src
COPY --chown=myuser:myuser frontend frontend

# Build the production package, assuming that we validated the version before so no need for running tests again.
RUN mvn clean package -DskipTests -Pproduction -Dvaadin.offlineKey=eyJraWQiOiI1NDI3NjRlNzAwMDkwOGU2NWRjM2ZjMWRhYmY0ZTJjZDI4OTY2NzU4IiwidHlwIjoiSldUIiwiYWxnIjoiRVM1MTIifQ.eyJhbGxvd2VkUHJvZHVjdHMiOlsidmFhZGluLWNoYXJ0cyIsInZhYWRpbi10ZXN0YmVuY2giLCJ2YWFkaW4tZGVzaWduZXIiLCJ2YWFkaW4tY2hhcnQiLCJ2YWFkaW4tYm9hcmQiLCJ2YWFkaW4tY29uZmlybS1kaWFsb2ciLCJ2YWFkaW4tY29va2llLWNvbnNlbnQiLCJ2YWFkaW4tcmljaC10ZXh0LWVkaXRvciIsInZhYWRpbi1ncmlkLXBybyIsInZhYWRpbi1tYXAiLCJ2YWFkaW4tc3ByZWFkc2hlZXQtZmxvdyIsInZhYWRpbi1jcnVkIiwidmFhZGluLWNvcGlsb3QiLCJ2YWFkaW4tZGFzaGJvYXJkIl0sInN1YiI6IjE4NzQwNDRhLTVmYWEtNDE2NC1iY2EyLWNlZDkxMzE3ZjczYyIsInZlciI6MSwiaXNzIjoiVmFhZGluIiwiYWxsb3dlZEZlYXR1cmVzIjpbImNlcnRpZmljYXRpb25zIiwic3ByZWFkc2hlZXQiLCJ0ZXN0YmVuY2giLCJkZXNpZ25lciIsImNoYXJ0cyIsImJvYXJkIiwiYXBwc3RhcnRlciIsInZpZGVvdHJhaW5pbmciLCJwcm8tcHJvZHVjdHMtMjAyMjEwIl0sIm1hY2hpbmVfaWQiOm51bGwsInN1YnNjcmlwdGlvbiI6IlZhYWRpbiBQcm8iLCJzdWJzY3JpcHRpb25LZXkiOm51bGwsIm5hbWUiOiJTdWdhbnRoaSBNYW5vaGFyYW4iLCJidWlsZF90eXBlcyI6WyJwcm9kdWN0aW9uIl0sImV4cCI6MTc1MDQ2NDAwMCwiaWF0IjoxNzQ1NTA0NDk3LCJhY2NvdW50IjoiTWFuYWdlbWVudCBDZW50ZXIgSW5uc2JydWNrIn0.AFyQZwoitcBcvxGnFu0wrMo3oDV2Z31zHAYa83YRsC1i31xBcdJAkIHEkYDFa-azCHrWhmHT_5g1pKnmX7mrEFfyAdmjb6qHD9O4zhCtlV5mmjVCKZIdwWXPI2FIUFoCEOdIDS7URw9Xy0KdBya1OnO8rY75o8BrMbIdYFWwJvp4d9jC

# Running stage: the part that is used for running the application
FROM maven:3.8.1-openjdk-17-slim
RUN curl -sL https://deb.nodesource.com/setup_14.x | bash -
RUN apt-get update -qq && apt-get install -qq --no-install-recommends nodejs

COPY --from=build /usr/src/app/target/*.jar /usr/app/app.jar
RUN useradd -m myuser
USER myuser
EXPOSE 8080
CMD java -jar /usr/app/app.jar