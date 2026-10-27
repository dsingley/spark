Spark - a tiny web framework for Java
======================================

[![Maven Central](https://img.shields.io/maven-central/v/com.dsingley.sparkjava/spark-core)](https://central.sonatype.com/artifact/com.dsingley.sparkjava/spark-core)
[![Build](https://github.com/dsingley/spark/actions/workflows/push.yml/badge.svg?branch=main)](https://github.com/dsingley/spark/actions/workflows/push.yml)
[![License](https://img.shields.io/badge/license-Apache%202.0-blue)](LICENSE)
[![Java 17+](https://img.shields.io/badge/Java-17%2B-blue)](https://adoptium.net/)

```xml
<dependency>
    <groupId>com.dsingley.sparkjava</groupId>
    <artifactId>spark-core</artifactId>
    <version>[current-version]</version>
</dependency>
```

Find the latest version on [Maven Central](https://central.sonatype.com/artifact/com.dsingley.sparkjava/spark-core). Requires Java 17+. The embedded server is Jetty 12.1 (EE11, `jakarta.servlet`).

A maintained fork of [perwendel/spark](https://github.com/perwendel/spark).

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

The static `Spark` class (`import static spark.Spark.*;`) still works and is fine for small apps and
tutorials. For production applications we recommend `Service`, because:

- it has no global state, so tests can start their own server on a free port
- the `Service` is an object you can pass to your code, so routes and their dependencies are easy to swap in tests
- it is clearer what is going on: the server is created, configured and stopped explicitly

Documentation
-------------

- [User guide](docs/user-guide.md) — routes, filters, sessions, static files, WebSockets, HTTPS, and more
- [Examples](docs/examples.md) — runnable example applications
- [Testing](docs/testing.md) — `spark.testing.SparkServerExtension`, a JUnit Jupiter extension that starts and stops a server for your tests
- [Migrating to 3.0.0](MIGRATING.md) — upgrade notes
