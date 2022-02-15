# cfe_16

cfe_16

Test client execution HOW-TO:

1. run "mvn clean compile"
2. run "java -classpath target/classes com.teragrep.cfe_16.TestClient localhost 8080 1 1"

This will cause the test client to connect to Spring embedded Tomcat at 
localhost:8080, and instantiate one thread doing the HTTP requests. The last
parameter tells the test client how many loops each thread will do. So if the last
two parameters are 8 and 10, 8 * 5 * 10 = 400 HTTP requests are made to the server.

In order to get relialable performance results, it is advised to use a large number
of loops to "warm" the HotSpot JVM, like 1000.
