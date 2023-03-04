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

import java.awt.EventQueue;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyVetoException;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.*;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.netbeans.junit.Log;
import org.netbeans.junit.MockServices;
import org.netbeans.junit.NbTestCase;
import org.netbeans.junit.RandomlyFails;
import org.netbeans.modules.openide.loaders.DataNodeUtils;
import org.netbeans.spi.queries.VisibilityQueryImplementation;
import org.openide.filesystems.*;

import org.openide.loaders.DefaultDataObjectTest.JspLoader;
import org.openide.loaders.MultiDataObject.Entry;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Node;
import org.openide.nodes.Children;
import org.openide.nodes.FilterNode;
import org.openide.nodes.NodeAdapter;
import org.openide.nodes.NodeEvent;
import org.openide.nodes.NodeListener;
import org.openide.nodes.NodeMemberEvent;
import org.openide.nodes.NodeReorderEvent;
import org.openide.util.Enumerations;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;
import org.openide.util.RequestProcessor.Task;
import org.openide.util.test.MockLookup;

public class FolderChildrenTest extends NbTestCase {
    private static Logger LOG;
    public FolderChildrenTest() {
        super("");
    }

    public FolderChildrenTest(java.lang.String testName) {
        super(testName);
    }

    @Override
    protected int timeOut() {
        return 65000;
    }

    @Override
    protected Level logLevel() {
        return Level.FINE;
    }

    protected void assertChildrenType(Children ch) {
        assertEquals("Use lazy children by default", FolderChildren.class, ch.getClass());
    }

    private static void setSystemProp(String key, String value) {
        java.util.Properties prop = System.getProperties();
        if (prop.get(key) != null) return;
        prop.put(key, value);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        clearWorkDir();

        LOG = Logger.getLogger("test." + getName());
        MockServices.setServices(Pool.class);
        Pool.setLoader(null);
        assertEquals("The right pool initialized", Pool.class, DataLoaderPool.getDefault().getClass());
        setSystemProp("netbeans.security.nocheck","true");

        FileObject[] arr = FileUtil.getConfigRoot().getChildren();
        for (int i = 0; i < arr.length; i++) {
            arr[i].delete();
        }
        FormKitDataLoader.waiter = null;
    }

    public void testCorrectLoggerName() throws Exception {
        FileObject fo = FileUtil.getConfigRoot();
        Node n = DataFolder.findFolder(fo).getNodeDelegate();
        Enumeration<String> en = java.util.logging.LogManager.getLogManager().getLoggerNames();
        StringBuilder sb = new StringBuilder();
        boolean ok = false;
        while(en.hasMoreElements()) {
            String log = en.nextElement();
            if (log.startsWith("org.openide.loaders.FolderChildren")) {
                sb.append(log).append("\n");
                if ("org.openide.loaders.FolderChildren".equals(log)) {
                    ok = true;
                }
            }
        }
        assertTrue(sb.toString(), ok);
    }

    @RandomlyFails // NB-Core-Build #2858
    public void testSimulateADeadlockThatWillBeFixedByIssue49459 () throws Exception {
        FileObject a = FileUtil.createData (FileUtil.getConfigRoot (), "XYZ49459/org-openide-loaders-FolderChildrenTest$N1.instance");
        FileObject bb = FileUtil.getConfigFile("XYZ49459");
        assertNotNull (bb);

        class Run implements Runnable {
            private boolean read;
            private DataFolder folder;

            public Node[] children;

            public Run (DataFolder folder) {
                this.folder = folder;
            }

            public void run () {
                if (!read) {
                    read = true;
                    Children.MUTEX.readAccess (this);
                    return;
                }


                // this will deadlock without fix #49459
                children = folder.getNodeDelegate ().getChildren ().getNodes (true);

            }
        }

        Run r = new Run (DataFolder.findFolder (bb));
        Children.MUTEX.writeAccess (r);

        assertNotNull ("Children filled", r.children);
        assertEquals ("But are empty as cannot wait under getNodes", 0, r.children.length);

        // try once more without the locks
        r.children = null;
        r.run ();
        assertNotNull ("But running without mutexs works better - children filled", r.children);
        assertEquals ("One child", 1, r.children.length);
        DataObject obj = r.children[0].getCookie(DataObject.class);
        assertNotNull ("There is data object", obj);
        assertEquals ("It belongs to our file", a, obj.getPrimaryFile ());
    }

    public void testAdditionOfNewFileDoesNotInfluenceAlreadyExistingLoaders ()
    throws Exception {
        FileUtil.createData (FileUtil.getConfigRoot (), "AA/org-openide-loaders-FolderChildrenTest$N1.instance");
        FileUtil.createData (FileUtil.getConfigRoot (), "AA/org-openide-loaders-FolderChildrenTest$N2.instance");

        FileObject bb = FileUtil.getConfigFile("AA");

        DataFolder folder = DataFolder.findFolder (bb);
        Node node = folder.getNodeDelegate();

        Node[] arr = node.getChildren ().getNodes (true);
        assertEquals ("There is a nodes for both", 2, arr.length);
        assertNotNull ("First one is our node", arr[0].getCookie (N1.class));

        FileObject n = bb.createData ("A.txt");
        Node[] newarr = node.getChildren ().getNodes (true);
        assertEquals ("There is new node", 3, newarr.length);

        n.delete ();

        Node[] last = node.getChildren ().getNodes (true);
        assertEquals ("Again they are two", 2, last.length);

        assertEquals ("First one is the same", last[0], arr[0]);
        assertEquals ("Second one is the same", last[1], arr[1]);

    }

    @RandomlyFails // NB-Core-Build #1058 (in FolderChildrenLazyTest)
    public void testChangeableDataFilter() throws Exception {
        String pref = getName() + "/";
        FileUtil.createData (FileUtil.getConfigRoot(), pref + "BB/A.txt");
        FileUtil.createData (FileUtil.getConfigRoot(), pref + "BB/B.txt");
        FileUtil.createData (FileUtil.getConfigRoot(), pref + "BB/AA.txt");
        FileUtil.createData (FileUtil.getConfigRoot(), pref + "BB/BA.txt");


        FileObject bb = FileUtil.getConfigFile(pref + "/BB");

        Filter filter = new Filter();
        DataFolder folder = DataFolder.findFolder (bb);

        Children ch = folder.createNodeChildren( filter );
        Node[] arr = ch.getNodes (true);

        assertNodes( arr, new String[] { "A.txt", "AA.txt" } );
        filter.fire();
        arr = ch.getNodes (true);
        assertNodes( arr, new String[] { "B.txt", "BA.txt" } );
    }

