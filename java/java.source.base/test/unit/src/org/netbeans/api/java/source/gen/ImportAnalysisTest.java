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
import com.sun.source.util.TreePath;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.prefs.Preferences;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Types;

import org.netbeans.api.editor.mimelookup.MimeLookup;
import org.netbeans.api.java.lexer.JavaTokenId;
import org.netbeans.api.java.source.Task;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.JavaSource.Phase;
import org.netbeans.api.java.source.SourceUtils;
import org.netbeans.api.java.source.TestUtilities;
import org.netbeans.api.java.source.TreeMaker;
import org.netbeans.api.java.source.WorkingCopy;
import org.netbeans.junit.NbTestSuite;
import org.openide.filesystems.FileStateInvalidException;
import org.openide.filesystems.FileUtil;

/**
 * Testing import analysis, QualIdentTree.
 * 
 * @author Dusan Balek
 * @author Jan Lahoda
 * @author Pavel Flaska
 */
public class ImportAnalysisTest extends GeneratorTestBase {

    /** Creates a new instance of ClashingImportsTest */
    public ImportAnalysisTest(String name) {
        super(name);
    }

    public static NbTestSuite suite() {
        NbTestSuite suite = new NbTestSuite();
//        suite.addTestSuite(ImportAnalysisTest.class);
        suite.addTest(new ImportAnalysisTest("testAddImport1"));
        suite.addTest(new ImportAnalysisTest("testAddImport2"));
        suite.addTest(new ImportAnalysisTest("testAddImport3"));
        suite.addTest(new ImportAnalysisTest("testAddImport4"));
        suite.addTest(new ImportAnalysisTest("testAddImport5"));
        suite.addTest(new ImportAnalysisTest("testAddImport6"));
        suite.addTest(new ImportAnalysisTest("testAddImport7"));
        suite.addTest(new ImportAnalysisTest("testAddImport8"));
        suite.addTest(new ImportAnalysisTest("testAddImport9"));
        suite.addTest(new ImportAnalysisTest("testAddImport10"));
//        suite.addTest(new ImportAnalysisTest("testAddImport11"));
        suite.addTest(new ImportAnalysisTest("testAddImport12"));
        suite.addTest(new ImportAnalysisTest("testAddImport13"));
        suite.addTest(new ImportAnalysisTest("testAddImport14"));
        suite.addTest(new ImportAnalysisTest("testAddImport15"));
//        suite.addTest(new ImportAnalysisTest("testAddImport16"));
        suite.addTest(new ImportAnalysisTest("testAddImport17"));
        suite.addTest(new ImportAnalysisTest("testAddImport18"));
        suite.addTest(new ImportAnalysisTest("testAddImportOrder1"));
        suite.addTest(new ImportAnalysisTest("testAddImportSamePackage"));
        suite.addTest(new ImportAnalysisTest("testAddImportThroughMethod1"));
        suite.addTest(new ImportAnalysisTest("testAddImportThroughMethod2"));
        suite.addTest(new ImportAnalysisTest("testAddImportThroughMethod3"));
        suite.addTest(new ImportAnalysisTest("testAddImportThroughMethod4"));
        suite.addTest(new ImportAnalysisTest("testAddImportThroughMethod5"));
        suite.addTest(new ImportAnalysisTest("testDefaultPackage1"));
        suite.addTest(new ImportAnalysisTest("testDefaultPackage3"));
        suite.addTest(new ImportAnalysisTest("testImportAddedAfterThrows"));
        suite.addTest(new ImportAnalysisTest("testImportGetterSetter"));
        suite.addTest(new ImportAnalysisTest("testImportClashWithJavaLang"));
        suite.addTest(new ImportAnalysisTest("testImportNoClashJavaLang"));
        suite.addTest(new ImportAnalysisTest("testImportNoClashCurrentPackage127486"));
        suite.addTest(new ImportAnalysisTest("test130479"));
        suite.addTest(new ImportAnalysisTest("test157162"));
        suite.addTest(new ImportAnalysisTest("test157566a"));
        suite.addTest(new ImportAnalysisTest("test157566b"));
        suite.addTest(new ImportAnalysisTest("test157566c"));
        suite.addTest(new ImportAnalysisTest("test157566d"));
        suite.addTest(new ImportAnalysisTest("test157566e"));
        return suite;
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        testFile = getFile(getSourceDir(), getSourcePckg() + "ClashingImports.java");
    }

    public void testAddImport1() throws IOException {
        JavaSource src = getJavaSource(testFile);
        Task<WorkingCopy> task = new Task<WorkingCopy>() {

            public void run(WorkingCopy workingCopy) throws IOException {
                workingCopy.toPhase(Phase.RESOLVED);
                CompilationUnitTree cut = workingCopy.getCompilationUnit();
                TreeMaker make = workingCopy.getTreeMaker();
                ClassTree clazz = (ClassTree) cut.getTypeDecls().get(0);
                MethodTree node = (MethodTree) clazz.getMembers().get(0);
                BlockTree body = node.getBody();
                List<StatementTree> stats = new ArrayList<StatementTree>();
                for (StatementTree st : body.getStatements()) {
                    stats.add(st);
                }
                TypeElement e = workingCopy.getElements().getTypeElement("java.util.List");
                ExpressionTree type = make.QualIdent(e);
                stats.add(make.Variable(make.Modifiers(Collections.<Modifier>emptySet()), "utilList", type, null));
                workingCopy.rewrite(body, make.Block(stats, false));
            }
        };
        src.runModificationTask(task).commit();
        assertFiles("testAddImport1.pass");
    }

    public void testAddImport2() throws IOException {
        JavaSource src = getJavaSource(testFile);
        Task<WorkingCopy> task = new Task<WorkingCopy>() {

            public void run(WorkingCopy workingCopy) throws IOException {
                workingCopy.toPhase(Phase.RESOLVED);
                CompilationUnitTree cut = workingCopy.getCompilationUnit();
                TreeMaker make = workingCopy.getTreeMaker();
                ClassTree clazz = (ClassTree) cut.getTypeDecls().get(0);
                MethodTree node = (MethodTree) clazz.getMembers().get(0);
                BlockTree body = node.getBody();
                List<StatementTree> stats = new ArrayList<StatementTree>();
                for (StatementTree st : body.getStatements()) {
                    stats.add(st);
                }
                TypeElement list = workingCopy.getElements().getTypeElement("java.util.List");
                stats.add(make.Variable(make.Modifiers(Collections.<Modifier>emptySet()), "utilList", make.Type(workingCopy.getTypes().erasure(list.asType())), null));
                workingCopy.rewrite(body, make.Block(stats, false));
            }
        };
        src.runModificationTask(task).commit();
        assertFiles("testAddImport2.pass");
    }

