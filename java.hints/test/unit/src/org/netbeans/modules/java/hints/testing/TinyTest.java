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
