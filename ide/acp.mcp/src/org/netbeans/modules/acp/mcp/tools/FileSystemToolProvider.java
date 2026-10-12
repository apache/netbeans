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
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ui.OpenProjects;
import org.netbeans.modules.acp.mcp.McpToolProvider;
import org.netbeans.modules.acp.mcp.ProjectPathGuard;
import org.openide.filesystems.FileUtil;
import org.openide.util.lookup.ServiceProvider;

@ServiceProvider(service = McpToolProvider.class)
public final class FileSystemToolProvider implements McpToolProvider {

    private static final Logger LOG = Logger.getLogger(FileSystemToolProvider.class.getName());

    @Override
    public List<McpToolDefinition> getTools() {
        List<McpToolDefinition> tools = new ArrayList<>();

        tools.add(new McpToolDefinition(
                "read_file",
                "Read content of a file. Returns file contents as text.",
                Map.of(
                        "type", "object",
                        "properties", Map.of(
                                "filePath", Map.of("type", "string", "description", "Absolute path to file"),
                                "startLine", Map.of("type", "integer", "description", "Start line (1-indexed, inclusive)"),
                                "endLine", Map.of("type", "integer", "description", "End line (1-indexed, inclusive)")
                        ),
                        "required", List.of("filePath")
                )
        ));

        tools.add(new McpToolDefinition(
                "write_to_file",
                "Write content to a file (creates or overwrites).",
                Map.of(
                        "type", "object",
                        "properties", Map.of(
                                "filePath", Map.of("type", "string", "description", "Absolute path to file"),
                                "content", Map.of("type", "string", "description", "Content to write")
                        ),
                        "required", List.of("filePath", "content")
                )
        ));

        tools.add(new McpToolDefinition(
                "replace_lines",
                "Replace specific lines in a file with new content.",
                Map.of(
                        "type", "object",
                        "properties", Map.of(
                                "filePath", Map.of("type", "string", "description", "Absolute path to file"),
                                "startLine", Map.of("type", "integer", "description", "Start line to replace (1-indexed, inclusive)"),
                                "endLine", Map.of("type", "integer", "description", "End line to replace (1-indexed, inclusive)"),
                                "content", Map.of("type", "string", "description", "Replacement content for lines startLine-endLine")
                        ),
                        "required", List.of("filePath", "startLine", "endLine", "content")
                )
        ));

        tools.add(new McpToolDefinition(
                "insert_in_file",
                "Insert content at a specific line in a file.",
                Map.of(
                        "type", "object",
                        "properties", Map.of(
                                "filePath", Map.of("type", "string", "description", "Absolute path to file"),
                                "line", Map.of("type", "integer", "description", "Line number to insert before (1-indexed)"),
                                "content", Map.of("type", "string", "description", "Content to insert")
                        ),
                        "required", List.of("filePath", "line", "content")
                )
        ));

        tools.add(new McpToolDefinition(
                "delete_lines",
                "Delete a range of lines from a file (end inclusive).",
                Map.of(
                        "type", "object",
                        "properties", Map.of(
                                "filePath", Map.of("type", "string", "description", "Absolute path to file"),
                                "startLine", Map.of("type", "integer", "description", "Start line to delete (1-indexed, inclusive)"),
                                "endLine", Map.of("type", "integer", "description", "End line to delete (1-indexed, inclusive)")
                        ),
                        "required", List.of("filePath", "startLine", "endLine")
                )
        ));

        tools.add(new McpToolDefinition(
                "search_project",
                "Search for a text pattern or regex in project files. Returns matching file paths and line numbers.",
                Map.of(
                        "type", "object",
                        "properties", Map.of(
                                "query", Map.of("type", "string", "description", "Search pattern (regex supported)"),
                                "filePattern", Map.of("type", "string", "description", "Glob filter, e.g. '*.java'"),
                                "directory", Map.of("type", "string", "description", "Subdirectory to restrict search")
                        ),
                        "required", List.of("query")
                )
        ));

        tools.add(new McpToolDefinition(
                "list_directory",
                "List files and subdirectories in a directory.",
                Map.of(
                        "type", "object",
                        "properties", Map.of(
                                "dirPath", Map.of("type", "string", "description", "Absolute path to directory"),
                                "recursive", Map.of("type", "boolean", "description", "Whether to list recursively", "default", false)
                        ),
                        "required", List.of("dirPath")
                )
        ));

        tools.add(new McpToolDefinition(
                "run_command",
                "Execute a shell command. Returns stdout, stderr, and exit code.",
                Map.of(
                        "type", "object",
                        "properties", Map.of(
                                "command", Map.of("type", "string", "description", "Shell command to execute"),
                                "workingDirectory", Map.of("type", "string", "description", "Working directory for the command"),
                                "timeoutSeconds", Map.of("type", "integer", "description", "Timeout in seconds", "default", 120)
                        ),
                        "required", List.of("command")
                )
        ));

        return tools;
    }

