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

package org.netbeans.modules.editor.mimelookup.impl;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.junit.NbTestCase;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

/**
 *
 * @author vita
 */
public class CompoundFolderChildrenTest extends NbTestCase {

    /** Creates a new instance of FolderChildrenTest */
    public CompoundFolderChildrenTest(String name) {
        super(name);
    }

    @Override
    protected void setUp() throws java.lang.Exception {
        clearWorkDir();
        // Set up the default lookup, repository, etc.
        EditorTestLookup.setLookup(
            new String[] {
                "Tmp/"
            },
            getWorkDir(), new Object[] {},
            getClass().getClassLoader(), 
            null
        );
        Logger.getLogger("org.openide.filesystems.Ordering").setLevel(Level.OFF);
    }

    // test collecting files on different layers

    public void testCollecting() throws Exception {
        String fileName1 = "file-on-layer-1.instance";
        String fileName2 = "file-on-layer-2.instance";
        TestUtilities.createFile(getWorkDir(), "Tmp/A/B/C/D/" + fileName1);
        TestUtilities.createFile(getWorkDir(), "Tmp/A/B/" + fileName2);

        CompoundFolderChildren cfch = new CompoundFolderChildren(new String [] { "Tmp/A/B/C/D", "Tmp/A/B" }, false);
        List files = cfch.getChildren();
        
        assertEquals("Wrong number of files", 2, files.size());
        assertNotNull("Files do not contain " + fileName1, findFileByName(files, fileName1));
        assertNotNull("Files do not contain " + fileName2, findFileByName(files, fileName2));
        
        cfch = new CompoundFolderChildren(new String [] { "Tmp/X/Y/Z" });
        files = cfch.getChildren();

        assertEquals("Wrong number of files", 0, files.size());
    }
    
    // test hiding files on lower layer by files on higher layers

    public void testHidingSameFilesOnLowerLayers() throws Exception {
        String fileName = "some-file.instance";
        TestUtilities.createFile(getWorkDir(), "Tmp/A/B/C/D/" + fileName);
        TestUtilities.createFile(getWorkDir(), "Tmp/A/B/" + fileName);

        CompoundFolderChildren cfch = new CompoundFolderChildren(new String [] { "Tmp/A/B/C/D", "Tmp/A/B" }, false);
        List files = cfch.getChildren();
        
        assertEquals("Wrong number of files", 1, files.size());
        assertEquals("Wrong layerA file", fileName, ((FileObject) files.get(0)).getNameExt());
    }
    
    // test hidden files

// This one's failing, because the filesystem doesn't show files with the _hidden suffix
//    public void testFilesHiddenBySuffix() throws Exception {
//        String fileName1 = "file-on-layer-A.instance";
//        String fileName2 = "file-on-layer-B.instance";
//        EditorTestLookup.createFile(getWorkDir(), "Tmp/A/B/C/D/" + fileName1);
//        EditorTestLookup.createFile(getWorkDir(), "Tmp/A/B/" + fileName2);
//        EditorTestLookup.createFile(getWorkDir(), "Tmp/A/" + fileName2);
//
//        File markerFile = new File(getWorkDir(), "Tmp/A/B/C/D/" + fileName2 + "_hidden");
//        markerFile.createNewFile();
//        
//        // Check precondition
//        FileObject f = FileUtil.getConfigFile("Tmp/A/B/C/D/");
//        f.refresh();
//        
//        f = FileUtil.getConfigFile("Tmp/A/B/C/D/" + fileName2 + "_hidden");
//        assertNotNull("The _hidden file does not exist", f);
//
//        f = FileUtil.getConfigFile("Tmp/A/B/" + fileName2);
//        assertNotNull("The original file on the second layer that should be hidden does not exist", f);
//
//        f = FileUtil.getConfigFile("Tmp/A/" + fileName2);
//        assertNotNull("The original file on the third layer that should be hidden does not exist", f);
//        
//        // Test compound children
//        CompoundFolderChildren cfch = new CompoundFolderChildren(new String [] { "Tmp/A/B/C/D", "Tmp/A/B", "Tmp/A" }, false);
//        List files = cfch.getChildren();
//        
//        assertEquals("Wrong number of files", 1, files.size());
//        assertEquals("Wrong layerA file", fileName1, ((FileObject) files.get(0)).getNameExt());
//    }

