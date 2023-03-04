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


package org.openide.text;


import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import javax.swing.JEditorPane;


import javax.swing.text.Document;
import junit.framework.AssertionFailedError;
import org.netbeans.junit.MockServices;
import org.netbeans.junit.NbTestCase;
import org.netbeans.junit.RandomlyFails;
import org.netbeans.spi.queries.FileEncodingQueryImplementation;
import org.openide.cookies.CloseCookie;
import org.openide.cookies.EditCookie;

import org.openide.cookies.EditorCookie;
import org.openide.cookies.OpenCookie;
import org.openide.filesystems.FileAlreadyLockedException;
import org.openide.filesystems.FileLock;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileStateInvalidException;
import org.openide.filesystems.FileSystem;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.LocalFileSystem;
import org.openide.loaders.DataFolder;
import org.openide.loaders.DataNode;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectExistsException;
import org.openide.loaders.MultiDataObject;
import org.openide.nodes.Children;
import org.openide.nodes.CookieSet;
import org.openide.nodes.Node;
import org.openide.util.Mutex;
import org.openide.util.io.NbMarshalledObject;
import org.openide.util.Lookup;


/**
 */
public class DataEditorSupportTest extends NbTestCase {
    // for file object support
    String content = "";
    long expectedSize = -1;
    java.util.Date date = new java.util.Date ();
    
    MyFileObject fileObject;
    org.openide.filesystems.FileSystem fs;
    static DataEditorSupportTest RUNNING;
    static {
        System.setProperty("org.openide.windows.DummyWindowManager.VISIBLE", "false");
    }
    
    public DataEditorSupportTest(String s) {
        super(s);
    }

    @Override
    protected Level logLevel() {
        return Level.FINE;
    }

    @Override
    protected void setUp () throws Exception {
        MockServices.setServices(Pool.class);
        RUNNING = this;
        DataEditorSupport.TABNAMES_HTML = false;
        
        fs = org.openide.filesystems.FileUtil.createMemoryFileSystem ();
        org.openide.filesystems.Repository.getDefault ().addFileSystem (fs);
        org.openide.filesystems.FileObject root = fs.getRoot ();
        fileObject = new MyFileObject (org.openide.filesystems.FileUtil.createData (root, "my" + getName() + ".obj"));
    }
    
    @Override
    protected void tearDown () throws Exception {
        waitEQ ();
        
        RUNNING = null;
        org.openide.filesystems.Repository.getDefault ().removeFileSystem (fs);
    }
    
    @Override
    protected boolean runInEQ() {
        return false;
    }
    
    private void waitEQ () throws Exception {
        javax.swing.SwingUtilities.invokeAndWait (new Runnable () { public void run () { } });
    }

    DES support () throws Exception {
        DataObject tmpObj = DataObject.find (fileObject);
        
        assertEquals ("My object was created", MyDataObject.class, tmpObj.getClass ());
        Object cookie = tmpObj.getCookie (org.openide.cookies.OpenCookie.class);
        assertNotNull ("Our object has this cookie", cookie);
        assertEquals ("It is my cookie", DES.class, cookie.getClass ());
        
        return (DES)cookie;
    }

    private static void assertLockFree(FileObject fo) throws Exception {
        fo.lock().releaseLock();
    }

    public void testChangeFile() throws Exception {
        obj = DataObject.find (fileObject);
        DES sup = support ();
        assertFalse ("It is closed now", sup.isDocumentLoaded ());

        assertNotNull ("DataObject found", obj);

        {
            Document doc = sup.openDocument ();
            assertTrue ("It is open now", support ().isDocumentLoaded ());

            doc.insertString(0, "Ahoj", null);

            EditorCookie s = (EditorCookie)sup;
            assertNotNull("Modified, so it has cookie", s);
            assertEquals(sup, s);

            s.saveDocument();
            assertLockFree(obj.getPrimaryFile());
            
            s.close();
            
            CloseCookie c = (CloseCookie)sup;
            assertNotNull("Has close", c);
            assertTrue("Close ok", c.close());

            assertLockFree(obj.getPrimaryFile());
        }


        DataFolder target = DataFolder.findFolder(fs.getRoot().createFolder("target"));


        obj.move(target);

        {
            EditorCookie ec = (EditorCookie)sup;
            assertNotNull("Still has EditorCookie", ec);
            Document doc = ec.openDocument ();
            doc.insertString(0, "NewText", null);

            EditorCookie s = (EditorCookie)sup;
            assertNotNull("Modified, so it has cookie", s);

            s.saveDocument();

            assertLockFree(obj.getPrimaryFile());
        }

        
    }

