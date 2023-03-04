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
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.logging.Level;
import javax.swing.text.StyledDocument;


import junit.framework.AssertionFailedError;
import org.netbeans.junit.MockServices;
import org.netbeans.junit.NbTestCase;
import org.netbeans.spi.queries.FileEncodingQueryImplementation;
import org.openide.cookies.EditCookie;

import org.openide.cookies.OpenCookie;
import org.openide.filesystems.FileLock;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileStateInvalidException;
import org.openide.filesystems.FileSystem;
import org.openide.loaders.DataNode;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectExistsException;
import org.openide.loaders.MultiDataObject;
import org.openide.nodes.Children;
import org.openide.nodes.CookieSet;
import org.openide.nodes.Node;


/**
 */
public class FileEncodingQueryDataEditorSupportTest extends NbTestCase {
    // for file object support
    String content = "";
    long expectedSize = -1;
    java.util.Date date = new java.util.Date ();
    
    MyFileObject fileObject;
    org.openide.filesystems.FileSystem fs;
    static FileEncodingQueryDataEditorSupportTest RUNNING;
    static {
        System.setProperty("org.openide.windows.DummyWindowManager.VISIBLE", "false");
    }
    
    public FileEncodingQueryDataEditorSupportTest(String s) {
        super(s);
    }

    @Override
    protected Level logLevel() {
        return Level.FINE;
    }
    
    @Override
    protected void setUp () throws Exception {
        MockServices.setServices(Pool.class, FileEncodingQueryImpl.class);
        RUNNING = this;
        
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
    
    public void testFileEncodingQuery () throws Exception {
        DES des = support();
        FileEncodingQueryImpl.getDefault().reset();
        StyledDocument doc = des.openDocument();
        FileEncodingQueryImpl.getDefault().assertFile(
            des.getDataObject().getPrimaryFile()
        );
        FileEncodingQueryImpl.getDefault().reset();
        doc.insertString(doc.getLength(), " Added text.", null);
        des.saveDocument();        
        FileEncodingQueryImpl.getDefault().assertFile(
            des.getDataObject().getPrimaryFile()
        );
        assertEquals(" Added text.", content);
    }
    
    /** File object that let us know what is happening and delegates to certain
     * instance variables of the test.
     */
    private static final class MyFileObject extends org.openide.filesystems.FileObject {
        private org.openide.filesystems.FileObject delegate;
        private int openStreams;
        private Throwable previousStream;
        
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
            return false;
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
    }
    
    private static final class Replace extends Object implements java.io.Serializable {
        static final long serialVersionUID = 2L;
        
        public Object readResolve () {
            return RUNNING.fileObject;
        }
    }

    /** Implementation of the DES */
    private static final class DES extends DataEditorSupport 
    implements OpenCookie, EditCookie {
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
            return super.getDataObject ().getPrimaryFile ().lock ();
        }
        
    }
    
    public static final class FileEncodingQueryImpl extends FileEncodingQueryImplementation {
        
        private static FileEncodingQueryImpl instance;
        
        private FileObject file;
        private Exception who;
        
        public FileEncodingQueryImpl () {
            instance = this;
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
            getCookieSet ().add (OpenCookie.class, this);
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
    
}
