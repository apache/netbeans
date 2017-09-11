/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2012 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2012 Sun Microsystems, Inc.
 */
package org.netbeans.modules.java.hints.suggestions;

import org.junit.Test;
import org.netbeans.modules.java.hints.test.api.HintTest;

public class IfsTest {

    @Test
    public void testComments() throws Exception {
        HintTest.create()
                .setCaretMarker('|')
                .input("package test;\n" +
                       "public class Test {\n" +
                       "    public static void main(String[] args) {\n" +
                       "        i|f\n" +
                       "        // A\n" +
                       "        (args[0].isEmpty())\n" +
                       "        // B\n" +
                       "        { //1\n" +
                       "            //2\n" +
                       "            System.err.println(\"1\");\n" +
                       "            //3\n" +
                       "        } \n" +
                       "        // C\n" +
                       "        else\n" +
                       "        // D\n" +
                       "        {//4\n" +
                       "            //5\n" +
                       "            System.err.println(\"2\");\n" +
                       "            //6\n" +
                       "        } //E\n" +
                       "    }\n" +
                       "}\n")
                .run(Ifs.class)
                .findWarning("3:9-3:9:verifier:" + Bundle.ERR_InvertIf())
                .applyFix()
                .assertCompilable()
                .assertOutput("package test;\n" +
                              "public class Test {\n" +
                              "    public static void main(String[] args) {\n" +
                              "        if\n" +
                              "        // A\n" +
                              "        (!args[0].isEmpty())\n" +
                              "        // D\n" +
                              "        {//4\n" +
                              "            //5\n" +
                              "            System.err.println(\"2\");\n" +
                              "            //6\n" +
                              "        } //E\n" +
                              "        // C\n" +
                              "        else\n" +
                              "        // B\n" +
                              "        { //1\n" +
                              "            //2\n" +
                              "            System.err.println(\"1\");\n" +
                              "            //3\n" +
                              "        }\n" +
                              "    }\n" +
                              "}\n");
    }
    
    @Test
    public void testCaretPosition() throws Exception {
        HintTest.create()
                .setCaretMarker('|')
                .input("package test;\n" +
                       "public class Test {\n" +
                       "    public static void main(String[] args) {\n" +
                       "        if (args[0].isEmpty()) |{\n" +
                       "            System.err.println(\"1\");\n" +
                       "        } else {\n" +
                       "            System.err.println(\"2\");\n" +
                       "        }\n" +
                       "    }\n" +
                       "}\n")
                .run(Ifs.class)
                .assertWarnings();
    }
    
    @Test
    public void testOptimizeNeg() throws Exception {
        HintTest.create()
                .setCaretMarker('|')
                .input("package test;\n" +
                       "public class Test {\n" +
                       "    public static void main(String[] args) {\n" +
                       "        i|f (!args[0].isEmpty()) {\n" +
                       "            System.err.println(\"1\");\n" +
                       "        } else {\n" +
                       "            System.err.println(\"2\");\n" +
                       "        }\n" +
                       "    }\n" +
                       "}\n")
                .run(Ifs.class)
                .findWarning("3:9-3:9:verifier:" + Bundle.ERR_InvertIf())
                .applyFix()
                .assertCompilable()
                .assertOutput("package test;\n" +
                              "public class Test {\n" +
                              "    public static void main(String[] args) {\n" +
                              "        if (args[0].isEmpty()) {\n" +
                              "            System.err.println(\"2\");\n" +
                              "        } else {\n" +
                              "            System.err.println(\"1\");\n" +
                              "        }\n" +
                              "    }\n" +
                              "}\n");
    }
    
    @Test
    public void testOptimizeNegParenthesized() throws Exception {
        HintTest.create()
                .setCaretMarker('|')
                .input("package test;\n" +
                       "public class Test {\n" +
                       "    public static void main(String[] args) {\n" +
                       "        i|f (!(args.length == 0)) {\n" +
                       "            System.err.println(\"1\");\n" +
                       "        } else {\n" +
                       "            System.err.println(\"2\");\n" +
                       "        }\n" +
                       "    }\n" +
                       "}\n")
                .run(Ifs.class)
                .findWarning("3:9-3:9:verifier:" + Bundle.ERR_InvertIf())
                .applyFix()
                .assertCompilable()
                .assertOutput("package test;\n" +
                              "public class Test {\n" +
                              "    public static void main(String[] args) {\n" +
                              "        if (args.length == 0) {\n" +
                              "            System.err.println(\"2\");\n" +
                              "        } else {\n" +
                              "            System.err.println(\"1\");\n" +
                              "        }\n" +
                              "    }\n" +
                              "}\n");
    }
    
