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

import java.awt.Dialog;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyVetoException;
import java.beans.VetoableChangeListener;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.logging.Level;
import javax.swing.JEditorPane;
import javax.swing.SwingUtilities;
import javax.swing.text.StyledDocument;
import org.netbeans.junit.MockServices;
import org.netbeans.junit.NbTestCase;
import org.netbeans.junit.RandomlyFails;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.cookies.EditCookie;
import org.openide.cookies.EditorCookie;
import org.openide.cookies.OpenCookie;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileSystem;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.Repository;
import org.openide.nodes.Node;
import org.openide.nodes.NodeAdapter;
import org.openide.nodes.NodeEvent;
import org.openide.nodes.NodeListener;
import org.openide.text.DataEditorSupport;
import org.openide.util.Enumerations;

/**
 *
 * @author Jaroslav Tulach <jtulach@netbeans.org>
 */
public class DefaultDataObjectTest extends NbTestCase {
    static {
        System.setProperty("org.openide.windows.DummyWindowManager.VISIBLE", "false");
    }

    private FileSystem lfs;
    private DataObject obj;
    
    public DefaultDataObjectTest(String testName) {
        super(testName);
    }

    @Override
    protected Level logLevel() {
        return Level.FINE;
    }

    @Override
    protected int timeOut() {
        return 15000;
    }
    
    @Override
    protected void setUp() throws Exception {
        MockServices.setServices(Pool.class);
        
        clearWorkDir();
        JspLoader.cnt = 0;

        String fsstruct [] = new String [] {
            "AA/a.test"
        };
        

        lfs = TestUtilHid.createLocalFileSystem(getWorkDir(), fsstruct);
        Repository.getDefault().addFileSystem(lfs);

        FileObject fo = lfs.findResource("AA/a.test");
        assertNotNull("file not found", fo);
        obj = DataObject.find(fo);

        assertEquals("The right class", obj.getClass(), DefaultDataObject.class);

        assertFalse("Designed to run outside of AWT", SwingUtilities.isEventDispatchThread());

        JspLoader.nodeListener = null;
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
    }

    public void testRenameName() throws Exception {
        Node node = obj.getNodeDelegate();
        
        class L extends NodeAdapter implements Runnable, VetoableChangeListener {
            StyledDocument doc;
            @Override
            public void nodeDestroyed(NodeEvent ev) {
                assertEquals(1, JspLoader.cnt);
                try {
                    DataObject nobj = DataObject.find(obj.getPrimaryFile());
                    assertEquals(JspLoader.class, nobj.getLoader().getClass());
                    EditorCookie ec = nobj.getLookup().lookup(EditorCookie.class);
                    assertNotNull("Cookie found", ec);
                    doc =ec.openDocument();
                } catch (IOException ex) {
                    throw new IllegalStateException(ex);
                }
            }

            public void run() {
                try {
                    obj.rename("x.jsp");
                } catch (IOException ex) {
                    throw new IllegalStateException(ex);
                }
            }

            public void vetoableChange(PropertyChangeEvent evt) throws PropertyVetoException {
            }
        }
        L listener = new L();
//        node.addNodeListener(listener);
        JspLoader.nodeListener = listener;
        obj.addVetoableChangeListener(listener);
        
        SwingUtilities.invokeAndWait(listener);
        assertEquals("One object created", 1, JspLoader.cnt);
        
        DataObject nobj = DataObject.find(obj.getPrimaryFile());
        assertEquals(JspLoader.class, nobj.getLoader().getClass());
        
        assertFalse("Invalidated", obj.isValid());
        
        assertNotNull("Document can be created", listener.doc);
    }

    public void testRenameOpenComponent() throws Exception {
        doRenameOpen(false);
    }

