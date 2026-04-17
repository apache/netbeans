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

import com.sun.source.util.TreePath;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import org.netbeans.api.java.source.CompilationController;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.TreePathHandle;
import org.netbeans.modules.refactoring.api.Problem;
import org.netbeans.modules.refactoring.api.RefactoringSession;
import org.netbeans.modules.refactoring.api.RenameRefactoring;
import org.netbeans.modules.refactoring.java.ui.JavaRenameProperties;
import org.openide.filesystems.FileObject;
import org.openide.util.lookup.Lookups;

public class ComplexProjectHierarchyTest extends RefactoringTestBase {

    private final ProjectDesc side = new ProjectDesc("side");
    private final ProjectDesc base = new ProjectDesc("base");
    private final ProjectDesc main = new ProjectDesc("main", side, base);

    public ComplexProjectHierarchyTest(String name) {
        super(name, "17");
    }

    public void testComplexRename1() throws Exception {
        writeFilesAndWaitForScan(getSource(side),
                new File("side/Side.java",
                         """
                         package side;
                         public class Side {}
                         """));

        String baseCode = """
                          package base;
                          public class Base<T> {
                              public void te|st(T t) {}
                          }
                          """;
        int pos = baseCode.indexOf('|');

        writeFilesAndWaitForScan(getSource(base),
                new File("base/Base.java",
                         baseCode.substring(0, pos) + baseCode.substring(pos + 1)));

        writeFilesAndWaitForScan(getSource(main),
                new File("main/Main.java",
                         """
                         package main;
                         import base.Base;
                         import side.Side;
                         public class Main extends Base<Side> {
                             public void test(Side t) {}
                             public void test(String t) {}
                             public void run() {
                                 test(new Side());
                             }
                         }
                         """));
        performRename(getSource(base).getFileObject("base/Base.java"), pos, "test2", null, false);
        verifyContent(getSource(main),
                new File("main/Main.java",
                         """
                         package main;
                         import base.Base;
                         import side.Side;
                         public class Main extends Base<Side> {
                             public void test2(Side t) {}
                             public void test(String t) {}
                             public void run() {
                                 test2(new Side());
                             }
                         }
                         """));
    }

    public void testComplexRename2() throws Exception {
        writeFilesAndWaitForScan(getSource(side),
                new File("side/Side.java",
                         """
                         package side;
                         public class Side {}
                         """));
        writeFilesAndWaitForScan(getSource(base),
                new File("base/Base.java",
                         """
                         package base;
                         public class Base<T> {
                             public void test(T t) {}
                         }
                         """));

        String mainCode = """
                          package main;
                          import base.Base;
                          import side.Side;
                          public class Main extends Base<Side> {
                              public void test(Side t) {}
                              public void test(String t) {}
                              public void run() {
                                  te|st(new Side());
                              }
                          }
                          """;

        int pos = mainCode.indexOf('|');

        writeFilesAndWaitForScan(getSource(main),
                new File("main/Main.java",
                         mainCode.substring(0, pos) + mainCode.substring(pos + 1)));
        performRename(getSource(main).getFileObject("main/Main.java"), pos, "test2", null, false, new Problem(false, "ERR_Overrides"));
        verifyContent(getSource(main),
                new File("main/Main.java",
                         """
                         package main;
                         import base.Base;
                         import side.Side;
                         public class Main extends Base<Side> {
                             public void test2(Side t) {}
                             public void test(String t) {}
                             public void run() {
                                 test2(new Side());
                             }
                         }
                         """));
    }

    @Override
    protected List<ProjectDesc> projects() {
        return List.of(main, base, side);
    }

    private void performRename(FileObject source, final int absPos, final String newname, final JavaRenameProperties props, final boolean searchInComments, Problem... expectedProblems) throws Exception {
        final RenameRefactoring[] r = new RenameRefactoring[1];
        JavaSource.forFileObject(source)
                  .runUserActionTask(javac -> {
                      javac.toPhase(JavaSource.Phase.RESOLVED);
                      TreePath tp = javac.getTreeUtilities().pathFor(absPos);

                      r[0] = new RenameRefactoring(Lookups.singleton(TreePathHandle.create(tp, javac)));
                      r[0].setNewName(newname);
                      r[0].setSearchInComments(searchInComments);
                      if(props != null) {
                          r[0].getContext().add(props);
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