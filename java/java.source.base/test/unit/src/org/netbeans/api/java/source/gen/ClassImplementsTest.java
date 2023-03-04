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
import com.sun.source.tree.IdentifierTree;
import com.sun.source.tree.Tree;
import java.io.File;
import java.io.IOException;
import org.netbeans.api.java.source.GeneratorUtilities;
import org.netbeans.api.java.source.Task;
import org.netbeans.api.java.source.JavaSource;
import static org.netbeans.api.java.source.JavaSource.*;
import org.netbeans.api.java.source.TestUtilities;
import org.netbeans.api.java.source.TreeMaker;
import org.netbeans.api.java.source.WorkingCopy;
import org.netbeans.junit.NbTestSuite;

/**
 * Tests modifying implements clause in the source.
 *
 * @author Pavel Flaska
 */
public class ClassImplementsTest extends GeneratorTestMDRCompat {
    
    /** Creates a new instance of ClassImplementsTest */
    public ClassImplementsTest(String testName) {
        super(testName);
    }
    
    public static NbTestSuite suite() {
        NbTestSuite suite = new NbTestSuite();
        suite.addTestSuite(ClassImplementsTest.class);
//        suite.addTest(new ClassImplementsTest("testAddFirst"));
//        suite.addTest(new ClassImplementsTest("testAddFirstToExisting"));
//        suite.addTest(new ClassImplementsTest("testAddFirstTwo"));
//        suite.addTest(new ClassImplementsTest("testAddThirdToExisting"));
//        suite.addTest(new ClassImplementsTest("testRemoveAll"));
//        suite.addTest(new ClassImplementsTest("testRemoveMid"));
//        suite.addTest(new ClassImplementsTest("testRemoveFirst"));
//        suite.addTest(new ClassImplementsTest("testRemoveFirstTwo"));
//        suite.addTest(new ClassImplementsTest("testRemoveLast"));
//        suite.addTest(new ClassImplementsTest("testRemoveJust"));
//        suite.addTest(new ClassImplementsTest("testAddFirstTypeParam"));
//        suite.addTest(new ClassImplementsTest("testRemoveAllImplTypeParam"));
//        suite.addTest(new ClassImplementsTest("testAddFirstImplExtends"));
//        suite.addTest(new ClassImplementsTest("testRemoveAllImplExtends"));
//        suite.addTest(new ClassImplementsTest("testRenameInImpl"));
//        suite.addTest(new ClassImplementsTest("testRenameInImpl2"));
//        suite.addTest(new ClassImplementsTest("test156731"));
        return suite;
    }
    
    public void testAddFirst() throws Exception {
        testFile = new File(getWorkDir(), "Test.java");
        TestUtilities.copyStringToFile(testFile, 
            "package hierbas.del.litoral;\n\n" +
            "import java.util.*;\n\n" +
            "public class Test {\n" +
            "    public void taragui() {\n" +
            "    }\n" +
            "}\n"
            );
        String golden =
            "package hierbas.del.litoral;\n\n" +
            "import java.util.*;\n\n" +
            "public class Test implements List {\n" +
            "    public void taragui() {\n" +
            "    }\n" +
            "}\n";

        JavaSource src = getJavaSource(testFile);
        Task task = new Task<WorkingCopy>() {

            public void run(WorkingCopy workingCopy) throws IOException {
                workingCopy.toPhase(Phase.RESOLVED);
                CompilationUnitTree cut = workingCopy.getCompilationUnit();
                TreeMaker make = workingCopy.getTreeMaker();
                for (Tree typeDecl : cut.getTypeDecls()) {
                    // should check kind, here we can be sure!
                    ClassTree clazz = (ClassTree) typeDecl;
                    ClassTree copy = make.addClassImplementsClause(
                        clazz, make.Identifier("List")
                    );
                    workingCopy.rewrite(clazz, copy);
                }
            }
            
        };
        src.runModificationTask(task).commit();
        String res = TestUtilities.copyFileToString(testFile);
        //System.err.println(res);
        assertEquals(golden, res);
    }
    
//    public void testAddMakeInterface() throws Exception {
//        testFile = new File(getWorkDir(), "Hombre.java");
//        TestUtilities.copyStringToFile(testFile, 
//            "package hierbas.del.litoral;\n\n" +
//            "import java.util.*;\n\n" +
//            "public class Hombre {\n" +
//            "    public void taragui() {\n" +
//            "    }\n" +
//            "}\n"
//            );
//        String golden =
//            "package hierbas.del.litoral;\n\n" +
//            "import java.util.*;\n\n" +
//            "public class Hombre implements DeMundo {\n" +
//            "    public void taragui() {\n" +
//            "    }\n" +
//            "}\n";
//
//        JavaSource src = getJavaSource(testFile);
//        Task task = new Task<WorkingCopy>() {
//
//            public void run(WorkingCopy workingCopy) throws IOException {
//                workingCopy.toPhase(Phase.RESOLVED);
//                CompilationUnitTree cut = workingCopy.getCompilationUnit();
//                TreeMaker make = workingCopy.getTreeMaker();
//                ClassTree inf = make.Interface(
//                        make.Modifiers(Collections.<Modifier>emptySet()),
//                        "DeMundo",
//                        Collections.<TypeParameterTree>emptyList(),
//                        Collections.<ExpressionTree>emptyList(),
//                        Collections.<Tree>emptyList()
//                );
//                for (Tree typeDecl : cut.getTypeDecls()) {
//                    // should check kind, here we can be sure!
//                    ClassTree clazz = (ClassTree) typeDecl;
//                    ClassTree copy = make.addClassImplementsClause(
//                        clazz, inf
//                    );
//                    workingCopy.rewrite(clazz, copy);
//                }
//            }
//            
//            public void cancel() {
//                throw new UnsupportedOperationException("Not supported yet.");
//            }
//        };
//        src.runModificationTask(task).commit();
//        String res = TestUtilities.copyFileToString(testFile);
//        //System.err.println(res);
//        assertEquals(golden, res);
//    }

