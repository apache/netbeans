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

import com.sun.source.tree.*;
import org.netbeans.api.java.source.support.ErrorAwareTreeScanner;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import javax.lang.model.element.Modifier;
import org.netbeans.api.java.source.Task;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.JavaSource.*;
import org.netbeans.api.java.source.TestUtilities;
import org.netbeans.api.java.source.TreeMaker;
import org.netbeans.api.java.source.WorkingCopy;
import org.netbeans.junit.NbTestSuite;
import org.openide.filesystems.FileUtil;

/**
 * Test modifications of NewClassTree.
 * 
 * @author Jan Lahoda and Pavel Flaska
 */
public class MultiCatchTest extends GeneratorTestBase {
    
    /** Creates a new instance of TryTest */
    public MultiCatchTest(String name) {
        super(name);
    }
    
    public static NbTestSuite suite() {
        NbTestSuite suite = new NbTestSuite();
        suite.addTestSuite(MultiCatchTest.class);
//        suite.addTest(new NewClassTreeTest("testRemoveClassBody"));
//        suite.addTest(new NewClassTreeTest("testAddArguments"));
        return suite;
    }

    public void testGenerateMultiCatch() throws Exception {
        testFile = new File(getWorkDir(), "Test.java");
        TestUtilities.copyStringToFile(testFile, 
            "package hierbas.del.litoral;\n" +
            "\n" +
            "public class Test {\n" +
            "    public void taragui() {\n" +
            "    }\n" +
            "}\n"
            );
        String golden = 
            "package hierbas.del.litoral;\n" +
            "\n" +
            "public class Test {\n" +
            "    public void taragui() {\n" +
            "        try {\n" +
            "        } catch (final java.net.MalformedURLException | java.io.IOException ex) {\n" +
            "        }\n" +
            "    }\n" +
            "}\n";
        JavaSource testSource = JavaSource.forFileObject(FileUtil.toFileObject(testFile));
        Task task = new Task<WorkingCopy>() {

            public void run(WorkingCopy workingCopy) throws IOException {
                workingCopy.toPhase(Phase.RESOLVED);
                TreeMaker make = workingCopy.getTreeMaker();
                
                ClassTree clazz = (ClassTree) workingCopy.getCompilationUnit().getTypeDecls().get(0);
                MethodTree method = (MethodTree) clazz.getMembers().get(1);
                TryTree tt = make.Try(make.Block(Collections.<StatementTree>emptyList(), false),
                                      Collections.singletonList(make.Catch(make.Variable(make.Modifiers(EnumSet.of(Modifier.FINAL)),
                                                                                         "ex",
                                                                                         make.UnionType(Arrays.asList(make.Identifier("java.net.MalformedURLException"),
                                                                                                                            make.Identifier("java.io.IOException"))
                                                                                                          ),
                                                                                         null),
                                                                           make.Block(Collections.<StatementTree>emptyList(), false))),
                                      null);
                workingCopy.rewrite(method.getBody(), make.addBlockStatement(method.getBody(), tt));
            }
            
        };
        testSource.runModificationTask(task).commit();
        String res = TestUtilities.copyFileToString(testFile);
        //System.err.println(res);
        assertEquals(golden, res);
    }

    public void testRenameInMultiCatch() throws Exception {
        testFile = new File(getWorkDir(), "Test.java");
        TestUtilities.copyStringToFile(testFile,
            "package hierbas.del.litoral;\n" +
            "import java.io.*;\n" +
            "import java.net.*;\n" +
            "public class Test {\n" +
            "    public void taragui() {\n" +
            "        try {\n" +
            "        } catch (MalformedURLException | FileNotFoundException ex) {\n" +
            "        }\n" +
            "    }\n" +
            "}\n"
            );
        String golden =
            "package hierbas.del.litoral;\n" +
            "import java.io.*;\n" +
            "import java.net.*;\n" +
            "public class Test {\n" +
            "    public void taragui() {\n" +
            "        try {\n" +
            "        } catch (MalformedURLException | IOException ex) {\n" +
            "        }\n" +
            "    }\n" +
            "}\n";
        JavaSource testSource = JavaSource.forFileObject(FileUtil.toFileObject(testFile));
        Task task = new Task<WorkingCopy>() {

            public void run(final WorkingCopy workingCopy) throws IOException {
                workingCopy.toPhase(Phase.RESOLVED);
                final TreeMaker make = workingCopy.getTreeMaker();

                new ErrorAwareTreeScanner<Void, Void>() {
                    @Override public Void visitIdentifier(IdentifierTree node, Void p) {
                        if (node.getName().contentEquals("FileNotFoundException")) {
                            workingCopy.rewrite(node, make.Identifier("IOException"));
                        }
                        return super.visitIdentifier(node, p);
                    }
                }.scan(workingCopy.getCompilationUnit(), null);
            }

        };
        testSource.runModificationTask(task).commit();
        String res = TestUtilities.copyFileToString(testFile);
        //System.err.println(res);
        assertEquals(golden, res);
    }

