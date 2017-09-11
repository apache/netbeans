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

package org.openide.loaders;

import java.io.IOException;
import java.util.Enumeration;
import org.netbeans.junit.NbTestCase;
import org.openide.cookies.EditCookie;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileSystem;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.Repository;
import org.openide.util.test.MockLookup;

/** 
 * Ensure that SaveAs functionality works as expected for DataObjects
 *
 * @author S. Aubrecht
 */
public class DataObjectSaveAsTest extends NbTestCase {
    /** sample data object */
    private DataObject obj1;
    /** sample data object */
    private DataObject obj2;
    /** monitor sfs */
    private PCL sfs;

    public DataObjectSaveAsTest (String name) {
        super(name);
    }
    
    /**
     * Sets up the testing environment by creating testing folders
     * on the system file system.
     */
    @Override
    protected void setUp() throws Exception {
        clearWorkDir();
        MockLookup.setInstances(new Repository(TestUtilHid.createLocalFileSystem(getWorkDir(), new String[0])),
                new Pool());
        
        MyDL1 loader1 = MyDL1.getLoader(MyDL1.class);
        MyDL2 loader2 = MyDL2.getLoader(MyDL2.class);

        FileSystem dfs = FileUtil.getConfigRoot().getFileSystem();
        dfs.refresh (true);        
        
        FileObject fo = FileUtil.createData (dfs.getRoot (), "a.ext1");
        obj1 = DataObject.find (fo);
        
        assertEquals ("The correct loader", loader1, obj1.getLoader ());
        
        fo = FileUtil.createData (dfs.getRoot (), "b.ext2");
        obj2 = DataObject.find (fo);
        
        assertEquals ("The correct loader", loader2, obj2.getLoader ());
        
        sfs = new PCL ();
        dfs.addFileChangeListener (sfs);
    }
    
    /**
     * Deletes the folders created in method setUp().
     */
    @Override
    protected void tearDown() throws Exception {
        FileUtil.getConfigRoot().getFileSystem().removeFileChangeListener (sfs);
    }
    
    public void testSaveAsSameExtension() throws IOException {
        FileObject fo = obj1.getPrimaryFile();
        String newName = "newFileName";
        String newExt = "ext1";
        DataFolder folder = DataFolder.findFolder( fo.getParent() );
        DataObject newDob = obj1.copyRename( folder, newName, newExt );
        
        assertNotNull( "New object was created", newDob );
        
        fo = newDob.getPrimaryFile();
        assertEquals( newName, fo.getName() );
        assertEquals( newExt, fo.getExt() );
        
        DataFolder newFolder = DataFolder.findFolder( fo.getParent() );
        assertEquals( "The new object is in the correct folder",
                folder, newFolder);
        
        assertEquals( "The correct loader has been used for renamed object", 
                MyDL1.class, newDob.getLoader().getClass() );
        
        sfs.assertEvent(obj1, 1, "fileDataCreated");
    }
    
    public void testSaveAsNewExtension() throws IOException {
        FileObject fo = obj1.getPrimaryFile();
        String newName = "newFileName";
        String newExt = "ext2";
        DataFolder folder = DataFolder.findFolder( fo.getParent() );
        DataObject newDob = obj1.copyRename( folder, newName, newExt );
        
        assertNotNull( "New object was created", newDob );
        
        fo = newDob.getPrimaryFile();
        assertEquals( newName, fo.getName() );
        assertEquals( newExt, fo.getExt() );
        
        DataFolder newFolder = DataFolder.findFolder( fo.getParent() );
        assertEquals( "The new object is in the correct folder",
                folder, newFolder);
        
        assertEquals( "The correct loader has been used for renamed object", 
                MyDL2.class, newDob.getLoader().getClass() );
        
        sfs.assertEvent(obj1, 1, "fileDataCreated");
    }
    
    public void testSaveAsOverwriteExisting() throws IOException {
        FileObject fo = obj2.getPrimaryFile();
        String newName = fo.getName();
        String newExt = fo.getExt();
        DataFolder folder = DataFolder.findFolder( fo.getParent() );
        
        try {
            obj1.copyRename( folder, newName, newExt );
            fail( "default implementation of copyRename cannot overwrite existing files" );
        } catch( IOException e ) {
            //this is what we want
        }
        
        sfs.assertEvent(obj1, 0, null);
    }
    
    public void testNoSaveAsForMultiFileLoadersByDefault() throws Exception {
        FileUtil.createData(FileUtil.getConfigRoot(), "someFolder/x.prima");
        FileUtil.createData(FileUtil.getConfigRoot(), "someFolder/x.seconda");
        
        DataObject obj = DataObject.find(FileUtil.getConfigFile("someFolder/x.prima"));
        assertEquals(MyMultiFileDataObject.class, obj.getClass());
        
        sfs.clear();
        
        try {
            obj.copyRename( obj.getFolder(), "newName", "newExt" );
            fail( "copyRename is not implemented for MultiFileLoaders" );
        } catch( IOException e ) {
            //this is what we want
        }
        
        sfs.assertEvent(obj1, 0, null);
    }

