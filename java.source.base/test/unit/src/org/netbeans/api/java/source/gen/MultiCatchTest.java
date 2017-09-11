/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
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
 * Software is Sun Microsystems, Inc. Portions Copyright 2010 Sun
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
import com.sun.source.util.TreeScanner;
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
        System.err.println(res);
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

                new TreeScanner<Void, Void>() {
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
        System.err.println(res);
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

                new TreeScanner<Void, Void>() {
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
        System.err.println(res);
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

                new TreeScanner<Void, Void>() {
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
        System.err.println(res);
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

                new TreeScanner<Void, Void>() {
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
        System.err.println(res);
        assertEquals(golden, res);
    }
    
    String getGoldenPckg() {
        return "";
    }

    String getSourcePckg() {
        return "";
    }

}
