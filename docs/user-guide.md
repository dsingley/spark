# User guide

A tour of the main features of `spark-core`. Every snippet assumes `http` is a `Service`, created as
shown in [Getting started](#getting-started). Runnable versions of most of these are described in
[examples.md](examples.md), and testing is covered in [testing.md](testing.md).

- [Getting started](#getting-started)
- [Routes](#routes)
- [Request and response](#request-and-response)
- [Halting and redirecting](#halting-and-redirecting)
- [Filters](#filters)
- [Grouping routes](#grouping-routes)
- [Exception handling and error pages](#exception-handling-and-error-pages)
- [Content negotiation](#content-negotiation)
- [Response transformers](#response-transformers)
- [Templates](#templates)
- [Sessions and cookies](#sessions-and-cookies)
- [Static files](#static-files)
- [WebSockets](#websockets)
- [HTTPS](#https)
- [Server configuration](#server-configuration)
- [The static API](#the-static-api)
- [Testing](#testing)

## Getting started

Create a `Service` with `Service.ignite()`, configure it, and add routes. The server starts when the
first route is added (or when you call `init()`), and it runs on its own threads, so `main` returns
immediately.

```java
var http = Service.ignite();
http.port(8080);
http.get("/hello", (request, response) -> "Hello World!");
http.awaitInitialization();   // optional: block until the server is accepting connections
// ...
http.stop();                  // stop the server
```

Configuration such as the port must be set *before* the first route is added. In a real application,
read the port from your own configuration (a file, an environment variable, a system property) rather
than hard-coding it; the [examples](examples.md) read it from the `port` system property.

## Routes

A route is an HTTP method, a path, and a handler. The handler receives a `Request` and a `Response`
and returns the response body.

```java
http.get("/hello", (request, response) -> "Hello World!");
http.post("/hello", (request, response) -> "Hello World: " + request.body());
http.put("/hello", (request, response) -> "put");
http.delete("/hello", (request, response) -> "delete");
http.patch("/hello", (request, response) -> "patch");
http.head("/hello", (request, response) -> "");
http.options("/hello", (request, response) -> "options");
```

Routes are matched in the order they were added. Paths can contain named parameters and wildcards:

```java
http.get("/users/:name", (request, response) -> "Selected user: " + request.params(":name"));

http.get("/say/*/to/*", (request, response) ->
        "Say " + request.splat()[0] + " to " + request.splat()[1]);
```

To send an empty body, return an empty string. A route that returns `null` without redirecting or
halting is treated as not found and responds with a 404.

## Request and response

`Request` gives access to everything the client sent:

```java
http.post("/echo", (request, response) -> {
    var method = request.requestMethod();          // POST
    var path = request.pathInfo();                 // /echo
    var body = request.body();                     // raw request body
    var name = request.queryParams("name");        // ?name=...
    var agent = request.headers("User-Agent");     // request header
    var type = request.contentType();
    return body;
});
```

`Response` controls what is sent back:

```java
http.get("/xml", (request, response) -> {
    response.status(201);
    response.type("text/xml");
    response.header("X-Custom", "value");
    return "<?xml version=\"1.0\" encoding=\"UTF-8\"?><news>world</news>";
});
```

Request attributes carry data between filters and routes for the duration of one request:

```java
http.before((request, response) -> request.attribute("startedAt", System.nanoTime()));
http.get("/timed", (request, response) -> "started at " + request.attribute("startedAt"));
```

## Halting and redirecting

`halt` stops processing immediately and sends the given status and body. It works from routes and
filters.

```java
http.get("/protected", (request, response) -> {
    http.halt(403, "I don't think so!!!");
    return null;
});
```

`response.redirect` sends a redirect:

```java
http.get("/redirect", (request, response) -> {
    response.redirect("/news/world");
    return null;
});
```

To redirect without writing a handler, use the `redirect` helper:

```java
http.redirect.get("/old-path", "/new-path");
http.redirect.any("/hi", "/hello");
```

## Filters

`before` filters run before the matching route and `after` filters run after it. Without a path they
apply to every request; with a path (wildcards allowed) only to matching requests.

```java
http.before((request, response) -> {
    if (request.queryParams("user") == null) {
        http.halt(401, "You are not welcome here");
    }
});

http.before("/hello", (request, response) -> response.header("Foo", "Set by second before filter"));

http.after("/hello", (request, response) -> response.header("spark", "added by after-filter"));
```

Several filters can be mapped in one call, and run in the order given:

```java
http.before("/hello", authenticate, logRequest);
```

`afterAfter` filters run after everything else, including `after` filters and exception handling,
which makes them a good place for logging or for headers that must always be present.

## Grouping routes

`path` groups routes under a common prefix. Groups can be nested.

```java
http.path("/api", () -> {
    http.before("/*", (request, response) -> response.header("X-Api", "true"));
    http.path("/books", () -> {
        http.get("", (request, response) -> "all books");
        http.get("/:id", (request, response) -> "book " + request.params(":id"));
    });
});
```

## Exception handling and error pages

Map an exception type to a handler. Handlers are looked up by exception class, including superclasses.

```java
http.exception(IllegalArgumentException.class, (exception, request, response) -> {
    response.status(400);
    response.body("Bad request: " + exception.getMessage());
});
```

Customize the built-in 404 and 500 responses with a page or a route:

```java
http.notFound((request, response) -> "Sorry, there is nothing here.");
http.internalServerError("<html><body>Something went wrong.</body></html>");
```

## Content negotiation

Give a route an accept type and it matches only requests whose `Accept` header allows it. The same
path can have different routes for different types.

```java
http.get("/hello", "application/json", (request, response) -> "{\"message\": \"Hello World\"}");
http.get("/hello", "text/html", (request, response) -> "<h1>Hello World</h1>");
```

## Response transformers

A `ResponseTransformer` turns the value a route returns into the response body, which is how you
return objects as JSON. `spark-core` has no JSON dependency, so you supply the transformer using
whatever library you like:

```java
ResponseTransformer json = model -> new Gson().toJson(model);

http.get("/hello", "application/json", (request, response) -> new Message("Hello World"), json);
```

To use one for every route, set a default; a route can still pass its own to override it. The
default applies only to routes added after the call, so set it first:

```java
http.defaultResponseTransformer(json);   // json is the transformer defined above
http.get("/hello", "application/json", (request, response) -> new Message("Hello World"));
```

## Templates

Return a `ModelAndView` from a route and pass a `TemplateEngine` to render it. `spark-core` does not
bundle any template libraries; implement `TemplateEngine` for the one you use (the
[FreeMarker example](examples.md#template-engine-freemarker) shows one).

```java
http.get("/hello", (request, response) -> {
    var attributes = Map.of("message", "Hello FreeMarker World");
    return new ModelAndView(attributes, "hello.ftl");
}, new FreeMarkerTemplateEngine());
```

## Sessions and cookies

```java
http.get("/", (request, response) -> {
    String name = request.session().attribute("username");
    return name == null ? "Who are you?" : "Hello, " + name + "!";
});

http.post("/entry", (request, response) -> {
    request.session().attribute("username", request.queryParams("name"));
    response.redirect("/");
    return null;
});
```

`request.session()` creates a session if there isn't one; use `request.session(false)` to get the
existing session or `null`. Cookies:

```java
http.get("/cookies", (request, response) -> {
    var seen = request.cookie("visited");
    response.cookie("visited", "yes", 3600);     // name, value, max age in seconds
    response.removeCookie("old");
    return "seen before: " + seen;
});
```

## Static files

Serve files from the classpath, or from a directory on disk. Static files are checked first: a
request for an existing file is served without running any filters or routes, so don't rely on a
`before` filter (for authentication, say) to protect them.

```java
http.staticFiles.location("/public");                  // classpath folder
http.staticFiles.externalLocation("/var/www/public");  // folder on disk
```

Both must be set before the first route is added.

## WebSockets

A WebSocket handler is a class annotated with Jetty's `@WebSocket`. Register it before the server
starts, then call `init()` since a service with only WebSocket handlers has no route to start it.

```java
http.webSocket("/echo", EchoWebSocket.class);
http.webSocket("/ping", PingWebSocket.class);
http.init();
```

See `EchoWebSocket` in the [examples](examples.md#websockets) for a complete handler.

## HTTPS

Pass a keystore (and optionally a truststore) to `secure`, before adding routes:

```java
http.secure("/path/to/keystore.jks", "keystorePassword", null, null);
```

For full control, including wanting but not needing client certificates, pass a Jetty
`SslContextFactory.Server`:

```java
var sslContextFactory = new SslContextFactory.Server();
sslContextFactory.setKeyStorePath("/path/to/keystore.jks");
sslContextFactory.setKeyStorePassword("keystorePassword");
sslContextFactory.setWantClientAuth(true);
http.secure(sslContextFactory);
```

## Server configuration

All of these must be called before the first route is added (or before `init()`):

```java
http.ipAddress("127.0.0.1");     // bind address; default is all interfaces
http.port(8080);                 // default is 4567; 0 picks a free port, readable with http.port() after awaitInitialization()
http.threadPool(40);             // max threads; or threadPool(max, min, idleTimeoutMillis)
```

### Behind a load balancer

By default a server only knows the address of whatever connected to it, so behind a load balancer
`request.ip()` is the load balancer's address, not the client's. Two standard mechanisms pass the
real address along, and both work with no configuration:

- **The proxy protocol (v1 and v2).** A TCP load balancer, such as an AWS network load balancer,
  sends a short preamble with the client's address before the HTTP request. Every connector accepts
  it, and `request.ip()` returns that address. A connection with no preamble works as usual. This
  cannot be turned off.
- **The `X-Forwarded-For` header.** A load balancer that understands HTTP, such as an AWS application
  load balancer, adds the header, and `request.ip()` returns it. This is on by default; call
  `http.untrustForwardHeaders()` before adding routes to ignore these headers.

See the [proxy protocol example](examples.md#proxy-protocol), which you can run and try with curl.

Both are trusted from whoever sends them, so a client that can reach the application directly can
claim any address. If you rely on `request.ip()` for something like an allow-list, make the
application reachable only through the load balancer, for example with security groups.

## The static API

The static `Spark` class (`import static spark.Spark.*;`) still works, and is convenient for tiny
apps and tutorials:

```java
import static spark.Spark.*;

public class HelloWorld {
    public static void main(String[] args) {
        get("/hello", (request, response) -> "Hello World!");
    }
}
```

The static API is a single global server. For production applications we recommend `Service`:

- **No global state.** Each test can start its own server on a free port, which is what lets a whole test suite share one JVM.
- **Easy to inject and swap.** Put your routes in a method or class that receives the `Service`, and tests can pass in whatever they need.
- **Clearer code.** The server is an object you create, configure and stop explicitly; there is no hidden singleton or `import static`.
- **More than one server per JVM.** For example, a public port plus an admin or metrics port, each with its own port, TLS and thread pool.
- **Independent lifecycles.** You can stop one `Service` and ignite another without touching shared global state.
- **Fits your application's lifecycle.** Create the `Service` from your configuration or DI container and stop it from a shutdown hook.

## Testing

`spark.testing.SparkServerExtension` starts a `Service` on a free port, once per test class when registered in a `static` field, and stops
it afterwards. See [testing.md](testing.md).
