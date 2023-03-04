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

package org.openide.explorer.view;

import javax.swing.tree.TreeNode;
import org.netbeans.junit.NbTestCase;
import org.netbeans.junit.NbTestSuite;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.nodes.Node;

/**
 * Tests for issue #95364.
 */
public class VisualizerSpeed95364Test extends NbTestCase {
    private int size;
    private TreeNode toCheck;
    private MyKeys chK;
    
    public VisualizerSpeed95364Test(String name) {
        super(name);
    }
    
    public static NbTestSuite suite() {
        return NbTestSuite.speedSuite(
            VisualizerSpeed95364Test.class, /* what tests to run */
            10 /* ten times slower */,
            17 /* try seventeen times if it fails */
        );
    }
    
    @Override
    protected void setUp() {
        size = getTestNumber();
        chK = new MyKeys();
        final AbstractNode root = new AbstractNode(chK);
        root.setName("test root");
        
        final String[] childrenNames = new String[size];
        for (int i = 0; i < size; i++) {
            childrenNames[i] = "test"+i;
        }
        chK.mySetKeys(childrenNames);
        toCheck = Visualizer.findVisualizer(root);
    }
    
    private void doTest() {
        TreeNode tn = null;
        for (int i = 0; i < 100000; i++) {
            tn = toCheck.getChildAt(size/2);
        }
        assertEquals("One node created + 50 at begining", 51, chK.cnt);
    }
    
    public void test100() { doTest(); }
    public void test1000() { doTest(); }
    public void test10000() { doTest(); }
    public void test100000() { doTest(); }
    
    
    private static Node createLeaf(String name) {
        AbstractNode n = new AbstractNode(Children.LEAF);
        n.setName(name);
        return n;
    }
    
    private static final class MyKeys extends Children.Keys<Object> {
        int cnt;
        
        public MyKeys() {
            super(true);
        }
        
        @Override
        protected Node[] createNodes(Object key) {
            cnt++;
            return new Node[] { createLeaf(key.toString())};
        }
        
        public void mySetKeys(Object[] newKeys) {
            super.setKeys(newKeys);
        }
    }
}
