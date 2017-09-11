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
import org.netbeans.modules.java.ui.FmtOptions;
import org.netbeans.modules.java.ui.FmtWrapping;
import org.openide.util.NbBundle;

/**
 *
 * @author jprox
 */
public class WrappingOperator extends FormattingPanelOperator {

    public static final String NEVER = NbBundle.getMessage(FmtWrapping.class, "LBL_wrp_WRAP_NEVER");
    public static final String ALWAYS = NbBundle.getMessage(FmtWrapping.class, "LBL_wrp_WRAP_ALWAYS");
    public static final String IF_LONG = NbBundle.getMessage(FmtWrapping.class, "LBL_wrp_WRAP_IF_LONG");

    private JComboBoxOperator extendImplementsKeyWord;
    
    private JComboBoxOperator extendsImplementsList;
    
    private JComboBoxOperator methodParameters;
    
    private JComboBoxOperator methodCallArguments;
    
    private JComboBoxOperator annotationArguments;
    
    private JComboBoxOperator chainedMethodCalls;
    
    private JCheckBoxOperator wrapAfterDot;
    
    private JComboBoxOperator throwsKeyword;
    
    private JComboBoxOperator throwsList;
    
    private JComboBoxOperator arrayInitializer;
    
    private JComboBoxOperator tryResources;
    
    private JComboBoxOperator disjunctiveCatchTypes;
    
    private JComboBoxOperator forArgs;
    
    private JComboBoxOperator forStatement;
    
    private JComboBoxOperator ifStatement;
    
    private JComboBoxOperator whileStatment;
    
    private JComboBoxOperator doWhileStatements;
    
    private JComboBoxOperator caseStatements;
    
    private JComboBoxOperator assertStatement;
    
    private JComboBoxOperator enumConstants;
    
    private JComboBoxOperator annotations;
    
    private JComboBoxOperator binaryOperators;
    
    private JCheckBoxOperator wrapAfterBinaryOperators;
    
    private JComboBoxOperator ternaryOperators;
    
    private JCheckBoxOperator wrapAfterTernaryOperators;
    
    private JComboBoxOperator assignmentOperators;
    
    private JCheckBoxOperator wrapAfterAssignmentOperators;
    
    private JComboBoxOperator lambdaParameters; 
    
    private JComboBoxOperator lambdaArrow;
    
    private JCheckBoxOperator wrapAfterLambdaArrow;
    
    public WrappingOperator(FormattingOptionsOperator formattingOperator) {
        super(formattingOperator, "Java", "Wrapping");
        switchToPanel();
    }

    @Override
    public List<OperatorGetter> getAllOperatorGetters() {
        return Arrays.asList((OperatorGetter[]) Settings.values());
    }

    public JComboBoxOperator getExtendImplementsKeyWord() {
        if (extendImplementsKeyWord == null) {
            extendImplementsKeyWord = formattingOperator.getComboBoxByLabel("Extends/Implements Keyword:");
            storeDefaultValue(Settings.WRAP_EXTENDSKEYWORD);
        }
        return extendImplementsKeyWord;
    }
    
    public JComboBoxOperator getExtendsImplementsList() {
        if (extendsImplementsList == null) {
            extendsImplementsList = formattingOperator.getComboBoxByLabel("Extends/Implements List:");
            storeDefaultValue(Settings.WRAP_EXTENDSLIST);
        }
        return extendsImplementsList;
    }
    
    public JComboBoxOperator getMethodParameters() {
        if (methodParameters == null) {
            methodParameters = formattingOperator.getComboBoxByLabel("Method Parameters");
            storeDefaultValue(Settings.WRAP_METHODPARAMETERS);
        }
        return methodParameters;
    }
    
    public JComboBoxOperator getMethodCallArguments() {
        if (methodCallArguments == null) {
            methodCallArguments = formattingOperator.getComboBoxByLabel("Method Call Arguments:");
            storeDefaultValue(Settings.WRAP_METHODCALLARGUMENTS);
        }
        return methodCallArguments;
    }
    
    public JComboBoxOperator getAnnotationArguments() {
        if (annotationArguments == null) {
            annotationArguments = formattingOperator.getComboBoxByLabel("Annotation Arguments");
            storeDefaultValue(Settings.WRAP_ANNOTATIONARGUMENTS);
        }
        return annotationArguments;
    }
    
