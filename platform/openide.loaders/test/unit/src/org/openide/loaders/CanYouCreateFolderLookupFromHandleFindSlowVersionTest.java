/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.openide.loaders;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.Enumeration;
import java.util.List;
import javax.swing.JButton;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Enumerations;
import org.openide.util.Lookup;


/** It must be possible to create lookup anytime, if there is no deadlock,
 * even if the recognition of FolderLookup takes really long time.
 *
 * @author Jaroslav Tulach
 */
public class CanYouCreateFolderLookupFromHandleFindSlowVersionTest extends LoggingTestCaseHid {
    
    /** Creates a new instance of CanYouQueryFolderLookupFromHandleFindTest */
    public CanYouCreateFolderLookupFromHandleFindSlowVersionTest(String s) {
        super(s);
    }
    
    protected void setUp() {
        registerIntoLookup(new Pool());
    }
    
    public void testCreateAndImmediatellyQueryWhenThereIsALotfSlowDataObjectsTheLookup() throws Exception {
        MyLoader m = MyLoader.getLoader(MyLoader.class);
        m.button = FileUtil.createFolder(FileUtil.getConfigRoot(), "FolderLookup");
        DataObject instance = InstanceDataObject.create(DataFolder.findFolder(m.button), "SomeName", JButton.class);
        m.instanceFile = instance.getPrimaryFile();
        for (int i = 0; i < 15; i++) {
            m.button.createData("slow" + i + ".slow");
        }
        
        
        WeakReference ref = new WeakReference(instance);
        instance = null;
        assertGC("Object must disappear first", ref);
        
        FileObject any = FileUtil.getConfigRoot().createData("Ahoj.txt");
        DataObject obj = DataObject.find(any);
        
        assertEquals("The right object found", m, obj.getLoader());
        assertNotNull("Value found", m.v);
        assertEquals("Button", JButton.class, m.v.getClass());
        assertNotNull("Lookup created", m.lookup);
        assertEquals("All slow files recognized", 15, m.slowCnt);
    }
    
    
    public static final class MyLoader extends UniFileLoader {
        public FileObject button;
        public Object v;
        public Lookup lookup;
        
        public InstanceDataObject created;
        
        private FileObject instanceFile;
        
        private DataObject middleCreation;

        private int slowCnt;
        
        public MyLoader() throws IOException {
            super("org.openide.loaders.MultiDataObject");
        }
        
        protected FileObject findPrimaryFile(FileObject fo) {
            if (fo.hasExt("slow")) {
                slowCnt++;
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException ex) {
                    ex.printStackTrace();
                    fail("No failures, please");
                }
                return null;
            }
            
            if (!fo.hasExt("txt")) {
                return null;
            }
            
            assertNull("First invocation", lookup);
            
            FolderLookup l = new FolderLookup(DataFolder.findFolder(button));
            lookup = l.getLookup();
            v = lookup.lookup(JButton.class);
            assertNotNull("The instance computed", v);
            
            return fo;
        }
        
        protected MultiDataObject createMultiObject(FileObject primaryFile) throws DataObjectExistsException, IOException {
            return new MultiDataObject(primaryFile, this);
        }
    }
    
    private static final class Pool extends DataLoaderPool {
        static List loaders;
        
        public Pool() {
        }
        
        public Enumeration<DataLoader> loaders() {
            return Enumerations.<DataLoader>singleton(DataLoader.getLoader(MyLoader.class));
        }
    }
    
}