    public void testChangeFileWhileOpen() throws Exception {
        obj = DataObject.find (fileObject);
        DES sup = support ();
        assertFalse ("It is closed now", sup.isDocumentLoaded ());

        assertNotNull ("DataObject found", obj);

        {
            Document doc = sup.openDocument ();
            assertTrue ("It is open now", support ().isDocumentLoaded ());

            doc.insertString(0, "Ahoj", null);

            EditorCookie s = (EditorCookie)sup;
            assertNotNull("Modified, so it has cookie", s);
            assertEquals(sup, s);
        }


        DataFolder target = DataFolder.findFolder(fs.getRoot().createFolder("target"));


        obj.move(target);

        {
            EditorCookie ec = (EditorCookie)sup;
            assertNotNull("Still has EditorCookie", ec);
            Document doc = ec.openDocument ();
            doc.insertString(0, "NewText", null);

            EditorCookie s = (EditorCookie)sup;
            assertNotNull("Modified, so it has cookie", s);

            s.saveDocument();

            assertLockFree(obj.getPrimaryFile());
        }


    }

    /** holds the instance of the object so insane is able to find the reference */
    private DataObject obj;
    public void testItCanBeGCedIssue57565 () throws Exception {
        DES sup = support ();
        assertFalse ("It is closed now", support ().isDocumentLoaded ());
        
        Lookup lkp = sup.getLookup ();
        obj = lkp.lookup(DataObject.class);
        assertNotNull ("DataObject found", obj);
        
        sup.openDocument ();
        assertTrue ("It is open now", support ().isDocumentLoaded ());
        
        assertTrue ("Closed ok", sup.close ());
        
        java.lang.ref.WeakReference refLkp = new java.lang.ref.WeakReference (lkp);
        lkp = null;
    
        java.lang.ref.WeakReference ref = new java.lang.ref.WeakReference (sup);
        sup = null;
        
        assertGC ("Can disappear", ref);
        assertGC ("And its lookup as well", refLkp);
        
        assertLockFree(obj.getPrimaryFile());
    }

    @RandomlyFails // NB-Core-Build #1208
    public void testGetOpenedPanesWorksAfterDeserialization () throws Exception {
        doGetOpenedPanesWorksAfterDeserialization (-1);
    }
    @RandomlyFails // NB-Core-Build #1434
    public void testGetOpenedPanesWorksAfterDeserializationIfTheFileGetsBig () throws Exception {
        doGetOpenedPanesWorksAfterDeserialization (1024 * 1024 * 10);
    }
    
    public void test68015 () throws Exception {
        DES edSupport = support();
        edSupport.open();
        
        waitEQ();
        
        edSupport.desEnv().markModified();
        
        assertTrue(edSupport.messageName().indexOf('*') != -1);
        assertTrue(edSupport.messageHtmlName().indexOf('*') != -1);

        try {
            assertLockFree(fileObject);
            fail("File shall be locked already");
        } catch (FileAlreadyLockedException ex) {
            // OK
        }

    }
    