    @Test
    public void testOptimizeEquals() throws Exception {
        HintTest.create()
                .setCaretMarker('|')
                .input("package test;\n" +
                       "public class Test {\n" +
                       "    public static void main(String[] args) {\n" +
                       "        i|f (args.length == 0) {\n" +
                       "            System.err.println(\"1\");\n" +
                       "        } else {\n" +
                       "            System.err.println(\"2\");\n" +
                       "        }\n" +
                       "    }\n" +
                       "}\n")
                .run(Ifs.class)
                .findWarning("3:9-3:9:verifier:" + Bundle.ERR_InvertIf())
                .applyFix()
                .assertCompilable()
                .assertOutput("package test;\n" +
                              "public class Test {\n" +
                              "    public static void main(String[] args) {\n" +
                              "        if (args.length != 0) {\n" +
                              "            System.err.println(\"2\");\n" +
                              "        } else {\n" +
                              "            System.err.println(\"1\");\n" +
                              "        }\n" +
                              "    }\n" +
                              "}\n");
    }
    
    @Test
    public void testOptimizeNotEquals() throws Exception {
        HintTest.create()
                .setCaretMarker('|')
                .input("package test;\n" +
                       "public class Test {\n" +
                       "    public static void main(String[] args) {\n" +
                       "        i|f (args.length != 0) {\n" +
                       "            System.err.println(\"1\");\n" +
                       "        } else {\n" +
                       "            System.err.println(\"2\");\n" +
                       "        }\n" +
                       "    }\n" +
                       "}\n")
                .run(Ifs.class)
                .findWarning("3:9-3:9:verifier:" + Bundle.ERR_InvertIf())
                .applyFix()
                .assertCompilable()
                .assertOutput("package test;\n" +
                              "public class Test {\n" +
                              "    public static void main(String[] args) {\n" +
                              "        if (args.length == 0) {\n" +
                              "            System.err.println(\"2\");\n" +
                              "        } else {\n" +
                              "            System.err.println(\"1\");\n" +
                              "        }\n" +
                              "    }\n" +
                              "}\n");
    }
    
    @Test
    public void testTrue() throws Exception {
        HintTest.create()
                .setCaretMarker('|')
                .input("package test;\n" +
                       "public class Test {\n" +
                       "    public static void main(String[] args) {\n" +
                       "        i|f (true) {\n" +
                       "            System.err.println(\"1\");\n" +
                       "        } else {\n" +
                       "            System.err.println(\"2\");\n" +
                       "        }\n" +
                       "    }\n" +
                       "}\n")
                .run(Ifs.class)
                .findWarning("3:9-3:9:verifier:" + Bundle.ERR_InvertIf())
                .applyFix()
                .assertCompilable()
                .assertOutput("package test;\n" +
                              "public class Test {\n" +
                              "    public static void main(String[] args) {\n" +
                              "        if (false) {\n" +
                              "            System.err.println(\"2\");\n" +
                              "        } else {\n" +
                              "            System.err.println(\"1\");\n" +
                              "        }\n" +
                              "    }\n" +
                              "}\n");
    }
    @Test
    public void testFalse() throws Exception {
        HintTest.create()
                .setCaretMarker('|')
                .input("package test;\n" +
                       "public class Test {\n" +
                       "    public static void main(String[] args) {\n" +
                       "        i|f (false) {\n" +
                       "            System.err.println(\"1\");\n" +
                       "        } else {\n" +
                       "            System.err.println(\"2\");\n" +
                       "        }\n" +
                       "    }\n" +
                       "}\n")
                .run(Ifs.class)
                .findWarning("3:9-3:9:verifier:" + Bundle.ERR_InvertIf())
                .applyFix()
                .assertCompilable()
                .assertOutput("package test;\n" +
                              "public class Test {\n" +
                              "    public static void main(String[] args) {\n" +
                              "        if (true) {\n" +
                              "            System.err.println(\"2\");\n" +
                              "        } else {\n" +
                              "            System.err.println(\"1\");\n" +
                              "        }\n" +
                              "    }\n" +
                              "}\n");
    }

