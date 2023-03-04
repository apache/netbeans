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
import org.netbeans.junit.MockServices;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.masterfs.filebasedfs.fileobjects.FileObjectFactory;
import org.netbeans.modules.masterfs.filebasedfs.naming.NamingFactory;
import org.openide.filesystems.FileChangeAdapter;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Lookup;

/** Test the behavior of Watcher.
 *
 * @author Jaroslav Tulach <jtulach@netbeans.org>
 */
public class WatcherTest extends NbTestCase {
    private TestNotifier notify;
    private L listener;
    private Watcher watcher;
    
    public WatcherTest(String s) {
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
        notify.awake();
    }
    
    public void testAddRemoveListener() throws IOException {
        FileObject root = FileUtil.toFileObject(getWorkDir());
        FileObject folder = root.createFolder("dir");
        
        notify.assertRegistered("No registered paths yet");
        
        root.addFileChangeListener(listener);
        
        notify.assertRegistered("One path is being listened to", root.getPath());
        
        root.removeFileChangeListener(listener);
        
        notify.assertRegistered("Path has been cleared", (String)null);
    }

    public void testRemoveListenerFromAFileObj() throws Exception {
        File f = new File(new File(getWorkDir(), "dir"), "X.txt");
        final File pf = f.getParentFile();
        pf.mkdirs();
        f.createNewFile();
        
        FileObject fo = FileUtil.toFileObject(f);
        assertTrue("Is data", fo.isData());
        
        f.delete();
        pf.delete();
        FileObject parent = FileUtil.toFileObject(getWorkDir()).createData("dir");
        assertTrue("Also data", parent.isData());
        
        
        fo.removeFileChangeListener(new FileChangeAdapter());
    }
    
    public void testGarbageCollectListener() throws Exception {
        FileObject root = FileUtil.toFileObject(getWorkDir());
        FileObject folder = root.createFolder("dir");
        
        notify.assertRegistered("No registered paths yet");
        
        root.addFileChangeListener(listener);
        
        notify.assertRegistered("One path is being listened to", root.getPath());
        
        Reference<FileObject> ref = new WeakReference<FileObject>(root);
        root = null;
        folder = null;
        assertGC("Root can be GCed", ref);

        watcher.clearQueue();
        notify.assertRegistered("Path has been cleared", (String)null);
    }
    
    public void testAddRemoveOnData() throws IOException {
        FileObject root = FileUtil.toFileObject(getWorkDir());
        FileObject folder = root.createFolder("dir");
        FileObject data = root.createData("data.txt");
        
        notify.assertRegistered("No registered paths yet");
        
        data.addFileChangeListener(listener);
        root.addFileChangeListener(listener);
        
        notify.assertRegistered("One path is being listened to", root.getPath());
        
        data.removeFileChangeListener(listener);

        notify.assertRegistered("Still one path is being listened to", root.getPath());
        
        root.removeFileChangeListener(listener);
        
        notify.assertRegistered("Path has been cleared", (String)null);
    }
    
    public void testAddRemoveOnData2() throws IOException {
        FileObject root = FileUtil.toFileObject(getWorkDir());
        FileObject folder = root.createFolder("dir");
        FileObject data = root.createData("data.txt");
        
        notify.assertRegistered("No registered paths yet");
        
        data.addFileChangeListener(listener);
        root.addFileChangeListener(listener);
        
        notify.assertRegistered("One path is being listened to", root.getPath());
        
        root.removeFileChangeListener(listener);

        notify.assertRegistered("Still one path is being listened to", root.getPath());
        
        data.removeFileChangeListener(listener);
        
        notify.assertRegistered("Path has been cleared", (String)null);
    }
    
    private static final class L extends FileChangeAdapter {
    }
    
    public static final class TestNotifier extends Notifier<Integer> {
        final List<String> registered = new LinkedList<String>();

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
        public synchronized String nextEvent() throws IOException, InterruptedException {
            wait();
            return null;
        }

        synchronized void awake() {
            notifyAll();
        }

        final void assertRegistered(String msg, String... expected) {
            assertEquals("Proper size for " + msg + "\n" + registered, expected.length, registered.size());
            for (int i = 0; i < expected.length; i++) {
                assertEquals(i + ". " + msg, expected[i], registered.get(i));
            }
        }

        @Override
        protected void start() throws IOException {
            registered.clear();
        }
    }
}
