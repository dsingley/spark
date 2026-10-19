# Migrating to 3.0.0

Version 3.0.0 moves the embedded server from Jetty 9.4 (`javax.servlet`, Java 8) to Jetty 12.1
(`jakarta.servlet`, EE11, Java 17). The migration was scoped to minimize externally-visible
breakage, but a few changes are unavoidable — Jetty 12 doesn't ship the old namespace, and it
requires a newer JDK outright. This page covers every change that can affect application code.

Everything else — the routing DSL, filters, sessions, exception mapping — is unchanged. New
features are described in the release notes.

## JDK 17 minimum

Jetty 12 requires Java 17 at a minimum. Applications running on Java 8–16 must upgrade their
JDK before adopting this version.

## `javax.servlet` → `jakarta.servlet`

The one deliberate namespace break. If your code calls `.raw()` on a Spark `Request`,
`Response`, or `Session` and imports the returned servlet type, switch that import:

```java
// before
import javax.servlet.http.HttpServletRequest;
HttpServletRequest raw = request.raw();

// after
import jakarta.servlet.http.HttpServletRequest;
HttpServletRequest raw = request.raw();
```

The same applies to `Response.raw()` (→ `jakarta.servlet.http.HttpServletResponse`) and
`Session.raw()` (→ `jakarta.servlet.http.HttpSession`).

Nothing else about the fluent `Request`/`Response`/`Session` API (`params()`, `body()`,
`status()`, `attribute()`, etc.) changed — this only matters if you drop down to the raw
servlet type.

## `SslContextFactory` → `SslContextFactory.Server`

Jetty 12 removed the unqualified `SslContextFactory` class. If you build one directly to pass to
`Service.secure(SslContextFactory.Server)`, use the nested `.Server` type, and note it now only
has a no-arg constructor — set the keystore path via a setter instead of a constructor argument:

```java
// before
SslContextFactory sslContextFactory = new SslContextFactory(keystorePath);

// after
SslContextFactory.Server sslContextFactory = new SslContextFactory.Server();
sslContextFactory.setKeyStorePath(keystorePath);
```

If you use the string-based `Service.secure(keystoreFile, keystorePassword, ...)` overloads
instead, nothing changes — Spark builds the `SslContextFactory.Server` for you.

## WebSocket handlers

- **`WebSocketListener`-style handlers have no direct replacement.** Jetty 12 removed the
  `org.eclipse.jetty.websocket.api.WebSocketListener` interface entirely. If your handler
  implements it, rewrite it to the `@WebSocket`-annotated style below — there's no drop-in
  substitute.
- **`@WebSocket`-annotated handlers need one rename**: `@OnWebSocketConnect` is now
  `@OnWebSocketOpen`. Every other callback annotation (`@OnWebSocketMessage`,
  `@OnWebSocketClose`, `@OnWebSocketError`, `@OnWebSocketFrame`) is unchanged.

```java
// before
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketConnect;

@OnWebSocketConnect
public void onConnect(Session session) { ... }

// after
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketOpen;

@OnWebSocketOpen
public void onConnect(Session session) { ... }
```

`Service.webSocket(...)`'s own signature is unchanged — it still takes a handler class or
instance.

- **A handler *class* now gets a new instance for each connection.** Before, `webSocket(path,
  HandlerClass.class)` created one instance when the path was mapped and used it for every
  connection to that path, so instance fields were shared between clients. Now each connection gets
  its own instance, as the class form suggests, and the class needs an accessible no-argument
  constructor, which is checked when the path is mapped: a class that is abstract or lacks one now fails
  there with an `IllegalArgumentException`, where before it failed with a `RuntimeException` when the
  server started. If your handler relied on that sharing,
  keep the state in a field that outlives the handler (a `static` field, or something you pass in), or
  register an instance with `webSocket(path, handler)`, which still uses the one instance for every
  connection and therefore has to be thread-safe.

## Static files: no `Expires` header

