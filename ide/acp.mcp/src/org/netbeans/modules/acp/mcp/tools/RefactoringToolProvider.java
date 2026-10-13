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
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.modules.acp.mcp.McpToolProvider;
import org.netbeans.modules.acp.mcp.ProjectPathGuard;
import org.netbeans.modules.acp.mcp.ToolResult;
import org.netbeans.modules.refactoring.api.SingleCopyRefactoring;
import org.netbeans.modules.refactoring.api.MoveRefactoring;
import org.netbeans.modules.refactoring.api.Problem;
import org.netbeans.modules.refactoring.api.RefactoringElement;
import org.netbeans.modules.refactoring.api.RefactoringSession;
import org.netbeans.modules.refactoring.api.RenameRefactoring;
import org.netbeans.modules.refactoring.api.SafeDeleteRefactoring;
import org.netbeans.modules.refactoring.api.WhereUsedQuery;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Lookup;
import org.openide.util.lookup.Lookups;
import org.openide.util.lookup.ServiceProvider;

@ServiceProvider(service = McpToolProvider.class)
public final class RefactoringToolProvider implements McpToolProvider {

    private static final Logger LOG = Logger.getLogger(RefactoringToolProvider.class.getName());

    @Override
    public List<McpToolDefinition> getTools() {
        List<McpToolDefinition> tools = new ArrayList<>();

        tools.add(new McpToolDefinition(
                "rename",
                "Rename a file, class, or package. For Java files, this performs a full refactoring: "
                + "renaming the class and updating all references across the project. "
                + "For other files, it renames the file.",
                Map.of(
                        "type", "object",
                        "properties", Map.of(
                                "filePath", Map.of("type", "string",
                                        "description", "Absolute path to the file or directory to rename"),
                                "newName", Map.of("type", "string",
                                        "description", "New name (without path or extension, e.g. 'MyNewClass')"),
                                "searchInComments", Map.of("type", "boolean",
                                        "description", "Also rename occurrences in comments and strings (default false)")
                        ),
                        "required", List.of("filePath", "newName")
                )
        ));

        tools.add(new McpToolDefinition(
                "move_refactor",
                "Move a file or class to a different directory/package. For Java files, this updates "
                + "all references (imports, fully qualified names) across the project.",
                Map.of(
                        "type", "object",
                        "properties", Map.of(
                                "filePath", Map.of("type", "string",
                                        "description", "Absolute path to the file to move"),
                                "targetDirectory", Map.of("type", "string",
                                        "description", "Absolute path to the target directory")
                        ),
                        "required", List.of("filePath", "targetDirectory")
                )
        ));

        tools.add(new McpToolDefinition(
                "safe_delete",
                "Safely delete a file or class after checking for usages. If usages are found, "
                + "the operation reports them instead of deleting. "
                + "For Java files, checks references across the project.",
                Map.of(
                        "type", "object",
                        "properties", Map.of(
                                "filePath", Map.of("type", "string",
                                        "description", "Absolute path to the file to delete"),
                                "checkInComments", Map.of("type", "boolean",
                                        "description", "Also check for usages in comments (default false)")
                        ),
                        "required", List.of("filePath")
                )
        ));

        tools.add(new McpToolDefinition(
                "find_usages",
                "Find all usages/references of a file or class across the project. "
                + "Returns a list of files and locations where the element is referenced.",
                Map.of(
                        "type", "object",
                        "properties", Map.of(
                                "filePath", Map.of("type", "string",
                                        "description", "Absolute path to the file to find usages of"),
                                "searchInComments", Map.of("type", "boolean",
                                        "description", "Also search in comments and strings (default false)")
                        ),
                        "required", List.of("filePath")
                )
        ));

        tools.add(new McpToolDefinition(
                "copy_class",
                "Copy a file or class to a new name and/or directory. For Java files, this updates "
                + "the class name, package declaration, and import statements in the copy.",
                Map.of(
                        "type", "object",
                        "properties", Map.of(
                                "filePath", Map.of("type", "string",
                                        "description", "Absolute path to the file to copy"),
                                "newName", Map.of("type", "string",
                                        "description", "New name for the copy (without extension, e.g. 'MyNewClass')"),
                                "targetDirectory", Map.of("type", "string",
                                        "description", "Absolute path to the target directory (defaults to same directory)")
                        ),
                        "required", List.of("filePath", "newName")
                )
        ));

        return tools;
    }

