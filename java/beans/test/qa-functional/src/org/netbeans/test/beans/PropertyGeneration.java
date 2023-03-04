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