    public void testAddImport3() throws IOException {
        JavaSource src = getJavaSource(testFile);
        Task<WorkingCopy> task = new Task<WorkingCopy>() {

            public void run(WorkingCopy workingCopy) throws IOException {
                workingCopy.toPhase(Phase.RESOLVED);
                CompilationUnitTree cut = workingCopy.getCompilationUnit();
                TreeMaker make = workingCopy.getTreeMaker();
                ClassTree clazz = (ClassTree) cut.getTypeDecls().get(0);
                MethodTree node = (MethodTree) clazz.getMembers().get(0);
                BlockTree body = node.getBody();
                List<StatementTree> stats = new ArrayList<StatementTree>();
                for (StatementTree st : body.getStatements()) {
                    stats.add(st);
                }
                TypeElement list = workingCopy.getElements().getTypeElement("java.util.List");
                TypeElement collection = workingCopy.getElements().getTypeElement("java.util.Collection");
                Types types = workingCopy.getTypes();
                TypeMirror tm = types.getDeclaredType(list, types.erasure(collection.asType()));
                stats.add(make.Variable(make.Modifiers(Collections.<Modifier>emptySet()), "utilList", make.Type(tm), null));
                workingCopy.rewrite(body, make.Block(stats, false));
            }
        };
        src.runModificationTask(task).commit();
        assertFiles("testAddImport3.pass");
    }

    public void testAddImport4() throws IOException {
        JavaSource src = getJavaSource(testFile);
        Task<WorkingCopy> task = new Task<WorkingCopy>() {

            public void run(WorkingCopy workingCopy) throws IOException {
                workingCopy.toPhase(Phase.RESOLVED);
                CompilationUnitTree cut = workingCopy.getCompilationUnit();
                TreeMaker make = workingCopy.getTreeMaker();
                ClassTree clazz = (ClassTree) cut.getTypeDecls().get(0);
                MethodTree node = (MethodTree) clazz.getMembers().get(0);
                BlockTree body = node.getBody();
                List<StatementTree> stats = new ArrayList<StatementTree>();
                for (StatementTree st : body.getStatements()) {
                    stats.add(st);
                }
                TypeElement list = workingCopy.getElements().getTypeElement("java.util.List");
                TypeElement collection = workingCopy.getElements().getTypeElement("java.util.Collection");
                Types types = workingCopy.getTypes();
                TypeMirror tm = types.getDeclaredType(list, types.getWildcardType(types.erasure(collection.asType()), null));
                stats.add(make.Variable(make.Modifiers(Collections.<Modifier>emptySet()), "utilList", make.Type(tm), null));
                workingCopy.rewrite(body, make.Block(stats, false));
            }
        };
        src.runModificationTask(task).commit();
        assertFiles("testAddImport4.pass");
    }

    public void testAddImport5() throws IOException {
        JavaSource src = getJavaSource(testFile);
        Task<WorkingCopy> task = new Task<WorkingCopy>() {

            public void run(WorkingCopy workingCopy) throws IOException {
                workingCopy.toPhase(Phase.RESOLVED);
                CompilationUnitTree cut = workingCopy.getCompilationUnit();
                TreeMaker make = workingCopy.getTreeMaker();
                ClassTree clazz = (ClassTree) cut.getTypeDecls().get(0);
                MethodTree node = (MethodTree) clazz.getMembers().get(0);
                BlockTree body = node.getBody();
                List<StatementTree> stats = new ArrayList<StatementTree>();
                for (StatementTree st : body.getStatements()) {
                    stats.add(st);
                }
                TypeElement list = workingCopy.getElements().getTypeElement("java.util.List");
                TypeElement collection = workingCopy.getElements().getTypeElement("java.util.Collection");
                Types types = workingCopy.getTypes();
                TypeMirror tm = types.getDeclaredType(list, types.getWildcardType(null, types.erasure(collection.asType())));
                stats.add(make.Variable(make.Modifiers(Collections.<Modifier>emptySet()), "utilList", make.Type(tm), null));
                workingCopy.rewrite(body, make.Block(stats, false));
            }
        };
        src.runModificationTask(task).commit();
        assertFiles("testAddImport5.pass");
    }

    public void testAddImport6() throws IOException {
        JavaSource src = getJavaSource(testFile);
        Preferences preferences = MimeLookup.getLookup(JavaTokenId.language().mimeType()).lookup(Preferences.class);
        preferences.putBoolean("importInnerClasses", true);
        Task<WorkingCopy> task = new Task<WorkingCopy>() {

            public void run(WorkingCopy workingCopy) throws IOException {
                workingCopy.toPhase(Phase.RESOLVED);
                CompilationUnitTree cut = workingCopy.getCompilationUnit();
                TreeMaker make = workingCopy.getTreeMaker();
                ClassTree clazz = (ClassTree) cut.getTypeDecls().get(0);
                MethodTree node = (MethodTree) clazz.getMembers().get(0);
                BlockTree body = node.getBody();
                List<StatementTree> stats = new ArrayList<StatementTree>();
                for (StatementTree st : body.getStatements()) {
                    stats.add(st);
                }
                TypeElement list = workingCopy.getElements().getTypeElement("java.util.Map.Entry");
                Types types = workingCopy.getTypes();
                stats.add(make.Variable(make.Modifiers(Collections.<Modifier>emptySet()), "entry", make.Type(types.erasure(list.asType())), null));
                workingCopy.rewrite(body, make.Block(stats, false));
            }
        };
        src.runModificationTask(task).commit();
        preferences.remove("importInnerClasses");
        assertFiles("testAddImport6.pass");
    }

    public void testAddImport7() throws IOException {
        JavaSource src = getJavaSource(testFile);
        Preferences preferences = MimeLookup.getLookup(JavaTokenId.language().mimeType()).lookup(Preferences.class);
        preferences.putBoolean("importInnerClasses", true);
        Task<WorkingCopy> task = new Task<WorkingCopy>() {

            public void run(WorkingCopy workingCopy) throws IOException {
                workingCopy.toPhase(Phase.RESOLVED);
                CompilationUnitTree cut = workingCopy.getCompilationUnit();
                TreeMaker make = workingCopy.getTreeMaker();
                ClassTree clazz = (ClassTree) cut.getTypeDecls().get(0);
                MethodTree node = (MethodTree) clazz.getMembers().get(0);
                BlockTree body = node.getBody();
                List<StatementTree> stats = new ArrayList<StatementTree>();
                for (StatementTree st : body.getStatements()) {
                    stats.add(st);
                }
                TypeElement list = workingCopy.getElements().getTypeElement("java.util.Map.Entry");
                stats.add(make.Variable(make.Modifiers(Collections.<Modifier>emptySet()), "entry", make.QualIdent(list), null));
                workingCopy.rewrite(body, make.Block(stats, false));
            }
        };
        src.runModificationTask(task).commit();
        preferences.remove("importInnerClasses");
        assertFiles("testAddImport6.pass"); //the same as testAddImport6, so using only one golden file
    }

