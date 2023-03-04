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

package org.netbeans.modules.openfile;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Semaphore;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import junit.framework.TestCase;
import org.netbeans.junit.Log;
import org.netbeans.modules.openfile.RecentFiles.HistoryItem;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.URLMapper;
import org.openide.loaders.DataObject;
import org.openide.util.Exceptions;
import org.openide.util.lookup.Lookups;
import org.openide.windows.CloneableTopComponent;


/**
 * Tests for RecentFiles support, tests list modification policy.
 *
 * @author Dafe Simonek
 */
public class RecentFilesTest extends TestCase {
    
    private static final Logger RFLOG = Logger.getLogger(
            RecentFiles.class.getName());
    WaitHandler waitHandler = null;

    public RecentFilesTest(String testName) {
        super(testName);
    }

    @Override
    protected void setUp() throws Exception {
        Log.enable(RFLOG.getName(), Level.FINE);

        waitHandler = new WaitHandler();
        RFLOG.addHandler(waitHandler);

        RecentFiles.clear();
        RecentFiles.init();
        waitHandler.waitUntilStored();
    }

    @Override
    protected void tearDown() throws Exception {
        RFLOG.removeHandler(waitHandler);
        waitHandler = null;
    }

    public void testGetRecentFiles () throws Exception {
        System.out.println("Testing RecentFiles.getRecentFiles...");
        URL url = RecentFilesTest.class.getResource("resources/recent_files/");
        assertNotNull("url not found.", url);
        
        FileObject folder = URLMapper.findFileObject(url);
        FileObject[] files = folder.getChildren();
        List<EditorLikeTC> tcs = new ArrayList<EditorLikeTC>();
        
        for (FileObject curFo : files) {
            EditorLikeTC curTC = new EditorLikeTC(curFo);
            tcs.add(0, curTC);
            curTC.open();
            waitHandler.waitUntilStored();
        }

        // close top components and check if they were added correctly to
        // recent files list
        for (EditorLikeTC curTC : tcs) {
            curTC.close();
            waitHandler.waitUntilStored();
        }
        int i = 0;
        List<HistoryItem> recentFiles = RecentFiles.getRecentFiles();
        assertTrue("Expected " + files.length + " recent files, got " + recentFiles.size(), files.length == recentFiles.size());
        for (FileObject fo : files) {
            assertEquals(RecentFiles.convertFile2Path(fo), recentFiles.get(i).getPath());
            i++;
        }

        // reopen first component again and check that it was removed from
        // recent files list
        tcs.get(0).open();
        waitHandler.waitUntilStored();
        recentFiles = RecentFiles.getRecentFiles();
        assertTrue(files.length == (recentFiles.size() + 1));
        
    }
    
    public void testPersistence () throws Exception {
        System.out.println("Testing perfistence of recent files history...");
        URL url = RecentFilesTest.class.getResource("resources/recent_files/");
        assertNotNull("url not found.", url);
        
        FileObject folder = URLMapper.findFileObject(url);
        FileObject[] files = folder.getChildren();
        
        // store, load and check for equality
        for (int i=0; i < files.length; i++) {
            FileObject file = files[i];
            RecentFiles.addFile(RecentFiles.convertFile2Path(file));
            waitHandler.waitUntilStored();
        }
        RecentFiles.store();
        waitHandler.waitUntilStored();
        List<HistoryItem> loaded = RecentFiles.load();
        waitHandler.waitUntilStored(); //load() also invokes store() internally
        assertTrue("Persistence failed, " + files.length + " stored items, " + loaded.size() + " loaded.", files.length == loaded.size());
        int i = files.length - 1;
        for (FileObject fileObject : files) {
            assertTrue("File #" + (i + 1) + " differs", 
                    fileObject.equals(RecentFiles.convertPath2File(loaded.get(i--).getPath())));
        }
    }
    
    public void test87252 () throws Exception {
        System.out.println("Testing fix for 87252...");
        URL url = RecentFilesTest.class.getResource("resources/recent_files/");
        assertNotNull("url not found.", url);
        
        FileObject folder = URLMapper.findFileObject(url);
        FileObject fo = folder.createData("ToBeDeleted.txt");
        
        EditorLikeTC tc = new EditorLikeTC(fo);
        tc.open();
        waitHandler.waitUntilStored();
        tc.close();
        waitHandler.waitUntilStored();
        
        // delete file and check that recent files *doesn't* contain the file
        fo.delete();
        List<HistoryItem> recentFiles = RecentFiles.getRecentFiles();
        boolean contained = false;
        for (HistoryItem historyItem : recentFiles) {
            if (fo.equals(RecentFiles.convertPath2File(historyItem.getPath()))) {
                contained = true;
                break;
            }
        }
        assertFalse("Deleted file should not be in recent files", contained);
    }
    

    /** Special TopComponent subclass which imitates TopComponents used for documents, editors */
    private static class EditorLikeTC extends CloneableTopComponent {
        
        public EditorLikeTC (FileObject fo) throws Exception {
            DataObject dObj = DataObject.find(fo);
            associateLookup(Lookups.singleton(dObj));
        }
        
    }

    /**
     * A Logger handler that waits until next "Stored" message is logged. So we
     * can wait for triggered background tasks before continuing in the test.
     */
    private static class WaitHandler extends java.util.logging.Handler {

        private final String pattern = "Stored";
        private final Semaphore semaphore;

        public WaitHandler() {
            this.semaphore = new Semaphore(0);
        }

        @Override
        public void publish(LogRecord record) {
            if (record.getMessage() != null && pattern != null
                    && record.getMessage().matches(pattern)) {
                semaphore.release();
            }
        }

        @Override
        public void flush() {
        }

        @Override
        public void close() throws SecurityException {
        }

        public void reset() {
            semaphore.drainPermits();
        }

        public void waitUntilStored() {
            try {
                semaphore.acquire();
            } catch (InterruptedException ex) {
                Exceptions.printStackTrace(ex);
            }
        }
    }
}
