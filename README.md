Spark - a tiny web framework for Java
======================================

```xml
<dependency>
    <groupId>com.dsingley.sparkjava</groupId>
    <artifactId>spark-core</artifactId>
    <version>[current-version]</version>
</dependency>
```

Requires Java 17+. The embedded server is Jetty 12.1 (EE11, `jakarta.servlet`).

Getting started
---------------

```java
import spark.Service;

public class HelloWorld {

    public static void main(String[] args) {
        var http = Service.ignite();
        http.port(Integer.getInteger("port", 4567));   // or read the port from your own configuration

        http.get("/hello", (request, response) -> "Hello World!");

        http.awaitInitialization();
    }
}
```

Run it and visit http://localhost:4567/hello.

`Service.ignite()` creates an independent server, so an application can be configured, tested and
stopped without sharing global state. The static `Spark` class (`import static spark.Spark.*;`) still
works and suits small apps and tutorials, but real applications should use `Service`.

Documentation
-------------

- [User guide](docs/user-guide.md) — routes, filters, sessions, static files, WebSockets, HTTPS, and more
- [Examples](docs/examples.md) — runnable example applications
- [Testing](docs/testing.md) — `spark.testing.SparkServerExtension`, a JUnit Jupiter extension that starts and stops a server for your tests
- [Migrating to Jetty 12](MIGRATING.md) — upgrade notes

Forked from https://github.com/perwendel/spark
----------------------------------------------

Changes:

- a secure `Service` can be configured with a `SslContextFactory` instance to allow full TLS configuration (including wanting but not needing client certificates)
- Jetty connections are configured with [proxy protocol v2](https://www.haproxy.org/download/2.6/doc/proxy-protocol.txt) support to allow access to client IP addresses through AWS network load balancers
- migrated the embedded server to Jetty 12.1 (EE11); requires Java 17+ — see [MIGRATING.md](MIGRATING.md)
- includes a JUnit Jupiter extension, `spark.testing.SparkServerExtension`, for testing — see [docs/testing.md](docs/testing.md)