    public void testAddImport8() throws IOException {
        JavaSource src = getJavaSource(testFile);
        Preferences preferences = MimeLookup.getLookup(JavaTokenId.language().mimeType()).lookup(Preferences.class);
        preferences.putBoolean("importInnerClasses", true);
        Task<WorkingCopy> task = new Task<WorkingCopy>() {

            public void run(WorkingCopy workingCopy) throws IOException {
                workingCopy.toPhase(Phase.RESOLVED);
                CompilationUnitTree cut = workingCopy.getCompilationUnit();
                TreeMaker make = workingCopy.getTreeMaker();
                ClassTree clazz = (ClassTree) cut.getTypeDecls().get(0);
                MethodTree node = (MethodTree) clazz.getMembers().get(0);
                BlockTree body = node.getBody();
                List<StatementTree> stats = new ArrayList<StatementTree>();
                for (StatementTree st : body.getStatements()) {
                    stats.add(st);
                }
                TypeElement list = workingCopy.getElements().getTypeElement("java.util.Map.Entry");
                Types types = workingCopy.getTypes();
                TypeMirror tm = types.getArrayType(types.erasure(list.asType()));
                stats.add(make.Variable(make.Modifiers(Collections.<Modifier>emptySet()), "entry", make.Type(tm), null));
                workingCopy.rewrite(body, make.Block(stats, false));
            }
        };
        src.runModificationTask(task).commit();
        preferences.remove("importInnerClasses");
        assertFiles("testAddImport8.pass");
    }

    public void testAddImport9() throws IOException {
        JavaSource src = getJavaSource(testFile);
        Task<WorkingCopy> task = new Task<WorkingCopy>() {

            public void run(WorkingCopy workingCopy) throws IOException {
                workingCopy.toPhase(Phase.RESOLVED);
                CompilationUnitTree cut = workingCopy.getCompilationUnit();
                TreeMaker make = workingCopy.getTreeMaker();
                ClassTree clazz = (ClassTree) cut.getTypeDecls().get(0);
                MethodTree node = (MethodTree) clazz.getMembers().get(0);
                BlockTree body = node.getBody();
                List<StatementTree> stats = new ArrayList<StatementTree>();
                for (StatementTree st : body.getStatements()) {
                    stats.add(st);
                }
                TypeElement list = workingCopy.getElements().getTypeElement("java.util.List");
                stats.add(make.Variable(make.Modifiers(Collections.<Modifier>emptySet()), "list1", make.QualIdent(list), null));
                stats.add(make.Variable(make.Modifiers(Collections.<Modifier>emptySet()), "list2", make.QualIdent(list), null));
                workingCopy.rewrite(body, make.Block(stats, false));
            }
        };
        src.runModificationTask(task).commit();
        assertFiles("testAddImport9.pass");
    }

    public void testAddImport10() throws IOException {
        JavaSource src = getJavaSource(testFile);
        Task<WorkingCopy> task = new Task<WorkingCopy>() {

            public void run(WorkingCopy workingCopy) throws IOException {
                workingCopy.toPhase(Phase.RESOLVED);
                CompilationUnitTree cut = workingCopy.getCompilationUnit();
                TreeMaker make = workingCopy.getTreeMaker();
                ClassTree clazz = (ClassTree) cut.getTypeDecls().get(0);
                MethodTree node = (MethodTree) clazz.getMembers().get(0);
                BlockTree body = node.getBody();
                List<StatementTree> stats = new ArrayList<StatementTree>();
                for (StatementTree st : body.getStatements()) {
                    stats.add(st);
                }
                TypeElement list = workingCopy.getElements().getTypeElement("java.util.List");
                TypeElement awtList = workingCopy.getElements().getTypeElement("java.awt.List");
                stats.add(make.Variable(make.Modifiers(Collections.<Modifier>emptySet()), "list1", make.QualIdent(list), null));
                stats.add(make.Variable(make.Modifiers(Collections.<Modifier>emptySet()), "list2", make.QualIdent(awtList), null));
                workingCopy.rewrite(body, make.Block(stats, false));
            }
        };
        src.runModificationTask(task).commit();
        assertFiles("testAddImport10.pass");
    }

    public void testAddImport11() throws IOException {
        JavaSource src = getJavaSource(testFile);
        Task<WorkingCopy> task = new Task<WorkingCopy>() {

            public void run(WorkingCopy workingCopy) throws IOException {
                workingCopy.toPhase(Phase.RESOLVED);
                CompilationUnitTree cut = workingCopy.getCompilationUnit();
                TreeMaker make = workingCopy.getTreeMaker();
                ClassTree clazz = (ClassTree) cut.getTypeDecls().get(0);
                MethodTree node = (MethodTree) clazz.getMembers().get(0);
                BlockTree body = node.getBody();
                List<StatementTree> stats = new ArrayList<StatementTree>();
                for (StatementTree st : body.getStatements()) {
                    stats.add(st);
                }
                TypeElement list = workingCopy.getElements().getTypeElement("java.util.Map.Entry");
                Types types = workingCopy.getTypes();
                TypeMirror tm = types.getArrayType(types.erasure(list.asType()));
                stats.add(make.Variable(make.Modifiers(Collections.<Modifier>emptySet()), "entry", make.Type(tm), null));
                workingCopy.rewrite(body, make.Block(stats, false));
            }
        };
        src.runModificationTask(task).commit();
        assertFiles("testAddImport11.pass");
    }

    public void testAddImport12() throws IOException {
        JavaSource src = getJavaSource(testFile);
        Task<WorkingCopy> task = new Task<WorkingCopy>() {

            public void run(WorkingCopy workingCopy) throws IOException {
                workingCopy.toPhase(Phase.RESOLVED);
                CompilationUnitTree cut = workingCopy.getCompilationUnit();
                TreeMaker make = workingCopy.getTreeMaker();
                ClassTree clazz = (ClassTree) cut.getTypeDecls().get(0);
                TypeElement map = workingCopy.getElements().getTypeElement("java.util.Map");
                ClassTree nue = make.addClassImplementsClause(clazz, make.QualIdent(map));
                workingCopy.rewrite(clazz, nue);
                MethodTree node = (MethodTree) clazz.getMembers().get(0);
                BlockTree body = node.getBody();
                List<StatementTree> stats = new ArrayList<StatementTree>();
                for (StatementTree st : body.getStatements()) {
                    stats.add(st);
                }
                TypeElement list = workingCopy.getElements().getTypeElement("java.util.Map.Entry");
                Types types = workingCopy.getTypes();
                TypeMirror tm = types.getArrayType(types.erasure(list.asType()));
                stats.add(make.Variable(make.Modifiers(Collections.<Modifier>emptySet()), "entry", make.Type(tm), null));
                workingCopy.rewrite(body, make.Block(stats, false));
            }
        };
        src.runModificationTask(task).commit();
        assertFiles("testAddImport12.pass");
    }

    public void testAddImport13() throws IOException {
        testFile = getFile(getSourceDir(), getSourcePckg() + "ImportsTest2.java");
        JavaSource src = getJavaSource(testFile);
        Task<WorkingCopy> task = new Task<WorkingCopy>() {

            public void run(WorkingCopy workingCopy) throws IOException {
                workingCopy.toPhase(Phase.RESOLVED);
                CompilationUnitTree cut = workingCopy.getCompilationUnit();
                TreeMaker make = workingCopy.getTreeMaker();
                ClassTree clazz = (ClassTree) cut.getTypeDecls().get(0);
                MethodTree node = (MethodTree) clazz.getMembers().get(0);
                BlockTree body = node.getBody();
                List<StatementTree> stats = new ArrayList<StatementTree>();
                for (StatementTree st : body.getStatements()) {
                    stats.add(st);
                }
                TypeElement list = workingCopy.getElements().getTypeElement("java.util.Map.Entry");
                Types types = workingCopy.getTypes();
                TypeMirror tm = types.getArrayType(types.erasure(list.asType()));
                stats.add(make.Variable(make.Modifiers(Collections.<Modifier>emptySet()), "entry", make.Type(tm), null));
                workingCopy.rewrite(body, make.Block(stats, false));
            }
        };
        src.runModificationTask(task).commit();
        assertFiles("testAddImport13.pass");
    }

