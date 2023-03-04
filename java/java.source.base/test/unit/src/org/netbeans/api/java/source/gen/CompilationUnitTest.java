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
import org.netbeans.api.java.source.support.ErrorAwareTreePathScanner;
import java.io.File;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.EnumSet;
import java.util.Map;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeKind;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.source.ClasspathInfo;
import org.netbeans.api.java.source.Comment;
import org.netbeans.api.java.source.GeneratorUtilities;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.JavaSource.Phase;
import org.netbeans.api.java.source.ModificationResult;
import org.netbeans.api.java.source.Task;
import org.netbeans.api.java.source.TestUtilities;
import org.netbeans.api.java.source.TreeMaker;
import org.netbeans.api.java.source.WorkingCopy;
import org.netbeans.junit.NbTestSuite;
import org.netbeans.modules.java.source.save.CasualDiff;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.CreateFromTemplateHandler;
import org.openide.loaders.DataFolder;
import org.openide.loaders.DataObject;
import org.openide.util.lookup.ServiceProvider;

/**
 * Test for compilation unit creation.
 * 
 * @author tom a lahvovej
 */
public class CompilationUnitTest extends GeneratorTestMDRCompat {

    public CompilationUnitTest(String name) {
        super(name);
    }

    public static NbTestSuite suite() {
        NbTestSuite suite = new NbTestSuite();
        suite.addTestSuite(CompilationUnitTest.class);
//        suite.addTest(new CompilationUnitTest("testNewCompilationUnit"));
//        suite.addTest(new CompilationUnitTest("test77010"));
//        suite.addTest(new CompilationUnitTest("test117607_1"));
//        suite.addTest(new CompilationUnitTest("test157760"));
        return suite;
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        FileObject template = FileUtil.getConfigFile("Templates/Classes/Class.java");
        if (template != null) template.delete();
        FileObject template2 = FileUtil.getConfigFile("Templates/Classes/package-info.java");
        if (template2 != null) template2.delete();
    }
    
    public void testRemoveClassFromCompUnit() throws Exception {
        testFile = new File(getWorkDir(), "Test.java");
        TestUtilities.copyStringToFile(testFile, 
                "package zoo;\n"
                + "\n"
                + "public class A {\n"
                + "  /** Something about a */\n"
                + "  int a;\n"
                + "  public class Krtek {\n"
                + "    public void foo() {\n"
                + "      int c=a;\n"
                + "    }\n"
                + "  }\n"
                + "}\n"
                + "class B {\n"
                + "    int a = 42;\n"
                + "}\n");
        
        FileObject testSourceFO = FileUtil.toFileObject(testFile);
        assertNotNull(testSourceFO);
        ClassPath sourcePath = ClassPath.getClassPath(testSourceFO, ClassPath.SOURCE);
        assertNotNull(sourcePath);
        FileObject[] roots = sourcePath.getRoots();
        assertEquals(1, roots.length);
        final FileObject sourceRoot = roots[0];
        assertNotNull(sourceRoot);
        ClassPath compilePath = ClassPath.getClassPath(testSourceFO, ClassPath.COMPILE);
        assertNotNull(compilePath);
        ClassPath bootPath = ClassPath.getClassPath(testSourceFO, ClassPath.BOOT);
        assertNotNull(bootPath);
        ClasspathInfo cpInfo = ClasspathInfo.create(bootPath, compilePath, sourcePath);
        
        String golden1 = 
            "package zoo;\n" +
            "\n" +
            "class B {\n" +
            "    int a = 42;\n" +
            "}\n";
        JavaSource javaSource = JavaSource.create(cpInfo, FileUtil.toFileObject(testFile));
        
        Task<WorkingCopy> task = new Task<WorkingCopy>() {

            public void cancel() {
            }

            public void run(WorkingCopy workingCopy) throws Exception {
                workingCopy.toPhase(JavaSource.Phase.PARSED);
                TreeMaker make = workingCopy.getTreeMaker();
                CompilationUnitTree cut = workingCopy.getCompilationUnit();
                CompilationUnitTree ccut = GeneratorUtilities.get(workingCopy).importComments(cut, cut);
                ClassTree clazz = (ClassTree) cut.getTypeDecls().get(0);
                CompilationUnitTree newTree = make.removeCompUnitTypeDecl(ccut, clazz);
                workingCopy.rewrite(cut, newTree);
            }
        };
        ModificationResult result = javaSource.runModificationTask(task);
        result.commit();
        String res = TestUtilities.copyFileToString(testFile);
        //System.err.println(res);
        assertEquals(golden1, res);
    }

    public void testNewCompilationUnitFromTemplate() throws Exception {
        testFile = new File(getWorkDir(), "Test.java");

        File fakeFile = new File(getWorkDir(), "Fake.java");
        FileObject fakeFO = FileUtil.createData(fakeFile);

        FileObject emptyJava = FileUtil.createData(FileUtil.getConfigRoot(), "Templates/Classes/Empty.java");
        emptyJava.setAttribute("template", Boolean.TRUE);

        FileObject classJava = FileUtil.createData(FileUtil.getConfigRoot(), "Templates/Classes/Class.java");
        classJava.setAttribute("template", Boolean.TRUE);
        classJava.setAttribute("verbatim-create-from-template", Boolean.TRUE);
        Writer w = new OutputStreamWriter(classJava.getOutputStream(), StandardCharsets.UTF_8);
        w.write("/*\n * License\n */\npackage zoo;\n\n/**\n * trida\n */\npublic class Template {\n}");
        w.close();

        FileObject packageJava = FileUtil.createData(FileUtil.getConfigRoot(), "Templates/Classes/package-info.java");
        packageJava.setAttribute("template", Boolean.TRUE);
        packageJava.setAttribute("verbatim-create-from-template", Boolean.TRUE);
        Writer w2 = new OutputStreamWriter(packageJava.getOutputStream(), StandardCharsets.UTF_8);
        w2.write("/*\n * License\n */\npackage zoo;\n");
        w2.close();

        FileObject testSourceFO = FileUtil.createData(testFile); assertNotNull(testSourceFO);
        ClassPath sourcePath = ClassPath.getClassPath(testSourceFO, ClassPath.SOURCE); assertNotNull(sourcePath);
        FileObject[] roots = sourcePath.getRoots(); assertEquals(1, roots.length);
        final FileObject sourceRoot = roots[0]; assertNotNull(sourceRoot);
        ClassPath compilePath = ClassPath.getClassPath(testSourceFO, ClassPath.COMPILE); assertNotNull(compilePath);
        ClassPath bootPath = ClassPath.getClassPath(testSourceFO, ClassPath.BOOT); assertNotNull(bootPath);
        ClasspathInfo cpInfo = ClasspathInfo.create(bootPath, compilePath, sourcePath);
        JavaSource javaSource = JavaSource.create(cpInfo, fakeFO);

        Task<WorkingCopy> task = new Task<WorkingCopy>() {

            public void cancel() {
            }

            public void run(WorkingCopy workingCopy) throws Exception {
                workingCopy.toPhase(JavaSource.Phase.PARSED);
                TreeMaker make = workingCopy.getTreeMaker();
                String path = "zoo/Krtek.java";
                GeneratorUtilities genUtils = GeneratorUtilities.get(workingCopy);
                CompilationUnitTree newTree = genUtils.createFromTemplate(sourceRoot, path, ElementKind.CLASS);
                MethodTree nju = make.Method(
                        make.Modifiers(Collections.<Modifier>emptySet()),
                        "m",
                        make.PrimitiveType(TypeKind.VOID), // return type - void
                        Collections.<TypeParameterTree>emptyList(),
                        Collections.<VariableTree>emptyList(),
                        Collections.<ExpressionTree>emptyList(),
                        make.Block(Collections.<StatementTree>emptyList(), false),
                        null // default value - not applicable
                );
                ClassTree clazz = make.Class(
                        make.Modifiers(Collections.<Modifier>singleton(Modifier.PUBLIC)),
                        "Krtek",
                        Collections.<TypeParameterTree>emptyList(),
                        null,
                        Collections.<Tree>emptyList(),
                        Collections.singletonList(nju)
                );
                if(newTree.getTypeDecls().isEmpty()) {
                    newTree = make.addCompUnitTypeDecl(newTree, clazz);
                } else {
                    Tree templateClass = newTree.getTypeDecls().get(0);
                    genUtils.copyComments(templateClass, clazz, true);
                    genUtils.copyComments(templateClass, clazz, false);
                    newTree = make.removeCompUnitTypeDecl(newTree, 0);
                    newTree = make.insertCompUnitTypeDecl(newTree, 0, clazz);
                }
                workingCopy.rewrite(null, newTree);

                String packagePath = "zoo/package-info.java";
                CompilationUnitTree newPackageTree = genUtils.createFromTemplate(sourceRoot, packagePath, ElementKind.PACKAGE);
                workingCopy.rewrite(null, newPackageTree);
            }
        };
        ModificationResult result = javaSource.runModificationTask(task);
        result.commit();

        String goldenClass =
            "/*\n * License\n */\n" +
            "package zoo;\n" +
            "\n" +
            "/**\n * trida\n */\n" +
            "public class Krtek {\n" +
            "\n" +
            "    void m() {\n" +
            "    }\n" +
            "}";
        String res = TestUtilities.copyFileToString(new File(getDataDir().getAbsolutePath() + "/zoo/Krtek.java"));
        assertEquals(goldenClass, res);

        String goldenPackage =
            "/*\n * License\n */\n" +
            "package zoo;\n";
        res = TestUtilities.copyFileToString(new File(getDataDir().getAbsolutePath() + "/zoo/package-info.java"));
        assertEquals(goldenPackage, res);
    }

