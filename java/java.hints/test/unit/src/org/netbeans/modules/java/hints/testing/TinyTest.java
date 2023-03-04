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
package org.netbeans.modules.java.hints.testing;

import junit.framework.Assert;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.java.hints.test.api.HintTest;
import org.openide.filesystems.FileUtil;

/**
 *
 * @author lahvac
 */
public class TinyTest extends NbTestCase {
    
    public TinyTest(String name) {
        super(name);
    }
    
    public void testAssertEqualsForArrays() throws Exception {
        HintTest.create()
                .input("package test;\n" +
                       "import junit.framework.Assert;\n" +
                       "public class Test {\n" +
                       "    private void test(Object[] g, Object[] a) {\n" +
                       "        Assert.assertEquals(g, a);\n" +
                       "    }\n" +
                       "}\n")
                .classpath(FileUtil.getArchiveRoot(Assert.class.getProtectionDomain().getCodeSource().getLocation()))
                .run(Tiny.class)
                .findWarning("4:15-4:27:verifier:" + Bundle.ERR_assertEqualsForArrays())
                .applyFix()
                .assertCompilable()
                .assertOutput("package test;\n" +
                              "import junit.framework.Assert;\n" +
                              "public class Test {\n" +
                              "    private void test(Object[] g, Object[] a) {\n" +
                              "        org.junit.Assert.assertArrayEquals(g, a);\n" +
                              "    }\n" +
                              "}\n");
    }
    
    public void testAssertEqualsForArraysWithMessages() throws Exception {
        HintTest.create()
                .input("package test;\n" +
                       "import junit.framework.Assert;\n" +
                       "public class Test {\n" +
                       "    private void test(int[] g, int[] a) {\n" +
                       "        Assert.assertEquals(\"a\", g, a);\n" +
                       "    }\n" +
                       "}\n")
                .classpath(FileUtil.getArchiveRoot(Assert.class.getProtectionDomain().getCodeSource().getLocation()))
                .run(Tiny.class)
                .findWarning("4:15-4:27:verifier:" + Bundle.ERR_assertEqualsForArrays())
                .applyFix()
                .assertCompilable()
                .assertOutput("package test;\n" +
                              "import junit.framework.Assert;\n" +
                              "public class Test {\n" +
                              "    private void test(int[] g, int[] a) {\n" +
                              "        org.junit.Assert.assertArrayEquals(\"a\", g, a);\n" +
                              "    }\n" +
                              "}\n");
    }
    
    public void testAssertEqualsNoArrays() throws Exception {
        HintTest.create()
                .input("package test;\n" +
                       "import junit.framework.Assert;\n" +
                       "public class Test {\n" +
                       "    private void test(Object g, Object a) {\n" +
                       "        Assert.assertEquals(g, a);\n" +
                       "    }\n" +
                       "}\n")
                .classpath(FileUtil.getArchiveRoot(Assert.class.getProtectionDomain().getCodeSource().getLocation()))
                .run(Tiny.class)
                .assertWarnings();
    }

    public void testAssertEqualsForOrgJunit() throws Exception {
        HintTest.create()
                .input("package test;\n" +
                       "import org.junit.Assert;\n" +
                       "public class Test {\n" +
                       "    private void test(char[] g, char[] a) {\n" +
                       "        Assert.assertEquals(\"a\", g, a);\n" +
                       "    }\n" +
                       "}\n")
                .classpath(FileUtil.getArchiveRoot(Assert.class.getProtectionDomain().getCodeSource().getLocation()))
                .run(Tiny.class)
                .findWarning("4:15-4:27:verifier:" + Bundle.ERR_assertEqualsForArrays())
                .applyFix()
                .assertCompilable()
                .assertOutput("package test;\n" +
                       "import org.junit.Assert;\n" +
                              "public class Test {\n" +
                              "    private void test(char[] g, char[] a) {\n" +
                              "        Assert.assertArrayEquals(\"a\", g, a);\n" +
                              "    }\n" +
                              "}\n");
    }
    
