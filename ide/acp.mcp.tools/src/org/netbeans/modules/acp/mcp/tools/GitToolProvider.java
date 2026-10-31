/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.netbeans.modules.acp.mcp.tools;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ui.OpenProjects;
import org.netbeans.modules.acp.mcp.spi.McpToolProvider;
import org.netbeans.modules.acp.mcp.spi.ProjectPathGuard;
import org.netbeans.modules.acp.mcp.spi.ToolResult;
import org.openide.filesystems.FileUtil;
import org.openide.util.lookup.ServiceProvider;

@ServiceProvider(service = McpToolProvider.class)
public final class GitToolProvider implements McpToolProvider {

    private static final Logger LOG = Logger.getLogger(GitToolProvider.class.getName());
    private static final Pattern SAFE_REV = Pattern.compile("^[A-Za-z0-9._/~^:-]+$");

    @Override
    public boolean isEnabledByDefault() {
        return false;
    }

    @Override
    public List<McpToolDefinition> getTools() {
        List<McpToolDefinition> tools = new ArrayList<>();

        tools.add(new McpToolDefinition(
                "git_status",
                "Show git status of the repository.",
                Map.of(
                        "type", "object",
                        "properties", Map.of(
                                "repoDir", Map.of("type", "string",
                                        "description", "Absolute path to the repository root. "
                                        + "If not provided, uses the first Git repo found in open projects.")
                        )
                )
        ));

        tools.add(new McpToolDefinition(
                "git_diff",
                "Show git diff (staged, unstaged, or of a specific commit).",
                Map.of(
                        "type", "object",
                        "properties", Map.of(
                                "repoDir", Map.of("type", "string",
                                        "description", "Absolute path to the repository root."),
                                "target", Map.of("type", "string",
                                        "description", "e.g. 'HEAD', a commit hash, or 'staged'")
                        )
                )
        ));

        tools.add(new McpToolDefinition(
                "git_log",
                "Show the commit history of the repository (hash, date, author, subject per line).",
                Map.of(
                        "type", "object",
                        "properties", Map.of(
                                "repoDir", Map.of("type", "string",
                                        "description", "Absolute path to the repository root."),
                                "maxCount", Map.of("type", "integer",
                                        "description", "Maximum number of commits to return (default 20, max 1000)"),
                                "since", Map.of("type", "string",
                                        "description", "Only commits after this date, e.g. '2026-09-01' or '2 weeks ago'")
                        )
                )
        ));

        tools.add(new McpToolDefinition(
                "file_history",
                "Show the commit history of a single file, including each version's patch.",
                Map.of(
                        "type", "object",
                        "properties", Map.of(
                                "repoDir", Map.of("type", "string",
                                        "description", "Absolute path to the repository root."),
                                "path", Map.of("type", "string",
                                        "description", "Path to the file (absolute or relative to repo root)"),
                                "maxCount", Map.of("type", "integer",
                                        "description", "Maximum number of commits to return (default 10)")
                        ),
                        "required", List.of("path")
                )
        ));

        return tools;
    }

    @Override
    public Map<String, Object> executeTool(String name, Map<String, Object> arguments) throws Exception {
        LOG.log(Level.FINE, "Executing git tool: {0}", name);
        Map<String, Object> result = switch (name) {
            case "git_status" -> gitStatus(arguments);
            case "git_diff" -> gitDiff(arguments);
            case "git_log" -> gitLog(arguments);
            case "file_history" -> fileHistory(arguments);
            default -> throw new IllegalArgumentException("Unknown tool: " + name);
        };
        if ("error".equals(result.get("status"))) {
            LOG.log(Level.WARNING, "Git tool {0} failed: {1}", new Object[]{name, result.get("message")});
        } else {
            LOG.log(Level.FINE, "Git tool {0} succeeded", name);
        }
        return result;
    }

    private Map<String, Object> gitStatus(Map<String, Object> args) throws Exception {
        File repoDir = resolveRepoDir((String) args.get("repoDir"));
        if (repoDir == null) {
            return ToolResult.error("No git repository found in open projects");
        }
        return runGit(repoDir, "git", "status", "--short");
    }

    private Map<String, Object> gitDiff(Map<String, Object> args) throws Exception {
        File repoDir = resolveRepoDir((String) args.get("repoDir"));
        if (repoDir == null) {
            return ToolResult.error("No git repository found in open projects");
        }
        String target = (String) args.get("target");
        if ("staged".equalsIgnoreCase(target)) {
            return runGit(repoDir, "git", "diff", "--cached");
        }
        if (target == null || target.isBlank()) {
            return runGit(repoDir, "git", "diff");
        }
        if (!SAFE_REV.matcher(target).matches()) {
            return ToolResult.error("Invalid revision: " + target);
        }
        return runGit(repoDir, "git", "diff", target);
    }

