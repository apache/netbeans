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

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Hashtable;
import java.util.logging.Level;
import org.netbeans.junit.NbTestCase;
import org.openide.filesystems.FileLock;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileSystem;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.Repository;
import org.openide.util.Lookup;
import org.openide.util.LookupEvent;
import org.openide.util.LookupListener;
import org.openide.util.lookup.AbstractLookup;
import org.openide.util.lookup.InstanceContent;
import org.openide.util.lookup.Lookups;
import org.openide.util.lookup.ProxyLookup;

public class FolderLookupTest extends NbTestCase implements LookupListener {
    private Lookup.Result<?> res;
    private boolean bad;
    private String threadName = "";

    public FolderLookupTest(java.lang.String testName) {
        super(testName);
    }

    static {
        System.setProperty ("org.openide.util.Lookup", GLkp.class.getName());
    }

    @Override
    protected Level logLevel() {
        return null;// Level.FINE;
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        clearWorkDir();
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
        if (res != null) {
            res.allItems();
            FolderLookup.ProxyLkp.DISPATCH.waitFinished();
            if (threadName.contains("Folder") || threadName.length() == 0) {
                fail("Wrong thread name: " + threadName);
            }
        }
    }



    private void addListener(Lookup l) {
        bad = true;
        assertNull("No result yet", res);
        res = l.lookupResult(Object.class);
        res.addLookupListener(this);
        res.allItems();
    }

    public void resultChanged(LookupEvent ev) {
        if (threadName.length() == 0) {
            threadName = Thread.currentThread().getName();
        } else {
            threadName = threadName + ", " + Thread.currentThread().getName();
        }
    }

    /** Test of the lookup method. Creates a file under Services directory
     * and tries to lookup it. The object should be immediatelly found.
     *
     */
    public void testFolderLookupIsUpdatedQuickly () throws Exception {
        String fsstruct [] = new String [] {
            "AA/",
        };

        TestUtilHid.destroyLocalFileSystem (getName());
        FileSystem lfs = TestUtilHid.createLocalFileSystem (getWorkDir(), fsstruct);

        FileObject bb = lfs.findResource("/AA");

        DataFolder folder = DataFolder.findFolder (bb);


        Lookup lookup = new org.openide.loaders.FolderLookup (folder).getLookup ();
        try {
            checkTheLookup (lookup, folder);
        } finally {
            folder.delete ();
        }
    }


   private void checkTheLookup (Lookup lookup, DataFolder folder) throws Exception {
       addListener(lookup);

        Class toFind = java.util.Dictionary.class;
        Class toCreate = java.util.Hashtable.class;

        Object wrongResult = lookup.lookup (toFind);
        DataObject obj = InstanceDataObject.create (folder, "Test", toCreate);

        if (lookup.lookup (toFind) == null) {
            fail ("Lookup has not found the class");
        }
        obj.delete ();

        if (lookup.lookup (toFind) != null) {
            fail ("Still it is possible to find the class");
        }
        if (wrongResult != null) {
            // report the original mistake, means the a previous test forgot
            // to clean after itself
            fail ("There is uncleaned environment: " + wrongResult);
        }

    }

    /** Test of the lookup method. Creates files under different levels of directory
     * hierarchy and tries to lookup it. The objects should be immediatelly found.
     *
     */
    public void testFolderLookupIsUpdatedQuicklyForSubfolders () throws Exception {
        String fsstruct [] = new String [] {
            "AA/",
        };

        TestUtilHid.destroyLocalFileSystem (getName());
        FileSystem lfs = TestUtilHid.createLocalFileSystem (getWorkDir(), fsstruct);

        FileObject bb = lfs.findResource("/AA");
        assertNotNull(bb + " not found", bb);

        DataFolder folder = DataFolder.findFolder (bb);


        Lookup lookup = new org.openide.loaders.FolderLookup (folder).getLookup ();
        checkTheLookupForSubfolders (lookup, folder);
    }

