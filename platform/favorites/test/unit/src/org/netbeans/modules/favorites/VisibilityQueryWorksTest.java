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

package org.netbeans.modules.favorites;

import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.event.ChangeListener;
import org.netbeans.junit.NbTestCase;
import org.netbeans.junit.RandomlyFails;
import org.netbeans.spi.queries.VisibilityQueryImplementation;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.Repository;
import org.openide.loaders.DataFolder;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataShadow;
import org.openide.nodes.Node;
import org.openide.util.ChangeSupport;
import org.openide.util.Lookup;
import org.openide.util.test.MockLookup;

public class VisibilityQueryWorksTest extends NbTestCase {
    private FileObject hiddenFO;
    private FileObject folderFO;
    private FileObject targetFO;
    private FileObject favoritesFO;
    
    private DataObject hiddenDO;
    private DataFolder folderDO;
    private DataFolder targetDO;
    private DataFolder favoritesDO;
    
    private Logger err;

    private DataFolder rootDO;
    
    
    public VisibilityQueryWorksTest(String name) {
        super (name);
    }

    @Override
    protected Level logLevel() {
        return Level.INFO;
    }

    static {
        VQI vqi = new VQI();
        vqi.init();
        Repository our = new Repository(FileUtil.createMemoryFileSystem());
        MockLookup.setInstances(vqi, our);
        assertEquals("We are using our repository", our, Repository.getDefault());
    }
    
    @Override
    protected void setUp () throws Exception {
        clearWorkDir();


        err = Logger.getLogger("TEST." + getName() + "");
        
        err.info("Starting test");
        
        super.setUp ();

        File folder = new File(getWorkDir(), "folder");
        folder.mkdirs();
        this.folderFO = FileUtil.toFileObject(folder);
        assertNotNull("Directory object found", folderFO);

        err.info("folder create");

        File hidden = new File(folder, "a-hidden.txt");
        hidden.createNewFile();
        this.hiddenFO = FileUtil.toFileObject(hidden);
        assertNotNull("File object found", hiddenFO);

        err.info("a-hidden.txt created");

        File target = new File(getWorkDir(), "target");
        target.mkdirs();
        this.targetFO = FileUtil.toFileObject(target);
        assertNotNull("Directory object found", targetFO);

        err.info("target created");

        this.favoritesFO = FileUtil.createFolder (FileUtil.getConfigRoot(), "Favorites");
        assertNotNull("Created favorites folder", this.favoritesFO);
        List<FileObject> children = Arrays.asList(FileUtil.getConfigRoot().getChildren());
        assertEquals("One child: " + children, 1, children.size());

        err.info("Favorites created");

        FileObject[] arr = this.favoritesFO.getChildren();
        for (FileObject arr1 : arr) {
            err.info("Delete: " + arr1);
            arr1.delete();
            err.info("Done");
        }

        this.hiddenDO = DataObject.find(hiddenFO);
        this.folderDO = DataFolder.findFolder(folderFO);
        this.favoritesDO = DataFolder.findFolder(favoritesFO);
        this.targetDO = DataFolder.findFolder(targetFO);
        this.rootDO = DataFolder.findFolder(FileUtil.toFileObject(getWorkDir()));

        err.info("DataObjects created");

        DataObject res;
        res = hiddenDO.createShadow(favoritesDO);
        err.info("shadow created: " + res);
        res = folderDO.createShadow(favoritesDO);
        err.info("shadow created: " + res);
        res = targetDO.createShadow(favoritesDO);
        err.info("shadow created: " + res);
        res = rootDO.createShadow(favoritesDO);
        err.info("shadow created: " + res);

        assertEquals("Four items in favorites", 4, favoritesDO.getChildren().length);
        err.info("Children are ok");
        assertEquals("Four items in node favorites", 4, favoritesDO.getNodeDelegate().getChildren().getNodes(true).length);
        err.info("Nodes are ok");
    }
    
