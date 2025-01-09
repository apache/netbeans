/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.netbeans.modules.refactoring.java.test;

import com.sun.source.tree.ClassTree;
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
import org.netbeans.modules.refactoring.java.api.InnerToOuterRefactoring;

public class InnerToOutterTest extends RefactoringTestBase {

    public InnerToOutterTest(String name) {
        super(name);
    }
    

    
    public void test259004() throws Exception {
        String source;
        writeFilesAndWaitForScan(src,
                new File("t/A.java", source = "package t;\n"
                        + "\n"
                        + "import java.util.function.Consumer;\n"
                        + "\n"
                        + "public class A {\n"
                        + "\n"
                        + "public static void main(String[] args) {\n"
                        + "    Consumer<F> c = f -> {};\n"
                        + "}\n"
                        + "\n"
                        + "public static final class F {}\n"
                        + "}"));
        performInnerToOuterTest(null, source.indexOf('F') + 1);
        verifyContent(src,
                new File("t/A.java", source = "package t;\n"
                        + "\n"
                        + "import java.util.function.Consumer;\n"
                        + "\n"
                        + "public class A {\n"
                        + "\n"
                        + "public static void main(String[] args) {\n"
                        + "    Consumer<F> c = f -> {};\n"
                        + "}\n"
                        + "\n"
                        + "}"),
                new File("t/F.java", "/*\n"
                        + " * Refactoring License\n"
                        + " */\n"
                        + "\n"
                        + "package t;\n"
                        + "\n"
                        + "/**\n"
                        + " *\n"
                        + " * @author junit\n"
                        + " */\n"
                        + "public final class F {\n"
                        + "}\n"));
    }
    
    public void test238000() throws Exception {
        String source;
        writeFilesAndWaitForScan(src,
                new File("t/A.java", source = "package t; public class A {\n"
                        + "    public class B {\n"
                        + "        public class F {\n"
                        + "        }\n"
                        + "    }\n"
                        + "    public class F {\n"
                        + "    }\n"
                        + "}"));
        performInnerToOuterTest("outer", source.indexOf('F') + 1, new Problem(true, "ERR_InnerToOuter_ClassNameClash"));
    }
    
    public void test238000a() throws Exception {
        String source;
        writeFilesAndWaitForScan(src,
                new File("t/A.java", source = "package t; public class A {\n"
                        + "    public class B {\n"
                        + "    }\n"
                        + "    public class F {\n"
                        + "    }\n"
                        + "}"),
                new File("t/F.java", "package t; public class F {\n"
                        + "}"));
        performInnerToOuterTest("outer", source.indexOf('F') + 1, new Problem(true, "ERR_ClassClash"));
    }

    public void test236189() throws Exception {
        writeFilesAndWaitForScan(src,
                new File("t/F.java", "package t; public enum F {  A, B, C }\n"),
                new File("t/A.java", "package t; public class A { int i; public class B { public void foo() { F f = F.A; switch(f) { case A: break; } } } }"));
        performInnerToOuterTest(null);
        verifyContent(src,
                new File("t/F.java", "package t; public enum F {  A, B, C }\n"),
                new File("t/B.java", "/*\n"
                        + " * Refactoring License\n"
                        + " */\n"
                        + "\n"
                        + "package t;\n"
                        + "\n"
                        + "/**\n"
                        + " *\n"
                        + " * @author junit\n"
                        + " */\n"
                        + "public class B {\n"
                        + "\n"
                        + "    public void foo() {\n"
                        + "        F f = F.A;\n"
                        + "        switch (f) {\n"
                        + "            case A:\n"
                        + "                break;\n"
                        + "        }\n"
                        + "    }\n"
                        + "\n"
                        + "}\n"),
                new File("t/A.java", "package t; public class A { int i; }"));
    }
    
