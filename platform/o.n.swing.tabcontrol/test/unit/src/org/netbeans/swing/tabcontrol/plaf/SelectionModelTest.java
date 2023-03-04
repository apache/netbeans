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

package org.netbeans.swing.tabcontrol.plaf;

import java.awt.Component;
import java.awt.Graphics;
import javax.swing.Icon;
import javax.swing.JLabel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.ListDataEvent;
import junit.framework.TestCase;
import org.netbeans.swing.tabcontrol.DefaultTabDataModel;
import org.netbeans.swing.tabcontrol.TabData;
import org.netbeans.swing.tabcontrol.TabDataModel;
import org.netbeans.swing.tabcontrol.event.ComplexListDataEvent;
import org.netbeans.swing.tabcontrol.event.ComplexListDataListener;

/** Tests for all of the functionality of TabLayoutModel instances
 *
 * @author  Tim Boudreau
 */
public class SelectionModelTest extends TestCase implements ComplexListDataListener, ChangeListener {
    TabDataModel mdl;
    DefaultTabSelectionModel sel;
    String lastListenerCall=null;
    ComplexListDataEvent lastEvent=null;
    
    public SelectionModelTest(String testName) {
        super(testName);
    }
    
    Icon ic = new Icon () {
        public int getIconWidth() {
            return 16;
        }
        public int getIconHeight() {
            return 16;
        }
        public void paintIcon (Component c, Graphics g, int x, int y) {
            //do nothing
        }
    };
    
    Icon sameSizeIcon = new Icon () {
        public int getIconWidth() {
            return 16;
        }
        public int getIconHeight() {
            return 16;
        }
        public void paintIcon (Component c, Graphics g, int x, int y) {
            //do nothing
        }
    };
    
    Icon biggerIcon = new Icon () {
        public int getIconWidth() {
            return 22;
        }
        public int getIconHeight() {
            return 22;
        }
        public void paintIcon (Component c, Graphics g, int x, int y) {
            //do nothing
        }
    };    
    
    /** Creates a new instance of SelectionModelTest */
    public void setUp() {
        prepareModel();
    }
    /** Weird, but this class was adapted from a standalone test written
     * long ago and rescued from cvs history.  It didn't use JUnit, and 
     * the assertTrue argument order was reversed.  So in the interest of 
     * laziness... */    
    private void assertPravda (boolean val, String msg) {
        assertTrue (msg, val);
    }
    
    private void assertEventFired () {
        assertPravda (eventFired, "Event expected but none fired");
        eventFired = false;
    }
    
    private void prepareModel() {
        if (mdl != null) {
            mdl.removeComplexListDataListener(this);
        }
        TabData[] td = new TabData[25];
        int ct = 0;
        for (char c='a'; c < 'z'; c++) {
            String name = new String (new char[]{c});
            Component comp = new JLabel(name);
            comp.setName (name);
            td[ct] = new TabData (comp, ic, name, "tip:"+name);
            ct++;
        }
        mdl = new DefaultTabDataModel (td);
//        mdl.addComplexListDataListener(this);
        sel = new DefaultTabSelectionModel (mdl);
        sel.addChangeListener (this);
    }

    /** Due to the non-junit way this test was originally written, tests must
     * be run in a specific order, since each makes modifiecations to the model
     * that the next one depends on */
    public void testEverything() {
        doTestGetSelectedIndex();
        doTestSetSelectedIndex();
        doTestInsertSingle();
        doTestRemoveSingle();
        doTestInsertContiguous();
        //XXX fix these older tests later
/*        doTestRemoveContiguous();
        doTestInsertNonContiguous();
        doTestRemoveNonContiguous();
        doTestSet();
        doTestSetIllegalIndex();
        doTestRemoveSelected();
        doTestRemoveLastWhenSelected();
        doTestRemoveMultipleIndices();
 */
        System.err.println("All tests passed for selection model");
    }
    