    public void testFilesHiddenByAttribute() throws Exception {
        String fileName1 = "file-on-layer-A.instance";
        String fileName2 = "file-on-layer-B.instance";
        TestUtilities.createFile(getWorkDir(), "Tmp/A/B/C/D/" + fileName1);
        TestUtilities.createFile(getWorkDir(), "Tmp/A/B/" + fileName2);
        TestUtilities.createFile(getWorkDir(), "Tmp/A/" + fileName2);

        // Check precondition
        FileObject f = FileUtil.getConfigFile("Tmp/A/B/" + fileName2);
        assertNotNull("The hidden file on the second layer does not exist", f);

        // Mark the file as hidden, which should hide both this file and
        // the same one on the third layer.
        f.setAttribute("hidden", Boolean.TRUE);
        
        f = FileUtil.getConfigFile("Tmp/A/" + fileName2);
        assertNotNull("The original file on the third layer that should be hidden does not exist", f);
        
        // Test compound children
        CompoundFolderChildren cfch = new CompoundFolderChildren(new String [] { "Tmp/A/B/C/D", "Tmp/A/B", "Tmp/A" }, false);
        List files = cfch.getChildren();
        
        assertEquals("Wrong number of files", 1, files.size());
        assertEquals("Wrong layerA file", fileName1, ((FileObject) files.get(0)).getNameExt());
    }
    
    // test sorting using attributes on different layers

    public void testSorting() throws Exception {
        // Create files
        String fileName1 = "file-1.instance";
        String fileName2 = "file-2.instance";
        String fileName3 = "file-3.instance";
        TestUtilities.createFile(getWorkDir(), "Tmp/A/B/C/D/" + fileName1);
        TestUtilities.createFile(getWorkDir(), "Tmp/A/B/" + fileName2);
        TestUtilities.createFile(getWorkDir(), "Tmp/A/" + fileName3);

        // Set the sorting attributes
        FileObject layer1 = FileUtil.getConfigFile("Tmp/A/B/C/D");
        FileObject layer2 = FileUtil.getConfigFile("Tmp/A/B");
        FileObject layer3 = FileUtil.getConfigFile("Tmp/A");
        
        layer1.setAttribute("file-3.instance/file-1.instance", Boolean.TRUE);
        layer2.setAttribute("file-2.instance/file-3.instance", Boolean.TRUE);
        
        // Create compound children
        CompoundFolderChildren cfch = new CompoundFolderChildren(new String [] { "Tmp/A/B/C/D", "Tmp/A/B", "Tmp/A" }, false);
        List files = cfch.getChildren();

        assertEquals("Wrong number of files", 3, files.size());
        assertEquals("Wrong first file", fileName2, ((FileObject) files.get(0)).getNameExt());
        assertEquals("Wrong second file", fileName3, ((FileObject) files.get(1)).getNameExt());
        assertEquals("Wrong third file", fileName1, ((FileObject) files.get(2)).getNameExt());
    }

    public void testSorting2() throws Exception {
        // Create files
        String fileName1 = "Zfile.instance";
        String fileName2 = "Yfile.instance";
        String fileName3 = "Xfile.instance";
        TestUtilities.createFile(getWorkDir(), "Tmp/A/B/C/D/" + fileName1);
        TestUtilities.createFile(getWorkDir(), "Tmp/A/B/" + fileName2);
        TestUtilities.createFile(getWorkDir(), "Tmp/A/" + fileName3);

        // Create compound children
        CompoundFolderChildren cfch = new CompoundFolderChildren(new String [] { "Tmp/A/B/C/D", "Tmp/A/B", "Tmp/A" }, false);
        List files = cfch.getChildren();

        assertEquals("Wrong number of files", 3, files.size());
        assertEquals("Wrong first file", fileName1, ((FileObject) files.get(0)).getNameExt());
        assertEquals("Wrong second file", fileName2, ((FileObject) files.get(1)).getNameExt());
        assertEquals("Wrong third file", fileName3, ((FileObject) files.get(2)).getNameExt());
    }
    