    @Override
    public Map<String, Object> executeTool(String name, Map<String, Object> arguments) throws Exception {
        return switch (name) {
            case "read_file" -> readFile(arguments);
            case "write_to_file" -> writeToFile(arguments);
            case "replace_lines" -> replaceLines(arguments);
            case "insert_in_file" -> insertInFile(arguments);
            case "delete_lines" -> deleteLines(arguments);
            case "search_project" -> searchProject(arguments);
            case "list_directory" -> listDirectory(arguments);
            case "run_command" -> runCommand(arguments);
            default -> throw new IllegalArgumentException("Unknown tool: " + name);
        };
    }

    private Map<String, Object> readFile(Map<String, Object> args) throws Exception {
        String filePath = (String) args.get("filePath");
        Map<String, Object> err = validateExistingFile(filePath);
        if (err != null) {
            return err;
        }
        File file = new File(filePath);
        List<String> lines = Files.readAllLines(file.toPath(), StandardCharsets.UTF_8);
        int start = args.containsKey("startLine") ? ((Number) args.get("startLine")).intValue() : 1;
        int end = args.containsKey("endLine") ? ((Number) args.get("endLine")).intValue() : lines.size();
        start = Math.max(1, start);
        end = Math.min(lines.size(), end);
        StringBuilder sb = new StringBuilder();
        for (int i = start - 1; i < end; i++) {
            sb.append(lines.get(i));
            if (i < end - 1) {
                sb.append("\n");
            }
        }
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("status", "ok");
        result.put("content", sb.toString());
        result.put("totalLines", lines.size());
        result.put("linesReturned", end - start + 1);
        return result;
    }

    private Map<String, Object> writeToFile(Map<String, Object> args) throws Exception {
        String filePath = (String) args.get("filePath");
        String content = (String) args.get("content");
        if (filePath == null || filePath.isBlank()) {
            return Map.of("status", "error", "message", "filePath is required");
        }
        if (content == null) {
            return Map.of("status", "error", "message", "content is required");
        }
        if (!ProjectPathGuard.isInOpenProject(filePath)) {
            return ProjectPathGuard.outsideProjectError(filePath);
        }
        File file = new File(filePath);
        File parent = file.getParentFile();
        if (parent != null && !parent.exists()) {
            parent.mkdirs();
        }
        Files.writeString(file.toPath(), content, StandardCharsets.UTF_8);
        return Map.of("status", "ok", "bytesWritten", content.length());
    }