    /** Deadlocked as
Group system
  Group main
    Thread main
        at java.lang.Thread.dumpThreads(Thread.java:-2)
        at java.lang.Thread.getAllStackTraces(Thread.java:1487)
        at org.netbeans.junit.NbTestCase.threadDump(NbTestCase.java:265)
        at org.netbeans.junit.NbTestCase.access$000(NbTestCase.java:95)
        at org.netbeans.junit.NbTestCase$1Guard.waitFinished(NbTestCase.java:332)
        at org.netbeans.junit.NbTestCase.runBare(NbTestCase.java:390)
        at org.netbeans.junit.NbTestCase.run(NbTestCase.java:228)
    Thread Timer-0
        at java.lang.Object.wait(Object.java:-2)
        at java.util.TimerThread.mainLoop(Timer.java:509)
        at java.util.TimerThread.run(Timer.java:462)
    Thread Active Reference Queue Daemon
        at java.lang.Object.wait(Object.java:-2)
        at java.lang.ref.ReferenceQueue.remove(ReferenceQueue.java:118)
        at org.openide.util.lookup.implspi.ActiveQueue$Impl.run(ActiveQueue.java:59)
        at java.lang.Thread.run(Thread.java:619)
    Thread AWT-XAWT
        at sun.awt.X11.XToolkit.waitForEvents(XToolkit.java:-2)
        at sun.awt.X11.XToolkit.run(XToolkit.java:559)
        at sun.awt.X11.XToolkit.run(XToolkit.java:523)
        at java.lang.Thread.run(Thread.java:619)
    Thread AWT-Shutdown
        at java.lang.Object.wait(Object.java:-2)
        at java.lang.Object.wait(Object.java:485)
        at sun.awt.AWTAutoShutdown.run(AWTAutoShutdown.java:265)
        at java.lang.Thread.run(Thread.java:619)
    Thread AWT-EventQueue-0
        at java.lang.Object.wait(Object.java:-2)
        at java.lang.Object.wait(Object.java:485)
        at org.openide.text.CloneableEditor$DoInitialize.initDocument(CloneableEditor.java:660)
        at org.openide.text.CloneableEditor$DoInitialize.initVisual(CloneableEditor.java:698)
        at org.openide.text.CloneableEditor.getEditorPane(CloneableEditor.java:1215)
        at org.openide.text.CloneableEditorSupport.getOpenedPanes(CloneableEditorSupport.java:1111)
        at org.openide.loaders.DefaultDataObjectTest$1R.run(DefaultDataObjectTest.java:238)
        at java.awt.event.InvocationEvent.dispatch(InvocationEvent.java:199)
        at java.awt.EventQueue.dispatchEvent(EventQueue.java:597)
        at java.awt.EventDispatchThread.pumpOneEventForFilters(EventDispatchThread.java:269)
        at java.awt.EventDispatchThread.pumpEventsForFilter(EventDispatchThread.java:184)
        at java.awt.EventDispatchThread.pumpEventsForHierarchy(EventDispatchThread.java:174)
        at java.awt.EventDispatchThread.pumpEvents(EventDispatchThread.java:169)
        at java.awt.EventDispatchThread.pumpEvents(EventDispatchThread.java:161)
        at java.awt.EventDispatchThread.run(EventDispatchThread.java:122)
    Thread Test Watch Dog: testRenameOpenComponentModified
        at java.lang.Object.wait(Object.java:-2)
        at java.lang.Object.wait(Object.java:485)
        at java.awt.EventQueue.invokeAndWait(EventQueue.java:993)
        at javax.swing.SwingUtilities.invokeAndWait(SwingUtilities.java:1320)
        at org.openide.loaders.DefaultDataObjectTest.getEPanes(DefaultDataObjectTest.java:242)
        at org.openide.loaders.DefaultDataObjectTest.doRenameOpen(DefaultDataObjectTest.java:223)
        at org.openide.loaders.DefaultDataObjectTest.testRenameOpenComponentModified(DefaultDataObjectTest.java:183)
        at org.netbeans.junit.NbTestCase.access$200(NbTestCase.java:95)
        at org.netbeans.junit.NbTestCase$2.doSomething(NbTestCase.java:365)
        at org.netbeans.junit.NbTestCase$1Guard.run(NbTestCase.java:294)
        at java.lang.Thread.run(Thread.java:619)
  Thread Reference Handler
        at java.lang.Object.wait(Object.java:-2)
        at java.lang.Object.wait(Object.java:485)
        at java.lang.ref.Reference$ReferenceHandler.run(Reference.java:116)
  Thread Finalizer
        at java.lang.Object.wait(Object.java:-2)
        at java.lang.ref.ReferenceQueue.remove(ReferenceQueue.java:118)
        at java.lang.ref.ReferenceQueue.remove(ReferenceQueue.java:134)
        at java.lang.ref.Finalizer$FinalizerThread.run(Finalizer.java:159)
  Thread Signal Dispatcher
  Thread Java2D Disposer
        at java.lang.Object.wait(Object.java:-2)
        at java.lang.ref.ReferenceQueue.remove(ReferenceQueue.java:118)
        at java.lang.ref.ReferenceQueue.remove(ReferenceQueue.java:134)
        at sun.java2d.Disposer.run(Disposer.java:127)
        at java.lang.Thread.run(Thread.java:619)
  Thread Inactive RequestProcessor thread [Was:NbStatusDisplayer/org.netbeans.core.NbStatusDisplayer$MessageImpl]
        at java.lang.Object.wait(Object.java:-2)
        at org.openide.util.RequestProcessor$Processor.run(RequestProcessor.java:1910)
  Thread TimerQueue
        at java.lang.Object.wait(Object.java:-2)
        at javax.swing.TimerQueue.run(TimerQueue.java:232)
        at java.lang.Thread.run(Thread.java:619)
  Thread Inactive RequestProcessor thread [Was:org.openide.text Editor Initialization/org.openide.text.CloneableEditor$DoInitialize]
        at java.lang.Object.wait(Object.java:-2)
        at org.openide.util.RequestProcessor$Processor.run(RequestProcessor.java:1910)
     */
    @RandomlyFails 
    public void testRenameOpenComponentModified() throws Exception {
        doRenameOpen(true);
    }

