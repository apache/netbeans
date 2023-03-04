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
package org.netbeans.modules.java.hints.bugs;

import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.java.hints.test.api.HintTest;

/**
 *
 * @author sdedic
 */
public class MathRandomCastTest extends NbTestCase {

    public MathRandomCastTest(String name) {
        super(name);
    }

    public void testCastAndCompute() throws Exception {
        HintTest.create()
                .input(
                "package test;\n" +
                "public class Test {\n" +
                "    public void a() {\n" +
                "        int foo = ((int)Math.random() + 3) * 4;\n" +
                "    }\n" +
                "}\n"
                )
                .run(MathRandomCast.class)
                .findWarning("3:19-3:37:verifier:Math.int() immediately casted to int or long")
                .applyFix()
                .assertCompilable()
                .assertOutput(
                ("package test;\n" +
                "public class Test {\n" +
                "    public void a() {\n" +
                "        int foo = (int) ((Math.random() + 3) * 4);\n" +
                "    }\n" +
                "}\n"
                ).replaceAll("\\s+", " "));
    }
    
    /**
     * Checks the expression is correctly parenthesized in the
     * @throws Exception 
     */
    public void testCastMultiExpression() throws Exception {
        HintTest.create()
                .input(
                "package test;\n" +
                "public class Test {\n" +
                "    public void a() {\n" +
                "        int foo = (int)Math.random() * 4 + 3;\n" +
                "    }\n" +
                "}\n"
                )
                .run(MathRandomCast.class)
                .findWarning("3:18-3:36:verifier:Math.int() immediately casted to int or long")
                .applyFix()
                .assertCompilable()
                .assertOutput(
                ("package test;\n" +
                "public class Test {\n" +
                "    public void a() {\n" +
                "        int foo = (int) (Math.random() * 4 + 3);\n" +
                "    }\n" +
                "}\n"
                ).replaceAll("\\s+", " "));
    }
    
    /**
     * Checks that the warning is presented, but no fix is offered
     * @throws Exception 
     */
    public void testCastInAssignment() throws Exception {
        HintTest.create()
                .input(
                "package test;\n" +
                "public class Test {\n" +
                "    public void a() {\n" +
                "        int foo = (int)Math.random();\n" +
                "    }\n" +
                "}\n"
                )
                .run(MathRandomCast.class)
                .findWarning("3:18-3:36:verifier:Math.int() immediately casted to int or long")
                .assertFixes();
    }
}
