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

package org.netbeans.modules.remote.impl.fs;

import java.io.File;
import java.util.Arrays;
import java.util.concurrent.Future;
import junit.framework.Test;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironment;
import org.netbeans.modules.nativeexecution.api.util.ConnectionManager;
import org.netbeans.modules.nativeexecution.api.util.FileInfoProvider;
import org.netbeans.modules.nativeexecution.api.util.FileInfoProvider.StatInfo;
import org.netbeans.modules.nativeexecution.test.ForAllEnvironments;
import org.netbeans.modules.remote.test.RemoteApiTest;

/**
 *
 */
public class DirectoryStorageTestCase extends RemoteFileTestBase {

    public DirectoryStorageTestCase(String testName, ExecutionEnvironment execEnv) {
        super(testName, execEnv);
    }
           
    @ForAllEnvironments
    public void testDirectoryStorageSftp() throws Exception {
        File file = File.createTempFile("directoryStorage", ".dat");
        try {
            ConnectionManager.getInstance().connectTo(execEnv);
            Future<StatInfo> res = FileInfoProvider.lstat(getTestExecutionEnvironment(), "/usr/include");
            assertNotNull(res);
            StatInfo statInfo = res.get();
            
            final String cacheName = "name.cache";
            DirEntry entry1 = DirEntryImpl.create(statInfo, cacheName, execEnv);
            final String name = statInfo.getName();
            final long size = statInfo.getSize();
            final String link = statInfo.getLinkTarget();
            String dummy = "inexistent_file";
            DirEntry dummyEntry = new DirEntryInvalid(dummy);
            DirectoryStorage ds1 = new DirectoryStorage(file, Arrays.asList(entry1, dummyEntry));
            ds1.store();
            DirectoryStorage ds2 = DirectoryStorage.load(file, execEnv);
            DirEntry entry2 = ds2.getValidEntry(entry1.getName());
            assertNotNull("No entry restored for " + entry1.getName(), entry2);
            assertEquals("Name", name, entry2.getName());
            assertTrue(entry2.isValid());
            assertEquals("Cache", cacheName, entry2.getCache());            
//            assertEquals("User", user, entry2.getUser());
//            assertEquals("Group", group, entry2.getGroup());
            assertEquals("Size", size, entry2.getSize());
            assertEquals("Timestamp", entry1.getLastModified(), entry2.getLastModified());
            assertEquals("Link", link, entry2.getLinkTarget());
            DirEntry dummyEntry2 = ds2.getValidEntry(dummy);
            assertTrue(ds2.isKnown(dummy));
            assertNull(dummyEntry2);
        } finally {
            file.delete();
        }        
    }

    private void assertEntriesEqual(DirEntry e1, DirEntry e2) throws Exception {
        assertEquals("getCache", e1.getCache(), e2.getCache());
        assertEquals("getName", e1.getName(), e2.getName());
        assertEquals("getDevice", e1.getDevice(), e2.getDevice());
        assertEquals("getINode", e1.getINode(), e2.getINode());
        assertEquals("getFileType", e1.getFileType(), e2.getFileType());
        assertEquals("getLastModified", e1.getLastModified(), e2.getLastModified());
        assertEquals("getLinkTarget", e1.getLinkTarget(), e2.getLinkTarget());
        assertEquals("getSize", e1.getSize(), e2.getSize());
        assertEquals("isDirectory", e1.isDirectory(), e2.isDirectory());
        assertEquals("isLink", e1.isLink(), e2.isLink());
        assertEquals("isPlainFile", e1.isPlainFile(), e2.isPlainFile());
        assertEquals("isValid", e1.isValid(), e2.isValid());
        assertEquals("canExecute", e1.canExecute(), e2.canExecute());
        assertEquals("canRead", e1.canRead(), e2.canRead());
        assertEquals("canWrite", e1.canWrite(), e2.canWrite());
    }

    @ForAllEnvironments
    public void testDirectoryStorageFS() throws Exception {
        File file = File.createTempFile("directoryStorage", ".dat");
        try {
            ConnectionManager.getInstance().connectTo(execEnv);
            DirEntryList entries = RemoteFileSystemTransport.readDirectory(execEnv, "/");
            DirectoryStorage ds1 = new DirectoryStorage(file, entries.getEntries());
            ds1.store();
            DirectoryStorage ds2 = DirectoryStorage.load(file, execEnv);
            for (DirEntry e1 : entries.getEntries()) {
                DirEntry e2 = ds2.getValidEntry(e1.getName());
                assertNotNull("Null entry for " + e1.getName(), e2);
                assertEntriesEqual(e1, e2);
            }
            for (DirEntry e2 : ds2.listAll()) {
                DirEntry e1 = ds1.getValidEntry(e2.getName());
                assertNotNull("Null entry for " + e1.getName(), e2);
                assertEntriesEqual(e1, e2);
            }
        } finally {
            file.delete();
        }
    }

    public static Test suite() {
        return RemoteApiTest.createSuite(DirectoryStorageTestCase.class);
    }
    
}
