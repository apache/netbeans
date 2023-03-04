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
package org.netbeans.modules.refactoring.java.test;

import com.sun.source.tree.*;
import com.sun.source.util.TreePath;
import java.io.IOException;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import org.netbeans.api.java.source.CompilationController;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.Task;
import org.netbeans.api.java.source.TreePathHandle;
import org.netbeans.modules.java.source.parsing.JavacParser;
import org.netbeans.modules.refactoring.api.Problem;
import org.netbeans.modules.refactoring.api.RefactoringSession;
import org.netbeans.modules.refactoring.java.api.InlineRefactoring;
import org.openide.filesystems.FileObject;

/**
 *
 * @author Ralph Ruijs
 */
public class InlineTest extends RefactoringTestBase {

    public InlineTest(String name) {
        super(name, "1.8");
    }
    
    static {
        JavacParser.DISABLE_SOURCE_LEVEL_DOWNGRADE = true;
    }

    public void test258579b() throws Exception {
        writeFilesAndWaitForScan(src,
                new File("t/A.java", "package t;\n"
                        + "public class A {\n"
                        + "    public static void main(String[] args) {\n"
                        + "        for (foo(); ; foo(), foo()) { }\n"
                        + "    }\n"
                        + "    private void foo() {\n"
                        + "    }\n"
                        + "}\n"));
        final InlineRefactoring[] r = new InlineRefactoring[1];
        createInlineMethodRefactoring(src.getFileObject("t/A.java"), 2, r);
        performRefactoring(r);
        verifyContent(src,
                new File("t/A.java", "package t;\n"
                        + "public class A {\n"
                        + "    public static void main(String[] args) {\n"
                        + "        for (; ; ) { }\n"
                        + "    }\n"
                        + "}\n"));
    }
    
    public void test258579a() throws Exception {
        writeFilesAndWaitForScan(src,
                new File("t/A.java", "package t;\n"
                        + "public class A {\n"
                        + "    String greet = \"\";\n"
                        + "    public static void main(String[] args) {\n"
                        + "        new A().foo(\"\");\n"
                        + "    }\n"
                        + "    private void foo(String msg) {\n"
                        + "    }\n"
                        + "    private class Inner {\n"
                        + "        public void bar() {\n"
                        + "            foo(\"\");\n"
                        + "        }\n"
                        + "    }\n"
                        + "}\n"));
        final InlineRefactoring[] r = new InlineRefactoring[1];
        createInlineMethodRefactoring(src.getFileObject("t/A.java"), 3, r);
        performRefactoring(r);
        verifyContent(src,
                new File("t/A.java", "package t;\n"
                        + "public class A {\n"
                        + "    String greet = \"\";\n"
                        + "    public static void main(String[] args) {\n"
                        + "    }\n"
                        + "    private class Inner {\n"
                        + "        public void bar() {\n"
                        + "        }\n"
                        + "    }\n"
                        + "}\n"));
    }
    
    public void test216817() throws Exception {
        writeFilesAndWaitForScan(src,
                new File("t/A.java", "package t;\n"
                        + "public class A {\n"
                        + "    String greet = \"\";\n"
                        + "    public static void main(String[] args) {\n"
                        + "        new A().foo(\"\");\n"
                        + "    }\n"
                        + "    private void foo(String msg) {\n"
                        + "        System.out.println(msg + greet);\n"
                        + "    }\n"
                        + "    private class Inner {\n"
                        + "        public void bar() {\n"
                        + "            foo(\"\");\n"
                        + "        }\n"
                        + "    }\n"
                        + "}\n"));
        final InlineRefactoring[] r = new InlineRefactoring[1];
        createInlineMethodRefactoring(src.getFileObject("t/A.java"), 3, r);
        performRefactoring(r);
        verifyContent(src,
                new File("t/A.java", "package t;\n"
                        + "public class A {\n"
                        + "    String greet = \"\";\n"
                        + "    public static void main(String[] args) {\n"
                        + "        System.out.println(\"\" + new A().greet);\n"
                        + "    }\n"
                        + "    private class Inner {\n"
                        + "        public void bar() {\n"
                        + "            System.out.println(\"\" + greet);\n"
                        + "        }\n"
                        + "    }\n"
                        + "}\n"));
    }
    
    public void test242995() throws Exception {
        writeFilesAndWaitForScan(src,
                new File("t/A.java", "package t;\n"
                        + "public class A {\n"
                        + "    public static boolean toBeInlined() {\n"
                        + "        return toBeMatched() == 24;\n"
                        + "    }\n"
                        + "\n"
                        + "    public static Integer toBeMatched() {\n"
                        + "        return 12;\n"
                        + "    }\n"
                        + "\n"
                        + "    public static void main(String[] args) {\n"
                        + "        if(!toBeInlined()) {\n"
                        + "            System.out.println(\"true\");\n"
                        + "        }\n"
                        + "    }\n"
                        + "}\n"));
        final InlineRefactoring[] r = new InlineRefactoring[1];
        createInlineMethodRefactoring(src.getFileObject("t/A.java"), 1, r);
        performRefactoring(r);
        verifyContent(src,
                new File("t/A.java", "package t;\n"
                        + "public class A {\n"
                        + "    public static Integer toBeMatched() {\n"
                        + "        return 12;\n"
                        + "    }\n"
                        + "\n"
                        + "    public static void main(String[] args) {\n"
                        + "        if(!(toBeMatched() == 24)) {\n"
                        + "            System.out.println(\"true\");\n"
                        + "        }\n"
                        + "    }\n"
                        + "}\n"));
    }
    
    public void test236447a() throws Exception {
        writeFilesAndWaitForScan(src,
                new File("t/A.java", "package t;\n"
                        + "public class A {\n"
                        + "    private void test() {\n"
                        + "        inline();\n"
                        + "    }\n"
                        + "\n"
                        + "    private void inline() {\n"
                        + "\n"
                        + "        System.out.println(\"Test1\");\n"
                        + "\n"
                        + "        //Hmmm What now?\n"
                        + "        System.out.println(\"Here\");\n"
                        + "    }\n"
                        + "}"));
        final InlineRefactoring[] r = new InlineRefactoring[1];
        createInlineMethodRefactoring(src.getFileObject("t/A.java"), 2, r);
        performRefactoring(r);
        verifyContent(src,
                new File("t/A.java", "package t;\n"
                        + "public class A {\n"
                        + "    private void test() {\n"
                        + "\n"
                        + "        System.out.println(\"Test1\");\n"
                        + "\n"
                        + "        //Hmmm What now?\n"
                        + "        System.out.println(\"Here\");\n"
                        + "    }\n"
                        + "}"));
    }
    
    public void test236447b() throws Exception {
        writeFilesAndWaitForScan(src,
                new File("t/A.java", "package t;\n"
                        + "public class A {\n"
                        + "    private void bar() {\n"
                        + "        //TODO barbar\n"
                        + "        foo();\n"
                        + "        //TODO barbar\n"
                        + "    }\n"
                        + "    \n"
                        + "    private void foo() {\n"
                        + "        //TODO blabla\n"
                        + "        System.out.println(\"this = \" + this);\n"
                        + "        //TODO blabla\n"
                        + "    }\n"
                        + "}"));
        final InlineRefactoring[] r = new InlineRefactoring[1];
        createInlineMethodRefactoring(src.getFileObject("t/A.java"), 2, r);
        performRefactoring(r);
        verifyContent(src,
                new File("t/A.java", "package t;\n"
                        + "public class A {\n"
                        + "    private void bar() {\n"
                        + "        //TODO barbar\n"
                        + "        //TODO blabla\n"
                        + "        System.out.println(\"this = \" + this);\n"
                        + "        //TODO blabla\n"
                        + "        //TODO barbar\n"
                        + "    }\n"
                        + "    \n"
                        + "}"));
    }
    
    public void test238831() throws Exception {
        writeFilesAndWaitForScan(src,
                new File("t/A.java", "package t;\n"
                        + "public class A {\n"
                        + "    public boolean kkk(int x) {\n"
                        + "        return true;\n"
                        + "    }\n"
                        + "\n"
                        + "    public void test() {\n"
                        + "        method(this::kkk);\n"
                        + "    }\n"
                        + "\n"
                        + "    public void method(FIface in) {\n"
                        + "        \n"
                        + "    }\n"
                        + "\n"
                        + "    interface FIface {\n"
                        + "        boolean test(int a);\n"
                        + "    }\n"
                        + "}\n"));
        final InlineRefactoring[] r = new InlineRefactoring[1];
        createInlineMethodRefactoring(src.getFileObject("t/A.java"), 1, r);
        performRefactoring(r);
        verifyContent(src,
                new File("t/A.java", "package t;\n"
                        + "public class A {\n"
                        + "    public void test() {    \n"
                        + "        method((int x) -> { return true; });\n"
                        + "    }\n"
                        + "\n"
                        + "    public void method(FIface in) {\n"
                        + "    }\n"
                        + "\n"
                        + "    interface FIface {\n"
                        + "        boolean test(int a);\n"
                        + "    }\n"
                        + "}\n"));
    }
    
    public void test231631a() throws Exception {
        writeFilesAndWaitForScan(src,
                new File("t/A.java", "package t;\n"
                + "public class A {\n"
                + "    public void test(int x) {\n"
                + "        if(x>3) x = 0;\n"
                + "    }\n"
                + "    public void usage() {\n"
                + "        test(2);\n"
                + "    }\n"
                + "}"));
        final InlineRefactoring[] r = new InlineRefactoring[1];
        createInlineMethodRefactoring(src.getFileObject("t/A.java"), 1, r);
        performRefactoring(r);
        verifyContent(src,
                new File("t/A.java", "package t;\n"
                + "public class A {\n"
                + "    public void usage() {\n"
                + "        int x = 2;\n"
                + "        if (x>3) { x = 0; }\n"
                + "    }\n"
                + "}"));
    }
    
    public void test231631b() throws Exception {
        writeFilesAndWaitForScan(src,
                new File("t/A.java", "package t;\n"
                + "public class A {\n"
                + "    public void test(int x) {\n"
                + "        if(x>3) x = 0;\n"
                + "    }\n"
                + "    public void usage(int x) {\n"
                + "        test(2);\n"
                + "    }\n"
                + "}"));
        final InlineRefactoring[] r = new InlineRefactoring[1];
        createInlineMethodRefactoring(src.getFileObject("t/A.java"), 1, r);
        performRefactoring(r);
        verifyContent(src,
                new File("t/A.java", "package t;\n"
                + "public class A {\n"
                + "    public void usage(int x) {\n"
                + "        int x1 = 2;\n"
                + "        if (x1 > 3) { x1 = 0; }\n"
                + "    }\n"
                + "}"));
    }
    