    public void doTestGetSelectedIndex() {
        System.err.println("testGetSelectedIndex");
        int i = sel.getSelectedIndex();
        assertPravda (i == -1, "Initial selected index should be -1");
    }
    
    public void doTestSetSelectedIndex() {
        System.err.println("testSetSelectedIndex");
        eventFired = false;
        sel.setSelectedIndex(5);
        assertEventFired();
        assertPravda (5 == sel.getSelectedIndex(), "Selected index should be 5 but is " + sel.getSelectedIndex());
        //make sure no event fired if the selection is the same
        noEvent = true;
        sel.setSelectedIndex(5);
        noEvent = false;
    }
    
    public void doTestInsertSingle () {
        System.err.println("testInsertSingle");
        TabData td = new TabData (new JLabel(), ic, "insertData", "Tip:insertData");
        noEvent = true;
        mdl.addTab(7, td);
        noEvent = false;
        assertPravda (5 == sel.getSelectedIndex(), "Selected index should be 5 but is " + sel.getSelectedIndex());
        TabData tdb = new TabData (new JLabel(), ic, "insertData2", "Tip:insertData2");
        mdl.addTab(3, tdb);
        assertEventFired();
        assertPravda (6 == sel.getSelectedIndex(), "After insert, selected index should be 6 but is " + sel.getSelectedIndex());
    }
    
    public void doTestRemoveSingle() {
        System.err.println("testRemoveSingle");
        mdl.removeTab(3);
        assertEventFired();
        assertPravda (5 == sel.getSelectedIndex(), "After remove, selected index should be 5 but is " + sel.getSelectedIndex());
        noEvent = true;
        mdl.removeTab(7);
        noEvent = false;
        assertPravda (5 == sel.getSelectedIndex(), "After remove, selected index should be 5 but is " + sel.getSelectedIndex());
        //model state is original state now
    }
    
    public void doTestInsertContiguous () {
        System.err.println("testInsertContiguous");

        TabData[] td = new TabData[5];
        for (int i=0; i < 5; i++) {
            String nm = "ic" + Integer.toString(i);
            td[i] = new TabData (new JLabel(), ic, nm, "tip:nm");
        }
        
        int idx = sel.getSelectedIndex();
        mdl.addTabs(0, td);
        assertEventFired();
        assertPravda (idx + 4 == sel.getSelectedIndex(), "After contiguous insert, selection should be " + (idx+5) + " but is " + sel.getSelectedIndex());
        noEvent=true;
        mdl.addTabs (20, td);
        noEvent = false;
    }
    
    public void doTestRemoveContiguous () {
        System.err.println("testRemoveContiguous");
        noEvent = true;
        mdl.removeTabs(20, 24);
        noEvent = false;
        mdl.removeTabs (2, 6);
        assertEventFired();
        assertPravda (4 == sel.getSelectedIndex(), "After contiguous remove, selected index should be 5 but is " + sel.getSelectedIndex());
    }
    
    public void doTestInsertNonContiguous () {
        System.err.println("testInsertNonContiguous");
        sel.setSelectedIndex(10);
        int[] indices = {1, 3, 5, 7};
        TabData[] td = new TabData[4];
        for (int i=0; i < 4; i++) {
            String nm = "icnc" + Integer.toString(i);
            td[i] = new TabData (new JLabel(), ic, nm, "tip:nm");
        }
        mdl.addTabs(indices, td);
        assertEventFired();
        assertPravda (14 == sel.getSelectedIndex(), "After non contiguous add of 4 items, selected index should be 14 but is " + sel.getSelectedIndex());
        
        indices = new int[] {16, 17, 21, 23};
        noEvent = true;
        mdl.addTabs (indices, td);
        noEvent = false;
        assertPravda (14 == sel.getSelectedIndex(), "After non contiguous add above the selected index, selected index should still be 14 but is " + sel.getSelectedIndex());
        
        indices = new int[] {3, 5, 22, 23};
        mdl.addTabs (indices, td);
        assertEventFired();
        assertPravda (16 == sel.getSelectedIndex(), "After non contiguous add straddling the selected index, selected index should still be 16 but is " + sel.getSelectedIndex());
    }
    
