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
import junit.framework.Test;
import org.netbeans.jellytools.Bundle;
import org.netbeans.jellytools.FavoritesOperator;
import org.netbeans.jellytools.JellyTestCase;
import org.netbeans.jellytools.nodes.Node;
import org.netbeans.junit.NbModuleSuite;

/**
 *
 * @author Tomas.Musil@sun.com
 */
public class RemoveFromFavoritesTest  extends JellyTestCase {
    
    public RemoveFromFavoritesTest(String testName) {
        super(testName);
    }
    
    public static Test suite() {
        return NbModuleSuite.create(RemoveFromFavoritesTest.class, ".*", ".*");
    }

    @Override
    public void setUp() throws Exception {
        JellyTestCase.closeAllModal();
    }
    
    public void testRemoveFromFavorites(){
        //Opening a favorites tab (or focusing into it)
        FavoritesOperator fo = FavoritesOperator.invoke();
        // Selecting home node in favorites window.
        File f = new File(System.getProperty("user.home"));
        Node node = new Node(fo.tree(), f.getName());
        // selecting Remove from favorites from popup on homeNode
        String removeFromFav = Bundle.getStringTrimmed("org.netbeans.modules.favorites.Bundle","ACT_Remove");
        node.performPopupAction(removeFromFav);
    }
    
}
