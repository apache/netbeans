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

package org.netbeans.modules.j2ee.ejbcore.action;

import com.sun.source.tree.MethodTree;
import java.io.IOException;
import java.util.Collections;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.TypeKind;
import javax.lang.model.util.ElementFilter;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.Task;
import org.netbeans.api.java.source.WorkingCopy;
import org.netbeans.modules.j2ee.core.api.support.java.method.MethodModel;
import org.netbeans.modules.j2ee.dd.api.ejb.DDProvider;
import org.netbeans.modules.j2ee.dd.api.ejb.EjbJar;
import org.netbeans.modules.j2ee.ejbcore.test.TestBase;
import org.openide.filesystems.FileObject;

/**
 *
 * @author Martin Adamek
 */
public class CreateMethodGeneratorTest extends TestBase {
    
    public CreateMethodGeneratorTest(String testName) {
        super(testName);
    }
    
    public void testGenerateSession() throws IOException {
        TestModule testModule = createEjb21Module();
        
        // add create method into local and remote interfaces of Stateful session EJB 
        FileObject beanClass = testModule.getSources()[0].getFileObject("statefullr/StatefulLRBean.java");
        EjbJar ejbJar = DDProvider.getDefault().getDDRoot(testModule.getDeploymentDescriptor());
        CreateMethodGenerator generator = CreateMethodGenerator.create("statefullr.StatefulLRBean", beanClass);
        final MethodModel methodModel = MethodModel.create(
                "createTest",
                "void",
                "",
                Collections.singletonList(MethodModel.Variable.create("java.lang.String", "name")),
                Collections.<String>emptyList(),
                Collections.<Modifier>emptySet()
                );
        generator.generate(methodModel, true, true);

        // ejb class, EJB 2.1 spec 7.11.3
        final boolean[] found = new boolean[] { false };
        JavaSource javaSource = JavaSource.forFileObject(beanClass);
        javaSource.runModificationTask(new Task<WorkingCopy>() {
            public void run(WorkingCopy workingCopy) throws IOException {
                workingCopy.toPhase(JavaSource.Phase.ELEMENTS_RESOLVED);
                TypeElement typeElement = workingCopy.getElements().getTypeElement("statefullr.StatefulLRBean");
                for (ExecutableElement executableElement : ElementFilter.methodsIn(typeElement.getEnclosedElements())) {
                    if (executableElement.getSimpleName().contentEquals("ejbCreateTest")) {
                        MethodTree methodTree = workingCopy.getTrees().getTree(executableElement);
                        assertNotNull(methodTree.getBody());
                        assertTrue(executableElement.getModifiers().contains(Modifier.PUBLIC));
                        assertFalse(executableElement.getModifiers().contains(Modifier.FINAL));
                        assertFalse(executableElement.getModifiers().contains(Modifier.STATIC));
                        assertSame(TypeKind.VOID, executableElement.getReturnType().getKind());
                        TypeElement createException = workingCopy.getElements().getTypeElement("javax.ejb.CreateException");
                        assertTrue(executableElement.getThrownTypes().contains(createException.asType()));
                        VariableElement parameter = executableElement.getParameters().get(0);
                        TypeElement stringTypeElement = workingCopy.getElements().getTypeElement(String.class.getName());
                        assertTrue(workingCopy.getTypes().isSameType(stringTypeElement.asType(), parameter.asType()));
                        found[0] = true;
                    }
                }
            }
        });
        assertTrue(found[0]);
        
        // local interface, EJB 2.1 spec 7.11.8
        found[0] = false;
        FileObject interfaceFileObject = testModule.getSources()[0].getFileObject("statefullr/StatefulLRLocalHome.java");
        javaSource = JavaSource.forFileObject(interfaceFileObject);
        javaSource.runModificationTask(new Task<WorkingCopy>() {
            public void run(WorkingCopy workingCopy) throws IOException {
                workingCopy.toPhase(JavaSource.Phase.ELEMENTS_RESOLVED);
                TypeElement typeElement = workingCopy.getElements().getTypeElement("statefullr.StatefulLRLocalHome");
                for (ExecutableElement executableElement : ElementFilter.methodsIn(typeElement.getEnclosedElements())) {
                    if (executableElement.getSimpleName().contentEquals("createTest")) {
                        MethodTree methodTree = workingCopy.getTrees().getTree(executableElement);
                        assertNull(methodTree.getBody());
                        TypeElement remoteException = workingCopy.getElements().getTypeElement("java.rmi.RemoteException");
                        assertFalse(executableElement.getThrownTypes().contains(remoteException.asType()));
                        TypeElement localTypeElement = workingCopy.getElements().getTypeElement("statefullr.StatefulLRLocal");
                        assertTrue(workingCopy.getTypes().isSameType(executableElement.getReturnType(), localTypeElement.asType()));
                        TypeElement createException = workingCopy.getElements().getTypeElement("javax.ejb.CreateException");
                        assertTrue(executableElement.getThrownTypes().contains(createException.asType()));
                        VariableElement parameter = executableElement.getParameters().get(0);
                        TypeElement stringTypeElement = workingCopy.getElements().getTypeElement(String.class.getName());
                        assertTrue(workingCopy.getTypes().isSameType(stringTypeElement.asType(), parameter.asType()));
                        found[0] = true;
                    }
                }
            }
        });
        assertTrue(found[0]);
        
        // remote interface, EJB 2.1 spec 7.11.6
        found[0] = false;
        interfaceFileObject = testModule.getSources()[0].getFileObject("statefullr/StatefulLRRemoteHome.java");
        javaSource = JavaSource.forFileObject(interfaceFileObject);
        javaSource.runModificationTask(new Task<WorkingCopy>() {
            public void run(WorkingCopy workingCopy) throws IOException {
                workingCopy.toPhase(JavaSource.Phase.ELEMENTS_RESOLVED);
                TypeElement typeElement = workingCopy.getElements().getTypeElement("statefullr.StatefulLRRemoteHome");
                for (ExecutableElement executableElement : ElementFilter.methodsIn(typeElement.getEnclosedElements())) {
                    if (executableElement.getSimpleName().contentEquals("createTest")) {
                        MethodTree methodTree = workingCopy.getTrees().getTree(executableElement);
                        assertNull(methodTree.getBody());
                        TypeElement remoteException = workingCopy.getElements().getTypeElement("java.rmi.RemoteException");
                        assertTrue(executableElement.getThrownTypes().contains(remoteException.asType()));
                        TypeElement remoteTypeElement = workingCopy.getElements().getTypeElement("statefullr.StatefulLRRemote");
                        assertTrue(workingCopy.getTypes().isSameType(executableElement.getReturnType(), remoteTypeElement.asType()));
                        TypeElement createException = workingCopy.getElements().getTypeElement("javax.ejb.CreateException");
                        assertTrue(executableElement.getThrownTypes().contains(createException.asType()));
                        VariableElement parameter = executableElement.getParameters().get(0);
                        TypeElement stringTypeElement = workingCopy.getElements().getTypeElement(String.class.getName());
                        assertTrue(workingCopy.getTypes().isSameType(stringTypeElement.asType(), parameter.asType()));
                        found[0] = true;
                    }
                }
            }
        });
        assertTrue(found[0]);
        
    }
    
