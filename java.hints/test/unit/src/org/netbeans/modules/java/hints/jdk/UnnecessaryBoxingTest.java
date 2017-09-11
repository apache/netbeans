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
package org.netbeans.modules.java.hints.jdk;

import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.java.hints.test.api.HintTest;

/**
 *
 * @author sdedic
 */
public class UnnecessaryBoxingTest extends NbTestCase {

    public UnnecessaryBoxingTest(String name) {
        super(name);
    }

    public void testAssignment() throws Exception {
        HintTest
                .create()
                .input("package test;\n"
                + "final class Test  {\n"
                + "    public void test() {\n"
                + "        int a = 5;\n"
                + "        Integer b;\n"
                + "        b = new Integer(a);\n"
                + "    }\n"
                + "}", false)
                .sourceLevel("1.5")
                .run(UnnecessaryBoxing.class)
                .findWarning("5:12-5:26:verifier:TEXT_UnnecessaryBoxing").
                applyFix().assertOutput("package test;\n"
                + "final class Test  {\n"
                + "    public void test() {\n"
                + "        int a = 5;\n"
                + "        Integer b;\n"
                + "        b = a;\n"
                + "    }\n"
                + "}");
    }

    public void noHintInJDK4() throws Exception {
        HintTest
                .create()
                .input("package test;\n"
                + "final class Test  {\n"
                + "    public void test() {\n"
                + "        int a = 5;\n"
                + "        Integer b;\n"
                + "        b = new Integer(a);\n"
                + "    }\n"
                + "}", false)
                .sourceLevel("1.4")
                .run(UnnecessaryBoxing.class)
                .assertWarnings();
    }

    public void testBoxingStringAppend() throws Exception {
        HintTest
                .create()
                .input("package test;\n"
                + "final class Test  {\n"
                + "    public void test() {\n"
                + "        String s = \"\" + Integer.valueOf(1);\n"
                + "    }\n"
                + "}", false)
                .sourceLevel("1.5")
                .run(UnnecessaryBoxing.class)
                .findWarning("3:24-3:42:verifier:TEXT_UnnecessaryBoxing").
                applyFix().
                assertOutput("package test;\n"
                + "final class Test  {\n"
                + "    public void test() {\n"
                + "        String s = \"\" + 1;\n"
                + "    }\n"
                + "}");
    }
    
    /**
     * As one of the operands is not known to be non-null, unnecessary unboxing
     * warning should be suppressed; unboxing the operands could cause a NPE.
     */
    public void testConditionalUnboxingPossibleNull() throws Exception {
        HintTest
                .create()
                .input("package test;\n"
                + "final class Test  {\n"
                + "    public Long l() {\n"
                + "        return null;\n"
                + "    }\n"
                        
                + "    public void test(boolean t) {\n"
                + "        Long a = t ? new Integer(1) : l();\n"
                + "    }\n"
                + "}", false)
                .sourceLevel("1.5").preference(UnnecessaryBoxing.PREFER_CAST_TO_BOXING, true)
                .run(UnnecessaryBoxing.class)
                .assertWarnings()
                ;
    }

    public void testConditionalCompatible() throws Exception {
        HintTest
                .create()
                .input("package test;\n"
                + "final class Test  {\n"
                + "    public void test(boolean t) {\n"
                + "        Long a = t ? new Integer(1) : Long.valueOf(5);\n"
                + "    }\n"
                + "}", false)
                .sourceLevel("1.5").preference(UnnecessaryBoxing.PREFER_CAST_TO_BOXING, true)
                .run(UnnecessaryBoxing.class)
                .assertWarnings("3:21-3:35:verifier:TEXT_UnnecessaryBoxing",
                    "3:38-3:53:verifier:TEXT_UnnecessaryBoxing")
                .findWarning("3:38-3:53:verifier:TEXT_UnnecessaryBoxing")
                .applyFix()
                .assertOutput("package test;\n"
                + "final class Test  {\n"
                + "    public void test(boolean t) {\n"
                + "        Long a = t ? new Integer(1) : (long) 5;\n"
                + "    }\n"
                + "}");
    }

    public void testConditionalDifferentTypes() throws Exception {
        HintTest
                .create()
                .input("package test;\n"
                + "final class Test  {\n"
                + "    public void test(boolean t) {\n"
                + "        Long a = t ? new Float(1) : Long.valueOf(5);\n"
                + "    }\n"
                + "}", false)
                .sourceLevel("1.5").preference(UnnecessaryBoxing.PREFER_CAST_TO_BOXING, true)
                .run(UnnecessaryBoxing.class)
                .assertWarnings("3:36-3:51:verifier:TEXT_UnnecessaryBoxing");
    }

    public void testQualifiedMethodOverload() throws Exception {
        HintTest
                .create()
                .input("package test;\n"
                + "final class Test  {\n"
                + "    public void test(boolean t) {\n"
                + "        m(Integer.valueOf(1));\n"
                + "    }\n"
                + "    \n"
                + "    public void m(int a) {}\n"
                + "    \n"
                + "    public void m(Integer a) {} \n"
                + "}", false)
                .sourceLevel("1.5")
                .run(UnnecessaryBoxing.class)
                .assertWarnings();
    }
    
    public void testBoxingToSpecificType() throws Exception {
        HintTest
                .create()
                .input("package test;\n"
                + "import javax.swing.AbstractAction;\n" +
                  "import javax.swing.Action;\n" +
                  "import javax.swing.text.DefaultEditorKit.BeepAction;"
                + "final class Test  {\n"
                + "    public void test(boolean t) {\n"
                + "    public void test() {\n" +
                  "        AbstractAction aa = new BeepAction();\n" +
                  "        aa.putValue(Action.MNEMONIC_KEY, Integer.valueOf('E'));\n" +
                  "    }\n"
                + "    }\n"
                + "}", false)
                .sourceLevel("1.5")
                .run(UnnecessaryBoxing.class)
                .assertWarnings();
        
    }

    public void testBoxingToSameType() throws Exception {
        HintTest
                .create()
                .input("package test;\n"
                + "import javax.swing.AbstractAction;\n" +
                  "import javax.swing.Action;\n" +
                  "import javax.swing.text.DefaultEditorKit.BeepAction;"
                + "final class Test  {\n"
                + "    public void test(boolean t) {\n"
                + "    public void test() {\n" +
                  "        AbstractAction aa = new BeepAction();\n" +
                  "        aa.putValue(Action.MNEMONIC_KEY, Integer.valueOf(0));\n" +
                  "    }\n"
                + "    }\n"
                + "}", false)
                .sourceLevel("1.5")
                .run(UnnecessaryBoxing.class)
                .assertWarnings("7:41-7:59:verifier:TEXT_UnnecessaryBoxing");
        
    }
}
