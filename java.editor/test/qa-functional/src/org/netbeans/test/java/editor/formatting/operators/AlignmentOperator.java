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
import org.netbeans.jemmy.operators.Operator;

/**
 *
 * @author jprox
 */
public class AlignmentOperator extends FormattingPanelOperator {

    private JCheckBoxOperator newLineElse;

    private JCheckBoxOperator newLineWhile;

    private JCheckBoxOperator newLineCatch;

    private JCheckBoxOperator newLineFinally;

    private JCheckBoxOperator newLineAfterModifiers;

    private JCheckBoxOperator alignmentMethodsParameters;

    private JCheckBoxOperator alignmentAnnotationArguments;

    private JCheckBoxOperator alignmentThrowsList;

    private JCheckBoxOperator alignmentDisjunctiveCatchTypes;

    private JCheckBoxOperator alignmentBinaryOperators;

    private JCheckBoxOperator alignmentAssignement;

    private JCheckBoxOperator alignmentParenthesized;

    private JCheckBoxOperator alignmentLambdaParameters;

    private JCheckBoxOperator alignmentMethodCallArguments;

    private JCheckBoxOperator alignmentImplementsList;

    private JCheckBoxOperator alignmentTryResources;

    private JCheckBoxOperator alignmentArrayInitializer;

    private JCheckBoxOperator alignmentTernaryOperators;

    private JCheckBoxOperator alignmentFor;

    public AlignmentOperator(FormattingOptionsOperator formattingOperator) {
        super(formattingOperator, "Java", "Alignment");
        switchToPanel();
    }

    @Override
    public List<OperatorGetter> getAllOperatorGetters() {
        return Arrays.asList((OperatorGetter[]) Settings.values());
    }

    public JCheckBoxOperator getNewLineElseOperator() {
        if (newLineElse == null) {
            newLineElse = formattingOperator.getCheckboxOperatorByLabel("\"else\"");
            storeDefaultValue(Settings.ALIGNMENTNEWLINEELSE);
        }
        return newLineElse;
    }

    public JCheckBoxOperator getNewLineWhileOperator() {
        if (newLineWhile == null) {
            newLineWhile = formattingOperator.getCheckboxOperatorByLabel("\"while\"");
            storeDefaultValue(Settings.ALIGNMENTNEWLINEWITH);
        }
        return newLineWhile;
    }

    public JCheckBoxOperator getNewLineCatch() {
        if (newLineCatch == null) {
            newLineCatch = formattingOperator.getCheckboxOperatorByLabel("\"catch\"");
            storeDefaultValue(Settings.ALIGNMENTNEWLINECATCH);
        }
        return newLineCatch;
    }

    public JCheckBoxOperator getNewLineFinally() {
        if (newLineFinally == null) {
            newLineFinally = formattingOperator.getCheckboxOperatorByLabel("\"finally\"");
            storeDefaultValue(Settings.ALIGNMENTNEWLINEFINALLY);
        }
        return newLineFinally;
    }

    public JCheckBoxOperator getNewLineAfterModifiers() {
        if (newLineAfterModifiers == null) {
            newLineAfterModifiers = formattingOperator.getCheckboxOperatorByLabel("After modifiers");
            storeDefaultValue(Settings.ALIGNMENTNEWLINEAFTERMODIFIERS);
        }
        return newLineAfterModifiers;
    }

    public JCheckBoxOperator getAlignmentMethodsParameters() {
        if (alignmentMethodsParameters == null) {
            alignmentMethodsParameters = formattingOperator.getCheckboxOperatorByLabel("Method Parameters");
            storeDefaultValue(Settings.ALIGNMENTMETHODPARAMETERS);
        }
        return alignmentMethodsParameters;
    }

