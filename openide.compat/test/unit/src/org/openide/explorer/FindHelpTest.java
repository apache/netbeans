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
