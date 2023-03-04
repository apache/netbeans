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
package org.netbeans.api.java.source.gen;

import com.sun.source.tree.ClassTree;
import com.sun.source.tree.CompilationUnitTree;
import com.sun.source.tree.ExpressionTree;
import com.sun.source.tree.Tree;
import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import org.netbeans.api.java.source.Task;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.JavaSource.Phase;
import org.netbeans.api.java.source.TestUtilities;
import org.netbeans.api.java.source.TreeMaker;
import org.netbeans.api.java.source.TreeUtilities;
import org.netbeans.api.java.source.WorkingCopy;

/**
 * Extends in interfaces can contain more identifiers (like implements
 * in classes). Consider and test interface with more extends identifiers.
 * (javac contains these part in ClassTree in implements part.)
 *
 * @author Pavel Flaska
 */
public class InterfaceExtendsTest extends GeneratorTestMDRCompat {
    
    /** Creates a new instance of InterfaceExtendsTest */
    public InterfaceExtendsTest(String testName) {
        super(testName);
    }
    
    public void testAddExtends() throws Exception {
        testFile = new File(getWorkDir(), "Test.java");
        TestUtilities.copyStringToFile(testFile, 
            "package hierbas.del.litoral;\n\n" +
            "import java.util.*;\n\n" +
            "public interface Test {\n" +
            "    public void taragui();\n" +
            "}\n"
            );
        String golden =
            "package hierbas.del.litoral;\n\n" +
            "import java.util.*;\n\n" +
            "public interface Test extends Serializable {\n" +
            "    public void taragui();\n" +
            "}\n";
        JavaSource src = getJavaSource(testFile);
        
        Task task = new Task<WorkingCopy>() {

            public void run(WorkingCopy workingCopy) throws IOException {
                workingCopy.toPhase(Phase.RESOLVED);
                CompilationUnitTree cut = workingCopy.getCompilationUnit();
                TreeMaker make = workingCopy.getTreeMaker();

                for (Tree typeDecl : cut.getTypeDecls()) {
                    // ensure that it is correct type declaration, i.e. class
                    if (TreeUtilities.CLASS_TREE_KINDS.contains(typeDecl.getKind())) {
                        ClassTree classTree = (ClassTree) typeDecl;
                        List<ExpressionTree> implementz = 
                            Collections.<ExpressionTree>singletonList(
                                make.Identifier("Serializable")
                            );
                        ClassTree copy = make.Class(
                                classTree.getModifiers(),
                                classTree.getSimpleName(),
                                classTree.getTypeParameters(),
                                classTree.getExtendsClause(),
                                implementz,
                                classTree.getMembers()
                            );
                        workingCopy.rewrite(classTree, copy);
                    }
                }
            }
            
        };
        src.runModificationTask(task).commit();
        String res = TestUtilities.copyFileToString(testFile);
        assertEquals(golden, res);
    }
    
    public void testAddTwoExtends() throws Exception {
        testFile = new File(getWorkDir(), "Test.java");
        TestUtilities.copyStringToFile(testFile, 
            "package hierbas.del.litoral;\n\n" +
            "import java.util.*;\n\n" +
            "public interface Test {\n" +
            "    public void taragui();\n" +
            "}\n"
            );
        String golden =
            "package hierbas.del.litoral;\n\n" +
            "import java.util.*;\n\n" +
            "public interface Test extends Serializable, Externalizable {\n" +
            "    public void taragui();\n" +
            "}\n";
        JavaSource src = getJavaSource(testFile);
        
        Task task = new Task<WorkingCopy>() {

            public void run(WorkingCopy workingCopy) throws IOException {
                workingCopy.toPhase(Phase.RESOLVED);
                CompilationUnitTree cut = workingCopy.getCompilationUnit();
                TreeMaker make = workingCopy.getTreeMaker();

                for (Tree typeDecl : cut.getTypeDecls()) {
                    // ensure that it is correct type declaration, i.e. class
                    if (TreeUtilities.CLASS_TREE_KINDS.contains(typeDecl.getKind())) {
                        ClassTree classTree = (ClassTree) typeDecl;
                        ClassTree copy = make.addClassImplementsClause(
                                classTree, make.Identifier("Serializable")
                            );
                        copy = make.addClassImplementsClause(
                                copy, make.Identifier("Externalizable")
                            );
                        workingCopy.rewrite(classTree, copy);
                    }
                }
            }
            
        };
        src.runModificationTask(task).commit();
        String res = TestUtilities.copyFileToString(testFile);
        assertEquals(golden, res);
    }
    
    public void testRemoveTwoExtends() throws Exception {
        testFile = new File(getWorkDir(), "Test.java");
        TestUtilities.copyStringToFile(testFile, 
            "package hierbas.del.litoral;\n\n" +
            "import java.util.*;\n\n" +
            "public interface Test extends Serializable, Externalizable {\n" +
            "    public void taragui();\n" +
            "}\n"
            );
        String golden =
            "package hierbas.del.litoral;\n\n" +
            "import java.util.*;\n\n" +
            "public interface Test {\n" +
            "    public void taragui();\n" +
            "}\n";
        JavaSource src = getJavaSource(testFile);
        
        Task task = new Task<WorkingCopy>() {

            public void run(WorkingCopy workingCopy) throws IOException {
                workingCopy.toPhase(Phase.RESOLVED);
                CompilationUnitTree cut = workingCopy.getCompilationUnit();
                TreeMaker make = workingCopy.getTreeMaker();

                for (Tree typeDecl : cut.getTypeDecls()) {
                    // ensure that it is correct type declaration, i.e. class
                    if (TreeUtilities.CLASS_TREE_KINDS.contains(typeDecl.getKind())) {
                        ClassTree classTree = (ClassTree) typeDecl;
                        ClassTree copy = make.removeClassImplementsClause(classTree, 0);
                        copy = make.removeClassImplementsClause(copy, 0);
                        workingCopy.rewrite(classTree, copy);
                    }
                }
            }
            
        };
        src.runModificationTask(task).commit();
        String res = TestUtilities.copyFileToString(testFile);
        assertEquals(golden, res);
    }
    
    String getGoldenPckg() {
        return "";
    }
    
    String getSourcePckg() {
        return "";
    }
    
}