    public JCheckBoxOperator getAlignmentAnnotationArguments() {
        if (alignmentAnnotationArguments == null) {
            alignmentAnnotationArguments = formattingOperator.getCheckboxOperatorByLabel("Annotation Arguments");
            storeDefaultValue(Settings.ALIGNMENTANNOTATIONARGUMENTS);
        }
        return alignmentAnnotationArguments;
    }

    public JCheckBoxOperator getAlignmentThrowsList() {
        if (alignmentThrowsList == null) {
            alignmentThrowsList = formattingOperator.getCheckboxOperatorByLabel("Throws List");
            storeDefaultValue(Settings.ALIGNMENTTHROWSLIST);
        }
        return alignmentThrowsList;
    }

    public JCheckBoxOperator getAlignmentDisjunctiveCatchTypes() {
        if (alignmentDisjunctiveCatchTypes == null) {
            alignmentDisjunctiveCatchTypes = formattingOperator.getCheckboxOperatorByLabel("Disjunctive Catch Types");
            storeDefaultValue(Settings.ALIGNMENTDISJUNCTIVECATCHTYPES);
        }
        return alignmentDisjunctiveCatchTypes;
    }

    public JCheckBoxOperator getAlignmentBinaryOperators() {
        if (alignmentBinaryOperators == null) {
            alignmentBinaryOperators = formattingOperator.getCheckboxOperatorByLabel("Binary Operators");
            storeDefaultValue(Settings.ALIGNMENTBINARYOPERATORS);
        }
        return alignmentBinaryOperators;
    }

    public JCheckBoxOperator getAlignmentAssignement() {
        if (alignmentAssignement == null) {
            alignmentAssignement = formattingOperator.getCheckboxOperatorByLabel("Assignment");
            storeDefaultValue(Settings.ALIGNMENTASSIGNMENT);
        }
        return alignmentAssignement;
    }

    public JCheckBoxOperator getAlignmentParenthesized() {
        if (alignmentParenthesized == null) {
            alignmentParenthesized = formattingOperator.getCheckboxOperatorByLabel("Parenthesized");
            storeDefaultValue(Settings.ALIGNMENTPARENTHESIZED);
        }
        return alignmentParenthesized;
    }

    public JCheckBoxOperator getAlignmentLambdaParameters() {
        if (alignmentLambdaParameters == null) {
            alignmentLambdaParameters = formattingOperator.getCheckboxOperatorByLabel("Lambda Parameters");
            storeDefaultValue(Settings.ALIGNMENTLAMBDAPARAMETERS);
        }
        return alignmentLambdaParameters;
    }

    public JCheckBoxOperator getAlignmentMethodCallArguments() {
        if (alignmentMethodCallArguments == null) {
            alignmentMethodCallArguments = formattingOperator.getCheckboxOperatorByLabel("Method Call Arguments");
            storeDefaultValue(Settings.ALIGNMENTMETHODCALLARGUMENTS);
        }
        return alignmentMethodCallArguments;
    }

    public JCheckBoxOperator getAlignmentImplementsList() {
        if (alignmentImplementsList == null) {
            alignmentImplementsList = formattingOperator.getCheckboxOperatorByLabel("Implements List");
            storeDefaultValue(Settings.ALIGNMENTIMPLEMENTSLIST);
        }
        return alignmentImplementsList;
    }

    public JCheckBoxOperator getAlignmentTryResources() {
        if (alignmentTryResources == null) {
            alignmentTryResources = formattingOperator.getCheckboxOperatorByLabel("Try Resources");
            storeDefaultValue(Settings.ALIGNMENTTRYRESOURCES);
        }
        return alignmentTryResources;
    }

    public JCheckBoxOperator getAlignmentArrayInitializer() {
        if (alignmentArrayInitializer == null) {
            alignmentArrayInitializer = formattingOperator.getCheckboxOperatorByLabel("Array Initializer");
            storeDefaultValue(Settings.ALIGNEMTARRAYINITIALIZER);
        }
        return alignmentArrayInitializer;
    }