    public void test232211a() throws Exception {
        writeFilesAndWaitForScan(src,
                new File("t/A.java", "package t;\n"
                + "public class A {\n"
                + "    private String getGreeting(String... names) {\n"
                + "        StringBuilder builder = new StringBuilder();\n"
                + "        for (String name : names) {\n"
                + "            builder.append(\"Hello \").append(name).append(\"!\");\n"
                + "        }\n"
                + "        return builder.toString();\n"
                + "    }\n"
                + "    public void testMethod() {\n"
                + "        System.out.println(getGreeting());\n"
                + "    }\n"
                + "}"));
        final InlineRefactoring[] r = new InlineRefactoring[1];
        createInlineMethodRefactoring(src.getFileObject("t/A.java"), 1, r);
        performRefactoring(r);
        verifyContent(src,
                new File("t/A.java", "package t;\n"
                + "public class A {\n"
                + "    public void testMethod() {\n"
                + "        String[] names = new String[]{};\n"
                + "        StringBuilder builder = new StringBuilder();\n"
                + "        for (String name : names) {\n"
                + "            builder.append(\"Hello \").append(name).append(\"!\");\n"
                + "        }\n"
                + "        System.out.println(builder.toString());\n"
                + "    }\n"
                + "}"));
    }
    
    public void test232211b() throws Exception {
        writeFilesAndWaitForScan(src,
                new File("t/A.java", "package t;\n"
                + "public class A {\n"
                + "    private String getGreeting(String... names) {\n"
                + "        StringBuilder builder = new StringBuilder();\n"
                + "        for (String name : names) {\n"
                + "            builder.append(\"Hello \").append(name).append(\"!\");\n"
                + "        }\n"
                + "        return builder.toString();\n"
                + "    }\n"
                + "    public void testMethod() {\n"
                + "        System.out.println(getGreeting(\"World\", \"Moon\"));\n"
                + "    }\n"
                + "}"));
        final InlineRefactoring[] r = new InlineRefactoring[1];
        createInlineMethodRefactoring(src.getFileObject("t/A.java"), 1, r);
        performRefactoring(r);
        verifyContent(src,
                new File("t/A.java", "package t;\n"
                + "public class A {\n"
                + "    public void testMethod() {\n"
                + "        String[] names = new String[]{\"World\", \"Moon\"};\n"
                + "        StringBuilder builder = new StringBuilder();\n"
                + "        for (String name : names) {\n"
                + "            builder.append(\"Hello \").append(name).append(\"!\");\n"
                + "        }\n"
                + "        System.out.println(builder.toString());\n"
                + "    }\n"
                + "}"));
    }
    
    public void test233082() throws Exception {
        writeFilesAndWaitForScan(src,
                new File("t/A.java", "package t;\n"
                + "public class A {\n"
                + "     public String concat(String s1, String s2) {\n"
                + "        System.out.println(\"Concatenating\"+s1+s2);\n"
                + "        String r= s1+s2; \n"
                + "        return r;\n"
                + "    }\n"
                + "}"),
                new File("t/B.java", "package t;\n"
                + "public class B {\n"
                + "    public void testMethodB(A a) {\n"
                + "        String con = a.concat(\"Hello\", a.concat(\" \", \"world\"));\n"
                + "    }\n"
                + "}"));
        final InlineRefactoring[] r = new InlineRefactoring[1];
        createInlineMethodRefactoring(src.getFileObject("t/A.java"), 1, r);
        performRefactoring(r);
        verifyContent(src,
                new File("t/A.java", "package t;\n"
                + "public class A {\n"
                + "}"),
                new File("t/B.java", "package t;\n"
                + "public class B {\n"
                + "    public void testMethodB(A a) {\n"
                + "        System.out.println(\"Concatenating\" + \" \" + \"world\");\n"
                + "        String r = \" \" + \"world\";\n"
                + "        System.out.println(\"Concatenating\" + \"Hello\" + r);\n"
                + "        String r1 = \"Hello\" + r;\n"
                + "        String con = r1;\n"
                + "    }\n"
                + "}"));
    }
    
    public void test228769() throws Exception {
        writeFilesAndWaitForScan(src,
                new File("t/A.java", "package t;\n"
                + "public class A {\n"
                + "     public String concat(String a, String b) {\n"
                + "        return a+\":\"+b;\n"
                + "    }\n"
                + "}"),
                new File("t/B.java", "package t;\n"
                + "public class B {\n"
                + "    public void testMethodB(A a) {\n"
                + "        a.concat(\"1\", a.concat(\"2\", \"3\"));\n"
                + "    }\n"
                + "}"));
        final InlineRefactoring[] r = new InlineRefactoring[1];
        createInlineMethodRefactoring(src.getFileObject("t/A.java"), 1, r);
        performRefactoring(r, new Problem(false, "WRN_InlineChangeReturn"));
        verifyContent(src,
                new File("t/A.java", "package t;\n"
                + "public class A {\n"
                + "}"),
                new File("t/B.java", "package t;\n"
                + "public class B {\n"
                + "    public void testMethodB(A a) {\n"
                + "    }\n"
                + "}"));
    }
    
    public void test228771() throws Exception {
        writeFilesAndWaitForScan(src,
                new File("t/A.java", "package t;\n"
                + "public class A {\n"
                + "    public String getName(int x, int y) {\n"
                + "        int local = 1;\n"
                + "        System.out.println(\"method called\");\n"
                + "        return \" \"+x;\n"
                + "    }\n"
                + "    public void usage() {\n"
                + "        String name = getName(1, 3);\n"
                + "        if(Math.random()>1) {\n"
                + "            name = getName(1, 3);\n"
                + "        }\n"
                + "    }\n"
                + "}"));
        final InlineRefactoring[] r = new InlineRefactoring[1];
        createInlineMethodRefactoring(src.getFileObject("t/A.java"), 1, r);
        performRefactoring(r);
        verifyContent(src,
                new File("t/A.java", "package t;\n"
                + "public class A {\n"
                + "    public void usage() {\n"
                + "        int local = 1;\n"
                + "        System.out.println(\"method called\");\n"
                + "        String name = \" \" + 1;\n"
                + "        if(Math.random()>1) {\n"
                + "            int local1 = 1;\n"
                + "            System.out.println(\"method called\");\n"
                + "            name = \" \" + 1;\n"
                + "        }\n"
                + "    }\n"
                + "}"));
    }
    
    public void test228772a() throws Exception {
        writeFilesAndWaitForScan(src,
                new File("t/A.java", "package t;\n"
                + "public class A {\n"
                + "     public String concat(String a, String b) {\n"
                + "        return a+\":\"+b;\n"
                + "    }\n"
                + "}"),
                new File("t/B.java", "package t;\n"
                + "public class B {\n"
                + "    public void testMethodB(A a) {\n"
                + "        String s = a.concat(\"1\", a.concat(\"2\", \"3\"));\n"
                + "    }\n"
                + "}"));
        final InlineRefactoring[] r = new InlineRefactoring[1];
        createInlineMethodRefactoring(src.getFileObject("t/A.java"), 1, r);
        performRefactoring(r);
        verifyContent(src,
                new File("t/A.java", "package t;\n"
                + "public class A {\n"
                + "}"),
                new File("t/B.java", "package t;\n"
                + "public class B {\n"
                + "    public void testMethodB(A a) {\n"
                + "        String s = \"1\" + \":\" + \"2\" + \":\" + \"3\";\n"
                + "    }\n"
                + "}"));
    }
    
    public void test228772b() throws Exception {
        writeFilesAndWaitForScan(src,
                new File("t/A.java", "package t;\n"
                + "public class A {\n"
                + "     public String concat(String a, String b) {\n"
                + "        String value = a+\":\"+b;\n"
                + "        return value;\n"
                + "    }\n"
                + "}"),
                new File("t/B.java", "package t;\n"
                + "public class B {\n"
                + "    public void testMethodB(A a) {\n"
                + "        String s = a.concat(\"1\", a.concat(\"2\", \"3\"));\n"
                + "    }\n"
                + "}"));
        final InlineRefactoring[] r = new InlineRefactoring[1];
        createInlineMethodRefactoring(src.getFileObject("t/A.java"), 1, r);
        performRefactoring(r);
        verifyContent(src,
                new File("t/A.java", "package t;\n"
                + "public class A {\n"
                + "}"),
                new File("t/B.java", "package t;\n"
                + "public class B {\n"
                + "    public void testMethodB(A a) {\n"
                + "        String value = \"2\" + \":\" + \"3\";\n"
                + "        String value1 = \"1\" + \":\" + value;\n"
                + "        String s = value1;\n"
                + "    }\n"
                + "}"));
    }
    
    public void test228776() throws Exception {
        writeFilesAndWaitForScan(src,
                new File("t/A.java", "package t;\n"
                + "public interface A {\n"
                + "    void printGreeting();\n"
                + "}"),
                new File("t/B.java", "package t;\n"
                + "public class B {\n"
                + "    public void testMethodB(A a) {\n"
                + "        if(true)\n"
                + "            a.printGreeting();\n"
                + "    }\n"
                + "}"));
        final InlineRefactoring[] r = new InlineRefactoring[1];
        createInlineMethodRefactoring(src.getFileObject("t/A.java"), 0, r);
        performRefactoring(r, new Problem(true, "ERR_InlineMethodInInterface"));
    }
    
    public void test222917() throws Exception {
        writeFilesAndWaitForScan(src,
                new File("t/A.java", "package t;\n"
                + "public class A {\n"
                + "    public static void printGreeting() {\n"
                + "        java.lang.System.out.println(\"Hello World!\");\n"
                + "    }\n"
                + "}"),
                new File("t/B.java", "package t;\n"
                + "public class B {\n"
                + "    public void testMethodB() {\n"
                + "        if(true)\n"
                + "            A.printGreeting();\n"
                + "    }\n"
                + "}"));
        final InlineRefactoring[] r = new InlineRefactoring[1];
        createInlineMethodRefactoring(src.getFileObject("t/A.java"), 1, r);
        performRefactoring(r);
        verifyContent(src,
                new File("t/A.java", "package t;\n"
                + "public class A {\n"
                + "}"),
                new File("t/B.java", "package t;\n"
                + "public class B {\n"
                + "    public void testMethodB() {\n"
                + "        if(true) {\n"
                + "            System.out.println(\"Hello World!\");\n"
                + "        }\n"
                + "    }\n"
                + "}"));
    }
    
