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

/**
 *
 * @author jprox
 */
public class BracesTest extends FormattingOptionsTest{
            

    public BracesTest(String testMethodName) {
        super(testMethodName);
    }
         
    private BracesOperator operator;
    
    @Override
    protected void setUp() throws Exception {
        super.setUp(); 
        operator = FormattingOptionsOperator.invoke(true).getBracesOperator();
    }
        
    public void testBracePositionClassDeclarationSameLine() {
        try {
            operator.getClassDeclaration().selectItem(BracesOperator.SAME_LINE);
            operator.ok();
            formatFileAndCompare("general", "BracesPosition.java");
        } finally {
            FormattingOptionsOperator.restoreDefaultValues();
        }
    }
    
    public void testBracePositionClassDeclarationNewLine() {
        try {
            operator.getClassDeclaration().selectItem(BracesOperator.NEW_LINE);
            operator.ok();
            formatFileAndCompare("general", "BracesPosition.java");
        } finally {
            FormattingOptionsOperator.restoreDefaultValues();
        }
    }
    
    public void testBracePositionClassDeclarationNewLineIndented() {
        try {
            operator.getClassDeclaration().selectItem(BracesOperator.NEW_LINE_INDENTED);
            operator.ok();
            formatFileAndCompare("general", "BracesPosition.java");
        } finally {
            FormattingOptionsOperator.restoreDefaultValues();
        }
    }
    
    public void testBracePositionClassDeclarationNewLineHalfIndented() {
        try {
            operator.getClassDeclaration().selectItem(BracesOperator.NEW_LINE_HALF_INDENTED);
            operator.ok();
            formatFileAndCompare("general", "BracesPosition.java");
        } finally {
            FormattingOptionsOperator.restoreDefaultValues();
        }
    }
    
    public void testBracePositionMethodDeclarationSameLine() {
        try {
            operator.getMethodDeclaration().selectItem(BracesOperator.SAME_LINE);
            operator.ok();
            formatFileAndCompare("general", "BracesPosition.java");
        } finally {
            FormattingOptionsOperator.restoreDefaultValues();
        }
    }
    
    public void testBracePositionMethodDeclarationNewLine() {
        try {
            operator.getMethodDeclaration().selectItem(BracesOperator.NEW_LINE);
            operator.ok();
            formatFileAndCompare("general", "BracesPosition.java");
        } finally {
            FormattingOptionsOperator.restoreDefaultValues();
        }
    }
    
    public void testBracePositionMethodDeclarationNewLineIndented() {
        try {
            operator.getMethodDeclaration().selectItem(BracesOperator.NEW_LINE_INDENTED);
            operator.ok();
            formatFileAndCompare("general", "BracesPosition.java");
        } finally {
            FormattingOptionsOperator.restoreDefaultValues();
        }
    }
    
    public void testBracePositionMethodDeclarationNewLineHalfIndented() {
        try {
            operator.getMethodDeclaration().selectItem(BracesOperator.NEW_LINE_HALF_INDENTED);
            operator.ok();
            formatFileAndCompare("general", "BracesPosition.java");
        } finally {
            FormattingOptionsOperator.restoreDefaultValues();
        }
    }
    
    public void testBracePositionOtherDeclarationSameLine() {
        try {
            operator.getOtherDeclaration().selectItem(BracesOperator.SAME_LINE);
            operator.ok();
            formatFileAndCompare("general", "BracesPosition.java");
        } finally {
            FormattingOptionsOperator.restoreDefaultValues();
        }
    }
    
    public void testBracePositionOtherDeclarationNewLine() {
        try {
            operator.getOtherDeclaration().selectItem(BracesOperator.NEW_LINE);
            operator.ok();
            formatFileAndCompare("general", "BracesPosition.java");
        } finally {
            FormattingOptionsOperator.restoreDefaultValues();
        }
    }
    
    public void testBracePositionOtherDeclarationNewLineIndented() {
        try {
            operator.getOtherDeclaration().selectItem(BracesOperator.NEW_LINE_INDENTED);
            operator.ok();
            formatFileAndCompare("general", "BracesPosition.java");
        } finally {
            FormattingOptionsOperator.restoreDefaultValues();
        }
    }
    
    public void testBracePositionOtherDeclarationNewLineHalfIndented() {
        try {
            operator.getOtherDeclaration().selectItem(BracesOperator.NEW_LINE_HALF_INDENTED);
            operator.ok();
            formatFileAndCompare("general", "BracesPosition.java");
        } finally {
            FormattingOptionsOperator.restoreDefaultValues();
        }
    }
    
