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

import com.sun.source.tree.Tree.Kind;
import com.sun.source.tree.*;
import java.io.*;
import java.util.Arrays;
import java.util.Collections;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeMirror;
import org.netbeans.api.java.source.*;
import org.netbeans.junit.NbTestSuite;
import static org.netbeans.api.java.source.JavaSource.*;
import org.netbeans.modules.java.source.save.CasualDiff;

/**
 *
 * @author Pavel Flaska
 */
public class ParameterizedTypeTest extends GeneratorTestMDRCompat {

    public ParameterizedTypeTest(String aName) {
        super(aName);
    }

    public static NbTestSuite suite() {
        NbTestSuite suite = new NbTestSuite();
        suite.addTestSuite(ParameterizedTypeTest.class);
//        suite.addTest(new ParameterizedTypeTest("test115176HowTo"));
//        suite.addTest(new ParameterizedTypeTest("test115176TestCase"));
//        suite.addTest(new ParameterizedTypeTest("testChangeToDiamond"));
//        suite.addTest(new ParameterizedTypeTest("test185306"));
        return suite;
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        CasualDiff.noInvalidCopyTos = true;
    }

    public void test115176TestCase() throws Exception {
        testFile = new File(getWorkDir(), "Test.java");
        TestUtilities.copyStringToFile(testFile, 
            "package hierbas.del.litoral;\n" +
            "\n" +
            "public class Test {\n" +
            "}\n"
            );
        String golden =
            "package hierbas.del.litoral;\n" +
            "\n" +
            "import java.util.List;\n" +
            "import java.util.Map;\n" +
            "\n" +
            "public class Test {\n" +
            "\n" +
            "    public List<Map<String, String>> foo() {\n" +
            "    }\n" +
            "}\n";

        JavaSource src = getJavaSource(testFile);
        Task<WorkingCopy> task = new Task<WorkingCopy>() {

            public void run(WorkingCopy workingCopy) throws IOException {
                workingCopy.toPhase(Phase.RESOLVED);
                CompilationUnitTree cut = workingCopy.getCompilationUnit();
                TreeMaker make = workingCopy.getTreeMaker();
                ClassTree classTree = (ClassTree) cut.getTypeDecls().get(0);
                MethodTree newMethod = make.Method(
                    make.Modifiers( 
                        Collections.singleton(Modifier.PUBLIC)
                    ),
                    "foo", // name
                    make.ParameterizedType(
                        make.QualIdent(workingCopy.getElements().getTypeElement("java.util.List")),
                        Collections.<Tree>singletonList(make.ParameterizedType(
                            make.QualIdent(workingCopy.getElements().getTypeElement("java.util.Map")),
                            Arrays.<Tree>asList(make.Identifier("String"), make.Identifier("String"))
                        ))
                    ),
                    Collections.<TypeParameterTree>emptyList(), // type parameters for parameters
                    Collections.<VariableTree>emptyList(), // parameters
                    Collections.<ExpressionTree>emptyList(),
                    make.Block(Collections.<StatementTree>emptyList(), false),
                    null // default value - not applicable here, used by annotations
                );
                ClassTree copy = make.addClassMember(classTree, newMethod);
                workingCopy.rewrite(classTree, copy);
            }
            
        };
        src.runModificationTask(task).commit();
        String res = TestUtilities.copyFileToString(testFile);
        //System.err.println(res);
        assertEquals(golden, res);
    }