    public void testAddLastToMultiCatch() throws Exception {
        testFile = new File(getWorkDir(), "Test.java");
        TestUtilities.copyStringToFile(testFile,
            "package hierbas.del.litoral;\n" +
            "import java.io.*;\n" +
            "import java.net.*;\n" +
            "public class Test {\n" +
            "    public void taragui() {\n" +
            "        try {\n" +
            "        } catch (MalformedURLException | FileNotFoundException ex) {\n" +
            "        }\n" +
            "    }\n" +
            "}\n"
            );
        String golden =
            "package hierbas.del.litoral;\n" +
            "import java.io.*;\n" +
            "import java.net.*;\n" +
            "public class Test {\n" +
            "    public void taragui() {\n" +
            "        try {\n" +
            "        } catch (MalformedURLException | FileNotFoundException | IOException ex) {\n" +
            "        }\n" +
            "    }\n" +
            "}\n";
        JavaSource testSource = JavaSource.forFileObject(FileUtil.toFileObject(testFile));
        Task task = new Task<WorkingCopy>() {

            public void run(final WorkingCopy workingCopy) throws IOException {
                workingCopy.toPhase(Phase.RESOLVED);
                final TreeMaker make = workingCopy.getTreeMaker();

                new ErrorAwareTreeScanner<Void, Void>() {
                    @Override public Void visitUnionType(UnionTypeTree node, Void p) {
                        List<Tree> alternatives = new ArrayList<Tree>(node.getTypeAlternatives());
                        alternatives.add(make.Identifier("IOException"));
                        workingCopy.rewrite(node, make.UnionType(alternatives));
                        return null;
                    }
                }.scan(workingCopy.getCompilationUnit(), null);
            }

        };
        testSource.runModificationTask(task).commit();
        String res = TestUtilities.copyFileToString(testFile);
        //System.err.println(res);
        assertEquals(golden, res);
    }

    public void testAddFirstToMultiCatch() throws Exception {
        testFile = new File(getWorkDir(), "Test.java");
        TestUtilities.copyStringToFile(testFile,
            "package hierbas.del.litoral;\n" +
            "import java.io.*;\n" +
            "import java.net.*;\n" +
            "public class Test {\n" +
            "    public void taragui() {\n" +
            "        try {\n" +
            "        } catch (MalformedURLException | FileNotFoundException ex) {\n" +
            "        }\n" +
            "    }\n" +
            "}\n"
            );
        String golden =
            "package hierbas.del.litoral;\n" +
            "import java.io.*;\n" +
            "import java.net.*;\n" +
            "public class Test {\n" +
            "    public void taragui() {\n" +
            "        try {\n" +
            "        } catch (IOException | MalformedURLException | FileNotFoundException ex) {\n" +
            "        }\n" +
            "    }\n" +
            "}\n";
        JavaSource testSource = JavaSource.forFileObject(FileUtil.toFileObject(testFile));
        Task task = new Task<WorkingCopy>() {

            public void run(final WorkingCopy workingCopy) throws IOException {
                workingCopy.toPhase(Phase.RESOLVED);
                final TreeMaker make = workingCopy.getTreeMaker();

                new ErrorAwareTreeScanner<Void, Void>() {
                    @Override public Void visitUnionType(UnionTypeTree node, Void p) {
                        List<Tree> alternatives = new ArrayList<Tree>(node.getTypeAlternatives());
                        alternatives.add(0, make.Identifier("IOException"));
                        workingCopy.rewrite(node, make.UnionType(alternatives));
                        return null;
                    }
                }.scan(workingCopy.getCompilationUnit(), null);
            }

        };
        testSource.runModificationTask(task).commit();
        String res = TestUtilities.copyFileToString(testFile);
        //System.err.println(res);
        assertEquals(golden, res);
    }

