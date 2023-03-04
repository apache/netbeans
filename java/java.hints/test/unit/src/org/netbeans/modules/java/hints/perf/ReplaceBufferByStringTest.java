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
