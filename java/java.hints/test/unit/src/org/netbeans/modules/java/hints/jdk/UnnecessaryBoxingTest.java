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
