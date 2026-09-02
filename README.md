Spark - a tiny web framework for Java
======================================

```xml
<dependency>
    <groupId>com.dsingley.sparkjava</groupId>
    <artifactId>spark-core</artifactId>
    <version>[current-version]</version>
</dependency>
```

Forked from https://github.com/perwendel/spark

Changes:

- a secure <code>Service</code> can be configured with a <code>SslContextFactory</code> instance to allow full TLS configuration (including wanting but not needing client certificates)
- Jetty connections are configured with [proxy protocol v2](https://www.haproxy.org/download/2.6/doc/proxy-protocol.txt) support to allow access to client IP addresses through AWS network load balancers
- migrated the embedded server to Jetty 12.1 (EE11); requires Java 17+ — see [MIGRATING.md](MIGRATING.md) for upgrade notes 

Runnable examples covering routes, filters, sessions, static files, TLS, WebSockets, and more
live in this repo's test sources — see [docs/examples.md](docs/examples.md) for how to run them.
