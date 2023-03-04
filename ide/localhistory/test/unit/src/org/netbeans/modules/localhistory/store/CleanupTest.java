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
package org.netbeans.modules.localhistory.store;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.util.logging.Level;
import org.netbeans.modules.localhistory.LogHandler;
import org.netbeans.modules.versioning.core.api.VCSFileProxy;
import org.netbeans.modules.versioning.ui.history.HistorySettings;

/**
 *
 * @author tomas
 */
public class CleanupTest extends LHTestCase {

    public CleanupTest(String testName) {
        super(testName);      
    }

    @Override
    protected Level logLevel() {
        return Level.ALL;
    }

    public void xtestCleanUp() throws Exception {
        LocalHistoryTestStore store = createStore();
        File folder = new File(getDataDir(), "datafolder");        
        folder.mkdirs();
        
        // create the files
        File file1 = new File(folder, "file1");
        File file2 = new File(folder, "file2");
        File file3 = new File(folder, "file3");
        File file4 = new File(folder, "file4");
        File file5 = new File(folder, "file5");
        File file6 = new File(folder, "file6");
        
        // lets get some history
        
        // 4 days ago
        long ts = System.currentTimeMillis() - 4 * 24 * 60 * 60 * 1000;
        createFile(store, folder, ts, null);
        createFile(store, file1, ts, "data1");
        createFile(store, file2, ts, "data2");
        createFile(store, file3, ts, "data3");
        createFile(store, file4, ts, "data4");
        store.setLabel(VCSFileProxy.createFileProxy(file1), ts, "dil");                
        store.setLabel(VCSFileProxy.createFileProxy(file2), ts, "dil2");                
        
        long tsCreateFile5 = ts; 
        createFile(store, file5, tsCreateFile5, "data5"); // this one will get deleted by cleanup
                
        // 2 days ago
        ts = System.currentTimeMillis() - 2 * 24 * 60 * 60 * 1000;
        changeFile(store, file1, ts, "data1.1");
        changeFile(store, file2, ts, "data2.1");
        changeFile(store, file3, ts, "data3.1");
        changeFile(store, file4, ts, "data4.1");
        
        long tsCreateFile6 = ts;
        createFile(store, file6, tsCreateFile6, "data6");
        
        long tsLabelFile2 = ts; 
        String labelFile2 = "dil2.1";
        store.setLabel(VCSFileProxy.createFileProxy(file2), tsLabelFile2, labelFile2);  // two labels - each timestamp got one               
        
        store.setLabel(VCSFileProxy.createFileProxy(file3), ts, "dil3");                
        
        // check the files created in storage
        assertFile(file1, store, ts,    -1, 3, 2, "data1.1", TOUCHED);
        assertFile(file2, store, ts,    -1, 3, 2, "data2.1", TOUCHED);
        assertFile(file3, store, ts,    -1, 3, 2, "data3.1", TOUCHED);
        assertFile(file4, store, ts,    -1, 2, 2, "data4.1", TOUCHED);
        assertFile(file5, store, tsCreateFile5, -1, 2, 2, "data5",   TOUCHED);
//        assertFile(file6, store, tsCreateFile6, -1, 2, 2, "data6",   TOUCHED);
        
        // run clean up - time to live = 3 days 
        long ttl = 3 * 24 * 60 * 60 * 1000;
        store.cleanUp(ttl); 
        
        // check the cleaned storage
        assertFile(file1, store, ts, -1, 1, 2, "data1.1", TOUCHED);
        assertFile(file2, store, ts, -1, 2, 2, "data2.1", TOUCHED);
        assertFile(file3, store, ts, -1, 2, 2, "data3.1", TOUCHED);
        assertFile(file4, store, ts, -1, 1, 2, "data4.1", TOUCHED);
        
        // check labels for file2 - the first one should be deleted
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        DataOutputStream dos = new DataOutputStream(baos);
        dos.writeLong(tsLabelFile2);
        dos.writeInt(labelFile2.length());
        dos.writeChars(labelFile2);        
        dos.flush();
        
        File labelsFile = store.getLabelsFile(VCSFileProxy.createFileProxy(file2));
        assertDataInFile(labelsFile, baos.toByteArray());
                
        // check the history for folder - should contain only 1 file created 2 days ago
        baos = new ByteArrayOutputStream();
        dos = new DataOutputStream(baos);
        dos.writeLong(tsCreateFile6);
        dos.writeInt(0);
        dos.writeChars("");                
        dos.writeInt(file6.getAbsolutePath().length());
        dos.writeChars(file6.getAbsolutePath());                
        dos.writeInt(TOUCHED);
        dos.flush();
        
        File historyFile = store.getHistoryFile(VCSFileProxy.createFileProxy(folder));
        
        assertDataInFile(historyFile, baos.toByteArray());
        dos.close();
    }                   