    public void testAddFirstTwo() throws Exception {
        testFile = new File(getWorkDir(), "Test.java");
        TestUtilities.copyStringToFile(testFile, 
            "package hierbas.del.litoral;\n\n" +
            "import java.io.*;\n\n" +
            "public class Test extends Object {\n" +
            "    public void taragui() {\n" +
            "    }\n" +
            "}\n"
            );
        String golden =
            "package hierbas.del.litoral;\n\n" +
            "import java.io.*;\n\n" +
            "public class Test extends Object implements Collection, List {\n" +
            "    public void taragui() {\n" +
            "    }\n" +
            "}\n";

        JavaSource src = getJavaSource(testFile);
        Task task = new Task<WorkingCopy>() {

            public void run(WorkingCopy workingCopy) throws IOException {
                workingCopy.toPhase(Phase.RESOLVED);
                CompilationUnitTree cut = workingCopy.getCompilationUnit();
                TreeMaker make = workingCopy.getTreeMaker();
                for (Tree typeDecl : cut.getTypeDecls()) {
                    // should check kind, here we can be sure!
                    ClassTree clazz = (ClassTree) typeDecl;
                    ClassTree copy = make.addClassImplementsClause(
                        clazz, make.Identifier("Collection")
                    );
                    copy = make.addClassImplementsClause(
                        copy, make.Identifier("List")
                    );
                    workingCopy.rewrite(clazz, copy);
                }
            }
        
        };
        src.runModificationTask(task).commit();
        String res = TestUtilities.copyFileToString(testFile);
        //System.err.println(res);
        assertEquals(golden, res);
    }
    
    public void testRemoveAll() throws Exception {
        testFile = new File(getWorkDir(), "Test.java");
        TestUtilities.copyStringToFile(testFile, 
            "package hierbas.del.litoral;\n\n" +
            "import java.io.*;\n\n" +
            "public class Test implements Serializable, List, Collection {\n" +
            "    public void taragui() {\n" +
            "    }\n" +
            "}\n"
            );
        String golden =
            "package hierbas.del.litoral;\n\n" +
            "import java.io.*;\n\n" +
            "public class Test {\n" +
            "    public void taragui() {\n" +
            "    }\n" +
            "}\n";

        JavaSource src = getJavaSource(testFile);
        Task task = new Task<WorkingCopy>() {

            public void run(WorkingCopy workingCopy) throws IOException {
                workingCopy.toPhase(Phase.RESOLVED);
                CompilationUnitTree cut = workingCopy.getCompilationUnit();
                TreeMaker make = workingCopy.getTreeMaker();
                for (Tree typeDecl : cut.getTypeDecls()) {
                    // should check kind, here we can be sure!
                    ClassTree clazz = (ClassTree) typeDecl;
                    ClassTree copy = make.removeClassImplementsClause(clazz, 0);
                    copy = make.removeClassImplementsClause(copy, 0);
                    copy = make.removeClassImplementsClause(copy, 0);
                    workingCopy.rewrite(clazz, copy);
                }
            }
        
        };
        src.runModificationTask(task).commit();
        String res = TestUtilities.copyFileToString(testFile);
        //System.err.println(res);
        assertEquals(golden, res);
    }
    
