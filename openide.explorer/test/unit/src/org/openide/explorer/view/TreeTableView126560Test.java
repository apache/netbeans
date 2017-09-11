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
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */

package org.openide.explorer.view;

import java.awt.BorderLayout;
import java.awt.GraphicsEnvironment;
import javax.swing.JFrame;
import javax.swing.SwingUtilities;
import junit.framework.Test;
import junit.framework.TestSuite;
import org.netbeans.junit.NbTestCase;
import org.openide.explorer.ExplorerManager;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.util.Exceptions;
import org.openide.util.RequestProcessor;

/**
 * A test for issue 126560
 * @author Tomas Holy
 */
public class TreeTableView126560Test extends NbTestCase {

    public static Test suite() {
        return GraphicsEnvironment.isHeadless() ? new TestSuite() : new TestSuite(TreeTableView126560Test.class);
    }

    static {
        System.setProperty("sun.awt.exception.handler", "org.openide.explorer.view.TreeTableView126560Test$AWTHandler"); // NOI18N
    }
    public static final class AWTHandler {
        /** The name MUST be handle and MUST be public 
         * @param t the throwable to print
         */
        public static void handle(Throwable t) {
            // Either org.netbeans or org.netbeans.core.execution pkgs:
            if (t.getClass().getName().endsWith(".ExitSecurityException")) { // NOI18N
                return;
            }
            TreeTableView126560Test.exceptionOccurred = true;
        }
    }
    static boolean exceptionOccurred;
    
    public TreeTableView126560Test(String name) {
        super(name);
    }
    
    TTVFrame frame;
    ExplorerManager manager = new ExplorerManager();
    TreeTableView view;
    NodeTableModel nodeTableModel;
    RootTestNode rootNode;

    protected void setUp() throws Exception {
        SwingUtilities.invokeAndWait(new Runnable() {

            public void run() {
                frame = new TTVFrame();
                frame.pack();
                frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
                frame.setVisible(true);
                view.expandAll();
            }
        });
    }
    
    private void clearAwtQueue() throws Exception {
        for (int i = 0; i < 2; i++) {
            SwingUtilities.invokeAndWait(new Runnable() {

                public void run() {
                }
            });
        }
    }
    
    public void testIssue126560() throws Exception {

        class AwtBlock implements Runnable {
            
            synchronized public void run() {
                notifyAll();
                doWait();
            }
            synchronized void doWait() {
                try {
                    wait();
                } catch (InterruptedException ex) {
                    Exceptions.printStackTrace(ex);
                }
            }
            synchronized void block() {
                SwingUtilities.invokeLater(this);
                doWait();
            }
            synchronized void unblock() {
                notify();
            }
        }
        clearAwtQueue();
        
        // clear childrens while awt thread is busy (blocked in test)
        // and set less childrens than node had before and release awt thread
        AwtBlock block = new AwtBlock();
        block.block();
        TestNode n = (TestNode) rootNode.getChildren().getNodes()[0];
        n.doSetChildren(Children.LEAF);
        n.doSetChildren(TestNode.prepareChildren(1, 0));
        block.unblock();
        clearAwtQueue();
        n.getChildren().add(new Node[]{new TestNode(Children.LEAF)});
        clearAwtQueue();
        assertFalse("Exception occurred!", exceptionOccurred);
    }
    
    private final class TTVFrame extends JFrame implements ExplorerManager.Provider {


        private TTVFrame() {

            rootNode = new RootTestNode();
            getRootPane().setLayout(new BorderLayout());
            manager.setRootContext(rootNode);
            nodeTableModel = new NodeTableModel();
            Node[] nodes = rootNode.getChildren().getNodes();
            nodeTableModel.setNodes(nodes);
            view = new TreeTableView(nodeTableModel);
            view.setRootVisible(false);
            getRootPane().add(view, BorderLayout.CENTER);
        }

        public ExplorerManager getExplorerManager() {
            return manager;
        }

    }

    private static class RootTestNode extends AbstractNode {

        RootTestNode() {
            super(TestNode.prepareChildren(1, 2));
        }
    }

    private static class TestNode extends AbstractNode {

        static int cnt;
        private static RequestProcessor requestProcessor;

        static RequestProcessor getRequestProcessor() {
            if (requestProcessor == null) {
                requestProcessor = new RequestProcessor("TestTreeModel", 1);
            }
            return requestProcessor;
        }

        TestNode(Children ch) {
            super(ch);
            this.setName("Test" + cnt++);
        }

        void doSetChildren(final Children ch) {
            /*getRequestProcessor().post(new Runnable() {

                public void run() {
                    setChildren(ch);
                }
                });*/
            setChildren(ch);
        }

        static Children prepareChildren(int nodesCount, int subnodesCount) {
            Children ch = new Children.Array();
            Node[] nodes = new Node[nodesCount];
            for (int i = 0; i < nodes.length; i++) {
                nodes[i] = createNodes(subnodesCount);
            }
            ch.add(nodes);
            return ch;
        }
        static Node createNodes(int subNodesCount) {
            if (subNodesCount == 0) {
                return new TestNode(Children.LEAF);
            }
            Node[] subnodes = new TestNode[subNodesCount];
            for (int i = 0; i < subnodes.length; i++) {
                subnodes[i] = new TestNode(Children.LEAF);
            }
            Children ch = new Children.Array();
            ch.add(subnodes);
            return new TestNode(ch);
        }
    }
}
