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
public class UnnecessaryUnboxingTest extends NbTestCase {

    public UnnecessaryUnboxingTest(String name) {
        super(name);
    }

    public void testNeededUnboxingLambdaReturn() throws Exception {
        HintTest
                .create()
                .input("package test;\n"
                + "import java.util.Map;\n"
                + "import java.util.stream.Collectors;\n" 
                + "final class Test  {"
                + "    String values = \"some text\"; \n" 
                + "    Map<Character, Long> freqV = values.chars().boxed().collect(\n" 
                + "        Collectors.groupingBy(integ -> (char) integ.intValue(), Collectors.counting())); // \n" 
                + "}", false)
                .sourceLevel("8")
                .run(UnnecessaryUnboxing.class)
                .assertWarnings();
    }

    public void testRedundantUnboxingLambdaReturn() throws Exception {
        HintTest
                .create()
                .input("package test;\n"
                + "import java.util.Map;\n"
                + "import java.util.stream.Collectors;\n" 
                + "final class Test  {"
                + "    String values = \"some text\"; \n" 
                + "    Map<Integer, Long> freqV = values.chars().boxed().collect(\n" 
                + "        Collectors.groupingBy(integ -> integ.intValue(), Collectors.counting())); // \n" 
                + "}", false)
                .sourceLevel("8")
                .run(UnnecessaryUnboxing.class)
                .findWarning("5:39-5:55:verifier:TEXT_UnnecessaryUnboxing").
                applyFix("FIX_UnnecessaryUnboxing").
                assertOutput("package test;\n"
                + "import java.util.Map;\n"
                + "import java.util.stream.Collectors;\n" 
                + "final class Test  {"
                + "    String values = \"some text\"; \n" 
                + "    Map<Integer, Long> freqV = values.chars().boxed().collect(\n" 
                + "        Collectors.groupingBy(integ -> integ, Collectors.counting())); // \n" 
                + "}");
    }

    public void testBinaryOperator() throws Exception {
        HintTest
                .create()
                .input("package test;\n"
                + "import java.util.Map;\n"
                + "import java.util.stream.Collectors;\n" 
                + "final class Test  {"
                + "    public void x() {\n" +
                "        Integer x = new Integer(4);\n" +
                "        Integer y = new Integer(4);\n" +
                "        if( x.intValue() == (int)y ) {\n" +
                "            System.out.println(x + \" == \" + y);\n" +
                "        } else {\n" +
                "            System.out.println(x + \" != \" + y);\n" +
                "        }\n" +
                "    } "
                + "}", false)
                .sourceLevel("1.5")
                .run(UnnecessaryUnboxing.class)
                .findWarning("6:12-6:24:verifier:TEXT_UnnecessaryUnboxing").
                applyFix("FIX_UnnecessaryUnboxing").
                assertCompilable().
                assertOutput("package test;\n"
                + "import java.util.Map;\n"
                + "import java.util.stream.Collectors;\n" 
                + "final class Test  {"
                + "    public void x() {\n" +
                "        Integer x = new Integer(4);\n" +
                "        Integer y = new Integer(4);\n" +
                "        if( x == (int)y ) {\n" +
                "            System.out.println(x + \" == \" + y);\n" +
                "        } else {\n" +
                "            System.out.println(x + \" != \" + y);\n" +
                "        }\n" +
                "    } "
                + "}");
    }

    /**
     * If both sides of binary operator are being unboxed, leave the code as it 
     * is for consistency reasons
     * @throws Exception 
     */
    public void testBinaryOperatorException() throws Exception {
        HintTest
                .create()
                .input("package test;\n"
                + "import java.util.Map;\n"
                + "import java.util.stream.Collectors;\n" 
                + "final class Test  {"
                + "    public void x() {\n" +
                "        Integer x = new Integer(4);\n" +
                "        Integer y = new Integer(4);\n" +
                "        if( x.intValue() == y.intValue() ) {\n" +
                "            System.out.println(x + \" == \" + y);\n" +
                "        } else {\n" +
                "            System.out.println(x + \" != \" + y);\n" +
                "        }\n" +
                "    } "
                + "}", false)
                .sourceLevel("1.5")
                .run(UnnecessaryUnboxing.class)
                .assertWarnings();
    }
}