    private void doGetOpenedPanesWorksAfterDeserialization (int size) throws Exception {
        support().open ();
        
        waitEQ ();

        CloneableEditor ed = (CloneableEditor)support().getRef ().getAnyComponent ();
        
        JEditorPane[] panes = getPanes();
        assertNotNull (panes);
        assertEquals ("One is there", 1, panes.length);
        
        NbMarshalledObject marshall = new NbMarshalledObject (ed);
        ed.close ();
        
        panes = getPanes();
        assertNull ("No panes anymore", panes);

        DataObject oldObj = DataObject.find (fileObject);
        oldObj.setValid (false);
        
        expectedSize = size;
        
        ed = (CloneableEditor)marshall.get ();
        
        DataObject newObj = DataObject.find (fileObject);
        
        if (oldObj == newObj) {
            fail ("Object should not be the same, new one shall be created after marking the old invalid");
        }
        
        panes = getPanes ();
        assertNotNull ("One again", panes);
        assertEquals ("One is there again", 1, panes.length);
    }

    private JEditorPane[] getPanes() throws Exception {
        return Mutex.EVENT.readAccess(new Mutex.ExceptionAction<JEditorPane[]>() {
            public JEditorPane[] run() throws Exception {
                return support().getOpenedPanes ();
            }
        });
    }
    
    public void testEnvOutputStreamTakesLock() throws Exception {
        DataEditorSupport.Env env = (DataEditorSupport.Env)support().desEnv();
        assertNull(env.fileLock);
        OutputStream stream = env.outputStream();
        assertNotNull(stream);
        stream.close();
        assertNotNull(env.fileLock);
        env.fileLock.releaseLock();
    }

    /** Tests that charset of saving document is not removed from cache by
     * concurrent openDocument() call (see #160784). */
    public void testSaveOpenConcurrent() throws Exception {
        obj = DataObject.find(fileObject);
        DES sup = support();
        assertFalse("It is closed now", sup.isDocumentLoaded());
        assertNotNull("DataObject found", obj);

        Document doc = sup.openDocument();
        assertTrue("It is open now", support().isDocumentLoaded());
        doc.insertString(0, "Ahoj", null);
        EditorCookie s = (EditorCookie) sup;
        assertNotNull("Modified, so it has cookie", s);
        assertEquals(sup, s);

        Logger.getLogger(DataEditorSupport.class.getName()).setLevel(Level.FINEST);
        Logger.getLogger(DataEditorSupport.class.getName()).addHandler(new OpeningHandler(sup));
        s.saveDocument();
        
        assertLockFree(obj.getPrimaryFile());
        s.close();
        CloseCookie c = (CloseCookie) sup;
        assertNotNull("Has close", c);
        assertTrue("Close ok", c.close());
        assertLockFree(obj.getPrimaryFile());
    }

    class OpeningHandler extends Handler {

        private DES des;

        public OpeningHandler(DES des) {
            super();
            this.des = des;
        }

        public synchronized void publish(LogRecord rec) {
            if ("SaveImpl - charset put".equals(rec.getMessage())) {
                Thread openingThread = new Thread(new Runnable() {

                    public void run() {
                        try {
                            des.openDocument();
                        } catch (IOException ex) {
                            fail(ex.getMessage());
                        }
                    }
                }, "Opening");
                openingThread.start();
                try {
                    wait();
                } catch (InterruptedException ex) {
                    fail(ex.getMessage());
                }
            }
            if ("openDocument - charset removed".equals(rec.getMessage())) {
                notify();
            }
        }

        @Override
        public void flush() {
        }

        @Override
        public void close() {
        }
    }

    /** File object that let us know what is happening and delegates to certain
     * instance variables of the test.
     */
    private static final class MyFileObject extends org.openide.filesystems.FileObject {
        private org.openide.filesystems.FileObject delegate;
        private int openStreams;
        private Throwable previousStream;
        private boolean readOnly = false;
        
        public MyFileObject (org.openide.filesystems.FileObject del) {
            delegate = del;
        }

