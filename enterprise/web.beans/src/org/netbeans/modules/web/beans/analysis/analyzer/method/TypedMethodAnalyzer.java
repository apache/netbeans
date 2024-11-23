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
package org.netbeans.modules.web.beans.analysis.analyzer.method;

import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.ExecutableType;
import javax.lang.model.type.PrimitiveType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;

import org.netbeans.api.java.source.CompilationInfo;
import org.netbeans.modules.web.beans.analysis.analyzer.AbstractTypedAnalyzer;
import org.netbeans.modules.web.beans.analysis.analyzer.AnnotationUtil;
import org.netbeans.modules.web.beans.analysis.CdiAnalysisResult;
import org.netbeans.modules.web.beans.analysis.analyzer.MethodElementAnalyzer.MethodAnalyzer;
import org.openide.util.NbBundle;

import static org.netbeans.modules.web.beans.analysis.analyzer.AnnotationUtil.PRODUCES_FQN;
import static org.netbeans.modules.web.beans.analysis.analyzer.AnnotationUtil.PRODUCES_FQN_JAKARTA;


/**
 * @author ads
 *
 */
public class TypedMethodAnalyzer extends AbstractTypedAnalyzer implements
        MethodAnalyzer
{

    /* (non-Javadoc)
     * @see org.netbeans.modules.web.beans.analysis.analyzer.MethodElementAnalyzer.MethodAnalyzer#analyze(javax.lang.model.element.ExecutableElement, javax.lang.model.type.TypeMirror, javax.lang.model.element.TypeElement, java.util.concurrent.atomic.AtomicBoolean, org.netbeans.modules.web.beans.analysis.analyzer.ElementAnalyzer.Result)
     */
    @Override
    public void analyze( ExecutableElement element, TypeMirror returnType,
            TypeElement parent, AtomicBoolean cancel , CdiAnalysisResult result )
    {
        analyze(element, returnType, cancel , result );
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.web.beans.analysis.analyzer.AbstractTypedAnalyzer#addError(javax.lang.model.element.Element, org.netbeans.modules.web.beans.analysis.analyzer.ElementAnalyzer.Result)
     */
    @Override
    protected void addError( Element element, CdiAnalysisResult result )
    {
        result.addError( element, NbBundle.getMessage(
                TypedMethodAnalyzer.class, "ERR_BadRestritedMethodType"));  // NOI18N
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.web.beans.analysis.analizer.AbstractTypedAnalyzer#hasBeanType(javax.lang.model.element.Element, javax.lang.model.type.TypeMirror, javax.lang.model.type.TypeMirror, org.netbeans.api.java.source.CompilationInfo)
     */
    @Override
    protected boolean hasBeanType( Element subject, TypeMirror returnType,
            TypeMirror requiredBeanType, CompilationInfo compInfo )
    {
        return compInfo.getTypes().isSubtype(returnType, requiredBeanType);
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.web.beans.analysis.analyzer.AbstractTypedAnalyzer#checkSpecializes(javax.lang.model.element.Element, javax.lang.model.type.TypeMirror, java.util.List, java.util.concurrent.atomic.AtomicBoolean, org.netbeans.modules.web.beans.analysis.analyzer.ElementAnalyzer.Result)
     */
    @Override
    protected void checkSpecializes( Element element, TypeMirror elementType,
            List<TypeMirror> restrictedTypes, AtomicBoolean cancel , CdiAnalysisResult result )
    {
        CompilationInfo compInfo = result.getInfo();
        if (!AnnotationUtil.hasAnnotation(element, compInfo, PRODUCES_FQN_JAKARTA, PRODUCES_FQN))
        {
            return;
        }
        ExecutableElement method = (ExecutableElement)element;
        ExecutableElement overriddenMethod = compInfo.getElementUtilities().
            getOverriddenMethod(method);
        if ( overriddenMethod == null ){
            return;
        }
        TypeElement clazz = compInfo.getElementUtilities().
            enclosingTypeElement(method);
        TypeMirror superType = clazz.getSuperclass();
        TypeElement superClass = compInfo.getElementUtilities().
            enclosingTypeElement(overriddenMethod);
        if ( !superClass.equals( compInfo.getTypes().asElement( superType))){
            return;
        }
        if ( cancel.get()){
            return;
        }
        List<TypeMirror> restrictedSuper = getRestrictedTypes(overriddenMethod,
                compInfo, cancel);
        if ( cancel.get()){
            return;
        }
        if ( restrictedSuper == null ) {
            if (!hasUnrestrictedOverridenType(elementType,
                    restrictedTypes, compInfo,overriddenMethod, superClass) )
            {
                result.addError( element, NbBundle.getMessage(
                            TypedMethodAnalyzer.class, "ERR_BadSpecializesMethod"));  // NOI18N
            }
        }
        else {
            if (!hasRestrictedType(elementType, restrictedTypes, compInfo,
                    restrictedSuper))
            {
                result.addError( element,  NbBundle.getMessage(
                            TypedMethodAnalyzer.class, "ERR_BadSpecializesMethod"));  // NOI18N
            }
        }
    }

    private boolean hasRestrictedType( TypeMirror elementType,
            List<TypeMirror> restrictedTypes, CompilationInfo compInfo,
            List<TypeMirror> restrictedSuper )
    {
        if ( elementType.getKind() == TypeKind.ARRAY ){
            for( TypeMirror mirror : restrictedSuper ){
                boolean found = false;
                for( TypeMirror restrictedType : restrictedTypes ){
                    if ( compInfo.getTypes().isSameType( restrictedType, mirror)){
                        found = true;
                        break;
                    }
                }
                if ( !found ){
                    return false;
                }
            }
            return true;
        }
        else {
            Set<TypeElement> specializedBeanTypes = getElements(
                    restrictedSuper, compInfo);
            Set<TypeElement> restrictedElements = getElements(restrictedTypes,
                    compInfo);
            restrictedElements.add( compInfo.getElements().getTypeElement(
                    Object.class.getCanonicalName()));
            return restrictedElements.containsAll( specializedBeanTypes );
        }
    }

    private boolean hasUnrestrictedOverridenType( TypeMirror elementType,
            List<TypeMirror> restrictedTypes, CompilationInfo compInfo,
            ExecutableElement overriddenMethod, TypeElement superClass )
    {
        TypeMirror methodType = compInfo.getTypes().asMemberOf(
                (DeclaredType)superClass.asType(), overriddenMethod);
        TypeMirror returnOverriden = ((ExecutableType)methodType).getReturnType();
        if ( elementType.getKind() == TypeKind.ARRAY ){
            for( TypeMirror mirror : restrictedTypes ){
                if ( compInfo.getTypes().isSameType( mirror, returnOverriden)){
                    return true;
                }
            }
            return false;
        }
        else if ( returnOverriden.getKind().isPrimitive() ) {
            TypeElement boxed = compInfo.getTypes().boxedClass(
                    (PrimitiveType)returnOverriden);
            return hasUnrestrictedType(boxed, restrictedTypes, compInfo);
        }
        else if ( returnOverriden instanceof DeclaredType ){
            Element returnElement = compInfo.getTypes().asElement( returnOverriden);
            if ( returnElement instanceof TypeElement ){
                return hasUnrestrictedType((TypeElement)returnElement,
                        restrictedTypes, compInfo);
            }
        }
        return true;
    }

    private boolean hasUnrestrictedType( TypeElement overriden,
            List<TypeMirror> restrictedTypes,CompilationInfo compInfo )
    {
        Set<TypeElement> specializedBeanTypes = getUnrestrictedBeanTypes(
                    overriden, compInfo);
        Set<TypeElement> restrictedElements = getElements(restrictedTypes,
                compInfo);
        restrictedElements.add( compInfo.getElements().getTypeElement(
                Object.class.getCanonicalName()));
        return restrictedElements.containsAll(specializedBeanTypes);
    }

}
