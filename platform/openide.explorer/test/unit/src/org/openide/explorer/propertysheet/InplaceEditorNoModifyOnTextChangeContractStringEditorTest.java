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
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.beans.PropertyEditor;
import java.beans.PropertyEditorSupport;
import java.lang.reflect.InvocationTargetException;
import javax.swing.JFrame;
import javax.swing.SwingUtilities;
import org.netbeans.junit.NbTestCase;
import org.openide.nodes.PropertySupport;

/** Tests the contract that an inplace editor will not modify the property
 *  editor if its value changes (the infrastructure should do this by
 *  accepting the COMMAND_SUCCESS action event).
 *
 * @author Tim Boudreau
 */
public class InplaceEditorNoModifyOnTextChangeContractStringEditorTest extends NbTestCase {
    public InplaceEditorNoModifyOnTextChangeContractStringEditorTest(String name) {
        super(name);
    }
    
    static Component edComp = null;
    static PropertyEditor ped = null;
    static InplaceEditor ied = null;
    static ActionEvent[] events = new ActionEvent[10];
    static Object postSetValuePropertyEdValue=null;
    static Object preSetValuePropertyEdValue=null;
    static Object finalValuePropertyEdValue=null;
    
    private static int idx=0;
    private static InplaceEditorFactory factory = new InplaceEditorFactory(true, new ReusablePropertyEnv());
    
    static boolean canRun = ExtTestCase.canSafelyRunFocusTests();
    
    protected void setUp() throws Exception {
        if (!canRun) {
            return;
        }
        PropUtils.forceRadioButtons=false;
        if (idx != 0) return;
        // Create new TestProperty
        tp = new TProperty("TProperty", true);
        // Create new TEditor
        te = new TagsEditor();
        
        ActionListener al = new ActionListener() {
            public void actionPerformed(ActionEvent ae) {
                events[idx] = ae;
            }
        };
        
        try {
            ied = factory.getInplaceEditor(tp, false);
            edComp = ied.getComponent();
            ped = ied.getPropertyEditor();
            
            preSetValuePropertyEdValue=ped.getValue();
            ied.setValue("newValue");
            
            postSetValuePropertyEdValue=ped.getValue();
            
            edComp = ied.getComponent();
            JFrame jf = new JFrame();
            jf.getContentPane().add(edComp);
            jf.setSize(200,200);
            new ExtTestCase.WaitWindow(jf);
            
            new ExtTestCase.WaitFocus(edComp);
            
            ied.addActionListener(al);
            sleep();
            
            final KeyEvent ke = new KeyEvent(edComp, KeyEvent.KEY_PRESSED, System.currentTimeMillis(), 0, KeyEvent.VK_ENTER, (char) KeyEvent.VK_ENTER);
            SwingUtilities.invokeAndWait(new Runnable() {
                public void run() {
                    edComp.dispatchEvent(ke);
                }
            });
            sleep();
            final KeyEvent ke2 = new KeyEvent(edComp, KeyEvent.KEY_RELEASED, System.currentTimeMillis(), 0, KeyEvent.VK_ENTER, (char) KeyEvent.VK_ENTER);
            SwingUtilities.invokeAndWait(new Runnable() {
                public void run() {
                    edComp.dispatchEvent(ke2);
                }
            });
            sleep();
            sleep();
            sleep();
            
            idx++;
            finalValuePropertyEdValue = ped.getValue();
            jf.hide();
            jf.dispose();
            ied.removeActionListener(al);
            ied.clear();
        } catch (Exception e) {
            e.printStackTrace();
            fail("FAILED - Exception thrown "+e.getClass().toString());
        }
    }
    
    private void sleep() {
        try {
            ExtTestCase.sleep();
        } catch (Exception e) {
            //do nothing
        }
    }
    
    public void testInplaceEditorSetValueDidNotChangePropertyEditorValue() throws Exception {
        if (!canRun) return;
        assertTrue("PreSetValue value is " + preSetValuePropertyEdValue + " but post value is " + postSetValuePropertyEdValue, preSetValuePropertyEdValue == postSetValuePropertyEdValue);
    }
    
    public void testEnterTriggeredActionSuccess() {
        if (!canRun) return;
        try {
            Thread.currentThread().sleep(2000);
        } catch (Exception e) {
        }
        assertTrue("Enter keystroke did not produce an action event", events[0] != null);
        assertTrue("Action command for faked Enter keystroke should be " + InplaceEditor.COMMAND_SUCCESS + " but is " + events[0].getActionCommand(), InplaceEditor.COMMAND_SUCCESS.equals(events[0].getActionCommand()));
    }
    
    public void testFinalPropertyValueIsUnchanged() {
        if (!canRun) return;
        assertTrue("Final value should be unchanged but is " + finalValuePropertyEdValue, "Value".equals(finalValuePropertyEdValue));
    }
    
    // Property definition
    public class TProperty extends PropertySupport {
        private String myValue = "Value";
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
    
    public class TagsEditor extends PropertyEditorSupport implements ExPropertyEditor {
        PropertyEnv env;
        
        public TagsEditor() {
        }
        
        public void attachEnv(PropertyEnv env) {
            this.env = env;
        }

        @Override
        public boolean supportsCustomEditor() {
            return false;
        }

        @Override
        public void setValue(Object newValue) {
            super.setValue(newValue);
        }
    }
    
    private TProperty tp;
    private TagsEditor te;
    private String initEditorValue;
    private String initPropertyValue;
    private String postChangePropertyValue;
    private String postChangeEditorValue;
}
