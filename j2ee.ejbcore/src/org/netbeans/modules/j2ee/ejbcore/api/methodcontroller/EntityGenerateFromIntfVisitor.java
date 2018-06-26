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