    public void test115176HowTo() throws Exception {
        testFile = new File(getWorkDir(), "Test.java");
        TestUtilities.copyStringToFile(testFile, 
            "package hierbas.del.litoral;\n" +
            "\n" +
            "public class Test {\n" +
            "}\n"
            );
        String golden =
            "package hierbas.del.litoral;\n" +
            "\n" +
            "import java.util.List;\n" +
            "import java.util.Map;\n" +
            "\n" +
            "public class Test {\n" +
            "\n" +
            "    public List<Map<String, String>> foo() {\n" +
            "    }\n" +
            "}\n";

        JavaSource src = getJavaSource(testFile);
        Task<WorkingCopy> task = new Task<WorkingCopy>() {

            public void run(WorkingCopy workingCopy) throws IOException {
                workingCopy.toPhase(Phase.RESOLVED);
                CompilationUnitTree cut = workingCopy.getCompilationUnit();
                TreeMaker make = workingCopy.getTreeMaker();
                ClassTree classTree = (ClassTree) cut.getTypeDecls().get(0);
                TypeMirror mirror = workingCopy.getTreeUtilities().parseType(
                        "java.util.List<java.util.Map<String, String>>",
                        (TypeElement) workingCopy.getTrees().getElement(workingCopy.getTrees().getPath(cut, classTree))
                );
                MethodTree newMethod = make.Method(
                    make.Modifiers( 
                        Collections.singleton(Modifier.PUBLIC)
                    ),
                    "foo", // name
                    make.Type(mirror),
                    Collections.<TypeParameterTree>emptyList(), // type parameters for parameters
                    Collections.<VariableTree>emptyList(), // parameters
                    Collections.<ExpressionTree>emptyList(),
                    make.Block(Collections.<StatementTree>emptyList(), false),
                    null // default value - not applicable here, used by annotations
                );
                ClassTree copy = make.addClassMember(classTree, newMethod);
                workingCopy.rewrite(classTree, copy);
            }
            
        };
        src.runModificationTask(task).commit();
        String res = TestUtilities.copyFileToString(testFile);
        //System.err.println(res);
        assertEquals(golden, res);
    }

    public void testChangeToDiamond() throws Exception {
        testFile = new File(getWorkDir(), "Test.java");
        TestUtilities.copyStringToFile(testFile,
            "package hierbas.del.litoral;\n" +
            "import java.util.LinkedList;\n" +
            "public class Test {" +
            "    private Object o = new LinkedList<String>();\n" +
            "}\n"
            );
        String golden =
            "package hierbas.del.litoral;\n" +
            "import java.util.LinkedList;\n" +
            "public class Test {" +
            "    private Object o = new LinkedList<>();\n" +
            "}\n";

        JavaSource src = getJavaSource(testFile);
        Task<WorkingCopy> task = new Task<WorkingCopy>() {

            public void run(WorkingCopy workingCopy) throws IOException {
                workingCopy.toPhase(Phase.RESOLVED);
                CompilationUnitTree cut = workingCopy.getCompilationUnit();
                TreeMaker make = workingCopy.getTreeMaker();
                ClassTree classTree = (ClassTree) cut.getTypeDecls().get(0);
                VariableTree var = (VariableTree) classTree.getMembers().get(1);
                ParameterizedTypeTree ptt = (ParameterizedTypeTree) ((NewClassTree) var.getInitializer()).getIdentifier();

                workingCopy.rewrite(ptt, make.ParameterizedType(ptt.getType(), Collections.<Tree>emptyList()));
            }

        };
        src.runModificationTask(task).commit();
        String res = TestUtilities.copyFileToString(testFile);
        //System.err.println(res);
        assertEquals(golden, res);
    }

    public void test185306() throws Exception {
        testFile = new File(getWorkDir(), "Test.java");
        TestUtilities.copyStringToFile(testFile,
            "package hierbas.del.litoral;\n" +
            "import java.util.LinkedList;\n" +
            "public class Test {" +
            "    private LinkedList<?> o = null;\n" +
            "}\n"
            );
        String golden =
            "package hierbas.del.litoral;\n" +
            "import java.util.LinkedList;\n" +
            "public class Test {" +
            "    private LinkedList<?> o = null;\n" +
            "}\n";

        JavaSource src = getJavaSource(testFile);
        Task<WorkingCopy> task = new Task<WorkingCopy>() {

            public void run(WorkingCopy workingCopy) throws IOException {
                workingCopy.toPhase(Phase.RESOLVED);
                CompilationUnitTree cut = workingCopy.getCompilationUnit();
                TreeMaker make = workingCopy.getTreeMaker();
                ClassTree classTree = (ClassTree) cut.getTypeDecls().get(0);
                VariableTree var = (VariableTree) classTree.getMembers().get(1);
                ParameterizedTypeTree man = make.ParameterizedType(make.Identifier("LinkedList"), Collections.singletonList(make.Wildcard(Tree.Kind.UNBOUNDED_WILDCARD, make.Identifier("Object"))));

                workingCopy.rewrite(var.getType(), man);
            }

        };
        src.runModificationTask(task).commit();
        String res = TestUtilities.copyFileToString(testFile);
        //System.err.println(res);
        assertEquals(golden, res);
    }
    
