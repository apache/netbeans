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

import java.io.File;
import java.io.IOException;
import java.beans.PropertyChangeEvent;
import java.util.HashSet;
import java.util.Set;
import java.beans.PropertyChangeListener;
import java.lang.ref.WeakReference;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.openide.ErrorManager;

import org.openide.filesystems.*;
import org.openide.nodes.*;

// XXX to do:
// - loaders are never asked to recognize an invalid file object (#13926)

/** Test invalidation of objects: getting of node delegate,
 * whether folder instances include them, whether loaders are
 * asked to recognize invalid objects, etc.
 * @author Jesse Glick
 */
public class DataObjectInvalidationTest extends LoggingTestCaseHid {
    Logger log;
    
    public DataObjectInvalidationTest(String name) {
        super(name);
    }

    protected Level logLevel() {
        return Level.FINE;
    }
    
    protected void setUp() throws IOException {
        clearWorkDir();

        log = Logger.getLogger("TEST-" + getName());
        registerIntoLookup(new Pool());
    }
    
    protected void tearDown() throws Exception {
        WeakReference ref = new WeakReference(DataLoader.getLoader(SlowDataLoader.class));
        Pool.setExtra(null);
        assertGC("Let's cleanup all nodes, data objects created in previous test", ref);
    }
    
    public void testNobodyCanAccessDataObjectWithUnfinishedConstructor () throws Throwable {
        FileSystem lfs = TestUtilHid.createLocalFileSystem(getWorkDir (), new String[] {
            "folder/file.slow",
        });
        final FileObject file = lfs.findResource("folder/file.slow");
        assertNotNull(file);
        
        final DataLoader l = DataLoader.getLoader(SlowDataLoader.class);
        Pool.setExtra(l);
        class DoTheWork extends Object implements Runnable {
            private DataObject first;
            /** any eception from run method */
            private Throwable ex;
            /** thread that shall call into the constructor */
            private Thread constructor;

            public void runInMainThread () throws Throwable {
                wait (); // notified from HERE

                // I am going to be the constructor of the SlowDataObject
                constructor = Thread.currentThread();
                first = DataObject.find (file);


                // waiting for results
                wait ();
                if (ex != null) {
                    throw ex;
                }
            }

            public void run () {
                try {
                    synchronized (l) {
                        synchronized (this) {
                            notifyAll (); // HERE
                        }

                        // this wait is notified from the midle of SlowDataObject
                        // constructor
                        l.wait ();

                        // that means the thread in runInMainThread() have not finished
                        // the assignment to first variable yet
                        assertNull (first);
                    }

                    // now try to get the DataObject while its constructor
                    // is blocked in the case
                    DataObject obj = DataObject.find (file);
                    assertEquals ("It is the slow obj", SlowDataObject.class, obj.getClass());
                    SlowDataObject slow = (SlowDataObject)obj;

                    assertEquals ("Constructor has to finish completely, by the main thread", constructor, slow.ok);

                } catch (Throwable ex) {
                    this.ex = ex;
                } finally {
                    synchronized (this) {
                        notify ();
                    }
                }
            }
        }

        DoTheWork dtt = new DoTheWork();
        synchronized (dtt) {
            new Thread (dtt, "Slow").start ();
            dtt.runInMainThread ();
        }


    }
    
    public void testNodeDelegateNotRequestedTillObjReady() throws Exception {
        FileSystem lfs = TestUtilHid.createLocalFileSystem(getWorkDir (), new String[] {
            "folder/file.slow",
        });
        FileObject folder = lfs.findResource("folder");
        assertNotNull(folder);
        DataLoader l = DataLoader.getLoader(SlowDataLoader.class);
        Pool.setExtra(l);
        DataFolder f = DataFolder.findFolder(folder);
        Node foldernode = f.getNodeDelegate();
        Children folderkids = foldernode.getChildren();
        // Force it to recognize its children:
        Node[] nodes = folderkids.getNodes(true);
        assertEquals("Number of children", 1, nodes.length);
        assertEquals("Correct node delegate", "slownode", nodes[0].getShortDescription());
    }
    
