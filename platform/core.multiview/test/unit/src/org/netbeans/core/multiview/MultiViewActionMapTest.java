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


package org.netbeans.core.multiview;
import java.awt.GraphicsEnvironment;
import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;

import javax.swing.Action;
import javax.swing.ActionMap;
import javax.swing.JComponent;
import junit.framework.Test;
import junit.framework.TestSuite;




import org.netbeans.core.api.multiview.MultiViewHandler;
import org.netbeans.core.api.multiview.MultiViews;
import org.netbeans.core.spi.multiview.MultiViewDescription;
import org.netbeans.core.spi.multiview.MultiViewElement;

import org.netbeans.core.spi.multiview.MultiViewFactory;
import org.netbeans.junit.NbTestCase;

import org.openide.util.Lookup;
import org.openide.util.LookupEvent;


import org.openide.util.LookupListener;
import org.openide.util.Utilities;
import org.openide.util.lookup.Lookups;


import org.openide.windows.TopComponent;



/** 
 *
 * @author Milos Kleint
 */
public class MultiViewActionMapTest extends NbTestCase {
    
    public static Test suite() {
        return GraphicsEnvironment.isHeadless() ? new TestSuite() : new TestSuite(MultiViewActionMapTest.class);
    }

    public MultiViewActionMapTest(String name) {
        super (name);
    }
    
    protected boolean runInEQ () {
        return true;
    }
    
    
    public void testElementIsTopComponent() throws Exception {
        MVElemTopComponent elem1 = new MVElemTopComponent();
        MVElemTopComponent elem2 = new MVElemTopComponent();
        MVElemTopComponent elem3 = new MVElemTopComponent();
        doTestActionMap(elem1, elem2, elem3);
    }
    
    public void testElementIsNotTC() throws Exception {
        MVElem elem1 = new MVElem();
        MVElem elem2 = new MVElem();
        MVElem elem3 = new MVElem();
        doTestActionMap(elem1, elem2, elem3);
    }    
    
    private void doTestActionMap(MultiViewElement elem1, MultiViewElement elem2, MultiViewElement elem3) {
        MultiViewDescription desc1 = new MVDesc("desc1", null, 0, elem1);
        MultiViewDescription desc2 = new MVDesc("desc2", null, 0, elem2);
        MultiViewDescription desc3 = new MVDesc("desc3", null, 0, elem3);
        
        MultiViewDescription[] descs = new MultiViewDescription[] { desc1, desc2, desc3 };
        TopComponent tc = MultiViewFactory.createMultiView(descs, desc1);
        // WARNING: as anything else the first element's action map is set only after the tc is opened..
        tc.open();
        
        Action act = new TestAction("MultiViewAction");
        // add action to the MVTC map
        tc.getActionMap().put("testkey", act);
        ActionMap obj = (ActionMap)tc.getLookup().lookup(ActionMap.class);
        assertNotNull(obj);
        assertEquals(obj.getClass(), MultiViewTopComponentLookup.LookupProxyActionMap.class);
        Action res = (Action)obj.get("testkey");
        assertNotNull(res);
        assertEquals("MultiViewAction", res.getValue(Action.NAME));
        // remove action from the MVTC map
        tc.getActionMap().remove("testkey");
        obj = (ActionMap)tc.getLookup().lookup(ActionMap.class);
        res = (Action)obj.get("testkey");
        assertNull(res);
        
        // make sure the action in MVTC has higher priority..
        JComponent elemtc = elem1.getVisualRepresentation();
        Action innerAct = new TestAction("InnerAction");
        elemtc.getActionMap().put("testkey", innerAct);
        assertNotNull(elemtc.getActionMap().get("testkey"));
        obj = (ActionMap)tc.getLookup().lookup(ActionMap.class);
        // check if anything there in elemen'ts actionmap
        assertNotNull(obj);
        res = (Action)obj.get("testkey");
        assertNotNull(res);
        // put actin to the mvtc actionmap as well..
        tc.getActionMap().put("testkey", act);
        assertNotNull(obj);
        res = (Action)obj.get("testkey");
        assertNotNull(res);
        assertEquals("MultiViewAction", res.getValue(Action.NAME));
        //remove from mvtc's map..
        tc.getActionMap().remove("testkey");
        res = (Action)obj.get("testkey");
        assertNotNull(res);
        assertEquals("InnerAction", res.getValue(Action.NAME));
        // now switch to the other element...
        MultiViewHandler handler = MultiViews.findMultiViewHandler(tc);
        // test related hack, easy establishing a  connection from Desc->perspective
        Accessor.DEFAULT.createPerspective(desc2);
        handler.requestVisible(Accessor.DEFAULT.createPerspective(desc2));
        obj = (ActionMap)tc.getLookup().lookup(ActionMap.class);
        res = (Action)obj.get("testkey");
        assertNull(res); // is not defined in element2
    }
    
