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