    public void testAddThirdToExisting() throws Exception {
        testFile = new File(getWorkDir(), "Test.java");
        TestUtilities.copyStringToFile(testFile, 
            "package hierbas.del.litoral;\n\n" +
            "import java.util.*;\n\n" +
            "public class Test implements List, Serializable {\n" +
            "    public void taragui() {\n" +
            "    }\n" +
            "}\n"
            );
        String golden =
            "package hierbas.del.litoral;\n\n" +
            "import java.util.*;\n\n" +
            "public class Test implements List, Serializable, Collection {\n" +
            "    public void taragui() {\n" +
            "    }\n" +
            "}\n";

        JavaSource src = getJavaSource(testFile);
        Task task = new Task<WorkingCopy>() {

            public void run(WorkingCopy workingCopy) throws IOException {
                workingCopy.toPhase(Phase.RESOLVED);
                CompilationUnitTree cut = workingCopy.getCompilationUnit();
                TreeMaker make = workingCopy.getTreeMaker();
                for (Tree typeDecl : cut.getTypeDecls()) {
                    // should check kind, here we can be sure!
                    ClassTree clazz = (ClassTree) typeDecl;
                    ClassTree copy = make.addClassImplementsClause(
                        clazz, make.Identifier("Collection")
                    );
                    workingCopy.rewrite(clazz, copy);
                }
            }
        
        };
        src.runModificationTask(task).commit();
        String res = TestUtilities.copyFileToString(testFile);
        //System.err.println(res);
        assertEquals(golden, res);
    }
    
    public void testAddFirstToExisting() throws Exception {
        testFile = new File(getWorkDir(), "Test.java");
        TestUtilities.copyStringToFile(testFile, 
            "package hierbas.del.litoral;\n\n" +
            "import java.util.*;\n\n" +
            "public class Test implements List {\n" +
            "    public void taragui() {\n" +
            "    }\n" +
            "}\n"
            );
        String golden =
            "package hierbas.del.litoral;\n\n" +
            "import java.util.*;\n\n" +
            "public class Test implements Serializable, List {\n" +
            "    public void taragui() {\n" +
            "    }\n" +
            "}\n";

        JavaSource src = getJavaSource(testFile);
        Task task = new Task<WorkingCopy>() {

            public void run(WorkingCopy workingCopy) throws IOException {
                workingCopy.toPhase(Phase.RESOLVED);
                CompilationUnitTree cut = workingCopy.getCompilationUnit();
                TreeMaker make = workingCopy.getTreeMaker();
                for (Tree typeDecl : cut.getTypeDecls()) {
                    // should check kind, here we can be sure!
                    ClassTree clazz = (ClassTree) typeDecl;
                    ClassTree copy = make.insertClassImplementsClause(
                        clazz, 0, make.Identifier("Serializable")
                    );
                    workingCopy.rewrite(clazz, copy);
                }
            }
        
        };
        src.runModificationTask(task).commit();
        String res = TestUtilities.copyFileToString(testFile);
        //System.err.println(res);
        assertEquals(golden, res);
    }
    
    public void testRemoveMid() throws Exception {
        testFile = new File(getWorkDir(), "Test.java");
        TestUtilities.copyStringToFile(testFile, 
            "package hierbas.del.litoral;\n\n" +
            "import java.util.*;\n\n" +
            "public class Test implements List, Serializable, Collection {\n" +
            "    public void taragui() {\n" +
            "    }\n" +
            "}\n"
            );
        String golden =
            "package hierbas.del.litoral;\n\n" +
            "import java.util.*;\n\n" +
            "public class Test implements List, Collection {\n" +
            "    public void taragui() {\n" +
            "    }\n" +
            "}\n";

        JavaSource src = getJavaSource(testFile);
        Task task = new Task<WorkingCopy>() {

            public void run(WorkingCopy workingCopy) throws IOException {
                workingCopy.toPhase(Phase.RESOLVED);
                CompilationUnitTree cut = workingCopy.getCompilationUnit();
                TreeMaker make = workingCopy.getTreeMaker();
                for (Tree typeDecl : cut.getTypeDecls()) {
                    // should check kind, here we can be sure!
                    ClassTree clazz = (ClassTree) typeDecl;
                    ClassTree copy = make.removeClassImplementsClause(clazz, 1);
                    workingCopy.rewrite(clazz, copy);
                }
            }
            
        };
        src.runModificationTask(task).commit();
        String res = TestUtilities.copyFileToString(testFile);
        //System.err.println(res);
        assertEquals(golden, res);
    }
    
