#!/usr/bin/env python3
"""Audit and optionally repair OpenCode project/session mapping.

This tool is designed to prevent and quickly recover from duplicated
`project.worktree` rows that hide sessions in the UI due to project_id splits.
"""

from __future__ import annotations

import argparse
import datetime as dt
import os
import shutil
import sqlite3
import subprocess
import sys
from pathlib import Path


DEFAULT_DB = Path("~/.local/share/opencode/opencode.db").expanduser()


def realpath_str(path: str) -> str:
    return str(Path(path).expanduser().resolve())


def sqlite_connect(db_path: Path) -> sqlite3.Connection:
    if not db_path.exists():
        raise FileNotFoundError(f"OpenCode DB not found: {db_path}")
    conn = sqlite3.connect(str(db_path))
    conn.row_factory = sqlite3.Row
    return conn


def git_ok(worktree: str) -> tuple[bool, list[str]]:
    notes: list[str] = []

    def run_git(args: list[str]) -> tuple[int, str]:
        proc = subprocess.run(
            ["git", *args],
            cwd=worktree,
            stdout=subprocess.PIPE,
            stderr=subprocess.PIPE,
            text=True,
            check=False,
        )
        return proc.returncode, proc.stdout.strip() or proc.stderr.strip()

    code, out = run_git(["rev-parse", "--is-inside-work-tree"])
    if code != 0 or out != "true":
        notes.append("not a git worktree")
        return False, notes

    code, out = run_git(["remote"])
    remotes = [line for line in out.splitlines() if line.strip()] if code == 0 else []
    if not remotes:
        notes.append("no git remote configured")

    code, out = run_git(["rev-parse", "--abbrev-ref", "--symbolic-full-name", "@{u}"])
    if code != 0:
        notes.append("upstream tracking branch is not set")

    return len(notes) == 0, notes


def get_scalar(conn: sqlite3.Connection, sql: str, params: tuple = ()) -> int:
    cur = conn.execute(sql, params)
    row = cur.fetchone()
    return int(row[0]) if row else 0


def fetch_all(conn: sqlite3.Connection, sql: str, params: tuple = ()) -> list[sqlite3.Row]:
    return conn.execute(sql, params).fetchall()


def backup_db(db_path: Path) -> Path:
    ts = dt.datetime.now().strftime("%Y%m%d-%H%M%S")
    backup_path = db_path.with_name(f"{db_path.name}.{ts}.bak")
    shutil.copy2(db_path, backup_path)
    return backup_path


def audit(conn: sqlite3.Connection, worktree: str) -> dict:
    stats = {
        "session_count": get_scalar(conn, "SELECT COUNT(*) FROM session"),
        "message_count": get_scalar(conn, "SELECT COUNT(*) FROM message"),
        "project_count": get_scalar(conn, "SELECT COUNT(*) FROM project"),
    }

    duplicates = fetch_all(
        conn,
        """
        SELECT worktree, COUNT(*) AS cnt
        FROM project
        GROUP BY worktree
        HAVING COUNT(*) > 1
        ORDER BY cnt DESC, worktree ASC
        """,
    )

    current_rows = fetch_all(
        conn,
        """
        SELECT p.id AS project_id, p.worktree,
               COUNT(s.id) AS session_count
        FROM project p
        LEFT JOIN session s ON s.project_id = p.id
        WHERE p.worktree = ?
        GROUP BY p.id, p.worktree
        ORDER BY session_count DESC, project_id ASC
        """,
        (worktree,),
    )

    prefix = f"{worktree}%"
    by_project_for_dir = fetch_all(
        conn,
        """
        SELECT s.project_id, COUNT(*) AS session_count
        FROM session s
        WHERE s.directory LIKE ?
        GROUP BY s.project_id
        ORDER BY session_count DESC, s.project_id ASC
        """,
        (prefix,),
    )

    return {
        "stats": stats,
        "duplicates": duplicates,
        "current_rows": current_rows,
        "by_project_for_dir": by_project_for_dir,
    }