    @Override
    public Map<String, Object> executeTool(String name, Map<String, Object> arguments) throws Exception {
        return switch (name) {
            case "rename" -> doRename(arguments);
            case "move_refactor" -> doMove(arguments);
            case "safe_delete" -> doSafeDelete(arguments);
            case "find_usages" -> doFindUsages(arguments);
            case "copy_class" -> doCopy(arguments);
            default -> throw new IllegalArgumentException("Unknown tool: " + name);
        };
    }

    private Map<String, Object> doRename(Map<String, Object> args) throws Exception {
        String filePath = (String) args.get("filePath");
        String newName = (String) args.get("newName");
        if (filePath == null || filePath.isBlank()) {
            return ToolResult.error("filePath is required");
        }
        if (newName == null || newName.isBlank()) {
            return ToolResult.error("newName is required");
        }
        if (!ProjectPathGuard.isInOpenProject(filePath)) {
            return ProjectPathGuard.outsideProjectError(filePath);
        }
        FileObject fo = resolveFileObject(filePath);
        if (fo == null) {
            return ToolResult.error("File not found: " + filePath);
        }

        boolean searchComments = Boolean.TRUE.equals(args.get("searchInComments"));

        Lookup lkp = Lookups.singleton(fo);
        RenameRefactoring refactoring = new RenameRefactoring(lkp);
        refactoring.setNewName(newName);
        refactoring.setSearchInComments(searchComments);

        Problem preCheck = refactoring.preCheck();
        String preCheckMsg = collectProblems(preCheck);
        if (preCheck != null && preCheck.isFatal()) {
            return ToolResult.error("Pre-check failed: " + preCheckMsg);
        }

        Problem paramCheck = refactoring.checkParameters();
        String paramCheckMsg = collectProblems(paramCheck);
        if (paramCheck != null && paramCheck.isFatal()) {
            return ToolResult.error("Parameter check failed: " + paramCheckMsg);
        }

        RefactoringSession session = RefactoringSession.create("Rename " + fo.getNameExt() + " to " + newName);
        Problem prepare = refactoring.prepare(session);
        String prepareMsg = collectProblems(prepare);
        if (prepare != null && prepare.isFatal()) {
            return ToolResult.error("Prepare failed: " + prepareMsg);
        }

        Collection<RefactoringElement> elements = session.getRefactoringElements();
        List<String> affectedFiles = collectAffectedFiles(elements);

        Problem result = session.doRefactoring(true);
        if (result != null && result.isFatal()) {
            return ToolResult.error("Refactoring failed: " + collectProblems(result));
        }

        List<String> warnings = new ArrayList<>();
        if (preCheckMsg != null) {
            warnings.add(preCheckMsg);
        }
        if (paramCheckMsg != null) {
            warnings.add(paramCheckMsg);
        }
        if (prepareMsg != null) {
            warnings.add(prepareMsg);
        }

        Map<String, Object> out = ToolResult.ok("affectedFiles", affectedFiles,
                "affectedCount", affectedFiles.size());
        if (!warnings.isEmpty()) {
            out.put("warnings", warnings);
        }
        return out;
    }

