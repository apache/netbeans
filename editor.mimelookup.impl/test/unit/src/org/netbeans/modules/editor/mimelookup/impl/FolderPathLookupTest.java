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

package org.netbeans.modules.editor.mimelookup.impl;

import java.net.URL;
import java.util.Collection;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.junit.NbTestCase;
import org.netbeans.spi.editor.mimelookup.Class2LayerFolder;
import org.openide.filesystems.FileObject;
import org.openide.util.Lookup;
import org.openide.util.LookupEvent;
import org.openide.util.LookupListener;

/**
 *
 * @author vita
 */
public class FolderPathLookupTest extends NbTestCase {

    /** Creates a new instance of FolderPathLookupTest */
    public FolderPathLookupTest(String name) {
        super(name);
    }

    @Override
    protected void setUp() throws java.lang.Exception {
        clearWorkDir();
        // Set up the default lookup, repository, etc.
        EditorTestLookup.setLookup(new String[0], getWorkDir(), new Object[] {},
            getClass().getClassLoader(), 
            null
        );
        Logger.getLogger("org.openide.filesystems.Ordering").setLevel(Level.OFF);
    }
    
    @Override
    protected void tearDown() {
        TestUtilities.gc();
    }
    
    public void testSimple() throws Exception {
        TestUtilities.createFile(getWorkDir(), "Tmp/A/B/C/D/org-netbeans-modules-editor-mimelookup-impl-DummySettingImpl.instance");

        // Create lookup over an existing folder
        Lookup lookup = new FolderPathLookup(new String [] { "Tmp/A/B/C/D" });
        Collection instances = lookup.lookupAll(DummySetting.class);

        assertEquals("Wrong number of instances", 1, instances.size());
        assertEquals("Wrong instance", DummySettingImpl.class, instances.iterator().next().getClass());

        // Now create lookup over a non-existing folder
        lookup = new FolderPathLookup(new String [] { "Tmp/X/Y/Z" });
        instances = lookup.lookupAll(Object.class);

        assertEquals("Wrong number of instances", 0, instances.size());
    }

    public void testAddingFolders() throws Exception {
        // Create lookup over a non-existing folder
        Lookup lookup = new FolderPathLookup(new String [] { "Tmp/A/B/C/D" });
        Collection instances = lookup.lookupAll(Class2LayerFolder.class);

        assertEquals("Wrong number of instances", 0, instances.size());

        // Create the folder and the instance
        TestUtilities.createFile(getWorkDir(), "Tmp/A/B/C/D/org-netbeans-modules-editor-mimelookup-impl-DummySettingImpl.instance");

        instances = lookup.lookupAll(DummySetting.class);
        assertEquals("Wrong number of instances", 1, instances.size());
        assertEquals("Wrong instance", DummySettingImpl.class, instances.iterator().next().getClass());
    }

    public void testRemovingFolders() throws Exception {
        TestUtilities.createFile(getWorkDir(), "Tmp/A/B/C/D/org-netbeans-modules-editor-mimelookup-impl-DummySettingImpl.instance");

        // Create lookup over an existing folder
        Lookup lookup = new FolderPathLookup(new String [] { "Tmp/A/B/C/D" });
        Collection instances = lookup.lookupAll(DummySetting.class);

        assertEquals("Wrong number of instances", 1, instances.size());
        assertEquals("Wrong instance", DummySettingImpl.class, instances.iterator().next().getClass());

        // Delete the folders
        TestUtilities.deleteFile(getWorkDir(), "Tmp");

        instances = lookup.lookupAll(Class2LayerFolder.class);
        assertEquals("Wrong number of instances", 0, instances.size());
    }

    public void testChangeEvents() throws Exception {
        Lookup.Result lr = new FolderPathLookup(new String [] { "Tmp/A/B/C/D" }).lookupResult(DummySetting.class);
        L listener = new L();
        lr.addLookupListener(listener);

        Collection instances = lr.allInstances();
        assertEquals("Wrong number of instances", 0, instances.size());

        // Create the folder and the instance
        TestUtilities.createFile(getWorkDir(), "Tmp/A/B/C/D/org-netbeans-modules-editor-mimelookup-impl-DummySettingImpl.instance");

        assertEquals("Wrong number of events", 1, listener.resultChangedCnt);

        instances = lr.allInstances();
        assertEquals("Wrong number of instances", 1, instances.size());
        assertEquals("Wrong instance", DummySettingImpl.class, instances.iterator().next().getClass());

        // Reset the listener
        listener.resultChangedCnt = 0;

        // Delete the folders
        TestUtilities.deleteFile(getWorkDir(), "Tmp");

        assertEquals("Wrong number of events", 1, listener.resultChangedCnt);

        instances = lr.allInstances();
        assertEquals("Wrong number of instances", 0, instances.size());
    }

    // IZ #104705
    public void testInstaceOf() throws Exception {
        EditorTestLookup.setLookup(
            new URL[] { getClass().getResource("test-layer.xml") }, 
            getWorkDir(), 
            new Object[] {},
            getClass().getClassLoader()
        );
        
        Lookup lookup = new FolderPathLookup(new String [] { "Tmp/PathFolderLookupTest/testInstanceOf" });
        
        // Check IfaceA instances, it should not pick up the one with
        // instanceOf == ...$IfaceB
        Collection instances = lookup.lookupAll(IfaceA.class);
        assertEquals("Wrong number of IfaceA instances", 1, instances.size());
        
        Object instance = instances.iterator().next();
        assertTrue("Wrong instance", instance instanceof ImplAB);
        assertEquals("Wrong instance file", 
            "Tmp/PathFolderLookupTest/testInstanceOf/ifaceA-impl.instance", 
            ((ImplAB) instance).fileObject.getPath());
    }

    public static Object createIfacesImpl(FileObject fo) {
        return new ImplAB(fo);
    }
    
    public static interface IfaceA {
        void methodA();
    }
    
    public static interface IfaceB {
        void methodB();
    }
    
    private static final class ImplAB implements IfaceA, IfaceB {
        public FileObject fileObject;
        public ImplAB(FileObject fo) {
            this.fileObject = fo;
        }
        public void methodA() {
        }

        public void methodB() {
        }
    }
    
    private static final class L implements LookupListener {
        public int resultChangedCnt = 0;
        public void resultChanged(LookupEvent ev) {
            resultChangedCnt++;
        }
    }
}
