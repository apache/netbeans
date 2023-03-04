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

package org.netbeans.modules.j2ee.core.api.support.java;

import com.sun.source.tree.AnnotationTree;
import com.sun.source.tree.ClassTree;
import com.sun.source.tree.ExpressionTree;
import com.sun.source.tree.MethodTree;
import com.sun.source.tree.PrimitiveTypeTree;
import com.sun.source.tree.VariableTree;
import java.io.IOException;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.AnnotationValue;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import org.netbeans.api.java.source.CompilationController;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.JavaSource.Phase;
import org.netbeans.api.java.source.ModificationResult;
import org.netbeans.api.java.source.Task;
import org.netbeans.api.java.source.TreeMaker;
import org.netbeans.api.java.source.WorkingCopy;
import org.netbeans.junit.NbTestCase;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.test.MockLookup;

/**
 *
 * @author Andrei Badea
 */
public class GenerationUtilsTest extends NbTestCase {

    private FileObject workDir;
    private FileObject testFO;

    public GenerationUtilsTest(String testName) {
        super(testName);
    }

    @Override
    protected void setUp() throws Exception {
        MockLookup.init();
        clearWorkDir();
        TestUtilities.setCacheFolder(getWorkDir());
        workDir = FileUtil.toFileObject(getWorkDir());
        testFO = workDir.createData("TestClass.java");

        MockLookup.setInstances(
                new ClassPathProviderImpl(new FileObject[] { workDir }),
                new FakeJavaDataLoaderPool(),
                new SourceLevelQueryImpl());
        initTemplates();

    }

    private void initTemplates() throws Exception{
        FileObject interfaceTemplate = FileUtil.createData(FileUtil.getConfigRoot(), "Templates/Classes/Interface.java");
        interfaceTemplate.setAttribute("javax.script.ScriptEngine", "freemarker");
        TestUtilities.copyStringToFileObject(interfaceTemplate,
                "package ${package};" +
                "public interface ${name} {\n" +
                "}");
        FileObject classTemplate = FileUtil.createData(FileUtil.getConfigRoot(), "Templates/Classes/Class.java");
        classTemplate.setAttribute("javax.script.ScriptEngine", "freemarker");
        TestUtilities.copyStringToFileObject(classTemplate,
                "package ${package};" +
                "public class ${name} {\n" +
                "   public ${name}(){\n" +
                "   }\n" +
                "}");
    }

    public void testCreateClass() throws Exception {
        FileObject javaFO = GenerationUtils.createClass(workDir, "NewTestClass", "Javadoc");
        runUserActionTask(javaFO, new Task<CompilationController>() {
            public void run(CompilationController controller) throws IOException {
                TypeElement typeElement = SourceUtils.getPublicTopLevelElement(controller);
                assertEquals(ElementKind.CLASS, typeElement.getKind());
                assertTrue(SourceUtils.getNoArgConstructor(controller, typeElement) != null);
                // TODO assert for Javadoc
            }
        });
    }

    public void testCreateInterface() throws Exception {
        FileObject javaFO = GenerationUtils.createInterface(workDir, "NewTestClass", "Javadoc");
        runUserActionTask(javaFO, new Task<CompilationController>() {
            public void run(CompilationController controller) throws IOException {
                TypeElement typeElement = SourceUtils.getPublicTopLevelElement(controller);
                assertEquals(ElementKind.INTERFACE, typeElement.getKind());
                // TODO assert for Javadoc
            }
        });
    }

    public void testEnsureNoArgConstructor() throws Exception {
        TestUtilities.copyStringToFileObject(testFO,
                "package foo;" +
                "public class TestClass {" +
                "}");
        runModificationTask(testFO, new Task<WorkingCopy>() {
            public void run(WorkingCopy copy) throws Exception {
                GenerationUtils genUtils = GenerationUtils.newInstance(copy);
                ClassTree classTree = (ClassTree)copy.getCompilationUnit().getTypeDecls().get(0);
                ClassTree newClassTree = genUtils.ensureNoArgConstructor(classTree);
                copy.rewrite(classTree, newClassTree);
            }
        }).commit();
        runUserActionTask(testFO, new Task<CompilationController>() {
            public void run(CompilationController controller) throws Exception {
                TypeElement typeElement = SourceUtils.getPublicTopLevelElement(controller);
                assertTrue(SourceUtils.getNoArgConstructor(controller, typeElement) != null);
            }
        });
    }