    @RandomlyFails // NB-Core-Build #1049 (in FolderChildrenLazyTest), #1051 (in this)
    public void testChangeableDataFilterOnNodeDelegate() throws Exception {
        String pref = getName() + "/";
        FileUtil.createData (FileUtil.getConfigRoot(), pref + "BB/A.txt");
        FileUtil.createData (FileUtil.getConfigRoot(), pref + "BB/B.txt");
        FileUtil.createData (FileUtil.getConfigRoot(), pref + "BB/AA.txt");
        FileUtil.createData (FileUtil.getConfigRoot(), pref + "BB/BA.txt");


        FileObject bb = FileUtil.getConfigFile(pref + "BB");

        Filter filter = new Filter();
        DataFolder folder = DataFolder.findFolder (bb);


        Node n = folder.getClonedNodeDelegate(filter);
        Children ch = n.getChildren();
        Node[] arr = ch.getNodes (true);

        assertNodes( arr, new String[] { "A.txt", "AA.txt" } );
        filter.fire();
        arr = ch.getNodes (true);
        assertNodes( arr, new String[] { "B.txt", "BA.txt" } );
    }

    @RandomlyFails // Because testChangeableDataFilter() RandomlyFails
    public void testChangeableDataFilterWithPartialRefresh() throws Exception {
        String pref = getName() + "/";
        FileUtil.createData (FileUtil.getConfigRoot(), pref + "BB/0/A.txt");
        FileUtil.createData (FileUtil.getConfigRoot(), pref + "BB/0/B.txt");
        FileUtil.createData (FileUtil.getConfigRoot(), pref + "BB/0/AA.txt");
        FileUtil.createData (FileUtil.getConfigRoot(), pref + "BB/0/BA.txt");
        FileUtil.createData (FileUtil.getConfigRoot(), pref + "BB/1/A.txt");
        FileUtil.createData (FileUtil.getConfigRoot(), pref + "BB/1/B.txt");
        FileUtil.createData (FileUtil.getConfigRoot(), pref + "BB/1/AA.txt");
        FileUtil.createData (FileUtil.getConfigRoot(), pref + "BB/1/BA.txt");

        FileObject bb = FileUtil.getConfigFile(pref + "/BB");

        Filter filter = new Filter();
        DataFolder folder = DataFolder.findFolder (bb);

        Children ch = folder.createNodeChildren( filter );
        doTestChangeableDataFilterWithPartialRefresh(ch, filter, bb);
    }
    
    @RandomlyFails // Because testChangeableDataFilterOnNodeDelegate() RandomlyFails
    public void testChangeableDataFilterOnNodeDelegateWithPartialRefresh() throws Exception {
        String pref = getName() + "/";
        FileUtil.createData (FileUtil.getConfigRoot(), pref + "BB/0/A.txt");
        FileUtil.createData (FileUtil.getConfigRoot(), pref + "BB/0/B.txt");
        FileUtil.createData (FileUtil.getConfigRoot(), pref + "BB/0/AA.txt");
        FileUtil.createData (FileUtil.getConfigRoot(), pref + "BB/0/BA.txt");
        FileUtil.createData (FileUtil.getConfigRoot(), pref + "BB/1/A.txt");
        FileUtil.createData (FileUtil.getConfigRoot(), pref + "BB/1/B.txt");
        FileUtil.createData (FileUtil.getConfigRoot(), pref + "BB/1/AA.txt");
        FileUtil.createData (FileUtil.getConfigRoot(), pref + "BB/1/BA.txt");

        FileObject bb = FileUtil.getConfigFile(pref + "/BB");

        Filter filter = new Filter();
        DataFolder folder = DataFolder.findFolder (bb);

        Node n = folder.getClonedNodeDelegate(filter);
        Children ch = n.getChildren();
        doTestChangeableDataFilterWithPartialRefresh(ch, filter, bb);
    }
    
    @RandomlyFails // Because testChangeableDataFilter() RandomlyFails
    public void testChangeableDataFilterOnFilterNodeWithPartialRefresh() throws Exception {
        String pref = getName() + "/";
        FileUtil.createData (FileUtil.getConfigRoot(), pref + "BB/0/A.txt");
        FileUtil.createData (FileUtil.getConfigRoot(), pref + "BB/0/B.txt");
        FileUtil.createData (FileUtil.getConfigRoot(), pref + "BB/0/AA.txt");
        FileUtil.createData (FileUtil.getConfigRoot(), pref + "BB/0/BA.txt");
        FileUtil.createData (FileUtil.getConfigRoot(), pref + "BB/1/A.txt");
        FileUtil.createData (FileUtil.getConfigRoot(), pref + "BB/1/B.txt");
        FileUtil.createData (FileUtil.getConfigRoot(), pref + "BB/1/AA.txt");
        FileUtil.createData (FileUtil.getConfigRoot(), pref + "BB/1/BA.txt");

        FileObject bb = FileUtil.getConfigFile(pref + "/BB");

        Filter filter = new Filter();
        DataFolder folder = DataFolder.findFolder (bb);

        Node root = new FilterNode (folder.getNodeDelegate (), folder.createNodeChildren (filter));
        Children ch = root.getChildren();
        doTestChangeableDataFilterWithPartialRefresh(ch, filter, bb);
    }
    
