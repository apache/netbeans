/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2013 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2013 Sun Microsystems, Inc.
 */
package org.netbeans.test.java.editor.formatting.operators;

import java.awt.Component;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.netbeans.jemmy.EventTool;
import org.netbeans.jemmy.operators.*;

/**
 *
 * @author jprox
 */
public abstract class FormattingPanelOperator extends Operator {

    private final String language;
    private final String category;
    protected FormattingOptionsOperator formattingOperator;
    private  boolean restoreMode = false;        
    
    private static final Map<Object, Object> defaultValues = new HashMap<>();

    /**
     *
     * @param formattingOperator
     * @param language
     * @param category
     */
    public FormattingPanelOperator(FormattingOptionsOperator formattingOperator, String language, String category) {
        this.language = language;
        this.category = category;
        this.formattingOperator = formattingOperator;
    }

    public void switchToPanel() {
        formattingOperator.selectLanguage(language);
        formattingOperator.selectCategory(category);
    }

    protected void storeDefaultValue(OperatorGetter operatorGetter) {        
        if(restoreMode) return;        
        Operator operator = operatorGetter.getOperator(this);
        String key = operatorGetter.key();
        if (operator instanceof JSpinnerOperator) {
            defaultValues.put(key, ((JSpinnerOperator) operator).getValue());
        } else if (operator instanceof JTextFieldOperator) {
            defaultValues.put(key, ((JTextFieldOperator) operator).getText());
        } else if (operator instanceof JCheckBoxOperator) {
            defaultValues.put(key, ((JCheckBoxOperator) operator).isSelected());
        } else if (operator instanceof JComboBoxOperator) {
            defaultValues.put(key, ((JComboBoxOperator) operator).getSelectedItem());
        } else {
            throw new IllegalArgumentException("Unknown type of operator " + operator.getClass().getName());
        }
    }

    private void restoreDefaultsValues(OperatorGetter operatorGetter) {                
        String key = operatorGetter.key();
        Object value = defaultValues.get(key);
        if(value==null) return;
        Operator operator  = operatorGetter.getOperator(this);
        if (operator instanceof JSpinnerOperator) {
            ((JSpinnerOperator) operator).setValue((Integer) value);
        } else if (operator instanceof JTextFieldOperator) {
            ((JTextFieldOperator) operator).setText((String) value);
        } else if (operator instanceof JCheckBoxOperator) {
            ((JCheckBoxOperator) operator).changeSelectionNoBlock((Boolean) value);
        } else if (operator instanceof JComboBoxOperator) {
            ((JComboBoxOperator) operator).selectItem(value.toString());
        } else {
            throw new IllegalArgumentException("Unknown type of operator " + operator.getClass().getName());
        }        
    }
                
    public void restoreDefaultsValues() {        
        restoreMode = true;
        switchToPanel();
        for (OperatorGetter operatorGetter : getAllOperatorGetters()) {
            restoreDefaultsValues(operatorGetter);   
            defaultValues.remove(operatorGetter.key());
            
        }        
        new EventTool().waitNoEvent(250);  //Timeout to propagate UI changes correctly
        restoreMode = false;
    }    
    
    public static <T extends Enum<T> & OperatorGetter > boolean isModified(Class<T> operatorGetter) {
        T[] enumConstants = operatorGetter.getEnumConstants();
        for (T t : enumConstants) {
            if(defaultValues.containsKey(t.key())) {
                return true;
            }
        }
        return false;
    }
    
    public abstract List<OperatorGetter> getAllOperatorGetters();

    @Override
    public Component getSource() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    public interface OperatorGetter {
        Operator getOperator(FormattingPanelOperator fpo);
        String key();
    }

    public void ok() {
        formattingOperator.ok();
    }

}


