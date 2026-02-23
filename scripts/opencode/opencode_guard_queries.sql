-- OpenCode DB quick inspection queries

-- 1) Global counts
SELECT
  (SELECT COUNT(*) FROM session) AS session_count,
  (SELECT COUNT(*) FROM message) AS message_count,
  (SELECT COUNT(*) FROM project) AS project_count;

-- 2) Duplicate worktrees in project table
SELECT worktree, COUNT(*) AS duplicate_count
FROM project
GROUP BY worktree
HAVING COUNT(*) > 1
ORDER BY duplicate_count DESC, worktree ASC;

-- 3) For one worktree prefix, check session split by project_id
-- Replace '/absolute/worktree/path' with your realpath.
SELECT project_id, COUNT(*) AS session_count
FROM session
WHERE directory LIKE '/absolute/worktree/path%'
GROUP BY project_id
ORDER BY session_count DESC, project_id ASC;
