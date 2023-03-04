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

package org.openide.util.actions;

import java.awt.event.ActionEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import javax.swing.Action;
import org.netbeans.junit.NbTestCase;
import org.openide.cookies.OpenCookie;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.nodes.CookieSet;
import org.openide.nodes.Node;
import org.openide.util.HelpCtx;
import org.openide.util.Lookup;
import org.openide.util.lookup.AbstractLookup;

/** Test that cookie actions are in fact sensitive to the correct cookies in the
 * correct numbers, and that changes to either node selection or cookies on the
 * selected nodes trigger a change in the selected state.
 * @author Jesse Glick
 */
public class CookieActionTest extends NbTestCase {
    
    static {
        NodeActionsInfraHid.install();
    }

    public CookieActionTest(String name) {
        super(name);
    }
    
    private SystemAction a1;
    private CookieNode n1, n2;
    private Node n3;
    
    @Override
    protected void setUp() throws Exception {
        a1 = SystemAction.get(SimpleCookieAction.class);
        n1 = new CookieNode();
        n1.setName("n1");
        n2 = new CookieNode();
        n2.setName("n2");
        n3 = new AbstractNode(Children.LEAF);
        n3.setName("n3");
        SimpleCookieAction.runOn.clear();
    }
    
    /**
     * in order to run in awt event queue
     * fix for #39789
     */
    protected boolean runInEQ() {
        return true;
    }
    
    /** Similar to NodeActionTest. */
    public void testBasicUsage() throws Exception {
        try {
            // Check enablement logic.
            NodeActionsInfraHid.WaitPCL l = new NodeActionsInfraHid.WaitPCL(NodeAction.PROP_ENABLED);
            a1.addPropertyChangeListener(l);
            assertFalse(a1.isEnabled());
            NodeActionsInfraHid.setCurrentNodes(new Node[] {n1});
            assertTrue(l.changed());
            l.gotit = 0;
            assertTrue(a1.isEnabled());
            NodeActionsInfraHid.setCurrentNodes(new Node[] {n1, n2});
            assertTrue(l.changed());
            l.gotit = 0;
            assertFalse(a1.isEnabled());
            NodeActionsInfraHid.setCurrentNodes(new Node[] {n2});
            assertTrue(l.changed());
            l.gotit = 0;
            assertTrue(a1.isEnabled());
            NodeActionsInfraHid.setCurrentNodes(new Node[] {n3});
            assertTrue(l.changed());
            l.gotit = 0;
            assertFalse(a1.isEnabled());
            NodeActionsInfraHid.setCurrentNodes(new Node[] {n3});
            if (!l.changed()) {
                Thread.sleep(1000);
            }
            l.gotit = 0;
            assertFalse(a1.isEnabled());
            NodeActionsInfraHid.setCurrentNodes(new Node[] {n1});
            assertTrue(l.changed());
            l.gotit = 0;
            assertTrue(a1.isEnabled());
            NodeActionsInfraHid.setCurrentNodes(new Node[] {n1});
            if (!l.changed()) {
                Thread.sleep(1000);
            }
            l.gotit = 0;
            assertTrue(a1.isEnabled());
            NodeActionsInfraHid.setCurrentNodes(new Node[] {n1, n2});
            assertTrue(l.changed());
            l.gotit = 0;
            assertFalse(a1.isEnabled());
        } finally {
            NodeActionsInfraHid.setCurrentNodes(new Node[0]);
            NodeActionsInfraHid.setCurrentNodes(null);
        }
    }
    
    // XXX test advanced cookie modes, multiple cookies, etc.:
    // all combinations of one cookie class vs. two, and any
    // disjunctions of MODE_* constants, against any combination
    // of nodes {n1, n2, n3} (first add a different cookie to n3 and also to n2)
    
    /** Make sure it works to change the cookies on a selected node. */
    public void testChangeCookiesOnNodes() throws Exception {
        NodeActionsInfraHid.WaitPCL l = new NodeActionsInfraHid.WaitPCL(NodeAction.PROP_ENABLED);
        try {
            a1.addPropertyChangeListener(l);
            assertFalse(a1.isEnabled());
            assertTrue(n1.getCookie(OpenCookie.class) != null);
            NodeActionsInfraHid.setCurrentNodes(new Node[] {n1});
            assertTrue("Received PROP_ENABLED on SimpleCookieAction after changing nodes", l.changed());
            l.gotit = 0;
            assertTrue(a1.isEnabled());
            n1.setHasCookie(false);
            assertTrue(l.changed());
            l.gotit = 0;
            assertFalse(a1.isEnabled());
            NodeActionsInfraHid.setCurrentNodes(null);
            if (!l.changed()) {
                Thread.sleep(1000);
            }
            l.gotit = 0;
            assertFalse(a1.isEnabled());
            n1.setHasCookie(true);
            assertTrue(l.changed());
            l.gotit = 0;
            assertTrue(a1.isEnabled());
            n2.setHasCookie(false);
            NodeActionsInfraHid.setCurrentNodes(new Node[] {n2});
            assertTrue(l.changed());
            l.gotit = 0;
            assertFalse(a1.isEnabled());
            n2.setHasCookie(true);
            assertTrue(l.changed());
            l.gotit = 0;
            assertTrue(a1.isEnabled());
            a1.removePropertyChangeListener(l);
            assertTrue(a1.isEnabled());
            n2.setHasCookie(false);
            assertFalse(a1.isEnabled());
            n2.setHasCookie(true);
            assertTrue(a1.isEnabled());
            NodeActionsInfraHid.setCurrentNodes(new Node[] {n1});
            assertTrue(a1.isEnabled());
            Thread.sleep(1000);
            assertTrue(a1.isEnabled());
            n1.setHasCookie(false);
            Thread.sleep(1000);
            assertFalse(a1.isEnabled());
        } finally {
            a1.removePropertyChangeListener(l);
            NodeActionsInfraHid.setCurrentNodes(new Node[0]);
            NodeActionsInfraHid.setCurrentNodes(null);
            n1.setHasCookie(true);
            n2.setHasCookie(true);
        }
    }