    private void checkTheLookupForSubfolders (Lookup lookup, DataFolder folder) throws Exception {
        addListener(lookup);

        Class toFind = java.awt.Component.class;
        Class toCreate = javax.swing.JButton.class;

        Lookup.Result res = lookup.lookupResult(toFind);
        assertEquals("no Component's in " + res.allInstances(), 0, res.allInstances().size());

        DataObject obj = InstanceDataObject.create (folder, "Test", toCreate);
        assertNotNull(obj.getPrimaryFile() + " not found",
            folder.getPrimaryFile().getFileSystem().findResource(obj.getPrimaryFile().getPath()));
        Collection all = res.allInstances();
        assertEquals("just one Component in " + all, 1, all.size());

        DataFolder subfolder = DataFolder.create(folder, "BB");
        assertNotNull(subfolder.getPrimaryFile() + " not found",
            folder.getPrimaryFile().getFileSystem().findResource(subfolder.getPrimaryFile().getPath()));

        obj = InstanceDataObject.create (subfolder, "Test", toCreate);
        assertNotNull(obj.getPrimaryFile() + " not found",
            folder.getPrimaryFile().getFileSystem().findResource(obj.getPrimaryFile().getPath()));
        assertEquals("now two Component's in " + res.allInstances(), 2, res.allInstances().size());
    }

    /** Tests delegation stuff.
     */
    public void testProxyLookups () throws Exception {
        String fsstruct [] = new String [] {
            "AA/",
        };

        TestUtilHid.destroyLocalFileSystem (getName());
        FileSystem lfs = TestUtilHid.createLocalFileSystem (getWorkDir(), fsstruct);

        FileObject bb = lfs.findResource("/AA");

        DataFolder folder = DataFolder.findFolder (bb);


        Lookup lookup = new org.openide.loaders.FolderLookup (folder).getLookup ();
        addListener(lookup);

        InstanceDataObject obj = InstanceDataObject.create (folder, null, ALkp.class);
        if (lookup.lookup (Integer.class) == null) {
            fail ("Integer not found in delegating lookup");
        }

    }

    public void testHandleDataShadow() throws Exception {
        String fsstruct [] = new String [] {
            "AA/",
            "BB/",
            "BB/java-io-IOException.instance"
        };
        TestUtilHid.destroyLocalFileSystem (getName());
        FileSystem lfs = TestUtilHid.createLocalFileSystem (getWorkDir(), fsstruct);

        FileObject aa = lfs.findResource("/AA");
        FileObject bb = lfs.findResource("/BB");

        DataFolder a = DataFolder.findFolder (aa);
        DataFolder b = DataFolder.findFolder (bb);

        b.createShadow(a);

        FolderLookup fl = new FolderLookup(b);
        IOException io = fl.getLookup().lookup(IOException.class);

        assertNotNull("IO Exception found", io);
    }
    
    public void testFindInstanceNotCreatedByYouIssue24986 () throws Exception {
        String fsstruct [] = new String [] {
            "AA/",
        };
        TestUtilHid.destroyLocalFileSystem (getName());
        FileSystem lfs = TestUtilHid.createLocalFileSystem (getWorkDir(), fsstruct);

        FileObject bb = lfs.findResource("/AA");

        String inst = "My instnace";

        DataFolder folder = DataFolder.findFolder (bb);
        FileObject fo = FileUtil.createData (folder.getPrimaryFile (), "test.ser");
        FileLock lock = fo.lock ();
        ObjectOutputStream oss = new ObjectOutputStream (fo.getOutputStream (lock));
        oss.writeObject (inst);
        oss.close ();
        lock.releaseLock ();
        DataObject o = DataObject.find (fo);
        assertTrue ("Is IDO: " + o, o instanceof InstanceDataObject);
        InstanceDataObject obj = (InstanceDataObject)o;

        assertEquals ("The instance is created", inst, obj.instanceCreate ());
        assertNotSame ("But is not the same", inst, obj.instanceCreate ());
        inst = (String)obj.instanceCreate ();

        Lookup lookup = new org.openide.loaders.FolderLookup (folder).getLookup ();

        Lookup.Template t = new Lookup.Template (null, null, inst);
        Collection found = lookup.lookup (t).allInstances ();

        assertEquals ("Lookup finds it as well", 1, found.size ());
        assertEquals ("Lookup finds it as well", inst, found.iterator ().next());
    }

