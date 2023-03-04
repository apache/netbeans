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
package org.openide.filesystems;

/**
 *
 * @author Alexander Simon
 */
public class TempFileObjectTestHid extends TestBaseHid {
    private FileObject root;

    public TempFileObjectTestHid(String name) {
        super(name);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        root = testedFS.findResource(getResourcePrefix());
    }

    @Override
    protected String[] getResources(String testName) {
        return new String[] {};
    }
    
    public void testTempDir() throws Exception {
        FileObject tempFolder = root.getFileSystem().getTempFolder();
        assertNotNull(tempFolder);
        assertTrue(tempFolder.isValid());
        assertTrue(tempFolder.isFolder());
        assertEquals(tempFolder.getFileSystem(), root.getFileSystem());
    }

    public void testTempFile() throws Exception {
        FileObject tempFolder = root.getFileSystem().getTempFolder();
        FileObject tempFile = root.getFileSystem().createTempFile(tempFolder, "out", ".tmp", true);
        assertNotNull(tempFile);
        assertTrue(tempFile.isValid());
        assertTrue(tempFile.isData());
        assertEquals(tempFile.getParent(), tempFolder);
        assertEquals(tempFolder.getFileSystem(), tempFile.getFileSystem());
        assertTrue(tempFile.getNameExt().startsWith("out"));
        assertTrue(tempFile.getNameExt().endsWith(".tmp"));
    }
}