    public void test158480() throws Exception {
        testFile = new File(getWorkDir(), "Test.java");
        TestUtilities.copyStringToFile(testFile,
            "package hierbas.del.litoral;\n" +
            "import java.util.LinkedList;\n" +
            "public class Test {" +
            "    private LinkedList<? super Integer> o = null;\n" +
            "}\n"
            );
        String golden =
            "package hierbas.del.litoral;\n" +
            "import java.util.LinkedList;\n" +
            "public class Test {" +
            "    private LinkedList<?> o = null;\n" +
            "}\n";

        JavaSource src = getJavaSource(testFile);
        Task<WorkingCopy> task = new Task<WorkingCopy>() {

            public void run(WorkingCopy workingCopy) throws IOException {
                workingCopy.toPhase(Phase.RESOLVED);
                CompilationUnitTree cut = workingCopy.getCompilationUnit();
                TreeMaker make = workingCopy.getTreeMaker();
                ClassTree classTree = (ClassTree) cut.getTypeDecls().get(0);
                VariableTree var = (VariableTree) classTree.getMembers().get(1);
                ParameterizedTypeTree type = (ParameterizedTypeTree) var.getType();
                Tree tp = type.getTypeArguments().get(0);

                workingCopy.rewrite(type.getTypeArguments().get(0), make.Wildcard(Kind.UNBOUNDED_WILDCARD, null));
            }

        };
        src.runModificationTask(task).commit();
        String res = TestUtilities.copyFileToString(testFile);
        //System.err.println(res);
        assertEquals(golden, res);
    }
    
    public void test221154a() throws Exception {
        testFile = new File(getWorkDir(), "Test.java");
        TestUtilities.copyStringToFile(testFile,
            "package hierbas.del.litoral;\n" +
            "import java.util.List;\n" +
            "public class Test {\n" +
            "    private List<Integer> o = null;\n" +
            "}\n"
            );
        String golden =
            "package hierbas.del.litoral;\n" +
            "import java.util.List;\n" +
            "public class Test {\n" +
            "    private List<?> o = null;\n" +
            "}\n";

        JavaSource src = getJavaSource(testFile);
        Task<WorkingCopy> task = new Task<WorkingCopy>() {

            public void run(WorkingCopy workingCopy) throws IOException {
                workingCopy.toPhase(Phase.RESOLVED);
                CompilationUnitTree cut = workingCopy.getCompilationUnit();
                TreeMaker make = workingCopy.getTreeMaker();
                ClassTree classTree = (ClassTree) cut.getTypeDecls().get(0);
                VariableTree var = (VariableTree) classTree.getMembers().get(1);
                ParameterizedTypeTree type = (ParameterizedTypeTree) var.getType();

                workingCopy.rewrite(type.getTypeArguments().get(0), make.Wildcard(Kind.UNBOUNDED_WILDCARD, null));
            }

        };
        src.runModificationTask(task).commit();
        String res = TestUtilities.copyFileToString(testFile);
        //System.err.println(res);
        assertEquals(golden, res);
    }
    
