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

import java.util.Collection;
import java.util.Iterator;
import javax.swing.Icon;
import javax.swing.event.ChangeEvent;
import org.openide.filesystems.*;
import javax.swing.event.ChangeListener;
import org.netbeans.api.actions.Savable;
import org.netbeans.junit.*;
import org.openide.cookies.EditorCookie;
import org.openide.cookies.SaveCookie;

public class DataGetModifiedTest extends NbTestCase {

    public DataGetModifiedTest(String name) {
        super(name);
    }

    @Override
    protected void setUp() throws Exception {
        clearWorkDir ();
        lfs = TestUtilHid.createLocalFileSystem (getWorkDir (), fsstruct);
        do1 = DataObject.find(lfs.findResource("/Dir/SubDir/A.txt"));
        do2 = DataObject.find(lfs.findResource("/Dir/SubDir/B.txt"));
        do3 = DataObject.find(lfs.findResource("/Dir/SubDir/C.txt"));
    }
    
    //Clear all stuff when the test finish
    @Override
    protected void tearDown() throws Exception {
        TestUtilHid.destroyLocalFileSystem (getName());
        for (Savable s : Savable.REGISTRY.lookupAll(Savable.class)) {
            s.save();
        }
        Collection<? extends Savable> empty = Savable.REGISTRY.lookupAll(Savable.class);
        assertTrue("registry is emptied: " + empty, empty.isEmpty());
    }

    
    public void testCanChangeModifiedFilesWhenIterating() throws Exception {
        do1.getLookup().lookup(EditorCookie.class).openDocument().insertString(0, "Ahoj", null);
        do3.getLookup().lookup(EditorCookie.class).openDocument().insertString(0, "Ahoj", null);
        
        Iterator<DataObject> it = DataObject.getRegistry().getModifiedSet().iterator();
        assertTrue("There is modified 1", it.hasNext());
        DataObject m1 = it.next();
        if (m1 != do1 && m1 != do3) {
            fail("Strange modified object1 " + m1);
        }
        
        do2.getLookup().lookup(EditorCookie.class).openDocument().insertString(0, "Ahoj", null);
        
        
        assertTrue("There is modified 2", it.hasNext());
        
        DataObject m2 = it.next();
        if (m2 != do1 && m2 != do3) {
            fail("Strange modified object2 " + m2);
        }
        if (m1 == m2) {
            fail("Same modified object twice: " + m1 + " = " + m2);
        }
        
        assertFalse("No third object added when iterating", it.hasNext());
        
        assertEquals("But now visible", 3, DataObject.getRegistry().getModifiedSet().size());
    }
    
    public void testSavableRegistry() throws Exception {
        doTestSavableRegistry(true);
    }
    
    public void testSetModifiedClearsSavable() throws Exception {
        doTestSavableRegistry(false);
    }
    
    private void doTestSavableRegistry(boolean save) throws Exception {
        class L implements ChangeListener {
            int cnt;

            @Override
            public void stateChanged(ChangeEvent e) {
                assertTrue(e.getSource() instanceof Collection);
                for (Object o : (Collection)e.getSource()) {
                    assertTrue("DataObject is the value: " + o, o instanceof DataObject);
                }
                cnt++;
            }
            
        }
        L listener = new L();
        
        DataObject.getRegistry().addChangeListener(listener);
        do1.getLookup().lookup(EditorCookie.class).openDocument().insertString(0, "Ahoj", null);
        String name = do1.getNodeDelegate().getDisplayName();
        assertTrue("DataObject is modified", do1.isModified());
        assertEquals("One change in registry", 1, listener.cnt);

        Savable savable = findSavable(name);
        assertNotNull("Savable for the do1 lookup found", savable);
        savable.save();
        assertFalse("DataObject no longer modified", do1.isModified());
        assertEquals("2nd change in registry", 2, listener.cnt);
        
        do1.getLookup().lookup(EditorCookie.class).openDocument().insertString(0, "Ahoj", null);
        assertTrue("DataObject is modified again", do1.isModified());
        assertEquals("3rd change in registry", 3, listener.cnt);
        
        Savable another = findSavable(name);
        assertNotSame("It is different instance", savable, another);
        assertEquals("But it remains equals", savable, another);
        assertTrue("DataObject savables provide Icons", another instanceof Icon);
        
        savable.save();
        assertTrue("Calling save on old savable has no impact", do1.isModified());
        
        SaveCookie sc = do1.getLookup().lookup(SaveCookie.class);
        if (save) {
            sc.save();
        } else {
            do1.setModified(false);
        }
        assertFalse("Unmodified", do1.isModified());
        assertNull("No save cookie", do1.getLookup().lookup(SaveCookie.class));
        
        Savable none = findSavable(name);
        assertNull("No savable for our dataobject found", none);
    }

    private Savable findSavable(String name) {
        Savable savable = null;
        for (Savable s : Savable.REGISTRY.lookupAll(Savable.class)) {
            if (s.toString().equals(name)) {
                savable = s;
                break;
            }
        }
        return savable;
    }
    
    
    private String fsstruct [] = new String [] {
        "Dir/SubDir/X.txt",
        "Dir/SubDir/T.txt",
        "Dir/SubDir/A.txt",
        "Dir/SubDir/B.txt",
        "Dir/SubDir/C.txt",
    };
    private FileSystem lfs;
    private DataObject do1;
    private DataObject do2;
    private DataObject do3;
}