    public void testAsynchronousInvocationIsOKOnClone() throws Exception {
        SimpleCookieAction s = SimpleCookieAction.get(AsynchSimpleCookieAction.class);

        CookieNode node = new CookieNode();
        node.setHasCookie(true);
        Action clone = s.createContextAwareInstance(node.getLookup());
        assertTrue("Enabled", clone.isEnabled());
        synchronized (SimpleCookieAction.runOn) {
            assertTrue("Empty now", SimpleCookieAction.runOn.isEmpty());
            clone.actionPerformed(new ActionEvent(this, 0, ""));
            SimpleCookieAction.runOn.wait();
            assertEquals("One list is there", 1, SimpleCookieAction.runOn.size());
            assertEquals("It has one node", 1, SimpleCookieAction.runOn.get(0).size());
            assertEquals("it is our node", node, SimpleCookieAction.runOn.get(0).get(0));
        }
    }
    
    //
    // cloneAction support
    //
    
    public void testNodeActionIsCorrectlyClonned() throws Exception {
        class Counter implements PropertyChangeListener {
            int cnt;
            
            public void propertyChange(PropertyChangeEvent ev) {
                cnt++;
            }
            
            public void assertCnt(String txt, int cnt) {
                assertEquals(txt, cnt, this.cnt);
                this.cnt = 0;
            }
        }
        
        
        SimpleCookieAction s = SimpleCookieAction.get(SimpleCookieAction.class);
        Counter counter = new Counter();
        
        CookieNode node = new CookieNode();
        node.setHasCookie(false);
        
        Action clone = s.createContextAwareInstance(node.getLookup());
        clone.addPropertyChangeListener(counter);
        
        assertTrue("Not enabled", !clone.isEnabled());
        
        node.setHasCookie(true);
        
        assertTrue("Enabled", clone.isEnabled());
        counter.assertCnt("Once change in enabled state", 1);
        
        clone.actionPerformed(new ActionEvent(this, 0, ""));
        
        assertEquals("Has been executed just once: ", 1, SimpleCookieAction.runOn.size());
        Collection c = (Collection)SimpleCookieAction.runOn.iterator().next();
        SimpleCookieAction.runOn.clear();
        assertTrue("Has been executed on mn1", c.contains(node));
        
        
        node.setHasCookie(false);
        assertTrue("Not enabled", !clone.isEnabled());
        counter.assertCnt("One change", 1);
        
        
        WeakReference w = new WeakReference(clone);
        clone = null;
        assertGC("Clone can disappear", w);
    }
    
    
    // #35834
    /** Test of enablement of CookieAction caused creation of that cookie instance in node, which has implemented lookup in 'nice' way.
     * @see #testCookiePrematureCreationInNodeWithDefaultLookup */
    public void testCookiePrematureCreationInNodeWithNiceLookup() {
        SimpleCookieAction2 action = SimpleCookieAction2.get(SimpleCookieAction2.class);
        NodeWithNiceLookup node = new NodeWithNiceLookup();
        
        assertTrue("Node has to be enabled on OpenCookie", action.enable(new Node[] {node})); // NOI18N
        assertFalse("Node may not create OpenCookie instance, when tested on presence only", node.isCookieCreated()); // NOI18N
    }
    
    // #35856
    /** Test of enablement of CookieAction causes creation of that cookie instance in node, which has default lookup.
     * @see #testCookiePrematureCreationInNodeWithNiceLookup */
    public void testCookiePrematureCreationInNodeWithDefaultLookup() {
        SimpleCookieAction2 action = SimpleCookieAction2.get(SimpleCookieAction2.class);
        NodeWithDefaultLookup node = new NodeWithDefaultLookup();
        
        assertTrue("Node has to be enabled on OpenCookie", action.enable(new Node[] {node})); // NOI18N
        assertFalse("Node may not create OpenCookie instance, when tested on presence only", node.isCookieCreated()); // NOI18N
    }
    
