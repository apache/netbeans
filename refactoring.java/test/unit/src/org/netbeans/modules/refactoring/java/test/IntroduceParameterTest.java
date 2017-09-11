/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2011 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2011 Sun Microsystems, Inc.
 */
package org.netbeans.modules.refactoring.java.test;

import com.sun.source.tree.CompilationUnitTree;
import com.sun.source.util.TreePath;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import org.netbeans.api.java.source.CompilationController;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.Task;
import org.netbeans.api.java.source.TreePathHandle;
import org.netbeans.modules.refactoring.api.Problem;
import org.netbeans.modules.refactoring.api.RefactoringSession;
import org.netbeans.modules.refactoring.java.api.IntroduceParameterRefactoring;
import org.netbeans.modules.refactoring.java.ui.ChangeParametersPanel.Javadoc;
import org.openide.filesystems.FileObject;

/**
 *
 * @author Jan Becicka
 * @author Ralph Ruijs
 */
public class IntroduceParameterTest extends RefactoringTestBase {

    public IntroduceParameterTest(String name) {
        super(name);
    }
    
    public void testAbstractMethod() throws Exception {
        String source;
        writeFilesAndWaitForScan(src,
                new File("t/A.java", source = "package t;\n"
                + "public abstract class A {\n"
                + "public abstract int cislo(int a);\n"
                + "public void m() {\n"
                + "    System.out.println(cislo(1));\n"
                + "}\n"
                + "}\n"),
                new File("t/F.java", "package t;\n"
                + "public class F extends A {\n"
                + "    public int cislo(int a) {\n"
                + "        return 2;\n"
                + "    }\n"
                + "    public void bar() {\n"
                + "        System.out.println(cislo(1) + 1);\n"
                + "    }\n"
                + "}\n"));
        performIntroduce(src.getFileObject("t/A.java"), source.lastIndexOf("int a") +4, Javadoc.NONE, false, false );
        verifyContent(src,
                new File("t/A.java", "package t;\n"
                + "public abstract class A {\n"
                + "public abstract int cislo(int a, int introduced);\n"
                + "public void m() {\n"
                + "    System.out.println(cislo(1, 1));\n"
                + "}\n"
                + "}\n"),
                new File("t/F.java", "package t;\n"
                + "public class F extends A {\n"
                + "    public int cislo(int a, int introduced) {\n"
                + "        return 2;\n"
                + "    }\n"
                + "    public void bar() {\n"
                + "        System.out.println(cislo(1, 1) + 1);\n"
                + "    }\n"
                + "}\n"));
    }
    
    public void test238154() throws Exception {
        String source;
        writeFilesAndWaitForScan(src,
                new File("t/A.java", source = "package t; public class A {\n"
                        + "    public int m(int a, int b){\n"
                        + "        System.out.println(\"abc\");\n"
                        + "          System.out.println(\"abc\");\n"
                        + "            System.out.println(\"abc\");\n"
                        + "              System.out.println(\"abc\");\n"
                        + "              \n"
                        + "        int[][] m = new int[][]{{-1,   2, 5},\n"
                        + "                                { 1, -13, 5}};\n"
                        + "        return b;\n"
                        + "    }\n"
                        + "    \n"
                        + "    public void m2(){\n"
                        + "        m(5, 6);\n"
                        + "    }\n"
                        + "}\n"));
        performIntroduce(src.getFileObject("t/A.java"), source.indexOf("abc") + 1, Javadoc.NONE, true, true);
        verifyContent(src,
                new File("t/A.java", "package t; public class A {\n"
                        + "    public int m(int a, int b){\n"
                        + "        return m(a, b, \"abc\");\n"
                        + "    }\n"
                        + "    public int m(int a, int b, String introduced) {\n"
                        + "        System.out.println(introduced);\n"
                        + "          System.out.println(introduced);\n"
                        + "            System.out.println(introduced);\n"
                        + "              System.out.println(introduced);\n"
                        + "              \n"
                        + "        int[][] m = new int[][]{{-1,   2, 5},\n"
                        + "                                { 1, -13, 5}};\n"
                        + "        return b;\n"
                        + "    }\n"
                        + "    \n"
                        + "    public void m2(){\n"
                        + "        m(5, 6);\n"
                        + "    }\n"
                        + "}\n"));
    }
    