    public void test215787() throws Exception {
        writeFilesAndWaitForScan(src,
                new File("t/A.java", "package t;\n"
                + "public class A {\n"
                + "    public static void printGreeting() {\n"
                + "        A.A();\n"
                + "    }\n"
                + "    public void testMethod() {\n"
                + "        if(true)\n"
                + "            A.printGreeting();\n"
                + "    }\n"
                + "}"),
                new File("t/B.java", "package t;\n"
                + "public class B {\n"
                + "    public void testMethodB() {\n"
                + "        if(true)\n"
                + "            A.printGreeting();\n"
                + "    }\n"
                + "}"));
        final InlineRefactoring[] r = new InlineRefactoring[1];
        createInlineMethodRefactoring(src.getFileObject("t/A.java"), 1, r);
        performRefactoring(r);
        verifyContent(src,
                new File("t/A.java", "package t;\n"
                + "public class A {\n"
                + "    public void testMethod() {\n"
                + "        if(true) {\n"
                + "            A.A();\n"
                + "        }\n"
                + "    }\n"
                + "}"),
                new File("t/B.java", "package t;\n"
                + "public class B {\n"
                + "    public void testMethodB() {\n"
                + "        if(true) {\n"
                + "            A.A();\n"
                + "        }\n"
                + "    }\n"
                + "}"));
    }
    
    public void test211356() throws Exception { // #211356 - java.util.NoSuchElementException at java.util.LinkedList.getLast
        writeFilesAndWaitForScan(src,
                new File("t/A.java", "package t;\n"
                + "public class A {\n"
                + "    private static final String message = true? getMessage(\"KEY\") : null;\n"
                + "    private static String getMessage(String key) {\n"
                + "        return key;\n"
                + "    }\n"
                + "    public void testMethod() {\n"
                + "        System.out.println(message);\n"
                + "    }\n"
                + "}"));
        final InlineRefactoring[] r = new InlineRefactoring[1];
        createInlineMethodRefactoring(src.getFileObject("t/A.java"), 2, r);
        performRefactoring(r);
        verifyContent(src,
                new File("t/A.java", "package t;\n"
                + "public class A {\n"
                + "    private static final String message = true? \"KEY\" : null;\n"
                + "    public void testMethod() {\n"
                + "        System.out.println(message);\n"
                + "    }\n"
                + "}"));
    }
    
    public void test210942() throws Exception {
        writeFilesAndWaitForScan(src,
                new File("t/A.java", "package t;\n"
                + "public class A {\n"
                + "    public static void printGreeting() {\n"
                + "    }\n"
                + "    public void testMethod() {\n"
                + "        if(true)\n"
                + "            A.printGreeting();\n"
                + "    }\n"
                + "}"),
                new File("t/B.java", "package t;\n"
                + "public class B {\n"
                + "    public void testMethodB() {\n"
                + "        if(true)\n"
                + "            A.printGreeting();\n"
                + "    }\n"
                + "}"));
        final InlineRefactoring[] r = new InlineRefactoring[1];
        createInlineMethodRefactoring(src.getFileObject("t/A.java"), 1, r);
        performRefactoring(r);
        verifyContent(src,
                new File("t/A.java", "package t;\n"
                + "public class A {\n"
                + "    public void testMethod() {\n"
                + "        if(true) {\n"
                + "        }\n"
                + "    }\n"
                + "}"),
                new File("t/B.java", "package t;\n"
                + "public class B {\n"
                + "    public void testMethodB() {\n"
                + "        if(true) {\n"
                + "        }\n"
                + "    }\n"
                + "}"));
    }
    
    public void test210250() throws Exception {
        writeFilesAndWaitForScan(src,
                new File("t/A.java", "package t;\n"
                + "public class A {\n"
                + "    int x;\n"
                + "    public static void statM() {\n"
                + "        A newClass = new A();\n"
                + "        newClass.method();\n"
                + "    }\n"
                + "\n"
                + "    public void method() {\n"
                + "        System.out.println(x);\n"
                + "        method2();\n"
                + "    }\n"
                + "\n"
                + "    public void method2() {\n"
                + "    }"
                + "}\n"));
        final InlineRefactoring[] r = new InlineRefactoring[1];
        createInlineMethodRefactoring(src.getFileObject("t/A.java"), 3, r);
        performRefactoring(r);
        verifyContent(src, new File("t/A.java", "package t;\n"
                + "public class A {\n"
                + "    int x;\n"
                + "    public static void statM() {\n"
                + "        A newClass = new A();\n"
                + "        System.out.println(newClass.x);\n"
                + "        newClass.method2();\n"
                + "    }\n"
                + "\n"
                + "    public void method2() {\n"
                + "    }"
                + "}\n"));
    }

    public void test209579() throws Exception {
        writeFilesAndWaitForScan(src,
                new File("t/A.java", "package t;\n"
                + "public @interface A {\n"
                + "    String name() default \"\";\n"
                + "}"));
        final InlineRefactoring[] r = new InlineRefactoring[1];
        createInlineMethodRefactoring(src.getFileObject("t/A.java"), 0, r);
        performRefactoring(r, new Problem(true, "ERR_InlineMethodInInterface"));
    }

    public void test208741() throws Exception {
        writeFilesAndWaitForScan(src,
                new File("t/A.java", "package t;\n"
                + "public class A {\n"
                + "    private int c = 4;\n"
                + "    private void testMethod() {\n"
                + "        Inner inner = new Inner();\n"
                + "        int a = c;\n"
                + "        int b = inner.power(a);\n"
                + "    }\n"
                + "    private class Inner {\n"
                + "        private int power(int b) {\n"
                + "            return b * c;\n"
                + "        }\n"
                + "        private int power2(int b) {\n"
                + "            return power(b);\n"
                + "        }\n"
                + "    }\n"
                + "}"));
        final InlineRefactoring[] r = new InlineRefactoring[1];
        createInlineMethodRefactoring(src.getFileObject("t/A.java"), 3, r);
        performRefactoring(r);
        verifyContent(src,
                new File("t/A.java", "package t;\n"
                + "public class A {\n"
                + "    private int c = 4;\n"
                + "    private void testMethod() {\n"
                + "        Inner inner = new Inner();\n"
                + "        int a = c;\n"
                + "        int b = a * c;\n"
                + "    }\n"
                + "    private class Inner {\n"
                + "        private int power2(int b) {\n"
                + "            return b * c;\n"
                + "        }\n"
                + "    }\n"
                + "}"));
    }

    public void test204694a() throws Exception { // #204694 - "Cannot inline public method which uses local accessors" when method used only in-class
        writeFilesAndWaitForScan(src,
                new File("t/A.java", "package t;\n"
                + "public class A {\n"
                + "    public int a = 5;\n"
                + "    public void printGreeting() {\n"
                + "        System.out.println(\"Hello World!\" + a);\n"
                + "        System.out.println(\"Hello World!\" + this.a);\n"
                + "    }\n"
                + "    public void testMethod() {\n"
                + "        if(true)\n"
                + "            printGreeting();\n"
                + "    }\n"
                + "}"),
                new File("t/B.java", "package t;\n"
                + "public class B {\n"
                + "    public void testMethodB() {\n"
                + "        A a = new A();\n"
                + "        a.printGreeting();\n"
                + "    }\n"
                + "}"));
        InlineRefactoring[] r = new InlineRefactoring[1];
        createInlineMethodRefactoring(src.getFileObject("t/A.java"), 2, r);
        performRefactoring(r);
        verifyContent(src,
                new File("t/A.java", "package t;\n"
                + "public class A {\n"
                + "    public int a = 5;\n"
                + "    public void testMethod() {\n"
                + "        if(true) {\n"
                + "            System.out.println(\"Hello World!\" + a);\n"
                + "            System.out.println(\"Hello World!\" + this.a);\n"
                + "        }\n"
                + "    }\n"
                + "}"),
                new File("t/B.java", "package t;\n"
                + "public class B {\n"
                + "    public void testMethodB() {\n"
                + "        A a = new A();\n"
                + "        System.out.println(\"Hello World!\" + a.a);\n"
                + "        System.out.println(\"Hello World!\" + a.a);\n"
                + "    }\n"
                + "}"));
    }

    public void test204694b() throws Exception { // #204694 - "Cannot inline public method which uses local accessors" when method used only in-class
        writeFilesAndWaitForScan(src,
                new File("t/A.java", "package t;\n"
                + "public class A {\n"
                + "    private int power;\n"
                + "    public void setPower(int power) {\n"
                + "        this.power = power;\n"
                + "    }\n"
                + "    private void testMethod() {\n"
                + "        int a = 33 * 42;\n"
                + "        setPower(a);\n"
                + "    }\n"
                + "    private class Inner {\n"
                + "        private void testMethod() {\n"
                + "            setPower(2);\n"
                + "        }\n"
                + "    }\n"
                + "}"));
        InlineRefactoring[] r = new InlineRefactoring[1];
        createInlineMethodRefactoring(src.getFileObject("t/A.java"), 2, r);
        performRefactoring(r);
        verifyContent(src,
                new File("t/A.java", "package t;\n"
                + "public class A {\n"
                + "    private int power;\n"
                + "    private void testMethod() {\n"
                + "        int a = 33 * 42;\n"
                + "        this.power = a;\n"
                + "    }\n"
                + "    private class Inner {\n"
                + "        private void testMethod() {\n"
                + "            A.this.power = 2;\n"
                + "        }\n"
                + "    }\n"
                + "}"));
    }

    public void test203914a() throws Exception { // #203914 - [inline]  Cannot inline this method, a already used.
        writeFilesAndWaitForScan(src,
                new File("t/A.java", "package t;\n"
                + "public class A {\n"
                + "    private int power(int b) {\n"
                + "        return b * c;\n"
                + "    }\n"
                + "    private int c = 4;\n"
                + "    private void testMethod() {\n"
                + "        int a = c;\n"
                + "        int b = power(a);\n"
                + "    }\n"
                + "}"));
        final InlineRefactoring[] r = new InlineRefactoring[1];
        createInlineMethodRefactoring(src.getFileObject("t/A.java"), 1, r);
        performRefactoring(r);
        verifyContent(src,
                new File("t/A.java", "package t;\n"
                + "public class A {\n"
                + "    private int c = 4;\n"
                + "    private void testMethod() {\n"
                + "        int a = c;\n"
                + "        int b = a * c;\n"
                + "    }\n"
                + "}"));
    }

    public void test203914b() throws Exception { // #203914 - [inline]  Cannot inline this method, a already used.
        writeFilesAndWaitForScan(src,
                new File("t/A.java", "package t;\n"
                + "public class A {\n"
                + "    private int power(int b) {\n"
                + "        return b * b;\n"
                + "    }\n"
                + "    private void testMethod() {\n"
                + "        int a = 3;\n"
                + "        int b = power(a);\n"
                + "    }\n"
                + "}"));
        final InlineRefactoring[] r1 = new InlineRefactoring[1];
        createInlineMethodRefactoring(src.getFileObject("t/A.java"), 1, r1);
        performRefactoring(r1);
        verifyContent(src,
                new File("t/A.java", "package t;\n"
                + "public class A {\n"
                + "    private void testMethod() {\n"
                + "        int a = 3;\n"
                + "        int b = a * a;\n"
                + "    }\n"
                + "}"));
    }

