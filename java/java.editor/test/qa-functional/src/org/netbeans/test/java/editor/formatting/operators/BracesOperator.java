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

import java.util.Arrays;
import java.util.List;
import org.netbeans.jemmy.operators.JCheckBoxOperator;
import org.netbeans.jemmy.operators.JComboBoxOperator;
import org.netbeans.jemmy.operators.Operator;
import org.netbeans.modules.java.ui.FmtBraces;
import org.netbeans.modules.java.ui.FmtOptions;
import org.openide.util.NbBundle;

/**
 *
 * @author jprox
 */
public class BracesOperator extends FormattingPanelOperator {    
    public static final String SAME_LINE = NbBundle.getMessage(FmtBraces.class, "LBL_bp_SAME_LINE");
    public static final String NEW_LINE_INDENTED = NbBundle.getMessage(FmtBraces.class, "LBL_bp_NEW_LINE_INDENTED");
    public static final String NEW_LINE_HALF_INDENTED = NbBundle.getMessage(FmtBraces.class, "LBL_bp_NEW_LINE_HALF_INDENTED");
    public static final String NEW_LINE = NbBundle.getMessage(FmtBraces.class, "LBL_bp_NEW_LINE");
    public static final String BRACES_GENERATE = NbBundle.getMessage(FmtBraces.class, "LBL_bg_GENERATE");
    public static final String BRACES_ELIMINATE = NbBundle.getMessage(FmtBraces.class, "LBL_bg_ELIMINATE");
    public static final String BRACES_LEAVE_ALONE = NbBundle.getMessage(FmtBraces.class, "LBL_bg_LEAVE_ALONE");
    
    private JComboBoxOperator classDeclaration;
    private JComboBoxOperator methodDeclaration;
    private JComboBoxOperator otherDeclaration;
    private JCheckBoxOperator specialElseIf;
    private JComboBoxOperator ifBrace;
    private JComboBoxOperator forBrace;
    private JComboBoxOperator whileBrace;
    private JComboBoxOperator doWhileBrace;
         
    public BracesOperator(FormattingOptionsOperator formattingOperator) {
        super(formattingOperator, "Java", "Braces");
        switchToPanel();
    }

    @Override
    public List<OperatorGetter> getAllOperatorGetters() {
        return Arrays.asList((OperatorGetter[]) BracesOperator.Settings.values());
    }
    
    public JComboBoxOperator getClassDeclaration() {
        if (classDeclaration == null) {
            classDeclaration = formattingOperator.getComboBoxByLabel("Class Declaration:");
            storeDefaultValue(Settings.BRACES_CLASS_DECLARATION);
        }
        return classDeclaration;
    }
    
    public JComboBoxOperator getMethodDeclaration() {
        if (methodDeclaration == null) {
            methodDeclaration = formattingOperator.getComboBoxByLabel("Method Declaration:");
            storeDefaultValue(Settings.BRACES_METHOD_DECLARATION);
        }
        return methodDeclaration;
    }
    
    public JComboBoxOperator getOtherDeclaration() {
        if (otherDeclaration == null) {
            otherDeclaration = formattingOperator.getComboBoxByLabel("Other:");
            storeDefaultValue(Settings.BRACES_OTHER);
        }
        return otherDeclaration;
    }
    
    public JCheckBoxOperator getspecialElseIf() {
        if (specialElseIf == null) {
            specialElseIf = formattingOperator.getCheckboxOperatorByLabel("Special \"else if\" Treatment");
            storeDefaultValue(Settings.BRACES_SPECIAL_ELSE_IF);
        }
        return specialElseIf;
    }
    
    public JComboBoxOperator getIfBrace() {
        if (ifBrace == null) {
            ifBrace = formattingOperator.getComboBoxByLabel("\"if\":");
            storeDefaultValue(Settings.BRACES_IF);
        }
        return ifBrace;
    }
    
    public JComboBoxOperator getForBrace() {
        if (forBrace == null) {
            forBrace = formattingOperator.getComboBoxByLabel("\"for\":");
            storeDefaultValue(Settings.BRACES_FOR);
        }
        return forBrace;
    }
    
    public JComboBoxOperator getWhileBrace() {
        if (whileBrace == null) {
            whileBrace = formattingOperator.getComboBoxByLabel("\"while\":");
            storeDefaultValue(Settings.BRACES_WHILE);
        }
        return whileBrace;
    }
    
    public JComboBoxOperator getDoWhileBrace() {
        if (doWhileBrace == null) {
            doWhileBrace = formattingOperator.getComboBoxByLabel("\"do ... while\":");
            storeDefaultValue(Settings.BRACES_DO_WHILE);
        }
        return doWhileBrace;
    }
    
    enum Settings implements OperatorGetter {

        BRACES_CLASS_DECLARATION, BRACES_METHOD_DECLARATION, BRACES_OTHER, 
        BRACES_SPECIAL_ELSE_IF, BRACES_IF, BRACES_FOR, BRACES_WHILE, BRACES_DO_WHILE;

        @Override
        public Operator getOperator(FormattingPanelOperator fpo) {
            BracesOperator bo = (BracesOperator) fpo;
            switch (this) {
                case BRACES_CLASS_DECLARATION:
                    return bo.getClassDeclaration();
                case BRACES_METHOD_DECLARATION:
                    return bo.getMethodDeclaration();
                case BRACES_OTHER:
                    return bo.getOtherDeclaration();
                case BRACES_SPECIAL_ELSE_IF:
                    return bo.getspecialElseIf();
                case BRACES_IF:
                    return bo.getIfBrace();
                case BRACES_FOR:
                    return bo.getForBrace();
                case BRACES_WHILE:
                    return bo.getWhileBrace();
                case BRACES_DO_WHILE:
                    return bo.getDoWhileBrace();                        
            }
            return null;            
        }

        @Override
        public String key() {
            return this.name();
        }

    }

}
