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
package org.netbeans.modules.jakarta.web.beans.analysis.analyzer;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.AnnotationValue;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeMirror;

import org.netbeans.api.java.source.CompilationInfo;
import org.netbeans.modules.jakarta.web.beans.analysis.CdiAnalysisResult;


/**
 * @author ads
 *
 */
public abstract class AbstractTypedAnalyzer {
    
    public void analyze( Element element, TypeMirror elementType, 
            AtomicBoolean cancel , CdiAnalysisResult result )
    {
        CompilationInfo compInfo = result.getInfo();
        List<TypeMirror> list = getRestrictedTypes(element, compInfo, cancel);
        if ( list == null ){
            return;
        }
        result.requireCdiEnabled(element);
        for (TypeMirror type : list) {
            if ( cancel.get()){
                return;
            }
            boolean isSubtype = hasBeanType(element, elementType, type, compInfo);
            if (!isSubtype) {
                addError(element, result );
            }
        }
        // check @Specializes types restriction conformance
        if ( cancel.get()){
            return;
        }
        if ( AnnotationUtil.hasAnnotation(element, AnnotationUtil.SPECIALIZES, 
                compInfo))
        {
            result.requireCdiEnabled(element);
            checkSpecializes( element , elementType , list,  cancel , result );
        }
    }
    
    protected abstract void checkSpecializes( Element element, TypeMirror elementType,
            List<TypeMirror> restrictedTypes, AtomicBoolean cancel , CdiAnalysisResult result );

    protected boolean hasBeanType( Element subject, TypeMirror elementType, 
            TypeMirror requiredBeanType,CompilationInfo compInfo )
    {
        return compInfo.getTypes().isSubtype(elementType, requiredBeanType);
    }
    
    protected abstract void addError ( Element element , 
            CdiAnalysisResult result );

    protected void collectAncestors(TypeElement type , Set<TypeElement> ancestors, 
            CompilationInfo compInfo )
    {
        TypeMirror superclass = type.getSuperclass();
        addAncestor( superclass, ancestors, compInfo);
        List<? extends TypeMirror> interfaces = type.getInterfaces();
        for (TypeMirror interfaze : interfaces) {
            addAncestor(interfaze, ancestors, compInfo);
        }
    }
    
    private void addAncestor( TypeMirror parent , Set<TypeElement> ancestors,
            CompilationInfo compInfo)
    {
        if ( parent == null ){
            return;
        }
        Element parentElement = compInfo.getTypes().asElement( parent );
        if ( parentElement instanceof TypeElement ){
            if ( ancestors.contains( (TypeElement)parentElement))
            {
                return;
            }
            ancestors.add( (TypeElement)parentElement);
            collectAncestors((TypeElement)parentElement, ancestors, compInfo);
        }
    }
    
    protected List<TypeMirror> getRestrictedTypes(Element element, 
            CompilationInfo compInfo , AtomicBoolean cancel)
    {
        AnnotationMirror typedMirror = AnnotationUtil.getAnnotationMirror(element, 
                AnnotationUtil.TYPED, compInfo);
        if ( typedMirror == null ){
            return null;
        }
        Map<? extends ExecutableElement, ? extends AnnotationValue> values = 
            typedMirror.getElementValues();
        AnnotationValue restrictedTypes = null;
        for (Entry<? extends ExecutableElement, ? extends AnnotationValue> entry : 
            values.entrySet() ) 
        {
            ExecutableElement key = entry.getKey();
            AnnotationValue value = entry.getValue();
            if ( AnnotationUtil.VALUE.contentEquals(key.getSimpleName())){ 
                restrictedTypes = value;
                break;
            }
        }
        if ( restrictedTypes == null ){
            return Collections.emptyList();
        }
        if ( cancel.get() ){
            return Collections.emptyList();
        }
        Object value = restrictedTypes.getValue();
        if ( value instanceof List<?> ){
            List<TypeMirror> result = new ArrayList<TypeMirror>(((List<?>)value).size());
            for( Object type : (List<?>)value){
                AnnotationValue annotationValue = (AnnotationValue)type;
                type = annotationValue.getValue();
                if (type instanceof TypeMirror){
                    result.add( (TypeMirror)type);
                }
            }
            return result;
        }
        return Collections.emptyList();
    }
    
    protected Set<TypeElement> getUnrestrictedBeanTypes( TypeElement element,
            CompilationInfo compInfo)
    {
        Set<TypeElement> set = new HashSet<TypeElement>();
        set.add( element );
        collectAncestors(element, set, compInfo);
        return set;
    }
    
    protected Set<TypeElement> getElements( Collection<TypeMirror> types ,
            CompilationInfo info  )
    {
        Set<TypeElement> result = new HashSet<TypeElement>();
        for (TypeMirror typeMirror : types) {
            Element element = info.getTypes().asElement(typeMirror);
            if ( element instanceof TypeElement  ){
                result.add( (TypeElement)element);
            }
        }
        return result;
    }
    
}
