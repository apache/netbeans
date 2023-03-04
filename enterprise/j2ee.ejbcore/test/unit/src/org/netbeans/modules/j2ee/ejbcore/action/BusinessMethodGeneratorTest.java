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

import com.sun.source.tree.MethodTree;
import java.io.IOException;
import java.util.Collections;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.ElementFilter;
import org.netbeans.api.java.source.CompilationController;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.WorkingCopy;
import org.netbeans.api.java.source.Task;
import org.netbeans.modules.j2ee.core.api.support.java.method.MethodModel;
import org.netbeans.modules.j2ee.core.api.support.java.method.MethodModelSupport;
import org.netbeans.modules.j2ee.dd.api.ejb.DDProvider;
import org.netbeans.modules.j2ee.dd.api.ejb.EjbJar;
import org.netbeans.modules.j2ee.ejbcore.test.TestBase;
import org.openide.filesystems.FileObject;

/**
 *
 * @author Martin Adamek
 */
public class BusinessMethodGeneratorTest extends TestBase {
    
    public BusinessMethodGeneratorTest(String testName) {
        super(testName);
    }
    
    public void testGenerateEE14() throws IOException {
        TestModule testModule = createEjb21Module();
        
        // add business method into local and remote interfaces of stateless session EJB 
        FileObject beanClass = testModule.getSources()[0].getFileObject("statelesslr/StatelessLRBean.java");
        EjbJar ejbJar = DDProvider.getDefault().getDDRoot(testModule.getDeploymentDescriptor());
        BusinessMethodGenerator generator = BusinessMethodGenerator.create("statelesslr.StatelessLRBean", beanClass);
        final MethodModel methodModel = MethodModel.create(
                "businessMethodTest",
                "java.lang.String",
                "return null;",
                Collections.<MethodModel.Variable>emptyList(),
                Collections.<String>emptyList(),
                Collections.<Modifier>emptySet()
                );
        generator.generate(methodModel, true, true);

        // ejb class
        final boolean[] found = new boolean[] { false };
        JavaSource javaSource = JavaSource.forFileObject(beanClass);
        javaSource.runUserActionTask(new Task<CompilationController>() {
            public void run(CompilationController controller) throws IOException {
                controller.toPhase(JavaSource.Phase.ELEMENTS_RESOLVED);
                MethodModel ejbClassMethodModel = MethodModel.create(
                        methodModel.getName(),
                        methodModel.getReturnType(),
                        methodModel.getBody(),
                        methodModel.getParameters(),
                        methodModel.getExceptions(),
                        Collections.singleton(Modifier.PUBLIC)
                        );
                TypeElement typeElement = controller.getElements().getTypeElement("statelesslr.StatelessLRBean");
                for (ExecutableElement executableElement : ElementFilter.methodsIn(typeElement.getEnclosedElements())) {
                    if (MethodModelSupport.isSameMethod(controller, executableElement, ejbClassMethodModel)) {
                        found[0] = true;
                    }
                }
            }
        }, true);
        assertTrue(found[0]);
        
        // local interface
        found[0] = false;
        FileObject interfaceFileObject = testModule.getSources()[0].getFileObject("statelesslr/StatelessLRLocal.java");
        javaSource = JavaSource.forFileObject(interfaceFileObject);
        javaSource.runModificationTask(new Task<WorkingCopy>() {
            public void run(WorkingCopy workingCopy) throws IOException {
                workingCopy.toPhase(JavaSource.Phase.ELEMENTS_RESOLVED);
                TypeElement typeElement = workingCopy.getElements().getTypeElement("statelesslr.StatelessLRLocal");
                for (ExecutableElement executableElement : ElementFilter.methodsIn(typeElement.getEnclosedElements())) {
                    if (MethodModelSupport.isSameMethod(workingCopy, executableElement, methodModel)) {
                        MethodTree methodTree = workingCopy.getTrees().getTree(executableElement);
                        assertNull(methodTree.getBody());
                        found[0] = true;
                    }
                }
            }
        });
        assertTrue(found[0]);
        
        // remote interface
        found[0] = false;
        interfaceFileObject = testModule.getSources()[0].getFileObject("statelesslr/StatelessLRRemote.java");
        javaSource = JavaSource.forFileObject(interfaceFileObject);
        javaSource.runModificationTask(new Task<WorkingCopy>() {
            public void run(WorkingCopy workingCopy) throws IOException {
                workingCopy.toPhase(JavaSource.Phase.ELEMENTS_RESOLVED);
                MethodModel interfaceMethodModel = MethodModel.create(
                        methodModel.getName(),
                        methodModel.getReturnType(),
                        methodModel.getBody(),
                        methodModel.getParameters(),
                        Collections.singletonList("java.rmi.RemoteException"),
                        methodModel.getModifiers()
                        );
                TypeElement typeElement = workingCopy.getElements().getTypeElement("statelesslr.StatelessLRRemote");
                for (ExecutableElement executableElement : ElementFilter.methodsIn(typeElement.getEnclosedElements())) {
                    if (MethodModelSupport.isSameMethod(workingCopy, executableElement, interfaceMethodModel)) {
                        MethodTree methodTree = workingCopy.getTrees().getTree(executableElement);
                        assertNull(methodTree.getBody());
                        assertTrue(containsType(workingCopy, executableElement.getThrownTypes(), "java.rmi.RemoteException"));
                        found[0] = true;
                    }
                }
            }
        });
        assertTrue(found[0]);
        
    }
    
