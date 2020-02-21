/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
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
