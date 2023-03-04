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
import org.netbeans.test.java.editor.formatting.operators.JavaTabsAndIndentsOperator;

public class JavaTabsAndIndentsTest extends FormattingOptionsTest {

    public JavaTabsAndIndentsTest(String testMethodName) {
        super(testMethodName);
    }

    public void testExpandTabsToSpacesJava() {
        FormattingOptionsOperator formattingOperator = FormattingOptionsOperator.invoke(true);
        JavaTabsAndIndentsOperator operator = formattingOperator.getJavaTabsAndIndentsOperator();
        try {
            operator.getUseAllLanguages().changeSelection(false);
            operator.getExpandTabsToSpaces().changeSelection(false);
            formattingOperator.ok();
            formatFileAndCompare("general", "Indentation.java");
        } finally {
            FormattingOptionsOperator.restoreDefaultValues();
        }
    }

    public void testTabSizeJava() {
        FormattingOptionsOperator formattingOperator = FormattingOptionsOperator.invoke(true);
        JavaTabsAndIndentsOperator operator = formattingOperator.getJavaTabsAndIndentsOperator();
        try {
            operator.getUseAllLanguages().changeSelection(false);
            operator.getExpandTabsToSpaces().changeSelection(false);
            operator.getTabSize().setValue(4);
            formattingOperator.ok();
            formatFileAndCompare("general", "Indentation.java");
        } finally {
            FormattingOptionsOperator.restoreDefaultValues();
        }
    }

    public void testIndentationLevelJava() {        
        try {
            JavaTabsAndIndentsOperator operator = FormattingOptionsOperator.invoke(true).getJavaTabsAndIndentsOperator();
            operator.getUseAllLanguages().changeSelection(false);
            operator.getNumberOfSpacesPerIndent().setValue(3);
            operator.ok();
            formatFileAndCompare("general", "Indentation.java");
        } finally {
            FormattingOptionsOperator.restoreDefaultValues();
        }
    }

    public void testContinuationIndentationSize() {
        try {
            JavaTabsAndIndentsOperator operator = FormattingOptionsOperator.invoke(true).getJavaTabsAndIndentsOperator();
            operator.getContinuationIndentationSize().setText("5");
            operator.ok();
            formatFileAndCompare("general", "Continuation.java");
        } finally {
            FormattingOptionsOperator.restoreDefaultValues();
        }
    }

    public void testLabelIndentationSize() {
        try {
            JavaTabsAndIndentsOperator operator = FormattingOptionsOperator.invoke(true).getJavaTabsAndIndentsOperator();
            operator.getLabelIndentation().setText("2");
            operator.ok();
            formatFileAndCompare("general", "Label.java");
        } finally {
            FormattingOptionsOperator.restoreDefaultValues();
        }
    }

    public void testAbsoluteLabelIndentationSize() {
        try {
            JavaTabsAndIndentsOperator operator = FormattingOptionsOperator.invoke(true).getJavaTabsAndIndentsOperator();
            operator.getAbsoluteLabelIndentation().changeSelection(true);
            operator.ok();
            formatFileAndCompare("general", "Label.java");
        } finally {
            FormattingOptionsOperator.restoreDefaultValues();
        }
    }

    public void testIndentTopLevelSize() {
        try {
            JavaTabsAndIndentsOperator operator = FormattingOptionsOperator.invoke(true).getJavaTabsAndIndentsOperator();
            operator.getIndentTopLevelClassMembers().changeSelection(false);
            operator.ok();
            formatFileAndCompare("general", "Indentation.java");
        } finally {
            FormattingOptionsOperator.restoreDefaultValues();
        }
    }

    public void testCaseIndent() {
        try {
            JavaTabsAndIndentsOperator operator = FormattingOptionsOperator.invoke(true).getJavaTabsAndIndentsOperator();
            operator.getIndentCaseStatementsInSwitch().changeSelection(false);
            operator.ok();
            formatFileAndCompare("general", "Indentation.java");
        } finally {
            FormattingOptionsOperator.restoreDefaultValues();
        }
    }


}