    public void testActionMapChanges() throws Exception {
        MVElemTopComponent elem1 = new MVElemTopComponent();
        MVElemTopComponent elem2 = new MVElemTopComponent();
        MVElem elem3 = new MVElem();
        MultiViewDescription desc1 = new MVDesc("desc1", null, 0, elem1);
        MultiViewDescription desc2 = new MVDesc("desc2", null, 0, elem2);
        MultiViewDescription desc3 = new MVDesc("desc3", null, 0, elem3);
        
        MultiViewDescription[] descs = new MultiViewDescription[] { desc1, desc2, desc3 };
        TopComponent tc = MultiViewFactory.createMultiView(descs, desc1);
        // WARNING: as anything else the first element's action map is set only after the tc is opened..
        Lookup.Result result = tc.getLookup().lookup(new Lookup.Template(ActionMap.class));
        LookListener list = new LookListener();
        list.resetCount();
        result.addLookupListener(list);
        result.allItems();
        
        tc.open();
        assertEquals(1, list.getCount());
        
        MultiViewHandler handler = MultiViews.findMultiViewHandler(tc);
        // test related hack, easy establishing a  connection from Desc->perspective
        Accessor.DEFAULT.createPerspective(desc2);
        handler.requestVisible(Accessor.DEFAULT.createPerspective(desc2));
        assertEquals(2, list.getCount());
        
        Accessor.DEFAULT.createPerspective(desc3);
        handler.requestVisible(Accessor.DEFAULT.createPerspective(desc3));
        assertEquals(3, list.getCount());
    }
    
    public void testSimplifiedActionMapChanges81117() {
        MultiViewTopComponentLookup.InitialProxyLookup lookup = new MultiViewTopComponentLookup.InitialProxyLookup(new ActionMap());
        Lookup.Result res = lookup.lookup(new Lookup.Template(ActionMap.class));
        LookListener list = new LookListener();
        list.resetCount();
        res.addLookupListener(list);
        assertEquals(1, res.allInstances().size());
        assertEquals(0, list.getCount());
        lookup.refreshLookup();
        assertEquals(1, list.getCount());
        assertEquals(1, res.allInstances().size());
        
        MultiViewTopComponentLookup lookup2 = new MultiViewTopComponentLookup(new ActionMap());
        res = lookup2.lookup(new Lookup.Template(ActionMap.class));
        list = new LookListener();
        list.resetCount();
        res.addLookupListener(list);
        assertEquals(1, res.allInstances().size());
        assertEquals(0, list.getCount());
        lookup2.setElementLookup(Lookups.fixed(new Object[] {new Object()} ));
        assertEquals(1, list.getCount());
        assertEquals(1, res.allInstances().size());
        
    }
    
    
    public void testActionMapChangesForElementsWithComponentShowingInit() throws Exception {
        Action act1 = new TestAction("MultiViewAction1");
        Action act2 = new TestAction("MultiViewAction2");
        MVElemTopComponent elem1 = new ComponentShowingElement("testAction", act1);
        MVElemTopComponent elem2 = new ComponentShowingElement("testAction", act2);
        MVElem elem3 = new MVElem();
        MultiViewDescription desc1 = new MVDesc("desc1", null, 0, elem1);
        MultiViewDescription desc2 = new MVDesc("desc2", null, 0, elem2);
        MultiViewDescription desc3 = new MVDesc("desc3", null, 0, elem3);
        
        MultiViewDescription[] descs = new MultiViewDescription[] { desc1, desc2, desc3 };
        TopComponent tc = MultiViewFactory.createMultiView(descs, desc1);
        Lookup.Result result = tc.getLookup().lookup(new Lookup.Template(ActionMap.class));
        LookListener2 list = new LookListener2();
        result.addLookupListener(list);
        result.allInstances().size();
        list.setCorrectValues("testAction", act1);
        // WARNING: as anything else the first element's action map is set only after the tc is opened..
        tc.open();
        assertEquals(1, list.getCount());
        MultiViewHandler handler = MultiViews.findMultiViewHandler(tc);
        // test related hack, easy establishing a  connection from Desc->perspective
        Accessor.DEFAULT.createPerspective(desc2);
        list.setCorrectValues("testAction", act2);
        handler.requestVisible(Accessor.DEFAULT.createPerspective(desc2));
        assertEquals(2, list.getCount());
        Accessor.DEFAULT.createPerspective(desc3);
        list.setCorrectValues("testAction", null);
        handler.requestVisible(Accessor.DEFAULT.createPerspective(desc3));
        assertEquals(3, list.getCount());
    }  
    
    
    public class ComponentShowingElement extends MVElemTopComponent {
        private String key;
        private Action action;
        
