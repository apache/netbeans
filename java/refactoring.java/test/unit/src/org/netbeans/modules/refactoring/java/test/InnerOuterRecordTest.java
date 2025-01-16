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
import java.io.IOException;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Name;
import org.junit.FixMethodOrder;
import org.junit.runners.MethodSorters;
import org.netbeans.api.java.source.CompilationController;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.Task;
import org.netbeans.api.java.source.TreePathHandle;
import org.netbeans.modules.refactoring.api.Problem;
import org.netbeans.modules.refactoring.api.RefactoringSession;
import org.netbeans.modules.refactoring.java.api.InnerToOuterRefactoring;
import static org.netbeans.modules.refactoring.java.test.RefactoringTestBase.addAllProblems;
import org.openide.util.Exceptions;

/**
 * Test inner to outer refactoring for test.
 *
 * In the input files, and the expected outcomes, the indentation does not
 * really matter as far as the tests are concerned because the indentation is
 * stripped away before the remaining source lines are compared to the expected
 * lines.
 *
 * @author homberghp {@code <pieter.van.den.hombergh@gmail.com)>}
 */
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class InnerOuterRecordTest extends RefactoringTestBase {

    public InnerOuterRecordTest(String name) {
        super(name);
        //ensure we are running on at least 16.
        try {
            SourceVersion.valueOf("RELEASE_16"); //NOI18N
        } catch (IllegalArgumentException ex) {
            //OK, no RELEASE_16, skip test
            throw new RuntimeException("need at least Java 16 for record");
        }
    }

    // for reference
    public void test0_259004() throws Exception {
        String source
                = """
            package t;

            import java.util.function.Consumer;

            public class A {

                public static void main(String[] args) {
                    Consumer<F> c = f -> {};
                }

                public static final class F {}
            }""";
        String newOuter
                = """
            package t;

            import java.util.function.Consumer;

            public class A {

            public static void main(String[] args) {
                Consumer<F> c = f -> {};
            }

            }""";
        String newInner
                = """
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
            """;
        innerOuterSetupAndTest(source, newOuter, newInner);

    }

    public void test9ApacheNetbeans7044() throws Exception {
        // initial outer has record with meaningful canonical constructor.
        // note that Inner class should be in last member for assumptions in the test.
        String source
                = """
                package t;

                import java.time.LocalDate;
                import java.util.Objects;

                public class A {

                    void useStudent() {
                        F s = new F(42,"Jan Klaassen", LocalDate.now().minusDays(1));
                        System.out.println("student = " + s);
                    }

                    record F(int id, String name, LocalDate dob) {

                        /**
                          * Validate stuff.
                          */
                        public F {
                            Objects.requireNonNull(id);
                            Objects.requireNonNull(name);
                            Objects.requireNonNull(dob);
                            assert !name.isEmpty() && !name.isBlank();
                            assert dob.isAfter(LocalDate.EPOCH);
                        }
                    }

                }
                """;
        String newOuter
                = """
                package t;

                import java.time.LocalDate;
                import java.util.Objects;

                public class A {

                    void useStudent() {
                        F s = new F(42,"Jan Klaassen", LocalDate.now().minusDays(1));
                        System.out.println("student = " + s);
                    }
                }
                """;
        String newInner
                = """
                /*
                 * Refactoring License
                 */
                package t;

                import java.time.LocalDate;
                import java.util.Objects;
                /**
                  *
                  * @author junit
                  */
                record F(int id, String name, LocalDate dob) {

                    /**
                     * Validate stuff.
                     */
                    public F {
                        Objects.requireNonNull(id);
                        Objects.requireNonNull(name);
                        Objects.requireNonNull(dob);
                        assert !name.isEmpty() && !name.isBlank();
                        assert dob.isAfter(LocalDate.EPOCH);
                    }
                }
                """;

        innerOuterSetupAndTest(source, newOuter, newInner);
    }

    public void test1BasicClassInClass() throws Exception {
        // initial outer has record with meaningful canonical constructor.
        String source
                = """
            package t;

            import java.time.LocalDate;
            import java.util.Objects;

            public class A {

                void useStudent() {
                    F s = new F(42, "Jan Klaassen", LocalDate.now().minusDays(1));
                    System.out.println("student = " + s);
                }

                public static class F {
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

            }
            """;
        String newOuter
                = """
                package t;

                import java.time.LocalDate;
                import java.util.Objects;

                public class A {

                    void useStudent() {
                        F s = new F(42, "Jan Klaassen", LocalDate.now().minusDays(1));
                        System.out.println("student = " + s);
                    }


                }
                """;
        String newInner
                = """
                /*
                 * Refactoring License
                 */

                package t;

                import java.time.LocalDate;
                import java.util.Objects;

                /**
                 *
                 * @author junit
                 */
                public class F {

                    int id;
                    String name;
                    LocalDate dob;

                    public F(int id, String name, LocalDate dob) {
                        Objects.requireNonNull(id);
                        Objects.requireNonNull(name);
                        Objects.requireNonNull(dob);
                        assert !name.isEmpty() && !name.isBlank();
                        assert dob.isAfter(LocalDate.EPOCH);
                        this.id = id;
                        this.name = name;
                        this.dob = dob;
                    }

                }
                """;

        innerOuterSetupAndTest(source, newOuter, newInner);
    }

    public void test2BasicRecordInRecord() throws Exception {
        String source
                = """
                package t;

                import java.time.LocalDate;

                record A(int id, String name, LocalDate dob) {
                   static F f;
                   record F(int x, int y){
                      /** I should be back. */
                      static String code = "nix";
                   }
                }
                """;
        String newOuter
                = """
                package t;

                import java.time.LocalDate;

                record A(int id, String name, LocalDate dob) {

                    static F f;

                }
                """;
        String newInner
                = """
                /*
                 * Refactoring License
                 */
                package t;

                /**
                 *
                 * @author hom
                 */
                record F(int x, int y) {
                    /** I should be back. */
                    static String code = "nix";
                }
                """;

        innerOuterSetupAndTest(source, newOuter, newInner);
    }

    /**
     * Test to verify what happens to the compact constructor in the outer
     * record. It appears to survive the refactoring.
     *
     * @throws Exception
     */
    public void test3OuterWithCompact() throws Exception {
        String source
                = """
                package t;
                import java.time.LocalDate;
                /** Record with compact ctor. */
                record A(F f){

                     public A{
                         assert f!=null;
                     }
                     record F(int id, String name, LocalDate dob){}
                }
                """;
        String newOuter
                = """
                package t;
                import java.time.LocalDate;
                /** Record with compact ctor. */
                record A(F f){
                    public A{
                         assert f!=null;
                    }
                }
                """;
        String newInner
                = """
                /*
                 * Refactoring License
                 */
                package t;
                import java.time.LocalDate;
                /**
                 *
                 * @author junit
                 */
                record F(int id, String name, LocalDate dob) {
                }
                """;
        innerOuterSetupAndTest(source, newOuter, newInner);
    }

    public void test4InnerWithCompact() throws Exception {
        String source
                = """
                package t;
                
                import java.time.LocalDate;
                
                record A(F f) {
                
                    public A {
                        assert f != null;
                    }
                
                    record F(int id, String name, LocalDate dob) {
                
                        public F {
                            if (dob.isBefore(LocalDate.EPOCH)) {
                                throw new IllegalArgumentException("to old " + dob);
                            }
                        }
                    }
                }
                """;
        String newOuter
                = """
                package t;
                import java.time.LocalDate;
                record A(F f) {
                public A {
                        assert f != null;
                    }
                }
                """;
        String newInner
                = """
                /*
                 * Refactoring License
                 */
                package t;
                import java.time.LocalDate;
                /**
                 *
                 * @author junit
                 */
                record F(int id, String name, LocalDate dob) {

                    public F {
                        if (dob.isBefore(LocalDate.EPOCH)) {
                            throw new IllegalArgumentException("to old " + dob);
                        }
                    }
                }
                """;
        innerOuterSetupAndTest(source, newOuter, newInner);
    }

    // outer may have effect
    public void test5ClassWithInnerRecord() throws Exception {
        String source
                = """
                package t;
                
                import java.time.LocalDate;
                
                class A {
                    final F f;
                    public A(F f) {
                        assert f != null;
                        this.f=f;
                    }
                
                    record F(int id, String name, LocalDate dob) {
                
                        public F   {
                            if (dob.isBefore(LocalDate.EPOCH)) {
                                throw new IllegalArgumentException("to old " + dob);
                            }
                        }
                    }
                }
                """;
        String newOuter
                = """
                package t;
                import java.time.LocalDate;
                class A {
                    final F f;
                    public A(F f) {
                        assert f != null;
                        this.f=f;
                    }
                }
                """;
        String newInner
                = """
                /*
                 * Refactoring License
                 */
                package t;
                import java.time.LocalDate;
                /**
                 *
                 * @author junit
                 */
                record F(int id, String name, LocalDate dob) {

                    public F {
                        if (dob.isBefore(LocalDate.EPOCH)) {
                            throw new IllegalArgumentException("to old " + dob);
                        }
                    }
                }
                """;
        innerOuterSetupAndTest(source, newOuter, newInner);
    }
    
    public void test6InnerWithCompactAndMethodAndExtraCtor() throws Exception {
        String source
                = """
                package t;
                
                import java.time.LocalDate;
                record A(F f) {

                enum Suite {
                    SPADE, CLUB, DIAMOND, HEART;
                }
                
                    public A {
                        assert f != null;
                    }
                
                    record F(int id, String name, LocalDate dob) {
                
                        public F {
                            if (dob.isBefore(LocalDate.EPOCH)) {
                                throw new IllegalArgumentException("to old " + dob);
                            }
                        }
                        public F(int id, String name){
                            this(id,name,LocalDate.now());
                        }
                        boolean bornBefore(LocalDate someDate) {
                            return dob.isBefore(someDate);
                        } 
                    }
                }
                """;
        String newOuter
                = """
                package t;
                import java.time.LocalDate;
                record A(F f) {
                    enum Suite { 
                      SPADE, CLUB, DIAMOND, HEART;
                    }
                    public A {
                        assert f != null;
                    }
                }
                """;
        String newInner
                = """
                /*
                 * Refactoring License
                 */
                package t;
                import java.time.LocalDate;
                /**
                 *
                 * @author junit
                 */
                record F(int id, String name, LocalDate dob) {

                    public F {
                        if (dob.isBefore(LocalDate.EPOCH)) {
                            throw new IllegalArgumentException("to old " + dob);
                        }
                    }
                    public F(int id, String name) {
                        this(id, name, LocalDate.now());
                    }
                    boolean bornBefore(LocalDate someDate) {
                        return dob.isBefore(someDate);
                    } 
                }
                """;
        innerOuterSetupAndTest(source, newOuter, newInner);
    }

    
    public void test7Generic() throws Exception{
    String source
                = """
                package t;
                
                record A(F f) {
                    public A {
                        assert f != null;
                    }
                    record F<P, Q>(P first, Q second) {
                
                        public F {
                            assert null != first;
                            assert null != second;
                        }
                    }
                }
                """;
        String newOuter
                = """
                package t;
                record A(F f) {
                    public A {
                        assert f != null;
                    }
                }
                """;
        String newInner
                = """
                /*
                 * Refactoring License
                 */
                package t;
                /**
                 *
                 * @author junit
                 */
                record F<P, Q>(P first, Q second) {
                    public F {
                        assert null != first;
                        assert null != second;
                    }
                }
                """;
        innerOuterSetupAndTest(source, newOuter, newInner);

    }
    
    void innerOuterSetupAndTest(String source, String newOuterName, String newInnerName) throws Exception {
        writeFilesNoIndexing(src, new File("t/A.java", source));
        performInnerToOuterTest2(null);
        verifyContent(src, new File("t/A.java", newOuterName), new File("t/F.java", newInnerName));
    }

    boolean debug = false;

    // variant for record inner to outer test
    private void performInnerToOuterTest2(String newOuterName, Problem... expectedProblems) throws Exception {
        final InnerToOuterRefactoring[] r = new InnerToOuterRefactoring[1];
        JavaSource.forFileObject(src.getFileObject("t/A.java")).runUserActionTask(new Task<CompilationController>() {

            @Override
            public void run(CompilationController parameter) {
                try {
                    parameter.toPhase(JavaSource.Phase.RESOLVED);
                } catch (IOException ex) {
                    Exceptions.printStackTrace(ex);
                }
                CompilationUnitTree cut = parameter.getCompilationUnit();
                if (debug) {
                    System.err.println("cut is of type " + cut.getClass().getCanonicalName());
                }
                ClassTree outer = (ClassTree) cut.getTypeDecls().get(0);
                if (debug) {
                    printNumbered(System.err, "start source " + outer.getKind().toString(), outer.toString());
                }
                List<? extends Tree> members = outer.getMembers();
                int m = 0;
                if (debug) {
                    printMembers(members, m);
                }
                // selecting the last element assumes that the inner class is the last member in the outer class.
                Tree lastInnerClass = outer.getMembers().get(outer.getMembers().size() - 1);
                if (debug && lastInnerClass instanceof ClassTree lct) {
//                    String n = "lastInnerClass " + lastInnerClass.getKind().toString();
//                    printNumbered(System.err, n, lastInnerClass.toString());
                    printClassTree(lct);
                }
                TreePath tp = TreePath.getPath(cut, lastInnerClass);

                try {
                    r[0] = new InnerToOuterRefactoring(TreePathHandle.create(tp, parameter));
                } catch (Throwable t) {
                    System.err.println("InnerOuter refatoring failed with exception " + t);
                    t.printStackTrace(System.out);
                    throw t;
                }
            }

        }, true);

        r[0].setClassName("F");
        if (debug) {
            printNumbered(System.err, "result ", r[0].toString());
        }
        r[0].setReferenceName(newOuterName);

        RefactoringSession rs = RefactoringSession.create("Session");
        List<Problem> problems = new LinkedList<Problem>();

        addAllProblems(problems, r[0].preCheck());
        addAllProblems(problems, r[0].prepare(rs));
        addAllProblems(problems, rs.doRefactoring(true));

        assertProblems(Arrays.asList(expectedProblems), problems);
    }

    // test helper
    static void printMembers(List<? extends Tree> members, int m) {
        printMembers(members, m, "");
    }

    // test helper
    static void printMembers(List<? extends Tree> members, int m, String indent) {
        for (Tree member : members) {
            printNumbered(System.err, indent + "member %d %15s".formatted(m, member.getKind()), member.toString());
            String toString = member.toString();
            if (member instanceof ClassTree ct) {
                int n = 0;
                Name simpleName = ct.getSimpleName();
                List<? extends Tree> members1 = ct.getMembers();
                printMembers(members1, n, indent + "  " + m + " ");
            }
            m++;
        }
    }

    // test helper
    static void printClassTree(ClassTree ct) {
        printMembers(ct.getMembers(), 0, "class " + ct.getSimpleName() + " type " + ct.getKind() + " ");
    }

}