    public void testDeadlockWhileWaitingForFolderRecognizerToFinish50768 () throws Exception {
        /*
         * Used to produce deadlock between main thread and folder recognizer.
         * Similar is described in issue 50768.

"Folder recognizer" daemon prio=1 tid=0x08218478 nid=0x4f4b in Object.wait() [0x4e640000..0x4e640790]
    at java.lang.Object.wait(Native Method)
    - waiting on <0x45e41eb8> (a java.lang.Object)
    at java.lang.Object.wait(Object.java:474)
    at org.openide.util.lookup.AbstractLookup.enterStorage(AbstractLookup.java:102)
    - locked <0x45e41eb8> (a java.lang.Object)
    at org.openide.util.lookup.AbstractLookup.setPairsAndCollectListeners(AbstractLookup.java:223)
    at org.openide.util.lookup.AbstractLookup.setPairs(AbstractLookup.java:213)
    at org.openide.util.lookup.AbstractLookup$Content.setPairs(AbstractLookup.java:895)
    at org.openide.loaders.FolderLookup$ProxyLkp.update(FolderLookup.java:336)
    at org.openide.loaders.FolderLookup.createInstance(FolderLookup.java:170)
    at org.openide.loaders.FolderInstance.defaultProcessObjects(FolderInstance.java:715)
    at org.openide.loaders.FolderInstance.access$000(FolderInstance.java:68)
    at org.openide.loaders.FolderInstance$2.run(FolderInstance.java:601)
    at org.openide.loaders.FolderLookup.postCreationTask(FolderLookup.java:234)
    at org.openide.loaders.FolderInstance.processObjects(FolderInstance.java:599)
    at org.openide.loaders.FolderInstance$Listener.finished(FolderInstance.java:880)
    at org.openide.loaders.FolderList.createObjects(FolderList.java:644)
    at org.openide.loaders.FolderList.getObjects(FolderList.java:512)
    at org.openide.loaders.FolderList.access$200(FolderList.java:50)
    at org.openide.loaders.FolderList$ListTask.run(FolderList.java:877)
    at org.openide.util.Task.run(Task.java:136)
    at org.openide.util.RequestProcessor$Task.run(RequestProcessor.java:330)
    at org.openide.util.RequestProcessor$Processor.run(RequestProcessor.java:686)


"main" prio=1 tid=0x0805c538 nid=0x4f35 in Object.wait() [0xbfffc000..0xbfffcf48]
    at java.lang.Object.wait(Native Method)
    - waiting on <0x459691d8> (a org.openide.util.RequestProcessor$Task)
    at java.lang.Object.wait(Object.java:474)
    at org.openide.util.Task.waitFinished(Task.java:85)
    - locked <0x459691d8> (a org.openide.util.RequestProcessor$Task)
    at org.openide.util.RequestProcessor$Task.waitFinished(RequestProcessor.java:447)
    at org.openide.loaders.FolderList.waitProcessingFinished(FolderList.java:247)
    at org.openide.loaders.FolderInstance.waitProcessingFinished(FolderInstance.java:555)
    at org.openide.loaders.FolderInstance.waitFinished(FolderInstance.java:274)
    at org.openide.loaders.FolderInstance.instanceFinished(FolderInstance.java:262)
    at org.openide.loaders.FolderLookup$ProxyLkp.beforeLookup(FolderLookup.java:355)
    at org.openide.util.lookup.ProxyLookup.lookup(ProxyLookup.java:135)
    at org.openide.loaders.FolderLookupTest$1R.operationPostCreate(FolderLookupTest.java:239)
    at org.openide.loaders.DataLoaderPool.fireOperationEvent(DataLoaderPool.java:205)
    at org.openide.loaders.DataObjectPool.notifyCreation(DataObjectPool.java:423)
    at org.openide.loaders.DataObjectPool.notifyCreationAll(DataObjectPool.java:438)
    at org.openide.loaders.DataObjectPool.exitAllowConstructor(DataObjectPool.java:96)
    at org.openide.loaders.DataObjectPool.handleFindDataObject(DataObjectPool.java:113)
    at org.openide.loaders.DataLoader.findDataObject(DataLoader.java:233)
    at org.openide.loaders.DataLoaderPool.findDataObject(DataLoaderPool.java:378)
    at org.openide.loaders.DataLoaderPool.findDataObject(DataLoaderPool.java:338)
    at org.openide.loaders.DataObject.find(DataObject.java:456)
    at org.openide.loaders.FolderLookup$ICItem.init(FolderLookup.java:400)
    at org.openide.loaders.FolderLookup$ICItem.instanceOf(FolderLookup.java:451)
    at org.openide.util.lookup.InheritanceTree$1TwoJobs.before(InheritanceTree.java:424)
    at org.openide.util.lookup.InheritanceTree.classToNode(InheritanceTree.java:494)
    at org.openide.util.lookup.InheritanceTree.searchClass(InheritanceTree.java:513)
    at org.openide.util.lookup.InheritanceTree.lookup(InheritanceTree.java:197)
    at org.openide.util.lookup.DelegatingStorage.lookup(DelegatingStorage.java:128)
    at org.openide.util.lookup.AbstractLookup.lookupItem(AbstractLookup.java:324)
    at org.openide.util.lookup.AbstractLookup.lookup(AbstractLookup.java:307)
    at org.openide.util.lookup.ProxyLookup.lookup(ProxyLookup.java:140)
    at org.openide.loaders.FolderLookupTest.testDeadlockWhileWaitingForFolderRecognizerToFinish50768(FolderLookupTest.java:309)
         */


        String fsstruct [] = new String [] {
            "AA/X/Y/Z//java-lang-StringBuffer.instance",
        };

        TestUtilHid.destroyLocalFileSystem (getName());
        FileSystem lfs = TestUtilHid.createLocalFileSystem (getWorkDir(), fsstruct);
        Repository.getDefault ().addFileSystem (lfs);

        final FileObject bb = lfs.findResource("/AA");
        final FileObject file = lfs.findResource ("/AA/X/Y/Z/java-lang-StringBuffer.instance");
        final DataFolder folder = DataFolder.findFolder (bb);
        final Thread t = Thread.currentThread ();
        DataLoaderPool pool = DataLoaderPool.getDefault ();
        assertNotNull (pool);



        class R implements OperationListener, FolderListListener {
            public int cnt;
            public int created;
            public Exception ex;
            public Lookup lookup;
            public boolean doWrongThing;

            /* Empty implementation */
            public void operationPostCreate (OperationEvent ev) {
                if (doWrongThing && file.equals (ev.getObject ().getPrimaryFile ())) {
                    assertSame ("The right thread", t, Thread.currentThread ());

                    doWrongThing = false;
                    cnt++;


                    try {
                        synchronized (this) {
                            if (!alreadyBlocked) {
                                wait ();
                            }
                        }
                        // this initializes the rescan
                        FileUtil.createData (bb, "java-lang-Object.instance");
                    } catch (Exception ex) {
                        this.ex = ex;
                    }
                    Lookup l = lookup;
                    if (l != null) {
                        // and this will deadlock
                        lookup.lookup (String.class);
                    }
                }
            }

            /* Empty implementation */
            public void operationCopy (OperationEvent.Copy ev) {
            }

            /* Empty implementation */
            public void operationMove (OperationEvent.Move ev) {
            }

            /* Empty implementation */
            public void operationDelete (OperationEvent ev) {
            }

            /* Empty implementation */
            public void operationRename (OperationEvent.Rename ev) {
            }

            /* Empty implementation */
            public void operationCreateShadow (OperationEvent.Copy ev) {
            }

            /* Empty implementation */
            public void operationCreateFromTemplate (OperationEvent.Copy ev) {
            }

            private boolean alreadyBlocked;
            public synchronized void process (DataObject obj, java.util.List arr) {
                if (!alreadyBlocked) {
                    try {
                        wait (1000);
                    } catch (InterruptedException ex) {
                        fail ("No exceptions");
                    }
                    alreadyBlocked = true;
                }
            }

            public synchronized void finished (java.util.List arr) {
                notifyAll ();
            }
        }

        R r = new R ();
        pool.addOperationListener (r);

        r.lookup = new org.openide.loaders.FolderLookup (folder).getLookup ();

        Object o = r.lookup.lookup (StringBuffer.class);
        assertNotNull ("StringBuffer found", o);

        org.openide.util.io.NbMarshalledObject mar = new org.openide.util.io.NbMarshalledObject (r.lookup);

        DataObject obj = DataObject.find (file);
        assertEquals ("IDO", InstanceDataObject.class, obj.getClass ());
        java.lang.ref.WeakReference ref = new java.lang.ref.WeakReference (obj);
        obj = null;
        r.lookup = null;
        assertGC ("Make sure the object goes away", ref);

        // this will block Folder Recognizer for a while
        FolderList l = FolderList.find (bb, true);
        org.openide.util.RequestProcessor.Task task = l.computeChildrenList (r);

        r.doWrongThing = true;
        r.lookup = (Lookup)mar.get ();

        // one of the next two lines was causing the deadlock
        o = r.lookup.lookup (StringBuffer.class);
        o = r.lookup.lookup (Runnable.class);

        assertEquals ("Called once", 1, r.cnt);

        if (r.ex != null) {
            throw r.ex;
        }

        pool.removeOperationListener (r);
    }