    public void testAddImport14() throws IOException {
        testFile = getFile(getSourceDir(), getSourcePckg() + "ImportsTest3.java");
        JavaSource src = getJavaSource(testFile);
        Task<WorkingCopy> task = new Task<WorkingCopy>() {

            public void run(WorkingCopy workingCopy) throws IOException {
                workingCopy.toPhase(Phase.RESOLVED);
                CompilationUnitTree cut = workingCopy.getCompilationUnit();
                TreeMaker make = workingCopy.getTreeMaker();
                ClassTree clazz = (ClassTree) cut.getTypeDecls().get(0);
                MethodTree node = (MethodTree) clazz.getMembers().get(0);
                BlockTree body = node.getBody();
                List<StatementTree> stats = new ArrayList<StatementTree>();
                for (StatementTree st : body.getStatements()) {
                    stats.add(st);
                }
                TypeElement list = workingCopy.getElements().getTypeElement("java.util.Map.Entry");
                Types types = workingCopy.getTypes();
                TypeMirror tm = types.getArrayType(types.erasure(list.asType()));
                stats.add(make.Variable(make.Modifiers(Collections.<Modifier>emptySet()), "entry", make.Type(tm), null));
                workingCopy.rewrite(body, make.Block(stats, false));
            }
        };
        src.runModificationTask(task).commit();
        assertFiles("testAddImport14.pass");
    }

    public void testAddImport15() throws IOException {
        testFile = getFile(getSourceDir(), getSourcePckg() + "ImportsTest4.java");
        JavaSource src = getJavaSource(testFile);
        Task<WorkingCopy> task = new Task<WorkingCopy>() {

            public void run(WorkingCopy workingCopy) throws IOException {
                workingCopy.toPhase(Phase.RESOLVED);
                CompilationUnitTree cut = workingCopy.getCompilationUnit();
                TreeMaker make = workingCopy.getTreeMaker();
                ClassTree clazz = (ClassTree) cut.getTypeDecls().get(0);
                MethodTree node = (MethodTree) clazz.getMembers().get(0);
                BlockTree body = node.getBody();
                List<StatementTree> stats = new ArrayList<StatementTree>();
                for (StatementTree st : body.getStatements()) {
                    stats.add(st);
                }
                TypeElement list = workingCopy.getElements().getTypeElement("java.lang.Math");
                ExecutableElement maxMethod = null;
                VariableElement pi = null;
                for (Element ee : list.getEnclosedElements()) {
                    if ("max".equals(ee.getSimpleName().toString())) {
                        maxMethod = (ExecutableElement) ee;
                    }
                    if ("PI".equals(ee.getSimpleName().toString())) {
                        pi = (VariableElement) ee;
                    }
                }

                assertNotNull(maxMethod);
                assertNotNull(pi);

                stats.add(make.ExpressionStatement(make.MethodInvocation(Collections.<ExpressionTree>emptyList(), make.QualIdent(maxMethod), Arrays.asList(make.QualIdent(pi), make.Literal(2)))));
                workingCopy.rewrite(body, make.Block(stats, false));
            }
        };
        src.runModificationTask(task).commit();
        assertFiles("testAddImport15.pass");
    }

    public void testAddImport16() throws IOException {
        JavaSource src = getJavaSource(testFile);
        Task<WorkingCopy> task = new Task<WorkingCopy>() {

            public void run(WorkingCopy workingCopy) throws IOException {
                workingCopy.toPhase(Phase.RESOLVED);
                CompilationUnitTree cut = workingCopy.getCompilationUnit();
                TreeMaker make = workingCopy.getTreeMaker();
                ClassTree clazz = (ClassTree) cut.getTypeDecls().get(0);
                MethodTree node = (MethodTree) clazz.getMembers().get(0);
                BlockTree body = node.getBody();
                List<StatementTree> stats = new ArrayList<StatementTree>();
                for (StatementTree st : body.getStatements()) {
                    stats.add(st);
                }
                TypeElement list = workingCopy.getElements().getTypeElement("java.lang.Math");
                ExecutableElement maxMethod = null;
                ExecutableElement minMethod = null;
                VariableElement pi = null;
                for (Element ee : list.getEnclosedElements()) {
                    if ("max".equals(ee.getSimpleName().toString())) {
                        maxMethod = (ExecutableElement) ee;
                    }
                    if ("min".equals(ee.getSimpleName().toString())) {
                        minMethod = (ExecutableElement) ee;
                    }
                    if ("PI".equals(ee.getSimpleName().toString())) {
                        pi = (VariableElement) ee;
                    }
                }

                assertNotNull(maxMethod);
                assertNotNull(minMethod);
                assertNotNull(pi);

                stats.add(make.ExpressionStatement(make.MethodInvocation(Collections.<ExpressionTree>emptyList(), make.QualIdent(maxMethod), Arrays.asList(make.QualIdent(pi), make.Literal(2)))));
                stats.add(make.ExpressionStatement(make.MethodInvocation(Collections.<ExpressionTree>emptyList(), make.QualIdent(minMethod), Arrays.asList(make.QualIdent(pi), make.Literal(2)))));

                workingCopy.rewrite(body, make.Block(stats, false));
            }
        };
        src.runModificationTask(task).commit();
        assertFiles("testAddImport16.pass");
    }

    public void testAddImport17() throws IOException {
        testFile = getFile(getSourceDir(), getSourcePckg() + "ImportsTest6.java");
        JavaSource src = getJavaSource(testFile);
        Task<WorkingCopy> task = new Task<WorkingCopy>() {

            public void run(WorkingCopy workingCopy) throws IOException {
                workingCopy.toPhase(Phase.RESOLVED);
                CompilationUnitTree cut = workingCopy.getCompilationUnit();
                TreeMaker make = workingCopy.getTreeMaker();
                ClassTree clazz = (ClassTree) cut.getTypeDecls().get(0);
                MethodTree node = (MethodTree) clazz.getMembers().get(0);
                BlockTree body = node.getBody();
                List<StatementTree> stats = new ArrayList<StatementTree>();
                for (StatementTree st : body.getStatements()) {
                    stats.add(st);
                }
                TypeElement list = workingCopy.getElements().getTypeElement("java.lang.String");
                Types types = workingCopy.getTypes();
                TypeMirror tm = types.getArrayType(types.erasure(list.asType()));
                stats.add(make.Variable(make.Modifiers(Collections.<Modifier>emptySet()), "s", make.Type(tm), null));
                workingCopy.rewrite(body, make.Block(stats, false));
            }
        };
        src.runModificationTask(task).commit();
        assertFiles("testAddImport17.pass");
    }

