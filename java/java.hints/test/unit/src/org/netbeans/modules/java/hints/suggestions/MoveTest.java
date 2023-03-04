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
package org.netbeans.modules.java.hints.suggestions;

import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.java.hints.test.api.HintTest;

/**
 *
 * @author Ralph Benjamin Ruijs <ralphbenjamin@netbeans.org>
 */
public class MoveTest extends NbTestCase {

    public MoveTest(String name) {
        super(name);
    }
    
    public void test219932() throws Exception { // #219932 - Move initializer to constructor
        HintTest.create()
                .setCaretMarker('|')
                .input("package test;\n"
                + "public class Test {\n"
                + "    private int[] mess|age = {1,2,3,4};\n"
                + "    public Test() {\n"
                + "    }\n"
                + "}\n")
                .run(Move.class)
                .findWarning("2:22-2:22:hint:Move initializer to constructor(s)")
                .applyFix("Move initializer to constructor(s)")
                .assertCompilable()
                .assertOutput("package test;\n"
                + "public class Test {\n"
                + "    private int[] message;\n"
                + "    public Test() {\n"
                + "        this.message = new int[]{1, 2, 3, 4};\n"
                + "    }\n"
                + "}\n");
    }

    public void testMoveToConstructor() throws Exception {
        HintTest.create()
                .setCaretMarker('|')
                .input("package test;\n"
                + "public class Test {\n"
                + "    private String mess|age = \"Hello World!\";\n"
                + "    public Test() {\n"
                + "    }\n"
                + "}\n")
                .run(Move.class)
                .findWarning("2:23-2:23:hint:Move initializer to constructor(s)")
                .applyFix("Move initializer to constructor(s)")
                .assertCompilable()
                .assertOutput("package test;\n"
                + "public class Test {\n"
                + "    private String message;\n"
                + "    public Test() {\n"
                + "        this.message = \"Hello World!\";\n"
                + "    }\n"
                + "}\n");
    }
    
    public void testMoveToConstructors() throws Exception {
        HintTest.create()
                .setCaretMarker('|')
                .input("package test;\n"
                + "public class Test {\n"
                + "    private String mess|age = \"Hello World!\";\n"
                + "    public Test() {\n"
                + "    }\n"
                + "    public Test(String something) {\n"
                + "        super();\n"
                + "    }\n"
                + "    public Test(String something, boolean replace) {\n"
                + "        if(replace) {\n"
                + "            message = something;\n"
                + "        }\n"
                + "    }\n"
                + "}\n")
                .run(Move.class)
                .findWarning("2:23-2:23:hint:Move initializer to constructor(s)")
                .applyFix("Move initializer to constructor(s)")
                .assertCompilable()
                .assertOutput("package test;\n"
                + "public class Test {\n"
                + "    private String message;\n"
                + "    public Test() {\n"
                + "        this.message = \"Hello World!\";\n"
                + "    }\n"
                + "    public Test(String something) {\n"
                + "        super();\n"
                + "        this.message = \"Hello World!\";\n"
                + "    }\n"
                + "    public Test(String something, boolean replace) {\n"
                + "        this.message = \"Hello World!\";\n"
                + "        if(replace) {\n"
                + "            message = something;\n"
                + "        }\n"
                + "    }\n"
                + "}\n");
    }
    
    public void testMoveToNewConstructor() throws Exception {
        HintTest.create()
                .setCaretMarker('|')
                .input("package test;\n"
                + "public class Test {\n"
                + "    private String mess|age = \"Hello World!\";\n"
                + "}\n")
                .run(Move.class)
                .findWarning("2:23-2:23:hint:Move initializer to constructor(s)")
                .applyFix("Move initializer to constructor(s)")
                .assertCompilable()
                .assertOutput("package test;\n"
                + "public class Test {\n"
                + "    private String message;\n"
                + "    public Test() {\n"
                + "        this.message = \"Hello World!\";\n"
                + "    }\n"
                + "}\n");
    }
    
    public void testMoveToNewConstructorAnonymous222391() throws Exception {
        HintTest.create()
                .setCaretMarker('|')
                .input("package test;\n"
                + "public class Test {\n"
                + "    {\n"
                + "        new Runnable() {\n"
                + "            private String mess|age = \"Hello World!\";\n"
                + "            public void run() {}\n"
                + "        };\n"
                + "    }\n"
                + "}\n")
                .run(Move.class)
                .findWarning("4:31-4:31:hint:Move initializer to constructor(s)")
                .applyFix("Move initializer to constructor(s)")
                .assertCompilable()
                .assertOutput("package test;\n"
                + "public class Test {\n"
                + "    {\n"
                + "        new Runnable() {\n"
                + "            private String message;\n"
                + "            {\n"
                + "                this.message = \"Hello World!\";\n"
                + "            }\n"
                + "            public void run() {}\n"
                + "        };\n"
                + "    }\n"
                + "}\n");
    }
}