    public void testRemoveFirst() throws Exception {
        testFile = new File(getWorkDir(), "Test.java");
        TestUtilities.copyStringToFile(testFile, 
            "package hierbas.del.litoral;\n\n" +
            "import java.util.*;\n\n" +
            "public class Test implements List {\n" +
            "    public void taragui() {\n" +
            "    }\n" +
            "}\n"
            );
        String golden =
            "package hierbas.del.litoral;\n\n" +
            "import java.util.*;\n\n" +
            "public class Test {\n" +
            "    public void taragui() {\n" +
            "    }\n" +
            "}\n";

        JavaSource src = getJavaSource(testFile);
        Task task = new Task<WorkingCopy>() {

            public void run(WorkingCopy workingCopy) throws IOException {
                workingCopy.toPhase(Phase.RESOLVED);
                CompilationUnitTree cut = workingCopy.getCompilationUnit();
                TreeMaker make = workingCopy.getTreeMaker();
                for (Tree typeDecl : cut.getTypeDecls()) {
                    // should check kind, here we can be sure!
                    ClassTree clazz = (ClassTree) typeDecl;
                    ClassTree copy = make.removeClassImplementsClause(clazz, 0);
                    workingCopy.rewrite(clazz, copy);
                }
            }
        
        };
        src.runModificationTask(task).commit();
        String res = TestUtilities.copyFileToString(testFile);
        //System.err.println(res);
        assertEquals(golden, res);
    }

    public void testRemoveFirstTwo() throws Exception {
        testFile = new File(getWorkDir(), "Test.java");
        TestUtilities.copyStringToFile(testFile,
            "package hierbas.del.litoral;\n\n" +
            "import java.util.*;\n\n" +
            "public class Test implements List, Collection, Serializable {\n" +
            "    public void taragui() {\n" +
            "    }\n" +
            "}\n"
            );
        String golden =
            "package hierbas.del.litoral;\n\n" +
            "import java.util.*;\n\n" +
            "public class Test implements Serializable {\n" +
            "    public void taragui() {\n" +
            "    }\n" +
            "}\n";

        JavaSource src = getJavaSource(testFile);
        Task task = new Task<WorkingCopy>() {

            public void run(WorkingCopy workingCopy) throws IOException {
                workingCopy.toPhase(Phase.RESOLVED);
                CompilationUnitTree cut = workingCopy.getCompilationUnit();
                TreeMaker make = workingCopy.getTreeMaker();
                for (Tree typeDecl : cut.getTypeDecls()) {
                    // should check kind, here we can be sure!
                    ClassTree clazz = (ClassTree) typeDecl;
                    ClassTree copy = make.removeClassImplementsClause(clazz, 0);
                    copy = make.removeClassImplementsClause(copy, 0);
                    workingCopy.rewrite(clazz, copy);
                }
            }

        };
        src.runModificationTask(task).commit();
        String res = TestUtilities.copyFileToString(testFile);
        //System.err.println(res);
        assertEquals(golden, res);
    }
    