    public void test238301() throws Exception {
        String source;
        writeFilesAndWaitForScan(src,
                new File("t/A.java", source = "package t; public class A {\n"
                + "    public static void testMethod(int i) {\n"
                + "        if (i > 5) {\n"
                + "            System.out.println(\"abcd\");\n"
                + "        }\n"
                + "    }\n"
                + "\n"
                + "    public static void main(string[] args) {\n"
                + "        testMethod(1);\n"
                + "    }\n"
                + "}\n"));
        performIntroduce(src.getFileObject("t/A.java"), source.indexOf('5') - 4, Javadoc.NONE, false, false);
        verifyContent(src,
                new File("t/A.java", source = "package t; public class A {\n"
                + "    public static void testMethod(int i, boolean introduced) {\n"
                + "        if (introduced) {\n"
                + "            System.out.println(\"abcd\");\n"
                + "        }\n"
                + "    }\n"
                + "\n"
                + "    public static void main(string[] args) {\n"
                + "        testMethod(1, 1 > 5);\n"
                + "    }\n"
                + "}\n"));
    }
    
    public void test235299a() throws Exception {
        String source;
        writeFilesAndWaitForScan(src,
                new File("t/A.java", source = "package t; public class A {\n"
                        + "    private String introduced;\n"
                        + "    public static void testMethod() {\n"
                        + "         String[] args = null;\n"
                        + "         introduced = \"\";\n"
                        + "    }\n"
                        + "}\n"));
        performIntroduce(src.getFileObject("t/A.java"), source.indexOf("null") + 1, Javadoc.NONE, false, false, new Problem(true, "ERR_NameAlreadyUsed"));
    }
    
    public void test235299b() throws Exception {
        String source;
        writeFilesAndWaitForScan(src,
                new File("t/A.java", source = "package t; public class A {\n"
                        + "    public static void testMethod() {\n"
                        + "         String introduced = \"\";\n"
                        + "         String[] args = null;\n"
                        + "    }\n"
                        + "\n"
                        + "    public static void main(string[] args) {\n"
                        + "        testMethod();\n"
                        + "    }\n"
                        + "}\n"));
        performIntroduce(src.getFileObject("t/A.java"), source.indexOf("null") + 1, Javadoc.NONE, false, false, new Problem(true, "ERR_NameAlreadyUsed"));
    }
    
    public void test235299c() throws Exception {
        String source;
        writeFilesAndWaitForScan(src,
                new File("t/A.java", source = "package t; public class A {\n"
                        + "    private final String introduced;\n"
                        + "    public static void testMethod() {\n"
                        + "         String[] args = null;\n"
                        + "    }\n"
                        + "\n"
                        + "    public static void main(string[] args) {\n"
                        + "        testMethod();\n"
                        + "    }\n"
                        + "}\n"));
        performIntroduce(src.getFileObject("t/A.java"), source.indexOf("null") + 1, Javadoc.NONE, false, false);
    }
    
    public void test235299d() throws Exception {
        String source;
        writeFilesAndWaitForScan(src,
                new File("t/A.java", source = "package t; public class A {\n"
                        + "    public static void testMethod() {\n"
                        + "         String[] args = null;\n"
                        + "         introduced();\n"
                        + "    }\n"
                        + "\n"
                        + "    public static void introduced() {\n"
                        + "    }\n"
                        + "}\n"));
        performIntroduce(src.getFileObject("t/A.java"), source.indexOf("null") + 1, Javadoc.NONE, false, false);
    }
    
