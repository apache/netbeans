/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.netbeans.modules.refactoring.java.test;

import com.sun.source.tree.ClassTree;
import com.sun.source.tree.CompilationUnitTree;
import com.sun.source.tree.Tree;
import com.sun.source.util.TreePath;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import org.netbeans.api.java.source.CompilationController;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.Task;
import org.netbeans.api.java.source.TestUtilities;
import org.netbeans.api.java.source.TestUtilities.TestInput;
import org.netbeans.api.java.source.TreePathHandle;
import org.netbeans.modules.refactoring.api.Problem;
import org.netbeans.modules.refactoring.api.RefactoringSession;
import org.netbeans.modules.refactoring.api.RenameRefactoring;
import org.netbeans.modules.refactoring.java.ui.JavaRenameProperties;
import org.netbeans.modules.refactoring.spi.impl.UndoManager;
import org.openide.filesystems.FileObject;
import org.openide.util.lookup.Lookups;

/**
 *
 * @author Jan Becicka
 */
public class RenameSealedTest extends RefactoringTestBase {

    public RenameSealedTest(String name) {
        super(name, "17");
    }

    public void testRenamePermittedClass0() throws Exception {
        String testCode = """
                          package test;
                          public class Outter {
                              public final class Sub|type implements Test {
                              }
                              public sealed interface Test permits Subtype {
                              }
                          }
                          """;
        TestInput splitCode = TestUtilities.splitCodeAndPos(testCode);
        writeFilesAndWaitForScan(src,
                                 new File("Outter.java", splitCode.code()));
        JavaRenameProperties props = new JavaRenameProperties();
        performRename(src.getFileObject("Outter.java"), splitCode.pos(), "NewSubtype", props, true);
        verifyContent(src, new File("Outter.java",
                                    """
                                    package test;
                                    public class Outter {
                                        public final class NewSubtype implements Test {
                                        }
                                        public sealed interface Test permits NewSubtype {
                                        }
                                    }
                                    """));

    }

    public void testRenamePermittedClass1() throws Exception {
        String testCode = """
                          package test;
                          public final class Sub|type implements Test {
                          }
                          """;
        TestInput splitCode = TestUtilities.splitCodeAndPos(testCode);
        writeFilesAndWaitForScan(src,
                                 new File("Test.java",
                                          """
                                          package test;
                                          public sealed interface Test permits Subtype {
                                          }
                                          """),
                                 new File("Subtype.java", splitCode.code()));
        JavaRenameProperties props = new JavaRenameProperties();
        performRename(src.getFileObject("Subtype.java"), splitCode.pos(), "NewSubtype", props, true);
        verifyContent(src, new File("Test.java",
                                    """
                                    package test;
                                    public sealed interface Test permits NewSubtype {
                                    }
                                    """),
                           new File("Subtype.java",
                                    """
                                    package test;
                                    public final class NewSubtype implements Test {
                                    }
                                    """));

    }

    public void testRenamePermittedClass2() throws Exception {
        String testCode = """
                          package test;
                          public sealed interface Test permits Sub|type {
                          }
                          """;
        TestInput splitCode = TestUtilities.splitCodeAndPos(testCode);
        writeFilesAndWaitForScan(src,
                                 new File("Test.java", splitCode.code()),
                                 new File("Subtype.java",
                                          """
                                          package test;
                                          public class Subtype implements Test {
                                          }
                                          """));
        JavaRenameProperties props = new JavaRenameProperties();
        performRename(src.getFileObject("Test.java"), splitCode.pos(), "NewSubtype", props, true);
        verifyContent(src, new File("Test.java",
                                    """
                                    package test;
                                    public sealed interface Test permits NewSubtype {
                                    }
                                    """),
                           new File("Subtype.java",
                                    """
                                    package test;
                                    public class NewSubtype implements Test {
                                    }
                                    """));

    }

    public void testRenameInterfaceWithPermittedClass() throws Exception {
        String testCode = """
                          package test;
                          public sealed interface Te|st permits Subtype {
                          }
                          """;
        TestInput splitCode = TestUtilities.splitCodeAndPos(testCode);
        writeFilesAndWaitForScan(src,
                                 new File("Test.java", splitCode.code()),
                                 new File("Subtype.java",
                                          """
                                          package test;
                                          public class Subtype implements Test {
                                          }
                                          """));
        JavaRenameProperties props = new JavaRenameProperties();
        performRename(src.getFileObject("Test.java"), splitCode.pos(), "Test2", props, true);
        verifyContent(src, new File("Test.java",
                                    """
                                    package test;
                                    public sealed interface Test2 permits Subtype {
                                    }
                                    """),
                           new File("Subtype.java",
                                    """
                                    package test;
                                    public class Subtype implements Test2 {
                                    }
                                    """));

    }

    private void performRename(FileObject source, final int absPos, final String newname, final JavaRenameProperties props, final boolean searchInComments, Problem... expectedProblems) throws Exception {
        final RenameRefactoring[] r = new RenameRefactoring[1];
        JavaSource.forFileObject(source).runUserActionTask(new Task<CompilationController>() {

            @Override
            public void run(CompilationController javac) throws Exception {
                javac.toPhase(JavaSource.Phase.RESOLVED);
                CompilationUnitTree cut = javac.getCompilationUnit();

                TreePath tp = javac.getTreeUtilities().pathFor(absPos);

                r[0] = new RenameRefactoring(Lookups.singleton(TreePathHandle.create(tp, javac)));
                r[0].setNewName(newname);
                r[0].setSearchInComments(searchInComments);
                if(props != null) {
                    r[0].getContext().add(props);
                }
            }
        }, true);

        RefactoringSession rs = RefactoringSession.create("Rename");
        List<Problem> problems = new LinkedList<>();

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
