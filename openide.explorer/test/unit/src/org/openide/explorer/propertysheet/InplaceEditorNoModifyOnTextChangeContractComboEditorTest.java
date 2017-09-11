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
        
        public String[] getTags() {
            return new String[] {"a","b","c","d","Value"};
        }
        
        public void attachEnv(PropertyEnv env) {
            this.env = env;
        }
        
        public boolean supportsCustomEditor() {
            return false;
        }
        
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
