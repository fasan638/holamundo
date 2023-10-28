FROM anapsix/alpine-java
COPY *.jar /home/
WORKDIR /home
CMD ["java","-jar","/home/app.jar"]
