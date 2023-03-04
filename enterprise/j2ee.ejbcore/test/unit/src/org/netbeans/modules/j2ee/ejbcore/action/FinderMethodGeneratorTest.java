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
import javax.lang.model.element.VariableElement;
import javax.lang.model.util.ElementFilter;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.Task;
import org.netbeans.api.java.source.WorkingCopy;
import org.netbeans.modules.j2ee.core.api.support.java.method.MethodModel;
import org.netbeans.modules.j2ee.dd.api.ejb.DDProvider;
import org.netbeans.modules.j2ee.dd.api.ejb.EjbJar;
import org.netbeans.modules.j2ee.dd.api.ejb.EnterpriseBeans;
import org.netbeans.modules.j2ee.dd.api.ejb.Entity;
import org.netbeans.modules.j2ee.dd.api.ejb.Query;
import org.netbeans.modules.j2ee.dd.api.ejb.QueryMethod;
import org.netbeans.modules.j2ee.ejbcore.test.TestBase;
import org.openide.filesystems.FileObject;

/**
 *
 * @author Martin Adamek
 */
public class FinderMethodGeneratorTest extends TestBase {
    
    public FinderMethodGeneratorTest(String testName) {
        super(testName);
    }
    
    public void testGenerateCmpCardinalityOne() throws IOException {
        TestModule testModule = createEjb21Module();

        // add create method into local and remote interfaces of CMP Entity EJB 
        FileObject beanClass = testModule.getSources()[0].getFileObject("cmplr/CmpLRBean.java");
        EjbJar ejbJar = DDProvider.getDefault().getDDRoot(testModule.getDeploymentDescriptor());
        FinderMethodGenerator generator = FinderMethodGenerator.create("cmplr.CmpLRBean", beanClass);
        final MethodModel methodModel = MethodModel.create(
                "findTestOne",
                "void",
                "",
                Collections.singletonList(MethodModel.Variable.create("java.lang.String", "name")),
                Collections.<String>emptyList(),
                Collections.<Modifier>emptySet()
                );
        String ejbql = "SELECT OBJECT(o) FROM CmpLR o";
        generator.generate(methodModel, true, true, true, ejbql);
        
        // local interface
        final boolean[] found = new boolean[] { false };
        FileObject interfaceFileObject = testModule.getSources()[0].getFileObject("cmplr/CmpLRLocalHome.java");
        JavaSource javaSource = JavaSource.forFileObject(interfaceFileObject);
        javaSource.runModificationTask(new Task<WorkingCopy>() {
            public void run(WorkingCopy workingCopy) throws IOException {
                workingCopy.toPhase(JavaSource.Phase.ELEMENTS_RESOLVED);
                TypeElement typeElement = workingCopy.getElements().getTypeElement("cmplr.CmpLRLocalHome");
                for (ExecutableElement executableElement : ElementFilter.methodsIn(typeElement.getEnclosedElements())) {
                    if (executableElement.getSimpleName().contentEquals("findTestOne")) {
                        MethodTree methodTree = workingCopy.getTrees().getTree(executableElement);
                        assertNull(methodTree.getBody());
                        TypeElement remoteException = workingCopy.getElements().getTypeElement("java.rmi.RemoteException");
                        assertFalse(executableElement.getThrownTypes().contains(remoteException.asType()));
                        TypeElement localTypeElement = workingCopy.getElements().getTypeElement("cmplr.CmpLRLocal");
                        assertTrue(workingCopy.getTypes().isSameType(executableElement.getReturnType(), localTypeElement.asType()));
                        TypeElement finderException = workingCopy.getElements().getTypeElement("javax.ejb.FinderException");
                        assertTrue(executableElement.getThrownTypes().contains(finderException.asType()));
                        VariableElement parameter = executableElement.getParameters().get(0);
                        TypeElement stringTypeElement = workingCopy.getElements().getTypeElement(String.class.getName());
                        assertTrue(workingCopy.getTypes().isSameType(stringTypeElement.asType(), parameter.asType()));
                        found[0] = true;
                    }
                }
            }
        });
        assertTrue(found[0]);
        
        // remote interface
        found[0] = false;
        interfaceFileObject = testModule.getSources()[0].getFileObject("cmplr/CmpLRRemoteHome.java");
        javaSource = JavaSource.forFileObject(interfaceFileObject);
        javaSource.runModificationTask(new Task<WorkingCopy>() {
            public void run(WorkingCopy workingCopy) throws IOException {
                workingCopy.toPhase(JavaSource.Phase.ELEMENTS_RESOLVED);
                TypeElement typeElement = workingCopy.getElements().getTypeElement("cmplr.CmpLRRemoteHome");
                for (ExecutableElement executableElement : ElementFilter.methodsIn(typeElement.getEnclosedElements())) {
                    if (executableElement.getSimpleName().contentEquals("findTestOne")) {
                        MethodTree methodTree = workingCopy.getTrees().getTree(executableElement);
                        assertNull(methodTree.getBody());
                        TypeElement remoteException = workingCopy.getElements().getTypeElement("java.rmi.RemoteException");
                        assertTrue(executableElement.getThrownTypes().contains(remoteException.asType()));
                        TypeElement remoteTypeElement = workingCopy.getElements().getTypeElement("cmplr.CmpLRRemote");
                        assertTrue(workingCopy.getTypes().isSameType(executableElement.getReturnType(), remoteTypeElement.asType()));
                        TypeElement finderException = workingCopy.getElements().getTypeElement("javax.ejb.FinderException");
                        assertTrue(executableElement.getThrownTypes().contains(finderException.asType()));
                        VariableElement parameter = executableElement.getParameters().get(0);
                        TypeElement stringTypeElement = workingCopy.getElements().getTypeElement(String.class.getName());
                        assertTrue(workingCopy.getTypes().isSameType(stringTypeElement.asType(), parameter.asType()));
                        found[0] = true;
                    }
                }
            }
        });
        assertTrue(found[0]);

        // entry in deployment descriptor
        Entity entity = (Entity) ejbJar.getEnterpriseBeans().findBeanByName(EnterpriseBeans.ENTITY, Entity.EJB_CLASS, "cmplr.CmpLRBean");
        boolean queryFound = false;
        for (Query query : entity.getQuery()) {
            QueryMethod queryMethod = query.getQueryMethod();
            if ("findTestOne".equals(queryMethod.getMethodName())) {
                assertEquals(ejbql, query.getEjbQl());
                queryFound = true;
            }
        }
        assertTrue(queryFound);
        
    }
    
