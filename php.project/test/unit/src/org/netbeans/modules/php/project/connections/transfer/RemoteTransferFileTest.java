/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2011 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2011 Sun Microsystems, Inc.
 */
package org.netbeans.modules.php.project.connections.transfer;

import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.php.project.connections.RemoteClientImplementation;
import org.netbeans.modules.php.project.connections.spi.RemoteFile;

public class RemoteTransferFileTest extends NbTestCase {

    public RemoteTransferFileTest(String name) {
        super(name);
    }

    // #204874
    public void testNoEndingSlashForParentDirectory0() {
        RemoteClientImplementation remoteClient = new RemoteClient(null, "/pub/myproject");
        RemoteFile remoteFile = new RemoteFileImpl("readme.txt", "/pub/myproject/mydir", false);
        RemoteTransferFile transferFile = new RemoteTransferFile(remoteClient, remoteFile, null);
        assertEquals("readme.txt", transferFile.getName());
        assertEquals("mydir", transferFile.getParentRemotePath());
    }

    // #204874
    public void testNoEndingSlashForParentDirectory1() {
        RemoteClientImplementation remoteClient = new RemoteClient(null, "/pub/myproject");
        RemoteFile remoteFile = new RemoteFileImpl("readme.txt", "/pub/myproject/mydir/", false);
        RemoteTransferFile transferFile = new RemoteTransferFile(remoteClient, remoteFile, null);
        assertEquals("readme.txt", transferFile.getName());
        assertEquals("mydir", transferFile.getParentRemotePath());
    }

    // #204874
    public void testNoEndingSlashForParentDirectory2() {
        RemoteClientImplementation remoteClient = new RemoteClient(null, "/pub/myproject");
        RemoteFile remoteFile = new RemoteFileImpl("readme.txt", "/pub/myproject/mydir//", false);
        RemoteTransferFile transferFile = new RemoteTransferFile(remoteClient, remoteFile, null);
        assertEquals("readme.txt", transferFile.getName());
        assertEquals("mydir", transferFile.getParentRemotePath());
    }

    // #204874
    public void testNoEndingSlashForParentDirectory3() {
        RemoteClientImplementation remoteClient = new RemoteClient(null, "/");
        RemoteFile remoteFile = new RemoteFileImpl("readme.txt", "/", false);
        RemoteTransferFile transferFile = new RemoteTransferFile(remoteClient, remoteFile, null);
        assertEquals("readme.txt", transferFile.getName());
        assertEquals(TransferFile.REMOTE_PROJECT_ROOT, transferFile.getParentRemotePath());
    }

    // #204874
    public void testNoEndingSlashForParentDirectory4() {
        RemoteClientImplementation remoteClient = new RemoteClient(null, "/");
        RemoteFile remoteFile = new RemoteFileImpl("readme.txt", "//", false);
        RemoteTransferFile transferFile = new RemoteTransferFile(remoteClient, remoteFile, null);
        assertEquals("readme.txt", transferFile.getName());
        assertEquals(TransferFile.REMOTE_PROJECT_ROOT, transferFile.getParentRemotePath());
    }

    public void testParentDirectory0() {
        RemoteTransferFile.checkParentDirectory("/home", "/home/project");
        RemoteTransferFile.checkParentDirectory("/", "/home");
    }

    public void testParentDirectory1() {
        try {
            RemoteTransferFile.checkParentDirectory("/home", "/var");
            fail("Should not get here");
        } catch (IllegalArgumentException exc) {
            // expected
        }
    }

    public void testParentDirectory2() {
        try {
            RemoteTransferFile.checkParentDirectory("/home", "/home2");
            fail("Should not get here");
        } catch (IllegalArgumentException exc) {
            // expected
        }
    }

    public void testParentDirectory3() {
        try {
            RemoteTransferFile.checkParentDirectory("/home", "/home2/project");
            fail("Should not get here");
        } catch (IllegalArgumentException exc) {
            // expected
        }
    }

    public void testRemotePath1() {
        RemoteClientImplementation remoteClient = new RemoteClient(null, "/");
        RemoteFile remoteFile1 = new RemoteFileImpl("readme.txt", "/", true);
        RemoteTransferFile transferFile1 = new RemoteTransferFile(remoteClient, remoteFile1, null);
        assertEquals("/readme.txt", transferFile1.getAbsolutePath());
        assertEquals("readme.txt", transferFile1.getRemotePath());
        RemoteFile remoteFile2 = new RemoteFileImpl("readme.txt", "/mydir", true);
        RemoteTransferFile transferFile2 = new RemoteTransferFile(remoteClient, remoteFile2, null);
        assertEquals("/mydir/readme.txt", transferFile2.getAbsolutePath());
        assertEquals("mydir/readme.txt", transferFile2.getRemotePath());
    }

    public void testRemotePath2() {
        RemoteClientImplementation remoteClient = new RemoteClient(null, "/subdir");
        RemoteFile remoteFile1 = new RemoteFileImpl("readme.txt", "/subdir", true);
        RemoteTransferFile transferFile1 = new RemoteTransferFile(remoteClient, remoteFile1, null);
        assertEquals("/subdir/readme.txt", transferFile1.getAbsolutePath());
        assertEquals("readme.txt", transferFile1.getRemotePath());
        RemoteFile remoteFile2 = new RemoteFileImpl("readme.txt", "/subdir/mydir", true);
        RemoteTransferFile transferFile2 = new RemoteTransferFile(remoteClient, remoteFile2, null);
        assertEquals("/subdir/mydir/readme.txt", transferFile2.getAbsolutePath());
        assertEquals("mydir/readme.txt", transferFile2.getRemotePath());
    }

}
