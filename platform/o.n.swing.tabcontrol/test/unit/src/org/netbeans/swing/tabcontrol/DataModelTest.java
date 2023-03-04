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

package org.netbeans.swing.tabcontrol;

import java.awt.Component;
import java.awt.Graphics;
import java.util.Arrays;
import java.util.EventObject;
import javax.swing.Icon;
import javax.swing.JLabel;
import javax.swing.event.ListDataEvent;
import junit.framework.TestCase;
import org.netbeans.swing.tabcontrol.event.ComplexListDataEvent;
import org.netbeans.swing.tabcontrol.event.ComplexListDataListener;

/** Some basic tests for DefaultTabDataModel, etc.  Can be fleshed out into proper
 *  unit tests later.
 *
 * @author  Tim Boudreau
 */
public class DataModelTest extends TestCase implements ComplexListDataListener {
    ComplexListDataEvent lastEvent = null;
    String lastListenerCall=null;
    

    public DataModelTest(String testName) {
        super(testName);
    }
    
    TabDataModel mdl=null;
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
    
    public void setUp () {
        prepareModel();
        mdl.addComplexListDataListener(this);
    }
    
    /** Weird, but this class was adapted from a standalone test written
     * long ago and rescued from cvs history.  It didn't use JUnit, and 
     * the assertTrue argument order was reversed.  So in the interest of 
     * laziness... */
    private void assertPravda (boolean val, String msg) {
        assertTrue (msg, val);
    }
    
    private void assertText (String name, int index) {
        TabData td = mdl.getTab (index);
        assertPravda (td.getText().equals (name), "Text at " + index + " was not " + name + " it was " + td.getText());
    }
    
    private void assertData (int index, TabData data) {
        TabData other = mdl.getTab (index);
        assertPravda (data.equals(other), "TabData at " + index + " is not " + data + " it is " + other);
    }
    
    private void assertListenerCall (String call) {
        assertPravda (lastEvent != null, "Last listener call should be non-null");
        assertPravda (call.equals (lastListenerCall), "Last listener call should have been " + call + " but was " + lastListenerCall);
    }
    
    private void assertEventIndices (int indices[]) {
        assertPravda (lastEvent != null, "Last event should have been fired but is null");
        int[] ids = lastEvent.getIndices();
        Arrays.sort (indices);
        Arrays.sort (ids);
        assertPravda (ids.length == indices.length, "Indices length in last change should be " + indices.length + " but is " + ids.length + " expected indices: " + arrToStr (indices) + " actual indices " + arrToStr(ids));
        for (int i=0; i < ids.length; i++) {
            assertPravda (ids[i] == indices[i], "Event indices should be " + arrToStr(indices) + " but are " + arrToStr(ids));
        }
    }
    
    private void assertEventIndices (int start, int end) {
        assertPravda (lastEvent != null, "Last event should have been fired but is null");
        assertPravda (start == lastEvent.getIndex0(), "Event start index should be " + start + " but is " + lastEvent.getIndex0());
        assertPravda (end == lastEvent.getIndex1(), "Event end index should be " + end + " but is " + lastEvent.getIndex1());
    }
    
    private void assertWidthChanged () {
        assertPravda (lastEvent != null, "Last event should have been fired but is null");
        assertPravda (lastEvent.isTextChanged(), "Last event should have been a text change event but wasn't");
    }
    
    private void assertWidthNotChanged () {
        assertPravda (lastEvent != null, "Last event should have been fired but is null");
        assertPravda (!lastEvent.isTextChanged(), "Last event should not have been a text change event but was");
    }
    
    /**
     * Since this test was written in non-junit style, the order of the tests
     * is important, so we have one junit method for all of the actual tests. 
     */
    public void testEverything() {
        doTestContentsValid();
        doTestAdd();
        doTestRemove();
        doTestAddContiguous();
        doTestRemoveContiguous();
        doTestAddNonContiguous();
        doTestRemoveNonContiguous();
        doTestSetTab();
        doTestSetTextNonContiguous();
        doTestSetIconNonContiguous();
        doTestSetIconAndTextNonContiguous();
        System.err.println("All tests passed");
    }
    
    public void doTestContentsValid () {
        System.err.println("testContentsValid");
        _testContentsValid();
    }
    
    private void _testContentsValid () {
        int ct=0;
        for (char c='a'; c < 'z'; c++) {
            TabData td = mdl.getTab(ct);
            assertPravda (td.getText().charAt(0) == c, "Name at " + ct + " is not " + c + " it is " + td.getText());
            ct++;
        }
    }
    