    @Test
    public void testDeMorganAnd() throws Exception {
        HintTest.create()
                .setCaretMarker('|')
                .input("package test;\n" +
                       "public class Test {\n" +
                       "    public static void main(String[] args) {\n" +
                       "        i|f (args.length != 0 && true) {\n" +
                       "            System.err.println(\"1\");\n" +
                       "        } else {\n" +
                       "            System.err.println(\"2\");\n" +
                       "        }\n" +
                       "    }\n" +
                       "}\n")
                .run(Ifs.class)
                .findWarning("3:9-3:9:verifier:" + Bundle.ERR_InvertIf())
                .applyFix()
                .assertCompilable()
                .assertOutput("package test;\n" +
                              "public class Test {\n" +
                              "    public static void main(String[] args) {\n" +
                              "        if (args.length == 0 || false) {\n" +
                              "            System.err.println(\"2\");\n" +
                              "        } else {\n" +
                              "            System.err.println(\"1\");\n" +
                              "        }\n" +
                              "    }\n" +
                              "}\n");
    }
    
    @Test
    public void testDeMorganOr() throws Exception {
        HintTest.create()
                .setCaretMarker('^')
                .input("package test;\n" +
                       "public class Test {\n" +
                       "    public static void main(String[] args) {\n" +
                       "        i^f (args.length != 0 || false) {\n" +
                       "            System.err.println(\"1\");\n" +
                       "        } else {\n" +
                       "            System.err.println(\"2\");\n" +
                       "        }\n" +
                       "    }\n" +
                       "}\n")
                .run(Ifs.class)
                .findWarning("3:9-3:9:verifier:" + Bundle.ERR_InvertIf())
                .applyFix()
                .assertCompilable()
                .assertOutput("package test;\n" +
                              "public class Test {\n" +
                              "    public static void main(String[] args) {\n" +
                              "        if (args.length == 0 && true) {\n" +
                              "            System.err.println(\"2\");\n" +
                              "        } else {\n" +
                              "            System.err.println(\"1\");\n" +
                              "        }\n" +
                              "    }\n" +
                              "}\n");
    }
    
    @Test
    public void testLess() throws Exception {
        HintTest.create()
                .setCaretMarker('^')
                .input("package test;\n" +
                       "public class Test {\n" +
                       "    public static void main(String[] args) {\n" +
                       "        i^f (args.length < 5) {\n" +
                       "            System.err.println(\"too few\");\n" +
                       "        } else {\n" +
                       "            System.err.println(\"too many\");\n" +
                       "        }\n" +
                       "    }\n" +
                       "}\n")
                .run(Ifs.class)
                .findWarning("3:9-3:9:verifier:" + Bundle.ERR_InvertIf())
                .applyFix()
                .assertCompilable()
                .assertOutput("package test;\n" +
                              "public class Test {\n" +
                              "    public static void main(String[] args) {\n" +
                              "        if (args.length >= 5) {\n" +
                              "            System.err.println(\"too many\");\n" +
                              "        } else {\n" +
                              "            System.err.println(\"too few\");\n" +
                              "        }\n" +
                              "    }\n" +
                              "}\n");
    }
    
    @Test
    public void testLessEq() throws Exception {
        HintTest.create()
                .setCaretMarker('^')
                .input("package test;\n" +
                       "public class Test {\n" +
                       "    public static void main(String[] args) {\n" +
                       "        i^f (args.length <= 5) {\n" +
                       "            System.err.println(\"too few\");\n" +
                       "        } else {\n" +
                       "            System.err.println(\"too many\");\n" +
                       "        }\n" +
                       "    }\n" +
                       "}\n")
                .run(Ifs.class)
                .findWarning("3:9-3:9:verifier:" + Bundle.ERR_InvertIf())
                .applyFix()
                .assertCompilable()
                .assertOutput("package test;\n" +
                              "public class Test {\n" +
                              "    public static void main(String[] args) {\n" +
                              "        if (args.length > 5) {\n" +
                              "            System.err.println(\"too many\");\n" +
                              "        } else {\n" +
                              "            System.err.println(\"too few\");\n" +
                              "        }\n" +
                              "    }\n" +
                              "}\n");
    }
    