    public void test203887() throws Exception {
        writeFilesAndWaitForScan(src,
                new File("t/TestClass.java", "package t;\n"
                + "public class TestClass {\n"
                + "    public int power(int x) {\n"
                + "        return x*x;\n"
                + "    }\n"
                + "    public void  neco(int i) {\n"
                + "        int a = 4;\n"
                + "        int c = power(1);\n"
                + "    }\n"
                + "}"));
        final InlineRefactoring[] r1 = new InlineRefactoring[1];
        createInlineMethodRefactoring(src.getFileObject("t/TestClass.java"), 1, r1);
        performRefactoring(r1);
        verifyContent(src,
                new File("t/TestClass.java", "package t;\n"
                + "public class TestClass {\n"
                + "    public void  neco(int i) {\n"
                + "        int a = 4;\n"
                + "        int c = 1 * 1;\n"
                + "    }\n"
                + "}"));
    }

    public void test203520() throws Exception {
        writeFilesAndWaitForScan(src,
                new File("t/A.java", "package t;\n"
                + "public class A {\n"
                + "    int a = 10 - 20;\n"
                + "    public void testMethod() {\n"
                + "        System.out.println(a-);\n"
                + "    }\n"
                + "}"));

        final InlineRefactoring[] r = new InlineRefactoring[1];
        createInlineConstantRefactoring(src.getFileObject("t/A.java"), 1, r);
        performRefactoring(r);
        verifyContent(src,
                new File("t/A.java", "package t;\n"
                + "public class A {\n"
                + "    public void testMethod() {\n"
                + "        System.out.println(10 - 20-);\n"
                + "    }\n"
                + "}"));
    }

    public void test203371() throws Exception {
        writeFilesAndWaitForScan(src,
                new File("t/IndexBean.java", "package t;\n"
                + "import java.io.File;\n"
                + "import javax.annotation.PostConstruct;\n"
                + "import javax.faces.bean.ManagedBean;\n"
                + "import javax.faces.bean.RequestScoped;\n"
                + "\n"
                + "@ManagedBean(name=\"IndexBean\")\n"
                + "@RequestScoped\n"
                + "public class IndexBean {\n"
                + "    private File[] roots = File.listRoots();\n"
                + "    public File[] getRoots() {\n"
                + "        return roots;\n"
                + "    }\n"
                + "    /** Creates a new instance of IndexBean */\n"
                + "    public IndexBean() {\n"
                + "    }\n"
                + "    @PostConstruct\n"
                + "    public void init() {\n"
                + "        doSome();\n"
                + "    }\n"
                + "    private void doSome() {\n"
                + "        System.out.println(\"hh\");\n"
                + "    }\n"
                + "}"));
        InlineRefactoring[] r = new InlineRefactoring[1];
        createInlineMethodRefactoring(src.getFileObject("t/IndexBean.java"), 4, r);
        performRefactoring(r);
        verifyContent(src, new File("t/IndexBean.java", "package t;\n"
                + "import java.io.File;\n"
                + "import javax.annotation.PostConstruct;\n"
                + "import javax.faces.bean.ManagedBean;\n"
                + "import javax.faces.bean.RequestScoped;\n"
                + "\n"
                + "@ManagedBean(name=\"IndexBean\")\n"
                + "@RequestScoped\n"
                + "public class IndexBean {\n"
                + "    private File[] roots = File.listRoots();\n"
                + "    public File[] getRoots() {\n"
                + "        return roots;\n"
                + "    }\n"
                + "    /** Creates a new instance of IndexBean */\n"
                + "    public IndexBean() {\n"
                + "    }\n"
                + "    @PostConstruct\n"
                + "    public void init() {\n"
                + "        System.out.println(\"hh\");\n"
                + "    }\n"
                + "}"));
    }
    
    public void testInlineTempComments() throws Exception {
        writeFilesAndWaitForScan(src,
                new File("t/A.java", "package t;\n"
                + "public class A {\n"
                + "    public void testMethod() {\n"
                + "        // my Text\n"
                + "        String text = \"text\";\n"
                + "        System.out.println(text);\n"
                + "    }\n"
                + "}"));

        InlineRefactoring[] r = new InlineRefactoring[1];
        createInlineTempRefactoring(src.getFileObject("t/A.java"), 0, r);
        performRefactoring(r);
        verifyContent(src,
                new File("t/A.java", "package t;\n"
                + "public class A {\n"
                + "    public void testMethod() {\n"
                + "        System.out.println(\n"
                + "                           // my Text\n"
                + "                           \"text\");\n"
                + "    }\n"
                + "}"));
    }

    public void testInlineTempa() throws Exception {
        writeFilesAndWaitForScan(src,
                new File("t/A.java", "package t;\n"
                + "public class A {\n"
                + "    public void testMethod() {\n"
                + "        String text = \"text\";\n"
                + "        System.out.println(text);\n"
                + "    }\n"
                + "}"));

        InlineRefactoring[] r = new InlineRefactoring[1];
        createInlineTempRefactoring(src.getFileObject("t/A.java"), 0, r);
        performRefactoring(r);
        verifyContent(src,
                new File("t/A.java", "package t;\n"
                + "public class A {\n"
                + "    public void testMethod() {\n"
                + "        System.out.println(\"text\");\n"
                + "    }\n"
                + "}"));
    }

    public void testInlineTempb() throws Exception {
        writeFilesAndWaitForScan(src,
                new File("t/A.java", "package t;\n"
                + "public class A {\n"
                + "    public void testMethod() {\n"
                + "        int a = 1, b = 2, c = 6;\n"
                + "        System.out.println(b);\n"
                + "    }\n"
                + "}"));

        InlineRefactoring[] r = new InlineRefactoring[1];
        createInlineTempRefactoring(src.getFileObject("t/A.java"), 1, r);
        performRefactoring(r);
        verifyContent(src,
                new File("t/A.java", "package t;\n"
                + "public class A {\n"
                + "    public void testMethod() {\n"
                + "        int a = 1, c = 6;\n"
                + "        System.out.println(2);\n"
                + "    }\n"
                + "}"));
    }

    public void testInlineTempc() throws Exception {
        writeFilesAndWaitForScan(src,
                new File("t/A.java", "package t;\n"
                + "public class A {\n"
                + "    public void testMethod() {\n"
                + "        int a = 1, b = 2, c = 6;\n"
                + "        System.out.println(c);\n"
                + "    }\n"
                + "}"));

        InlineRefactoring[] r = new InlineRefactoring[1];
        createInlineTempRefactoring(src.getFileObject("t/A.java"), 2, r);
        performRefactoring(r);
        verifyContent(src,
                new File("t/A.java", "package t;\n"
                + "public class A {\n"
                + "    public void testMethod() {\n"
                + "        int a = 1, b = 2;\n"
                + "        System.out.println(6);\n"
                + "    }\n"
                + "}"));
    }

    public void testInlineTempd() throws Exception {
        writeFilesAndWaitForScan(src,
                new File("t/A.java", "package t;\n"
                + "public class A {\n"
                + "    public void testMethod() {\n"
                + "        int a = 1 + 2;\n"
                + "        System.out.println(1 + a * 3);\n"
                + "    }\n"
                + "}"));
        InlineRefactoring[] r = new InlineRefactoring[1];
        createInlineTempRefactoring(src.getFileObject("t/A.java"), 0, r);
        performRefactoring(r);
        verifyContent(src,
                new File("t/A.java", "package t;\n"
                + "public class A {\n"
                + "    public void testMethod() {\n"
                + "        System.out.println(1 + (1 + 2) * 3);\n"
                + "    }\n"
                + "}"));
    }
    
    public void testInlineTempd2() throws Exception {
        writeFilesAndWaitForScan(src,
                new File("t/A.java", "package t;\n"
                + "public class A {\n"
                + "    public void testMethod(String[] args) {\n"
                + "        String a = args[0];\n"
                + "        System.out.println(1 + a);\n"
                + "    }\n"
                + "}"));
        InlineRefactoring[] r = new InlineRefactoring[1];
        createInlineTempRefactoring(src.getFileObject("t/A.java"), 0, r);
        performRefactoring(r);
        verifyContent(src,
                new File("t/A.java", "package t;\n"
                + "public class A {\n"
                + "    public void testMethod(String[] args) {\n"
                + "        System.out.println(1 + args[0]);\n"
                + "    }\n"
                + "}"));
    }

    public void testInlineTempe() throws Exception {
        writeFilesAndWaitForScan(src,
                new File("t/A.java", "package t;\n"
                + "public class A {\n"
                + "    public void testMethod() {\n"
                + "        int a = 1 + 2;\n"
                + "        System.out.println(2 * a + 3);\n"
                + "    }\n"
                + "}"));
        InlineRefactoring[] r = new InlineRefactoring[1];
        createInlineTempRefactoring(src.getFileObject("t/A.java"), 0, r);
        performRefactoring(r);
        verifyContent(src,
                new File("t/A.java", "package t;\n"
                + "public class A {\n"
                + "    public void testMethod() {\n"
                + "        System.out.println(2 * (1 + 2) + 3);\n"
                + "    }\n"
                + "}"));
    }

    public void testInlineTempf() throws Exception {
        writeFilesAndWaitForScan(src,
                new File("t/A.java", "package t;\n"
                + "public class A {\n"
                + "    public void testMethod() {\n"
                + "        int a = 1 + 2;\n"
                + "        System.out.println(3 - a);\n"
                + "    }\n"
                + "}"));
        InlineRefactoring[] r = new InlineRefactoring[1];
        createInlineTempRefactoring(src.getFileObject("t/A.java"), 0, r);
        performRefactoring(r);
        verifyContent(src,
                new File("t/A.java", "package t;\n"
                + "public class A {\n"
                + "    public void testMethod() {\n"
                + "        System.out.println(3 - (1 + 2));\n"
                + "    }\n"
                + "}"));
    }

    public void testInlineTempg() throws Exception {
        writeFilesAndWaitForScan(src,
                new File("t/A.java", "package t;\n"
                + "public class A {\n"
                + "    public void testMethod() {\n"
                + "        int a = 1 - 2;\n"
                + "        System.out.println(3 - a);\n"
                + "    }\n"
                + "}"));
        InlineRefactoring[] r = new InlineRefactoring[1];
        createInlineTempRefactoring(src.getFileObject("t/A.java"), 0, r);
        performRefactoring(r);
        verifyContent(src,
                new File("t/A.java", "package t;\n"
                + "public class A {\n"
                + "    public void testMethod() {\n"
                + "        System.out.println(3 - (1 - 2));\n"
                + "    }\n"
                + "}"));
    }