    public void testDeserializationOnFolder () throws Exception {
        doDeser (true);
    }

    public void testDeserializationOnSubFolder () throws Exception {
        doDeser (false);
    }

    public void testGcWhenHoldingOnlyResult () throws Exception {
        String fsstruct [] = new String [] {
            "AA/BB/A.simple"
        };

        TestUtilHid.destroyLocalFileSystem (getName());
        FileSystem lfs = TestUtilHid.createLocalFileSystem (getWorkDir(), fsstruct);
        Repository.getDefault ().addFileSystem (lfs);

        DataFolder folder = DataFolder.findFolder (lfs.findResource("/AA"));
        DataFolder subfolder = DataFolder.findFolder (lfs.findResource("/AA/BB/"));
        DataObject tmp = InstanceDataObject.create (subfolder, null, Hashtable.class);

        FolderLookup lkp = new FolderLookup (folder);
        Lookup.Result res = lkp.getLookup ().lookup (new Lookup.Template(Hashtable.class));
        java.lang.ref.WeakReference ref2 = new java.lang.ref.WeakReference (lkp);

        lkp = null;
        folder = null;
        subfolder = null;
        tmp = null;
        boolean collected;
        try {
            assertGC("XXX", ref2);
            collected = true;
        } catch (junit.framework.AssertionFailedError x) {
            collected = false;
        }
        assertEquals(res.allInstances().size(), 1);
        if (collected) {
            fail("Lookup got GCed when holding only result..");
        }
    }

