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

import org.netbeans.modules.refactoring.api.Problem;
import org.netbeans.modules.refactoring.java.api.JavaMoveMembersProperties.Visibility;

/**
 *
 * @author Ralph Ruijs
 */
public class MoveFieldTest extends MoveBase {

    public MoveFieldTest(String name) {
        super(name);
    }
    
    public void testMoveField() throws Exception {
        writeFilesAndWaitForScan(src,
                new File("t/A.java", "package t;\n"
                        + "public class A {\n"
                        + "    private B t;\n"
                        + "    public void usage(/* B t */) {\n"
                        + "        this.t = new B();\n"
                        + "    }\n"
                        + "}\n"),
                new File("t/B.java", "package t;\n"
                        + "public class B {\n"
                        + "}\n"));
        performMove(src.getFileObject("t/A.java"), new int[]{1}, src.getFileObject("t/B.java"), Visibility.ESCALATE, false, new Problem(false, "WRN_NoAccessor"));
        verifyContent(src,
                new File("t/A.java", "package t;\n"
                        + "public class A {\n"
                        + "    public void usage(/* B t */) {\n"
                        + "        this.t = new B();\n"
                        + "    }\n"
                        + "}\n"),
                new File("t/B.java", "package t;\n"
                        + "public class B {\n"
                        + "    B t;\n"
                        + "}\n"));
    }
    
    public void test242909() throws Exception {
        writeFilesAndWaitForScan(src,
                new File("t/A.java", "package t;\n"
                        + "public class A {\n"
                        + "    private B b;\n"
                        + "}\n"),
                new File("t/B.java", "package t;\n"
                        + "public class B {\n"
                        + "}\n"),
                new File("v/C.java", "package v;\n"
                        + "public class C {\n"
                        + "}\n"));
        performMove(src.getFileObject("t/A.java"), new int[]{1}, src.getFileObject("v/C.java"), Visibility.ESCALATE, false);
        verifyContent(src,
                new File("t/A.java", "package t;\n"
                        + "public class A {\n"
                        + "}\n"),
                new File("t/B.java", "package t;\n"
                        + "public class B {\n"
                        + "}\n"),
                new File("v/C.java", "package v;\n"
                        + "import t.B;\n"
                        + "public class C {\n"
                        + "    private B b;\n"
                        + "}\n"));
    }
    
    public void testMoveEscalate() throws Exception {
        writeFilesAndWaitForScan(src,
                new File("t/A.java", "package t;\n"
                + "public class A {\n"
                + "    static int i;\n"
                + "}\n"),
                new File("v/B.java", "package v;\n"
                + "public class B {\n"
                + "}\n"),
                new File("t/C.java", "package t;\n"
                + "public class C {\n"
                + "    private void foo() {\n"
                + "        System.out.println(A.i);\n"
                + "    }\n"
                + "}\n"));
        performMove(src.getFileObject("t/A.java"), new int[]{1}, src.getFileObject("v/B.java"), Visibility.ESCALATE, false);
        verifyContent(src,
                new File("t/A.java", "package t;\n"
                + "public class A {\n"
                + "}\n"),
                new File("v/B.java", "package v;\n"
                + "public class B {\n"
                + "    public static int i;\n"
                + "}\n"),
                new File("t/C.java", "package t;\n"
                + "import v.B;\n"
                + "public class C {\n"
                + "    private void foo() {\n"
                + "        System.out.println(B.i);\n"
                + "    }\n"
                + "}\n"));
    }

    public void testMoveStatic() throws Exception {
        writeFilesAndWaitForScan(src,
                new File("t/A.java", "package t;\n"
                + "public class A {\n"
                + "    /** Something about i */\n"
                + "    static int i;\n"
                + "    public void foo() {\n"
                + "        System.out.println(i);\n"
                + "    }\n"
                + "}\n"),
                new File("t/B.java", "package t;\n"
                + "public class B {\n"
                + "    public void foo() {\n"
                + "        System.out.println(A.i);\n"
                + "    }\n"
                + "}\n"),
                new File("t/C.java", "package t;\n"
                + "public class C {\n"
                + "    public void foo() {\n"
                + "        System.out.println(A.i);\n"
                + "    }\n"
                + "}\n"));
        performMove(src.getFileObject("t/A.java"), new int[]{1}, src.getFileObject("t/B.java"), Visibility.PUBLIC, false);
        verifyContent(src,
                new File("t/A.java", "package t;\n"
                + "public class A {\n"
                + "    public void foo() {\n"
                + "        System.out.println(B.i);\n"
                + "    }\n"
                + "}\n"),
                new File("t/B.java", "package t;\n"
                + "public class B {\n"
                + "    /** Something about i */\n"
                + "    public static int i;\n"
                + "    public void foo() {\n"
                + "        System.out.println(i);\n"
                + "    }\n"
                + "}\n"),
                new File("t/C.java", "package t;\n"
                + "public class C {\n"
                + "    public void foo() {\n"
                + "        System.out.println(B.i);\n"
                + "    }\n"
                + "}\n"));
    }

