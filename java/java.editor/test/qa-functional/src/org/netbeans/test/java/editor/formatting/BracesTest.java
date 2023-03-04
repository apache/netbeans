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
