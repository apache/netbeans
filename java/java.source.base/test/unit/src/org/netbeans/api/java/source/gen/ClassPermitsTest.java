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
package org.netbeans.api.java.source.gen;

import com.sun.source.tree.ClassTree;
import com.sun.source.tree.CompilationUnitTree;
import com.sun.source.tree.ModifiersTree;
import com.sun.source.tree.Tree;
import com.sun.source.tree.VariableTree;
import java.io.File;
import java.io.IOException;
import java.util.*;
import javax.lang.model.element.Modifier;
import javax.lang.model.type.TypeKind;
import org.netbeans.api.java.source.Task;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.TestUtilities;
import org.netbeans.api.java.source.TreeMaker;
import static org.netbeans.api.java.source.JavaSource.*;
import org.netbeans.api.java.source.WorkingCopy;
import org.netbeans.junit.NbTestSuite;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

/**
 * Test adding/removing/modifying permits clause in source.
 * In addition to, tries to work with extends in interfaces.
 */
public class ClassPermitsTest extends GeneratorTestMDRCompat {

    /** Creates a new instance of ClassExtendsTest */
    public ClassPermitsTest(String testName) {
        super(testName);
    }

    public static NbTestSuite suite() {
        NbTestSuite suite = new NbTestSuite();
        suite.addTestSuite(ClassPermitsTest.class);
        return suite;
    }

    public void testModifyExistingPermits1() throws Exception {
        testFile = new File(getWorkDir(), "Test.java");
        TestUtilities.copyStringToFile(testFile,
            """
            package hierbas.del.litoral;

            public sealed class Test permits Subtype1 {
            }
            final class Subtype1 extends Test {}
            final class Subtype2 extends Test {}
            """
        );
        String golden =
            """
            package hierbas.del.litoral;

            public sealed class Test permits Subtype2 {
            }
            final class Subtype1 extends Test {}
            final class Subtype2 extends Test {}
            """;
        JavaSource src = getJavaSource(testFile);

        Task<WorkingCopy> task = new Task<WorkingCopy>() {

            public void run(WorkingCopy workingCopy) throws IOException {
                workingCopy.toPhase(Phase.RESOLVED);
                CompilationUnitTree cut = workingCopy.getCompilationUnit();
                TreeMaker make = workingCopy.getTreeMaker();
                ClassTree clazz = (ClassTree) cut.getTypeDecls().get(0);
                workingCopy.rewrite(clazz.getPermitsClause().get(0),
                                    make.setLabel(clazz.getPermitsClause().get(0), "Subtype2"));
            }

        };
        src.runModificationTask(task).commit();
        String res = TestUtilities.copyFileToString(testFile);
        //System.err.println(res);
        assertEquals(golden, res);
    }

