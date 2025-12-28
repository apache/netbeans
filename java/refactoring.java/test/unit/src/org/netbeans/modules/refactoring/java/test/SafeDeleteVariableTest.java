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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import org.netbeans.api.java.source.CompilationController;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.Task;
import org.netbeans.api.java.source.TreePathHandle;
import org.netbeans.modules.java.source.parsing.JavacParser;
import org.netbeans.modules.refactoring.api.Problem;
import org.netbeans.modules.refactoring.api.RefactoringSession;
import org.netbeans.modules.refactoring.api.SafeDeleteRefactoring;
import static org.netbeans.modules.refactoring.java.test.RefactoringTestBase.addAllProblems;
import org.openide.filesystems.FileObject;
import org.openide.util.lookup.Lookups;

/**
 *
 * @author Ralph Benjamin Ruijs <ralphbenjamin@netbeans.org>
 */
public class SafeDeleteVariableTest extends RefactoringTestBase {

    public SafeDeleteVariableTest(String name) {
        super(name, "1.8");
    }
    
    static {
        JavacParser.DISABLE_SOURCE_LEVEL_DOWNGRADE = true;
    }
    
    public void testEmptyStatement() throws Exception {
        writeFilesAndWaitForScan(src,
                new File("t/A.java", "package t;; public class A {\n"
                + "    public static void main(String[] args) {\n"
                + "        for(int i = 0; i< 10; i++) {\n"
                + "            A a = new A();\n"
                + "        }\n"
                + "    }\n"
                + "}\n"));
        performSafeDelete(src.getFileObject("t/A.java"), 0, false);
        verifyContent(src);
    }
    
    public void testPackage() throws Exception {
        writeFilesAndWaitForScan(src,
                new File("t/A.java", "package t; public class A {\n"
                + "    public static void main(String[] args) {\n"
                + "        for(int i = 0; i< 10; i++) {\n"
                + "            A a = new A();\n"
                + "        }\n"
                + "    }\n"
                + "}\n"));
        performSafeDelete(src.getFileObject("t"), 0, false);
        verifyContent(src);
    }
    
    public void testVariable() throws Exception {
        String source;
        writeFilesAndWaitForScan(src,
                new File("t/A.java", source = "package t; public class A {\n"
                        + "    int i;\n"
                        + "    public A() {\n"
                        + "    }\n"
                        + "}\n"));
        performSafeDelete(src.getFileObject("t/A.java"), source.indexOf("i;") + 1, false);
        verifyContent(src,
                new File("t/A.java", "package t; public class A {\n"
                        + "    public A() {\n"
                        + "    }\n"
                        + "}\n"));
    }
    
    public void testVariableUsed() throws Exception {
        String source;
        writeFilesAndWaitForScan(src,
                new File("t/A.java", source = "package t; public class A {\n"
                        + "    int i;\n"
                        + "    public A() {\n"
                        + "        System.out.println(i)\n"
                        + "    }\n"
                        + "}\n"));
        performSafeDelete(src.getFileObject("t/A.java"), source.indexOf("i;") + 1, false, new Problem(false, "ERR_ReferencesFound"));
        verifyContent(src,
                new File("t/A.java", "package t; public class A {\n"
                        + "    public A() {\n"
                        + "        System.out.println(i)\n"
                        + "    }\n"
                        + "}\n"));
    }
    
    public void testVariableAndMethod() throws Exception {
        writeFilesAndWaitForScan(src,
                new File("t/A.java", "package t; public class A {\n"
                        + "    int i;\n"
                        + "    public void foo() {\n"
                        + "        System.out.println(i);\n"
                        + "    }\n"
                        + "}\n"));
        performSafeDelete(src.getFileObject("t/A.java"), -1, false);
        verifyContent(src,
                new File("t/A.java", "package t; public class A {\n"
                        + "}\n"));
    }
    
    public void testForVariable() throws Exception {
        String source;
        writeFilesAndWaitForScan(src,
                new File("t/A.java", source = "package t; public class A {\n"
                + "    public static void main(String[] args) {\n"
                + "        for(int i = 0; i< 10; i++) {\n"
                + "            A a = new A();\n"
                + "        }\n"
                + "    }\n"
                + "}\n"));
        performSafeDelete(src.getFileObject("t/A.java"), source.indexOf(" i") + 1, false, new Problem(true, "ERR_VarNotInBlockOrMethod"));
        verifyContent(src,
                new File("t/A.java", "package t; public class A {\n"
                + "    public static void main(String[] args) {\n"
                + "        for(int i = 0; i< 10; i++) {\n"
                + "            A a = new A();\n"
                + "        }\n"
                + "    }\n"
                + "}\n"));
    }
    
    public void testMain() throws Exception {
        String source;
        writeFilesAndWaitForScan(src,
                new File("t/A.java", source = "package t; public class A {\n"
                + "    public static void main(String[] args) {\n"
                + "        A a = new A();\n"
                + "    }\n"
                + "}\n"));
        performSafeDelete(src.getFileObject("t/A.java"), source.indexOf("main") + 1, false);
        verifyContent(src,
                new File("t/A.java", "package t; public class A {\n"
                + "}\n"));
    }
    
    public void testRecursive() throws Exception {
        String source;
        writeFilesAndWaitForScan(src,
                new File("t/A.java", source = "package t; public class A {\n"
                + "    public static void main(String[] args) {\n"
                + "        A a = new A();\n"
                + "        main(args);\n"
                + "    }\n"
                + "}\n"));
        performSafeDelete(src.getFileObject("t/A.java"), source.indexOf("main") + 1, false);
        verifyContent(src,
                new File("t/A.java", "package t; public class A {\n"
                + "}\n"));
    }
    
