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

import javax.swing.SwingUtilities;
import org.openide.filesystems.*;
import org.openide.loaders.*;
import java.beans.*;
import java.util.logging.Level;
import org.netbeans.junit.*;
import org.openide.nodes.Node;

/*
 * This class tests the basic functionality of standard DataObjects - Instance,
 * Settings, Default, Folder and Shadow.
 * @author js104452
 */
import org.openide.util.Lookup;
public class BasicDataObjectTest extends NbTestCase {

    /** Creates new BasicDataObjectTest */
    public BasicDataObjectTest(String name) {
        super(name);
    }

    // For each test setup a FileSystem and DataObjects
    protected void setUp() throws Exception {
        clearWorkDir ();
        lfs = TestUtilHid.createLocalFileSystem (getWorkDir (), fsstruct);
        Repository.getDefault ().addFileSystem (lfs);
        subDir = lfs.findResource("/Dir/SubDir");
        dir = lfs.findResource("/Dir");
        do1 = DataObject.find(subDir);
        do2 = DataObject.find(dir);        
        df1 = DataFolder.findFolder(subDir);
        df2 = DataFolder.findFolder(dir);
    }
    
    //Clear all stuff when the test finish
    protected void tearDown() throws Exception {
        TestUtilHid.destroyLocalFileSystem (getName());
    }

    
    public void testLookupIsReturnedEvenIfDataObjectIsDeleted() throws Exception {
        CharSequence log = Log.enable("org.openide.loaders", Level.WARNING);

        DataObject obj = DataObject.find (
            FileUtil.createData (subDir, "somedata.txt")
        );
        
        Lookup l = obj.getLookup();
        
        obj.delete();
        
        assertFalse("Does not exist", obj.isValid());
        
        Lookup ln = obj.getLookup();
        
        assertEquals("Lookups are the same type", l.getClass(), ln.getClass());
        
     //   assertEquals("No warnings", "", log.toString());
    }
    
    
    public void testDirectCallToDataObjectContructorIsNotAllowed () throws Exception {
        try {
            LocalFileSystem lfs = new LocalFileSystem ();
            new MultiDataObject (lfs.getRoot(), DataLoaderPool.getFolderLoader ());
            fail ("Constructor succeeded, it should not");
        } catch (IllegalStateException ex) {
            // ok, we expect IllegalStateException
        }
    }
    
    public void testRenameShouldNotMove () throws Exception {
        FileObject fo = dir.createData ("file.txt");
        DataObject obj = DataObject.find (fo);
        try {
            obj.getNodeDelegate ().setName ("SubDir/x.txt");
            fail ("Rename should not be allowed to move: " + obj);
        } catch (IllegalArgumentException ex) {
            // this is what should be thrown
        }
        try {
            obj.getNodeDelegate ().setName ("../x.txt");
            fail ("Move to root should not be allowed to move: " + obj);
        } catch (IllegalArgumentException ex) {
            // this is what should be thrown
        }
    }

