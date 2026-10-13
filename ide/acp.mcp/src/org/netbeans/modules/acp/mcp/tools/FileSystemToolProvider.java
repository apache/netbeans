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

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ui.OpenProjects;
import org.netbeans.modules.acp.mcp.McpToolProvider;
import org.netbeans.modules.acp.mcp.ProjectPathGuard;
import org.netbeans.modules.acp.mcp.ToolResult;
import org.openide.filesystems.FileUtil;
import org.openide.util.lookup.ServiceProvider;

@ServiceProvider(service = McpToolProvider.class)
public final class FileSystemToolProvider implements McpToolProvider {

    private static final Logger LOG = Logger.getLogger(FileSystemToolProvider.class.getName());

    @Override
    public boolean isEnabledByDefault() {
        return false;
    }

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
        int linesReturned = (start > end) ? 0 : end - start + 1;
        return ToolResult.ok("content", sb.toString(),
                "totalLines", lines.size(), "linesReturned", linesReturned);
    }

    private Map<String, Object> writeToFile(Map<String, Object> args) throws Exception {
        String filePath = (String) args.get("filePath");
        String content = (String) args.get("content");
        if (filePath == null || filePath.isBlank()) {
            return ToolResult.error("filePath is required");
        }
        if (content == null) {
            return ToolResult.error("content is required");
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
        return ToolResult.ok("bytesWritten", content.length());
    }

    private Map<String, Object> replaceLines(Map<String, Object> args) throws Exception {
        String filePath = (String) args.get("filePath");
        Map<String, Object> err = validateExistingFile(filePath);
        if (err != null) {
            return err;
        }
        File file = new File(filePath);
        byte[] raw = Files.readAllBytes(file.toPath());
        String rawContent = new String(raw, StandardCharsets.UTF_8);
        String separator = rawContent.contains("\r\n") ? "\r\n" : "\n";
        List<String> lines = new ArrayList<>(List.of(rawContent.split("\\r?\\n", -1)));
        int start = Math.max(1, ((Number) args.get("startLine")).intValue());
        int end = Math.min(lines.size(), ((Number) args.get("endLine")).intValue());
        if (start > end) {
            return ToolResult.error("startLine > endLine");
        }
        String content = (String) args.get("content");
        String[] replacementLines = content.split("\n", -1);
        lines.subList(start - 1, end).clear();
        for (int i = replacementLines.length - 1; i >= 0; i--) {
            lines.add(start - 1, replacementLines[i]);
        }
        Files.writeString(file.toPath(), String.join(separator, lines), StandardCharsets.UTF_8);
        return ToolResult.ok("linesReplaced", end - start + 1);
    }

    private Map<String, Object> insertInFile(Map<String, Object> args) throws Exception {
        String filePath = (String) args.get("filePath");
        Map<String, Object> err = validateExistingFile(filePath);
        if (err != null) {
            return err;
        }
        File file = new File(filePath);
        byte[] raw = Files.readAllBytes(file.toPath());
        String rawContent = new String(raw, StandardCharsets.UTF_8);
        String separator = rawContent.contains("\r\n") ? "\r\n" : "\n";
        List<String> lines = new ArrayList<>(List.of(rawContent.split("\\r?\\n", -1)));
        int line = Math.max(1, Math.min(lines.size() + 1, ((Number) args.get("line")).intValue()));
        String content = (String) args.get("content");
        String[] insertionLines = content.split("\n", -1);
        for (int i = 0; i < insertionLines.length; i++) {
            lines.add(line - 1 + i, insertionLines[i]);
        }
        Files.writeString(file.toPath(), String.join(separator, lines), StandardCharsets.UTF_8);
        return ToolResult.ok("linesInserted", insertionLines.length);
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
        String[] lines = content.split("\\r?\\n", -1);

        int start = Math.max(1, ((Number) args.get("startLine")).intValue());
        int end = Math.min(lines.length, ((Number) args.get("endLine")).intValue());
        if (start > end) {
            return ToolResult.error("startLine (" + start + ") > endLine (" + end + ")");
        }

        String[] result = new String[lines.length - (end - start + 1)];
        System.arraycopy(lines, 0, result, 0, start - 1);
        System.arraycopy(lines, end, result, start - 1, lines.length - end);

        Files.writeString(file.toPath(), String.join(separator, result), StandardCharsets.UTF_8);
        return ToolResult.ok("linesDeleted", end - start + 1);
    }

    private static final java.util.Set<String> SEARCH_SKIPPED_DIRS = java.util.Set.of(
            ".git", ".hg", ".svn", "build", "target", "node_modules", "nbbuild", ".gradle");

    private Map<String, Object> searchProject(Map<String, Object> args) throws Exception {
        String query = (String) args.get("query");
        if (query == null || query.isBlank()) {
            return ToolResult.error("query is required");
        }
        Pattern pattern;
        try {
            pattern = Pattern.compile(query, Pattern.CASE_INSENSITIVE);
        } catch (PatternSyntaxException e) {
            return ToolResult.error("Invalid regex: " + e.getMessage());
        }
        Project[] projects = OpenProjects.getDefault().getOpenProjects();
        if (projects.length == 0) {
            return ToolResult.error("No open projects");
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
                return ToolResult.error("directory escapes the project root: " + searchDir);
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
                        byte[] raw = Files.readAllBytes(file);
                        for (int b = 0; b < Math.min(raw.length, 8192); b++) {
                            if (raw[b] == 0) {
                                return FileVisitResult.CONTINUE;
                            }
                        }
                        String fileContent = new String(raw, StandardCharsets.UTF_8);
                        List<String> fileLines = List.of(fileContent.split("\\r?\\n", -1));
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
                    } catch (java.nio.charset.MalformedInputException e) {
                        // binary or non-UTF-8 file — skip silently
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
        return ToolResult.ok("matches", matches, "totalMatches", matches.size());
    }

    private static final int MAX_LIST_ENTRIES = 5000;

    private Map<String, Object> listDirectory(Map<String, Object> args) throws Exception {
        String dirPath = (String) args.get("dirPath");
        if (dirPath == null || dirPath.isBlank()) {
            return ToolResult.error("dirPath is required");
        }
        if (!ProjectPathGuard.isInOpenProject(dirPath)) {
            return ProjectPathGuard.outsideProjectError(dirPath);
        }
        File dir = new File(dirPath);
        if (!dir.exists()) {
            return ToolResult.error("Directory not found: " + dirPath);
        }
        if (!dir.isDirectory()) {
            return ToolResult.error("Not a directory: " + dirPath);
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
        Map<String, Object> result = ToolResult.ok("entries", entries,
                "totalEntries", entries.size());
        if (truncated[0]) {
            result.put("truncated", true);
        }
        return result;
    }

    private static Map<String, Object> validateExistingFile(String filePath) {
        if (filePath == null || filePath.isBlank()) {
            return ToolResult.error("filePath is required");
        }
        if (!ProjectPathGuard.isInOpenProject(filePath)) {
            return ProjectPathGuard.outsideProjectError(filePath);
        }
        File file = new File(filePath);
        if (!file.exists()) {
            return ToolResult.error("File not found: " + filePath);
        }
        if (!file.isFile()) {
            return ToolResult.error("Not a file: " + filePath);
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
