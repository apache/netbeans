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

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import javax.swing.JEditorPane;
import javax.swing.SwingUtilities;
import javax.swing.text.Document;
import javax.swing.text.JTextComponent;
import org.netbeans.api.editor.EditorRegistry;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ui.OpenProjects;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
import org.openide.util.Lookup;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.acp.mcp.spi.McpToolProvider;

public class EditorToolProviderTest extends NbTestCase {

    private EditorToolProvider provider;

    public EditorToolProviderTest(String name) {
        super(name);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        provider = new EditorToolProvider();
    }

    public void testIsEnabledByDefault() {
        assertTrue(provider.isEnabledByDefault());
    }

    public void testToolCount() {
        List<McpToolProvider.McpToolDefinition> tools = provider.getTools();
        assertEquals(3, tools.size());
    }

    public void testToolNames() {
        Set<String> names = provider.getTools().stream()
                .map(McpToolProvider.McpToolDefinition::name)
                .collect(Collectors.toSet());
        assertTrue(names.contains("get_current_file_context"));
        assertTrue(names.contains("get_tabs"));
        assertTrue(names.contains("open_pos"));
    }

    public void testToolDefinitionsHaveDescriptions() {
        for (McpToolProvider.McpToolDefinition tool : provider.getTools()) {
            assertNotNull("Tool " + tool.name() + " should have a description", tool.description());
            assertFalse("Tool " + tool.name() + " description should not be empty",
                    tool.description().isEmpty());
        }
    }

    public void testToolDefinitionsHaveSchemas() {
        for (McpToolProvider.McpToolDefinition tool : provider.getTools()) {
            Map<String, Object> schema = tool.inputSchema();
            assertNotNull("Tool " + tool.name() + " should have a schema", schema);
            assertEquals("object", schema.get("type"));
        }
    }

    public void testCurrentEditorRequiresOpenProject() throws Exception {
        clearWorkDir();
        Path projectDir = Files.createDirectory(getWorkDir().toPath().resolve("project"));
        Path inside = Files.writeString(projectDir.resolve("Inside.txt"), "selected inside");
        Path outside = Files.writeString(getWorkDir().toPath().resolve("Outside.txt"), "private outside");
        Project project = new Project() {
            @Override
            public FileObject getProjectDirectory() {
                return FileUtil.toFileObject(projectDir.toFile());
            }

            @Override
            public Lookup getLookup() {
                return Lookup.EMPTY;
            }
        };
        OpenProjects projects = OpenProjects.getDefault();
        projects.open(new Project[]{project}, false);
        JEditorPane pane = new JEditorPane();
        try {
            SwingUtilities.invokeAndWait(() -> {
                try {
                    var register = EditorRegistry.class.getDeclaredMethod("register", JTextComponent.class);
                    register.setAccessible(true);
                    register.invoke(null, pane);
                    var focus = EditorRegistry.class.getDeclaredMethod("focusGained", JTextComponent.class,
                            java.awt.Component.class);
                    focus.setAccessible(true);
                    focus.invoke(null, pane, null);
                    pane.setText("selected inside");
                    pane.select(0, 8);
                    pane.getDocument().putProperty(Document.StreamDescriptionProperty,
                            DataObject.find(FileUtil.toFileObject(inside.toFile())));
                } catch (Exception ex) {
                    throw new AssertionError(ex);
                }
            });
            Map<String, Object> result = provider.executeTool("get_current_file_context", Map.of());
            assertEquals("ok", result.get("status"));
            assertEquals(inside.toString(), result.get("filePath"));
            assertEquals("selected", result.get("selectedText"));

            SwingUtilities.invokeAndWait(() -> {
                try {
                    pane.setText("private outside");
                    pane.selectAll();
                    pane.getDocument().putProperty(Document.StreamDescriptionProperty,
                            DataObject.find(FileUtil.toFileObject(outside.toFile())));
                } catch (Exception ex) {
                    throw new AssertionError(ex);
                }
            });
            result = provider.executeTool("get_current_file_context", Map.of());
            assertEquals("error", result.get("status"));
            assertFalse(result.toString().contains("private outside"));
            assertFalse(result.toString().contains(outside.toString()));

            SwingUtilities.invokeAndWait(() ->
                    pane.getDocument().putProperty(Document.StreamDescriptionProperty, null));
            assertEquals("error", provider.executeTool("get_current_file_context", Map.of()).get("status"));
        } finally {
            SwingUtilities.invokeAndWait(() -> {
                try {
                    var close = EditorRegistry.class.getDeclaredMethod("releasedByCloneableEditor", JTextComponent.class);
                    close.setAccessible(true);
                    close.invoke(null, pane);
                } catch (Exception ex) {
                    throw new AssertionError(ex);
                }
            });
            projects.close(new Project[]{project});
        }
    }

    public void testOpenPosRequiredParams() {
        McpToolProvider.McpToolDefinition openPos = provider.getTools().stream()
                .filter(t -> "open_pos".equals(t.name()))
                .findFirst()
                .orElseThrow();
        @SuppressWarnings("unchecked")
        List<String> required = (List<String>) openPos.inputSchema().get("required");
        assertNotNull(required);
        assertTrue(required.contains("filePath"));
        assertTrue(required.contains("line"));
    }

    public void testUnknownToolThrows() {
        try {
            provider.executeTool("nonexistent", Map.of());
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException ex) {
            assertTrue(ex.getMessage().contains("nonexistent"));
        } catch (Exception ex) {
            fail("Expected IllegalArgumentException, got " + ex.getClass().getSimpleName());
        }
    }

    public void testOpenPosNullFilePath() throws Exception {
        Map<String, Object> args = new HashMap<>();
        args.put("filePath", null);
        args.put("line", 1);
        Map<String, Object> result = provider.executeTool("open_pos", args);
        assertEquals("error", result.get("status"));
    }

    public void testOpenPosBlankFilePath() throws Exception {
        Map<String, Object> result = provider.executeTool("open_pos",
                Map.of("filePath", "  ", "line", 1));
        assertEquals("error", result.get("status"));
    }

    public void testOpenPosNullLine() throws Exception {
        Map<String, Object> args = new HashMap<>();
        args.put("filePath", "/some/File.java");
        args.put("line", null);
        Map<String, Object> result = provider.executeTool("open_pos", args);
        assertEquals("error", result.get("status"));
    }
}
