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
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.Enumeration;
import javax.swing.Action;
import javax.swing.JSeparator;
import org.netbeans.junit.NbTestCase;
import org.openide.actions.CopyAction;
import org.openide.actions.CutAction;
import org.openide.actions.DeleteAction;
import org.openide.actions.PropertiesAction;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileSystem;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.Repository;
import org.openide.nodes.Node;
import org.openide.util.Enumerations;
import org.openide.util.Utilities;
import org.openide.util.actions.SystemAction;
import org.openide.util.io.NbMarshalledObject;
import org.openide.util.lookup.Lookups;
import org.openide.util.test.MockLookup;

/** Tests the proposed behaviour of DataLoader.getActions that is going 
 * to read its values from layer.
 *
 * @author Jaroslav Tulach (taken from openidex/enode by David Strupl)
 */
public class DataLoaderGetActionsTest extends NbTestCase {
    /** root folder FileObject */
    private FileObject root;
    /** sample data object */
    private DataObject obj;
    /** its node */
    private Node node;

    public DataLoaderGetActionsTest (String name) {
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
        root = FileUtil.createFolder (dfs.getRoot (), loader.actionsContext ());
        
        
        FileObject fo = FileUtil.createData (dfs.getRoot (), "a.txt");
        obj = DataObject.find (fo);
        
        assertEquals ("The correct loader", loader, obj.getLoader ());
        
        node = obj.getNodeDelegate ();    
    }
    
    /**
     * Deletes the folders created in method setUp().
     */
    @Override
    protected void tearDown() throws Exception {
        FileObject[] arr = root.getChildren ();
        for (int i = 0; i < arr.length; i++) {
            arr[i].delete();
        }
        int l = node.getActions (false).length;
        if (l != 0) {
            System.err.println("Not empty actions at the end!!!");
        }
    }
    
    /**
     * This test tests the presence of declarative actions from
     * system file system without the hierarchical flag set (the ExtensibleNode
     * instance is created with constructor ExtensibleNode("test", false).
     * The tests performs following steps:
     * <OL><LI> Create an instance of ExtensibleNode with folder set to "test"
     *     <LI> No actions should be returned by getActions since the "test" folder
     *          is not there
     *     <LI> Create one action in the testing folder
     *     <LI> The action should be visible in the result of getActions
     *     <LI> After deleting the action from the folder the action should
     *          not be returned from getActions().
     * </OL>
     */
    public void testCreateAndDeleteAction() throws Exception {
        assertEquals("No actions at the start", 0, node.getActions(false).length);
        FileObject test = root;
        
        PCL pcl = new PCL ();
        obj.getLoader ().addPropertyChangeListener (pcl);
        
        FileObject a1 = test.createData("org-openide-actions-PropertiesAction.instance");
        
        pcl.assertEvent (1, "actions");
        
        Action [] res = node.getActions(false);
        assertEquals("There should be exactly one action.", 1, res.length);
        a1.delete();

        pcl.assertEvent (2, "actions");
        assertEquals("No actions after deleting", 0, node.getActions(false).length);
        
        obj.getLoader ().removePropertyChangeListener (pcl);
    }
    
    /**
     * An attempt to create a simple stress test. Just calls
     * the <code>testCreateAndDeleteAction</code> 100 times.
     */
    public void testRepetitiveDeleting() throws Exception {
        for (int i = 0; i < 10; i++) {
            testCreateAndDeleteAction();
        }
    }
    
    /**
     * This test should test behaviour of the getActions method when
     * there is some alien object specified in the configuration folder.
     * The testing object is of type Integer (instead of javax.swing.Action).
     */
    public void testWrongActionObjectInConfig() throws Exception {
        assertEquals("No actions at the start", 0, node.getActions(false).length);
        FileObject test = root;
        FileObject a1 = test.createData("java-lang-String.instance");
        Action [] res = node.getActions(false);
        assertEquals("There should be zero actions.", 0, res.length);        
    }
    
    /**
     * This test checks whether the JSeparator added from the configuration
     * file is reflected in the resulting popup.
     * The tests performs following steps:
     * <OL><LI> Create an instance of ExtensibleNode with folder set to "test"
     *     <LI> No actions should be returned by getActions since the "test" folder
     *          is not there
     *     <LI> Create two actions in the testing folder separated by JSeparator
     *     <LI> getActions should return 3 elements - null element for the separator
     *     <LI> Popup is created from the actions array - the null element
     *              should be replaced by a JSeparator again
     * </OL>
     */
    public void testAddingSeparators() throws Exception {
        Node en1 = node;
        assertEquals("No actions at the start", 0, en1.getActions(false).length);
        FileObject test = root;
        FileObject a1 = test.createData("1[org-openide-actions-PropertiesAction].instance");
        FileObject sep = test.createData("2[javax-swing-JSeparator].instance");
        FileObject a2 = test.createData("3[org-openide-actions-CutAction].instance");
        Action[] actions = en1.getActions(false);
        assertEquals("Actions array should contain 3 elements: "+Arrays.asList(actions), 3, actions.length);
        assertNull("separator should create null element in the array", actions[1]);
        javax.swing.JPopupMenu jp = Utilities.actionsToPopup(actions, Lookups.singleton(en1));
        assertEquals("Popup should contain 3 components", 3, jp.getComponentCount());
        assertTrue("Separator should be second", jp.getComponent(1) instanceof JSeparator);
    }

