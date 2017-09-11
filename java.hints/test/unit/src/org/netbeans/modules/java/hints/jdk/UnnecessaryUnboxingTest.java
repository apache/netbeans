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
