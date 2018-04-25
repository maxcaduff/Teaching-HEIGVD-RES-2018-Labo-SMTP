FROM java:8
WORKDIR /
ADD MockMock.jar MockMock.jar
EXPOSE 8080
EXPOSE 5555
CMD java -jar MockMock.jar -p 5555
