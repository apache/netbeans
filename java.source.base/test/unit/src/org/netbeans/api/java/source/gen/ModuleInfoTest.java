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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
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

import java.io.*;
import java.util.*;
import com.sun.source.tree.*;
import org.netbeans.api.java.source.*;
import static org.netbeans.api.java.source.JavaSource.*;
import org.netbeans.junit.NbTestSuite;

/**
 *
 * @author Dusan Balek
 */
public class ModuleInfoTest extends GeneratorTestMDRCompat {
    
    /** Creates a new instance of ModuleInfoTest */
    public ModuleInfoTest(String testName) {
        super(testName);
    }
    
    public static NbTestSuite suite() {
        NbTestSuite suite = new NbTestSuite();
        suite.addTestSuite(ModuleInfoTest.class);
        return suite;
    }
    
    public void testRename() throws Exception {
        testFile = new File(getWorkDir(), "module-info.java");
        TestUtilities.copyStringToFile(testFile, 
                "module test {\n" +
                "}\n"
            );
        String golden =
            "module hierbas.test {\n" +
            "}\n";

        JavaSource src = getJavaSource(testFile);
        Task<WorkingCopy> task = new Task<WorkingCopy>() {

            public void run(WorkingCopy workingCopy) throws IOException {
                workingCopy.toPhase(Phase.RESOLVED);
                CompilationUnitTree cut = workingCopy.getCompilationUnit();
                TreeMaker make = workingCopy.getTreeMaker();

                for (Tree typeDecl : cut.getTypeDecls()) {
                    // ensure that it is module
                    if (typeDecl.getKind() == Tree.Kind.MODULE) {
                        ExpressionTree nju = make.QualIdent("hierbas.test");
                        workingCopy.rewrite(((ModuleTree)typeDecl).getName(), nju);
                    }
                }
            }
            
        };
        src.runModificationTask(task).commit();
        String res = TestUtilities.copyFileToString(testFile);
        System.err.println(res);
        assertEquals(golden, res);
    }

    public void testRemoveAll() throws Exception {
        testFile = new File(getWorkDir(), "module-info.java");
        TestUtilities.copyStringToFile(testFile, 
                "module test {\n" +
                "\n" +
                "    requires java.base;\n" +
                "    requires java.desktop;\n" +
                "\n" +
                "    exports hierbas.del.litoral;\n" +
                "}\n"
            );
        String golden =
            "module test {\n" +
            "}\n";

        JavaSource src = getJavaSource(testFile);
        Task<WorkingCopy> task = new Task<WorkingCopy>() {

            public void run(WorkingCopy workingCopy) throws IOException {
                workingCopy.toPhase(Phase.RESOLVED);
                CompilationUnitTree cut = workingCopy.getCompilationUnit();
                TreeMaker make = workingCopy.getTreeMaker();

                for (Tree typeDecl : cut.getTypeDecls()) {
                    // ensure that it is module
                    if (typeDecl.getKind() == Tree.Kind.MODULE) {
                        ModuleTree moduleTree = (ModuleTree) typeDecl;
                        ModuleTree nju = moduleTree;
                        for (DirectiveTree tree : moduleTree.getDirectives()) {
                            nju = make.removeModuleDirective(nju, tree);
                        }
                        workingCopy.rewrite(moduleTree, nju);
                    }
                }
            }
            
        };
        src.runModificationTask(task).commit();
        String res = TestUtilities.copyFileToString(testFile);
        System.err.println(res);
        assertEquals(golden, res);
    }

