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
import java.beans.PropertyEditor;
import java.beans.PropertyEditorSupport;
import java.lang.reflect.InvocationTargetException;
import javax.swing.JComboBox;
import org.netbeans.junit.NbTestCase;
import org.openide.nodes.PropertySupport;

/** Tests basic functionality of InplaceEditorFactory and its code to
 *  correctly configure a property editor and associated InplaceEditor
 *  with the data encapsulated by a Node.Property.
 *
 * @author Tim Boudreau
 */

public class InplaceEditorFactoryTest extends NbTestCase {
    public InplaceEditorFactoryTest(String name) {
        super(name);
    }
    
    Component edComp = null;
    PropertyEditor ped = null;
    InplaceEditor ied = null;
    
    protected void setUp() throws Exception {
        PropUtils.forceRadioButtons=false;
        // Create new TestProperty
        tp = new TProperty("TProperty", true);
        // Create new TEditor
        te = new TagsEditor();
        
        try {
            ied = new InplaceEditorFactory(true, new ReusablePropertyEnv()).getInplaceEditor(tp, false);
            edComp = ied.getComponent();
            ped = ied.getPropertyEditor();
        } catch (Exception e) {
            fail("FAILED - Exception thrown "+e.getClass().toString());
        }
    }
    
    public void testInplaceIsCombo() throws Exception {
        assertTrue("Editor for tagged value not a combo box", edComp instanceof JComboBox);
    }
    
    public void testCorrectInplaceEditorValue() throws Exception {
        assertTrue("InplaceEditor.getValue() returns " + ied.getValue() + " should be \"Value\"", "Value".equals(ied.getValue()));
    }
    
    public void testCorrectPropertyEditorValue() throws Exception {
        assertTrue("PropertyEditor.getValue() returns " + ped.getValue() + " should be \"Value\"", "Value".equals(ped.getValue()));
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
