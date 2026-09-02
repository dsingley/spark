# Examples

Runnable examples live under [`src/test/java/spark/examples/`](../src/test/java/spark/examples/).
They aren't shipped in the `spark-core` jar — they're part of this repo's own test sources, a
sandbox for trying out the framework's features while working on it.

## Running an example

Every example is a plain class with a `main` method. Run one with:

```
mvn exec:java -Dexec.mainClass=<fully.qualified.ClassName>
```

for example:

```
mvn exec:java -Dexec.mainClass=spark.examples.hello.HelloWorld
```

This starts an embedded server on `localhost:4567` (unless noted otherwise below). Leave it
running and issue the `curl` commands listed for that example in another terminal; stop the
server with `Ctrl+C` when done.

Don't add `-q` — Maven's quiet mode also silences the example's own log output (`before`/`after`
filter logging in particular relies on it).

## Hello, World

| | |
|---|---|
| Class | `spark.examples.hello.HelloWorld` |
| What it shows | The smallest possible Spark app: one route. |

```
curl http://localhost:4567/hello
```

## Simple example

| | |
|---|---|
| Class | `spark.examples.simple.SimpleExample` |
| What it shows | GET/POST, path params, status codes, `halt`, and redirects. |

```
curl http://localhost:4567/hello
curl -X POST -d "body text" http://localhost:4567/hello
curl http://localhost:4567/private              # 401
curl http://localhost:4567/users/scott
curl http://localhost:4567/news/world            # XML response
curl http://localhost:4567/protected             # 403, via halt()
curl -L http://localhost:4567/redirect           # follows to /news/world
curl http://localhost:4567/
```

## Secure (TLS) examples

| | |
|---|---|
| Classes | `spark.examples.simple.SimpleSecureExample`, `spark.examples.hello.HelloSecureWorld` |
| What they show | Serving over HTTPS with a self-signed cert. |

Both print the generated keystore/truststore/CA locations and a ready-to-run
`curl --cacert ...` command on startup, e.g.:

```
Keystore:   /tmp/spark-test-default-keystore4523423452345234523/keystore (password: key_pass_1234567890123)
Truststore: /tmp/spark-test-default-keystore4523423452345234523/truststore (password: trust_pass_1234567890124)
CA cert:    /tmp/ca9876543219876543219.pem

Try: curl --cacert /tmp/ca9876543219876543219.pem https://localhost:4567/hello
```

Copy the actual `curl --cacert ...` line from the console rather than the placeholder paths
above — the keystore/truststore/CA are freshly generated per run, in a new temp location
each time.