`staticFiles.expireTime(seconds)` now sends only `Cache-Control: private, max-age=<seconds>`. It used to
send an `Expires` header as well, but one computed once when `expireTime` was called, so it was in the
past by the time the configured time had gone by. HTTP/1.1 clients and caches ignore `Expires` when
`max-age` is present, so they are not affected. If something depends on seeing an `Expires` header, add
your own with `staticFiles.header("Expires", value)`.

## MIME type settings are per `Service`

`staticFiles.registerMimeType(...)` and `staticFiles.disableMimeTypeGuessing()` used to change a
static field, so setting them on one `Service` (or through the static `Spark` API) changed the `Content-Type`
of static files for every `Service` in the JVM, including ones created later. They now apply only to the
`Service` they were called on, like the rest of the `staticFiles` settings. If you run several services and
relied on one call applying to all of them, make the call on each service.

The public static methods `MimeType.register(...)`, `MimeType.disableGuessing()` and
`MimeType.fromResource(...)` are gone, because the registered mappings no longer live in `MimeType`. Use the
`staticFiles` methods instead.

When Spark runs as a filter in a servlet container (`SparkFilter`), all services share one static files
configuration, as they do for every other `staticFiles` setting, so the settings are then shared too.

## `web.xml` deployment (`SparkFilter`)

If you deploy Spark as a filter in `web.xml` rather than the embedded server, your servlet
container needs to support Jakarta Servlet 6.1 (e.g. Tomcat 11+, or another EE11-capable
container). This deployment mode is uncommon — most Spark applications run embedded as a
standalone `java -jar` uber jar and are unaffected by this requirement.

## Other public servlet types

Every other public method that took or returned a `javax.servlet` type now uses the `jakarta.servlet`
type, for example `SparkFilter`, `MatcherFilter`, `QueryParamsMap`, `RequestResponseFactory` and
`StaticFilesConfiguration.consume`. Application code rarely calls these directly.

## Custom embedded servers

If you plug in your own `EmbeddedServer` (with `EmbeddedServers.add`), note that:

- `ignite(...)` now takes a `SslContextFactory.Server` where it took a `SslContextFactory`.
- `configureWebSockets(Map, Optional<Long>)` is deprecated in favor of `configureWebSockets(Map, Long)`,
  which takes `null` for no idle timeout. The old method still exists.

The Jetty-specific classes in `spark.embeddedserver.jetty` (`EmbeddedJettyServer`, `JettyHandler`,
`HttpRequestWrapper` and so on) were rewritten for Jetty 12, and their constructors and overridden methods
use the Jetty 12 types. They are internal, so code that extends or calls them directly has to be
adapted.

## Internal classes and utility methods

A few public classes and methods that were only meant for Spark's own use were narrowed or removed.
Application code that uses Spark's documented API is not affected:

- `spark.http.matching.Halt` is now package-private.
- `spark.embeddedserver.jetty.websocket.WebSocketServletContextHandlerFactory` is removed.
- The utility classes in `spark.utils`, plus `TypeUtil` and `UrlDecode`, have private constructors, and these
  unused methods and constants are removed: `Assert.hasLength` and `Assert.notEmpty(Object[], String)`,
  `ClassUtils.forName`, `ClassUtils.resolvePrimitiveClassName` and `ClassUtils.ARRAY_SUFFIX`,
  `IOUtils.LINE_SEPARATOR`, `ResourceUtils.getFile`, `ResourceUtils.getURL` and
  `ResourceUtils.URL_PROTOCOL_VFSZIP`, `StringUtils.isNotEmpty` and `TypeUtil.toString(byte[], int)`.
- `RequestWrapper` and `ResponseWrapper` no longer override `equals` and `hashCode`.

## Deprecations

Still available, but deprecated in 3.0.0:

- The `MatcherFilter` constructor with the unused `externalContainer` parameter; use the constructor that
  takes an `UnmatchedRequestHandling`.
- `StaticFilesFolder`, which is scheduled for removal in 4.0.0.
- `configureWebSockets(Map, Optional<Long>)` on `EmbeddedServer` and `EmbeddedJettyServer`, as described
  above.

## No OSGi bundle

The jar is now packaged as a plain jar. It no longer carries the OSGi manifest headers it used to have, so
it cannot be used as an OSGi bundle directly.
