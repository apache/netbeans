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

import org.netbeans.test.java.editor.formatting.operators.BracesOperator;
import org.netbeans.test.java.editor.formatting.operators.FormattingOptionsOperator;
import org.netbeans.test.java.editor.formatting.operators.JavaTabsAndIndentsOperator;
import org.netbeans.test.java.editor.formatting.operators.WrappingOperator;

/**
 *
 * @author jprox
 */
public class WrappingTest extends FormattingOptionsTest {

    public WrappingTest(String testMethodName) {
        super(testMethodName);
    }
   
    private FormattingOptionsOperator fo;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        fo = FormattingOptionsOperator.invoke(true);   
    }

    public void testExtendsImplementsKeyword_Always() {
        try {
            fo.getWrappingOperator().getExtendImplementsKeyWord().selectItem(WrappingOperator.ALWAYS);
            fo.ok();
            formatFileAndCompare("general", "Wrapping1.java");
        } finally {
            FormattingOptionsOperator.restoreDefaultValues();
        }
    }
    
    public void testExtendsImplementsKeyword_IfLong() {
        try {
            fo.getAllLanguageTabsAndIndentsOperator().getRightMargin().setValue(47);
            fo.getWrappingOperator().getExtendImplementsKeyWord().selectItem(WrappingOperator.IF_LONG);
            fo.ok();
            formatFileAndCompare("general", "Wrapping1.java");
        } finally {
            FormattingOptionsOperator.restoreDefaultValues();
        }
    }
    
    public void testExtendsImplementsList_Always() {
        try {
            fo.getWrappingOperator().getExtendsImplementsList().selectItem(WrappingOperator.ALWAYS);
            fo.ok();
            formatFileAndCompare("general", "Wrapping1.java");
        } finally {
            FormattingOptionsOperator.restoreDefaultValues();
        }
    }
    
    public void testExtendsImplementsList_IfLong() {
        try {
            fo.getAllLanguageTabsAndIndentsOperator().getRightMargin().setValue(45);
            fo.getWrappingOperator().getExtendsImplementsList().selectItem(WrappingOperator.IF_LONG);
            fo.ok();
            formatFileAndCompare("general", "Wrapping1.java");
        } finally {
            FormattingOptionsOperator.restoreDefaultValues();
        }
    }
    
    public void testMethodParameters_Always() {
        try {
            fo.getWrappingOperator().getMethodParameters().selectItem(WrappingOperator.ALWAYS);
            fo.ok();
            formatFileAndCompare("general", "Wrapping1.java");
        } finally {
            FormattingOptionsOperator.restoreDefaultValues();
        }
    }
    
    public void testMethodParameters_IfLong() {
        try {
            fo.getAllLanguageTabsAndIndentsOperator().getRightMargin().setValue(55);
            fo.getWrappingOperator().getMethodParameters().selectItem(WrappingOperator.IF_LONG);
            fo.ok();
            formatFileAndCompare("general", "Wrapping1.java");
        } finally {
            FormattingOptionsOperator.restoreDefaultValues();
        }
    }
    
    public void testMethodCallArgument_Always() {
        try {
            fo.getWrappingOperator().getMethodCallArguments().selectItem(WrappingOperator.ALWAYS);
            fo.ok();
            formatFileAndCompare("general", "Wrapping1.java");
        } finally {
            FormattingOptionsOperator.restoreDefaultValues();
        }
    }
    
    public void testMethodCallArgument_IfLong() {
        try {
            fo.getAllLanguageTabsAndIndentsOperator().getRightMargin().setValue(21);
            fo.getWrappingOperator().getMethodCallArguments().selectItem(WrappingOperator.IF_LONG);
            fo.ok();
            formatFileAndCompare("general", "Wrapping1.java");
        } finally {
            FormattingOptionsOperator.restoreDefaultValues();
        }
    }
    
    public void testAnnotationArguments_Always() {
        try {
            fo.getWrappingOperator().getAnnotationArguments().selectItem(WrappingOperator.ALWAYS);
            fo.ok();
            formatFileAndCompare("general", "Wrapping1.java");
        } finally {
            FormattingOptionsOperator.restoreDefaultValues();
        }
    }
    
    public void testAnnotationArguments_IfLong() {
        try {
            fo.getAllLanguageTabsAndIndentsOperator().getRightMargin().setValue(45);
            fo.getWrappingOperator().getAnnotationArguments().selectItem(WrappingOperator.IF_LONG);
            fo.ok();
            formatFileAndCompare("general", "Wrapping1.java");
        } finally {
            FormattingOptionsOperator.restoreDefaultValues();
        }
    }
    
    public void testChainedMethod_Always() {
        try {
            fo.getWrappingOperator().getChainedMethodCalls().selectItem(WrappingOperator.ALWAYS);
            fo.ok();
            formatFileAndCompare("general", "Wrapping1.java");
        } finally {
            FormattingOptionsOperator.restoreDefaultValues();
        }
    }
    
    public void testChainedMethod_IfLong() {
        try {
            fo.getAllLanguageTabsAndIndentsOperator().getRightMargin().setValue(48);
            fo.getWrappingOperator().getChainedMethodCalls().selectItem(WrappingOperator.IF_LONG);
            fo.ok();
           formatFileAndCompare("general", "Wrapping1.java");
        } finally {
            FormattingOptionsOperator.restoreDefaultValues();
        }
    }
    
    public void testWrapAfterDot() {
        try {
            fo.getWrappingOperator().getChainedMethodCalls().selectItem(WrappingOperator.ALWAYS);
            fo.getWrappingOperator().getWrapAfterDot().changeSelection(false);
            fo.ok();
            formatFileAndCompare("general", "Wrapping1.java");
        } finally {
            FormattingOptionsOperator.restoreDefaultValues();
        }
    }
    
    public void testThrowsKeyword_Always() {
        try {
            fo.getWrappingOperator().getThrowsKeyword().selectItem(WrappingOperator.ALWAYS);
            fo.ok();
            formatFileAndCompare("general", "Wrapping1.java");
        } finally {
            FormattingOptionsOperator.restoreDefaultValues();
        }
    }
    
    public void testThrowsKeyword_IfLong() {
        try {
            fo.getAllLanguageTabsAndIndentsOperator().getRightMargin().setValue(40);
            fo.getWrappingOperator().getThrowsKeyword().selectItem(WrappingOperator.IF_LONG);
            fo.ok();
            formatFileAndCompare("general", "Wrapping1.java");
        } finally {
            FormattingOptionsOperator.restoreDefaultValues();
        }
    }
    
    public void testThrowsList_Always() {
        try {
            fo.getWrappingOperator().getThrowsList().selectItem(WrappingOperator.ALWAYS);
            fo.ok();
            formatFileAndCompare("general", "Wrapping1.java");
        } finally {
            FormattingOptionsOperator.restoreDefaultValues();
        }
    }
    
    public void testThrowsList_IfLong() {
        try {
            fo.getAllLanguageTabsAndIndentsOperator().getRightMargin().setValue(31);
            fo.getWrappingOperator().getThrowsList().selectItem(WrappingOperator.IF_LONG);
            fo.ok();
            formatFileAndCompare("general", "Wrapping1.java");
        } finally {
            FormattingOptionsOperator.restoreDefaultValues();
        }
    }
    
    public void testArrayInitializer_Always() {
        try {
            fo.getWrappingOperator().getArrayInitializer().selectItem(WrappingOperator.ALWAYS);
            fo.ok();
            formatFileAndCompare("general", "Wrapping1.java");
        } finally {
            FormattingOptionsOperator.restoreDefaultValues();
        }
    }
    
    public void testArrayInitializer_IfLong() {
        try {
            fo.getAllLanguageTabsAndIndentsOperator().getRightMargin().setValue(35);
            fo.getWrappingOperator().getArrayInitializer().selectItem(WrappingOperator.IF_LONG);
            fo.ok();
            formatFileAndCompare("general", "Wrapping1.java");
        } finally {
            FormattingOptionsOperator.restoreDefaultValues();
        }
    }
    
    public void testTryResoueces_Always() {
        try {
            fo.getWrappingOperator().getTryResources().selectItem(WrappingOperator.ALWAYS);
            fo.ok();
            formatFileAndCompare("general", "Wrapping1.java");
        } finally {
            FormattingOptionsOperator.restoreDefaultValues();
        }
    }
    
    public void testTryResources_IfLong() {
        try {
            fo.getAllLanguageTabsAndIndentsOperator().getRightMargin().setValue(35);
            fo.getWrappingOperator().getTryResources().selectItem(WrappingOperator.IF_LONG);
            fo.ok();
            formatFileAndCompare("general", "Wrapping1.java");
        } finally {
            FormattingOptionsOperator.restoreDefaultValues();
        }
    }

    public void testDisjunctiveCatch_Always() {
        try {
            fo.getWrappingOperator().getDisjunctiveCatchTypes().selectItem(WrappingOperator.ALWAYS);
            fo.ok();
            formatFileAndCompare("general", "Wrapping1.java");
        } finally {
            FormattingOptionsOperator.restoreDefaultValues();
        }
    }
    
    public void testDisjunctiveCatch_IfLong() {
        try {
            fo.getAllLanguageTabsAndIndentsOperator().getRightMargin().setValue(25);
            fo.getWrappingOperator().getDisjunctiveCatchTypes().selectItem(WrappingOperator.IF_LONG);
            fo.ok();
            formatFileAndCompare("general", "Wrapping1.java");
        } finally {
            FormattingOptionsOperator.restoreDefaultValues();
        }
    }       
    
    private void disabledBracesGeneration() {
        fo.getBracesOperator().getIfBrace().selectItem(BracesOperator.BRACES_LEAVE_ALONE);
        fo.getBracesOperator().getForBrace().selectItem(BracesOperator.BRACES_LEAVE_ALONE);
        fo.getBracesOperator().getWhileBrace().selectItem(BracesOperator.BRACES_LEAVE_ALONE);
        fo.getBracesOperator().getDoWhileBrace().selectItem(BracesOperator.BRACES_LEAVE_ALONE);
    }
    
    public void testFor_Always() {
        try {
            disabledBracesGeneration();
            fo.getWrappingOperator().getForArgs().selectItem(WrappingOperator.ALWAYS);
            fo.ok();
            formatFileAndCompare("general", "Wrapping2.java");
        } finally {
            FormattingOptionsOperator.restoreDefaultValues();
        }
    }
    
    public void testFor_IfLong() {
        try {
            disabledBracesGeneration();
            fo.getAllLanguageTabsAndIndentsOperator().getRightMargin().setValue(45);
            fo.getWrappingOperator().getForArgs().selectItem(WrappingOperator.IF_LONG);
            fo.ok();
            formatFileAndCompare("general", "Wrapping2.java");
        } finally {
            FormattingOptionsOperator.restoreDefaultValues();
        }
    }
    
     public void testForStatement_Never() {
        try {
            disabledBracesGeneration();
            fo.getWrappingOperator().getForStatement().selectItem(WrappingOperator.NEVER);
            fo.ok();
            formatFileAndCompare("general", "Wrapping2.java");
        } finally {
            FormattingOptionsOperator.restoreDefaultValues();
        }
    }
    
    public void testForStatement_IfLong() {
        try {
            disabledBracesGeneration();
            fo.getAllLanguageTabsAndIndentsOperator().getRightMargin().setValue(45);
            fo.getWrappingOperator().getForStatement().selectItem(WrappingOperator.IF_LONG);
            fo.ok();
            formatFileAndCompare("general", "Wrapping2.java");
        } finally {
            FormattingOptionsOperator.restoreDefaultValues();
        }
    }
    
    
    public void testIfStatement_Never() {
        try {
            disabledBracesGeneration();
            fo.getWrappingOperator().getIfStatement().selectItem(WrappingOperator.NEVER);
            fo.ok();
            formatFileAndCompare("general", "Wrapping2.java");
        } finally {
            FormattingOptionsOperator.restoreDefaultValues();
        }
    }
    
    public void testIfStatement_IfLong() {
        try {
            disabledBracesGeneration();
            fo.getAllLanguageTabsAndIndentsOperator().getRightMargin().setValue(45);
            fo.getWrappingOperator().getIfStatement().selectItem(WrappingOperator.IF_LONG);
            fo.ok();
            formatFileAndCompare("general", "Wrapping2.java");
        } finally {
            FormattingOptionsOperator.restoreDefaultValues();
        }
    }
    
    public void testWhileStatement_Never() {
        try {
            disabledBracesGeneration();
            fo.getWrappingOperator().getWhileStatment().selectItem(WrappingOperator.NEVER);
            fo.ok();
            formatFileAndCompare("general", "Wrapping2.java");
        } finally {
            FormattingOptionsOperator.restoreDefaultValues();
        }
    }
    
    public void testWhileStatement_IfLong() {
        try {
            disabledBracesGeneration();
            fo.getAllLanguageTabsAndIndentsOperator().getRightMargin().setValue(45);
            fo.getWrappingOperator().getWhileStatment().selectItem(WrappingOperator.IF_LONG);
            fo.ok();
            formatFileAndCompare("general", "Wrapping2.java");
        } finally {
            FormattingOptionsOperator.restoreDefaultValues();
        }
    }
    
    public void testDoWhileStatement_Never() {
        try {
            disabledBracesGeneration();
            fo.getWrappingOperator().getDoWhileStatements().selectItem(WrappingOperator.NEVER);
            fo.ok();
            formatFileAndCompare("general", "Wrapping2.java");
        } finally {
            FormattingOptionsOperator.restoreDefaultValues();
        }
    }
    
    public void testDoWhileStatement_IfLong() {
        try {
            disabledBracesGeneration();
            fo.getAllLanguageTabsAndIndentsOperator().getRightMargin().setValue(25);
            fo.getWrappingOperator().getDoWhileStatements().selectItem(WrappingOperator.IF_LONG);
            fo.ok();
            formatFileAndCompare("general", "Wrapping2.java");
        } finally {
            FormattingOptionsOperator.restoreDefaultValues();
        }
    }
    
    public void testCaseStatement_Never() {
        try {
            disabledBracesGeneration();
            fo.getWrappingOperator().getCaseStatements().selectItem(WrappingOperator.NEVER);
            fo.ok();
            formatFileAndCompare("general", "Wrapping2.java");
        } finally {
            FormattingOptionsOperator.restoreDefaultValues();
        }
    }
    
    public void testCaseStatement_IfLong() {
        try {
            disabledBracesGeneration();
            fo.getAllLanguageTabsAndIndentsOperator().getRightMargin().setValue(25);
            fo.getWrappingOperator().getCaseStatements().selectItem(WrappingOperator.IF_LONG);
            fo.ok();
            formatFileAndCompare("general", "Wrapping2.java");
        } finally {
            FormattingOptionsOperator.restoreDefaultValues();
        }
    }
    
    public void testAssert_Always() {
        try {
            disabledBracesGeneration();
            fo.getWrappingOperator().getAssertStatement().selectItem(WrappingOperator.ALWAYS);
            fo.ok();
            formatFileAndCompare("general", "Wrapping2.java");
        } finally {
            FormattingOptionsOperator.restoreDefaultValues();
        }
    }
    
    public void testAssert_IfLong() {
        try {
            disabledBracesGeneration();
            fo.getAllLanguageTabsAndIndentsOperator().getRightMargin().setValue(25);
            fo.getWrappingOperator().getAssertStatement().selectItem(WrappingOperator.IF_LONG);
            fo.ok();
            formatFileAndCompare("general", "Wrapping2.java");
        } finally {
            FormattingOptionsOperator.restoreDefaultValues();
        }
    }
    
    public void testEnumConstants_Always() {
        try {
            disabledBracesGeneration();
            fo.getWrappingOperator().getEnumConstants().selectItem(WrappingOperator.ALWAYS);
            fo.ok();
            formatFileAndCompare("general", "Wrapping2.java");
        } finally {
            FormattingOptionsOperator.restoreDefaultValues();
        }
    }
    
    public void testEnumConstants_IfLong() {
        try {
            disabledBracesGeneration();
            fo.getAllLanguageTabsAndIndentsOperator().getRightMargin().setValue(45);
            fo.getWrappingOperator().getEnumConstants().selectItem(WrappingOperator.IF_LONG);
            fo.ok();
            formatFileAndCompare("general", "Wrapping2.java");
        } finally {
            FormattingOptionsOperator.restoreDefaultValues();
        }
    }
    
    public void testAnnotations_Never() {
        try {
            disabledBracesGeneration();
            fo.getWrappingOperator().getAnnotations().selectItem(WrappingOperator.NEVER);
            fo.ok();
            formatFileAndCompare("general", "Wrapping2.java");
        } finally {
            FormattingOptionsOperator.restoreDefaultValues();
        }
    }
    
    public void testAnnotations_IfLong() {
        try {
            disabledBracesGeneration();
            fo.getAllLanguageTabsAndIndentsOperator().getRightMargin().setValue(30);
            fo.getWrappingOperator().getAnnotations().selectItem(WrappingOperator.IF_LONG);
            fo.ok();
            formatFileAndCompare("general", "Wrapping2.java");
        } finally {
            FormattingOptionsOperator.restoreDefaultValues();
        }
    }
}