    private void doTestChangeableDataFilterWithPartialRefresh(Children ch, Filter filter, FileObject bb) throws Exception {
        Node[] arr = ch.getNodes (true);
        assertTwoFolders(arr, new String[] { "A.txt", "AA.txt" },
                              new String[] { "A.txt", "AA.txt" });
        
        // Test, that all folders change
        filter.fire();
        arr = ch.getNodes (true);
        assertTwoFolders(arr, new String[] { "B.txt", "BA.txt" },
                              new String[] { "B.txt", "BA.txt" });
        
        // Test that just the folder containing a changed file is refreshed:
        FileObject chFo = bb.getFileObject("0/B.txt");
        filter.fire(chFo);
        arr = ch.getNodes (true);
        assertTwoFolders(arr, new String[] { "A.txt", "AA.txt" },
                              new String[] { "B.txt", "BA.txt" });
        
        // Sync all folders
        filter.fire();
        arr = ch.getNodes (true);
        assertTwoFolders(arr, new String[] { "B.txt", "BA.txt" },
                              new String[] { "B.txt", "BA.txt" });
        
        // Test that just the changed folder is refreshed:
        chFo = bb.getFileObject("1");
        filter.fire(chFo);
        arr = ch.getNodes (true);
        assertTwoFolders(arr, new String[] { "B.txt", "BA.txt" },
                              new String[] { "A.txt", "AA.txt" });
        
        // Sync all folders
        filter.fire();
        arr = ch.getNodes (true);
        assertTwoFolders(arr, new String[] { "B.txt", "BA.txt" },
                              new String[] { "B.txt", "BA.txt" });
        
        // Test that just the folder containing a changed data object is refreshed:
        chFo = bb.getFileObject("0/BA.txt");
        DataObject chDo = DataObject.find(chFo);
        filter.fire(chDo);
        arr = ch.getNodes (true);
        assertTwoFolders(arr, new String[] { "A.txt", "AA.txt" },
                              new String[] { "B.txt", "BA.txt" });
        
        // Sync all folders
        filter.fire();
        arr = ch.getNodes (true);
        assertTwoFolders(arr, new String[] { "B.txt", "BA.txt" },
                              new String[] { "B.txt", "BA.txt" });
        
        // Test that just the changed data folder is refreshed:
        chFo = bb.getFileObject("1");
        chDo = DataObject.find(chFo);
        filter.fire(chDo);
        arr = ch.getNodes (true);
        assertTwoFolders(arr, new String[] { "B.txt", "BA.txt" },
                              new String[] { "A.txt", "AA.txt" });
    }
    
    private void assertTwoFolders(Node[] arr, String[] names0, String[] names1) {
        assertNodes (arr, "0", "1" );
        Node[] arr0 = arr[0].getChildren().getNodes(true);
        Node[] arr1 = arr[1].getChildren().getNodes(true);

        assertNodes( arr0, names0 );
        assertNodes( arr1, names1 );
    }

    public void testOrderAttributesAreReflected() throws Exception {
        FileObject root = FileUtil.createFolder(FileUtil.getConfigRoot(), "order");

        for (int i = 0; i < 256; i++) {
            FileUtil.createData(root, "file" + i + ".txt");
        }

        FileObject[] arr = root.getChildren();
        assertEquals(256, arr.length);

        for (int i = 0; i < 256; i++) {
            arr[i].setAttribute("position", i ^ 0x6B);
        }

        DataFolder folder = DataFolder.findFolder (root);
        Node n = folder.getNodeDelegate();
        Children ch = n.getChildren();
        Node[] nodes = ch.getNodes (true);
        assertEquals(256, nodes.length);

        for (int i = 0; i < 256; i++) {
            FileObject fo = nodes[i].getLookup().lookup(FileObject.class);
            assertNotNull(i + " Has file object: " + nodes[i], fo);
            assertEquals(i + " It is the correct one: ", arr[i ^ 0x6B], fo);
        }
    }

    private static Object holder;
    public void testChildrenCanGC () throws Exception {
        Filter filter = new Filter();
        holder = filter;

        String pref = getName() + '/';
        FileObject bb = FileUtil.createFolder(FileUtil.getConfigRoot(), pref + "/BB");
        bb.createData("Ahoj.txt");
        bb.createData("Hi.txt");
        DataFolder folder = DataFolder.findFolder(bb);

        Children ch = folder.createNodeChildren(filter);
        LOG.info("children created: " + ch);
        Node[] arr = ch.getNodes(true);
        LOG.info("nodes obtained" + arr);
        assertEquals("Accepts only Ahoj", 1, arr.length);
        LOG.info("The one node" + arr[0]);

        WeakReference<Children> ref = new WeakReference<Children>(ch);
        ch = null;
        arr = null;

        assertGC("Children can disappear even we hold the filter", ref);
    }

    public void testSeemsLikeTheAbilityToRefreshIsBroken() throws Exception {
        String pref = getName() + '/';
        FileObject bb = FileUtil.createFolder(FileUtil.getConfigRoot(), pref + "/BB");
	bb.createData("Ahoj.txt");
	bb.createData("Hi.txt");

        DataFolder folder = DataFolder.findFolder (bb);

	Node n = folder.getNodeDelegate();
	Node[] arr = n.getChildren().getNodes(true);
	assertEquals("Both are visible", 2, arr.length);

	WeakReference<Node> ref = new WeakReference<Node>(arr[0]);
	arr = null;
	assertGC("Nodes can disappear", ref);


	bb.createData("Third.3rd");

	arr = n.getChildren().getNodes(true);
	assertEquals("All are visbile ", 3, arr.length);
    }

    @RandomlyFails // NB-Core-Build #1868
    public void testReorderAfterRename() throws Exception {
        String pref = getName() + '/';
        FileObject bb = FileUtil.createFolder(FileUtil.getConfigRoot(), pref + "/BB");
        FileObject ahoj = bb.createData("Ahoj.txt");
        bb.createData("Hi.txt");

        DataFolder folder = DataFolder.findFolder (bb);

        Node n = folder.getNodeDelegate();
        Node[] arr = n.getChildren().getNodes(true);
        assertEquals("Both are visible", 2, arr.length);
        assertEquals("Ahoj is 1st", "Ahoj.txt", arr[0].getName());
        assertEquals("Hi is 2nd", "Hi.txt", arr[1].getName());


        DataObject obj = DataObject.find(ahoj);
        obj.rename("xyz.txt");

        arr = n.getChildren().getNodes(true);
        assertEquals("All are visbile ", 2, arr.length);
        assertEquals("Hi is 1st", "Hi.txt", arr[0].getName());
        assertEquals("xyz is 2nd", "xyz.txt", arr[1].getName());
    }


    public static class N1 extends org.openide.nodes.AbstractNode
    implements Node.Cookie {
        public N1 () {
            this (true);
        }

        private N1 (boolean doGc) {
            super (org.openide.nodes.Children.LEAF);

            if (doGc) {
                for (int i = 0; i < 5; i++) {
                    System.gc ();
                }
            }
        }

        @Override
        public Node cloneNode () {
            return new N1 (false);
        }

        @Override
        public <T extends Node.Cookie> T getCookie(Class<T> type) {
            if (type == getClass()) {
                return type.cast(this);
            }
            return null;
        }
    }

    public static final class N2 extends N1 {
    }


    final void assertNodes( Node[] nodes, String... names) {
        Object t = Arrays.asList(nodes);
        assertEquals( "Wrong number of nodes: " + t, names.length, nodes.length );

        for( int i = 0; i < nodes.length; i++ ) {
            assertEquals( "Wrong name at index " + i + ": " + t, names[i], nodes[i].getName() );
        }

    }