    // According to the weak stability clause in Utilities.topologicalSort this
    // test could be failing. But it is the behavior we would like to have. The
    // test seems to be passing, but probably just by sheer luck. In general U.tS
    // could move file-1.instance anywhere it likes.
    public void testSorting3() throws Exception {
        // Create files
        String fileName1 = "file-1.instance";
        String fileName2 = "file-2.instance";
        String fileName3 = "file-3.instance";
        TestUtilities.createFile(getWorkDir(), "Tmp/A/B/C/D/" + fileName1);
        TestUtilities.createFile(getWorkDir(), "Tmp/A/B/" + fileName2);
        TestUtilities.createFile(getWorkDir(), "Tmp/A/" + fileName3);

        // Set the sorting attributes
        FileObject layer1 = FileUtil.getConfigFile("Tmp/A/B/C/D");
        FileObject layer2 = FileUtil.getConfigFile("Tmp/A/B");
        FileObject layer3 = FileUtil.getConfigFile("Tmp/A");
        
        layer2.setAttribute("file-3.instance/file-2.instance", Boolean.TRUE);
        
        // Create compound children
        CompoundFolderChildren cfch = new CompoundFolderChildren(new String [] { "Tmp/A/B/C/D", "Tmp/A/B", "Tmp/A" }, false);
        List files = cfch.getChildren();

        assertEquals("Wrong number of files", 3, files.size());
        assertEquals("Wrong first file", fileName1, ((FileObject) files.get(0)).getNameExt());
        assertEquals("Wrong second file", fileName3, ((FileObject) files.get(1)).getNameExt());
        assertEquals("Wrong third file", fileName2, ((FileObject) files.get(2)).getNameExt());
    }

    public void testSortingPositional() throws Exception {
        // Create files
        String fileName1 = "file-1.instance";
        String fileName2 = "file-2.instance";
        String fileName3 = "file-3.instance";
        TestUtilities.createFile(getWorkDir(), "Tmp/A/B/C/D/" + fileName1);
        TestUtilities.createFile(getWorkDir(), "Tmp/A/B/" + fileName2);
        TestUtilities.createFile(getWorkDir(), "Tmp/A/" + fileName3);

        // Set the sorting attributes
        FileObject layer1 = FileUtil.getConfigFile("Tmp/A/B/C/D");
        FileObject layer2 = FileUtil.getConfigFile("Tmp/A/B");
        FileObject layer3 = FileUtil.getConfigFile("Tmp/A");

        layer1.getFileObject(fileName1).setAttribute("position", 300);
        layer2.getFileObject(fileName2).setAttribute("position", 100);
        layer3.getFileObject(fileName3).setAttribute("position", 200);
        
        // Create compound children
        CompoundFolderChildren cfch = new CompoundFolderChildren(new String [] { "Tmp/A/B/C/D", "Tmp/A/B", "Tmp/A" }, false);
        List files = cfch.getChildren();

        assertEquals("Wrong number of files", 3, files.size());
        assertEquals("Wrong first file", fileName2, ((FileObject) files.get(0)).getNameExt());
        assertEquals("Wrong second file", fileName3, ((FileObject) files.get(1)).getNameExt());
        assertEquals("Wrong third file", fileName1, ((FileObject) files.get(2)).getNameExt());
    }

    // test events

    private FileObject findFileByName(List files, String nameExt) {
        for (Iterator i = files.iterator(); i.hasNext(); ) {
            FileObject f = (FileObject) i.next();
            if (nameExt.equals(f.getNameExt())) {
                return f;
            }
        }
        return null;
    }
    
    private static class L implements PropertyChangeListener {
        public int changeEventsCnt = 0;
        public PropertyChangeEvent lastEvent = null;
        
        public void propertyChange(PropertyChangeEvent evt) {
            changeEventsCnt++;
            lastEvent = evt;
        }
        
        public void reset() {
            changeEventsCnt = 0;
            lastEvent = null;
        }
    } // End of L class