    public void testNewCompilationUnitFromNonExistingTemplate() throws Exception {
        testFile = new File(getWorkDir(), "Test.java");

        File fakeFile = new File(getWorkDir(), "Fake.java");
        FileObject fakeFO = FileUtil.createData(fakeFile);

        FileObject template = FileUtil.getConfigFile("Templates/Classes/Class.java");
        if (template != null) template.delete();
        template = FileUtil.getConfigFile("Templates/Classes/Empty.java");
        if(template != null) template.delete();

        FileObject testSourceFO = FileUtil.createData(testFile); assertNotNull(testSourceFO);
        ClassPath sourcePath = ClassPath.getClassPath(testSourceFO, ClassPath.SOURCE); assertNotNull(sourcePath);
        FileObject[] roots = sourcePath.getRoots(); assertEquals(1, roots.length);
        final FileObject sourceRoot = roots[0]; assertNotNull(sourceRoot);
        ClassPath compilePath = ClassPath.getClassPath(testSourceFO, ClassPath.COMPILE); assertNotNull(compilePath);
        ClassPath bootPath = ClassPath.getClassPath(testSourceFO, ClassPath.BOOT); assertNotNull(bootPath);
        ClasspathInfo cpInfo = ClasspathInfo.create(bootPath, compilePath, sourcePath);
        JavaSource javaSource = JavaSource.create(cpInfo, fakeFO);

        Task<WorkingCopy> task = new Task<WorkingCopy>() {

            public void cancel() {
            }

            public void run(WorkingCopy workingCopy) throws Exception {
                workingCopy.toPhase(JavaSource.Phase.PARSED);
                TreeMaker make = workingCopy.getTreeMaker();
                String path = "zoo/Krtek.java";
                GeneratorUtilities genUtils = GeneratorUtilities.get(workingCopy);
                CompilationUnitTree newTree = genUtils.createFromTemplate(sourceRoot, path, ElementKind.CLASS);
                workingCopy.rewrite(null, newTree);
            }
        };
        ModificationResult result = javaSource.runModificationTask(task);
        result.commit();

        String res = TestUtilities.copyFileToString(new File(getDataDir().getAbsolutePath() + "/zoo/Krtek.java"));
        assertEquals(res, "package zoo;\n\n");
    }

    public void testNewCompilationUnit() throws Exception {
        testFile = new File(getWorkDir(), "Test.java");

        File fakeFile = new File(getWorkDir(), "Fake.java");
        FileObject fakeFO = FileUtil.createData(fakeFile);

        FileObject emptyJava = FileUtil.createData(FileUtil.getConfigRoot(), "Templates/Classes/Empty.java");
        emptyJava.setAttribute("template", Boolean.TRUE);
        FileObject testSourceFO = FileUtil.createData(testFile);
        assertNotNull(testSourceFO);
        ClassPath sourcePath = ClassPath.getClassPath(testSourceFO, ClassPath.SOURCE);
        assertNotNull(sourcePath);
        FileObject[] roots = sourcePath.getRoots();
        assertEquals(1, roots.length);
        final FileObject sourceRoot = roots[0];
        assertNotNull(sourceRoot);
        ClassPath compilePath = ClassPath.getClassPath(testSourceFO, ClassPath.COMPILE);
        assertNotNull(compilePath);
        ClassPath bootPath = ClassPath.getClassPath(testSourceFO, ClassPath.BOOT);
        assertNotNull(bootPath);
        ClasspathInfo cpInfo = ClasspathInfo.create(bootPath, compilePath, sourcePath);
        JavaSource javaSource = JavaSource.create(cpInfo, fakeFO);
        
        String golden = 
            "package zoo;\n" +
            "\n" +
            "public class Krtek {\n" +
            "\n" +
            "    void m() {\n" +
            "    }\n" +
            "}\n";
        Task<WorkingCopy> task = new Task<WorkingCopy>() {

            public void cancel() {
            }

            public void run(WorkingCopy workingCopy) throws Exception {
                workingCopy.toPhase(JavaSource.Phase.PARSED);
                TreeMaker make = workingCopy.getTreeMaker();
                CompilationUnitTree newTree = make.CompilationUnit(
                        sourceRoot,
                        "zoo/Krtek.java",
                        Collections.<ImportTree>emptyList(),
                        Collections.<Tree>emptyList()
                );
                ClassTree clazz = make.Class(
                        make.Modifiers(Collections.<Modifier>singleton(Modifier.PUBLIC)),
                        "Krtek",
                        Collections.<TypeParameterTree>emptyList(),
                        null,
                        Collections.<Tree>emptyList(),
                        Collections.<Tree>emptyList()
                );
                newTree = make.addCompUnitTypeDecl(newTree, clazz);
                MethodTree nju = make.Method(
                        make.Modifiers(Collections.<Modifier>emptySet()),
                        "m",
                        make.PrimitiveType(TypeKind.VOID), // return type - void
                        Collections.<TypeParameterTree>emptyList(),
                        Collections.<VariableTree>emptyList(),
                        Collections.<ExpressionTree>emptyList(),
                        make.Block(Collections.<StatementTree>emptyList(), false),
                        null // default value - not applicable
                );
                workingCopy.rewrite(null, newTree);
                workingCopy.rewrite(clazz, make.addClassMember(clazz, nju));
            }
        };
        ModificationResult result = javaSource.runModificationTask(task);
        result.commit();
        String res = TestUtilities.copyFileToString(new File(getDataDir().getAbsolutePath() + "/zoo/Krtek.java"));
        //System.err.println(res);
        assertEquals(res, golden);
    }