    private void doDeser (boolean root) throws Exception {
        String fsstruct [] = new String [] {
            "AA/BB/A.simple"
        };

        TestUtilHid.destroyLocalFileSystem (getName());
        FileSystem lfs = TestUtilHid.createLocalFileSystem (getWorkDir(), fsstruct);
        Repository.getDefault ().addFileSystem (lfs);

        DataFolder folder = DataFolder.findFolder (lfs.findResource("/AA"));
        DataFolder subfolder = DataFolder.findFolder (lfs.findResource("/AA/BB/"));
        DataObject tmp = InstanceDataObject.create (subfolder, null, Hashtable.class);

        FolderLookup lkp = new FolderLookup (folder);
        Object res = lkp.getLookup ().lookup (Hashtable.class);
        assertNotNull ("The table is obtained", res);

        org.openide.util.io.NbMarshalledObject mar = new org.openide.util.io.NbMarshalledObject (lkp.getLookup ());

        java.lang.ref.WeakReference ref1 = new java.lang.ref.WeakReference (subfolder);
        java.lang.ref.WeakReference ref2 = new java.lang.ref.WeakReference (lkp);
        subfolder = null;
        tmp = null;
        folder = null;
        lkp = null;
        assertGC ("Lookup can disappear", ref1);
        assertGC ("Folder can disappear", ref2);


        Lookup lookup = (Lookup)mar.get ();
        ((FolderLookup.ProxyLkp)lookup).waitFinished ();
        res = lookup.lookup (Hashtable.class);
        assertNotNull ("A table is there", res);
        res = lookup.lookup (ArrayList.class);
        assertNull ("No array list", res);

        DataFolder my = DataFolder.findFolder (lfs.findResource(root ? "/AA/" : "/AA/BB/"));
        tmp = InstanceDataObject.create (my, null, ArrayList.class);
        res = lookup.lookup (ArrayList.class);
        assertNotNull ("array list is there", res);

        Repository.getDefault ().removeFileSystem (lfs);
    }





   public static class ALkp extends AbstractLookup {
       public InstanceContent ic;

       public ALkp () {
           this (new InstanceContent ());
       }

       private ALkp (InstanceContent ic) {
           super (ic);
           ic.add (new Integer (1));
           this.ic = ic;
       }

   }

   public static final class GLkp extends ProxyLookup {
       public GLkp() {
           super(new Lookup[] {
               new ALkp(),
               Lookups.metaInfServices(GLkp.class.getClassLoader()),
           });
       }
   }

}
