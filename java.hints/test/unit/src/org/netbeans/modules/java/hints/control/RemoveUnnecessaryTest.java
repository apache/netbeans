/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2011 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development and
 * Distribution License("CDDL") (collectively, the "License"). You may not use
 * this file except in compliance with the License. You can obtain a copy of
 * the License at http://www.netbeans.org/cddl-gplv2.html or
 * nbbuild/licenses/CDDL-GPL-2-CP. See the License for the specific language
 * governing permissions and limitations under the License. When distributing
 * the software, include this License Header Notice in each file and include
 * the License file at nbbuild/licenses/CDDL-GPL-2-CP. Oracle designates this
 * particular file as subject to the "Classpath" exception as provided by
 * Oracle in the GPL Version 2 section of the License file that accompanied
 * this code. If applicable, add the following below the License Header, with
 * the fields enclosed by brackets [] replaced by your own identifying
 * information: "Portions Copyrighted [year] [name of copyright owner]"
 *
 * If you wish your version of this file to be governed by only the CDDL or
 * only the GPL Version 2, indicate your decision by adding "[Contributor]
 * elects to include this software in this distribution under the [CDDL or GPL
 * Version 2] license." If you do not indicate a single choice of license, a
 * recipient has the option to distribute your version of this file under
 * either the CDDL, the GPL Version 2 or to extend the choice of license to its
 * licensees as provided above. However, if you add GPL Version 2 code and
 * therefore, elected the GPL Version 2 license, then the option applies only
 * if the new code is made subject to such option by the copyright holder.
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2011 Sun Microsystems, Inc.
 */
package org.netbeans.modules.java.hints.control;

import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.java.hints.test.api.HintTest;

/**
 *
 * @author lahvac
 */
public class RemoveUnnecessaryTest extends NbTestCase {

    public RemoveUnnecessaryTest(String name) {
        super(name);
    }

    public void testReturnSimple() throws Exception {
        HintTest
                .create()
                .input("package test;\n" +
                       "public class Test {\n" +
                       "    public void testReturn() {\n" +
                       "        return ;\n" +
                       "    }\n" +
                       "}\n")
                .run(RemoveUnnecessary.class)
                .findWarning("3:8-3:16:verifier:" + Bundle.ERR_UnnecessaryReturnStatement())
                .applyFix(Bundle.FIX_UnnecessaryReturnStatement())
                .assertCompilable()
                .assertOutput("package test;\n" +
                              "public class Test {\n" +
                              "    public void testReturn() {\n" +
                              "    }\n" +
                              "}\n");
    }

    public void testReturnIfNoBlock() throws Exception {
        HintTest
                .create()
                .input("package test;\n" +
                       "public class Test {\n" +
                       "    public void testReturn(boolean b) {\n" +
                       "        if (b) return ;\n" +
                       "    }\n" +
                       "}\n")
                .run(RemoveUnnecessary.class)
                .findWarning("3:15-3:23:verifier:" + Bundle.ERR_UnnecessaryReturnStatement())
                .applyFix(Bundle.FIX_UnnecessaryReturnStatement())
                .assertCompilable()
                .assertOutput("package test;\n" +
                              "public class Test {\n" +
                              "    public void testReturn(boolean b) {\n" +
                              "        if (b) { }\n" +
                              "    }\n" +
                              "}\n");
    }

    public void testReturnNeg1() throws Exception {
        HintTest
                .create()
                .input("package test;\n" +
                       "public class Test {\n" +
                       "    public void testReturn(boolean b) {\n" +
                       "        if (b) { return ; }\n" +
                       "        System.err.println();\n" +
                       "    }\n" +
                       "}\n")
                .run(RemoveUnnecessary.class)
                .assertWarnings();
    }

    public void testReturnNeg2() throws Exception {
        HintTest
                .create()
                .input("package test;\n" +
                       "public class Test {\n" +
                       "    public void testReturn(int b) {\n" +
                       "        switch (b) {\n" +
                       "            case 0: if (b == 0) { return ; }\n" +
                       "                    System.err.println();\n" +
                       "                    break;\n" +
                       "        }\n" +
                       "    }\n" +
                       "}\n")
                .run(RemoveUnnecessary.class)
                .assertWarnings();
    }

