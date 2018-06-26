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

package org.netbeans.modules.j2ee.ejbcore.ui.logicalview.entries;

import com.sun.source.tree.ClassTree;
import com.sun.source.tree.MethodTree;
import com.sun.source.tree.VariableTree;
import java.io.IOException;
import java.util.Collections;
import java.util.Set;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.ElementFilter;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.project.JavaProjectConstants;
import org.netbeans.api.java.source.CompilationController;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.Task;
import org.netbeans.api.java.source.TreeMaker;
import org.netbeans.api.java.source.WorkingCopy;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.api.project.Sources;
import org.netbeans.modules.j2ee.core.api.support.java.method.MethodModel;
import org.netbeans.modules.j2ee.core.api.support.java.method.MethodModelSupport;
import org.netbeans.spi.java.classpath.ClassPathProvider;
import org.netbeans.spi.java.classpath.PathResourceImplementation;
import org.netbeans.spi.java.classpath.support.ClassPathSupport;
import org.openide.filesystems.FileObject;
import org.openide.util.Exceptions;

/**
 *
 * @author Chris Webster
 * @author Martin Adamek
 */
public class ServiceLocatorStrategy {
    private static final String CREATE_INVOCATION = ".create()"; //NOI18N
    private final String serviceLocator;
    
    private ServiceLocatorStrategy(String serviceLocator) {
        this.serviceLocator = serviceLocator;
    }
    
    public String genLocalEjbStringLookup(String jndiName, String homeName, FileObject referencingFO, String referencingClass, boolean create) {
        String initString = initString("getLocalHome", jndiName, referencingFO, referencingClass, serviceLocator, ""); //NOI18N
        return "return " + addCast(create, homeName, initString, CREATE_INVOCATION) + ";"; // NOI18N
    }
    
    public String genRemoteEjbStringLookup(String jndiName, String homeCls, FileObject fileObject, String className, boolean create) {
        String initString = initString("getRemoteHome", jndiName, fileObject, className, serviceLocator, "," + homeCls + ".class"); //NOI18N
        return "return " + addCast(create, homeCls, initString, CREATE_INVOCATION) + ";"; //NOI18N
    }
    
    public String genDestinationLookup(String jndiName, FileObject fileObject, String className) {
        return initString("getDestination", jndiName, fileObject, className, serviceLocator, ""); //NOI18N
    }
    
    public String genJMSFactory(String jndiName, FileObject fileObject, String className) {
        return initString("getConnectionFactory", jndiName, fileObject, className, serviceLocator, ""); //NOI18N
    }
    
    public String genDataSource(String jndiName, FileObject fileObject, String className) {
        return initString("getDataSource", jndiName, fileObject, className, serviceLocator, ""); //NOI18N
    }
    
    public String genMailSession(String jndiName, FileObject fileObject, String className) {
        return initString("getSession", jndiName, fileObject, className, serviceLocator, ""); //NOI18N
    }
    
    public static ServiceLocatorStrategy create(Project project, FileObject srcFile, String serviceLocator) {
        return new ServiceLocatorStrategy(serviceLocator);
    }
    
    private ClassPath buildClassPathFromImportedProject(FileObject fileObject) {
        Project project = FileOwnerQuery.getOwner(fileObject);
        assert project != null : "cannot find project for file";
        ClassPathProvider cpp = (ClassPathProvider)
            project.getLookup().lookup(ClassPathProvider.class);
        assert cpp != null: "project doesn't have class path provider";
        Sources sources = ProjectUtils.getSources(project);
        SourceGroup[] groups = sources.getSourceGroups(JavaProjectConstants.SOURCES_TYPE_JAVA);
        ClassPath classPath = ClassPathSupport.createClassPath(Collections.<PathResourceImplementation>emptyList());
        for (int i = 0; i < groups.length; i++) {
            FileObject root = groups[i].getRootFolder();
            if (root.getChildren().length > 0) {
                ClassPath tcp = cpp.findClassPath(root.getChildren()[0], ClassPath.SOURCE);
                classPath = ClassPathSupport.createProxyClassPath(new ClassPath[]{tcp,classPath});
            }
        }
        return classPath;
    }
    
    private String addCast(boolean cast, String clName, String current, String inv) {
        String newValue = current;
        newValue = "("+clName+") " + current; //NOI18N
        if (cast) {
            newValue = "(" + newValue + ")" + inv; //NOI18N
        }
        return newValue;
    }
    