    private void doRenameOpen(boolean modify) throws Exception {
        {
            OpenCookie oc = obj.getLookup().lookup(OpenCookie.class);
            assertNotNull("We have open cookie", oc);
            oc.open();
        }

        waitEQ();
        EditorCookie ec = obj.getLookup().lookup(EditorCookie.class);
        JEditorPane[] arr = getEPanes(ec);
        assertNotNull("Editor is open", arr);
        assertEquals("One Editor is open", 1, arr.length);
        assertEquals("Not Modified", false, obj.isModified());
        if (modify) {
            MockServices.setServices(FirstDD.class);
            ec.openDocument().insertString(0, "Ahoj", null);
            assertEquals("Modified now", true, obj.isModified());
        }

        Node[] origNodes = obj.getFolder().getNodeDelegate().getChildren().getNodes(true);
        assertEquals("One node", 1, origNodes.length);
        assertEquals("the obj", obj, origNodes[0].getLookup().lookup(DataObject.class));

        obj.rename("ToSomeStrangeName.jsp");
        assertFalse("Invalid now", obj.isValid());

        DataObject newObj = DataObject.find(obj.getPrimaryFile());
        if (newObj == obj) {
            fail("They should be different now: " + obj + ", " + newObj);
        }

        {
            OpenCookie oc = newObj.getLookup().lookup(OpenCookie.class);
            assertNotNull("We have open cookie", oc);
            oc.open();
        }
        ec = newObj.getLookup().lookup(EditorCookie.class);
        JEditorPane[] arr2 = getEPanes(ec);
        assertNotNull("Editor is open", arr2);
        assertEquals("One Editor is open", 1, arr2.length);

        Node[] newNodes = obj.getFolder().getNodeDelegate().getChildren().getNodes(true);
        assertEquals("One new node", 1, newNodes.length);
        assertEquals("the new obj.\nOld nodes: " + Arrays.toString(origNodes) + "\nNew nodes: " + Arrays.toString(newNodes),
            newObj, newNodes[0].getLookup().lookup(DataObject.class)
        );
    }

