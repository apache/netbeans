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
