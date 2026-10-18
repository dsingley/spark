# CLAUDE.md

Guidance for Claude Code sessions working in this repository.

## What this repo is

The `dsingley/spark` repository: a fork of the original, now-abandoned
[perwendel/spark](https://github.com/perwendel/spark) — a micro web framework for
Java. Published as `com.dsingley.sparkjava:spark-core`.

## Branching

- **`main` is the default branch** — all current work happens here, and PRs target
  it. GitHub's `Closes #N` / `Fixes #N` auto-close works normally since this is the
  default branch.
- **`legacy`** is the old `master` branch, kept for history. Don't base work on it.
  (Both were renamed on 2026-10-18; `main` used to be called `ossrh`.)
- Releases to Maven Central are cut from `main` by the repository owner, using
  `maven-central-deploy.sh`; don't bump the pom version or tag a release unprompted.
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
  not yet merged into `main`, its branch is based on the dependency's branch (a
  stacked PR) rather than waiting.
- The core goal is minimizing changes visible to applications that depend on
  `spark-core` — see the roadmap's external-impact ledger for what is and isn't
  expected to break.

## Build

`mvn clean verify` — requires JDK 17+.
