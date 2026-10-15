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

package org.netbeans.modules.favorites.api;

import java.awt.EventQueue;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import org.netbeans.junit.NbTestCase;
import org.netbeans.junit.RandomlyFails;
import org.netbeans.modules.favorites.FavoritesNode;
import org.netbeans.modules.favorites.RootsTest;
import org.openide.explorer.ExplorerManager;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
import org.openide.nodes.Node;
import org.openide.windows.TopComponent;

/**
 *
 * @author Richard Michalsky
 */
public class FavoritesTest extends NbTestCase {
    private static final String TEST_TXT = "test.txt";
    private Favorites fav;
    private FileObject wd;
    private FileObject jh;
    private FileObject file;
    private List<FileObject> origFavs;

    public FavoritesTest(String name) {
        super(name);
        fav = Favorites.getDefault();
        origFavs = fav.getFavoriteRoots();
    }

    @Override
    public void setUp() throws IOException {
        clearWorkDir();
        wd = FileUtil.toFileObject(getWorkDir());
        jh = FileUtil.toFileObject(new File(System.getProperty("java.home")));
        file = wd.createData(TEST_TXT);
    }

    @Override
    public void tearDown() throws IOException {
        // setup favorites to its initial state
        List<FileObject> roots = fav.getFavoriteRoots();
        fav.remove(roots.toArray(new FileObject[0]));
        fav.add(origFavs.toArray(new FileObject[0]));
    }

    public void testBasicCycle() throws Exception {
        assertEquals("Fresh favorites contain only home folder.", 1, origFavs.size());
        File home = new File(System.getProperty("user.home"));
        assertEquals("Fresh favorites contain home folder.", home, FileUtil.toFile(origFavs.get(0)));
        assertTrue("isInFavorites consistent with getFavoriteRoots",
                fav.isInFavorites(FileUtil.toFileObject(home)));

        fav.add(wd);
        assertEquals(2, fav.getFavoriteRoots().size());

        // this doesn't work: at some point in org.netbeans.modules.favorites.Actions, getChildren() seems to sort alphabetically,
        // which makes order somewhat input dependant (JDK name!). Re-ordering is done too, but this does not influence the children.
        // -> lets add them sequentially for now
        fav.add(jh); fav.add(file); // TODO
//        fav.add(jh, file);
        assertEquals(4, fav.getFavoriteRoots().size());

        fav.add(jh, file);  // re-adding already contained roots does nothing
        assertEquals(4, fav.getFavoriteRoots().size());

        // check content
        List<FileObject> expected = Arrays.asList(FileUtil.toFileObject(home), wd, jh, file);
        assertEquals("unexpected favorites content", new HashSet<>(expected), new HashSet<>(fav.getFavoriteRoots()));

        // check correct ordering
        assertEquals("unexpected favorites order", expected, fav.getFavoriteRoots());

        // check removal of all values
        fav.remove(file);
        assertEquals(3, fav.getFavoriteRoots().size());
        fav.remove(file);   // removing nonexistent root does nothing (even if child of existing root)
        assertEquals(3, fav.getFavoriteRoots().size());
        fav.remove(fav.getFavoriteRoots().toArray(new FileObject[3]));
        assertEquals(0, fav.getFavoriteRoots().size());
    }

    public void testAddRemoveExceptions() throws Exception {
        assertEquals("Favorites are reset to initial state.", 1, fav.getFavoriteRoots().size());
        
        boolean thrown = false;
        try {
            fav.add((FileObject[]) null);
        } catch (NullPointerException e) {
            thrown = true;
        }
        assertTrue("NPE has been thrown", thrown);

        fav.add(new FileObject[0]); // nothing thrown

        thrown = false;
        try {
            fav.add(jh , null);
        } catch (NullPointerException e) {
            thrown = true;
        }
        assertTrue("NPE has been thrown", thrown);

        thrown = false;
        try {
            fav.remove((FileObject[]) null);
        } catch (NullPointerException e) {
            thrown = true;
        }
        assertTrue("NPE has been thrown", thrown);

        fav.add(new FileObject[0]); // nothing thrown

        thrown = false;
        try {
            fav.remove(jh , null);
        } catch (NullPointerException e) {
            thrown = true;
        }
        assertTrue("NPE has been thrown", thrown);
    }

    @RandomlyFails
    public void testExternallyDeletedRoot() throws Exception {
        fav.add(file);
        assertTrue(fav.isInFavorites(file));
        final boolean[] isDeleted = new boolean[] { false };
        PropertyChangeListener pcl = (PropertyChangeEvent evt) -> {
            System.out.println("Property change: " + evt.getPropertyName());
            if (DataObject.Container.PROP_CHILDREN.equals(evt.getPropertyName())) {
                isDeleted[0] = true;
            }
        };
        FavoritesNode.getFolder().addPropertyChangeListener(pcl);
        try {
            file.delete();
            // wait for deletion, cca 10s timeout
            long msCounter = 0;
            while (!isDeleted[0] && msCounter < 10000) {
                Thread.sleep(500);
                msCounter += 500;
            }
            assertTrue("Did not timeout", msCounter < 10000);
            assertFalse(fav.isInFavorites(file));
        } finally {
            FavoritesNode.getFolder().removePropertyChangeListener(pcl);
            // clear broken shadow for test.txt, it interferes with later tests
            fav.clearBrokenShadows();
        }
    }

    @RandomlyFails // NB-Core-Build #3081
    public void testSelectWithAdditionNew() throws Exception {
        RootsTest.clearBareFavoritesTabInstance();
        final TopComponent win = RootsTest.getBareFavoritesTabInstance();
        assertNull(win);
        assertFalse(fav.isInFavorites(file));
        fav.selectWithAddition(file);
        assertFalse(EventQueue.isDispatchThread());
        EventQueue.invokeAndWait(() -> {
            // Favorites tab EM refreshed in invokeLater, we have to wait too
            TopComponent win1 = RootsTest.getBareFavoritesTabInstance();
            assertNotNull(win1);
            assertTrue(win1.isOpened());
            assertTrue(fav.isInFavorites(file));
            ExplorerManager man = ((ExplorerManager.Provider) win1).getExplorerManager();
            assertNotNull(man);
            Node[] nodes = man.getSelectedNodes();
            assertEquals(1, nodes.length);
            assertEquals(nodes[0].getName(), TEST_TXT);
        });
    }

    @RandomlyFails // got empty list of nodes in NB-Core-Build #3603
    public void testSelectWithAdditionExisting() throws Exception {
        RootsTest.clearBareFavoritesTabInstance();
        TopComponent win = RootsTest.getBareFavoritesTabInstance();
        assertNull(win);
        fav.add(file);
        assertTrue(fav.isInFavorites(file));
        fav.selectWithAddition(file);
        win = RootsTest.getBareFavoritesTabInstance();
        assertNotNull(win);
        assertTrue(win.isOpened());
        assertTrue(fav.isInFavorites(file));
        EventQueue.invokeAndWait(() -> {
            // Favorites tab EM refreshed in invokeLater, we have to wait too
            ExplorerManager man = ((ExplorerManager.Provider) RootsTest.getBareFavoritesTabInstance()).getExplorerManager();
            assertNotNull(man);
            Node[] nodes = man.getSelectedNodes();
            assertEquals(Arrays.toString(nodes), 1, nodes.length);
            assertEquals(TEST_TXT, nodes[0].getName());
        });
    }

}