    public void test248745() throws Exception { // #248745 - Move Inner to outer Level does not alter static import of moved class
        writeFilesAndWaitForScan(src,
                new File("t/A.java", "package t;\n public class A {\n public static class B {\n }\n }"),
                new File("t/C.java", "package t;\n import static t.A.B;\n public class C {\n public void foo() {\n B b = new B(); } }"));
        performInnerToOuterTest(null);
        verifyContent(src,
                new File("t/B.java", "/*\n"
                        + " * Refactoring License\n"
                        + " */\n"
                        + "\n"
                        + "package t;\n"
                        + "\n"
                        + "/**\n"
                        + " *\n"
                        + " * @author junit\n"
                        + " */\n"
                        + "public class B {\n"
                        + "}\n"),
                new File("t/A.java", "package t; public class A { }"),
                new File("t/C.java", "package t; public class C { public void foo() { B b = new B(); } }"));
    }
    
    public void test249299() throws Exception { // #249299 - JavaDoc comments for enum values are lost during Refactor -> Move Inner to Outer level 
        writeFilesAndWaitForScan(src,
                new File("t/A.java", "package t;\n"
                        + "public class A {\n"
                        + "    /**\n"
                        + "     * JavaDoc for SampleEnum\n"
                        + "     */\n"
                        + "    public enum SampleEnum {\n"
                        + "        /**\n"
                        + "         * JavaDoc for value1\n"
                        + "         */\n"
                        + "        Value1,\n"
                        + "        /**\n"
                        + "         * JavaDoc for value2\n"
                        + "         */\n"
                        + "        Value2;\n"
                        + "    }\n"
                        + "}"));
        performInnerToOuterTest(null);
        verifyContent(src,
                      new File("t/SampleEnum.java", "/* * Refactoring License */ package t;\n"
                        + "/**\n"
                        + " * JavaDoc for SampleEnum\n"
                        + " */\n"
                        + "public enum SampleEnum {\n"
                        + "    /**\n"
                        + "     * JavaDoc for value1\n"
                        + "     */\n"
                        + "    Value1,\n"
                        + "    /**\n"
                        + "     * JavaDoc for value2\n"
                        + "     */\n"
                        + "    Value2\n"
                        + "}\n"),
                      new File("t/A.java", "package t; public class A { }"));
    }
    
    public void test100305() throws Exception {
        writeFilesAndWaitForScan(src,
                                 new File("t/A.java", "package t; public class A { class B { } class F { F(int outer) { System.out.println(outer); } } }"));
        performInnerToOuterTest("outer", new Problem(true, "ERR_InnerToOuter_OuterNameClash"));
    }
    
    public void test100305a() throws Exception {
        writeFilesAndWaitForScan(src,
                                 new File("t/A.java", "package t; public class A { class B extends F { B(int buiten) { } } class F { F(int outer) { System.out.println(outer); } } }"));
        performInnerToOuterTest("buiten", new Problem(true, "ERR_InnerToOuter_OuterNameClashSubtype"));
    }
    
    public void test218080() throws Exception { // #218080 - Move inner to outer fails if inner class code refers to statically imported methods
        writeFilesAndWaitForScan(src,
                                 new File("t/A.java", "package t;\n import static java.lang.Math.*;\n public class A {\n class B {\n }\n\n/** * Klazz F */\nclass F {\n B b; \n void method() {\n max(2, 3); }\n }\n }"));
        performInnerToOuterTest("outer");
        verifyContent(src,
                      new File("t/F.java", "/* * Refactoring License */ package t; /** * Klazz F */ class F { A.B b; private final A outer; F(final A outer) { this.outer = outer; } void method() { Math.max(2, 3); } } "),
                      new File("t/A.java", "package t; import static java.lang.Math.*; public class A { class B { } }"));
    }

    public void test208438() throws Exception { // #208438 - [Move Inner To Outer Level] Class javadoc is lost
        writeFilesAndWaitForScan(src,
                                 new File("t/A.java", "package t;\n public class A {\n class B {\n }\n\n/** * Klazz F */\nclass F {\n B b; }\n }"));
        performInnerToOuterTest("outer");
        verifyContent(src,
                      new File("t/F.java", "/* * Refactoring License */ package t; /** * Klazz F */ class F { A.B b; private final A outer; F(final A outer) { this.outer = outer; } } "),
                      new File("t/A.java", "package t; public class A { class B { } }"));
    }
    
