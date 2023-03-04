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

package org.netbeans.modules.j2ee.ejbcore.action;

import java.io.IOException;
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
import org.netbeans.api.java.source.ElementHandle;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.Task;
import org.netbeans.api.java.source.WorkingCopy;
import org.netbeans.modules.j2ee.core.api.support.java.method.MethodModel;
import org.netbeans.modules.j2ee.deployment.common.api.ConfigurationException;
import org.netbeans.modules.j2ee.deployment.common.api.Datasource;
import org.netbeans.modules.j2ee.deployment.devmodules.spi.J2eeModuleProvider;
import org.netbeans.modules.j2ee.ejbcore.util._RetoucheUtil;
import org.netbeans.modules.j2ee.ejbcore.test.TestBase;
import org.openide.filesystems.FileObject;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.util.lookup.Lookups;

/**
 *
 * @author Martin Adamek
 */
public class UseDatabaseGeneratorTest extends TestBase {
    
    public UseDatabaseGeneratorTest(String testName) {
        super(testName);
    }
    
    public void testGenerate() throws IOException, ConfigurationException {
        UseDatabaseGenerator generator = new UseDatabaseGenerator();
        
        // EJB 2.1 Stateless Session Bean
        TestModule testModule = createEjb21Module();
        FileObject beanClass = testModule.getSources()[0].getFileObject("statelesslr/StatelessLRBean.java");
        Node node = new AbstractNode(Children.LEAF, Lookups.singleton(beanClass));
        final ElementHandle<TypeElement> elementHandle = _RetoucheUtil.getJavaClassFromNode(node);
        Datasource datasource = new DatasourceImpl();
        J2eeModuleProvider j2eeModuleProvider = testModule.getProject().getLookup().lookup(J2eeModuleProvider.class);
        generator.generate(beanClass, elementHandle.getQualifiedName(), j2eeModuleProvider, "referenceName", datasource, false, null);
        
        JavaSource javaSource = JavaSource.forFileObject(beanClass);
        javaSource.runModificationTask(new Task<WorkingCopy>() {
            public void run(WorkingCopy workingCopy) throws IOException {
                workingCopy.toPhase(JavaSource.Phase.ELEMENTS_RESOLVED);
                TypeElement typeElement = elementHandle.resolve(workingCopy);
                MethodModel methodModel = MethodModel.create(
                        "getReferenceName",
                        javax.sql.DataSource.class.getName(),
                        null,
                        Collections.<MethodModel.Variable>emptyList(),
                        Collections.singletonList(javax.naming.NamingException.class.getName()),
                        Collections.singleton(Modifier.PRIVATE)
                        );
                assertTrue(containsMethod(workingCopy, methodModel, typeElement));
            }
        });
        
        // EJB 3.0 Stateless Session Bean
        testModule = createEjb30Module();
        beanClass = testModule.getSources()[0].getFileObject("statelesslr/StatelessLRBean.java");
        node = new AbstractNode(Children.LEAF, Lookups.singleton(beanClass));
        final ElementHandle<TypeElement> elementHandle2 = _RetoucheUtil.getJavaClassFromNode(node);
        datasource = new DatasourceImpl();
        j2eeModuleProvider = testModule.getProject().getLookup().lookup(J2eeModuleProvider.class);
        generator.generate(beanClass, elementHandle2.getQualifiedName(), j2eeModuleProvider, "referenceName", datasource, false, null);
        
        javaSource = JavaSource.forFileObject(beanClass);
        javaSource.runModificationTask(new Task<WorkingCopy>() {
            public void run(WorkingCopy workingCopy) throws IOException {
                workingCopy.toPhase(JavaSource.Phase.ELEMENTS_RESOLVED);
                TypeElement typeElement = elementHandle2.resolve(workingCopy);
                checkDatasourceField(typeElement, "referenceName");
            }
        });
    }
    
    // private helpers =========================================================
    
    private static void checkDatasourceField(TypeElement typeElement, String name) {
        List<VariableElement> elements = ElementFilter.fieldsIn(typeElement.getEnclosedElements());
        VariableElement variableElement = elements.get(0);
        assertTrue(variableElement.getSimpleName().contentEquals(name)); // field name
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
        assertEquals(name, attributeValue);
    }

    private static class DatasourceImpl implements Datasource {
        
        public DatasourceImpl() {}
        
        public String getJndiName() { return "testJndiName"; }

        public String getUrl() { return "testUrl"; }

        public String getUsername() { return "testUsername"; }

        public String getPassword() { return "testPassword"; }

        public String getDriverClassName() { return "testDriverClassName"; }

        public String getDisplayName() { return "testDisplayName"; }
        
    }
    
}