    private Map<String, Object> doMove(Map<String, Object> args) throws Exception {
        String filePath = (String) args.get("filePath");
        String targetDir = (String) args.get("targetDirectory");
        if (filePath == null || filePath.isBlank()) {
            return ToolResult.error("filePath is required");
        }
        if (targetDir == null || targetDir.isBlank()) {
            return ToolResult.error("targetDirectory is required");
        }
        if (!ProjectPathGuard.isInOpenProject(filePath)) {
            return ProjectPathGuard.outsideProjectError(filePath);
        }
        if (!ProjectPathGuard.isInOpenProject(targetDir)) {
            return ProjectPathGuard.outsideProjectError(targetDir);
        }
        FileObject fo = resolveFileObject(filePath);
        if (fo == null) {
            return ToolResult.error("File not found: " + filePath);
        }
        File target = new File(targetDir);
        if (!target.isDirectory()) {
            return ToolResult.error("Target directory not found: " + targetDir);
        }

        URL targetUrl;
        try {
            targetUrl = target.toURI().toURL();
        } catch (MalformedURLException ex) {
            return ToolResult.error("Invalid target directory path");
        }

        Lookup lkp = Lookups.singleton(fo);
        MoveRefactoring refactoring = new MoveRefactoring(lkp);
        refactoring.setTarget(Lookups.singleton(targetUrl));

        Problem preCheck = refactoring.preCheck();
        String preCheckMsg = collectProblems(preCheck);
        if (preCheck != null && preCheck.isFatal()) {
            return ToolResult.error("Pre-check failed: " + preCheckMsg);
        }

        Problem paramCheck = refactoring.checkParameters();
        String paramCheckMsg = collectProblems(paramCheck);
        if (paramCheck != null && paramCheck.isFatal()) {
            return ToolResult.error("Parameter check failed: " + paramCheckMsg);
        }

        RefactoringSession session = RefactoringSession.create("Move " + fo.getNameExt());
        Problem prepare = refactoring.prepare(session);
        String prepareMsg = collectProblems(prepare);
        if (prepare != null && prepare.isFatal()) {
            return ToolResult.error("Prepare failed: " + prepareMsg);
        }

        Collection<RefactoringElement> elements = session.getRefactoringElements();
        List<String> affectedFiles = collectAffectedFiles(elements);

        Problem result = session.doRefactoring(true);
        if (result != null && result.isFatal()) {
            return ToolResult.error("Refactoring failed: " + collectProblems(result));
        }

        Map<String, Object> out = ToolResult.ok("affectedFiles", affectedFiles,
                "affectedCount", affectedFiles.size());
        List<String> warnings = collectWarnings(preCheckMsg, paramCheckMsg, prepareMsg);
        if (!warnings.isEmpty()) {
            out.put("warnings", warnings);
        }
        return out;
    }

    private Map<String, Object> doSafeDelete(Map<String, Object> args) throws Exception {
        String filePath = (String) args.get("filePath");
        if (filePath == null || filePath.isBlank()) {
            return ToolResult.error("filePath is required");
        }
        if (!ProjectPathGuard.isInOpenProject(filePath)) {
            return ProjectPathGuard.outsideProjectError(filePath);
        }
        FileObject fo = resolveFileObject(filePath);
        if (fo == null) {
            return ToolResult.error("File not found: " + filePath);
        }

        boolean checkComments = Boolean.TRUE.equals(args.get("checkInComments"));

        Lookup lkp = Lookups.singleton(fo);
        SafeDeleteRefactoring refactoring = new SafeDeleteRefactoring(lkp);
        refactoring.setCheckInComments(checkComments);

        Problem preCheck = refactoring.preCheck();
        String preCheckMsg = collectProblems(preCheck);
        if (preCheck != null && preCheck.isFatal()) {
            return ToolResult.error("Pre-check failed: " + preCheckMsg);
        }

        Problem paramCheck = refactoring.checkParameters();
        String paramCheckMsg = collectProblems(paramCheck);
        if (paramCheck != null && paramCheck.isFatal()) {
            return ToolResult.error("Parameter check failed: " + paramCheckMsg);
        }

        RefactoringSession session = RefactoringSession.create("Safe Delete " + fo.getNameExt());
        Problem prepare = refactoring.prepare(session);
        String prepareMsg = collectProblems(prepare);
        if (prepare != null && prepare.isFatal()) {
            return ToolResult.error("Prepare failed: " + prepareMsg);
        }

        Collection<RefactoringElement> elements = session.getRefactoringElements();
        List<String> usages = collectAffectedFiles(elements);

        if (!usages.isEmpty()) {
            List<Map<String, Object>> usageDetails = new ArrayList<>();
            for (RefactoringElement elem : elements) {
                Map<String, Object> detail = new LinkedHashMap<>();
                FileObject parent = elem.getParentFile();
                if (parent != null) {
                    detail.put("file", FileUtil.toFile(parent) != null
                            ? FileUtil.toFile(parent).getAbsolutePath()
                            : parent.getPath());
                }
                detail.put("description", elem.getDisplayText()
                        .replaceAll("<[^>]+>", ""));
                usageDetails.add(detail);
            }
            return ToolResult.ok("deleted", false,
                    "usages", usageDetails, "usageCount", usageDetails.size());
        }

        Problem result = session.doRefactoring(true);
        if (result != null && result.isFatal()) {
            return ToolResult.error("Refactoring failed: " + collectProblems(result));
        }

        Map<String, Object> out = ToolResult.ok("deleted", true);
        List<String> warnings = collectWarnings(preCheckMsg, paramCheckMsg, prepareMsg);
        if (!warnings.isEmpty()) {
            out.put("warnings", warnings);
        }
        return out;
    }