    @Test
    public void testGreater() throws Exception {
        HintTest.create()
                .setCaretMarker('^')
                .input("package test;\n" +
                       "public class Test {\n" +
                       "    public static void main(String[] args) {\n" +
                       "        i^f (args.length > 5) {\n" +
                       "            System.err.println(\"too many\");\n" +
                       "        } else {\n" +
                       "            System.err.println(\"too few\");\n" +
                       "        }\n" +
                       "    }\n" +
                       "}\n")
                .run(Ifs.class)
                .findWarning("3:9-3:9:verifier:" + Bundle.ERR_InvertIf())
                .applyFix()
                .assertCompilable()
                .assertOutput("package test;\n" +
                              "public class Test {\n" +
                              "    public static void main(String[] args) {\n" +
                              "        if (args.length <= 5) {\n" +
                              "            System.err.println(\"too few\");\n" +
                              "        } else {\n" +
                              "            System.err.println(\"too many\");\n" +
                              "        }\n" +
                              "    }\n" +
                              "}\n");
    }
    
    @Test
    public void testGreaterEq() throws Exception {
        HintTest.create()
                .setCaretMarker('^')
                .input("package test;\n" +
                       "public class Test {\n" +
                       "    public static void main(String[] args) {\n" +
                       "        i^f (args.length >= 5) {\n" +
                       "            System.err.println(\"too many\");\n" +
                       "        } else {\n" +
                       "            System.err.println(\"too few\");\n" +
                       "        }\n" +
                       "    }\n" +
                       "}\n")
                .run(Ifs.class)
                .findWarning("3:9-3:9:verifier:" + Bundle.ERR_InvertIf())
                .applyFix()
                .assertCompilable()
                .assertOutput("package test;\n" +
                              "public class Test {\n" +
                              "    public static void main(String[] args) {\n" +
                              "        if (args.length < 5) {\n" +
                              "            System.err.println(\"too few\");\n" +
                              "        } else {\n" +
                              "            System.err.println(\"too many\");\n" +
                              "        }\n" +
                              "    }\n" +
                              "}\n");
    }
    
    @Test
    public void testWithoutElse225913() throws Exception {
        HintTest.create()
                .setCaretMarker('^')
                .input("package test;\n" +
                       "public class Test {\n" +
                       "    public static void main(String[] args) {\n" +
                       "        i^f (args.length >= 5) {\n" +
                       "            System.err.println(\"too many\");\n" +
                       "        }\n" +
                       "    }\n" +
                       "}\n")
                .run(Ifs.class)
                .findWarning("3:9-3:9:verifier:" + Bundle.ERR_InvertIf())
                .applyFix()
                .assertCompilable()
                .assertOutput("package test;\n" +
                              "public class Test {\n" +
                              "    public static void main(String[] args) {\n" +
                              "        if (args.length < 5) {\n" +
                              "        } else {\n" +
                              "            System.err.println(\"too many\");\n" +
                              "        }\n" +
                              "    }\n" +
                              "}\n");
    }
    
    @Test
    public void testNegInstanceof228864() throws Exception {
        HintTest.create()
                .setCaretMarker('|')
                .input("package test;\n" +
                       "public class Test {\n" +
                       "    public static void main(Object o) {\n" +
                       "        i|f (o instanceof String) {\n" +
                       "            System.err.println(\"1\");\n" +
                       "        } else {\n" +
                       "            System.err.println(\"2\");\n" +
                       "        }\n" +
                       "    }\n" +
                       "}\n")
                .run(Ifs.class)
                .findWarning("3:9-3:9:verifier:" + Bundle.ERR_InvertIf())
                .applyFix()
                .assertCompilable()
                .assertOutput("package test;\n" +
                              "public class Test {\n" +
                              "    public static void main(Object o) {\n" +
                              "        if (!(o instanceof String)) {\n" +
                              "            System.err.println(\"2\");\n" +
                              "        } else {\n" +
                              "            System.err.println(\"1\");\n" +
                              "        }\n" +
                              "    }\n" +
                              "}\n");
    }
    
