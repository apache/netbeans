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

import java.net.URL;
import org.netbeans.modules.java.source.parsing.JavacParser;
import org.netbeans.modules.refactoring.api.Problem;

/**
 *
 * @author Ralph Ruijs <ralphbenjamin@netbeans.org>
 */
public class MoveClassTest extends MoveBase {

    public MoveClassTest(String name) {
        super(name, "testModuleInfoMoveFileDifferentPackage".equals(name) ? "11" : "1.6");
    }
    
    public void test204444() throws Exception { // #204444 - Improve Move Refactoring to support nested/inner classes
        writeFilesAndWaitForScan(src,
                new File("t/A.java", "package t;\n"
                + "import u.B;\n"
                + "public class A {\n"
                + "    /** Something about i */\n"
                + "    static int i(B b) { return b.i; }\n"
                + "    public void foo() {\n"
                + "        System.out.println(i(new B()));\n"
                + "    }\n"
                + "}\n"),
                new File("u/B.java", "package u;\n"
                + "import java.util.List;\n"
                + "/** Class B */\n"
                + "public class B {\n"
                + "    public int i = 42;\n"
                + "    private List lijst;\n"
                + "}\n"),
                new File("u/C.java", "package u;\n"
                + "public class C {\n"
                + "}\n"));
        performMove(src.getFileObject("u/B.java"), 0, src.getFileObject("u/C.java"), 0);
        verifyContent(src,
                new File("t/A.java", "package t;\n"
                + "import u.C;\n"
                + "public class A {\n"
                + "    /** Something about i */\n"
                + "    static int i(C.B b) { return b.i; }\n"
                + "    public void foo() {\n"
                + "        System.out.println(i(new C.B()));\n"
                + "    }\n"
                + "}\n"),
                new File("u/C.java", "package u;\n"
                + "import java.util.List;\n"
                + "public class C {\n"
                + "    /** Class B */\n"
                + "    public static class B {\n"
                + "        public int i = 42;\n"
                + "        private List lijst;\n"
                + "    }\n"
                + "}\n"));
    }
    
    public void testNETBEANS892() throws Exception { // #204444 - Improve Move Refactoring to support nested/inner classes
        writeFilesAndWaitForScan(src,
                new File("a/A.java", "package a;\n"
                + "import java.util.List;\n"
                + "import java.util.function.Function;\n"
                + "public class A {\n"
                + "public void v() {try{String bar = \"foo\";}catch (RuntimeException | AssertionError e){}}\n"
                + "public void breaks(){doStuff(x->x.substring(5));}\n"
                + "public void doStuff(Function<String, String> stuff){}\n"
                + "}\n"),
                new File("a/B.java", "package a;\n"
                + "import java.util.List;\n"
                + "/** Class B */\n"
                + "public class B {\n"
                + "    public int i = 42;\n"
                + "    private List list;\n"
                + "}\n"),
                new File("a/C.java", "package a;\n"
                + "import java.util.function.Function;\n"
                + "public class C {\n"
                + "public void v() {try{String bar = \"foo\";}catch (RuntimeException | AssertionError e){}}\n"
                + "public void breaks(){doStuff(x->x.substring(5));}\n"
                + "public void doStuff(Function<String, String> stuff){}\n"
                + "}\n"));
        performMove(src.getFileObject("a/B.java"), 0, src.getFileObject("a/C.java"), 0);
        verifyContent(src,
                new File("a/A.java", "package a;\n"                
                + "import java.util.List;\n"
                + "import java.util.function.Function;\n"
                + "public class A {\n"
                + "public void v() {try{String bar = \"foo\";}catch (RuntimeException | AssertionError e){}}\n"
                + "public void breaks(){doStuff(x->x.substring(5));}\n"
                + "public void doStuff(Function<String, String> stuff){}\n"
                + "}\n"),
                new File("a/C.java", "package a;\n"
                + "import java.util.List;\n"
                + "import java.util.function.Function;\n"
                + "public class C {\n"
                + "public void v() {try{String bar = \"foo\";}catch (RuntimeException | AssertionError e){}}\n"
                + "public void breaks(){doStuff(x->x.substring(5));}\n"
                + "public void doStuff(Function<String, String> stuff){}\n"
                + "/** Class B */\n"
                + "public static class B {\n"
                + "    public int i = 42;\n"
                + "    private List list;\n"
                + "}\n"
                + "}\n"));
    }
    
