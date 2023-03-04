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

package org.netbeans.modules.refactoring.java.test;

import com.sun.source.tree.ClassTree;
import com.sun.source.tree.CompilationUnitTree;
import com.sun.source.tree.MethodTree;
import com.sun.source.tree.VariableTree;
import com.sun.source.util.TreePath;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import org.netbeans.api.java.source.CompilationController;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.SourceUtils;
import org.netbeans.api.java.source.Task;
import org.netbeans.api.java.source.TreePathHandle;
import org.netbeans.modules.parsing.api.indexing.IndexingManager;
import org.netbeans.modules.parsing.impl.indexing.errors.TaskCache;
import org.netbeans.modules.refactoring.api.Problem;
import org.netbeans.modules.refactoring.api.RefactoringSession;
import org.netbeans.modules.refactoring.java.api.InvertBooleanRefactoring;
import org.openide.filesystems.FileObject;

/**
 *
 * @author lahvac
 */
public class InvertBooleanTest extends RefTestBase {

    public InvertBooleanTest(String name) {
        super(name);
    }
    
    public void test238057() throws Exception {
        writeFilesAndWaitForScan(src,
                                 new File("test/Test.java", "package test;\n public class Test {\n public boolean b() { if (true) return Boolean.getBoolean(\"\"); else return false; } { if (b()) System.err.println(1); \n } }\n"),
                                 new File("test/Use.java", "package test; public class Use { { new Test().b();\n } }")
                                 );

        performMethodTest(1);

        assertContent(src,
                      new File("test/Test.java", "package test;\n public class Test {\n public boolean c() { if (true) return !Boolean.getBoolean(\"\"); else return true; } { if (!c()) System.err.println(1); \n } }\n"),
                      new File("test/Use.java", "package test; public class Use { { new Test().c();\n } }")/*,
                      new File("META-INF/upgrade/test.Test.hint", "new test.Test($1, $2) :: $1 instanceof int && $2 instanceof java.util.List<java.lang.String> => test.Test.create($1, $2);;")*/
                     );
    }

    public void testInvertField1() throws Exception {
        writeFilesAndWaitForScan(src,
                                 new File("test/Test.java", "package test;\n public class Test {\n public boolean b = Boolean.getBoolean(\"\"); { b = Boolean.getBoolean(\"\"); if (b) System.err.println(1);\n } }\n"),
                                 new File("test/Use.java", "package test; public class Use { { new Test().b = Boolean.getBoolean(\"\"); if (new Test().b) System.err.println(1);\n } }")
                                 );

        performFieldTest();

        assertContent(src,
                      new File("test/Test.java", "package test;\n public class Test {\n public boolean c = !Boolean.getBoolean(\"\"); { c = !Boolean.getBoolean(\"\"); if (!c) System.err.println(1);\n } }\n"),
                      new File("test/Use.java", "package test; public class Use { { new Test().c = !Boolean.getBoolean(\"\"); if (!new Test().c) System.err.println(1);\n } }")/*,
                      new File("META-INF/upgrade/test.Test.hint", "new test.Test($1, $2) :: $1 instanceof int && $2 instanceof java.util.List<java.lang.String> => test.Test.create($1, $2);;")*/
                     );
    }

    public void testInvertField2() throws Exception {
        writeFilesAndWaitForScan(src,
                                 new File("test/Test.java", "package test;\n public class Test {\n public boolean b = Integer.getInteger(\"\") == 0; { b = Integer.getInteger(\"\") != 0; b = !b; boolean n1 = false; b = !n1; b = !(n1); b = (!n1); b = true; b = false; System.err.println(!b);\n } }\n")
                                 );

        performFieldTest();

        assertContent(src,
                      new File("test/Test.java", "package test;\n public class Test {\n public boolean c = Integer.getInteger(\"\") != 0; { c = Integer.getInteger(\"\") == 0; c = !c; boolean n1 = false; c = n1; c = n1; c = n1; c = false; c = true; System.err.println(c);\n } }\n")/*,
                      new File("META-INF/upgrade/test.Test.hint", "new test.Test($1, $2) :: $1 instanceof int && $2 instanceof java.util.List<java.lang.String> => test.Test.create($1, $2);;")*/
                     );
    }
    
