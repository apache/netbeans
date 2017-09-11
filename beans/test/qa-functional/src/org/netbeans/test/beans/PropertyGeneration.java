/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2014 Oracle and/or its affiliates. All rights reserved.
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
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2014 Sun Microsystems, Inc.
 */
package org.netbeans.test.beans;

import junit.framework.Test;
import org.netbeans.jellytools.EditorOperator;
import org.netbeans.jellytools.NbDialogOperator;
import org.netbeans.jemmy.EventTool;
import org.netbeans.jemmy.operators.JTextFieldOperator;
import org.netbeans.junit.NbModuleSuite;
import org.netbeans.test.beans.operators.AddProperty;

/**
 *
 * @author jprox
 */
public class PropertyGeneration extends BeansTestCase {

    EditorOperator operator;

    public PropertyGeneration(String testName) {
        super(testName);
    }

    public void testDefault() {
        try {
            operator = openEditor("Beans");
            operator.setCaretPosition(4, 1);
            openDialog(operator);
            AddProperty addProperty = new AddProperty();
            addProperty.ok();
            checkEditorContent(operator);
        } finally {
            if (operator != null) {
                operator.closeDiscard();
            }
        }
    }
    
    public void testChangeName() {
        try {
            operator = openEditor("Beans");
            operator.setCaretPosition(4, 1);
            openDialog(operator);
            AddProperty addProperty = new AddProperty();
            addProperty.txtName().clearText();
            addProperty.txtName().typeText("name");                                    
            assertEquals("PROP_NAME", addProperty.txtBoundName().getText());
            addProperty.ok();
            checkEditorContent(operator);
        } finally {
            if (operator != null) {
                operator.closeDiscard();
            }
        }
    }
    
    public void testDefaultValue() {
        try {
            operator = openEditor("Beans");
            operator.setCaretPosition(4, 1);
            openDialog(operator);
            AddProperty addProperty = new AddProperty();
            addProperty.txtDefaultValue().clearText();
            addProperty.txtDefaultValue().typeText("\"default\"");                                    
            addProperty.ok();
            checkEditorContent(operator);
        } finally {
            if (operator != null) {
                operator.closeDiscard();
            }
        }
    }
    
    public void testTypeManual() {
        try {
            operator = openEditor("Beans");
            operator.setCaretPosition(4, 1);
            openDialog(operator);
            AddProperty addProperty = new AddProperty();
            addProperty.cboType().clearText();            
            addProperty.cboType().typeText("Double");            
            addProperty.btOK().requestFocus();
            addProperty.ok();            
            checkEditorContent(operator);
        } finally {
            if (operator != null) {
                operator.closeDiscard();
            }
        }
    }
    
    public void testTypeSelect() {
        try {
            operator = openEditor("Beans");
            operator.setCaretPosition(4, 1);
            openDialog(operator);
            AddProperty addProperty = new AddProperty();
            addProperty.cboType().selectItem("long");            
            addProperty.ok();
            checkEditorContent(operator);
        } finally {
            if (operator != null) {
                operator.closeDiscard();
            }
        }
    }
    
    public void testTypeBrowse() {
        try {
            operator = openEditor("Beans");
            operator.setCaretPosition(4, 1);
            openDialog(operator);
            AddProperty addProperty = new AddProperty();
            addProperty.browse();
            NbDialogOperator ndo = new NbDialogOperator("Find Type");
            JTextFieldOperator jtfo = new JTextFieldOperator(ndo);
            jtfo.typeText("LinkedList");
            ndo.ok();
            assertEquals("java.util.LinkedList", addProperty.cboType().getTextField().getText());
            addProperty.cancel();            
        } finally {
            if (operator != null) {
                operator.closeDiscard();
            }
        }
    }
    
    public void testModifierProtected() {
        try {
            operator = openEditor("Beans");
            operator.setCaretPosition(4, 1);
            openDialog(operator);
            AddProperty addProperty = new AddProperty();
            addProperty.protectedBt();
            addProperty.ok();
            checkEditorContent(operator);
        } finally {
            if (operator != null) {
                operator.closeDiscard();
            }
        }
    }
    
    public void testModifierPublic() {
        try {
            operator = openEditor("Beans");
            operator.setCaretPosition(4, 1);
            openDialog(operator);
            AddProperty addProperty = new AddProperty();
            addProperty.publicBt();
            addProperty.ok();
            checkEditorContent(operator);
        } finally {
            if (operator != null) {
                operator.closeDiscard();
            }
        }
    }
    
    public void testModifierPackage() {
        try {
            operator = openEditor("Beans");
            operator.setCaretPosition(4, 1);
            openDialog(operator);
            AddProperty addProperty = new AddProperty();
            addProperty.packageBt();
            addProperty.ok();
            checkEditorContent(operator);
        } finally {
            if (operator != null) {
                operator.closeDiscard();
            }
        }
    }
    
