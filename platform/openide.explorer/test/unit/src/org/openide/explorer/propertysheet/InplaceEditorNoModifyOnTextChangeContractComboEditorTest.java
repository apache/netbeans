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
public class InplaceEditorNoModifyOnTextChangeContractComboEditorTest extends NbTestCase {
    public InplaceEditorNoModifyOnTextChangeContractComboEditorTest(String name) {
        super(name);
    }
    
    Component edComp = null;
    PropertyEditor ped = null;
    InplaceEditor ied = null;
    ActionEvent[] events = new ActionEvent[10];
    Object postSetValuePropertyEdValue=null;
    Object preSetValuePropertyEdValue=null;
    Object finalValuePropertyEdValue=null;
    Object finalInplaceEditorValue=null;
    
    int i=0;
    
    private static boolean canRun = ExtTestCase.canSafelyRunFocusTests();
    
    private static InplaceEditorFactory factory = new InplaceEditorFactory(true, new ReusablePropertyEnv());
    private int idx=0;
    protected void setUp() throws Exception {
        if (!canRun) {
            return;
        }
        PropUtils.forceRadioButtons=false;
        
        tp = new TProperty("TProperty", true);
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
            sleep();
            
            postSetValuePropertyEdValue=ped.getValue();
            
            edComp = ied.getComponent();
            JFrame jf = new JFrame();
            jf.getContentPane().add(edComp);
            new ExtTestCase.WaitWindow(jf);
            
            new ExtTestCase.WaitFocus(edComp);
            
            ied.addActionListener(al);
            
            KeyEvent ke = new KeyEvent(edComp, KeyEvent.KEY_PRESSED, System.currentTimeMillis(), 0, KeyEvent.VK_UP, (char) KeyEvent.VK_UP);
            dispatchEvent(ke, edComp);
            sleep();
            
            ke = new KeyEvent(edComp, KeyEvent.KEY_RELEASED, System.currentTimeMillis(), 0, KeyEvent.VK_UP, (char) KeyEvent.VK_UP);
            dispatchEvent(ke, edComp);
            sleep();
            
            ke = new KeyEvent(edComp, KeyEvent.KEY_PRESSED, System.currentTimeMillis(), 0, KeyEvent.VK_UP, (char) KeyEvent.VK_UP);
            dispatchEvent(ke, edComp);
            sleep();
            ke = new KeyEvent(edComp, KeyEvent.KEY_RELEASED, System.currentTimeMillis(), 0, KeyEvent.VK_UP, (char) KeyEvent.VK_UP);
            dispatchEvent(ke, edComp);
            sleep();
            
            ke = new KeyEvent(edComp, KeyEvent.KEY_PRESSED, System.currentTimeMillis(), 0, KeyEvent.VK_ENTER, (char) KeyEvent.VK_ENTER);
            dispatchEvent(ke, edComp);
            sleep();
            
            ke = new KeyEvent(edComp, KeyEvent.KEY_RELEASED, System.currentTimeMillis(), 0, KeyEvent.VK_ENTER, (char) KeyEvent.VK_ENTER);
            dispatchEvent(ke, edComp);
            sleep();
            sleep();
            
            ke = new KeyEvent(edComp, KeyEvent.KEY_PRESSED, System.currentTimeMillis(), 0, KeyEvent.VK_ENTER, (char) KeyEvent.VK_ENTER);
            dispatchEvent(ke, edComp);
            sleep();
            ke = new KeyEvent(edComp, KeyEvent.KEY_RELEASED, System.currentTimeMillis(), 0, KeyEvent.VK_ENTER, (char) KeyEvent.VK_ENTER);
            dispatchEvent(ke, edComp);
            sleep();
            
            idx++;
            
            ke = new KeyEvent(edComp, KeyEvent.KEY_PRESSED, System.currentTimeMillis(), 0, KeyEvent.VK_ESCAPE, (char) KeyEvent.VK_ESCAPE);
            dispatchEvent(ke, edComp);
            sleep();
            ke = new KeyEvent(edComp, KeyEvent.KEY_RELEASED, System.currentTimeMillis(), 0, KeyEvent.VK_ESCAPE, (char) KeyEvent.VK_ESCAPE);
            dispatchEvent(ke, edComp);
            sleep();
            
            finalInplaceEditorValue = ied.getValue();
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
    
    private void sleep() throws Exception {
        Thread.currentThread().sleep(100);
        if (!SwingUtilities.isEventDispatchThread()) {
            SwingUtilities.invokeAndWait(new Runnable() {
                public void run() {
                    System.currentTimeMillis();
                }
            });
        }
        Thread.currentThread().sleep(100);
    }
    
    
    private void dispatchEvent(final KeyEvent ke, final Component comp) throws Exception {
        if (!SwingUtilities.isEventDispatchThread()) {
            SwingUtilities.invokeAndWait(new Runnable() {
                public void run() {
                    comp.dispatchEvent(ke);
                }
            });
            
        }  else {
            comp.dispatchEvent(ke);
        }
        sleep();
    }
    
    public void testInplaceEditorSetValueDidNotChangePropertyEditorValue() throws Exception {
        if (ExtTestCase.canSafelyRunFocusTests()) {
            assertTrue("PreSetValue value is " + preSetValuePropertyEdValue + " but post value is " + postSetValuePropertyEdValue, preSetValuePropertyEdValue == postSetValuePropertyEdValue);
        }
    }
    
    public void testEnterTriggeredActionSuccess() {
        if (ExtTestCase.canSafelyRunFocusTests()) {
            assertTrue("Enter keystroke did not produce an action event", events[0] != null);
            assertTrue("Action command for faked Enter keystroke should be " + InplaceEditor.COMMAND_SUCCESS + " but is " + events[0].getActionCommand(), InplaceEditor.COMMAND_SUCCESS.equals(events[0].getActionCommand()));
        }
    }
    
    public void testFinalInplaceEditorValue() throws Exception {
        if (ExtTestCase.canSafelyRunFocusTests()) {
            assertTrue("Final inplace editor value should be \"c\" but is " + finalInplaceEditorValue, "c".equals(finalInplaceEditorValue));
        }
    }
    
    public void testEscTriggeredActionFailure() {
        if (ExtTestCase.canSafelyRunFocusTests()) {
            assertTrue("Escape keystroke did not produce an action event", events[1] != null);
            assertTrue("Action command for faked Escape keystroke should be " + InplaceEditor.COMMAND_FAILURE + " but is " + events[1].getActionCommand(), InplaceEditor.COMMAND_FAILURE.equals(events[1].getActionCommand()));
        }
    }
    
    public void testFinalPropertyValueIsUnchanged() {
        if (ExtTestCase.canSafelyRunFocusTests()) {
            assertTrue("Final value should be unchanged but is " + finalValuePropertyEdValue, "Value".equals(finalValuePropertyEdValue));
        }
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

        @Override
        public String[] getTags() {
            return new String[] {"a","b","c","d","Value"};
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
