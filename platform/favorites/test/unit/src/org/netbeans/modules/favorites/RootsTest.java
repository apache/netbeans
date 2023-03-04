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
import java.util.HashSet;
import junit.textui.TestRunner;
import org.netbeans.junit.NbTestCase;
import org.netbeans.junit.NbTestSuite;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataFolder;
import org.openide.modules.ModuleInfo;
import org.openide.nodes.Node;
import org.openide.util.Lookup;
import org.openide.windows.TopComponent;

public class RootsTest extends NbTestCase {
    private File userDir, platformDir, clusterDir;

    public RootsTest(String name) {
        super (name);
    }
    
    public static void main(String[] args) {
        TestRunner.run(new NbTestSuite(RootsTest.class));
    }    
    
    
    @Override
    protected void setUp () throws Exception {
        super.setUp ();
        
        // initialize module system with all modules
        Lookup.getDefault().lookup (
            ModuleInfo.class
        );
        
/*
        // clear directories first
        this.clearWorkDir();
        
        userDir = new File (getWorkDir(), "user");
        assertTrue (userDir.mkdirs ());
        platformDir = new File (getWorkDir(), "platform");
        assertTrue (platformDir.mkdirs ());
        clusterDir = new File (getWorkDir (), "clstr");
        assertTrue (clusterDir.mkdirs ());
        
        System.setProperty("netbeans.home", platformDir.toString ());
        System.setProperty("netbeans.user", userDir.toString ());
        */
    }
    
    /* UI was changed. There are no more FS roots displayed in Favorites tab */
    /*public void testRootNodeContainsAllFileSystemRoots () throws Exception {
        HashSet roots = new HashSet ();
        {
            File[] arr = File.listRoots();
            for (int i = 0; i < arr.length; i++) {
                roots.add (arr[i]);
            }
        }
        
        Node[] arr = FavoritesNode.getNode ().getChildren ().getNodes (true);
        
        for (int i = 0; i < arr.length; i++) {
            File f = FavoritesNode.fileForNode (arr[i]);
            if (f != null) {
                roots.remove (f);
            }
        }

        if (!roots.isEmpty()) {
            fail (
                "All roots should be children, but these were missing:\n" + 
                roots +
                " this is the list of children nodes:\n" +
                Arrays.asList (arr)
            );
        }
    }*/
    
    public void testLinkToHomeDirIsThere () throws Exception {
        Node[] arr = FavoritesNode.getNode ().getChildren ().getNodes (true);
        
        File home = new File (System.getProperty("user.home")).getCanonicalFile();

        HashSet<DataFolder> folders = new HashSet<>();
        for (Node arr1 : arr) {
            DataFolder f = arr1.getCookie(DataFolder.class);
            if (f == null) continue;
            folders.add (f);
            File file = FileUtil.toFile (
                    f.getPrimaryFile()
            );
            assertNotNull ("All folders have files", file);
            file = file.getCanonicalFile();
            if (file.equals (home)) {
                return;
            }
        }
        
        fail ("none of the folders represent user home: " + home + "\n" + folders);
    }

    
    public void testNodesUnderRootRepresentTheirFiles () throws Exception {
        HashSet<File> roots = new HashSet<>(Arrays.asList (File.listRoots()));
        
        Node[] arr = FavoritesNode.getNode ().getChildren ().getNodes (true);
        
        for (int i = 0; i < arr.length; i++) {
            File f = FavoritesNode.fileForNode (arr[i]);
            if (roots.remove (f)) {
                Node[] nexts = arr[i].getChildren().getNodes (true);
                for (Node next : nexts) {
                    File file = FavoritesNode.fileForNode (nexts[i]);
                    assertNotNull ("For node: " + nexts[i] + " there has to be file", file);
                    assertEquals ("Correct parent for " + nexts[i], f, file.getParentFile());
                }
            }
        }
    }

    // helper method for access to o.n.m.favorites.Tab singleton
    public static TopComponent getBareFavoritesTabInstance() {
        return Tab.DEFAULT;
    }

    // helper method for access to o.n.m.favorites.Tab singleton
    public static void clearBareFavoritesTabInstance() {
        Tab.DEFAULT = null;
    }
}
