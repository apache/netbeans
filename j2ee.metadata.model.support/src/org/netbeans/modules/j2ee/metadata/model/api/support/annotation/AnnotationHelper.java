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
package org.netbeans.modules.j2ee.metadata.model.api.support.annotation;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Types;

import org.netbeans.api.java.source.CompilationInfo;



/**
 * @author ads
 *
 */
public class AnnotationHelper {

    public AnnotationHelper( CompilationInfo info ){
        this.info = info;
    }
    
    public AnnotationHelper( AnnotationModelHelper helper ){
        this.helper = helper;
    }
    
    public TypeMirror resolveType(String typeName) {
        TypeElement type = getCompilationInfo().getElements().getTypeElement(typeName);
        if (type != null) {
            return type.asType();
        }
        return null;
    }
    
    public AnnotationScanner getAnnotationScanner(){
        return new AnnotationScanner(this );
    }

    public boolean isSameRawType(TypeMirror type1, String type2ElementName) {
        TypeElement type2Element = getCompilationInfo().getElements().getTypeElement(type2ElementName);
        if (type2Element != null) {
            Types types = getCompilationInfo().getTypes();
            TypeMirror type2 = types.erasure(type2Element.asType());
            return types.isSameType(types.erasure(type1), type2);
        }
        return false;
    }

    public List<? extends TypeElement> getSuperclasses(TypeElement type) {
        List<TypeElement> result = new ArrayList<TypeElement>();
        TypeElement currentType = type;
        for (;;) {
            currentType = getSuperclass(currentType);
            if (currentType != null) {
                result.add(currentType);
            } else {
                break;
            }
        }
        return Collections.unmodifiableList(result);
    }

    public TypeElement getSuperclass(TypeElement type) {
        TypeMirror supertype = type.getSuperclass();
        if (TypeKind.DECLARED.equals(supertype.getKind())) {
            Element element = ((DeclaredType)supertype).asElement();
            if (ElementKind.CLASS.equals(element.getKind())) {
                TypeElement superclass = (TypeElement)element;
                if (!superclass.getQualifiedName().contentEquals(
                        Object.class.getCanonicalName())) { 
                    return superclass;
                }
            }
        }
        return null;
    }

    public boolean hasAnnotation(List<? extends AnnotationMirror> annotations, 
            String annotationTypeName) 
    {
        for (AnnotationMirror annotation : annotations) {
            String typeName = getAnnotationTypeName(annotation.getAnnotationType());
            if (annotationTypeName.equals(typeName)) {
                return true;
            }
        }
        return false;
    }

    public boolean hasAnyAnnotation(List<? extends AnnotationMirror> annotations, 
            Set<String> annotationTypeNames) 
    {
        for (AnnotationMirror annotation : annotations) {
            String annotationTypeName = getAnnotationTypeName(
                    annotation.getAnnotationType());
            if (annotationTypeName != null && 
                    annotationTypeNames.contains(annotationTypeName)) 
            {
                return true;
            }
        }
        return false;
    }

    public Map<String, ? extends AnnotationMirror> getAnnotationsByType(
            List<? extends AnnotationMirror> annotations) 
    {
        Map<String, AnnotationMirror> result = new HashMap<String, AnnotationMirror>();
        for (AnnotationMirror annotation : annotations) {
            String typeName = getAnnotationTypeName(annotation.getAnnotationType());
            if (typeName != null) {
                result.put(typeName, annotation);
            }
        }
        return Collections.unmodifiableMap(result);
    }

    /**
     * @return the annotation type name or null if <code>typeMirror</code>
     *         was not an annotation type.
     */
    public String getAnnotationTypeName(DeclaredType typeMirror) {
        if (!TypeKind.DECLARED.equals(typeMirror.getKind())) {
            return null;
        }
        Element element = typeMirror.asElement();
        if (!ElementKind.ANNOTATION_TYPE.equals(element.getKind())) {
            return null;
        }
        return ((TypeElement)element).getQualifiedName().toString();
    }
    
    public CompilationInfo getCompilationInfo(){
        if ( helper == null ){
            return info;
        }
        else {
            return helper.getCompilationController();
        }
    }

    private CompilationInfo info;
    private AnnotationModelHelper helper;
}