    public void testSpecialElseIfTreatment() {
        try {
            operator.getspecialElseIf().changeSelection(false);
            operator.ok();
            formatFileAndCompare("general", "BracesPosition.java");
        } finally {
            FormattingOptionsOperator.restoreDefaultValues();
        }
    }
    
    public void testBraceGenerationIf_Generate() {
        try {
            operator.getIfBrace().selectItem(BracesOperator.BRACES_GENERATE);
            operator.ok();
            formatFileAndCompare("general", "BracesGeneration.java");
        } finally {
            FormattingOptionsOperator.restoreDefaultValues();
        }
    }
    
    public void testBraceGenerationIf_Eliminate() {
        try {
            operator.getIfBrace().selectItem(BracesOperator.BRACES_ELIMINATE);
            operator.ok();
            formatFileAndCompare("general", "BracesGeneration.java");
        } finally {
            FormattingOptionsOperator.restoreDefaultValues();
        }
    }
    
    public void testBraceGenerationIf_LeaveAlone() {
        try {
            operator.getIfBrace().selectItem(BracesOperator.BRACES_LEAVE_ALONE);
            operator.ok();
            formatFileAndCompare("general", "BracesGeneration.java");
        } finally {
            FormattingOptionsOperator.restoreDefaultValues();
        }
    }
    
    public void testBraceGenerationFor_Generate() {
        try {
            operator.getForBrace().selectItem(BracesOperator.BRACES_GENERATE);
            operator.ok();
            formatFileAndCompare("general", "BracesGeneration.java");
        } finally {
            FormattingOptionsOperator.restoreDefaultValues();
        }
    }
    
    public void testBraceGenerationFor_Eliminate() {
        try {
            operator.getForBrace().selectItem(BracesOperator.BRACES_ELIMINATE);
            operator.ok();
            formatFileAndCompare("general", "BracesGeneration.java");
        } finally {
            FormattingOptionsOperator.restoreDefaultValues();
        }
    }
    
    public void testBraceGenerationFor_LeaveAlone() {
        try {
            operator.getForBrace().selectItem(BracesOperator.BRACES_LEAVE_ALONE);
            operator.ok();
            formatFileAndCompare("general", "BracesGeneration.java");
        } finally {
            FormattingOptionsOperator.restoreDefaultValues();
        }
    }
 
    public void testBraceGenerationWhile_Generate() {
        try {
            operator.getWhileBrace().selectItem(BracesOperator.BRACES_GENERATE);
            operator.ok();
            formatFileAndCompare("general", "BracesGeneration.java");
        } finally {
            FormattingOptionsOperator.restoreDefaultValues();
        }
    }
    
    public void testBraceGenerationWhile_Eliminate() {
        try {
            operator.getWhileBrace().selectItem(BracesOperator.BRACES_ELIMINATE);
            operator.ok();
            formatFileAndCompare("general", "BracesGeneration.java");
        } finally {
            FormattingOptionsOperator.restoreDefaultValues();
        }
    }
    
    public void testBraceGenerationWhile_LeaveAlone() {
        try {
            operator.getWhileBrace().selectItem(BracesOperator.BRACES_LEAVE_ALONE);
            operator.ok();
            formatFileAndCompare("general", "BracesGeneration.java");
        } finally {
            FormattingOptionsOperator.restoreDefaultValues();
        }
    }
    
    public void testBraceGenerationDoWhile_Generate() {
        try {
            operator.getDoWhileBrace().selectItem(BracesOperator.BRACES_GENERATE);
            operator.ok();
            formatFileAndCompare("general", "BracesGeneration.java");
        } finally {
            FormattingOptionsOperator.restoreDefaultValues();
        }
    }
    
    public void testBraceGenerationDoWhile_Eliminate() {
        try {
            operator.getDoWhileBrace().selectItem(BracesOperator.BRACES_ELIMINATE);
            operator.ok();
            formatFileAndCompare("general", "BracesGeneration.java");
        } finally {
            FormattingOptionsOperator.restoreDefaultValues();
        }
    }
    
    public void testBraceGenerationDoWhile_LeaveAlone() {
        try {
            operator.getDoWhileBrace().selectItem(BracesOperator.BRACES_LEAVE_ALONE);
            operator.ok();
            formatFileAndCompare("general", "BracesGeneration.java");
        } finally {
            FormattingOptionsOperator.restoreDefaultValues();
        }
    }
    
    
    
}
