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