    public void testAddImport18() throws IOException {
        testFile = getFile(getSourceDir(), getSourcePckg() + "ImportsTest6.java");
        JavaSource src = getJavaSource(testFile);
        Task<WorkingCopy> task = new Task<WorkingCopy>() {

            public void run(WorkingCopy workingCopy) throws IOException {
                workingCopy.toPhase(Phase.RESOLVED);
                CompilationUnitTree node = workingCopy.getCompilationUnit();
                TreeMaker make = workingCopy.getTreeMaker();
                ExpressionTree pack = node.getPackageName();
                PackageElement pe = workingCopy.getElements().getPackageElement("org.netbeans.test");
                ExpressionTree nuePack = make.QualIdent(pe);

                workingCopy.rewrite(pack, nuePack);
            }
        };
        src.runModificationTask(task).commit();
        assertFiles("testAddImport18.pass");
    }

    public void testAddImportOrder1() throws IOException {
        testFile = getFile(getSourceDir(), getSourcePckg() + "ImportsTest7.java");
        JavaSource src = getJavaSource(testFile);
        Task<WorkingCopy> task = new Task<WorkingCopy>() {

            public void run(WorkingCopy workingCopy) throws IOException {
                workingCopy.toPhase(Phase.RESOLVED);
                CompilationUnitTree cut = workingCopy.getCompilationUnit();
                TreeMaker make = workingCopy.getTreeMaker();
                ClassTree clazz = (ClassTree) cut.getTypeDecls().get(0);
                MethodTree node = (MethodTree) clazz.getMembers().get(0);
                BlockTree body = node.getBody();
                List<StatementTree> stats = new ArrayList<StatementTree>();
                for (StatementTree st : body.getStatements()) {
                    stats.add(st);
                }
                TypeElement list = workingCopy.getElements().getTypeElement("java.util.LinkedList");
                Types types = workingCopy.getTypes();
                TypeMirror tm = types.getArrayType(types.erasure(list.asType()));
                stats.add(make.Variable(make.Modifiers(Collections.<Modifier>emptySet()), "s", make.Type(tm), null));
                workingCopy.rewrite(body, make.Block(stats, false));
            }
        };
        src.runModificationTask(task).commit();
        assertFiles("testAddImportOrder1.pass");
    }

    public void testAddImportSamePackage() throws IOException {
        testFile = getFile(getSourceDir(), getSourcePckg() + "ImportsTest7.java");
        JavaSource src = getJavaSource(testFile);
        Task<WorkingCopy> task = new Task<WorkingCopy>() {

            public void run(WorkingCopy workingCopy) throws IOException {
                workingCopy.toPhase(Phase.RESOLVED);
                CompilationUnitTree cut = workingCopy.getCompilationUnit();
                TreeMaker make = workingCopy.getTreeMaker();
                ClassTree clazz = (ClassTree) cut.getTypeDecls().get(0);
                MethodTree node = (MethodTree) clazz.getMembers().get(0);
                BlockTree body = node.getBody();
                List<StatementTree> stats = new ArrayList<StatementTree>();
                for (StatementTree st : body.getStatements()) {
                    stats.add(st);
                }
                TypeElement list = workingCopy.getElements().getTypeElement("org.netbeans.test.codegen.ImportsTest6");
                assertNotNull(list);
                Types types = workingCopy.getTypes();
                TypeMirror tm = types.getArrayType(types.erasure(list.asType()));
                stats.add(make.Variable(make.Modifiers(Collections.<Modifier>emptySet()), "s", make.Type(tm), null));
                workingCopy.rewrite(body, make.Block(stats, false));
            }
        };
        src.runModificationTask(task).commit();
        assertFiles("testAddImportSamePackage.pass");
    }

    //XXX: more tests default package
    public void testDefaultPackage1() throws IOException {
        testFile = getFile(getSourceDir(), "ImportAnalysisDefaultPackage1.java");
        JavaSource src = getJavaSource(testFile);
        Task<WorkingCopy> task = new Task<WorkingCopy>() {

            public void run(WorkingCopy workingCopy) throws IOException {
                workingCopy.toPhase(Phase.RESOLVED);
                CompilationUnitTree cut = workingCopy.getCompilationUnit();
                TreeMaker make = workingCopy.getTreeMaker();
                ClassTree clazz = (ClassTree) cut.getTypeDecls().get(0);
                MethodTree node = (MethodTree) clazz.getMembers().get(0);
                BlockTree body = node.getBody();
                List<StatementTree> stats = new ArrayList<StatementTree>();
                for (StatementTree st : body.getStatements()) {
                    stats.add(st);
                }
                TypeElement exc = workingCopy.getElements().getTypeElement("ImportAnalysisDefaultPackage2");
                stats.add(make.Variable(make.Modifiers(Collections.<Modifier>emptySet()), "s", make.QualIdent(exc), null));
                workingCopy.rewrite(body, make.Block(stats, false));
            }
        };
        src.runModificationTask(task).commit();
        assertFiles("testDefaultPackage1.pass");
    }

    public void testDefaultPackage3() throws IOException {
        testFile = getFile(getSourceDir(), "ImportAnalysisDefaultPackage3.java");
        JavaSource src = getJavaSource(testFile);
        Task<WorkingCopy> task = new Task<WorkingCopy>() {

            public void run(WorkingCopy workingCopy) throws IOException {
                workingCopy.toPhase(Phase.RESOLVED);
                CompilationUnitTree cut = workingCopy.getCompilationUnit();
                TreeMaker make = workingCopy.getTreeMaker();
                ClassTree clazz = (ClassTree) cut.getTypeDecls().get(0);
                MethodTree node = (MethodTree) clazz.getMembers().get(0);
                BlockTree body = node.getBody();
                List<StatementTree> stats = new ArrayList<StatementTree>();
                for (StatementTree st : body.getStatements()) {
                    stats.add(st);
                }
                TypeElement exc = workingCopy.getElements().getTypeElement("ImportAnalysisDefaultPackage3A.B");
                stats.add(make.Variable(make.Modifiers(Collections.<Modifier>emptySet()), "s", make.QualIdent(exc), null));
                workingCopy.rewrite(body, make.Block(stats, false));
            }
        };
        src.runModificationTask(task).commit();
        assertFiles("testDefaultPackage3.pass");
    }

    public void testImportAddedAfterThrows() throws IOException {
        testFile = getFile(getSourceDir(), getSourcePckg() + "ImportsTest7.java");
        JavaSource src = getJavaSource(testFile);
        Task<WorkingCopy> task = new Task<WorkingCopy>() {

            public void run(WorkingCopy workingCopy) throws IOException {
                workingCopy.toPhase(Phase.RESOLVED);
                CompilationUnitTree cut = workingCopy.getCompilationUnit();
                TreeMaker make = workingCopy.getTreeMaker();
                ClassTree clazz = (ClassTree) cut.getTypeDecls().get(0);
                MethodTree node = (MethodTree) clazz.getMembers().get(0);
                TypeElement exc = workingCopy.getElements().getTypeElement("javax.swing.text.BadLocationException");
                MethodTree nueMethod = make.addMethodThrows(node, (ExpressionTree) make.Type(exc.asType()));
                workingCopy.rewrite(node, nueMethod);
            }
        };
        src.runModificationTask(task).commit();
        assertFiles("testImportAddedAfterThrows.pass");
    }

