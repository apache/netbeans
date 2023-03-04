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

package org.netbeans.test.java.editor.formatting;

import org.netbeans.test.java.editor.formatting.operators.FormattingOptionsOperator;
import org.netbeans.test.java.editor.formatting.operators.SpacesOperator;

/**
 *
 * @author jprox
 */
public class SpacesTest extends FormattingOptionsTest {

    public SpacesTest(String testMethodName) {
        super(testMethodName);
    }

    private SpacesOperator so;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        so = FormattingOptionsOperator.invoke(true).getSpacesOperatror();
    }

    private void performTest(String path, boolean value) {
        performTest(path, value,"Spaces.java");
    }
    
    private void performTest(String path, boolean value, String file) {
        try {
            so.setValue(path, value);
            so.ok();
            formatFileAndCompare("general", file);
        } finally {
            FormattingOptionsOperator.restoreDefaultValues();
        }
    }

    public void testBeforeKeywordWhile() {
        performTest("Before Keywords|\"while\"", false);
    }
    public void testBeforeKeywordElse() {
        performTest("Before Keywords|\"else\"", false);
    }
    public void testBeforeKeywordCatch() {
        performTest("Before Keywords|\"catch\"", false);
    }
    public void testBeforeKeywordFinally() {
        performTest("Before Keywords|\"finally\"", false);
    }

    public void testBeforeParenthesesMethodDeclaration() {
        performTest("Before Parentheses|Method Declaration", true);
    }
    public void testBeforeParenthesesMethodCall() {
        performTest("Before Parentheses|Method Call", true);
    }
    public void testBeforeParenthesesIf() {
        performTest("Before Parentheses|\"if\"", false);
    }
    public void testBeforeParenthesesFor() {
        performTest("Before Parentheses|\"for\"", false);
    }
    public void testBeforeParenthesesWhile() {
        performTest("Before Parentheses|\"while\"", false);
    }
    public void testBeforeParenthesesTry() {
        performTest("Before Parentheses|\"try\"", false);
    }
    public void testBeforeParenthesesCatch() {
        performTest("Before Parentheses|\"catch\"", false);
    }
    public void testBeforeParenthesesSwitch() {
        performTest("Before Parentheses|\"switch\"", false);
    }
    public void testBeforeParenthesesSynchronized() {
        performTest("Before Parentheses|\"synchronized\"", false);
    }
    public void testBeforeParenthesesAnnotationParameters() {
        performTest("Before Parentheses|Annotation Parameters", true);
    }
    
    public void testAroundOperatorsUnary() {
        performTest("Around Operators|Unary Operators", true);
    }
    public void testAroundOperatorsBinary() {
        performTest("Around Operators|Binary Operators", false);
    }
    public void testAroundOperatorsTernary() {
        performTest("Around Operators|Ternary Operators", false);
    }
    public void testAroundOperatorsAssignment() {
        performTest("Around Operators|Assignment Operators", false);
    }
    public void testAroundOperatorsAnnotationValueAssignment() {
        performTest("Around Operators|Annotation Value Assignment Operator", false);
    }
    public void testAroundOperatorsLambdaArrow() {
        performTest("Around Operators|Lambda Arrow", false);
    }
    public void testAroundOperatorsMethodReferenceDoubleColon() {
        performTest("Around Operators|Method Reference Double Colon", true);
    }
    
    public void testBeforeLeftBracesClassDeclaration() {
        performTest("Before Left Braces|Class Declaration", false);
    }
    public void testBeforeLeftBracesMethodDeclaration() {
        performTest("Before Left Braces|Method Declaration", false);
    }
    public void testBeforeLeftBracesIf() {
        performTest("Before Left Braces|\"if\"", false);
    }
    public void testBeforeLeftBracesElse() {
        performTest("Before Left Braces|\"else\"", false);
    }
    public void testBeforeLeftBracesWhile() {
        performTest("Before Left Braces|\"while\"", false);
    }
    public void testBeforeLeftBracesFor() {
        performTest("Before Left Braces|\"for\"", false);
    }
    public void testBeforeLeftBracesDo() {
        performTest("Before Left Braces|\"do\"", false);
    }
    public void testBeforeLeftBracesSwitch() {
        performTest("Before Left Braces|\"switch\"", false);
    }
    public void testBeforeLeftBracesTry() {
        performTest("Before Left Braces|\"try\"", false);
    }
    public void testBeforeLeftBracesCatch() {
        performTest("Before Left Braces|\"catch\"", false);
    }
    public void testBeforeLeftBracesFinally() {
        performTest("Before Left Braces|\"finally\"", false);
    }
    public void testBeforeLeftBracesSynchronized() {
        performTest("Before Left Braces|\"synchronized\"", false);
    }
    public void testBeforeLeftBracesStaticInitializer() {
        performTest("Before Left Braces|Static Initializer", false);
    }
    public void testBeforeLeftBracesArrayInitializer() {
        performTest("Before Left Braces|Array Initializer", true);
    }
    
    public void testWithinParenthesesParentheses() {
        performTest("Within Parentheses|Parentheses", true);
    }
    public void testWithinParenthesesMethodDeclaration() {
        performTest("Within Parentheses|Method Declaration", true);
    }
    public void testWithinParenthesesLambdaParameters() {
        performTest("Within Parentheses|Lambda Parameters", true);
    }
    public void testWithinParenthesesMethodCall() {
        performTest("Within Parentheses|Method Call", true);
    }
    public void testWithinParenthesesIf() {
        performTest("Within Parentheses|\"if\"", true);
    }
    public void testWithinParenthesesFor() {
        performTest("Within Parentheses|\"for\"", true);
    }
    public void testWithinParenthesesWhile() {
        performTest("Within Parentheses|\"while\"", true);
    }
    public void testWithinParenthesesSwitch() {
        performTest("Within Parentheses|\"switch\"", true);
    }
    public void testWithinParenthesesTry() {
        performTest("Within Parentheses|\"try\"", true);
    }
    public void testWithinParenthesesCatch() {
        performTest("Within Parentheses|\"catch\"", true);
    }
    public void testWithinParenthesesSynchronized() {
        performTest("Within Parentheses|\"synchronized\"", true);
    }
    public void testWithinParenthesesTypeCast() {
        performTest("Within Parentheses|Type Cast", true);
    }
    public void testWithinParenthesesAnnotation() {
        performTest("Within Parentheses|Annotation", true);
    }
    public void testWithinParenthesesBraces() {
        performTest("Within Parentheses|Braces", true);
    }
    public void testWithinParenthesesArray() {
        performTest("Within Parentheses|Array Initializer Brackets", true);
    }
    
    public void testOtherBeforeComma() {
        performTest("Other|Before Comma", true);
    }
    public void testOtherAfterComma() {
        performTest("Other|After Comma", false);
    }
    public void testOtherBeforeSemicolon() {
        performTest("Other|Before Semicolon", true);
    }
    public void testOtherAfterSemicolon() {
        performTest("Other|After Semicolon", false);
    }
    public void testOtherBeforeColon() {
        performTest("Other|Before Colon", false,"Spaces2.java");
    }
    public void testOtherAfterColon() {
        performTest("Other|After Colon", false,"Spaces2.java");
    }
    public void testOtherAfterTypeCast() {
        performTest("Other|After Type Cast", false);
    }
}