    public void testModifyExistingPermits2() throws Exception {
        testFile = new File(getWorkDir(), "Test.java");
        String code = """
                      package hierbas.del.litoral;

                      public sealed class Test permits Subtype2 {
                      }
                      final class Subtype1 extends Test {}
                      final class Subtype2 extends Test {}
                      final class Subtype3 extends Test {}
                      """;
        TestUtilities.copyStringToFile(testFile, code);

        JavaSource src = getJavaSource(testFile);

        Task<WorkingCopy> task;
        String res;

        //add first:
        task = (WorkingCopy workingCopy) -> {
            workingCopy.toPhase(Phase.RESOLVED);
            CompilationUnitTree cut = workingCopy.getCompilationUnit();
            TreeMaker make = workingCopy.getTreeMaker();
            ClassTree clazz = (ClassTree) cut.getTypeDecls().get(0);
            List<Tree> augmentedPermits = new ArrayList<>(clazz.getPermitsClause());
            augmentedPermits.add(0, make.QualIdent("hierbas.del.litoral.Subtype1"));
            ClassTree newClass = make.Class(clazz.getModifiers(), clazz.getSimpleName(), clazz.getTypeParameters(), clazz.getExtendsClause(), clazz.getImplementsClause(), augmentedPermits, clazz.getMembers());
            workingCopy.rewrite(clazz, newClass);
        };
        src.runModificationTask(task).commit();
        res = TestUtilities.copyFileToString(testFile);
        //System.err.println(res);
        assertEquals("""
                     package hierbas.del.litoral;

                     public sealed class Test permits Subtype1, Subtype2 {
                     }
                     final class Subtype1 extends Test {}
                     final class Subtype2 extends Test {}
                     final class Subtype3 extends Test {}
                     """, res);

        //remove first:
        task = (WorkingCopy workingCopy) -> {
            workingCopy.toPhase(Phase.RESOLVED);
            CompilationUnitTree cut = workingCopy.getCompilationUnit();
            TreeMaker make = workingCopy.getTreeMaker();
            ClassTree clazz = (ClassTree) cut.getTypeDecls().get(0);
            List<Tree> augmentedPermits = new ArrayList<>(clazz.getPermitsClause());
            augmentedPermits.remove(0);
            ClassTree newClass = make.Class(clazz.getModifiers(), clazz.getSimpleName(), clazz.getTypeParameters(), clazz.getExtendsClause(), clazz.getImplementsClause(), augmentedPermits, clazz.getMembers());
            workingCopy.rewrite(clazz, newClass);
        };
        src.runModificationTask(task).commit();
        res = TestUtilities.copyFileToString(testFile);
        //System.err.println(res);
        assertEquals(code, res);

        //add last:
        task = (WorkingCopy workingCopy) -> {
            workingCopy.toPhase(Phase.RESOLVED);
            CompilationUnitTree cut = workingCopy.getCompilationUnit();
            TreeMaker make = workingCopy.getTreeMaker();
            ClassTree clazz = (ClassTree) cut.getTypeDecls().get(0);
            List<Tree> augmentedPermits = new ArrayList<>(clazz.getPermitsClause());
            augmentedPermits.add(make.QualIdent("hierbas.del.litoral.Subtype3"));
            ClassTree newClass = make.Class(clazz.getModifiers(), clazz.getSimpleName(), clazz.getTypeParameters(), clazz.getExtendsClause(), clazz.getImplementsClause(), augmentedPermits, clazz.getMembers());
            workingCopy.rewrite(clazz, newClass);
        };
        src.runModificationTask(task).commit();
        res = TestUtilities.copyFileToString(testFile);
        //System.err.println(res);
        assertEquals("""
                     package hierbas.del.litoral;

                     public sealed class Test permits Subtype2, Subtype3 {
                     }
                     final class Subtype1 extends Test {}
                     final class Subtype2 extends Test {}
                     final class Subtype3 extends Test {}
                     """, res);

        //remove last:
        task = (WorkingCopy workingCopy) -> {
            workingCopy.toPhase(Phase.RESOLVED);
            CompilationUnitTree cut = workingCopy.getCompilationUnit();
            TreeMaker make = workingCopy.getTreeMaker();
            ClassTree clazz = (ClassTree) cut.getTypeDecls().get(0);
            List<Tree> augmentedPermits = new ArrayList<>(clazz.getPermitsClause());
            augmentedPermits.remove(1);
            ClassTree newClass = make.Class(clazz.getModifiers(), clazz.getSimpleName(), clazz.getTypeParameters(), clazz.getExtendsClause(), clazz.getImplementsClause(), augmentedPermits, clazz.getMembers());
            workingCopy.rewrite(clazz, newClass);
        };
        src.runModificationTask(task).commit();
        res = TestUtilities.copyFileToString(testFile);
        //System.err.println(res);
        assertEquals(code, res);
    }

