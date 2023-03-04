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
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ActionMap;
import javax.swing.text.DefaultEditorKit;
import org.netbeans.junit.NbTestCase;
import org.openide.util.ContextAwareAction;
import org.openide.util.Lookup;
import org.openide.util.Utilities;
import org.openide.util.lookup.AbstractLookup;
import org.openide.util.lookup.InstanceContent;
import org.openide.util.lookup.Lookups;

/** Copied from org.openide.util.actions and modified to work on 
 * new Actions Support.
 */
public class CallbackSystemActionTest extends NbTestCase {
    private Logger LOG;
    
    
    public CallbackSystemActionTest(String name) {
        super(name);
    }
    
    @Override
    protected void setUp() throws Exception {
        LOG = Logger.getLogger("TEST-" + getName());
        
        LOG.info("setUp");
    }
    
    @Override
    protected void tearDown() throws Exception {
        LOG.info("tearDown");
        super.tearDown();
        LOG.info("tearDown super finished");
    }

    @Override
    protected Level logLevel() {
        return Level.FINE;
    }

    @Override
    protected int timeOut() {
        return 5000;
    }
    
    @Override
    protected boolean runInEQ() {
        return true;
    }
    
    public void testPropertyChangeListenersDetachedAtFinalizeIssue58100() throws Exception {
        
        class MyAction extends AbstractAction {
            public void actionPerformed(ActionEvent ev) {
            }
        }
        MyAction action = new MyAction();
        ActionMap map = new ActionMap();
        
        map.put("key", action);
        Lookup context = Lookups.singleton(map);
        ContextAwareAction systemaction = CallbackActionTest.callback("key", null, Utilities.actionsGlobalContext(), false);
        Action delegateaction = systemaction.createContextAwareInstance(context);
        
        assertEquals("Action is expected to have no listener", 0, action.getPropertyChangeListeners().length);
        
        Reference<Object> actionref = new WeakReference<Object>(systemaction);
        systemaction = null;
        delegateaction = null;
        assertGC("CallbackSystemAction is supposed to be GCed", actionref);
        
        assertEquals(
            "Action is expected to have no PropertyChangeListener attached:\n" + 
                Arrays.asList(action.getPropertyChangeListeners())
            , 0, 
            action.getPropertyChangeListeners().length
        );
    }
    
    public void testSurviveFocusChangeInTheNewWay() throws Exception {
        doSurviveFocusChangeInTheNewWay(false);
    }
    
    public void testSurviveFocusChangeInTheNewWayEvenActionIsGCed() throws Exception {
        doSurviveFocusChangeInTheNewWay(true);
    }
    
    private void doSurviveFocusChangeInTheNewWay(boolean doGC) throws Exception {
        class MyAction extends AbstractAction {
            public int cntEnabled;
            public int cntPerformed;
            
            @Override
            public boolean isEnabled() {
                cntEnabled++;
                return true;
            }
            
            public void actionPerformed(ActionEvent ev) {
                cntPerformed++;
            }
        }
        class Disabled extends AbstractAction {
            @Override
            public boolean isEnabled() {
                return false;
            }
            @Override
            public void actionPerformed(ActionEvent e) {
                throw new UnsupportedOperationException("Not supported yet.");
            }
        }
        MyAction myAction = new MyAction();
        
        ActionMap other = new ActionMap();
        ActionMap tc = new ActionMap();
        ActionMap disabled = new ActionMap();
        disabled.put("somekey", new Disabled());
        
        InstanceContent ic = new InstanceContent();
        AbstractLookup al = new AbstractLookup(ic);
        
        ContextAwareAction a = CallbackActionTest.callback("somekey", null, al, true);
        tc.put("somekey", myAction);
        
        ic.add(other);
        assertFalse("Disabled on other component", a.isEnabled());
        ic.remove(other);
        ic.add(tc);
        assertTrue("MyAction is enabled", a.isEnabled());
        assertEquals("isEnabled called once", 1, myAction.cntEnabled);

        if (doGC) {
            WeakReference<?> ref = new WeakReference<Object>(a);
            a = null;
            assertGC("Action can disappear", ref);
            a = CallbackActionTest.callback("somekey", null, al, true);
        }

        ic.remove(tc);
        ic.add(other);
        assertTrue("Remains enabled", a.isEnabled());
        ic.remove(other);
        ic.add(disabled);
        assertFalse("Becomes disabled", a.isEnabled());
        
        WeakReference<?> ref = new WeakReference<Object>(a);
        WeakReference<?> ref2 = new WeakReference<Object>(myAction);
        WeakReference<?> ref3 = new WeakReference<Object>(tc);
        a = null;
        myAction = null;
        tc = null;
        assertGC("We are able to clear global action", ref);
        assertGC("Even our action", ref2);
        assertGC("Even our component", ref3);
    }
    
