/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 */

package org.netbeans.modules.j2ee.core.api.support.java.method;

import org.netbeans.modules.j2ee.core.api.support.java.method.MethodModelSupport;
import org.netbeans.modules.j2ee.core.api.support.java.method.MethodModel;
import com.sun.source.tree.MethodTree;
import com.sun.source.tree.PrimitiveTypeTree;
import com.sun.source.tree.StatementTree;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.List;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.ArrayType;
import javax.lang.model.type.PrimitiveType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.util.ElementFilter;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import org.netbeans.api.java.source.CompilationController;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.ModificationResult;
import org.netbeans.api.java.source.Task;
import org.netbeans.api.java.source.WorkingCopy;
import org.netbeans.junit.MockServices;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.j2ee.core.api.support.java.FakeJavaDataLoaderPool;
import org.netbeans.modules.j2ee.core.api.support.java.SourceUtils;
import org.netbeans.modules.j2ee.core.api.support.java.TestUtilities;
import org.netbeans.modules.java.source.usages.IndexUtil;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

/**
 *
 * @author Martin Adamek
 */
public class MethodModelSupportTest extends NbTestCase {
    
    private FileObject testFO;

    public MethodModelSupportTest(String testName) {
        super(testName);
    }
    
    protected void setUp() throws Exception {
        MockServices.setServices(FakeJavaDataLoaderPool.class, RepositoryImpl.class);

        clearWorkDir();
        
        File file = new File(getWorkDir(),"cache");	//NOI18N
        FileUtil.createFolder(file);
        IndexUtil.setCacheFolder(file);

        FileObject workDir = FileUtil.toFileObject(getWorkDir());
        testFO = workDir.createData("TestClass.java");
    }
    
    public void testCreateMethodModel() throws Exception {
        TestUtilities.copyStringToFileObject(testFO,
                "package foo;" +
                "public class TestClass {" +
                "    private boolean method1() throws java.io.IOException {" +
                "        return false;" +
                "    }" +
                "    public static void method2(String name, int age, String[] interests) {" +
                "        return null;" +
                "    }" +
                "}");
        runUserActionTask(testFO, new Task<CompilationController>() {
            public void run(CompilationController controller) throws IOException {
                controller.toPhase(JavaSource.Phase.ELEMENTS_RESOLVED);
                TypeElement typeElement = SourceUtils.getPublicTopLevelElement(controller);
                for (ExecutableElement method : ElementFilter.methodsIn(typeElement.getEnclosedElements())) {
                    MethodModel methodModel = MethodModelSupport.createMethodModel(controller, method);
                    if (method.getSimpleName().contentEquals("method1")) {
                        assertEquals("boolean", methodModel.getReturnType());
                        assertEquals("method1", methodModel.getName());
                        assertTrue(methodModel.getModifiers().size() == 1);
                        assertTrue(methodModel.getModifiers().contains(Modifier.PRIVATE));
                        assertTrue(methodModel.getParameters().size() == 0);
                        assertTrue(methodModel.getExceptions().size() == 1);
                        assertTrue(methodModel.getExceptions().contains("java.io.IOException"));
                        //TODO: RETOUCHE test method body, see #90926
                    } else if (method.getSimpleName().contentEquals("method2")) {
                        assertEquals("void", methodModel.getReturnType());
                        assertEquals("method2", methodModel.getName());
                        assertTrue(methodModel.getModifiers().size() == 2);
                        assertTrue(methodModel.getModifiers().contains(Modifier.PUBLIC));
                        assertTrue(methodModel.getModifiers().contains(Modifier.STATIC));
                        MethodModel.Variable nameVariable = null;
                        MethodModel.Variable ageVariable = null;
                        MethodModel.Variable interestsVariable = null;
                        List<MethodModel.Variable> variables = methodModel.getParameters();
                        assertTrue(variables.size() == 3);
                        for (MethodModel.Variable variable : variables) {
                            if ("name".equals(variable.getName())) {
                                nameVariable = variable;
                            } else if ("age".equals(variable.getName())) {
                                ageVariable = variable;
                            } else if ("interests".equals(variable.getName())) {
                                interestsVariable = variable;
                            }
                        }
                        assertNotNull(nameVariable);
                        assertNotNull(ageVariable);
                        assertNotNull(interestsVariable);
                        assertEquals("java.lang.String", nameVariable.getType());
                        assertEquals("int", ageVariable.getType());
                        assertEquals("java.lang.String[]", interestsVariable.getType());
                        assertTrue(methodModel.getExceptions().size() == 0);
                        //TODO: RETOUCHE test method body, see #90926
                    }
                    
                }
            }
        });
    }
    
