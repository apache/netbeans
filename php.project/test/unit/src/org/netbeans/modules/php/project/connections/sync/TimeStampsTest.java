/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2013 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2013 Sun Microsystems, Inc.
 */

package org.netbeans.modules.php.project.connections.sync;

import java.util.HashMap;
import java.util.Map;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.php.project.connections.RemoteClientImplementation;
import org.netbeans.modules.php.project.connections.transfer.RemoteClient;
import org.netbeans.modules.php.project.connections.transfer.RemoteFileImpl;
import org.netbeans.modules.php.project.connections.transfer.TransferFile;

public class TimeStampsTest extends NbTestCase {

    private static final String PATH_PREFIX = "/path/prefix";

    private TimeStamps timeStamps;
    private StorageImpl storage;
    private TransferFile myproject;
    private TransferFile www;
    private TransferFile docs;
    private TransferFile readmeTxt;
    private TransferFile src;


    public TimeStampsTest(String name) {
        super(name);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        storage = new StorageImpl();
        timeStamps = new TimeStamps(storage, PATH_PREFIX);
        RemoteClientImplementation remoteClient = new RemoteClient(null, "/myserver");
        myproject = TransferFile.fromRemoteFile(remoteClient, null,
                new RemoteFileImpl("myproject", "/myserver", false));
        www = TransferFile.fromRemoteFile(remoteClient, myproject,
                new RemoteFileImpl("www", "/myserver/myproject", false));
        docs = TransferFile.fromRemoteFile(remoteClient, myproject,
                new RemoteFileImpl("docs", "/myserver/myproject/www", false));
        readmeTxt = TransferFile.fromRemoteFile(remoteClient, docs,
                new RemoteFileImpl("readme.txt", "/myserver/myproject/www/docs", true));
        src = TransferFile.fromRemoteFile(remoteClient, myproject,
                new RemoteFileImpl("src", "/myserver/myproject", false));
    }

    public void testFullPath() {
        assertEquals(PATH_PREFIX + "/somepath", timeStamps.getFullPath("/somepath"));
    }

    public void testEmptyTimestamps() {
        assertTrue(storage.data.isEmpty());
        assertEquals(-1, timeStamps.getSyncTimestamp(myproject));
        assertEquals(-1, timeStamps.getSyncTimestamp(www));
        assertEquals(-1, timeStamps.getSyncTimestamp(docs));
        assertEquals(-1, timeStamps.getSyncTimestamp(readmeTxt));
        assertEquals(-1, timeStamps.getSyncTimestamp(src));
        assertTrue(storage.data.isEmpty());
    }

    public void testFileTimestamp() {
        timeStamps.setSyncTimestamp(readmeTxt, 10);
        assertEquals(-1, timeStamps.getSyncTimestamp(readmeTxt));
        assertEquals(-1, timeStamps.getSyncTimestamp(www));
        assertEquals(-1, timeStamps.getSyncTimestamp(docs));
        assertEquals(-1, timeStamps.getSyncTimestamp(myproject));
        assertEquals(-1, timeStamps.getSyncTimestamp(src));
        assertTrue(storage.data.toString(), storage.data.isEmpty());
    }

    public void testDirTimestamp() {
        timeStamps.setSyncTimestamp(docs, 10);
        assertEquals(10, timeStamps.getSyncTimestamp(docs));
        assertEquals(10, timeStamps.getSyncTimestamp(readmeTxt));
        assertEquals(-1, timeStamps.getSyncTimestamp(www));
        assertEquals(-1, timeStamps.getSyncTimestamp(myproject));
        assertEquals(-1, timeStamps.getSyncTimestamp(src));
        assertEquals(storage.data.toString(), 1, storage.data.size());
        assertStorageContains(docs);
    }

    public void testParentDirTimestamp() {
        timeStamps.setSyncTimestamp(www, 10);
        assertEquals(10, timeStamps.getSyncTimestamp(www));
        assertEquals(10, timeStamps.getSyncTimestamp(docs));
        assertEquals(10, timeStamps.getSyncTimestamp(readmeTxt));
        assertEquals(-1, timeStamps.getSyncTimestamp(myproject));
        assertEquals(-1, timeStamps.getSyncTimestamp(src));
        assertEquals(storage.data.toString(), 1, storage.data.size());
        assertStorageContains(www);
    }

    public void testRootDirTimestamp() {
        timeStamps.setSyncTimestamp(myproject, 10);
        assertEquals(10, timeStamps.getSyncTimestamp(myproject));
        assertEquals(10, timeStamps.getSyncTimestamp(www));
        assertEquals(10, timeStamps.getSyncTimestamp(docs));
        assertEquals(10, timeStamps.getSyncTimestamp(readmeTxt));
        assertEquals(10, timeStamps.getSyncTimestamp(src));
        assertEquals(storage.data.toString(), 1, storage.data.size());
        assertStorageContains(myproject);
    }

    public void testMoreDirsTimestamp() {
        timeStamps.setSyncTimestamp(www, 20);
        timeStamps.setSyncTimestamp(docs, 10);
        assertEquals(20, timeStamps.getSyncTimestamp(www));
        assertEquals(10, timeStamps.getSyncTimestamp(docs));
        assertEquals(10, timeStamps.getSyncTimestamp(readmeTxt));
        assertEquals(-1, timeStamps.getSyncTimestamp(myproject));
        assertEquals(-1, timeStamps.getSyncTimestamp(src));
        assertEquals(storage.data.toString(), 2, storage.data.size());
        assertStorageContains(www);
        assertStorageContains(docs);
    }

    private void assertStorageContains(TransferFile transferFile) {
        String path = timeStamps.getFullPath(transferFile.getRemoteAbsolutePath());
        assertTrue(path + " not found in: " + storage.data, storage.data.containsKey(path));
    }

    //~ Inner classes

    private static final class StorageImpl implements TimeStamps.Storage {

        final Map<String, Long> data = new HashMap<>();


        @Override
        public long getLong(String key) {
            Long value = data.get(key);
            if (value == null) {
                return -1;
            }
            return value;
        }

        @Override
        public void setLong(String key, long value) {
            data.put(key, value);
        }

    }

}