    private Map<String, Object> gitLog(Map<String, Object> args) throws Exception {
        File repoDir = resolveRepoDir((String) args.get("repoDir"));
        if (repoDir == null) {
            return ToolResult.error("No git repository found in open projects");
        }
        int maxCount = 20;
        if (args.containsKey("maxCount")) {
            maxCount = Math.max(1, Math.min(1000, ((Number) args.get("maxCount")).intValue()));
        }
        String since = (String) args.get("since");

        List<String> command = new ArrayList<>(List.of("git", "log",
                "--no-color", "--date=iso-strict",
                "--pretty=format:%h%x09%ad%x09%an%x09%s",
                "--max-count=" + maxCount));
        if (since != null && !since.isBlank()) {
            if (!Pattern.matches("^[A-Za-z0-9 .:/_+~-]+$", since)) {
                return ToolResult.error("Invalid since value: " + since);
            }
            command.add("--since=" + since);
        }
        return runGit(repoDir, command.toArray(new String[0]));
    }

    private Map<String, Object> fileHistory(Map<String, Object> args) throws Exception {
        File repoDir = resolveRepoDir((String) args.get("repoDir"));
        if (repoDir == null) {
            return ToolResult.error("No git repository found in open projects");
        }
        String filePath = (String) args.get("path");
        if (filePath == null || filePath.isBlank()) {
            return ToolResult.error("path is required");
        }
        int maxCount = 10;
        if (args.containsKey("maxCount")) {
            maxCount = Math.max(1, Math.min(100, ((Number) args.get("maxCount")).intValue()));
        }
        File file = new File(filePath);
        String relative;
        if (file.isAbsolute()) {
            Path normalized = file.toPath().normalize();
            if (!normalized.startsWith(repoDir.toPath().normalize())) {
                return ToolResult.error("File path is outside the repository");
            }
            relative = repoDir.toPath().normalize().relativize(normalized).toString();
        } else {
            Path resolved = repoDir.toPath().resolve(filePath).normalize();
            if (!resolved.startsWith(repoDir.toPath().normalize())) {
                return ToolResult.error("File path is outside the repository");
            }
            relative = filePath;
        }
        return runGit(repoDir, "git", "log", "--follow", "-p", "--no-color",
                "--date=iso-strict", "--pretty=format:%h|%ad|%an|%s",
                "--max-count=" + maxCount, "--", relative);
    }

    private File resolveRepoDir(String repoDir) {
        if (repoDir != null && !repoDir.isBlank()) {
            if (!ProjectPathGuard.isInOpenProject(repoDir.trim())) {
                return null;
            }
            File gitRoot = findGitRoot(new File(repoDir.trim()));
            if (gitRoot != null && !isGitRootAllowed(gitRoot)) {
                return null;
            }
            return gitRoot;
        }
        for (Project p : OpenProjects.getDefault().getOpenProjects()) {
            File projectDir = FileUtil.toFile(p.getProjectDirectory());
            if (projectDir != null) {
                File gitRoot = findGitRoot(projectDir);
                if (gitRoot != null && isGitRootAllowed(gitRoot)) {
                    return gitRoot;
                }
            }
        }
        return null;
    }

    private static boolean isGitRootAllowed(File gitRoot) {
        java.nio.file.Path gitRootNorm = gitRoot.toPath().normalize();
        for (Project p : OpenProjects.getDefault().getOpenProjects()) {
            File projectDir = FileUtil.toFile(p.getProjectDirectory());
            if (projectDir != null && projectDir.toPath().normalize().startsWith(gitRootNorm)) {
                return true;
            }
        }
        return false;
    }

    private static final int MAX_PARENT_DEPTH = 10;
    private static final int MAX_OUTPUT_CHARS = 500_000;

    private static File findGitRoot(File dir) {
        File current = dir;
        for (int depth = 0; current != null && depth < MAX_PARENT_DEPTH; depth++) {
            if (new File(current, ".git").exists()) {
                return current;
            }
            current = current.getParentFile();
        }
        return null;
    }

    private Map<String, Object> runGit(File workDir, String... command) throws Exception {
        ProcessBuilder pb = new ProcessBuilder(command);
        pb.directory(workDir);
        pb.redirectErrorStream(true);
        Process proc = pb.start();
        StringBuilder output = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(proc.getInputStream(), StandardCharsets.UTF_8))) {
            String line;
            boolean truncated = false;
            while ((line = reader.readLine()) != null) {
                if (output.length() > MAX_OUTPUT_CHARS) {
                    truncated = true;
                    break;
                }
                if (output.length() > 0) {
                    output.append("\n");
                }
                output.append(line);
            }
            if (truncated) {
                output.append("\n[...output truncated...]");
            }
        }
        boolean finished = proc.waitFor(30, TimeUnit.SECONDS);
        if (!finished) {
            proc.destroyForcibly();
            return ToolResult.error("Git command timed out");
        }
        return ToolResult.ok("output", output.toString(), "exitCode", proc.exitValue());
    }
}
