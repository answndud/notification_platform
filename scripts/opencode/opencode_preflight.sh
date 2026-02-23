#!/usr/bin/env bash
set -euo pipefail

WORKTREE="${1:-$(pwd)}"
WORKTREE_REAL="$(python3 -c 'import os,sys; print(os.path.realpath(sys.argv[1]))' "$WORKTREE")"
PWD_REAL="$(pwd -P)"

echo "worktree: $WORKTREE_REAL"

if [[ "$PWD_REAL" != "$WORKTREE_REAL" ]]; then
  echo "WARN: current shell path differs from realpath"
  echo "- shell: $PWD_REAL"
  echo "- target: $WORKTREE_REAL"
fi

if ! git -C "$WORKTREE_REAL" rev-parse --is-inside-work-tree >/dev/null 2>&1; then
  echo "ERROR: not a git worktree: $WORKTREE_REAL" >&2
  exit 2
fi

if [[ -z "$(git -C "$WORKTREE_REAL" remote)" ]]; then
  echo "WARN: no git remote configured"
fi

if ! git -C "$WORKTREE_REAL" rev-parse --abbrev-ref --symbolic-full-name '@{u}' >/dev/null 2>&1; then
  echo "WARN: upstream tracking branch is not set"
fi

python3 "$(dirname "$0")/opencode_project_guard.py" --worktree "$WORKTREE_REAL"