    public void testGenerateCmpCardinalityMany() throws IOException {
        TestModule testModule = createEjb21Module();

        // add create method into local and remote interfaces of CMP Entity EJB 
        FileObject beanClass = testModule.getSources()[0].getFileObject("cmplr/CmpLRBean.java");
        EjbJar ejbJar = DDProvider.getDefault().getDDRoot(testModule.getDeploymentDescriptor());
        FinderMethodGenerator generator = FinderMethodGenerator.create("cmplr.CmpLRBean", beanClass);
        final MethodModel methodModel = MethodModel.create(
                "findTestMany",
                "void",
                "",
                Collections.singletonList(MethodModel.Variable.create("java.lang.String", "name")),
                Collections.<String>emptyList(),
                Collections.<Modifier>emptySet()
                );
        String ejbql = "SELECT OBJECT(o) FROM CmpLR o";
        generator.generate(methodModel, true, true, false, ejbql);
        
        // local interface
        final boolean[] found = new boolean[] { false };
        FileObject interfaceFileObject = testModule.getSources()[0].getFileObject("cmplr/CmpLRLocalHome.java");
        JavaSource javaSource = JavaSource.forFileObject(interfaceFileObject);
        javaSource.runModificationTask(new Task<WorkingCopy>() {
            public void run(WorkingCopy workingCopy) throws IOException {
                workingCopy.toPhase(JavaSource.Phase.ELEMENTS_RESOLVED);
                TypeElement typeElement = workingCopy.getElements().getTypeElement("cmplr.CmpLRLocalHome");
                for (ExecutableElement executableElement : ElementFilter.methodsIn(typeElement.getEnclosedElements())) {
                    if (executableElement.getSimpleName().contentEquals("findTestMany")) {
                        MethodTree methodTree = workingCopy.getTrees().getTree(executableElement);
                        assertNull(methodTree.getBody());
                        TypeElement remoteException = workingCopy.getElements().getTypeElement("java.rmi.RemoteException");
                        assertFalse(executableElement.getThrownTypes().contains(remoteException.asType()));
                        TypeElement collectionTypeElement = workingCopy.getElements().getTypeElement("java.util.Collection");
                        // isSameType should be used here but fails: Collection vs Collection<E>
                        assertTrue(workingCopy.getTypes().isAssignable(executableElement.getReturnType(), collectionTypeElement.asType()));
                        TypeElement finderException = workingCopy.getElements().getTypeElement("javax.ejb.FinderException");
                        assertTrue(executableElement.getThrownTypes().contains(finderException.asType()));
                        VariableElement parameter = executableElement.getParameters().get(0);
                        TypeElement stringTypeElement = workingCopy.getElements().getTypeElement(String.class.getName());
                        assertTrue(workingCopy.getTypes().isSameType(stringTypeElement.asType(), parameter.asType()));
                        found[0] = true;
                    }
                }
            }
        });
        assertTrue(found[0]);
        
        // remote interface
        found[0] = false;
        interfaceFileObject = testModule.getSources()[0].getFileObject("cmplr/CmpLRRemoteHome.java");
        javaSource = JavaSource.forFileObject(interfaceFileObject);
        javaSource.runModificationTask(new Task<WorkingCopy>() {
            public void run(WorkingCopy workingCopy) throws IOException {
                workingCopy.toPhase(JavaSource.Phase.ELEMENTS_RESOLVED);
                TypeElement typeElement = workingCopy.getElements().getTypeElement("cmplr.CmpLRRemoteHome");
                for (ExecutableElement executableElement : ElementFilter.methodsIn(typeElement.getEnclosedElements())) {
                    if (executableElement.getSimpleName().contentEquals("findTestMany")) {
                        MethodTree methodTree = workingCopy.getTrees().getTree(executableElement);
                        assertNull(methodTree.getBody());
                        TypeElement remoteException = workingCopy.getElements().getTypeElement("java.rmi.RemoteException");
                        assertTrue(executableElement.getThrownTypes().contains(remoteException.asType()));
                        TypeElement collectionTypeElement = workingCopy.getElements().getTypeElement("java.util.Collection");
                        // isSameType should be used here but fails: Collection vs Collection<E>
                        assertTrue(workingCopy.getTypes().isAssignable(executableElement.getReturnType(), collectionTypeElement.asType()));
                        TypeElement finderException = workingCopy.getElements().getTypeElement("javax.ejb.FinderException");
                        assertTrue(executableElement.getThrownTypes().contains(finderException.asType()));
                        VariableElement parameter = executableElement.getParameters().get(0);
                        TypeElement stringTypeElement = workingCopy.getElements().getTypeElement(String.class.getName());
                        assertTrue(workingCopy.getTypes().isSameType(stringTypeElement.asType(), parameter.asType()));
                        found[0] = true;
                    }
                }
            }
        });
        assertTrue(found[0]);

        // entry in deployment descriptor
        Entity entity = (Entity) ejbJar.getEnterpriseBeans().findBeanByName(EnterpriseBeans.ENTITY, Entity.EJB_CLASS, "cmplr.CmpLRBean");
        boolean queryFound = false;
        for (Query query : entity.getQuery()) {
            QueryMethod queryMethod = query.getQueryMethod();
            if ("findTestMany".equals(queryMethod.getMethodName())) {
                assertEquals(ejbql, query.getEjbQl());
                queryFound = true;
            }
        }
        assertTrue(queryFound);

    }
    
}
