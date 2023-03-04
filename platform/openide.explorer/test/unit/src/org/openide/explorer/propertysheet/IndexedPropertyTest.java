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
import java.awt.GraphicsEnvironment;
import java.beans.PropertyChangeListener;
import java.beans.PropertyEditor;
import java.beans.PropertyEditorSupport;
import java.lang.reflect.InvocationTargetException;
import javax.swing.JFrame;
import javax.swing.SwingUtilities;
import junit.framework.Test;
import junit.framework.TestSuite;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.nodes.Sheet;

/**
 * Ensures that the proper property editor is used for indexed properties
 */
public class IndexedPropertyTest extends ExtTestCase {

    public static Test suite() {
        return GraphicsEnvironment.isHeadless() ? new TestSuite() : new TestSuite(IndexedPropertyTest.class);
    }

    private PropertySheet ps = null;
    public IndexedPropertyTest(String name) {
        super(name);
        super.installCorePropertyEditors();
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
        
        SwingUtilities.invokeAndWait(new Runnable() {
            public void run() {
                ps.setNodes(new Node[] {tn});
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
    
    public void testIndexedProperty() throws Exception {
        System.err.println("Plain Editor: " + PropUtils.getPropertyEditor(plain));
        System.err.println("Fancy Editor: " + PropUtils.getPropertyEditor(fancy));
        
        assertTrue("Plain editor should be an IndexedPropertyEditor ", PropUtils.getPropertyEditor(plain) instanceof IndexedPropertyEditor);
        assertTrue("Overridden editor should be used if present", PropUtils.getPropertyEditor(fancy) == te);
        
    }
    
    
    //Node definition
    public class TNode extends AbstractNode {
        //create Node
        public TNode() throws Exception {
            super(Children.LEAF);
            setName("TNode"); // or, super.setName if needed
            setDisplayName("TNode");
        }
        //clone existing Node
        public Node cloneNode() {
            try {
                return new TNode();
            } catch (Exception e) {
                throw new RuntimeException("Failed to clone node");
            }
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
            props.put(plain);
            props.put(fancy);
            return sheet;
        }
        // Method firing changes
        public void fireMethod(String s, Object o1, Object o2) {
            firePropertyChange(s,o1,o2);
        }
    }
    
    PlainIndexedProperty plain = new PlainIndexedProperty();
    FancyIndexedProperty fancy = new FancyIndexedProperty();
    
    public class PlainIndexedProperty extends Node.IndexedProperty {
        private StringBuffer value = new StringBuffer(getClass().getName());
        public PlainIndexedProperty() {
            super(char[].class, Character.TYPE);
            setDisplayName("Plain");
            setName(getDisplayName());
        }
        
        public boolean canIndexedRead() {
            return true;
        }
        
        public boolean canIndexedWrite() {
            return true;
        }
        
        public boolean canRead() {
            return true;
        }
        
        public boolean canWrite() {
            return true;
        }
        
        public PropertyEditor getPropertyEditor() {
            return null;
        }
        
        public Object getIndexedValue(int index) throws IllegalAccessException, IllegalArgumentException, InvocationTargetException {
            return new Character(value.charAt(index));
        }
        
        public Object getValue() throws IllegalAccessException, InvocationTargetException {
            return value.toString().toCharArray();
        }
        
        public void setIndexedValue(int indx, Object val) throws IllegalAccessException, IllegalArgumentException, InvocationTargetException {
            String old = value.toString();
            value.setCharAt(indx, ((Character) val).charValue());
            tn.fireMethod(getName(), old, val);
        }
        
        public void setValue(Object val) throws IllegalAccessException, IllegalArgumentException, InvocationTargetException {
            String old = value.toString();
            value = new StringBuffer(val == null ? "" : val.toString());
            tn.fireMethod(getName(), old, val);
        }
        
        public int hashCode() {
            return 23;
        }
        
        public boolean equals(Object o) {
            return o == this;
        }
    }
    
    public class FancyIndexedProperty extends PlainIndexedProperty {
        public FancyIndexedProperty() {
            setDisplayName("Fancy");
            setName(getDisplayName());
        }

        @Override
        public PropertyEditor getPropertyEditor() {
            return te;
        }

        @Override
        public int hashCode() {
            return 24;
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
    private TEditor te;
}