    public void testAddImportThroughMethod1() throws IOException {
        JavaSource testSource = JavaSource.forFileObject(FileUtil.toFileObject(testFile));
        Task<WorkingCopy> task = new Task<WorkingCopy>() {

            public void run(WorkingCopy workingCopy) throws java.io.IOException {
                workingCopy.toPhase(Phase.RESOLVED);
                TreeMaker make = workingCopy.getTreeMaker();
                ClassTree clazz = (ClassTree) workingCopy.getCompilationUnit().getTypeDecls().get(0);
                MethodTree node = (MethodTree) clazz.getMembers().get(0);
                int offset = (int) (workingCopy.getTrees().getSourcePositions().getStartPosition(workingCopy.getCompilationUnit(), node) + 1);
                TreePath context = workingCopy.getTreeUtilities().pathFor(offset);
                try {
                    assertEquals("List", SourceUtils.resolveImport(workingCopy, context, "java.util.List"));
                    assertEquals("java.awt.List", SourceUtils.resolveImport(workingCopy, context, "java.awt.List"));
                } catch (IOException e) {
                    throw new IllegalStateException(e);
                }
            }
        };
        testSource.runModificationTask(task).commit();
        String res = TestUtilities.copyFileToString(testFile);
        //System.err.println(res);
        assertFiles("testAddImportThroughMethod1.pass");
    }

    public void testAddImportThroughMethod2() throws IOException {
        JavaSource testSource = JavaSource.forFileObject(FileUtil.toFileObject(testFile));
        Task<WorkingCopy> task = new Task<WorkingCopy>() {

            public void run(WorkingCopy workingCopy) throws java.io.IOException {
                workingCopy.toPhase(Phase.RESOLVED);
                TreeMaker make = workingCopy.getTreeMaker();
                ClassTree clazz = (ClassTree) workingCopy.getCompilationUnit().getTypeDecls().get(0);
                MethodTree node = (MethodTree) clazz.getMembers().get(0);
                int offset = (int) (workingCopy.getTrees().getSourcePositions().getStartPosition(workingCopy.getCompilationUnit(), node) + 1);
                TreePath context = workingCopy.getTreeUtilities().pathFor(offset);
                try {
                    assertEquals("List", SourceUtils.resolveImport(workingCopy, context, "java.util.List"));
                    assertEquals("java.awt.List", SourceUtils.resolveImport(workingCopy, context, "java.awt.List"));
                } catch (IOException e) {
                    throw new IllegalStateException(e);
                }
            }
        };
        testSource.runModificationTask(task).commit();
        String res = TestUtilities.copyFileToString(testFile);
        //System.err.println(res);
        assertFiles("testAddImportThroughMethod2.pass");
    }

    public void testAddImportThroughMethod3() throws IOException {
        JavaSource testSource = JavaSource.forFileObject(FileUtil.toFileObject(testFile));
        Task<WorkingCopy> task = new Task<WorkingCopy>() {

            public void run(WorkingCopy workingCopy) throws java.io.IOException {
                workingCopy.toPhase(Phase.RESOLVED);
                TreeMaker make = workingCopy.getTreeMaker();
                ClassTree clazz = (ClassTree) workingCopy.getCompilationUnit().getTypeDecls().get(0);
                MethodTree node = (MethodTree) clazz.getMembers().get(0);
                int offset = (int) (workingCopy.getTrees().getSourcePositions().getStartPosition(workingCopy.getCompilationUnit(), node) + 1);
                TreePath context = workingCopy.getTreeUtilities().pathFor(offset);
                try {
                    assertEquals("List", SourceUtils.resolveImport(workingCopy, context, "java.util.List"));
                    assertEquals("Map", SourceUtils.resolveImport(workingCopy, context, "java.util.Map"));
                    assertEquals("java.awt.List", SourceUtils.resolveImport(workingCopy, context, "java.awt.List"));
                } catch (IOException e) {
                    throw new IllegalStateException(e);
                }
            }
        };
        testSource.runModificationTask(task).commit();
        String res = TestUtilities.copyFileToString(testFile);
        //System.err.println(res);
        assertFiles("testAddImportThroughMethod3.pass");
    }

    public void testAddImportThroughMethod4() throws IOException {
        JavaSource testSource = JavaSource.forFileObject(FileUtil.toFileObject(testFile));
        Task<WorkingCopy> task = new Task<WorkingCopy>() {

            public void run(WorkingCopy workingCopy) throws java.io.IOException {
                workingCopy.toPhase(Phase.RESOLVED);
                TreeMaker make = workingCopy.getTreeMaker();
                ClassTree clazz = (ClassTree) workingCopy.getCompilationUnit().getTypeDecls().get(0);
                MethodTree node = (MethodTree) clazz.getMembers().get(0);
                int offset = (int) (workingCopy.getTrees().getSourcePositions().getStartPosition(workingCopy.getCompilationUnit(), node) + 1);
                TreePath context = workingCopy.getTreeUtilities().pathFor(offset);
                try {
                    assertEquals("SuperClassTest", SourceUtils.resolveImport(workingCopy, context, "org.netbeans.test.codegen.SuperClassTest"));
                } catch (IOException e) {
                    throw new IllegalStateException(e);
                }
            }
        };
        testSource.runModificationTask(task).commit();
        String res = TestUtilities.copyFileToString(testFile);
        //System.err.println(res);
        assertFiles("testAddImportThroughMethod4.pass");
    }

    public void testAddImportThroughMethod5() throws IOException {
        JavaSource testSource = JavaSource.forFileObject(FileUtil.toFileObject(testFile));
        Task<WorkingCopy> task = new Task<WorkingCopy>() {

            public void run(WorkingCopy workingCopy) throws java.io.IOException {
                workingCopy.toPhase(Phase.RESOLVED);
                TreeMaker make = workingCopy.getTreeMaker();
                ClassTree clazz = (ClassTree) workingCopy.getCompilationUnit().getTypeDecls().get(0);
                MethodTree node = (MethodTree) clazz.getMembers().get(0);
                int offset = (int) (workingCopy.getTrees().getSourcePositions().getStartPosition(workingCopy.getCompilationUnit(), node) + 1);
                TreePath context = workingCopy.getTreeUtilities().pathFor(offset);
                try {
                    assertEquals("SuperClassTest.FirstInnerClass", SourceUtils.resolveImport(workingCopy, context, "org.netbeans.test.codegen.SuperClassTest.FirstInnerClass"));
                } catch (IOException e) {
                    throw new IllegalStateException(e);
                }
            }
        };
        testSource.runModificationTask(task).commit();
        String res = TestUtilities.copyFileToString(testFile);
        //System.err.println(res);
        assertFiles("testAddImportThroughMethod4.pass");
    }

