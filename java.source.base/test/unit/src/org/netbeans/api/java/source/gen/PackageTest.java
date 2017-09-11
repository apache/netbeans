/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
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
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
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
 */
package org.netbeans.api.java.source.gen;

import com.sun.source.tree.*;
import java.io.File;
import java.io.IOException;
import org.netbeans.api.java.source.*;
import static org.netbeans.api.java.source.JavaSource.*;
import org.netbeans.junit.NbTestSuite;

/**
 * Test packages.
 * 
 * @author Pavel Flaska
 */
public class PackageTest extends GeneratorTestMDRCompat {
    
    /** Creates a new instance of PackageTest */
    public PackageTest(String testName) {
        super(testName);
    }

    public static NbTestSuite suite() {
        NbTestSuite suite = new NbTestSuite();
        suite.addTestSuite(PackageTest.class);
//        suite.addTest(new PackageTest("testChangePackage"));
//        suite.addTest(new PackageTest("testChangeDefToNamedPackage"));
//        suite.addTest(new PackageTest("testChangeDefToNamedPackageWithImport"));
//        suite.addTest(new PackageTest("testChangeToDefPackage"));
//        suite.addTest(new PackageTest("testPackageRenameWithAnnotation"));
        return suite;
    }
    
    /**
     * Change package declaration 'package org.nothing;' to
     * 'package com.unit;'.
     */
    public void testChangePackage() throws Exception {
        testFile = new File(getWorkDir(), "Test.java");
        TestUtilities.copyStringToFile(testFile,
            "/**\n" +
            " * What?\n" +
            " */\n" +
            "package org.nothing;\n\n" +
            "class Test {\n" +
            "}\n"
            );
        String golden = 
            "/**\n" +
            " * What?\n" +
            " */\n" +
            "package com.unit;\n\n" +
            "class Test {\n" +
            "}\n";
        
        JavaSource src = getJavaSource(testFile);
        Task<WorkingCopy> task = new Task<WorkingCopy>() {

            public void run(WorkingCopy workingCopy) throws IOException {
                workingCopy.toPhase(Phase.RESOLVED);
                TreeMaker make = workingCopy.getTreeMaker();
                CompilationUnitTree cut = workingCopy.getCompilationUnit();
                CompilationUnitTree copy = make.CompilationUnit(
                    make.Identifier("com.unit"),
                    cut.getImports(),
                    cut.getTypeDecls(),
                    cut.getSourceFile()
                );
                workingCopy.rewrite(cut, copy);
            }

        };
        src.runModificationTask(task).commit();
        String res = TestUtilities.copyFileToString(testFile);
        System.err.println(res);
        assertEquals(golden, res);
    }
    
    /**
     * Remove the package declartion, i.e. make the class part of
     * default package.
     */
    public void testChangeToDefPackage() throws Exception {
        testFile = new File(getWorkDir(), "Test.java");
        TestUtilities.copyStringToFile(testFile,
            "/**\n" +
            " * What?\n" +
            " */\n" +
            "package org.nothing;\n\n" +
            "class Test {\n" +
            "}\n"
            );
        String golden = 
            "/**\n" +
            " * What?\n" +
            " */\n" +
            "\n\n" +
            "class Test {\n" +
            "}\n";

        JavaSource src = getJavaSource(testFile);
        Task<WorkingCopy> task = new Task<WorkingCopy>() {

            public void run(WorkingCopy workingCopy) throws IOException {
                workingCopy.toPhase(Phase.RESOLVED);
                TreeMaker make = workingCopy.getTreeMaker();
                CompilationUnitTree cut = workingCopy.getCompilationUnit();
                CompilationUnitTree copy = make.CompilationUnit(
                    (ExpressionTree)null,
                    cut.getImports(),
                    cut.getTypeDecls(),
                    cut.getSourceFile()
                );
                workingCopy.rewrite(cut, copy);
            }

        };
        src.runModificationTask(task).commit();
        String res = TestUtilities.copyFileToString(testFile);
        System.err.println(res);
        assertEquals(golden, res);
    }
    
