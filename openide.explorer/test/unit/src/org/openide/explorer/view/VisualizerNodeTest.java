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

import java.awt.Image;
import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.util.Collections;
import java.util.List;
import javax.swing.Icon;
import javax.swing.tree.TreeNode;
import org.netbeans.junit.NbTestCase;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.nodes.FilterNode;
import org.openide.nodes.Node;
import org.openide.util.ImageUtilities;

/** VisualizerNode tests, mostly based on reported bugs.
 */
public class VisualizerNodeTest extends NbTestCase {
    {
        System.setProperty("org.openide.explorer.VisualizerNode.prefetchCount", "0");
    }

    public VisualizerNodeTest(String name) {
        super(name);
    }

    @Override
    protected boolean runInEQ() {
        return true;
    }

    public void testIconIsProvidedEvenTheNodeIsBrokenIssue46727() {
        final boolean[] arr = new boolean[1];
        
        AbstractNode a = new AbstractNode(Children.LEAF) {
            @Override
            public Image getIcon(int type) {
                arr[0] = true;
                return null;
            }
        };
        
        VisualizerNode v = VisualizerNode.getVisualizer(null, a);
        assertNotNull("Visualizer node", v);
        
        Icon icon = v.getIcon(false, false);
        assertNotNull("Cannot be null even the node's icon is null", icon);
        assertTrue("getIcon called", arr[0]);
    }
    
    public void testIndexOfProvidesResultsEvenIfTheVisualizerIsComputedViaDifferentMeans() throws Exception {
        AbstractNode a = new AbstractNode(new Children.Array());
        AbstractNode m = new AbstractNode(Children.LEAF);
        a.getChildren().add(new Node[] { Node.EMPTY.cloneNode(), m, Node.EMPTY.cloneNode() });
        
        TreeNode ta = Visualizer.findVisualizer(a);
        TreeNode tm = Visualizer.findVisualizer(m);
        
        assertEquals("Index is 1", 1, ta.getIndex(tm));
    }
    
    public void testIconsAreShared() {
        AbstractNode a1 = new AbstractNode(Children.LEAF);
        VisualizerNode v1 = VisualizerNode.getVisualizer(null, a1);
        Icon icon1 = v1.getIcon(false, false);
        
        AbstractNode a2 = new AbstractNode(Children.LEAF);
        VisualizerNode v2 = VisualizerNode.getVisualizer(null, a2);
        Icon icon2 = v2.getIcon(false, false);
        
        assertSame("Icon instances should be same", icon1, icon2);
    }
    
    public void testLazyVisGet() throws Exception {
        LazyChildren lch = new LazyChildren();
        AbstractNode a = new AbstractNode(lch);
        
        TreeNode ta = Visualizer.findVisualizer(a);
        
        assertEquals("Child check", "c", ta.getChildAt(2).toString());
        assertEquals("Counter should be 1", 1, lch.cnt);
    }

    public void testLazyVisDestroy() throws Exception {
        LazyChildren lch = new LazyChildren();
        AbstractNode a = new AbstractNode(lch);
        List<String> arr = Collections.nCopies(100, "A");
        lch.keys(arr.toArray(new String[0]));

        TreeNode ta = Visualizer.findVisualizer(a);
        final TreeNode snd = ta.getChildAt(2);

        Reference<Node> sndNode = new WeakReference<Node>(Visualizer.findNode(snd));

        assertEquals("Child check", "A", snd.toString());
        assertEquals("Counter should be 1", 1, lch.cnt);

        a.destroy();

        assertGC("Whole subtree under a can be GCed now", sndNode);
    }
    
    public void testLazyFilterGet() throws Exception {
        LazyChildren lch = new LazyChildren();
        AbstractNode a = new AbstractNode(lch);
        FilterNode fnode = new FilterNode(a);
        
        TreeNode ta = Visualizer.findVisualizer(fnode);
        
        assertEquals("Child check", "c", ta.getChildAt(2).toString());
        assertEquals("Counter should be 1", 1, lch.cnt);

        VisualizerNode vn = (VisualizerNode)ta.getChildAt(2);
        String msg = ((VisualizerNode)ta).getChildren().dumpIndexes(vn);
        if (msg.indexOf("'c'") == -1) {
            fail("Missing note about visualizer node 'c': " + msg);
        }
    }

