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