    /** See #15902.
     * If a filesystem changes root, existing file objects become invalid
     * and any data objects from them must also become invalid quickly.
     */
    public void testDataObjectInvalidatedAfterRootChange() throws Exception {
        LocalFileSystem lfs = (LocalFileSystem)TestUtilHid.createLocalFileSystem(getWorkDir (), new String[] {
            "folder/file.simple",
        });
        Repository.getDefault().addFileSystem(lfs);
        try {
            FileObject fo = lfs.findResource("folder/file.simple");
            DataLoader l = DataLoader.getLoader(DataLoaderOrigTest.SimpleUniFileLoader.class);
            Pool.setExtra(l);
            DataObject dob = DataObject.find(fo);
            assertEquals(l, dob.getLoader());
            assertTrue(fo.isValid());
            assertTrue(dob.isValid());
            File olddir = lfs.getRootDirectory();
            File newdir = new File(olddir, "folder");
            //File newdir = olddir.getParentFile();
            assertTrue(newdir.exists());
            ExpectingListener el = new ExpectingListener();
            lfs.addPropertyChangeListener(el);
            lfs.setRootDirectory(newdir);
            assertTrue("PROP_ROOT was fired", el.gotSomething(FileSystem.PROP_ROOT));
            assertTrue("PROP_SYSTEM_NAME was fired", el.gotSomething(FileSystem.PROP_SYSTEM_NAME));
            FileObject fo2 = lfs.findResource("file.simple");
            assertNotNull(fo2);
            assertTrue(fo != fo2);
            DataObject dob2 = DataObject.find(fo2);
            assertEquals(l, dob2.getLoader());
            assertTrue(dob != dob2);
            assertTrue("FileSystem is still valid after change in root directory", lfs.isValid());
            assertTrue(fo == dob.getPrimaryFile());
            //assertTrue(fo.getFileSystem() == lfs);
            if (fo.isValid()) {
                // Just in case it needs time to be invalidated:
                Thread.sleep(1000);
            }
            // Does nothing: lfs.getRoot().refresh()
            // Currently this fails, not sure why:
            assertTrue("FileObject invalidated after change in root directory", ! fo.isValid());
            assertTrue("DataObject invalidated after change in root directory", ! dob.isValid());
        } finally {
            Repository.getDefault().removeFileSystem(lfs);
        }
        TestUtilHid.destroyLocalFileSystem(getName());
    }
    
    public void testCopyAndTemplateWorks () throws Exception {
        String[] arr = new String[] {
            "folder/file.slow",
        };
        FileSystem lfs = TestUtilHid.createLocalFileSystem(getWorkDir (), arr);
        FileObject file = lfs.findResource(arr[0]);
        DataLoader l = DataLoader.getLoader(SlowDataLoader.class);
        Pool.setExtra(l);
        try {
            DataObject obj = DataObject.find (file);
            DataFolder f = DataFolder.findFolder(file.getFileSystem().getRoot());
            obj.copy (f);
            obj.createFromTemplate(f);
        } finally {
            TestUtilHid.destroyLocalFileSystem(getName());
        }
    }

    public void testInvalidationHappensImmediatelly () throws Exception {
        String[] arr = new String[] {
            "folder/file.slow",
        };
        FileSystem lfs = TestUtilHid.createLocalFileSystem(getWorkDir (), arr);
        FileObject file = lfs.findResource(arr[0]);
        
        DataObject obj = DataObject.find (file);
        DataObject newObj;
        
        DataLoader l = DataLoader.getLoader(SlowDataLoader.class);
        Pool.setExtra(l);
        try {
            assertFalse ("The previous data object is not valid anymore", obj.isValid ());
            newObj = DataObject.find (file);
            assertTrue ("This is valid", newObj.isValid ());
        } finally {
            TestUtilHid.destroyLocalFileSystem(getName());
            Pool.setExtra(null);
        }
        
        assertFalse ("After remove, it is invalidated", newObj.isValid ());
        
        DataObject again = DataObject.find (file);
        
        assertEquals ("The same loader as before", obj.getLoader (), again.getLoader ());
    }
    