    private static class Filter implements ChangeableDataFilter  {

        private boolean selectA = true;

        private final List<ChangeListener> listeners = new CopyOnWriteArrayList<ChangeListener>();

        public boolean acceptDataObject (DataObject obj) {
            FileObject fo = obj.getPrimaryFile();
            if (fo.isFolder()) {
                return true;
            }
            String fileName = fo.getName();
            boolean select = fileName.startsWith( "A" );
            select = selectA ? select : !select;
            return select;
        }

        @Override
        public void addChangeListener( ChangeListener listener ) {
            LOG.log(Level.INFO, "addChangeListener: " + listener, new Throwable());
            listeners.add(listener);
        }

        @Override
        public void removeChangeListener( ChangeListener listener ) {
            LOG.log(Level.INFO, "removeChangeListener: " + listener, new Throwable());
            listeners.remove(listener);
        }
        
        public void fire( ) {

            selectA = !selectA;

            ChangeEvent event = new ChangeEvent(this);
            fireChange(event);
        }

        public void fire(FileObject fo) {

            selectA = !selectA;

            ChangeEvent event = new ChangeEvent(fo);
            fireChange(event);
        }

        public void fire(DataObject dobj) {

            selectA = !selectA;

            ChangeEvent event = new ChangeEvent(dobj);
            fireChange(event);
        }

        private void fireChange(ChangeEvent event) {
            for (ChangeListener listener : listeners) {
                try {
                    listener.stateChanged(event);
                } catch (RuntimeException x) {
                    Exceptions.printStackTrace(x);
                }
            }
        }

    }

    @RandomlyFails // NB-Core-Build #8181
    public void testChildrenListenToFilesystemByABadea () throws Exception {
        doChildrenListenToFilesystem (false);
    }
    @RandomlyFails // NB-Core-Build #6258: FolderChildren doesn't contain /hudson/workdir/jobs/NB-Core-Build/workspace/openide.loaders/build/test/unit/work/o.o.l.F/cltfbab-1/workFolder/C.txt expected:<1> but was:<0>
    public void testChildrenListenToFileByABadea () throws Exception {
        doChildrenListenToFilesystem (true);
    }

    private void doChildrenListenToFilesystem (boolean useFileObject) throws Exception {

        final Object waitObj = new Object();

        class MyFileChangeListener implements FileChangeListener {
            boolean created;

            public void fileFolderCreated(FileEvent fe) {}
            public void fileChanged(FileEvent fe) {}
            public void fileDeleted(FileEvent fe) {}
            public void fileRenamed(FileRenameEvent fe) {}
            public void fileAttributeChanged(FileAttributeEvent fe) {}
            public void fileDataCreated(FileEvent e) {
                synchronized (waitObj) {
                    created = true;
                    waitObj.notify();
                }
            }
        }

        final String FILE_NAME = "C.txt";

        MyFileChangeListener fcl = new MyFileChangeListener();



        FileObject rootFO = FileUtil.toFileObject(getWorkDir());
        final FileObject workDir = FileUtil.createFolder(rootFO, "workFolder");
        final FileObject sibling = FileUtil.createFolder(rootFO, "unimportantSibling");

        workDir.addFileChangeListener(fcl);

        DataFolder workDirDo = DataFolder.findFolder(workDir);
        FolderChildren fc = new FolderChildren(workDirDo);

        // init the FolderChildren
        fc.getNodes();

        File newFile;

        if (useFileObject) {
            FileObject newFo = FileUtil.createData (workDir, FILE_NAME);
            newFile = FileUtil.toFile(newFo);
        } else {
            newFile = new File(FileUtil.toFile(workDir), FILE_NAME);
            new FileOutputStream(newFile).close();
            workDir.refresh();
        }

        synchronized (waitObj) {

            // wait for create notification
            if (!fcl.created) {
                waitObj.wait(1000);
            }

            // wait for FolderChildren to receive and process the create notification
            int cnt = 10;
            while (cnt-- > 0 && fc.getNodes ().length < 1) {
                Thread.sleep(100);
            }

            assertEquals("FolderChildren doesn't contain " + newFile, 1, fc.getNodes().length);
        }
    }

    @RandomlyFails // in FolderChildrenLazyTest in NB-Core-Build #1478
    public void testRenameOpenComponent() throws Exception {
        JspLoader.cnt = 0;
        Pool.setLoader(JspLoader.class);

        String fsstruct [] = new String [] {
            "AA/a.test"
        };

        FileSystem lfs = TestUtilHid.createLocalFileSystem(getWorkDir(), fsstruct);

        FileObject fo = lfs.findResource("AA/a.test");
        assertNotNull("file not found", fo);
        DataObject obj = DataObject.find(fo);

        assertEquals("The right class", obj.getClass(), DefaultDataObject.class);

        Node[] origNodes = obj.getFolder().getNodeDelegate().getChildren().getNodes(true);
        assertEquals("One node", 1, origNodes.length);
        assertEquals("the obj", obj, origNodes[0].getLookup().lookup(DataObject.class));

        obj.rename("ToSomeStrangeName.jsp");
        assertFalse("Invalid now", obj.isValid());

        DataObject newObj = DataObject.find(obj.getPrimaryFile());
        if (newObj == obj) {
            fail("They should be different now: " + obj + ", " + newObj);
        }

        Node[] newNodes = obj.getFolder().getNodeDelegate().getChildren().getNodes(true);
        assertEquals("One new node", 1, newNodes.length);
        assertEquals("the new obj.\nOld nodes: " + Arrays.toString(origNodes) + "\nNew nodes: " + Arrays.toString(newNodes),
            newObj, newNodes[0].getLookup().lookup(DataObject.class)
        );
    }

    public void testRefreshInvalidDO() throws Exception {
        FileObject fo = FileUtil.createData(new File(getWorkDir(), "AA/a.test"));
        assertNotNull("file not found", fo);
        DataObject obj = DataObject.find(fo);

        assertEquals("The right class", obj.getClass(), DefaultDataObject.class);

        Node folderNode = obj.getFolder().getNodeDelegate();

        Node[] origNodes = folderNode.getChildren().getNodes(true);
        assertEquals("One node", 1, origNodes.length);
        assertEquals("the obj", obj, origNodes[0].getLookup().lookup(DataObject.class));

        LOG.info("before setValid");
        obj.setValid(false);
        LOG.info("end of setValid");
        assertFalse("Invalid now", obj.isValid());

        DataObject newObj = DataObject.find(obj.getPrimaryFile());
        assertNotSame(newObj, obj);

        LOG.info("before getNodes: " + Arrays.asList(origNodes));
        Node[] newNodes = folderNode.getChildren().getNodes(true);
        LOG.info("end    getNodes: " + Arrays.asList(newNodes));
        assertEquals("One new node", 1, newNodes.length);
        assertEquals("the new obj", newObj, newNodes[0].getLookup().lookup(DataObject.class));

    }

