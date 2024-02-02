/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
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
        if (TypeKind.DECLARED == supertype.getKind()) {
            Element element = ((DeclaredType)supertype).asElement();
            if (ElementKind.CLASS == element.getKind()) {
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
        if (TypeKind.DECLARED != typeMirror.getKind()) {
            return null;
        }
        Element element = typeMirror.asElement();
        if (ElementKind.ANNOTATION_TYPE != element.getKind()) {
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