    public void testRemoveLast() throws Exception {
        testFile = new File(getWorkDir(), "Test.java");
        TestUtilities.copyStringToFile(testFile, 
            "package hierbas.del.litoral;\n\n" +
            "import java.util.*;\n\n" +
            "public class Test implements List, Collection, Serializable {\n" +
            "    public void taragui() {\n" +
            "    }\n" +
            "}\n"
            );
        String golden =
            "package hierbas.del.litoral;\n\n" +
            "import java.util.*;\n\n" +
            "public class Test implements List, Collection {\n" +
            "    public void taragui() {\n" +
            "    }\n" +
            "}\n";

        JavaSource src = getJavaSource(testFile);
        Task task = new Task<WorkingCopy>() {

            public void run(WorkingCopy workingCopy) throws IOException {
                workingCopy.toPhase(Phase.RESOLVED);
                CompilationUnitTree cut = workingCopy.getCompilationUnit();
                TreeMaker make = workingCopy.getTreeMaker();
                for (Tree typeDecl : cut.getTypeDecls()) {
                    // should check kind, here we can be sure!
                    ClassTree clazz = (ClassTree) typeDecl;
                    ClassTree copy = make.removeClassImplementsClause(clazz, 2);
                    workingCopy.rewrite(clazz, copy);
                }
            }
            
        };
        src.runModificationTask(task).commit();
        String res = TestUtilities.copyFileToString(testFile);
        //System.err.println(res);
        assertEquals(golden, res);
    }
    
    public void testRemoveJust() throws Exception {
        testFile = new File(getWorkDir(), "Test.java");
        TestUtilities.copyStringToFile(testFile, 
            "package hierbas.del.litoral;\n\n" +
            "import java.util.*;\n\n" +
            "public class Test implements List, Collection {\n" +
            "    public void taragui() {\n" +
            "    }\n" +
            "}\n"
            );
        String golden =
            "package hierbas.del.litoral;\n\n" +
            "import java.util.*;\n\n" +
            "public class Test {\n" +
            "    public void taragui() {\n" +
            "    }\n" +
            "}\n";

        JavaSource src = getJavaSource(testFile);
        Task task = new Task<WorkingCopy>() {

            public void run(WorkingCopy workingCopy) throws IOException {
                workingCopy.toPhase(Phase.RESOLVED);
                CompilationUnitTree cut = workingCopy.getCompilationUnit();
                TreeMaker make = workingCopy.getTreeMaker();
                for (Tree typeDecl : cut.getTypeDecls()) {
                    // should check kind, here we can be sure!
                    ClassTree clazz = (ClassTree) typeDecl;
                    ClassTree copy = make.removeClassImplementsClause(clazz, 1);
                    copy = make.removeClassImplementsClause(copy, 0);
                    workingCopy.rewrite(clazz, copy);
                }
            }
            
        };
        src.runModificationTask(task).commit();
        String res = TestUtilities.copyFileToString(testFile);
        //System.err.println(res);
        assertEquals(golden, res);
    }
    
    public void testAddFirstTypeParam() throws Exception {
        testFile = new File(getWorkDir(), "Test.java");
        TestUtilities.copyStringToFile(testFile, 
            "package hierbas.del.litoral;\n\n" +
            "import java.util.*;\n\n" +
            "public class Test<E> {\n" +
            "    public void taragui() {\n" +
            "    }\n" +
            "}\n"
            );
        String golden =
            "package hierbas.del.litoral;\n\n" +
            "import java.util.*;\n\n" +
            "public class Test<E> implements List {\n" +
            "    public void taragui() {\n" +
            "    }\n" +
            "}\n";

        JavaSource src = getJavaSource(testFile);
        Task task = new Task<WorkingCopy>() {

            public void run(WorkingCopy workingCopy) throws IOException {
                workingCopy.toPhase(Phase.RESOLVED);
                CompilationUnitTree cut = workingCopy.getCompilationUnit();
                TreeMaker make = workingCopy.getTreeMaker();
                for (Tree typeDecl : cut.getTypeDecls()) {
                    // should check kind, here we can be sure!
                    ClassTree clazz = (ClassTree) typeDecl;
                    ClassTree copy = make.addClassImplementsClause(
                        clazz, make.Identifier("List")
                    );
                    workingCopy.rewrite(clazz, copy);
                }
            }
        
        };
        src.runModificationTask(task).commit();
        String res = TestUtilities.copyFileToString(testFile);
        //System.err.println(res);
        assertEquals(golden, res);
    }
    
