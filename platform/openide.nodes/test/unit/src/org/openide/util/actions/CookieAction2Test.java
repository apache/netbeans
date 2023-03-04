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

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.util.Arrays;
import javax.swing.Action;
import javax.swing.SwingUtilities;
import javax.swing.event.EventListenerList;
import org.netbeans.junit.MemoryFilter;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.openide.util.NbMutexEventProvider;
import org.openide.cookies.OpenCookie;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.nodes.NodeListener;
import org.openide.util.ContextGlobalProvider;
import org.openide.util.HelpCtx;
import org.openide.util.Lookup;
import org.openide.util.Utilities;
import org.openide.util.lookup.Lookups;
import org.openide.util.lookup.ProxyLookup;

/** Test CookieAction functionality.
 * @author Jesse Glick
 */
public class CookieAction2Test extends NbTestCase {
    
    public CookieAction2Test(String name) {
        super(name);
    }
    
    protected void setUp() {
        System.setProperty("org.openide.util.Lookup", "org.openide.util.actions.CookieAction2Test$Lkp");
        
        assertTrue(Utilities.actionsGlobalContext() instanceof Lkp);
    }
    
    public void testDirectCallToEnabled() throws Exception {
        SimpleCookieAction sca = SystemAction.get(SimpleCookieAction.class);
        assertTrue(sca.enable(new Node[] {new CookieNode()}));
        assertTrue(!sca.enable(new Node[] {}));
        sca.getName();
        assertTrue(sca.enable(new Node[] {new CookieNode()}));
        assertTrue(!sca.enable(new Node[] {}));
    }
    
    public void testChangesOfCookiesPossibleFromNonAWTThreadIssue40937() throws Exception {
        doAWT(true);
    }
    public void testChangesOfCookiesPossibleFromNonAWTThreadWithGlobalActionIssue40937() throws Exception {
        doAWT(false);
    }
    
    public void testNodeListenersDetachedAtFinalizeIssue58065() throws Exception {
        CookieNode node = new CookieNode();
        SimpleCookieAction2 sca = new SimpleCookieAction2();
        Action action = sca.createContextAwareInstance(node.getLookup());
        
        class NodeListenerMemoryFilter implements MemoryFilter {
            public int numofnodelisteners = 0;
            public boolean reject(Object obj) {
                numofnodelisteners += (obj instanceof NodeListener)?1:0;
                return !((obj instanceof EventListenerList) | (obj instanceof Object[]));
            }
        }
        NodeListenerMemoryFilter filter = new NodeListenerMemoryFilter();
        assertSize("",Arrays.asList( new Object[] {node} ),1000000,filter);
        assertTrue("Node is expected to have a NodeListener attached", filter.numofnodelisteners > 0);
        
        Reference actionref = new WeakReference(sca);
        sca = null;
        action = null;
        assertGC("CookieAction is supposed to be GCed", actionref);
        
        NodeListenerMemoryFilter filter2 = new NodeListenerMemoryFilter();
        assertSize("",Arrays.asList( new Object[] {node} ),1000000,filter2);
        assertEquals("Node is expected to have no NodeListener attached", 0, filter2.numofnodelisteners);
    }
    public static class SimpleCookieAction2 extends CookieAction {
        protected int mode() {
            return MODE_EXACTLY_ONE;
        }
        protected Class[] cookieClasses() {
            return new Class[] {OpenCookie.class};
        }
        protected void performAction(Node[] activatedNodes) {
            // do nothing
        }
        public String getName() {
            return "SimpleCookieAction2";
        }
        public HelpCtx getHelpCtx() {
            return null;
        }
    }
    
    private void doAWT(boolean clone) throws Exception {
        assertFalse("We should not run in AWT thread", SwingUtilities.isEventDispatchThread());
        
        SimpleCookieAction sca = SystemAction.get(SimpleCookieAction.class);
        
        CookieNode node = new CookieNode();
        
        
        
        Action action;
        if (clone) {
            action = sca.createContextAwareInstance(node.getLookup());
            Lkp l = (Lkp)Lkp.getDefault();
            l.set(Lookup.EMPTY);
        } else {
            action = sca;
            Lkp l = (Lkp)Lkp.getDefault();
            l.set(node.getLookup());
        }
        
        
        class L implements PropertyChangeListener, Runnable {
            public int cnt;
            public void propertyChange(PropertyChangeEvent ev) {
                cnt++;
                assertTrue("Change delivered in AWT thread", SwingUtilities.isEventDispatchThread());
            }
            public void run() {
                
            }
        }
        L l = new L();
        action.addPropertyChangeListener(l);
        
        assertTrue("Is enabled", action.isEnabled());
        
        
        node.enable(false);
        // just wait for all changes in AWT to be processed
        SwingUtilities.invokeAndWait(l);
        
        assertFalse("Not enabled", action.isEnabled());
        assertEquals("One change", 1, l.cnt);
    }
    public static class SimpleCookieAction extends CookieAction {
        protected int mode() {
            return MODE_EXACTLY_ONE;
        }
        protected Class[] cookieClasses() {
            return new Class[] {OpenCookie.class};
        }
        protected void performAction(Node[] activatedNodes) {
            // do nothing
        }
        public String getName() {
            return "SimpleCookieAction";
        }
        public HelpCtx getHelpCtx() {
            return null;
        }
    }
    
    private static final class CookieNode extends AbstractNode {
        private Open open;
        
        private static final class Open implements OpenCookie {
            public void open() {
                // do nothing
            }
        }
        public CookieNode() {
            super(Children.LEAF);
            open = new Open();
            getCookieSet().add(open);
        }
        
        public void enable(boolean t) {
            if (t) {
                getCookieSet().add(open);
            } else {
                getCookieSet().remove(open);
            }
        }
        
    }
    
    public static final class Lkp extends ProxyLookup
            implements ContextGlobalProvider {
        public Lkp() {
            super(new Lookup[0]);
            set(Lookup.EMPTY);
        }
        
        public void set(Lookup lkp) {
            setLookups(new Lookup[]  {
                lkp,
                Lookups.singleton(new NbMutexEventProvider()),
                Lookups.singleton(this)
            });
        }
        
        public Lookup createGlobalContext() {
            return this;
        }
    }
}
