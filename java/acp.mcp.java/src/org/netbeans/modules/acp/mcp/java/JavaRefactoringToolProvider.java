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
package org.netbeans.modules.acp.mcp.java;

import com.sun.source.util.TreePath;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.Modifier;
import org.netbeans.api.java.source.CompilationController;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.Task;
import org.netbeans.api.java.source.TreePathHandle;
import org.netbeans.modules.acp.mcp.McpToolProvider;
import org.netbeans.modules.acp.mcp.ProjectPathGuard;
import org.netbeans.modules.acp.mcp.ToolResult;
import org.netbeans.modules.refactoring.api.Problem;
import org.netbeans.modules.refactoring.api.RefactoringElement;
import org.netbeans.modules.refactoring.api.RefactoringSession;
import org.netbeans.modules.refactoring.api.RenameRefactoring;
import org.netbeans.modules.refactoring.api.WhereUsedQuery;
import org.netbeans.modules.refactoring.java.api.EncapsulateFieldRefactoring;
import org.netbeans.modules.refactoring.java.api.InlineRefactoring;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.lookup.Lookups;
import org.openide.util.lookup.ServiceProvider;

@ServiceProvider(service = McpToolProvider.class)
public final class JavaRefactoringToolProvider implements McpToolProvider {

    private static final Logger LOG = Logger.getLogger(JavaRefactoringToolProvider.class.getName());

    @Override
    public List<McpToolDefinition> getTools() {
        List<McpToolDefinition> tools = new ArrayList<>();

        tools.add(new McpToolDefinition(
                "rename_symbol",
                "Rename a Java symbol (class, method, field, variable, parameter) at a given offset in a source file. "
                + "Updates all references across the project.",
                Map.of(
                        "type", "object",
                        "properties", Map.of(
                                "filePath", Map.of("type", "string",
                                        "description", "Absolute path to the Java source file"),
                                "offset", Map.of("type", "integer",
                                        "description", "Character offset of the symbol to rename (0-based)"),
                                "newName", Map.of("type", "string",
                                        "description", "New name for the symbol"),
                                "searchInComments", Map.of("type", "boolean",
                                        "description", "Also rename occurrences in comments and strings (default false)")
                        ),
                        "required", List.of("filePath", "offset", "newName")
                )
        ));

        tools.add(new McpToolDefinition(
                "find_symbol_usages",
                "Find all usages of a Java symbol (class, method, field, variable) at a given offset. "
                + "Returns file paths, line content, and position for each usage.",
                Map.of(
                        "type", "object",
                        "properties", Map.of(
                                "filePath", Map.of("type", "string",
                                        "description", "Absolute path to the Java source file"),
                                "offset", Map.of("type", "integer",
                                        "description", "Character offset of the symbol (0-based)"),
                                "searchInComments", Map.of("type", "boolean",
                                        "description", "Also search in comments and strings (default false)")
                        ),
                        "required", List.of("filePath", "offset")
                )
        ));

        tools.add(new McpToolDefinition(
                "inline_symbol",
                "Inline a Java variable, method, or constant at the given offset. "
                + "Replaces all references with the element's value or body.",
                Map.of(
                        "type", "object",
                        "properties", Map.of(
                                "filePath", Map.of("type", "string",
                                        "description", "Absolute path to the Java source file"),
                                "offset", Map.of("type", "integer",
                                        "description", "Character offset of the symbol to inline (0-based)")
                        ),
                        "required", List.of("filePath", "offset")
                )
        ));

        tools.add(new McpToolDefinition(
                "encapsulate_field",
                "Encapsulate a Java field by generating getter and setter methods, and replacing "
                + "all direct field accesses with accessor calls.",
                Map.of(
                        "type", "object",
                        "properties", Map.of(
                                "filePath", Map.of("type", "string",
                                        "description", "Absolute path to the Java source file"),
                                "offset", Map.of("type", "integer",
                                        "description", "Character offset of the field to encapsulate (0-based)"),
                                "getterName", Map.of("type", "string",
                                        "description", "Custom getter name (default: auto-generated from field name)"),
                                "setterName", Map.of("type", "string",
                                        "description", "Custom setter name (default: auto-generated from field name)")
                        ),
                        "required", List.of("filePath", "offset")
                )
        ));

        return tools;
    }

