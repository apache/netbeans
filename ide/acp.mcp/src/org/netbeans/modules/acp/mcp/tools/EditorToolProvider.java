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
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import javax.swing.JEditorPane;
import javax.swing.SwingUtilities;
import javax.swing.text.Document;
import javax.swing.text.StyledDocument;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ui.OpenProjects;
import org.netbeans.modules.acp.mcp.McpToolProvider;
import org.netbeans.modules.acp.mcp.ProjectPathGuard;
import org.openide.cookies.EditorCookie;
import org.openide.cookies.LineCookie;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
import org.openide.text.Line;
import org.openide.util.lookup.ServiceProvider;
import org.openide.windows.TopComponent;

@ServiceProvider(service = McpToolProvider.class)
public final class EditorToolProvider implements McpToolProvider {

    @Override
    public List<McpToolDefinition> getTools() {
        List<McpToolDefinition> tools = new ArrayList<>();

        tools.add(new McpToolDefinition(
                "get_current_file_context",
                "Returns the current file path, cursor position, and any text selection from the active editor tab. "
                + "Call when you see phrases like 'this file', 'highlighted text', 'selected text', 'selected code', "
                + "'current file', 'current line' or similar phrases indicating current editing context.",
                Map.of("type", "object", "properties", Map.of())
        ));

        tools.add(new McpToolDefinition(
                "get_tabs",
                "Returns a list of file paths currently open in editor tabs.",
                Map.of("type", "object", "properties", Map.of())
        ));

        tools.add(new McpToolDefinition(
                "open_pos",
                "Opens a file at the specified line number in the editor. Only works for files within open projects. "
                + "focus=false (default) opens the tab without stealing focus; "
                + "focus=true activates the editor — use only when the user explicitly asks to be taken to a location.",
                Map.of(
                        "type", "object",
                        "properties", Map.of(
                                "filePath", Map.of("type", "string", "description", "Absolute path to the file to open"),
                                "line", Map.of("type", "integer", "description", "Line number (1-indexed)"),
                                "focus", Map.of("type", "boolean", "description", "Whether to focus the editor window", "default", false)
                        ),
                        "required", List.of("filePath", "line")
                )
        ));

        return tools;
    }

    @Override
    public Map<String, Object> executeTool(String name, Map<String, Object> arguments) throws Exception {
        return switch (name) {
            case "get_current_file_context" -> getCurrentFileContext();
            case "get_tabs" -> getTabs();
            case "open_pos" -> openPos(arguments);
            default -> throw new IllegalArgumentException("Unknown tool: " + name);
        };
    }

    private Map<String, Object> getCurrentFileContext() throws Exception {
        Map<String, Object>[] result = new Map[1];
        SwingUtilities.invokeAndWait(() -> {
            TopComponent active = TopComponent.getRegistry().getActivated();
            if (active == null) {
                result[0] = Map.of("status", "error", "message", "No active editor");
                return;
            }
            EditorCookie ec = active.getLookup().lookup(EditorCookie.class);
            if (ec == null) {
                result[0] = Map.of("status", "error", "message", "Active component is not an editor");
                return;
            }
            Map<String, Object> ctx = new LinkedHashMap<>();
            FileObject fo = active.getLookup().lookup(FileObject.class);
            if (fo != null) {
                File file = FileUtil.toFile(fo);
                if (file != null) {
                    ctx.put("filePath", file.getAbsolutePath());
                }
                ctx.put("fileName", fo.getNameExt());
                ctx.put("mimeType", fo.getMIMEType());
            }
            JEditorPane[] panes = ec.getOpenedPanes();
            if (panes != null && panes.length > 0) {
                JEditorPane pane = panes[0];
                ctx.put("cursorPosition", pane.getCaretPosition());
                Document doc = pane.getDocument();
                if (doc != null) {
                    ctx.put("documentLength", doc.getLength());
                }
                String selectedText = pane.getSelectedText();
                if (selectedText != null && !selectedText.isEmpty()) {
                    ctx.put("selectedText", selectedText);
                    ctx.put("selectionStart", pane.getSelectionStart());
                    ctx.put("selectionEnd", pane.getSelectionEnd());
                }
            }
            ctx.put("status", "ok");
            result[0] = ctx;
        });
        return result[0];
    }

    private Map<String, Object> getTabs() {
        List<String> paths = new ArrayList<>();
        for (TopComponent tc : TopComponent.getRegistry().getOpened()) {
            EditorCookie ec = tc.getLookup().lookup(EditorCookie.class);
            if (ec != null) {
                FileObject fo = tc.getLookup().lookup(FileObject.class);
                if (fo != null) {
                    File file = FileUtil.toFile(fo);
                    if (file != null) {
                        String filePath = file.getAbsolutePath();
                        if (ProjectPathGuard.isInOpenProject(filePath)) {
                            paths.add(filePath);
                        }
                    }
                }
            }
        }
        return Map.of("status", "ok", "tabs", paths);
    }

    private Map<String, Object> openPos(Map<String, Object> arguments) throws Exception {
        String filePath = (String) arguments.get("filePath");
        if (filePath == null || filePath.isBlank()) {
            return Map.of("status", "error", "message", "filePath is required");
        }
        if (!ProjectPathGuard.isInOpenProject(filePath)) {
            return ProjectPathGuard.outsideProjectError(filePath);
        }
        Number lineNum = (Number) arguments.get("line");
        if (lineNum == null) {
            return Map.of("status", "error", "message", "line is required");
        }
        Boolean focus = (Boolean) arguments.getOrDefault("focus", Boolean.FALSE);

        File requestedFile = new File(filePath).getCanonicalFile();
        FileObject fo = FileUtil.toFileObject(requestedFile);
        if (fo == null) {
            return Map.of("status", "error", "message", "File not found: " + filePath);
        }
        DataObject data = DataObject.find(fo);
        LineCookie lc = data.getLookup().lookup(LineCookie.class);
        if (lc == null) {
            return Map.of("status", "error", "message", "Cannot open file in editor: " + filePath);
        }
        Line.Set lineSet = lc.getLineSet();
        Line line = lineSet.getOriginal(Math.max(0, lineNum.intValue() - 1));

        Line.ShowVisibilityType visibility = Boolean.TRUE.equals(focus)
                ? Line.ShowVisibilityType.FOCUS
                : Line.ShowVisibilityType.NONE;
        SwingUtilities.invokeLater(() -> {
            line.show(Line.ShowOpenType.OPEN, visibility);
        });
        return Map.of("status", "ok");
    }
}
