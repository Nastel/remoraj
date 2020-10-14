





| Feature                | OpenTelemetry java            | RemoraJ                       | Notes                  |
|                        | auto instrumentation agent    |                               |                        |
| ---------------------- | ----------------------------- | ----------------------------- | ---------------------- |
| Instrumentation:       |                               |                               |                        |
|    Servlet             |              +                |              +                |                        |
|    ApacheHttpClient    |              +                |              +                |                        |
|    HttpUrlConnection   |              +                |              +                |                        |
|    EjbRemote           |              +                |              +                |                        |
|    JDBC                |              +                |              +                |                        |
|    JMS                 |              +                |              +                |                        |
|    Kafka               |              +                |              +                |                        |
|    Spring MVC          |              +                |              +                |                        |
|    Websocket           |              +                |              +                |                        |
|    - Own methods       |              -/+ (Annotation) |              +(Configuration) |                        |
|    Akka HTTP           |              +                |              -                |                        |
|    Armeria             |              +                |              -                |                        |
|    AWS Lambda          |              +                |              -                |                        |
|    AWS SDK             |              +                |              -                |                        |
|    Cassandra Driver    |              +                |              +/-              |                        |
|    Couchbase Client    |              +                |              -                |                        |
|    Dropwizard Views    |              +                |              -                |                        |
|    Elasticsearch API   |              +                |              +/-              |                        |
|    Elasticsearch REST  |              +                |              +/-              |                        |
|    Finatra             |              +                |              -                |                        |
|    Geode Client        |              +                |              -                |                        |
|    Google HTTP Client  |              +                |              -                |                        |
|    gRPC                |              +                |              -                |                        |
|    Hibernate           |              +                |              -                |                        |
|    Jedis               |              +                |              -                |                        |
|    JSP                 |              +                |              +/-              |                        |
|    khttp               |              +                |              -                |                        |
|    Grizzly REST        |              +                |              -                |                        |
|    Kubernetes          |              +                |              -                |                        |
|    Lettuce             |              +                |              -                |                        |
|    Log4j               |              +                |              -                |                        |
|    Logback             |              +                |              -                |                        |
|    MongoDB Drivers     |              +                |              -                |                        |
|    Netty               |              +                |              -                |                        |
|    OkHttp              |              +                |              -                |                        |
|    Logback             |              +                |              -                |                        |
|    Play                |              +                |              -                |                        |
|    Play WS             |              +                |              -                |                        |
|    RabbitMQ Client     |              +                |              -                |                        |
|    Ratpack             |              +                |              +/-?             |                        |
|    Reactor             |              +                |              -                |                        |
|    Rediscala           |              +                |              -                |                        |
|    RMI    Java         |              +                |              -                |                        |
|    Spark Web Framework |              +                |              -                |                        |
|    Spring Data         |              +                |              -                |                        |
|    Spring Scheduling   |              +                |              -                |                        |
|    Spring Webflux      |              +                |              -                |                        |
|    Spymemcached        |              +                |              -                |                        |
|    Twilio              |              +                |              -                |                        |
|    RxJava              |              +                |              -                |                        |
|    Vert.x              |              +                |              -                |                        |
| Configuration          |       System.properties       |              file             | The maximum length of the string that you can use at the command prompt is 2047 characters. OT might cause an issue if you need wery specific solution.  |
| Runtime configuration  |              -                |              +                |                        |
| Remote  configuration  |              -                |              + (JSON)         |                        |
| Modular                |          Build only           |     delete/include modules    |                        |
| Enable/Dissable instrumentation|          ? pre run/ system properties           |     remote REST/ configuration    |                        |
| Enable/Dissable instrumentation|          ? pre run/ system properties           |     remote REST/ configuration    |                        |
| StackTrace             |              -                |              +                |                         |
| Community              | Numerous                      | Small                         |                         |
| Documentation          | +                             | +                             |                         |
| Logging                |         System.out            | Text file/module separated    |                         |
| Supported JAVA         |              8                |              8                | RemoraJ would't complain instrumenting older java compiled files, but runtime Java 8 is must. It's not clear either OT would allow pre java 8 compiled files                         |
| Logging                |         System.out            | Text file/module separated    |                         |

