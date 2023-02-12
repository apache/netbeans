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

package org.openide.explorer.propertysheet;

import java.awt.BorderLayout;
import java.awt.Graphics;
import java.awt.GraphicsEnvironment;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyEditor;
import java.beans.PropertyEditorSupport;
import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import javax.swing.JFrame;
import javax.swing.SwingUtilities;
import junit.framework.Test;
import junit.framework.TestSuite;
import org.netbeans.junit.NbTestCase;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.nodes.NodeEvent;
import org.openide.nodes.NodeListener;
import org.openide.nodes.NodeMemberEvent;
import org.openide.nodes.NodeReorderEvent;
import org.openide.nodes.PropertySupport;
import org.openide.nodes.Sheet;

// This test class tests the main functionality of the property sheet
public class NodeDeletionTest extends NbTestCase {

    public static Test suite() {
        return GraphicsEnvironment.isHeadless() ? new TestSuite() : new TestSuite(NodeDeletionTest.class);
    }

    private PropertySheet ps = null;
    public NodeDeletionTest(String name) {
        super(name);
    }
    
    protected boolean runInEQ() {
        return false;
    }
    
    private static boolean setup = false;
/*
 * This test creates a Property, Editor and Node. First test checks if initialized
 * editor contains the same value as property. The second checks if the property
 * value is changed if the same change will be done in the editor.
 */
    protected void setUp() throws Exception {
        // Create new TEditor
        te = new TEditor();
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
        final Node[] nodes = new Node[]{tn};
        SwingUtilities.invokeAndWait(new Runnable() {
            public void run() {
                ps.setNodes(nodes);
                //ps.setCurrentNode(tn);
                jf.show();
            }
        });
        
        jf.show();
        new ExtTestCase.WaitWindow(jf);
        
        try {
            // Wait for the initialization
            for (int i = 0; i < 10; i++) {
                final String asText = te.getAsText();
                if (asText == null || asText.equals("null")) {
                    //System.out.println("null");
                    Thread.sleep(1000);
                } else break;
            }
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
    
    
    public void testNodeDestroyClearsPropertySheet() throws Exception {
        int count = ps.table.getRowCount();
        assertTrue("Property sheet should contain one property set and one property but table has " + count + " rows", count==2);
        tn.destroy();
        Thread.currentThread().yield();
        Thread.currentThread().sleep(500);
        final int [] postCountRef = new int[] { count };
        SwingUtilities.invokeAndWait(new Runnable() {
            public void run() {
                int rc = ps.table.getRowCount();
                synchronized (postCountRef) {
                    postCountRef[0] = rc;
                }
            }
        });
        int postCount;
        synchronized (postCountRef) {
            postCount = postCountRef[0];
        }
        assertTrue("Property sheet should synchronously reflect node destruction" +
                " even if destroyed on a non EQ thread", postCount == 0);
    }
    
    public void testProxyNodeReflectsNodeDestruction() throws Exception {
        TNode node1 = new TNode();
        TNode node2 = new TNode();
        ProxyNode proxy = new ProxyNode(new Node[] {node1, node2});
        L l = new L();
        proxy.addNodeListener(l);
        
        node1.destroy();
        assertTrue("Proxy node should not represent a destroyed node",
                !Arrays.asList(proxy.getOriginalNodes()).contains(node1));
        
        l.assertNotDestroyed();
        
        node2.destroy();
        l.assertDestroyed();
    }
    
    private class L implements NodeListener {
        boolean destroyed = false;
        
        public void assertDestroyed() {
            assertTrue("Node was not destroyed", destroyed);
        }
        
        public void assertNotDestroyed() {
            assertTrue("Node was not destroyed", !destroyed);
        }
        
        public void childrenAdded(NodeMemberEvent ev) {
        }
        
        public void childrenRemoved(NodeMemberEvent ev) {
        }
        
        public void childrenReordered(NodeReorderEvent ev) {
        }
        
        public void nodeDestroyed(NodeEvent ev) {
            destroyed = true;
        }
        
        public void propertyChange(PropertyChangeEvent evt) {
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
        
        public void destroy() {
            fireNodeDestroyed();
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
            TProperty tp = new TProperty("property", true);
            props.put(tp);
            return sheet;
        }
        // Method firing changes
        public void fireMethod(String s, Object o1, Object o2) {
            firePropertyChange(s,o1,o2);
        }
    }
    
    // Property definition
    public class TProperty extends PropertySupport {
        private Object myValue = "Value";
        // Create new Property
        public TProperty(String name, boolean isWriteable) {
            super(name, Object.class, name, "", true, isWriteable);
        }
        // get property value
        public Object getValue() {
            return myValue;
        }
        
        // set property value
        public void setValue(Object value) throws IllegalArgumentException,IllegalAccessException, InvocationTargetException {
            Object oldVal = myValue;
            myValue = value;
            tn.fireMethod(getName(), oldVal, myValue);
        }
        // get the property editor
        public PropertyEditor getPropertyEditor() {
            return te;
        }
    }
    
    // Editor definition
    public class TEditor extends PropertyEditorSupport implements ExPropertyEditor {
        PropertyEnv env;
        
        // Create new TEditor
        public TEditor() {
        }
        
        /*
         * This method is called by the IDE to pass
         * the environment to the property editor.
         */
        public void attachEnv(PropertyEnv env) {
            this.env = env;
        }
        
        // Set that this Editor doesn't support custom Editor
        @Override
        public boolean supportsCustomEditor() {
            return false;
        }

        @Override
        public void addPropertyChangeListener(PropertyChangeListener l) {
            super.addPropertyChangeListener(l);
        }

        @Override
        public void removePropertyChangeListener(PropertyChangeListener l) {
            super.removePropertyChangeListener(l);
        }
        
        
        
        // Set the Property value threw the Editor
        @Override
        public void setValue(Object newValue) {
            super.setValue(newValue);
        }

        @Override
        public void firePropertyChange() {
            super.firePropertyChange();
        }
    }
    
    private TNode tn;
    private TProperty tp;
    private TEditor te;
}