    public void testCheckType() {
        DataFolder folder = DataFolder.findFolder(FileUtil.createMemoryFileSystem().getRoot());
        Children ch = folder.getNodeDelegate().getChildren();
        assertChildrenType(ch);
    }
    
    public void testDeadlockWithChildrenMutex() throws Exception {
        class R implements Runnable, NodeListener {
            private RequestProcessor RP = new RequestProcessor("testDeadlockWithChildrenMutex");
            private Node node;
            private FileObject folderAA;
            private FileObject fileATXTInFolderAA;
            private DataObject[] arr;
            private DataObject[] newarr;
            private DataFolder folder;
            private Node[] nodes;
            private int changes;
            public void init() throws IOException {
                FileUtil.createData(FileUtil.getConfigRoot(), "AA/org-openide-loaders-FolderChildrenTest$N1.instance");
                FileUtil.createData(FileUtil.getConfigRoot(), "AA/org-openide-loaders-FolderChildrenTest$N2.instance");

                folderAA = FileUtil.getConfigFile("/AA");

                folder = DataFolder.findFolder(folderAA);
                node = folder.getNodeDelegate();
                node.getChildren().getNodes(true);
                node.addNodeListener(this);
            }

            int state;
            public void run() {
                try {
                    switch (state++) {
                        case 0: clean(); return;
                        case 1: createATxt(); return;
                        default: throw new IllegalStateException("state: " + (state - 1));
                    }
                } catch (IOException ex) {
                    throw new IllegalStateException(ex);
                }
            }

            private void createATxt() throws IOException {
                fileATXTInFolderAA = folderAA.createData("A.txt");
            }

            private void clean() throws IOException {
                arr = folder.getChildren();
                assertEquals("There is a obj for both", 2, arr.length);
                // calls createATxt in different thread
                RP.post(this).waitFinished();
                newarr = folder.getChildren();
                assertEquals("There is new node", 3, newarr.length);
                fileATXTInFolderAA.delete ();
            }

            public void finish() {
                Node[] last = node.getChildren ().getNodes (true);
                assertEquals ("Again they are two", 2, last.length);
            }

            private void ch() {
                nodes = node.getChildren().getNodes();
                changes++;
            }

            public void childrenAdded(NodeMemberEvent ev) {
                ch();
            }

            public void childrenRemoved(NodeMemberEvent ev) {
                ch();
            }

            public void childrenReordered(NodeReorderEvent ev) {
                ch();
            }

            public void nodeDestroyed(NodeEvent ev) {
                ch();
            }

            public void propertyChange(PropertyChangeEvent evt) {
                // oK
            }
        }


        R run = new R();
        run.init();
        CharSequence seq = Log.enable(FolderChildren.class.getName(), Level.WARNING);
        Children.MUTEX.readAccess(run);
        if (seq.length() > 0) {
            fail("No warnings please:\n" + seq);
        }
        run.finish();
    }

    @RandomlyFails // NB-Core-Build #985
    public void testCountNumberOfNodesWhenUsingFormLikeLoader() throws Exception {
        FileUtil.createData (FileUtil.getConfigRoot(), "FK/A.java");
        FileUtil.createData (FileUtil.getConfigRoot(), "FK/A.formKit");

        Pool.setLoader(FormKitDataLoader.class);

        FileObject bb = FileUtil.getConfigFile("/FK");

        DataFolder folder = DataFolder.findFolder (bb);
        Task t = DataNodeUtils.reqProcessor().post(new Runnable() {

                         @Override
                         public void run() {
                try {
                    Thread.sleep(10000);
                } catch (InterruptedException ex) {
                    Exceptions.printStackTrace(ex);
                }
                         }
                     });
        Node[] arr = folder.getNodeDelegate().getChildren().getNodes(true);
        
        assertNodes( arr, new String[] { "A" } );
        t.waitFinished();
    }
    
    public void testNodesHaveDataObjectInLookup() throws Exception {
        FileObject fa = FileUtil.createData(FileUtil.getConfigRoot(), "FK/A");
        FileObject fb = FileUtil.createData(FileUtil.getConfigRoot(), "FK/B");
    
        FileObject bb = FileUtil.getConfigFile("/FK");

        DataFolder folder = DataFolder.findFolder(bb);
        final CountDownLatch latch = new CountDownLatch(1);
        FormKitDataLoader.waiter = latch;
        // wake up in 3s
        RequestProcessor.getDefault().post(new Runnable() {
            @Override
            public void run() {
                latch.countDown();
            }
        }, 3000);
        
        Pool.setLoader(FormKitDataLoader.class);
        final Children ch = folder.getNodeDelegate().getChildren();
        int cnt = ch.getNodesCount(true);
        assertEquals("Two children", 2, cnt);

        FileObject af = ch.getNodeAt(0).getLookup().lookup(FileObject.class);
        assertEquals("The right file a", fa, af);
        FileObject bf = ch.getNodeAt(1).getLookup().lookup(FileObject.class);
        assertEquals("The right file b", fb, bf);

        FormKitDataLoader.assertMode = false;
        DataObject a = ch.getNodeAt(0).getLookup().lookup(DataObject.class);
        DataObject b = ch.getNodeAt(1).getLookup().lookup(DataObject.class);

        assertSame("Node is there #0", ch.getNodeAt(0), ch.getNodeAt(0).getLookup().lookup(Node.class));
        assertSame("Node is there #1", ch.getNodeAt(1), ch.getNodeAt(1).getLookup().lookup(Node.class));
        
        latch.countDown();
        
        assertNotNull("Obj A found", a);
        assertNotNull("Obj B found", b);
        
        assertEquals("Right primary File A", fa, a.getPrimaryFile());
        assertEquals("Right primary File B", fb, b.getPrimaryFile());
    }
    public void testFoldersAreNotLeaves() throws Exception {
        FileUtil.createFolder(FileUtil.getConfigRoot(), "FK/A");
        FileUtil.createFolder(FileUtil.getConfigRoot(), "FK/B");
    
        FileObject bb = FileUtil.getConfigFile("/FK");

        DataFolder folder = DataFolder.findFolder(bb);

        Node[] arr = folder.getNodeDelegate().getChildren().getNodes(true);

        assertNodes(arr, new String[]{"A", "B"});
        
        assertFalse("No leaf", arr[0].isLeaf());
        assertFalse("No leaf 2", arr[1].isLeaf());
    }