    public void testNewCompilationUnitInDefaultPackage() throws Exception {
        testFile = new File(getWorkDir(), "Test.java");

        File fakeFile = new File(getWorkDir(), "Fake.java");
        FileObject fakeFO = FileUtil.createData(fakeFile);

        FileObject emptyJava = FileUtil.createData(FileUtil.getConfigRoot(), "Templates/Classes/Empty.java");
        emptyJava.setAttribute("template", Boolean.TRUE);
        FileObject testSourceFO = FileUtil.createData(testFile);
        assertNotNull(testSourceFO);
        ClassPath sourcePath = ClassPath.getClassPath(testSourceFO, ClassPath.SOURCE);
        assertNotNull(sourcePath);
        FileObject[] roots = sourcePath.getRoots();
        assertEquals(1, roots.length);
        final FileObject sourceRoot = roots[0];
        assertNotNull(sourceRoot);
        ClassPath compilePath = ClassPath.getClassPath(testSourceFO, ClassPath.COMPILE);
        assertNotNull(compilePath);
        ClassPath bootPath = ClassPath.getClassPath(testSourceFO, ClassPath.BOOT);
        assertNotNull(bootPath);
        ClasspathInfo cpInfo = ClasspathInfo.create(bootPath, compilePath, sourcePath);
        JavaSource javaSource = JavaSource.create(cpInfo, fakeFO);

        String golden =
            "\n" +
            "public class Krtek {\n" +
            "\n" +
            "    void m() {\n" +
            "    }\n" +
            "}\n";
        Task<WorkingCopy> task = new Task<WorkingCopy>() {

            public void cancel() {
            }

            public void run(WorkingCopy workingCopy) throws Exception {
                workingCopy.toPhase(JavaSource.Phase.PARSED);
                TreeMaker make = workingCopy.getTreeMaker();
                CompilationUnitTree newTree = make.CompilationUnit(
                        sourceRoot,
                        "Krtek.java",
                        Collections.<ImportTree>emptyList(),
                        Collections.<Tree>emptyList()
                );
                ClassTree clazz = make.Class(
                        make.Modifiers(Collections.<Modifier>singleton(Modifier.PUBLIC)),
                        "Krtek",
                        Collections.<TypeParameterTree>emptyList(),
                        null,
                        Collections.<Tree>emptyList(),
                        Collections.<Tree>emptyList()
                );
                newTree = make.addCompUnitTypeDecl(newTree, clazz);
                MethodTree nju = make.Method(
                        make.Modifiers(Collections.<Modifier>emptySet()),
                        "m",
                        make.PrimitiveType(TypeKind.VOID), // return type - void
                        Collections.<TypeParameterTree>emptyList(),
                        Collections.<VariableTree>emptyList(),
                        Collections.<ExpressionTree>emptyList(),
                        make.Block(Collections.<StatementTree>emptyList(), false),
                        null // default value - not applicable
                );
                workingCopy.rewrite(null, newTree);
                workingCopy.rewrite(clazz, make.addClassMember(clazz, nju));
            }
        };
        ModificationResult result = javaSource.runModificationTask(task);
        result.commit();
        String res = TestUtilities.copyFileToString(new File(getDataDir().getAbsolutePath() + "/Krtek.java"));
        //System.err.println(res);
        assertEquals(res, golden);
    }

     public void test77010() throws Exception {
        testFile = new File(getWorkDir(), "Test.java");
        TestUtilities.copyStringToFile(testFile, 
            "package zoo;\n" +
            "\n" +
            "public class A {\n" +
            "  int a;\n" +
            "  public class Krtek {\n" +
            "    public void foo() {\n" +
            "      int c=a;\n" +
            "    }\n" +
            "  }\n" +
            "}");
        
        FileObject emptyJava = FileUtil.createData(FileUtil.getConfigRoot(), "Templates/Classes/Empty.java");
        emptyJava.setAttribute("template", Boolean.TRUE);
        FileObject testSourceFO = FileUtil.toFileObject(testFile);
        assertNotNull(testSourceFO);
        ClassPath sourcePath = ClassPath.getClassPath(testSourceFO, ClassPath.SOURCE);
        assertNotNull(sourcePath);
        FileObject[] roots = sourcePath.getRoots();
        assertEquals(1, roots.length);
        final FileObject sourceRoot = roots[0];
        assertNotNull(sourceRoot);
        ClassPath compilePath = ClassPath.getClassPath(testSourceFO, ClassPath.COMPILE);
        assertNotNull(compilePath);
        ClassPath bootPath = ClassPath.getClassPath(testSourceFO, ClassPath.BOOT);
        assertNotNull(bootPath);
        ClasspathInfo cpInfo = ClasspathInfo.create(bootPath, compilePath, sourcePath);
        
        String golden1 = 
            "package zoo;\n" +
            "\n" +
            "public class Krtek {\n" +
            "\n" +
            "    public void foo() {\n" +
            "        int c = outer.a;\n" +
            "    }\n" +
            "}\n";
        String golden2 = 
            "package zoo;\n" +
            "\n" +
            "public class A {\n" +
            "  int a;\n" +
            "}";
        JavaSource javaSource = JavaSource.create(cpInfo, FileUtil.toFileObject(testFile));
        
        Task<WorkingCopy> task = new Task<WorkingCopy>() {

            public void cancel() {
            }

            public void run(WorkingCopy workingCopy) throws Exception {
                workingCopy.toPhase(JavaSource.Phase.PARSED);
                TreeMaker make = workingCopy.getTreeMaker();
                CompilationUnitTree cut = workingCopy.getCompilationUnit();
                if (cut.getTypeDecls().isEmpty()) return;
                ClassTree clazz = (ClassTree) cut.getTypeDecls().get(0);
                clazz = (ClassTree) clazz.getMembers().get(1);
                MethodTree method = (MethodTree) clazz.getMembers().get(0);
                VariableTree var = (VariableTree) method.getBody().getStatements().get(0);
                CompilationUnitTree newTree = make.CompilationUnit(
                        sourceRoot,
                        "zoo/Krtek.java",
                        Collections.<ImportTree>emptyList(),
                        Collections.<Tree>emptyList()
                );
                newTree = make.addCompUnitTypeDecl(newTree, clazz);
                workingCopy.rewrite(null, newTree);
                workingCopy.rewrite(var.getInitializer(), make.Identifier("outer.a"));
                workingCopy.rewrite(
                        cut.getTypeDecls().get(0), 
                        make.removeClassMember((ClassTree) cut.getTypeDecls().get(0), clazz)
                );
            }
        };
        ModificationResult result = javaSource.runModificationTask(task);
        result.commit();
        String res = TestUtilities.copyFileToString(new File(getDataDir().getAbsolutePath() + "/zoo/Krtek.java"));
        //System.err.println(res);
        assertEquals(res, golden1);
        res = TestUtilities.copyFileToString(testFile);
        //System.err.println(res);
        assertEquals(res, golden2);
    }
     
