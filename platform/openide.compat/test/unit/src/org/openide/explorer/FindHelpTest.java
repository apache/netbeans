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

package org.openide.explorer;

import org.openide.nodes.Node;
import org.openide.nodes.Children;
import org.openide.nodes.AbstractNode;
import java.util.Collections;
import java.util.Arrays;
import java.awt.BorderLayout;
import java.awt.GraphicsEnvironment;
import org.openide.explorer.view.BeanTreeView;
import javax.swing.JLabel;
import junit.framework.Test;
import junit.framework.TestSuite;
import org.netbeans.junit.NbTestCase;
import org.openide.util.HelpCtx;

/** Test finding help IDs on explorers.
 * @author Jesse Glick
 * @see "#14701"
 */
public class FindHelpTest extends NbTestCase {

    public static Test suite() {
        return GraphicsEnvironment.isHeadless() ? new TestSuite() : new TestSuite(FindHelpTest.class);
    }

    public FindHelpTest(String name) {
        super(name);
    }
    
    private static Node[] nodes;
    private static Node root;

    @Override
    protected boolean runInEQ() {
        return true;
    }
    
    protected void setUp() throws Exception {
        Children kids = new Children.Array();
        nodes = new Node[] {
            new NoHelpNode(),
            new WithHelpNode("foo"),
            new WithHelpNode("bar"),
            new WithHelpNode("foo"),
        };
        kids.add(nodes);
        root = new AbstractNode(kids);
    }
    
    public void testFindHelpOnExplorer() throws Exception {
        ExplorerPanel p = new ExplorerPanel();
        ExplorerManager m = p.getExplorerManager();
        m.setRootContext(root);
        assertEquals(Collections.EMPTY_LIST, Arrays.asList(m.getSelectedNodes()));
        HelpCtx base = new HelpCtx(ExplorerPanel.class);
        assertEquals(base, p.getHelpCtx());
        m.setSelectedNodes(new Node[] {nodes[0]});
        assertEquals(base, p.getHelpCtx());
        m.setSelectedNodes(new Node[] {nodes[1]});
        assertEquals(new HelpCtx("foo"), p.getHelpCtx());
        m.setSelectedNodes(new Node[] {nodes[1], nodes[2]});
        assertEquals(base, p.getHelpCtx());
        m.setSelectedNodes(new Node[] {nodes[1], nodes[3]});
        assertEquals(new HelpCtx("foo"), p.getHelpCtx());
        p = new WithHelpExplorer();
        m = p.getExplorerManager();
        m.setRootContext(root);
        assertEquals(Collections.EMPTY_LIST, Arrays.asList(m.getSelectedNodes()));
        base = new HelpCtx("base");
        assertEquals(base, p.getHelpCtx());
        m.setSelectedNodes(new Node[] {nodes[0]});
        assertEquals(base, p.getHelpCtx());
        m.setSelectedNodes(new Node[] {nodes[1]});
        assertEquals(new HelpCtx("foo"), p.getHelpCtx());
        m.setSelectedNodes(new Node[] {nodes[1], nodes[2]});
        assertEquals(base, p.getHelpCtx());
        m.setSelectedNodes(new Node[] {nodes[1], nodes[3]});
        assertEquals(new HelpCtx("foo"), p.getHelpCtx());
    }
    
    public void testFindHelpHierarchically() throws Exception {
        ExplorerPanel p = new ExplorerPanel();
        p.setLayout(new BorderLayout());
        BeanTreeView b = new BeanTreeView();
        p.add(b, BorderLayout.CENTER);
        JLabel l1 = new JLabel("test1");
        HelpCtx.setHelpIDString(l1, "test");
        p.add(l1, BorderLayout.NORTH);
        JLabel l2 = new JLabel("test2");
        assertEquals(HelpCtx.DEFAULT_HELP, HelpCtx.findHelp(l2));
        p.add(l2, BorderLayout.SOUTH);
        ExplorerManager m = p.getExplorerManager();
        m.setRootContext(root);
        assertEquals(Collections.EMPTY_LIST, Arrays.asList(m.getSelectedNodes()));
        HelpCtx base = new HelpCtx(ExplorerPanel.class);
        assertEquals(base, HelpCtx.findHelp(b));
        m.setSelectedNodes(new Node[] {nodes[0]});
        assertEquals(base, HelpCtx.findHelp(b));
        m.setSelectedNodes(new Node[] {nodes[1]});
        assertEquals(new HelpCtx("foo"), HelpCtx.findHelp(b));
        m.setSelectedNodes(new Node[] {nodes[1], nodes[2]});
        assertEquals(base, HelpCtx.findHelp(b));
        m.setSelectedNodes(new Node[] {nodes[1], nodes[3]});
        assertEquals(new HelpCtx("foo"), HelpCtx.findHelp(b));
        assertEquals(new HelpCtx("foo"), HelpCtx.findHelp(p));
        assertEquals(new HelpCtx("test"), HelpCtx.findHelp(l1));
        assertEquals(new HelpCtx("foo"), HelpCtx.findHelp(l2));
    }
    
    private static final class NoHelpNode extends AbstractNode {
        public NoHelpNode() {
            super(Children.LEAF);
        }
    }
    
    private static final class WithHelpNode extends AbstractNode {
        private final String id;
        public WithHelpNode(String id) {
            super(Children.LEAF);
            this.id = id;
        }
        public HelpCtx getHelpCtx() {
            return new HelpCtx(id);
        }
    }
    
    private static final class WithHelpExplorer extends ExplorerPanel {
        public HelpCtx getHelpCtx() {
            return getHelpCtx(getExplorerManager().getSelectedNodes(), new HelpCtx("base"));
        }
    }
    
}