    public void testReturnNeg3() throws Exception {
        HintTest
                .create()
                .input("package test;\n" +
                       "public class Test {\n" +
                       "    public void testReturn(int b) {\n" +
                       "        switch (b) {\n" +
                       "            case 0: if (b == 0) { return ; }\n" +
                       "            case 1: System.err.println(); break;\n" +
                       "        }\n" +
                       "    }\n" +
                       "}\n")
                .run(RemoveUnnecessary.class)
                .assertWarnings();
    }

    public void testReturnNeg4() throws Exception {
        HintTest
                .create()
                .input("package test;\n" +
                       "public class Test {\n" +
                       "    public int test(int b) {\n" +
                       "        switch (b) {\n" +
                       "            case 0: if (b == 0) { return 1; }\n" +
                       "            case 1: System.err.println(); break;\n" +
                       "        }\n" +
                       "        return 0;\n" +
                       "    }\n" +
                       "}\n")
                .run(RemoveUnnecessary.class)
                .assertWarnings();
    }

    public void testReturnNegCase() throws Exception {
        HintTest
                .create()
                .input("package test;\n" +
                       "public class Test {\n" +
                       "    public void testReturn(int b) {\n" +
                       "        switch (b) {\n" +
                       "            case 0: { return ; }\n" +
                       "            case 1: { System.err.println(1); break; }\n" +
                       "        }\n" +
                       "    }\n" +
                       "}\n")
                .run(RemoveUnnecessary.class)
                .assertWarnings();
    }

    public void testReturnSwitchRemove1() throws Exception {
        HintTest
                .create()
                .input("package test;\n" +
                       "public class Test {\n" +
                       "    public void testReturn(int b) {\n" +
                       "        switch (b) {\n" +
                       "            case 0: if (b == 0) { return ; } break;\n" +
                       "            case 1: System.err.println(); break;\n" +
                       "        }\n" +
                       "    }\n" +
                       "}\n")
                .run(RemoveUnnecessary.class)
                .assertWarnings("4:34-4:42:verifier:" + Bundle.ERR_UnnecessaryReturnStatement());
    }

    public void testReturnSwitchRemove2() throws Exception {
        HintTest
                .create()
                .input("package test;\n" +
                       "public class Test {\n" +
                       "    public void testReturn(int b) {\n" +
                       "        switch (b) {\n" +
                       "            case 0: { if (b == 0) { return ; } break; };\n" +
                       "            case 1: System.err.println(); break;\n" +
                       "        }\n" +
                       "    }\n" +
                       "}\n", false)
                .run(RemoveUnnecessary.class)
                .assertWarnings("4:36-4:44:verifier:" + Bundle.ERR_UnnecessaryReturnStatement());
    }

    public void testReturnSwitchRemove3() throws Exception {
        HintTest
                .create()
                .input("package test;\n" +
                       "public class Test {\n" +
                       "    public void testReturn(int b) {\n" +
                       "        switch (b) {\n" +
                       "            case 0: { if (b == 0) { return ; } };\n" +
                       "            case 1: break;\n" +
                       "        }\n" +
                       "    }\n" +
                       "}\n")
                .run(RemoveUnnecessary.class)
                .assertWarnings("4:36-4:44:verifier:" + Bundle.ERR_UnnecessaryReturnStatement());
    }

    public void testReturnSwitchRemove4() throws Exception {
        HintTest
                .create()
                .input("package test;\n" +
                       "public class Test {\n" +
                       "    public void testReturn(int b) {\n" +
                       "        switch (b) {\n" +
                       "            case 0: { if (b == 0) { return ; } };\n" +
                       "            case 1: { ; break; }\n" +
                       "        }\n" +
                       "    }\n" +
                       "}\n")
                .run(RemoveUnnecessary.class)
                .assertWarnings("4:36-4:44:verifier:" + Bundle.ERR_UnnecessaryReturnStatement());
    }

    public void testReturnLastCase() throws Exception {
        HintTest
                .create()
                .input("package test;\n" +
                       "public class Test {\n" +
                       "    public void testReturn(int b) {\n" +
                       "        switch (b) {\n" +
                       "            case 1: if (b == 0) { return ; }\n" +
                       "        }\n" +
                       "    }\n" +
                       "}\n")
                .run(RemoveUnnecessary.class)
                .assertWarnings("4:34-4:42:verifier:" + Bundle.ERR_UnnecessaryReturnStatement());
    }

