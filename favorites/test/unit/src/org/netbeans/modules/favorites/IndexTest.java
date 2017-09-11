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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
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

import org.netbeans.junit.NbTestCase;
import org.netbeans.junit.RandomlyFails;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataFolder;
import org.openide.loaders.DataObject;
import org.openide.modules.ModuleInfo;
import org.openide.nodes.Index;
import org.openide.nodes.Node;
import org.openide.util.Lookup;

public class IndexTest extends NbTestCase {

    public IndexTest(String name) {
        super (name);
    }
    
    @Override
    protected void setUp () throws Exception {
        super.setUp ();
        
        // initialize module system with all modules
        Lookup.getDefault().lookup (
            ModuleInfo.class
        );
    }
    
    /**
     * Test basic functionality of Index on FavoritesNode node.
     */
    @RandomlyFails
    public void testReorder () throws Exception {
        FileObject folder = FileUtil.createFolder (
            FileUtil.getConfigRoot(),
            "FavoritesTest"
        );
        FileObject fo1 = FileUtil.createData(folder,"Test1");
        FileObject fo2 = FileUtil.createData(folder,"Test2");
        
        DataObject dObj1 = DataObject.find(fo1);
        DataObject dObj2 = DataObject.find(fo2);
        
        DataFolder favorites = FavoritesNode.getFolder();
        
        dObj1.createShadow(favorites);
        dObj2.createShadow(favorites);
        
        Node n = FavoritesNode.getNode();
        
        Node n1 = n.getChildren().findChild("Test1");
        assertNotNull("Node must exist", n1);
        Node n2 = n.getChildren().findChild("Test2");
        assertNotNull("Node must exist", n2);
        
        Index ind = n.getCookie(Index.class);
        assertNotNull("Index must exist", ind);
        
        int i;
        i = ind.indexOf(n1);
        assertEquals("Node index must be 1", i, 1);
        i = ind.indexOf(n2);
        assertEquals("Node index must be 2", i, 2);
        
        ind.reorder(new int [] {0,2,1});
        
        i = ind.indexOf(n1);
        assertEquals("Node index must be 2", i, 2);
        i = ind.indexOf(n2);
        assertEquals("Node index must be 1", i, 1);
    }
    
}
