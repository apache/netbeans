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
package org.netbeans.modules.java.hints;

import com.sun.source.tree.Tree;
import org.junit.Test;
import static org.netbeans.modules.java.hints.EmptyStatements.getDisplayName;
import org.netbeans.modules.java.hints.test.api.HintTest;

/**
 *
 * @author markiewb
 */
public class RemoveEmptyStatementTest {

    @Test
    public void testRemoveEmptyBLOCK() throws Exception {
        HintTest.create()
                .input("package test;\n"
                        + "public class Test {\n"
                        + "    public static void main(String[] args) {\n"
                        + "        System.out.println(\"hello world\");;\n"
                        + "    }\n"
                        + "}\n")
                .run(EmptyStatements.class)
                .findWarning("3:42-3:43:verifier:" + getDisplayName(Tree.Kind.BLOCK))
                .applyFix(Bundle.ERR_EmptyBLOCK())
                .assertCompilable()
                .assertOutput("package test;\n"
                        + "public class Test {\n"
                        + "    public static void main(String[] args) {\n"
                        + "        System.out.println(\"hello world\");\n"
                        + "    }\n"
                        + "}\n");
    }

    @Test
    public void testRemoveEmptyWHILE_LOOP() throws Exception {
        HintTest.create()
                .input("package test;\n"
                        + "public class Test {\n"
                        + "    public static void main(String[] args) {\n"
                        + "        while(true);\n"
                        + "    }\n"
                        + "}\n")
                .run(EmptyStatements.class)
                .findWarning("3:8-3:20:verifier:" + getDisplayName(Tree.Kind.WHILE_LOOP))
                .assertFixes();
    }

    @Test
    public void testRemoveEmptyFOR_LOOP() throws Exception {
        HintTest.create()
                .input("package test;\n"
                        + "public class Test {\n"
                        + "    public static void main(String[] args) {\n"
                        + "        for(int i=0;i<10;i++);\n"
                        + "    }\n"
                        + "}\n")
                .run(EmptyStatements.class)
                .findWarning("3:8-3:30:verifier:" + getDisplayName(Tree.Kind.FOR_LOOP))
                .assertFixes();
    }

    @Test
    public void testRemoveEmptyENHANCED_FOR_LOOP() throws Exception {

        HintTest.create()
                .input("package test;\n"
                        + "public class Test {\n"
                        + "    public static void main(String[] args) {\n"
                        + "        java.util.List<String> list = new java.util.ArrayList<String>();"
                        + "        for(String s:list);\n"
                        + "    }\n"
                        + "}\n")
                .run(EmptyStatements.class)
                .findWarning("3:80-3:99:verifier:" + getDisplayName(Tree.Kind.ENHANCED_FOR_LOOP))
                .assertFixes();
    }

    @Test
    public void testRemoveEmptyDO_WHILE_LOOP() throws Exception {
        HintTest.create()
                .input("package test;\n"
                        + "public class Test {\n"
                        + "    public static void main(String[] args) {\n"
                        + "        do;while(true);\n"
                        + "    }\n"
                        + "}\n")
                .run(EmptyStatements.class)
                .findWarning("3:8-3:23:verifier:" + getDisplayName(Tree.Kind.DO_WHILE_LOOP))
                .assertFixes();
    }

    @Test
    public void testRemoveEmptyIF() throws Exception {
        final String ifWarn = "3:8-4:13:verifier:" + getDisplayName(Tree.Kind.IF);
        final String elseWarn = "3:8-4:13:verifier:" + getDisplayName(Tree.Kind.IF);
        HintTest.create()
                .input("package test;\n"
                        + "public class Test {\n"
                        + "    public static void main(String[] args) {\n"
                        + "        if (\"1\".equals(\"1\")); \n"
                        + "        else;"
                        + "    }\n"
                        + "}\n")
                .run(EmptyStatements.class)
                .assertWarnings(ifWarn, elseWarn)
                .findWarning(ifWarn)
                .assertFixes();

        HintTest.create()
                .input("package test;\n"
                        + "public class Test {\n"
                        + "    public static void main(String[] args) {\n"
                        + "        if (\"1\".equals(\"1\")); \n"
                        + "        else;"
                        + "    }\n"
                        + "}\n")
                .run(EmptyStatements.class)
                .assertWarnings(ifWarn, elseWarn)
                .findWarning(elseWarn)
                .assertFixes();
    }

}
