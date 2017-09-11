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

package org.openide.explorer.view;

import java.lang.ref.WeakReference;
import java.util.LinkedList;
import java.util.List;
import javax.swing.tree.TreeNode;
import org.netbeans.junit.NbTestCase;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.nodes.Node;

/*
 * Tests for class NodeListModel
 */
public class NodeListModelTest extends NbTestCase {

    private static final int NO_OF_NODES = 20;

    public NodeListModelTest(String name) {
        super(name);
    }

    @Override
    protected boolean runInEQ() {
        return true;
    }
    
    /*
     * Tests whether children of the root node are
     * kept in the memory after the root is passed
     * to the constructor of NodeListModel.
     */
    public void testNodesAreReferenced() {
        
        WeakReference[] tn;
        
        Node c = new AbstractNode(new CNodeChildren());
        NodeListModel model = new NodeListModel(c);
        
        
        
        tn = new WeakReference[model.getSize()];
        for (int i = 0; i < model.getSize(); i++) {
            tn[i] = new WeakReference(model.getElementAt(i));
        }
        
        assertTrue("Need to have more than one child", tn.length > 0);
        
        boolean fail;
        try {
            assertGC("First node should not be gone", tn[0]);
            fail = true;
        } catch (Error err) {
            fail = false;
        }
        if (fail) {
            fail("First node garbage collected!!! " + tn[0].get());
        }
        
        for (int i = 0; i < tn.length; i++) {
            // else fail
            assertNotNull("One of the nodes was gone. Index: " + i, tn[i].get());
        }
    }
    
    /**
     * Tests proper initialization in constructors.
     */
    public void testConstructors() {
        Node c = new AbstractNode(new CNodeChildren());
        NodeListModel model = new NodeListModel(c);
        
        // the following line used to fail if the
        // no parameter costructor does not initialize
        // childrenCount
        model.getSize();
    }

    public void testIsRootIncluded() {
        Node c = new AbstractNode(new CNodeChildren());
        NodeListModel model = new NodeListModel();
        model.setNode(c, true);

        assertEquals(NO_OF_NODES + 1, model.getSize());

        assertNode("Parent is first", c, model.getElementAt(0));
        for (int i= 0; i < NO_OF_NODES; i++) {
            assertNode(i + "th node", c.getChildren().getNodeAt(i), model.getElementAt(i + 1));
        }
    }

    private static void assertNode(String msg, Node n, Object e) {
        TreeNode v = Visualizer.findVisualizer(n);
        assertEquals(msg, v, e);
    }
    
    /*
     * Children for testNodesAreReferenced.
     */
    private static class CNodeChildren extends Children.Keys {
        public CNodeChildren() {
            List myKeys = new LinkedList();
            for (int i = 0; i < NO_OF_NODES; i++) {
                myKeys.add(Integer.valueOf(i));
            }
            
            setKeys(myKeys);
        }
        
        @Override
        protected Node[] createNodes(Object key) {
            AbstractNode an = new AbstractNode(Children.LEAF);
            an.setName(key.toString());
            return  new Node[] { an };
        }
    }
}