    private static final class ExpectingListener implements PropertyChangeListener {
        private final Set changes = new HashSet(); // Set<String>
        public synchronized void propertyChange(PropertyChangeEvent ev) {
            changes.add(ev.getPropertyName());
            //System.err.println("got: " + ev.getSource() + " " + ev.getPropertyName() + " " + ev.getOldValue() + " " + ev.getNewValue());//XXX
            notifyAll();
        }
        public synchronized boolean gotSomething(String prop) throws InterruptedException {
            if (changes.contains(prop)) return true;
            wait(3000);
            return changes.contains(prop);
        }
    }
    
    public static final class SlowDataLoader extends UniFileLoader {
        public static int createCount = 0;
        private static Logger ERR = Logger.getLogger("SlowDataLoader");
        public SlowDataLoader() {
            super(SlowDataObject.class.getName());
        }
        protected void initialize() {
            super.initialize();
            getExtensions().addExtension("slow");
        }
        protected String displayName() {
            return "Slow";
        }
        protected MultiDataObject createMultiObject(FileObject pf) throws IOException {
            ERR.info("in createMultiObject for: " + pf);
            SlowDataObject o = new SlowDataObject(pf, this);
            ERR.info("created object : " + o);
            //new Exception("creating for: " + pf + " count=" + createCount).printStackTrace();
            return o;
        }
    }
    public static final class SlowDataObject extends MultiDataObject {
        public Thread ok;
        public static int createCount = 0;
        public SlowDataObject(FileObject pf, MultiFileLoader loader) throws IOException {
            super(pf, loader);
            synchronized (loader) {
                SlowDataLoader.ERR.info("Incrementing SlowDataObject count to " + ++createCount);
                SlowDataLoader.ERR.info("Incrementing SlowDataLoader count to " + ++SlowDataLoader.createCount);
                
                // in case somebody is listening on the loader for our creation
                // let him wake up
                SlowDataLoader.ERR.info("Wake up sleepers");
                loader.notifyAll ();
            }
            
            int cnt = 1;
            
            while (cnt-- > 0) {
                try {
                    Thread.sleep(2000);
                } catch (InterruptedException ie) {
                    throw new IOException(ie.toString());
                }
            }
            
            
            ok = Thread.currentThread();
            SlowDataLoader.ERR.info("End of constructor");
        }
        protected Node createNodeDelegate() {
            return new SlowDataNode(this);
        }
        
        protected DataObject handleCopy (DataFolder df) throws java.io.IOException {
            FileObject fo = this.getPrimaryEntry().copy (df.getPrimaryFile(), "slow");
            return new SlowDataObject (fo, (MultiFileLoader)this.getLoader());
        }
        protected DataObject handleCreateFromTemplate (DataFolder df, String s) throws java.io.IOException {
            FileObject fo = this.getPrimaryEntry().createFromTemplate (df.getPrimaryFile(), null);
            return new SlowDataObject (fo, (MultiFileLoader)this.getLoader());
        }
    }
    public static final class SlowDataNode extends DataNode {
        public SlowDataNode(SlowDataObject o) {
            super(o, Children.LEAF);
            if (o.ok == null) throw new IllegalStateException("getDataNode called too early");
            // Serve as a marker that this is the correct data node kind
            // (instanceof will not work because of filter nodes):
            setShortDescription("slownode");
        }
    }

    private static final class Pool extends DataLoaderPool {
        private static DataLoader extra;
        
        
        protected java.util.Enumeration loaders () {
            if (extra == null) {
                return org.openide.util.Enumerations.empty ();
            } else {
                return org.openide.util.Enumerations.singleton (extra);
            }
        }

        public static void setExtra(DataLoader aExtra) {
            if (aExtra != null && extra != null) {
                fail("Cannot set extra loader while one is already there. 1: " + extra + " 2: " + aExtra);
            }
            extra = aExtra;
            Pool p = (Pool)DataLoaderPool.getDefault();
            p.fireChangeEvent(new javax.swing.event.ChangeEvent(p));
        }
    }
}
