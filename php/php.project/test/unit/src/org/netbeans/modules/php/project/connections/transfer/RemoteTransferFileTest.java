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
