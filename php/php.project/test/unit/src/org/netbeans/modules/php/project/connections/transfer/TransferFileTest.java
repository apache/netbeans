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
package org.netbeans.modules.php.project.connections.transfer;

import java.io.File;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.php.project.connections.RemoteClientImplementation;
import org.openide.filesystems.FileUtil;

/**
 * @author Tomas Mysik
 */
public class TransferFileTest extends NbTestCase {

    public TransferFileTest(String name) {
        super(name);
    }

    public void testLocalTransferFilePaths() {
        RemoteClientImplementation remoteClient = new RemoteClient(FileUtil.normalizePath("/a"), "/pub/project");
        TransferFile parent1 = TransferFile.fromDirectory(remoteClient, null, new File("/a/b"));
        TransferFile file1 = TransferFile.fromFile(remoteClient, parent1, new File("/a/b/c"));
        assertEquals("c", file1.getName());
        assertEquals("b/c", file1.getRemotePath());
        assertEquals("b", file1.getParent().getRemotePath());
        assertEquals(new File("/tmp/b/c").getAbsolutePath(), file1.resolveLocalFile(new File("/tmp")).getAbsolutePath());
        assertEquals(FileUtil.normalizePath("/a/b/c"), file1.getLocalAbsolutePath());
        assertEquals("/pub/project/b/c", file1.getRemoteAbsolutePath());

        TransferFile file2 = TransferFile.fromFile(new RemoteClient(FileUtil.normalizePath("/a/b"), "/"), null, new File("/a/b/c"));
        assertFalse(file1.equals(file2));

        TransferFile file3 = TransferFile.fromFile(new RemoteClient(FileUtil.normalizePath("/0/1/2"), "/"), null, new File("/0/1/2/b/c"));
        assertTrue(file1.equals(file3));

        TransferFile file4 = TransferFile.fromFile(new RemoteClient(FileUtil.normalizePath("/a"), "/"), null, new File("/a/b"));
        assertEquals("b", file4.getName());
        assertEquals("b", file4.getRemotePath());

        TransferFile file5 = TransferFile.fromFile(new RemoteClient(FileUtil.normalizePath("/a"), "/"), null, new File("/a"));
        assertEquals("a", file5.getName());
        assertTrue(file5.isProjectRoot());
        assertSame(TransferFile.REMOTE_PROJECT_ROOT, file5.getRemotePath());
        try {
            assertEquals(null, file5.getParent().getRemotePath());
            fail("Should not get here");
        } catch (IllegalStateException ex) {
            // expected
        }
    }

    public void testRemoteTransferFilePaths() {
        RemoteClientImplementation remoteClient = new RemoteClient("/tmp2", "/pub/myproject");
        TransferFile parent = TransferFile.fromRemoteFile(remoteClient, null,
                new RemoteFileImpl("info", "/pub/myproject/tests", false));
        TransferFile file = TransferFile.fromRemoteFile(remoteClient, parent,
                new RemoteFileImpl("readme.txt", "/pub/myproject/tests/info", true));
        assertEquals("readme.txt", file.getName());
        assertEquals("tests/info/readme.txt", file.getRemotePath());
        assertEquals("tests/info", file.getParent().getRemotePath());
        assertEquals(new File("/tmp/tests/info/readme.txt").getAbsolutePath(), file.resolveLocalFile(new File("/tmp")).getAbsolutePath());
        assertEquals(new File("/tmp2/tests/info/readme.txt").getAbsolutePath(), file.getLocalAbsolutePath());
        assertEquals("/pub/myproject/tests/info/readme.txt", file.getRemoteAbsolutePath());
    }

    public void testTransferFileRelations() {
        RemoteClientImplementation remoteClient = new RemoteClient(FileUtil.normalizePath("/a"), "/");
        TransferFile projectRoot = TransferFile.fromDirectory(remoteClient, null, new File("/a"));
        assertTrue(projectRoot.isRoot());
        assertTrue(projectRoot.isProjectRoot());
        assertTrue(projectRoot.getLocalChildren().toString(), projectRoot.getLocalChildren().isEmpty());

        TransferFile child1 = TransferFile.fromFile(remoteClient, projectRoot, new File("/a/1"));
        TransferFile child2 = TransferFile.fromFile(remoteClient, projectRoot, new File("/a/2"));
        for (TransferFile child : new TransferFile[] {child1, child2}) {
            assertNotNull(child.getParent());
            assertFalse(child.isRoot());
            assertFalse(child.isProjectRoot());
            assertSame(child.getParent().toString(), projectRoot, child.getParent());
        }
        assertFalse(projectRoot.getLocalChildren().toString(), projectRoot.getLocalChildren().isEmpty());
        assertSame(projectRoot.getLocalChildren().toString(), 2, projectRoot.getLocalChildren().size());
        assertTrue(projectRoot.getLocalChildren().toString(), projectRoot.getLocalChildren().contains(child1));
        assertTrue(projectRoot.getLocalChildren().toString(), projectRoot.getLocalChildren().contains(child2));
    }

    public void testParentRemotePath() {
        RemoteClientImplementation remoteClient = new RemoteClient(FileUtil.normalizePath("/a"), "/");
        TransferFile projectRoot = TransferFile.fromDirectory(remoteClient, null, new File("/a"));
        TransferFile childWithParent = TransferFile.fromDirectory(remoteClient, projectRoot, new File("/a/b"));
        TransferFile childWithoutParent = TransferFile.fromFile(remoteClient, null, new File("/a/b"));

        assertEquals(projectRoot.getRemotePath(), childWithParent.getParentRemotePath());
        assertEquals(projectRoot.getRemotePath(), childWithoutParent.getParentRemotePath());

        TransferFile grandchildWithParent = TransferFile.fromFile(remoteClient, childWithParent, new File("/a/b/c"));
        TransferFile grandchildWithoutParent = TransferFile.fromFile(remoteClient, null, new File("/a/b/c"));

        assertEquals(childWithParent.getRemotePath(), grandchildWithParent.getParentRemotePath());
        assertEquals(childWithParent.getRemotePath(), grandchildWithoutParent.getParentRemotePath());
    }

    // #220823
    public void testAbsoluteRemotePath() {
        RemoteClientImplementation remoteClient = new RemoteClient(null, "/");
        TransferFile file = TransferFile.fromRemoteFile(remoteClient, null,
                new RemoteFileImpl("readme.txt", "/", false));
        assertEquals("/readme.txt", file.getRemoteAbsolutePath());
    }

}