    public void testReturnNPE200462a() throws Exception {
        HintTest
                .create()
                .input("package test;\n" +
                       "public class Test {\n" +
                       "    public Test(int b) {\n" +
                       "        switch (b) {\n" +
                       "            case 1: if (b == 0) { return ; }\n" +
                       "        }\n" +
                       "    }\n" +
                       "}\n")
                .run(RemoveUnnecessary.class)
                .assertWarnings("4:34-4:42:verifier:" + Bundle.ERR_UnnecessaryReturnStatement());
    }

    public void testReturnNPE200462b() throws Exception {
        HintTest
                .create()
                .input("package test;\n" +
                       "public class Test {\n" +
                       "    public Test(int b) {\n" +
                       "        switch (b) {\n" +
                       "            case 1: if (b == 0) { return ; }\n" +
                       "        }\n" +
                       "    }\n" +
                       "}\n")
                .run(RemoveUnnecessary.class)
                .assertWarnings("4:34-4:42:verifier:" + Bundle.ERR_UnnecessaryReturnStatement());
    }

    public void testReturnInLoop201393() throws Exception {
        HintTest
                .create()
                .input("package test;\n" +
                       "public class Test {\n" +
                       "    public Test(int i) {\n" +
                       "        while (i-- > 0) {\n" +
                       "            System.err.println(1);\n" +
                       "            if (i == 3) return ;\n" +
                       "        }\n" +
                       "    }\n" +
                       "}\n")
                .run(RemoveUnnecessary.class)
                .assertWarnings();
    }

    public void testReturnNegFinally203576() throws Exception {
        HintTest
                .create()
                .input("package test;\n" +
                       "public class Test {\n" +
                       "    public static void main(String[] args) {\n" +
                       "        try {\n" +
                       "            throw new NullPointerException(\"NullPointerException 1\");\n" +
                       "        } catch (NullPointerException e) {\n" +
                       "            throw new NullPointerException(\"NullPointerException 2\");\n" +
                       "        } finally {\n" +
                       "            System.out.println(\"Do I ever get printed?\");\n" +
                       "            return;\n" +
                       "        }\n" +
                       "    }\n" +
                       "}\n")
                .run(RemoveUnnecessary.class)
                .assertWarnings();
    }

    public void TODOtestPosFinally203576() throws Exception {
        HintTest
                .create()
                .input("package test;\n" +
                       "public class Test {\n" +
                       "    public static void main(String[] args) {\n" +
                       "        try {\n" +
                       "            throw new NullPointerException(\"NullPointerException 1\");\n" +
                       "        } catch (NullPointerException e) {\n" +
                       "            throw new NullPointerException(\"NullPointerException 2\");\n" +
                       "        } finally {\n" +
                       "            if (args.length == 0) { return ; }\n" +
                       "            return;\n" +
                       "        }\n" +
                       "    }\n" +
                       "}\n")
                .run(RemoveUnnecessary.class)
                .assertWarnings("<missing>");
    }

    public void testReturnIgnoreExtraBreak222422() throws Exception {
        HintTest
                .create()
                .input("package test;\n" +
                       "public class Test {\n" +
                       "    public void testReturn(int b) {\n" +
                       "        if (true) return ;\n" +
                       "        break;\n" +
                       "    }\n" +
                       "}\n",
                       false)
                .run(RemoveUnnecessary.class)
                .assertWarnings();
    }
    
    public void testLambda227899a() throws Exception {
        HintTest.create()
                .sourceLevel("1.8")
                .input("package test;\n" +
                       "import java.util.Comparator;\n" +
                       "public class Test {\n" +
                       "    public void testReturn(int b) {\n" +
                       "        Comparator<String> c = (String s1, String s2) -> {\n" +
                       "               return s1.compareToIgnoreCase(s2);\n" +
                       "            };\n" +
                       "    }\n" +
                       "}\n",
                       false)
                .run(RemoveUnnecessary.class)
                .assertWarnings();
    }
    