    public void testEnsureNoArgConstructorMakesConstructorPublic() throws Exception {
        TestUtilities.copyStringToFileObject(testFO,
                "package foo;" +
                "public class TestClass {" +
                "    private TestClass() {" +
                "    }" +
                "}");
        runModificationTask(testFO, new Task<WorkingCopy>() {
            public void run(WorkingCopy copy) throws Exception {
                GenerationUtils genUtils = GenerationUtils.newInstance(copy);
                ClassTree classTree = (ClassTree)copy.getCompilationUnit().getTypeDecls().get(0);
                ClassTree newClassTree = genUtils.ensureNoArgConstructor(classTree);
                copy.rewrite(classTree, newClassTree);
            }
        }).commit();
        runUserActionTask(testFO, new Task<CompilationController>() {
            public void run(CompilationController controller) throws Exception {
                TypeElement typeElement = SourceUtils.getPublicTopLevelElement(controller);
                assertTrue(SourceUtils.getNoArgConstructor(controller, typeElement).getModifiers().contains(Modifier.PUBLIC));
            }
        });
    }

    public void testPrimitiveTypes() throws Exception {
        TestUtilities.copyStringToFileObject(testFO,
                "package foo;" +
                "public class TestClass {" +
                "}");
        runModificationTask(testFO, new Task<WorkingCopy>() {
            public void run(WorkingCopy copy) throws Exception {
                GenerationUtils genUtils = GenerationUtils.newInstance(copy);
                TypeElement scope = SourceUtils.getPublicTopLevelElement(copy);
                assertEquals(TypeKind.BOOLEAN, ((PrimitiveTypeTree)genUtils.createType("boolean", scope)).getPrimitiveTypeKind());
                assertEquals(TypeKind.BYTE, ((PrimitiveTypeTree)genUtils.createType("byte", scope)).getPrimitiveTypeKind());
                assertEquals(TypeKind.SHORT, ((PrimitiveTypeTree)genUtils.createType("short", scope)).getPrimitiveTypeKind());
                assertEquals(TypeKind.INT, ((PrimitiveTypeTree)genUtils.createType("int", scope)).getPrimitiveTypeKind());
                assertEquals(TypeKind.LONG, ((PrimitiveTypeTree)genUtils.createType("long", scope)).getPrimitiveTypeKind());
                assertEquals(TypeKind.CHAR, ((PrimitiveTypeTree)genUtils.createType("char", scope)).getPrimitiveTypeKind());
                assertEquals(TypeKind.FLOAT, ((PrimitiveTypeTree)genUtils.createType("float", scope)).getPrimitiveTypeKind());
                assertEquals(TypeKind.DOUBLE, ((PrimitiveTypeTree)genUtils.createType("double", scope)).getPrimitiveTypeKind());
            }
        });
    }

