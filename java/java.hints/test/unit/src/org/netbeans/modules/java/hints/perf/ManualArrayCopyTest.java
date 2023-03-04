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
package org.netbeans.modules.java.hints.perf;

import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.java.hints.test.api.HintTest;

/**
 *
 * @author lahvac
 */
public class ManualArrayCopyTest extends NbTestCase {

    public ManualArrayCopyTest(String name) {
        super(name);
    }

    public void testArrayCopy1() throws Exception {
        HintTest
                .create()
                .input("package test;\n" +
                       "public class Test {\n" +
                       "    public void test(String[] args) {\n" +
                       "        int[] source = new int[3];\n" +
                       "        int[] target = new int[6];\n" +
                       "        int o = 3;\n" +
                       "\n" +
                       "        for (int i = 0; i < source.length; i++) {\n" +
                       "            target[o + i] = source[i];\n" +
                       "        }\n" +
                       "    }\n" +
                       "}\n")
                .run(ManualArrayCopy.class)
                .findWarning("7:8-7:11:verifier:ERR_manual-array-copy")
                .applyFix("FIX_manual-array-copy")
                .assertCompilable()
                .assertOutput("package test;\n" +
                              "public class Test {\n" +
                              "    public void test(String[] args) {\n" +
                              "        int[] source = new int[3];\n" +
                              "        int[] target = new int[6];\n" +
                              "        int o = 3;\n" +
                              "\n" +
                              "        System.arraycopy(source, 0, target, o, source.length);\n" +
                              "     }\n" +
                              "}\n");
    }

    public void testArrayCopy2() throws Exception {
        HintTest
                .create()
                .input("package test;\n" +
                       "public class Test {\n" +
                       "    public void test(String[] args) {\n" +
                       "        int[] source = new int[3];\n" +
                       "        int[] target = new int[6];\n" +
                       "        int o = 3;\n" +
                       "\n" +
                       "        for (int i = 2; i < source.length; i++) {\n" +
                       "            target[o + i] = source[i];\n" +
                       "        }\n" +
                       "    }\n" +
                       "}\n")
                .run(ManualArrayCopy.class)
                .findWarning("7:8-7:11:verifier:ERR_manual-array-copy")
                .applyFix("FIX_manual-array-copy")
                .assertCompilable()
                .assertOutput("package test;\n" +
                              "public class Test {\n" +
                              "    public void test(String[] args) {\n" +
                              "        int[] source = new int[3];\n" +
                              "        int[] target = new int[6];\n" +
                              "        int o = 3;\n" +
                              "\n" +
                              "        System.arraycopy(source, 2, target, o + 2, source.length - 2);\n" +
                              "     }\n" +
                              "}\n");
    }

    public void testArrayCopy3() throws Exception {
        HintTest
                .create()
                .input("package test;\n" +
                       "public class Test {\n" +
                       "    public void test(String[] args) {\n" +
                       "        int[] source = new int[3];\n" +
                       "        int[] target = new int[6];\n" +
                       "        int o = 3;\n" +
                       "\n" +
                       "        for (int i = 2; i < source.length; i++) {\n" +
                       "            target[i + o] = source[i];\n" +
                       "        }\n" +
                       "    }\n" +
                       "}\n")
                .run(ManualArrayCopy.class)
                .findWarning("7:8-7:11:verifier:ERR_manual-array-copy")
                .applyFix("FIX_manual-array-copy")
                .assertCompilable()
                .assertOutput("package test;\n" +
                              "public class Test {\n" +
                              "    public void test(String[] args) {\n" +
                              "        int[] source = new int[3];\n" +
                              "        int[] target = new int[6];\n" +
                              "        int o = 3;\n" +
                              "\n" +
                              "        System.arraycopy(source, 2, target, 2 + o, source.length - 2);\n" +
                              "     }\n" +
                              "}\n");
    }

    public void testArrayCollectionCopy1() throws Exception {
        HintTest
                .create()
                .input("package test;\n" +
                       "public class Test {\n" +
                       "    public void test(String[] args) {\n" +
                       "        java.util.List<String> l = null;\n" +
                       "\n" +
                       "        for (int c = 0; c < args.length; c++) {\n" +
                       "            l.add(args[c]);\n" +
                       "        }\n" +
                       "    }\n" +
                       "}\n")
                .run(ManualArrayCopy.class)
                .findWarning("5:8-5:11:verifier:ERR_manual-array-copy-coll")
                .applyFix("FIX_manual-array-copy-coll")
                .assertCompilable()
                .assertOutput("package test;\n" +
                              "import java.util.Arrays;\n" +
                              "public class Test {\n" +
                              "    public void test(String[] args) {\n" +
                              "        java.util.List<String> l = null;\n" +
                              "\n" +
                              "        l.addAll(Arrays.asList(args));\n" +
                              "     }\n" +
                              "}\n");
    }

