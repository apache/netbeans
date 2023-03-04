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
package org.netbeans.modules.j2ee.ejbcore.api.methodcontroller;

import java.io.IOException;
import java.util.Collections;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import org.netbeans.api.java.source.CompilationController;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.JavaSource.Phase;
import org.netbeans.api.java.source.Task;
import org.netbeans.modules.j2ee.core.api.support.java.method.MethodModel;
import org.netbeans.modules.j2ee.dd.api.ejb.EjbJarMetadata;
import org.netbeans.modules.j2ee.dd.api.ejb.Entity;
import org.netbeans.modules.j2ee.ejbcore.Utils;
import org.netbeans.modules.j2ee.ejbcore.api.methodcontroller.MethodType.BusinessMethodType;
import org.netbeans.modules.j2ee.ejbcore.api.methodcontroller.MethodType.CreateMethodType;
import org.netbeans.modules.j2ee.ejbcore.api.methodcontroller.MethodType.FinderMethodType;
import org.netbeans.modules.j2ee.ejbcore.api.methodcontroller.MethodType.HomeMethodType;
import org.netbeans.modules.j2ee.metadata.model.api.MetadataModel;
import org.netbeans.modules.j2ee.metadata.model.api.MetadataModelAction;
import org.openide.filesystems.FileObject;
import org.openide.util.Exceptions;

/**
 *
 * @author Chris Webster
 * @author Martin Adamek
 */
class EntityGenerateFromIntfVisitor implements MethodType.MethodTypeVisitor, AbstractMethodController.GenerateFromIntf {

    private static final String TODO = "//TODO implement "; //NOI18N
    
    private final MetadataModel<EjbJarMetadata> model;
    private final String ejbClass;
    private final String primaryKeyClass;
    private MethodModel implMethod;
    private MethodModel secondaryMethod;
    
    public EntityGenerateFromIntfVisitor(final String ejbClass, MetadataModel<EjbJarMetadata> model) throws IOException {
        this.ejbClass = ejbClass;
        this.model = model;
        this.primaryKeyClass = model.runReadAction(new MetadataModelAction<EjbJarMetadata, String>() {
            public String run(EjbJarMetadata metadata) throws Exception {
                Entity entity = (Entity) metadata.findByEjbClass(ejbClass);
                return entity != null ? entity.getPrimKeyClass() : null;
            }
        });
    }
    
    public void getInterfaceMethodFromImpl(MethodType methodType) {
        methodType.accept(this);
    }
    
    public MethodModel getImplMethod() {
        return implMethod;
    }
    
    public MethodModel getSecondaryMethod() {
        return secondaryMethod;
    }
    
    public void visit(BusinessMethodType bmt) {
        implMethod = bmt.getMethodElement();
        String body = TODO + implMethod.getName() + implMethod.getReturnType();
        implMethod = MethodModel.create(
                implMethod.getName(), 
                implMethod.getReturnType(),
                body,
                implMethod.getParameters(),
                implMethod.getExceptions(),
                Collections.singleton(Modifier.PUBLIC)
                );
    }
    
    public void visit(CreateMethodType cmt) {
        implMethod = cmt.getMethodElement();
        String origName = implMethod.getName();
        String newName = prependAndUpper(origName,"ejb"); //NOI18N
        String type = primaryKeyClass;
        String body = TODO + newName + type;
        implMethod = MethodModel.create(
                newName, 
                type,
                body,
                implMethod.getParameters(),
                implMethod.getExceptions(),
                Collections.singleton(Modifier.PUBLIC)
                );
        secondaryMethod = cmt.getMethodElement();
        origName = secondaryMethod.getName();
        newName = prependAndUpper(origName,"ejbPost"); //NOI18N
        body = TODO + newName;
        secondaryMethod = MethodModel.create(
                newName, 
                "void",
                body,
                secondaryMethod.getParameters(),
                secondaryMethod.getExceptions(),
                Collections.singleton(Modifier.PUBLIC)
                );
    }
    
    public void visit(HomeMethodType hmt) {
        implMethod = hmt.getMethodElement();
        String origName = implMethod.getName();
        String newName = prependAndUpper(origName,"ejbHome"); //NOI18N
        String body = TODO + implMethod.getName() + implMethod.getReturnType();
        implMethod = MethodModel.create(
                newName, 
                implMethod.getReturnType(),
                body,
                implMethod.getParameters(),
                implMethod.getExceptions(),
                Collections.singleton(Modifier.PUBLIC)
                );
    }
    
    public void visit(FinderMethodType fmt) {
        implMethod = fmt.getMethodElement();
        String origName = implMethod.getName();
        String newName = prependAndUpper(origName,"ejb"); //NOI18N
        String body = TODO + implMethod.getName() + implMethod.getReturnType();
        String collectionType = java.util.Collection.class.getName();
        String implMethodElement = implMethod.getReturnType();
        boolean isAssignable = false;
        try {
            isAssignable = isSubtype(implMethodElement, collectionType);
        } catch (IOException e) {
            Exceptions.printStackTrace(e);
        }
        implMethod = MethodModel.create(
                newName, 
                isAssignable ? "void" : primaryKeyClass,
                body,
                implMethod.getParameters(),
                implMethod.getExceptions(),
                Collections.singleton(Modifier.PUBLIC)
                );
    }
    
    private boolean isSubtype(final String className1, final String className2) throws IOException {
        FileObject ejbClassFO = model.runReadAction(new MetadataModelAction<EjbJarMetadata, FileObject>() {
            @Override
            public FileObject run(EjbJarMetadata metadata) throws Exception {
                return metadata.findResource(Utils.toResourceName(ejbClass));
            }
        });
        JavaSource javaSource = JavaSource.forFileObject(ejbClassFO);
        final boolean[] result = new boolean[] {false};
        javaSource.runUserActionTask(new Task<CompilationController>() {
            @Override
            public void run(CompilationController controller) throws IOException {
                controller.toPhase(Phase.ELEMENTS_RESOLVED);
                TypeElement typeElement1 = controller.getElements().getTypeElement(className1);
                TypeElement typeElement2 = controller.getElements().getTypeElement(className2);
                if (typeElement1 != null && typeElement2 != null) {
                    result[0] = controller.getTypes().isSubtype(typeElement1.asType(), typeElement2.asType());
                }
            }
        }, true);
        return result[0];
    }
    
    private String prependAndUpper(String fullName, String prefix) {
        StringBuffer stringBuffer = new StringBuffer(fullName);
        stringBuffer.setCharAt(0, Character.toUpperCase(stringBuffer.charAt(0)));
        return prefix + stringBuffer.toString();
    }
    
    public static String getReturnStatement(String type) {
        String result = "";
        if ("boolean".equals(type)) {
            result = "\nreturn false;";
        } else if ("byte".equals(type)) {
            result = "\nreturn 0;";
        } else if ("char".equals(type)) {
            result ="\nreturn '0';";
        } else if ("double".equals(type)) {
            result ="\nreturn 0.0;";
        } else if ("float".equals(type)) {
            result ="\nreturn 0;";
        } else if ("int".equals(type)) {
            result ="\nreturn 0;";
        } else if ("long".equals(type)) {
            result ="\nreturn 0;";
        } else if ("short".equals(type)) {
            result ="\nreturn 0;";
        } else{
            result ="\nreturn null;";
        }
        return result;
    }

}
