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
package org.netbeans.modules.rust.project;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import static org.junit.Assert.assertEquals;
import org.junit.Test;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.URLMapper;


public class RustProjectOperationsTest {

    public RustProjectOperationsTest() {
    }

    @Test
    public void testEscapeString() {
        assertEquals("\"Test\"", RustProjectOperations.escapeTomlString("Test"));
        assertEquals("\"Te\\\"st\"", RustProjectOperations.escapeTomlString("Te\"st"));
        assertEquals("\"Te\\nst\"", RustProjectOperations.escapeTomlString("Te\nst"));
        assertEquals("\"Teäst\"", RustProjectOperations.escapeTomlString("Teäst"));
        assertEquals("\"Te\\tst\"", RustProjectOperations.escapeTomlString("Te\tst"));
        assertEquals("\"Te\\tst\"", RustProjectOperations.escapeTomlString("Te\tst"));
        assertEquals("\"Te\\u001Est\"", RustProjectOperations.escapeTomlString("Te\u001Est"));
    }

    @Test
    public void testUpdateProjectNameLiteralMultilineString() throws IOException {
        testRename("Reference.Cargo.toml", "Test.Cargo.LiteralMultilineString.toml");
    }

    @Test
    public void testUpdateProjectNameLiteralString() throws IOException {
        testRename("Reference.Cargo.toml", "Test.Cargo.LiteralString.toml");
    }

    @Test
    public void testUpdateProjectNameMultilineString() throws IOException {
        testRename("Reference.Cargo.toml", "Test.Cargo.MultilineString.toml");
    }

    @Test
    public void testUpdateProjectNameString() throws IOException {
        testRename("Reference.Cargo.toml", "Test.Cargo.String.toml");
    }

    @Test
    public void testUpdateProjectNameDottedName() throws IOException {
        testRename("Reference.Cargo.DottedName.toml", "Test.Cargo.DottedName.toml");
    }


    private void testRename(String referenceName, String fileName) throws IOException {
        Path tempPath = Files.createTempFile("Cargo", "toml");
        FileObject tempFile = FileUtil.toFileObject(tempPath);
        try {
            try (InputStream is = RustProjectOperationsTest.class.getResourceAsStream("data/" + fileName);
                 OutputStream os = tempFile.getOutputStream()) {
                FileUtil.copy(is, os);
            }
            RustProjectOperations.updateProjectName(tempFile, "Renamed\"Test");
            String reference = URLMapper
                    .findFileObject(RustProjectOperationsTest.class.getResource("data/" + referenceName))
                    .asText("UTF-8");
            String result = tempFile.asText("UTF-8");
            assertEquals(reference, result);
        } finally {
            tempFile.delete();
        }
    }

}
