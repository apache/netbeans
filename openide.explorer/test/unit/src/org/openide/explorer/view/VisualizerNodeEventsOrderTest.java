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
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */

package org.openide.explorer.view;

import java.lang.reflect.InvocationTargetException;
import javax.swing.SwingUtilities;
import javax.swing.tree.TreeNode;
import org.netbeans.junit.NbTestCase;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.util.Exceptions;

/**
 *
 * @author Holy
 */
public class VisualizerNodeEventsOrderTest extends NbTestCase {

    {
        System.setProperty("org.openide.explorer.VisualizerNode.prefetchCount", "0");
    }

    public VisualizerNodeEventsOrderTest(String name) {
        super(name);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        lch = new LazyChildren();
        a = new AbstractNode(lch);
        ta = Visualizer.findVisualizer(a);     
    }

    LazyChildren lch;
    AbstractNode a;
    TreeNode ta;

    public void testAddingJavaAndFormAtTheEndOfExistingFolder() throws Exception {

        assertEquals("Child check", "c", getChildAt(2).toString());
        assertEquals("Counter should be 1", 1, lch.cnt);

        assertEquals("Child check", "b", getChildAt(1).toString());
        assertEquals("Counter should be 2", 2, lch.cnt);

        assertEquals("Child check", "a", getChildAt(0).toString());
        assertEquals("Counter should be all", 3, lch.cnt);

        lch.keys("a", "b", "c", "x", "-x");
        
        assertEquals("Counter should still be 3", 3, lch.cnt);
        assertEquals("Size is 5", 5, getChildCount());
        
        lch.keys("a", "b", "c", "x", "-x");
        
        assertEquals("Counter should still be 3", 3, lch.cnt);
        assertEquals("Size is 5", 5, getChildCount());

        assertTrue("Child is empty", isDummyNode(getChildAt(4)));
        waitForAwtQueue();
        assertEquals("We have just four children", 4, getChildCount());
        assertEquals("Three nodes created, still", 3, lch.cnt);
        
        assertEquals("x Child check", "x", getChildAt(3).toString());
        
        lch.keys("a", "b", "c", "x", "-x", "-y", "y");

        waitForAwtQueue();
        assertEquals("Should be 6", 6, getChildCount());
        assertTrue("-y still presents", isDummyNode(getChildAt(4)));
        assertEquals("Now y should be 4th", "y", getChildAt(4).toString());
    }
    
    public void testOrderOfEvents() throws Exception {

        assertEquals("Child check", "c", getChildAt(2).toString());
        assertEquals("Counter should be 1", 1, lch.cnt);

        assertEquals("Child check", "b", getChildAt(1).toString());
        assertEquals("Counter should be 2", 2, lch.cnt);

        assertEquals("Child check", "a", getChildAt(0).toString());
        assertEquals("Counter should be all", 3, lch.cnt);
        lch.keys("a", "b", "c", "x", "-x");

        // block AWT thread
        Block b = new Block();
        b.block();
        
        // this invokes remove node event, which should be delayed
        invokeGetChildAt(4);
        
        // this invokes add event, which should be after delayed remove event
        lch.keys("a", "b", "c", "x", "-x", "-y", "y");
        
        assertEquals("Counter should still be 3", 3, lch.cnt);
        assertEquals("Size should be still 5, awt is still blocked", 5, ta.getChildCount());
        
        // unblock awt
        b.unblock();
        waitForAwtQueue();
        assertEquals("Size should be 6", 6, getChildCount());

        assertTrue("Child is empty", isDummyNode(getChildAt(4)));
        waitForAwtQueue();
        assertEquals("We should have just five children now", 5, getChildCount());
        assertEquals("Child check", "a", getChildAt(0).toString());
        assertEquals("Child check", "b", getChildAt(1).toString());
        assertEquals("Child check", "c", getChildAt(2).toString());
        assertEquals("Child check", "x", getChildAt(3).toString());
        assertEquals("Child check", "y", getChildAt(4).toString());
    }

    Throwable e = null;
    public void testForToStrictAssertsInVisualizerChildren() throws InterruptedException, InvocationTargetException {
        // block AWT thread
        Block b = new Block();
        b.block();
        Node n = lch.getNodeAt(0);
        final TreeNode tn = Visualizer.findVisualizer(n);
        SwingUtilities.invokeLater(new Runnable() {

            public void run() {
                try {
                    int idx = ta.getIndex(tn);
                } catch (Throwable ex) {
                    e = ex;
                }
            }
        });
        lch.keys("x", "y", "y");
        b.unblock();
        waitForAwtQueue();
        if (e != null) {
            fail();
        }
    }

    void invokeGetChildAt(final int pos) {
        SwingUtilities.invokeLater(new Runnable() {

            public void run() {
                ta.getChildAt(pos);
            }
        });
    }
    
    TreeNode getChildAt(int pos) throws InterruptedException, InvocationTargetException {
        class GetChild implements Runnable {
            TreeNode visNode;
            int pos;

            public GetChild(int pos) {
                this.pos = pos;
            }
            
            public void run() {
                visNode = ta.getChildAt(pos);
            }
        }
        GetChild gch = new GetChild(pos);
        SwingUtilities.invokeAndWait(gch);
        return gch.visNode;
    }
    
    int getChildCount() throws InterruptedException, InvocationTargetException {
        class GetChild implements Runnable {
            int count;
            
            public void run() {
                count = ta.getChildCount();
            }
        }
        GetChild gch = new GetChild();
        SwingUtilities.invokeAndWait(gch);
        return gch.count;
    }
    
    void waitForAwtQueue() throws InterruptedException, InvocationTargetException {
        SwingUtilities.invokeAndWait(new Runnable() {

            public void run() {
            }
        });
    }
    
    
    final boolean isDummyNode(TreeNode visNode) {
        return VisualizerNodeTest.isDummyNode(visNode);
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
    
    class Block implements
            Runnable {

        public synchronized void run() {
            notify();
            try {
                wait();
            } catch (InterruptedException ex) {
                Exceptions.printStackTrace(ex);
            }
        }
        synchronized void block() {
            SwingUtilities.invokeLater(this);
            try {
                wait();
            } catch (InterruptedException ex) {
                Exceptions.printStackTrace(ex);
            }
        }
        synchronized void unblock() {
            notifyAll();
        }
    }
}
