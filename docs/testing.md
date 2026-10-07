# Testing with SparkServerExtension

`spark-core` includes a JUnit Jupiter extension, `spark.testing.SparkServerExtension`, that starts a
Spark server for your tests and stops it again afterward. It's handy for functional tests of code
that makes HTTP calls: stand up a server with the routes you need, point your client at it, and
assert on the results.

JUnit Jupiter is an *optional* dependency of `spark-core`, so it isn't brought in transitively. Tests
that use the extension already have it on their classpath.

## Starting a server

Annotate the test class with `@ExtendWith(SparkServerExtension.class)` and declare a `SparkStarter`
parameter on a lifecycle method. Call `runSpark` with a lambda that configures the `Service` — it
ignites a new server, passes it to your lambda, and returns once the server has initialized.

```java
import static org.assertj.core.api.Assertions.assertThat;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import spark.testing.SparkServerExtension;
import spark.testing.SparkServerExtension.SparkStarter;

@ExtendWith(SparkServerExtension.class)
class PingTest {

    @BeforeAll
    static void startServer(SparkStarter starter) {
        starter.runSpark(http -> {
            http.get("/ping", (request, response) -> "pong");
            http.get("/health", (request, response) -> "healthy");
        });
    }

    @Test
    void shouldRespondToPing() throws Exception {
        var request = HttpRequest.newBuilder(URI.create("http://localhost:4567/ping")).build();

        var response = HttpClient.newHttpClient().send(request, HttpResponse.BodyHandlers.ofString());

        assertThat(response.statusCode()).isEqualTo(200);
        assertThat(response.body()).isEqualTo("pong");
    }
}
```

Spark listens on port 4567 by default, which is why the client above uses it.

## Choosing the lifecycle

Where you declare the `SparkStarter` parameter decides how long the server lives. The server is
stopped automatically when that scope ends.

| Declare it in | Server lives for |
|---|---|
| a static `@BeforeAll` method | all the tests in the class |
| a `@BeforeEach` method | one test; a new server is started for each test |
| a `@Test` method | that test only |

## Port, address, and TLS

The lambda receives the `Service`, so use the usual configuration methods:

```java
starter.runSpark(http -> {
    http.ipAddress("127.0.0.1");
    http.port(9876);
    http.get("/ping", (request, response) -> "pong");
});
```

To serve HTTPS, configure a keystore (your client then has to trust its certificate):

```java
starter.runSpark(https -> {
    https.ipAddress("127.0.0.1");
    https.port(9876);
    https.secure(keystorePath, keystorePassword, null, null);
    https.get("/ping", (request, response) -> "pong");
});
```

Choose a fixed port that is free on your machines. Random ports (`port(0)`) are not reliable yet;
see [#283](https://github.com/dsingley/spark/issues/283).

## Coming from sparkjava-testing

This is the same extension as the one in the archived
[sparkjava-testing](https://github.com/sleberknight/sparkjava-testing) project, moved into
`spark-core`:

- `JavaSparkRunnerExtension` is now `spark.testing.SparkServerExtension`; `SparkStarter` is still its
  nested class.
- You no longer need a separate dependency; use `spark-core`.
- The JUnit 4 rule from that project was not ported.
