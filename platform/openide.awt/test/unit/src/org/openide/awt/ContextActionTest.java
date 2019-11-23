/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.openide.awt;

import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.lang.ref.WeakReference;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ActionMap;
import org.netbeans.junit.NbTestCase;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.ContextAwareAction;
import org.openide.util.ContextGlobalProvider;
import org.openide.util.Lookup;
import org.openide.util.Utilities;
import org.openide.util.lookup.AbstractLookup;
import org.openide.util.lookup.InstanceContent;
import org.openide.util.lookup.Lookups;
import org.openide.util.lookup.ProxyLookup;
import org.openide.util.test.MockLookup;

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
    
    private static class CGP implements ContextGlobalProvider, Lookup.Provider {
        private final Lookup prx = Lookups.proxy(this);
        
        volatile Lookup current;
    
        @Override
        public Lookup createGlobalContext() {
            return prx;
        }

        @Override
        public Lookup getLookup() {
            Lookup c = current;
            return c != null ? c : Lookup.EMPTY;
        }
    }
    
    static CGP actionLookup = new CGP();
    
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
        
        actionLookup.current = lookupProxy;
        
        MockLookup.setLookup(Lookups.metaInfServices(getClass().getClassLoader()), 
                lookupProxy, 
                Lookups.fixed(actionLookup));
    }

    @Override
    protected void tearDown() throws Exception {
        actionLookup.current = null;
        super.tearDown(); 
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

    protected boolean getIsChecked(final Action a1) throws InterruptedException, InvocationTargetException {
        assertTrue("In AWT", EventQueue.isDispatchThread());
        
        return Boolean.TRUE.equals(a1.getValue(Action.SELECTED_KEY));
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
    
    private static class TestData {

        private boolean stateValue;

        public static final String PROP_STATEVALUE = "stateValue";

        public boolean isStateValue() {
            return stateValue;
        }

        public void setStateValue(boolean stateValue) {
            boolean oldStateValue = this.stateValue;
            this.stateValue = stateValue;
            propertyChangeSupport.firePropertyChange(PROP_STATEVALUE, oldStateValue, stateValue);
        }

        private transient final PropertyChangeSupport propertyChangeSupport = new PropertyChangeSupport(this);

        public void addPropertyChangeListener(PropertyChangeListener listener) {
            propertyChangeSupport.addPropertyChangeListener(listener);
        }

        public void removePropertyChangeListener(PropertyChangeListener listener) {
            propertyChangeSupport.removePropertyChangeListener(listener);
        }
    }
    
    /**
     * Test action, whose state is driven by a model
     */
    @ActionID(category = "Test", id = "CheckedTest")
    @ActionRegistration(displayName = "Test Action", 
        checkedOn = @ActionState(
            type = TestData.class,
            property = "stateValue"
        )
    )
    public static class CheckAction extends AbstractAction {
        private final TestData data;

        public CheckAction(TestData data) {
            this.data = data;
        }
        
        @Override
        public void actionPerformed(ActionEvent e) {
        }
    }
    
    /**
     * Checks that a stateful action properly changes its state, based
     * on the supplied data.
     * @throws Exception 
     */
    public void testActionGlobalState() throws Exception {
        Action baseA = Actions.forID("Test", "CheckedTest");
        
        // baseAction is not enabled - no data:
        assertFalse("No data, action must be disabled", getIsEnabled(baseA));
        // baseAction is also not checked:
        assertFalse("No data, action cannot be checked", getIsChecked(baseA));

        // now provide the action:
        TestData data = new TestData();
        activate(Lookups.fixed(data));
        
        assertTrue("Data was published, action must enable", getIsEnabled(baseA));
        assertFalse("Data is false, action must be unchedked", getIsChecked(baseA));
        
        data.setStateValue(true);

        assertTrue(getIsEnabled(baseA));
        assertTrue("Data changed to true, action must be checked", getIsChecked(baseA));
    }
    
    public void testActionGlobalStateStartUnchecked() throws Exception {
        TestData data = new TestData();
        
        activate(Lookups.fixed(data));
        Action baseA = Actions.forID("Test", "CheckedTest");
        
        // baseAction is not enabled - no data:
        assertTrue("Action must start up enabled", getIsEnabled(baseA));
        assertFalse("Data is false, action must be unchecked", getIsChecked(baseA));
        
        data.setStateValue(true);
        assertTrue("Data changed to true, action checked", getIsChecked(baseA));
    }

    public void testActionGlobalStateStartChecked() throws Exception {
        TestData data = new TestData();
        data.setStateValue(true);
        
        activate(Lookups.fixed(data));
        Action baseA = Actions.forID("Test", "CheckedTest");
        
        // baseAction is not enabled - no data:
        assertTrue(getIsEnabled(baseA));
        // baseAction is also not checked:
        assertTrue(getIsChecked(baseA));
        
        data.setStateValue(false);
        assertFalse("Data is false, action must be unchecked", getIsChecked(baseA));
    }
    
    public void testContextDelegate() throws Exception {
        TestData data = new TestData();
        data.setStateValue(true);
        
        TestData otherData = new TestData();
        
        
        Action baseA = Actions.forID("Test", "CheckedTest");

        // must change main action Lookup after the main delegate exists,
        // so it gets property change and changes its Action.SELECTED_KEY
        // otherwise the .map is null and action just delegates to Fallback
        activate(Lookups.fixed(data));
        
        assertSame(data, Utilities.actionsGlobalContext().lookup(TestData.class));

        InstanceContent ic = new InstanceContent();
        Lookup context = new AbstractLookup(ic);
        ic.add(data);
        assertTrue(getIsEnabled(baseA));
        assertTrue(getIsChecked(baseA));
        
        Action actionA = ((ContextAwareAction)baseA).createContextAwareInstance(context);
        
        assertTrue(getIsEnabled(actionA));
        assertTrue(getIsChecked(actionA));
        
        // let's have completely different local context:
        Lookup context2 = Lookups.fixed(otherData);
        Action actionB = ((ContextAwareAction)baseA).createContextAwareInstance(context2);
        assertTrue(getIsEnabled(actionB));
        // in this context, action should be enabled, but UNchecked.
        assertFalse(getIsChecked(actionB));

        class PCL implements PropertyChangeListener {
            Set<String> propChanges = Collections.synchronizedSet(new HashSet<>());
            
            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                String n = evt.getPropertyName();
                if (n != null) {
                    propChanges.add(n);
                }
            }
        }
        
        PCL listenerBase = new PCL();
        PCL listenerA = new PCL();
        PCL listenerB = new PCL();
        baseA.addPropertyChangeListener(listenerBase);
        actionA.addPropertyChangeListener(listenerA);
        actionB.addPropertyChangeListener(listenerB);
        
        TestData data3 = new TestData();
        // the data has property false, so actionA should fire & change, not the other ones
        ic.set(Collections.singleton(data3), null);

        // also potentially replans to AWT, so the pending change event from lookup 
        // will be probably processed.
        assertFalse(getIsChecked(actionA));
        assertTrue(getIsChecked(baseA));

        assertTrue(listenerBase.propChanges.isEmpty());
        assertTrue(listenerB.propChanges.isEmpty());
        
        assertTrue(listenerA.propChanges.contains(Action.SELECTED_KEY));
        
        listenerA.propChanges.clear();
        
        otherData.setStateValue(true);

        // again sync with AWT
        assertTrue(getIsChecked(baseA));
        assertTrue(getIsChecked(actionB));

        assertTrue(listenerBase.propChanges.isEmpty());
        assertTrue(listenerA.propChanges.isEmpty());
        
        assertTrue(listenerB.propChanges.contains(Action.SELECTED_KEY));
    }
}