    public void testRenameHiddenEntry() throws Exception {
        FileObject folder = FileUtil.createFolder(FileUtil.getConfigRoot(), "two");
        List<FileObject> arr = new ArrayList<FileObject>();
        final int FILES = 2;
        for (int i = 0; i < FILES; i++) {
            arr.add(FileUtil.createData(folder, "" + i + ".dat"));
        }
        DataFolder df = DataFolder.findFolder(folder);

        VisQ visq = new VisQ();

        FilterNode fn = new FilterNode(new FilterNode(new AbstractNode(df.createNodeChildren(visq))));
        Node[] one = fn.getChildren().getNodes(true);
        assertEquals("One node", 1, one.length);
        assertEquals("0.dat", one[0].getName());
        
        FileObject first = folder.getFileObject("1.dat");
        assertNotNull("First found", first);
        FileLock lock = first.lock();
        first.rename(lock, "2", "dat");
        lock.releaseLock();
        
        Node[] two = fn.getChildren().getNodes(true);
        assertEquals("Two are now visible", 2, two.length);
    }

    public void testALotOfHiddenEntries() throws Exception {
        FileObject folder = FileUtil.createFolder(FileUtil.getConfigRoot(), "aLotOf");
        List<FileObject> arr = new ArrayList<FileObject>();
        final int FILES = 1000;
        for (int i = 0; i < FILES; i++) {
            arr.add(FileUtil.createData(folder, "" + i + ".dat"));
        }

        DataFolder df = DataFolder.findFolder(folder);

        VisQ visq = new VisQ();

        FilterNode fn = new FilterNode(new FilterNode(new AbstractNode(df.createNodeChildren(visq))));
        class L implements NodeListener {
            int cnt;

            public void childrenAdded(NodeMemberEvent ev) {
                cnt++;
            }

            public void childrenRemoved(NodeMemberEvent ev) {
                cnt++;
            }

            public void childrenReordered(NodeReorderEvent ev) {
                cnt++;
            }

            public void nodeDestroyed(NodeEvent ev) {
                cnt++;
            }

            public void propertyChange(PropertyChangeEvent evt) {
                cnt++;
            }
        }
        L listener = new L();
        fn.addNodeListener(listener);

        List<Node> nodes = new ArrayList<Node>();
        int cnt = fn.getChildren().getNodesCount(true);
        assertEquals("We expect all files", FILES / 2, cnt);
        List<Node> snapshot = fn.getChildren().snapshot();
        assertEquals("Count as expected", cnt, snapshot.size());
        for (int i = 0; i < cnt; i++) {
            nodes.add(snapshot.get(i));
        }
        assertEquals("No events delivered", 0, listener.cnt);
        assertEquals("Size is half cut", FILES / 2, fn.getChildren().getNodesCount(true));
    }
    
    public void testALotOfHiddenEntriesInLazyMode() throws Exception {
        FileObject folder = FileUtil.createFolder(FileUtil.getConfigRoot(), "aLotOf");
        List<FileObject> arr = new ArrayList<FileObject>();
        final int FILES = 1000;
        for (int i = 0; i < FILES; i++) {
            arr.add(FileUtil.createData(folder, "" + i + ".dat"));
        }

        DataFolder df = DataFolder.findFolder(folder);

        VisQ visq = new VisQ();

        FilterNode fn = new FilterNode(new FilterNode(new AbstractNode(df.createNodeChildren(visq))));
        class L implements NodeListener {
            int cnt;

            public void childrenAdded(NodeMemberEvent ev) {
                cnt++;
            }

            public void childrenRemoved(NodeMemberEvent ev) {
                cnt++;
            }

            public void childrenReordered(NodeReorderEvent ev) {
                cnt++;
            }

            public void nodeDestroyed(NodeEvent ev) {
                cnt++;
            }

            public void propertyChange(PropertyChangeEvent evt) {
                cnt++;
            }
        }
        L listener = new L();
        fn.addNodeListener(listener);

        visq.block = new Object();
        int cnt;
        List<Node> nodes;
        synchronized (visq.block) {
            nodes = new ArrayList<Node>();
            cnt = fn.getChildren().getNodesCount(false);
            assertEquals("We expect no files at all", 0, cnt);

            visq.block.wait();
            visq.block.notify();
        }

        assertEquals("Size is half cut", FILES / 2, fn.getChildren().getNodesCount(true));
        assertTrue("Visibility query goes on", visq.success);
        
        List<Node> snapshot = fn.getChildren().snapshot();
        assertEquals("Files filtered", FILES / 2, snapshot.size());
        for (int i = 0; i < cnt; i++) {
            nodes.add(snapshot.get(i));
        }
        assertEquals("No events delivered", 0, listener.cnt);
    }

    /** Tests node keys are not invalidated if only position attribute changes (see #155673). */
    public void testRefreshPreservesNodeKeys() throws Exception {
        MockLookup.setInstances(new Repository(FileUtil.createMemoryFileSystem()));
        FileObject rootFolder = FileUtil.getConfigRoot().createFolder("TestNodeKeys");
        DataFolder rf = DataFolder.findFolder(rootFolder);
        FileObject fo1 = rootFolder.createData("file1.java");
        assertNotNull(fo1);
        Node testNode = rf.getNodeDelegate ();
        testNode.getChildren().getNodes(true);
        Node childNode = testNode.getChildren().getNodeAt(0);
        assertNotNull(childNode);
        Node parent = childNode.getParentNode ();
        assertNotNull("Node " + childNode + " has a parent.", parent);
        // change position and refresh
        fo1.setAttribute("position", 100);
        testNode.getChildren().getNodes(true);
        assertNotNull("Node " + childNode + " has a parent.", childNode.getParentNode());
    }