    public void testIntroduceRemovePermits() throws Exception {
        testFile = new File(getWorkDir(), "Test.java");
        String code = """
                      package hierbas.del.litoral;

                      public sealed class Test {
                      }
                      final class Subtype1 extends Test {}
                      final class Subtype2 extends Test {}
                      final class Subtype3 extends Test {}
                      """;
        TestUtilities.copyStringToFile(testFile, code);

        JavaSource src = getJavaSource(testFile);

        Task<WorkingCopy> task;
        String res;

        //add first:
        task = (WorkingCopy workingCopy) -> {
            workingCopy.toPhase(Phase.RESOLVED);
            CompilationUnitTree cut = workingCopy.getCompilationUnit();
            TreeMaker make = workingCopy.getTreeMaker();
            ClassTree clazz = (ClassTree) cut.getTypeDecls().get(0);
            List<Tree> augmentedPermits = new ArrayList<>();
            augmentedPermits.add(make.QualIdent("hierbas.del.litoral.Subtype1"));
            ClassTree newClass = make.Class(clazz.getModifiers(), clazz.getSimpleName(), clazz.getTypeParameters(), clazz.getExtendsClause(), clazz.getImplementsClause(), augmentedPermits, clazz.getMembers());
            workingCopy.rewrite(clazz, newClass);
        };
        src.runModificationTask(task).commit();
        res = TestUtilities.copyFileToString(testFile);
        //System.err.println(res);
        assertEquals("""
                     package hierbas.del.litoral;

                     public sealed class Test permits Subtype1 {
                     }
                     final class Subtype1 extends Test {}
                     final class Subtype2 extends Test {}
                     final class Subtype3 extends Test {}
                     """, res);

        //remove first:
        task = (WorkingCopy workingCopy) -> {
            workingCopy.toPhase(Phase.RESOLVED);
            CompilationUnitTree cut = workingCopy.getCompilationUnit();
            TreeMaker make = workingCopy.getTreeMaker();
            ClassTree clazz = (ClassTree) cut.getTypeDecls().get(0);
            List<Tree> augmentedPermits = new ArrayList<>(clazz.getPermitsClause());
            augmentedPermits.remove(0);
            ClassTree newClass = make.Class(clazz.getModifiers(), clazz.getSimpleName(), clazz.getTypeParameters(), clazz.getExtendsClause(), clazz.getImplementsClause(), augmentedPermits, clazz.getMembers());
            workingCopy.rewrite(clazz, newClass);
        };
        src.runModificationTask(task).commit();
        res = TestUtilities.copyFileToString(testFile);
        //System.err.println(res);
        assertEquals(code, res);
    }