    public void testLambda227899b() throws Exception {
        HintTest.create()
                .sourceLevel("1.8")
                .input("package test;\n" +
                       "public class Test {\n" +
                       "    public void testReturn(int b) {\n" +
                       "        Runnable r = () -> {\n" +
                       "               return ;\n" +
                       "            };\n" +
                       "    }\n" +
                       "}\n",
                       false)
                .run(RemoveUnnecessary.class)
                .assertWarnings("4:15-4:23:verifier:" + Bundle.ERR_UnnecessaryReturnStatement());
    }

    public void testContinueSimple() throws Exception {
        HintTest
                .create()
                .input("package test;\n" +
                       "public class Test {\n" +
                       "    public void testContinue(int i) {\n" +
                       "        while (i-- > 0) {\n" +
                       "            continue;\n" +
                       "        }\n" +
                       "    }\n" +
                       "}\n")
                .run(RemoveUnnecessary.class)
                .findWarning("4:12-4:21:verifier:" + Bundle.ERR_UnnecessaryContinueStatement())
                .applyFix(Bundle.FIX_UnnecessaryContinueStatement())
                .assertCompilable()
                .assertOutput("package test;\n" +
                              "public class Test {\n" +
                              "    public void testContinue(int i) {\n" +
                              "        while (i-- > 0) {\n" +
                              "        }\n" +
                              "    }\n" +
                              "}\n");
    }
    
    public void testContinueToOutter() throws Exception {
        HintTest.create()
                .input("package test;\n" +
                       "public class Test {\n" +
                       "    public void testContinue(int i, int j) {\n" +
                       "        OUTER: while (i-- > 0) {\n" +
                       "            while (j-- > 0) {\n" +
                       "                continue OUTER;\n" +
                       "            }\n" +
                       "        }\n" +
                       "    }\n" +
                       "}\n")
                .run(RemoveUnnecessary.class)
                .assertWarnings();
    }
    
    public void testContinueWithLabel() throws Exception {
        HintTest.create()
                .input("package test;\n" +
                       "public class Test {\n" +
                       "    public void testContinue(int i) {\n" +
                       "        OUTER: while (i-- > 0) {\n" +
                       "            continue OUTER;\n" +
                       "        }\n" +
                       "    }\n" +
                       "}\n")
                .run(RemoveUnnecessary.class)
                .findWarning("4:12-4:20:verifier:" + Bundle.ERR_UnnecessaryContinueStatementLabel())
                .applyFix(Bundle.FIX_UnnecessaryContinueStatementLabel())
                .assertCompilable()
                .assertOutput("package test;\n" +
                              "public class Test {\n" +
                              "    public void testContinue(int i) {\n" +
                              "        OUTER: while (i-- > 0) {\n" +
                              "            continue;\n" +
                              "        }\n" +
                              "    }\n" +
                              "}\n");
    }
    
    public void testBreakWithLabel() throws Exception {
        HintTest.create()
                .input("package test;\n" +
                       "public class Test {\n" +
                       "    public void testContinue(int i) {\n" +
                       "        OUTER: while (i-- > 0) {\n" +
                       "            break OUTER;\n" +
                       "        }\n" +
                       "    }\n" +
                       "}\n")
                .run(RemoveUnnecessary.class)
                .findWarning("4:12-4:17:verifier:" + Bundle.ERR_UnnecessaryBreakStatementLabel())
                .applyFix(Bundle.FIX_UnnecessaryBreakStatementLabel())
                .assertCompilable()
                .assertOutput("package test;\n" +
                              "public class Test {\n" +
                              "    public void testContinue(int i) {\n" +
                              "        OUTER: while (i-- > 0) {\n" +
                              "            break;\n" +
                              "        }\n" +
                              "    }\n" +
                              "}\n");
    }
    
    public void testBreakWithLabelInSwitch() throws Exception {
        HintTest.create()
                .input("package test;\n" +
                       "public class Test {\n" +
                       "    public void testContinue(int i) {\n" +
                       "        OUTER: while (i-- > 0) {\n" +
                       "            switch (i) {\n" +
                       "                case 0: break OUTER;\n" +
                       "            }\n" +
                       "        }\n" +
                       "    }\n" +
                       "}\n")
                .run(RemoveUnnecessary.class)
                .assertWarnings();
    }
}