    public void testRenameFiresProperly () throws Exception {
        
        class L implements PropertyChangeListener{
            boolean name;
            boolean files;
            boolean dname;
            
            public void propertyChange(PropertyChangeEvent evt) {
                if(evt.getPropertyName().equals(DataObject.PROP_NAME)) name = true;
                if(evt.getPropertyName().equals(DataObject.PROP_FILES)) files = true;
                if(evt.getPropertyName().equals(Node.PROP_DISPLAY_NAME)) dname = true;
            }
        }
        
        FileObject fo = dir.createData ("file.txt");
        DataObject data = DataObject.find(fo);
        L dol = new L();
        L dnl = new L();
        data.addPropertyChangeListener(dol);
        data.getNodeDelegate().addPropertyChangeListener(dnl);

        data.getNodeDelegate ().setName ("file2.txt");
        
        // wait for DataNode firing in AWT
        SwingUtilities.invokeAndWait(new Runnable() { public void run() {} });
        
        assertTrue("DataObject fired PROP_NAME on rename.", dol.name);
        assertTrue("DataObject fired PROP_FILES on rename.", dol.files);
        assertTrue("DataNode fired PROP_NAME on rename.", dnl.name);
        assertTrue("DataNode fired PROP_FILES on rename.", dnl.files);
        // assertTrue("DataNode fired PROP_DISPLAY_NAME on rename.", dnl.dname);
    }

    
    // Test basic functionality of a DataFolder
    public void testBasicDataFolder() throws Exception {        
        
        assertTrue("The DataObject.getFolder() method hasn't returned the same Folder as Folder which was created directly from the FileObject.",do1.equals(df1));

        assertTrue("The DataObject.getClass() method hasn't returned class org.openide.loaders.DataFolder but "+do1.getClass().toString()+".",do1.getClass().toString().equalsIgnoreCase("class org.openide.loaders.DataFolder"));

        assertTrue("The DataObject.getNodeDelegate().getCookie(DataObject.getClass()) method hasn't returned the DataFolder itself.",df1.equals(do1.getNodeDelegate().getCookie(do1.getClass())));
        assertTrue("The DataObject.getNodeDelegate().getCookie(DataObject.getClass()) method hasn't returned the DataObject itself.",do1.equals(do1.getNodeDelegate().getCookie(do1.getClass())));

        assertTrue("The DataObject.getFolder().getPrimaryFile() hasn't returned the same value as the DataObject.getPrimaryFile().getParent() method.",do1.getFolder().getPrimaryFile().equals(do1.getPrimaryFile().getParent()));

        assertTrue("The DataObject.getName() hasn't returned the proper name but "+do1.getName()+".",do1.getName().equalsIgnoreCase("SubDir"));
        
        assertTrue("The DataObject.getLoader() hasn't returned the proper loader class but "+do1.getLoader().getClass().toString()+".",do1.getLoader().getClass().toString().equalsIgnoreCase("class org.openide.loaders.DataLoaderPool$FolderLoader"));
        assertTrue("Two different DataObjects created on folder have not the same loader.",do1.getLoader().equals(do2.getLoader()));
        assertTrue("DataObject created on folder has not the same loader as the DataFolder.",do2.getLoader().equals(df1.getLoader()) || df1.getLoader().equals(df2.getLoader()));

        DataObject[] arr = df1.getChildren();

        assertTrue("The DataFolder.getChildren() hasn't returned proper value.",arr.length == 0);
        
        boolean hlpBool_1 = do1.isValid();
        do1.setValid(!hlpBool_1);
        boolean hlpBool_2 = (do1.isValid() == !hlpBool_1);

        assertTrue("The DataObject.setValid() method has not changed the Valid state.", hlpBool_2);

        hlpBool_2 = (do1.isValid() == df1.isValid());

        assertTrue("The DataObject.setValid() method has not changed the Valid state on a DataFolder object created directly from the Folder.", hlpBool_2);
    }    

    // Test basic functionality of a DataInstance
    public void testBasicDataInstance() throws Exception {            
        FileObject fo1 = do1.getPrimaryFile().createData("file.instance");
        FileObject fo2 = do1.getPrimaryFile().createData("file.settings");
        Object nc = do1.getCookie(DataFolder.class);
        if (nc == null) { 
            fail("FAILED - can't recieave the DataFolder.class Coockie.");
        }
        DataFolder df1 = (DataFolder)nc;
        DataObject[] arr = df1.getChildren();
        
        if (arr.length <= 0 && arr.length > 2) {
            fail("The DataFolder.getChildren() hasn't returned proper value.");
        }

        for (int i=0; i<arr.length; i++) {
            assertTrue("The DataObject.getFolder() hasn't returned the same DataFolder as the existing DataFolder is." ,arr[i].getFolder().equals(df1));
            
            assertTrue("The DataObject.getLoader() hasn't returned the proper loader class but "+arr[i].getLoader().getClass().toString()+".",arr[i].getLoader().getClass().toString().equalsIgnoreCase("class org.openide.loaders.DataLoaderPool$InstanceLoader"));
            
            assertTrue("The DataObject.getPrimaryFile().getParent().getName() hasn't returned the same value as the existing DataFolder.getName()",arr[i].getPrimaryFile().getParent().getName().equalsIgnoreCase(df1.getName()));

            assertTrue("The DataObject.getClass() method hasn't returned class org.openide.loaders.InstanceDataObject but "+arr[i].getClass().toString()+".",arr[i].getClass().toString().equalsIgnoreCase("class org.openide.loaders.InstanceDataObject"));

            assertTrue("The DataObject.getPrimaryFile().getName() hasn't returned the same value as DataObject.getName().",arr[i].getPrimaryFile().getName().equalsIgnoreCase(arr[i].getName()));
        }
    }

    // Test basic functionality of a DataDefault
    public void testBasicDataDefault() throws Exception {            
        FileObject fo = do1.getPrimaryFile().createData("file.default");
        Object nc = do1.getCookie(DataFolder.class);
        if (nc == null) { 
            fail("FAILED - can't recieave the DataFolder.class Coockie.");
        }
        DataFolder df1 = (DataFolder)nc;
        DataObject[] arr = df1.getChildren();
        
        if (arr.length <= 0 && arr.length > 1) {
            fail("The DataFolder.getChildren() hasn't returned proper value.");
        }

        int i = 0;
        
        assertTrue("The DataObject.getFolder() hasn't returned the same DataFolder as the existing DataFolder is." ,arr[i].getFolder().equals(df1));

        assertTrue("The DataObject.getLoader() hasn't returned the proper loader class but "+arr[i].getLoader().getClass().toString()+".",arr[i].getLoader().getClass().toString().equalsIgnoreCase("class org.openide.loaders.DataLoaderPool$DefaultLoader"));

        assertTrue("The DataObject.getPrimaryFile().getParent().getName() hasn't returned the same value as the existing DataFolder.getName()",arr[i].getPrimaryFile().getParent().getName().equalsIgnoreCase(df1.getName()));

        assertTrue("The DataObject.getClass() method hasn't returned class org.openide.loaders.DefaultDataObject but "+arr[i].getClass().toString()+".",arr[i].getClass().toString().equalsIgnoreCase("class org.openide.loaders.DefaultDataObject"));

        assertTrue("The DataObject.getPrimaryFile().getNameExt() hasn't returned the same value as DataObject.getName().",arr[i].getPrimaryFile().getNameExt().equalsIgnoreCase(arr[i].getName()));
    }

