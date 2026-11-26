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

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ui.OpenProjects;
import org.netbeans.junit.NbTestCase;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Lookup;

public class JavaRefactoringToolProviderTest extends NbTestCase {

    public JavaRefactoringToolProviderTest(String name) {
        super(name);
    }

    public void testRefactoringToolsAreDisabledByDefault() {
        assertFalse(new JavaRefactoringToolProvider().isEnabledByDefault());
    }

    public void testPositionConversionWithLfAndCrLf() {
        for (String text : List.of("class A {\n  int value;\n}", "class A {\r\n  int value;\r\n}")) {
            var position = JavaRefactoringToolProvider.SourcePosition.from(Map.of("line", 2, "column", 7));
            assertEquals(text.indexOf("value"), position.toOffset(text));
            assertEquals(text.indexOf("value"), JavaRefactoringToolProvider.SourcePosition.from(
                    Map.of("offset", text.indexOf("value"))).toOffset(text));
        }
        assertEquals(3, JavaRefactoringToolProvider.SourcePosition.from(
                Map.of("line", 1, "column", 4)).toOffset("abc\n"));
        assertEquals(4, JavaRefactoringToolProvider.SourcePosition.from(
                Map.of("line", 2, "column", 1)).toOffset("abc\n"));
        assertInvalidPosition(Map.of("line", 0, "column", 1), "class A {}");
        assertInvalidPosition(Map.of("line", 2, "column", 1), "class A {}");
        assertInvalidPosition(Map.of("line", 1, "column", 12), "class A {}");
        assertInvalidPosition(Map.of("line", 1), "class A {}");
        assertInvalidPosition(Map.of("line", 1, "column", 1, "offset", 0), "class A {}");
        assertInvalidPosition(Map.of("offset", 1.5), "class A {}");
    }

    private void assertInvalidPosition(Map<String, Object> args, String text) {
        try {
            JavaRefactoringToolProvider.SourcePosition.from(args).toOffset(text);
            fail("Expected an invalid position: " + args);
        } catch (IllegalArgumentException expected) {
            // Invalid positions must not be passed to the parser.
        }
    }

    public void testEveryToolRejectsOutsideProject() throws Exception {
        clearWorkDir();
        Path projectPath = Files.createDirectory(getWorkDir().toPath().resolve("project"));
        Path outside = Files.writeString(getWorkDir().toPath().resolve("Outside.java"), "class Outside {}");
        Project project = new Project() {
            @Override
            public FileObject getProjectDirectory() {
                return FileUtil.toFileObject(projectPath.toFile());
            }

            @Override
            public Lookup getLookup() {
                return Lookup.EMPTY;
            }
        };
        OpenProjects projects = OpenProjects.getDefault();
        projects.open(new Project[]{project}, false);
        try {
            var provider = new JavaRefactoringToolProvider();
            for (String name : List.of("rename_symbol", "find_symbol_usages", "inline_symbol", "encapsulate_field")) {
                Map<String, Object> result = provider.executeTool(name,
                        Map.of("filePath", outside.toString(), "line", 1, "column", 1, "newName", "NewName"));
                assertEquals("error", result.get("status"));
                assertTrue(result.get("message").toString().contains("outside any open project"));
            }
        } finally {
            projects.close(new Project[]{project});
        }
    }

    public void testSchemasAcceptLineColumnOrOffset() {
        for (var tool : new JavaRefactoringToolProvider().getTools()) {
            Map<String, Object> schema = tool.inputSchema();
            assertEquals("object", schema.get("type"));
            assertFalse(schema.containsKey("oneOf"));
            assertFalse(schema.containsKey("anyOf"));
            assertFalse(schema.containsKey("allOf"));
            @SuppressWarnings("unchecked")
            Map<String, Object> properties = (Map<String, Object>) schema.get("properties");
            assertTrue(properties.containsKey("line"));
            assertTrue(properties.containsKey("column"));
            assertTrue(properties.containsKey("offset"));
            @SuppressWarnings("unchecked")
            List<String> required = (List<String>) schema.get("required");
            assertFalse(required.contains("offset"));
            assertFalse(required.contains("line"));
            assertFalse(required.contains("column"));
        }
    }
}