    public void testStatic() {
        try {
            operator = openEditor("Beans");
            operator.setCaretPosition(4, 1);
            openDialog(operator);
            AddProperty addProperty = new AddProperty();
            addProperty.checkStatic(true);
            assertFalse(addProperty.cbGeneratePropertyChangeSupport().isEnabled());
            assertFalse(addProperty.cbGenerateVetoableChangeSupport().isEnabled());
            addProperty.ok();
            checkEditorContent(operator);
        } finally {
            if (operator != null) {
                operator.closeDiscard();
            }
        }
    }
    
    public void testFinal() {
        try {
            operator = openEditor("Beans");
            operator.setCaretPosition(4, 1);
            openDialog(operator);
            AddProperty addProperty = new AddProperty();
            addProperty.checkFinal(true);            
            assertFalse(addProperty.rbGenerateGetterAndSetter().isEnabled());                    
            assertFalse(addProperty.rbGenerateSetter().isEnabled());                    
            assertEquals("Specify initializer expression for final property", addProperty.lblError().getText());            
            addProperty.txtDefaultValue().typeText("\"def\"");            
            addProperty.ok();
            checkEditorContent(operator);
        } finally {
            if (operator != null) {
                operator.closeDiscard();
            }
        }
    }
    
    public void testGenerateJavadoc() {
        try {
            operator = openEditor("Beans");
            operator.setCaretPosition(4, 1);
            openDialog(operator);
            AddProperty addProperty = new AddProperty();
            addProperty.checkGenerateJavadoc(false);
            addProperty.ok();
            checkEditorContent(operator);
        } finally {
            if (operator != null) {
                operator.closeDiscard();
            }
        }
    }
    
    public void testBound() {
        try {
            operator = openEditor("Beans");
            operator.setCaretPosition(4, 1);
            openDialog(operator);
            AddProperty addProperty = new AddProperty();
            addProperty.checkBound(true);
            addProperty.txtBoundName().typeText("_NAME");
            assertTrue(addProperty.cbVetoable().isEnabled());
            assertTrue(addProperty.cbGeneratePropertyChangeSupport().isSelected());
            addProperty.ok();
            checkEditorContent(operator);
        } finally {
            if (operator != null) {
                operator.closeDiscard();
            }
        }
    }
    
    public void testVetoable() {
        try {
            operator = openEditor("Beans");
            operator.setCaretPosition(4, 1);
            openDialog(operator);
            AddProperty addProperty = new AddProperty();
            addProperty.checkBound(true);
            addProperty.checkVetoable(true);            
            assertTrue(addProperty.cbGenerateVetoableChangeSupport().isSelected());
            addProperty.ok();
            checkEditorContent(operator);
        } finally {
            if (operator != null) {
                operator.closeDiscard();
            }
        }
    }
    
    public void testIndexed() {
        try {
            operator = openEditor("Beans");
            operator.setCaretPosition(4, 1);
            openDialog(operator);
            AddProperty addProperty = new AddProperty();
            addProperty.checkIndexed(true);            
            addProperty.ok();
            checkEditorContent(operator);
        } finally {
            if (operator != null) {
                operator.closeDiscard();
            }
        }
    }
    
    public void testVetoableIndexed() {
        try {
            operator = openEditor("Beans");
            operator.setCaretPosition(4, 1);
            openDialog(operator);
            AddProperty addProperty = new AddProperty();
            addProperty.checkBound(true);
            addProperty.checkVetoable(true);
            addProperty.checkIndexed(true);            
            addProperty.ok();
            checkEditorContent(operator);
        } finally {
            if (operator != null) {
                operator.closeDiscard();
            }
        }
    }
    
    public void testEmptyName() {
        try {
            operator = openEditor("Beans");
            operator.setCaretPosition(4, 1);
            openDialog(operator);
            AddProperty addProperty = new AddProperty();
            addProperty.txtName().clearText();
            assertEquals("Field name is empty", addProperty.lblError().getText());
            addProperty.cancel();
            
        } finally {
            if (operator != null) {
                operator.closeDiscard();
            }
        }
    }
    
    public void testExistingName() {
        try {
            operator = openEditor("Beans");
            operator.setCaretPosition(4, 1);
            openDialog(operator);
            AddProperty addProperty = new AddProperty();
            addProperty.ok();
            
            openDialog(operator);
            addProperty = new AddProperty();                        
            assertEquals("Field string already exists", addProperty.lblError().getText());
            addProperty.cancel();
            
        } finally {
            if (operator != null) {
                operator.closeDiscard();
            }
        }
    }
    
    public void testCancel() {
        try {
            operator = openEditor("Beans");
            operator.setCaretPosition(4, 1);
            openDialog(operator);
            AddProperty addProperty = new AddProperty();                        
            addProperty.cancel();
            checkEditorContent(operator);
        } finally {
            if (operator != null) {
                operator.closeDiscard();
            }
        }
    }
    
    
    
    public static Test suite() {
        return NbModuleSuite.create(
                NbModuleSuite.createConfiguration(PropertyGeneration.class)                        
                        .enableModules(".*")
                        .clusters(".*"));
    }
    

}
