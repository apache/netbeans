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