    public void testInvertField3() throws Exception {
        writeFilesAndWaitForScan(src,
                                 new File("test/Test.java", "package test;\n public class Test {\n public boolean b = true; public boolean crazyOtherMethod() { return b; }\n}")
                                 );

        performFieldTest("b");

        assertContent(src,
                      new File("test/Test.java", "package test;\n public class Test {\n public boolean b = false; public boolean crazyOtherMethod() { return !b; }\n}"));
    }
    
    
    public void test210971() throws Exception {
        writeFilesAndWaitForScan(src,
                                 new File("test/Test.java", "package test; public class Test { public boolean someMethod() { return true; } public int returnInt() { return 1099; } }")
                                 );

        performMethodTest(1);

        assertContent(src,
                      new File("test/Test.java", "package test; public class Test { public boolean c() { return false; } public int returnInt() { return 1099; } }")
                     );
    }
    

    public void testInvertFieldStatic() throws Exception {
        writeFilesAndWaitForScan(src,
                                 new File("test/Test.java", "package test;\n public class Test {\n public static boolean b = true; static { b = Integer.getInteger(\"\") != 0; \n } }\n")
                                 );

        performFieldTest();

        assertContent(src,
                      new File("test/Test.java", "package test;\n public class Test {\n public static boolean c = false; static { " + /*XXX:*/ "Test." + "c = Integer.getInteger(\"\") == 0; \n } }\n")/*,
                      new File("META-INF/upgrade/test.Test.hint", "new test.Test($1, $2) :: $1 instanceof int && $2 instanceof java.util.List<java.lang.String> => test.Test.create($1, $2);;")*/
                     );
    }

    public void testInvertMethod() throws Exception {
        writeFilesAndWaitForScan(src,
                                 new File("test/Test.java", "package test;\n public class Test {\n public boolean b() { if (true) return Boolean.getBoolean(\"\"); else return false; } { if (b()) System.err.println(1); \n } }\n"),
                                 new File("test/Use.java", "package test; public class Use { { if (!new Test().b()) System.err.println(1);\n } }")
                                 );

        performMethodTest(1);

        assertContent(src,
                      new File("test/Test.java", "package test;\n public class Test {\n public boolean c() { if (true) return !Boolean.getBoolean(\"\"); else return true; } { if (!c()) System.err.println(1); \n } }\n"),
                      new File("test/Use.java", "package test; public class Use { { if (new Test().c()) System.err.println(1);\n } }")/*,
                      new File("META-INF/upgrade/test.Test.hint", "new test.Test($1, $2) :: $1 instanceof int && $2 instanceof java.util.List<java.lang.String> => test.Test.create($1, $2);;")*/
                     );
    }
    
    public void testInvertMethodInterface() throws Exception {
        writeFilesAndWaitForScan(src,
                new File("test/Test.java", "package test;\n public interface Test { boolean test(int a); }\n"),
                new File("test/Iface.java", "package test;"
                        + "public class Iface implements Test {\n"
                        + "    @Override\n"
                        + "    public boolean test(int a) {\n"
                        + "        return false;\n"
                        + "    }\n"
                        + "}\n"));

        performMethodTest(0, new Problem(true, "ERR_InvertMethodInInterface"));

        assertContent(src,
                new File("test/Test.java", "package test;\n public interface Test { boolean test(int a); }\n"),
                new File("test/Iface.java", "package test;"
                        + "public class Iface implements Test {\n"
                        + "    @Override\n"
                        + "    public boolean test(int a) {\n"
                        + "        return false;\n"
                        + "    }\n"
                        + "}\n")/*,
         new File("META-INF/upgrade/test.Test.hint", "new test.Test($1, $2) :: $1 instanceof int && $2 instanceof java.util.List<java.lang.String> => test.Test.create($1, $2);;")*/
        );
    }