    public void test243552() throws Exception { // #204444 - Improve Move Refactoring to support nested/inner classes
        writeFilesAndWaitForScan(src,
                new File("t/A.java", "package t;\n"
                + "public interface A {\n"
                + "}\n"
                + "interface B extends A {\n"
                + "}\n"));
        performMove(src.getFileObject("t/A.java"), 1, src.getFileObject("t/A.java"), 0);
        verifyContent(src,
                new File("t/A.java", "package t;\n"
                + "public interface A {\n"
                + "    static interface B extends A {\n"
                + "    }\n"
                + "}\n"));
    }
    
    public void test204444c() throws Exception { // #204444 - Improve Move Refactoring to support nested/inner classes
        writeFilesAndWaitForScan(src,
                new File("t/A.java", "package t;\n"
                + "public class A {\n"
                + "    /** Something about i */\n"
                + "    static int i(B b) { return b.i; }\n"
                + "    public void foo() {\n"
                + "        System.out.println(i(new B()));\n"
                + "    }\n"
                + "}\n"
                + "class B {\n"
                + "    public int i = 42;\n"
                + "}\n"));
        performMove(src.getFileObject("t/A.java"), 1, src.getFileObject("t/A.java"), 0);
        verifyContent(src,
                new File("t/A.java", "package t;\n"
                + "public class A {\n"
                + "    /** Something about i */\n"
                + "    static int i(B b) { return b.i; }\n"
                + "    public void foo() {\n"
                + "        System.out.println(i(new B()));\n"
                + "    }\n"
                + "    static class B {\n"
                + "        public int i = 42;\n"
                + "    }\n"
                + "}\n"));
    }
    
    public void test204444a() throws Exception {
        writeFilesAndWaitForScan(src,
                new File("t/A.java", "package t;\n"
                + "public class A {\n"
                + "    /** Something about i */\n"
                + "    static int i(B b) { return b.i; }\n"
                + "    public void foo() {\n"
                + "        System.out.println(i(new B()));\n"
                + "    }\n"
                + "    enum B { }\n"
                + "}\n"
                + "class B {\n"
                + "    public int i = 42;\n"
                + "}\n"));
        performMove(src.getFileObject("t/A.java"), 1, src.getFileObject("t/A.java"), 0, new Problem(true, "ERR_ClassToMoveClashesInner"));
    }
    
    public void test127535() throws Exception { // #127535 - [Move] Cannot move second class in the file
        writeFilesAndWaitForScan(src,
                new File("t/A.java", "package t;\n"
                + "public class A {\n"
                + "    /** Something about i */\n"
                + "    static int i(B b) { return b.i; }\n"
                + "    public void foo() {\n"
                + "        System.out.println(i(new B()));\n"
                + "    }\n"
                + "}\n"
                + "class B {\n"
                + "    public int i = 42;\n"
                + "}\n"));
        performMove(src.getFileObject("t/A.java"), 1, new URL(src.getURL(), "t/"));
        verifyContent(src,
                new File("t/A.java", "package t;\n"
                + "public class A {\n"
                + "    /** Something about i */\n"
                + "    static int i(B b) { return b.i; }\n"
                + "    public void foo() {\n"
                + "        System.out.println(i(new B()));\n"
                + "    }\n"
                + "}\n"),
                new File("t/B.java", "/* * Refactoring License */\n"
                + "package t;\n"
                + "/** * * @author junit */\n"
                + "class B {\n"
                + "    public int i = 42;\n"
                + "}\n"));
    }
    