    public JComboBoxOperator getChainedMethodCalls() {
        if (chainedMethodCalls == null) {
            chainedMethodCalls = formattingOperator.getComboBoxByLabel("Chained Method Calls:");
            storeDefaultValue(Settings.WRAP_CHAINEDMETHODCALLS);
        }
        return chainedMethodCalls;
    }
    
    public JCheckBoxOperator getWrapAfterDot() {
        if (wrapAfterDot == null) {
            wrapAfterDot = formattingOperator.getCheckboxOperatorByLabel("Wrap After Dot In Chained Method Call");
            storeDefaultValue(Settings.WRAP_WRAPAFTERDOT);
        }
        return wrapAfterDot;
    }
    
    public JComboBoxOperator getThrowsKeyword() {
        if (throwsKeyword == null) {
            throwsKeyword = formattingOperator.getComboBoxByLabel("Throws Keyword:");
            storeDefaultValue(Settings.WRAP_THROWSKEYWORD);
        }
        return throwsKeyword;
    }
    
    public JComboBoxOperator getThrowsList() {
        if (throwsList == null) {
            throwsList = formattingOperator.getComboBoxByLabel("Throws List:");
            storeDefaultValue(Settings.WRAP_THROWSLIST);
        }
        return throwsList;
    }
    
    public JComboBoxOperator getArrayInitializer() {
        if (arrayInitializer == null) {
            arrayInitializer = formattingOperator.getComboBoxByLabel("Array Initializer:");
            storeDefaultValue(Settings.WRAP_ARRAYINITIALIZER);
        }
        return arrayInitializer;
    }
    
    public JComboBoxOperator getTryResources() {
        if (tryResources == null) {
            tryResources = formattingOperator.getComboBoxByLabel("Try Resources:");
            storeDefaultValue(Settings.WRAP_TRYRESOURCES);
        }
        return tryResources;        
    }
    
    public JComboBoxOperator getDisjunctiveCatchTypes() {
        if (disjunctiveCatchTypes == null) {
            disjunctiveCatchTypes = formattingOperator.getComboBoxByLabel("Disjunctive Catch Types:");
            storeDefaultValue(Settings.WRAP_DISJUNCTIVECATCHTYPES);
        }
        return disjunctiveCatchTypes;
    }
    
    public JComboBoxOperator getForArgs() {
        if (forArgs == null) {
            forArgs = formattingOperator.getComboBoxByLabel("For:");
            storeDefaultValue(Settings.WRAP_FOR);
        }
        return forArgs;
    }
    
    public JComboBoxOperator getForStatement() {
        if (forStatement == null) {
            forStatement = formattingOperator.getComboBoxByLabel("For Statement:");
            storeDefaultValue(Settings.WRAP_FORSTATEMENT);
        }
        return forStatement;
    }
    
    public JComboBoxOperator getIfStatement() {
        if (ifStatement == null) {
            ifStatement = formattingOperator.getComboBoxByLabel("If Statement:");
            storeDefaultValue(Settings.WRAP_IFSTATEMENT);
        }
        return ifStatement;
    }
    
    public JComboBoxOperator getWhileStatment() {
        if (whileStatment == null) {
            whileStatment = formattingOperator.getComboBoxByLabel("While Statement:");
            storeDefaultValue(Settings.WRAP_WHILESTATEMENT);
        }
        return whileStatment;
    }
    
    public JComboBoxOperator getDoWhileStatements() {
        if (doWhileStatements == null) {
            doWhileStatements = formattingOperator.getComboBoxByLabel("Do ... While Statement:");
            storeDefaultValue(Settings.WRAP_DOWHILESTATEMENT);
        }
        return doWhileStatements;
    }
    
    public JComboBoxOperator getCaseStatements() {
        if (caseStatements == null) {
            caseStatements = formattingOperator.getComboBoxByLabel("Case Statements:");
            storeDefaultValue(Settings.WRAP_CASESTATEMENTS);
        }
        return caseStatements;
    }
    
    public JComboBoxOperator getAssertStatement() {
        if (assertStatement == null) {
            assertStatement = formattingOperator.getComboBoxByLabel("Assert:");
            storeDefaultValue(Settings.WRAP_ASSERT);
        }
        return assertStatement;
    }
    
