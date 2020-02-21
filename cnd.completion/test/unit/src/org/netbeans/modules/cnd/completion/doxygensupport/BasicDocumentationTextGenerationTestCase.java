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
package org.netbeans.modules.cnd.completion.doxygensupport;

import org.netbeans.cnd.api.lexer.CppTokenId;

/**
 *
 *
 */
public class BasicDocumentationTextGenerationTestCase extends DocumentationTextGenerationBaseTestCase {

    public BasicDocumentationTextGenerationTestCase(String testName) {
        super(testName);
    }

    public void test216015() throws Exception {
        performTest("doctest.c", 21, 13, DoxygenDocumentation.doxygen2HTML("/* Link count.  */", CppTokenId.BLOCK_COMMENT));
    }

    public void test216015_1() throws Exception {
        performTest("doctest.c", 22, 12, DoxygenDocumentation.doxygen2HTML("/* File mode.  */", CppTokenId.BLOCK_COMMENT));
    }

    public void test216015_2() throws Exception {
        performTest("doctest.c", 23, 6, DoxygenDocumentation.doxygen2HTML("/* this is a comment\n    and it extends until the closing\n    star-slash comment mark */", CppTokenId.BLOCK_COMMENT));
    }

    public void test216015_3() throws Exception {
        performTest("doctest.c", 24, 6, DoxygenDocumentation.doxygen2HTML("// double slash comment", CppTokenId.LINE_COMMENT));
    }
    
    public void test216015_4() throws Exception {
        performTest("doctest.c", 25, 6, DoxygenDocumentation.doxygen2HTML("// double slash comment2", CppTokenId.LINE_COMMENT));
    }
    
    public void test216015_5() throws Exception {
        performTest("doctest.c", 26, 7, DoxygenDocumentation.doxygen2HTML("// Comment 1", CppTokenId.LINE_COMMENT));
    }
    
    public void test216015_6() throws Exception {
        performTest("doctest.c", 27, 7, DoxygenDocumentation.doxygen2HTML("/*! Comment 4*/", CppTokenId.DOXYGEN_COMMENT));
    }
    
    public void test216015_7() throws Exception {
        performTest("doctest.c", 28, 7, DoxygenDocumentation.doxygen2HTML("/// Comment 6", CppTokenId.DOXYGEN_LINE_COMMENT));
    }
    
    public void test228509() throws Exception {
        performTest("doctest.c", 30, 17, DoxygenDocumentation.doxygen2HTML("// maximum iterated elements allowed to written per file", CppTokenId.LINE_COMMENT));
    }
    
    public void test228509_1() throws Exception {
        performTest("doctest.c", 31, 61, DoxygenDocumentation.doxygen2HTML("/*param*/", CppTokenId.BLOCK_COMMENT));
    }
    
    public void test228509_2() throws Exception {
        performTest("doctest.c", 31, 65, DoxygenDocumentation.doxygen2HTML("/*param2*/", CppTokenId.BLOCK_COMMENT));
    }
    
    public void test228509_3() throws Exception {
        performTest("doctest.c", 31, 10, DoxygenDocumentation.doxygen2HTML("// line com", CppTokenId.LINE_COMMENT));
    }
}