    public void testInvertMethodStaticTypeParam() throws Exception {
        writeFilesAndWaitForScan(src,
                                 new File("test/Test.java", "package test;\n public class Test {\n public static <T> boolean b(T t) { return true; } { if (Test.<String>b(null)) System.err.println(1); \n } }\n")
                                 );

        performMethodTest(1);

        assertContent(src,
                      new File("test/Test.java", "package test;\n public class Test {\n public static <T> boolean c(T t) { return false; } { if (!Test.<String>c(null)) System.err.println(1); \n } }\n")/*,
                      new File("META-INF/upgrade/test.Test.hint", "new test.Test($1, $2) :: $1 instanceof int && $2 instanceof java.util.List<java.lang.String> => test.Test.create($1, $2);;")*/
                     );
    }

    private void performFieldTest(final String newName) throws Exception {
        final InvertBooleanRefactoring[] r = new InvertBooleanRefactoring[1];
        FileObject testFile = src.getFileObject("test/Test.java");

        JavaSource.forFileObject(testFile).runUserActionTask(new Task<CompilationController>() {

            public void run(CompilationController parameter) throws Exception {
                parameter.toPhase(JavaSource.Phase.RESOLVED);
                CompilationUnitTree cut = parameter.getCompilationUnit();

                VariableTree var = (VariableTree) ((ClassTree) cut.getTypeDecls().get(0)).getMembers().get(1);

                TreePath tp = TreePath.getPath(cut, var);
                r[0] = new InvertBooleanRefactoring(TreePathHandle.create(tp, parameter));
                r[0].setNewName(newName);
            }
        }, true);

        RefactoringSession rs = RefactoringSession.create("Session");
        Thread.sleep(1000);
        r[0].prepare(rs);
        rs.doRefactoring(true);

        IndexingManager.getDefault().refreshIndex(src.getURL(), null);
        SourceUtils.waitScanFinished();
        assertEquals(false, TaskCache.getDefault().isInError(src, true));
    }

    private void performFieldTest() throws Exception {
        performFieldTest("c");
    }

    private void performMethodTest(final int position, Problem... expectedProblems) throws Exception {
        final InvertBooleanRefactoring[] r = new InvertBooleanRefactoring[1];
        FileObject testFile = src.getFileObject("test/Test.java");

        JavaSource.forFileObject(testFile).runUserActionTask(new Task<CompilationController>() {

            public void run(CompilationController parameter) throws Exception {
                parameter.toPhase(JavaSource.Phase.RESOLVED);
                CompilationUnitTree cut = parameter.getCompilationUnit();

                MethodTree var = (MethodTree) ((ClassTree) cut.getTypeDecls().get(0)).getMembers().get(position);

                TreePath tp = TreePath.getPath(cut, var);
                r[0] = new InvertBooleanRefactoring(TreePathHandle.create(tp, parameter));
                r[0].setNewName("c");
            }
        }, true);

        RefactoringSession rs = RefactoringSession.create("Session");
        Thread.sleep(1000);
        List<Problem> problems = new LinkedList<>();

        addAllProblems(problems, r[0].preCheck());
        if (!problemIsFatal(problems)) {
            addAllProblems(problems, r[0].prepare(rs));
        }
        if (!problemIsFatal(problems)) {
            addAllProblems(problems, rs.doRefactoring(true));
        }

        assertProblems(Arrays.asList(expectedProblems), problems);

        IndexingManager.getDefault().refreshIndex(src.getURL(), null);
        SourceUtils.waitScanFinished();
        assertEquals(false, TaskCache.getDefault().isInError(src, true));
    }

}