     public void test117607_1() throws Exception {
        testFile = new File(getWorkDir(), "Test.java");
        TestUtilities.copyStringToFile(testFile, 
            "package zoo;\n" +
            "\n" +
            "import java.util.List;\n" +
            "\n" +
            "public class A {\n" +
            "  public class Krtek {\n" +
            "    @SuppressWarnings(\"a\")\n" + //#176955
            "    public void foo() {\n" +
            "        List l = null;\n" +
            "    }\n" +
            "  }\n" +
            "}\n"
        );
        
        FileObject emptyJava = FileUtil.createData(FileUtil.getConfigRoot(), "Templates/Classes/Empty.java");
        emptyJava.setAttribute("template", Boolean.TRUE);
        FileObject testSourceFO = FileUtil.toFileObject(testFile);
        assertNotNull(testSourceFO);
        ClassPath sourcePath = ClassPath.getClassPath(testSourceFO, ClassPath.SOURCE);
        assertNotNull(sourcePath);
        FileObject[] roots = sourcePath.getRoots();
        assertEquals(1, roots.length);
        final FileObject sourceRoot = roots[0];
        assertNotNull(sourceRoot);
        ClassPath compilePath = ClassPath.getClassPath(testSourceFO, ClassPath.COMPILE);
        assertNotNull(compilePath);
        ClassPath bootPath = ClassPath.getClassPath(testSourceFO, ClassPath.BOOT);
        assertNotNull(bootPath);
        ClasspathInfo cpInfo = ClasspathInfo.create(bootPath, compilePath, sourcePath);
        
        String golden1 = 
            "package zoo;\n" +
            "\n" +
            "import java.util.List;\n" +
            "\n" +
            "public class Krtek {\n" +
            "\n" +
            "    @SuppressWarnings(value = \"a\")\n" +
            "    public void foo() {\n" +
            "        List l = null;\n" +
            "    }\n" +
            "}\n";
        String golden2 = 
            "package zoo;\n" +
            "\n" +
            "import java.util.List;\n" +
            "\n" +
            "public class A {\n" +
            "}\n";
        
        JavaSource javaSource = JavaSource.create(cpInfo, FileUtil.toFileObject(testFile));
        
        Task<WorkingCopy> task = new Task<WorkingCopy>() {

            public void cancel() {
            }

            public void run(WorkingCopy workingCopy) throws Exception {
                workingCopy.toPhase(JavaSource.Phase.RESOLVED);
                TreeMaker make = workingCopy.getTreeMaker();
                CompilationUnitTree cut = workingCopy.getCompilationUnit();
                if (cut.getTypeDecls().isEmpty()) return;
                ClassTree clazz = (ClassTree) cut.getTypeDecls().get(0);
                clazz = (ClassTree) clazz.getMembers().get(1);
                CompilationUnitTree newTree = make.CompilationUnit(
                        sourceRoot,
                        "zoo/Krtek.java",
                        Collections.<ImportTree>emptyList(),
                        Collections.<Tree>emptyList()
                );
                newTree = make.addCompUnitTypeDecl(newTree, GeneratorUtilities.get(workingCopy).importFQNs(clazz));
                workingCopy.rewrite(null, newTree);
                workingCopy.rewrite(
                        cut.getTypeDecls().get(0), 
                        make.removeClassMember((ClassTree) cut.getTypeDecls().get(0), clazz)
                );
            }
        };
        ModificationResult result = javaSource.runModificationTask(task);
        result.commit();
        String res = TestUtilities.copyFileToString(new File(getDataDir().getAbsolutePath() + "/zoo/Krtek.java"));
        //System.err.println(res);
        assertEquals(res, golden1);
        res = TestUtilities.copyFileToString(testFile);
        //System.err.println(res);
        assertEquals(res, golden2);
    }
     
     public void test117607_2() throws Exception {
        testFile = new File(getWorkDir(), "Test.java");
        TestUtilities.copyStringToFile(testFile, 
            "package zoo;\n" +
            "\n" +
            "import java.io.File;\n" +
            "\n" +
            "public class A {\n" +
            "  public class Krtek {\n" +
            "    File m() {\n" +
            "      return null;\n" +
            "    }\n" +
            "  }\n" +
            "}\n"
        );
        
        FileObject emptyJava = FileUtil.createData(FileUtil.getConfigRoot(), "Templates/Classes/Empty.java");
        emptyJava.setAttribute("template", Boolean.TRUE);
        FileObject testSourceFO = FileUtil.toFileObject(testFile);
        assertNotNull(testSourceFO);
        ClassPath sourcePath = ClassPath.getClassPath(testSourceFO, ClassPath.SOURCE);
        assertNotNull(sourcePath);
        FileObject[] roots = sourcePath.getRoots();
        assertEquals(1, roots.length);
        final FileObject sourceRoot = roots[0];
        assertNotNull(sourceRoot);
        ClassPath compilePath = ClassPath.getClassPath(testSourceFO, ClassPath.COMPILE);
        assertNotNull(compilePath);
        ClassPath bootPath = ClassPath.getClassPath(testSourceFO, ClassPath.BOOT);
        assertNotNull(bootPath);
        ClasspathInfo cpInfo = ClasspathInfo.create(bootPath, compilePath, sourcePath);
        
        String golden1 = 
            "package zoo;\n" +
            "\n" +
            "import java.io.File;\n" +
            "\n" +
            "public class Krtek {\n" +
            "\n" +
            "    File m() {\n" +
            "        return null;\n" +
            "    }\n" +
            "}\n";
        String golden2 = 
            "package zoo;\n" +
            "\n" +
            "import java.io.File;\n" +
            "\n" +
            "public class A {\n" +
            "}\n";
        
        JavaSource javaSource = JavaSource.create(cpInfo, FileUtil.toFileObject(testFile));
        
        Task<WorkingCopy> task = new Task<WorkingCopy>() {

            public void cancel() {
            }

            public void run(WorkingCopy workingCopy) throws Exception {
                workingCopy.toPhase(JavaSource.Phase.RESOLVED);
                TreeMaker make = workingCopy.getTreeMaker();
                CompilationUnitTree cut = workingCopy.getCompilationUnit();
                if (cut.getTypeDecls().isEmpty()) return;
                ClassTree clazz = (ClassTree) cut.getTypeDecls().get(0);
                clazz = (ClassTree) clazz.getMembers().get(1);
                CompilationUnitTree newTree = make.CompilationUnit(
                        sourceRoot,
                        "zoo/Krtek.java",
                        Collections.<ImportTree>emptyList(),
                        Collections.<Tree>emptyList()
                );
                newTree = make.addCompUnitTypeDecl(newTree, GeneratorUtilities.get(workingCopy).importFQNs(clazz));
                workingCopy.rewrite(null, newTree);
                workingCopy.rewrite(
                        cut.getTypeDecls().get(0), 
                        make.removeClassMember((ClassTree) cut.getTypeDecls().get(0), clazz)
                );
            }
        };
        ModificationResult result = javaSource.runModificationTask(task);
        result.commit();
        String res = TestUtilities.copyFileToString(new File(getDataDir().getAbsolutePath() + "/zoo/Krtek.java"));
        //System.err.println(res);
        assertEquals(res, golden1);
        res = TestUtilities.copyFileToString(testFile);
        //System.err.println(res);
        assertEquals(res, golden2);
    }
     
