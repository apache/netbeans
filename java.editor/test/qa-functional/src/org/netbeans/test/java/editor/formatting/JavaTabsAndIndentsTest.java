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