    public void testToStringOfDelegateContainsNameOfOriginalAction() throws Exception {
        SimpleCookieAction sa = SimpleCookieAction.get(SimpleCookieAction.class);
        Action a = sa.createContextAwareInstance(Lookup.EMPTY);
        if (a.toString().indexOf("SimpleCookieAction") == -1) {
            fail("We need name of the original action:\n" + a.toString());
        }
    }
    
    public static class SimpleCookieAction extends CookieAction {
        @Override
        protected int mode() {
            return MODE_EXACTLY_ONE;
        }
        @Override
        protected Class[] cookieClasses() {
            return new Class[] {OpenCookie.class};
        }
        public static final List<List<Node>> runOn = new ArrayList<List<Node>>();
        @Override
        protected void performAction(Node[] activatedNodes) {
            synchronized (runOn) {
                runOn.add(Arrays.asList(activatedNodes));
                runOn.notifyAll();
            }
        }
        @Override
        public String getName() {
            return "SimpleCookieAction";
        }
        @Override
        public HelpCtx getHelpCtx() {
            return null;
        }
        @Override
        protected boolean asynchronous() {
            return false;
        }
    }

    public static class AsynchSimpleCookieAction extends SimpleCookieAction {
        @Override
        protected boolean asynchronous() {
            return true;
        }
    }
    
    private static final class CookieNode extends AbstractNode {
        private static final class Open implements OpenCookie {
            public void open() {
                // do nothing
            }
        }
        public CookieNode() {
            super(Children.LEAF);
            getCookieSet().add(new Open());
        }
        public void setHasCookie(boolean b) {
            if (b && getCookie(OpenCookie.class) == null) {
                getCookieSet().add(new Open());
            } else if (!b) {
                OpenCookie o = getCookie(OpenCookie.class);
                if (o != null) {
                    getCookieSet().remove(o);
                }
            }
        }
    }
    
    
    public static class SimpleCookieAction2 extends CookieAction {
        protected int mode() {
            return MODE_EXACTLY_ONE;
        }
        protected Class[] cookieClasses() {
            return new Class[] {OpenCookie.class};
        }
        protected void performAction(Node[] activatedNodes) {
        }
        public String getName() {
            return "SimpleCookieAction2";
        }
        public HelpCtx getHelpCtx() {
            return null;
        }
        protected boolean asynchronous() {
            return false;
        }
    } // End of SimpleCookieAction2.
    
    private static final class NodeWithDefaultLookup extends AbstractNode {
        private static final class Open implements OpenCookie {
            public void open() {
                // Do nothing.
            }
        }
        
        private boolean cookieCreated;
        
        public NodeWithDefaultLookup() {
            super(Children.LEAF);
            getCookieSet().add(OpenCookie.class, new CookieSet.Factory() {
                public Node.Cookie createCookie(Class clazz) {
                    if(clazz.isAssignableFrom(OpenCookie.class)) {
                        synchronized(NodeWithDefaultLookup.this) {
                            NodeWithDefaultLookup.this.cookieCreated = true;
                        }
                        return new Open();
                    }
                    return null;
                }
            });
        }
        
        public synchronized boolean isCookieCreated() {
            return cookieCreated;
        }
    } // End of class NodeWithDefaultLookup.
    
    
    private static class NodeWithNiceLookup extends AbstractNode {
        private static final class Open implements OpenCookie {
            public void open() {
                // Do nothing.
            }
        }
        
        private boolean cookieCreated;
        
        public NodeWithNiceLookup() {
            super(Children.LEAF, new NiceLookup());
        }
        
        public synchronized boolean isCookieCreated() {
            return ((NiceLookup)getLookup()).isInstanceCreated();
        }
        
        private static class NiceLookup extends AbstractLookup {
            private boolean instanceCreated;
            
            private NiceLookup() {
                addPair(new AbstractLookup.Pair() {
                    private Object instance;
                    
                    public boolean creatorOf(Object o) {
                        synchronized(NiceLookup.this) {
                            return o != null && o == instance;
                        }
                    }
                    
                    public boolean instanceOf(Class c) {
                        return c.isAssignableFrom(OpenCookie.class);
                    }
                    
                    public String getDisplayName() {
                        return "OpenCookie item"; // NOI18N XXX
                    }
                    
                    public String getId() {
                        return toString(); // XXX
                    }
                    
                    public Class getType() {
                        return NodeWithNiceLookup.Open.class;
                    }
                    
                    public Object getInstance() {
                        synchronized(NiceLookup.this) {
                            if(instance == null) {
                                instance = new Open();
                                instanceCreated = true;
                            }
                            return instance;
                        }
                    }
                });
            }
            public synchronized boolean isInstanceCreated() {
                return instanceCreated;
            }
        }
        
    } // End of class NodeWithNiceLookup.
    
}

