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
import java.awt.Dimension;
import java.awt.KeyboardFocusManager;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.beans.PropertyEditor;
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

public class InplaceEditorNoModifyOnTextChangeContractBooleanEditorTest extends NbTestCase {
    public InplaceEditorNoModifyOnTextChangeContractBooleanEditorTest(String name) {
        super(name);
    }
    
    static Component edComp = null;
    static PropertyEditor ped = null;
    static InplaceEditor ied = null;
    static ActionEvent[] events = new ActionEvent[10];
    static Object postSetValuePropertyEdValue=null;
    static Object preSetValuePropertyEdValue=null;
    static Object finalValuePropertyEdValue=null;
    static Object finalInplaceEditorValue=null;
    
    int i=0;
    
    private int idx=0;
    
    private static InplaceEditorFactory factory = new InplaceEditorFactory(true, new ReusablePropertyEnv());
    
    private static boolean canRun = ExtTestCase.canSafelyRunFocusTests();
    
    private static boolean setup = false;
    protected void setUp() throws Exception {
        if (!canRun) {
            return;
        }
        
        PropUtils.forceRadioButtons=false;
        factory.setUseRadioBoolean(false);
        
        tp = new TProperty("TProperty", true);
        
        ActionListener al = new ActionListener() {
            public void actionPerformed(ActionEvent ae) {
                System.err.println("Got an action event - " + ae.getActionCommand());
                events[idx] = ae;
            }
        };
        
        try {
            ied = factory.getInplaceEditor(tp, false);
            edComp = ied.getComponent();
            System.err.println("EdComp is " + edComp);
            
            
            
            ped = ied.getPropertyEditor();
            
            preSetValuePropertyEdValue=ped.getValue();
            ied.setValue("newValue");
            
            sleep();
            postSetValuePropertyEdValue=ped.getValue();
            
            edComp = ied.getComponent();
            JFrame jf = new JFrame();
            jf.getContentPane().add(edComp);
            jf.setLocation(new Point(20,20));
            jf.setSize(new Dimension(30, 200));
            new ExtTestCase.WaitWindow(jf);
            
            sleep();
            sleep();
            sleep();
            
            while (!edComp.isShowing()) {
                
            }
            
            new ExtTestCase.WaitFocus(edComp);
            
            ied.addActionListener(al);
            
            Component comp = KeyboardFocusManager.getCurrentKeyboardFocusManager().getPermanentFocusOwner();
            canRun = edComp == comp;
            if (!canRun) {
                System.err.println("Platform focus behavior not sane - aborting tests");
            }
            
            sleep();
            System.err.println("Sending key pressed - space");
            KeyEvent ke = new KeyEvent(edComp, KeyEvent.KEY_PRESSED, System.currentTimeMillis(), 0, KeyEvent.VK_SPACE, (char) KeyEvent.VK_SPACE);
            dispatchEvent(ke, edComp);
            
            sleep();
            System.err.println("Sending key released - space");
            ke = new KeyEvent(edComp, KeyEvent.KEY_RELEASED, System.currentTimeMillis(), 0, KeyEvent.VK_SPACE, (char) KeyEvent.VK_SPACE);
            dispatchEvent(ke, edComp);
            
            sleep();
            
            System.err.println("Sending key pressed - enter");
            ke = new KeyEvent(edComp, KeyEvent.KEY_PRESSED, System.currentTimeMillis(), 0, KeyEvent.VK_ENTER, (char) KeyEvent.VK_ENTER);
            dispatchEvent(ke, edComp);
            sleep();
            
            System.err.println("Sending key released - enter");
            ke = new KeyEvent(edComp, KeyEvent.KEY_RELEASED, System.currentTimeMillis(), 0, KeyEvent.VK_ENTER, (char) KeyEvent.VK_ENTER);
            dispatchEvent(ke, edComp);
            
            sleep();
            
            idx++;
            
            sleep();
            
            ke = new KeyEvent(edComp, KeyEvent.KEY_PRESSED, System.currentTimeMillis(), 0, KeyEvent.VK_ESCAPE, (char) KeyEvent.VK_ESCAPE);
            dispatchEvent(ke, edComp);
            ke = new KeyEvent(edComp, KeyEvent.KEY_RELEASED, System.currentTimeMillis(), 0, KeyEvent.VK_ESCAPE, (char) KeyEvent.VK_ESCAPE);
            dispatchEvent(ke, edComp);
            
            sleep();
            sleep();
            
            finalInplaceEditorValue = ied.getValue();
            jf.hide();
            jf.dispose();
            sleep();
            
            finalValuePropertyEdValue = ped.getValue();
            ied.removeActionListener(al);
            ied.clear();
        } catch (Exception e) {
            e.printStackTrace();
            fail("FAILED - Exception thrown "+e.getClass().toString());
        }
        setup = true;
    }
    
    public void testInplaceEditorSetValueDidNotChangePropertyEditorValue() throws Exception {
        if (!canRun) return;
        assertTrue("PreSetValue value is " + preSetValuePropertyEdValue + " but post value is " + postSetValuePropertyEdValue, preSetValuePropertyEdValue == postSetValuePropertyEdValue);
    }
    
    public void testEnterTriggeredActionSuccess() {
        if (!canRun) return;
        assertTrue("Enter keystroke did not produce an action event", events[0] != null);
        assertTrue("Action command for faked Enter keystroke should be " + InplaceEditor.COMMAND_SUCCESS + " but is " + events[0].getActionCommand(), InplaceEditor.COMMAND_SUCCESS.equals(events[0].getActionCommand()));
    }
    
    public void testFinalInplaceEditorValue() throws Exception {
        if (!canRun) return;
        assertTrue("Final inplace editor value should be Boolean.FALSE but is " + finalInplaceEditorValue, Boolean.FALSE.equals(finalInplaceEditorValue));
    }
    
    public void testFinalPropertyValueIsUnchanged() {
        if (!canRun) return;
        assertTrue("Final value should be unchanged but is " + finalValuePropertyEdValue, Boolean.TRUE.equals(finalValuePropertyEdValue));
    }
    
    // Property definition
    public class TProperty extends PropertySupport {
        private Boolean myValue = Boolean.TRUE;
        // Create new Property
        public TProperty(String name, boolean isWriteable) {
            super(name, Boolean.class, name, "", true, isWriteable);
        }
        // get property value
        public Object getValue() {
            return myValue;
        }
        // set property value
        public void setValue(Object value) throws IllegalArgumentException,IllegalAccessException, InvocationTargetException {
            myValue = (Boolean) value;
        }
    }
    
    private void sleep() throws Exception {
        Thread.currentThread().sleep(100);
        SwingUtilities.invokeAndWait(new Runnable() {
            public void run() {
                System.currentTimeMillis();
            }
        });
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
    }
    
    static {
        ExtTestCase.installCorePropertyEditors();
    }
    
    private TProperty tp;
    private String initEditorValue;
    private String initPropertyValue;
    private String postChangePropertyValue;
    private String postChangeEditorValue;
}

