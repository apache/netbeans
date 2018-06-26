/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */
package org.netbeans.modules.php.project.connections.transfer;

import java.io.File;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.php.project.connections.RemoteClientImplementation;

/**
 * @author Tomas Mysik
 */
public class TransferFileTest extends NbTestCase {

    public TransferFileTest(String name) {
        super(name);
    }

    public void testLocalTransferFilePaths() {
        RemoteClientImplementation remoteClient = new RemoteClient("/a", "/pub/project");
        TransferFile parent1 = TransferFile.fromDirectory(remoteClient, null, new File("/a/b"));
        TransferFile file1 = TransferFile.fromFile(remoteClient, parent1, new File("/a/b/c"));
        assertEquals("c", file1.getName());
        assertEquals("b/c", file1.getRemotePath());
        assertEquals("b", file1.getParent().getRemotePath());
        assertEquals("/tmp/b/c", file1.resolveLocalFile(new File("/tmp")).getAbsolutePath());
        assertEquals("/a/b/c", file1.getLocalAbsolutePath());
        assertEquals("/pub/project/b/c", file1.getRemoteAbsolutePath());

        TransferFile file2 = TransferFile.fromFile(new RemoteClient("/a/b", "/"), null, new File("/a/b/c"));
        assertFalse(file1.equals(file2));

        TransferFile file3 = TransferFile.fromFile(new RemoteClient("/0/1/2", "/"), null, new File("/0/1/2/b/c"));
        assertTrue(file1.equals(file3));

        TransferFile file4 = TransferFile.fromFile(new RemoteClient("/a", "/"), null, new File("/a/b"));
        assertEquals("b", file4.getName());
        assertEquals("b", file4.getRemotePath());

        TransferFile file5 = TransferFile.fromFile(new RemoteClient("/a", "/"), null, new File("/a"));
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
        assertEquals("/tmp/tests/info/readme.txt", file.resolveLocalFile(new File("/tmp")).getAbsolutePath());
        assertEquals("/tmp2/tests/info/readme.txt", file.getLocalAbsolutePath());
        assertEquals("/pub/myproject/tests/info/readme.txt", file.getRemoteAbsolutePath());
    }

    public void testTransferFileRelations() {
        RemoteClientImplementation remoteClient = new RemoteClient("/a", "/");
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
        RemoteClientImplementation remoteClient = new RemoteClient("/a", "/");
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
