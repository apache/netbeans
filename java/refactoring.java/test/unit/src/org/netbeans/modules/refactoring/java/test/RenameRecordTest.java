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

import com.sun.source.tree.CompilationUnitTree;
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
import static org.netbeans.junit.AssertLinesEqualHelpers.*;
import org.netbeans.modules.refactoring.api.Problem;
import org.netbeans.modules.refactoring.api.RefactoringSession;
import org.netbeans.modules.refactoring.api.RenameRefactoring;
import org.netbeans.modules.refactoring.java.ui.JavaRenameProperties;
import org.openide.filesystems.FileObject;
import org.openide.util.lookup.Lookups;

public class RenameRecordTest extends RefactoringTestBase {

    public RenameRecordTest(String name) {
        super(name, "17");
        sideBySideCompare=true;
        showOutputOnPass=true;
    }

    public void testRenameComponent1() throws Exception {
        String testCode = """
                        package test;
                        public record Test(int compo|nent, int y) {}
                        """;
        TestInput splitCode = TestUtilities.splitCodeAndPos(testCode);
        writeFilesAndWaitForScan(src,
                                 new File("Test.java", splitCode.code()),
                                 new File("Use.java",
                                          """
                                          package test;
                                          public class Use {
                                              private void test(Test t) {
                                                  int i = t.component();
                                              }
                                          }
                                          """));
        JavaRenameProperties props = new JavaRenameProperties();
        performRename(src.getFileObject("Test.java"), splitCode.pos(), "newName", props, true);
        verifyContent(src, new File("Test.java",
                                    """
                                    package test;
                                    public record Test(int newName, int y) {}
                                    """),
                           new File("Use.java",
                                    """
                                    package test;
                                    public class Use {
                                        private void test(Test t) {
                                            int i = t.newName();
                                        }
                                    }
                                    """));

    }

    // changes in refactoring or javs.source.base  breaks rename
    // when there is somthing before the 'com|ponent'.
    public void testRenameComponent1a() throws Exception {
        String testCode = """
                        package test;
                        public record Test(int x, int compo|nent) {}
                        """;
        TestInput splitCode = TestUtilities.splitCodeAndPos(testCode);
        writeFilesAndWaitForScan(src,
                                 new File("Test.java", splitCode.code()),
                                 new File("Use.java",
                                          """
                                          package test;
                                          public class Use {
                                              private void test(Test t) {
                                                  int i = t.component();
                                              }
                                          }
                                          """));
        JavaRenameProperties props = new JavaRenameProperties();
        performRename(src.getFileObject("Test.java"), splitCode.pos(), "newName", props, true);
        verifyContent(src, new File("Test.java",
                                    """
                                    package test;
                                    public record Test(int x, int newName) {}
                                    """),
                           new File("Use.java",
                                    """
                                    package test;
                                    public class Use {
                                        private void test(Test t) {
                                            int i = t.newName();
                                        }
                                    }
                                    """));

    }

    // disabled, somehow having int x before to be renamed component
    // breaks list diff
    public void testRenameComponent1b() throws Exception {
        String testCode = """
                        package test;
                        public record Test(int x, int compo|nent) {}
                        """;
        TestInput splitCode = TestUtilities.splitCodeAndPos(testCode);
        writeFilesAndWaitForScan(src,
                                 new File("Test.java", splitCode.code()),
                                 new File("Use.java",
                                          """
                                          package test;
                                          public class Use {
                                              private void test(Test t) {
                                                  int i = t.component();
                                              }
                                          }
                                          """));
        JavaRenameProperties props = new JavaRenameProperties();
        performRename(src.getFileObject("Test.java"), splitCode.pos(), "newName", props, true);
        verifyContent(src, new File("Test.java",
                                    """
                                    package test;
                                    public record Test(int x, int newName) {}
                                    """),
                           new File("Use.java",
                                    """
                                    package test;
                                    public class Use {
                                        private void test(Test t) {
                                            int i = t.newName();
                                        }
                                    }
                                    """));

    }

    // this test has an explicit accessor.
    // this appears to break on potential compact constructor not being compact.
    public void testRenameComponent2() throws Exception {
        String testCode = """
                        package test;
                        public record Test(int compo|nent, int y) {
                            public Test {
                                component = -1;
                            }
                            public int component() {
                                return component;
                            }
                            public int hashCode() {
                                return component;
                            }
                            public Test(int someInt) {
                              this(someInt, 0);
                            }
                        }
                        """;
        TestInput splitCode = TestUtilities.splitCodeAndPos(testCode);
        writeFilesAndWaitForScan(src,
                                 new File("Test.java", splitCode.code()),
                                 new File("Use.java",
                                          """
                                          package test;
                                          public class Use {
                                              private void test(Test t) {
                                                  int i = t.component();
                                              }
                                          }
                                          """));
        JavaRenameProperties props = new JavaRenameProperties();
        performRename(src.getFileObject("Test.java"), splitCode.pos(), "newName", props, true);
        verifyContent(src, new File("Test.java",
                                    """
                                    package test;
                                    public record Test(int newName, int y) {
                                        public Test {
                                            newName = -1;
                                        }
                                        public int newName() {
                                            return newName;
                                        }
                                        public int hashCode() {
                                            return newName;
                                        }
                                        public Test(int someInt) {
                                          this(someInt, 0);
                                        }
                                    }
                                    """),
                           new File("Use.java",
                                    """
                                    package test;
                                    public class Use {
                                        private void test(Test t) {
                                            int i = t.newName();
                                        }
                                    }
                                    """));

    }