        public java.io.OutputStream getOutputStream (FileLock lock) throws IOException {
            if (openStreams != 0) {
                IOException e = new IOException("There is stream already, cannot write down!");
                if (previousStream != null) {
                    e.initCause(previousStream);
                }
                throw e;
            }
            class ContentStream extends java.io.ByteArrayOutputStream {
                public ContentStream() {
                    openStreams = -1;
                }
                @Override
                public void close () throws java.io.IOException {
                    if (openStreams != -1) {
                        IOException ex = new IOException("One output stream");
                        ex.initCause(previousStream);
                        throw ex;
                    }
                    //assertEquals("One output stream", -1, openStreams);
                    openStreams = 0;
                    previousStream = new Exception("Closed");
                    super.close ();
                    RUNNING.content = new String (toByteArray ());
                }
            }
            previousStream = new Exception("Output");
            return new ContentStream ();
        }

        public void delete (FileLock lock) throws IOException {
            delegate.delete (lock);
        }

        public void setImportant (boolean b) {
            delegate.setImportant (b);
        }

        public void addFileChangeListener (org.openide.filesystems.FileChangeListener fcl) {
            delegate.addFileChangeListener (fcl);
        }

        public void removeFileChangeListener (org.openide.filesystems.FileChangeListener fcl) {
            delegate.removeFileChangeListener (fcl);
        }

        public Object getAttribute (String attrName) {
            return delegate.getAttribute (attrName);
        }

        public FileObject createFolder (String name) throws IOException {
            throw new IOException ("Not supported");
        }

        public void rename (FileLock lock, String name, String ext) throws IOException {
            throw new IOException ("Not supported");
        }

        public void setAttribute (String attrName, Object value) throws IOException {
            delegate.setAttribute (attrName, value);
        }

        public String getName () {
            return delegate.getName ();
        }

        public java.io.InputStream getInputStream () throws java.io.FileNotFoundException {
            if (openStreams < 0) {
                FileNotFoundException e = new FileNotFoundException("Already exists output stream");
                if (previousStream != null) {
                    e.initCause(previousStream);
                }
                throw e;
            }
            
            class IS extends ByteArrayInputStream {
                public IS(byte[] arr) {
                    super(arr);
                    openStreams++;
                }

                @Override
                public void close() throws IOException {
                    openStreams--;
                    super.close();
                }
            }
            previousStream = new Exception("Input");
            
            return new IS(RUNNING.content.getBytes ());
        }

        public FileSystem getFileSystem () throws FileStateInvalidException {
            return delegate.getFileSystem ();
        }

        public FileObject getFileObject (String name, String ext) {
            return null;
        }

        public String getExt () {
            return delegate.getExt ();
        }

        public FileObject[] getChildren () {
            return null;
        }

        public java.util.Enumeration getAttributes () {
            return delegate.getAttributes ();
        }

        public FileObject createData (String name, String ext) throws IOException {
            throw new IOException ("Not supported");
        }

        public FileObject getParent () {
            return delegate.getParent ();
        }

        public long getSize () {
            return RUNNING.expectedSize;
        }

        public boolean isData () {
            return true;
        }

        public boolean isFolder () {
            return false;
        }

        public boolean isReadOnly () {
            return readOnly;
        }

        public boolean isRoot () {
            return false;
        }

        public boolean isValid () {
            return delegate.isValid ();
        }

        public java.util.Date lastModified () {
            return RUNNING.date;
        }

        public FileLock lock () throws IOException {
            return delegate.lock ();
        }
        
        public Object writeReplace () {
            return new Replace ();
        }

        private void setReadOnly(boolean readOnly) {
            this.readOnly = readOnly;
        }
    }
    
    private static final class Replace extends Object implements java.io.Serializable {
        static final long serialVersionUID = 2L;
        
        public Object readResolve () {
            return RUNNING.fileObject;
        }
    }

    /** Implementation of the DES */
    private static final class DES extends DataEditorSupport 
    implements OpenCookie, CloseCookie, EditCookie, EditorCookie {
        public DES (DataObject obj, Env env) {
            super (obj, env);
        }
        
        public org.openide.windows.CloneableTopComponent.Ref getRef () {
            return allEditors;
        }
        
    }
    