    public void testAddingJavaAndFormAtTheEndOfExistingFolder() throws Exception {
        LazyChildren lch = new LazyChildren();
        AbstractNode a = new AbstractNode(lch);

        TreeNode ta = Visualizer.findVisualizer(a);

        assertEquals("Child check", "c", ta.getChildAt(2).toString());
        assertEquals("Counter should be 1", 1, lch.cnt);

        assertEquals("Child check", "b", ta.getChildAt(1).toString());
        assertEquals("Counter should be 2", 2, lch.cnt);

        assertEquals("Child check", "a", ta.getChildAt(0).toString());
        assertEquals("Counter should be all", 3, lch.cnt);

        lch.keys("a", "b", "c", "x", "-x");
        
        assertEquals("Counter should still be 3", 3, lch.cnt);
        assertEquals("Size is 5", 5, ta.getChildCount());
        
        lch.keys("a", "b", "c", "x", "-x");
        
        assertEquals("Counter should still be 3", 3, lch.cnt);
        assertEquals("Size is 5", 5, ta.getChildCount());

        assertTrue("Child is empty", isDummyNode(ta.getChildAt(4)));
        assertEquals("We have still 5 children, no opportunity to update", 5, ta.getChildCount());
        assertEquals("Three nodes created, still", 3, lch.cnt);
        
        assertEquals("x Child check", "x", ta.getChildAt(3).toString());
        
        lch.keys("a", "b", "c", "x", "-x", "-y", "y");

        assertEquals("No time to update, should be 5", 5, ta.getChildCount());
        assertTrue("Nothing removed, -x still present", isDummyNode(ta.getChildAt(4)));
    }

    public void testVisualizerChildrenGC() throws Exception {
        LazyChildren ch = new LazyChildren();
        AbstractNode a = new AbstractNode(ch);
        VisualizerNode vn = (VisualizerNode) Visualizer.findVisualizer(a);
        VisualizerChildren vch = vn.getChildren();
        WeakReference<VisualizerChildren> ref = new WeakReference<VisualizerChildren>(vch);
        vch = null;
        boolean gced = true;
        try {
            assertGC("", ref);
        } catch (Error e) {
            gced = false;
        }
        if (gced) {
            fail("VisualizerChildren should not be GCed.");
        }
        TreeNode child = vn.getChildAt(0);

        gced = true;
        try {
            assertGC("", ref);
        } catch (Error e) {
            gced = false;
        }
        if (gced) {
            fail("VisualizerChildren should not be GCed.");
        }

        child = null;
        assertGC("VisualizerChildren should be GCed", ref);
    }

    public void testNodeToolTip() {
        AbstractNode n = new AbstractNode(Children.LEAF) {



            @Override
            public Image getIcon(int type) {
                return ImageUtilities.assignToolTipToImage(super.getIcon(type), "test");
            }

        };
        VisualizerNode vn = (VisualizerNode) Visualizer.findVisualizer(n);
        assertEquals(vn.getShortDescription(), "<html><br>test</html>");
    }

    public void testNodeHtmlDescription() {
        AbstractNode n = new AbstractNode(Children.LEAF) {

            @Override
            public Image getIcon(int type) {
                return ImageUtilities.assignToolTipToImage(super.getIcon(type), "test");
            }

            @Override
            public String getShortDescription () {
                return "<html><strong>my description</strong><br>another description</html>";
            }

        };
        VisualizerNode vn = (VisualizerNode) Visualizer.findVisualizer(n);
        assertEquals(vn.getShortDescription(), "<html><strong>my description</strong><br>another description<br>test</html>");
    }

    static boolean isDummyNode(TreeNode visNode) {
        Node node = ((VisualizerNode)(visNode)).node;
        return node.getClass().getName().endsWith("EntrySupportLazy$DummyNode");
    }
    
    static class LazyChildren extends Children.Keys<String> {
        public LazyChildren() {
            super(true);
            setKeys(new String[] {"a", "b", "c"});
        }
        int cnt;
        @Override
        protected Node[] createNodes(String key) {
            if (key.startsWith("-")) {
                return null;
            }

            AbstractNode node = new AbstractNode(LEAF);
            node.setName(key);
            cnt++;
            return new Node[] {node};
        }

        public void keys(String... arr) {
            super.setKeys(arr);
        }
    }
}
