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

package org.netbeans.modules.versioning.historystore;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.lang.reflect.Field;
import java.util.Map;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.versioning.util.Utils;
import org.openide.util.NbPreferences;


/**
 *
 * @author ondra
 */
public class StorageTest extends NbTestCase {

    private File workdir;

    public StorageTest (String arg0) {
        super(arg0);
    }
    
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        System.setProperty("netbeans.user", getDataDir().getAbsolutePath());
        // create
        workdir = getWorkDir();
    }
    
    public void testSetGetContent () throws Exception {
        File file = new File(workdir, "file");
        file.createNewFile();
        File[] files = new File(getDataDir(), "historycache").listFiles(new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) {
                return !name.contains("-");
            }
        });
        for (File originalFile : files) {
            testStore(originalFile);
        }
    }
    
    public void testCleanUp () throws Exception {
        File testUserDir = new File(getDataDir(), "testuserdir");
        System.setProperty("netbeans.user", testUserDir.getAbsolutePath());
        File storageBaseFolder = new File(new File(new File(testUserDir, "var"), "cache"), "vcshistory");
        assertTrue(storageBaseFolder.exists());
        File toBeCleaned1 = new File(new File(new File(new File(storageBaseFolder, "63127913e930c1ccb7623dd55664305c"), "249"), "19a1ed8cdfb53c4ccda1e65fa02c3903"), "f1");
        File toBeCleaned2 = new File(new File(new File(new File(storageBaseFolder, "63127913e930c1ccb7623dd55664305c"), "250"), "02b118e681e97d9d0f847ff2793145a2"), "f2");
        File toBeCleaned3 = new File(new File(new File(new File(storageBaseFolder, "782a5cc0e0ea079cdc9008c86a34eb37"), "249"), "19a1ed8cdfb53c4ccda1e65fa02c3903"), "f1");
        File toRemain1 = new File(new File(new File(new File(storageBaseFolder, "782a5cc0e0ea079cdc9008c86a34eb37"), "250"), "02b118e681e97d9d0f847ff2793145a2"), "f2");
        assertTrue(toBeCleaned1.exists());
        assertTrue(toBeCleaned2.exists());
        assertTrue(toBeCleaned3.exists());
        assertTrue(toRemain1.exists());
        
        toBeCleaned1.setLastModified(System.currentTimeMillis() - 1000 * 3600 * 24 * 7);
        toBeCleaned2.setLastModified(System.currentTimeMillis() - 1000 * 3600 * 24 * 7);
        toBeCleaned3.setLastModified(System.currentTimeMillis() - 1000 * 3600 * 24 * 7);
        toRemain1.setLastModified(System.currentTimeMillis() - 1000 * 3600 * 24 * 5);
        NbPreferences.forModule(Storage.class).putInt(Storage.PREF_KEY_TTL, 6); // set TTL
        Field f = StorageManager.class.getDeclaredField("INSTANCE");
        f.setAccessible(true);
        f.set(StorageManager.class, null);
        // test cleanup, should leave one content in the cache
        waitCleanUpComplete();
        assertFalse(toBeCleaned1.exists());
        assertFalse(toBeCleaned2.exists());
        assertFalse(toBeCleaned3.exists());
        assertTrue(toRemain1.exists());
        
        assertTrue(toRemain1.exists());
        assertFalse(toBeCleaned1.getParentFile().exists());
        assertFalse(toBeCleaned1.getParentFile().getParentFile().exists());
        assertFalse(toBeCleaned1.getParentFile().getParentFile().getParentFile().exists());
        assertFalse(toBeCleaned3.getParentFile().exists());
        assertFalse(toBeCleaned3.getParentFile().getParentFile().exists());
        assertTrue(storageBaseFolder.exists());
        
        // test cleanup, should remove the whole repository
        File toBeCleaned4 = toRemain1;
        toBeCleaned4.setLastModified(System.currentTimeMillis() - 1000 * 3600 * 24 * 7);
        f.set(StorageManager.class, null);
        waitCleanUpComplete();
        assertFalse(toBeCleaned4.getParentFile().exists());
        assertFalse(toBeCleaned4.getParentFile().getParentFile().exists());
        assertFalse(toBeCleaned4.getParentFile().getParentFile().getParentFile().exists());
        assertFalse(storageBaseFolder.exists());
        
        // test that we don't break anything by the cleanup and that cache is still usable
        f = StorageManager.class.getDeclaredField("storages");
        f.setAccessible(true);
        Map<String, Storage> storages = (Map<String, Storage>) f.get(StorageManager.getInstance());
        Storage storage = storages.values().iterator().next();
        File originalFile = new File(new File(getDataDir(), "historycache"), "f1");
        testContents(storage, originalFile, originalFile, true);
        storage.setContent(originalFile.getAbsolutePath(), originalFile.getName(), originalFile);
        testContents(storage, originalFile, originalFile, false);
    }
    
    public void testSetGetRevisionInfo () throws Exception {
        Storage historyStore = StorageManager.getInstance().getStorage(workdir.getAbsolutePath());
        String revision = "revisionABCD";
        String content = "content of revision";
        assertNull(historyStore.getRevisionInfo(revision));
        historyStore.setRevisionInfo(revision, new ByteArrayInputStream(content.getBytes()));
        byte[] buf = historyStore.getRevisionInfo(revision);
        assertEquals(content, new String(buf));
        
        // now does it clash with content storage??
        // let's say a file has the same path as revision
        String path = revision;
        File f = new File(workdir, "file");
        f.createNewFile();
        String fileContent = "content of file";
        Utils.copyStreamsCloseAll(new FileOutputStream(f), new ByteArrayInputStream(fileContent.getBytes()));
        historyStore.setContent(path, revision, f);
        historyStore.setRevisionInfo(revision, new ByteArrayInputStream(content.getBytes()));
        
        // test
        buf = historyStore.getRevisionInfo(revision);
        assertEquals(content, new String(buf));
        String tempFileName = "temp";
        File tmpFile = historyStore.getContent(path, tempFileName, revision);
        assertFile(f, tmpFile);
    }

    private void testStore (final File file) {
        File[] revisions = file.getParentFile().listFiles(new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) {
                return name.startsWith(file.getName());
            }
        });
        for (File rev : revisions) {
            Storage historyStore = StorageManager.getInstance().getStorage(workdir.getAbsolutePath());
            testContents(historyStore, file, rev, true);
            historyStore.setContent(file.getAbsolutePath(), rev.getName(), rev);
            testContents(historyStore, file, rev, false);
        }
    }

    private void testContents (Storage historyStore, File originalFile, File revisionContent, boolean empty) {
        File cachedContent = historyStore.getContent(originalFile.getAbsolutePath(), originalFile.getName(), revisionContent.getName());
        if (empty) {
            assertFalse(cachedContent.exists());
            assertEquals(0, cachedContent.length());
        } else {
            assertTrue(cachedContent.exists());
            assertFile(cachedContent, revisionContent);
        }
    }

    private void waitCleanUpComplete() {
        LogHandler handler = new LogHandler();
        StorageManager.LOG.setLevel(Level.ALL);
        StorageManager.LOG.addHandler(handler);
        StorageManager.getInstance();
        handler.waitComplete();
        assertTrue(handler.cleaned);
        StorageManager.LOG.removeHandler(handler);
    }
    
    private class LogHandler extends Handler {

        private boolean cleaned;
        
        @Override
        public void publish(LogRecord record) {
            if (record.getMessage().contains("SM.cleanUp: cleanup complete")) { //NOI18N
                synchronized (this) {
                    cleaned = true;
                    notifyAll();
                }
            }
        }

        @Override
        public void flush() {
            //            
        }

        @Override
        public void close() throws SecurityException {
            //
        }

        private void waitComplete() {
            synchronized (this) {
                if (!cleaned) {
                    try {
                        wait(20000);
                    } catch (InterruptedException ex) {
                        //
                    }
                }
            }
        }
        
    }
}
