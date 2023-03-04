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

package org.openide.explorer.propertysheet;

import java.awt.BorderLayout;
import java.awt.Graphics;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.lang.reflect.InvocationTargetException;
import javax.swing.JFrame;
import javax.swing.SwingUtilities;
import org.netbeans.junit.NbTestCase;
import org.netbeans.junit.RandomlyFails;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.nodes.NodeAdapter;
import org.openide.nodes.PropertySupport;
import org.openide.nodes.Sheet;

// This test class tests the main functionality of the property sheet
@RandomlyFails
public class PropertiesFlushTest extends NbTestCase {
    private PropertySheet ps = null;
    public PropertiesFlushTest(String name) {
        super(name);
    }
    
    protected boolean runInEQ() {
        return false;
    }
    
/*
 * This test creates a Property, Editor and Node. First test checks if initialized
 * editor contains the same value as property. The second checks if the property
 * value is changed if the same change will be done in the editor.
 */
    protected void setUp() throws Exception {
        try {
            // Create new TNode
            tn = new TNode();
            
            //Replacing NodeOp w/ JFrame to eliminate depending on full IDE init
            //and long delay while waiting for property sheet thus requested to
            //initialize
            final JFrame jf = new JFrame();
            ps = new PropertySheet();
            jf.getContentPane().setLayout(new BorderLayout());
            jf.getContentPane().add(ps, BorderLayout.CENTER);
            jf.setLocation(30,30);
            jf.setSize(500,500);
            new ExtTestCase.WaitWindow(jf);
            final Node[] nodes = new Node[]{tn};
            
            SwingUtilities.invokeAndWait(new Runnable() {
                public void run() {
                    ps.setNodes(nodes);
                }
            });
            
            sleep();
            ensurePainted(ps);
        } catch (Exception e) {
            fail("FAILED - Exception thrown "+e.getClass().toString());
        }
    }
    
    private void ensurePainted(final PropertySheet ps) throws Exception {
        //issues 39205 & 39206 - ensure the property sheet really repaints
        //before we get the value, or the value in the editor will not
        //have changed
        SwingUtilities.invokeAndWait(new Runnable() {
            public void run() {
                Graphics g = ps.getGraphics();
                ps.paintImmediately(0,0,ps.getWidth(), ps.getHeight());
            }
        });
    }
    
    
    public void testNullChangePerformedAndReflectedInPropertySheet() throws Exception {
        System.err.println(".testNullChangePerformedAndReflectedInPropertySheet");
        int count = ps.table.getRowCount();
        assertTrue("Property sheet should contain three rows ", count==3);
        L l = new L();
        tn.addPropertyChangeListener(l);
        
        SwingUtilities.invokeAndWait(new Runnable() {
            public void run() {
                tn.replaceProps();
            }
        });
        Thread.sleep(500);
        //        SwingUtilities.invokeAndWait (new Runnable(){public void run() {System.currentTimeMillis();}});
        
        l.assertEventReceived();
        
        assertTrue("Should only be one property", tn.getPropertySets()[0].getProperties().length == 1);
        sleep();
        ensurePainted(ps);
        int rc = ps.table.getRowCount();
        assertTrue("Property sheet should now only show 2 rows, not " + rc, rc == 2);
    }
    
    private Exception throwMe = null;
    public void testSetSheetChangesPropertySheetContents() throws Exception {
        System.err.println(".testSetSheetChangesPropertySheetContents");
        final TNode2 tnd = new TNode2();
        throwMe = null;
        final Node[] nodes = new Node[]{tnd};
        SwingUtilities.invokeAndWait(new Runnable() {
            public void run() {
                try {
                    ps.setNodes(nodes);
                } catch (Exception e) {
                    throwMe = e;
                }
            }
        });
        
        if (throwMe != null) {
            throw throwMe;
        }
        sleep();
        
        int rowCount = ps.table.getRowCount();
        assertTrue("With a single property in a single property set, row count should be 2 but is " + rowCount, rowCount == 2);
        
        
        L2 l2 = new L2();
        tnd.addNodeListener(l2);
        throwMe = null;
        
        SwingUtilities.invokeAndWait(new Runnable() {
            public void run() {
                try {
                    System.err.println("Replacing property sets");
                    tnd.replaceSets();
                } catch (Exception e) {
                    throwMe = e;
                }
            }
        });
        if (throwMe != null) {
            throw throwMe;
        }
        sleep();
        
        System.err.println("EnsurePainted");
        ensurePainted(ps);
        
        System.err.println("Asserting event received");
        l2.assertEventReceived();
        
        int nueCount = ps.table.getRowCount();
        System.err.println("Checking count - it is " + nueCount);
        assertTrue("With two properties in two property sets, row count should be 4 but is " + nueCount, nueCount == 4);
        
        
    }
    
    private void sleep() throws Exception {
        Thread.sleep(500);
        SwingUtilities.invokeAndWait(new Runnable() {
            public void run() {
                System.currentTimeMillis();
            }
        });
    }
    
    private class L implements PropertyChangeListener {
        private boolean eventReceived = false;
        
        public void assertEventReceived() {
            assertTrue("null null null property change not received on sets change", eventReceived);
            eventReceived = false;
        }
        
        public void propertyChange(PropertyChangeEvent evt) {
            if (evt.getPropertyName() == null && evt.getOldValue() == null && evt.getNewValue() == null) {
                eventReceived = true;
            }
            System.err.println("Event: " + evt);
        }
    }
    