    public void test208791() throws Exception {
        writeFilesAndWaitForScan(src, new File("t/A.java", "package t; public class A { class B { public String outer; } class F extends B { B b; } }"));
        performInnerToOuterTest("2outer", new Problem(true, "ERR_InvalidIdentifier"));
    }
    public void test208791a() throws Exception {
        writeFilesAndWaitForScan(src, new File("t/A.java", "package t; public class A { class B { public String outer; } class F extends B { B b; } }"));
        performInnerToOuterTest("", new Problem(true, "ERR_EmptyReferenceName"));
    }
    public void test208791b() throws Exception {
        writeFilesAndWaitForScan(src, new File("t/A.java", "package t; public class A { class B { public String outer; } class F extends B { B b; } }"));
        performInnerToOuterTest("outer", new Problem(false, "WRN_OuterNameAlreadyUsed"));
    }
    public void test208791c() throws Exception {
        writeFilesAndWaitForScan(src, new File("t/A.java", "package t; public class A { class B { public String outer; } class F extends B { B b; } }"));
        performInnerToOuterTest("b", new Problem(true, "ERR_OuterNameAlreadyUsed"));
    }
    
    public void test196955() throws Exception {
        writeFilesAndWaitForScan(src,
                                 new File("t/A.java", "package t; public class A { class B { } class F { B b; } }"));
        performInnerToOuterTest("outer");
        verifyContent(src,
                      new File("t/F.java", "/* * Refactoring License */ package t; /** * * @author junit */ class F { A.B b; private final A outer; F(final A outer) { this.outer = outer; } } "),
                      new File("t/A.java", "package t; public class A { class B { } }"));
    }

    public void test178451() throws Exception {
        writeFilesAndWaitForScan(src,
                                 new File("t/A.java", "@A(foo=A.FOO) package t; public @interface A { public String foo(); public static final String FOO = \"foo\"; public static class F { } }"));
        performInnerToOuterTest(null);
        verifyContent(src,
                      new File("t/F.java", "/* * Refactoring License */ package t; /** * * @author junit */\n\npublic class F { }\n"),
                      new File("t/A.java", "@A(foo=A.FOO) package t; public @interface A { public String foo(); public static final String FOO = \"foo\"; }"));
    }

    public void test138204a() throws Exception {
        writeFilesAndWaitForScan(src,
                                 new File("t/A.java", "package t; public class A { static class S { private static void f() {} } private class F { private void t() {S.f();} } }"));
        performInnerToOuterTest("outer");
        verifyContent(src,
                      new File("t/F.java", "/* * Refactoring License */ package t; /** * * @author junit */ class F { private final A outer; F(final A outer) { this.outer = outer; }\n private void t() { A.S.f(); } }\n"),//TODO: why outer reference?
                      new File("t/A.java", "package t; public class A { static class S { private static void f() {} } }"));
    }

    public void test195947() throws Exception {
        writeFilesAndWaitForScan(src,
                                 new File("t/A.java", "package t; public class A { private final int foo; public A() { this.foo = 0; } static class F { } }")); 
        performInnerToOuterTest(null);
        verifyContent(src,
                      new File("t/F.java", "/* * Refactoring License */ package t; /** * * @author junit */ class F { }\n"),
                      new File("t/A.java", "package t; public class A { private final int foo; public A() { this.foo = 0; } }"));

}
    
    public void test138204b() throws Exception {
        writeFilesAndWaitForScan(src,
                                 new File("t/A.java", "package t; public class A { static class S { private static void f() {} } private class F { private void t() { A.S.f(); t();} } }"));
        performInnerToOuterTest("outer");
        verifyContent(src,
                      new File("t/F.java", "/* * Refactoring License */ package t; /** * * @author junit */ class F { private final A outer; F(final A outer) { this.outer = outer; }\n private void t() { A.S.f();  t(); } }\n"),//TODO: why outer reference?
                      new File("t/A.java", "package t; public class A { static class S { private static void f() {} } }"));
    }

