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
import java.beans.PropertyChangeListener;
import java.beans.PropertyEditor;
import java.beans.PropertyEditorSupport;
import java.lang.reflect.InvocationTargetException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JFrame;
import javax.swing.SwingUtilities;
import junit.framework.Test;
import junit.framework.TestSuite;
import org.netbeans.junit.NbTestCase;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.nodes.PropertySupport;
import org.openide.nodes.Sheet;

// This test class tests the main functionality of the property sheet
public class PropertySheetTest extends NbTestCase {

    public static Test suite() {
        return GraphicsEnvironment.isHeadless() ? new TestSuite() : new TestSuite(PropertySheetTest.class);
    }

    private static Logger LOG = Logger.getLogger(PropertySheetTest.class.getName());
    
    public PropertySheetTest(String name) {
        super(name);
    }
    
    @Override
    protected boolean runInEQ() {
        return false;
    }

    @Override
    protected Level logLevel() {
        return Level.INFO;
    }
    
    private static boolean setup = false;
/*
 * This test creates a Property, Editor and Node. First test checks if initialized
 * editor contains the same value as property. The second checks if the property
 * value is changed if the same change will be done in the editor.
 */
    protected void setUp() throws Exception {
        if (setup) return;
        setup = true;
        // Create new TestProperty
        tp = new TProperty("TProperty", true);
        // Create new TEditor
        te = new TEditor();
        // Create new TNode
        tn = new TNode();
        
        LOG.info("RUNNING ON THREAD " + Thread.currentThread());
        
        //Replacing NodeOp w/ JFrame to eliminate depending on full IDE init
        //and long delay while waiting for property sheet thus requested to
        //initialize
        final JFrame jf = new JFrame();
        final PropertySheet ps = new PropertySheet();
        jf.getContentPane().setLayout(new BorderLayout());
        jf.getContentPane().add(ps, BorderLayout.CENTER);
        jf.setLocation(30,30);
        jf.setSize(500,500);
        final Node[] nodes = new Node[]{tn};
        
        SwingUtilities.invokeAndWait(new Runnable() {
            public void run() {
                ps.setNodes(nodes);
                jf.show();
            }
        });
        
        
        jf.show();
        new ExtTestCase.WaitWindow(jf);
        
        LOG.info("Current node set ");
        try {
            
            // Wait for the initialization
            for (int i = 0; i < 10; i++) {
                final String asText = te.getAsText();
                if (asText == null || asText.equals("null")) {
                    LOG.info("Checking editor getAsText - " + te.getAsText());
                    //System.out.println("null");
                    Thread.sleep(1000);
                } else break;
            }
            // Test if the initialization was successfull
            
            initEditorValue = te.getAsText();
            LOG.info("Got initial editor value " + initEditorValue);
            
            initPropertyValue = tp.getValue().toString();
            LOG.info("Got initial property value " + initPropertyValue);
            
            
            //Set new value to the Property
            tp.setValue("Test2");
            postChangePropertyValue = tp.getValue().toString();
            
            LOG.info("Post change property value is " + postChangePropertyValue);
            
            
            // Wait for the reinitialization
            for (int i = 0; i < 100; i++) {
                if (te.getAsText().equals(initEditorValue)) {
                    //LOG.info(i + " value not updated ");
                    Thread.sleep(50);
                } else {
                    LOG.info("value was updated");
                    break;
                }
            }
            
            //issues 39205 & 39206 - ensure the property sheet really repaints
            //before we get the value, or the value in the editor will not
            //have changed
            SwingUtilities.invokeAndWait(new Runnable() {
                public void run() {
                    Graphics g = ps.getGraphics();
                    ps.paintImmediately(0,0,ps.getWidth(), ps.getHeight());
                }
            });
            
            // Test if the reinitialization was successfull
            postChangeEditorValue = te.getAsText();
            LOG.info("postEditorChangeValue = " + postChangeEditorValue);
        } finally {
            jf.hide();
            jf.dispose();
        }
    }
    
    public void testInitializeEditorValue() throws Exception {
        assertTrue("Editor wasn't initialized successfuly (null) - value was " + initEditorValue,!initEditorValue.equals("null"));
    }
    
    public void testPropertyEQEditorValueAfterInit() throws Exception {
        assertEquals("Editor was initialized to the same value as the Property, value was " + initPropertyValue, initPropertyValue, initEditorValue);
    }
    
    public void testSetPropertyValue() throws Exception {
        assertTrue("Property value wasn't successfuly changed. Initial property value, " + initPropertyValue + " should not match " + postChangePropertyValue,!initPropertyValue.equals(postChangePropertyValue));
    }
    
    public void testSetEditorValue() throws Exception {
        assertTrue("Editor value wasn't changed successfuly. Initial editor value, " + initEditorValue + " should not match " + postChangeEditorValue,!initEditorValue.equals(postChangeEditorValue));
    }
    
    public void testPropertyEQEditorValueAfterChange() throws Exception {
        assertEquals("Editor value doesn't reflect the Property value. Post change property value, " + postChangePropertyValue + " should equal " + postChangeEditorValue, postChangePropertyValue, postChangeEditorValue);
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
        
        // Create a property sheet:
        protected Sheet createSheet() {
            Sheet sheet = super.createSheet();
            // Make sure there is a "Properties" set:
            Sheet.Set props = sheet.get(Sheet.PROPERTIES);
            if (props == null) {
                props = Sheet.createPropertiesSet();
                sheet.put(props);
            }
            props.put(tp);
            return sheet;
        }
        // Method firing changes
        public void fireMethod(String s, Object o1, Object o2) {
            LOG.info("TNode firing change " + s + " from " + o1 + " to " + o2);
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
            LOG.info("TProperty setValue: " + value);
            Object oldVal = myValue;
            myValue = value;
            LOG.info("TProperty triggering node property change");
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
            LOG.info("Property change listener added to property editor " + System.identityHashCode(this) + " - " + l);
            super.addPropertyChangeListener(l);
        }

        @Override
        public void removePropertyChangeListener(PropertyChangeListener l) {
            LOG.info("Property change listener removed from property editor " + System.identityHashCode(this) + " - " + l);
            super.removePropertyChangeListener(l);
        }
        
        
        
        // Set the Property value threw the Editor
        @Override
        public void setValue(Object newValue) {
            LOG.info("TEditor.setValue: " + newValue);
            super.setValue(newValue);
        }

        @Override
        public void firePropertyChange() {
            LOG.info("TEditor.firePropertyChange");
            super.firePropertyChange();
        }
    }
    
    private static TNode tn;
    private static TProperty tp;
    private static TEditor te;
    private static String initEditorValue;
    private static String initPropertyValue;
    private static String postChangePropertyValue;
    private static String postChangeEditorValue;
}
