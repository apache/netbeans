/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2010 Sun Microsystems, Inc.
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