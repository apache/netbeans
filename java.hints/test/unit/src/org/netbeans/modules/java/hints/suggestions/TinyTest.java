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
package org.netbeans.modules.java.hints.suggestions;

import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.java.hints.test.api.HintTest;

/**
 *
 * @author lahvac
 */
public class TinyTest extends NbTestCase {

    public TinyTest(String name) {
        super(name);
    }

    public void testSimpleFlip() throws Exception {
        HintTest
                .create()
                .setCaretMarker('|')
                .input("package test;\n" +
                       "import java.util.List;" +
                       "public class Test {\n" +
                       "     private boolean test(List l) {\n" +
                       "         return l.e|quals(this);\n" +
                       "     }\n" +
                       "}\n")
                .run(Tiny.class)
                .findWarning("3:19-3:19:hint:Flip .equals")
                .applyFix("Flip .equals")
                .assertCompilable()
                .assertOutput("package test;\n" +
                              "import java.util.List;" +
                              "public class Test {\n" +
                              "     private boolean test(List l) {\n" +
                              "         return this.equals(l);\n" +
                              "     }\n" +
                              "}\n");
    }

    public void testFlipImplicitThis() throws Exception {
        HintTest
                .create()
                .setCaretMarker('|')
                .input("package test;\n" +
                       "import java.util.List;" +
                       "public class Test {\n" +
                       "     private boolean test(List l) {\n" +
                       "         return e|quals(l);\n" +
                       "     }\n" +
                       "}\n")
                .run(Tiny.class)
                .findWarning("3:17-3:17:hint:Flip .equals")
                .applyFix("Flip .equals")
                .assertCompilable()
                .assertOutput("package test;\n" +
                              "import java.util.List;" +
                              "public class Test {\n" +
                              "     private boolean test(List l) {\n" +
                              "         return l.equals(this);\n" +
                              "     }\n" +
                              "}\n");
    }

    public void testConvertBase1() throws Exception {
        HintTest
                .create()
                .setCaretMarker('|')
                .input("package test;\n" +
                       "public class Test {\n" +
                       "     private final int I = 1|8;\n" +
                       "}\n")
                .run(Tiny.class)
                .findWarning("2:28-2:28:hint:ERR_convertToDifferentBase")
                .applyFix("FIX_convertToDifferentBase_16")
                .assertCompilable()
                .assertOutput("package test;\n" +
                              "public class Test {\n" +
                              "     private final int I = 0x12;\n" +
                              "}\n");
    }

    public void testConvertBase2() throws Exception {
        HintTest
                .create()
                .setCaretMarker('|')
                .input("package test;\n" +
                       "public class Test {\n" +
                       "     private final int I = 1|8;\n" +
                       "}\n")
                .sourceLevel("1.7")
                .run(Tiny.class)
                .findWarning("2:28-2:28:hint:ERR_convertToDifferentBase")
                .applyFix("FIX_convertToDifferentBase_2")
                .assertCompilable()
                .assertOutput("package test;\n" +
                              "public class Test {\n" +
                              "     private final int I = 0b10010;\n" +
                              "}\n");
    }

    public void testConvertBaseLong() throws Exception {
        HintTest
                .create()
                .setCaretMarker('|')
                .input("package test;\n" +
                       "public class Test {\n" +
                       "     private final long I = 42|94967296L;\n" +
                       "}\n")
                .sourceLevel("1.7")
                .run(Tiny.class)
                .findWarning("2:30-2:30:hint:ERR_convertToDifferentBase")
                .applyFix("FIX_convertToDifferentBase_16")
                .assertCompilable()
                .assertOutput("package test;\n" +
                              "public class Test {\n" +
                              "     private final long I = 0x100000000L;\n" +
                              "}\n");
    }

    public void testConvertBaseNegative1() throws Exception {
        HintTest
                .create()
                .setCaretMarker('|')
                .input("package test;\n" +
                       "public class Test {\n" +
                       "     private final long I = -42|94967296L;\n" +
                       "}\n")
                .sourceLevel("1.7")
                .run(Tiny.class)
                .findWarning("2:31-2:31:hint:ERR_convertToDifferentBase")
                .applyFix("FIX_convertToDifferentBase_16")
                .assertCompilable()
                .assertOutput("package test;\n" +
                              "public class Test {\n" +
                              "     private final long I = 0xffffffff00000000L;\n" +
                              "}\n");
    }

    public void testConvertBaseNegative2() throws Exception {
        HintTest
                .create()
                .setCaretMarker('|')
                .input("package test;\n" +
                       "public class Test {\n" +
                       "     private final long I = 0xffffffff|00000000L;\n" +
                       "}\n")
                .sourceLevel("1.7")
                .run(Tiny.class)
                .findWarning("2:38-2:38:hint:ERR_convertToDifferentBase")
                .applyFix("FIX_convertToDifferentBase_10")
                .assertCompilable()
                .assertOutput("package test;\n" +
                              "public class Test {\n" +
                              "     private final long I = -4294967296L;\n" +
                              "}\n");
    }

