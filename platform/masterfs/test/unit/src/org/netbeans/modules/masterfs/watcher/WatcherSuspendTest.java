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
package org.netbeans.modules.masterfs.watcher;

import java.io.File;
import org.netbeans.modules.masterfs.providers.Notifier;
import java.io.IOException;
import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import org.netbeans.junit.MockServices;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.masterfs.filebasedfs.fileobjects.FileObjectFactory;
import org.netbeans.modules.masterfs.filebasedfs.naming.NamingFactory;
import org.openide.filesystems.FileChangeAdapter;
import org.openide.filesystems.FileEvent;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Lookup;

/** Test the behavior of Watcher.
 *
 * @author Jaroslav Tulach <jtulach@netbeans.org>
 */
public class WatcherSuspendTest extends NbTestCase {
    private TestNotifier notify;
    private L listener;
    private Watcher watcher;
    
    public WatcherSuspendTest(String s) {
        super(s);
    }

    @Override
    protected void setUp() throws Exception {
        clearWorkDir();
        MockServices.setServices(TestNotifier.class);
        listener = new L();
        watcher = Lookup.getDefault().lookup(Watcher.class);
        notify = Lookup.getDefault().lookup(TestNotifier.class);
        notify.start();
    }

    @Override
    protected void tearDown() throws Exception {
    }
    
    public void testSuspendRequests() throws Exception {
        FileObject root = FileUtil.toFileObject(getWorkDir());
        FileObject folder = root.createFolder("dir");
        File dir = FileUtil.toFile(folder);
        
        FileObject[] arr = folder.getChildren();
        folder.addFileChangeListener(listener);
        assertEquals("Empty ", 0, arr.length);
        
        final String prop = "org.netbeans.io.suspend".intern();
        synchronized (prop) {
            int prev = Integer.getInteger(prop, 0);
            System.setProperty(prop, "" + (prev + 1));
            prop.notifyAll();
        }
        
        new File(dir, "data.txt").createNewFile();
        notify.event.offer(dir.getPath());
        
        assertProperty("One path waiting", "1", "org.netbeans.io.pending");
        listener.assertEvents("No event yet", 0, 200);

        synchronized (prop) {
            int prev = Integer.getInteger(prop, 0);
            System.setProperty(prop, "" + (prev - 1));
            prop.notifyAll();
        }
        assertProperty("Pending status is cleared", null, "org.netbeans.io.pending");
        
        listener.assertEvents("One event delivered", 1, 5000);
        
        arr = folder.getChildren();
        assertEquals("One child", 1, arr.length);
        assertEquals("data.txt", arr[0].getNameExt());
    }

    private void assertProperty(String msg, String expVal, String propName) throws InterruptedException {
        String val = null;
        for (int i = 0; i < 100; i++) {
            val = System.getProperty(propName);
            if (expVal == null && val == null) {
                return;
            }
            if (expVal != null && expVal.equals(val)) {
                return;
            }
            Thread.sleep(100);
        }
        fail(msg + " exp: " + expVal + " was: " + val);
    }

    
    private static final class L extends FileChangeAdapter {
        private int cnt;

        @Override
        public synchronized void fileDataCreated(FileEvent fe) {
            cnt++;
        }

        @Override
        public void fileFolderCreated(FileEvent fe) {
            cnt++;
        }

        private synchronized void assertEvents(
            String msg, int cnt, int timeOut
        ) throws InterruptedException {
            if (this.cnt != cnt) {
                wait(timeOut);
            }
            assertEquals(msg, cnt, this.cnt);
        }
        
    }
    
    public static final class TestNotifier extends Notifier<Integer> {
        final List<String> registered = new LinkedList<String>();
        final BlockingQueue<String> event = new ArrayBlockingQueue<String>(10);

        @Override
        public Integer addWatch(String path) throws IOException {
            int size = registered.size();
            registered.add(path);
            return size;
        }

        @Override
        public void removeWatch(Integer key) throws IOException {
            registered.set(key, null);
        }

        @Override
        public String nextEvent() throws IOException, InterruptedException {
            return event.take();
        }

        @Override
        protected void start() throws IOException {
            registered.clear();
        }
    }
}