    /** Test to see whether a compatibility behaviour is still kept. E.g.
     * if one adds actions using DataLoader.setActions they really will be 
     * there.
     */
    public void testCompatibilityIsPropagatedToDisk () throws Exception {
        assertEquals("No actions at the start", Collections.emptyList(), Arrays.asList(node.getActions(false)));
        FileObject test = root;
        
        PCL pcl = new PCL ();
        obj.getLoader ().addPropertyChangeListener (pcl);
        
        obj.getLoader().setActions(new SystemAction[] {
            SystemAction.get(PropertiesAction.class)
        });
        
        pcl.assertEvent (1, "actions");
        
        Action [] res = node.getActions(false);
        assertEquals("There should be exactly one action.", 1, res.length);
        assertEquals("One file created", 1, test.getChildren ().length);
        
        obj.getLoader().setActions(new SystemAction[0]);

        pcl.assertEvent (2, "actions");
        assertEquals("No actions after deleting", 0, node.getActions(false).length);
        
        assertEquals("file disappeared", 0, test.getChildren ().length);
        obj.getLoader ().removePropertyChangeListener (pcl);
    }
    
    /** Test to check that the deserialization of actions is completely ignored.
     */
    public void testNoDeserializationOfActions () throws Exception {
        assertEquals("No actions at the start", 0, node.getActions(false).length);
        FileObject test = root;
        
        PCL pcl = new PCL ();
        obj.getLoader ().addPropertyChangeListener (pcl);
        
        obj.getLoader().setActions(new SystemAction[] {
            SystemAction.get(PropertiesAction.class)
        });
        
        pcl.assertEvent (1, "actions");
        
        Action [] res = node.getActions(false);
        assertEquals("There should be exactly one action.", 1, res.length);
        assertEquals("One file created", 1, test.getChildren ().length);
        
        NbMarshalledObject m = new NbMarshalledObject (obj.getLoader ());
        
        obj.getLoader().setActions(new SystemAction[0]);

        pcl.assertEvent (2, "actions");
        assertEquals("No actions after deleting", 0, node.getActions(false).length);
        
        assertEquals("file disappeared", 0, test.getChildren ().length);
        
        assertEquals ("Loader deserialized", obj.getLoader (), m.get ());
        assertEquals("Still no actions", 0, node.getActions(false).length);
        
        obj.getLoader ().removePropertyChangeListener (pcl);
    }
    
    public void testDefaultActionsUsedWhenCreatedForTheFirstTime () throws Exception {
        SndDL loader = SndDL.getLoader(SndDL.class);
        
        SystemAction[] arr = loader.getActions();
        
        assertEquals (
            "Arrays of actions are the same", 
            Arrays.asList(loader.defaultActions()),
            Arrays.asList(arr)
        );
    }
    
    private static class MyDL extends UniFileLoader {
        public MyDL () {
            super ("org.openide.loaders.DataObject");
            getExtensions ().addExtension ("txt");
        }
        
        /** Returns the name of the folder to read the actions from
         */
        @Override
        protected String actionsContext () {
            return "test";
        }
        
        protected MultiDataObject createMultiObject (FileObject primaryFile) throws DataObjectExistsException, IOException {
            return new MultiDataObject (primaryFile, this);
        }
        
        @Override
        protected SystemAction[] defaultActions() {
            return new SystemAction[0];
        }
        
    } // end of MyDL
    
    private static final class Pool extends DataLoaderPool {
        
        protected Enumeration<? extends DataLoader> loaders() {
            return Enumerations.singleton(DataLoader.getLoader(MyDL.class));
        }
        
    } // end of Pool

    private final class PCL implements PropertyChangeListener {
        int cnt;
        String name;

        public void propertyChange(PropertyChangeEvent ev) {
            name = ev.getPropertyName();
            cnt++;
        }
        
        public void assertEvent (int cnt, String name) {
            obj.getLoader ().waitForActions ();

            if (cnt > this.cnt) {
                fail ("Excepted more changes then we got: expected: " + cnt + " we got: " + this.cnt);
            }
            assertEquals ("same name", name, this.name);
        }
    } // end of PCL
    
    public static final class SndDL extends MyDL {
        public SndDL () {
            getExtensions ().addExtension ("bla");
        }
        
        @Override
        protected SystemAction[] defaultActions() {
            return new SystemAction[] {
                SystemAction.get(CutAction.class),
                null,
                SystemAction.get(CopyAction.class),
                null,
                SystemAction.get(DeleteAction.class),
            };
        }
        
        @Override
        protected String actionsContext() {
            return "2ndtestdir";
        }
        
    }
    
}