    public void testCreateAnnotation() throws Exception {
        TestUtilities.copyStringToFileObject(testFO,
                "package foo;" +
                "public class TestClass {" +
                "}");
        runModificationTask(testFO, new Task<WorkingCopy>() {
            public void run(WorkingCopy copy) throws Exception {
                GenerationUtils genUtils = GenerationUtils.newInstance(copy);
                ClassTree classTree = (ClassTree)copy.getCompilationUnit().getTypeDecls().get(0);
                AnnotationTree annotationTree = genUtils.createAnnotation("java.lang.SuppressWarnings",
                        Collections.singletonList(genUtils.createAnnotationArgument(null, "unchecked")));
                ClassTree newClassTree = genUtils.addAnnotation(classTree, annotationTree);
                annotationTree = genUtils.createAnnotation("java.lang.annotation.Retention",
                        Collections.singletonList(genUtils.createAnnotationArgument(null, "java.lang.annotation.RetentionPolicy", "RUNTIME")));
                newClassTree = genUtils.addAnnotation(newClassTree, annotationTree);
                copy.rewrite(classTree, newClassTree);
            }
        }).commit();
        runUserActionTask(testFO, new Task<CompilationController>() {
            public void run(CompilationController controller) throws Exception {
                TypeElement typeElement = SourceUtils.getPublicTopLevelElement(controller);
                assertEquals(2, typeElement.getAnnotationMirrors().size());
                SuppressWarnings suppressWarnings = typeElement.getAnnotation(SuppressWarnings.class);
                assertNotNull(suppressWarnings);
                assertEquals(1, suppressWarnings.value().length);
                assertEquals("unchecked", suppressWarnings.value()[0]);
                Retention retention = typeElement.getAnnotation(Retention.class);
                assertNotNull(retention);
                assertEquals(RetentionPolicy.RUNTIME, retention.value());
            }
        });
    }

    @SuppressWarnings("unchecked")
    public void testCreateAnnotationArrayArgument() throws Exception {
        TestUtilities.copyStringToFileObject(testFO,
                "package foo;" +
                "@interface NamedQueries {" +
                "   NamedQuery[] value();" +
                "}" +
                "@interface NamedQuery {" +
                "   String name();" +
                "   String query();" +
                "}" +
                "public class TestClass {" +
                "}");
        runModificationTask(testFO, new Task<WorkingCopy>() {
            public void run(WorkingCopy copy) throws Exception {
                GenerationUtils genUtils = GenerationUtils.newInstance(copy);
                ClassTree classTree = SourceUtils.getPublicTopLevelTree(copy);
                ExpressionTree namedQueryAnnotation0 = genUtils.createAnnotation("foo.NamedQuery", Arrays.asList(
                        genUtils.createAnnotationArgument("name", "foo0"),
                        genUtils.createAnnotationArgument("query", "q0")));
                ExpressionTree namedQueryAnnotation1 = genUtils.createAnnotation("foo.NamedQuery", Arrays.asList(
                        genUtils.createAnnotationArgument("name", "foo1"),
                        genUtils.createAnnotationArgument("query", "q1")));
                ExpressionTree namedQueriesAnnValue = genUtils.createAnnotationArgument("value", Arrays.asList(namedQueryAnnotation0, namedQueryAnnotation1));
                AnnotationTree namedQueriesAnnotation = genUtils.createAnnotation("foo.NamedQueries", Collections.singletonList(namedQueriesAnnValue));
                ClassTree newClassTree = genUtils.addAnnotation(classTree, namedQueriesAnnotation);
                copy.rewrite(classTree, newClassTree);
            }
        }).commit();
        runUserActionTask(testFO, new Task<CompilationController>() {
            public void run(CompilationController controller) throws Exception {
                TypeElement typeElement = SourceUtils.getPublicTopLevelElement(controller);
                List<? extends AnnotationMirror> annotations = typeElement.getAnnotationMirrors();
                Map<? extends ExecutableElement, ? extends AnnotationValue> namedQueriesAnnElements = annotations.get(0).getElementValues();
                List<? extends AnnotationMirror> namedQueriesAnnValue = (List<? extends AnnotationMirror>)namedQueriesAnnElements.values().iterator().next().getValue();
                assertEquals(2, namedQueriesAnnValue.size());
                int outer = 0;
                for (AnnotationMirror namedQueryAnn : namedQueriesAnnValue) {
                    int inner = 0;
                    for (Map.Entry<? extends ExecutableElement, ? extends AnnotationValue> namedQueryAnnElement : namedQueryAnn.getElementValues().entrySet()) {
                        String namedQueryAnnElementName = namedQueryAnnElement.getKey().getSimpleName().toString();
                        String namedQueryAnnElementValue = (String)namedQueryAnnElement.getValue().getValue();
                        switch (inner) {
                            case 0:
                                assertEquals("name", namedQueryAnnElementName);
                                assertEquals("foo" + outer, namedQueryAnnElementValue);
                                break;
                            case 1:
                                assertEquals("query", namedQueryAnnElementName);
                                assertEquals("q" + outer, namedQueryAnnElementValue);
                                break;
                            default:
                                fail();
                        }
                        inner++;
                    }
                    outer++;
                }
            }
        });
    }

