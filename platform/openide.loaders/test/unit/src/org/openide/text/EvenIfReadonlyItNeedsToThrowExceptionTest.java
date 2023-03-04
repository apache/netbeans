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

import java.awt.EventQueue;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Date;
import java.util.Enumeration;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import org.netbeans.junit.NbTestCase;
import org.openide.ErrorManager;
import org.openide.cookies.EditCookie;
import org.openide.cookies.OpenCookie;
import org.openide.filesystems.FileChangeListener;
import org.openide.filesystems.FileLock;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileStateInvalidException;
import org.openide.filesystems.FileSystem;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataLoader;
import org.openide.loaders.DataLoaderPool;
import org.openide.loaders.DataNode;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectExistsException;
import org.openide.loaders.FileEntry;
import org.openide.loaders.MultiDataObject;
import org.openide.loaders.UniFileLoader;
import org.openide.nodes.Children;
import org.openide.nodes.CookieSet;
import org.openide.nodes.Node;
import org.openide.util.Enumerations;
import org.openide.util.test.MockLookup;
import org.openide.windows.CloneableTopComponent;

public class EvenIfReadonlyItNeedsToThrowExceptionTest extends NbTestCase {
    static {
        System.setProperty("org.openide.windows.DummyWindowManager.VISIBLE", "false");
    }

    // for file object support
    String content = "";
    long expectedSize = -1;
    Date date = new Date();
    MyFileObject fileObject;
    
    public EvenIfReadonlyItNeedsToThrowExceptionTest(String s) {
        super(s);
    }
    
    protected @Override void setUp() throws Exception {
        MockLookup.setInstances(new Pool());
        FileObject root = FileUtil.createMemoryFileSystem().getRoot();
        fileObject = new MyFileObject(FileUtil.createData(root, "my.obj"));
    }
    
    protected @Override boolean runInEQ() {
        return false;
    }
    
    private void waitEQ () throws Exception {
        EventQueue.invokeAndWait(new Runnable() {public void run() {}});
    }

    DES support () throws Exception {
        DataObject obj = DataObject.find (fileObject);
        
        assertEquals ("My object was created", MyDataObject.class, obj.getClass ());
        Object cookie = obj.getCookie(OpenCookie.class);
        assertNotNull ("Our object has this cookie", cookie);
        assertEquals ("It is my cookie", DES.class, cookie.getClass ());
        
        return (DES)cookie;
    }

    public void testSaveThrowsException() throws IOException, BadLocationException, Exception {
        fileObject.canWrite = true;
        
        DES des = support();
        des.open();
        waitEQ();
        
        Document doc = des.openDocument();
        
        doc.insertString(0, "Ahoj", null);
        
        assertTrue("Now it is modified", des.isModified());
        
        fileObject.canWrite = false;
        
        try {
            des.saveDocument();
            fail("This has to throw exception");
        } catch (IOException ex) {
            ErrorManager.Annotation[] ann = ErrorManager.getDefault().findAnnotations(ex);
            assertNotNull("There are annotations", ann);
        }
    }
    
    
    /** File object that let us know what is happening and delegates to certain
     * instance variables of the test.
     */
    private static final class MyFileObject extends FileObject {
        private FileObject delegate;
        public boolean canWrite;
        public String content;
        
        public MyFileObject(FileObject del) {
            delegate = del;
        }

        public OutputStream getOutputStream (FileLock lock) throws IOException {
            class ContentStream extends ByteArrayOutputStream {
                public @Override void close () throws IOException {
                    super.close ();
                    content = new String (toByteArray ());
                }
            }

            return new ContentStream ();
        }

        public void delete (FileLock lock) throws IOException {
            delegate.delete (lock);
        }

        @SuppressWarnings("deprecation")
        public void setImportant (boolean b) {
            delegate.setImportant (b);
        }

        public void addFileChangeListener(FileChangeListener fcl) {
            delegate.addFileChangeListener (fcl);
        }

        public void removeFileChangeListener(FileChangeListener fcl) {
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

        public InputStream getInputStream() throws FileNotFoundException {
            return new ByteArrayInputStream(new byte[0]);
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

        public Enumeration<String> getAttributes() {
            return delegate.getAttributes ();
        }

        public FileObject createData (String name, String ext) throws IOException {
            throw new IOException ("Not supported");
        }

        public FileObject getParent () {
            return delegate.getParent ();
        }

        public long getSize () {
            return 0;
        }

        public boolean isData () {
            return true;
        }

        public boolean isFolder () {
            return false;
        }

        @SuppressWarnings("deprecation")
        public boolean isReadOnly () {
            return !canWrite;
        }

        public boolean isRoot () {
            return false;
        }

        public boolean isValid () {
            return delegate.isValid ();
        }

        public Date lastModified() {
            return new Date();
        }

        public FileLock lock () throws IOException {
            return delegate.lock ();
        }
        
        public @Override boolean canWrite() {
            return canWrite;
        }
    }
    
    /** Implementation of the DES */
    private static final class DES extends DataEditorSupport 
    implements OpenCookie, EditCookie {
        public DES (DataObject obj, Env env) {
            super (obj, env);
        }
        
        public CloneableTopComponent.Ref getRef() {
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

    private static final class Pool extends DataLoaderPool {
        protected @Override Enumeration<? extends DataLoader> loaders() {
            return Enumerations.singleton(MyLoader.get());
        }
    }
    
    public static final class MyLoader extends UniFileLoader {
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
        protected @Override MultiDataObject.Entry createPrimaryEntry(MultiDataObject obj, FileObject primaryFile) {
            primary++;
            return new FileEntry(obj, primaryFile);
        }
    }
    public static final class MyDataObject extends MultiDataObject 
    implements CookieSet.Factory {
        public MyDataObject(MyLoader l, FileObject folder) throws DataObjectExistsException {
            super(folder, l);
            getCookieSet ().add (OpenCookie.class, this);
        }

        public <T extends Node.Cookie> T createCookie(Class<T> klass) {
            if (klass.isAssignableFrom(DES.class)) {
                return klass.cast(new DES(this, new MyEnv(this)));
            } else {
                return null;
            }
        }
        
        protected @Override Node createNodeDelegate() {
            return new MyNode(this, Children.LEAF); 
        }
    }

    /* Node which always returns non-null getHtmlDisplayName */
    public static final class MyNode extends DataNode {
        
        public MyNode (DataObject obj, Children ch) {
            super(obj, ch);
        }
        
        public @Override String getHtmlDisplayName() {
            return "<b>" + getDisplayName() + "</b>";
        }
    }
    
}
