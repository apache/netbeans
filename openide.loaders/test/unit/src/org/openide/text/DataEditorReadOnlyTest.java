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


import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.logging.Level;


import javax.swing.text.Document;
import junit.framework.AssertionFailedError;
import org.netbeans.junit.MockServices;
import org.netbeans.junit.NbTestCase;
import org.netbeans.spi.queries.FileEncodingQueryImplementation;
import org.openide.cookies.CloseCookie;
import org.openide.cookies.EditCookie;

import org.openide.cookies.EditorCookie;
import org.openide.cookies.OpenCookie;
import org.openide.filesystems.FileLock;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataNode;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectExistsException;
import org.openide.loaders.MultiDataObject;
import org.openide.nodes.Children;
import org.openide.nodes.CookieSet;
import org.openide.nodes.Node;


/**
 */
public class DataEditorReadOnlyTest extends NbTestCase {
    // for file object support
    String content = "";
    long expectedSize = -1;
    java.util.Date date = new java.util.Date ();
    boolean readOnly;
    
    FileObject fileObject;
    org.openide.filesystems.FileSystem fs;
    static DataEditorReadOnlyTest RUNNING;
    static {
        System.setProperty("org.openide.windows.DummyWindowManager.VISIBLE", "false");
    }
    private DataObject obj;
    
    public DataEditorReadOnlyTest(String s) {
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
        
        fs = new MyFS();
        org.openide.filesystems.Repository.getDefault ().addFileSystem (fs);
        fileObject = fs.findResource("dir/x.txt");
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

    /** Tests that name is changed if read-only state of FileObject is externally changed (#129178). */
    public void testReadOnly() throws Exception {
        obj = DataObject.find(fileObject);
        DES sup = support();
        assertNotNull("DataObject not found.", obj);
        {
            Document doc = sup.openDocument();
            sup.open();
            waitEQ();
            assertTrue("Not open.", sup.isDocumentLoaded());
            CloneableEditor ed = (CloneableEditor) support().getRef().getAnyComponent();
            assertFalse("Display name should not contain r/o.", ed.getDisplayName().contains("r/o"));
            // simulate external change
            readOnly = true;
            // simulate event normally fired from FileObj.refreshImpl()
            fileObject.setAttribute("DataEditorSupport.read-only.refresh", Boolean.TRUE);
            waitEQ();
            assertTrue("Display name should contain r/o.", ed.getDisplayName().contains("r/o"));
            readOnly = false;
            fileObject.setAttribute("DataEditorSupport.read-only.refresh", Boolean.FALSE);
            waitEQ();
            assertFalse("Display name should not contain r/o.", ed.getDisplayName().contains("r/o"));
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
        
        public synchronized static FileEncodingQueryImpl getDefault () {
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
            getExtensions ().addExtension ("txt");
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

    static class MyFS extends TestFileSystem {

        @Override
        public boolean readOnly(String name) {
            for (StackTraceElement e : new Exception().getStackTrace()) {
                if (e.getMethodName().equals("<init>") && e.getClassName().endsWith("Env")) {
                    throw new IllegalStateException("Don't call into the filesystem now!");
                }
            }
            return RUNNING.readOnly;
        }
        
    }
    
}
