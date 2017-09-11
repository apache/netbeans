/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2012 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2012 Sun Microsystems, Inc.
 */
package org.netbeans.modules.refactoring.java.test;

import java.net.URL;
import org.netbeans.modules.refactoring.api.Problem;

/**
 *
 * @author Ralph Ruijs <ralphbenjamin@netbeans.org>
 */
public class MoveClassTest extends MoveBase {

    public MoveClassTest(String name) {
        super(name);
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
}