    public void testConvertBaseNegative203362() throws Exception {
        HintTest
                .create()
                .setCaretMarker('|')
                .input("package test;\n" +
                       "public class Test {\n" +
                       "     private final int I = -|1;\n" +
                       "}\n")
                .sourceLevel("1.7")
                .run(Tiny.class)
                .findWarning("2:28-2:28:hint:ERR_convertToDifferentBase")
                .applyFix("FIX_convertToDifferentBase_16")
                .assertCompilable()
                .assertOutput("package test;\n" +
                              "public class Test {\n" +
                              "     private final int I = 0xffffffff;\n" +
                              "}\n");
    }

    public void testSplitDeclaration1() throws Exception {
        HintTest
                .create()
                .setCaretMarker('|')
                .input("package test;\n" +
                       "public class Test {\n" +
                       "    {\n" +
                       "        int I =| -1;\n" +
                       "    }\n" +
                       "}\n")
                .sourceLevel("1.7")
                .run(Tiny.class)
                .findWarning("3:15-3:15:hint:ERR_splitDeclaration")
                .applyFix("FIX_splitDeclaration")
                .assertCompilable()
                .assertOutput("package test;\n" +
                              "public class Test {\n" +
                              "    {\n" +
                              "        int I;\n" +
                              "        I = -1;\n" +
                              "    }\n" +
                              "}\n");
    }

    public void testSplitDeclaration2() throws Exception {
        HintTest
                .create()
                .setCaretMarker('|')
                .input("package test;\n" +
                       "public class Test {\n" +
                       "    {\n" +
                       "        final int I =| -1;\n" +
                       "    }\n" +
                       "}\n")
                .sourceLevel("1.7")
                .run(Tiny.class)
                .findWarning("3:21-3:21:hint:ERR_splitDeclaration")
                .applyFix("FIX_splitDeclaration")
                .assertCompilable()
                .assertOutput("package test;\n" +
                              "public class Test {\n" +
                              "    {\n" +
                              "        final int I;\n" +
                              "        I = -1;\n" +
                              "    }\n" +
                              "}\n");
    }

    public void testSplitDeclaration3() throws Exception {
        HintTest
                .create()
                .setCaretMarker('|')
                .input("package test;\n" +
                       "public class Test {\n" +
                       "    {\n" +
                       "        System.err.println(1);\n" +
                       "        @SuppressWarnings(\"dummy\") final int I =| -1;\n" +
                       "        System.err.println(2);\n" +
                       "    }\n" +
                       "}\n")
                .sourceLevel("1.7")
                .run(Tiny.class)
                .findWarning("4:48-4:48:hint:ERR_splitDeclaration")
                .applyFix("FIX_splitDeclaration")
                .assertCompilable()
                .assertOutput("package test;\n" +
                              "public class Test {\n" +
                              "    {\n" +
                              "        System.err.println(1);\n" +
                              "        @SuppressWarnings(\"dummy\") final int I;" +
                              "        I = -1;\n" +
                              "        System.err.println(2);\n" +
                              "    }\n" +
                              "}\n");
    }

    public void testFillSwitch1() throws Exception {
        HintTest
                .create()
                .setCaretMarker('|')
                .input("package test;\n" +
                       "public enum Test {\n" +
                       "    A, B, C;\n" +
                       "    private static void t(Test a) {\n" +
                       "        sw|itch (a) {\n" +
                       "            case A: break;\n" +
                       "        }\n" +
                       "    }\n" +
                       "}\n")
                .sourceLevel("1.7")
                .preference(Tiny.KEY_DEFAULT_ENABLED, false)
                .run(Tiny.class)
                .findWarning("4:10-4:10:hint:ERR_Tiny.fillSwitchCases")
                .applyFix("FIX_Tiny.fillSwitchCases")
                .assertCompilable()
                .assertOutput("package test;\n" +
                              "public enum Test {\n" +
                              "    A, B, C;\n" +
                              "    private static void t(Test a) {\n" +
                              "        switch (a) {\n" +
                              "            case A: break;\n" +
                              "            case B: break;\n" +
                              "            case C: break;\n" +
                              "        }\n" +
                              "    }\n" +
                              "}\n");
    }