    public void testCreateAnnotationBooleanArgumentIssue89230() throws Exception {
        TestUtilities.copyStringToFileObject(testFO,
                "package foo;" +
                "@interface Column {" +
                "   boolean nullable();" +
                "}" +
                "public class TestClass {" +
                "}");
        runModificationTask(testFO, new Task<WorkingCopy>() {
            public void run(WorkingCopy copy) throws Exception {
                GenerationUtils genUtils = GenerationUtils.newInstance(copy);
                ClassTree classTree = SourceUtils.getPublicTopLevelTree(copy);
                AnnotationTree annotationTree = genUtils.createAnnotation("foo.Column", Collections.singletonList(genUtils.createAnnotationArgument("nullable", true)));
                ClassTree newClassTree = genUtils.addAnnotation(classTree, annotationTree);
                copy.rewrite(classTree, newClassTree);
            }
        }).commit();
        runUserActionTask(testFO, new Task<CompilationController>() {
            public void run(CompilationController controller) throws Exception {
                TypeElement typeElement = SourceUtils.getPublicTopLevelElement(controller);
                assertEquals(1, typeElement.getAnnotationMirrors().size());
                AnnotationMirror columnAnn = typeElement.getAnnotationMirrors().get(0);
                assertEquals(1, columnAnn.getElementValues().size());
                Map.Entry<? extends ExecutableElement, ? extends AnnotationValue> columnAnnNullableElement = columnAnn.getElementValues().entrySet().iterator().next();
                assertEquals("nullable", columnAnnNullableElement.getKey().getSimpleName().toString());
                assertEquals(true, columnAnn.getElementValues().values().iterator().next().getValue());
            }
        });
    }

    public void testCreateAnnotationArgumentWithNullName() throws Exception {
        FileObject annotationFO = workDir.createData("Annotations.java");
        TestUtilities.copyStringToFileObject(testFO,
                "package foo;" +
                "public class TestClass {" +
                "}");
        runModificationTask(testFO, new Task<WorkingCopy>() {
            public void run(WorkingCopy copy) throws Exception {
                GenerationUtils genUtils = GenerationUtils.newInstance(copy);
                ClassTree classTree = (ClassTree)copy.getCompilationUnit().getTypeDecls().get(0);
                AnnotationTree annWithLiteralArgument = genUtils.createAnnotation("java.lang.SuppressWarnings",
                        Collections.singletonList(genUtils.createAnnotationArgument(null, "unchecked")));
                AnnotationTree annWithArrayArgument = genUtils.createAnnotation("java.lang.annotation.Target",
                        Collections.singletonList(genUtils.createAnnotationArgument(null, Collections.<ExpressionTree>emptyList())));
                AnnotationTree annWithMemberSelectArgument = genUtils.createAnnotation("java.lang.annotation.Retention",
                        Collections.singletonList(genUtils.createAnnotationArgument(null, "java.lang.annotation.RetentionPolicy", "RUNTIME")));
                ClassTree newClassTree = genUtils.addAnnotation(classTree, annWithLiteralArgument);
                newClassTree = genUtils.addAnnotation(newClassTree, annWithArrayArgument);
                newClassTree = genUtils.addAnnotation(newClassTree, annWithMemberSelectArgument);
                copy.rewrite(classTree, newClassTree);
            }
        }).commit();
        assertFalse(TestUtilities.copyFileObjectToString(testFO).contains("value"));
    }