     public void test121729() throws Exception {
        testFile = new File(getWorkDir(), "Test.java");
        TestUtilities.copyStringToFile(testFile, 
            "package zoo;\n" +
            "\n" +
            "public class A {\n" +
            "}\n"
        );
        
        FileObject testSourceFO = FileUtil.toFileObject(testFile);
        assertNotNull(testSourceFO);
        
        String golden = 
            "package zoo;\n" +
            "\n" +
            "public class B {\n" +
            "}\n";
        
        JavaSource javaSource = JavaSource.forFileObject(testSourceFO);
        
        //does not modify the source:
        javaSource.runModificationTask(new Task<WorkingCopy>() {
            public void run(final WorkingCopy parameter) throws Exception {
                parameter.toPhase(Phase.ELEMENTS_RESOLVED);
                
                new ErrorAwareTreePathScanner<Void, Void>() {
                    @Override
                    public Void visitClass(ClassTree node, Void p) {
                        assertNotNull(parameter.getTrees().getElement(getCurrentPath()));
                        return super.visitClass(node, p);
                    }
                }.scan(parameter.getCompilationUnit(), null);
            }
        }).commit();
        
        //does the modification:
        javaSource.runModificationTask(new Task<WorkingCopy>() {
            public void run(final WorkingCopy parameter) throws Exception {
                parameter.toPhase(Phase.ELEMENTS_RESOLVED);
                
                new ErrorAwareTreePathScanner<Void, Void>() {
                    @Override
                    public Void visitClass(ClassTree node, Void p) {
                        assertNotNull(parameter.getTrees().getElement(getCurrentPath()));
                        parameter.rewrite(node, parameter.getTreeMaker().setLabel(node, "B"));
                        return super.visitClass(node, p);
                    }
                }.scan(parameter.getCompilationUnit(), null);
            }
        }).commit();
        
        String res = TestUtilities.copyFileToString(testFile);
        //System.err.println(res);
        assertEquals(res, golden);
    }
     
     public void test157760a() throws Exception {
        testFile = new File(getWorkDir(), "Test.java");
        TestUtilities.copyStringToFile(testFile, "package foo; public @interface YY {}");

        FileObject emptyJava = FileUtil.createData(FileUtil.getConfigRoot(), "Templates/Classes/Empty.java");
        emptyJava.setAttribute("template", Boolean.TRUE);
        FileObject testSourceFO = FileUtil.toFileObject(testFile);
        assertNotNull(testSourceFO);
        ClassPath sourcePath = ClassPath.getClassPath(testSourceFO, ClassPath.SOURCE);
        assertNotNull(sourcePath);
        FileObject[] roots = sourcePath.getRoots();
        assertEquals(1, roots.length);
        final FileObject sourceRoot = roots[0];
        assertNotNull(sourceRoot);
        ClassPath compilePath = ClassPath.getClassPath(testSourceFO, ClassPath.COMPILE);
        assertNotNull(compilePath);
        ClassPath bootPath = ClassPath.getClassPath(testSourceFO, ClassPath.BOOT);
        assertNotNull(bootPath);
        ClasspathInfo cpInfo = ClasspathInfo.create(bootPath, compilePath, sourcePath);

        String golden =
            "@YY\n" +
            "package zoo;\n\n" +
            "import foo.YY;\n" + /*XXX:*/"\n";

        JavaSource javaSource = JavaSource.create(cpInfo, FileUtil.toFileObject(testFile));

        Task<WorkingCopy> task = new Task<WorkingCopy>() {
            public void run(WorkingCopy workingCopy) throws Exception {
                workingCopy.toPhase(JavaSource.Phase.RESOLVED);
                TypeElement yyAnnotation = workingCopy.getElements().getTypeElement("foo.YY");
                assertNotNull(yyAnnotation);
                TreeMaker make = workingCopy.getTreeMaker();
                CompilationUnitTree newTree = make.CompilationUnit(
                        Collections.singletonList(make.Annotation(make.QualIdent(yyAnnotation), Collections.<ExpressionTree>emptyList())),
                        sourceRoot,
                        "zoo/package-info.java",
                        Collections.<ImportTree>emptyList(),
                        Collections.<Tree>emptyList()
                );
                workingCopy.rewrite(null, newTree);
            }
        };
        ModificationResult result = javaSource.runModificationTask(task);
        result.commit();
        String res = TestUtilities.copyFileToString(new File(getDataDir().getAbsolutePath() + "/zoo/package-info.java"));
        //System.err.println(res);
        assertEquals(golden, res);
    }

    public void test157760b() throws Exception {
        testFile = new File(getWorkDir(), "package-info.java");
        TestUtilities.copyStringToFile(testFile, "@AA\n@BB\n@CC\npackage foo;");

        FileObject testSourceFO = FileUtil.toFileObject(testFile);
        assertNotNull(testSourceFO);
        ClassPath sourcePath = ClassPath.getClassPath(testSourceFO, ClassPath.SOURCE);
        assertNotNull(sourcePath);
        FileObject[] roots = sourcePath.getRoots();
        assertEquals(1, roots.length);
        final FileObject sourceRoot = roots[0];
        assertNotNull(sourceRoot);
        ClassPath compilePath = ClassPath.getClassPath(testSourceFO, ClassPath.COMPILE);
        assertNotNull(compilePath);
        ClassPath bootPath = ClassPath.getClassPath(testSourceFO, ClassPath.BOOT);
        assertNotNull(bootPath);
        ClasspathInfo cpInfo = ClasspathInfo.create(bootPath, compilePath, sourcePath);

        String golden = "@EE\n@CC\n@DD\npackage foo;";

        JavaSource javaSource = JavaSource.create(cpInfo, FileUtil.toFileObject(testFile));

        Task<WorkingCopy> task = new Task<WorkingCopy>() {
            public void run(WorkingCopy workingCopy) throws Exception {
                workingCopy.toPhase(JavaSource.Phase.RESOLVED);
                TreeMaker make = workingCopy.getTreeMaker();
                CompilationUnitTree nue = make.addPackageAnnotation(workingCopy.getCompilationUnit(), make.Annotation(make.Identifier("DD"), Collections.<ExpressionTree>emptyList()));
                nue = make.insertPackageAnnotation(nue, 0, make.Annotation(make.Identifier("EE"), Collections.<ExpressionTree>emptyList()));
                nue = make.removePackageAnnotation(nue, 1);
                nue = make.removePackageAnnotation(nue, workingCopy.getCompilationUnit().getPackageAnnotations().get(1));
                workingCopy.rewrite(workingCopy.getCompilationUnit(), nue);
            }
        };
        ModificationResult result = javaSource.runModificationTask(task);
        result.commit();
        String res = TestUtilities.copyFileToString(testFile);
        //System.err.println(res);
        assertEquals(golden, res);
    }