    @Test
    public void testJoinIfs1() throws Exception {
        HintTest.create()
                .setCaretMarker('^')
                .input("package test;\n" +
                       "public class Test {\n" +
                       "    public static void main(int i) {\n" +
                       "        if (i == 0) {\n" +
                       "            System.err.println(\"0\");\n" +
                       "        } el^se {\n" +
                       "            if (i == 1) {\n" +
                       "                System.err.println(\"1\");\n" +
                       "            }\n" +
                       "        }\n" +
                       "    }\n" +
                       "}\n")
                .run(Ifs.class)
                .findWarning("5:12-5:12:verifier:" + Bundle.ERR_JoinElseIf())
                .applyFix()
                .assertCompilable()
                .assertOutput("package test;\n" +
                              "public class Test {\n" +
                              "    public static void main(int i) {\n" +
                              "        if (i == 0) {\n" +
                              "            System.err.println(\"0\");\n" +
                              "        } else if (i == 1) {\n" +
                              "            System.err.println(\"1\");\n" +
                              "        }\n" +
                              "    }\n" +
                              "}\n");
    }
    
    @Test
    public void testJoinIfs2() throws Exception {
        HintTest.create()
                .setCaretMarker('^')
                .input("package test;\n" +
                       "public class Test {\n" +
                       "    public static void main(int i) {\n" +
                       "        if (i == 0) {\n" +
                       "            System.err.println(\"0\");\n" +
                       "        } el^se {\n" +
                       "            if (i == 1) {\n" +
                       "                System.err.println(\"1\");\n" +
                       "            }\n" +
                       "            System.err.println(\"extra\");\n" +
                       "        }\n" +
                       "    }\n" +
                       "}\n")
                .run(Ifs.class)
                .assertWarnings();
    }

    @Test
    public void testJoinIfs3() throws Exception {
        HintTest.create()
                .setCaretMarker('^')
                .input("package test;\n" +
                       "public class Test {\n" +
                       "    public static void main(int i) {\n" +
                       "        if (i == 0) {\n" +
                       "            System.err.println(\"0\");\n" +
                       "        } else {^\n" +
                       "            if (i == 1) {\n" +
                       "                System.err.println(\"1\");\n" +
                       "            }\n" +
                       "        }\n" +
                       "    }\n" +
                       "}\n")
                .run(Ifs.class)
                .assertWarnings();
    }
    
    @Test
    public void testToIfOr1() throws Exception {
        HintTest.create()
                .setCaretMarker('^')
                .input("package test;\n" +
                       "public class Test {\n" +
                       "    public static void main(int i) {\n" +
                       "        if (i == 0) {\n" +
                       "            System.err.println(\"0\");\n" +
                       "        } el^se {\n" +
                       "            if (i == 1) {\n" +
                       "                System.err.println(\"0\");\n" +
                       "            }\n" +
                       "        }\n" +
                       "    }\n" +
                       "}\n")
                .run(Ifs.class)
                .findWarning("5:12-5:12:verifier:" + Bundle.ERR_ToOrIf())
                .applyFix()
                .assertCompilable()
                .assertVerbatimOutput("package test;\n" +
                                      "public class Test {\n" +
                                      "    public static void main(int i) {\n" +
                                      "        if (i == 0 || i == 1) {\n" +
                                      "            System.err.println(\"0\");\n" +
                                      "        }\n" +
                                      "    }\n" +
                                      "}\n");
    }
    