    /* TBD whether any of the following, originally from FolderChildrenTest, are still applicable:
    public void testSimple() throws Exception {
        String fileName = "org-netbeans-modules-editor-mimelookup-DummyClass2LayerFolder.instance";
        TestUtilities.createFile(getWorkDir(), "Tmp/A/B/C/D/" + fileName);

        FolderChildren fch = new FolderChildren("Tmp/A/B/C/D");
        List files = fch.getChildren();
        
        assertEquals("Wrong number of files", 1, files.size());
        assertEquals("Wrong file", fileName, ((FileObject) files.get(0)).getNameExt());
        
        fch = new FolderChildren("Tmp/X/Y/Z");
        files = fch.getChildren();

        assertEquals("Wrong number of files", 0, files.size());
    }

    public void testAddingFolders() throws Exception {
        String fileName = "org-netbeans-modules-editor-mimelookup-DummyClass2LayerFolder.instance";
        FolderChildren fch = new FolderChildren("Tmp/A/B/C/D");
        List files = fch.getChildren();

        assertEquals("Wrong number of files", 0, files.size());
        
        TestUtilities.createFile(getWorkDir(), "Tmp/A/B/C/D/" + fileName);
        
        files = fch.getChildren();
        assertEquals("Wrong number of files", 1, files.size());
        assertEquals("Wrong file", fileName, ((FileObject) files.get(0)).getNameExt());
    }

    public void testRemovingFolders() throws Exception {
        String fileName = "org-netbeans-modules-editor-mimelookup-DummyClass2LayerFolder.instance";
        TestUtilities.createFile(getWorkDir(), "Tmp/A/B/C/D/" + fileName);

        FolderChildren fch = new FolderChildren("Tmp/A/B/C/D");
        List files = fch.getChildren();

        assertEquals("Wrong number of files", 1, files.size());
        assertEquals("Wrong file", fileName, ((FileObject) files.get(0)).getNameExt());
        
        TestUtilities.deleteFile(getWorkDir(), "Tmp/A/");
        
        files = fch.getChildren();
        assertEquals("Wrong number of files", 0, files.size());
    }
    
    public void testMultipleAddRemove() throws Exception {
        for (int i = 0; i < 7; i++) {
            testAddingFolders();
            testRemovingFolders();
        }
    }
    
    public void testChangeEvents() throws Exception {
        String fileName = "org-netbeans-modules-editor-mimelookup-DummyClass2LayerFolder.instance";
        FolderChildren fch = new FolderChildren("Tmp/A/B/C/D");
        L listener = new L();
        fch.addPropertyChangeListener(listener);
        
        List files = fch.getChildren();
        assertEquals("Wrong number of events", 0, listener.changeEventsCnt);
        assertEquals("Wrong number of files", 0, files.size());
        
        TestUtilities.createFile(getWorkDir(), "Tmp/A/B/C/D/" + fileName);

        assertEquals("Wrong number of events", 1, listener.changeEventsCnt);
        assertEquals("Wrong event", FolderChildren.PROP_CHILDREN, listener.lastEvent.getPropertyName());
        files = fch.getChildren();
        assertEquals("Wrong number of files", 1, files.size());
        assertEquals("Wrong file", fileName, ((FileObject) files.get(0)).getNameExt());

        listener.reset();
        
        TestUtilities.deleteFile(getWorkDir(), "Tmp/A/");

        assertEquals("Wrong number of events", 1, listener.changeEventsCnt);
        assertEquals("Wrong event", FolderChildren.PROP_CHILDREN, listener.lastEvent.getPropertyName());
        files = fch.getChildren();
        assertEquals("Wrong number of files", 0, files.size());
    }

    public void testEventsWithMultipleChanges() throws Exception {
        for (int i = 0; i < 11; i++) {
            testChangeEvents();
        }
    }

    public void testEmptyFolder() throws Exception {
        FolderChildren fch = new FolderChildren("Tmp/A/B/C/D");
        L listener = new L();
        fch.addPropertyChangeListener(listener);
        
        List files = fch.getChildren();
        assertEquals("Wrong number of events", 0, listener.changeEventsCnt);
        assertEquals("Wrong number of files", 0, files.size());
        
        TestUtilities.createFile(getWorkDir(), "Tmp/A/B/C/D/");

        assertEquals("Wrong number of events", 0, listener.changeEventsCnt);
        files = fch.getChildren();
        assertEquals("Wrong number of files", 0, files.size());

        listener.reset();
        
        TestUtilities.deleteFile(getWorkDir(), "Tmp/A/");

        assertEquals("Wrong number of events", 0, listener.changeEventsCnt);
        files = fch.getChildren();
        assertEquals("Wrong number of files", 0, files.size());
    }

    public void testAttributeChanges() throws Exception {
        TestUtilities.createFile(getWorkDir(), "Tmp/A/B/C/D/");
        FolderChildren fch = new FolderChildren("Tmp/A/B/C/D");
        L listener = new L();
        fch.addPropertyChangeListener(listener);
        
        List files = fch.getChildren();
        assertEquals("Wrong number of events", 0, listener.changeEventsCnt);
        assertEquals("Wrong number of files", 0, files.size());
        
        FileObject f = FileUtil.getConfigFile("Tmp/A/B/C/D");
        assertNotNull("Can't find the folder", f);
        
        f.setAttribute("attrName", "attrValue");
        
        assertEquals("Wrong number of events", 1, listener.changeEventsCnt);
        assertEquals("Wrong event", FolderChildren.PROP_ATTRIBUTES, listener.lastEvent.getPropertyName());
        files = fch.getChildren();
        assertEquals("Wrong number of files", 0, files.size());

        listener.reset();
        
        f.setAttribute("attrName", null);

        assertEquals("Wrong number of events", 1, listener.changeEventsCnt);
        assertEquals("Wrong event", FolderChildren.PROP_ATTRIBUTES, listener.lastEvent.getPropertyName());
        files = fch.getChildren();
        assertEquals("Wrong number of files", 0, files.size());
    }
     */

}
