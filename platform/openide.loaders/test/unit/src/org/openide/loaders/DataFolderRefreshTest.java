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

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import org.openide.filesystems.*;

import java.util.ArrayList;


/** Testing that a change in a pool triggers notification of a change in DataFolder's
 * children.
 *
 * @author  Jaroslav Tulach
 */
public class DataFolderRefreshTest extends LoggingTestCaseHid {
    private ArrayList hold = new ArrayList();
    private org.openide.ErrorManager err;

    private FileObject root;

    /** Creates new DataFolderTest */
    public DataFolderRefreshTest (String name) {
        super (name);
    }
    
    @Override
    protected void setUp () throws Exception {
        err = org.openide.ErrorManager.getDefault().getInstance("TEST-" + getName());

        registerIntoLookup(new FolderInstanceTest.Pool());
        
        DataLoaderPool pool = DataLoaderPool.getDefault ();
        assertNotNull (pool);
        assertEquals (FolderInstanceTest.Pool.class, pool.getClass ());
        
        clearWorkDir ();
        
        root = FileUtil.createFolder(
            FileUtil.getConfigRoot(),
            "dir"
        );
        
        FileUtil.createData(root, "s1.simple");
        FileUtil.createData(root, "s2.simple");
    }

    public void testIsChangeFired() throws Exception {
        DataLoader l = DataLoader.getLoader(DataLoaderOrigTest.SimpleUniFileLoader.class);
        err.log("Add loader: " + l);
        FolderInstanceTest.Pool.setExtra(l);
        err.log("Loader added");
        
        DataFolder f = DataFolder.findFolder(root);
        class C implements PropertyChangeListener {
            PropertyChangeEvent ev;
            
            public void propertyChange(PropertyChangeEvent evt) {
                assertNull("Only one event", this.ev);
                this.ev = evt;
            }
        }
        
        C c = new C();
        f.addPropertyChangeListener(c);
        
        DataObject[] arr = f.getChildren();
        
        assertEquals("Two objects", 2, arr.length);
        assertEquals("Loader1", arr[0].getLoader(), l);
        assertEquals("Loader2", arr[1].getLoader(), l);
        
        FolderInstanceTest.Pool.setExtra(null);
        
        arr = f.getChildren();
        
        assertNotNull("A change event delivered", c.ev);
        assertEquals("children", DataFolder.PROP_CHILDREN, c.ev.getPropertyName());
        
        
        assertEquals("Two objects", 2, arr.length);
        assertEquals("Loader1", arr[0].getLoader(), DataLoaderPool.getDefaultFileLoader());
        assertEquals("Loader2", arr[1].getLoader(), DataLoaderPool.getDefaultFileLoader());
    }
}