    @Test
    public void testToIfOr2() throws Exception {
        HintTest.create()
                .setCaretMarker('^')
                .input("package test;\n" +
                       "public class Test {\n" +
                       "    public static void main(int i) {\n" +
                       "        i^f (i == 0) {\n" +
                       "            System.err.println(\"0\");\n" +
                       "        } else if (i == 1) {\n" +
                       "            System.err.println(\"0\");\n" +
                       "        } else {\n" +
                       "            System.err.println(\"1\");\n" +
                       "        }\n" +
                       "    }\n" +
                       "}\n")
                .run(Ifs.class)
                .findWarning("3:9-3:9:verifier:" + Bundle.ERR_ToOrIf())
                .applyFix()
                .assertCompilable()
                .assertVerbatimOutput("package test;\n" +
                                      "public class Test {\n" +
                                      "    public static void main(int i) {\n" +
                                      "        if (i == 0 || i == 1) {\n" +
                                      "            System.err.println(\"0\");\n" +
                                      "        } else {\n" +
                                      "            System.err.println(\"1\");\n" +
                                      "        }\n" +
                                      "    }\n" +
                                      "}\n");
    }
    
    @Test
    public void testSplitOrIf1() throws Exception {
        HintTest.create()
                .setCaretMarker('^')
                .input("package test;\n" +
                       "public class Test {\n" +
                       "    public static void main(int i) {\n" +
                       "        if (i == 0 |^| i == 1) {\n" +
                       "            System.err.println(\"0\");\n" +
                       "        }\n" +
                       "    }\n" +
                       "}\n")
                .run(Ifs.class)
                .findWarning("3:20-3:20:verifier:" + Bundle.ERR_splitIfCondition())
                .applyFix()
                .assertCompilable()
                .assertVerbatimOutput("package test;\n" +
                                      "public class Test {\n" +
                                      "    public static void main(int i) {\n" +
                                      "        if (i == 0) {\n" +
                                      "            System.err.println(\"0\");\n" +
                                      "        } else if (i == 1) {\n" +
                                      "            System.err.println(\"0\");\n" +
                                      "        }\n" +
                                      "    }\n" +
                                      "}\n");
    }
    
    @Test
    public void testSplitOrIf2() throws Exception {
        HintTest.create()
                .setCaretMarker('^')
                .input("package test;\n" +
                       "public class Test {\n" +
                       "    public static void main(int i) {\n" +
                       "        if (i == 0 |^| i == 1) {\n" +
                       "            System.err.println(\"0\");\n" +
                       "        } else {\n" +
                       "            System.err.println(\"1\");\n" +
                       "        }\n" +
                       "    }\n" +
                       "}\n")
                .run(Ifs.class)
                .findWarning("3:20-3:20:verifier:" + Bundle.ERR_splitIfCondition())
                .applyFix()
                .assertCompilable()
                .assertVerbatimOutput("package test;\n" +
                                      "public class Test {\n" +
                                      "    public static void main(int i) {\n" +
                                      "        if (i == 0) {\n" +
                                      "            System.err.println(\"0\");\n" +
                                      "        } else if (i == 1) {\n" +
                                      "            System.err.println(\"0\");\n" +
                                      "        } else {\n" +
                                      "            System.err.println(\"1\");\n" +
                                      "        }\n" +
                                      "    }\n" +
                                      "}\n");
    }
    
    public void testMergeIfs1() throws Exception {
        HintTest.create()
                .setCaretMarker('|')
                .input("package test;\n" +
                       "public class Test {\n" +
                       "    private static void t(int i, int j) {\n" +
                       "        i|f (i == 0) {\n" +
                       "            if (j == 0) {\n" +
                       "                System.err.println(1);\n" +
                       "            }" +
                       "        }\n" +
                       "    }\n" +
                       "}\n")
                .sourceLevel("1.6")
                .run(Tiny.class)
                .findWarning("3:9-3:9:hint:" + Bundle.ERR_org_netbeans_modules_java_hints_suggestions_Tiny_mergeIfs())
                .applyFix()
                .assertCompilable()
                .assertOutput("package test;\n" +
                              "public class Test {\n" +
                              "    private static void t(int i, int j) {\n" +
                              "        if (i == 0 && j == 0) {\n" +
                              "            System.err.println(1);\n" +
                              "        }\n" +
                              "    }\n" +
                              "}\n");
    }
    