    /*
     * Show that with compact constructor behaves.
     */
    public void testRenameComponent3() throws Exception {
        String testCode = """
                          package test;
                          public record Test(int compo|nent, int y) {
                              public Test { //compact
                                  component = -1;
                              }
                              public int component() {
                                  return component;
                              }
                              public int hashCode() {
                                  return component;
                              }
                          }
                          """;
        TestInput splitCode = TestUtilities.splitCodeAndPos(testCode);
        writeFilesAndWaitForScan(src,
                                 new File("Test.java", splitCode.code()),
                                 new File("Use.java",
                                          """
                                          package test;
                                          public class Use {
                                              private void test(Test t) {
                                                  int i = t.component();
                                              }
                                          }
                                          """));
        JavaRenameProperties props = new JavaRenameProperties();
        performRename(src.getFileObject("Test.java"), splitCode.pos(), "newName", props, true);
        verifyContent(src, new File("Test.java",
                                    """
                                    package test;
                                    public record Test(int newName, int y) {
                                        public Test { //compact
                                            newName = -1;
                                        }
                                        public int newName() {
                                            return newName;
                                        }
                                        public int hashCode() {
                                            return newName;
                                        }
                                    }
                                    """),
                           new File("Use.java",
                                    """
                                    package test;
                                    public class Use {
                                        private void test(Test t) {
                                            int i = t.newName();
                                        }
                                    }
                                    """));

    }

    public void testRenameComponentStartFromAccessor1() throws Exception {
        String useCode = """
                         package test;
                         public class Use {
                             private void test(Test t) {
                                 int i = t.com|ponent();
                             }
                         }
                         """;
        TestInput splitCode = TestUtilities.splitCodeAndPos(useCode);
        writeFilesAndWaitForScan(src,
                                 new File("Test.java",
                                          """
                                          package test;
                                          public record Test(int component, int y) {
                                              public Test {
                                                  component = -1;
                                              }
                                              public int component() {
                                                  return component;
                                              }
                                              public int hashCode() {
                                                  return component;
                                              }
                                          }
                                          """),
                                 new File("Use.java", splitCode.code()));
        JavaRenameProperties props = new JavaRenameProperties();
        performRename(src.getFileObject("Use.java"), splitCode.pos(), "newName", props, true);
        verifyContent(src, new File("Test.java",
                                    """
                                    package test;
                                    public record Test(int newName, int y) {
                                        public Test {
                                            newName = -1;
                                        }
                                        public int newName() {
                                            return newName;
                                        }
                                        public int hashCode() {
                                            return newName;
                                        }
                                    }
                                    """),
                           new File("Use.java",
                                    """
                                    package test;
                                    public class Use {
                                        private void test(Test t) {
                                            int i = t.newName();
                                        }
                                    }
                                    """));

    }

    public void testRenameComponentStartFromAccessor2() throws Exception {
        String useCode = """
                         package test;
                         public class Use {
                             private void test(Test t) {
                                 int i = t.com|ponent();
                             }
                         }
                         """;
        TestInput splitCode = TestUtilities.splitCodeAndPos(useCode);
        writeFilesAndWaitForScan(src,
                                 new File("Test.java",
                                          """
                                          package test;
                                          public record Test(int component, int y) {
                                          }
                                          """),
                                 new File("Use.java", splitCode.code()));
        JavaRenameProperties props = new JavaRenameProperties();
        performRename(src.getFileObject("Use.java"), splitCode.pos(), "newName", props, true);
        verifyContent(src, new File("Test.java",
                                    """
                                    package test;
                                    public record Test(int newName, int y) {
                                    }
                                    """),
                           new File("Use.java",
                                    """
                                    package test;
                                    public class Use {
                                        private void test(Test t) {
                                            int i = t.newName();
                                        }
                                    }
                                    """));

    }

