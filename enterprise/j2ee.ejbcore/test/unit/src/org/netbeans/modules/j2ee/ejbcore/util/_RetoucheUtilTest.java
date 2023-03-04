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

package org.netbeans.modules.j2ee.ejbcore.util;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.AnnotationValue;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.util.ElementFilter;
import org.netbeans.api.java.source.CompilationController;
import org.netbeans.api.java.source.ElementHandle;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.Task;
import org.netbeans.modules.j2ee.core.api.support.java.SourceUtils;
import org.netbeans.modules.j2ee.ejbcore.test.TestBase;
import org.netbeans.modules.j2ee.ejbcore.test.TestUtilities;
import org.openide.filesystems.FileObject;

/**
 *
 * @author Martin Adamek
 */
public class _RetoucheUtilTest extends TestBase{
    
    public _RetoucheUtilTest(String testName) {
        super(testName);
    }

    public void testGenerateInjectedField() throws IOException {
        // creates and tests following field:
        // @javax.annotation.Resource(name="MyJndiName")
        // javax.sql.DataSource myResource;
        TestUtilities.copyStringToFileObject(testFO,
                "package foo;" +
                "public class TestClass {" +
                "}");
        _RetoucheUtil.generateAnnotatedField(
                testFO,
                "foo.TestClass",
                "javax.annotation.Resource", 
                "myResource", 
                "javax.sql.DataSource", 
                Collections.singletonMap("name", "MyJndiName"), 
                false);
        testAddedField(testFO, false);

        // creates and tests following field:
        // @javax.annotation.Resource(name="MyJndiName")
        // static javax.sql.DataSource myResource;
        TestUtilities.copyStringToFileObject(testFO,
                "package foo;" +
                "public class TestClass {" +
                "}");
        _RetoucheUtil.generateAnnotatedField(
                testFO,
                "foo.TestClass",
                "javax.annotation.Resource", 
                "myResource", 
                "javax.sql.DataSource", 
                Collections.singletonMap("name", "MyJndiName"), 
                true);
        testAddedField(testFO, true);
    }
    
    public void testIsInterface() throws IOException {
        TestUtilities.copyStringToFileObject(testFO,
                "package foo;" +
                "public class TestClass {" +
                "}");
        final List<ElementHandle<TypeElement>> result1 = new ArrayList<ElementHandle<TypeElement>>(1);
        JavaSource javaSource = JavaSource.forFileObject(testFO);
        javaSource.runUserActionTask(new Task<CompilationController>() {
            public void run(CompilationController controller) throws IOException {
                controller.toPhase(JavaSource.Phase.ELEMENTS_RESOLVED);
                TypeElement typeElement = SourceUtils.getPublicTopLevelElement(controller);
                result1.add(ElementHandle.create(typeElement));
            }
        }, true);
        assertFalse(_RetoucheUtil.isInterface(testFO, result1.get(0)));

        TestUtilities.copyStringToFileObject(testFO,
                "package foo;" +
                "public interface TestClass {" +
                "}");
        final List<ElementHandle<TypeElement>> result2 = new ArrayList<ElementHandle<TypeElement>>(1);
        javaSource = JavaSource.forFileObject(testFO);
        javaSource.runUserActionTask(new Task<CompilationController>() {
            public void run(CompilationController controller) throws IOException {
                controller.toPhase(JavaSource.Phase.ELEMENTS_RESOLVED);
                TypeElement typeElement = SourceUtils.getPublicTopLevelElement(controller);
                result2.add(ElementHandle.create(typeElement));
            }
        }, true);
        assertTrue(_RetoucheUtil.isInterface(testFO, result2.get(0)));
    }
    
    private void testAddedField(FileObject fileObject, final boolean isStatic) throws IOException {
        JavaSource javaSource = JavaSource.forFileObject(fileObject);
        javaSource.runUserActionTask(new Task<CompilationController>() {
            public void run(CompilationController controller) throws IOException {
                controller.toPhase(JavaSource.Phase.ELEMENTS_RESOLVED);
                TypeElement typeElement = SourceUtils.getPublicTopLevelElement(controller);
                List<VariableElement> elements = ElementFilter.fieldsIn(typeElement.getEnclosedElements());
                VariableElement variableElement = (VariableElement) elements.get(0);
                assertEquals(isStatic, variableElement.getModifiers().contains(Modifier.STATIC));
                assertTrue(variableElement.getSimpleName().contentEquals("myResource")); // field name
                DeclaredType declaredType = (DeclaredType) variableElement.asType();
                TypeElement returnTypeElement = (TypeElement) declaredType.asElement();
                assertTrue(returnTypeElement.getQualifiedName().contentEquals("javax.sql.DataSource")); // field type
                AnnotationMirror annotationMirror = variableElement.getAnnotationMirrors().get(0);
                DeclaredType annotationDeclaredType = annotationMirror.getAnnotationType();
                TypeElement annotationTypeElement = (TypeElement) annotationDeclaredType.asElement();
                assertTrue(annotationTypeElement.getQualifiedName().contentEquals("javax.annotation.Resource")); // annotation type
                Map.Entry<? extends ExecutableElement, ? extends AnnotationValue> entry = annotationMirror.getElementValues().entrySet().iterator().next();
                String attributeName = entry.getKey().getSimpleName().toString();
                String attributeValue = (String) entry.getValue().getValue();
                assertEquals("name", attributeName); // attributes
                assertEquals("MyJndiName", attributeValue);
            }
        }, true);
    }

}