    public void testCleanUpAll() throws Exception {
        LocalHistoryTestStore store = createStore();
        File folder = new File(getDataDir(), "datafolder");        
        folder.mkdirs();
        
        // create the files
        File file1 = new File(folder, "file1");
        File file2 = new File(folder, "file2");        

        // CREATE HISTORY
        // 4 days ago
        long ts = System.currentTimeMillis() - 4 * 24 * 60 * 60 * 1000;
        long ts1 = createFile(store, file1, ts, "data1");
        long ts2 = createFile(store, file2, ts, "data2");        

        // check the files created in storage
        assertFile(file1, store, ts1,    -1, 1, 1, "data1", TOUCHED);
        assertFile(file2, store, ts2,    -1, 1, 1, "data2", TOUCHED);
                
        // run clean up - time to live = 3 days 
        long ttl = 3 * 24 * 60 * 60 * 1000;
        store.cleanUp(ttl); 

        // storage is EMPTY
        File storage = new File(new File(getDataDir(), "var"), "filehistory");
        assertEquals(1, storage.list().length);
    }
    
    public void testCleanUp() throws Exception {
        System.setProperty("netbeans.localhistory.storeChangesAsynchronously", "true");
                
        LocalHistoryTestStore store = createStore();
        LogHandler lh = new LogHandler("copied file", LogHandler.Compare.STARTS_WITH);
        File folder = new File(getDataDir(), "datafolder");        
        folder.mkdirs();
        
        // create the files
        File file1 = new File(folder, "file1");
        File file2 = new File(folder, "file2");        

        // CREATE HISTORY
        // 4 days ago
        long ts = System.currentTimeMillis() - 4 * 24 * 60 * 60 * 1000;
        createFile(store, file1, ts, "data1");
        createFile(store, file2, ts, "data2");        

        // 2 days ago
        ts = System.currentTimeMillis() - 2 * 24 * 60 * 60 * 1000;
        lh.reset(); long ts1 = changeFile(store, file1, ts, "data1.1"); lh.waitUntilDone();
        lh.reset(); long ts2 = changeFile(store, file2, ts, "data2.1"); lh.waitUntilDone();
        
        // check the files created in storage
        assertFile(file1, store, ts1, -1, 2, 1, "data1.1", TOUCHED);
        assertFile(file2, store, ts2, -1, 2, 1, "data2.1", TOUCHED);
                
        // run clean up - time to live = 3 days 
        long ttl = 3 * 24 * 60 * 60 * 1000;
        store.cleanUp(ttl); 

        // check the files after the cleanup
        // the versions data1 and data2 are to be deleted
        assertFile(file1, store, ts1, -1, 1, 0, "data1.1", TOUCHED);
        assertFile(file2, store, ts2, -1, 1, 0, "data2.1", TOUCHED);
    }    
        
    public void testCleanUpLabels() throws Exception {
        System.setProperty("netbeans.localhistory.storeChangesAsynchronously", "true");
        
        LocalHistoryTestStore store = createStore();
        LogHandler lh = new LogHandler("copied file", LogHandler.Compare.STARTS_WITH);
        File folder = new File(getDataDir(), "datafolder");        
        folder.mkdirs();
        
        // create the files
        File file1 = new File(folder, "file1");
        
        // CREATE HISTORY
        // 5 days ago
        long ts5days = System.currentTimeMillis() - 5 * 24 * 60 * 60 * 1000;
        ts5days = createFile(store, file1, ts5days, "data1");
        
        // check the files created in storage
        assertFile(file1, store, ts5days, -1, 1, 1, "data1", TOUCHED, false);
        
        // 4 days ago
        long ts4days = System.currentTimeMillis() - 4 * 24 * 60 * 60 * 1000;
        lh.reset(); ts4days = changeFile(store, file1, ts4days, "data1.1"); lh.waitUntilDone();
        
        // label a file => it's not going to be cleanedup
        store.setLabel(VCSFileProxy.createFileProxy(file1), ts4days, "labeltext");

        // check the files created in storage
        assertFile(file1, store, ts4days, -1, 2, 1, "data1.1", TOUCHED, true);
        
        // 2 days ago
        long ts2days = System.currentTimeMillis() - 2 * 24 * 60 * 60 * 1000;
        lh.reset(); ts2days = changeFile(store, file1, ts2days, "data1.2"); lh.waitUntilDone();
        
        // check the files created in storage
        assertFile(file1, store, ts2days, -1, 3, 1, "data1.2", TOUCHED, true);
        
        HistorySettings.getInstance().setCleanUpLabeled(false);
        
        // run clean up - time to live = 3 days 
        long ttl = 3 * 24 * 60 * 60 * 1000;
        store.cleanUp(ttl); 

        // check the files after the cleanup
        // only the versions ts5days are to be deleted
        assertFile(file1, store, ts2days, -1, 2, 0, "data1.2", TOUCHED, true); // still 2 revision there

        HistorySettings.getInstance().setCleanUpLabeled(true);
        
        // run clean up - time to live = 3 days 
        store.cleanUp(ttl); 
        
        // check the files after the cleanup
        // the versions data1 and data1.1 and data1.2 are to be deleted
        assertFile(file1, store, ts2days, -1, 1, 0, "data1.2", TOUCHED, false); // only this one revision left
        
    }    
    
    
    
}




















