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

        HashSet<DataFolder> folders = new HashSet<DataFolder>();
        for (int i = 0; i < arr.length; i++) {
            DataFolder f = arr[i].getCookie(DataFolder.class);
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
        HashSet<File> roots = new HashSet<File>(Arrays.asList (File.listRoots()));
        
        Node[] arr = FavoritesNode.getNode ().getChildren ().getNodes (true);
        
        for (int i = 0; i < arr.length; i++) {
            File f = FavoritesNode.fileForNode (arr[i]);
            if (roots.remove (f)) {
                Node[] nexts = arr[i].getChildren().getNodes (true);
                for (int j = 0; j < nexts.length; j++) {
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