    public void test231635() throws Exception {
        String source;
        writeFilesAndWaitForScan(src,
                new File("t/A.java", source = "package t; public class A {\n"
                + "    /**\n"
                + "     * My Test Method\n"
                + "     */\n"
                + "    public static void testMethod() {\n"
                + "         System.out.println(2);\n"
                + "    } // End of Method\n"
                + "\n"
                + "    public static void main(string[] args) {\n"
                + "        testMethod();\n"
                + "    }\n"
                + "}\n"));
        performIntroduce(src.getFileObject("t/A.java"), source.indexOf('2') + 1, Javadoc.UPDATE, true, false);
        verifyContent(src,
                new File("t/A.java", "package t; public class A {\n"
                + "    /**\n"
                + "     * My Test Method\n"
                + "     */\n"
                + "    public static void testMethod() {\n"
                + "         testMethod(2);\n"
                + "    } // End of Method\n"
                + "\n"
                + "    /**\n"
                + "     * My Test Method\n"
                + "     * @param introduced the value of introduced\n"
                + "     */\n"
                + "    public static void testMethod(int introduced) {\n"
                + "         System.out.println(introduced);\n"
                + "    }\n"
                + "\n"
                + "    public static void main(string[] args) {\n"
                + "        testMethod();\n"
                + "    }\n"
                + "}\n"));
    }
    
    public void test221440() throws Exception {
        String source;
        writeFilesAndWaitForScan(src,
                new File("t/A.java", source = "package t; public class A {\n"
                + "    public static void testMethod(String... args) {\n"
                + "         args = null;\n"
                + "    }\n"
                + "\n"
                + "    public static void main(string[] args) {\n"
                + "        testMethod();\n"
                + "    }\n"
                + "}\n"));
        performIntroduce(src.getFileObject("t/A.java"), source.indexOf("null") + 1, Javadoc.NONE, false, false);
        verifyContent(src,
                new File("t/A.java", "package t; public class A {\n"
                + "    public static void testMethod(String[] introduced, String... args) {\n"
                + "         args = introduced;\n"
                + "    }\n"
                + "\n"
                + "    public static void main(string[] args) {\n"
                + "        testMethod(null);\n"
                + "    }\n"
                + "}\n"));
    }
    
    public void test208699() throws Exception {
        String source;
        writeFilesAndWaitForScan(src,
                new File("t/A.java", source = "package t; public class A {\n"
                + "    /**\n"
                + "     * My Test Method\n"
                + "     */\n"
                + "    public static void testMethod() {\n"
                + "         System.out.println(2);\n"
                + "    } // End of Method\n"
                + "\n"
                + "    public static void main(string[] args) {\n"
                + "        testMethod();\n"
                + "    }\n"
                + "}\n"));
        performIntroduce(src.getFileObject("t/A.java"), source.indexOf('2') + 1, Javadoc.NONE, true, false);
        verifyContent(src,
                new File("t/A.java", "package t; public class A {\n"
                + "    /**\n"
                + "     * My Test Method\n"
                + "     */\n"
                + "    public static void testMethod() {\n"
                + "         testMethod(2);\n"
                + "    } // End of Method\n"
                + "\n"
                + "    /**\n"
                + "     * My Test Method\n"
                + "     */\n"
                + "    public static void testMethod(int introduced) {\n"
                + "         System.out.println(introduced);\n"
                + "    }\n"
                + "\n"
                + "    public static void main(string[] args) {\n"
                + "        testMethod();\n"
                + "    }\n"
                + "}\n"));
    }
    
    public void test213063() throws Exception {
        String source;
        writeFilesAndWaitForScan(src,
                new File("t/A.java", source = "package t;\n"
                + "public class A {\n"
                + "    public A() {\n"
                + "         System.out.println(2);\n"
                + "    }\n"
                + "\n"
                + "    public static void main(string[] args) {\n"
                + "        A a = new A();\n"
                + "    }\n"
                + "}\n"));
        performIntroduce(src.getFileObject("t/A.java"), source.indexOf('2') + 1, Javadoc.NONE, true, false);
        verifyContent(src,
                new File("t/A.java", "package t;\n"
                + "public class A {\n"
                + "    public A() {\n"
                + "         this(2);\n"
                + "    }\n"
                + "\n"
                + "    public A(int introduced) {\n"
                + "         System.out.println(introduced);\n"
                + "    }\n"
                + "\n"
                + "    public static void main(string[] args) {\n"
                + "        A a = new A();\n"
                + "    }\n"
                + "}\n"));
    }