    public void testGetTypeName() throws Exception {
        TestUtilities.copyStringToFileObject(testFO,
                "package foo;" +
                "public class TestClass {" +
                "}");
        runUserActionTask(testFO, new Task<CompilationController>() {
            public void run(CompilationController controller) throws IOException {
                controller.toPhase(JavaSource.Phase.ELEMENTS_RESOLVED);
                Elements elements = controller.getElements();
                Types types = controller.getTypes();
                
                String typeName = String.class.getName();
                String resolvedTypeName = MethodModelSupport.getTypeName(elements.getTypeElement(typeName).asType());
                assertEquals(typeName, resolvedTypeName);
                
                typeName = InputStream.class.getName();
                resolvedTypeName = MethodModelSupport.getTypeName(elements.getTypeElement(typeName).asType());
                assertEquals(typeName, resolvedTypeName);
                
                resolvedTypeName = MethodModelSupport.getTypeName(types.getPrimitiveType(TypeKind.INT));
                assertEquals("int", resolvedTypeName);

                typeName = String.class.getName();
                resolvedTypeName = MethodModelSupport.getTypeName(types.getArrayType(elements.getTypeElement(typeName).asType()));
                assertEquals("java.lang.String[]", resolvedTypeName);
                
                PrimitiveType primitiveType = types.getPrimitiveType(TypeKind.BYTE);
                ArrayType arrayType = types.getArrayType(primitiveType);
                resolvedTypeName = MethodModelSupport.getTypeName(arrayType);
                assertEquals("byte[]", resolvedTypeName);
            }
        });
    }
    
    public void testCreateMethodTree() throws Exception {
        final MethodModel methodModel = MethodModel.create(
                "method",
                "void",
                "{ String name; }", // for now, Retouche requires those parenthesis (they won't appear in file)
                Collections.<MethodModel.Variable>emptyList(),
                Collections.<String>emptyList(),
                Collections.<Modifier>emptySet()
                );
        TestUtilities.copyStringToFileObject(testFO,
                "package foo;" +
                "public class TestClass {" +
                "}");
        runModificationTask(testFO, new Task<WorkingCopy>() {
            public void run(WorkingCopy workingCopy) throws IOException {
                workingCopy.toPhase(JavaSource.Phase.ELEMENTS_RESOLVED);
                MethodTree methodTree = MethodModelSupport.createMethodTree(workingCopy, methodModel);
                assertEquals(0, methodTree.getModifiers().getFlags().size());
                PrimitiveTypeTree returnTypeTree = (PrimitiveTypeTree) methodTree.getReturnType();
                assertTrue(TypeKind.VOID == returnTypeTree.getPrimitiveTypeKind());
                assertTrue(methodTree.getName().contentEquals("method"));
                assertEquals(0, methodTree.getParameters().size());
                assertEquals(0, methodTree.getThrows().size());
                List<? extends StatementTree> statements = methodTree.getBody().getStatements();
                assertEquals(1, statements.size());
            }
        });
    }
    
    public void testCreateVariable() throws Exception {
        TestUtilities.copyStringToFileObject(testFO,
                "package foo;" +
                "public class TestClass {" +
                "  private String name;" +
                "  private final String address;" +
                "}");
        runUserActionTask(testFO, new Task<CompilationController>() {
            public void run(CompilationController controller) throws IOException {
                controller.toPhase(JavaSource.Phase.ELEMENTS_RESOLVED);
                TypeElement typeElement = SourceUtils.getPublicTopLevelElement(controller);
                List<VariableElement> fields = ElementFilter.fieldsIn(typeElement.getEnclosedElements());
                MethodModel.Variable nonFinalVariable = MethodModelSupport.createVariable(controller, fields.get(0));
                assertEquals("java.lang.String", nonFinalVariable.getType());
                assertEquals("name", nonFinalVariable.getName());
                assertFalse(nonFinalVariable.getFinalModifier());
                MethodModel.Variable finalVariable = MethodModelSupport.createVariable(controller, fields.get(1));
                assertEquals("java.lang.String", finalVariable.getType());
                assertEquals("address", finalVariable.getName());
                assertTrue(finalVariable.getFinalModifier());
            }
        });
    }

    private static void runUserActionTask(FileObject javaFile, Task<CompilationController> taskToTest) throws IOException {
        JavaSource javaSource = JavaSource.forFileObject(javaFile);
        javaSource.runUserActionTask(taskToTest, true);
    }

    private static ModificationResult runModificationTask(FileObject javaFile, Task<WorkingCopy> taskToTest) throws IOException {
        JavaSource javaSource = JavaSource.forFileObject(javaFile);
        return javaSource.runModificationTask(taskToTest);
    }

}
