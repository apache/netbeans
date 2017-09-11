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