    public void doTestAdd () {
        System.err.println("testAdd");
        TabData td = new TabData (new JLabel ("testAdd"), ic, "testAdd", "Tip:testAdd");
        mdl.addTabs(0, new TabData[] {td});
        assertData (0, td);
    }
    
    public void doTestRemove () {
        System.err.println("testRemove");
        mdl.removeTab(0);
        assertText ("a", 0);
    }
    
    public void doTestAddContiguous() {
        System.err.println("testAddContiguous");
        int formerSize = mdl.size();
        TabData[] td = new TabData[10];
        for (int i=0; i < 10; i++) {
            String name = Integer.toString (i);
            Component comp = new JLabel();
            td[i] = new TabData (comp, ic, name, "Tip:"+name);
        }
        mdl.addTabs(10, td);
        int expectedSize=formerSize+10;
        assertPravda (mdl.size() == expectedSize, "Model size should be " + expectedSize + " after adding 10 items, but is " + mdl.size());
        for (int i=10; i < 20; i++) {
            assertData (i, td[i-10]);
        }
    }
    
    public void doTestRemoveContiguous() {
        System.err.println("testRemoveContiguous");
        int formerSize = mdl.size();
        mdl.removeTabs (10, 20);
        int expectedSize=formerSize - 10;
        assertPravda (mdl.size() == expectedSize, "Model size should be " + expectedSize + " after removing 10 items, but is " + mdl.size());
        try {
            _testContentsValid();
        } catch (RuntimeException e) {
            e.printStackTrace();
            fail("After removing 10 items, contents should be original contents, but are " + mdl.toString());
        }
    }
    
    TabData[] data=null;
    public void doTestAddNonContiguous () {
        System.err.println("testAddNonContiguous");
        int[] indices = new int[] {3, 1, 5};
        data = new TabData[3];
        for (int i=0; i < indices.length; i++) {
            String name = Integer.toString (indices[i]);
            data[i] = new TabData (new JLabel(), ic, name, "Tip:"+name);
        }
        mdl.addTabs(indices, data);
        assertData (3, data[0]);
        assertData (1, data[1]);
        assertData (5, data[2]);
        Arrays.sort (indices);
        assertEventIndices (indices);
        assertListenerCall ("indicesAdded");
    }
    
    public void doTestRemoveNonContiguous () {
        int[] indices = new int[] {5, 1, 3};
        mdl.removeTabs(indices);
        try {
            _testContentsValid();
        } catch (RuntimeException e) {
            System.err.println("After non-contiguous removal of " + arrToStr(indices) + ", contents should be original contents, but are " + mdl.toString());
            throw e;
        }
        Arrays.sort(indices);
        assertEventIndices (indices);
        assertListenerCall ("indicesRemoved");
    }
    
    public void doTestSetTab () {
        System.err.println("testSetTab");
        TabData former = mdl.getTab (22);
        TabData nue = new TabData (new JLabel(), ic, "foo", "Tip:foo");
        mdl.setTab (22, nue);
        assertData (22, nue);
        mdl.setTab (22, former);
        assertListenerCall ("contentsChanged");
        assertEventIndices (22, 22);
        //Make sure an event is not generated for changes that should not generate one
        noEvent = true;
        mdl.setTab (22, former);
        mdl.setText(22, former.getText());
        mdl.setIcon (22, ic);
        noEvent = false;
    }
    
    public void doTestSetTextNonContiguous() {
        System.err.println("testSetTextNonContiguous");
        String[] names = new String [5];
        int[] indices = new int[] {22,11,15,8,3};
        for (int i=0; i < names.length; i++) {
            names[i] = mdl.getTab(indices[i]).getText();
        }
        noEvent = true;
        //should produce no event since the names haven't changed
        mdl.setText(indices, names);
        noEvent = false;
        String[] s = new String[names.length];
        for (int i=0; i < s.length; i++) {
            s[i] = names[i] + "modified";
        }
        mdl.setText(indices, s);
        for (int i=0; i < s.length; i++) {
            assertText(s[i], indices[i]);
        }
        Arrays.sort (indices);
        assertEventIndices(indices);
        assertListenerCall("contentsChanged");
        assertWidthChanged();
        //restore the original text
        mdl.setText(indices, names);
    }
    
