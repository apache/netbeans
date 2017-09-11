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
import javax.swing.text.DefaultStyledDocument;
import javax.swing.text.StyledDocument;
import org.netbeans.junit.NbTestCase;
import org.openide.cookies.OpenCookie;
import org.openide.filesystems.FileLock;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataLoader;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectExistsException;
import org.openide.loaders.FileEntry;
import org.openide.loaders.MultiDataObject;
import org.openide.loaders.MultiFileLoader;
import org.openide.loaders.SaveAsCapable;
import org.openide.loaders.UniFileLoader;
import org.openide.nodes.Node.Cookie;
import org.openide.util.test.MockLookup;

public class DataEditorSupportSaveAsTest extends NbTestCase {
    
    static {
        System.setProperty("org.openide.windows.DummyWindowManager.VISIBLE", "false");
    }
    
    public DataEditorSupportSaveAsTest(String s) {
        super(s);
    }
    
    @Override
    protected void setUp() throws Exception {
        clearWorkDir();
        MockLookup.setInstances(new Pool());
    }
    
    public void testUnmodifiedDocumentSaveAs() throws IOException {
        FileUtil.createData(FileUtil.getConfigRoot(), "someFolder/someFile.obj");
        
        DataObject obj = DataObject.find(FileUtil.getConfigFile("someFolder/someFile.obj"));
        assertEquals( MyDataObject.class, obj.getClass());
        assertTrue( "we need UniFileLoader", obj.getLoader() instanceof UniFileLoader );
        
        MyEnv env = new MyEnv( obj );
        MyDataEditorSupport des = new MyDataEditorSupport( obj, env );
        
        FileObject newFolder = FileUtil.createFolder(FileUtil.getConfigRoot(), "otherFolder");
        
        des.saveAs( newFolder, "newFile.newExt" );
        
        DataObject newObj = DataObject.find(FileUtil.getConfigFile("otherFolder/newFile.newExt"));
        assertEquals( MyDataObject.class, newObj.getClass());
        MyDataObject myObj = (MyDataObject)newObj;
        
        assertEquals("the original document was closed", 1, des.closeCounter );
        assertEquals("we don't ask before closing the original document", 0, des.canCloseCounter );
        assertEquals("new document was opened", 1, myObj.openCookieCalls);
    }
    
    public void testModifiedDocumentSaveAs() throws IOException {
        FileUtil.createData(FileUtil.getConfigRoot(), "someFolder/someFile.obj");
        
        DataObject obj = DataObject.find(FileUtil.getConfigFile("someFolder/someFile.obj"));
        assertEquals( MyDataObject.class, obj.getClass());
        assertTrue( "we need UniFileLoader", obj.getLoader() instanceof UniFileLoader );
        
        obj.setModified( true );
        
        MyEnv env = new MyEnv( obj );
        MyDataEditorSupport des = new MyDataEditorSupport( obj, env );
        
        FileObject newFolder = FileUtil.createFolder(FileUtil.getConfigRoot(), "otherFolder");
        
        des.saveAs( newFolder, "newFile.newExt" );
        
        DataObject newObj = DataObject.find(FileUtil.getConfigFile("otherFolder/newFile.newExt"));
        assertEquals( MyDataObject.class, newObj.getClass());
        MyDataObject myObj = (MyDataObject)newObj;
        
        assertEquals("the original StyledDocument was rendered (no file copy)", 1, des.renderCounter);
        assertFalse("the original document is no longer modified", obj.isModified() );
        assertEquals("the original document was closed", 1, des.closeCounter );
        assertEquals("we don't ask before closing the original document", 0, des.canCloseCounter );
        assertTrue("new document was opened", myObj.openCookieCalls > 0);
    }
    
//    public void testEnvAddsSaveAsImpl() throws IOException {
//        FileUtil.createData(FileUtil.getConfigRoot(), "someFolder/someFile.obj");
//        
//        DataObject obj = DataObject.find(FileUtil.getConfigFile("someFolder/someFile.obj"));
//        assertEquals( MyDataObject.class, obj.getClass());
//        assertTrue( "we need UniFileLoader", obj.getLoader() instanceof UniFileLoader );
//        
//        MyEnv env = new MyEnv( obj );
//        MyDataObject myObj = (MyDataObject)obj;
//        
//        assertNotNull("we have SaveAs support for default data objects with uni file loaders", myObj.getSaveAsImpl() );
//    }
//    
//    public void testNoSaveAsImpl() throws IOException {
//        FileUtil.createData(FileUtil.getConfigRoot(), "someFolder/x.prima");
//        FileUtil.createData(FileUtil.getConfigRoot(), "someFolder/x.seconda");
//        
//        DataObject obj = DataObject.find(FileUtil.getConfigFile("someFolder/x.prima"));
//        assertEquals( MyMultiFileDataObject.class, obj.getClass());
//        assertEquals( "we need an object with MultiFileLoader", MyMultiFileLoader.class, obj.getLoader().getClass());
//        
//        MyEnv env = new MyEnv( obj );
//        
//        assertNull("there's no default SaveAs support for multi file loaders", obj.getLookup().lookup( SaveAsCapable.class ) );
//    }
    