    private Map<String, Object> doFindUsages(Map<String, Object> args) throws Exception {
        String filePath = (String) args.get("filePath");
        if (filePath == null || filePath.isBlank()) {
            return ToolResult.error("filePath is required");
        }
        if (!ProjectPathGuard.isInOpenProject(filePath)) {
            return ProjectPathGuard.outsideProjectError(filePath);
        }
        FileObject fo = resolveFileObject(filePath);
        if (fo == null) {
            return ToolResult.error("File not found: " + filePath);
        }

        boolean searchComments = Boolean.TRUE.equals(args.get("searchInComments"));

        Lookup lkp = Lookups.singleton(fo);
        WhereUsedQuery query = new WhereUsedQuery(lkp);
        query.putValue(WhereUsedQuery.SEARCH_IN_COMMENTS, searchComments);
        query.putValue(WhereUsedQuery.FIND_REFERENCES, true);

        Problem preCheck = query.preCheck();
        String preCheckMsg = collectProblems(preCheck);
        if (preCheck != null && preCheck.isFatal()) {
            return ToolResult.error("Pre-check failed: " + preCheckMsg);
        }

        RefactoringSession session = RefactoringSession.create("Find Usages of " + fo.getNameExt());
        Problem prepare = query.prepare(session);
        String prepareMsg = collectProblems(prepare);
        if (prepare != null && prepare.isFatal()) {
            return ToolResult.error("Prepare failed: " + prepareMsg);
        }

        Collection<RefactoringElement> elements = session.getRefactoringElements();
        List<Map<String, Object>> usages = new ArrayList<>();
        for (RefactoringElement elem : elements) {
            Map<String, Object> usage = new LinkedHashMap<>();
            FileObject parent = elem.getParentFile();
            if (parent != null) {
                File f = FileUtil.toFile(parent);
                usage.put("file", f != null ? f.getAbsolutePath() : parent.getPath());
            }
            usage.put("text", elem.getText());
            usage.put("displayText", elem.getDisplayText()
                    .replaceAll("<[^>]+>", ""));
            if (elem.getPosition() != null) {
                try {
                    usage.put("startOffset", elem.getPosition().getBegin().getOffset());
                    usage.put("endOffset", elem.getPosition().getEnd().getOffset());
                } catch (Exception ex) {
                    LOG.log(Level.FINE, "Could not get position bounds", ex);
                }
            }
            usages.add(usage);
        }

        return ToolResult.ok("usages", usages, "usageCount", usages.size());
    }

