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

import java.awt.EventQueue;
import java.awt.GraphicsEnvironment;
import java.beans.PropertyVetoException;
import java.lang.ref.WeakReference;
import javax.swing.JFrame;
import javax.swing.tree.TreeNode;
import junit.framework.Test;
import junit.framework.TestSuite;
import org.netbeans.junit.NbTestCase;
import org.openide.explorer.ExplorerManager;
import org.openide.nodes.*;

/**
 * A test covering JDK issue 6472844 and its NetBeans workaround
 * @author  Petr Nejedly
 */
public final class TreeNodeLeakTest extends NbTestCase {
    
    public static Test suite() {
        return GraphicsEnvironment.isHeadless() ? new TestSuite() : new TestSuite(TreeNodeLeakTest.class);
    }

    private TreeView treeView;
    private ExplorerWindow testWindow;
    private Node toSelect[] = new Node[6];
    
    public TreeNodeLeakTest(String testName) {
        super(testName);
    }

    @Override
    protected int timeOut() {
        return 30000;
    }

    private static Node createNode(String name, Node ... sub) {
        Children ch = Children.LEAF;
        if (sub != null) {
            ch = new Children.Array();
            ch.add(sub);
        }
        AbstractNode n = new AbstractNode(ch);
        n.setName(name);
        return n;
    }
    
    /**
     * @see http://www.netbeans.org/issues/show_bug.cgi?id=84970
     */
    public void testNodesLeak() throws Exception {
        assert !EventQueue.isDispatchThread();
        final Node root = createNode("Root",
            toSelect[0] = createNode("ch1",
                toSelect[1] = createNode("A", (Node[])null),
                toSelect[2] = createNode("B", (Node[])null)),
            toSelect[3] = createNode("ch2",
                toSelect[4] = createNode("A", (Node[])null),
                toSelect[5] = createNode("B", (Node[])null)),
            createNode("ch3",
                createNode("A", (Node[])null),
                createNode("B", (Node[])null))
        );
        EventQueue.invokeAndWait(new Runnable() { public void run() {
            treeView = new BeanTreeView();
            testWindow = new ExplorerWindow();
            testWindow.getContentPane().add(treeView);
            testWindow.pack();
            testWindow.setVisible(true);
            testWindow.getExplorerManager().setRootContext(root);
            try {
                testWindow.getExplorerManager().setSelectedNodes(toSelect);
            } catch (PropertyVetoException pve) {
                fail(pve.getMessage());
            }
        }});
        clearAWTQueue();
        root.getChildren().remove(new Node[] {toSelect[0], toSelect[3]});
        clearAWTQueue();

        WeakReference wr = new WeakReference(toSelect[0]);
        toSelect = null;
        assertGC("Node freed", wr);
    }

    public void testDestroyedNodesAreNotHeldByVisualizers() {
        class K extends Children.Keys<String> {
            @Override
            protected Node[] createNodes(String key) {
                AbstractNode an = new AbstractNode(Children.LEAF);
                an.setName(key);
                return new Node[] { an };
            }

            void keys(String... arr) {
                setKeys(arr);
            }
        }
        K keys = new K();
        AbstractNode root = new AbstractNode(keys);
        keys.keys("A");

        TreeNode v = Visualizer.findVisualizer(root);
        assertEquals("One child", 1, v.getChildCount());
        TreeNode ch0 = v.getChildAt(0);

        Node n0 = Visualizer.findNode(ch0);
        assertNotNull("Node for visualizer " + ch0, n0);
        assertEquals("Name is OK", "A", n0.getName());
        WeakReference wr = new WeakReference(n0);
        n0 = null;

        keys.keys();
        assertGC("Node freed in spite we have a reference to visualizer", wr);
    }
    
    void clearAWTQueue() throws Exception {
        for (int i = 0; i < 2; i++) {
            EventQueue.invokeAndWait(new Runnable() { public void run() {}});
        }
    }
    
    
    private static final class ExplorerWindow extends JFrame
                               implements ExplorerManager.Provider {
        
        private final ExplorerManager explManager = new ExplorerManager();
        
        ExplorerWindow() {
            super("TreeView test");                                     //NOI18N
        }
        
        public ExplorerManager getExplorerManager() {
            return explManager;
        }
        
    }
}