    /**
     * Remove the package declartion, i.e. make the class part of
     * default package.
     */
    public void testChangeDefToNamedPackage() throws Exception {
        testFile = new File(getWorkDir(), "Test.java");
        TestUtilities.copyStringToFile(testFile,
            "/**\n" +
            " * What?\n" +
            " */\n" +
            "class Test {\n" +
            "}\n"
            );
        String golden = 
            "package gro.snaebten.seludom.avaj;\n" +
            "\n" +
            "/**\n" +
            " * What?\n" +
            " */\n" +
            "class Test {\n" +
            "}\n";

        JavaSource src = getJavaSource(testFile);
        Task<WorkingCopy> task = new Task<WorkingCopy>() {

            public void run(WorkingCopy workingCopy) throws IOException {
                workingCopy.toPhase(Phase.RESOLVED);
                TreeMaker make = workingCopy.getTreeMaker();
                CompilationUnitTree cut = workingCopy.getCompilationUnit();
                    CompilationUnitTree copy = make.CompilationUnit(
                        make.Identifier("gro.snaebten.seludom.avaj"),
                        cut.getImports(),
                        cut.getTypeDecls(),
                        cut.getSourceFile()
                    );
                workingCopy.rewrite(cut, copy);
            }

        };
        src.runModificationTask(task).commit();
        String res = TestUtilities.copyFileToString(testFile);
        System.err.println(res);
        assertEquals(golden, res);
    }
    
    /**
     * Remove the package declartion, i.e. make the class part of
     * default package.
     */
    public void testChangeDefToNamedPackageWithImport() throws Exception {
        testFile = new File(getWorkDir(), "Test.java");
        TestUtilities.copyStringToFile(testFile,
            "/**\n" +
            " * What?\n" +
            " */\n" +
            "import gro;\n\n" +
            "class Test {\n" +
            "}\n"
            );
        String golden = 
            "package gro.snaebten.seludom.avaj;\n" +
            "\n" +
            "/**\n" +
            " * What?\n" +
            " */\n" +
            "import gro;\n\n" +
            "class Test {\n" +
            "}\n";

        JavaSource src = getJavaSource(testFile);
        Task<WorkingCopy> task = new Task<WorkingCopy>() {

            public void run(WorkingCopy workingCopy) throws IOException {
                workingCopy.toPhase(Phase.RESOLVED);
                TreeMaker make = workingCopy.getTreeMaker();
                CompilationUnitTree cut = workingCopy.getCompilationUnit();
                    CompilationUnitTree copy = make.CompilationUnit(
                        make.Identifier("gro.snaebten.seludom.avaj"),
                        cut.getImports(),
                        cut.getTypeDecls(),
                        cut.getSourceFile()
                    );
                workingCopy.rewrite(cut, copy);
            }

        };
        src.runModificationTask(task).commit();
        String res = TestUtilities.copyFileToString(testFile);
        System.err.println(res);
        assertEquals(golden, res);
    }
    
    /**
     * #93045: Package annotation removed when renaming package
     */
    public void testPackageRenameWithAnnotation() throws Exception {
        testFile = new File(getWorkDir(), "Test.java");
        TestUtilities.copyStringToFile(testFile,
            "@SuppressWarnings()\n" +
            "package javaapplication1;\n");
        String golden = 
            "@SuppressWarnings()\n" +
            "package app;\n";

        JavaSource src = getJavaSource(testFile);
        Task<WorkingCopy> task = new Task<WorkingCopy>() {

            public void run(WorkingCopy workingCopy) throws IOException {
                workingCopy.toPhase(Phase.RESOLVED);
                TreeMaker make = workingCopy.getTreeMaker();
                ExpressionTree pckgName = workingCopy.getCompilationUnit().getPackageName();
                workingCopy.rewrite(pckgName, make.setLabel(pckgName, "app"));
            }

        };
        src.runModificationTask(task).commit();
        String res = TestUtilities.copyFileToString(testFile);
        System.err.println(res);
        assertEquals(golden, res);
    }
    
    // not important for this test
    String getGoldenPckg() {
        return "";
    }

    String getSourcePckg() {
        return "";
    }

    
}