    /** #175220 - Tests that children keys are not changed when node and underlying
     * data object are garbage collected. It caused collapsing of tree.
     */
    public void testNodeKeysNotChanged() throws Exception {
        LOG.info("testNodeKeysNotChanged starting");
        FileObject rootFolder = FileUtil.createMemoryFileSystem().getRoot();
        FileObject fo1 = rootFolder.createData("file1.java");
        assertNotNull(fo1);
        FileObject fo2 = rootFolder.createData("file2.java");
        DataObject do2 = DataObject.find(fo2);
        assertNotNull(fo2);
        Node folderNode = DataFolder.findFolder(rootFolder).getNodeDelegate();
        LOG.log(Level.INFO, "testNodeKeysNotChanged folderNode: {0}", folderNode);
        final AtomicInteger removedEventCount = new AtomicInteger(0);
        folderNode.addNodeListener(new NodeAdapter() {

            @Override
            public void childrenRemoved(NodeMemberEvent ev) {
                removedEventCount.incrementAndGet();
                LOG.log(Level.INFO, "testNodeKeysNotChanged childrenRemoved: {0}", ev);
            }
        });
        LOG.info("testNodeKeysNotChanged addNodeListener");

        // refresh children
        LOG.info("testNodeKeysNotChanged about to getNodes");
        folderNode.getChildren().getNodes(true);
        Node childNode1 = folderNode.getChildren().getNodeAt(0);
        LOG.log(Level.INFO, "testNodeKeysNotChanged child0{0}", childNode1);
        assertNotNull(childNode1);
        Node childNode2 = folderNode.getChildren().getNodeAt(1);
        LOG.log(Level.INFO, "testNodeKeysNotChanged child1{0}", childNode2);
        assertNotNull(childNode2);

        // GC node 2
        WeakReference<Node> ref = new WeakReference<Node>(childNode2);
        childNode2 = null;
        assertGC("Cannot GC childNode2", ref);
        // GC data object 2
        WeakReference<DataObject> refDO = new WeakReference<DataObject>(do2);
        do2 = null;
        assertGC("Cannot GC do2", refDO);

        // add new data object
        FileObject fo3 = rootFolder.createData("file3.java");
        assertNotNull(fo3);
        LOG.log(Level.INFO, "testNodeKeysNotChanged fo3: {0}", fo3);
        // refresh children
        folderNode.getChildren().getNodes(true);
        LOG.info("after get children");
        Node childNodeX = folderNode.getChildren().getNodeAt(1);
        LOG.log(Level.INFO, "childeNodeX: {0}", childNodeX);
        assertNotSame("Node 2 should not be the same when GC'd before.", childNode2, childNodeX);
        assertEquals("No node should be removed.", 0, removedEventCount.intValue());
        LOG.info("done");
    }

    /**
     * Test for bug 229746 - Slow versioning via favorites via FolderChilden
     * with YAGL.
     *
     * @throws java.io.IOException
     * @throws java.beans.PropertyVetoException
     * @throws java.lang.InterruptedException
     */
    public void testVisQNotCalledUnderMutex() throws IOException,
            PropertyVetoException,
            InterruptedException,
            Throwable {
        FileObject fo = FileUtil.createData(new File(getWorkDir(), "AA/a.test"));
        assertNotNull("file not found", fo);
        DataObject obj = DataObject.find(fo);
        DataFolder aa = obj.getFolder();
        FileBasedFilterOffMutex f = new FileBasedFilterOffMutex();
        f.semaphore = new Semaphore(0);
        Children ch = aa.createNodeChildren(f);
        if (ch instanceof FolderChildren) {
            FolderChildren fch = (FolderChildren) ch;
            // This should lead to VisQ.acceptDataObject().
            fch.stateChanged(new ChangeEvent(new Object()));
            f.semaphore.acquire();
            if (f.exception != null) {
                throw new RuntimeException(f.exception);
            }
        } else {
            fail("FolderChildren instance expected.");
        }
    }