    private static class MyDataEditorSupport extends DataEditorSupport {
        private int renderCounter = 0;
        public MyDataEditorSupport( DataObject obj, CloneableEditorSupport.Env env ) {
            super( obj, env );
        }
        
        private int canCloseCounter = 0;
        @Override
        protected boolean canClose() {
            canCloseCounter++;
            return super.canClose();
        }

        private int closeCounter = 0;
        @Override
        protected boolean close(boolean ask) {
            closeCounter++;
            return super.close(ask);
        }

        @Override
        public StyledDocument getDocument() {
            if( getDataObject() instanceof MyDataObject ) {
                return _getDocument();
            }
            return super.getDocument();
        }

        MyStyledDocument _myDocument;
        private StyledDocument _getDocument() {
            if( null == _myDocument ) {
                _myDocument = new MyStyledDocument();
            }
            return _myDocument;
        }
        private class MyStyledDocument extends DefaultStyledDocument {
            @Override
            public void render(Runnable arg0) {
                renderCounter++;
                super.render(arg0);
            }
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
    
    private static final class Pool extends org.openide.loaders.DataLoaderPool {
        protected java.util.Enumeration<? extends DataLoader> loaders() {
            return org.openide.util.Enumerations.array(DataLoader.getLoader(MyLoader.class), 
                    DataLoader.getLoader(MyMultiFileLoader.class));
        }
    }
    
    public static final class MyLoader extends UniFileLoader {
        
        public MyLoader() {
            super(MyDataObject.class.getName ());
            getExtensions ().addExtension ("obj");
            getExtensions ().addExtension ("newExt");
        }
        protected MultiDataObject createMultiObject(FileObject primaryFile) throws DataObjectExistsException, IOException {
            return new MyDataObject(this, primaryFile);
        }
    }
    
    public static final class MyDataObject extends MultiDataObject  {
        private int openCookieCalls = 0;
        
        public MyDataObject(MyLoader l, FileObject folder) throws DataObjectExistsException {
            super(folder, l);
        }

        @Override
        public <T extends Cookie> T getCookie(Class<T> type) {
            if( type.equals( OpenCookie.class) ) {
                OpenCookie oc = new OpenCookie() {
                    public void open() {
                        openCookieCalls++;
                    }
                };
                return type.cast(oc);
            }
            return super.getCookie(type);
        }

        SaveAsCapable getSaveAsImpl() {
            return getCookieSet().getLookup().lookup( SaveAsCapable.class );
        }
    }

    private static class MyMultiFileLoader extends MultiFileLoader {
        public MyMultiFileLoader () {
            super(MyMultiFileDataObject.class.getName());
        }
        
        protected MultiDataObject createMultiObject (FileObject primaryFile) throws DataObjectExistsException, IOException {
            return new MyMultiFileDataObject( primaryFile, this );
        }
    
        protected FileObject findPrimaryFile(FileObject fo) {
            if (!fo.isFolder()) {
                // here is the common code for the worse behaviour
                if (fo.hasExt("prima")) {
                    return FileUtil.findBrother(fo, "seconda") != null ? fo : null;
                }
                
                if (fo.hasExt("seconda")) {
                    return FileUtil.findBrother(fo, "prima");
                }
            }
            return null;
        }

        protected MultiDataObject.Entry createPrimaryEntry(MultiDataObject obj, FileObject primaryFile) {
            return new FileEntry (obj, primaryFile);
        }

        protected MultiDataObject.Entry createSecondaryEntry(MultiDataObject obj, FileObject secondaryFile) {
            return new FileEntry (obj, secondaryFile);
        }
    } // end of MyDL3

    private static class MyMultiFileDataObject extends MultiDataObject {
        public MyMultiFileDataObject( FileObject primaryFile, MultiFileLoader loader ) throws DataObjectExistsException {
            super( primaryFile, loader );
        }
    }
}