    public void testLinksAreVisibleAllTheTime() throws Exception {
        Node[] arr = FavoritesNode.getNode().getChildren().getNodes(true);
        assertNodeForDataObject("hidden object is there", hiddenDO, true, arr);
        assertNodeForDataObject("folder as well", folderDO, true, arr);
    }

    @RandomlyFails // NB-Core-Build #4913: folder as well in []
    public void testHiddenFilesInFoldersAreHidden() throws Exception {
        Node[] arr = FavoritesNode.getNode().getChildren().getNodes(true);
        Node f = assertNodeForDataObject("folder as well", folderDO, true, arr);
        
        arr = f.getChildren().getNodes(true);
        
        assertNodeForDataObject("hidden object is not there", hiddenDO, false, arr);
        assertEquals("No children at all", 0, arr.length);

        VQI vqi = Lookup.getDefault().lookup(VQI.class);
        vqi.showAll = true;
        vqi.fire();

        // initialize the children
        Node some = f.getChildren().findChild(null);
        assertNotNull("Some node needs to be found", some);
        arr = f.getChildren().getNodes(true);
        assertNodeForDataObject("hidden object is now there", hiddenDO, true, arr);
        assertEquals("One child at all", 1, arr.length);
    }

    /* these tests were created to fix issue 62863, but it is not going 
      to be fixed this way, so leaving commented out...
     
    public void testCopyOfFolderIgnoresHiddenFile() throws Exception {
        doCopyOrCut(true);
    }
    public void testCutOfFolderIgnoresHiddenFile() throws Exception {
        doCopyOrCut(false);
    }
    
    private void doCopyOrCut(boolean copy) throws Exception {
        Node[] arr = FavoritesNode.getNode().getChildren().getNodes(true);
        Node f = assertNodeForDataObject("folder is there ", rootDO, true, arr);
        arr = f.getChildren().getNodes(true);
        f = assertNodeForDataObject("folder is there ", folderDO, true, arr);
        Node t = assertNodeForDataObject("target as well", targetDO, true, arr);
        
        Transferable trans = copy ? f.clipboardCopy() : f.clipboardCut();
        PasteType[] pastes = t.getPasteTypes(trans);
        assertEquals ("One paste", 1, pastes.length);
        
        pastes[0].paste();
        
        arr = t.getChildren().getNodes(true);
        assertEquals("No children at all", 0, arr.length);
        
        Thread.sleep(1000);
        
        assertEquals("No children on loader level", 0, targetDO.getChildren().length);
        assertEquals("No children on fs level", 0, targetDO.getPrimaryFile().getChildren().length);
        assertEquals("No children on disk", 0, FileUtil.toFile(targetDO.getPrimaryFile()).list().length);
    }
     */
    
    /** @return node that contains the data object or null */
    private Node assertNodeForDataObject(String msg, DataObject obj, boolean shouldBeThere, Node[] arr) {
        for (int i = 0; i < arr.length; i++) {
            DataObject in = arr[i].getCookie(DataObject.class);
            
            if (obj == in || ((in instanceof DataShadow) && ((DataShadow)in).getOriginal() == obj)) {
                if (shouldBeThere) {
                    return arr[i];
                } else {
                    fail(msg + " at " + i + " as " + arr[i]);
                }
            }
        }
        
        if (shouldBeThere) {
            fail(msg + " in " + Arrays.asList(arr));
        }
        return null;
    }
    
    private static final class VQI implements VisibilityQueryImplementation {
        
        public void init() {
            showAll = false;
//            listener = null;
        }

        boolean showAll;
        
        @Override
        public boolean isVisible(FileObject file) {
            if (showAll) {
                return true;
            }
            return file.getPath().indexOf("hidden") == -1;
        }

        
        private final ChangeSupport cs = new ChangeSupport(this);
        @Override
        public void addChangeListener(ChangeListener l) {
            cs.addChangeListener(l);
        }

        @Override
        public void removeChangeListener(ChangeListener l) {
            cs.removeChangeListener(l);
        }
        
        public void fire() {
            cs.fireChange();
        }
    }
}