    /**
     * Test for bug 252073 - Can expand no explorer nodes if one directory is
     * very slow
     * @throws java.io.IOException
     */
    public void testSlowFSDoesNotBlockFastFS() throws IOException {

        final Semaphore contentSemaphore = new Semaphore(0);
        final Semaphore orderingSemaphore = new Semaphore(0);

        FileObject fastRoot = FileUtil.createMemoryFileSystem().getRoot();
        FileObject fastContent = fastRoot.createFolder("content");
        fastContent.createData("a.txt");
        fastContent.createData("b.txt");
        fastContent.createData("c.txt");

        /*
         * Slow filesystem is slow because it must wait for the semaphore before
         * it can return children of folder "content".
         */
        FileObject slowRoot = new SlowFileSystem(contentSemaphore).getRoot();
        FileObject slowContent = slowRoot.getFileObject("content");

        final Node fastNode = DataObject.find(fastContent).getNodeDelegate();
        final Node slowNode = DataObject.find(slowContent).getNodeDelegate();

        assertNotNull(fastNode);
        assertNotNull(slowNode);

        // Ensure the fast node can finish before the slow node
        final AtomicInteger resultSlow = new AtomicInteger(0);
        final AtomicInteger resultFast = new AtomicInteger(0);

        Thread t1 = new Thread(new Runnable() {
            @Override
            public void run() {
                orderingSemaphore.release();
                int res = slowNode.getChildren().getNodesCount(true);
                resultSlow.set(res);
            }
        });

        Thread t2 = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    // wait until slow node has been posted
                    orderingSemaphore.acquire(1);
                    int res = fastNode.getChildren().getNodesCount(true);
                    resultFast.set(res);
                    contentSemaphore.release(); // we have overtaken slow node,
                                                // let it continue now
                } catch (InterruptedException ex) {
                    Exceptions.printStackTrace(ex);
                }
                fastNode.getChildren();
            }
        });
        t1.start();
        t2.start();
        try {
            t1.join();
            t2.join();
        } catch (InterruptedException ex) {
            Exceptions.printStackTrace(ex);
        }
        /* If slower thread blocks faster thread, timeout occurs, empty child
         * list is returned from SlowFileSystem#children and the test fails. */
        assertTrue("Slower folder should not block faster folder",
            resultFast.get() == 3 && resultSlow.get() == 3);
    }

    public static final class FileBasedFilterOffMutex implements
            DataFilter.FileBased {

        Semaphore semaphore;
        Throwable exception;

        private boolean acceptObject() {
            try {
                assertFalse("No readAccess", Children.MUTEX.isReadAccess());
                assertFalse("No writeAccess", Children.MUTEX.isWriteAccess());
            } catch (Throwable e) {
                exception = e;
            } finally {
                if (semaphore != null) {
                    semaphore.release();
                }
            }
            return true;
        }

        @Override
        public boolean acceptFileObject(FileObject fo) {
            return acceptObject();
        }

        @Override
        public boolean acceptDataObject(DataObject obj) {
            return acceptObject();
        }
    }

    public static final class VisQ implements VisibilityQueryImplementation, DataFilter.FileBased {
        Object block;
        boolean success;
        
        public boolean isVisible(FileObject file) {
            if (block != null) {
                assertFalse("No readAccess", Children.MUTEX.isReadAccess());
                assertFalse("No writeAccess", Children.MUTEX.isWriteAccess());
                synchronized (block) {
                    try {
                        block.notifyAll();
                        block.wait();
                    } catch (InterruptedException ex) {
                        throw new IllegalStateException(ex);
                    }
                    block = null;
                }
            }
            try {
                int number = Integer.parseInt(file.getName());
                return number % 2 == 0;
            } catch (NumberFormatException numberFormatException) {
                return true;
            } finally {
                success = true;
            }
        }

        public void addChangeListener(ChangeListener l) {
        }

        public void removeChangeListener(ChangeListener l) {
        }

        public boolean acceptDataObject(DataObject obj) {
            return isVisible(obj.getPrimaryFile());
        }

        public boolean acceptFileObject(FileObject obj) {
            return isVisible(obj);
        }

    }


    public static final class Pool extends DataLoaderPool {
        private static Class<? extends DataLoader> loader;

        /**
         * @return the loader
         */
        private static Class<? extends DataLoader> getLoader() {
            return loader;
        }

        /**
         * @param aLoader the loader to set
         */
        static void setLoader(Class<? extends DataLoader> aLoader) {
            FormKitDataLoader.assertMode = false;
            try {
                loader = aLoader;
                ((Pool)getDefault()).fireChangeEvent(new ChangeEvent(getDefault()));
            } finally  {
                FormKitDataLoader.assertMode = true;
            }
        }

        @Override
        protected Enumeration<? extends DataLoader> loaders() {
            Class<? extends DataLoader> l = getLoader();
            return l == null ? Enumerations.<DataLoader>empty() : Enumerations.singleton(DataLoader.getLoader(l));
        }
    }

    public static class FormKitDataLoader extends MultiFileLoader {
        public static final String FORM_EXTENSION = "formKit"; // NOI18N
        private static final String JAVA_EXTENSION = "java"; // NOI18N

        private static final long serialVersionUID = 1L;
        static int cnt;
        static boolean assertMode;
        static volatile CountDownLatch waiter;

        public FormKitDataLoader() {
            super(FormKitDataObject.class.getName());
        }

        @Override
        protected String defaultDisplayName() {
            return NbBundle.getMessage(FormKitDataLoader.class, "LBL_FormKit_loader_name");
        }

        @Override
        protected FileObject findPrimaryFile(FileObject fo)
        {
            if (fo.isFolder()) {
                return null;
            }
            if (assertMode) {
                assertFalse("No AWT thread queries", EventQueue.isDispatchThread());
            }
            cnt++;
            if (waiter != null) {
                try {
                    waiter.await();
                } catch (InterruptedException ex) {
                    Exceptions.printStackTrace(ex);
                }
            }

            String ext = fo.getExt();
            if (ext.equals(FORM_EXTENSION))
            {
                return FileUtil.findBrother(fo, JAVA_EXTENSION);
            }
            if (ext.equals(JAVA_EXTENSION) && FileUtil.findBrother(fo, FORM_EXTENSION) != null)
            {
                return fo;
            }
            return null;
        }

        protected MultiDataObject createMultiObject(FileObject primaryFile) throws DataObjectExistsException, java.io.IOException
        {
            return new FormKitDataObject(FileUtil.findBrother(primaryFile, FORM_EXTENSION),
                    primaryFile,
                    this);
        }

        protected MultiDataObject.Entry createSecondaryEntry(MultiDataObject multiDataObject, FileObject fileObject)
        {
            FileEntry formEntry = new FileEntry(multiDataObject, fileObject);
            return formEntry;
        }

        @Override
        protected Entry createPrimaryEntry(MultiDataObject obj, FileObject primaryFile) {
            return new FileEntry(obj, primaryFile);
        }

        public final class FormKitDataObject extends MultiDataObject {
            FileEntry formEntry;

            public FormKitDataObject(FileObject ffo, FileObject jfo, FormKitDataLoader loader) throws DataObjectExistsException, IOException
            {
                super(jfo, loader);
                formEntry = (FileEntry)registerEntry(ffo);
            }


        }
    }

    /**
     * Slow file system for {@link #testSlowFSDoesNotBlockFastFS()}.
     */
    private static class SlowFileSystem extends AbstractFileSystem
            implements AbstractFileSystem.List, AbstractFileSystem.Info {

        private final Semaphore contentSemaphore;

        /**
         * Semaphore that makes listing of folder "content" slow. The listing
         * is blocked until the semaphore is released.
         *
         * @param contentSemaphore
         */
        @SuppressWarnings("LeakingThisInConstructor")
        public SlowFileSystem(Semaphore contentSemaphore) {
            this.contentSemaphore = contentSemaphore;
            this.info = this;
            this.list = this;
            this.attr = new DefaultAttributes(info, null, list);
        }

        @Override
        public String getDisplayName() {
            return "Slow File System";
        }

        @Override
        public boolean isReadOnly() {
            return true;
        }

        @Override
        public String[] children(String f) {
            if ("".equals(f)) {
                return new String[] {"content"};
            } else if ("content".equals(f)) {
                try {
                    if (contentSemaphore.tryAcquire(1, 10, TimeUnit.SECONDS)) {
                        return new String[] {"a.txt", "b.txt", "c.txt"};
                    } else {
                        return new String[0];
                    }
                } catch (InterruptedException ex) {
                    Exceptions.printStackTrace(ex);
                    return new String[0];
                }
            } else {
                return new String[0];
            }
        }

        @Override
        public Date lastModified(String name) {
            return new Date(123456);
        }

        @Override
        public boolean folder(String name) {
            return "".equals(name) || "content".equals(name);
        }

        @Override
        public boolean readOnly(String name) {
            return true;
        }

        @Override
        public String mimeType(String name) {
            return "text/plain";
        }

        @Override
        public long size(String name) {
            return 0;
        }

        @Override
        public InputStream inputStream(String name) throws FileNotFoundException {
            return new ByteArrayInputStream(new byte[] {});
        }

        @Override
        public OutputStream outputStream(String name) throws IOException {
            throw new UnsupportedOperationException("Not supported.");
        }

        @Override
        public void lock(String name) throws IOException {
            throw new UnsupportedOperationException("Not supported.");
        }

        @Override
        public void unlock(String name) {
            throw new UnsupportedOperationException("Not supported.");
        }

        @Override
        public void markUnimportant(String name) {
            throw new UnsupportedOperationException("Not supported.");
        }
    }
}