    public void testClassModification() throws Exception {
        testFile = new File(getWorkDir(), "Test.java");
        String code;
        code = """
               package hierbas.del.litoral;

               public sealed class Test permits Subtype1 {
               }
               final class Subtype1 extends Test {}
               """;
        TestUtilities.copyStringToFile(testFile, code);

        JavaSource src = getJavaSource(testFile);
        FileObject testFileFO = FileUtil.toFileObject(testFile);

        Task<WorkingCopy> task;
        String res;

        task = (WorkingCopy workingCopy) -> {
            workingCopy.toPhase(Phase.RESOLVED);
            CompilationUnitTree cut = workingCopy.getCompilationUnit();
            TreeMaker make = workingCopy.getTreeMaker();
            ClassTree clazz = (ClassTree) cut.getTypeDecls().get(0);
            ClassTree newClass = make.setLabel(clazz, "Test2");
            workingCopy.rewrite(clazz, newClass);
        };
        res = src.runModificationTask(task).getResultingSource(testFileFO);
        //System.err.println(res);
        assertEquals("""
                     package hierbas.del.litoral;

                     public sealed class Test2 permits Subtype1 {
                     }
                     final class Subtype1 extends Test {}
                     """,
                     res);

        task = (WorkingCopy workingCopy) -> {
            workingCopy.toPhase(Phase.RESOLVED);
            CompilationUnitTree cut = workingCopy.getCompilationUnit();
            TreeMaker make = workingCopy.getTreeMaker();
            ClassTree clazz = (ClassTree) cut.getTypeDecls().get(0);
            ClassTree newClass = make.setExtends(clazz, make.Identifier("Object"));
            workingCopy.rewrite(clazz, newClass);
        };
        res = src.runModificationTask(task).getResultingSource(testFileFO);
        //System.err.println(res);
        assertEquals("""
                     package hierbas.del.litoral;

                     public sealed class Test extends Object permits Subtype1 {
                     }
                     final class Subtype1 extends Test {}
                     """,
                     res);

        task = (WorkingCopy workingCopy) -> {
            workingCopy.toPhase(Phase.RESOLVED);
            CompilationUnitTree cut = workingCopy.getCompilationUnit();
            TreeMaker make = workingCopy.getTreeMaker();
            ClassTree clazz = (ClassTree) cut.getTypeDecls().get(0);
            ClassTree newClass = make.addClassImplementsClause(clazz, make.Identifier("Runnable"));
            workingCopy.rewrite(clazz, newClass);
        };
        res = src.runModificationTask(task).getResultingSource(testFileFO);
        //System.err.println(res);
        assertEquals("""
                     package hierbas.del.litoral;

                     public sealed class Test implements Runnable permits Subtype1 {
                     }
                     final class Subtype1 extends Test {}
                     """,
                     res);

        task = (WorkingCopy workingCopy) -> {
            workingCopy.toPhase(Phase.RESOLVED);
            CompilationUnitTree cut = workingCopy.getCompilationUnit();
            TreeMaker make = workingCopy.getTreeMaker();
            ClassTree clazz = (ClassTree) cut.getTypeDecls().get(0);
            ClassTree newClass = make.addClassTypeParameter(clazz, make.TypeParameter("T", List.of()));
            workingCopy.rewrite(clazz, newClass);
        };
        res = src.runModificationTask(task).getResultingSource(testFileFO);
        //System.err.println(res);
        assertEquals("""
                     package hierbas.del.litoral;

                     public sealed class Test<T> permits Subtype1 {
                     }
                     final class Subtype1 extends Test {}
                     """,
                     res);

        task = (WorkingCopy workingCopy) -> {
            workingCopy.toPhase(Phase.RESOLVED);
            CompilationUnitTree cut = workingCopy.getCompilationUnit();
            TreeMaker make = workingCopy.getTreeMaker();
            ClassTree clazz = (ClassTree) cut.getTypeDecls().get(0);
            VariableTree var = make.Variable(make.Modifiers(EnumSet.noneOf(Modifier.class)), "I", make.PrimitiveType(TypeKind.INT), null);
            ClassTree newClass = make.addClassMember(clazz, var);
            workingCopy.rewrite(clazz, newClass);
        };
        res = src.runModificationTask(task).getResultingSource(testFileFO);
        //System.err.println(res);
        assertEquals("""
                     package hierbas.del.litoral;

                     public sealed class Test permits Subtype1 {
                     
                         int I;
                     }
                     final class Subtype1 extends Test {}
                     """,
                     res);

        code = """
               package hierbas.del.litoral;

               public sealed class Test implements Runnable permits Subtype1 {
               }
               final class Subtype1 extends Test {}
               """;
        TestUtilities.copyStringToFile(testFileFO, code);

        task = (WorkingCopy workingCopy) -> {
            workingCopy.toPhase(Phase.RESOLVED);
            CompilationUnitTree cut = workingCopy.getCompilationUnit();
            TreeMaker make = workingCopy.getTreeMaker();
            ClassTree clazz = (ClassTree) cut.getTypeDecls().get(0);
            ClassTree newClass = make.removeClassImplementsClause(clazz, 0);
            workingCopy.rewrite(clazz, newClass);
        };
        res = src.runModificationTask(task).getResultingSource(testFileFO);
        //System.err.println(res);
        assertEquals("""
                     package hierbas.del.litoral;

                     public sealed class Test permits Subtype1 {
                     }
                     final class Subtype1 extends Test {}
                     """,
                     res);

        code = """
               package hierbas.del.litoral;

               public sealed class Test<T> permits Subtype1 {
               }
               final class Subtype1 extends Test {}
               """;
        TestUtilities.copyStringToFile(testFileFO, code);
        task = (WorkingCopy workingCopy) -> {
            workingCopy.toPhase(Phase.RESOLVED);
            CompilationUnitTree cut = workingCopy.getCompilationUnit();
            TreeMaker make = workingCopy.getTreeMaker();
            ClassTree clazz = (ClassTree) cut.getTypeDecls().get(0);
            ClassTree newClass = make.removeClassTypeParameter(clazz, 0);
            workingCopy.rewrite(clazz, newClass);
        };
        res = src.runModificationTask(task).getResultingSource(testFileFO);
        //System.err.println(res);
        assertEquals("""
                     package hierbas.del.litoral;

                     public sealed class Test permits Subtype1 {
                     }
                     final class Subtype1 extends Test {}
                     """,
                     res);

        code = """
               package hierbas.del.litoral;

               public sealed class Test permits Subtype1 {
                   int I;
               }
               final class Subtype1 extends Test {}
               """;
        TestUtilities.copyStringToFile(testFileFO, code);
        task = (WorkingCopy workingCopy) -> {
            workingCopy.toPhase(Phase.RESOLVED);
            CompilationUnitTree cut = workingCopy.getCompilationUnit();
            TreeMaker make = workingCopy.getTreeMaker();
            ClassTree clazz = (ClassTree) cut.getTypeDecls().get(0);
            ClassTree newClass = make.removeClassMember(clazz, 1);
            workingCopy.rewrite(clazz, newClass);
        };
        res = src.runModificationTask(task).getResultingSource(testFileFO);
        //System.err.println(res);
        assertEquals("""
                     package hierbas.del.litoral;

                     public sealed class Test permits Subtype1 {
                     }
                     final class Subtype1 extends Test {}
                     """,
                     res);
    }

