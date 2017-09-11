/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2009-2010 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2009-2010 Sun Microsystems, Inc.
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
