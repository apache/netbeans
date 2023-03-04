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


package org.openide.loaders;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.util.logging.Level;
import java.util.*;
import java.util.concurrent.Semaphore;
import java.util.logging.Handler;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import org.netbeans.junit.Log;
import org.netbeans.junit.NbTestCase;
import org.openide.filesystems.*;
import org.openide.util.*;

/** Tests for internals of FolderList as there seems to be some
 * inherent problems.
 *
 * @author Jaroslav Tulach
 */
public class FolderListTest extends NbTestCase {
    private FileObject folder;
    private FolderList list;
    private FileObject a;
    private FileObject b;
    private FileObject c;
    
    
    public FolderListTest(String testName) {
        super(testName);
    }
    
    @Override
    protected Level logLevel() {
        return Level.FINE;
    }

    @Override
    protected void setUp() throws Exception {
        clearWorkDir();
        
        LocalFileSystem lfs = new LocalFileSystem();
        lfs.setRootDirectory(getWorkDir());

        folder = FileUtil.createFolder(lfs.getRoot(), "folder");

        a = FileUtil.createData(folder, "A.txt");
        b = FileUtil.createData(folder, "B.txt");
        c = FileUtil.createData(folder, "C.txt");
        
        list = FolderList.find(folder, true);
    }

    public void testComputeChildrenList() throws Exception {
        L listener = new L();       
        RequestProcessor.Task t = list.computeChildrenList(listener);
        t.waitFinished();
        
        assertEquals("Three files", 3, listener.cnt);
        assertTrue("finished", listener.finished);

        assertEquals("a", a, listener.cummulate.get(0).getPrimaryFile());
        assertEquals("c", b, listener.cummulate.get(1).getPrimaryFile());
        assertEquals("b", c, listener.cummulate.get(2).getPrimaryFile());
    }

    public void testComputeChildrenListOrder() throws Exception {
        list.computeChildrenList(new L()).waitFinished();
        
        class PCL implements PropertyChangeListener {
            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                list.assertNullComparator();
            }
        }
        PCL pcl = new PCL();
        
        list.addPropertyChangeListener(pcl);
        
        a.setAttribute("position", 300);
        b.setAttribute("position", 200);
        c.setAttribute("position", 100);

        L listener = new L();
        RequestProcessor.Task t = list.computeChildrenList(listener);
        t.waitFinished();

        assertEquals("Three files", 3, listener.cnt);
        assertTrue("finished", listener.finished);

        assertEquals("a", a, listener.cummulate.get(2).getPrimaryFile());
        assertEquals("c", b, listener.cummulate.get(1).getPrimaryFile());
        assertEquals("b", c, listener.cummulate.get(0).getPrimaryFile());
    }
    
    /**
     * Test for bug 240388.
     *
     * @throws java.io.IOException
     */
    public void testComparatorTaskCalledOnlyOnce() throws IOException {

        Logger err = Logger.getLogger("org.openide.loaders.FolderList");
        Log.enable(err.getName(), Level.FINE);
        final int[] comparisons = new int[1];
        final Semaphore canSort = new Semaphore(0);

        Handler h = new Handler() {

            @Override
            public void publish(LogRecord record) {
                String message = record.getMessage();
                if (message != null) {
                    if (message.equals("changeComparator on {0}: previous")) {
                        try {
                            canSort.acquire();
                        } catch (InterruptedException ex) {
                            Exceptions.printStackTrace(ex);
                        }
                    } else if (message.equals("changeComparator: get new")) {
                        comparisons[0]++;
                    }
                }
            }

            @Override
            public void flush() {
            }

            @Override
            public void close() throws SecurityException {
            }
        };

        list.computeChildrenList(new L()).waitFinished();
        err.addHandler(h);
        try {
            a.setAttribute("position", 300);
            b.setAttribute("position", 200);
            c.setAttribute("position", 100);

            canSort.release(99); //Postpone sorting until all positions are set.

            L listener = new L();
            RequestProcessor.Task t = list.computeChildrenList(listener);
            t.waitFinished();
        } finally {
            err.removeHandler(h);
        }

        assertEquals("Comparator task should be called only once.", 1,
                comparisons[0]);
    }

    static class L implements FolderListListener {
        int cnt;
        boolean finished;
        List<DataObject> cummulate = new ArrayList<DataObject>();

        @Override
        public void finished(List<DataObject> arr) {
            assertTrue(arr.isEmpty());
            finished = true;
        }

        @Override
        public void process(DataObject obj, List<DataObject> arr) {
            cnt++;
            cummulate.add(obj);
        }
    }
}
