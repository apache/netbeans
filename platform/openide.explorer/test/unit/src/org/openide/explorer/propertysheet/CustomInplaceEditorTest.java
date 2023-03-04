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

import java.awt.Component;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import java.beans.PropertyEditor;
import java.beans.PropertyEditorSupport;
import java.lang.reflect.InvocationTargetException;
import javax.swing.JComponent;
import javax.swing.KeyStroke;
import org.netbeans.junit.NbTestCase;
import org.openide.nodes.PropertySupport;

/** Tests basic functionality of InplaceEditorFactory and its code to
 *  correctly configure a property editor and associated InplaceEditor
 *  with the data encapsulated by a Node.Property.
 *
 * @author Tim Boudreau
 */
public class CustomInplaceEditorTest extends NbTestCase {
    public CustomInplaceEditorTest(String name) {
        super(name);
    }
    
    Component edComp = null;
    PropertyEditor ped = null;
    InplaceEditor ied = null;
    InplaceEditor ied2 = null;
    private static InplaceEditorFactory factory = new InplaceEditorFactory(true, new ReusablePropertyEnv());
    
    protected void setUp() throws Exception {
        // Create new TestProperty
        tp = new TProperty("TProperty", true);
        // Create new TEditor
        te = new TEditor();
        
        TProperty2 tp2 = new TProperty2("TProperty2", true);
        
        try {
            ied = factory.getInplaceEditor(tp, false);
            ied2 = factory.getInplaceEditor(tp2, false);
            edComp = ied.getComponent();
            ped = ied.getPropertyEditor();
        } catch (Exception e) {
            e.printStackTrace();
            fail("FAILED - Exception thrown "+e.getClass().toString());
        }
    }
    
    public void testRegisterInplaceEditorViaPropertyEnv() throws Exception {
        assertTrue("Inplace editor should be instance of test class registered by PropertyEnv.registerInplaceEditor, but is instance of " + ied.getClass(), ied instanceof TInplaceEditor);
    }
    
    public void testRegisterInplaceEditorViaHint() throws Exception {
        assertTrue("Inplace editor should be instance of test class as returned by TProperty2.getValue(\"inplaceEditor\"), but is instance of " + ied2.getClass(), ied2 instanceof TInplaceEditor);
    }
    
    // Property definition
    public class TProperty2 extends PropertySupport {
        private Boolean myValue = Boolean.TRUE;
        // Create new Property
        public TProperty2(String name, boolean isWriteable) {
            super(name, Boolean.class, name, "", true, isWriteable);
        }
        // get property value
        public Object getValue() {
            return myValue;
        }
        
        public Object getValue(String key) {
            if ("inplaceEditor".equals(key)) {
                return new TInplaceEditor();
            } else {
                return super.getValue(key);
            }
        }
        
        // set property value
        public void setValue(Object value) throws IllegalArgumentException,IllegalAccessException, InvocationTargetException {
            myValue = (Boolean) value;
        }
    }
    
    // Property definition
    public class TProperty extends PropertySupport {
        private String myValue = "foo";
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
            myValue = value.toString();
        }
        // get the property editor
        public PropertyEditor getPropertyEditor() {
            return te;
        }
    }
    
    public class TEditor extends PropertyEditorSupport implements ExPropertyEditor, InplaceEditor.Factory {
        PropertyEnv env;
        
        public TEditor() {
        }
        
        public void attachEnv(PropertyEnv env) {
            this.env = env;
            env.registerInplaceEditorFactory(this);
        }

        @Override
        public boolean supportsCustomEditor() {
            return false;
        }

        @Override
        public void setValue(Object newValue) {
            super.setValue(newValue);
        }
        
        public InplaceEditor getInplaceEditor() {
            return new TInplaceEditor();
        }
        
    }
    
    public class TInplaceEditor extends JComponent implements InplaceEditor {
        PropertyEditor pe=null;
        public void clear() {
        }
        
        public void connect(PropertyEditor pe, PropertyEnv env) {
            this.pe = pe;
        }
        
        public JComponent getComponent() {
            return this;
        }
        
        public KeyStroke[] getKeyStrokes() {
            return null;
        }
        
        public PropertyEditor getPropertyEditor() {
            return pe;
        }
        
        public PropertyModel getPropertyModel() {
            return null;
        }
        
        public Object getValue() {
            return null;
        }
        
        public void handleInitialInputEvent(InputEvent e) {
        }
        
        public boolean isKnownComponent(Component c) {
            return false;
        }
        
        public void reset() {
        }
        
        public void setPropertyModel(PropertyModel pm) {
        }
        
        public void setValue(Object o) {
        }
        
        public boolean supportsTextEntry() {
            return false;
        }
        
        public void addActionListener(ActionListener al) {
        }
        
        public void removeActionListener(ActionListener al) {
        }
        
    }
    
    private TProperty tp;
    private TEditor te;
    private String initEditorValue;
    private String initPropertyValue;
    private String postChangePropertyValue;
    private String postChangeEditorValue;
}