    private static String initString(String methodName, String jndiName, FileObject referencingFO, String referencingClass, 
            String serviceLocator, String otherParams) {
        String initString = null;
        try {
            String staticCreation = getStaticLocator(referencingFO, serviceLocator);
            if (staticCreation != null) {
                initString = serviceLocator + "." + staticCreation + "()." + methodName + //NOI18N
                             "(\"java:comp/env/" + jndiName + "\""+ otherParams +")"; //NOI18N
            } else {
                    initString = findOrCreateArtifacts(referencingFO, referencingClass, serviceLocator) + "()." + methodName + //NOI18N
                        "(\"java:comp/env/" + jndiName + "\"" + otherParams + ")"; //NOI18N
            }
        } catch (IOException ioe) {
            Exceptions.printStackTrace(ioe);
        }
        return initString;
    }
    
    private static String findOrCreateArtifacts(FileObject fileObject, final String className, final String serviceLocator) throws IOException {
        JavaSource javaSource = JavaSource.forFileObject(fileObject);
        final String[] methodName = new String[1];
        javaSource.runModificationTask(new Task<WorkingCopy>() {
            public void run(WorkingCopy workingCopy) throws IOException {
                workingCopy.toPhase(JavaSource.Phase.ELEMENTS_RESOLVED);
                TypeElement target = workingCopy.getElements().getTypeElement(className);
                for (ExecutableElement executableElement : ElementFilter.methodsIn(target.getEnclosedElements())) {
                    if (executableElement.getParameters().size() == 0 &&
                            workingCopy.getTypes().isSameType(target.asType(), executableElement.getReturnType())) {
                        methodName[0] = executableElement.getSimpleName().toString();
                        break;
                    }
                }
                TreeMaker treeMaker = workingCopy.getTreeMaker();
                TypeElement fieldTypeElement = workingCopy.getElements().getTypeElement(serviceLocator);
                if (methodName[0] == null) {
                    VariableTree variableTree = treeMaker.Variable(
                            treeMaker.Modifiers(Collections.singleton(Modifier.PRIVATE)),
                            "serviceLocator",
                            treeMaker.QualIdent(fieldTypeElement),
                            null
                            );
                    ClassTree classTree = workingCopy.getTrees().getTree(target);
                    ClassTree newClassTree = treeMaker.insertClassMember(classTree, 0, variableTree);

                    MethodModel methodModel = MethodModel.create(
                            "getServiceLocator",
                            serviceLocator,
                            "if (serviceLocator == null) {\n" +
                            "serviceLocator = new " + serviceLocator + "();\n" +
                            "}\n" +
                            "return serviceLocator;\n",
                            Collections.<MethodModel.Variable>emptyList(),
                            Collections.<String>emptyList(),
                            Collections.singleton(Modifier.PRIVATE)
                            );
                    MethodTree methodTree = MethodModelSupport.createMethodTree(workingCopy, methodModel);
                    classTree = workingCopy.getTrees().getTree(target);
                    newClassTree = treeMaker.addClassMember(newClassTree, methodTree);
                    workingCopy.rewrite(classTree, newClassTree);
                    
                    methodName[0] = "getServiceLocator";
                }
            }
        }).commit();
        return methodName[0];
    }
    
    private static String getStaticLocator(FileObject referencingFO, final String serviceLocator) throws IOException {
        JavaSource javaSource = JavaSource.forFileObject(referencingFO);
        final String[] methodName = new String[1];
        javaSource.runUserActionTask(new Task<CompilationController>() {
            public void run(CompilationController controller) throws IOException {
                controller.toPhase(JavaSource.Phase.ELEMENTS_RESOLVED);
                TypeElement typeElement = controller.getElements().getTypeElement(serviceLocator);
                if (typeElement != null) {
                    for (ExecutableElement executableElement : ElementFilter.methodsIn(typeElement.getEnclosedElements())) {
                        Set<Modifier> modifiers = executableElement.getModifiers();
                        if (modifiers.contains(Modifier.STATIC) && modifiers.contains(Modifier.PUBLIC) &&
                            controller.getTypes().isSameType(typeElement.asType(), executableElement.getReturnType())) {
                                methodName[0] = executableElement.getSimpleName().toString();
                        }
                    }
                }
            }
        }, true);
        return methodName[0];
    }
    
}
