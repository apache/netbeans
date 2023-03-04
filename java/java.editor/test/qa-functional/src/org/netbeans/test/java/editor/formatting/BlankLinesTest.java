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