    public void doTestRemoveNonContiguous() {
        System.err.println("testRemoveNonContiguous");
        sel.setSelectedIndex (10);
        int[] indices = {1, 3, 5, 7};
        mdl.removeTabs(indices);
        assertEventFired();
        assertPravda (5 == sel.getSelectedIndex(), "After non contiguous remove before the selected index, selected index should still be 6 but is " + sel.getSelectedIndex());
        
        indices = new int[] {13, 15, 17};
        noEvent = true;
        mdl.removeTabs (indices);
        noEvent = false;
        assertPravda (5 == sel.getSelectedIndex(), "After non contiguous remove above the selected index, selected index should still be 6 but is " + sel.getSelectedIndex());
        
        indices = new int[] {2, 5, 18, 19};
        mdl.removeTabs(indices);
        assertEventFired();
        assertPravda (4 == sel.getSelectedIndex(), "After non contiguous remove before the selected index, selected index should still be 6 but is " + sel.getSelectedIndex());
    }
    
    public void doTestSet () {
        System.err.println("testSet");
        int i = sel.getSelectedIndex();
        TabData td = new TabData (new JLabel(), ic, "inserted", "tip:inserted");
        noEvent = true;
        mdl.setTab(i, td);
        mdl.setTab (i-3, td);
        mdl.setTab (i+5, td);
        noEvent = false;
    }
    
    public void doTestRemoveSelected () {
        System.err.println("testRemoveSelected");
        prepareModel();
        int i = mdl.size() - 5;
        sel.setSelectedIndex(i);
        mdl.removeTab(i);
        assertEventFired();
        assertPravda (i == sel.getSelectedIndex(), "After remove of the selected index when not at edge, selected index should still be " + i + " but is " + sel.getSelectedIndex());
        
        sel.setSelectedIndex (mdl.size()-1);
        mdl.removeTab (mdl.size() -1);
        assertEventFired();
        assertPravda (mdl.size()-1 == sel.getSelectedIndex(), "After remove of selected final element, selected index should be " + (mdl.size()-1) + "but is " + sel.getSelectedIndex());
        
        sel.setSelectedIndex (10);
        mdl.removeTabs (8, 12);
        assertEventFired();
        assertPravda (sel.getSelectedIndex() == 8, "After contiguous remove straddling selection, selection should be 8 but is " + sel.getSelectedIndex());
        
        
        mdl.removeTabs(0, mdl.size());
        assertEventFired();
        assertPravda (sel.getSelectedIndex() == -1, "After remove of all elements, selected index should be -1 but is " + sel.getSelectedIndex());
        
        prepareModel();
        sel.setSelectedIndex (10);
        int[] indices = new int[] {5, 8, 10, 15};
        mdl.removeTabs (indices);
        assertEventFired();
        assertPravda (sel.getSelectedIndex() == 8, "After remove two lower elements and the selected element, selected index should be 8 but is " + sel.getSelectedIndex());
        
        sel.setSelectedIndex (10);
        indices = new int[] {5, 8, 3, 2};
        mdl.removeTabs (indices);
        assertEventFired();
        assertPravda (sel.getSelectedIndex() == 6, "After noncontiguous remove four lower elements, selected index should be 6 but is " + sel.getSelectedIndex());

        sel.setSelectedIndex (10);
        indices = new int[] {12,13,14};
        mdl.removeTabs (indices);
        assertEventFired();
        assertPravda (sel.getSelectedIndex() == 10, "After noncontiguous remove of four upper elements, selected index should still be 10 but is " + sel.getSelectedIndex());
        
        
        int ids[] = new int[mdl.size()];
        for (int j=0; j < ids.length; j++) {
            ids[j] = j;
        }
        mdl.removeTabs (ids);
        assertEventFired();
        assertPravda (sel.getSelectedIndex() == -1, "After noncontiguous remove of all elements, selected index should be -1 but is " + sel.getSelectedIndex());
    }
    