    public void testAddAtIndex0() throws Exception {
        testFile = new File(getWorkDir(), "module-info.java");
        TestUtilities.copyStringToFile(testFile, 
            "module test {\n" +
            "    exports hierbas.del.litoral;\n" +
            "}\n"
            );
        String golden =
            "module test {\n" +
            "    requires java.base;\n" +
            "    exports hierbas.del.litoral;\n" +
            "}\n";

        JavaSource src = getJavaSource(testFile);
        Task<WorkingCopy> task = new Task<WorkingCopy>() {

            public void run(WorkingCopy workingCopy) throws IOException {
                workingCopy.toPhase(Phase.RESOLVED);
                CompilationUnitTree cut = workingCopy.getCompilationUnit();
                TreeMaker make = workingCopy.getTreeMaker();

                for (Tree typeDecl : cut.getTypeDecls()) {
                    // ensure that it is module
                    if (typeDecl.getKind() == Tree.Kind.MODULE) {
                        ModuleTree moduleTree = (ModuleTree) typeDecl;
                        ExpressionTree name = make.QualIdent("java.base");
                        ModuleTree copy = make.insertModuleDirective(moduleTree, 0, make.Requires(false, false, name));
                        workingCopy.rewrite(moduleTree, copy);
                    }
                }
            }
            
        };
        src.runModificationTask(task).commit();
        String res = TestUtilities.copyFileToString(testFile);
        System.err.println(res);
        assertEquals(golden, res);
    }
    
    public void testAddToEnd() throws Exception {
        testFile = new File(getWorkDir(), "module-info.java");
        TestUtilities.copyStringToFile(testFile, 
            "module test {\n" +
            "    requires java.base;\n" +
            "}\n"
            );
        String golden =
            "module test {\n" +
            "    requires java.base;\n" +
            "    exports hierbas.del.litoral;\n" +
            "}\n";

        JavaSource src = getJavaSource(testFile);
        Task<WorkingCopy> task = new Task<WorkingCopy>() {

            public void run(WorkingCopy workingCopy) throws IOException {
                workingCopy.toPhase(Phase.RESOLVED);
                CompilationUnitTree cut = workingCopy.getCompilationUnit();
                TreeMaker make = workingCopy.getTreeMaker();
                for (Tree typeDecl : cut.getTypeDecls()) {
                    // ensure that it is module
                    if (typeDecl.getKind() == Tree.Kind.MODULE) {
                        ModuleTree moduleTree = (ModuleTree) typeDecl;
                        ExpressionTree pkgName = make.QualIdent("hierbas.del.litoral");
                        ModuleTree copy = make.addModuleDirective(moduleTree, make.Exports(pkgName, Collections.emptyList()));
                        workingCopy.rewrite(moduleTree, copy);
                    }
                }
            }
            
        };
        src.runModificationTask(task).commit();
        String res = TestUtilities.copyFileToString(testFile);
        System.err.println(res);
        assertEquals(golden, res);
    }
    
    public void testAddToEmpty() throws Exception {
        testFile = new File(getWorkDir(), "module-info.java");
        TestUtilities.copyStringToFile(testFile, 
            "module test {\n" +
            "}\n"
            );
        String golden =
            "module test {\n" +
            "    exports hierbas.del.litoral;\n" +
            "}\n";

        JavaSource src = getJavaSource(testFile);
        Task<WorkingCopy> task = new Task<WorkingCopy>() {

            public void run(WorkingCopy workingCopy) throws IOException {
                workingCopy.toPhase(Phase.RESOLVED);
                CompilationUnitTree cut = workingCopy.getCompilationUnit();
                TreeMaker make = workingCopy.getTreeMaker();

                for (Tree typeDecl : cut.getTypeDecls()) {
                    // ensure that it is module
                    if (typeDecl.getKind() == Tree.Kind.MODULE) {
                        ModuleTree moduleTree = (ModuleTree) typeDecl;
                        ExpressionTree pkgName = make.QualIdent("hierbas.del.litoral");
                        ModuleTree copy = make.addModuleDirective(moduleTree, make.Exports(pkgName, Collections.emptyList()));
                        workingCopy.rewrite(moduleTree, copy);
                    }
                }
            }
            
        };
        src.runModificationTask(task).commit();
        String res = TestUtilities.copyFileToString(testFile);
        System.err.println(res);
        assertEquals(golden, res);
    }
    
    String getGoldenPckg() {
        return "";
    }

    String getSourcePckg() {
        return "";
    }

    @Override
    String getSourceLevel() {
        return "1.9";
    }    
}