        public ComponentShowingElement(String actionkey, Action value) {
            action = value;
            key = actionkey;
        }
        
        public void componentShowing() {
            super.componentShowing();
            getActionMap().put(key, action);
        }
        
    }
    
    private class LookListener2 implements LookupListener {
        private String key;
        private Action action;
        int count = 0;
        private ActionMap lastMap;
        
        public void setCorrectValues(String keyValue, Action actionValue) {
            action = actionValue;
            key = keyValue;
        }
        
        public int getCount() {
            return count;
        }
        
        public void resultChanged (LookupEvent ev) {
            Lookup.Result res = (Lookup.Result)ev.getSource();
            assertEquals(1, res.allInstances().size());
            ActionMap map = (ActionMap)res.allInstances().iterator().next();
            if (lastMap != null) {
                // because of CallbackSystemAction.GlobalManager
                assertNotSame(map, lastMap);
            }
            lastMap = map;
            Action act = map.get(key);
            assertEquals(action, act);
            count++;
        }
    }
    
//   //
//    // Set of tests for ActionMap and context.. copied from CallbackSystemActionTest
//    //
//    
//    public void testLookupOfStateInActionMap () throws Exception {
//        
//        class MyAction extends javax.swing.AbstractAction 
//                       implements org.openide.util.actions.ActionPerformer {
//            int actionPerformed;
//            int performAction;
//            
//            public void actionPerformed (java.awt.event.ActionEvent ev) {
//                actionPerformed++;
//            }
//            
//            public void performAction (SystemAction a) {
//		performAction++;
//            }
//        }
//        MyAction action = new MyAction ();
//        
//        ActionMap map = new ActionMap ();
//        CallbackSystemAction system = (CallbackSystemAction)SystemAction.get(SurviveFocusChgCallbackAction.class);
//        system.setActionPerformer (null);
//        map.put (system.getActionMapKey(), action);
//
//        javax.swing.Action clone;
//        clone = system.createContextAwareInstance(org.openide.util.Lookup.EMPTY);
//        
//        assertTrue ("Action should not be enabled if no callback provided", !clone.isEnabled());
//        
//        system.setActionPerformer (action);
//        assertTrue ("Is enabled, because it has a performer", clone.isEnabled());
//        system.setActionPerformer (null);
//        assertTrue ("Is disabled, because the performer has been unregistered", !clone.isEnabled ());
//        
//        //
//        // test with actionmap
//        //
//        action.setEnabled (false);
//        
//        org.openide.util.Lookup context = org.openide.util.lookup.Lookups.singleton(map);
//        clone = system.createContextAwareInstance(context);
//        
//        CntListener listener = new CntListener ();
//        clone.addPropertyChangeListener (listener);
//        
//        assertTrue ("Not enabled now", !clone.isEnabled ());
//        action.setEnabled (true);
//        assertTrue ("Clone is enabled because the action in ActionMap is", clone.isEnabled ());
//        listener.assertCnt ("One change expected", 1);
//        
//        system.setActionPerformer (action);
//        clone.actionPerformed(new java.awt.event.ActionEvent (this, 0, ""));
//        assertEquals ("MyAction.actionPerformed invoked", 1, action.actionPerformed);
//        assertEquals ("MyAction.performAction is not invoked", 0, action.performAction);
//        
//        
//        action.setEnabled (false);
//        assertTrue ("Clone is disabled because the action in ActionMap is", !clone.isEnabled ());
//        listener.assertCnt ("Another change expected", 1);
//        
//        clone.actionPerformed(new java.awt.event.ActionEvent (this, 0, ""));
//        assertEquals ("MyAction.actionPerformed invoked again", 2, action.actionPerformed);
//        assertEquals ("MyAction.performAction is not invoked, remains 0", 0, action.performAction);
//        
//    }   
//    
//   private static final class CntListener extends Object
//    implements java.beans.PropertyChangeListener {
//        private int cnt;
//        
//        public void propertyChange(java.beans.PropertyChangeEvent evt) {
//            cnt++;
//        }
//        
//        public void assertCnt (String msg, int count) {
//            assertEquals (msg, count, this.cnt);
//            this.cnt = 0;
//        }
//    } // end of CntListener    