    public void test138204c() throws Exception {
        writeFilesAndWaitForScan(src,
                                 new File("t/A.java", "package t; public class A { private static class S { private static void f() {} } private class F { private void t() {S.f();} } }"));
        performInnerToOuterTest("outer", new Problem(false, "WRN_InnerToOuterRefToPrivate/t.A.S"));
        verifyContent(src,
                      new File("t/F.java", "/* * Refactoring License */ package t; /** * * @author junit */ class F { private final A outer; F(final A outer) { this.outer = outer; }\n private void t() { A.S.f(); } }\n"),//TODO: why outer reference?
                      new File("t/A.java", "package t; public class A { private static class S { private static void f() {} } }"));
    }

    public void test180364() throws Exception {
        writeFilesAndWaitForScan(src,
                                 new File("t/A.java", "package t; public class A { int i; static class F extends A { private void t() { i = 0; } } }"));
        performInnerToOuterTest(null);
        verifyContent(src,
                      new File("t/F.java", "/* * Refactoring License */ package t; /** * * @author junit */ class F extends A {  private void t() { i = 0; } }\n"),//TODO: why outer reference?
                      new File("t/A.java", "package t; public class A { int i; }"));
    }
    
    public void test144209() throws Exception {
        writeFilesAndWaitForScan(src,
                                 new File("t/A.java",
                                          "package t;\n" +
                                          "\n" +
                                          "import java.awt.event.ActionEvent;\n" +
                                          "import java.awt.event.MouseEvent;\n" +
                                          "import javax.swing.AbstractAction;\n" +
                                          "\n" +
                                          "public class Outer {\n" +
                                          "\n" +
                                          "    static void refresh() {\n" +
                                          "    }\n" +
                                          "\n" +
                                          "    /**\n" +
                                          "     * javadoc comment for F.\n" +
                                          "     */\n" +
                                          "    static class F {\n" +
                                          "        void refresh() {\n" +
                                          "            //Outer.refresh();\n" +
                                          "        }\n" +
                                          "\n" +
                                          "        /**\n" +
                                          "         * javadoc for F.handler\n" +
                                          "         * @param e\n" +
                                          "         */\n" +
                                          "        void handler(MouseEvent e) {\n" +
                                          "            new InnerInner();\n" +
                                          "        }\n" +
                                          "\n" +
                                          "        private void someInnerMethod() {\n" +
                                          "            // test comment\n" +
                                          "            System.err.println(\"in inner method\");\n" +
                                          "        }\n" +
                                          "\n" +
                                          "        /**\n" +
                                          "         * javadoc comment for InnerInner\n" +
                                          "         */\n" +
                                          "        private class InnerInner extends AbstractAction {\n" +
                                          "\n" +
                                          "            /* coment with '*' */\n" +
                                          "\n" +
                                          "            @Override\n" +
                                          "            public void actionPerformed(ActionEvent e) {\n" +
                                          "                someInnerMethod();\n" +
                                          "            }\n" +
                                          "\n" +
                                          "        }\n" +
                                          "    }\n" +
                                          "\n" +
                                          "}\n"));
        performInnerToOuterTest(null);
        verifyContent(src,
                                 new File("t/A.java",
                                          "package t;\n" +
                                          "\n" +
                                          "import java.awt.event.ActionEvent;\n" +
                                          "import java.awt.event.MouseEvent;\n" +
                                          "import javax.swing.AbstractAction;\n" +
                                          "\n" +
                                          "public class Outer {\n" +
                                          "\n" +
                                          "    static void refresh() {\n" +
                                          "    }\n" +
                                          "\n" +
                                          "}\n"),
                                 new File("t/F.java",
                                          "/* * Refactoring License */ package t;\n" +
                                          "\n" +
                                          "import java.awt.event.ActionEvent;\n" +
                                          "import java.awt.event.MouseEvent;\n" +
                                          "import javax.swing.AbstractAction;\n" +
//                                          " /** * * @author junit */\n" +
                                          "/**\n" +
                                          " * javadoc comment for F.\n" +
                                          " */\n" +
                                          "class F {\n" +
                                          "    void refresh() {\n" +
                                          "        //Outer.refresh();\n" +
                                          "    }\n" +
                                          "\n" +
                                          "    /**\n" +
                                          "     * javadoc for F.handler\n" +
                                          "     * @param e\n" +
                                          "     */\n" +
                                          "    void handler(MouseEvent e) {\n" +
                                          "        new InnerInner();\n" +
                                          "    }\n" +
                                          "\n" +
                                          "    private void someInnerMethod() {\n" +
                                          "        // test comment\n" +
                                          "        System.err.println(\"in inner method\");\n" +
                                          "    }\n" +
                                          "\n" +
                                          "    /**\n" +
                                          "     * javadoc comment for InnerInner\n" +
                                          "     */\n" +
                                          "    private class InnerInner extends AbstractAction {\n" +
                                          "\n" +
                                          "        /* coment with '*' */\n" +
                                          "\n" +
                                          "        @Override\n" +
                                          "        public void actionPerformed(ActionEvent e) {\n" +
                                          "            someInnerMethod();\n" +
                                          "        }\n" +
                                          "\n" +
                                          "    }\n" +
                                          "}\n"));
    }

