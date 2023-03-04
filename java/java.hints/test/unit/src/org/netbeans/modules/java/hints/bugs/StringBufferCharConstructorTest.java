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
public class StringBufferCharConstructorTest extends NbTestCase {

    public StringBufferCharConstructorTest(String name) {
        super(name);
    }
    
    public void testIntPass() throws Exception {
        HintTest.create().
                input(
                "package test;\n"
                + "public final class Test {\n"
                + "    public static void main(String[] args) {\n"
                + "        StringBuffer sb = new StringBuffer(35);\n"
                + "    }\n"
                + "}"
                )
                .run(StringBufferCharConstructor.class).
                assertWarnings();
    }

    public void testChar() throws Exception {
        HintTest.create().
                input(
                "package test;\n"
                + "public final class Test {\n"
                + "    public static void main(String[] args) {\n"
                + "        StringBuffer sb = new StringBuffer('a');\n"
                + "    }\n"
                + "}"
                )
                .run(StringBufferCharConstructor.class).
                assertWarnings("3:26-3:47:verifier:StringBuffer constructor called with `char' argument").
                findWarning("3:26-3:47:verifier:StringBuffer constructor called with `char' argument").
                applyFix().
                assertOutput(
                "package test;\n"
                + "public final class Test {\n"
                + "    public static void main(String[] args) {\n"
                + "        StringBuffer sb = new StringBuffer().append('a');\n"
                + "    }\n"
                + "}");
    }

    public void testBoxedCharacter() throws Exception {
        HintTest.create().
                input(
                "package test;\n"
                + "public final class Test {\n"
                + "    public static void main(String[] args) {\n"
                + "        StringBuffer sb = new StringBuffer(Character.valueOf('a'));\n"
                + "    }\n"
                + "}"
                )
                .run(StringBufferCharConstructor.class).
                assertWarnings("3:26-3:66:verifier:StringBuffer constructor called with `char' argument").
                findWarning("3:26-3:66:verifier:StringBuffer constructor called with `char' argument").
                applyFix().
                assertOutput(
                "package test;\n"
                + "public final class Test {\n"
                + "    public static void main(String[] args) {\n"
                + "        StringBuffer sb = new StringBuffer().append(Character.valueOf('a'));\n"
                + "    }\n"
                + "}");
    }

    public void testRetainsComments() throws Exception {
        HintTest.create().
                input(
                "package test;\n"
                + "public final class Test {\n"
                + "    public static void main(String[] args) {\n"
                + "        StringBuffer sb = /* comment before */ new /* comment 2*/ StringBuffer /* comment 3*/ ('a'); // comment after\n"
                + "    }\n"
                + "}"
                )
                .run(StringBufferCharConstructor.class).
                assertWarnings("3:47-3:99:verifier:StringBuffer constructor called with `char' argument").
                findWarning("3:47-3:99:verifier:StringBuffer constructor called with `char' argument").
                applyFix().
                assertOutput(
                "package test;\n"
                + "public final class Test {\n"
                + "    public static void main(String[] args) {\n"
                + "        StringBuffer sb = /* comment before */ new /* comment 2*/ StringBuffer /* comment 3*/().append('a'); // comment after\n"
                + "    }\n"
                + "}");
    }
}
