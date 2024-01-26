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
package org.netbeans.modules.java.file.launcher;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import org.junit.Test;

import static org.junit.Assert.*;

import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Lookup;
import org.openide.util.lookup.Lookups;

public class SingleSourceFileUtilTest {
    
    public SingleSourceFileUtilTest() {
    }
    
    @Test
    public void testGetJavaFileWithoutProjectFromLookup() throws IOException {
        FileObject java = FileUtil.createMemoryFileSystem().getRoot().createData("Ahoj.java");
        Lookup lookup = Lookups.fixed(java);
        FileObject result = SingleSourceFileUtil.getJavaFileWithoutProjectFromLookup(lookup);
        
        assertEquals("Java FileObject found in the lookup", java, result);
    }

    @Test
    public void testCanFindSiblingClass() throws IOException {
        final FileObject folder = FileUtil.createMemoryFileSystem().getRoot().createFolder("dir");
        FileObject java = folder.createData("Ahoj.java");
        assertFalse("No sibling found", SingleSourceFileUtil.hasClassSibling(java));

        FileObject clazz = folder.createData("Ahoj.class");
        assertNotNull("class created", clazz);

        assertTrue("Sibling found", SingleSourceFileUtil.hasClassSibling(java));
    }

    @Test
    public void testIsSupportedFile() throws IOException {
        File vcsDemoDir = null;
        File supportedFile = null;
        File unsupportedFile = null;
        try {
            vcsDemoDir = Files.createTempDirectory("vcs-dummy").toFile();
            supportedFile = Files.createTempFile("dummy", ".java").toFile();
            unsupportedFile = new File(vcsDemoDir, "dummy.java");
            FileUtil.createData(unsupportedFile);

            assertTrue(SingleSourceFileUtil.isSupportedFile(FileUtil.createData(supportedFile)));
            assertFalse(SingleSourceFileUtil.isSupportedFile(FileUtil.createData(unsupportedFile)));

        } finally {
            if(supportedFile != null && supportedFile.exists()) {
                supportedFile.delete();
            }
            if(unsupportedFile != null && unsupportedFile.exists()) {
                unsupportedFile.delete();
            }
            if(vcsDemoDir != null && vcsDemoDir.exists()) {
                vcsDemoDir.delete();
            }
        }
    }
}
