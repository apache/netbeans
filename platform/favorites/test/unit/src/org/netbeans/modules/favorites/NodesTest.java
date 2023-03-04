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
import java.util.Collection;
import javax.swing.Action;
import junit.textui.TestRunner;
import org.netbeans.junit.NbTestCase;
import org.netbeans.junit.NbTestSuite;
import org.openide.modules.ModuleInfo;
import org.openide.nodes.Node;
import org.openide.util.Lookup;

public class NodesTest extends NbTestCase {
    private File userDir, platformDir, clusterDir;

    public NodesTest(String name) {
        super (name);
    }

    public static void main(String[] args) {
        TestRunner.run(new NbTestSuite(NodesTest.class));
    }    
    
    
    @Override
    protected void setUp () throws Exception {
        super.setUp ();
        
        // initialize module system with all modules
        Lookup.getDefault().lookup (
            ModuleInfo.class
        );
    }
    
    public void testNoneOfTheNodesHaveShadowLinks () throws Exception {
        doCheckDepth (FavoritesNode.getNode (), 1);
    }
    
    private void doCheckDepth (Node node, int depth) throws Exception {
        //Limit test to 2 levels
        if (depth > 2) {
            return;
        }
        Node[] arr = node.getChildren().getNodes(true);
        Action add = Actions.add();
        Action remove = Actions.remove();
        
        for (Node arr1 : arr) {
            File f = FavoritesNode.fileForNode(arr1);
            //First level (link) has action remove
            //Further level has action add
            Collection<Action> set = Arrays.asList(arr1.getActions(false));
            if (depth == 1) {
                if (!set.contains (remove)) {
                    fail("Node " + arr1 + " does not contain action remove, but:\n" + set);
                }
                if (set.contains(add)) {
                    fail("Node " + arr1 + " contains action add.");
                }
            } else {
                if (!set.contains(add)) {
                    fail("Node " + arr1 + " does not contain action, but:\n" + set);
                }
                if (set.contains (remove)) {
                    fail("Node " + arr1 + " contains action remove.");
                }
            }
            doCheckDepth(arr1, depth + 1);
        }
    }
}