    public void test157760c() throws Exception {
        testFile = new File(getWorkDir(), "package-info.java");
        TestUtilities.copyStringToFile(testFile, "/*\n a\n */\npackage foo;");

        FileObject testSourceFO = FileUtil.toFileObject(testFile);
        assertNotNull(testSourceFO);
        ClassPath sourcePath = ClassPath.getClassPath(testSourceFO, ClassPath.SOURCE);
        assertNotNull(sourcePath);
        FileObject[] roots = sourcePath.getRoots();
        assertEquals(1, roots.length);
        final FileObject sourceRoot = roots[0];
        assertNotNull(sourceRoot);
        ClassPath compilePath = ClassPath.getClassPath(testSourceFO, ClassPath.COMPILE);
        assertNotNull(compilePath);
        ClassPath bootPath = ClassPath.getClassPath(testSourceFO, ClassPath.BOOT);
        assertNotNull(bootPath);
        ClasspathInfo cpInfo = ClasspathInfo.create(bootPath, compilePath, sourcePath);

        String golden = "/*\n a\n */\n@AA\npackage foo;";

        JavaSource javaSource = JavaSource.create(cpInfo, FileUtil.toFileObject(testFile));

        Task<WorkingCopy> task = new Task<WorkingCopy>() {
            public void run(WorkingCopy workingCopy) throws Exception {
                workingCopy.toPhase(JavaSource.Phase.RESOLVED);
                TreeMaker make = workingCopy.getTreeMaker();
                CompilationUnitTree nue = make.addPackageAnnotation(workingCopy.getCompilationUnit(), make.Annotation(make.Identifier("AA"), Collections.<ExpressionTree>emptyList()));
                workingCopy.rewrite(workingCopy.getCompilationUnit(), nue);
            }
        };
        ModificationResult result = javaSource.runModificationTask(task);
        result.commit();
        String res = TestUtilities.copyFileToString(testFile);
        //System.err.println(res);
        assertEquals(golden, res);
    }

    public void testNewCompilationUnitWithoutExistingFile() throws Exception {
        testFile = new File(getWorkDir(), "Test.java");

        File fakeFile = new File(getWorkDir(), "Fake.java");

        FileObject emptyJava = FileUtil.createData(FileUtil.getConfigRoot(), "Templates/Classes/Empty.java");
        emptyJava.setAttribute("template", Boolean.TRUE);
        FileObject testSourceFO = FileUtil.createData(testFile);
        assertNotNull(testSourceFO);
        ClassPath sourcePath = ClassPath.getClassPath(testSourceFO, ClassPath.SOURCE);
        assertNotNull(sourcePath);
        FileObject[] roots = sourcePath.getRoots();
        assertEquals(1, roots.length);
        final FileObject sourceRoot = roots[0];
        assertNotNull(sourceRoot);
        ClassPath compilePath = ClassPath.getClassPath(testSourceFO, ClassPath.COMPILE);
        assertNotNull(compilePath);
        ClassPath bootPath = ClassPath.getClassPath(testSourceFO, ClassPath.BOOT);
        assertNotNull(bootPath);
        ClasspathInfo cpInfo = ClasspathInfo.create(bootPath, compilePath, sourcePath);
        JavaSource javaSource = JavaSource.create(cpInfo);
        
        String golden = 
            "package zoo;\n" +
            "\n" +
            "public class Krtek {\n" +
            "\n" +
            "    void m() {\n" +
            "    }\n" +
            "}\n";
        Task<WorkingCopy> task = new Task<WorkingCopy>() {

            public void cancel() {
            }

            public void run(WorkingCopy workingCopy) throws Exception {
                workingCopy.toPhase(JavaSource.Phase.PARSED);
                TreeMaker make = workingCopy.getTreeMaker();
                CompilationUnitTree newTree = make.CompilationUnit(
                        sourceRoot,
                        "zoo/Krtek.java",
                        Collections.<ImportTree>emptyList(),
                        Collections.<Tree>emptyList()
                );
                ClassTree clazz = make.Class(
                        make.Modifiers(Collections.<Modifier>singleton(Modifier.PUBLIC)),
                        "Krtek",
                        Collections.<TypeParameterTree>emptyList(),
                        null,
                        Collections.<Tree>emptyList(),
                        Collections.<Tree>emptyList()
                );
                newTree = make.addCompUnitTypeDecl(newTree, clazz);
                MethodTree nju = make.Method(
                        make.Modifiers(Collections.<Modifier>emptySet()),
                        "m",
                        make.PrimitiveType(TypeKind.VOID), // return type - void
                        Collections.<TypeParameterTree>emptyList(),
                        Collections.<VariableTree>emptyList(),
                        Collections.<ExpressionTree>emptyList(),
                        make.Block(Collections.<StatementTree>emptyList(), false),
                        null // default value - not applicable
                );
                newTree = GeneratorUtilities.get(workingCopy).importFQNs(newTree);
                workingCopy.rewrite(null, newTree);
                workingCopy.rewrite(clazz, make.addClassMember(clazz, nju));
            }
        };
        ModificationResult result = javaSource.runModificationTask(task);
        result.commit();
        String res = TestUtilities.copyFileToString(new File(getDataDir().getAbsolutePath() + "/zoo/Krtek.java"));
        //System.err.println(res);
        assertEquals(res, golden);
    }
    
    public void testNewPackageInfo() throws Exception {
        testFile = new File(getWorkDir(), "Test.java");

        FileObject emptyJava = FileUtil.createData(FileUtil.getConfigRoot(), "Templates/Classes/Empty.java");
        emptyJava.setAttribute("template", Boolean.TRUE);
        FileObject packInfo = FileUtil.createData(FileUtil.getConfigRoot(), "Templates/Classes/package-info.java");
        TestUtilities.copyStringToFile(packInfo, "package foo;\n/*mark*/\n");
        packInfo.setAttribute("template", Boolean.TRUE);
        FileObject testSourceFO = FileUtil.createData(testFile);
        assertNotNull(testSourceFO);
        ClassPath sourcePath = ClassPath.getClassPath(testSourceFO, ClassPath.SOURCE);
        assertNotNull(sourcePath);
        FileObject[] roots = sourcePath.getRoots();
        assertEquals(1, roots.length);
        final FileObject sourceRoot = roots[0];
        assertNotNull(sourceRoot);
        ClassPath compilePath = ClassPath.getClassPath(testSourceFO, ClassPath.COMPILE);
        assertNotNull(compilePath);
        ClassPath bootPath = ClassPath.getClassPath(testSourceFO, ClassPath.BOOT);
        assertNotNull(bootPath);
        ClasspathInfo cpInfo = ClasspathInfo.create(bootPath, compilePath, sourcePath);
        JavaSource javaSource = JavaSource.create(cpInfo);
        
        String golden = 
            "package zoo;\n\n\n" +
            "/*mark*/\n";
        Task<WorkingCopy> task = new Task<WorkingCopy>() {
            public void run(WorkingCopy workingCopy) throws Exception {
                TreeMaker make = workingCopy.getTreeMaker();
                CompilationUnitTree newTree = make.CompilationUnit(
                        sourceRoot,
                        "zoo/package-info.java",
                        Collections.<ImportTree>emptyList(),
                        Collections.<Tree>emptyList()
                );
                workingCopy.rewrite(null, newTree);
            }
        };
        ModificationResult result = javaSource.runModificationTask(task);
        result.commit();
        String res = TestUtilities.copyFileToString(new File(getDataDir().getAbsolutePath() + "/zoo/package-info.java"));
        //System.err.println(res);
        assertEquals(golden, res);
    }
    
