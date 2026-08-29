# Migrating to Jetty 12

This fork's embedded server was migrated from Jetty 9.4 (`javax.servlet`, Java 8) to Jetty
12.1 (`jakarta.servlet`, EE11, Java 17). The migration was scoped to minimize externally-visible
breakage, but a few changes are unavoidable — Jetty 12 doesn't ship the old namespace, and it
requires a newer JDK outright. This page covers every change that can affect application code.

Everything else — the routing DSL, filters, static files, sessions, exception mapping — is
unchanged.

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

## `web.xml` deployment (`SparkFilter`)

If you deploy Spark as a filter in `web.xml` rather than the embedded server, your servlet
container needs to support Jakarta Servlet 6.1 (e.g. Tomcat 11+, or another EE11-capable
container). This deployment mode is uncommon — most Spark applications run embedded as a
standalone `java -jar` uber jar and are unaffected by this requirement.
