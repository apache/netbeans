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
import java.util.concurrent.atomic.AtomicInteger;
import javax.lang.model.SourceVersion;
import org.netbeans.api.java.source.CompilationController;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.Task;
import org.netbeans.api.java.source.TreePathHandle;
import org.netbeans.modules.refactoring.api.Problem;
import org.netbeans.modules.refactoring.api.RefactoringSession;
import org.netbeans.modules.refactoring.java.api.InnerToOuterRefactoring;
import static org.netbeans.modules.refactoring.java.test.RefactoringTestBase.addAllProblems;
import static org.netbeans.modules.refactoring.java.test.RefactoringTestBase.writeFilesAndWaitForScan;

/**
 * Test inner to outer refactoring for test.
 *
 * @author homberghp {@code <pieter.van.den.hombergh@gmail.com)>}
 */
public class InnerOuterRecordTest extends RefactoringTestBase {

    public InnerOuterRecordTest(String name) {
        super(name);
    }

    public void test259004() throws Exception {
        String source;
        writeFilesAndWaitForScan(src,
                new File("t/A.java",
                        source = """
                        package t;

                        import java.util.function.Consumer;

                        public class A {

                            public static void main(String[] args) {
                                Consumer<F> c = f -> {};
                            }

                            public static final class F {}
                        }"""));
        performInnerToOuterTest2(null);
        verifyContent(src,
                new File("t/A.java",
                        source = """
                        package t;

                        import java.util.function.Consumer;

                        public class A {

                        public static void main(String[] args) {
                            Consumer<F> c = f -> {};
                        }

                        }"""),
                new File("t/F.java",
                        """
                        /*
                         * Refactoring License
                         */

                        package t;

                        /**
                         *
                         * @author junit
                         */
                        public final class F {
                        }
                        """));
    }

    public void testApacheNetbeans7044() throws Exception {
        //ensure we are running on at least 17.
        try {
            SourceVersion.valueOf("RELEASE_17"); //NOI18N
        } catch (IllegalArgumentException ex) {
            //OK, no RELEASE_17, skip test
            return;
        }
        // initial outer has record with meaningful canonical constructor.
        writeFilesAndWaitForScan(src, new File("t/A.java",
                """
                    package t;

                    import java.time.LocalDate;
                    import java.util.Objects;

                    public class A {

                        record Student(int id, String name, LocalDate dob) {

                            public Student {
                                Objects.requireNonNull(id);
                                Objects.requireNonNull(name);
                                Objects.requireNonNull(dob);
                                assert !name.isEmpty() && !name.isBlank();
                                assert dob.isAfter(LocalDate.EPOCH);
                            }
                        }

                        void useStudent() {
                            var s = new Student(42,"Jan Klaassen", LocalDate.now().minusDays(1));
                            System.out.println("student = " + s); System.out.println("student = " + s);
                        }
                    }

                    """));
        performInnerToOuterTest2(null);
        verifyContent(src,
                new File("t/A.java",
                        """
                    package t;

                    import java.time.LocalDate;
                    import java.util.Objects;

                    public class A {

                        void useStudent(Student s) {
                            System.out.println("student = " + s);
                        }
                    }
                    """),
                new File("t/Student.java",
                        """
                        record Student(int id, String name, LocalDate dob) {
                        
                            public Student {
                                Objects.requireNonNull(id);
                                Objects.requireNonNull(name);
                                Objects.requireNonNull(dob);
                                assert !name.isEmpty() && !name.isBlank();
                                assert dob.isAfter(LocalDate.EPOCH);
                            }
                        }
                        """
                ));

    }