def choose_target_project_id(
    current_rows: list[sqlite3.Row],
    explicit_target: str | None,
) -> str:
    if explicit_target:
        ids = {str(row["project_id"]) for row in current_rows}
        if ids and explicit_target not in ids:
            raise ValueError(
                "--target-project-id is not among current worktree project rows"
            )
        return explicit_target

    if len(current_rows) != 1:
        raise ValueError(
            "cannot infer target project_id: pass --target-project-id explicitly"
        )
    return str(current_rows[0]["project_id"])


def repair(conn: sqlite3.Connection, worktree: str, target_project_id: str) -> int:
    prefix = f"{worktree}%"
    cur = conn.execute(
        """
        UPDATE session
           SET project_id = ?
         WHERE directory LIKE ?
           AND project_id <> ?
        """,
        (target_project_id, prefix, target_project_id),
    )
    conn.commit()
    return cur.rowcount


def print_audit(report: dict, worktree: str, git_notes: list[str]) -> None:
    print(f"worktree: {worktree}")
    print(
        "counts: "
        f"session={report['stats']['session_count']} "
        f"message={report['stats']['message_count']} "
        f"project={report['stats']['project_count']}"
    )

    if git_notes:
        print("git_identity: WARN")
        for note in git_notes:
            print(f"- {note}")
    else:
        print("git_identity: OK")

    if report["duplicates"]:
        print("worktree_duplicates: WARN")
        for row in report["duplicates"]:
            print(f"- {row['worktree']} ({row['cnt']})")
    else:
        print("worktree_duplicates: OK")

    if report["current_rows"]:
        print("current_worktree_projects:")
        for row in report["current_rows"]:
            print(f"- {row['project_id']} sessions={row['session_count']}")
    else:
        print("current_worktree_projects: NONE")

    if report["by_project_for_dir"]:
        print("session_distribution_for_directory_prefix:")
        for row in report["by_project_for_dir"]:
            print(f"- {row['project_id']} sessions={row['session_count']}")
    else:
        print("session_distribution_for_directory_prefix: NONE")


def main() -> int:
    parser = argparse.ArgumentParser(
        description="Audit/repair OpenCode project_id split by worktree duplication"
    )
    parser.add_argument(
        "--db",
        default=str(DEFAULT_DB),
        help="Path to opencode.db (default: ~/.local/share/opencode/opencode.db)",
    )
    parser.add_argument(
        "--worktree",
        default=os.getcwd(),
        help="Project worktree path to audit (default: current directory)",
    )
    parser.add_argument(
        "--repair",
        action="store_true",
        help="Repair session.project_id by directory prefix (makes DB backup first)",
    )
    parser.add_argument(
        "--target-project-id",
        help="Project id to map sessions to during --repair",
    )
    args = parser.parse_args()

    db_path = Path(args.db).expanduser().resolve()
    worktree = realpath_str(args.worktree)

    git_clean, git_notes = git_ok(worktree)

    try:
        conn = sqlite_connect(db_path)
    except FileNotFoundError as exc:
        print(str(exc), file=sys.stderr)
        return 2

    report = audit(conn, worktree)
    print_audit(report, worktree, git_notes)

    has_duplicates = bool(report["duplicates"])
    split_detected = len(report["by_project_for_dir"]) > 1
    has_issue = (not git_clean) or has_duplicates or split_detected

    if args.repair:
        try:
            target = choose_target_project_id(
                report["current_rows"], args.target_project_id
            )
        except ValueError as exc:
            print(f"repair_error: {exc}", file=sys.stderr)
            return 2

        backup_path = backup_db(db_path)
        changed = repair(conn, worktree, target)
        print(f"backup_created: {backup_path}")
        print(f"repair_target_project_id: {target}")
        print(f"sessions_relinked: {changed}")

        report = audit(conn, worktree)
        print("post_repair_check:")
        print_audit(report, worktree, git_notes)
        split_detected = len(report["by_project_for_dir"]) > 1
        has_issue = (not git_clean) or bool(report["duplicates"]) or split_detected

    conn.close()
    return 1 if has_issue else 0


if __name__ == "__main__":
    raise SystemExit(main())
