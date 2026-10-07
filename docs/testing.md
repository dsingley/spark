# Testing with SparkServerExtension

`spark-core` includes a JUnit Jupiter extension, `spark.testing.SparkServerExtension`, that starts a
Spark server for your tests and stops it again afterward. It's handy for functional tests of code
that makes HTTP calls: stand up a server with the routes you need, point your client at it, and
assert on the results.

JUnit Jupiter is an *optional* dependency of `spark-core`, so it isn't brought in transitively. Tests
that use the extension already have it on their classpath.

There are two ways to use it: register the extension in a field, or have a `SparkStarter` injected
into a test method.

## Registering the extension

Create a `SparkServerExtension` with a `ServiceInitializer` — a lambda that configures the `Service` —
and register it with `@RegisterExtension`. The server is started before the test class's own
`@BeforeAll` or `@BeforeEach` methods run, and `port()` and `service()` give you access to it.

```java
import static org.assertj.core.api.Assertions.assertThat;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import spark.testing.SparkServerExtension;

class RegisteredPingTest {

    @RegisterExtension
    static final SparkServerExtension SPARK = new SparkServerExtension(http -> {
        http.get("/ping", (request, response) -> "pong");
        http.get("/health", (request, response) -> "healthy");
    });

    @Test
    void shouldRespondToPing() throws Exception {
        var uri = URI.create("http://localhost:" + SPARK.port() + "/ping");
        var request = HttpRequest.newBuilder(uri).build();

        var response = HttpClient.newHttpClient().send(request, HttpResponse.BodyHandlers.ofString());

        assertThat(response.statusCode()).isEqualTo(200);
        assertThat(response.body()).isEqualTo("pong");
    }
}
```

Whether the field is static decides how long the server lives, just like `@ClassRule` and `@Rule` in
JUnit 4:

| Field | Server lives for |
|---|---|
| `static` | all the tests in the class (and its `@Nested` classes) |
| instance | one test; a new server is started for each test |

If the class uses `@TestInstance(Lifecycle.PER_CLASS)` there is only one test instance, so an instance
field behaves like a static one: all the tests share a single server.

## Injecting a SparkStarter

Alternatively, annotate the class with `@ExtendWith(SparkServerExtension.class)` and declare a
`SparkStarter` parameter on a lifecycle or test method. Call `runSpark` with the same kind of lambda;
it ignites a new server, passes it to your lambda, and returns once the server has initialized.
`SparkStarter` also has `port()` and `service()`.

```java
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.extension.ExtendWith;
import spark.testing.SparkServerExtension;
import spark.testing.SparkServerExtension.SparkStarter;

@ExtendWith(SparkServerExtension.class)
class InjectedPingTest {

    @BeforeAll
    static void startServer(SparkStarter starter) throws Exception {
        starter.runSpark(http -> {
            http.get("/ping", (request, response) -> "pong");
        });
    }

    // tests...
}
```

Where you declare the parameter decides how long the server lives:

| Declare it in | Server lives for |
|---|---|
| a static `@BeforeAll` method | all the tests in the class |
| a `@BeforeEach` method | one test; a new server is started for each test |
| a `@Test` method | that test only |

Injection is the better fit when the configuration depends on something only available to a test
method, or when a single test needs to start its own server.

In both forms the server is stopped automatically when its scope ends, even if the lambda that
configures it throws, and the extension waits for the server to finish stopping before moving on, so
the next test can use the same port. A `SparkServerExtension` that was created with a lambda does not
also inject `SparkStarter` parameters.

## Configuring the server

The lambda is a `ServiceInitializer`, which receives the `Service` and may throw checked exceptions.
Use the usual configuration methods. The port and address must be set before the first route, and the
lambda must define at least one route, because that is what initializes the server:

```java
new SparkServerExtension(http -> {
    http.ipAddress("127.0.0.1");
    http.port(9876);
    http.get("/ping", (request, response) -> "pong");
});
```

To serve HTTPS, configure a keystore (your client then has to trust its certificate):

```java
new SparkServerExtension(https -> {
    https.ipAddress("127.0.0.1");
    https.port(9876);
    https.secure(keystorePath, keystorePassword, null, null);
    https.get("/ping", (request, response) -> "pong");
});
```

Spark listens on port 4567 by default. Choose a fixed port that is free on your machines; random
ports (`port(0)`) are not reliable yet, see [#283](https://github.com/dsingley/spark/issues/283).

## Coming from sparkjava-testing

This is the same extension as the one in the archived
[sparkjava-testing](https://github.com/sleberknight/sparkjava-testing) project, moved into
`spark-core`, with the JUnit 4 rule's style added:

- You no longer need a separate dependency; use `spark-core`.
- `JavaSparkRunnerExtension` is now `spark.testing.SparkServerExtension`, and `SparkStarter` is still its
  nested class. Injection works as before, except that `runSpark` takes a `ServiceInitializer`, which
  may throw, so lifecycle methods that call it may need `throws Exception`.
- The JUnit 4 `SparkServerRule` and its `ServiceInitializer` map to registering `SparkServerExtension`
  with `@RegisterExtension`: a static field replaces `@ClassRule`, and an instance field replaces `@Rule`.