    public void testCreateProperty() throws Exception {
        TestUtilities.copyStringToFileObject(testFO,
                "package foo;" +
                "public class TestClass {" +
                "   private Object x;" +
                "   public TestClass() {" +
                "   }" +
                "}");
        runModificationTask(testFO, new Task<WorkingCopy>() {
            public void run(WorkingCopy copy) throws Exception {
                GenerationUtils genUtils = GenerationUtils.newInstance(copy);
                ClassTree classTree = (ClassTree)copy.getCompilationUnit().getTypeDecls().get(0);
                TypeElement scope = SourceUtils.classTree2TypeElement(copy, classTree);
                VariableTree field = genUtils.createField(scope, genUtils.createModifiers(Modifier.PRIVATE), "someProp", "java.lang.String", null);
                MethodTree getter = genUtils.createPropertyGetterMethod(scope, genUtils.createModifiers(Modifier.PUBLIC), "someProp", "java.lang.String");
                MethodTree setter = genUtils.createPropertySetterMethod(scope, genUtils.createModifiers(Modifier.PUBLIC), "someProp", "java.lang.String");
                TreeMaker make = copy.getTreeMaker();
                ClassTree newClassTree = classTree;
                newClassTree = make.insertClassMember(newClassTree, 0, field);
                newClassTree = make.addClassMember(newClassTree, getter);
                newClassTree = make.addClassMember(newClassTree, setter);
                copy.rewrite(classTree, newClassTree);
            }
        }).commit();
        // TODO check the field and methods
    }

    public void testAddImplementsClause() throws Exception {
        TestUtilities.copyStringToFileObject(testFO,
                "package foo;" +
                "public class TestClass {" +
                "}");
        runModificationTask(testFO, new Task<WorkingCopy>() {
            public void run(WorkingCopy copy) throws Exception {
                GenerationUtils genUtils = GenerationUtils.newInstance(copy);
                ClassTree classTree = (ClassTree)copy.getCompilationUnit().getTypeDecls().get(0);
                ClassTree newClassTree = genUtils.addImplementsClause(classTree, "java.io.Serializable");
                newClassTree = genUtils.addImplementsClause(newClassTree, "java.lang.Cloneable");
                copy.rewrite(classTree, newClassTree);
            }
        }).commit();
        runUserActionTask(testFO, new Task<CompilationController>() {
            public void run(CompilationController controller) throws Exception {
                TypeElement typeElement = SourceUtils.getPublicTopLevelElement(controller);
                assertImplements(controller, typeElement, "java.io.Serializable");
                assertImplements(controller, typeElement, "java.lang.Cloneable");
            }
        });
    }

    public void testCreateType() throws Exception {
        TestUtilities.copyStringToFileObject(testFO,
                "package foo;" +
                "public class TestClass {" +
                "}");
        runModificationTask(testFO, new Task<WorkingCopy>() {
            public void run(WorkingCopy copy) throws Exception {
                GenerationUtils genUtils = GenerationUtils.newInstance(copy);
                TypeElement scope = SourceUtils.getPublicTopLevelElement(copy);
                assertNotNull(genUtils.createType("byte[]", scope));
            }
        });
    }

    private static void runUserActionTask(FileObject javaFile, final Task<CompilationController> taskToTest) throws Exception {
        JavaSource javaSource = JavaSource.forFileObject(javaFile);
        javaSource.runUserActionTask(new Task<CompilationController>() {
            public void run(CompilationController controller) throws Exception {
                controller.toPhase(Phase.RESOLVED);
                taskToTest.run(controller);
            }
        }, true);
    }

    private static ModificationResult runModificationTask(FileObject javaFile, final Task<WorkingCopy> taskToTest) throws Exception {
        JavaSource javaSource = JavaSource.forFileObject(javaFile);
        return javaSource.runModificationTask(new Task<WorkingCopy>() {
            public void run(WorkingCopy controller) throws Exception {
                controller.toPhase(Phase.RESOLVED);
                taskToTest.run(controller);
            }
        });
    }

    private static void assertImplements(CompilationController controller, TypeElement typeElement, String interfaceName) {
        TypeMirror interfaceType = controller.getElements().getTypeElement("java.io.Serializable").asType();
        for (TypeMirror type : typeElement.getInterfaces()) {
            if (controller.getTypes().isSameType(interfaceType, type)) {
                return;
            }
        }
        fail("Type " + typeElement + " does not implement " + interfaceName);
    }
}
