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

package org.netbeans.modules.viewmodel;

import java.util.ArrayList;
import java.util.Arrays;
import org.netbeans.junit.NbTestCase;
import org.netbeans.spi.viewmodel.Model;
import org.netbeans.spi.viewmodel.ModelEvent;
import org.netbeans.spi.viewmodel.ModelListener;
import org.netbeans.spi.viewmodel.Models;
import org.netbeans.spi.viewmodel.NodeModel;
import org.netbeans.spi.viewmodel.ReorderableTreeModel;
import org.netbeans.spi.viewmodel.TreeModel;
import org.netbeans.spi.viewmodel.UnknownTypeException;
import org.openide.nodes.Index;
import org.openide.nodes.Node;
import org.openide.util.RequestProcessor;

/**
 * Tests that reorderable model produces node with Index implementation
 * 
 * @author Martin Entlicher
 */
public class ReorderableTreeModelTest extends NbTestCase {

    private Node root;

    public ReorderableTreeModelTest (String s) {
        super (s);
    }

    private void setUpModel() {
        Model m = new ReorderableModelImpl();

        ArrayList l = new ArrayList ();
        l.add(m);
        Models.CompoundModel mcm = Models.createCompoundModel(l);
        OutlineTable tt = BasicTest.createView(mcm);
        
        RequestProcessor rp = tt.currentTreeModelRoot.getRootNode().getRequestProcessor();
        BasicTest.waitFinished (rp);

        root = tt.getExplorerManager ().getRootContext ();
    }

    public void testChildrenReorder() {
        setUpModel();

        Index indexImpl = root.getLookup().lookup(Index.class);
        assertNull("Root must not provide Index, root is not reordeable!", indexImpl);

        Node[] ch1 = root.getChildren().getNodes();
        assertEquals(2, ch1.length);

        indexImpl = ch1[1].getLookup().lookup(Index.class);
        assertNull(ch1[1]+ " must not provide Index, it is not reordeable!", indexImpl);
        indexImpl = ch1[0].getLookup().lookup(Index.class);
        assertNotNull(ch1[0]+ " must provide Index, it is reordeable!", indexImpl);

        indexImpl.reorder(new int[] { 2, 0, 1, 4, 3 });
        //                      =>  "3", "1", "2", "5", "4"
        String[] reorderedNames = new String[] { "2", "3", "1", "5", "4" };
        Node[] reorderedNodes = ch1[0].getChildren().getNodes();
        assertEquals(reorderedNames.length, reorderedNodes.length);
        String nodes = "Nodes = "+Arrays.toString(reorderedNodes);
        for (int i = 0; i < reorderedNodes.length; i++) {
            assertEquals(nodes, reorderedNames[i], reorderedNodes[i].getDisplayName());
        }
    }

    private static final class ReorderableModelImpl implements ReorderableTreeModel, NodeModel {

        private static final String CAN_REORDER = "canReorder";
        private static final String CAN_NOT_REORDER = "canNotReorder";

        private final String[] childrenReordered = new String[] { "1", "2", "3", "4", "5" };

        public boolean canReorder(Object parent) throws UnknownTypeException {
            return /*ROOT == parent ||*/ CAN_REORDER.equals(parent);
        }

        public void reorder(Object parent, int[] perm) throws UnknownTypeException {
            //System.err.println("reorder("+parent+", "+Arrays.toString(perm)+")");
            //Thread.dumpStack();
            if (!(/*ROOT == parent ||*/ CAN_REORDER.equals(parent))) {
                throw new IllegalStateException("reorder called on "+parent);
            }
            if (perm.length != childrenReordered.length) {
                throw new IllegalArgumentException("Permutation of length "+perm.length+", but have "+childrenReordered.length+" children.");
            }
            checkPermutation(perm);
            String[] ch = childrenReordered.clone();
            for (int i = 0; i < ch.length; i++) {
                //System.err.println("ch["+perm[i]+"] = "+ch[i]);
                childrenReordered[perm[i]] = ch[i];
            }
        }

        private static void checkPermutation(int[] permutation) throws IllegalArgumentException {
            int max = permutation.length;
            int[] check = new int[max];
            for (int i = 0; i < max; i++) {
                int p = permutation[i];
                if (p >= max) {
                    throw new IllegalArgumentException("Permutation "+Arrays.toString(permutation)+" is not a valid permutation, it contains element "+p+", which is bigger than the length of the permutation.");
                }
                if (p < 0) {
                    throw new IllegalArgumentException("Permutation "+Arrays.toString(permutation)+" is not a valid permutation, it contains element "+p+", which is negative.");
                }
                if (check[p] != 0) {
                    throw new IllegalArgumentException("Permutation "+Arrays.toString(permutation)+" is not a valid permutation, it contains element "+p+" twice or more times.");
                }
                check[p] = 1;
            }
        }

        public Object getRoot() {
            return ROOT;
        }

        public Object[] getChildren(Object parent, int from, int to) throws UnknownTypeException {
            if (ROOT == parent) {
                return new String[] { CAN_REORDER, CAN_NOT_REORDER };
            } else if (CAN_REORDER.equals(parent)) {
                return childrenReordered;
            } else if (CAN_NOT_REORDER.equals(parent)) {
                return new String[] { "a", "b", "c", "d", "e" };
            } else {
                return new Object[] {};
            }
        }

        public boolean isLeaf(Object node) throws UnknownTypeException {
            return ((String) node).length() == 1;
        }

        public int getChildrenCount(Object node) throws UnknownTypeException {
            return Integer.MAX_VALUE;
        }

        public void addModelListener(ModelListener l) {
        }

        public void removeModelListener(ModelListener l) {
        }

        public String getDisplayName(Object node) throws UnknownTypeException {
            return (String) node;
        }

        public String getIconBase(Object node) throws UnknownTypeException {
            return null;
        }

        public String getShortDescription(Object node) throws UnknownTypeException {
            return node+" of "+getClass().getSimpleName();
        }
        
    }
            
}
