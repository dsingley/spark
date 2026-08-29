# CLAUDE.md

Guidance for Claude Code sessions working in this repository.

## What this repo is

A fork of [dsingley/spark](https://github.com/dsingley/spark), itself a fork of the
original, now-abandoned [perwendel/spark](https://github.com/perwendel/spark) — a
micro web framework for Java. Published as `com.dsingley.sparkjava:spark-core`.

## Branching

- **`ossrh` is this fork's default branch** — all current work happens here, and it's
  the branch dsingley intends to eventually use for a Maven Central (OSSRH) release.
  PRs target `ossrh`. GitHub's `Closes #N` / `Fixes #N` auto-close works normally
  since this is the default branch.
- The upstream repo, [dsingley/spark](https://github.com/dsingley/spark), still has
  `master` as its default branch — that's independent of this fork's setting, so keep
  it in mind when eventually opening a PR back upstream (the base branch there may
  not be `ossrh`).
- **Actually cutting an OSSRH/Maven Central release can only happen from upstream**,
  not this fork — the `com.dsingley.sparkjava` groupId and OSSRH publishing rights
  belong to dsingley's account. This fork can prepare everything short of that (version
  bumps, changelogs, verifying the release plugin config), but the real `mvn deploy`
  to Central has to run after this fork's work merges back upstream.
- This repo is configured for **squash merges only**, with the squash commit message
  defaulting to "PR title and description" — so PR titles and descriptions should be
  written as the commit message they'll become.

## Current initiative: Jetty 9 → 12 / Java 17 migration

This repo is mid-migration from Jetty 9.4 (javax.servlet, Java 8) to Jetty 12.1
(jakarta.servlet, EE11, Java 17). Full plan, phase breakdown, and rationale:
[Jetty 12 Migration Roadmap](docs/jetty12-migration-roadmap.md).

Working conventions for this migration:
- One GitHub issue per roadmap item, titled `[Jetty 12 · Phase N.M] <description>`.
- Issues are filed incrementally, phase by phase, not all up front — later phases
  depend on findings from earlier spikes and would otherwise need rewriting.
- One PR per issue, kept small and single-concern. When an issue depends on another
  not yet merged into `ossrh`, its branch is based on the dependency's branch (a
  stacked PR) rather than waiting.
- The core goal is minimizing changes visible to applications that depend on
  `spark-core` — see the roadmap's external-impact ledger for what is and isn't
  expected to break.

## Build

`mvn clean verify` — requires JDK 17+. The surefire plugin's `argLine` in `pom.xml`
carries JVM flags required for the existing Mockito/PowerMock test stack to run under
JDK 17+; see the comment there before removing them.