    public void testFillSwitch2() throws Exception {
        HintTest
                .create()
                .setCaretMarker('|')
                .input("package test;\n" +
                       "public enum Test {\n" +
                       "    A, B, C;\n" +
                       "    private static void t(Object a) {\n" +
                       "        sw|itch ((Test) a) {\n" +
                       "            case A: break;\n" +
                       "        }\n" +
                       "    }\n" +
                       "}\n")
                .sourceLevel("1.7")
                .preference(Tiny.KEY_DEFAULT_ENABLED, false)
                .run(Tiny.class)
                .findWarning("4:10-4:10:hint:ERR_Tiny.fillSwitchCases")
                .applyFix("FIX_Tiny.fillSwitchCases")
                .assertCompilable()
                .assertOutput("package test;\n" +
                              "public enum Test {\n" +
                              "    A, B, C;\n" +
                              "    private static void t(Object a) {\n" +
                              "        switch ((Test) a) {\n" +
                              "            case A: break;\n" +
                              "            case B: break;\n" +
                              "            case C: break;\n" +
                              "        }\n" +
                              "    }\n" +
                              "}\n");
    }

    public void testFillSwitchDefault() throws Exception {
        HintTest
                .create()
                .setCaretMarker('|')
                .input("package test;\n" +
                       "public enum Test {\n" +
                       "    A, B, C;\n" +
                       "    private static void t(Object a) {\n" +
                       "        sw|itch ((Test) a) {\n" +
                       "            case A: break;\n" +
                       "            default: break;\n" +
                       "        }\n" +
                       "    }\n" +
                       "}\n")
                .sourceLevel("1.7")
                .run(Tiny.class)
                .findWarning("4:10-4:10:hint:ERR_Tiny.fillSwitchCases")
                .applyFix("FIX_Tiny.fillSwitchCases")
                .assertCompilable()
                .assertOutput("package test;\n" +
                              "public enum Test {\n" +
                              "    A, B, C;\n" +
                              "    private static void t(Object a) {\n" +
                              "        switch ((Test) a) {\n" +
                              "            case A: break;\n" +
                              "            case B: break;\n" +
                              "            case C: break;\n" +
                              "            default: break;\n" +
                              "        }\n" +
                              "    }\n" +
                              "}\n");
    }

    public void testFillSwitchGenerateDefault() throws Exception {
        HintTest
                .create()
                .setCaretMarker('|')
                .input("package test;\n" +
                       "public enum Test {\n" +
                       "    A, B, C;\n" +
                       "    private static void t(Object a) {\n" +
                       "        sw|itch ((Test) a) {\n" +
                       "            case A: break;\n" +
                       "        }\n" +
                       "    }\n" +
                       "}\n")
                .sourceLevel("1.7")
                .run(Tiny.class)
                .findWarning("4:10-4:10:hint:ERR_Tiny.fillSwitchCasesAndDefault")
                .applyFix("FIX_Tiny.fillSwitchCasesAndDefault")
                .assertCompilable()
                .assertOutput("package test;\n" +
                              "public enum Test {\n" +
                              "    A, B, C;\n" +
                              "    private static void t(Object a) {\n" +
                              "        switch ((Test) a) {\n" +
                              "            case A: break;\n" +
                              "            case B: break;\n" +
                              "            case C: break;\n" +
                              "            default: throw new AssertionError(((Test) a).name());\n" +
                              "        }\n" +
                              "    }\n" +
                              "}\n");
    }

    public void testFillSwitchOnlyGenerateDefault() throws Exception {
        HintTest
                .create()
                .setCaretMarker('|')
                .input("package test;\n" +
                       "public enum Test {\n" +
                       "    A, B, C;\n" +
                       "    private static void t(Object a) {\n" +
                       "        sw|itch ((Test) a) {\n" +
                       "            case A: break;\n" +
                       "            case B: break;\n" +
                       "            case C: break;\n" +
                       "        }\n" +
                       "    }\n" +
                       "}\n")
                .sourceLevel("1.7")
                .run(Tiny.class)
                .findWarning("4:10-4:10:hint:ERR_Tiny.fillSwitchDefault")
                .applyFix("FIX_Tiny.fillSwitchDefault")
                .assertCompilable()
                .assertOutput("package test;\n" +
                              "public enum Test {\n" +
                              "    A, B, C;\n" +
                              "    private static void t(Object a) {\n" +
                              "        switch ((Test) a) {\n" +
                              "            case A: break;\n" +
                              "            case B: break;\n" +
                              "            case C: break;\n" +
                              "            default: throw new AssertionError(((Test) a).name());\n" +
                              "        }\n" +
                              "    }\n" +
                              "}\n");
    }
    
    public void testFillSwitchTypeVar222372() throws Exception {
        HintTest
                .create()
                .setCaretMarker('|')
                .input("package test;\n" +
                       "public enum Test {\n" +
                       "    A, B, C;\n" +
                       "    private static <T extends Enum> void t(T a) {\n" +
                       "        sw|itch (a) {\n" +
                       "            case A: break;\n" +
                       "            case B: break;\n" +
                       "            case C: break;\n" +
                       "        }\n" +
                       "    }\n" +
                       "}\n",
                       false)
                .sourceLevel("1.7")
                .run(Tiny.class)
                .assertWarnings();
    }
}