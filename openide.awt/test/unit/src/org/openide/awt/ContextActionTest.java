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

package org.openide.awt;

import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.lang.ref.WeakReference;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ActionMap;
import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertTrue;
import org.netbeans.junit.NbTestCase;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.ContextAwareAction;
import org.openide.util.Lookup;
import org.openide.util.lookup.AbstractLookup;
import org.openide.util.lookup.InstanceContent;
import org.openide.util.lookup.Lookups;
import org.openide.util.lookup.ProxyLookup;

/** Test that cookie actions are in fact sensitive to the correct cookies in the
 * correct numbers, and that changes to either node selection or cookies on the
 * selected nodes trigger a change in the selected state.
 * @author Jesse Glick
 */
public class ContextActionTest extends NbTestCase 
implements Lookup.Provider, ContextActionEnabler<ContextActionTest.Openable> {
    
    public ContextActionTest(String name) {
        super(name);
    }
    
    private Lookup lookup;
    private Lookup lookupProxy;
    
    private ContextAwareAction a1, a2, any, each, all;
    private LookupWithOpenable n1, n2;
    private Lookup n3, n4;
    
    
    private int expectedEnabledmentCount = 2;
    
    @Override
    protected void setUp() throws Exception {
        lookup = Lookup.EMPTY;
        lookupProxy = Lookups.proxy(this);
        
        a1 = context(new SimpleCookieAction(), null, ContextSelection.EXACTLY_ONE, lookupProxy, Openable.class);
        a2 = context(new SimpleCookieAction(), this, ContextSelection.ANY, lookupProxy, Openable.class);
        any = context(new SimpleCookieAction(), null, ContextSelection.ANY, lookupProxy, Openable.class);
        each = context(new SimpleCookieAction(), null, ContextSelection.EACH, lookupProxy, Openable.class);
        all = context(new SimpleCookieAction(), null, ContextSelection.ALL, lookupProxy, Openable.class);
        n1 = new LookupWithOpenable();
        n2 = new LookupWithOpenable();
        n3 = new LookupWithOpenable(false);
        n4 = new LookupWithOpenable(n1.lookup(Openable.class)); // share the same cookie instance with n1
        
        SimpleCookieAction.runOn.clear();
    }
    
    @Override
    protected boolean runInEQ() {
        return true;
    }
    
    public void testFilterOutDuplicates() throws Exception {
        // Check enablement logic.
        ActionsInfraHid.WaitPCL l = new ActionsInfraHid.WaitPCL("enabled");
        a1.addPropertyChangeListener(l);
        assertFalse(getIsEnabled(a1));
        final int[] cnt = { 0 };
        class O implements Openable {
            @Override
            public void open() {
                cnt[0]++;
            }
        }
        O o = new O();
        activate(Lookups.fixed(o, o));
        assertTrue(l.changed());
        l.gotit = 0;
        assertTrue(getIsEnabled(a1));
        
        doActionPerformed(a1, new ActionEvent(this, 0, ""));
        
        assertEquals("One invocation", 1, cnt[0]);
    }
    
    /** Similar to NodeActionTest. */
    public void testBasicUsage() throws Exception {
        // Check enablement logic.
        ActionsInfraHid.WaitPCL l = new ActionsInfraHid.WaitPCL("enabled");
        a1.addPropertyChangeListener(l);
        assertFalse(getIsEnabled(a1));
        activate(new Lookup[] {n1});
        assertTrue(l.changed());
        l.gotit = 0;
        assertTrue(getIsEnabled(a1));
        activate(new Lookup[] {n1, n2});
        assertTrue(l.changed());
        l.gotit = 0;
        assertFalse(getIsEnabled(a1));
        activate(new Lookup[] {n2});
        assertTrue(l.changed());
        l.gotit = 0;
        assertTrue(getIsEnabled(a1));
        activate(new Lookup[] {n3});
        assertTrue(l.changed());
        l.gotit = 0;
        assertFalse(getIsEnabled(a1));
        activate(new Lookup[] {n3});
        assertFalse(l.changed());
        l.gotit = 0;
        assertFalse(getIsEnabled(a1));
        activate(new Lookup[] {n1});
        assertTrue(l.changed());
        l.gotit = 0;
        assertTrue(getIsEnabled(a1));
        activate(new Lookup[] {n1});
        assertFalse("No change", l.changed());
        l.gotit = 0;
        assertTrue(getIsEnabled(a1));
        activate(new Lookup[] {n1, n2});
        assertTrue(l.changed());
        l.gotit = 0;
        assertFalse(getIsEnabled(a1));
        activate(new Lookup[] {n1, n4});
        assertTrue("Change generated as we are now enabled", l.changed());
        l.gotit = 0;
        assertTrue("Open in n1 and n4 is the same, still gets enabled", getIsEnabled(a1));
    }
    
    // XXX test advanced cookie modes, multiple cookies, etc.:
    // all combinations of one cookie class vs. two, and any
    // disjunctions of MODE_* constants, against any combination
    // of nodes {n1, n2, n3} (first add a different cookie to n3 and also to n2)
    
    /** Make sure it works to change the cookies on a selected node. */
    public void testChangeCookiesOnNodes() throws Exception {
        ActionsInfraHid.WaitPCL l = new ActionsInfraHid.WaitPCL("enabled");
        assertFalse(getIsEnabled(a1));
        assertTrue(n1.lookup(Openable.class) != null);
        a1.addPropertyChangeListener(l);
        activate(new Lookup[] {n1});
        assertTrue("Received PROP_ENABLED on SimpleCookieAction after changing nodes", l.changed());
        l.gotit = 0;
        assertTrue(getIsEnabled(a1));
        n1.setHasCookie(false);
        assertTrue(l.changed());
        l.gotit = 0;
        assertFalse(getIsEnabled(a1));
        activate();
        assertFalse("No change in enablement", l.changed());
        l.gotit = 0;
        assertFalse(getIsEnabled(a1));
        activate(n1);
        n1.setHasCookie(true);
        assertTrue(l.changed());
        l.gotit = 0;
        assertTrue(getIsEnabled(a1));
        n2.setHasCookie(false);
        activate(new Lookup[] {n2});
        assertTrue(l.changed());
        l.gotit = 0;
        assertFalse(getIsEnabled(a1));
        n2.setHasCookie(true);
        assertTrue(l.changed());
        l.gotit = 0;
        assertTrue(getIsEnabled(a1));
        a1.removePropertyChangeListener(l);
        assertTrue(getIsEnabled(a1));
        n2.setHasCookie(false);
        assertFalse(getIsEnabled(a1));
        n2.setHasCookie(true);
        assertTrue(getIsEnabled(a1));
        activate(new Lookup[] {n1});
        assertTrue(getIsEnabled(a1));
        assertTrue(getIsEnabled(a1));
        n1.setHasCookie(false);
        assertFalse(getIsEnabled(a1));
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
        
        
        Counter counter = new Counter();
        
        LookupWithOpenable node = new LookupWithOpenable();
        node.setHasCookie(false);
        
        Action clone = a1.createContextAwareInstance(node);
        clone.addPropertyChangeListener(counter);
        
        assertTrue("Not enabled", !getIsEnabled(clone));
        
        node.setHasCookie(true);
        
        assertTrue("Enabled", getIsEnabled(clone));
        counter.assertCnt("Once change in enabled state", 1);
        
        doActionPerformed(clone, new ActionEvent(this, 0, ""));
        
        assertEquals("Has been executed just once: ", 1, SimpleCookieAction.runOn.size());
        Collection c = (Collection)SimpleCookieAction.runOn.iterator().next();
        SimpleCookieAction.runOn.clear();
        assertTrue("Has been executed on mn1", c.contains(node.lookup(Openable.class)));
        
        
        node.setHasCookie(false);
        assertTrue("Not enabled", !getIsEnabled(clone));
        counter.assertCnt("One change", 1);
        
        
        WeakReference<?> w = new WeakReference<Object>(clone);
        clone = null;
        assertGC("Clone can disappear", w);
    }

    public void testSelectModeAnyBasicUsage() throws Exception {
        ActionsInfraHid.WaitPCL l = new ActionsInfraHid.WaitPCL("enabled");
        any.addPropertyChangeListener(l);
        assertFalse(getIsEnabled(any));
        activate(new Lookup[] {n1});
        assertTrue(l.changed());
        l.gotit = 0;
        assertTrue(getIsEnabled(any));
        activate(new Lookup[] {n1, n2});
        assertFalse("No change as it was enabled before", l.changed());
        l.gotit = 0;
        assertTrue(getIsEnabled(any));
        n2.setHasCookie(false);
        assertTrue(getIsEnabled(any));
        assertFalse("No change, still on", l.changed());
        n1.setHasCookie(false);
        assertTrue(l.changed());
        l.gotit = 0;
        assertFalse("Now it is disabled as both of the actions are", getIsEnabled(any));
        activate(new Lookup[] {n3});
        assertFalse("No change still", l.changed());
        l.gotit = 0;
        assertFalse(getIsEnabled(any));
        n1.setHasCookie(true);
        activate(n1);
        assertTrue(l.changed());
        l.gotit = 0;
        assertTrue("Now it is enabled", getIsEnabled(any));
    }
    
    public void testSelectModeEachBasicUsage() throws Exception {
        ActionsInfraHid.WaitPCL l = new ActionsInfraHid.WaitPCL("enabled");
        each.addPropertyChangeListener(l);
        assertFalse(getIsEnabled(each));
        activate(new Lookup[] {n1});
        assertTrue(l.changed());
        l.gotit = 0;
        assertTrue(getIsEnabled(each));
        activate(new Lookup[] {n1, n2});
        assertFalse("No change as it was enabled before", l.changed());
        l.gotit = 0;
        assertTrue(getIsEnabled(each));
        n2.setHasCookie(false);
        assertFalse("No longer enabled, we need all", getIsEnabled(each));
        assertTrue("The change is on", l.changed());
        l.gotit = 0;
        n1.setHasCookie(false);
        assertFalse("No change now", l.changed());
        assertFalse("Now it is disabled as both of the actions are", getIsEnabled(each));
        activate(new Lookup[] {n3});
        assertFalse("No change still", l.changed());
        l.gotit = 0;
        assertFalse(getIsEnabled(each));
        n1.setHasCookie(true);
        activate(n1);
        assertTrue(l.changed());
        l.gotit = 0;
        assertTrue("Now it is enabled", getIsEnabled(each));
        
        class O implements Openable {
            public int cnt;
            public void open() {
                cnt++;
            }
        }
        O o = new O();
        n2.setHasCookie(true);
        n2.ic.add(o);
        activate(n2, n3);
        assertTrue("Going to disabled state", l.changed());
        l.gotit = 0;
        assertFalse("Not enabled as one node has two cookies", getIsEnabled(each));
        activate(n2);
        assertFalse("No change, probably", l.changed());
        l.gotit = 0;
        assertFalse("Not enabled as one node has two cookies", getIsEnabled(each));
        
        n2.ic.remove(o);
        
        assertTrue("Enabled again", l.changed());
        l.gotit = 0;
        assertTrue("On", getIsEnabled(each));
    }

    public void testSelectModeAllBasicUsage() throws Exception {
        ActionsInfraHid.WaitPCL l = new ActionsInfraHid.WaitPCL("enabled");
        all.addPropertyChangeListener(l);
        assertFalse(getIsEnabled(all));
        activate(new Lookup[] {n1});
        assertTrue(l.changed());
        l.gotit = 0;
        assertTrue(getIsEnabled(all));
        activate(new Lookup[] {n1, n2});
        assertFalse("No change as it was enabled before", l.changed());
        l.gotit = 0;
        assertTrue(getIsEnabled(all));
        n2.setHasCookie(false);
        assertFalse("No longer enabled, we need all", getIsEnabled(all));
        assertTrue("The change is on", l.changed());
        l.gotit = 0;
        n1.setHasCookie(false);
        assertFalse("No change now", l.changed());
        assertFalse("Now it is disabled as both of the actions are", getIsEnabled(all));
        activate(new Lookup[] {n3});
        assertFalse("No change still", l.changed());
        l.gotit = 0;
        assertFalse(getIsEnabled(all));
        n1.setHasCookie(true);
        activate(n1);
        assertTrue(l.changed());
        l.gotit = 0;
        assertTrue("Now it is enabled", getIsEnabled(all));
        
        class O implements Openable {
            public int cnt;
            public void open() {
                cnt++;
            }
        }
        O o = new O();
        n2.setHasCookie(true);
        n2.ic.add(o);
        activate(n2, n3);
        assertTrue("Going to disabled state", l.changed());
        l.gotit = 0;
        assertFalse("Not enabled as one node has two cookies", getIsEnabled(all));
        activate(n2);
        assertTrue("No change, probably", l.changed());
        l.gotit = 0;
        assertTrue("Enabled as one node can have more cookies", getIsEnabled(all));
        
        n2.ic.remove(o);
        
        assertFalse("No change", l.changed());
        l.gotit = 0;
        assertTrue("Still On", getIsEnabled(all));
    }
    
    public void testContextXMLDefinition() throws Exception {
        FileObject folder;
        folder = FileUtil.getConfigFile("actions/support/test");
        assertNotNull("testing layer is loaded: ", folder);

        FileObject fo = folder.getFileObject("testContext.instance");
        
        Object obj = fo.getAttribute("instanceCreate");
        if (!(obj instanceof ContextAwareAction)) {
            fail("Shall create an action: " + obj);
        }
        ContextAwareAction caa = (ContextAwareAction)obj;
        Action action = caa.createContextAwareInstance(lookupProxy);

        assertEquals("Both actions are equal", action, caa);
        assertEquals("and have the same hash", action.hashCode(), caa.hashCode());
        
        
        class SimpleAction extends AbstractAction {
            public int cnt;
            
            public void actionPerformed(ActionEvent e) {
                cnt++;
            }
        }
        SimpleAction simpleAction = new SimpleAction();
        
        ActionMap map = new ActionMap();
        
        
        LookupWithOpenable openLookup = new LookupWithOpenable();
        
        activate(openLookup);
        assertTrue("Our action is enabled", this.getIsEnabled(action));
        openLookup.setHasCookie(false);
        assertFalse("Our action is not enabled", this.getIsEnabled(action));

        activate(openLookup, Lookups.singleton(map));
        assertFalse("Still disabled", this.getIsEnabled(action));
        map.put("contextKey", simpleAction);
        assertTrue("Now enabled", this.getIsEnabled(action));
        
        doActionPerformed(action, new ActionEvent(this, 0, ""));
        assertEquals("simple action invoked", 1, simpleAction.cnt);
        
        openLookup.setHasCookie(true);
        assertTrue("Still enabled", this.getIsEnabled(action));

        doActionPerformed(action, new ActionEvent(this, 0, ""));
        assertEquals("simple action invoked again", 2, simpleAction.cnt);
        
        activate(openLookup, Lookups.singleton(new ActionMap()));
        assertTrue("Yet enabled", this.getIsEnabled(action));
        doActionPerformed(action, new ActionEvent(this, 0, ""));

        assertEquals("Our SimpleCookieAction invoked", 1, SimpleCookieAction.runOn.size());
        List<? extends Openable> open = SimpleCookieAction.runOn.get(0);
        assertEquals("Our SimpleCookieAction invoked", 1, open.size());
        assertSame("the right instance", openLookup.lookup(Openable.class), open.get(0));
        
        String n = (String)action.getValue(Action.NAME);
        assertEquals("Open", n);
    }
    public void testContextXMLDefinitionNoKey() throws Exception {
        FileObject folder;
        folder = FileUtil.getConfigFile("actions/support/test");
        assertNotNull("testing layer is loaded: ", folder);

        FileObject fo = folder.getFileObject("testContextNoKey.instance");

        Object obj = fo.getAttribute("instanceCreate");
        if (!(obj instanceof ContextAwareAction)) {
            fail("Shall create an action: " + obj);
        }
        ContextAwareAction caa = (ContextAwareAction)obj;
        Action action = caa.createContextAwareInstance(lookupProxy);

        assertEquals("Both actions are equal", action, caa);
        assertEquals("and have the same hash", action.hashCode(), caa.hashCode());


        class SimpleAction extends AbstractAction {
            public int cnt;

            public void actionPerformed(ActionEvent e) {
                cnt++;
            }
        }
        SimpleAction simpleAction = new SimpleAction();

        ActionMap map = new ActionMap();


        LookupWithOpenable openLookup = new LookupWithOpenable();

        activate(openLookup);
        assertTrue("Our action is enabled", this.getIsEnabled(action));
        openLookup.setHasCookie(false);
        assertFalse("Our action is not enabled", this.getIsEnabled(action));

        activate(openLookup, Lookups.singleton(map));
        assertFalse("Still disabled", this.getIsEnabled(action));
        map.put("contextKey", simpleAction);
        assertFalse("Action does not react to any key", this.getIsEnabled(action));

        openLookup.setHasCookie(true);
        assertTrue("Still enabled", this.getIsEnabled(action));

        doActionPerformed(action, new ActionEvent(this, 0, ""));
        assertEquals("no meaning in simple action", 0, simpleAction.cnt);
        assertEquals("Our SimpleCookieAction invoked", 1, SimpleCookieAction.runOn.size());
        List<? extends Openable> open = SimpleCookieAction.runOn.get(0);
        assertEquals("Our SimpleCookieAction invoked", 1, open.size());
        assertSame("the right instance", openLookup.lookup(Openable.class), open.get(0));

        String n = (String)action.getValue(Action.NAME);
        assertEquals("Open", n);
    }
    
    public void testBasicUsageWithEnabler() throws Exception {
        ActionsInfraHid.WaitPCL l = doBasicUsageWithEnabler(a2);
        
        expectedEnabledmentCount = 1;
        // api to rescan the enablement state
        try {
            a2.getValue("enabler");
        } catch (AssertionError err) {
            if (!EventQueue.isDispatchThread()) {
                // ok, it is expected that the "enabler" API can be invoked
                // only from AWT thread, for now
                return;
            }
        }
        
        assertTrue("change in the enabled state", l.changed());
        l.gotit = 0;
        assertFalse("no longer enabled, even no change in lookup happened", getIsEnabled(a2));
    }
    
    public void testBasicUsageWithEnablerFromLayer() throws Exception {
        FileObject folder;
        folder = FileUtil.getConfigFile("actions/support/test");
        assertNotNull("testing layer is loaded: ", folder);

        FileObject fo = folder.getFileObject("testContextEnabler.instance");
        
        Object obj = fo.getAttribute("instanceCreate");
        if (!(obj instanceof ContextAwareAction)) {
            fail("Shall create an action: " + obj);
        }
        ContextAwareAction caa = (ContextAwareAction)obj;
        Action action = caa.createContextAwareInstance(lookupProxy);
        
        
        doBasicUsageWithEnabler(action);
    }

    static URL myIconResource() {
        return ContextAwareAction.class.getResource("TestIcon.png");
    }
    
    private ActionsInfraHid.WaitPCL doBasicUsageWithEnabler(Action operateOn) throws Exception {
        // Check enablement logic.
        ActionsInfraHid.WaitPCL l = new ActionsInfraHid.WaitPCL("enabled");
        operateOn.addPropertyChangeListener(l);
        assertFalse(getIsEnabled(operateOn));
        activate(new Lookup[] {n1});
        assertFalse("We need two nodes to become enabled", l.changed());
        l.gotit = 0;
        assertFalse("and there is just one", getIsEnabled(operateOn));
        activate(new Lookup[] {n1, n2});
        assertTrue("Ok, now we are enabled", l.changed());
        l.gotit = 0;
        assertTrue("Yes", getIsEnabled(operateOn));
        activate(new Lookup[] {n2});
        assertTrue("Disabled again", l.changed());
        l.gotit = 0;
        assertFalse("Disabled", getIsEnabled(operateOn));
        activate(new Lookup[] {n3});
        assertFalse(l.changed());
        l.gotit = 0;
        assertFalse(getIsEnabled(operateOn));
        activate(new Lookup[] {n3});
        assertFalse("Again not changed", l.changed());
        l.gotit = 0;
        assertFalse(getIsEnabled(operateOn));
        activate(new Lookup[] {n1});
        assertFalse(l.changed());
        l.gotit = 0;
        assertFalse(getIsEnabled(operateOn));
        activate(new Lookup[] {n1});
        assertFalse("No change", l.changed());
        l.gotit = 0;
        assertFalse(getIsEnabled(operateOn));
        activate(new Lookup[] {n1, n2});
        assertTrue("now there is enabledment", l.changed());
        l.gotit = 0;
        assertTrue(getIsEnabled(operateOn));
        
        return l;
    }
    
    public Lookup getLookup() {
        return lookup;
    }

    private void activate(Lookup... lkps) {
        if (lkps.length == 1) {
            lookup = lkps[0];
        } else if (lkps.length == 0) {
            lookup = Lookup.EMPTY;
        } else {
            lookup = new ProxyLookup(lkps);
        }
        // refresh
        lookupProxy.lookup(Object.class);
    }

    protected boolean getIsEnabled(final Action a1) throws InterruptedException, InvocationTargetException {
        assertTrue("In AWT", EventQueue.isDispatchThread());
        
        return a1.isEnabled();
    }
    protected void doActionPerformed(final Action a1, final ActionEvent ev) throws InterruptedException, InvocationTargetException {
        assertTrue("In AWT", EventQueue.isDispatchThread());
        a1.actionPerformed(ev);
    }

    public boolean enabled(List<? extends Openable> data) {
        return data.size() == expectedEnabledmentCount;
    }
    
    static ContextActionEnabler<?> getEnabler() {
        return new ContextActionTest("");
    }

    private static <T> ContextAwareAction context(
        ContextActionPerformer<T> a, ContextActionEnabler<T> e, ContextSelection s, Lookup lookupProxy, Class<T> c
    ) {
        ContextAction.Performer<T> perf = new ContextAction.Performer<T>(a, e);
        return GeneralAction.context(perf, s, lookupProxy, c);
    }
    
    public static interface Openable {
        public void open();
    }
    
    
    public static class SimpleCookieAction implements ContextActionPerformer<Openable> {
        
        public void actionPerformed(ActionEvent ev, List<? extends Openable> toOpen) {
            runOn.add(toOpen);
            for (Openable o : toOpen) {
                o.open();
            }
        }
        public static final List<List<? extends Openable>> runOn = new ArrayList<List<? extends Openable>>();
    }
    
    private static final class LookupWithOpenable extends AbstractLookup 
    implements Lookup.Provider {
        private InstanceContent ic;
        private static final class Open implements Openable {
            public void open() {
                // do nothing
            }
        }
        public LookupWithOpenable() {
            this(true);
        }
        public LookupWithOpenable(boolean add) {
            this(new InstanceContent(), add ? new Open() : null);
        }
        public LookupWithOpenable(Openable open) {
            this(new InstanceContent(), open);
        }
        private LookupWithOpenable(InstanceContent ic, Openable open) {
            super(ic);
            this.ic = ic;
            if (open != null) {
                ic.add(open);
            }
            ic.add(this);
        }
        public void setHasCookie(boolean b) {
            if (b && lookup(Openable.class) == null) {
                ic.add(new Open());
            } else if (!b) {
                Openable o = lookup(Openable.class);
                if (o != null) {
                    ic.remove(o);
                }
            }
        }

        public Lookup getLookup() {
            return this;
        }
    }
    
    
}