    public void doTestRemoveLastWhenSelected () {
        System.err.println("testRemoveLastWhenSelected");
        prepareModel();
        int sz = mdl.size();
        System.err.println("Size is " + sz);
        int i = sz - 1;
        sel.setSelectedIndex(i);
        mdl.removeTab(i);
        System.err.println("Size is now " + mdl.size());
        assertEventFired();
        assertPravda (sz != mdl.size(), "After removal of one tab, model size has not changed.");
        assertPravda (mdl.size()-1== sel.getSelectedIndex(), "After removal of the selected index " + i + " (final index in model), selected index should be model.size() -1 (" +(mdl.size()-1) + ") but is " + sel.getSelectedIndex());
    }
            
    
    public void doTestSetIllegalIndex() {
        System.err.println("testSetIllegalIndex");
        Exception e = null;
        try {
            sel.setSelectedIndex(mdl.size() + 100);
        } catch (IllegalArgumentException e1) {
            e = e1;
        }
        assertPravda (e != null, "Set selection to an illegal positive value, but no exception was thrown");
        try {
            sel.setSelectedIndex(0 - mdl.size());
        } catch (IllegalArgumentException e1) {
            e = e1;
        }
        assertPravda (e != null, "Set selection to an illegal negative value, but no exception was thrown");
    }

    
    public void doTestRemoveMultipleIndices() {
        System.err.println("testRemoveMultipleIndices");
        prepareModel();
        sel.setSelectedIndex(mdl.size()-4);
        int m = mdl.size()-1;
        int[] toRemove = new int[8];
        for (int i=0; i < toRemove.length; i++) {
            toRemove[i]=m-i;
        }
        mdl.removeTabs(toRemove);
        assertEventFired();
        assertPravda (sel.getSelectedIndex() < mdl.size(), "After remove of non-contiguous indices, selected index is " + sel.getSelectedIndex() + " but model only contains " + mdl.size() + " entries.");
    }    
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        new SelectionModelTest("foo").testEverything();
    }
    
    public void contentsChanged(ListDataEvent e) {
        lastListenerCall="contentsChanged";
        lastEvent = (ComplexListDataEvent)e;
        if (noEvent) {
            assertPravda (false, "No event expected but " + e + " receieved");
        }
    }
    
    public void indicesAdded(ComplexListDataEvent e) {
        lastListenerCall="indicesAdded";
        lastEvent = e;
        if (noEvent) {
            assertPravda (false, "No event expected but " + e + " receieved");
        }
    }
    
    public void indicesChanged(ComplexListDataEvent e) {
        lastListenerCall="indicesChanged";
        lastEvent = e;
        if (noEvent) {
            assertPravda (false, "No event expected but " + e + " receieved");
        }
    }
    
    public void indicesRemoved(ComplexListDataEvent e) {
        lastListenerCall="indicesRemoved";
        lastEvent = e;
        if (noEvent) {
            assertPravda (false, "No event expected but " + e + " receieved");
        }
    }
    
    public void intervalAdded(ListDataEvent e) {
        lastListenerCall="intervalAdded";
        lastEvent = (ComplexListDataEvent)e;
        if (noEvent) {
            assertPravda (false, "No event expected but " + e + " receieved");
        }
    }
    
    public void intervalRemoved(ListDataEvent e) {
        lastListenerCall="intervalRemoved";
        lastEvent = (ComplexListDataEvent)e;
        if (noEvent) {
            assertPravda (false, "No event expected but " + e + " receieved");
        }
    }

    public void stateChanged(ChangeEvent e) {
        eventFired = true;
        if (noSelEvent) {
            assertPravda (false, "No change event expected but " + e + " receieved");
        }            
    }
    
    boolean eventFired=false;
    boolean noEvent = false;    
    boolean noSelEvent=false;
}