    private Map<String, Object> replaceLines(Map<String, Object> args) throws Exception {
        String filePath = (String) args.get("filePath");
        Map<String, Object> err = validateExistingFile(filePath);
        if (err != null) {
            return err;
        }
        File file = new File(filePath);
        List<String> lines = new ArrayList<>(Files.readAllLines(file.toPath(), StandardCharsets.UTF_8));
        int start = Math.max(1, ((Number) args.get("startLine")).intValue());
        int end = Math.min(lines.size(), ((Number) args.get("endLine")).intValue());
        if (start > end) {
            return Map.of("status", "error", "message", "startLine > endLine");
        }
        String content = (String) args.get("content");
        String[] replacementLines = content.split("\n", -1);
        lines.subList(start - 1, end).clear();
        for (int i = replacementLines.length - 1; i >= 0; i--) {
            lines.add(start - 1, replacementLines[i]);
        }
        Files.writeString(file.toPath(), String.join("\n", lines), StandardCharsets.UTF_8);
        return Map.of("status", "ok", "linesReplaced", end - start + 1);
    }

    private Map<String, Object> insertInFile(Map<String, Object> args) throws Exception {
        String filePath = (String) args.get("filePath");
        Map<String, Object> err = validateExistingFile(filePath);
        if (err != null) {
            return err;
        }
        File file = new File(filePath);
        List<String> lines = new ArrayList<>(Files.readAllLines(file.toPath(), StandardCharsets.UTF_8));
        int line = Math.max(1, Math.min(lines.size() + 1, ((Number) args.get("line")).intValue()));
        String content = (String) args.get("content");
        String[] insertionLines = content.split("\n", -1);
        for (int i = 0; i < insertionLines.length; i++) {
            lines.add(line - 1 + i, insertionLines[i]);
        }
        Files.writeString(file.toPath(), String.join("\n", lines), StandardCharsets.UTF_8);
        return Map.of("status", "ok", "linesInserted", insertionLines.length);
    }

    private Map<String, Object> deleteLines(Map<String, Object> args) throws Exception {
        String filePath = (String) args.get("filePath");
        Map<String, Object> err = validateExistingFile(filePath);
        if (err != null) {
            return err;
        }
        File file = new File(filePath);
        byte[] raw = Files.readAllBytes(file.toPath());
        String content = new String(raw, StandardCharsets.UTF_8);
        String separator = content.contains("\r\n") ? "\r\n" : "\n";
        String[] lines = content.split(separator.equals("\r\n") ? "\r\n" : "\n", -1);

        int start = Math.max(1, ((Number) args.get("startLine")).intValue());
        int end = Math.min(lines.length, ((Number) args.get("endLine")).intValue());
        if (start > end) {
            return Map.of("status", "error", "message",
                    "startLine (" + start + ") > endLine (" + end + ")");
        }

        String[] result = new String[lines.length - (end - start + 1)];
        System.arraycopy(lines, 0, result, 0, start - 1);
        System.arraycopy(lines, end, result, start - 1, lines.length - end);

        Files.writeString(file.toPath(), String.join(separator, result), StandardCharsets.UTF_8);
        return Map.of("status", "ok", "linesDeleted", end - start + 1);
    }

    private static final java.util.Set<String> SEARCH_SKIPPED_DIRS = java.util.Set.of(
            ".git", ".hg", ".svn", "build", "target", "node_modules", "nbbuild", ".gradle");