    public void test187766() throws Exception {
        writeFilesAndWaitForScan(src,
                                 new File("t/A.java", "package t; public class A { int i; public enum F { A, B, C; } }"));
        performInnerToOuterTest(null);
        verifyContent(src,
                      new File("t/F.java", "/* * Refactoring License */ package t; /** * * @author junit */ public enum F {  A, B, C }\n"),
                      new File("t/A.java", "package t; public class A { int i; }"));
    }
    
    public void test198186() throws Exception {
        writeFilesAndWaitForScan(src,
                new File("t/A.java", "/*\n"
                + " * To change this template, choose Tools | Templates\n"
                + " * and open the template in the editor.\n"
                + " */\n"
                + "package t;\n"
                + "\n"
                + "/**\n"
                + " *\n"
                + " * @author Ernie Rael <err at raelity.com>\n"
                + " */\n"
                + "public class A {\n"
                + "    ChangeNotify changeNotify;\n"
                + "\n"
                + "    public A(ChangeNotify changeNotify)\n"
                + "    {\n"
                + "        this.changeNotify = changeNotify;\n"
                + "    }\n"
                + "\n"
                + "    void foo() {\n"
                + "        StartAsNested n = new StartAsNested();\n"
                + "    }\n"
                + "\n"
                + "    public static interface ChangeNotify {\n"
                + "        public void change();\n"
                + "    }\n"
                + "    public static interface ForDebug {\n"
                + "        public void iFunc();\n"
                + "    }\n"
                + "\n"
                + "    class StartAsNested {\n"
                + "\n"
                + "        public StartAsNested()\n"
                + "        {\n"
                + "            ForDebug pcl;\n"
                + "            pcl = new ForDebug() {\n"
                + "                @Override\n"
                + "                public void iFunc() {\n"
                + "                    changeNotify.change();\n"
                + "                }\n"
                + "            };\n"
                + "        }\n"
                + "\n"
                + "        void func1()\n"
                + "        {\n"
                + "            C1 c = new MyC1(1);\n"
                + "        }\n"
                + "\n"
                + "        class MyC1 extends C1\n"
                + "        {\n"
                + "            public MyC1(int i)\n"
                + "            {\n"
                + "                super(i);\n"
                + "            }\n"
                + "        }\n"
                + "\n"
                + "        class C1\n"
                + "        {\n"
                + "            int i;\n"
                + "\n"
                + "            public C1()\n"
                + "            {\n"
                + "            }\n"
                + "\n"
                + "            public C1(int i)\n"
                + "            {\n"
                + "                this();\n"
                + "                this.i = i;\n"
                + "            }\n"
                + "\n"
                + "            @Override\n"
                + "            protected Object clone() throws CloneNotSupportedException\n"
                + "            {\n"
                + "                C1 c1 = new C1(i);\n"
                + "                return c1;\n"
                + "            }\n"
                + "        }\n"
                + "\n"
                + "    }\n"
                + "}\n"));
        performInnerToOuterTest("outer");
        verifyContent(src,
                new File("t/StartAsNested.java", "/* * Refactoring License */ package t; /** * * @author junit */\n"
                + "\n"
                + "class StartAsNested {\n"
                + "\n"
                + "    private final A outer;\n"
                + "\n"
                + "    public StartAsNested(final A outer) {\n"
                + "        this.outer = outer;\n"
                + "        A.ForDebug pcl;\n"
                + "        pcl = new A.ForDebug() {\n"
                + "\n"
                + "            @Override\n"
                + "            public void iFunc() {\n"
                + "                outer.changeNotify.change();\n"
                + "            }\n"
                + "        };\n"
                + "    }\n"
                + "\n"
                + "    void func1() {\n"
                + "        C1 c = new MyC1(1);\n"
                + "    }\n"
                + "\n"
                + "    class MyC1 extends C1 {\n"
                + "\n"
                + "        public MyC1(int i) {\n"
                + "            super(i);\n"
                + "        }\n"
                + "    }\n"
                + "\n"
                + "    class C1 {\n"
                + "\n"
                + "        int i;\n"
                + "\n"
                + "        public C1() {\n"
                + "        }\n"
                + "\n"
                + "        public C1(int i) {\n"
                + "            this();\n"
                + "            this.i = i;\n"
                + "        }\n"
                + "\n"
                + "        @Override\n"
                + "        protected Object clone() throws CloneNotSupportedException {\n"
                + "            C1 c1 = new C1(i);\n"
                + "            return c1;\n"
                + "        }\n"
                + "    }\n"
                + "}\n"
                + "\n"),
                new File("t/A.java", "/*\n"
                + " * To change this template, choose Tools | Templates\n"
                + " * and open the template in the editor.\n"
                + " */\n"
                + "package t;\n"
                + "\n"
                + "/**\n"
                + " *\n"
                + " * @author Ernie Rael <err at raelity.com>\n"
                + " */\n"
                + "public class A {\n"
                + "    ChangeNotify changeNotify;\n"
                + "\n"
                + "    public A(ChangeNotify changeNotify)\n"
                + "    {\n"
                + "        this.changeNotify = changeNotify;\n"
                + "    }\n"
                + "\n"
                + "    void foo() {\n"
                + "        StartAsNested n = new StartAsNested(this);\n"
                + "    }\n"
                + "\n"
                + "    public static interface ChangeNotify {\n"
                + "        public void change();\n"
                + "    }\n"
                + "    public static interface ForDebug {\n"
                + "        public void iFunc();\n"
                + "    }\n"
                + "}\n"));
    }

