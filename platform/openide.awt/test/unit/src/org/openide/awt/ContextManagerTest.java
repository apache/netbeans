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
package org.openide.awt;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.lang.ref.WeakReference;
import javax.swing.Action;
import org.netbeans.junit.NbTestCase;
import org.netbeans.junit.RandomlyFails;
import org.openide.util.ContextAwareAction;
import org.openide.util.Lookup;
import org.openide.util.lookup.AbstractLookup;
import org.openide.util.lookup.InstanceContent;

/**
 *
 * @author Jaroslav Tulach <jtulach@netbeans.org>
 */
public class ContextManagerTest extends NbTestCase {
    private AbstractLookup lkp;
    private ContextManager cm;
    
    public ContextManagerTest(String name) {
        super(name);
    }
    
    @Override
    protected boolean runInEQ() {
        return true;
    }
    
    public void testSurviveFocusChange() throws Exception {
        InstanceContent ic = new InstanceContent();
        Lookup lkp = new AbstractLookup(ic);

        Action clone = ((ContextAwareAction) Actions.forID("cat", "survive")).createContextAwareInstance(lkp);
        L listener = new L();
        clone.addPropertyChangeListener(listener);

        assertFalse("Disabled", clone.isEnabled());
        Object val = Integer.valueOf(1);
        ic.add(val);
        assertTrue("Enabled now", clone.isEnabled());
        assertEquals("One change", 1, listener.cnt);
        ic.remove(val);
        assertTrue("Still Enabled", clone.isEnabled());

        Survival.value = 0;
        clone.actionPerformed(new ActionEvent(this, 0, ""));
        assertEquals("Added one", 1, Survival.value);
    }

    public void testListenerGCed () throws Exception {
        InstanceContent ic = new InstanceContent();
        lkp = new AbstractLookup(ic);
        Lookup.Result<Integer> lookupResult = lkp.lookupResult(Integer.class);

        Action action = ((ContextAwareAction) Actions.forID("cat", "survive")).createContextAwareInstance(lkp);
        Action fallbackAction = ((GeneralAction.DelegateAction) action).fallback;
        WeakReference<Action> fallbackActionRef = new WeakReference<Action>(fallbackAction);
        WeakReference<Action> clone = new WeakReference<Action>(action);
        cm = ContextManager.findManager(lkp, true);
        WeakReference lsetRef = new WeakReference<Object>(cm.findLSet(Integer.class));

        action = null;

        assertGC("Action should be GCed", clone);

        fallbackAction = null;

        assertGC("Fallback action should be GCed", fallbackActionRef);
        assertGC("Action LSet Should be GCed", lsetRef);
        lookupResult.allInstances();
    }

    public void testAllResultListenersRemoved () throws Exception {
        InstanceContent ic = new InstanceContent();
        lkp = new AbstractLookup(ic);
        Lookup.Result<Integer> lookupResult = lkp.lookupResult(Integer.class);

        Action action = ((ContextAwareAction) Actions.forID("cat", "survive")).createContextAwareInstance(lkp);
        Action fallbackAction = ((GeneralAction.DelegateAction) action).fallback;
        WeakReference<Action> fallbackActionRef = new WeakReference<Action>(fallbackAction);
        WeakReference<Action> clone = new WeakReference<Action>(action);
        cm = ContextManager.findManager(lkp, true);
        WeakReference<ContextManager.LSet> lsetRef = new WeakReference<ContextManager.LSet>(cm.findLSet(Integer.class));
        WeakReference<Lookup.Result> lookupResultRef = new WeakReference<Lookup.Result>(lsetRef.get().result);

        action = null;

        assertGC("Action should be GCed", clone);

        fallbackAction = null;

        assertGC("Fallback action should be GCed", fallbackActionRef);
        assertGC("Action LSet Should be GCed", lsetRef);
        if (lookupResultRef.get() == lookupResult) {
            // LSet holds ref to the actual real lookup result, nothing to test
        } else {
            // LSet holds ref to a wrapper class NeverEmptyResult, which should have been GCed
            assertGC("NeverEmptyResult should be GCed", lookupResultRef);
        }
    }

    @RandomlyFails
    public void testListenerGCedAfterActionGCed () throws Exception {
        InstanceContent ic = new InstanceContent();
        lkp = new AbstractLookup(ic);
        Lookup.Result<Integer> lookupResult = lkp.lookupResult(Integer.class);

        Action action = ((ContextAwareAction) Actions.forID("cat", "survive")).createContextAwareInstance(lkp);
        Action fallbackAction = ((GeneralAction.DelegateAction) action).fallback;
        WeakReference<Action> fallbackActionRef = new WeakReference<Action>(fallbackAction);
        WeakReference<Action> clone = new WeakReference<Action>(action);
        cm = ContextManager.findManager(lkp, true);
        WeakReference lsetRef = new WeakReference<Object>(cm.findLSet(Integer.class));

        // both delegate and delegating actions are GCed before WeakListenerSupport is triggered in ActiveRefQueue:
        // fallbackAction.removePropertyChangeListener(delegating.weakL);
        fallbackAction = null;
        action = null;
        assertGC("Action should be GCed", clone);

        assertGC("Fallback action should be GCed", fallbackActionRef);
        assertGC("Action LSet Should be GCed", lsetRef);
        lookupResult.allInstances();
    }
    
    private static class L implements PropertyChangeListener {
        int cnt;

        @Override
        public void propertyChange(PropertyChangeEvent evt) {
            cnt++;
        }
    }
    
    @ActionID(category="cat", id="survive")
    @ActionRegistration(displayName="Survive", surviveFocusChange=true)
    public static final class Survival implements ActionListener {
        static int value;
        
        private Integer context;

        public Survival(Integer context) {
            this.context = context;
        }

        @Override
        public void actionPerformed(ActionEvent ae) {
            value += context;
        }
    }
}