    public void testInlineTemph() throws Exception {
        writeFilesAndWaitForScan(src,
                new File("t/A.java", "package t;\n"
                + "public class A {\n"
                + "    public void testMethod() {\n"
                + "        String a = 3 + \"euro\";\n"
                + "        System.out.println(9 + a);\n"
                + "    }\n"
                + "}"));

        InlineRefactoring[] r = new InlineRefactoring[1];
        createInlineTempRefactoring(src.getFileObject("t/A.java"), 0, r);
        performRefactoring(r);
        verifyContent(src,
                new File("t/A.java", "package t;\n"
                + "public class A {\n"
                + "    public void testMethod() {\n"
                + "        System.out.println(9 + (3 + \"euro\"));\n"
                + "    }\n"
                + "}"));
    }

    public void testInlineTempi() throws Exception {
        writeFilesAndWaitForScan(src,
                new File("t/A.java", "package t;\n"
                + "public class A {\n"
                + "    public void testMethod() {\n"
                + "        String a = getString();\n"
                + "        System.out.println(a);\n"
                + "    }\n"
                + "    public String getString() {\n"
                + "        return \"text\";\n"
                + "    }\n"
                + "}"));

        InlineRefactoring[] r = new InlineRefactoring[1];
        createInlineTempRefactoring(src.getFileObject("t/A.java"), 0, r);
        performRefactoring(r);
        verifyContent(src,
                new File("t/A.java", "package t;\n"
                + "public class A {\n"
                + "    public void testMethod() {\n"
                + "        System.out.println(getString());\n"
                + "    }\n"
                + "    public String getString() {\n"
                + "        return \"text\";\n"
                + "    }\n"
                + "}"));
    }
    
    public void testInlineTempj() throws Exception {
        writeFilesAndWaitForScan(src,
                new File("t/A.java", "package t;\n"
                + "public class A {\n"
                + "    public boolean run() {\n"
                + "        boolean isClass = (\"\" == \"Class\");\n"
                + "        if (!isClass) {\n"
                + "            return false;\n"
                + "        }\n"
                + "        return true;\n"
                + "    }\n"
                + "}"));

        InlineRefactoring[] r = new InlineRefactoring[1];
        createInlineTempRefactoring(src.getFileObject("t/A.java"), 0, r);
        performRefactoring(r);
        verifyContent(src,
                new File("t/A.java", "package t;\n"
                + "public class A {\n"
                + "    public boolean run() {\n"
                + "        if (!(\"\" == \"Class\")) {\n"
                + "            return false;\n"
                + "        }\n"
                + "        return true;\n"
                + "    }\n"
                + "}"));
    }

    public void testCannotInlinea() throws Exception {
        writeFilesAndWaitForScan(src,
                new File("t/A.java", "package t;\n"
                + "public class A {\n"
                + "    public void testMethod() {\n"
                + "        Object[] x = {\"a\", \"b\"};\n"
                + "        System.out.println(x);\n"
                + "    }\n"
                + "}"));
        InlineRefactoring[] r = new InlineRefactoring[1];
        createInlineTempRefactoring(src.getFileObject("t/A.java"), 0, r);
        performRefactoring(r, new Problem(true, "ERR_InlineNotCompoundArrayInit"), new Problem(false, "WRN_InlineChange"));
    }

    public void testCannotInlineb() throws Exception {
        writeFilesAndWaitForScan(src,
                new File("t/A.java", "package t;\n"
                + "public class A {\n"
                + "    public void testMethod() {\n"
                + "        Object[] x = null;\n"
                + "        System.out.println(x.length);\n"
                + "    }\n"
                + "}"));
        InlineRefactoring[] r = new InlineRefactoring[1];
        createInlineTempRefactoring(src.getFileObject("t/A.java"), 0, r);
        performRefactoring(r, new Problem(true, "ERR_InlineNullVarInitializer"));
    }

    public void testCannotInlinec() throws Exception {
        writeFilesAndWaitForScan(src,
                new File("t/A.java", "package t;\n"
                + "public class A {\n"
                + "    public void testMethod() {\n"
                + "        int i;\n"
                + "        if(true)"
                + "            i = 3;\n"
                + "        System.out.println(i);\n"
                + "    }\n"
                + "}"));
        InlineRefactoring[] r = new InlineRefactoring[1];
        createInlineTempRefactoring(src.getFileObject("t/A.java"), 0, r);
        performRefactoring(r, new Problem(true, "ERR_InlineNoVarInitializer"));
    }

    public void testCannotInlined() throws Exception {
        writeFilesAndWaitForScan(src,
                new File("t/A.java", "package t;\n"
                + "public class A {\n"
                + "    public void testMethod() {\n"
                + "        int i = 0;\n"
                + "        i = 3;\n"
                + "        System.out.println(i);\n"
                + "    }\n"
                + "}"));
        InlineRefactoring[] r = new InlineRefactoring[1];
        createInlineTempRefactoring(src.getFileObject("t/A.java"), 0, r);
        performRefactoring(r, new Problem(true, "ERR_InlineAssignedOnce"));
    }

    public void testInlineConstant() throws Exception {
        writeFilesAndWaitForScan(src,
                new File("t/A.java", "package t;\n"
                + "public class A {\n"
                + "    int a = 10 + 20;\n"
                + "    public void testMethod() {\n"
                + "        System.out.println(1 - a);\n"
                + "    }\n"
                + "}"));

        final InlineRefactoring[] r = new InlineRefactoring[1];
        createInlineConstantRefactoring(src.getFileObject("t/A.java"), 1, r);
        performRefactoring(r);
        verifyContent(src,
                new File("t/A.java", "package t;\n"
                + "public class A {\n"
                + "    public void testMethod() {\n"
                + "        System.out.println(1 - (10 + 20));\n"
                + "    }\n"
                + "}"));

        writeFilesAndWaitForScan(src,
                new File("t/A.java", "package t;\n"
                + "public class A {\n"
                + "    static final int a = 10 + 20;\n"
                + "    public void testMethod() {\n"
                + "        System.out.println(1 - a);\n"
                + "    }\n"
                + "}"),
                new File("t/B.java", "package t;\n"
                + "public class B {\n"
                + "    public void testMethod() {\n"
                + "        System.out.println(1 - A.a);\n"
                + "    }\n"
                + "}"));

        final InlineRefactoring[] r2 = new InlineRefactoring[1];
        createInlineConstantRefactoring(src.getFileObject("t/A.java"), 1, r2);
        performRefactoring(r2);
        verifyContent(src,
                new File("t/A.java", "package t;\n"
                + "public class A {\n"
                + "    public void testMethod() {\n"
                + "        System.out.println(1 - (10 + 20));\n"
                + "    }\n"
                + "}"),
                new File("t/B.java", "package t;\n"
                + "public class B {\n"
                + "    public void testMethod() {\n"
                + "        System.out.println(1 - (10 + 20));\n"
                + "    }\n"
                + "}"));
    }

    public void testInlineMethodCasualDiffProblemMaybe() throws Exception {
        writeFilesAndWaitForScan(src,
                new File("t/A.java", "package t;\n"
                + "public class A {\n"
                + "    private void printGreeting() {\n"
                + "        System.out.println(\"Hello World!\");\n"
                + "    }\n"
                + "    public static void main(String[] args) {\n"
                + "        printGreeting();\n"
                + "        printGreeting();\n"
                + "        printGreeting();\n"
                + "    }\n"
                + "}"));
        final InlineRefactoring[] r = new InlineRefactoring[1];
        createInlineMethodRefactoring(src.getFileObject("t/A.java"), 1, r);
        performRefactoring(r);
        verifyContent(src,
                new File("t/A.java", "package t;\n"
                + "public class A {\n"
                + "    public static void main(String[] args) {\n"
                + "        System.out.println(\"Hello World!\");\n"
                + "        System.out.println(\"Hello World!\");\n"
                + "        System.out.println(\"Hello World!\");\n"
                + "    }\n"
                + "}"));
    }

    public void testInlineMethodReturn() throws Exception {
        writeFilesAndWaitForScan(src,
                new File("t/A.java", "package t;\n"
                + "public class A {\n"
                + "    private boolean moreThanFiveDeliveries() {\n"
                + "        System.out.println(\"Less then five?\");\n"
                + "        return numberOfLateDeliveries > 5;"
                + "    }\n"
                + "    public int getRating() {\n"
                + "        moreThanFiveDeliveries();\n"
                + "        return (moreThanFiveDeliveries()) ? 2 : 1;\n"
                + "    }\n"
                + "    private int numberOfLateDeliveries = 6;\n"
                + "}"));
        final InlineRefactoring[] r = new InlineRefactoring[1];
        createInlineMethodRefactoring(src.getFileObject("t/A.java"), 1, r);
        performRefactoring(r, new Problem(false, "WRN_InlineChangeReturn"));
        verifyContent(src,
                new File("t/A.java", "package t;\n"
                + "public class A {\n"
                + "    public int getRating() {\n"
                + "        System.out.println(\"Less then five?\");\n"
                + "        System.out.println(\"Less then five?\");\n"
                + "        return (numberOfLateDeliveries > 5) ? 2 : 1;\n"
                + "    }\n"
                + "    private int numberOfLateDeliveries = 6;\n"
                + "}"));
    }
    
    public void testInlineMethodReturnComments() throws Exception {
        writeFilesAndWaitForScan(src,
                new File("t/A.java", "package t;\n"
                + "public class A {\n"
                + "    private boolean moreThanFiveDeliveries() {\n"
                + "        System.out.println(\"Less then five?\");\n"
                + "        // Less than five?\n"
                + "        return numberOfLateDeliveries > 5;"
                + "    }\n"
                + "    public int getRating() {\n"
                + "        return (moreThanFiveDeliveries()) ? 2 : 1;\n"
                + "    }\n"
                + "    private int numberOfLateDeliveries = 6;\n"
                + "}"));
        final InlineRefactoring[] r = new InlineRefactoring[1];
        createInlineMethodRefactoring(src.getFileObject("t/A.java"), 1, r);
        performRefactoring(r);
        verifyContent(src,
                new File("t/A.java", "package t;\n"
                + "public class A {\n"
                + "    public int getRating() {\n"
                + "        System.out.println(\"Less then five?\");\n"
                + "        return ( // Less than five?\n"
                + "                numberOfLateDeliveries > 5) ? 2 : 1;\n"
                + "    }\n"
                + "    private int numberOfLateDeliveries = 6;\n"
                + "}"));
    }
    
