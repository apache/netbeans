/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
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
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2009 Sun Microsystems, Inc.
 */

package org.netbeans.modules.favorites.api;

import java.awt.EventQueue;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import org.netbeans.junit.NbTestCase;
import org.netbeans.junit.RandomlyFails;
import org.netbeans.modules.favorites.FavoritesNode;
import org.netbeans.modules.favorites.RootsTest;
import org.openide.explorer.ExplorerManager;
import static org.junit.Assert.*;
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
        fav.remove(roots.toArray(new FileObject[roots.size()]));
        fav.add(origFavs.toArray(new FileObject[origFavs.size()]));
    }

    public void testBasicCycle() throws Exception {
        assertEquals("Fresh favorites contain only home folder.", 1, origFavs.size());
        File home = new File(System.getProperty("user.home"));
        assertEquals("Fresh favorites contain home folder.", home, FileUtil.toFile(origFavs.get(0)));
        assertTrue("isInFavorites consistent with getFavoriteRoots",
                fav.isInFavorites(FileUtil.toFileObject(home)));
        fav.add(wd);
        assertEquals(2, fav.getFavoriteRoots().size());
        fav.add(jh, file);
        assertEquals(4, fav.getFavoriteRoots().size());
        fav.add(jh, file);  // re-adding already contained roots does nothing
        assertEquals(4, fav.getFavoriteRoots().size());

        // check correct ordering
        List<FileObject> content = Arrays.asList(FileUtil.toFileObject(home), wd, jh, file);
        assertEquals("Favorites remain in the same order", content, fav.getFavoriteRoots());

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
        PropertyChangeListener pcl = new PropertyChangeListener() {

            public void propertyChange(PropertyChangeEvent evt) {
                System.out.println("Property change: " + evt.getPropertyName());
                if (DataObject.Container.PROP_CHILDREN.equals(evt.getPropertyName())) {
                    isDeleted[0] = true;
                }
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
        EventQueue.invokeAndWait(new Runnable() {   // Favorites tab EM refreshed in invokeLater, we have to wait too
            public void run() {
                TopComponent win = RootsTest.getBareFavoritesTabInstance();
                assertNotNull(win);
                assertTrue(win.isOpened());
                assertTrue(fav.isInFavorites(file));
                ExplorerManager man = ((ExplorerManager.Provider) win).getExplorerManager();
                assertNotNull(man);
                Node[] nodes = man.getSelectedNodes();
                assertEquals(1, nodes.length);
                assertEquals(nodes[0].getName(), TEST_TXT);
            }
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
        EventQueue.invokeAndWait(new Runnable() {   // Favorites tab EM refreshed in invokeLater, we have to wait too

            public void run() {
                ExplorerManager man = ((ExplorerManager.Provider) RootsTest.getBareFavoritesTabInstance()).getExplorerManager();
                assertNotNull(man);
                Node[] nodes = man.getSelectedNodes();
                assertEquals(Arrays.toString(nodes), 1, nodes.length);
                assertEquals(TEST_TXT, nodes[0].getName());
            }

        });
    }

}
