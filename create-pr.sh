#!/usr/bin/env bash
#
# create-pr.sh — open a PR against dsingley/spark's ossrh branch
#
# Solves the recurring GitHub UI annoyance where "Compare & pull request"
# defaults to the upstream fork (apache/spark or similar) and master
# instead of dsingley/spark + ossrh. This script always targets the
# right repo/branch explicitly via `gh pr create`.
#
# Usage:
#   ./create-pr.sh                              # uses current branch, --fill for title/body
#   ./create-pr.sh -t "My title" -b "My body"    # explicit title/body
#   ./create-pr.sh -t "My title"                 # title only, opens $EDITOR for body
#   ./create-pr.sh --draft                       # open as a draft PR
#   ./create-pr.sh -a sleberknight -m 3.0.0 -l "code cleanup"  # defaults shown explicitly
#   ./create-pr.sh -l ""                         # pass "" to omit assignee/milestone/label
#
# Env overrides (useful once ossrh is renamed to main):
#   TARGET_REPO=dsingley/spark
#   TARGET_BASE=ossrh
#   ASSIGNEE=sleberknight,dsingley
#   MILESTONE=3.0.0
#   LABEL="code cleanup"

set -euo pipefail

TARGET_REPO="${TARGET_REPO:-dsingley/spark}"
TARGET_BASE="${TARGET_BASE:-ossrh}"
ASSIGNEE="${ASSIGNEE:-sleberknight,dsingley}"
MILESTONE="${MILESTONE:-3.0.0}"
LABEL="${LABEL:-code cleanup}"

TITLE=""
BODY=""
DRAFT=""
HEAD_BRANCH=""

usage() {
  grep '^#' "$0" | sed -e 's/^#!\?//' -e 's/^ //'
  exit 1
}

while [[ $# -gt 0 ]]; do
  case "$1" in
    -t|--title)
      TITLE="$2"; shift 2 ;;
    -b|--body)
      BODY="$2"; shift 2 ;;
    --head)
      HEAD_BRANCH="$2"; shift 2 ;;
    --draft)
      DRAFT="--draft"; shift ;;
    -a|--assignee)
      ASSIGNEE="$2"; shift 2 ;;
    -m|--milestone)
      MILESTONE="$2"; shift 2 ;;
    -l|--label)
      LABEL="$2"; shift 2 ;;
    -h|--help)
      usage ;;
    *)
      echo "Unknown option: $1" >&2
      usage ;;
  esac
done

if ! command -v gh &> /dev/null; then
  echo "Error: GitHub CLI ('gh') is not installed. See https://cli.github.com" >&2
  exit 1
fi

if [[ -z "$HEAD_BRANCH" ]]; then
  HEAD_BRANCH="$(git rev-parse --abbrev-ref HEAD)"
fi

if [[ "$HEAD_BRANCH" == "$TARGET_BASE" ]]; then
  echo "Error: current branch is '$HEAD_BRANCH', same as base branch '$TARGET_BASE'." >&2
  echo "Check out your feature branch first." >&2
  exit 1
fi

CMD=(gh pr create --repo "$TARGET_REPO" --base "$TARGET_BASE" --head "$HEAD_BRANCH")

if [[ -n "$DRAFT" ]]; then
  CMD+=("$DRAFT")
fi

if [[ -n "$ASSIGNEE" ]]; then
  CMD+=(--assignee "$ASSIGNEE")
fi

if [[ -n "$MILESTONE" ]]; then
  CMD+=(--milestone "$MILESTONE")
fi

if [[ -n "$LABEL" ]]; then
  CMD+=(--label "$LABEL")
fi

if [[ -n "$TITLE" ]]; then
  CMD+=(--title "$TITLE")
  if [[ -n "$BODY" ]]; then
    CMD+=(--body "$BODY")
  fi
  # else: gh will open $EDITOR for the body
else
  # No title given: auto-fill from commit(s) on this branch
  CMD+=(--fill)
fi

echo "Running: ${CMD[*]}"
exec "${CMD[@]}"

