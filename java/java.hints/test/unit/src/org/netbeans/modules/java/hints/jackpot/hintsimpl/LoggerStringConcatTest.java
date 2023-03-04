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
package org.netbeans.modules.java.hints.jackpot.hintsimpl;

import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.java.hints.test.api.HintTest;

/**
 *
 * @author lahvac
 */
public class LoggerStringConcatTest extends NbTestCase {

    public LoggerStringConcatTest(String name) {
        super(name);
    }

    public void testSimpleLogMethod() throws Exception {
        HintTest
                .create()
                .input("package test;\n" +
                       "import java.util.logging.Level;\n" +
                       "import java.util.logging.Logger;\n" +
                       "public class Test {\n" +
                       "    private void t(Logger l, int a, int b, int c) {\n" +
                       "        l.log(Level.SEVERE, \"a=\" + a + \", b=\" + b + \", c=\" + c);\n" +
                       "    }\n" +
                       "}\n")
                .run(LoggerStringConcat.class)
                .findWarning("5:28-5:62:verifier:Inefficient use of string concatenation in logger")
                .applyFix("MSG_LoggerStringConcat_fix")
                .assertCompilable()
                .assertOutput("package test;\n" +
                              "import java.util.logging.Level;\n" +
                              "import java.util.logging.Logger;\n" +
                              "public class Test {\n" +
                              "    private void t(Logger l, int a, int b, int c) {\n" +
                              "        l.log(Level.SEVERE, \"a={0}, b={1}, c={2}\", new Object[]{a, b, c});\n" +
                              "    }\n" +
                              "}\n");
    }

    public void testSimpleOtherMethod() throws Exception {
        HintTest
                .create()
                .input("package test;\n" +
                       "import java.util.logging.Level;\n" +
                       "import java.util.logging.Logger;\n" +
                       "public class Test {\n" +
                       "    private void t(Logger l, int a, int b, int c) {\n" +
                       "        l.severe(\"a=\" + a + \", b=\" + b + \", c=\" + c);\n" +
                       "    }\n" +
                       "}\n")
                .run(LoggerStringConcat.class)
                .findWarning("5:17-5:51:verifier:Inefficient use of string concatenation in logger")
                .applyFix("MSG_LoggerStringConcat_fix")
                .assertCompilable()
                .assertOutput("package test;\n" +
                              "import java.util.logging.Level;\n" +
                              "import java.util.logging.Logger;\n" +
                              "public class Test {\n" +
                              "    private void t(Logger l, int a, int b, int c) {\n" +
                              "        l.log(Level.SEVERE, \"a={0}, b={1}, c={2}\", new Object[]{a, b, c});\n" +
                              "    }\n" +
                              "}\n");
    }

    public void testEscape1() throws Exception {
        HintTest
                .create()
                .input("package test;\n" +
                       "import java.util.logging.Level;\n" +
                       "import java.util.logging.Logger;\n" +
                       "public class Test {\n" +
                       "    private void t(Logger l, int a, int b, int c) {\n" +
                       "        l.severe(\"a'=\" + a + \",' b'='\" + b + \", c=\" + c);\n" +
                       "    }\n" +
                       "}\n")
                .run(LoggerStringConcat.class)
                .findWarning("5:17-5:55:verifier:Inefficient use of string concatenation in logger")
                .applyFix("MSG_LoggerStringConcat_fix")
                .assertCompilable()
                .assertOutput("package test;\n" +
                              "import java.util.logging.Level;\n" +
                              "import java.util.logging.Logger;\n" +
                              "public class Test {\n" +
                              "    private void t(Logger l, int a, int b, int c) {\n" +
                              "        l.log(Level.SEVERE, \"a''={0},'' b''=''{1}, c={2}\", new Object[]{a, b, c});\n" +
                              "    }\n" +
                              "}\n");
    }

    public void testEscape2() throws Exception {
        HintTest
                .create()
                .input("package test;\n" +
                       "import java.util.logging.Level;\n" +
                       "import java.util.logging.Logger;\n" +
                       "public class Test {\n" +
                       "    private void t(Logger l, int a, int b, int c) {\n" +
                       "        l.severe(\"a=${\" + a + \"}.\");\n" +
                       "    }\n" +
                       "}\n")
                .run(LoggerStringConcat.class)
                .findWarning("5:17-5:34:verifier:Inefficient use of string concatenation in logger")
                .applyFix("MSG_LoggerStringConcat_fix")
                .assertCompilable()
                .assertOutput("package test;\n" +
                              "import java.util.logging.Level;\n" +
                              "import java.util.logging.Logger;\n" +
                              "public class Test {\n" +
                              "    private void t(Logger l, int a, int b, int c) {\n" +
                              "        l.log(Level.SEVERE, \"a=$'{'{0}'}'.\", a);\n" +
                              "    }\n" +
                              "}\n");
    }

