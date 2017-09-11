/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
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