    /** MyEnv that uses DataEditorSupport.Env */
    private static final class MyEnv extends DataEditorSupport.Env {
        static final long serialVersionUID = 1L;
        
        public MyEnv (DataObject obj) {
            super (obj);
        }
        
        protected FileObject getFile () {
            return super.getDataObject ().getPrimaryFile ();
        }

        protected FileLock takeLock () throws IOException {
            if (getDataObject() instanceof MultiDataObject) {
                return ((MultiDataObject)getDataObject()).getPrimaryEntry().takeLock();
            } else {
                return super.getDataObject ().getPrimaryFile ().lock ();
            }
        }
        
    }
    
    private static final class FileEncodingQueryImpl extends FileEncodingQueryImplementation {
        
        private static FileEncodingQueryImpl instance;
        
        private FileObject file;
        private Exception who;
        
        private FileEncodingQueryImpl () {
            
        }
            
        public Charset getEncoding(FileObject file) {
            InputStream is  = null;
            try {
                this.file = file;
                this.who = new Exception("Assigned from here");
                byte[] arr = new byte[4096];
                is = file.getInputStream();
                is.read(arr);
            } catch (IOException ex) {
                throw new IllegalStateException(ex);
            } finally {
                if (is != null) {
                    try {
                        is.close();
                    } catch (IOException ex) {
                        throw new IllegalStateException(ex);
                    }
                }
            }
            return Charset.defaultCharset();
        }
        
        public void reset () {
            this.file = null;
            this.who = new Exception("Cleaned from here");
        }
        
        public FileObject getFile () {
            return this.file;
        }
        
        public static synchronized FileEncodingQueryImpl getDefault () {
            if (instance == null) {
                instance = new FileEncodingQueryImpl ();
            }
            return instance;
        }

        private void assertFile(FileObject primaryFile) {
            if (!primaryFile.equals(file)) {
                AssertionFailedError afe = new AssertionFailedError("Files shall be the same:\nExpected:" + primaryFile + "\nReal    :" + file);
                afe.initCause(who);
                throw afe;
            }
        }
    }
    
    public static final class Pool extends org.openide.loaders.DataLoaderPool {
        protected java.util.Enumeration loaders () {
            return org.openide.util.Enumerations.singleton(MyLoader.get ());
        }
    }
    
    public static final class MyLoader extends org.openide.loaders.UniFileLoader {
        public int primary;
        
        public static MyLoader get () {
            return MyLoader.findObject(MyLoader.class, true);
        }
        
        public MyLoader() {
            super(MyDataObject.class.getName ());
            getExtensions ().addExtension ("obj");
        }
        protected String displayName() {
            return "MyPart";
        }
        protected MultiDataObject createMultiObject(FileObject primaryFile) throws DataObjectExistsException, IOException {
            return new MyDataObject(this, primaryFile);
        }
        @Override
        protected MultiDataObject.Entry createPrimaryEntry(MultiDataObject obj, FileObject primaryFile) {
            primary++;
            return new org.openide.loaders.FileEntry (obj, primaryFile);
        }
    }
    public static final class MyDataObject extends MultiDataObject 
    implements CookieSet.Factory {
        public MyDataObject(MyLoader l, FileObject folder) throws DataObjectExistsException {
            super(folder, l);
            getCookieSet ().add (new Class[] { OpenCookie.class, CloseCookie.class, EditorCookie.class }, this);
        }

        public org.openide.nodes.Node.Cookie createCookie (Class klass) {
            return new DES (this, new MyEnv (this)); 
        }
        
        @Override
        protected Node createNodeDelegate() {
            return new MyNode(this, Children.LEAF); 
        }
    }

    /* Node which always returns non-null getHtmlDisplayName */
    public static final class MyNode extends DataNode {
        
        public MyNode (DataObject obj, Children ch) {
            super(obj, ch);
        }
        