    public void testGenerateEntity() throws IOException {
        TestModule testModule = createEjb21Module();
        
        // add create method into local and remote interfaces of CMP Entity EJB 
        FileObject beanClass = testModule.getSources()[0].getFileObject("cmplr/CmpLRBean.java");
        EjbJar ejbJar = DDProvider.getDefault().getDDRoot(testModule.getDeploymentDescriptor());
        CreateMethodGenerator generator = CreateMethodGenerator.create("cmplr.CmpLRBean", beanClass);
        final MethodModel methodModel = MethodModel.create(
                "createTest",
                "void",
                "",
                Collections.singletonList(MethodModel.Variable.create("java.lang.String", "name")),
                Collections.<String>emptyList(),
                Collections.<Modifier>emptySet()
                );
        generator.generate(methodModel, true, true);

        // ejb class, EJB 2.1 spec 10.6.4, 10.6.5
        final boolean[] found = new boolean[] { false, false }; // ejbCreate, ejbPostCreate
        JavaSource javaSource = JavaSource.forFileObject(beanClass);
        javaSource.runModificationTask(new Task<WorkingCopy>() {
            public void run(WorkingCopy workingCopy) throws IOException {
                workingCopy.toPhase(JavaSource.Phase.ELEMENTS_RESOLVED);
                TypeElement typeElement = workingCopy.getElements().getTypeElement("cmplr.CmpLRBean");
                for (ExecutableElement executableElement : ElementFilter.methodsIn(typeElement.getEnclosedElements())) {
                    if (executableElement.getSimpleName().contentEquals("ejbCreateTest")) {
                        MethodTree methodTree = workingCopy.getTrees().getTree(executableElement);
                        assertNotNull(methodTree.getBody());
                        assertTrue(executableElement.getModifiers().contains(Modifier.PUBLIC));
                        assertFalse(executableElement.getModifiers().contains(Modifier.FINAL));
                        assertFalse(executableElement.getModifiers().contains(Modifier.STATIC));
                        TypeElement longTypeElement = workingCopy.getElements().getTypeElement("java.lang.Long");
                        assertTrue(workingCopy.getTypes().isSameType(executableElement.getReturnType(), longTypeElement.asType()));
                        TypeElement createException = workingCopy.getElements().getTypeElement("javax.ejb.CreateException");
                        assertTrue(executableElement.getThrownTypes().contains(createException.asType()));
                        VariableElement parameter = executableElement.getParameters().get(0);
                        TypeElement stringTypeElement = workingCopy.getElements().getTypeElement(String.class.getName());
                        assertTrue(workingCopy.getTypes().isSameType(stringTypeElement.asType(), parameter.asType()));
                        found[0] = true;
                    }
                    if (executableElement.getSimpleName().contentEquals("ejbPostCreateTest")) {
                        MethodTree methodTree = workingCopy.getTrees().getTree(executableElement);
                        assertNotNull(methodTree.getBody());
                        assertTrue(executableElement.getModifiers().contains(Modifier.PUBLIC));
                        assertFalse(executableElement.getModifiers().contains(Modifier.FINAL));
                        assertFalse(executableElement.getModifiers().contains(Modifier.STATIC));
                        assertSame(TypeKind.VOID, executableElement.getReturnType().getKind());
                        TypeElement createException = workingCopy.getElements().getTypeElement("javax.ejb.CreateException");
                        assertTrue(executableElement.getThrownTypes().contains(createException.asType()));
                        VariableElement parameter = executableElement.getParameters().get(0);
                        TypeElement stringTypeElement = workingCopy.getElements().getTypeElement(String.class.getName());
                        assertTrue(workingCopy.getTypes().isSameType(stringTypeElement.asType(), parameter.asType()));
                        found[1] = true;
                    }
                }
            }
        });
        assertTrue(found[0]);
        assertTrue(found[1]);
        
        // local interface, EJB 2.1 spec 10.6.12
        found[0] = false;
        FileObject interfaceFileObject = testModule.getSources()[0].getFileObject("cmplr/CmpLRLocalHome.java");
        javaSource = JavaSource.forFileObject(interfaceFileObject);
        javaSource.runModificationTask(new Task<WorkingCopy>() {
            public void run(WorkingCopy workingCopy) throws IOException {
                workingCopy.toPhase(JavaSource.Phase.ELEMENTS_RESOLVED);
                TypeElement typeElement = workingCopy.getElements().getTypeElement("cmplr.CmpLRLocalHome");
                for (ExecutableElement executableElement : ElementFilter.methodsIn(typeElement.getEnclosedElements())) {
                    if (executableElement.getSimpleName().contentEquals("createTest")) {
                        MethodTree methodTree = workingCopy.getTrees().getTree(executableElement);
                        assertNull(methodTree.getBody());
                        TypeElement remoteException = workingCopy.getElements().getTypeElement("java.rmi.RemoteException");
                        assertFalse(executableElement.getThrownTypes().contains(remoteException.asType()));
                        TypeElement localTypeElement = workingCopy.getElements().getTypeElement("cmplr.CmpLRLocal");
                        assertTrue(workingCopy.getTypes().isSameType(executableElement.getReturnType(), localTypeElement.asType()));
                        TypeElement createException = workingCopy.getElements().getTypeElement("javax.ejb.CreateException");
                        assertTrue(executableElement.getThrownTypes().contains(createException.asType()));
                        VariableElement parameter = executableElement.getParameters().get(0);
                        TypeElement stringTypeElement = workingCopy.getElements().getTypeElement(String.class.getName());
                        assertTrue(workingCopy.getTypes().isSameType(stringTypeElement.asType(), parameter.asType()));
                        found[0] = true;
                    }
                }
            }
        });
        assertTrue(found[0]);
        
        // remote interface, EJB 2.1 spec 10.6.10
        found[0] = false;
        interfaceFileObject = testModule.getSources()[0].getFileObject("cmplr/CmpLRRemoteHome.java");
        javaSource = JavaSource.forFileObject(interfaceFileObject);
        javaSource.runModificationTask(new Task<WorkingCopy>() {
            public void run(WorkingCopy workingCopy) throws IOException {
                workingCopy.toPhase(JavaSource.Phase.ELEMENTS_RESOLVED);
                TypeElement typeElement = workingCopy.getElements().getTypeElement("cmplr.CmpLRRemoteHome");
                for (ExecutableElement executableElement : ElementFilter.methodsIn(typeElement.getEnclosedElements())) {
                    if (executableElement.getSimpleName().contentEquals("createTest")) {
                        MethodTree methodTree = workingCopy.getTrees().getTree(executableElement);
                        assertNull(methodTree.getBody());
                        TypeElement remoteException = workingCopy.getElements().getTypeElement("java.rmi.RemoteException");
                        assertTrue(executableElement.getThrownTypes().contains(remoteException.asType()));
                        TypeElement remoteTypeElement = workingCopy.getElements().getTypeElement("cmplr.CmpLRRemote");
                        assertTrue(workingCopy.getTypes().isSameType(executableElement.getReturnType(), remoteTypeElement.asType()));
                        TypeElement createException = workingCopy.getElements().getTypeElement("javax.ejb.CreateException");
                        assertTrue(executableElement.getThrownTypes().contains(createException.asType()));
                        VariableElement parameter = executableElement.getParameters().get(0);
                        TypeElement stringTypeElement = workingCopy.getElements().getTypeElement(String.class.getName());
                        assertTrue(workingCopy.getTypes().isSameType(stringTypeElement.asType(), parameter.asType()));
                        found[0] = true;
                    }
                }
            }
        });
        assertTrue(found[0]);
        
    }

}