    public void testMergeIfs2() throws Exception {
        HintTest.create()
                .setCaretMarker('|')
                .input("package test;\n" +
                       "public class Test {\n" +
                       "    private static void t(int i, int j) {\n" +
                       "        i|f (i == 0 || i == 1) {\n" +
                       "            if (j == 0 || j == 1) {\n" +
                       "                System.err.println(1);\n" +
                       "            }" +
                       "        }\n" +
                       "    }\n" +
                       "}\n")
                .sourceLevel("1.6")
                .run(Tiny.class)
                .findWarning("3:9-3:9:hint:" + Bundle.ERR_org_netbeans_modules_java_hints_suggestions_Tiny_mergeIfs())
                .applyFix()
                .assertCompilable()
                .assertOutput("package test;\n" +
                              "public class Test {\n" +
                              "    private static void t(int i, int j) {\n" +
                              "        if ((i == 0 || i == 1) && (j == 0 || j == 1)) {\n" +
                              "            System.err.println(1);\n" +
                              "        }\n" +
                              "    }\n" +
                              "}\n");
    }
    
    public void testSplitIf1() throws Exception {
        HintTest.create()
                .setCaretMarker('|')
                .input("package test;\n" +
                       "public class Test {\n" +
                       "    private static void t(int i, int j) {\n" +
                       "        if (i =|= 0 && j == 0) {\n" +
                       "            System.err.println(1);\n" +
                       "        }\n" +
                       "    }\n" +
                       "}\n")
                .sourceLevel("1.6")
                .run(Tiny.class)
                .findWarning("3:15-3:15:hint:" + Bundle.ERR_org_netbeans_modules_java_hints_suggestions_Tiny_extractIf())
                .applyFix()
                .assertCompilable()
                .assertOutput("package test;\n" +
                              "public class Test {\n" +
                              "    private static void t(int i, int j) {\n" +
                              "        if (i == 0) {\n" +
                              "            if (j == 0) {\n" +
                              "                System.err.println(1);\n" +
                              "            }\n" +
                              "        }\n" +
                              "    }\n" +
                              "}\n");
    }

    public void testSplitIf2() throws Exception {
        HintTest.create()
                .setCaretMarker('|')
                .input("package test;\n" +
                       "public class Test {\n" +
                       "    private static void t(int i, int j) {\n" +
                       "        if ((i =|= 0 || i == 1) && (j == 0 || j == 1)) {\n" +
                       "            System.err.println(1);\n" +
                       "        }\n" +
                       "    }\n" +
                       "}\n")
                .sourceLevel("1.6")
                .run(Tiny.class)
                .findWarning("3:16-3:16:hint:" + Bundle.ERR_org_netbeans_modules_java_hints_suggestions_Tiny_extractIf())
                .applyFix()
                .assertCompilable()
                .assertOutput("package test;\n" +
                              "public class Test {\n" +
                              "    private static void t(int i, int j) {\n" +
                              "        if (i == 0 || i == 1) {\n" +
                              "            if (j == 0 || j == 1) {\n" +
                              "                System.err.println(1);\n" +
                              "            }\n" +
                              "        }\n" +
                              "    }\n" +
                              "}\n");
    }

    public void testSplitIf3() throws Exception {
        HintTest.create()
                .setCaretMarker('|')
                .input("package test;\n" +
                       "public class Test {\n" +
                       "    private static void t(int i, int j) {\n" +
                       "        if (i == 0 && j =|= 0) {\n" +
                       "            System.err.println(1);\n" +
                       "        }\n" +
                       "    }\n" +
                       "}\n")
                .sourceLevel("1.6")
                .run(Tiny.class)
                .findWarning("3:25-3:25:hint:" + Bundle.ERR_org_netbeans_modules_java_hints_suggestions_Tiny_extractIf())
                .applyFix()
                .assertCompilable()
                .assertOutput("package test;\n" +
                              "public class Test {\n" +
                              "    private static void t(int i, int j) {\n" +
                              "        if (i == 0) {\n" +
                              "            if (j == 0) {\n" +
                              "                System.err.println(1);\n" +
                              "            }\n" +
                              "        }\n" +
                              "    }\n" +
                              "}\n");
    }
}
