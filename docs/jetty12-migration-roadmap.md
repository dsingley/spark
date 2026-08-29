# Jetty 12 Migration Roadmap

*Archived from the planning artifact used at the start of this migration. Kept here as a
historical record of what was planned and why — see the "Status" line on each phase for what
actually happened, which in places differs from the original per-issue-per-PR granularity below.
For the consumer-facing summary of what changed, see [MIGRATING.md](../MIGRATING.md).*

A phased plan for moving `spark-core`'s embedded server from Jetty 9.4 to Jetty 12, with a
Java 17 baseline, while keeping the break in applications that use it as small and as
clearly-scoped as possible.

**Core strategy:** almost everything that breaks lives inside `spark.embeddedserver.jetty.*` and
can be rewritten without any consumer ever noticing. The one change that can't be avoided is the
servlet namespace itself — `javax.servlet` → `jakarta.servlet` — because Jetty 12 no longer ships
the old one. That single, well-documented break is worth taking deliberately (as a 3.0.0) rather
than working around with a legacy shim.

## §1 What actually reaches consuming applications

*the thing to optimize for*

Almost every Spark app runs as an uber jar — `java -jar myapp.jar` — with Spark's embedded Jetty
bundled inside it. There is no separate host container for those apps to be compatible with; they
simply get whatever Jetty/EE version `spark-core`'s own `pom.xml` pulls in. Container
compatibility is only a question for the minority of apps using `SparkFilter` to run inside
someone else's servlet container.

**Untouched — recompiles as-is:**

- The routing DSL — `get()`, `post()`, `before()`, `after()`, `path()`, `halt()`
- `Request`'s fluent API — `params()`, `queryParams()`, `body()`, `headers()`, `attribute()`, `splat()`
- `Response`'s fluent API — `status()`, `type()`, `header()`, `redirect()`, `cookie()`
- `Session`'s fluent API — `attribute()`, `invalidate()`, `id()`, `isNew()`
- Static file config — `staticFiles.location()`, `externalLocation()`
- `exception()` handlers, `Service.secure(String…)` (keystore-path overload)

**Changes — narrow and named:**

- Minimum JDK moves 8 → **17** (Jetty 12 requires it outright)
- `request.raw()` / `response.raw()` / `session.raw()` now return `jakarta.servlet.*` types
- `Service.secure(SslContextFactory)` becomes `Service.secure(SslContextFactory.Server)`
- `@WebSocket`-annotated / `WebSocketListener` handler classes need a recompile against Jetty 12's websocket artifact
- `SparkFilter` / `web.xml` deployment (a minority path) needs a Jakarta EE 11-capable host container — the default embedded/uber-jar model has no host container to satisfy

| Who's affected | What changes | How to soften it |
|---|---|---|
| Everyone | Runtime/build JDK must be 17+ | Call out prominently in release notes; can't be avoided, Jetty 12 itself requires it |
| Apps calling `.raw()` | Import swaps from `javax.servlet.*` to `jakarta.servlet.*` wherever the raw object is used | Mechanical find/replace; document the exact package rename in the migration guide |
| Custom-SSL apps | `SslContextFactory` → `SslContextFactory.Server` | One-line signature change; only hits apps using the `SslContextFactory` overload of `secure()` |
| WebSocket apps | Handler classes recompile against new Jetty websocket artifact; annotation/listener shape should survive conceptually | Confirm exact surface in the Phase 3 spike before promising more than "recompile" |
| WAR / `web.xml` apps only | Host container must support Jakarta EE 11 (Tomcat 11+, Jetty 12.1 ee11, etc.) | The minority path — most Spark apps run embedded as an uber jar with no host container to satisfy at all. Document the requirement next to `SparkFilter` |

> **Retrospective note:** the WebSocket break turned out larger than "recompile" for one case —
> `WebSocketListener` was removed entirely in Jetty 12 with no replacement, so that handler style
> requires a real rewrite to the `@WebSocket`-annotated style, not just a recompile. See
> [MIGRATING.md](../MIGRATING.md) for the confirmed, final list of breaks.

## §2 Which servlet environment to target

*jetty 12.1 ships ee8 – ee11*

Jetty 12 splits the servlet layer into swappable "EE" environments, and as of the 12.1.x line all
four — ee8, ee9, ee10, and ee11 — ship side by side in one release. That matters now
specifically: as of January 1, 2026, the Jetty project stopped publishing the old 9.x/10.x/11.x
lines to Maven Central, so 12.1.x is the only actively maintained line to build against
regardless of which EE is chosen. This decision drives whether the break above is
`javax→jakarta` or nothing at all — worth deciding deliberately, not by default.