    public void testIntroduceParametera() throws Exception {
        String source;
        writeFilesAndWaitForScan(src,
                new File("t/A.java", source = "package t;\n"
                + "public class A {\n"
                + "public int add(int a, int b) {\n"
                + "    System.out.println(a+b);\n"
                + "    return a*(a+b);\n"
                + "}\n"
                + "public void m() {\n"
                + "    System.out.println(add(1,1));\n"
                + "}\n"
                + "}\n"),
                new File("t/F.java", "package t;\n"
                + "public class F extends A {\n"
                + "    public int add(int a, int b) {\n"
                + "        return super.add(a, b);\n"
                + "    }\n"
                + "    public void bar() {\n"
                + "        System.out.println(add(1,add(23,1)) + 1);\n"
                + "    }\n"
                + "}\n"));
        performIntroduce(src.getFileObject("t/A.java"), source.lastIndexOf("a+b"), Javadoc.NONE, false, false );
        verifyContent(src,
                new File("t/A.java", "package t;\n"
                + "public class A {\n"
                + "public int add(int a, int b, int introduced) {\n"
                + "    System.out.println(a+b);\n"
                + "    return a*introduced;\n"
                + "}\n"
                + "public void m() {\n"
                + "    System.out.println(add(1,1, 1 + 1));\n"
                + "}\n"
                + "}\n"),
                new File("t/F.java", "package t;\n"
                + "public class F extends A {\n"
                + "    public int add(int a, int b, int introduced) {\n"
                + "        return super.add(a, b, introduced);\n"
                + "    }\n"
                + "    public void bar() {\n"
                + "        System.out.println(add(1,add(23,1, 23 + 1), 1 + add(23, 1, 23 + 1)) + 1);\n"
                + "    }\n"
                + "}\n"));
    }
    
    public void testIntroduceParameterb() throws Exception {
        String source;
        writeFilesAndWaitForScan(src,
                new File("t/A.java", source = "package t;\n"
                + "public class A {\n"
                + "public int length(String unused) {\n"
                + "    String introduced = \"Hello World!\";\n"
                + "    return introduced.length();\n"
                + "}\n"
                + "}\n"),
                new File("t/F.java", "package t;\n"
                + "public class F extends A {\n"
                + "    public int length(String unused) {\n"
                + "        return super.length(unused);\n"
                + "    }\n"
                + "    public void bar() {\n"
                + "        System.out.println(length(\"\"));\n"
                + "    }\n"
                + "}\n"));
        performIntroduce(src.getFileObject("t/A.java"), source.indexOf("introduced"), Javadoc.NONE, false, false );
        verifyContent(src,
                new File("t/A.java", "package t;\n"
                + "public class A {\n"
                + "public int length(String unused, String introduced) {\n"
                + "    return introduced.length();\n"
                + "}\n"
                + "}\n"),
                new File("t/F.java", "package t;\n"
                + "public class F extends A {\n"
                + "    public int length(String unused, String introduced) {\n"
                + "        return super.length(unused, introduced);\n"
                + "    }\n"
                + "    public void bar() {\n"
                + "        System.out.println(length(\"\", \"Hello World!\"));\n"
                + "    }\n"
                + "}\n"));
    }
     
    public void testIntroduceParameterc() throws Exception {
        String source;
        writeFilesAndWaitForScan(src,
                new File("t/A.java", source = "package t;\n"
                + "public class A {\n"
                + "public int add(int a, int b) {\n"
                + "    return a*(a+b);\n"
                + "}\n"
                + "public void m() {\n"
                + "    System.out.println(add(1,1));\n"
                + "}\n"
                + "}\n"),
                new File("t/F.java", "package t;\n"
                + "public class F extends A {\n"
                + "    public int add(int a, int b) {\n"
                + "        return super.add(a, b);\n"
                + "    }\n"
                + "    public void bar() {\n"
                + "        System.out.println(add(1,add(23,1)) + 1);\n"
                + "    }\n"
                + "}\n"));
        performIntroduce(src.getFileObject("t/A.java"), source.lastIndexOf("a+b"), Javadoc.NONE, true, false );
        verifyContent(src,
                new File("t/A.java", "package t;\n"
                + "public class A {\n"
                + "public int add(int a, int b) {\n"
                + "    return add(a, b, a + b);\n"
                + "}\n"
                + "public int add(int a, int b, int introduced) {\n"
                + "    return a * introduced;\n"
                + "}\n"
                + "public void m() {\n"
                + "    System.out.println(add(1,1));\n"
                + "}\n"
                + "}\n"),
                new File("t/F.java", "package t;\n"
                + "public class F extends A {\n"
                + "    public int add(int a, int b) {\n"
                + "        return super.add(a, b);\n"
                + "    }\n"
                + "    public void bar() {\n"
                + "        System.out.println(add(1,add(23,1)) + 1);\n"
                + "    }\n"
                + "}\n"));
    }
    