    public void testPolymorphic() throws Exception {
        String source;
        writeFilesAndWaitForScan(src,
                new File("t/A.java", "package t; public class A {\n"
                        + "    public void foo() {\n"
                        + "        A a = new A();\n"
                        + "    }\n"
                        + "}\n"),
                new File("t/B.java", source = "package t; public class B extends A {\n"
                        + "public void foo() { }\n"
                        + "}\n"));
        performSafeDelete(src.getFileObject("t/B.java"), source.indexOf("foo") + 1, false);
        verifyContent(src,
                new File("t/A.java", "package t; public class A {\n"
                        + "}\n"),
                new File("t/B.java", "package t; public class B extends A {\n"
                        + "}\n"));
    }
    
    public void testPolymorphicUsed() throws Exception {
        String source;
        writeFilesAndWaitForScan(src,
                new File("t/A.java", "package t; public class A {\n"
                        + "    public void foo() {\n"
                        + "        A a = new A();\n"
                        + "    }\n"
                        + "    public static void main(String[] args) {\n"
                        + "        new A().foo();\n"
                        + "    }\n"
                        + "}\n"),
                new File("t/B.java", source = "package t; public class B extends A {\n"
                        + "public void foo() { }\n"
                        + "}\n"));
        performSafeDelete(src.getFileObject("t/B.java"), source.indexOf("foo") + 1, false, new Problem(false, "ERR_ReferencesFound"));
        verifyContent(src,
                new File("t/A.java", "package t; public class A {\n"
                        + "    public static void main(String[] args) {\n"
                        + "        new A().foo();\n"
                        + "    }\n"
                        + "}\n"),
                new File("t/B.java", "package t; public class B extends A {\n"
                        + "}\n"));
    }
    
    public void testPolymorphicMultiple() throws Exception {
        String source;
        writeFilesAndWaitForScan(src,
                new File("t/A.java", "package t; public class A {\n"
                        + "    public void foo() {\n"
                        + "        A a = new A();\n"
                        + "    }\n"
                        + "}\n"),
                new File("t/I.java", "package t; public interface I {\n"
                        + "public void foo();\n"
                        + "}\n"),
                new File("t/B.java", source = "package t; public class B extends A implements I {\n"
                        + "public void foo() { }\n"
                        + "}\n"));
        performSafeDelete(src.getFileObject("t/B.java"), source.indexOf("foo") + 1, false, new Problem(false, "WRN_Implements"));
        verifyContent(src,
                new File("t/A.java", "package t; public class A {\n"
                        + "}\n"),
                new File("t/I.java", "package t; public interface I {\n"
                        + "public void foo();\n"
                        + "}\n"),
                new File("t/B.java", "package t; public class B extends A implements I {\n"
                        + "}\n"));
    }
    
    public void testMainArgs() throws Exception {
        String source;
        writeFilesAndWaitForScan(src,
                new File("t/A.java", source = "package t; public class A {\n"
                + "    public static void main(String[] args) {\n"
                + "        A a = new A();\n"
                + "    }\n"
                + "}\n"));
        performSafeDelete(src.getFileObject("t/A.java"), source.indexOf("args") + 1, false);
        verifyContent(src,
                new File("t/A.java", "package t; public class A {\n"
                + "    public static void main() {\n"
                + "        A a = new A();\n"
                + "    }\n"
                + "}\n"));
    }
    
    public void testMainArgsUsed() throws Exception {
        String source;
        writeFilesAndWaitForScan(src,
                new File("t/A.java", source = "package t; public class A {\n"
                + "    public static void main(String[] args) {\n"
                + "        for (int i = 0; i < args.length; i++) {\n"
                + "            String string = args[i];\n"
                + "        }\n"
                + "    }\n"
                + "}\n"));
        performSafeDelete(src.getFileObject("t/A.java"), source.indexOf("args") + 1, false, new Problem(false, "ERR_ReferencesFound"));
        verifyContent(src,
                new File("t/A.java", "package t; public class A {\n"
                + "    public static void main() {\n"
                + "        for (int i = 0; i < args.length; i++) {\n"
                + "            String string = args[i];\n"
                + "        }\n"
                + "    }\n"
                + "}\n"));
    }
    
    private void performSafeDelete(FileObject source, final int position, final boolean checkInComments, Problem... expectedProblems) throws Exception {
        final SafeDeleteRefactoring[] r = new SafeDeleteRefactoring[1];
        
        if(source.isFolder() || position == 0) {
            r[0] = new SafeDeleteRefactoring(Lookups.fixed(source));
            r[0].setCheckInComments(checkInComments);
        } else {

            JavaSource.forFileObject(source).runUserActionTask(new Task<CompilationController>() {

                @Override
                public void run(CompilationController javac) throws Exception {
                    javac.toPhase(JavaSource.Phase.RESOLVED);
                    CompilationUnitTree cut = javac.getCompilationUnit();

                    if(position > 0) {
                        TreePath tp = javac.getTreeUtilities().pathFor(position);
                        r[0] = new SafeDeleteRefactoring(Lookups.fixed(TreePathHandle.create(tp, javac)));
                    } else {
                        List<TreePathHandle> handles = new ArrayList<>();
                        for (Tree typeDecl : cut.getTypeDecls()) {
                            for (Tree member : ((ClassTree)typeDecl).getMembers()) {
                                handles.add(TreePathHandle.create(javac.getTrees().getPath(cut, member), javac));
                            }
                        }
                        r[0] = new SafeDeleteRefactoring((Lookups.fixed(handles.toArray(new TreePathHandle[0]))));
                    }
                    r[0].setCheckInComments(checkInComments);
                }
            }, true);
        }

        RefactoringSession rs = RefactoringSession.create("Safe Delete Test");
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
