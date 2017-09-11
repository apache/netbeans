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
        
        public boolean supportsCustomEditor() {
            return false;
        }
        
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