    public void testInterfaceModification() throws Exception {
        testFile = new File(getWorkDir(), "Test.java");
        String code;
        code = """
               package hierbas.del.litoral;

               public sealed interface Test permits Subtype1 {
               }
               final class Subtype1 extends Test {}
               """;
        TestUtilities.copyStringToFile(testFile, code);

        JavaSource src = getJavaSource(testFile);
        FileObject testFileFO = FileUtil.toFileObject(testFile);

        Task<WorkingCopy> task;
        String res;

        task = (WorkingCopy workingCopy) -> {
            workingCopy.toPhase(Phase.RESOLVED);
            CompilationUnitTree cut = workingCopy.getCompilationUnit();
            TreeMaker make = workingCopy.getTreeMaker();
            ClassTree clazz = (ClassTree) cut.getTypeDecls().get(0);
            ClassTree newClass = make.setLabel(clazz, "Test2");
            workingCopy.rewrite(clazz, newClass);
        };
        res = src.runModificationTask(task).getResultingSource(testFileFO);
        //System.err.println(res);
        assertEquals("""
                     package hierbas.del.litoral;

                     public sealed interface Test2 permits Subtype1 {
                     }
                     final class Subtype1 extends Test {}
                     """,
                     res);

        task = (WorkingCopy workingCopy) -> {
            workingCopy.toPhase(Phase.RESOLVED);
            CompilationUnitTree cut = workingCopy.getCompilationUnit();
            TreeMaker make = workingCopy.getTreeMaker();
            ClassTree clazz = (ClassTree) cut.getTypeDecls().get(0);
            ClassTree newClass = make.setExtends(clazz, make.Identifier("Object"));
            workingCopy.rewrite(clazz, newClass);
        };
        res = src.runModificationTask(task).getResultingSource(testFileFO);
        //System.err.println(res);
        assertEquals("""
                     package hierbas.del.litoral;

                     public sealed interface Test extends Object permits Subtype1 {
                     }
                     final class Subtype1 extends Test {}
                     """,
                     res);

        task = (WorkingCopy workingCopy) -> {
            workingCopy.toPhase(Phase.RESOLVED);
            CompilationUnitTree cut = workingCopy.getCompilationUnit();
            TreeMaker make = workingCopy.getTreeMaker();
            ClassTree clazz = (ClassTree) cut.getTypeDecls().get(0);
            ClassTree newClass = make.addClassImplementsClause(clazz, make.Identifier("Runnable"));
            workingCopy.rewrite(clazz, newClass);
        };
        res = src.runModificationTask(task).getResultingSource(testFileFO);
        //System.err.println(res);
        assertEquals("""
                     package hierbas.del.litoral;

                     public sealed interface Test extends Runnable permits Subtype1 {
                     }
                     final class Subtype1 extends Test {}
                     """,
                     res);

        task = (WorkingCopy workingCopy) -> {
            workingCopy.toPhase(Phase.RESOLVED);
            CompilationUnitTree cut = workingCopy.getCompilationUnit();
            TreeMaker make = workingCopy.getTreeMaker();
            ClassTree clazz = (ClassTree) cut.getTypeDecls().get(0);
            ClassTree newClass = make.addClassTypeParameter(clazz, make.TypeParameter("T", List.of()));
            workingCopy.rewrite(clazz, newClass);
        };
        res = src.runModificationTask(task).getResultingSource(testFileFO);
        //System.err.println(res);
        assertEquals("""
                     package hierbas.del.litoral;

                     public sealed interface Test<T> permits Subtype1 {
                     }
                     final class Subtype1 extends Test {}
                     """,
                     res);

        task = (WorkingCopy workingCopy) -> {
            workingCopy.toPhase(Phase.RESOLVED);
            CompilationUnitTree cut = workingCopy.getCompilationUnit();
            TreeMaker make = workingCopy.getTreeMaker();
            ClassTree clazz = (ClassTree) cut.getTypeDecls().get(0);
            VariableTree var = make.Variable(make.Modifiers(EnumSet.noneOf(Modifier.class)), "I", make.PrimitiveType(TypeKind.INT), null);
            ClassTree newClass = make.addClassMember(clazz, var);
            workingCopy.rewrite(clazz, newClass);
        };
        res = src.runModificationTask(task).getResultingSource(testFileFO);
        //System.err.println(res);
        assertEquals("""
                     package hierbas.del.litoral;

                     public sealed interface Test permits Subtype1 {
                     
                         int I;
                     }
                     final class Subtype1 extends Test {}
                     """,
                     res);

        code = """
               package hierbas.del.litoral;

               public sealed interface Test extends Runnable permits Subtype1 {
               }
               final class Subtype1 extends Test {}
               """;
        TestUtilities.copyStringToFile(testFileFO, code);

        task = (WorkingCopy workingCopy) -> {
            workingCopy.toPhase(Phase.RESOLVED);
            CompilationUnitTree cut = workingCopy.getCompilationUnit();
            TreeMaker make = workingCopy.getTreeMaker();
            ClassTree clazz = (ClassTree) cut.getTypeDecls().get(0);
            ClassTree newClass = make.removeClassImplementsClause(clazz, 0);
            workingCopy.rewrite(clazz, newClass);
        };
        res = src.runModificationTask(task).getResultingSource(testFileFO);
        //System.err.println(res);
        assertEquals("""
                     package hierbas.del.litoral;

                     public sealed interface Test permits Subtype1 {
                     }
                     final class Subtype1 extends Test {}
                     """,
                     res);

        code = """
               package hierbas.del.litoral;

               public sealed interface Test<T> permits Subtype1 {
               }
               final class Subtype1 extends Test {}
               """;
        TestUtilities.copyStringToFile(testFileFO, code);
        task = (WorkingCopy workingCopy) -> {
            workingCopy.toPhase(Phase.RESOLVED);
            CompilationUnitTree cut = workingCopy.getCompilationUnit();
            TreeMaker make = workingCopy.getTreeMaker();
            ClassTree clazz = (ClassTree) cut.getTypeDecls().get(0);
            ClassTree newClass = make.removeClassTypeParameter(clazz, 0);
            workingCopy.rewrite(clazz, newClass);
        };
        res = src.runModificationTask(task).getResultingSource(testFileFO);
        //System.err.println(res);
        assertEquals("""
                     package hierbas.del.litoral;

                     public sealed interface Test permits Subtype1 {
                     }
                     final class Subtype1 extends Test {}
                     """,
                     res);

        code = """
               package hierbas.del.litoral;

               public sealed interface Test permits Subtype1 {
                   int I;
               }
               final class Subtype1 extends Test {}
               """;
        TestUtilities.copyStringToFile(testFileFO, code);
        task = (WorkingCopy workingCopy) -> {
            workingCopy.toPhase(Phase.RESOLVED);
            CompilationUnitTree cut = workingCopy.getCompilationUnit();
            TreeMaker make = workingCopy.getTreeMaker();
            ClassTree clazz = (ClassTree) cut.getTypeDecls().get(0);
            ClassTree newClass = make.removeClassMember(clazz, 0);
            workingCopy.rewrite(clazz, newClass);
        };
        res = src.runModificationTask(task).getResultingSource(testFileFO);
        //System.err.println(res);
        assertEquals("""
                     package hierbas.del.litoral;

                     public sealed interface Test permits Subtype1 {
                     }
                     final class Subtype1 extends Test {}
                     """,
                     res);
    }