    public void testConstructora() throws Exception {
        String source;
        writeFilesAndWaitForScan(src,
                new File("t/A.java", source = "package t; public class A {\n"
                + "    public A() {\n"
                + "         System.out.println(2);\n"
                + "    }\n"
                + "\n"
                + "    public static void main(string[] args) {\n"
                + "        A a = new A();\n"
                + "    }\n"
                + "}\n"));
        performIntroduce(src.getFileObject("t/A.java"), source.indexOf('2') + 1, Javadoc.NONE, false, false);
        verifyContent(src,
                new File("t/A.java", "package t; public class A {\n"
                + "    public A(int introduced) {\n"
                + "         System.out.println(introduced);\n"
                + "    }\n"
                + "\n"
                + "    public static void main(string[] args) {\n"
                + "        A a = new A(2);\n"
                + "    }\n"
                + "}\n"));
    }
        
    public void testConstructorb() throws Exception {
        String source;
        writeFilesAndWaitForScan(src,
                new File("t/A.java", source = "package t; public class A {\n"
                + "    public A() {\n"
                + "         System.out.println(2);\n"
                + "    }\n"
                + "\n"
                + "    public A(int x) {\n"
                + "         System.out.println(x);\n"
                + "    }\n"
                + "}\n"));
        performIntroduce(src.getFileObject("t/A.java"), source.indexOf('2') + 1, Javadoc.NONE, false, false, new Problem(false, "ERR_existingConstructor"));
    }
    
    public void testExistingMethod() throws Exception {
        String source;
        writeFilesAndWaitForScan(src,
                new File("t/A.java", source = "package t; public class A {\n"
                + "    public static void testMethod() {\n"
                + "         System.out.println(2);\n"
                + "    }\n"
                + "\n"
                + "    public static void testMethod(int x) {\n"
                + "         System.out.println(x);\n"
                + "    }\n"
                + "}\n"));
        performIntroduce(src.getFileObject("t/A.java"), source.indexOf('2') + 1, Javadoc.NONE, false, false, new Problem(false, "ERR_existingMethod"));

        writeFilesAndWaitForScan(src,
                new File("t/A.java", source = "package t; public class A extends B{\n"
                + "    public void testMethod() {\n"
                + "         System.out.println(2);\n"
                + "    }\n"
                + "}\n"),
                new File("t/B.java", "package t; public class B {\n"
                + "    public void testMethod(int x) {\n"
                + "         System.out.println(x);\n"
                + "    }\n"
                + "}\n"));
        performIntroduce(src.getFileObject("t/A.java"), source.indexOf('2') + 1, Javadoc.NONE, false, false, new Problem(false, "ERR_existingMethod"));
    }
    
    public void testJavadoca() throws Exception {
        String source;
        writeFilesAndWaitForScan(src,
                new File("t/A.java", source = "package t; public class A {\n"
                + "    public static void testMethod() {\n"
                + "         System.out.println(2);\n"
                + "    }\n"
                + "\n"
                + "    public static void main(string[] args) {\n"
                + "        testMethod();\n"
                + "    }\n"
                + "}\n"));
        performIntroduce(src.getFileObject("t/A.java"), source.indexOf('2') + 1, Javadoc.GENERATE, false, false);
        verifyContent(src,
                new File("t/A.java", "package t; public class A {\n"
                + "    /**\n"
                + "     * \n"
                + "     * @param introduced the value of introduced\n"
                + "     */\n"
                + "    public static void testMethod(int introduced) {\n"
                + "         System.out.println(introduced);\n"
                + "    }\n"
                + "\n"
                + "    public static void main(string[] args) {\n"
                + "        testMethod(2);\n"
                + "    }\n"
                + "}\n"));
    }
     
