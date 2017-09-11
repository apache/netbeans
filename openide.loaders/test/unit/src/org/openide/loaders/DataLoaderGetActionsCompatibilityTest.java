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
import javax.swing.Action;
import org.netbeans.junit.NbTestCase;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileSystem;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.Repository;
import org.openide.util.io.NbMarshalledObject;
import org.openide.util.test.MockLookup;

/** Check how the default behaviour of DataLoader without overriden
 * actionsContext works.
 *
 * @author Jaroslav Tulach
 */
public class DataLoaderGetActionsCompatibilityTest extends NbTestCase {
    /** sample data object */
    private DataObject obj;
    /** its node */
    private org.openide.nodes.Node node;
    /** monitor sfs */
    private PCL sfs;

    public DataLoaderGetActionsCompatibilityTest (String name) {
        super(name);
    }
    
    /**
     * Sets up the testing environment by creating testing folders
     * on the system file system.
     */
    @Override
    protected void setUp() throws Exception {
        clearWorkDir();
        MockLookup.setInstances(
                new Repository(TestUtilHid.createLocalFileSystem(getWorkDir(), new String[0])),
                new Pool());
        
        MyDL loader = MyDL.getLoader(MyDL.class);

        FileSystem dfs = FileUtil.getConfigRoot().getFileSystem();
        dfs.refresh (true);        
        
        FileObject fo = FileUtil.createData (dfs.getRoot (), "a.txt");
        obj = DataObject.find (fo);
        
        assertEquals ("The correct loader", loader, obj.getLoader ());
        
        node = obj.getNodeDelegate ();    
        
        sfs = new PCL ();
        dfs.addFileChangeListener (sfs);
    }
    
    /**
     * Deletes the folders created in method setUp().
     */
    @Override
    protected void tearDown() throws Exception {
        obj.getLoader ().setActions (new org.openide.util.actions.SystemAction[0]);
        
        int l = node.getActions (false).length;
        if (l != 0) {
            fail ("Not empty actions at the end!!!");
        }
        FileUtil.getConfigRoot().getFileSystem().removeFileChangeListener (sfs);

        // no suspicious activity on getDefaultFileSystem
        sfs.assertEvent (0, null);
    }
    
    public void testDefaultActionContextReturnsNull () {
        assertNull (obj.getLoader ().actionsContext ());
    }
    
    public void testDefaultActionsUsedWhenCreatedForTheFirstTime () throws Exception {
        SndDL loader = SndDL.getLoader(SndDL.class);
        
        org.openide.util.actions.SystemAction[] arr = loader.getActions ();
        
        assertEquals (
            "Arrays of actions are the same", 
            java.util.Arrays.asList (loader.defaultActions ()),
            java.util.Arrays.asList (arr)
        );
    }

    /** Test to check that the deserialization of actions works as expected.
     */
    public void testNoDeserializationOfActions () throws Exception {
        assertEquals("No actions at the start", 0, node.getActions(false).length);
        
        PCL pcl = new PCL ();
        obj.getLoader ().addPropertyChangeListener (pcl);
        
        obj.getLoader ().setActions (new org.openide.util.actions.SystemAction[] {
                org.openide.util.actions.SystemAction.get (org.openide.actions.PropertiesAction.class)
        });
        
        pcl.assertEvent (1, "actions");
        
        Action [] res = node.getActions(false);
        assertEquals("There should be exactly one action.", 1, res.length);
        
        NbMarshalledObject m = new NbMarshalledObject (obj.getLoader ());
        
        obj.getLoader ().setActions (new org.openide.util.actions.SystemAction[0]);

        pcl.assertEvent (2, "actions");
        assertEquals("No actions after setting empty array", 0, node.getActions(false).length);
        
        assertEquals ("Loader deserialized", obj.getLoader (), m.get ());
        res = node.getActions(false);
        assertEquals("One action", 1, res.length);
        assertEquals (
            "and that is the property action", 
            org.openide.util.actions.SystemAction.get (org.openide.actions.PropertiesAction.class),
            res[0]
        );
        
        obj.getLoader ().removePropertyChangeListener (pcl);
    }
    
    /** Loader that does not override the actionsContext.
     */
    private static class MyDL extends UniFileLoader {
        public MyDL () {
            super ("org.openide.loaders.DataObject");
            getExtensions ().addExtension ("txt");
        }
        
        protected org.openide.loaders.MultiDataObject createMultiObject (FileObject primaryFile) throws org.openide.loaders.DataObjectExistsException, IOException {
            
            
            
            return new MultiDataObject (primaryFile, this);
        }
        
        @Override
        protected org.openide.util.actions.SystemAction[] defaultActions () {
            return new org.openide.util.actions.SystemAction[0];
        }
        
    } // end of MyDL
    
    private static final class Pool extends DataLoaderPool {
        
        protected java.util.Enumeration<? extends DataLoader> loaders() {
            return org.openide.util.Enumerations.singleton (
                DataLoader.getLoader(MyDL.class)
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
        
        public void assertEvent (int cnt, String name) {
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
    } // end of PCL
    
    public static final class SndDL extends MyDL {
        public SndDL () {
            getExtensions ().addExtension ("bla");
        }
        
        @Override
        protected org.openide.util.actions.SystemAction[] defaultActions () {
            return new org.openide.util.actions.SystemAction[] {
                org.openide.util.actions.SystemAction.get (org.openide.actions.CutAction.class),
                null,
                org.openide.util.actions.SystemAction.get (org.openide.actions.CopyAction.class),
                null,
                org.openide.util.actions.SystemAction.get (org.openide.actions.DeleteAction.class),
                
            };
        }
        
    }
    
}