**ee8 · legacy shim — Keep javax.servlet** — *not recommended*
`.raw()` keeps returning `javax.servlet.*` — zero import break for consumers. But it's an
explicitly transitional compatibility layer tied to Jakarta EE 8's end of life, not where a new
2026 release should live, and it doesn't remove the Handler rewrite in §3 anyway.

**ee11 · current — Move to jakarta.servlet 6.1** — ✅ *recommended, and chosen*
The newest environment in the actively maintained 12.1.x line — Servlet 6.1, still just Java 17
baseline. One clean, well-documented namespace break instead of building on a shim with a posted
expiry date. Since ee10 (Servlet 6.0) ships in the same release line, it's a low-cost fallback if
any transitive dependency (a websocket module, a container a consumer runs `SparkFilter` under)
hasn't caught up to ee11 yet.

**jetty core · no servlet api — Drop the servlet API** — *rejected*
Rebuild `Request`/`Response` against Jetty's own core types directly, no EE layer at all. Faster
internally, but `.raw()` would return a non-standard type instead of a familiar one, and
`SparkFilter`'s WAR deployment mode stops being possible entirely.

## §3 Phased roadmap

*Originally scoped as 7 phases, 26 issues, one PR per issue. In practice, Phase 1 and Phase 2
turned out to be compile-coupled — fixing Phase 1's namespace break required touching
`JettyHandler` immediately, which is Phase 2's own architectural rewrite — so with the user's
explicit sign-off ("we can work with real feedback"), Phases 1 and 2 landed together in one PR
(#11) driven by the compiler's own errors as a checklist, rather than issue-by-issue. Phases 4
and 5 (5.1–5.3) turned out to already be complete as a side effect of that same PR when audited
during Phase 6. Actual status is noted per phase below.*

### Phase 0 — Baseline & guardrails

Get the toolchain and CI onto Java 17 first, and pin down exactly which Jetty 12 artifacts to
target, before touching application code.

| # | Type | Title | Notes |
|---|---|---|---|
| 0.1 | external · JDK17 | Raise the Java baseline to 17 | Bump `java.version`, the compiler plugin source/target, and the enforcer's `requireJavaVersion` rule in `pom.xml`. |
| 0.2 | internal | Move CI to a JDK 17/21 matrix | Update `setup-java` in the push workflow off JDK 8; consider testing 17 and 21 side by side. |
| 0.3 | spike | Pin exact Jetty 12.1 artifact coordinates | Confirm the current jetty-bom version (12.1.x — the only line still publishing to Maven Central since Jan 2026) and the ee11 module set (servlet, webapp, websocket) to depend on. |

**Status:** ✅ Done — issues #1, #2, #3.

### Phase 1 — Servlet namespace migration

The one deliberate external break. Mechanical in most files; the public-API three (`Request`,
`Response`, `Session`) are where it actually surfaces.

| # | Type | Title | Notes |
|---|---|---|---|
| 1.1 | internal | Add EE11 dependencies to the POM | Bring in `jakarta.servlet-api` 6.1 and the ee11 servlet/webapp Jetty modules; drop the plain `jetty-webapp` that resolves javax today. |
| 1.2 | external · raw() | Migrate the public API surface to jakarta.servlet | `Request.raw()`, `Response.raw()`, `Session.raw()` switch return type. The headline change in the migration guide. |
| 1.3 | internal | Sweep internal plumbing to jakarta.servlet | Mechanical rename across the request/response pipeline — invisible to consumers since none of these types are public return values. |
| 1.4 | external · WAR | Migrate the web.xml deployment path | `SparkFilter` and `FilterTools` move to jakarta.servlet, same ee11 baseline as the rest of the codebase. |

**Status:** ✅ Done — issues #7, #8, #9, #10, all landed via PR #11.

### Phase 2 — Embedded Jetty core rewrite

The real architectural work. Jetty 12's `Handler` no longer exposes the internal hook Spark
currently overrides to inject its filter — this has to be redesigned, not renamed.

| # | Type | Title | Notes |
|---|---|---|---|
| 2.1 | internal · highest risk | Redesign JettyHandler's dispatch mechanism | `SessionHandler.doHandle(...)` is gone. Replaced with a real `jakarta.servlet.Filter` registration on an ee11 `ServletContextHandler`. |
| 2.2 | internal | Port EmbeddedJettyServer's handler wiring | `HandlerList` is removed; moved to Jetty 12's `Handler.Sequence`. |
| 2.3 | external · SSL API | Move SocketConnectorFactory to SslContextFactory.Server | The unqualified `SslContextFactory` class is gone. |
| 2.4 | internal | Verify Utf8Appendable's Jetty dependency | Confirmed `org.eclipse.jetty.util.Utf8Appendable.NotUtf8Exception` still resolves at the same coordinates. |

**Status:** ✅ Done — no separate issues were filed for this phase; it landed directly in PR #11
alongside Phase 1, per the compile-coupling discovery above.

### Phase 3 — WebSocket layer rewrite

The wiring is Jetty-9-specific throughout; the annotation/listener programming model consumers
write against should survive conceptually, but that needed confirming before it was promised.

| # | Type | Title | Notes |
|---|---|---|---|
| 3.1 | spike | Map the Jetty 9 → 12 websocket API | Found the Jetty 12 equivalents of `WebSocketListener`, `@WebSocket`, `WebSocketCreator`, `WebSocketUpgradeFilter`. |
| 3.2 | internal | Rewrite the websocket context wiring | Reimplemented how upgrade handling mounts onto the ee11 `ServletContextHandler`. |
| 3.3 | external · WebSocket | Confirm and document the handler-class break | Verified: `WebSocketListener` was removed entirely with no replacement (real rewrite required); `@WebSocket`-annotated handlers need one rename (`@OnWebSocketConnect` → `@OnWebSocketOpen`). |
| 3.4 | internal | Update websocket fixtures and examples | Ported the echo/ping example handlers and the test client/handler. |

**Status:** ✅ Done — issue #12, landed via PR #19 plus a follow-up gap-fill PR #20 (the example
files and an idle-timeout test that #19 initially missed).

### Phase 4 — Static files & resource serving

Smaller surface, but Jetty's resource-serving types have also moved and this path uses
`RequestDispatcher` directly.

| # | Type | Title | Notes |
|---|---|---|---|
| 4.1 | internal | Migrate resource handling | Update `AbstractResourceHandler` and `StaticFilesConfiguration` for jakarta.servlet. |

**Status:** ✅ Done — no separate issue filed. Verified complete when auditing Phase 6: this path
never actually depended on Jetty's own resource types (Spark has its own
`AbstractFileResolvingResource`/`ClassPathResourceHandler`), and the jakarta.servlet migration
landed as part of PR #11.