    public void testEscape3() throws Exception {
        HintTest
                .create()
                .input("package test;\n" +
                       "import java.util.logging.Level;\n" +
                       "import java.util.logging.Logger;\n" +
                       "public class Test {\n" +
                       "    private void t(Logger l, int a, int b, int c) {\n" +
                       "        l.severe(\"a=\" + \", b='\" + b + \"'.\");\n" +
                       "    }\n" +
                       "}\n")
                .run(LoggerStringConcat.class)
                .findWarning("5:17-5:42:verifier:Inefficient use of string concatenation in logger")
                .applyFix("MSG_LoggerStringConcat_fix")
                .assertCompilable()
                .assertOutput("package test;\n" +
                              "import java.util.logging.Level;\n" +
                              "import java.util.logging.Logger;\n" +
                              "public class Test {\n" +
                              "    private void t(Logger l, int a, int b, int c) {\n" +
                              "        l.log(Level.SEVERE, \"a=, b=''{0}''.\", b);\n" +
                              "    }\n" +
                              "}\n");
    }

    public void testErroneous() throws Exception {
        HintTest
                .create()
                .input("package test;\n" +
                       "import java.util.logging.Level;\n" +
                       "import java.util.logging.Logger;\n" +
                       "public class Test {\n" +
                       "    private void t(Logger l, int a, int b, int c) {\n" +
                       "        l.severe(\"a=\" + \", b='\" + );\n" +
                       "    }\n" +
                       "}\n", false)
                .run(LoggerStringConcat.class)
                .assertWarnings();
    }

    public void test186524() throws Exception {
        HintTest
                .create()
                .input("package test;\n" +
                       "import java.util.logging.Level;\n" +
                       "import java.util.logging.Logger;\n" +
                       "public class Test {\n" +
                       "    private static final String A = \"a\";\n" +
                       "    private void t(Logger l, int a, int b, int c) {\n" +
                       "        l.severe(A + \"=${\" + a + \"}.\");\n" +
                       "    }\n" +
                       "}\n")
                .run(LoggerStringConcat.class)
                .findWarning("6:17-6:37:verifier:Inefficient use of string concatenation in logger")
                .applyFix("MSG_LoggerStringConcat_fix")
                .assertCompilable()
                .assertOutput("package test;\n" +
                              "import java.util.logging.Level;\n" +
                              "import java.util.logging.Logger;\n" +
                              "public class Test {\n" +
                              "    private static final String A = \"a\";\n" +
                              "    private void t(Logger l, int a, int b, int c) {\n" +
                              "        l.log(Level.SEVERE,A + \"=$'{'{0}'}'.\", a);\n" +
                              "    }\n" +
                              "}\n");
    }

    public void test181962() throws Exception {
        HintTest
                .create()
                .input("package test;\n" +
                       "import java.util.logging.Level;\n" +
                       "import java.util.logging.Logger;\n" +
                       "public class Test {\n" +
                       "    private void t(Logger l, int a, int b, int c) {\n" +
                       "        l.severe(\"A=/\" + a + '/');\n" +
                       "    }\n" +
                       "}\n")
                .run(LoggerStringConcat.class)
                .findWarning("5:17-5:32:verifier:Inefficient use of string concatenation in logger")
                .applyFix("MSG_LoggerStringConcat_fix")
                .assertCompilable()
                .assertOutput("package test;\n" +
                              "import java.util.logging.Level;\n" +
                              "import java.util.logging.Logger;\n" +
                              "public class Test {\n" +
                              "    private void t(Logger l, int a, int b, int c) {\n" +
                              "        l.log(Level.SEVERE, \"A=/{0}/\", a);\n" +
                              "    }\n" +
                              "}\n");
    }

    public void test204634() throws Exception {
        HintTest
                .create()
                .input("package test;\n" +
                       "import java.util.logging.Level;\n" +
                       "import java.util.logging.Logger;\n" +
                       "public class Test extends Logger {\n" +
                       "    private void t(Logger l, int a, int b, int c) {\n" +
                       "        severe(\"A=/\" + a + '/');\n" +
                       "    }\n" +
                       "}\n", false)
                . //missing constructor
                run(LoggerStringConcat.class)
                .findWarning("5:15-5:30:verifier:Inefficient use of string concatenation in logger")
                .applyFix("MSG_LoggerStringConcat_fix")
                .assertOutput("package test;\n" +
                              "import java.util.logging.Level;\n" +
                              "import java.util.logging.Logger;\n" +
                              "public class Test extends Logger {\n" +
                              "    private void t(Logger l, int a, int b, int c) {\n" +
                              "        log(Level.SEVERE, \"A=/{0}/\", a);\n" +
                              "    }\n" +
                              "}\n");
    }
}
