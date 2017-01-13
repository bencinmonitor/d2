FROM anapsix/alpine-java:8_jdk

RUN mkdir -p /home/app

COPY target/scala-2.11/d2.jar /home/app/

EXPOSE 90000

CMD ["java", "-jar", "/home/app/d2.jar"]