    public void testRemoveAllImplTypeParam() throws Exception {
        testFile = new File(getWorkDir(), "Test.java");
        TestUtilities.copyStringToFile(testFile, 
            "package hierbas.del.litoral;\n\n" +
            "import java.util.*;\n\n" +
            "public class Test<E> implements List {\n" +
            "    public void taragui() {\n" +
            "    }\n" +
            "}\n"
            );
        String golden =
            "package hierbas.del.litoral;\n\n" +
            "import java.util.*;\n\n" +
            "public class Test<E> {\n" +
            "    public void taragui() {\n" +
            "    }\n" +
            "}\n";
        
        JavaSource src = getJavaSource(testFile);
        Task task = new Task<WorkingCopy>() {

            public void run(WorkingCopy workingCopy) throws IOException {
                workingCopy.toPhase(Phase.RESOLVED);
                CompilationUnitTree cut = workingCopy.getCompilationUnit();
                TreeMaker make = workingCopy.getTreeMaker();
                for (Tree typeDecl : cut.getTypeDecls()) {
                    // should check kind, here we can be sure!
                    ClassTree clazz = (ClassTree) typeDecl;
                    ClassTree copy = make.removeClassImplementsClause(
                        clazz, 0
                    );
                    workingCopy.rewrite(clazz, copy);
                }
            }
            
        };
        src.runModificationTask(task).commit();
        String res = TestUtilities.copyFileToString(testFile);
        //System.err.println(res);
        assertEquals(golden, res);
    }
    
    public void testAddFirstImplExtends() throws Exception {
        testFile = new File(getWorkDir(), "Test.java");
        TestUtilities.copyStringToFile(testFile, 
            "package hierbas.del.litoral;\n\n" +
            "import java.util.*;\n\n" +
            "public class Test<E> extends Object {\n" +
            "    public void taragui() {\n" +
            "    }\n" +
            "}\n"
            );
        String golden =
            "package hierbas.del.litoral;\n\n" +
            "import java.util.*;\n\n" +
            "public class Test<E> extends Object implements List {\n" +
            "    public void taragui() {\n" +
            "    }\n" +
            "}\n";
        
        JavaSource src = getJavaSource(testFile);
        Task task = new Task<WorkingCopy>() {

            public void run(WorkingCopy workingCopy) throws IOException {
                workingCopy.toPhase(Phase.RESOLVED);
                CompilationUnitTree cut = workingCopy.getCompilationUnit();
                TreeMaker make = workingCopy.getTreeMaker();
                for (Tree typeDecl : cut.getTypeDecls()) {
                    // should check kind, here we can be sure!
                    ClassTree clazz = (ClassTree) typeDecl;
                    ClassTree copy = make.addClassImplementsClause(
                        clazz, make.Identifier("List")
                    );
                    workingCopy.rewrite(clazz, copy);
                }
            }
            
        };
        src.runModificationTask(task).commit();
        String res = TestUtilities.copyFileToString(testFile);
        //System.err.println(res);
        assertEquals(golden, res);
    }
    
    public void testRemoveAllImplExtends() throws Exception {
        testFile = new File(getWorkDir(), "Test.java");
        TestUtilities.copyStringToFile(testFile, 
            "package hierbas.del.litoral;\n\n" +
            "import java.util.*;\n\n" +
            "public class Test<E> extends Object implements List {\n" +
            "    public void taragui() {\n" +
            "    }\n" +
            "}\n"
            );
        String golden =
            "package hierbas.del.litoral;\n\n" +
            "import java.util.*;\n\n" +
            "public class Test<E> extends Object {\n" +
            "    public void taragui() {\n" +
            "    }\n" +
            "}\n";
        
        JavaSource src = getJavaSource(testFile);
        Task task = new Task<WorkingCopy>() {

            public void run(WorkingCopy workingCopy) throws IOException {
                workingCopy.toPhase(Phase.RESOLVED);
                CompilationUnitTree cut = workingCopy.getCompilationUnit();
                TreeMaker make = workingCopy.getTreeMaker();
                for (Tree typeDecl : cut.getTypeDecls()) {
                    // should check kind, here we can be sure!
                    ClassTree clazz = (ClassTree) typeDecl;
                    ClassTree copy = make.removeClassImplementsClause(
                        clazz, 0
                    );
                    workingCopy.rewrite(clazz, copy);
                }
            }
        
        };
        src.runModificationTask(task).commit();
        String res = TestUtilities.copyFileToString(testFile);
        //System.err.println(res);
        assertEquals(golden, res);
    }
    