    @Override
    public Map<String, Object> executeTool(String name, Map<String, Object> arguments) throws Exception {
        LOG.log(Level.FINE, "Executing Java refactoring tool: {0}", name);
        Map<String, Object> result = switch (name) {
            case "rename_symbol" -> doRenameSymbol(arguments);
            case "find_symbol_usages" -> doFindSymbolUsages(arguments);
            case "inline_symbol" -> doInlineSymbol(arguments);
            case "encapsulate_field" -> doEncapsulateField(arguments);
            default -> throw new IllegalArgumentException("Unknown tool: " + name);
        };
        if ("error".equals(result.get("status"))) {
            LOG.log(Level.WARNING, "Java refactoring tool {0} failed: {1}", new Object[]{name, result.get("message")});
        } else {
            LOG.log(Level.FINE, "Java refactoring tool {0} succeeded", name);
        }
        return result;
    }

    private Map<String, Object> doRenameSymbol(Map<String, Object> args) throws Exception {
        String filePath = (String) args.get("filePath");
        String newName = (String) args.get("newName");
        Object offsetArg = args.get("offset");
        if (offsetArg == null) {
            return ToolResult.error("offset is required");
        }
        int offset = ((Number) offsetArg).intValue();
        if (newName == null || newName.isBlank()) {
            return ToolResult.error("newName is required");
        }

        TreePathHandle handle = resolveHandle(filePath, offset);
        if (handle == null) {
            return ToolResult.error("No Java element found at offset " + offset);
        }

        boolean searchComments = Boolean.TRUE.equals(args.get("searchInComments"));

        RenameRefactoring refactoring = new RenameRefactoring(Lookups.singleton(handle));
        refactoring.setNewName(newName);
        refactoring.setSearchInComments(searchComments);

        return executeRefactoring(refactoring, "Rename to " + newName);
    }

