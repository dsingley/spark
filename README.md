Spark - a tiny web framework for Java 8
=======================================

```xml
<dependency>
    <groupId>com.dsingley</groupId>
    <artifactId>spark-core</artifactId>
    <version>2.9.4-SNAPSHOT</version>
</dependency>
```

Forked from https://github.com/perwendel/spark

Changes:

- a secure <code>Service</code> can be configured with a <code>SslContextFactory</code> instance to allow full TLS configuration (including wanting but not needing client certificates)
- Jetty connections are configured with [proxy protocol v2](https://www.haproxy.org/download/2.6/doc/proxy-protocol.txt) support to allow access to client IP addresses through AWS network load balancers
- updated Jetty version 
