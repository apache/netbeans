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

package org.netbeans.modules.j2ee.ejbcore.api.codegeneration;

import java.io.IOException;
import java.util.List;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Name;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.ArrayType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.ElementFilter;
import org.netbeans.api.java.source.CancellableTask;
import org.netbeans.api.java.source.CompilationController;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.modules.j2ee.core.api.support.java.method.MethodModel;
import org.netbeans.modules.j2ee.core.api.support.java.method.MethodModelSupport;
import org.openide.filesystems.FileObject;

/**
 *
 * @author Martin Adamek
 */
public final class Util {
    
    private Util() {}
    
    protected static boolean directlyImplements(CompilationController controller, TypeElement typeElement, String[] interfaces) {
        List<? extends TypeMirror> foundInterfaces = typeElement.getInterfaces();
        if (foundInterfaces.size() != interfaces.length) {
            return false;
        }
        for (TypeMirror typeMirror : foundInterfaces) {
            TypeElement element = (TypeElement) controller.getTypes().asElement(typeMirror);
            if (!containsName(interfaces, element.getQualifiedName())) {
                return false;
            }
        }
        return true;
    }
    
    protected static boolean contains(CompilationController controller, TypeElement typeElement, MethodModel methodModel) {
        for (ExecutableElement executableElement : ElementFilter.methodsIn(typeElement.getEnclosedElements())) {
            if (MethodModelSupport.isSameMethod(controller, executableElement, methodModel)) {
                return true;
            }
        }
        return false;
    }
    
    protected static boolean contains(CompilationController controller, TypeElement typeElement, MethodModel.Variable field) {
        for (VariableElement variableElement : ElementFilter.fieldsIn(typeElement.getEnclosedElements())) {
            if (getTypeName(controller, variableElement.asType()).equals(field.getType()) &&
                    variableElement.getSimpleName().contentEquals(field.getName())) {
                return true;
            }
        }
        
        return false;
    }
    
    protected static boolean containsName(String[] stringNames, Name name) {
        for (String stringName : stringNames) {
            if (name.contentEquals(stringName)) {
                return true;
            }
        }
        return false;
    }
    
    protected static void runUserActionTask(FileObject javaFile, CancellableTask<CompilationController> taskToTest) throws IOException {
        JavaSource javaSource = JavaSource.forFileObject(javaFile);
        javaSource.runUserActionTask(taskToTest, true);
    }
    
    // see #90968
    protected static String getTypeName(CompilationController controller, TypeMirror typeMirror) {
        TypeKind typeKind = typeMirror.getKind();
        switch (typeKind) {
            case BOOLEAN : return "boolean"; // NOI18N
            case BYTE : return "byte"; // NOI18N
            case CHAR : return "char"; // NOI18N
            case DOUBLE : return "double"; // NOI18N
            case FLOAT : return "float"; // NOI18N
            case INT : return "int"; // NOI18N
            case LONG : return "long"; // NOI18N
            case SHORT : return "short"; // NOI18N
            case VOID : return "void"; // NOI18N
            case DECLARED : 
                Element element = controller.getTypes().asElement(typeMirror);
                return ((TypeElement) element).getQualifiedName().toString();
            case ARRAY : 
                ArrayType arrayType = (ArrayType) typeMirror;
                Element componentTypeElement = controller.getTypes().asElement(arrayType.getComponentType());
                return ((TypeElement) componentTypeElement).getQualifiedName().toString() + "[]";
            case ERROR :
            case EXECUTABLE :
            case NONE :
            case NULL :
            case OTHER :
            case PACKAGE :
            case TYPEVAR :
            case WILDCARD :
                break;
        }
        return null;
    }

}
