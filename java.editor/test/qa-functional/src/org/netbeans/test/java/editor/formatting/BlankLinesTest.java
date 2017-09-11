/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2014 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2014 Sun Microsystems, Inc.
 */

package org.netbeans.test.java.editor.formatting;

import org.netbeans.test.java.editor.formatting.operators.BlankLinesOperatror;
import org.netbeans.test.java.editor.formatting.operators.BracesOperator;
import org.netbeans.test.java.editor.formatting.operators.FormattingOptionsOperator;

/**
 *
 * @author jprox
 */
public class BlankLinesTest  extends FormattingOptionsTest {

    public BlankLinesTest(String testMethodName) {
        super(testMethodName);
    }
    
    private BlankLinesOperatror operator;
    
    @Override
    protected void setUp() throws Exception {
        super.setUp(); 
        operator = FormattingOptionsOperator.invoke(true).getBlankLinesOperatror();
    }
    
    
    public void testBeforePackage() {
        try {
            operator.getInDeclaration().setText("3");
            operator.getBeforePackage().setText("3");
            operator.ok();
            formatFileAndCompare("general", "BlankLines.java");
        } finally {
            FormattingOptionsOperator.restoreDefaultValues();
        }
    }
    
    public void testAfterPackage() {
        try {
            operator.getInDeclaration().setText("3");
            operator.getAfterPackage().setText("3");
            operator.ok();
            formatFileAndCompare("general", "BlankLines.java");
        } finally {
            FormattingOptionsOperator.restoreDefaultValues();
        }
    }
    
    public void testBeforeImports() {
        try {
            operator.getInDeclaration().setText("3");
            operator.getBeforeImports().setText("3");
            operator.ok();
            formatFileAndCompare("general", "BlankLines.java");
        } finally {
            FormattingOptionsOperator.restoreDefaultValues();
        }
    }
    
    public void testAfterImports() {
        try {
            operator.getInDeclaration().setText("3");
            operator.getAfterImports().setText("3");
            operator.ok();
            formatFileAndCompare("general", "BlankLines.java");
        } finally {
            FormattingOptionsOperator.restoreDefaultValues();
        }
    }
    
    public void testBeforeClass() {
        try {
            operator.getInDeclaration().setText("3");
            operator.getBeforeClass().setText("3");
            operator.ok();
            formatFileAndCompare("general", "BlankLines.java");
        } finally {
            FormattingOptionsOperator.restoreDefaultValues();
        }
    }
    
    public void testAfterClass() {
        try {
            operator.getInDeclaration().setText("3");
            operator.getAfterClass().setText("3");
            operator.ok();
            formatFileAndCompare("general", "BlankLines.java");
        } finally {
            FormattingOptionsOperator.restoreDefaultValues();
        }
    }
    
    public void testAfterClassHeader() {
        try {
            operator.getInDeclaration().setText("3");
            operator.getAfterClassHeader().setText("3");
            operator.ok();
            formatFileAndCompare("general", "BlankLines.java");
        } finally {
            FormattingOptionsOperator.restoreDefaultValues();
        }
    }
    
    public void testAfterAnonymousClassHeader() {
        try {
            operator.getInDeclaration().setText("3");
            operator.getInCode().setText("3");
            operator.getAfterAnonymousClassHeader().setText("3");
            operator.ok();
            formatFileAndCompare("general", "BlankLines.java");
        } finally {
            FormattingOptionsOperator.restoreDefaultValues();
        }
    }
    
    public void testBeforeClassClosingBrace() {
        try {
            operator.getInDeclaration().setText("3");
            operator.getBeforeClassClosingBrace().setText("3");
            operator.ok();
            formatFileAndCompare("general", "BlankLines.java");
        } finally {
            FormattingOptionsOperator.restoreDefaultValues();
        }
    }
    
    public void testBeforeAnonymousClassClosingBrace() {
        try {
            operator.getInDeclaration().setText("3");
            operator.getInCode().setText("3");
            operator.getBeforeAnonymousClassClosingBrace().setText("3");
            operator.ok();
            formatFileAndCompare("general", "BlankLines.java");
        } finally {
            FormattingOptionsOperator.restoreDefaultValues();
        }
    }
    
    public void testBeforeField() {
        try {
            operator.getInDeclaration().setText("3");
            operator.getBeforeField().setText("3");
            operator.ok();
            formatFileAndCompare("general", "BlankLines.java");
        } finally {
            FormattingOptionsOperator.restoreDefaultValues();
        }
    }
    
    public void testAfterField() {
        try {
            operator.getInDeclaration().setText("3");
            operator.getAfterField().setText("3");
            operator.ok();
            formatFileAndCompare("general", "BlankLines.java");
        } finally {
            FormattingOptionsOperator.restoreDefaultValues();
        }
    }
    
    public void testBeforeMethod() {
        try {
            operator.getInDeclaration().setText("3");
            operator.getBeforeMethod().setText("3");
            operator.ok();
            formatFileAndCompare("general", "BlankLines.java");
        } finally {
            FormattingOptionsOperator.restoreDefaultValues();
        }
    }
    
    public void testAfterMethod() {
        try {
            operator.getInDeclaration().setText("3");
            operator.getAfterMethod().setText("3");
            operator.ok();
            formatFileAndCompare("general", "BlankLines.java");
        } finally {
            FormattingOptionsOperator.restoreDefaultValues();
        }
    }
    
    public void testInDeclaration() {
        try {
            operator.getInDeclaration().setText("1");
            operator.getInCode().setText("10");
            operator.ok();
            formatFileAndCompare("general", "ReduceBlankLines.java");
        } finally {
            FormattingOptionsOperator.restoreDefaultValues();
        }
    }
    
    public void testInCode() {
        try {
            operator.getInDeclaration().setText("10");
            operator.getInCode().setText("1");
            operator.ok();
            formatFileAndCompare("general", "ReduceBlankLines.java");
        } finally {
            FormattingOptionsOperator.restoreDefaultValues();
        }
    }
    
}