    public void test177996() throws Exception {
        writeFilesAndWaitForScan(src,
                                 new File("t/A.java", "package t; public class A { public void t() { A t = new A(); Inner inner = t.new Inner(); } class Inner { }}"));
        performInnerToOuterTest("outer");
        verifyContent(src,
                      new File("t/Inner.java", "/* * Refactoring License */ package t; /** * * @author junit */ class Inner { private final A outer; Inner(final A outer) { this.outer = outer; } } "),
                      new File("t/A.java", "package t; public class A { public void t() { A t = new A(); Inner inner = new Inner(t); } }"));
    }
    
    public void test206086() throws Exception {
        writeFilesAndWaitForScan(src,
                                 new File("t/A.java", "package t; public class A { class B { } class F { /** jdoc */ B b; } }"));
        performInnerToOuterTest("outer");
        verifyContent(src,
                      new File("t/F.java", "/* * Refactoring License */ package t; /** * * @author junit */ class F { /** jdoc */ A.B b; private final A outer; F(final A outer) { this.outer = outer; } } "),
                      new File("t/A.java", "package t; public class A { class B { } }"));
    }
    
    public void test119419() throws Exception {
        writeFilesAndWaitForScan(src,
                                 new File("t/A.java", "package t; public class A {\n enum B { A(0); private B(int i) { System.err.println(i); } }\n}"));
        performInnerToOuterTest(null);
        verifyContent(src,
                      new File("t/A.java", "package t; public class A { }"),
                      new File("t/B.java", "/* * Refactoring License */ package t; /** * * @author junit */ enum B { A(0); private B(int i) { System.err.println(i); } } "));
    }

