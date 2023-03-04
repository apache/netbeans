/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.netbeans.modules.web.clientproject.api.validation;

import java.io.File;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.web.common.api.ValidationResult;

public class FolderValidatorTest extends NbTestCase {

    private File folder;


    public FolderValidatorTest(String name) {
        super(name);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        clearWorkDir();
        File projectDir = new File(getWorkDir(), "project123");
        assertTrue(projectDir.mkdir());
        folder = new File(projectDir, "public_html");
        assertTrue(folder.mkdir());
    }

    public void testValidate() {
        ValidationResult result = new FolderValidator()
                .validateFolder(folder)
                .getResult();
        assertTrue(result.getErrors().isEmpty());
        assertTrue(result.getWarnings().isEmpty());
    }

    public void testNoFolder() {
        ValidationResult result = new FolderValidator()
                .validateFolder(null)
                .getResult();
        assertFalse(result.getErrors().isEmpty());
        assertEquals(FolderValidator.FOLDER, result.getErrors().get(0).getSource());
        assertTrue(result.getWarnings().isEmpty());
    }

    public void testFileAsFolder() throws Exception {
        File readme = new File(folder, "readme.txt");
        assertTrue(readme.createNewFile());
        ValidationResult result = new FolderValidator()
                .validateFolder(readme)
                .getResult();
        assertFalse(result.getErrors().isEmpty());
        assertEquals(FolderValidator.FOLDER, result.getErrors().get(0).getSource());
        assertTrue(result.getWarnings().isEmpty());
    }

    public void testNonExistingFolder() throws Exception {
        File subdir = new File(folder, "subdir");
        assertFalse(subdir.exists());
        ValidationResult result = new FolderValidator()
                .validateFolder(subdir)
                .getResult();
        assertFalse(result.getErrors().isEmpty());
        assertEquals(FolderValidator.FOLDER, result.getErrors().get(0).getSource());
        assertTrue(result.getWarnings().isEmpty());
    }

    public void testNonExistingFolderWithCustomName() throws Exception {
        String dirName = "sdf5465fd4sdf";
        File subdir = new File(folder, "subdir");
        assertFalse(subdir.exists());
        ValidationResult result = new FolderValidator()
                .validateFolder(subdir, dirName)
                .getResult();
        assertFalse(result.getErrors().isEmpty());
        ValidationResult.Message error = result.getErrors().get(0);
        assertEquals(FolderValidator.FOLDER, error.getSource());
        assertTrue(error.getMessage(), error.getMessage().contains(dirName));
        assertTrue(result.getWarnings().isEmpty());
    }

}