    public void testImportGetterSetter() throws IOException {
        testFile = getFile(getSourceDir(), getSourcePckg() + "ImportsTest8.java");
        JavaSource src = getJavaSource(testFile);
        Task<WorkingCopy> task = new Task<WorkingCopy>() {

            public void run(WorkingCopy workingCopy) throws IOException {
                workingCopy.toPhase(Phase.RESOLVED);
                CompilationUnitTree cut = workingCopy.getCompilationUnit();
                TreeMaker make = workingCopy.getTreeMaker();
                ClassTree clazz = (ClassTree) cut.getTypeDecls().get(0);
                ExpressionTree type = make.QualIdent(workingCopy.getElements().getTypeElement("java.awt.geom.Point2D.Double"));
                VariableTree vt = make.Variable(make.Modifiers(EnumSet.noneOf(Modifier.class)), "test",type, null);
                workingCopy.rewrite(clazz, make.addClassMember(clazz, vt));
            }
        };
        src.runModificationTask(task).commit();
        assertFiles("testImportGetterSetter.pass");
    }
    
    public void testImportClashWithJavaLang() throws IOException {
        testFile = getFile(getSourceDir(), getSourcePckg() + "ImportsTest9.java");
        JavaSource src = getJavaSource(testFile);
        Task<WorkingCopy> task = new Task<WorkingCopy>() {

            public void run(WorkingCopy workingCopy) throws IOException {
                workingCopy.toPhase(Phase.RESOLVED);
                CompilationUnitTree cut = workingCopy.getCompilationUnit();
                TreeMaker make = workingCopy.getTreeMaker();
                ClassTree clazz = (ClassTree) cut.getTypeDecls().get(0);
                ExpressionTree type = make.QualIdent(workingCopy.getElements().getTypeElement("java.awt.geom.Point2D.Double"));
                VariableTree vt = make.Variable(make.Modifiers(EnumSet.noneOf(Modifier.class)), "test",type, null);
                workingCopy.rewrite(clazz, make.addClassMember(clazz, vt));
            }
        };
        src.runModificationTask(task).commit();
        assertFiles("testImportClashWithJavaLang.pass");
    }
    
    public void testImportNoClashJavaLang() throws IOException {
        testFile = getFile(getSourceDir(), getSourcePckg() + "ImportsTesta.java");
        JavaSource src = getJavaSource(testFile);
        Task<WorkingCopy> task = new Task<WorkingCopy>() {

            public void run(WorkingCopy workingCopy) throws IOException {
                workingCopy.toPhase(Phase.RESOLVED);
                CompilationUnitTree cut = workingCopy.getCompilationUnit();
                TreeMaker make = workingCopy.getTreeMaker();
                ClassTree clazz = (ClassTree) cut.getTypeDecls().get(0);
                ExpressionTree type = make.QualIdent(workingCopy.getElements().getTypeElement("java.lang.Double"));
                VariableTree vt = make.Variable(make.Modifiers(EnumSet.noneOf(Modifier.class)), "test",type, null);
                workingCopy.rewrite(clazz, make.addClassMember(clazz, vt));
            }
        };
        src.runModificationTask(task).commit();
        assertFiles("testImportNoClashJavaLang.pass");
    }
    
    public void testImportNoClashCurrentPackage127486() throws IOException {
        testFile = getFile(getSourceDir(), getSourcePckg() + "ImportsTest127486.java");
        JavaSource src = getJavaSource(testFile);
        Task<WorkingCopy> task = new Task<WorkingCopy>() {

            public void run(WorkingCopy workingCopy) throws IOException {
                workingCopy.toPhase(Phase.RESOLVED);
                CompilationUnitTree cut = workingCopy.getCompilationUnit();
                TreeMaker make = workingCopy.getTreeMaker();
                ClassTree clazz = (ClassTree) cut.getTypeDecls().get(0);
                ExpressionTree type = make.QualIdent(workingCopy.getElements().getTypeElement("org.netbeans.test.codegen.InputStream"));
                VariableTree vt = make.Variable(make.Modifiers(EnumSet.noneOf(Modifier.class)), "test",type, null);
                workingCopy.rewrite(clazz, make.addClassMember(clazz, vt));
            }
        };
        src.runModificationTask(task).commit();
        assertFiles("testImportNoClashCurrentPackage127486.pass");
    }
    
    public void test130479() throws IOException {
        testFile = getFile(getSourceDir(), getSourcePckg() + "ImportsTest130479.java");
        JavaSource src = getJavaSource(testFile);
        Task<WorkingCopy> task = new Task<WorkingCopy>() {

            public void run(WorkingCopy workingCopy) throws IOException {
                workingCopy.toPhase(Phase.RESOLVED);
                CompilationUnitTree cut = workingCopy.getCompilationUnit();
                TreeMaker make = workingCopy.getTreeMaker();
                ClassTree clazz = (ClassTree) cut.getTypeDecls().get(0);
                MethodTree method = (MethodTree) clazz.getMembers().get(2);
                BlockTree block = method.getBody();
                final TypeElement foo = workingCopy.getElements().getTypeElement("org.netbeans.test.codegen.ImportsTest130479.Foo");
                final TypeElement clazzType = workingCopy.getElements().getTypeElement("java.lang.Class");
                TypeMirror tm = workingCopy.getTypes().getDeclaredType(clazzType, workingCopy.getTypes().erasure(foo.asType()));
                assertNotNull(foo);
                Tree type = make.Type(tm);
                VariableTree vt = make.Variable(make.Modifiers(EnumSet.noneOf(Modifier.class)), "test", type, null);
                workingCopy.rewrite(block, make.addBlockStatement(block, vt));
            }
        };
        src.runModificationTask(task).commit();
        assertFiles("testImports130479.pass");
    }
    
    public void testAddImportThroughMethod130479() throws IOException {
        testFile = getFile(getSourceDir(), getSourcePckg() + "ImportsTest130479.java");
        JavaSource testSource = JavaSource.forFileObject(FileUtil.toFileObject(testFile));
        Task<WorkingCopy> task = new Task<WorkingCopy>() {
            public void run(WorkingCopy workingCopy) throws java.io.IOException {
                workingCopy.toPhase(Phase.RESOLVED);
                CompilationUnitTree cut = workingCopy.getCompilationUnit();
                ClassTree clazz = (ClassTree) cut.getTypeDecls().get(0);
                MethodTree method = (MethodTree) clazz.getMembers().get(2);
                BlockTree block = method.getBody();
                int offset = (int) (workingCopy.getTrees().getSourcePositions().getStartPosition(workingCopy.getCompilationUnit(), block) + 1);
                TreePath context = workingCopy.getTreeUtilities().pathFor(offset);
                try {
                    assertEquals("Foo", SourceUtils.resolveImport(workingCopy, context, "org.netbeans.test.codegen.ImportsTest130479.Foo"));
                } catch (IOException e) {
                    throw new IllegalStateException(e);
                }
            }
        };
        testSource.runModificationTask(task).commit();
        String res = TestUtilities.copyFileToString(testFile);
        //System.err.println(res);
        assertFiles("testAddImportThroughMethod1.pass");
    }
    