    public void testArrayCollectionCopy2() throws Exception {
        HintTest
                .create()
                .input("package test;\n" +
                       "public class Test {\n" +
                       "    public void test(String[] args) {\n" +
                       "        java.util.List<String> l = null;\n" +
                       "\n" +
                       "        for (String s : args) {\n" +
                       "            l.add(s);\n" +
                       "        }\n" +
                       "    }\n" +
                       "}\n")
                .run(ManualArrayCopy.class)
                .findWarning("5:8-5:11:verifier:ERR_manual-array-copy-coll")
                .applyFix("FIX_manual-array-copy-coll")
                .assertCompilable()
                .assertOutput("package test;\n" +
                              "import java.util.Arrays;\n" +
                              "public class Test {\n" +
                              "    public void test(String[] args) {\n" +
                              "        java.util.List<String> l = null;\n" +
                              "\n" +
                              "        l.addAll(Arrays.asList(args));\n" +
                              "     }\n" +
                              "}\n");
    }

    public void testNoBoxing188830() throws Exception {
        HintTest
                .create()
                .input("package test;\n" +
                       "public class Test {\n" +
                       "    public void test(String[] args) {\n" +
                       "        int[] source = new int[3];\n" +
                       "        Integer[] target = new Integer[6];\n" +
                       "        int o = 3;\n" +
                       "\n" +
                       "        for (int i = 0; i < source.length; i++) {\n" +
                       "            target[o + i] = source[i];\n" +
                       "        }\n" +
                       "    }\n" +
                       "}\n")
                .run(ManualArrayCopy.class)
                .assertWarnings();
    }

    public void testArrayCopySubType1() throws Exception {
        HintTest
                .create()
                .input("package test;\n" +
                       "public class Test {\n" +
                       "    public void test(String[] args) {\n" +
                       "        String[] source = new String[3];\n" +
                       "        Object[] target = new Object[6];\n" +
                       "        int o = 3;\n" +
                       "\n" +
                       "        for (int i = 0; i < source.length; i++) {\n" +
                       "            target[o + i] = source[i];\n" +
                       "        }\n" +
                       "    }\n" +
                       "}\n")
                .run(ManualArrayCopy.class)
                .findWarning("7:8-7:11:verifier:ERR_manual-array-copy")
                .applyFix("FIX_manual-array-copy")
                .assertCompilable()
                .assertOutput("package test;\n" +
                              "public class Test {\n" +
                              "    public void test(String[] args) {\n" +
                              "        String[] source = new String[3];\n" +
                              "        Object[] target = new Object[6];\n" +
                              "        int o = 3;\n" +
                              "\n" +
                              "        System.arraycopy(source, 0, target, o, source.length);\n" +
                              "     }\n" +
                              "}\n");
    }

    public void testArrayCopySubType2() throws Exception {
        HintTest
                .create()
                .input("package test;\n" +
                       "public class Test {\n" +
                       "    public void test(String[] args) {\n" +
                       "        Object[] source = new Object[6];\n" +
                       "        String[] target = new String[3];\n" +
                       "        int o = 3;\n" +
                       "\n" +
                       "        for (int i = 0; i < source.length; i++) {\n" +
                       "            target[o + i] = source[i];\n" +
                       "        }\n" +
                       "    }\n" +
                       "}\n", false)
                .run(ManualArrayCopy.class)
                .assertWarnings();
    }

    public void testArrayCopy191435() throws Exception {
        HintTest
                .create()
                .input("package test;\n" +
                       "public class Test {\n" +
                       "    public void test(String[] args) {\n" +
                       "        int[] source = new int[6];\n" +
                       "        float[] target = new float[3];\n" +
                       "        int o = 3;\n" +
                       "\n" +
                       "        for (int i = 0; i < source.length; i++) {\n" +
                       "            target[o + i] = source[i];\n" +
                       "        }\n" +
                       "    }\n" +
                       "}\n")
                .run(ManualArrayCopy.class)
                .assertWarnings();
    }
    
    public void testArrayCopy227264() throws Exception {
        HintTest
                .create()
                .input("package test;\n" +
                       "public class Test {\n" +
                       "    void test(String[] result, String[][] vals) {\n" +
                       "        for (int i = 0; i < result.length; i++) {\n" +
                       "            result[i] = vals[i][i];\n" +
                       "        }\n" +
                       "    }\n" +
                       "}\n")
                .run(ManualArrayCopy.class)
                .assertWarnings();
    }
}