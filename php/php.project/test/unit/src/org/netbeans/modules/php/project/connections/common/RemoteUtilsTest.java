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
package org.netbeans.modules.php.project.connections.common;

import org.netbeans.junit.NbTestCase;

public class RemoteUtilsTest extends NbTestCase {

    public RemoteUtilsTest(String name) {
        super(name);
    }

    public void testSanitizeDirectoryPath() {
        assertEquals("/dir", RemoteUtils.sanitizeDirectoryPath("/dir/"));
        assertEquals("/dir", RemoteUtils.sanitizeDirectoryPath("/dir//"));
        assertEquals("/dir", RemoteUtils.sanitizeDirectoryPath("/dir/////"));
        assertEquals("/", RemoteUtils.sanitizeDirectoryPath("/"));
    }

    public void testSanitizeUploadDirectory() {
        assertEquals("/dir", RemoteUtils.sanitizeUploadDirectory("/dir", true));
        assertEquals("/dir", RemoteUtils.sanitizeUploadDirectory("/dir", false));
        assertEquals("/", RemoteUtils.sanitizeUploadDirectory("/", false));
        assertEquals("", RemoteUtils.sanitizeUploadDirectory("/", true));
        assertEquals("/", RemoteUtils.sanitizeUploadDirectory(null, false));
        assertEquals("/", RemoteUtils.sanitizeUploadDirectory("", false));
        assertEquals("", RemoteUtils.sanitizeUploadDirectory(null, true));
        assertEquals("", RemoteUtils.sanitizeUploadDirectory("", true));
    }

    public void testParentPathForFiles() {
        assertEquals(null, RemoteUtils.getParentPath("a"));
        assertEquals("a", RemoteUtils.getParentPath("a/b"));
        assertEquals("a/b", RemoteUtils.getParentPath("a/b/c"));
        assertEquals("/", RemoteUtils.getParentPath("/a"));
        assertEquals("/a", RemoteUtils.getParentPath("/a/b"));
        assertEquals("/a/b", RemoteUtils.getParentPath("/a/b/c"));
    }

    public void testParentPathForFolders() {
        assertEquals(null, RemoteUtils.getParentPath("/"));
        assertEquals(null, RemoteUtils.getParentPath("a/"));
        assertEquals("a", RemoteUtils.getParentPath("a/b/"));
        assertEquals("a/b", RemoteUtils.getParentPath("a/b/c/"));
        assertEquals("/", RemoteUtils.getParentPath("/a/"));
        assertEquals("/a", RemoteUtils.getParentPath("/a/b/"));
        assertEquals("/a/b", RemoteUtils.getParentPath("/a/b/c/"));
    }

    public void testNameForFiles() {
        assertEquals("a", RemoteUtils.getName("a"));
        assertEquals("b", RemoteUtils.getName("a/b"));
        assertEquals("c", RemoteUtils.getName("a/b/c"));
        assertEquals("a", RemoteUtils.getName("/a"));
        assertEquals("b", RemoteUtils.getName("/a/b"));
        assertEquals("c", RemoteUtils.getName("/a/b/c"));
    }

    public void testNameForFolders() {
        assertEquals("/", RemoteUtils.getName("/"));
        assertEquals("a", RemoteUtils.getName("a/"));
        assertEquals("b", RemoteUtils.getName("a/b/"));
        assertEquals("c", RemoteUtils.getName("a/b/c/"));
        assertEquals("a", RemoteUtils.getName("/a/"));
        assertEquals("b", RemoteUtils.getName("/a/b/"));
        assertEquals("c", RemoteUtils.getName("/a/b/c/"));
    }

}
