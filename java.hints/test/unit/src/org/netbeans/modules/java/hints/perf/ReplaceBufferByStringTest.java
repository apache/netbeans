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
package org.netbeans.modules.java.hints.perf;

import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.java.hints.test.api.HintTest;

/**
 *
 * @author sdedic
 */
public class ReplaceBufferByStringTest extends NbTestCase {

    public ReplaceBufferByStringTest(String name) {
        super(name);
    }
    
    public void testOnlyInitializer() throws Exception {
        HintTest
                .create()
                .input("package test;\n" +
"public final class Test {\n" +
"    public void test() {\n" +
"        StringBuffer sb = new StringBuffer().append(\"aa\");        \n" +
"        System.err.println(sb.toString());\n" +
"    }\n" +
"}")
                .run(ReplaceBufferByString.class)
                .findWarning("3:8-3:58:verifier:Replace StringBuffer/Builder by String")
                .applyFix("Replace by String")
                .assertCompilable()
                .assertOutput("package test;\n" +
"public final class Test {\n" +
"    public void test() {\n" +
"        String sb = \"aa\";      \n" +
"        System.err.println(sb);\n" +
"    }\n" +
"}");
    }
    
    public void testAppendedInMethod() throws Exception {
        HintTest
                .create()
                .input("package test;\n" +
"public final class Test {\n" +
"    public void test() {\n" +
"        StringBuffer sb = new StringBuffer().append(\"aa\");      \n" +
"        sb.append(\"bar\");\n" +
"        System.err.println(sb.toString());\n" +
"    }\n" +
"}")
                .run(ReplaceBufferByString.class)
                .assertWarnings();
    }
    
    // see #239082, StringBuffer(int) should not be reported, not known yet what is the intent
    public void testConstructorWithLength() throws Exception {
        HintTest
                .create()
                .input("package test;\n" +
                "public final class Test {\n" +
                "    public void test() {\n" +
                "        int x = 1;\n" +
                "        StringBuffer sb = new StringBuffer(x);\n" +
                "        String y = sb.toString();\n" +
                "    }\n" +
                "}")
                .run(ReplaceBufferByString.class)
                .assertWarnings();
    }
    
    public void testAppendStringAndChar() throws Exception {
        HintTest
                .create()
                .input("package test;\n" +
                "public final class Test {\n" +
                "    public void test() {\n" +
                "        StringBuffer sb = new StringBuffer(\"x\").append('a');\n" +
                "        String y = sb.toString();\n" +
                "    }\n" +
                "}")
                .run(ReplaceBufferByString.class)
                .findWarning("3:8-3:60:verifier:Replace StringBuffer/Builder by String")
                .applyFix()
                .assertCompilable()
                .assertOutput("package test;\n" +
                "public final class Test {\n" +
                "    public void test() {\n" +
                "        String sb = \"xa\";\n" +
                "        String y = sb;\n" +
                "    }\n" +
                "}");
    }

    public void testStringAccessInMethod() throws Exception {
        HintTest
                .create()
                .input("package test;\n" +
"public final class Test {\n" +
"    public void test() {\n" +
"        StringBuffer sb = new StringBuffer().append(\"aa\");      \n" +
"        System.err.println(sb.indexOf(\"a\"));\n" +
"        System.err.println(sb.toString());\n" +
"    }\n" +
"}")
                .run(ReplaceBufferByString.class)
                .findWarning("3:8-3:58:verifier:Replace StringBuffer/Builder by String")
                .applyFix("Replace by String")
                .assertCompilable()
                .assertOutput("package test;\n" +
"public final class Test {\n" +
"    public void test() {\n" +
"        String sb = \"aa\";      \n" +
"        System.err.println(sb.indexOf(\"a\"));\n" +
"        System.err.println(sb);\n" +
"    }\n" +
"}");
    }
    
    public void testNonStringAccessInMethod() throws Exception {
        HintTest
                .create()
                .input("package test;\n" +
"public final class Test {\n" +
"    public void test() {\n" +
"        StringBuffer sb = new StringBuffer().append(\"aa\");      \n" +
"        System.err.println(sb.capacity());\n" +
"        System.err.println(sb.toString());\n" +
"    }\n" +
"}")
                .run(ReplaceBufferByString.class)
                .assertWarnings();
    }
    
    public void testSingleObjectInInit() throws Exception {
        HintTest
                .create()
                .input("package test;\n" +
"public final class Test {\n" +
"    public void test() {\n" +
"        StringBuffer sb = new StringBuffer().append(new Object());      \n" +
"        System.err.println(sb.toString());\n" +
"    }\n" +
"}")
                .run(ReplaceBufferByString.class)
                .findWarning("3:8-3:66:verifier:Replace StringBuffer/Builder by String")
                .applyFix("Replace by String")
                .assertCompilable()
                .assertOutput("package test;\n" +
"public final class Test {\n" +
"    public void test() {\n" +
"        String sb = String.valueOf(new Object());      \n" +
"        System.err.println(sb);\n" +
"    }\n" +
"}");
    }
    
    public void testTwoNumbers() throws Exception {
        HintTest
                .create()
                .input("package test;\n" +
"public final class Test {\n" +
"    public void test() {\n" +
"        StringBuffer sb = new StringBuffer().append(1).append(2);      \n" +
"        System.err.println(sb.toString());\n" +
"    }\n" +
"}")
                .run(ReplaceBufferByString.class)
                .findWarning("3:8-3:65:verifier:Replace StringBuffer/Builder by String")
                .applyFix("Replace by String")
                .assertCompilable()
                .assertOutput("package test;\n" +
"public final class Test {\n" +
"    public void test() {\n" +
"        String sb = \"12\";      \n" +
"        System.err.println(sb);\n" +
"    }\n" +
"}");
    }
}
