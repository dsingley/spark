# Third-Party Code Origins

Some classes in `spark-core` are not original Spark code. They were copied (and in most
cases renamed or trimmed) from other open-source projects, before those projects were
usable as ordinary Maven dependencies for whatever reason at the time. This is a working
inventory of those classes: where each one actually came from, and whether the copy still
matches its upstream shape closely enough that a future major release could replace it
with a real dependency instead of maintaining a fork of it in this codebase.

This is not a migration plan and nothing here is scheduled. It exists so that decision
doesn't have to start from a fresh source-diffing exercise every time it comes up.

## How a class ended up in this list

A class is listed here if its Javadoc, header comment, or `@author` tags show it was
copied from another project's source, as opposed to code Spark's own authors wrote from
scratch (most of the codebase carries a `Copyright ... Per Wendel` header, which is
Spark's own original copyright, not a third-party one, and isn't listed here).

## Eclipse Jetty

Copied from Jetty (Mort Bay Consulting copyright headers, dual EPL v1.0 / Apache 2.0
licensed). Jetty is already a runtime dependency of this project (it's what backs the
embedded server), so these are prime candidates for replacement with direct calls into
`jetty-util` / `jetty-server` if their upstream equivalents still cover the same surface.

| File | Upstream class | Notes |
|---|---|---|
| `utils/urldecoding/TypeUtil.java` | `org.eclipse.jetty.util.TypeUtil` | Hex parsing helpers only; upstream class is much larger. Has an `@implNote` (PR #259). |
| `utils/urldecoding/UrlDecode.java` | `org.eclipse.jetty.util.URIUtil` (`decodePath` methods) | Extracted and renamed (`decodePath` -> `path`). Has an `@implNote` (this session). `decodeISO88591Path` was removed entirely in PR #264, matching Jetty's own current approach. |
| `utils/urldecoding/Utf8Appendable.java` | `org.eclipse.jetty.util.Utf8Appendable` | No `@implNote` yet. |
| `utils/urldecoding/Utf8StringBuilder.java` | `org.eclipse.jetty.util.Utf8StringBuilder` | No `@implNote` yet. |
| `resource/AbstractResourceHandler.java` | Jetty resource-handling source (unattributed to a specific class in the header) | Header says "Code snippets copied from the Eclipse Jetty source. Modifications made by Per Wendel." No `@implNote` yet; upstream class not yet pinned down. |
| `resource/ClassPathResourceHandler.java` | same as above | Same header pattern. |
| `resource/ExternalResourceHandler.java` | same as above | Same header pattern. |

## Spring Framework

Copied from Spring (`Copyright 2002-201x the original author or authors`, Apache 2.0
licensed, `@author` tags naming Spring core committers - Juergen Hoeller, Keith Donald,
Rob Harrop, Sam Brannen). Spark does not currently depend on Spring at all, so replacing
these would mean adding `spring-core` (or a similar minimal artifact) as a new dependency
rather than reusing one that's already there - a bigger tradeoff than the Jetty group.

| File | Upstream class | Notes |
|---|---|---|
| `utils/StringUtils.java` | `org.springframework.util.StringUtils` | Subset of the upstream API. |
| `utils/CollectionUtils.java` | `org.springframework.util.CollectionUtils` | |
| `utils/ClassUtils.java` | `org.springframework.util.ClassUtils` | |
| `utils/Assert.java` | `org.springframework.util.Assert` | |
| `utils/ObjectUtils.java` | `org.springframework.util.ObjectUtils` | |
| `utils/ResourceUtils.java` | `org.springframework.util.ResourceUtils` | |
| `resource/AbstractFileResolvingResource.java` | `org.springframework.core.io.AbstractFileResolvingResource` | |
| `resource/AbstractResource.java` | `org.springframework.core.io.AbstractResource` | |
| `resource/Resource.java` | `org.springframework.core.io.Resource` | |
| `resource/ClassPathResource.java` | `org.springframework.core.io.ClassPathResource` | |
| `resource/InputStreamResource.java` | `org.springframework.core.io.InputStreamResource` | |

## Apache Commons

Copied from Apache Commons IO (ASF license header - "Licensed to the Apache Software
Foundation" - rather than a literal `Copyright` line, plus `@author`/`@version`/`@since`
tags naming Commons IO committers like Stephen Colebourne and pinning `Commons IO 1.1`/
`2.2`). Spark does not currently depend on Commons IO, so replacing this would mean
adding it as a new dependency, the same tradeoff as the Spring group below.

| File | Upstream class | Notes |
|---|---|---|
| `utils/IOUtils.java` | `org.apache.commons.io.IOUtils` | Subset of the upstream API. No `@implNote` yet. |

## None of these are the Jetty 12 migration

This list is unrelated to the [Jetty 12 migration roadmap](jetty12-migration-roadmap.md).
That initiative is about the embedded server's Jetty version and servlet API generation.
This list is about whether small, forked-and-frozen utility classes should eventually be
replaced by real dependencies on the libraries they were copied from - a separate,
unscheduled decision, most realistically revisited around a 4.0.0 release given the
external-API impact either replacement could have.
