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

package org.netbeans.modules.editor.mimelookup;

import java.util.Collection;
import java.util.concurrent.CyclicBarrier;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import org.netbeans.api.editor.mimelookup.MimeLookup;
import org.netbeans.api.editor.mimelookup.MimePath;
import org.netbeans.junit.NbTestCase;
import org.netbeans.junit.RandomlyFails;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;
import org.openide.util.LookupEvent;
import org.openide.util.LookupListener;

/**
 *
 * @author vita
 */
@RandomlyFails // NB-Core-Build #1103, probably due to TestUtilities.sleepForWhile
public class MimePathLookupTest extends NbTestCase {

    private TestHandler handler;
    
    /** Creates a new instance of MimePathLookupTest */
    public MimePathLookupTest(String name) {
        super(name);
    }

    @Override
    protected void setUp() throws java.lang.Exception {
        clearWorkDir();
        // Set up the default lookup, repository, etc.
        EditorTestLookup.setLookup(new String[0], getWorkDir(), new Object[] {},
            getClass().getClassLoader()
        );
        handler = new TestHandler();
        final Logger log = Logger.getLogger(MimePathLookup.class.getName());
        log.setLevel(Level.FINE);
        log.addHandler(handler);
    }
    
    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
        final Logger log = Logger.getLogger(MimePathLookupTest.class.getName());
        log.removeHandler(handler);
    }
    
    public void testAddingMimeDataProvider() throws Exception {
        checkAddingMimeDataProvider(
            "Services/org-netbeans-modules-editor-mimelookup-DummyMimeDataProvider.instance",
            DummyMimeDataProvider.Marker.class
        );
        checkAddingMimeDataProvider(
            "Services/org-netbeans-modules-editor-mimelookup-DummyMimeLookupInitializer.instance",
            DummyMimeLookupInitializer.Marker.class
        );
    }
    
    private <T> void checkAddingMimeDataProvider(String instanceFile, Class<T> markerClass) throws Exception {
        MimePath path = MimePath.get("text/x-java");
        Lookup lookup = MimeLookup.getLookup(path);
        
        Collection markers = lookup.lookupAll(markerClass);
        assertEquals("There should be no markers", 0, markers.size());
        
        // Add the data provider
        TestUtilities.createFile(getWorkDir(), instanceFile);
        TestUtilities.sleepForWhile();
        
        markers = lookup.lookupAll(markerClass);
        assertEquals("No markers found", 1, markers.size());
    }

    public void testAddingMimeDataProvider2() throws Exception {
        checkAddingMimeDataProvider2(
            "Services/org-netbeans-modules-editor-mimelookup-DummyMimeDataProvider.instance",
            DummyMimeDataProvider.Marker.class
        );
        checkAddingMimeDataProvider2(
            "Services/org-netbeans-modules-editor-mimelookup-DummyMimeLookupInitializer.instance",
            DummyMimeLookupInitializer.Marker.class
        );
    }
    
    private <T> void checkAddingMimeDataProvider2(String instanceFile, Class<T> markerClass) throws Exception {
        MimePath path = MimePath.get("text/x-java");
        
        Lookup.Result result = MimeLookup.getLookup(path).lookupResult(markerClass);
        Collection markers = result.allInstances();
        assertEquals("There should be no markers", 0, markers.size());

        L listener = new L();
        result.addLookupListener(listener);
        assertEquals("There should be no changes received", 0, listener.resultChangedCnt);
        
        // Add the data provider
        TestUtilities.createFile(getWorkDir(), instanceFile);
        TestUtilities.sleepForWhile();
        
        assertEquals("No changes received", 1, listener.resultChangedCnt);
        markers = result.allInstances();
        assertEquals("No markers found", 1, markers.size());
    }
    
    public void testRemovingMimeDataProvider() throws Exception {
        checkRemovingMimeDataProvider(
            "Services/org-netbeans-modules-editor-mimelookup-DummyMimeDataProvider.instance",
            DummyMimeDataProvider.Marker.class
        );
        checkRemovingMimeDataProvider(
            "Services/org-netbeans-modules-editor-mimelookup-DummyMimeLookupInitializer.instance",
            DummyMimeLookupInitializer.Marker.class
        );
    }
    
    private <T> void checkRemovingMimeDataProvider(String instanceFile, Class<T> markerClass) throws Exception {
        TestUtilities.createFile(getWorkDir(), instanceFile);
        TestUtilities.sleepForWhile();

        MimePath path = MimePath.get("text/x-java");
        Lookup lookup = MimeLookup.getLookup(path);
        
        Collection markers = lookup.lookupAll(markerClass);
        assertEquals("No markers found", 1, markers.size());

        TestUtilities.deleteFile(getWorkDir(), instanceFile);
        TestUtilities.sleepForWhile();
        
        markers = lookup.lookupAll(markerClass);
        assertEquals("There should be no markers", 0, markers.size());
    }

    public void testRemovingMimeDataProvider2() throws Exception {
        checkRemovingMimeDataProvider2(
            "Services/org-netbeans-modules-editor-mimelookup-DummyMimeDataProvider.instance",
            DummyMimeDataProvider.Marker.class
        );
        checkRemovingMimeDataProvider2(
            "Services/org-netbeans-modules-editor-mimelookup-DummyMimeLookupInitializer.instance",
            DummyMimeLookupInitializer.Marker.class
        );
    }
    
    private <T> void checkRemovingMimeDataProvider2(String instanceFile, Class<T> markerClass) throws Exception {
        TestUtilities.createFile(getWorkDir(), instanceFile);
        TestUtilities.sleepForWhile();

        MimePath path = MimePath.get("text/x-java");
        Lookup.Result result = MimeLookup.getLookup(path).lookupResult(markerClass);
        Collection markers = result.allInstances();
        assertEquals("No markers found", 1, markers.size());

        L listener = new L();
        result.addLookupListener(listener);
        assertEquals("There should be no changes received", 0, listener.resultChangedCnt);

        // Remove the data provider
        TestUtilities.deleteFile(getWorkDir(), instanceFile);
        TestUtilities.sleepForWhile();
        
        assertEquals("No changes received", 1, listener.resultChangedCnt);
        markers = result.allInstances();
        assertEquals("There should be no markers", 0, markers.size());
    }
    
    private static class L implements LookupListener {
        public int resultChangedCnt = 0;
        
        @Override
        public void resultChanged(LookupEvent ev) {
            resultChangedCnt++;
        }
    } // End of L class
    
    void addMimeDataProvider3() throws Exception {
        addMimeDataProvider3(
            "Services/org-netbeans-modules-editor-mimelookup-DummyMimeDataProvider.instance",
            DummyMimeDataProvider.Marker.class
        );
        addMimeDataProvider3(
            "Services/org-netbeans-modules-editor-mimelookup-DummyMimeLookupInitializer.instance",
            DummyMimeLookupInitializer.Marker.class
        );
}
    
    private <T> void addMimeDataProvider3(String instanceFile, Class<T> markerClass) throws Exception {
        MimePath path = MimePath.get("text/x-java");
        Lookup lookup = MimeLookup.getLookup(path);
        Collection markers = lookup.lookupAll(markerClass);
        assertEquals("There should be no markers", 0, markers.size());
        
        // Add the data provider
        TestUtilities.createFile(getWorkDir(), instanceFile);
    }
    
    public void testRebuildNoDeadlock() throws Exception {
        CyclicBarrier barrier= new CyclicBarrier(2);
        
        final Task1 task1 = new Task1();
        Thread t1 = new Thread(task1, "Thread 1");
        final Task2 task2 = new Task2(barrier);
        Thread t2 = new Thread(task2, "Thread 2");
        handler.setBarrier(barrier);
        t1.start();
        t2.start();
        t1.join(60000);
        t2.join(60000); // wait max 1 min for the test to finish
        assertTrue(task1.done);
        assertTrue(task2.done);
    }
    
    private static class Task1 implements Runnable {

        volatile boolean done = false;

        @Override
        public void run() {
            // System.out.println("T1 running");
            MimePath path = MimePath.get("text/x-java");
            Lookup lookup = MimeLookup.getLookup(path);
            lookup.lookup(Task1.class);
            // System.out.println("T1 done");
            done = true;
        }
        
    }
    
    private class Task2 implements Runnable {

        volatile boolean done = false;
        private CyclicBarrier barrier;

        public Task2(CyclicBarrier b) {
            barrier = b;
        }
        
        @Override
        public void run() {
            
            try {
                barrier.await();
                // System.out.println("T2 running");
                addMimeDataProvider3();
                // System.out.println("T2 done");
                done = true;
            } catch (Exception ex) {
                Exceptions.printStackTrace(ex);
            }
        }
        
    }
    
    private static class TestHandler extends Handler {
        
        private CyclicBarrier barrier;
        
        void setBarrier(CyclicBarrier b) {
            this.barrier = b;
        }
        
        @Override
        public void publish(LogRecord record) {
            final String message = record.getMessage();
            if (message.startsWith("Rebuilding MimeLookup for") && Thread.currentThread().getName().equals("Thread 1")) {
                try {
                    // System.out.println("Publish enter");
                    barrier.await();
                    // System.out.println("Publish waiting");
                    Thread.sleep(5000); // Give the other thread a chance to deadlock
                    // System.out.println("Publish exit");
                } catch (Exception ex) {
                    Exceptions.printStackTrace(ex);
                }
            }
        }

        @Override
        public void flush() {
        }

        @Override
        public void close() throws SecurityException {
        }
    }
}
