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

import org.junit.After;
import org.junit.AfterClass;
import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.netbeans.cnd.api.lexer.CppTokenId;

/**
 *
 *
 */
public class DoxygenDocumentationTest {

    public DoxygenDocumentationTest() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }

    @Before
    public void setUp() {
    }

    @After
    public void tearDown() {
    }

    @Test
    public void doxygen2HTMLDefault() {
        String doxygen =
 "/**\n" +
 "* Document main(int,char**) here...\n" +
 "*\n" +
 "* @param argc\n" +
 "* @param argv\n" +
 "* @return ...\n" +
 "*\n" +
 "*/";
        String expResult = "<p>Document main(int,char**) here...\n</p><p>\n<strong>Parameter:</strong><br>&nbsp;  <i>argc</i>\n</p><p>\n<strong>Parameter:</strong><br>&nbsp;  <i>argv</i>\n</p><p>\n<strong>Returns:</strong><br>&nbsp;  ...\n</p><p>\n<strong>Author:</strong><br>&nbsp;  thp</p>";
        String result = DoxygenDocumentation.doxygen2HTML(doxygen, CppTokenId.DOXYGEN_COMMENT);
        assertEquals(expResult, result);
    }

    @Test
    public void doxygen2HTMLVerbatim() {
        String doxygen =
 "/**\n" +
 "* abc def\n" +
 "* ghi\n" +
 "*\n" +
 "* @verbatim\n" +
 "* 111\n" +
 "*   333\n" +
 "* 444\n" +
 "* @endverbatim\n" +
 "*\n" +
 "* jkl lmn\n" +
 "* opq\n" +
 "*/";
        String expResult = "<p>abc def ghi\n</p><p>\n<pre>111\n  333\n444\n</pre>\n</p><p>\n jkl lmn opq</p>";
        String result = DoxygenDocumentation.doxygen2HTML(doxygen, CppTokenId.DOXYGEN_COMMENT);
        assertEquals(expResult, result);
    }

    @Test
    public void doxygen2HTMLdoxygen2HTMLVerbatim2() {
        String doxygen =
 "/**\\verbatim\n" +
 "* 2<1>3\n" +
 "* \\endverbatim\n" +
 "*/";
        String expResult = "<p><pre>2&lt;1&gt;3\n</pre></p>";
        String result = DoxygenDocumentation.doxygen2HTML(doxygen, CppTokenId.DOXYGEN_COMMENT);
        assertEquals(expResult, result);
    }

    @Test
    public void doxygen2HTMLUnimplemented() {
        System.out.println("doxygen2HTMLUnimplemented");
        String doxygen =
 "/**\n" +
 "* Document...\n" +
 "*\n" +
 "* @unimplemented xyz\n" +
 "*\n" +
 "*/";
        String expResult = "<p>Document...\n</p><p>\n<strong>unimplemented:</strong><br>&nbsp;  xyz\n</p><p>\n<strong>Author:</strong><br>&nbsp;  thp</p>";
        String result = DoxygenDocumentation.doxygen2HTML(doxygen, CppTokenId.DOXYGEN_COMMENT);
        assertEquals(expResult, result);
    }

    @Test
    public void doxygen2HTMLCode() {
        String doxygen =
 "/**\n" +
 "* abc def\n" +
 "* ghi\n" +
 "*\n" +
 "* \\code\n" +
 "* 111\n" +
 "*   333\n" +
 "* 444\n" +
 "* \\endcode\n" +
 "*\n" +
 "* jkl lmn\n" +
 "* opq\n" +
 "*/";
        String expResult = "<p>abc def ghi\n</p><p>\n<pre>111\n  333\n444\n</pre>\n</p><p>\n jkl lmn opq</p>";
        String result = DoxygenDocumentation.doxygen2HTML(doxygen, CppTokenId.DOXYGEN_COMMENT);
        assertEquals(expResult, result);
    }

    @Test
    public void doxygen2HTMLCode2() {
        String doxygen =
 "/**\\code\n" +
 "*  2<1>3\n" +
 "* \\endcode\n" +
 "*/";
        String expResult = "<p><pre> 2&lt;1&gt;3\n</pre></p>";
        String result = DoxygenDocumentation.doxygen2HTML(doxygen, CppTokenId.DOXYGEN_COMMENT);
        assertEquals(expResult, result);
    }

    @Test
    public void doxygen2HTMLEscaped() {
        String doxygen =
 "/**\n" +
 "* 2\\<1\\>3\n" +
 "* \n" +
 "*/";
        String expResult = "<p>2&lt;<1&gt;>3</p>";
        String result = DoxygenDocumentation.doxygen2HTML(doxygen, CppTokenId.DOXYGEN_COMMENT);
        assertEquals(expResult, result);
    }
}