    public void testAddRemovedSealed() throws Exception {
        testFile = new File(getWorkDir(), "Test.java");
        String code = """
                      package hierbas.del.litoral;

                      public class Test {
                      }
                      final class Subtype extends Test {}
                      """;
        TestUtilities.copyStringToFile(testFile, code);

        JavaSource src = getJavaSource(testFile);

        Task<WorkingCopy> task;
        String res;

        //add sealed:
        task = (WorkingCopy workingCopy) -> {
            workingCopy.toPhase(Phase.RESOLVED);
            CompilationUnitTree cut = workingCopy.getCompilationUnit();
            TreeMaker make = workingCopy.getTreeMaker();
            ClassTree clazz = (ClassTree) cut.getTypeDecls().get(0);
            List<Tree> augmentedPermits = new ArrayList<>();
            augmentedPermits.add(make.QualIdent("hierbas.del.litoral.Subtype"));
            ModifiersTree mods = clazz.getModifiers();
            mods = make.addModifiersModifier(mods, Modifier.SEALED);
            ClassTree newClass = make.Class(mods, clazz.getSimpleName(), clazz.getTypeParameters(), clazz.getExtendsClause(), clazz.getImplementsClause(), augmentedPermits, clazz.getMembers());
            workingCopy.rewrite(clazz, newClass);
        };
        src.runModificationTask(task).commit();
        res = TestUtilities.copyFileToString(testFile);
        //System.err.println(res);
        assertEquals("""
                     package hierbas.del.litoral;

                     public sealed class Test permits Subtype {
                     }
                     final class Subtype extends Test {}
                     """, res);

        //remove sealed:
        task = (WorkingCopy workingCopy) -> {
            workingCopy.toPhase(Phase.RESOLVED);
            CompilationUnitTree cut = workingCopy.getCompilationUnit();
            TreeMaker make = workingCopy.getTreeMaker();
            ClassTree clazz = (ClassTree) cut.getTypeDecls().get(0);
            List<Tree> augmentedPermits = new ArrayList<>(clazz.getPermitsClause());
            augmentedPermits.remove(0);
            ModifiersTree mods = clazz.getModifiers();
            mods = make.removeModifiersModifier(mods, Modifier.SEALED);
            ClassTree newClass = make.Class(mods, clazz.getSimpleName(), clazz.getTypeParameters(), clazz.getExtendsClause(), clazz.getImplementsClause(), augmentedPermits, clazz.getMembers());
            workingCopy.rewrite(clazz, newClass);
        };
        src.runModificationTask(task).commit();
        res = TestUtilities.copyFileToString(testFile);
        //System.err.println(res);
        assertEquals(code, res);

        //add non-sealed:
        task = (WorkingCopy workingCopy) -> {
            workingCopy.toPhase(Phase.RESOLVED);
            CompilationUnitTree cut = workingCopy.getCompilationUnit();
            TreeMaker make = workingCopy.getTreeMaker();
            ClassTree clazz = (ClassTree) cut.getTypeDecls().get(0);
            ModifiersTree mods = clazz.getModifiers();
            mods = make.addModifiersModifier(mods, Modifier.NON_SEALED);
            ClassTree newClass = make.Class(mods, clazz.getSimpleName(), clazz.getTypeParameters(), clazz.getExtendsClause(), clazz.getImplementsClause(), clazz.getPermitsClause(), clazz.getMembers());
            workingCopy.rewrite(clazz, newClass);
        };
        src.runModificationTask(task).commit();
        res = TestUtilities.copyFileToString(testFile);
        //System.err.println(res);
        assertEquals("""
                     package hierbas.del.litoral;

                     public non-sealed class Test {
                     }
                     final class Subtype extends Test {}
                     """, res);

        //remove sealed:
        task = (WorkingCopy workingCopy) -> {
            workingCopy.toPhase(Phase.RESOLVED);
            CompilationUnitTree cut = workingCopy.getCompilationUnit();
            TreeMaker make = workingCopy.getTreeMaker();
            ClassTree clazz = (ClassTree) cut.getTypeDecls().get(0);
            ModifiersTree mods = clazz.getModifiers();
            mods = make.removeModifiersModifier(mods, Modifier.NON_SEALED);
            ClassTree newClass = make.Class(mods, clazz.getSimpleName(), clazz.getTypeParameters(), clazz.getExtendsClause(), clazz.getImplementsClause(), clazz.getPermitsClause(), clazz.getMembers());
            workingCopy.rewrite(clazz, newClass);
        };
        src.runModificationTask(task).commit();
        res = TestUtilities.copyFileToString(testFile);
        //System.err.println(res);
        assertEquals(code, res);

        //TODO: syntheticze sealed/non-sealed:
        task = (WorkingCopy workingCopy) -> {
            workingCopy.toPhase(Phase.RESOLVED);
            CompilationUnitTree cut = workingCopy.getCompilationUnit();
            TreeMaker make = workingCopy.getTreeMaker();
            ClassTree clazz = (ClassTree) cut.getTypeDecls().get(0);
            List<Tree> newMembers = new ArrayList<>(clazz.getMembers());
            newMembers.add(make.Interface(make.Modifiers(Set.of(Modifier.SEALED)), "Sealed", List.of(), List.of(), List.of(), List.of()));
            newMembers.add(make.Interface(make.Modifiers(Set.of(Modifier.NON_SEALED)), "NonSealed", List.of(), List.of(), List.of(), List.of()));
            ClassTree newClass = make.Class(clazz.getModifiers(), clazz.getSimpleName(), clazz.getTypeParameters(), clazz.getExtendsClause(), clazz.getImplementsClause(), clazz.getPermitsClause(), newMembers);
            workingCopy.rewrite(clazz, newClass);
        };
        src.runModificationTask(task).commit();
        res = TestUtilities.copyFileToString(testFile);
        //System.err.println(res);
        assertEquals("""
                     package hierbas.del.litoral;

                     public class Test {

                         sealed interface Sealed {
                         }

                         non-sealed interface NonSealed {
                         }
                     }
                     final class Subtype extends Test {}
                     """, res);
    }

    String getGoldenPckg() {
        return "";
    }

    String getSourcePckg() {
        return "";
    }

    @Override
    String getSourceLevel() {
        return "17";
    }
}