    public void test196276() throws Exception {
        testFile = new File(getWorkDir(), "Test.java");
        TestUtilities.copyStringToFile(testFile,
            "package zoo;\n" +
            "\n" +
            "public class A {\n" +
            "}\n"
        );

        FileObject emptyJava = FileUtil.createData(FileUtil.getConfigRoot(), "Templates/Classes/Empty.java");
        emptyJava.setAttribute("template", Boolean.TRUE);
        FileObject classJava = FileUtil.createData(FileUtil.getConfigRoot(), "Templates/Classes/Class.java");
        classJava.setAttribute("template", Boolean.TRUE);
        Writer w = new OutputStreamWriter(classJava.getOutputStream(), StandardCharsets.UTF_8);
        w.write("package zoo;\npublic class Template {\n    public Template() {}\n}");
        w.close();
        FileObject testSourceFO = FileUtil.toFileObject(testFile);
        assertNotNull(testSourceFO);
        ClassPath sourcePath = ClassPath.getClassPath(testSourceFO, ClassPath.SOURCE);
        assertNotNull(sourcePath);
        FileObject[] roots = sourcePath.getRoots();
        assertEquals(1, roots.length);
        final FileObject sourceRoot = roots[0];
        assertNotNull(sourceRoot);
        ClassPath compilePath = ClassPath.getClassPath(testSourceFO, ClassPath.COMPILE);
        assertNotNull(compilePath);
        ClassPath bootPath = ClassPath.getClassPath(testSourceFO, ClassPath.BOOT);
        assertNotNull(bootPath);
        ClasspathInfo cpInfo = ClasspathInfo.create(bootPath, compilePath, sourcePath);

        String golden1 =
            "package zoo;\n" +
            "\n" +
            "\n" + //XXX
            "public class Krtek {\n" +
            "    public Krtek() {}\n" +
            "}\n";

        JavaSource javaSource = JavaSource.create(cpInfo, FileUtil.toFileObject(testFile));

        Task<WorkingCopy> task = new Task<WorkingCopy>() {

            public void cancel() {
            }

            public void run(WorkingCopy workingCopy) throws Exception {
                workingCopy.toPhase(JavaSource.Phase.RESOLVED);
                TreeMaker make = workingCopy.getTreeMaker();
                CompilationUnitTree newTree = make.CompilationUnit(
                        sourceRoot,
                        "zoo/Krtek.java",
                        Collections.<ImportTree>emptyList(),
                        Collections.<Tree>emptyList()
                );
                MethodTree constr = make.Method(make.Modifiers(EnumSet.of(Modifier.PUBLIC)), "Krtek", null, Collections.<TypeParameterTree>emptyList(), Collections.<VariableTree>emptyList(), Collections.<ExpressionTree>emptyList(), "{}", null);
                newTree = make.addCompUnitTypeDecl(newTree, make.Class(make.Modifiers(EnumSet.of(Modifier.PUBLIC)), "Krtek", Collections.<TypeParameterTree>emptyList(), null, Collections.<Tree>emptyList(), Collections.<Tree>singletonList(constr)));
                workingCopy.rewrite(null, newTree);
            }
        };
        ModificationResult result = javaSource.runModificationTask(task);
        result.commit();
        String res = TestUtilities.copyFileToString(new File(getDataDir().getAbsolutePath() + "/zoo/Krtek.java"));
        //System.err.println(res);
        assertEquals(res, golden1);
    }

    /** 
     * With bugfix #239849, newly created compilation units should retain their initial template comment, UNLESS
     * the replacement tree comes with its own initial comment(s). This test ensures that a CU correctly replaces
     * the template initial comment. The {@link #testNewCompilationUnitFromTemplate} checks that the template
     * init comment is retained in case the new CU content does not specify any.
     */
    public void testReplaceInitialComment() throws Exception {
        setupJavaTemplates();
        FileObject testSourceFO = FileUtil.toFileObject(testFile);
        assertNotNull(testSourceFO);
        DataObject created = DataObject.find(classJava).createFromTemplate(DataFolder.findFolder(testSourceFO.getParent()));
        assertEquals(template, created.getPrimaryFile().asText());
        ClassPath sourcePath = ClassPath.getClassPath(testSourceFO, ClassPath.SOURCE);
        assertNotNull(sourcePath);
        FileObject[] roots = sourcePath.getRoots();
        assertEquals(1, roots.length);
        final FileObject sourceRoot = roots[0];
        assertNotNull(sourceRoot);
        ClassPath compilePath = ClassPath.getClassPath(testSourceFO, ClassPath.COMPILE);
        assertNotNull(compilePath);
        ClassPath bootPath = ClassPath.getClassPath(testSourceFO, ClassPath.BOOT);
        assertNotNull(bootPath);
        ClasspathInfo cpInfo = ClasspathInfo.create(bootPath, compilePath, sourcePath);

        String golden1 =
            "/*\n" +
            " * replaced\n" +
            " * content */\n" +
            "package zoo;\n" +
            "public class Krtek {\n" +
            "    public Krtek() {}\n" +
            "}\n";

        JavaSource javaSource = JavaSource.create(cpInfo, FileUtil.toFileObject(testFile));

        Task<WorkingCopy> task = new Task<WorkingCopy>() {

            public void cancel() {
            }

            public void run(WorkingCopy workingCopy) throws Exception {
                workingCopy.toPhase(JavaSource.Phase.RESOLVED);
                TreeMaker make = workingCopy.getTreeMaker();
                CompilationUnitTree newTree = make.CompilationUnit(
                        sourceRoot,
                        "zoo/Krtek.java",
                        Collections.<ImportTree>emptyList(),
                        Collections.<Tree>emptyList()
                );
                GeneratorUtilities gu = GeneratorUtilities.get(workingCopy);

                Comment comment = Comment.create(Comment.Style.BLOCK, 0, 0, 0, "\nreplaced\ncontent\n");
                
                MethodTree constr = make.Method(make.Modifiers(EnumSet.of(Modifier.PUBLIC)), "Krtek", null, Collections.<TypeParameterTree>emptyList(), Collections.<VariableTree>emptyList(), Collections.<ExpressionTree>emptyList(), "{}", null);
                newTree = make.addCompUnitTypeDecl(newTree, make.Class(make.Modifiers(EnumSet.of(Modifier.PUBLIC)), "Krtek", Collections.<TypeParameterTree>emptyList(), null, Collections.<Tree>emptyList(), Collections.<Tree>singletonList(constr)));
                make.addComment(newTree, comment, true);
                workingCopy.rewrite(null, newTree);
            }
        };
        ModificationResult result = javaSource.runModificationTask(task);
        result.commit();
        String res = TestUtilities.copyFileToString(new File(getDataDir().getAbsolutePath() + "/zoo/Krtek.java"));
        //System.err.println(res);
        assertEquals(golden1, res);
    }

    private FileObject classJava;
    private String template;

    private void setupJavaTemplates() throws Exception {
        testFile = new File(getWorkDir(), "Test.java");
        TestUtilities.copyStringToFile(testFile,
            "package zoo;\n" +
            "\n" +
            "public class A {\n" +
            "}\n"
        );

        FileObject emptyJava = FileUtil.createData(FileUtil.getConfigRoot(), "Templates/Classes/Empty.java");
        emptyJava.setAttribute("template", Boolean.TRUE);
        classJava = FileUtil.createData(FileUtil.getConfigRoot(), "Templates/Classes/Class.java");
        classJava.setAttribute("template", Boolean.TRUE);
        classJava.setAttribute("verbatim-create-from-template", Boolean.TRUE);
        template = "/*\r\ninitial\r\ncomment\r\n*/\r\npackage zoo;\r\npublic class Template {\r\n    public Template() {}\r\n}\r\n";
        Writer w = new OutputStreamWriter(classJava.getOutputStream(), StandardCharsets.UTF_8);
        w.write(template);
        w.close();
    }
    