    public void testAddMiddleToMultiCatch() throws Exception {
        testFile = new File(getWorkDir(), "Test.java");
        TestUtilities.copyStringToFile(testFile,
            "package hierbas.del.litoral;\n" +
            "import java.io.*;\n" +
            "import java.net.*;\n" +
            "public class Test {\n" +
            "    public void taragui() {\n" +
            "        try {\n" +
            "        } catch (MalformedURLException | FileNotFoundException ex) {\n" +
            "        }\n" +
            "    }\n" +
            "}\n"
            );
        String golden =
            "package hierbas.del.litoral;\n" +
            "import java.io.*;\n" +
            "import java.net.*;\n" +
            "public class Test {\n" +
            "    public void taragui() {\n" +
            "        try {\n" +
            "        } catch (MalformedURLException | IOException | FileNotFoundException ex) {\n" +
            "        }\n" +
            "    }\n" +
            "}\n";
        JavaSource testSource = JavaSource.forFileObject(FileUtil.toFileObject(testFile));
        Task task = new Task<WorkingCopy>() {

            public void run(final WorkingCopy workingCopy) throws IOException {
                workingCopy.toPhase(Phase.RESOLVED);
                final TreeMaker make = workingCopy.getTreeMaker();

                new ErrorAwareTreeScanner<Void, Void>() {
                    @Override public Void visitUnionType(UnionTypeTree node, Void p) {
                        List<Tree> alternatives = new ArrayList<Tree>(node.getTypeAlternatives());
                        alternatives.add(1, make.Identifier("IOException"));
                        workingCopy.rewrite(node, make.UnionType(alternatives));
                        return null;
                    }
                }.scan(workingCopy.getCompilationUnit(), null);
            }

        };
        testSource.runModificationTask(task).commit();
        String res = TestUtilities.copyFileToString(testFile);
        //System.err.println(res);
        assertEquals(golden, res);
    }

    public void testRemoveAddInMultiCatch() throws Exception {
        testFile = new File(getWorkDir(), "Test.java");
        TestUtilities.copyStringToFile(testFile,
            "package hierbas.del.litoral;\n" +
            "import java.io.*;\n" +
            "import java.net.*;\n" +
            "public class Test {\n" +
            "    public void taragui() {\n" +
            "        try {\n" +
            "        } catch (MalformedURLException | FileNotFoundException ex) {\n" +
            "        }\n" +
            "    }\n" +
            "}\n"
            );
        String golden =
            "package hierbas.del.litoral;\n" +
            "import java.io.*;\n" +
            "import java.net.*;\n" +
            "public class Test {\n" +
            "    public void taragui() {\n" +
            "        try {\n" +
            "        } catch (IOException | RuntimeException ex) {\n" +
            "        }\n" +
            "    }\n" +
            "}\n";
        JavaSource testSource = JavaSource.forFileObject(FileUtil.toFileObject(testFile));
        Task task = new Task<WorkingCopy>() {

            public void run(final WorkingCopy workingCopy) throws IOException {
                workingCopy.toPhase(Phase.RESOLVED);
                final TreeMaker make = workingCopy.getTreeMaker();

                new ErrorAwareTreeScanner<Void, Void>() {
                    @Override public Void visitUnionType(UnionTypeTree node, Void p) {
                        List<Tree> alternatives = new ArrayList<Tree>(node.getTypeAlternatives());
                        alternatives.remove(0);
                        alternatives.remove(0);
                        alternatives.add(0, make.Identifier("IOException"));
                        alternatives.add(1, make.Identifier("RuntimeException"));
                        workingCopy.rewrite(node, make.UnionType(alternatives));
                        return null;
                    }
                }.scan(workingCopy.getCompilationUnit(), null);
            }

        };
        testSource.runModificationTask(task).commit();
        String res = TestUtilities.copyFileToString(testFile);
        //System.err.println(res);
        assertEquals(golden, res);
    }
    
    String getGoldenPckg() {
        return "";
    }

    String getSourcePckg() {
        return "";
    }

}