    public void testRenameInImpl() throws Exception {
        testFile = new File(getWorkDir(), "Test.java");
        TestUtilities.copyStringToFile(testFile, 
            "package hierbas.del.litoral;\n\n" +
            "import java.util.*;\n\n" +
            "public class Test<E> extends Object implements List {\n" +
            "    public void taragui() {\n" +
            "    }\n" +
            "}\n"
            );
        String golden =
            "package hierbas.del.litoral;\n\n" +
            "import java.util.*;\n\n" +
            "public class Test<E> extends Object implements Seznam {\n" +
            "    public void taragui() {\n" +
            "    }\n" +
            "}\n";
        
        JavaSource src = getJavaSource(testFile);
        Task task = new Task<WorkingCopy>() {

            public void run(WorkingCopy workingCopy) throws IOException {
                workingCopy.toPhase(Phase.RESOLVED);
                CompilationUnitTree cut = workingCopy.getCompilationUnit();
                TreeMaker make = workingCopy.getTreeMaker();
                for (Tree typeDecl : cut.getTypeDecls()) {
                    // should check kind, here we can be sure!
                    ClassTree clazz = (ClassTree) typeDecl;
                    IdentifierTree ident = (IdentifierTree) clazz.getImplementsClause().get(0);
                    workingCopy.rewrite(ident, make.setLabel(ident, "Seznam"));
                }
            }
        
        };
        src.runModificationTask(task).commit();
        String res = TestUtilities.copyFileToString(testFile);
        //System.err.println(res);
        assertEquals(golden, res);
    }
    
    public void testRenameInImpl2() throws Exception {
        testFile = new File(getWorkDir(), "Test.java");
        TestUtilities.copyStringToFile(testFile, 
            "package hierbas.del.litoral;\n\n" +
            "import java.util.*;\n\n" +
            "public class Test<E> extends Object implements PropertyChangeListener, List {\n" +
            "    public void taragui() {\n" +
            "    }\n" +
            "}\n"
            );
        String golden =
            "package hierbas.del.litoral;\n\n" +
            "import java.util.*;\n\n" +
            "public class Test<E> extends Object implements PropertyChangeListener, Seznam {\n" +
            "    public void taragui() {\n" +
            "    }\n" +
            "}\n";
        
        JavaSource src = getJavaSource(testFile);
        Task task = new Task<WorkingCopy>() {

            public void run(WorkingCopy workingCopy) throws IOException {
                workingCopy.toPhase(Phase.RESOLVED);
                CompilationUnitTree cut = workingCopy.getCompilationUnit();
                TreeMaker make = workingCopy.getTreeMaker();
                for (Tree typeDecl : cut.getTypeDecls()) {
                    // should check kind, here we can be sure!
                    ClassTree clazz = (ClassTree) typeDecl;
                    IdentifierTree ident = (IdentifierTree) clazz.getImplementsClause().get(1);
                    workingCopy.rewrite(ident, make.setLabel(ident, "Seznam"));
                }
            }
        
        };
        src.runModificationTask(task).commit();
        String res = TestUtilities.copyFileToString(testFile);
        //System.err.println(res);
        assertEquals(golden, res);
    }
    
    public void test156731() throws Exception {
        testFile = new File(getWorkDir(), "Test.java");
        TestUtilities.copyStringToFile(testFile,
            "package hierbas.del.litoral;\n\n" +
            "public class Test implements java.awt.event.ActionListener, java.awt.event.MouseListener, javax.swing.event.AncestorListener {\n" +
            "}\n"
            );
        String golden =
            "package hierbas.del.litoral;\n\n" +
            "import java.awt.event.ActionListener;\n" +
            "import java.awt.event.MouseListener;\n" +
            "import javax.swing.event.AncestorListener;\n\n" +
            "public class Test implements ActionListener, MouseListener, AncestorListener {\n" +
            "}\n";

        JavaSource src = getJavaSource(testFile);
        Task task = new Task<WorkingCopy>() {

            public void run(WorkingCopy workingCopy) throws IOException {
                workingCopy.toPhase(Phase.RESOLVED);
                CompilationUnitTree cut = workingCopy.getCompilationUnit();
                workingCopy.rewrite(cut, GeneratorUtilities.get(workingCopy).importFQNs(cut));
            }

        };
        src.runModificationTask(task).commit();
        String res = TestUtilities.copyFileToString(testFile);
        //System.err.println(res);
        assertEquals(golden, res);
    }

    protected void setUp() throws Exception {
        super.setUp();
        testFile = getFile(getSourceDir(), getSourcePckg() + "Test.java");
    }

    String getGoldenPckg() {
        return "";
    }

    String getSourcePckg() {
        return "";
    }
    
}
