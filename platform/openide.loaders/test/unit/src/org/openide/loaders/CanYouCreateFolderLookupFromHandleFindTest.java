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
import javax.swing.JButton;
import org.netbeans.junit.MockServices;
import org.netbeans.junit.NbTestCase;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Enumerations;
import org.openide.util.Lookup;
import org.openide.util.SharedClassObject;

/** It must be possible to create lookup anytime, if there is no deadlock...
 *
 * @author Jaroslav Tulach
 */
public class CanYouCreateFolderLookupFromHandleFindTest extends NbTestCase {
    
    public CanYouCreateFolderLookupFromHandleFindTest(String s) {
        super(s);
    }
    
    protected void setUp() {
        MockServices.setServices(new Class[] {Pool.class});
    }
    
    public void testCreateAndImmediatelyQueryTheLookup() throws Exception {
        MyLoader m = MyLoader.getLoader(MyLoader.class);
        m.button = FileUtil.createFolder(FileUtil.getConfigRoot(), "FolderLookup");
        DataObject instance = InstanceDataObject.create(DataFolder.findFolder(m.button), "SomeName", JButton.class);
        m.instanceFile = instance.getPrimaryFile();
        
        WeakReference ref = new WeakReference(instance);
        instance = null;
        assertGC("Object must disappear first", ref);
        
        FileObject any = FileUtil.getConfigRoot().createData("Ahoj.txt");
        DataObject obj = DataObject.find(any);
        
        assertEquals("The right object found", m, obj.getLoader());
        assertNotNull("Value found", m.v);
        assertEquals("Button", JButton.class, m.v.getClass());
        assertNotNull("Lookup created", m.lookup);
    }
    
    /**
     * Registering directly MyLoader does not work since core's DLP is found
     * and that one does not check META-INF/services.
     */
    public static final class Pool extends DataLoaderPool {
        protected Enumeration<? extends DataLoader> loaders() {
            return Enumerations.singleton(SharedClassObject.findObject(MyLoader.class, true));
        }
    }
    
    public static final class MyLoader extends UniFileLoader {
        public FileObject button;
        public Object v;
        public Lookup lookup;
        
        public InstanceDataObject created;
        
        private FileObject instanceFile;
        
        private DataObject middleCreation;
        
        public MyLoader() throws IOException {
            super("org.openide.loaders.MultiDataObject");
        }
        
        protected FileObject findPrimaryFile(FileObject fo) {
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
    
}