    private Map<String, Object> doFindSymbolUsages(Map<String, Object> args) throws Exception {
        String filePath = (String) args.get("filePath");
        Object offsetArg = args.get("offset");
        if (offsetArg == null) {
            return ToolResult.error("offset is required");
        }
        int offset = ((Number) offsetArg).intValue();

        TreePathHandle handle = resolveHandle(filePath, offset);
        if (handle == null) {
            return ToolResult.error("No Java element found at offset " + offset);
        }

        boolean searchComments = Boolean.TRUE.equals(args.get("searchInComments"));

        WhereUsedQuery query = new WhereUsedQuery(Lookups.singleton(handle));
        query.putValue(WhereUsedQuery.SEARCH_IN_COMMENTS, searchComments);
        query.putValue(WhereUsedQuery.FIND_REFERENCES, true);

        Problem preCheck = query.preCheck();
        if (preCheck != null && preCheck.isFatal()) {
            return ToolResult.error("Pre-check failed: " + collectProblems(preCheck));
        }

        RefactoringSession session = RefactoringSession.create("Find Symbol Usages");
        Problem prepare = query.prepare(session);
        if (prepare != null && prepare.isFatal()) {
            return ToolResult.error("Prepare failed: " + collectProblems(prepare));
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
            usage.put("displayText", elem.getDisplayText().replaceAll("<[^>]+>", ""));
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

    private Map<String, Object> doInlineSymbol(Map<String, Object> args) throws Exception {
        String filePath = (String) args.get("filePath");
        Object offsetArg = args.get("offset");
        if (offsetArg == null) {
            return ToolResult.error("offset is required");
        }
        int offset = ((Number) offsetArg).intValue();

        FileObject fo = resolveJavaFile(filePath);
        if (fo == null) {
            return ToolResult.error("Java file not found: " + filePath);
        }

        TreePathHandle[] handleHolder = new TreePathHandle[1];
        InlineRefactoring.Type[] typeHolder = new InlineRefactoring.Type[1];

        JavaSource js = JavaSource.forFileObject(fo);
        if (js == null) {
            return ToolResult.error("Cannot create JavaSource for: " + filePath);
        }

        js.runUserActionTask(new Task<CompilationController>() {
            @Override
            public void run(CompilationController cc) throws Exception {
                cc.toPhase(JavaSource.Phase.RESOLVED);
                TreePath path = cc.getTreeUtilities().pathFor(offset);
                path = resolveToElement(cc, path);
                if (path == null) {
                    return;
                }
                Element el = cc.getTrees().getElement(path);
                if (el == null) {
                    return;
                }
                handleHolder[0] = TreePathHandle.create(path, cc);
                typeHolder[0] = switch (el.getKind()) {
                    case METHOD, CONSTRUCTOR -> InlineRefactoring.Type.METHOD;
                    case LOCAL_VARIABLE -> InlineRefactoring.Type.TEMP;
                    case FIELD -> {
                        Set<Modifier> mods = el.getModifiers();
                        yield (mods.contains(Modifier.STATIC) && mods.contains(Modifier.FINAL))
                                ? InlineRefactoring.Type.CONSTANT
                                : InlineRefactoring.Type.UNSUPPORTED;
                    }
                    default -> InlineRefactoring.Type.UNSUPPORTED;
                };
            }
        }, true);

        if (handleHolder[0] == null) {
            return ToolResult.error("No Java element found at offset " + offset);
        }
        if (typeHolder[0] == InlineRefactoring.Type.UNSUPPORTED) {
            return ToolResult.error("Element at offset " + offset + " cannot be inlined");
        }

        InlineRefactoring refactoring = new InlineRefactoring(handleHolder[0], typeHolder[0]);
        return executeRefactoring(refactoring, "Inline");
    }

    private Map<String, Object> doEncapsulateField(Map<String, Object> args) throws Exception {
        String filePath = (String) args.get("filePath");
        Object offsetArg = args.get("offset");
        if (offsetArg == null) {
            return ToolResult.error("offset is required");
        }
        int offset = ((Number) offsetArg).intValue();

        FileObject fo = resolveJavaFile(filePath);
        if (fo == null) {
            return ToolResult.error("Java file not found: " + filePath);
        }

        TreePathHandle[] handleHolder = new TreePathHandle[1];
        String[] fieldNameHolder = new String[1];

        JavaSource js = JavaSource.forFileObject(fo);
        if (js == null) {
            return ToolResult.error("Cannot create JavaSource for: " + filePath);
        }

        js.runUserActionTask(new Task<CompilationController>() {
            @Override
            public void run(CompilationController cc) throws Exception {
                cc.toPhase(JavaSource.Phase.RESOLVED);
                TreePath path = cc.getTreeUtilities().pathFor(offset);
                path = resolveToElement(cc, path);
                if (path == null) {
                    return;
                }
                Element el = cc.getTrees().getElement(path);
                if (el != null && el.getKind() == ElementKind.FIELD) {
                    handleHolder[0] = TreePathHandle.create(path, cc);
                    fieldNameHolder[0] = el.getSimpleName().toString();
                }
            }
        }, true);

        if (handleHolder[0] == null) {
            return ToolResult.error("No field found at offset " + offset);
        }

        String fieldName = fieldNameHolder[0];
        EncapsulateFieldRefactoring refactoring = new EncapsulateFieldRefactoring(handleHolder[0]);

        String getterName = (String) args.get("getterName");
        String setterName = (String) args.get("setterName");
        if (getterName == null || getterName.isBlank()) {
            getterName = "get" + Character.toUpperCase(fieldName.charAt(0)) + fieldName.substring(1);
        }
        if (setterName == null || setterName.isBlank()) {
            setterName = "set" + Character.toUpperCase(fieldName.charAt(0)) + fieldName.substring(1);
        }
        refactoring.setGetterName(getterName);
        refactoring.setSetterName(setterName);
        refactoring.setMethodModifiers(EnumSet.of(Modifier.PUBLIC));
        refactoring.setFieldModifiers(EnumSet.of(Modifier.PRIVATE));
        refactoring.setAlwaysUseAccessors(true);

        return executeRefactoring(refactoring, "Encapsulate " + fieldName);
    }

    private TreePathHandle resolveHandle(String filePath, int offset) throws IOException {
        FileObject fo = resolveJavaFile(filePath);
        if (fo == null) {
            return null;
        }

        JavaSource js = JavaSource.forFileObject(fo);
        if (js == null) {
            return null;
        }

        TreePathHandle[] result = new TreePathHandle[1];
        js.runUserActionTask(new Task<CompilationController>() {
            @Override
            public void run(CompilationController cc) throws Exception {
                cc.toPhase(JavaSource.Phase.RESOLVED);
                TreePath path = cc.getTreeUtilities().pathFor(offset);
                path = resolveToElement(cc, path);
                if (path != null) {
                    result[0] = TreePathHandle.create(path, cc);
                }
            }
        }, true);

        return result[0];
    }

    private static TreePath resolveToElement(CompilationController cc, TreePath path) {
        while (path != null) {
            Element el = cc.getTrees().getElement(path);
            if (el != null) {
                ElementKind kind = el.getKind();
                if (kind == ElementKind.CLASS || kind == ElementKind.INTERFACE
                        || kind == ElementKind.ENUM || kind == ElementKind.RECORD
                        || kind == ElementKind.METHOD || kind == ElementKind.CONSTRUCTOR
                        || kind == ElementKind.FIELD || kind == ElementKind.ENUM_CONSTANT
                        || kind == ElementKind.LOCAL_VARIABLE || kind == ElementKind.PARAMETER
                        || kind == ElementKind.ANNOTATION_TYPE) {
                    return path;
                }
            }
            path = path.getParentPath();
        }
        return null;
    }

    private Map<String, Object> executeRefactoring(
            org.netbeans.modules.refactoring.api.AbstractRefactoring refactoring,
            String description) throws Exception {

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

        RefactoringSession session = RefactoringSession.create(description);
        Problem prepare = refactoring.prepare(session);
        String prepareMsg = collectProblems(prepare);
        if (prepare != null && prepare.isFatal()) {
            return ToolResult.error("Prepare failed: " + prepareMsg);
        }

        Collection<RefactoringElement> elements = session.getRefactoringElements();
        List<String> affectedFiles = new ArrayList<>();
        for (RefactoringElement elem : elements) {
            FileObject parent = elem.getParentFile();
            if (parent != null) {
                File f = FileUtil.toFile(parent);
                String path = f != null ? f.getAbsolutePath() : parent.getPath();
                if (!affectedFiles.contains(path)) {
                    affectedFiles.add(path);
                }
            }
        }

        Problem result = session.doRefactoring(true);
        if (result != null && result.isFatal()) {
            return ToolResult.error("Refactoring failed: " + collectProblems(result));
        }

        Map<String, Object> out = ToolResult.ok("affectedFiles", affectedFiles,
                "affectedCount", affectedFiles.size());
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
        if (!warnings.isEmpty()) {
            out.put("warnings", warnings);
        }
        return out;
    }

    private static FileObject resolveJavaFile(String filePath) {
        if (filePath == null || filePath.isBlank()) {
            return null;
        }
        if (!ProjectPathGuard.isInOpenProject(filePath)) {
            return null;
        }
        File file = new File(filePath);
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
}
