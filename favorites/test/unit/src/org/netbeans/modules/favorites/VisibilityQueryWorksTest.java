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
        for (int i = 0; i < arr.length; i++) {
            err.info("Delete: " + arr[i]);
            arr[i].delete();
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

        VQI vqi = (VQI) Lookup.getDefault().lookup(VQI.class);
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
            boolean ok;
            DataObject in = (DataObject)arr[i].getCookie(DataObject.class);
            
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
        
        public boolean isVisible(FileObject file) {
            if (showAll) {
                return true;
            }
            return file.getPath().indexOf("hidden") == -1;
        }

        
        private final ChangeSupport cs = new ChangeSupport(this);
        public void addChangeListener(ChangeListener l) {
            cs.addChangeListener(l);
        }

        public void removeChangeListener(ChangeListener l) {
            cs.removeChangeListener(l);
        }
        
        public void fire() {
            cs.fireChange();
        }
    }
}
