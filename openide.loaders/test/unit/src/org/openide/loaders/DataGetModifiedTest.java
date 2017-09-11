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
