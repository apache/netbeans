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

import java.awt.Dialog;
import java.awt.EventQueue;
import java.awt.Frame;
import java.awt.GraphicsEnvironment;
import java.io.IOException;
import java.util.concurrent.atomic.AtomicReference;
import javax.swing.JEditorPane;
import javax.swing.text.StyledDocument;
import junit.framework.Test;
import junit.framework.TestSuite;
import org.netbeans.junit.NbTestCase;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.cookies.EditorCookie;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataFolder;
import org.openide.loaders.DataLoader;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectExistsException;
import org.openide.loaders.FileEntry;
import org.openide.loaders.MultiDataObject;
import org.openide.loaders.MultiFileLoader;
import org.openide.loaders.UniFileLoader;
import org.openide.util.test.MockLookup;

public class DataEditorSupportMoveTest extends NbTestCase {
    
    public static Test suite() {
        return GraphicsEnvironment.isHeadless() ? new TestSuite() : new TestSuite(DataEditorSupportMoveTest.class);
    }

    static {
        System.setProperty("org.openide.windows.DummyWindowManager.VISIBLE", "false");
    }
    
    public DataEditorSupportMoveTest(String s) {
        super(s);
    }
    
    @Override
    protected void setUp() throws Exception {
        clearWorkDir();
        MockLookup.setInstances(new Pool(), new DD());
    }
    
    public void testModifiedMove() throws Exception {
        FileObject root = FileUtil.toFileObject(getWorkDir());
        FileObject data = FileUtil.createData(root, "someFolder/someFile.obj");
        
        DataObject obj = DataObject.find(data);
        assertEquals( MyDataObject.class, obj.getClass());
        assertEquals(MyLoader.class, obj.getLoader().getClass());
        
        EditorCookie ec = obj.getLookup().lookup(EditorCookie.class);
        assertNotNull("Editor cookie found", ec);
        ec.open();
        JEditorPane[] arr = openedPanes(ec);
        assertEquals("One pane is opened", 1, arr.length);
                
        StyledDocument doc = ec.openDocument();
        doc.insertString(0, "Ahoj", null);
        assertTrue("Modified", obj.isModified());
        Thread.sleep(100);
        
        FileObject newFolder = FileUtil.createFolder(root, "otherFolder");
        DataFolder df = DataFolder.findFolder(newFolder);
        
        obj.move(df);
        
        assertEquals(newFolder, obj.getPrimaryFile().getParent());
        
        assertEquals("Still one editor", 1, openedPanes(ec).length);
        DD.assertNoCalls();
    }

    private JEditorPane[] openedPanes(final EditorCookie ec) throws Exception {
        final AtomicReference<JEditorPane[]> ref = new AtomicReference<JEditorPane[]>();
        EventQueue.invokeAndWait(new Runnable() {
            @Override
            public void run() {
                ref.set(ec.getOpenedPanes());
            }
        });
        return ref.get();
    }

    private static final class Pool extends org.openide.loaders.DataLoaderPool {
        @Override
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
        @Override
        protected MultiDataObject createMultiObject(FileObject primaryFile) throws DataObjectExistsException, IOException {
            return new MyDataObject(this, primaryFile);
        }
    }
    
    public static final class MyDataObject extends MultiDataObject  {
        public MyDataObject(MyLoader l, FileObject folder) throws DataObjectExistsException {
            super(folder, l);
            registerEditor("text/plain", false);
        }

    }

    private static class MyMultiFileLoader extends MultiFileLoader {
        public MyMultiFileLoader () {
            super(MyMultiFileDataObject.class.getName());
        }
        
        @Override
        protected MultiDataObject createMultiObject (FileObject primaryFile) throws DataObjectExistsException, IOException {
            return new MyMultiFileDataObject( primaryFile, this );
        }
    
        @Override
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

        @Override
        protected MultiDataObject.Entry createPrimaryEntry(MultiDataObject obj, FileObject primaryFile) {
            return new FileEntry (obj, primaryFile);
        }

        @Override
        protected MultiDataObject.Entry createSecondaryEntry(MultiDataObject obj, FileObject secondaryFile) {
            return new FileEntry (obj, secondaryFile);
        }
    } // end of MyDL3

    private static class MyMultiFileDataObject extends MultiDataObject {
        public MyMultiFileDataObject( FileObject primaryFile, MultiFileLoader loader ) throws DataObjectExistsException {
            super( primaryFile, loader );
        }
    }
    
    public static final class DD extends DialogDisplayer {
        static Exception called;
        
        @Override
        public Object notify(NotifyDescriptor descriptor) {
            called = new Exception("Notify called");
            return NotifyDescriptor.CANCEL_OPTION;
        }

        @Override
        public Dialog createDialog(DialogDescriptor descriptor) {
            called = new Exception("Dialog created");
            return new Dialog((Frame)null);
        }
        
        public static void assertNoCalls() throws Exception {
            if (called != null) {
                throw called;
            }
        }
    }
}