    public void testInlineMethodImports() throws Exception {
        writeFilesAndWaitForScan(src,
                new File("t/A.java", "package t;\n"
                + "import java.util.Arrays;\n"
                + "import static java.lang.System.out;\n"
                + "public class A {\n"
                + "    public static void printGreeting() {\n"
                + "        out.println(Arrays.toString(new String[] {\"Hello\", \"World!\"}));\n"
                + "    }\n"
                + "    public void testMethod() {\n"
                + "        if(true)\n"
                + "            A.printGreeting();\n"
                + "    }\n"
                + "}"),
                new File("t/B.java", "package t;\n"
                + "public class B {\n"
                + "    public void testMethodB() {\n"
                + "        if(true)\n"
                + "            A.printGreeting();\n"
                + "    }\n"
                + "}"));
        final InlineRefactoring[] r = new InlineRefactoring[1];
        createInlineMethodRefactoring(src.getFileObject("t/A.java"), 1, r);
        performRefactoring(r);
        verifyContent(src,
                new File("t/A.java", "package t;\n"
                + "import java.util.Arrays;\n"
                + "import static java.lang.System.out;\n"
                + "public class A {\n"
                + "    public void testMethod() {\n"
                + "        if(true) {\n"
                + "            out.println(Arrays.toString(new String[] {\"Hello\", \"World!\"}));\n"
                + "        }\n"
                + "    }\n"
                + "}"),
                new File("t/B.java", "package t;\n"
                + "import java.util.Arrays;\n"
                + "public class B {\n"
                + "    public void testMethodB() {\n"
                + "        if(true) {\n"
                + "            System.out.println(Arrays.toString(new String[]{\"Hello\", \"World!\"}));\n"
                + "        }\n"
                + "    }\n"
                + "}"));
    }

    public void testInlineMethoda() throws Exception {
        writeFilesAndWaitForScan(src,
                new File("t/A.java", "package t;\n"
                + "public class A {\n"
                + "    private void printGreeting() {\n"
                + "        System.out.println(\"Hello World!\");\n"
                + "    }\n"
                + "    public void testMethod() {\n"
                + "        if(true)\n"
                + "            printGreeting();\n"
                + "    }\n"
                + "}"));
        final InlineRefactoring[] r = new InlineRefactoring[1];
        createInlineMethodRefactoring(src.getFileObject("t/A.java"), 1, r);
        performRefactoring(r);
        verifyContent(src,
                new File("t/A.java", "package t;\n"
                + "public class A {\n"
                + "    public void testMethod() {\n"
                + "        if(true) {\n"
                + "            System.out.println(\"Hello World!\");\n"
                + "        }\n"
                + "    }\n"
                + "}"));
    }

    public void testInlineMethodb() throws Exception {
        writeFilesAndWaitForScan(src,
                new File("t/A.java", "package t;\n"
                + "public class A {\n"
                + "    private String getGreeting() {\n"
                + "        return \"Hello World!\";\n"
                + "    }\n"
                + "    public static void testMethod() {\n"
                + "        getInstance().getGreeting().toString();\n"
                + "    }\n"
                + "    private A getInstance() {\n"
                + "        return new A();\n"
                + "    }\n"
                + "}"));
        final InlineRefactoring[] r2 = new InlineRefactoring[1];
        createInlineMethodRefactoring(src.getFileObject("t/A.java"), 1, r2);
        performRefactoring(r2);
        verifyContent(src,
                new File("t/A.java", "package t;\n"
                + "public class A {\n"
                + "    public static void testMethod() {\n"
                + "        \"Hello World!\".toString();\n"
                + "    }\n"
                + "    private A getInstance() {\n"
                + "        return new A();\n"
                + "    }\n"
                + "}"));
    }

    public void testInlineNoUsageInFilea() throws Exception {
        writeFilesAndWaitForScan(src,
                new File("t/A.java", "package t;\n"
                + "public class A {\n"
                + "    public static void printGreeting() {\n"
                + "        System.out.println(\"Hello World!\");\n"
                + "    }\n"
                + "}"),
                new File("t/B.java", "package t;\n"
                + "public class B {\n"
                + "    public void testMethodB() {\n"
                + "        if(true)\n"
                + "            A.printGreeting();\n"
                + "    }\n"
                + "}"));
        final InlineRefactoring[] r = new InlineRefactoring[1];
        createInlineMethodRefactoring(src.getFileObject("t/A.java"), 1, r);
        performRefactoring(r);
        verifyContent(src,
                new File("t/A.java", "package t;\n"
                + "public class A {\n"
                + "}"),
                new File("t/B.java", "package t;\n"
                + "public class B {\n"
                + "    public void testMethodB() {\n"
                + "        if(true) {\n"
                + "            System.out.println(\"Hello World!\");\n"
                + "        }\n"
                + "    }\n"
                + "}"));
    }

    public void testInlineNoUsageInFileb() throws Exception {
        writeFilesAndWaitForScan(src,
                new File("t/A.java", "package t;\n"
                + "public class A {\n"
                + "    static final int a = 10 + 20;\n"
                + "}"),
                new File("t/B.java", "package t;\n"
                + "public class B {\n"
                + "    public void testMethod() {\n"
                + "        System.out.println(1 - A.a);\n"
                + "    }\n"
                + "}"));

        final InlineRefactoring[] r2 = new InlineRefactoring[1];
        createInlineConstantRefactoring(src.getFileObject("t/A.java"), 1, r2);
        performRefactoring(r2);
        verifyContent(src,
                new File("t/A.java", "package t;\n"
                + "public class A {\n"
                + "}"),
                new File("t/B.java", "package t;\n"
                + "public class B {\n"
                + "    public void testMethod() {\n"
                + "        System.out.println(1 - (10 + 20));\n"
                + "    }\n"
                + "}"));
    }

    public void testInlineMethodMultipleFiles() throws Exception {
        writeFilesAndWaitForScan(src,
                new File("t/A.java", "package t;\n"
                + "public class A {\n"
                + "    public static void printGreeting() {\n"
                + "        System.out.println(\"Hello World!\");\n"
                + "    }\n"
                + "    public void testMethod() {\n"
                + "        if(true)\n"
                + "            A.printGreeting();\n"
                + "    }\n"
                + "}"),
                new File("t/B.java", "package t;\n"
                + "public class B {\n"
                + "    public void testMethodB() {\n"
                + "        if(true)\n"
                + "            A.printGreeting();\n"
                + "    }\n"
                + "}"));
        final InlineRefactoring[] r = new InlineRefactoring[1];
        createInlineMethodRefactoring(src.getFileObject("t/A.java"), 1, r);
        performRefactoring(r);
        verifyContent(src,
                new File("t/A.java", "package t;\n"
                + "public class A {\n"
                + "    public void testMethod() {\n"
                + "        if(true) {\n"
                + "            System.out.println(\"Hello World!\");\n"
                + "        }\n"
                + "    }\n"
                + "}"),
                new File("t/B.java", "package t;\n"
                + "public class B {\n"
                + "    public void testMethodB() {\n"
                + "        if(true) {\n"
                + "            System.out.println(\"Hello World!\");\n"
                + "        }\n"
                + "    }\n"
                + "}"));
    }

    public void testInlineMethodMultipleFilesAccessora() throws Exception {
        writeFilesAndWaitForScan(src,
                new File("t/A.java", "package t;\n"
                + "public class A {\n"
                + "    public static String message = \"Hello World!\";\n"
                + "    public static void printGreeting() {\n"
                + "        System.out.println(message);\n"
                + "    }\n"
                + "    public void testMethod() {\n"
                + "        if(true)\n"
                + "            A.printGreeting();\n"
                + "    }\n"
                + "}"),
                new File("t/B.java", "package t;\n"
                + "public class B {\n"
                + "    public void testMethodB() {\n"
                + "        if(true)\n"
                + "            A.printGreeting();\n"
                + "    }\n"
                + "}"));
        InlineRefactoring[] r = new InlineRefactoring[1];
        createInlineMethodRefactoring(src.getFileObject("t/A.java"), 2, r);
        performRefactoring(r);
        verifyContent(src, new File("t/A.java", "package t;\n"
                + "public class A {\n"
                + "    public static String message = \"Hello World!\";\n"
                + "    public void testMethod() {\n"
                + "        if(true) {\n"
                + "            System.out.println(message);\n"
                + "        }\n"
                + "    }\n"
                + "}"),
                new File("t/B.java", "package t;\n"
                + "public class B {\n"
                + "    public void testMethodB() {\n"
                + "        if(true) {\n"
                + "            System.out.println(A.message);\n"
                + "        }\n"
                + "    }\n"
                + "}"));
    }

    public void testInlineMethodMultipleFilesAccessorb() throws Exception {
        writeFilesAndWaitForScan(src,
                new File("t/A.java", "package t;\n"
                + "public class A {\n"
                + "    public static String message = \"Hello World!\";\n"
                + "    public static void printGreeting() {\n"
                + "        System.out.println(message);\n"
                + "    }\n"
                + "    public void testMethod() {\n"
                + "        if(true)\n"
                + "            A.printGreeting();\n"
                + "    }\n"
                + "}"),
                new File("t/B.java", "package t;\n"
                + "public class B {\n"
                + "    public void testMethodB() {\n"
                + "        if(true) {\n"
                + "            String message = \"Hello World!\";\n"
                + "            A.printGreeting();\n"
                + "        }\n"
                + "    }\n"
                + "}"));
        InlineRefactoring[] r = new InlineRefactoring[1];
        createInlineMethodRefactoring(src.getFileObject("t/A.java"), 2, r);
        performRefactoring(r);
        verifyContent(src, new File("t/A.java", "package t;\n"
                + "public class A {\n"
                + "    public static String message = \"Hello World!\";\n"
                + "    public void testMethod() {\n"
                + "        if(true) {\n"
                + "            System.out.println(message);\n"
                + "        }\n"
                + "    }\n"
                + "}"),
                new File("t/B.java", "package t;\n"
                + "public class B {\n"
                + "    public void testMethodB() {\n"
                + "        if(true) {\n"
                + "            String message = \"Hello World!\";\n"
                + "            System.out.println(A.message);\n"
                + "        }\n"
                + "    }\n"
                + "}"));
    }
    