    public void testMoveProperty() throws Exception {
        writeFilesAndWaitForScan(src,
                new File("t/A.java", "package t;\n"
                + "public class A {\n"
                + "    private int i;\n"
                + "    /** Something about i */\n"
                + "    public int getI() {\n"
                + "        return i;\n"
                + "    }\n"
                + "    /** Something about i */\n"
                + "    public void setI(int newI) {\n"
                + "        i = newI;\n"
                + "    }\n"
                + "}\n"),
                new File("t/B.java", "package t;\n"
                + "public class B {\n"
                + "}\n"),
                new File("t/C.java", "package t;\n"
                + "public class C {\n"
                + "}\n"));
        performMove(src.getFileObject("t/A.java"), new int[]{1, 2, 3}, src.getFileObject("t/B.java"), Visibility.ASIS, false);
        verifyContent(src,
                new File("t/A.java", "package t;\n"
                + "public class A {\n"
                + "}\n"),
                new File("t/B.java", "package t;\n"
                + "public class B {\n"
                + "    private int i;\n"
                + "    /** Something about i */\n"
                + "    public int getI() {\n"
                + "        return i;\n"
                + "    }\n"
                + "    /** Something about i */\n"
                + "    public void setI(int newI) {\n"
                + "        i = newI;\n"
                + "    }\n"
                + "}\n"),
                new File("t/C.java", "package t;\n"
                + "public class C {\n"
                + "}\n"));
    }

    public void testMoveInitializedField() throws Exception {
        writeFilesAndWaitForScan(src,
                new File("t/A.java", "package t;\n"
                + "public class A {\n"
                + "    private int i;\n"
                + "    private int k = calculate();\n"
                + "\n"
                + "    public static int calculate() {\n"
                + "        retun 42;\n"
                + "    }\n"
                + "}\n"),
                new File("v/B.java", "package v;\n"
                + "public class B {\n"
                + "}\n"),
                new File("t/C.java", "package t;\n"
                + "public class C {\n"
                + "}\n"));
        performMove(src.getFileObject("t/A.java"), new int[]{2}, src.getFileObject("v/B.java"), Visibility.ASIS, false);
        verifyContent(src,
                new File("t/A.java", "package t;\n"
                + "public class A {\n"
                + "    private int i;\n"
                + "\n"
                + "    public static int calculate() {\n"
                + "        retun 42;\n"
                + "    }\n"
                + "}\n"),
                new File("v/B.java", "package v;\n"
                + "import t.A;\n"
                + "public class B {\n"
                + "    private int k = A.calculate();\n"
                + "}\n"),
                new File("t/C.java", "package t;\n"
                + "public class C {\n"
                + "}\n"));
    }
    
    public void testMoveInitializedFieldSystem() throws Exception {
        writeFilesAndWaitForScan(src,
                new File("t/A.java", "package t;\n"
                + "public class A {\n"
                + "    private int i;\n"
                + "    private int k = System.identityHashCode(System.out);\n"
                + "\n"
                + "    public static int calculate() {\n"
                + "        retun 42;\n"
                + "    }\n"
                + "}\n"),
                new File("v/B.java", "package v;\n"
                + "public class B {\n"
                + "}\n"),
                new File("t/C.java", "package t;\n"
                + "public class C {\n"
                + "}\n"));
        performMove(src.getFileObject("t/A.java"), new int[]{2}, src.getFileObject("v/B.java"), Visibility.ASIS, false);
        verifyContent(src,
                new File("t/A.java", "package t;\n"
                + "public class A {\n"
                + "    private int i;\n"
                + "\n"
                + "    public static int calculate() {\n"
                + "        retun 42;\n"
                + "    }\n"
                + "}\n"),
                new File("v/B.java", "package v;\n"
                + "public class B {\n"
                + "    private int k = System.identityHashCode(System.out);\n"
                + "}\n"),
                new File("t/C.java", "package t;\n"
                + "public class C {\n"
                + "}\n"));
    }
    
    public void testMoveInitializedField2() throws Exception {
        writeFilesAndWaitForScan(src,
                new File("t/A.java", "package t;\n"
                + "public class A {\n"
                + "    private int i;\n"
                + "    private int k = calculate();\n"
                + "\n"
                + "    public int calculate() {\n"
                + "        retun 42;\n"
                + "    }\n"
                + "}\n"),
                new File("v/B.java", "package v;\n"
                + "public class B {\n"
                + "}\n"),
                new File("t/C.java", "package t;\n"
                + "public class C {\n"
                + "}\n"));
        performMove(src.getFileObject("t/A.java"), new int[]{2}, src.getFileObject("v/B.java"), Visibility.ASIS, false, new Problem(false, "WRN_InitNoAccess"));
        verifyContent(src,
                new File("t/A.java", "package t;\n"
                + "public class A {\n"
                + "    private int i;\n"
                + "\n"
                + "    public int calculate() {\n"
                + "        retun 42;\n"
                + "    }\n"
                + "}\n"),
                new File("v/B.java", "package v;\n"
                + "public class B {\n"
                + "    private int k = calculate();\n"
                + "}\n"),
                new File("t/C.java", "package t;\n"
                + "public class C {\n"
                + "}\n"));
    }
    
    
    public void testMoveGenericField() throws Exception {
        writeFilesAndWaitForScan(src,
                new File("t/A.java", "package t;\n"
                + "public class A <E> {\n"
                + "    private E i;\n"
                + "}\n"),
                new File("v/B.java", "package v;\n"
                + "public class B {\n"
                + "}\n"));
        performMove(src.getFileObject("t/A.java"), new int[]{1}, src.getFileObject("v/B.java"), Visibility.ASIS, false, new Problem(true, "ERR_MoveGenericField"));
    }
}