    public void testActionsGlobalContext() throws Exception {
        Lookup look = Utilities.actionsGlobalContext();
        MVElemTopComponent elem1 = new MVElemTopComponent();
        MVElemTopComponent elem2 = new MVElemTopComponent();
        MVElemTopComponent elem3 = new MVElemTopComponent();
        MultiViewDescription desc1 = new MVDesc("desc1", null, 0, elem1);
        MultiViewDescription desc2 = new MVDesc("desc2", null, 0, elem2);
        MultiViewDescription desc3 = new MVDesc("desc3", null, 0, elem3);
        MultiViewDescription[] descs = new MultiViewDescription[] { desc1, desc2, desc3 };
        TopComponent tc = MultiViewFactory.createMultiView(descs, desc1);
        // WARNING: as anything else the first element's action map is set only after the tc is opened..
        tc.open();
        tc.requestActive();
        
        ActionMap map = (ActionMap)look.lookup(ActionMap.class);
        assertNotNull("is null", map);
        assertEquals("is wrong class=" + map.getClass(), map.getClass(), MultiViewTopComponentLookup.LookupProxyActionMap.class);
        Action res = map.get("testkey");
        assertNull(res);
        Action act = new TestAction("MultiViewAction");
        // add action to the MVTC map
        elem1.getVisualRepresentation().getActionMap().put("testkey", act);
        res = map.get("testkey");
        assertNotNull(res);
        
        // test switching to a different component..
        TopComponent tc2 = new TopComponent();
        tc2.open();
        tc2.requestActive();
        map = (ActionMap)look.lookup(ActionMap.class);
        res = map.get("testkey");
        assertNull(res);
        
        // switch back and test a different element..
        tc.requestActive();
        map = (ActionMap)look.lookup(ActionMap.class);
        MultiViewHandler handler = MultiViews.findMultiViewHandler(tc);
        // test related hack, easy establishing a  connection from Desc->perspective
        Accessor.DEFAULT.createPerspective(desc2);
        handler.requestVisible(Accessor.DEFAULT.createPerspective(desc2));
        res = map.get("testkey");
        assertNull(res);
        // now switch back to the original element and see if the action is stil there..
        Accessor.DEFAULT.createPerspective(desc1);
        handler.requestVisible(Accessor.DEFAULT.createPerspective(desc1));
        res = map.get("testkey");
        assertNotNull(res);
        
    }
    
    
    static class TestAction extends AbstractAction {
        public TestAction(String name) {
            super(name);
        }
        
        public void actionPerformed(ActionEvent event) {
            
        }
        
    }
    
    private class LookListener implements LookupListener {
        int count = 0;
        
        public void resetCount() {
            count = 0;
        }
        
        
        public int getCount() {
            return count;
        }
        
        public void resultChanged (LookupEvent ev) {
            count++;
        }
    }
    
 }