    // If types are omitted on lambda parameters (IMPLICIT paramKind), then the
    // generated lambda source code should also omit parameter types.
    public void testNETBEANS345() throws Exception {
        writeFilesAndWaitForScan(src,
                                 new File("t/A.java", "package t;\n" +
                                                      "\n" +
                                                      "import java.util.List;\n" +
                                                      "import java.util.concurrent.RunnableFuture;\n" +
                                                      "\n" +
                                                      "public class A {\n" +
                                                      "    public static class B {\n" +
                                                      "        public B(List<? extends Runnable> runnables) {\n" +
                                                      "            assert runnables.stream().noneMatch((r) -> r instanceof RunnableFuture);\n" +
                                                      "        }\n" +
                                                      "    }\n" +
                                                      "}"));
        performInnerToOuterTest(null);
        verifyContent(src,
                      new File("t/A.java", "package t;\n" +
                                           "\n" +
                                           "import java.util.List;\n" +
                                           "import java.util.concurrent.RunnableFuture;\n" +
                                           "\n" +
                                           "public class A {\n" +
                                           "}"),
                      new File("t/B.java", "/* * Refactoring License */ package t;\n" +
                                           "\n" +
                                           "import java.util.List;\n" +
                                           "import java.util.concurrent.RunnableFuture;\n" +
                                           "\n" +
                                           "/** * * @author junit */ public class B {\n" +
                                           "    public B(List<? extends Runnable> runnables) {\n" +
                                           "        assert runnables.stream().noneMatch(r -> r instanceof RunnableFuture);\n" + //TODO: note the conversion of "(r)" to "r" - would be better if the form would be kept
                                           "    }\n" +
                                           "} "));
    }

    private void performInnerToOuterTest(String outerNameInInner, Problem... expectedProblems) throws Exception {
        final InnerToOuterRefactoring[] r = new InnerToOuterRefactoring[1];

        JavaSource.forFileObject(src.getFileObject("t/A.java")).runUserActionTask(new Task<CompilationController>() {

            @Override
            public void run(CompilationController parameter) throws Exception {
                parameter.toPhase(JavaSource.Phase.RESOLVED);
                CompilationUnitTree cut = parameter.getCompilationUnit();
                
                ClassTree outter = (ClassTree) cut.getTypeDecls().get(0);
                ClassTree inner = (ClassTree) outter.getMembers().get(outter.getMembers().size() - 1);

                TreePath tp = TreePath.getPath(cut, inner);
                r[0] = new InnerToOuterRefactoring(TreePathHandle.create(tp, parameter));
            }
        }, true);

        r[0].setClassName("F");
        r[0].setReferenceName(outerNameInInner);

        RefactoringSession rs = RefactoringSession.create("Session");
        List<Problem> problems = new LinkedList<Problem>();

        addAllProblems(problems, r[0].preCheck());
        addAllProblems(problems, r[0].prepare(rs));
        addAllProblems(problems, rs.doRefactoring(true));

        assertProblems(Arrays.asList(expectedProblems), problems);
    }
    
    private void performInnerToOuterTest(String generateOuter, final int position, Problem... expectedProblems) throws Exception {
        final InnerToOuterRefactoring[] r = new InnerToOuterRefactoring[1];

        JavaSource.forFileObject(src.getFileObject("t/A.java")).runUserActionTask(new Task<CompilationController>() {

            @Override
            public void run(CompilationController parameter) throws Exception {
                parameter.toPhase(JavaSource.Phase.RESOLVED);
                TreePath tp = parameter.getTreeUtilities().pathFor(position);
                r[0] = new InnerToOuterRefactoring(TreePathHandle.create(tp, parameter));
            }
        }, true);

        r[0].setClassName("F");
        r[0].setReferenceName(generateOuter);

        RefactoringSession rs = RefactoringSession.create("Session");
        List<Problem> problems = new LinkedList<>();

        addAllProblems(problems, r[0].preCheck());
        addAllProblems(problems, r[0].prepare(rs));
        addAllProblems(problems, rs.doRefactoring(true));

        assertProblems(Arrays.asList(expectedProblems), problems);
    }

}