        @Override
        public String getHtmlDisplayName() {
            return "<b>" + getDisplayName() + "</b>";
        }
    }

    public void testAnnotateName() throws Exception {
        assertEquals("foo", DataEditorSupport.annotateName("foo", false, false, false));
        assertEquals("foo *", DataEditorSupport.annotateName("foo", false, true, false));
        assertEquals("foo [r/o]", DataEditorSupport.annotateName("foo", false, false, true));
        assertEquals("foo [r/o] *", DataEditorSupport.annotateName("foo", false, true, true));
        assertEquals("<html>foo", DataEditorSupport.annotateName("foo", true, false, false));
        assertEquals("<html>foo *", DataEditorSupport.annotateName("foo", true, true, false));
        assertEquals("<html>foo [r/o]", DataEditorSupport.annotateName("foo", true, false, true));
        assertEquals("<html>foo [r/o] *", DataEditorSupport.annotateName("foo", true, true, true));
        assertEquals("<html>foo", DataEditorSupport.annotateName("<html>foo", true, false, false));
        assertEquals("<html>foo *", DataEditorSupport.annotateName("<html>foo", true, true, false));
        assertEquals("<html>foo [r/o]", DataEditorSupport.annotateName("<html>foo", true, false, true));
        assertEquals("<html>foo [r/o] *", DataEditorSupport.annotateName("<html>foo", true, true, true));
        DataEditorSupport.TABNAMES_HTML = true;
        assertEquals("foo", DataEditorSupport.annotateName("foo", false, false, false));
        assertEquals("foo *", DataEditorSupport.annotateName("foo", false, true, false));
        assertEquals("foo [r/o]", DataEditorSupport.annotateName("foo", false, false, true));
        assertEquals("foo [r/o] *", DataEditorSupport.annotateName("foo", false, true, true));
        assertEquals("<html>foo", DataEditorSupport.annotateName("foo", true, false, false));
        assertEquals("<html><b>foo</b>", DataEditorSupport.annotateName("foo", true, true, false));
        assertEquals("<html><i>foo</i>", DataEditorSupport.annotateName("foo", true, false, true));
        assertEquals("<html><i><b>foo</b></i>", DataEditorSupport.annotateName("foo", true, true, true));
        assertEquals("<html>foo", DataEditorSupport.annotateName("<html>foo", true, false, false));
        assertEquals("<html><b>foo</b>", DataEditorSupport.annotateName("<html>foo", true, true, false));
        assertEquals("<html><i>foo</i>", DataEditorSupport.annotateName("<html>foo", true, false, true));
        assertEquals("<html><i><b>foo</b></i>", DataEditorSupport.annotateName("<html>foo", true, true, true));
        try {
            DataEditorSupport.annotateName(null, true, false, false);
            fail();
        } catch (NullPointerException x) {/*expected*/}
    }

    public void testToolTip() throws Exception {
        clearWorkDir();
        LocalFileSystem lfs = new LocalFileSystem();
        lfs.setRootDirectory(getWorkDir());
        FileObject fo = lfs.getRoot().createData("foo.txt");
        assertNotNull(fo);
        File f = FileUtil.toFile(fo);
        assertNotNull(f);
        String path = f.getAbsolutePath();
        assertEquals(path, DataEditorSupport.toolTip(fo, false, false));
        assertEquals(path, DataEditorSupport.toolTip(fo, true, false));
        assertEquals(path, DataEditorSupport.toolTip(fo, false, true));
        assertEquals(path, DataEditorSupport.toolTip(fo, true, true));
        DataEditorSupport.TABNAMES_HTML = true;
        assertEquals(path, DataEditorSupport.toolTip(fo, false, false));
        assertEquals(path + " (modified)", DataEditorSupport.toolTip(fo, true, false));
        assertEquals(path + " (read-only)", DataEditorSupport.toolTip(fo, false, true));
        assertEquals(path + " (modified) (read-only)", DataEditorSupport.toolTip(fo, true, true));
    }
    
}
