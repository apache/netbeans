/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
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