    public void test221154b() throws Exception {
        testFile = new File(getWorkDir(), "Test.java");
        TestUtilities.copyStringToFile(testFile,
            "package hierbas.del.litoral;\n" +
            "import java.util.List;\n" +
            "public class Test {\n" +
            "    private List<?> o = null;\n" +
            "}\n"
            );
        String golden =
            "package hierbas.del.litoral;\n" +
            "import java.util.List;\n" +
            "public class Test {\n" +
            "    private List<? extends Integer> o = null;\n" +
            "}\n";

        JavaSource src = getJavaSource(testFile);
        Task<WorkingCopy> task = new Task<WorkingCopy>() {

            public void run(WorkingCopy workingCopy) throws IOException {
                workingCopy.toPhase(Phase.RESOLVED);
                CompilationUnitTree cut = workingCopy.getCompilationUnit();
                TreeMaker make = workingCopy.getTreeMaker();
                ClassTree classTree = (ClassTree) cut.getTypeDecls().get(0);
                VariableTree var = (VariableTree) classTree.getMembers().get(1);
                ParameterizedTypeTree type = (ParameterizedTypeTree) var.getType();

                workingCopy.rewrite(type.getTypeArguments().get(0), make.Wildcard(Kind.EXTENDS_WILDCARD, make.Identifier("Integer")));
            }

        };
        src.runModificationTask(task).commit();
        String res = TestUtilities.copyFileToString(testFile);
        //System.err.println(res);
        assertEquals(golden, res);
    }
    
    public void testRemoveType229123a() throws Exception {
        testFile = new File(getWorkDir(), "Test.java");
        TestUtilities.copyStringToFile(testFile,
            "package hierbas.del.litoral;\n" +
            "import java.util.*;\n" +
            "public class Test {\n" +
            "    private Map<String, List<List<String>>> o = null;\n" +
            "}\n"
            );
        String golden =
            "package hierbas.del.litoral;\n" +
            "import java.util.*;\n" +
            "public class Test {\n" +
            "    private Map<String, List<String>> o = null;\n" +
            "}\n";

        JavaSource src = getJavaSource(testFile);
        Task<WorkingCopy> task = new Task<WorkingCopy>() {

            public void run(WorkingCopy workingCopy) throws IOException {
                workingCopy.toPhase(Phase.RESOLVED);
                CompilationUnitTree cut = workingCopy.getCompilationUnit();
                TreeMaker make = workingCopy.getTreeMaker();
                ClassTree classTree = (ClassTree) cut.getTypeDecls().get(0);
                VariableTree var = (VariableTree) classTree.getMembers().get(1);
                ParameterizedTypeTree type = (ParameterizedTypeTree) var.getType();

                workingCopy.rewrite(type, make.Type("java.util.Map<java.lang.String, java.util.List<java.lang.String>>"));
            }

        };
        src.runModificationTask(task).commit();
        String res = TestUtilities.copyFileToString(testFile);
        //System.err.println(res);
        assertEquals(golden, res);
    }
    
    public void testRemoveType229123b() throws Exception {
        testFile = new File(getWorkDir(), "Test.java");
        TestUtilities.copyStringToFile(testFile,
            "package hierbas.del.litoral;\n" +
            "import java.util.*;\n" +
            "public class Test {\n" +
            "    private Map<String, List<List<String>>> o = null;\n" +
            "}\n"
            );
        String golden =
            "package hierbas.del.litoral;\n" +
            "import java.util.*;\n" +
            "public class Test {\n" +
            "    private List<List<String>> o = null;\n" +
            "}\n";

        JavaSource src = getJavaSource(testFile);
        Task<WorkingCopy> task = new Task<WorkingCopy>() {

            public void run(WorkingCopy workingCopy) throws IOException {
                workingCopy.toPhase(Phase.RESOLVED);
                CompilationUnitTree cut = workingCopy.getCompilationUnit();
                TreeMaker make = workingCopy.getTreeMaker();
                ClassTree classTree = (ClassTree) cut.getTypeDecls().get(0);
                VariableTree var = (VariableTree) classTree.getMembers().get(1);
                ParameterizedTypeTree type = (ParameterizedTypeTree) var.getType();

                workingCopy.rewrite(type, make.Type("java.util.List<java.util.List<java.lang.String>>"));
            }

        };
        src.runModificationTask(task).commit();
        String res = TestUtilities.copyFileToString(testFile);
        //System.err.println(res);
        assertEquals(golden, res);
    }

    @Override
    String getGoldenPckg() {
        return "";
    }

    @Override
    String getSourcePckg() {
        return "";
    }
}