    public void test157162() throws IOException {
        testFile = getFile(getSourceDir(), "org/netbeans/test/codegen/imports157162/test/Test.java");
        JavaSource src = getJavaSource(testFile);
        Task<WorkingCopy> task = new Task<WorkingCopy>() {
            public void run(WorkingCopy workingCopy) throws IOException {
                workingCopy.toPhase(Phase.RESOLVED);
                CompilationUnitTree cut = workingCopy.getCompilationUnit();
                TreeMaker make = workingCopy.getTreeMaker();
                ClassTree clazz = (ClassTree) cut.getTypeDecls().get(0);
                final TypeElement foo = workingCopy.getElements().getTypeElement("org.netbeans.test.codegen.imports157162.foo.Foo.Protected");
                assertNotNull(foo);
                Tree type = make.QualIdent(foo);
                VariableTree vt = make.Variable(make.Modifiers(EnumSet.noneOf(Modifier.class)), "test", type, null);
                workingCopy.rewrite(clazz, make.addClassMember(clazz, vt));
            }
        };
        src.runModificationTask(task).commit();
        assertFiles("testImports157162.pass");
    }
    
    public void test157566a() throws IOException {
        testFile = getFile(getSourceDir(), "org/netbeans/test/codegen/imports157566/b/Test.java");
        JavaSource src = getJavaSource(testFile);
        Task<WorkingCopy> task = new Task<WorkingCopy>() {
            public void run(WorkingCopy workingCopy) throws IOException {
                workingCopy.toPhase(Phase.RESOLVED);
                CompilationUnitTree cut = workingCopy.getCompilationUnit();
                TreeMaker make = workingCopy.getTreeMaker();
                ClassTree clazz = (ClassTree) cut.getTypeDecls().get(0);
                final TypeElement foo = workingCopy.getElements().getTypeElement("org.netbeans.test.codegen.imports157566.a.C");
                assertNotNull(foo);
                Tree type = make.QualIdent(foo);
                VariableTree vt = make.Variable(make.Modifiers(EnumSet.noneOf(Modifier.class)), "test", type, null);
                workingCopy.rewrite(clazz, make.addClassMember(clazz, vt));
            }
        };
        src.runModificationTask(task).commit();
        assertFiles("testImports157566a.pass");
    }

    public void test157566b() throws IOException {
        testFile = getFile(getSourceDir(), "org/netbeans/test/codegen/imports157566/b/Test.java");
        JavaSource src = getJavaSource(testFile);
        Task<WorkingCopy> task = new Task<WorkingCopy>() {
            public void run(WorkingCopy workingCopy) throws IOException {
                workingCopy.toPhase(Phase.RESOLVED);
                CompilationUnitTree cut = workingCopy.getCompilationUnit();
                TreeMaker make = workingCopy.getTreeMaker();
                ClassTree clazz = (ClassTree) cut.getTypeDecls().get(0);
                final TypeElement foo = workingCopy.getElements().getTypeElement("org.netbeans.test.codegen.imports157566.b.String");
                assertNotNull(foo);
                Tree type = make.QualIdent(foo);
                VariableTree vt = make.Variable(make.Modifiers(EnumSet.noneOf(Modifier.class)), "test", type, null);
                workingCopy.rewrite(clazz, make.addClassMember(clazz, vt));
            }
        };
        src.runModificationTask(task).commit();
        assertFiles("testImports157566b.pass");
    }

    public void test157566c() throws IOException {
        testFile = getFile(getSourceDir(), "org/netbeans/test/codegen/imports157566/b/Test.java");
        JavaSource src = getJavaSource(testFile);
        Task<WorkingCopy> task = new Task<WorkingCopy>() {
            public void run(WorkingCopy workingCopy) throws IOException {
                workingCopy.toPhase(Phase.RESOLVED);
                CompilationUnitTree cut = workingCopy.getCompilationUnit();
                TreeMaker make = workingCopy.getTreeMaker();
                ClassTree clazz = (ClassTree) cut.getTypeDecls().get(0);
                final TypeElement foo = workingCopy.getElements().getTypeElement("java.lang.Character");
                assertNotNull(foo);
                Tree type = make.QualIdent(foo);
                VariableTree vt = make.Variable(make.Modifiers(EnumSet.noneOf(Modifier.class)), "test", type, null);
                workingCopy.rewrite(clazz, make.addClassMember(clazz, vt));
            }
        };
        src.runModificationTask(task).commit();
        assertFiles("testImports157566c.pass");
    }

    public void test157566d() throws IOException {
        testFile = getFile(getSourceDir(), "org/netbeans/test/codegen/imports157566/b/Testd.java");
        JavaSource src = getJavaSource(testFile);
        Task<WorkingCopy> task = new Task<WorkingCopy>() {
            public void run(WorkingCopy workingCopy) throws IOException {
                workingCopy.toPhase(Phase.RESOLVED);
                CompilationUnitTree cut = workingCopy.getCompilationUnit();
                TreeMaker make = workingCopy.getTreeMaker();
                ClassTree clazz = (ClassTree) cut.getTypeDecls().get(0);
                final TypeElement foo = workingCopy.getElements().getTypeElement("java.lang.String");
                assertNotNull(foo);
                Tree type = make.QualIdent(foo);
                VariableTree vt = make.Variable(make.Modifiers(EnumSet.noneOf(Modifier.class)), "test", type, null);
                workingCopy.rewrite(clazz, make.addClassMember(clazz, vt));
            }
        };
        src.runModificationTask(task).commit();
        assertFiles("testImports157566d.pass");
    }

    public void test157566e() throws IOException {
        testFile = getFile(getSourceDir(), "org/netbeans/test/codegen/imports157566/b/Teste.java");
        JavaSource src = getJavaSource(testFile);
        Task<WorkingCopy> task = new Task<WorkingCopy>() {
            public void run(WorkingCopy workingCopy) throws IOException {
                workingCopy.toPhase(Phase.RESOLVED);
                CompilationUnitTree cut = workingCopy.getCompilationUnit();
                TreeMaker make = workingCopy.getTreeMaker();
                ClassTree clazz = (ClassTree) cut.getTypeDecls().get(0);
                final TypeElement foo = workingCopy.getElements().getTypeElement("org.netbeans.test.codegen.imports157566.b.String");
                assertNotNull(foo);
                Tree type = make.QualIdent(foo);
                VariableTree vt = make.Variable(make.Modifiers(EnumSet.noneOf(Modifier.class)), "test", type, null);
                workingCopy.rewrite(clazz, make.addClassMember(clazz, vt));
            }
        };
        src.runModificationTask(task).commit();
        assertFiles("testImports157566e.pass");
    }

    String getSourcePckg() {
        if (getName().contains("DefaultPackage")) {
            return "";
        } else {
            return "org/netbeans/test/codegen/";
        }
    }

    String getGoldenPckg() {
        return "org/netbeans/jmi/javamodel/codegen/ImportAnalysisTest/";
    }

    @Override
    void assertFiles(final String aGoldenFile) throws IOException, FileStateInvalidException {
        assertFile("File is not correctly generated.", getTestFile(), getFile(getGoldenDir(), getGoldenPckg() + aGoldenFile), getWorkDir(), new WhitespaceIgnoringDiff());
    }
}