    public void testRenameComponentStartFromConstructorArg() throws Exception {
        String testCode = """
                          package test;
                          public record Test(int component, int y) {
                              public Test {
                                  compo|nent = -1;
                              }
                              public int component() {
                                  return component;
                              }
                              public int hashCode() {
                                  return component;
                              }
                          }
                          """;
        TestInput splitCode = TestUtilities.splitCodeAndPos(testCode);
        writeFilesAndWaitForScan(src,
                                 new File("Test.java", splitCode.code()),
                                 new File("Use.java",
                                          """
                                          package test;
                                          public class Use {
                                              private void test(Test t) {
                                                  int i = t.component();
                                              }
                                          }
                                          """));
        JavaRenameProperties props = new JavaRenameProperties();
        performRename(src.getFileObject("Test.java"), splitCode.pos(), "newName", props, true);
        verifyContent(src, new File("Test.java",
                                    """
                                    package test;
                                    public record Test(int newName, int y) {
                                        public Test {
                                            newName = -1;
                                        }
                                        public int newName() {
                                            return newName;
                                        }
                                        public int hashCode() {
                                            return newName;
                                        }
                                    }
                                    """),
                           new File("Use.java",
                                    """
                                    package test;
                                    public class Use {
                                        private void test(Test t) {
                                            int i = t.newName();
                                        }
                                    }
                                    """));
    }

    public void testRenameRecord() throws Exception {
        String testCode = """
                          package test;
                          public record Te|st(int component) {
                              public Test {
                                  component = "";
                              }
                          }
                          """;
        TestInput splitCode = TestUtilities.splitCodeAndPos(testCode);
        writeFilesAndWaitForScan(src,
                                 new File("Test.java", splitCode.code()),
                                 new File("Use.java",
                                          """
                                          package test;
                                          public class Use {
                                              private Test test() {
                                                  return new Test(0);
                                              }
                                          }
                                          """));
        JavaRenameProperties props = new JavaRenameProperties();
        performRename(src.getFileObject("Test.java"), splitCode.pos(), "NewName", props, true);
        verifyContent(src, new File("Test.java",
                                    """
                                    package test;
                                    public record NewName(int component) {
                                        public NewName {
                                            component = "";
                                        }
                                    }
                                    """),
                           new File("Use.java",
                                    """
                                    package test;
                                    public class Use {
                                        private NewName test() {
                                            return new NewName(0);
                                        }
                                    }
                                    """));

    }

    // test for varargs and generic.
    public void testRenameRecordGenVar() throws Exception {
        sideBySideCompare=true;
        showOutputOnPass=true;

        String testCode = """
                          package test;
                          public record Te|st<G extends Number>(G... component) {
                              public Test {
                                  assert 0 < component.length;
                              }
                          }
                          """;
        TestInput splitCode = TestUtilities.splitCodeAndPos(testCode);
        writeFilesAndWaitForScan(src,
                                 new File("Test.java", splitCode.code()),
                                 new File("Use.java",
                                          """
                                          package test;
                                          public class Use {
                                              private Test<Integer> test() {
                                                  return new Test(1, 2);
                                              }
                                          }
                                          """));
        JavaRenameProperties props = new JavaRenameProperties();
        performRename(src.getFileObject("Test.java"), splitCode.pos(), "NewName", props, true);
        verifyContent(src, new File("Test.java",
                                    """
                                    package test;
                                    public record NewName<G extends Number>(G... component) {
                                        public NewName {
                                            assert 0 < component.length;
                                        }
                                    }
                                    """),
                           new File("Use.java",
                                    """
                                    package test;
                                    public class Use {
                                        private NewName<Integer> test() {
                                            return new NewName(1, 2);
                                        }
                                    }
                                    """));

    }
    // test for varargs and generic.
    public void testRenameRecordGenVarComponent() throws Exception {
        sideBySideCompare=true;
        showOutputOnPass=true;

        String testCode = """
                          package test;
                          public record Test<G extends Number>(G... comp|onent) {
                              public Test {
                                  assert 0 < component.length;
                              }
                          }
                          """;
        TestInput splitCode = TestUtilities.splitCodeAndPos(testCode);
        writeFilesAndWaitForScan(src,
                                 new File("Test.java", splitCode.code()),
                                 new File("Use.java",
                                          """
                                          package test;
                                          public class Use {
                                              private Test<Integer> test() {
                                                  return new Test(1, 2);
                                              }
                                          }
                                          """));
        JavaRenameProperties props = new JavaRenameProperties();
        performRename(src.getFileObject("Test.java"), splitCode.pos(), "parts", props, true);
        verifyContent(src, new File("Test.java",
                                    """
                                    package test;
                                    public record Test<G extends Number>(G... parts) {
                                        public Test {
                                            assert 0 < parts.length;
                                        }
                                    }
                                    """),
                           new File("Use.java",
                                    """
                                    package test;
                                    public class Use {
                                        private Test<Integer> test() {
                                            return new Test(1, 2);
                                        }
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
