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
package org.netbeans.modules.j2ee.ejbverification;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashMap;
import java.util.List;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.AnnotationValue;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.TypeParameterElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import org.netbeans.api.java.source.CompilationInfo;

/**
 *
 * @author Tomasz.Slota
 */
public class JavaUtils {
    public static String extractClassNameFromType(TypeMirror type){
        if (type instanceof DeclaredType){
            Element elem = ((DeclaredType)type).asElement();
            
            if (elem.getKind() == ElementKind.CLASS
                    || elem.getKind() == ElementKind.INTERFACE){
                return ((TypeElement)elem).getQualifiedName().toString();
            }
        }
        
        return null;
    }
    
    /**
     * A convenience method, returns true if findAnnotation(...) != null
     */
    public static boolean hasAnnotation(Element element, String annClass){
        AnnotationMirror annEntity = findAnnotation(element, annClass);
        return annEntity != null;
    }
    
    public static AnnotationMirror findAnnotation(Element element, String annotationClass){
        for (AnnotationMirror ann : element.getAnnotationMirrors()){
            if (annotationClass.equals(ann.getAnnotationType().toString())){
                return ann;
            }
        }
        
        return null;
    }
    
    /**
     * @return the value of annotation attribute, null if the attribute
     * was not found or when ann was null
     */
    public static AnnotationValue getAnnotationAttrValue(AnnotationMirror ann, String attrName){
        if (ann != null){
            for (ExecutableElement attr : ann.getElementValues().keySet()){
                if (attrName.equals(attr.getSimpleName().toString())){
                    return ann.getElementValues().get(attr);
                }
            }
        }
        
        return null;
    }
    
    public static String getShortClassName(String qualifiedClassName){
        return qualifiedClassName.substring(qualifiedClassName.lastIndexOf(".") + 1); //NOI18N
    }

    public static boolean isMethodSignatureSame(CompilationInfo cinfo,
            ExecutableElement method1, ExecutableElement method2){

        // check for parameters count
        int paramCount = method1.getParameters().size();
        if (paramCount != method2.getParameters().size()) {
            return false;
        }

        for (int i = 0; i < paramCount; i++) {
            TypeMirror param1 = method1.getParameters().get(i).asType();
            TypeMirror param2 = method2.getParameters().get(i).asType();

            if (!cinfo.getTypes().isSameType(param1, param2)) {
                if (isSameDeclaredType(param1, param2)) {
                    continue;
                } else if (param2.getKind() == TypeKind.TYPEVAR) {
                    // interface method contains type erasure - see issue #201543
                    if (isParamEquivalentOfErasure(cinfo, method1, method2, param1.toString(), param2.toString())) {
                        continue;
                    }
                }
                return false;
            }
        }
        return true;
    }

    private static boolean isParamEquivalentOfErasure(CompilationInfo cinfo, ExecutableElement sourceMethod,
            ExecutableElement targetMethod, String sourceParam, String targetParam) {
        TypeMirror classTypeMirror = sourceMethod.getEnclosingElement().asType();
        for (TypeMirror typeMirror : cinfo.getTypes().directSupertypes(classTypeMirror)) {
            if (typeMirror instanceof DeclaredType) {
                DeclaredType declaredType = (DeclaredType) typeMirror;
                if (declaredType.asElement().getEnclosedElements().contains(targetMethod)) {
                    HashMap<String, String> argumentNamesMap = getErasureTypesMap(
                            cinfo.getElementUtilities().enclosingTypeElement(targetMethod).getTypeParameters(),
                            declaredType.getTypeArguments());

                    if (sourceParam.equals(argumentNamesMap.get(targetParam))) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    private static HashMap<String, String> getErasureTypesMap(List<? extends TypeParameterElement> erasureArguments,
            List<? extends TypeMirror> typeArguments) {
        HashMap<String, String> result = new HashMap<String, String>(erasureArguments.size());

        for (int i = 0; i < erasureArguments.size(); i++) {
            if (erasureArguments.size() == i || typeArguments.size() == i) {
                return result;
            }
            result.put(erasureArguments.get(i).toString(), typeArguments.get(i).toString());
        }
        return result;
    }

    public static boolean isSameDeclaredType(TypeMirror param1, TypeMirror param2) {
        String classNameParam1 = extractClassNameFromType(param1);
        // classNameParam1 == null when param1 is not declared type
        if (classNameParam1 != null && classNameParam1.equals(extractClassNameFromType(param2))) {
            return true;
        }
        return false;
    }

    /**
     * Says whether given element is subtype of the entered type.
     * @param info compilation info
     * @param element element to be examined
     * @param superType type which is looking for
     * @return {@code true} is the element implements or extends the given superType, {@code false} otherwise
     */
    public static boolean isTypeOf(CompilationInfo info, Element element, String superType) {
        final TypeMirror tm = element.asType();
        List<Element> types = new ArrayList<>();

        Deque<TypeMirror> deque = new ArrayDeque<>();
        deque.add(tm);
        while (!deque.isEmpty()) {
            TypeMirror mirror = deque.pop();
            if (mirror.getKind() == TypeKind.DECLARED) {
                Element el = info.getTypes().asElement(mirror);
                types.add(el);
                if (el.getKind() == ElementKind.CLASS) {
                    TypeElement tel = (TypeElement) el;
                    deque.add(tel.getSuperclass());
                    deque.addAll(tel.getInterfaces());
                } else if (el.getKind() == ElementKind.INTERFACE) {
                    TypeElement tel = (TypeElement) el;
                    for (TypeMirror ifaceMirror : tel.getInterfaces()) {
                        deque.add(ifaceMirror);
                    }
                }
            }
        }

        for (Element type : types) {
            if (superType.equals(type.asType().toString())) {
                return true;
            }
        }
        return false;
    }
}