    public void testJavadocb() throws Exception {
        String source;
        writeFilesAndWaitForScan(src,
                new File("t/A.java", source = "package t; public class A {\n"
                + "    /**\n"
                + "     * My Test Method\n"
                + "     */\n"
                + "    public static void testMethod() {\n"
                + "         System.out.println(2);\n"
                + "    }\n"
                + "\n"
                + "    public static void main(string[] args) {\n"
                + "        testMethod();\n"
                + "    }\n"
                + "}\n"));
        performIntroduce(src.getFileObject("t/A.java"), source.indexOf('2') + 1, Javadoc.UPDATE, false, false);
        verifyContent(src,
                new File("t/A.java", "package t; public class A {\n"
                + "    /**\n"
                + "     * My Test Method\n"
                + "     * @param introduced the value of introduced\n"
                + "     */\n"
                + "    public static void testMethod(int introduced) {\n"
                + "         System.out.println(introduced);\n"
                + "    }\n"
                + "\n"
                + "    public static void main(string[] args) {\n"
                + "        testMethod(2);\n"
                + "    }\n"
                + "}\n"));
    }
    
    public void testReplaceAll() throws Exception {
        String source;
        writeFilesAndWaitForScan(src,
                new File("t/A.java", source = "package t; public class A {\n"
                + "    public static void testMethod() {\n"
                + "         System.out.println(2);\n"
                + "         System.out.println(2);\n"
                + "    }\n"
                + "\n"
                + "    public static void main(string[] args) {\n"
                + "        testMethod();\n"
                + "    }\n"
                + "}\n"));
        performIntroduce(src.getFileObject("t/A.java"), source.indexOf('2') + 1, Javadoc.GENERATE, false, true);
        verifyContent(src,
                new File("t/A.java", "package t; public class A {\n"
                + "    /**\n"
                + "     * \n"
                + "     * @param introduced the value of introduced\n"
                + "     */\n"
                + "    public static void testMethod(int introduced) {\n"
                + "         System.out.println(introduced);\n"
                + "         System.out.println(introduced);\n"
                + "    }\n"
                + "\n"
                + "    public static void main(string[] args) {\n"
                + "        testMethod(2);\n"
                + "    }\n"
                + "}\n"));
    }
    

    private void performIntroduce(FileObject source, final int position, final Javadoc javadoc, final boolean compatible, final boolean replaceall, Problem... expectedProblems) throws Exception {
        final IntroduceParameterRefactoring[] r = new IntroduceParameterRefactoring[1];

        JavaSource.forFileObject(source).runUserActionTask(new Task<CompilationController>() {

            @Override
            public void run(CompilationController javac) throws Exception {
                javac.toPhase(JavaSource.Phase.RESOLVED);
                CompilationUnitTree cut = javac.getCompilationUnit();

                TreePath tp = javac.getTreeUtilities().pathFor(position);

                r[0] = new IntroduceParameterRefactoring(TreePathHandle.create(tp, javac));
                r[0].getContext().add(javadoc);
                r[0].setParameterName("introduced");
                r[0].setReplaceAll(replaceall);
                r[0].setOverloadMethod(compatible);
                r[0].setFinal(false);
            }
        }, true);

        RefactoringSession rs = RefactoringSession.create("Introduce Parameter");
        List<Problem> problems = new LinkedList<Problem>();

        addAllProblems(problems, r[0].preCheck());
        if (!problemIsFatal(problems)) {
            addAllProblems(problems, r[0].prepare(rs));
        }
        if (!problemIsFatal(problems)) {
            addAllProblems(problems, rs.doRefactoring(true));
        }

        assertProblems(Arrays.asList(expectedProblems), problems);
    }
}