    public void doTestSetIcon () {
        System.err.println("testSetIcon");
        TabData td = mdl.getTab (20);
        noEvent = true;
        mdl.setIcon(20, td.getIcon());
        noEvent = false;
        
        mdl.setIcon(20, sameSizeIcon);
        assertEventIndices(20,20);
        assertListenerCall("contentsChanged");
        assertPravda (td.getIcon() == sameSizeIcon, "Icon was changed but same old still returned from TabData");
        assertWidthNotChanged();
        EventObject last = lastEvent;
        
        mdl.setIcon(20, biggerIcon);
        assertWidthChanged();
        assertPravda (last != lastEvent,  "Icon changed but no event fired");
        
        //restore the state
        mdl.setIcon(20,ic);
    }

    public void doTestSetIconNonContiguous() {
        System.err.println("testSetIconNonContiguous");
        Icon[] icons = new Icon[5];
        int[] indices = new int[] {22,11,15,8,3};
        for (int i=0; i < icons.length; i++) {
            icons[i] = mdl.getTab(indices[i]).getIcon();
        }
        noEvent = true;
        //should produce no event since the names haven't changed
        mdl.setIcon(indices, icons);
        noEvent = false;
        
        EventObject last = lastEvent;
        
        Arrays.fill (icons, sameSizeIcon);
        mdl.setIcon (indices, icons);
        assertPravda (last != lastEvent,  "Icons changed but no event fired");
        last = lastEvent;
        assertListenerCall("contentsChanged");
        assertWidthNotChanged();
        
        Arrays.fill (icons, biggerIcon);
        icons[2] = sameSizeIcon;
        int[] expectedIndices = new int[]{3,8,22,11};
        
        mdl.setIcon(indices, icons);
        assertPravda (last != lastEvent,  "Icons changed but no event fired");
        assertListenerCall("contentsChanged");
        assertWidthChanged();
        assertEventIndices(expectedIndices);
        
        Arrays.fill (icons, ic);
        //restore the original text
        mdl.setIcon(indices, icons);
    }
    
    public void doTestSetIconAndTextNonContiguous() {
        System.err.println("testSetIconAndTextNonContiguous");
        int indices[] = new int[] {3, 10, 5};
        Icon[] icons = new Icon[3];
        Arrays.fill (icons, ic);
        String[] sts = new String[3];
        for (int i=0; i < 3; i++) {
            sts[i] = mdl.getTab(i).getText();
        }
        //ensure expected results
        mdl.setIconsAndText (indices, sts, icons);
        
        noEvent = true;
        mdl.setIconsAndText(indices, sts, icons);
        noEvent = false;
        String[] realText = new String[sts.length];
        System.arraycopy(sts, 0, realText, 0, sts.length);
        EventObject last = lastEvent;
        
        icons[0] = sameSizeIcon;
        mdl.setIconsAndText(indices, sts, icons);
        assertPravda (last != lastEvent,  "Icons and text changed but no event fired");
        assertWidthNotChanged();
        assertEventIndices(new int[] {3});
        last = lastEvent;
        
        icons[0] = biggerIcon;
        sts[1] = "foobar";
        mdl.setIconsAndText(indices, sts, icons);
        assertPravda (last != lastEvent,  "Icons and text changed but no event fired");
        assertWidthChanged();
        assertEventIndices(new int[] {3, 10});
        last = lastEvent;
        
        //Also test the simpler firing code for when all changed icons have changed text
        icons[0] = ic;
        sts[1] = "boo";
        mdl.setIconsAndText(indices, sts, icons);
        assertPravda (last != lastEvent,  "Icons and text changed but no event fired");
        assertWidthChanged();
        assertEventIndices(new int[] {3, 10});
        
        //restore the state
        Arrays.fill (icons, ic);
        mdl.setIconsAndText (indices, realText, icons);
    }
    
    
    static String arrToStr (int[] ints) {
        if (ints == null) return "null";
        StringBuffer out = new StringBuffer (ints.length * 3);
        for (int i=0; i < ints.length; i++) {
            out.append (ints[i]);
            if (i != ints.length-1) {
                out.append (",");
            }
        }
        return out.toString();
    }
    
    static String arrToStr (Object[] o) {
        if (o == null) return "null";
        StringBuffer out = new StringBuffer (o.length * 3);
        for (int i=0; i < o.length; i++) {
            out.append (o[i]);
            if (i != o.length-1) {
                out.append (",");
            }
        }
        return out.toString();
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
    }
    
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        new DataModelTest("foo").run();
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
        assertPravda (e.getIndex0() <= e.getIndex1(), "Event start index > end index");

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
    
    boolean noEvent = false;
}
