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


