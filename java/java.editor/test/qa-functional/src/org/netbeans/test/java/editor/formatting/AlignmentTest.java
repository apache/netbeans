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

import org.netbeans.test.java.editor.formatting.operators.AlignmentOperator;
import org.netbeans.test.java.editor.formatting.operators.FormattingOptionsOperator;

/**
 *
 * @author jprox
 */
public class AlignmentTest extends FormattingOptionsTest {

    public AlignmentTest(String testMethodName) {
        super(testMethodName);
    }
    
    AlignmentOperator operator;
    
    @Override
    protected void setUp() throws Exception {
        super.setUp(); 
        operator = FormattingOptionsOperator.invoke(true).getAlignmentOperator();
    }
    
    
    public void testElseAlignment() {
        try {
            operator.getNewLineElseOperator().changeSelection(true);
            operator.ok();
            formatFileAndCompare("general", "Alignment.java");
        } finally {
            FormattingOptionsOperator.restoreDefaultValues();
        }
    }

    public void testWhileAlignment() {
        try {
            operator.getNewLineWhileOperator().changeSelection(true);
            operator.ok();
            formatFileAndCompare("general", "Alignment.java");
        } finally {
            FormattingOptionsOperator.restoreDefaultValues();
        }
    }

    public void testCatchAlignment() {
        try {
            operator.getNewLineCatch().changeSelection(true);
            operator.ok();
            formatFileAndCompare("general", "Alignment.java");
        } finally {
            FormattingOptionsOperator.restoreDefaultValues();
        }
    }

    public void testFinallyAlignment() {
        try {
            operator.getNewLineFinally().changeSelection(true);
            operator.ok();
            formatFileAndCompare("general", "Alignment.java");
        } finally {
            FormattingOptionsOperator.restoreDefaultValues();
        }
    }

    public void testModifiersAlignment() {
        try {
            operator.getNewLineAfterModifiers().changeSelection(true);
            operator.ok();
            formatFileAndCompare("general", "Alignment.java");
        } finally {
            FormattingOptionsOperator.restoreDefaultValues();
        }
    }

    public void testMethodsParametersAlignment() {
        try {
            operator.getAlignmentMethodsParameters().changeSelection(true);
            operator.ok();
            formatFileAndCompare("general", "Alignment2.java");
        } finally {
            FormattingOptionsOperator.restoreDefaultValues();
        }
    }

    public void testAnnotationArgumentsAlignment() {
        try {
            operator.getAlignmentAnnotationArguments().changeSelection(true);
            operator.ok();
            formatFileAndCompare("general", "Alignment2.java");
        } finally {
            FormattingOptionsOperator.restoreDefaultValues();
        }
    }

    public void testThrowsListAlignment() {
        try {
            operator.getAlignmentThrowsList().changeSelection(true);
            operator.ok();
            formatFileAndCompare("general", "Alignment2.java");
        } finally {
            FormattingOptionsOperator.restoreDefaultValues();
        }
    }

    public void testBinaryOperatorsAlignment() {
        try {
            operator.getAlignmentBinaryOperators().changeSelection(true);
            operator.ok();
            formatFileAndCompare("general", "Alignment2.java");
        } finally {
            FormattingOptionsOperator.restoreDefaultValues();
        }
    }

    public void testAssignmentAlignment() {
        try {
            operator.getAlignmentAssignement().changeSelection(true);
            operator.ok();
            formatFileAndCompare("general", "Alignment2.java");
        } finally {
            FormattingOptionsOperator.restoreDefaultValues();
        }
    }

    public void testMethodCallArgumentsAlignment() {
        try {
            operator.getAlignmentMethodCallArguments().changeSelection(true);
            operator.ok();
            formatFileAndCompare("general", "Alignment3.java");
        } finally {
            FormattingOptionsOperator.restoreDefaultValues();
        }
    }

    public void testImplementsListAlignment() {
        try {
            operator.getAlignmentImplementsList().changeSelection(true);
            operator.ok();
            formatFileAndCompare("general", "Alignment3.java");
        } finally {
            FormattingOptionsOperator.restoreDefaultValues();
        }
    }

    public void testArrayInitializerAlignment() {
        try {
            operator.getAlignmentArrayInitializer().changeSelection(true);
            operator.ok();
            formatFileAndCompare("general", "Alignment3.java");
        } finally {
            FormattingOptionsOperator.restoreDefaultValues();
        }
    }

    public void testTernaryOperatorsAlignment() {
        try {
            operator.getAlignmentTernaryOperators().changeSelection(true);
            operator.ok();
            formatFileAndCompare("general", "Alignment3.java");
        } finally {
            FormattingOptionsOperator.restoreDefaultValues();
        }
    }

    public void testFor() {
        try {
            operator.getAlignmentFor().changeSelection(true);
            operator.ok();
            formatFileAndCompare("general", "Alignment3.java");
        } finally {
            FormattingOptionsOperator.restoreDefaultValues();
        }
    }

}