    private Map<String, Object> searchProject(Map<String, Object> args) throws Exception {
        String query = (String) args.get("query");
        if (query == null || query.isBlank()) {
            return Map.of("status", "error", "message", "query is required");
        }
        Pattern pattern;
        try {
            pattern = Pattern.compile(query, Pattern.CASE_INSENSITIVE);
        } catch (PatternSyntaxException e) {
            return Map.of("status", "error", "message", "Invalid regex: " + e.getMessage());
        }
        Project[] projects = OpenProjects.getDefault().getOpenProjects();
        if (projects.length == 0) {
            return Map.of("status", "error", "message", "No open projects");
        }
        String searchDir = (String) args.get("directory");
        String fileGlob = (String) args.get("filePattern");
        Pattern globPattern = (fileGlob != null && !fileGlob.isEmpty())
                ? compileGlob(fileGlob) : null;

        List<Map<String, Object>> matches = new ArrayList<>();
        for (Project p : projects) {
            File projectDirFile = FileUtil.toFile(p.getProjectDirectory());
            if (projectDirFile == null) {
                continue;
            }
            Path projectDir = projectDirFile.toPath().normalize();
            Path searchRoot = searchDir != null
                    ? projectDir.resolve(searchDir).normalize() : projectDir;
            if (!searchRoot.startsWith(projectDir)) {
                return Map.of("status", "error", "message",
                        "directory escapes the project root: " + searchDir);
            }
            if (!Files.isDirectory(searchRoot)) {
                continue;
            }
            Files.walkFileTree(searchRoot, new SimpleFileVisitor<>() {
                @Override
                public FileVisitResult preVisitDirectory(Path d, BasicFileAttributes attrs) {
                    if (!d.equals(searchRoot) && SEARCH_SKIPPED_DIRS.contains(d.getFileName().toString())) {
                        return FileVisitResult.SKIP_SUBTREE;
                    }
                    return FileVisitResult.CONTINUE;
                }

                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) {
                    if (globPattern != null) {
                        String fileName = file.getFileName().toString();
                        if (!globPattern.matcher(fileName).matches()) {
                            return FileVisitResult.CONTINUE;
                        }
                    }
                    try {
                        List<String> fileLines = Files.readAllLines(file, StandardCharsets.UTF_8);
                        for (int i = 0; i < fileLines.size(); i++) {
                            if (pattern.matcher(fileLines.get(i)).find()) {
                                Map<String, Object> match = new HashMap<>();
                                match.put("file", file.toString());
                                match.put("line", i + 1);
                                match.put("content", fileLines.get(i).trim());
                                matches.add(match);
                                if (matches.size() >= 200) {
                                    return FileVisitResult.TERMINATE;
                                }
                            }
                        }
                    } catch (IOException e) {
                        LOG.log(Level.FINE, "Error reading file: {0}", e.getMessage());
                    }
                    return FileVisitResult.CONTINUE;
                }
            });
            if (matches.size() >= 200) {
                break;
            }
        }
        Map<String, Object> result = new HashMap<>();
        result.put("status", "ok");
        result.put("matches", matches);
        result.put("totalMatches", matches.size());
        return result;
    }

    private static final int MAX_LIST_ENTRIES = 5000;

    private Map<String, Object> listDirectory(Map<String, Object> args) throws Exception {
        String dirPath = (String) args.get("dirPath");
        if (dirPath == null || dirPath.isBlank()) {
            return Map.of("status", "error", "message", "dirPath is required");
        }
        if (!ProjectPathGuard.isInOpenProject(dirPath)) {
            return ProjectPathGuard.outsideProjectError(dirPath);
        }
        File dir = new File(dirPath);
        if (!dir.exists()) {
            return Map.of("status", "error", "message", "Directory not found: " + dirPath);
        }
        if (!dir.isDirectory()) {
            return Map.of("status", "error", "message", "Not a directory: " + dirPath);
        }
        boolean recursive = Boolean.TRUE.equals(args.get("recursive"));
        List<Map<String, Object>> entries = new ArrayList<>();
        boolean[] truncated = {false};
        if (recursive) {
            Files.walkFileTree(dir.toPath(), new SimpleFileVisitor<>() {
                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) {
                    Map<String, Object> entry = new HashMap<>();
                    entry.put("path", file.toString());
                    entry.put("type", "file");
                    entry.put("size", attrs.size());
                    entries.add(entry);
                    return limitReached();
                }

                @Override
                public FileVisitResult preVisitDirectory(Path d, BasicFileAttributes attrs) {
                    Map<String, Object> entry = new HashMap<>();
                    entry.put("path", d.toString());
                    entry.put("type", "directory");
                    entries.add(entry);
                    return limitReached();
                }

                private FileVisitResult limitReached() {
                    if (entries.size() >= MAX_LIST_ENTRIES) {
                        truncated[0] = true;
                        return FileVisitResult.TERMINATE;
                    }
                    return FileVisitResult.CONTINUE;
                }
            });
        } else {
            File[] files = dir.listFiles();
            if (files != null) {
                for (File f : files) {
                    Map<String, Object> entry = new HashMap<>();
                    entry.put("path", f.getAbsolutePath());
                    entry.put("type", f.isDirectory() ? "directory" : "file");
                    entry.put("size", f.length());
                    entries.add(entry);
                }
            }
        }
        Map<String, Object> result = new HashMap<>();
        result.put("status", "ok");
        result.put("entries", entries);
        result.put("totalEntries", entries.size());
        if (truncated[0]) {
            result.put("truncated", true);
        }
        return result;
    }

    private Map<String, Object> runCommand(Map<String, Object> args) throws Exception {
        String command = (String) args.get("command");
        if (command == null || command.isBlank()) {
            return Map.of("status", "error", "message", "command is required");
        }
        String workDir = (String) args.get("workingDirectory");
        if (workDir == null || workDir.isBlank()) {
            Project main = OpenProjects.getDefault().getMainProject();
            if (main != null) {
                File d = FileUtil.toFile(main.getProjectDirectory());
                if (d != null) {
                    workDir = d.getAbsolutePath();
                }
            }
        }
        if (workDir == null || workDir.isBlank()) {
            return Map.of("status", "error", "message",
                    "No working directory specified and no main project set");
        }
        if (!ProjectPathGuard.isInOpenProject(workDir)) {
            return ProjectPathGuard.outsideProjectError(workDir);
        }
        File dir = new File(workDir);
        if (!dir.exists() || !dir.isDirectory()) {
            return Map.of("status", "error", "message", "Invalid working directory: " + workDir);
        }
        int timeout = args.containsKey("timeoutSeconds")
                ? ((Number) args.get("timeoutSeconds")).intValue() : 120;

        ProcessBuilder pb = new ProcessBuilder("bash", "-c", command);
        pb.directory(dir);
        pb.redirectErrorStream(true);
        Process proc = pb.start();
        StringBuilder output = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(proc.getInputStream(), StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (output.length() > 0) {
                    output.append("\n");
                }
                output.append(line);
            }
        }
        boolean finished = proc.waitFor(timeout, TimeUnit.SECONDS);
        if (!finished) {
            proc.destroyForcibly();
            return Map.of("status", "error", "message", "Command timed out after " + timeout + "s");
        }
        int exitCode = proc.exitValue();
        Map<String, Object> result = new HashMap<>();
        result.put("status", "ok");
        result.put("exitCode", exitCode);
        result.put("output", output.toString());
        return result;
    }

    private static Map<String, Object> validateExistingFile(String filePath) {
        if (filePath == null || filePath.isBlank()) {
            return Map.of("status", "error", "message", "filePath is required");
        }
        if (!ProjectPathGuard.isInOpenProject(filePath)) {
            return ProjectPathGuard.outsideProjectError(filePath);
        }
        File file = new File(filePath);
        if (!file.exists()) {
            return Map.of("status", "error", "message", "File not found: " + filePath);
        }
        if (!file.isFile()) {
            return Map.of("status", "error", "message", "Not a file: " + filePath);
        }
        return null;
    }

    private static Pattern compileGlob(String glob) {
        StringBuilder sb = new StringBuilder(glob.length() * 2);
        for (int i = 0; i < glob.length(); i++) {
            char c = glob.charAt(i);
            switch (c) {
                case '*' -> sb.append(".*");
                case '?' -> sb.append('.');
                default -> {
                    if ("\\[](){}<>^$|.?*+-=".indexOf(c) >= 0) {
                        sb.append('\\');
                    }
                    sb.append(c);
                }
            }
        }
        return Pattern.compile(sb.toString());
    }
}