    private class L2 extends NodeAdapter {
        private boolean eventReceived = false;
        
        public void assertEventReceived() {
            assertTrue("AbstractNode did not fire PROP_PROPERTY_SETS when setSheet() was called", eventReceived);
            eventReceived = false;
        }
        
        public void propertyChange(PropertyChangeEvent evt) {
            if (AbstractNode.PROP_PROPERTY_SETS.equals(evt.getPropertyName()) && evt.getOldValue() == null && evt.getNewValue() == null) {
                eventReceived = true;
            }
            System.err.println("Event: " + evt + " name " + evt.getPropertyName());
        }
    }
    
    //Node definition
    public class TNode extends AbstractNode {
        //create Node
        public TNode() {
            super(Children.LEAF);
            setName("TNode"); // or, super.setName if needed
            setDisplayName("TNode");
        }
        //clone existing Node
        public Node cloneNode() {
            return new TNode();
        }
        
        public void replaceProps() {
            sets = null;
            firePropertyChange(null, null, null);
        }
        
        Node.PropertySet[] sets = null;
        
        private boolean firstTime = true;
        // Create a property sheet:
        public Node.PropertySet[] getPropertySets() {
            if (sets == null) {
                System.err.println("Create sheet");
                Sheet sheet = new Sheet();
                // Make sure there is a "Properties" set:
                Sheet.Set props = sheet.get(Sheet.PROPERTIES);
                props = Sheet.createPropertiesSet();
                sheet.put(props);
                TProperty tp = new TProperty("property", true);
                props.put(tp);
                if (firstTime) {
                    props.put(new TProperty("second", true));
                    System.err.println("first time");
                    firstTime = false;
                } else {
                    System.err.println("Second  time");
                }
                sets = sheet.toArray();
            }
            return sets;
        }
        // Method firing changes
        public void fireMethod(String s, Object o1, Object o2) {
            System.err.println("firing");
        }
    }
    
    //Node definition
    public class TNode2 extends AbstractNode {
        //create Node
        public TNode2() {
            super(Children.LEAF);
            setName("TNode2"); // or, super.setName if needed
            setDisplayName("TNode2");
        }
        //clone existing Node
        public Node cloneNode() {
            return new TNode();
        }
        
        public void replaceSets() {
            Sheet sheet = new Sheet();
            Sheet.Set props = sheet.get(Sheet.PROPERTIES);
            if (props == null) {
                props = Sheet.createPropertiesSet();
            }
            props.put(new TProperty("after - first", true));
            sheet.put(props);
            props = sheet.get(Sheet.EXPERT);
            if (props == null) {
                props = Sheet.createExpertSet();
            }
            props.put(new TProperty("after - second", true));
            sheet.put(props);
            setSheet(sheet);
        }
        
        // Create a property sheet:
        protected Sheet createSheet() {
            Sheet sheet = super.createSheet();
            // Make sure there is a "Properties" set:
            Sheet.Set props = sheet.get(Sheet.PROPERTIES);
            if (props == null) {
                props = Sheet.createPropertiesSet();
                sheet.put(props);
            }
            props.put(new TProperty("before", true));
            return sheet;
        }
        
        
    }
    
    // Property definition
    public class TProperty extends PropertySupport {
        private Object myValue = "Value";
        // Create new Property
        public TProperty(String name, boolean isWriteable) {
            super(name, String.class, name, "", true, isWriteable);
        }
        // get property value
        public Object getValue() {
            return myValue;
        }
        
        // set property value
        public void setValue(Object value) throws IllegalArgumentException,IllegalAccessException, InvocationTargetException {
            Object oldVal = myValue;
            myValue = value;
        }
    }
    
    private Exception throwMe2 = null;
    public void testSetNodesToNullReleasesOldNode() throws Exception {
        System.err.println(".testSetNodesToNullReleasesOldNode");
        TNode2 tnd = new TNode2();
        Reference oldNode = new WeakReference(tnd);
        throwMe2 = null;
        class R1 implements Runnable {
            Node[] nodes;
            R1 (Node n) {
                nodes = new Node[] {n};
            }
            
            public void run() {
                try {
                    ps.setNodes(nodes);
                } catch (Exception e) {
                    throwMe2 = e;
                }
            }
        }
        SwingUtilities.invokeAndWait(new R1(tnd));
        tnd = null;
        
        if (throwMe2 != null) {
            throw throwMe2;
        }
        sleep();
        
        int rowCount = ps.table.getRowCount();
        assertTrue("With a single property in a single property set, row count should be 2 but is " + rowCount, rowCount == 2);
        
        
        throwMe2 = null;
        
        SwingUtilities.invokeAndWait(new Runnable() {
            public void run() {
                try {
                    System.err.println("Replacing property sets");
                    ps.setNodes(new Node[] {});
                } catch (Exception e) {
                    throwMe2 = e;
                }
            }
        });
        if (throwMe2 != null) {
            throw throwMe2;
        }
        sleep();
        assertGC("Old node has to be released", oldNode);
    }
    
    
    private static TNode tn;
    private static TProperty tp;
    private static String initEditorValue;
    private static String initPropertyValue;
    private static String postChangePropertyValue;
    private static String postChangeEditorValue;
}