    public void test127535a() throws Exception { // #127535 - [Move] Cannot move second class in the file
        writeFilesAndWaitForScan(src,
                new File("t/A.java", "package t;\n"
                + "public class A {\n"
                + "    /** Something about i */\n"
                + "    static int i(B b) { return b.i; }\n"
                + "    public void foo() {\n"
                + "        System.out.println(i(new B()));\n"
                + "    }\n"
                + "}\n"
                + "class B {\n"
                + "    public int i = 42;\n"
                + "}\n"));
        performMove(src.getFileObject("t/A.java"), 1, new URL(src.getURL(), "v/"), new Problem(false, "ERR_AccessesPackagePrivateFeature"));
        verifyContent(src,
                new File("t/A.java", "package t;\n"
                + "import v.B;\n"
                + "public class A {\n"
                + "    /** Something about i */\n"
                + "    static int i(B b) { return b.i; }\n"
                + "    public void foo() {\n"
                + "        System.out.println(i(new B()));\n"
                + "    }\n"
                + "}\n"),
                new File("v/B.java", "/* * Refactoring License */\n"
                + "package v;\n"
                + "/** * * @author junit */\n"
                + "class B {\n"
                + "    public int i = 42;\n"
                + "}\n"));
    }
    
    public void test127535b() throws Exception { // #127535 - [Move] Cannot move second class in the file
        writeFilesAndWaitForScan(src,
                new File("t/A.java", "package t;\n"
                + "public class A {\n"
                + "    /** Something about i */\n"
                + "    static int i(B b) { return b.i; }\n"
                + "    public void foo() {\n"
                + "        System.out.println(i(new B()));\n"
                + "    }\n"
                + "}\n"
                + "class B {\n"
                + "    public int i = 42;\n"
                + "}\n"));
        performMove(src.getFileObject("t/A.java"), 0, new URL(src.getURL(), "t/"), new Problem(true, "ERR_CannotMovePublicIntoSamePackage"));
        verifyContent(src,
                new File("t/A.java", "package t;\n"
                + "public class A {\n"
                + "    /** Something about i */\n"
                + "    static int i(B b) { return b.i; }\n"
                + "    public void foo() {\n"
                + "        System.out.println(i(new B()));\n"
                + "    }\n"
                + "}\n"
                + "class B {\n"
                + "    public int i = 42;\n"
                + "}\n"));
    }
    
    public void test127535c() throws Exception { // #127535 - [Move] Cannot move second class in the file
        writeFilesAndWaitForScan(src,
                new File("t/A.java", "package t;\n"
                + "public class A {\n"
                + "    /** Something about i */\n"
                + "    static int i(B b) { return b.i; }\n"
                + "    public void foo() {\n"
                + "        System.out.println(i(new B()));\n"
                + "    }\n"
                + "}\n"
                + "class B {\n"
                + "    public int i = 42;\n"
                + "}\n"));
        performMove(src.getFileObject("t/A.java"), 0, new URL(src.toURL(), "v/"),
                new Problem(false, "ERR_AccessesPackagePrivateFeature2"));
        verifyContent(src,
                new File("v/A.java", "/* * Refactoring License */\n"
                + "package v;\n"
                + "import t.B;\n"
                + "/** * * @author junit */\n"
                + "public class A {\n"
                + "    /** Something about i */\n"
                + "    static int i(B b) { return b.i; }\n"
                + "    public void foo() {\n"
                + "        System.out.println(i(new B()));\n"
                + "    }\n"
                + "}\n"),
                new File("t/A.java", "package t;\n"
                + "class B {\n"
                + "    public int i = 42;\n"
                + "}\n"));
    }

    public void testModuleInfoMoveFileDifferentPackage() throws Exception {
        writeFilesAndWaitForScan(src,
                new File("t/A.java", "package t;\n"
                + "public class A {\n"
                + "    public void foo() {\n"
                + "    }\n"
                + "}\n"),
                new File("module-info.java", "module m {\n"
                + "    requires java.base;\n"
                + "    exports t to m;\n"
                + "    uses t.A;\n"
                + "}\n"));
        performMove(src.getFileObject("t/A.java"), new URL(src.toURL(), "p/"));
        verifyContent(src,
                new File("p/A.java", "package p;\n"
                + "public class A {\n"
                + "    public void foo() {\n"
                + "    }\n"
                + "}\n"),
                new File("module-info.java", "module m {\n"
                + "    requires java.base;\n"
                + "    exports t to m;\n"
                + "    uses p.A;\n"
                + "}\n"));
    }

    static {
        JavacParser.DISABLE_SOURCE_LEVEL_DOWNGRADE = true;
    }
}