`--cacert` (not `-k`/`--insecure`) is what lets `curl` verify the self-signed cert properly
instead of just ignoring TLS errors; `--cacert` needs the CA's certificate specifically, not
the keystore or truststore (those are Java-only formats `curl` can't read directly), which is
why the CA cert location is printed separately.

`HelloSecureWorld` also accepts a real keystore, as `keystoreFile keystorePassword` args —
in which case there's no separate CA file, so `curl` needs `-k` instead:

```
mvn exec:java -Dexec.mainClass=spark.examples.hello.HelloSecureWorld -Dexec.args="/path/to/keystore.p12 changeit"
curl -k https://localhost:4567/hello
```

## Content negotiation

| | |
|---|---|
| Class | `spark.examples.accept.JsonAcceptTypeExample` |
| What it shows | Routing on the `Accept` header rather than just the path. |

```
curl -H "Accept: application/json" http://localhost:4567/hello   # 200, JSON
curl -H "Accept: text/html" http://localhost:4567/hello          # 404, no matching route
```

## Response transformers

| | |
|---|---|
| Classes | `spark.examples.transformer.TransformerExample`, `spark.examples.transformer.DefaultTransformerExample` |
| What they show | Converting route return values to JSON via a `ResponseTransformer`, and setting a default transformer for all routes (with a per-route override). |

```
curl -H "Accept: application/json" http://localhost:4567/hello
```

For `DefaultTransformerExample`, also try `/hello2`, which overrides the default transformer:

```
curl -H "Accept: application/json" http://localhost:4567/hello2
```

## Static files

| | |
|---|---|
| Class | `spark.examples.staticresources.StaticResources` |
| What it shows | Serving static files from the classpath (`src/test/resources/public`) alongside regular routes. |

```
curl http://localhost:4567/hello
curl http://localhost:4567/page.html
```

## Template engine (FreeMarker)

| | |
|---|---|
| Class | `spark.examples.templateview.FreeMarkerExample` |
| What it shows | Rendering a view with `modelAndView(...)` and a custom `TemplateEngine`. |

```
curl http://localhost:4567/hello
```

## Sessions

| | |
|---|---|
| Class | `spark.examples.session.SessionExample` |
| What it shows | Reading and writing session attributes; needs a cookie jar to see the stateful behavior. |

```
curl -c /tmp/spark-cookies http://localhost:4567/
curl -b /tmp/spark-cookies -c /tmp/spark-cookies -X POST --data "name=Scott" http://localhost:4567/entry
curl -b /tmp/spark-cookies http://localhost:4567/      # remembers "Scott"
curl -b /tmp/spark-cookies http://localhost:4567/clear
curl -b /tmp/spark-cookies http://localhost:4567/      # forgotten again
```

## Filters

| | |
|---|---|
| Classes | `spark.examples.filter.FilterExample`, `spark.examples.filter.FilterExampleAttributes`, `spark.examples.filter.FilterExampleWildcard`, `spark.examples.filter.DummyFilter` |
| What they show | `before`/`after` filters: a toy auth check, passing data between filters and a route via request attributes, path-wildcard filters, and (for `DummyFilter`) filters that log on every request regardless of route. |

`FilterExample` — a before-filter checks `user`/`password` query params:

```
curl "http://localhost:4567/hello?user=some&password=guy"   # 401
curl -D - "http://localhost:4567/hello?user=foo&password=bar" -o /dev/null   # 200, extra headers
```

`FilterExampleAttributes`:

```
curl http://localhost:4567/hi
```

`FilterExampleWildcard` — every path under `/protected/*` is blocked, even though no route is
registered there:

```
curl http://localhost:4567/protected/anything   # 401
```

`DummyFilter` registers global `before`/`after` filters (they run on every request,
regardless of path or route) alongside a single `/hello` route — watch the **server
console** for the "Before"/"After" log lines as you curl:

```
curl http://localhost:4567/hello      # 200, and check the console for "Before" / "After"
curl http://localhost:4567/anything   # 404, but the filters still logged
```

## Multiple services

| | |
|---|---|
| Class | `spark.examples.multiple.MultipleServices` |
| What it shows | Running two independent `Service` instances (via `Service.ignite()`) in one process, on different ports. |

```
curl http://localhost:4567/hello
curl http://localhost:1234/hello
curl -L http://localhost:1234/hi        # redirects to /hello
```

## WebSockets

| | |
|---|---|
| Class | `spark.examples.websocket.WebSocketExample` |
| What it shows | `webSocket(...)` routes: a plain echo endpoint and a ping/pong endpoint. |

`curl` doesn't make a good WebSocket client here — use a browser's dev console instead:

```js
const ws = new WebSocket("ws://localhost:4567/echo");
ws.onmessage = e => console.log(e.data);
ws.onopen = () => ws.send("hello ws");
```

or point it at `ws://localhost:4567/ping` and send `"PING"` to get back `"PONG"`.

If you have Node.js installed (22+; it needs a global `WebSocket`, added in 22), the same
three lines work in a `node` REPL, then send messages with `ws.send(...)`:

```js
const ws = new WebSocket("ws://127.0.0.1:4567/echo");
ws.onmessage = e => console.log(e.data);
ws.onopen = () => ws.send("hello ws");
> ws.send("hola")
```

Use `127.0.0.1`, not `localhost`, for the Node version — Node's `WebSocket` resolves
`localhost` to `::1` and doesn't fall back to IPv4 the way browsers do, so it fails to
connect since this server only listens on the IPv4 wildcard address.

## Gzip

| | |
|---|---|
| Class | `spark.examples.gzip.GzipExample` |
| What it shows | Serving gzip-encoded content. |

On startup it makes one request to itself and prints the decompressed response, then stays up
like every other example so you can try it yourself:

```
curl http://localhost:4567/hello
curl -H "Accept-Encoding: gzip" --compressed http://localhost:4567/zipped
```

## REST resource (Books)

| | |
|---|---|
| Class | `spark.examples.books.Books` |
| What it shows | A small CRUD REST API over an in-memory map. |

```
ID=$(curl -s -X POST "http://localhost:4567/books?author=Orwell&title=1984")
curl http://localhost:4567/books/$ID
curl -X PUT "http://localhost:4567/books/$ID?title=Animal+Farm"
curl http://localhost:4567/books/$ID
curl -X DELETE http://localhost:4567/books/$ID
curl http://localhost:4567/books/$ID   # 404, deleted
```

## Syntactic sugar

| | |
|---|---|
| Class | `spark.examples.sugar.SugarExample` |
| What it shows | Extending `Spark` (see `spark.examples.sugar.http`) to add a route-definition shorthand. |

```
curl http://localhost:4567/hi
curl http://localhost:4567/hello
```
