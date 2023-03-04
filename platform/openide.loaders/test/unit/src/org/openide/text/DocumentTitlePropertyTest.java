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

import java.io.IOException;
import javax.swing.text.Document;
import javax.swing.text.StyledDocument;
import org.netbeans.junit.NbTestCase;
import org.openide.cookies.CloseCookie;
import org.openide.cookies.EditorCookie;
import org.openide.cookies.OpenCookie;
import org.openide.filesystems.FileLock;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileSystem;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataFolder;
import org.openide.loaders.DataLoader;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectExistsException;
import org.openide.loaders.FileEntry;
import org.openide.loaders.MultiDataObject;
import org.openide.loaders.MultiFileLoader;
import org.openide.loaders.UniFileLoader;
import org.openide.nodes.CookieSet;
import org.openide.nodes.Node;
import org.openide.util.test.MockLookup;

/**
 * Test updating document property Document.TitleProperty when dataobject is renamed/moved.
 * It is important because this property is used for error messages in CES.
  */
public class DocumentTitlePropertyTest extends NbTestCase {
    
    FileSystem fs;
    static {
        System.setProperty("org.openide.windows.DummyWindowManager.VISIBLE", "false");
    }
    
    public DocumentTitlePropertyTest(String s) {
        super(s);
    }
    
    @Override
    protected void setUp() throws Exception {
        clearWorkDir();
        MockLookup.setInstances(new Pool());
        fs = org.openide.filesystems.FileUtil.createMemoryFileSystem ();
    }

    /** Test updating document property Document.TitleProperty when dataobject is renamed */
    public void testRename () throws IOException {
        FileUtil.createData(FileUtil.getConfigRoot(), "someFolder/someFile.obj");
        
        DataObject obj = DataObject.find(FileUtil.getConfigFile("someFolder/someFile.obj"));
        assertEquals( MyDataObject.class, obj.getClass());
        assertTrue( "we need UniFileLoader", obj.getLoader() instanceof UniFileLoader );

        EditorCookie ec = obj.getCookie(EditorCookie.class);
        
        StyledDocument doc = ec.openDocument();

        String val = (String) doc.getProperty(Document.TitleProperty);
        assertTrue("Test property value", val.startsWith("someFolder/someFile.obj"));

        obj.rename("newFile");

        val = (String) doc.getProperty(Document.TitleProperty);
        assertTrue("Test property value", val.startsWith("someFolder/newFile.obj"));
    }

    public void testDocumentId () throws IOException {
        FileUtil.createData(FileUtil.getConfigRoot(), "someFolder/someFile.obj");
        FileUtil.createData(FileUtil.getConfigRoot(), "someFolder/someFile.txt");
        
        DataObject obj = DataObject.find(FileUtil.getConfigFile("someFolder/someFile.obj"));
        DataObject txt = DataObject.find(FileUtil.getConfigFile("someFolder/someFile.txt"));
        assertEquals( MyDataObject.class, obj.getClass());
        assertTrue( "we need UniFileLoader", obj.getLoader() instanceof UniFileLoader );

        CloneableEditorSupport ecobj = (CloneableEditorSupport) obj.getCookie(EditorCookie.class);
        CloneableEditorSupport ectxt = (CloneableEditorSupport) txt.getCookie(EditorCookie.class);
        
        if (ecobj.documentID().equals(ectxt.documentID())) {
            fail("The same ID: " + ectxt.documentID());
        }
        assertEquals("Should be full name of the fileObj", obj.getPrimaryFile().getNameExt(), ecobj.documentID());
        assertEquals("Should be full name of the txtObj", txt.getPrimaryFile().getNameExt(), ectxt.documentID());
    }
    
    
    /** Test updating document property Document.TitleProperty when dataobject is moved */
    public void testMove () throws IOException {
        FileUtil.createData(FileUtil.getConfigRoot(), "someFolder/someFile.obj");
        FileUtil.createFolder(FileUtil.getConfigRoot(), "newFolder");

        DataObject obj = DataObject.find(FileUtil.getConfigFile("someFolder/someFile.obj"));
        DataFolder dFolder = (DataFolder) DataObject.find(FileUtil.getConfigFile("newFolder"));

        assertEquals( MyDataObject.class, obj.getClass());
        assertTrue( "we need UniFileLoader", obj.getLoader() instanceof UniFileLoader );

        EditorCookie ec = obj.getCookie(EditorCookie.class);

        StyledDocument doc = ec.openDocument();

        String val = (String) doc.getProperty(Document.TitleProperty);
        assertTrue("Test property value", val.startsWith("someFolder/someFile.obj"));

        obj.move(dFolder);

        val = (String) doc.getProperty(Document.TitleProperty);
        assertTrue("Test property value", val.startsWith("newFolder/someFile.obj"));
    }
    
    private static class MyDataEditorSupport extends DataEditorSupport implements OpenCookie, CloseCookie, EditorCookie {
        public MyDataEditorSupport( DataObject obj, CloneableEditorSupport.Env env ) {
            super( obj, env );
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
    
    public static final class MyDataObject extends MultiDataObject
    implements CookieSet.Factory {
        public MyDataObject(MyLoader l, FileObject folder) throws DataObjectExistsException {
            super(folder, l);
            getCookieSet ().add (new Class[] { OpenCookie.class, CloseCookie.class, EditorCookie.class }, this);
        }

        public <T extends Node.Cookie> T createCookie(Class<T> klass) {
            if (klass.isAssignableFrom(MyDataEditorSupport.class)) {
                return klass.cast(new MyDataEditorSupport(this, new MyEnv(this)));
            } else {
                return null;
            }
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