    public JComboBoxOperator getEnumConstants() {
        if (enumConstants == null) {
            enumConstants = formattingOperator.getComboBoxByLabel("Enum Constants:");
            storeDefaultValue(Settings.WRAP_ENUMCONSTANTS);
        }
        return enumConstants;
    }
    
    public JComboBoxOperator getAnnotations() {
        if (annotations == null) {
            annotations = formattingOperator.getComboBoxByLabel("Annotations:");
            storeDefaultValue(Settings.WRAP_ANNOTATIONS);
        }
        return annotations;
    }
    
    public JComboBoxOperator getBinaryOperators() {
        if (binaryOperators == null) {
            binaryOperators = formattingOperator.getComboBoxByLabel("Binary Operators:");
            storeDefaultValue(Settings.WRAP_BINARYOPERATORS);
        }
        return binaryOperators;
    }
    
    public JCheckBoxOperator getWrapAfterBinaryOperators() {
        if (wrapAfterBinaryOperators == null) {
            wrapAfterBinaryOperators = formattingOperator.getCheckboxOperatorByLabel("Wrap After Binary Operators");
            storeDefaultValue(Settings.WRAP_WRAPAFTERBINARYOPERATORS);
        }
        return wrapAfterBinaryOperators;
    }
    
    public JComboBoxOperator getTernaryOperators() {
        if (ternaryOperators == null) {
            ternaryOperators = formattingOperator.getComboBoxByLabel("Ternary Operators:");
            storeDefaultValue(Settings.WRAP_TERNARYOPERATORS);
        }
        return ternaryOperators;
    }
    
    public JCheckBoxOperator getWrapAfterTernaryOperators() {
        if (wrapAfterTernaryOperators == null) {
            wrapAfterTernaryOperators = formattingOperator.getCheckboxOperatorByLabel("Wrap After Ternary Operators");
            storeDefaultValue(Settings.WRAP_WRAPAFTERTERNARYOPERATORS);
        }
        return wrapAfterTernaryOperators;
    }
    
    public JComboBoxOperator getAssignmentOperators() {
        if (assignmentOperators == null) {
            assignmentOperators = formattingOperator.getComboBoxByLabel("Assignment Operators:");
            storeDefaultValue(Settings.WRAP_ASSIGNMENTOPERATORS);
        }
        return assignmentOperators;
    }
    
    public JCheckBoxOperator getWrapAfterAssignmentOperators() {
        if (wrapAfterAssignmentOperators == null) {
            wrapAfterAssignmentOperators = formattingOperator.getCheckboxOperatorByLabel("Warp After Assignment Operators:");
            storeDefaultValue(Settings.WRAP_WRAPAFTERASSIGNMENTOPERATORS);
        }
        return wrapAfterAssignmentOperators;
    }
    
    public JComboBoxOperator getLambdaParameters() {
        if (lambdaParameters == null) {
            lambdaParameters = formattingOperator.getComboBoxByLabel("Lambda Parameters:");
            storeDefaultValue(Settings.WRAP_LAMBDAPARAMETERS);
        }
        return lambdaParameters;
    }
    
    public JComboBoxOperator getLambdaArrow() {
        if (lambdaArrow == null) {
            lambdaArrow = formattingOperator.getComboBoxByLabel("Lambda Arrow:");
            storeDefaultValue(Settings.WRAP_LAMBDAARROW);
        }
        return lambdaArrow;
    }
    
    public JCheckBoxOperator getWrapAfterLambdaArrow() {
        if (wrapAfterLambdaArrow == null) {
            wrapAfterLambdaArrow = formattingOperator.getCheckboxOperatorByLabel("Wrap After Lambda Arrow");
            storeDefaultValue(Settings.WRAP_WRAPAFTERLAMBDAARROW);
        }
        return wrapAfterLambdaArrow;
    }
    

    enum Settings implements OperatorGetter {