    public void testAssertEqualsForDouble() throws Exception {
        HintTest.create()
                .input("package test;\n" +
                       "import org.junit.Assert;\n" +
                       "public class Test {\n" +
                       "    private void test(double[] g, double[] a) {\n" +
                       "        Assert.assertEquals(\"a\", g, a);\n" +
                       "    }\n" +
                       "}\n")
                .classpath(FileUtil.getArchiveRoot(Assert.class.getProtectionDomain().getCodeSource().getLocation()))
                .run(Tiny.class)
                .findWarning("4:15-4:27:verifier:" + Bundle.ERR_assertEqualsForArrays())
                .assertFixes();
    }

    public void testAssertEqualsMismatchedConstantVSReal() throws Exception {
        HintTest.create()
                .input("package test;\n" +
                       "import junit.framework.Assert;\n" +
                       "public class Test {\n" +
                       "    private void test(int actual) {\n" +
                       "        Assert.assertEquals(actual, 0);\n" +
                       "    }\n" +
                       "}\n")
                .classpath(FileUtil.getArchiveRoot(Assert.class.getProtectionDomain().getCodeSource().getLocation()))
                .run(Tiny.class)
                .findWarning("4:15-4:27:verifier:" + Bundle.ERR_assertEqualsMismatchedConstantVSReal())
                .applyFix()
                .assertCompilable()
                .assertOutput("package test;\n" +
                              "import junit.framework.Assert;\n" +
                              "public class Test {\n" +
                              "    private void test(int actual) {\n" +
                              "        Assert.assertEquals(0, actual);\n" +
                              "    }\n" +
                              "}\n");
    }
    
    public void testAssertEqualsMismatchedConstantVSReal2() throws Exception {
        HintTest.create()
                .input("package test;\n" +
                       "import junit.framework.Assert;\n" +
                       "public class Test {\n" +
                       "    private void test(String actual) {\n" +
                       "        Assert.assertEquals(\"a\", actual, \"golden\");\n" +
                       "    }\n" +
                       "}\n")
                .classpath(FileUtil.getArchiveRoot(Assert.class.getProtectionDomain().getCodeSource().getLocation()))
                .run(Tiny.class)
                .findWarning("4:15-4:27:verifier:" + Bundle.ERR_assertEqualsMismatchedConstantVSReal())
                .applyFix()
                .assertCompilable()
                .assertOutput("package test;\n" +
                              "import junit.framework.Assert;\n" +
                              "public class Test {\n" +
                              "    private void test(String actual) {\n" +
                              "        Assert.assertEquals(\"a\", \"golden\", actual);\n" +
                              "    }\n" +
                              "}\n");
    }
    
    public void testAssertEqualsMismatchedConstantVSRealCorrect() throws Exception {
        HintTest.create()
                .input("package test;\n" +
                       "import junit.framework.Assert;\n" +
                       "public class Test {\n" +
                       "    private void test(String actual) {\n" +
                       "        Assert.assertEquals(\"a\", \"golden\", actual);\n" +
                       "    }\n" +
                       "}\n")
                .classpath(FileUtil.getArchiveRoot(Assert.class.getProtectionDomain().getCodeSource().getLocation()))
                .run(Tiny.class)
                .assertWarnings();
    }
    
    public void testAssertEqualsInconvertiblePos1() throws Exception {
        HintTest.create()
                .input("package test;\n" +
                       "import junit.framework.Assert;\n" +
                       "public class Test {\n" +
                       "    private void test() {\n" +
                       "        Assert.assertEquals(\"a\", \"golden\", 1);\n" +
                       "    }\n" +
                       "}\n")
                .classpath(FileUtil.getArchiveRoot(Assert.class.getProtectionDomain().getCodeSource().getLocation()))
                .run(Tiny.class)
                .assertWarnings("4:15-4:27:verifier:" + Bundle.ERR_assertEqualsIncovertibleTypes());
    }
    
    public void testAssertEqualsInconvertibleNeg1() throws Exception {
        HintTest.create()
                .input("package test;\n" +
                       "import junit.framework.Assert;\n" +
                       "public class Test {\n" +
                       "    private void test(CharSequence actual) {\n" +
                       "        Assert.assertEquals(\"a\", \"golden\", actual);\n" +
                       "    }\n" +
                       "}\n")
                .classpath(FileUtil.getArchiveRoot(Assert.class.getProtectionDomain().getCodeSource().getLocation()))
                .run(Tiny.class)
                .assertWarnings();
    }
}