### Phase 5 — Test suite modernization

Every test that exercises the Jetty layer directly needs rewriting alongside it. Since these
files are being touched regardless, it's the cheapest place to start retiring PowerMock — as a
follow-on, not scope creep on this plan.

| # | Type | Title | Notes |
|---|---|---|---|
| 5.1 | internal | Rewrite the embedded-server unit tests | `JettyServerTest`, `SocketConnectorFactoryTest`, `EmbeddedJettyFactoryTest` against the new Handler/connector/SSL APIs. |
| 5.2 | internal | Rewrite the websocket unit tests | Against the new websocket wiring from Phase 3. |
| 5.3 | internal | Rewrite integration & servlet-mode tests | `GenericIntegrationTest`, `ServletTest`, `EmbeddedServersTest`. |
| 5.4 | optional | Drop PowerMock in the files touched above | Opportunistic, not required. |

**Status:** 5.1–5.3 ✅ done, no separate issues filed — landed as a side effect of PR #11's
rewrite, same as Phase 2/4. 5.4 is tracked separately as [BACKLOG] issue #22 (open) since it's
explicitly optional and unrelated to the migration itself.

### Phase 6 — Verification & release

Prove it end to end, then ship the break as a clearly-versioned, well-documented event rather
than a surprise.

| # | Type | Title | Notes |
|---|---|---|---|
| 6.1 | internal | Full pass: embedded, WAR, websocket, SSL | Manual smoke test of all four deployment shapes, including a real `java -jar` uber jar. |
| 6.2 | internal | Recheck the OSGi bundle manifest | Confirm `maven-bundle-plugin`'s `Import-Package` still generates correctly. |
| 6.3 | external · docs | Write the consumer migration guide | JDK 17 minimum, the `javax→jakarta` rename, `SslContextFactory.Server`, the websocket changes, the `web.xml` container requirement. |
| 6.4 | external · versioning | Release as a new major version | Ship the Jetty 12 line as e.g. 3.0.0. |

**Status:** 6.1 ✅ done — issue #23. 6.2 ✅ done — issue #24. 6.3 ✅ done — issue #25, landed via
PR #28 as [MIGRATING.md](../MIGRATING.md). 6.4 still open — issue #26.

---

*Investigated against `ossrh` @ jetty 9.4.58.v20250814 → target jetty 12.1.x (ee11), sparkjava
fork `com.dsingley.sparkjava:spark-core`. Originally scoped as 26 issues across 7 phases.*