    private Map<String, Object> doCopy(Map<String, Object> args) throws Exception {
        String filePath = (String) args.get("filePath");
        String newName = (String) args.get("newName");
        if (filePath == null || filePath.isBlank()) {
            return ToolResult.error("filePath is required");
        }
        if (newName == null || newName.isBlank()) {
            return ToolResult.error("newName is required");
        }
        if (!ProjectPathGuard.isInOpenProject(filePath)) {
            return ProjectPathGuard.outsideProjectError(filePath);
        }
        FileObject fo = resolveFileObject(filePath);
        if (fo == null) {
            return ToolResult.error("File not found: " + filePath);
        }

        String targetDir = (String) args.get("targetDirectory");
        File target;
        if (targetDir != null && !targetDir.isBlank()) {
            if (!ProjectPathGuard.isInOpenProject(targetDir)) {
                return ProjectPathGuard.outsideProjectError(targetDir);
            }
            target = new File(targetDir);
        } else {
            target = FileUtil.toFile(fo.getParent());
        }
        if (target == null || !target.isDirectory()) {
            return ToolResult.error("Target directory not found");
        }

        URL targetUrl;
        try {
            targetUrl = target.toURI().toURL();
        } catch (MalformedURLException ex) {
            return ToolResult.error("Invalid target directory path");
        }

        Lookup lkp = Lookups.singleton(fo);
        SingleCopyRefactoring refactoring = new SingleCopyRefactoring(lkp);
        refactoring.setNewName(newName);
        refactoring.setTarget(Lookups.singleton(targetUrl));

        Problem preCheck = refactoring.preCheck();
        String preCheckMsg = collectProblems(preCheck);
        if (preCheck != null && preCheck.isFatal()) {
            return ToolResult.error("Pre-check failed: " + preCheckMsg);
        }

        Problem paramCheck = refactoring.checkParameters();
        String paramCheckMsg = collectProblems(paramCheck);
        if (paramCheck != null && paramCheck.isFatal()) {
            return ToolResult.error("Parameter check failed: " + paramCheckMsg);
        }

        RefactoringSession session = RefactoringSession.create("Copy " + fo.getNameExt() + " as " + newName);
        Problem prepare = refactoring.prepare(session);
        String prepareMsg = collectProblems(prepare);
        if (prepare != null && prepare.isFatal()) {
            return ToolResult.error("Prepare failed: " + prepareMsg);
        }

        Problem result = session.doRefactoring(true);
        if (result != null && result.isFatal()) {
            return ToolResult.error("Refactoring failed: " + collectProblems(result));
        }

        String newFilePath = new File(target, newName + "." + fo.getExt()).getAbsolutePath();
        Map<String, Object> out = ToolResult.ok("newFile", newFilePath);
        List<String> warnings = collectWarnings(preCheckMsg, paramCheckMsg, prepareMsg);
        if (!warnings.isEmpty()) {
            out.put("warnings", warnings);
        }
        return out;
    }

    private static FileObject resolveFileObject(String path) {
        File file = new File(path);
        if (!file.exists()) {
            return null;
        }
        return FileUtil.toFileObject(FileUtil.normalizeFile(file));
    }

    private static String collectProblems(Problem problem) {
        if (problem == null) {
            return null;
        }
        StringBuilder sb = new StringBuilder();
        Problem p = problem;
        while (p != null) {
            if (sb.length() > 0) {
                sb.append("; ");
            }
            if (p.isFatal()) {
                sb.append("[FATAL] ");
            }
            sb.append(p.getMessage());
            p = p.getNext();
        }
        return sb.toString();
    }

    private static List<String> collectAffectedFiles(Collection<RefactoringElement> elements) {
        List<String> files = new ArrayList<>();
        for (RefactoringElement elem : elements) {
            FileObject parent = elem.getParentFile();
            if (parent != null) {
                File f = FileUtil.toFile(parent);
                String path = f != null ? f.getAbsolutePath() : parent.getPath();
                if (!files.contains(path)) {
                    files.add(path);
                }
            }
        }
        return files;
    }

    private static List<String> collectWarnings(String... messages) {
        List<String> warnings = new ArrayList<>();
        for (String msg : messages) {
            if (msg != null) {
                warnings.add(msg);
            }
        }
        return warnings;
    }
}