    public void testInlineMethodParametersMultipleFiles() throws Exception { // #210335 - NullPointerException at com.sun.tools.javac.api.JavacTrees.getElement
        writeFilesAndWaitForScan(src,
                new File("t/A.java", "package t;\n"
                + "public class A {\n"
                + "    public String getGreeting(String name) {\n"
                + "        String message = \"Hello \" + name + \"!\";"
                + "        return name + \": \" + message;\n"
                + "    }\n"
                + "    public void testMethod() {\n"
                + "        System.out.println(getGreeting(\"World\"));\n"
                + "    }\n"
                + "}"),
                new File("t/B.java", "package t;\n"
                + "public class B {\n"
                + "    public void testMethod() {\n"
                + "        A a = new A();\n"
                + "        System.out.println(a.getGreeting(\"World\"));\n"
                + "    }\n"
                + "}"));
        final InlineRefactoring[] r = new InlineRefactoring[1];
        createInlineMethodRefactoring(src.getFileObject("t/A.java"), 1, r);
        performRefactoring(r);
        verifyContent(src,
                new File("t/A.java", "package t;\n"
                + "public class A {\n"
                + "    public void testMethod() {\n"
                + "        String message = \"Hello \" + \"World\" + \"!\";"
                + "        System.out.println(\"World\" + \": \" + message);\n"
                + "    }\n"
                + "}"),
                new File("t/B.java", "package t;\n"
                + "public class B {\n"
                + "    public void testMethod() {\n"
                + "        A a = new A();\n"
                + "        String message = \"Hello \" + \"World\" + \"!\";"
                + "        System.out.println(\"World\" + \": \" + message);\n"
                + "    }\n"
                + "}")
                );
    }

    public void testInlineMethodParameters() throws Exception {
        writeFilesAndWaitForScan(src,
                new File("t/A.java", "package t;\n"
                + "public class A {\n"
                + "    private String getGreeting(String name) {\n"
                + "        String message = \"Hello \" + name + \"!\";"
                + "        return name + \": \" + message;\n"
                + "    }\n"
                + "    public void testMethod() {\n"
                + "        System.out.println(getGreeting(\"World\"));\n"
                + "    }\n"
                + "}"));
        final InlineRefactoring[] r = new InlineRefactoring[1];
        createInlineMethodRefactoring(src.getFileObject("t/A.java"), 1, r);
        performRefactoring(r);
        verifyContent(src,
                new File("t/A.java", "package t;\n"
                + "public class A {\n"
                + "    public void testMethod() {\n"
                + "        String message = \"Hello \" + \"World\" + \"!\";"
                + "        System.out.println(\"World\" + \": \" + message);\n"
                + "    }\n"
                + "}"));
    }

    public void testCannotInlineMethodVoidReturn() throws Exception {
        writeFilesAndWaitForScan(src,
                new File("t/A.java", "package t;\n"
                + "public class A {\n"
                + "    private void printGreeting() {\n"
                + "        System.out.println(\"Hello World!\");\n"
                + "        return\n"
                + "    }\n"
                + "    public static void testMethod() {\n"
                + "        printGreeting();\n"
                + "    }\n"
                + "}"));
        final InlineRefactoring[] r = new InlineRefactoring[1];
        createInlineMethodRefactoring(src.getFileObject("t/A.java"), 1, r);
        performRefactoring(r, new Problem(true, "ERR_InlineMethodVoidReturn"));
    }

    public void testCannotInlineMethodRecursion() throws Exception {
        writeFilesAndWaitForScan(src,
                new File("t/A.java", "package t;\n"
                + "public class A {\n"
                + "    private int fact(int n) {\n"
                + "        int result;\n"
                + "        if (n == 1)\n"
                + "            result = 1;\n"
                + "        else\n"
                + "            result = fact(n - 1) * n;\n"
                + "        return result;\n"
                + "    }\n"
                + "    public static void testMethod() {\n"
                + "        System.out.println(\"Factorial of 3 is: \" + fact(3));\n"
                + "    }\n"
                + "}"));
        final InlineRefactoring[] r = new InlineRefactoring[1];
        createInlineMethodRefactoring(src.getFileObject("t/A.java"), 1, r);
        performRefactoring(r, new Problem(true, "ERR_InlineMethodRecursion"));
    }
    
    public void testCannotInlineAbstractMethod() throws Exception {
        writeFilesAndWaitForScan(src,
                new File("t/A.java", "package t;\n"
                + "public class A {\n"
                + "    public abstract String getGreeting(String name);\n"
                + "}"));
        final InlineRefactoring[] r = new InlineRefactoring[1];
        createInlineMethodRefactoring(src.getFileObject("t/A.java"), 1, r);
        performRefactoring(r, new Problem(true, "ERR_InlineMethodAbstract"));
    }

    public void testCannotInlineMethodMultipleReturn() throws Exception {
        writeFilesAndWaitForScan(src,
                new File("t/A.java", "package t;\n"
                + "public class A {\n"
                + "    private String getGreeting(String name) {\n"
                + "        if(name.length() > 3)\n"
                + "            return name;\n"
                + "        else\n"
                + "            return name + \"...\";\n"
                + "    }\n"
                + "    public static void testMethod() {\n"
                + "        System.out.println(getGreeting(\"World\"));\n"
                + "    }\n"
                + "}"));
        final InlineRefactoring[] r = new InlineRefactoring[1];
        createInlineMethodRefactoring(src.getFileObject("t/A.java"), 1, r);
        performRefactoring(r, new Problem(true, "ERR_InlineMethodMultipleReturn"));
    }
    
    public void testCanInlineMethodMultipleReturn() throws Exception {
        writeFilesAndWaitForScan(src,
                new File("t/A.java", "package t;\n"
                + "public class A {\n"
                + "    private String getGreeting(String name) {\n"
                + "        if(name.length() > 3)\n"
                + "            return name;\n"
                + "        else\n"
                + "            return name + \"...\";\n"
                + "    }\n"
                + "    public static String testMethod() {\n"
                + "        return getGreeting(\"World\");\n"
                + "    }\n"
                + "}"));
        final InlineRefactoring[] r = new InlineRefactoring[1];
        createInlineMethodRefactoring(src.getFileObject("t/A.java"), 1, r);
        performRefactoring(r);
        verifyContent(src,
                new File("t/A.java", "package t;\n"
                + "public class A {\n"
                + "    public static String testMethod() {\n"
                + "        if (\"World\".length() > 3) {\n"
                + "            return \"World\";\n"
                + "        } else {\n"
                + "            return \"World\" + \"...\";\n"
                + "        }\n"
                + "    }\n"
                + "}"));
    }

    public void testCannotInlineMethodNoLastReturn() throws Exception {
        writeFilesAndWaitForScan(src,
                new File("t/A.java", "package t;\n"
                + "public class A {\n"
                + "    private String getGreeting(String name) {\n"
                + "        throw new UnsupportedOperationException(\"Not yet implemented\");\n"
                + "    }\n"
                + "    public static void testMethod() {\n"
                + "        System.out.println(getGreeting(\"World\"));\n"
                + "    }\n"
                + "}"));
        final InlineRefactoring[] r = new InlineRefactoring[1];
        createInlineMethodRefactoring(src.getFileObject("t/A.java"), 1, r);
        performRefactoring(r, new Problem(true, "ERR_InlineMethodNoLastReturn"));

        writeFilesAndWaitForScan(src,
                new File("t/A.java", "package t;\n"
                + "public class A {\n"
                + "    private String getGreeting(String name) {\n"
                + "        try {\n"
                + "            System.out.println(\"Hello World!\");\n"
                + "        }\n"
                + "        finally {\n"
                + "            return \"Hello Finally\";\n"
                + "        }\n"
                + "    }\n"
                + "    public static void testMethod() {\n"
                + "        System.out.println(getGreeting(\"World\"));\n"
                + "    }\n"
                + "}"));
        final InlineRefactoring[] r2 = new InlineRefactoring[1];
        createInlineMethodRefactoring(src.getFileObject("t/A.java"), 1, r2);
        performRefactoring(r2, new Problem(true, "ERR_InlineMethodNoLastReturn"));
    }
    
    public void testCanInlineMethodNoLastReturn() throws Exception {
        writeFilesAndWaitForScan(src,
                new File("t/A.java", "package t;\n"
                + "public class A {\n"
                + "    private String getGreeting(String name) {\n"
                + "        throw new UnsupportedOperationException(\"Not yet implemented\");\n"
                + "    }\n"
                + "    public static void testMethod() {\n"
                + "        getGreeting(\"World\");\n"
                + "    }\n"
                + "}"));
        final InlineRefactoring[] r = new InlineRefactoring[1];
        createInlineMethodRefactoring(src.getFileObject("t/A.java"), 1, r);
        performRefactoring(r);
        verifyContent(src,
                new File("t/A.java", "package t;\n"
                        + "public class A {\n"
                        + "    public static void testMethod() {\n"
                        + "        throw new UnsupportedOperationException(\"Not yet implemented\");\n"
                        + "    }\n"
                        + "}"));
        
        
        writeFilesAndWaitForScan(src,
                new File("t/A.java", "package t;\n"
                + "public class A {\n"
                + "    private String getGreeting(String name) {\n"
                + "        try {\n"
                + "            System.out.println(\"Hello World!\");\n"
                + "        }\n"
                + "        finally {\n"
                + "            return \"Hello Finally\";\n"
                + "        }\n"
                + "    }\n"
                + "    public static void testMethod() {\n"
                + "        getGreeting(\"World\");\n"
                + "    }\n"
                + "}"));
        final InlineRefactoring[] r2 = new InlineRefactoring[1];
        createInlineMethodRefactoring(src.getFileObject("t/A.java"), 1, r2);
        performRefactoring(r2);
        verifyContent(src,
                new File("t/A.java", "package t;\n"
                + "public class A {\n"
                + "    public static void testMethod() {\n"
                + "        try {\n"
                + "            System.out.println(\"Hello World!\");\n"
                + "        }\n"
                + "        finally {\n"
                + "            return \"Hello Finally\";\n"
                + "        }\n"
                + "    }\n"
                + "}"));
    }

    public void testCannotInlineMethodNoAccessors() throws Exception {
        writeFilesAndWaitForScan(src,
                new File("t/A.java", "package t;\n"
                + "public class A {\n"
                + "    public static void printGreeting() {\n"
                + "        System.out.println(C.message);\n"
                + "    }\n"
                + "    public void testMethod() {\n"
                + "        if(true)\n"
                + "            A.printGreeting();\n"
                + "    }\n"
                + "}"),
                new File("v/B.java", "package v;\n"
                + "public class B {\n"
                + "    public void testMethodB() {\n"
                + "        if(true)\n"
                + "            t.A.printGreeting();\n"
                + "    }\n"
                + "}"),
                new File("t/C.java", "package t;\n"
                + "public class C {\n"
                + "    static String message = \"Hello World!\";\n"
                + "}"));
        final InlineRefactoring[] r = new InlineRefactoring[1];
        createInlineMethodRefactoring(src.getFileObject("t/A.java"), 1, r);
        performRefactoring(r, new Problem(false, "WRN_InlineNotAccessible"));
    }