    private JEditorPane[] getEPanes(final EditorCookie ec) throws Exception {
        class R implements Runnable {
            JEditorPane[] arr;
            public void run() {
                arr = ec == null ? null : ec.getOpenedPanes();
            }
        }
        R r = new R();
        SwingUtilities.invokeAndWait(r);
        return r.arr;
    }
    
    public void testRenameHtAccess() throws Exception {
        FileObject fo = lfs.getRoot().createData("example.htaccess");
        DataObject mine = DataObject.find(fo);
        assertEquals("It is default DO", DefaultDataObject.class, mine.getClass());
        mine.rename(".htaccess");
        assertEquals("Primary file remains", fo, mine.getPrimaryFile());
        assertEquals(".htaccess", fo.getNameExt());
    }

    /**
     * Test that text files are not recognized as binary files. See bug 228700.
     */
    public void testFixCookieSetRecognizesBinaryFiles() throws IOException {

        byte[] allowedTextBytes = new byte[]{
            '\n', '\r', '\t', '\f'
        };
        for (byte b : allowedTextBytes) {
            assertNotNull("File that contains an allowed byte " + b
                    + " shouldn't be recognized as binary file",
                    createDataObjectWithContent(b)
                    .getLookup().lookup(EditCookie.class));
        }
        for (byte b = 0; b <= 31; b++) {
            if (createDataObjectWithContent(b)
                    .getLookup().lookup(EditCookie.class) != null) {
                boolean isAllowed = false;
                for (int j = 0; j < allowedTextBytes.length; j++) {
                    if (b == allowedTextBytes[j]) {
                        isAllowed = true;
                        break;
                    }
                }
                assertTrue("File containing byte " + b + " should not be "
                        + "recognized as text file.", isAllowed);
            }
        }
    }

    private DataObject createDataObjectWithContent(byte singleByteContent)
            throws IOException {
        FileSystem fs = FileUtil.createMemoryFileSystem();
        FileObject fob = fs.getRoot().createData("test.txt");
        OutputStream os = fob.getOutputStream();
        try {
            os.write(new byte[]{singleByteContent});
        } finally {
            os.close();
        }
        DataObject dob = DataObject.find(fob);
        if (!(dob instanceof DefaultDataObject)) {
            fail("Expected an instance of DefaultDataObject");
        }
        return dob;
    }

    private void waitEQ() throws Exception {
        getEPanes(null);
    }

    public static final class Pool extends DataLoaderPool {

        @Override
        protected Enumeration<? extends DataLoader> loaders() {
            return Enumerations.singleton(JspLoader.getLoader(JspLoader.class));
        }
        
    }

    public static final class JspLoader extends UniFileLoader {
        
        static int cnt; 
        static NodeListener nodeListener;
        
        public JspLoader() {
            super(MultiDataObject.class.getName());
        }

        @Override
        protected void initialize() {
            super.initialize();
            
            getExtensions().addExtension("jsp");
        }
        
        @Override
        protected MultiDataObject createMultiObject(FileObject primaryFile) throws DataObjectExistsException, IOException {
            MultiDataObject obj = new MultiDataObject(primaryFile, this);
            cnt++;
            obj.getCookieSet().assign(EditorCookie.class, DataEditorSupport.create(obj, obj.getPrimaryEntry(), obj.getCookieSet()));

            if (nodeListener != null) {
                nodeListener.nodeDestroyed(null);
            }
            
            return obj;
        }
        
    }

    public static final class FirstDD extends DialogDisplayer {

        @Override
        public Object notify(NotifyDescriptor descriptor) {
            return descriptor.getOptions()[0];
        }

        @Override
        public Dialog createDialog(DialogDescriptor descriptor) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

    }
}