        WRAP_EXTENDSKEYWORD, WRAP_EXTENDSLIST, WRAP_METHODPARAMETERS, WRAP_METHODCALLARGUMENTS, 
        WRAP_ANNOTATIONARGUMENTS, WRAP_CHAINEDMETHODCALLS, WRAP_WRAPAFTERDOT,
        WRAP_THROWSKEYWORD, WRAP_THROWSLIST, WRAP_ARRAYINITIALIZER, WRAP_TRYRESOURCES, WRAP_DISJUNCTIVECATCHTYPES,
        WRAP_FOR, WRAP_FORSTATEMENT, WRAP_IFSTATEMENT, WRAP_WHILESTATEMENT, WRAP_DOWHILESTATEMENT,
        WRAP_CASESTATEMENTS, WRAP_ASSERT, WRAP_ENUMCONSTANTS, WRAP_ANNOTATIONS, WRAP_BINARYOPERATORS,
        WRAP_WRAPAFTERBINARYOPERATORS, WRAP_TERNARYOPERATORS, WRAP_WRAPAFTERTERNARYOPERATORS, 
        WRAP_ASSIGNMENTOPERATORS, WRAP_WRAPAFTERASSIGNMENTOPERATORS, WRAP_LAMBDAPARAMETERS, WRAP_LAMBDAARROW,WRAP_WRAPAFTERLAMBDAARROW;

        @Override
        public Operator getOperator(FormattingPanelOperator fpo) {
            WrappingOperator wo = (WrappingOperator) fpo;
            switch (this) {
                case WRAP_EXTENDSKEYWORD:
                    return wo.getExtendImplementsKeyWord();
                case WRAP_EXTENDSLIST:
                    return wo.getExtendsImplementsList();
                case WRAP_METHODPARAMETERS:
                    return wo.getMethodParameters();
                case WRAP_METHODCALLARGUMENTS:
                    return wo.getMethodCallArguments();
                case WRAP_ANNOTATIONARGUMENTS:
                    return wo.getAnnotationArguments();
                case WRAP_CHAINEDMETHODCALLS:
                    return wo.getChainedMethodCalls();
                case WRAP_WRAPAFTERDOT:
                    return wo.getWrapAfterDot();
                case WRAP_THROWSKEYWORD:
                    return wo.getThrowsKeyword();
                case WRAP_THROWSLIST:
                    return wo.getThrowsList();
                case WRAP_ARRAYINITIALIZER:
                    return wo.getArrayInitializer();
                case WRAP_TRYRESOURCES:
                    return wo.getTryResources();
                case WRAP_DISJUNCTIVECATCHTYPES:
                    return wo.getDisjunctiveCatchTypes();
                case WRAP_FOR:
                    return wo.getForArgs();
                case WRAP_FORSTATEMENT:
                    return wo.getForStatement();
                case WRAP_IFSTATEMENT:
                    return wo.getIfStatement();
                case WRAP_WHILESTATEMENT:
                    return wo.getWhileStatment();
                case WRAP_DOWHILESTATEMENT:
                    return wo.getDoWhileStatements();
                case WRAP_CASESTATEMENTS:
                    return wo.getCaseStatements();
                case WRAP_ASSERT:
                    return wo.getAssertStatement();
                case WRAP_ENUMCONSTANTS:
                    return wo.getEnumConstants();
                case WRAP_ANNOTATIONS:
                    return wo.getAnnotations();
                case WRAP_BINARYOPERATORS:
                    return wo.getBinaryOperators();
                case WRAP_WRAPAFTERBINARYOPERATORS:
                    return wo.getWrapAfterBinaryOperators();
                case WRAP_TERNARYOPERATORS:
                    return wo.getTernaryOperators();
                case WRAP_WRAPAFTERTERNARYOPERATORS:
                    return wo.getWrapAfterTernaryOperators();
                case WRAP_ASSIGNMENTOPERATORS:
                    return wo.getAssignmentOperators();
                case WRAP_WRAPAFTERASSIGNMENTOPERATORS:
                    return wo.getWrapAfterAssignmentOperators();
                case WRAP_LAMBDAPARAMETERS:
                    return wo.getLambdaParameters();
                case WRAP_LAMBDAARROW:
                    return wo.getLambdaArrow();
                case WRAP_WRAPAFTERLAMBDAARROW:
                    break;
                default:
                    throw new AssertionError(this.name());                                
            }
            return null;       
        }

        @Override
        public String key() {
            return this.name();
        }

    }

}