    public void testGenerateEE5() throws IOException {
        TestModule testModule = createEjb30Module();
        
        // add business method into local and remote interfaces of stateless session EJB 
        FileObject beanClass = testModule.getSources()[0].getFileObject("statelesslr/StatelessLRBean.java");
        EjbJar ejbJar = DDProvider.getDefault().getDDRoot(testModule.getDeploymentDescriptor());
        BusinessMethodGenerator generator = BusinessMethodGenerator.create("statelesslr.StatelessLRBean", beanClass);
        final MethodModel methodModel = MethodModel.create(
                "businessMethodTest",
                "java.lang.String",
                "return null;",
                Collections.<MethodModel.Variable>emptyList(),
                Collections.<String>emptyList(),
                Collections.<Modifier>emptySet()
                );
        generator.generate(methodModel, true, true);

        // ejb class
        final boolean[] found = new boolean[] { false };
        JavaSource javaSource = JavaSource.forFileObject(beanClass);
        javaSource.runUserActionTask(new Task<CompilationController>() {
            public void run(CompilationController controller) throws IOException {
                controller.toPhase(JavaSource.Phase.ELEMENTS_RESOLVED);
                MethodModel ejbClassMethodModel = MethodModel.create(
                        methodModel.getName(),
                        methodModel.getReturnType(),
                        methodModel.getBody(),
                        methodModel.getParameters(),
                        methodModel.getExceptions(),
                        Collections.singleton(Modifier.PUBLIC)
                        );
                TypeElement typeElement = controller.getElements().getTypeElement("statelesslr.StatelessLRBean");
                for (ExecutableElement executableElement : ElementFilter.methodsIn(typeElement.getEnclosedElements())) {
                    if (MethodModelSupport.isSameMethod(controller, executableElement, ejbClassMethodModel)) {
                        found[0] = true;
                    }
                }
            }
        }, true);
        assertTrue(found[0]);
        
        // local interface
        found[0] = false;
        FileObject interfaceFileObject = testModule.getSources()[0].getFileObject("statelesslr/StatelessLRLocal.java");
        javaSource = JavaSource.forFileObject(interfaceFileObject);
        javaSource.runModificationTask(new Task<WorkingCopy>() {
            public void run(WorkingCopy workingCopy) throws IOException {
                workingCopy.toPhase(JavaSource.Phase.ELEMENTS_RESOLVED);
                TypeElement typeElement = workingCopy.getElements().getTypeElement("statelesslr.StatelessLRLocal");
                for (ExecutableElement executableElement : ElementFilter.methodsIn(typeElement.getEnclosedElements())) {
                    if (MethodModelSupport.isSameMethod(workingCopy, executableElement, methodModel)) {
                        MethodTree methodTree = workingCopy.getTrees().getTree(executableElement);
                        assertNull(methodTree.getBody());
                        found[0] = true;
                    }
                }
            }
        });
        assertTrue(found[0]);
        
        // remote interface
        found[0] = false;
        interfaceFileObject = testModule.getSources()[0].getFileObject("statelesslr/StatelessLRRemote.java");
        javaSource = JavaSource.forFileObject(interfaceFileObject);
        javaSource.runModificationTask(new Task<WorkingCopy>() {
            public void run(WorkingCopy workingCopy) throws IOException {
                workingCopy.toPhase(JavaSource.Phase.ELEMENTS_RESOLVED);
                MethodModel interfaceMethodModel = MethodModel.create(
                        methodModel.getName(),
                        methodModel.getReturnType(),
                        methodModel.getBody(),
                        methodModel.getParameters(),
                        Collections.singletonList("java.rmi.RemoteException"),
                        methodModel.getModifiers()
                        );
                TypeElement typeElement = workingCopy.getElements().getTypeElement("statelesslr.StatelessLRRemote");
                for (ExecutableElement executableElement : ElementFilter.methodsIn(typeElement.getEnclosedElements())) {
                    if (MethodModelSupport.isSameMethod(workingCopy, executableElement, interfaceMethodModel)) {
                        MethodTree methodTree = workingCopy.getTrees().getTree(executableElement);
                        assertNull(methodTree.getBody());
                        assertFalse(containsType(workingCopy, executableElement.getThrownTypes(), "java.rmi.RemoteException"));
                        found[0] = true;
                    }
                }
            }
        });
        assertTrue(found[0]);
        
    }
    
}