    public void testGlobalChanges() throws Exception {
        class MyAction extends AbstractAction {
            public int cntEnabled;
            public int cntPerformed;
            
            public MyAction() {
                setEnabled(true);
            }
            
            @Override
            public boolean isEnabled() {
                cntEnabled++;
                return super.isEnabled();
            }
            
            public void actionPerformed(ActionEvent ev) {
                cntPerformed++;
            }
        }
        MyAction myAction = new MyAction();
        
        ActionMap tc = new ActionMap();
        tc.put(DefaultEditorKit.copyAction, myAction);

        InstanceContent ic = new InstanceContent();
        AbstractLookup al = new AbstractLookup(ic);
        ContextAwareAction a = CallbackActionTest.callback(DefaultEditorKit.copyAction, null, al, false);
        
        ic.add(tc);
        assertTrue("MyAction is enabled", a.isEnabled());
        assertEquals("isEnabled called once", 1, myAction.cntEnabled);
        
        myAction.setEnabled(false);
        assertEquals("An enabled is not called on property change", 1, myAction.cntEnabled);
        assertFalse("MyAction is disabled", a.isEnabled());
        assertEquals("An enabled is currentlly called again", 2, myAction.cntEnabled);
    }
    
    
    //
    // Set of tests for ActionMap and context
    //
    
    public void testLookupOfStateInActionMap() throws Exception {
        class MyAction extends AbstractAction {
            int actionPerformed;
            
            public void actionPerformed(ActionEvent ev) {
                actionPerformed++;
            }
            
        }
        MyAction action = new MyAction();
        
        ActionMap map = new ActionMap();
        InstanceContent ic = new InstanceContent();
        AbstractLookup al = new AbstractLookup(ic);
        ContextAwareAction system = CallbackActionTest.callback("systemKey", null, al, false);
        map.put("systemKey", action);
        
        
        
        Action clone;
        
        
        //
        // Without action map
        //
        
        clone = system.createContextAwareInstance(Lookup.EMPTY);
        
        assertTrue("Action should not be enabled if no callback provided", !clone.isEnabled());
        
        //
        // test with actionmap
        //
        action.setEnabled(false);
        
        Lookup context = Lookups.singleton(map);
        clone = system.createContextAwareInstance(context);
        Action clone2 = system.createContextAwareInstance(context);
        
        CntListener listener = new CntListener();
        clone.addPropertyChangeListener(listener);
        CntListener listener2 = new CntListener();
        clone2.addPropertyChangeListener(listener2);
        
        assertTrue("Not enabled now", !clone.isEnabled());
        action.setEnabled(true);
        assertTrue("Clone is enabled because the action in ActionMap is", clone.isEnabled());
        listener.assertCnt("One change expected", 1);
        listener2.assertCnt("One change expected", 1);
        
        clone.actionPerformed(new ActionEvent(this, 0, ""));
        assertEquals("MyAction.actionPerformed invoked", 1, action.actionPerformed);
        
        
        action.setEnabled(false);
        assertTrue("Clone is disabled because the action in ActionMap is", !clone.isEnabled());
        listener.assertCnt("Another change expected", 1);
        listener2.assertCnt("One change expected", 1);
        
        clone.actionPerformed(new ActionEvent(this, 0, ""));
        assertEquals("MyAction.actionPerformed invoked again", 2, action.actionPerformed);
    }
    
    private static final class CntListener extends Object
            implements PropertyChangeListener {
        private int cnt;
        
        public void propertyChange(PropertyChangeEvent evt) {
            cnt++;
        }
        
        public void assertCnt(String msg, int count) {
            assertEquals(msg, count, this.cnt);
            this.cnt = 0;
        }
    } // end of CntListener
    
}