    // Test basic functionality of a DataShadow
    public void testBasicDataShadow() throws Exception {        
        FileObject fo = do1.getPrimaryFile().createData("file.shadow");
        Object nc = do1.getCookie(DataFolder.class);
        if (nc == null) { 
            fail("FAILED - can't recieave the DataFolder.class Coockie.");
        }
        DataFolder df1 = (DataFolder)nc;
        DataObject[] arr = df1.getChildren();
        
        if (arr.length <= 0 && arr.length > 1) {
            fail("The DataFolder.getChildren() hasn't returned proper value.");
        }

        int i = 0;
        
        assertTrue("The DataObject.getFolder() hasn't returned the same DataFolder as the existing DataFolder is." ,arr[i].getFolder().equals(df1));

        assertTrue("The DataObject.getLoader() hasn't returned the proper loader class but "+arr[i].getLoader().getClass().toString()+".",arr[i].getLoader().getClass().toString().equalsIgnoreCase("class org.openide.loaders.DataLoaderPool$ShadowLoader"));

        assertTrue("The DataObject.getPrimaryFile().getParent().getName() hasn't returned the same value as the existing DataFolder.getName()",arr[i].getPrimaryFile().getParent().getName().equalsIgnoreCase(df1.getName()));

        assertTrue("The DataObject.getClass() method hasn't returned class org.openide.loaders.BrokenDataShadow but "+arr[i].getClass().toString()+".",arr[i].getClass().toString().equalsIgnoreCase("class org.openide.loaders.BrokenDataShadow"));
        
        assertTrue("The DataObject.getPrimaryFile().getName() hasn't returned the same value as DataObject.getName().",arr[i].getPrimaryFile().getName().equalsIgnoreCase(arr[i].getName()));
    }
    
    
    private String fsstruct [] = new String [] {"Dir/SubDir/"};
    private FileSystem lfs;
    private FileObject subDir;
    private FileObject dir;
    private DataObject do1;
    private DataObject do2;
    private DataFolder df1;
    private DataFolder df2;
    
    public void testPropValidAfeterFileDeletion() throws Exception{
        class PropListener implements PropertyChangeListener{
            PropertyChangeEvent validEvent;
            public void propertyChange(PropertyChangeEvent evt) {
                if(evt.getPropertyName().equals(DataObject.PROP_VALID)){
                validEvent = evt;
                }
            }
            public PropertyChangeEvent getEvent(){
                return validEvent;
            }
        }
        
        FileObject fo = dir.createData("test.test");
        DataObject data = DataObject.find(fo);
        PropListener l = new PropListener();
        data.addPropertyChangeListener(l);
        assertTrue(data.isValid());
        assertNull(l.getEvent());
        fo.delete();
        // now DO becomes invalid
        assertFalse(data.isValid());
        assertNotNull("Data object schould recive DataObject.PROP_VALID event.",l.getEvent());
    }
    
    public void testDeleteAndTryToDeserializeIssue47446 () throws Exception {
        doSerTest (this.do1);
    }
    
    public void testDeleteAndTryToDeserializeOnFolderIssue47446 () throws Exception {
        doSerTest (DataObject.find (
            FileUtil.createData (subDir, "somedata.txt")
        ));
    }
    
    public void testDataObjectIsInItLookup() throws Exception {
        DataObject obj = DataObject.find (
            FileUtil.createData (subDir, "somedata.txt")
        );
        
        DataObject query = obj.getLookup().lookup(DataObject.class);
        assertSame("Object is in its own lookup", obj, query);
    }

    private void doSerTest (DataObject obj) throws Exception {
        org.openide.util.io.NbMarshalledObject mar = new org.openide.util.io.NbMarshalledObject (obj);
        
        assertSame ("If my object exists, deserialization returns the same", obj, mar.get ());
        obj.delete ();
        
        try {
            mar.get ();
            fail ("Deserialization is supposed to fire an exception");
        } catch (java.io.FileNotFoundException ex) {
            // ok
        }
    }
}