    public void testCannotInlineMethodPolymorphic() throws Exception {
        writeFilesAndWaitForScan(src,
                new File("t/A.java", "package t;\n"
                + "public class A extends B {\n"
                + "    public void printGreeting() {\n"
                + "        System.out.println(\"Hello World\");\n"
                + "    }\n"
                + "    public void testMethod() {\n"
                + "        if(true)\n"
                + "            printGreeting();\n"
                + "    }\n"
                + "}"),
                new File("t/B.java", "package t;\n"
                + "public class B {\n"
                + "    public void printGreeting() {\n"
                + "        System.out.println(\"Hello\");"
                + "    }\n"
                + "}"));
        final InlineRefactoring[] r = new InlineRefactoring[1];
        createInlineMethodRefactoring(src.getFileObject("t/A.java"), 1, r);
        performRefactoring(r, new Problem(true, "ERR_InlineMethodPolymorphic"));

        writeFilesAndWaitForScan(src,
                new File("t/A.java", "package t;\n"
                + "public class A {\n"
                + "    public void printGreeting() {\n"
                + "        System.out.println(\"Hello World\");\n"
                + "    }\n"
                + "    public void testMethod() {\n"
                + "        if(true)\n"
                + "            printGreeting();\n"
                + "    }\n"
                + "}"),
                new File("t/B.java", "package t;\n"
                + "public class B extends A {\n"
                + "    public void printGreeting() {\n"
                + "        System.out.println(\"Hello\");"
                + "    }\n"
                + "}"));
        final InlineRefactoring[] r2 = new InlineRefactoring[1];
        createInlineMethodRefactoring(src.getFileObject("t/A.java"), 1, r2);
        performRefactoring(r2, new Problem(true, "ERR_InlineMethodPolymorphic"));
    }

    public void testCannotInlineMethodNameClash() throws Exception {
        writeFilesAndWaitForScan(src,
                new File("t/A.java", "package t;\n"
                + "public class A {\n"
                + "    public void printGreeting() {\n"
                + "        String message = \"Hello World!\";\n"
                + "        System.out.println(message);\n"
                + "    }\n"
                + "    public void testMethod() {\n"
                + "        String message = \"Hello\";\n"
                + "        printGreeting();\n"
                + "    }\n"
                + "}"));
        final InlineRefactoring[] r = new InlineRefactoring[1];
        createInlineMethodRefactoring(src.getFileObject("t/A.java"), 1, r);
        performRefactoring(r);
        verifyContent(src,
                new File("t/A.java", "package t;\n"
                + "public class A {\n"
                + "    public void testMethod() {\n"
                + "        String message = \"Hello\";\n"
                + "        String message1 = \"Hello World!\";\n"
                + "        System.out.println(message1);\n"
                + "    }\n"
                + "}"));
    }

    public void test198821() throws Exception {
        writeFilesAndWaitForScan(src,
                new File("t/A.java", "package t;\n"
                + "public class A implements B{\n"
                + "    public void printGreeting() {\n"
                + "        System.out.println(\"Hello World\");\n"
                + "    }\n"
                + "    public void testMethod() {\n"
                + "        if(true)\n"
                + "            printGreeting();\n"
                + "    }\n"
                + "}"),
                new File("t/B.java", "package t;\n"
                + "public interface B {\n"
                + "    public void printGreeting();\n"
                + "}"));
        final InlineRefactoring[] r = new InlineRefactoring[1];
        createInlineMethodRefactoring(src.getFileObject("t/A.java"), 1, r);
        performRefactoring(r, new Problem(true, "ERR_InlineMethodPolymorphic"));
    }

    public void test199068() throws Exception {
        writeFilesAndWaitForScan(src,
                new File("t/A.java", "package t;\n"
                + "public class A {\n"
                + "    public void testMethod() {\n"
                + "        int a = 3;\n"
                + "        switch(a) {\n"
                + "            case 1:\n"
                + "                int b = 5;\n"
                + "                System.out.println(b);\n"
                + "    }\n"
                + "}"));
        final InlineRefactoring[] r = new InlineRefactoring[1];
        JavaSource.forFileObject(src.getFileObject("t/A.java")).runUserActionTask(new Task<CompilationController>() {

            @Override
            public void run(CompilationController parameter) throws Exception {
                parameter.toPhase(JavaSource.Phase.RESOLVED);
                CompilationUnitTree cut = parameter.getCompilationUnit();

                MethodTree testMethod = (MethodTree) ((ClassTree) cut.getTypeDecls().get(0)).getMembers().get(1);
                SwitchTree switchTree = (SwitchTree) testMethod.getBody().getStatements().get(1);
                CaseTree case1 = switchTree.getCases().get(0);
                VariableTree variable = (VariableTree) case1.getStatements().get(0);

                TreePath tp = TreePath.getPath(cut, variable);
                r[0] = new InlineRefactoring(TreePathHandle.create(tp, parameter), InlineRefactoring.Type.TEMP);
            }
        }, true);
        performRefactoring(r);
        verifyContent(src,
                new File("t/A.java", "package t;\n"
                + "public class A {\n"
                + "    public void testMethod() {\n"
                + "        int a = 3;\n"
                + "        switch(a) {\n"
                + "            case 1:\n"
                + "                System.out.println(5);\n"
                + "    }\n"
                + "}"));
    }
    
    /**
     * Checks that the instance used to invoke the method will be used in dereferenced instance's members
     */
    public void test271065InlineMethod() throws Exception {
        writeFilesAndWaitForScan(src,
                new File("t/TestInline.java", "package t;\n"
                        + "public class TestInline {\n"
                        + "    protected Object[] entities;\n"
                        + "\n"
                        + "    public Object[] getEntities() {\n"
                        + "        return entities;\n"
                        + "    }\n"
                        + "\n"
                        + "    public void copy(TestInline source)\n"
                        + "    {\n"
                        + "        this.entities = source.getEntities();\n"
                        + "    }\n"
                        + "}")
        );
        final InlineRefactoring[] r = new InlineRefactoring[1];
        createInlineMethodRefactoring(src.getFileObject("t/TestInline.java"), 2, r);
        performRefactoring(r);
        verifyContent(src,
                new File("t/TestInline.java", "package t;\n"
                        + "public class TestInline {\n"
                        + "    protected Object[] entities;\n"
                        + "\n"
                        + "    public void copy(TestInline source)\n"
                        + "    {\n"
                        + "        this.entities = source.entities;\n"
                        + "    }\n"
                        + "}")
        );
    }
    
    /**
     * "this" qualifier should be suppressed.
     */
    public void test271065InlineMethodReduceThis() throws Exception {
        writeFilesAndWaitForScan(src,
                new File("t/TestInline4.java", "package t;\n"
                        + "public class TestInline4 {\n"
                        + "    protected Object[] entities;\n"
                        + "    protected Object[] entities2;\n"
                        + "    \n"
                        + "    public Object[] getEntities() {\n"
                        + "        return entities;\n"
                        + "    }\n"
                        + "\n"
                        + "    public void copy()\n"
                        + "    {\n"
                        + "        this.entities2 = this.getEntities();\n"
                        + "    }\n"
                        + "}")
        );
        final InlineRefactoring[] r = new InlineRefactoring[1];
        createInlineMethodRefactoring(src.getFileObject("t/TestInline4.java"), 3, r);
        performRefactoring(r);
        verifyContent(src,
                new File("t/TestInline4.java", "package t;\n"
                        + "public class TestInline4 {\n"
                        + "    protected Object[] entities;\n"
                        + "    protected Object[] entities2;\n"
                        + "    \n"
                        + "    public void copy()\n"
                        + "    {\n"
                        + "        this.entities2 = entities;\n"
                        + "    }\n"
                        + "}")
        );
    }

    private void createInlineConstantRefactoring(FileObject source, final int position, final InlineRefactoring[] r) throws IOException, IllegalArgumentException {
        JavaSource.forFileObject(source).runUserActionTask(new Task<CompilationController>() {

            @Override
            public void run(CompilationController parameter) throws Exception {
                parameter.toPhase(JavaSource.Phase.RESOLVED);
                CompilationUnitTree cut = parameter.getCompilationUnit();

                VariableTree variable = (VariableTree) ((ClassTree) cut.getTypeDecls().get(0)).getMembers().get(position);

                TreePath tp = TreePath.getPath(cut, variable);
                r[0] = new InlineRefactoring(TreePathHandle.create(tp, parameter), InlineRefactoring.Type.CONSTANT);
            }
        }, true);
    }

    private void createInlineTempRefactoring(FileObject source, final int position, final InlineRefactoring[] r) throws IllegalArgumentException, IOException {
        JavaSource.forFileObject(source).runUserActionTask(new Task<CompilationController>() {

            @Override
            public void run(CompilationController parameter) throws Exception {
                parameter.toPhase(JavaSource.Phase.RESOLVED);
                CompilationUnitTree cut = parameter.getCompilationUnit();

                MethodTree testMethod = (MethodTree) ((ClassTree) cut.getTypeDecls().get(0)).getMembers().get(1);
                VariableTree variable = (VariableTree) testMethod.getBody().getStatements().get(position);

                TreePath tp = TreePath.getPath(cut, variable);
                r[0] = new InlineRefactoring(TreePathHandle.create(tp, parameter), InlineRefactoring.Type.TEMP);
            }
        }, true);
    }

    private void createInlineMethodRefactoring(FileObject source, final int position, final InlineRefactoring[] r) throws IllegalArgumentException, IOException {
        JavaSource.forFileObject(source).runUserActionTask(new Task<CompilationController>() {

            @Override
            public void run(CompilationController parameter) throws Exception {
                parameter.toPhase(JavaSource.Phase.RESOLVED);
                CompilationUnitTree cut = parameter.getCompilationUnit();

                Tree member = ((ClassTree) cut.getTypeDecls().get(0)).getMembers().get(position);
                if(member.getKind() == Tree.Kind.CLASS) {
                    ClassTree klazz = (ClassTree) member;
                    member = klazz.getMembers().get(1); // Skip the first, generated constr.
                }
                MethodTree method = (MethodTree) member;

                TreePath tp = TreePath.getPath(cut, method);
                r[0] = new InlineRefactoring(TreePathHandle.create(tp, parameter), InlineRefactoring.Type.METHOD);
            }
        }, true);
    }

    private void performRefactoring(final InlineRefactoring[] r, Problem... expectedProblems) throws InterruptedException {
        RefactoringSession rs = RefactoringSession.create("Session");
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
