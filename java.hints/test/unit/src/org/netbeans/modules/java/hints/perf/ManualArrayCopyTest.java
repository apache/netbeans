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