    public void test204638() throws Exception {
        setupJavaTemplates();
        FileObject testSourceFO = FileUtil.toFileObject(testFile);
        assertNotNull(testSourceFO);
        DataObject created = DataObject.find(classJava).createFromTemplate(DataFolder.findFolder(testSourceFO.getParent()));
        assertEquals(template, created.getPrimaryFile().asText());
        ClassPath sourcePath = ClassPath.getClassPath(testSourceFO, ClassPath.SOURCE);
        assertNotNull(sourcePath);
        FileObject[] roots = sourcePath.getRoots();
        assertEquals(1, roots.length);
        final FileObject sourceRoot = roots[0];
        assertNotNull(sourceRoot);
        ClassPath compilePath = ClassPath.getClassPath(testSourceFO, ClassPath.COMPILE);
        assertNotNull(compilePath);
        ClassPath bootPath = ClassPath.getClassPath(testSourceFO, ClassPath.BOOT);
        assertNotNull(bootPath);
        ClasspathInfo cpInfo = ClasspathInfo.create(bootPath, compilePath, sourcePath);

        String golden1 =
            "/*\n" +
            "initial\n" +
            "comment\n" +
            "*/\n" +
            "package zoo;\n" +
            "public class Krtek {\n" +
            "    public Krtek() {}\n" +
            "}\n";

        JavaSource javaSource = JavaSource.create(cpInfo, FileUtil.toFileObject(testFile));

        Task<WorkingCopy> task = new Task<WorkingCopy>() {

            public void cancel() {
            }

            public void run(WorkingCopy workingCopy) throws Exception {
                workingCopy.toPhase(JavaSource.Phase.RESOLVED);
                TreeMaker make = workingCopy.getTreeMaker();
                CompilationUnitTree newTree = make.CompilationUnit(
                        sourceRoot,
                        "zoo/Krtek.java",
                        Collections.<ImportTree>emptyList(),
                        Collections.<Tree>emptyList()
                );
                MethodTree constr = make.Method(make.Modifiers(EnumSet.of(Modifier.PUBLIC)), "Krtek", null, Collections.<TypeParameterTree>emptyList(), Collections.<VariableTree>emptyList(), Collections.<ExpressionTree>emptyList(), "{}", null);
                newTree = make.addCompUnitTypeDecl(newTree, make.Class(make.Modifiers(EnumSet.of(Modifier.PUBLIC)), "Krtek", Collections.<TypeParameterTree>emptyList(), null, Collections.<Tree>emptyList(), Collections.<Tree>singletonList(constr)));
                workingCopy.rewrite(null, newTree);
            }
        };
        ModificationResult result = javaSource.runModificationTask(task);
        result.commit();
        String res = TestUtilities.copyFileToString(new File(getDataDir().getAbsolutePath() + "/zoo/Krtek.java"));
        //System.err.println(res);
        assertEquals(res, golden1);
    }

    public void test197097a() throws Exception {
        boolean origKeep = CasualDiff.OLD_TREES_VERBATIM;

        CasualDiff.OLD_TREES_VERBATIM = true;

        try {
            doTest197097();
        } finally {
            CasualDiff.OLD_TREES_VERBATIM = origKeep;
        }
    }

    public void test197097b() throws Exception {
        boolean origKeep = CasualDiff.OLD_TREES_VERBATIM;

        CasualDiff.OLD_TREES_VERBATIM = false;

        try {
            doTest197097();
        } finally {
            CasualDiff.OLD_TREES_VERBATIM = origKeep;
        }
    }
    
    public void doTest197097() throws Exception {
        testFile = new File(getWorkDir(), "Test.java");
        TestUtilities.copyStringToFile(testFile,
            "package zoo;\n" +
            "\n" +
            "public class A {\n" +
            "    public class I extends java.util.ArrayList {\n" +
            "        public I(java.util.Collection c) {\n" +
            "            super(c);\n" +
            "        }\n" +
            "    }\n" +
            "}\n"
        );

        FileObject emptyJava = FileUtil.createData(FileUtil.getConfigRoot(), "Templates/Classes/Empty.java");
        emptyJava.setAttribute("template", Boolean.TRUE);
        FileObject classJava = FileUtil.createData(FileUtil.getConfigRoot(), "Templates/Classes/Class.java");
        classJava.setAttribute("template", Boolean.TRUE);
        Writer w = new OutputStreamWriter(classJava.getOutputStream(), StandardCharsets.UTF_8);
        w.write("package zoo;\npublic class Template {\n  \n}");
        w.close();
        FileObject testSourceFO = FileUtil.toFileObject(testFile);
        assertNotNull(testSourceFO);
        ClassPath sourcePath = ClassPath.getClassPath(testSourceFO, ClassPath.SOURCE);
        assertNotNull(sourcePath);
        FileObject[] roots = sourcePath.getRoots();
        assertEquals(1, roots.length);
        final FileObject sourceRoot = roots[0];
        assertNotNull(sourceRoot);
        ClassPath compilePath = ClassPath.getClassPath(testSourceFO, ClassPath.COMPILE);
        assertNotNull(compilePath);
        ClassPath bootPath = ClassPath.getClassPath(testSourceFO, ClassPath.BOOT);
        assertNotNull(bootPath);
        ClasspathInfo cpInfo = ClasspathInfo.create(bootPath, compilePath, sourcePath);

        String golden1 =
            "package zoo;\n" +
            "\n" +
            "\n" + //XXX
            "public class I extends java.util.ArrayList {\n\n" +
            "    public I(java.util.Collection c) {\n" +
            "        super(c);\n" +
            "    }\n" +
            "  \n" +//XXX
            "}\n";

        JavaSource javaSource = JavaSource.create(cpInfo, FileUtil.toFileObject(testFile));

        Task<WorkingCopy> task = new Task<WorkingCopy>() {

            public void cancel() {
            }

            public void run(WorkingCopy workingCopy) throws Exception {
                workingCopy.toPhase(JavaSource.Phase.RESOLVED);
                TreeMaker make = workingCopy.getTreeMaker();
                Tree classDecl = ((ClassTree) workingCopy.getCompilationUnit().getTypeDecls().get(0)).getMembers().get(1);
                CompilationUnitTree newTree = make.CompilationUnit(
                        sourceRoot,
                        "zoo/I.java",
                        Collections.<ImportTree>emptyList(),
                        Collections.<Tree>singletonList(classDecl)
                );
                workingCopy.rewrite(null, newTree);
                MethodTree constructor = (MethodTree) ((ClassTree) classDecl).getMembers().get(0);
                workingCopy.rewrite(constructor, make.setLabel(constructor, "I"));
            }
        };
        ModificationResult result = javaSource.runModificationTask(task);
        result.commit();
        String res = TestUtilities.copyFileToString(new File(getDataDir().getAbsolutePath() + "/zoo/I.java"));
        //System.err.println(res);
        assertEquals(res, golden1);
    }
    
    String getGoldenPckg() {
        return "";
    }

    String getSourcePckg() {
        return "";
    }

    @ServiceProvider(service=CreateFromTemplateHandler.class)
    public static final class CreateFromTemplateHandlerImpl extends CreateFromTemplateHandler {

        @Override
        protected boolean accept(FileObject orig) {
            return orig.getAttribute("verbatim-create-from-template") != null;
        }

        @Override
        protected FileObject createFromTemplate(FileObject orig, FileObject f, String name, Map<String, Object> parameters) throws IOException {
            return FileUtil.copyFile(orig, f, name);
        }

    }
}
