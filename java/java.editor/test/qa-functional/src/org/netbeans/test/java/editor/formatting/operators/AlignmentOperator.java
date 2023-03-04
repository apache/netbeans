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