    public JCheckBoxOperator getAlignmentTernaryOperators() {
        if (alignmentTernaryOperators == null) {
            alignmentTernaryOperators = formattingOperator.getCheckboxOperatorByLabel("Ternary Operators");
            storeDefaultValue(Settings.ALIGNMENTTERNARYOPERATORS);
        }
        return alignmentTernaryOperators;
    }

    public JCheckBoxOperator getAlignmentFor() {
        if (alignmentFor == null) {
            alignmentFor = formattingOperator.getCheckboxOperatorByLabel("For");
            storeDefaultValue(Settings.ALIGNMENTFOR);
        }
        return alignmentFor;
    }

    enum Settings implements OperatorGetter {

        ALIGNMENTNEWLINEELSE, ALIGNMENTNEWLINEWITH, ALIGNMENTNEWLINECATCH, ALIGNMENTNEWLINEFINALLY, ALIGNMENTNEWLINEAFTERMODIFIERS,
        ALIGNMENTMETHODPARAMETERS, ALIGNMENTANNOTATIONARGUMENTS, ALIGNMENTTHROWSLIST, ALIGNMENTDISJUNCTIVECATCHTYPES, ALIGNMENTBINARYOPERATORS, ALIGNMENTASSIGNMENT, ALIGNMENTPARENTHESIZED,
        ALIGNMENTLAMBDAPARAMETERS, ALIGNMENTMETHODCALLARGUMENTS, ALIGNMENTIMPLEMENTSLIST, ALIGNMENTTRYRESOURCES, ALIGNEMTARRAYINITIALIZER, ALIGNMENTTERNARYOPERATORS, ALIGNMENTFOR;

        @Override
        public Operator getOperator(FormattingPanelOperator fpo) {
            AlignmentOperator ao = (AlignmentOperator) fpo;
            switch (this) {
                case ALIGNMENTNEWLINEELSE:
                    return ao.getNewLineElseOperator();
                case ALIGNMENTNEWLINEWITH:
                    return ao.getNewLineWhileOperator();
                case ALIGNMENTNEWLINECATCH:
                    return ao.getNewLineCatch();
                case ALIGNMENTNEWLINEFINALLY:
                    return ao.getNewLineFinally();
                case ALIGNMENTNEWLINEAFTERMODIFIERS:
                    return ao.getNewLineAfterModifiers();
                case ALIGNMENTMETHODPARAMETERS:
                    return ao.getAlignmentMethodsParameters();
                case ALIGNMENTANNOTATIONARGUMENTS:
                    return ao.getAlignmentAnnotationArguments();
                case ALIGNMENTTHROWSLIST:
                    return ao.getAlignmentThrowsList();
                case ALIGNMENTDISJUNCTIVECATCHTYPES:
                    return ao.getAlignmentDisjunctiveCatchTypes();
                case ALIGNMENTBINARYOPERATORS:
                    return ao.getAlignmentBinaryOperators();
                case ALIGNMENTASSIGNMENT:
                    return ao.getAlignmentAssignement();
                case ALIGNMENTPARENTHESIZED:
                    return ao.getAlignmentParenthesized();
                case ALIGNMENTLAMBDAPARAMETERS:
                    return ao.getAlignmentLambdaParameters();
                case ALIGNMENTMETHODCALLARGUMENTS:
                    return ao.getAlignmentMethodCallArguments();
                case ALIGNMENTIMPLEMENTSLIST:
                    return ao.getAlignmentImplementsList();
                case ALIGNMENTTRYRESOURCES:
                    return ao.getAlignmentTryResources();
                case ALIGNEMTARRAYINITIALIZER:
                    return ao.getAlignmentArrayInitializer();
                case ALIGNMENTTERNARYOPERATORS:
                    return ao.getAlignmentTernaryOperators();
                case ALIGNMENTFOR:
                    return ao.getAlignmentFor();
            }
            return null;
        }

        @Override
        public String key() {
            return this.name();
        }
    }           
}