    public void testSaveAsWorksFineForDefaultDataObjects() throws Exception {
        FileUtil.createData(FileUtil.getConfigRoot(), "someFile.unknownExtension");
        
        DataObject obj = DataObject.find(FileUtil.getConfigFile("someFile.unknownExtension"));
        assertEquals(DefaultDataObject.class, obj.getClass());
        
        //this is ok because save as works for opened editors only so when the dataobject
        //is opened its editor support with saveas impl will be initialized already by then
        assertNotNull( obj.getLookup().lookup(EditCookie.class) );
        assertNotNull( obj.getLookup().lookup(SaveAsCapable.class) );
        
        sfs.clear();
        
        DataObject newDob = obj.copyRename( obj.getFolder(), "newName", "newExt" );
        
        assertNotNull( "object created", newDob );
        assertEquals(DefaultDataObject.class, newDob.getClass());
        
        FileObject fo = newDob.getPrimaryFile();
        assertEquals( "newName", fo.getName() );
        assertEquals( "newExt", fo.getExt() );
        
        sfs.assertEvent(obj1, 1, "fileDataCreated");
    }
    
    /** Loader that does not override the actionsContext.
     */
    private static class MyDL1 extends UniFileLoader {
        public MyDL1 () {
            super("org.openide.loaders.DataObject");
            getExtensions ().addExtension("ext1");
        }
        
        protected MultiDataObject createMultiObject (FileObject primaryFile) throws DataObjectExistsException, IOException {
            return new MyMultiDataObject1(primaryFile, this);
        }
    } // end of MyDL1
    
    private static class MyDL2 extends UniFileLoader {
        public MyDL2 () {
            super("org.openide.loaders.DataObject");
            getExtensions ().addExtension( "ext2" );
        }
        
        protected MultiDataObject createMultiObject (FileObject primaryFile) throws DataObjectExistsException, IOException {
            return new MyMultiDataObject2( primaryFile, this );
        }
    } // end of MyDL2
    
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
    
    private static class MyMultiDataObject1 extends MultiDataObject {
        public MyMultiDataObject1( FileObject primaryFile, MultiFileLoader loader ) throws DataObjectExistsException {
            super( primaryFile, loader );
        }
    }
    
    private static class MyMultiDataObject2 extends MultiDataObject {
        public MyMultiDataObject2( FileObject primaryFile, MultiFileLoader loader ) throws DataObjectExistsException {
            super( primaryFile, loader );
        }
    }
    
    private static class MyMultiFileDataObject extends MultiDataObject {
        public MyMultiFileDataObject( FileObject primaryFile, MultiFileLoader loader ) throws DataObjectExistsException {
            super( primaryFile, loader );
        }
    }
    
    private static final class Pool extends DataLoaderPool {
        
        protected Enumeration<? extends DataLoader> loaders() {
            return org.openide.util.Enumerations.array(
                DataLoader.getLoader(MyDL1.class), 
                DataLoader.getLoader(MyDL2.class),
                DataLoader.getLoader(MyMultiFileLoader.class)
            );
        }
        
    } // end of Pool

    private final class PCL implements org.openide.filesystems.FileChangeListener, java.beans.PropertyChangeListener {
        int cnt;
        String name;

        public void propertyChange (java.beans.PropertyChangeEvent ev) {
            name = ev.getPropertyName();
            cnt++;
        }
        
        public void assertEvent (DataObject obj, int cnt, String name) {
            obj.getLoader ().waitForActions ();

            if (cnt != this.cnt) {
                fail ("Excepted more changes then we got: expected: " + cnt + " we got: " + this.cnt + " with name: " + this.name);
            }
        }

        public void fileAttributeChanged(org.openide.filesystems.FileAttributeEvent fe) {
            cnt++;
            name = "fileAttributeChanged";
        }

        public void fileChanged(org.openide.filesystems.FileEvent fe) {
            cnt++;
            name = "fileChanged";
        }

        public void fileDataCreated(org.openide.filesystems.FileEvent fe) {
            cnt++;
            name = "fileDataCreated";
        }

        public void fileDeleted(org.openide.filesystems.FileEvent fe) {
            cnt++;
            name = "fileDeleted";
        }

        public void fileFolderCreated(org.openide.filesystems.FileEvent fe) {
            cnt++;
            name = "fileFolderCreated";
        }

        public void fileRenamed(org.openide.filesystems.FileRenameEvent fe) {
            cnt++;
            name = "fileRenamed";
        }
        
        void clear() {
            cnt = 0;
            name = null;
        }
    } // end of PCL
}