    public void testApacheNetbeans7044a() throws Exception {
        //ensure we are running on at least 17.
        try {
            SourceVersion.valueOf("RELEASE_17"); //NOI18N
        } catch (IllegalArgumentException ex) {
            //OK, no RELEASE_17, skip test
            return;
        }
        // initial outer has record with meaningful canonical constructor.
        writeFilesAndWaitForScan(src, new File("t/A.java",
                """
                    package t;

                    import java.time.LocalDate;
                    import java.util.Objects;

                    public class A {

                        public static class Student {
                            int id;
                            String name;
                            LocalDate dob
                            public Student(int id, String name, LocalDate dob) {
                                Objects.requireNonNull(id);
                                Objects.requireNonNull(name);
                                Objects.requireNonNull(dob);
                                assert !name.isEmpty() && !name.isBlank();
                                assert dob.isAfter(LocalDate.EPOCH);
                                this.id=id;
                                this.name=name;
                                this.dob=dob;
                            }
                        }

                        void useStudent() {
                            var s = new Student(42,"Jan Klaassen", LocalDate.now().minusDays(1));
                            System.out.println("student = " + s); System.out.println("student = " + s);
                        }
                    }

                    """.trim()));
        performInnerToOuterTest2(null);
        verifyContent(src,
                new File("t/A.java",
                        """
                    package t;


                    public class A {

                        void useStudent(Student s) {
                            System.out.println("student = " + s);
                        }
                    }
                    """.trim()),
                new File("t/Student.java",
                        """
                        import java.time.LocalDate;
                        import java.util.Objects;

                        record Student(int id, String name, LocalDate dob) {
                        
                            public Student {
                                Objects.requireNonNull(id);
                                Objects.requireNonNull(name);
                                Objects.requireNonNull(dob);
                                assert !name.isEmpty() && !name.isBlank();
                                assert dob.isAfter(LocalDate.EPOCH);
                                this.id=id;
                                this.name=name;
                                this.dob=dob;
                            }
                        }
                        """.trim()
                ));

    }

    // variant for record inner to outer test
    private void performInnerToOuterTest2(String generateOuter, Problem... expectedProblems) throws Exception {
        final InnerToOuterRefactoring[] r = new InnerToOuterRefactoring[1];

        JavaSource.forFileObject(src.getFileObject("t/A.java")).runUserActionTask(new Task<CompilationController>() {

            @Override
            public void run(CompilationController parameter) throws Exception {
                parameter.toPhase(JavaSource.Phase.RESOLVED);
                CompilationUnitTree cut = parameter.getCompilationUnit();
//                System.out.println("cut class = " + cut.getClass());
                ClassTree outter = (ClassTree)cut.getTypeDecls().get(0);
                printNumbered("outter", outter.toString());
                List<? extends Tree> members = outter.getMembers();
                int m = 0;
                for (Tree member : members) {
                    printNumbered("member " + (m++), member.toString());
                }
                var tps = cut.getTypeDecls();
                for (int i = 0; i < tps.size(); i++) {
                    var type = tps.get(i);
                    var kind = type.getKind();
                    printNumbered("decl " + i + " " + kind + " ", type.toString());
                }
                var inner = outter.getMembers().get(outter.getMembers().size() - 1);
                printNumbered("inner", inner.toString());
                TreePath tp = TreePath.getPath(cut, inner);
                r[0] = new InnerToOuterRefactoring(TreePathHandle.create(tp, parameter));
            }
        }, true);

        r[0].setClassName("F");
        printNumbered("result ", r[0].toString());
        r[0].setReferenceName(generateOuter);

        RefactoringSession rs = RefactoringSession.create("Session");
        List<Problem> problems = new LinkedList<Problem>();

        addAllProblems(problems, r[0].preCheck());
        addAllProblems(problems, r[0].prepare(rs));
        addAllProblems(problems, rs.doRefactoring(true));

        assertProblems(Arrays.asList(expectedProblems), problems);
    }
    
    static boolean debug = true;

    static void printNumbered(final String name, String source) {
        if (!debug) {
            return;
        }
        AtomicInteger c = new AtomicInteger(1);
        source.trim().lines().forEach(l -> System.out.println("%s [%4d] %s".formatted(name, c.getAndIncrement(), l)));
    }
}
