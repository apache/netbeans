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
package org.netbeans.modules.web.beans.analysis.analyzer.field;

import java.io.Serializable;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.TypeMirror;

import org.netbeans.api.java.source.CompilationInfo;
import org.netbeans.modules.web.beans.analysis.analyzer.AnnotationUtil;
import org.netbeans.modules.web.beans.analysis.CdiAnalysisResult;
import org.netbeans.modules.web.beans.analysis.analyzer.FieldElementAnalyzer.FieldAnalyzer;
import org.netbeans.modules.web.beans.hints.EditorAnnotationsHelper;
import org.openide.util.NbBundle;

import static org.netbeans.modules.web.beans.analysis.analyzer.AnnotationUtil.DECORATOR;
import static org.netbeans.modules.web.beans.analysis.analyzer.AnnotationUtil.DECORATOR_JAKARTA;
import static org.netbeans.modules.web.beans.analysis.analyzer.AnnotationUtil.DELEGATE_FQN;
import static org.netbeans.modules.web.beans.analysis.analyzer.AnnotationUtil.DELEGATE_FQN_JAKARTA;
import static org.netbeans.modules.web.beans.analysis.analyzer.AnnotationUtil.INJECT_FQN;
import static org.netbeans.modules.web.beans.analysis.analyzer.AnnotationUtil.INJECT_FQN_JAKARTA;


/**
 * @author ads
 *
 */
public class DelegateFieldAnalizer implements FieldAnalyzer {

    /* (non-Javadoc)
     * @see org.netbeans.modules.web.beans.analysis.analyzer.FieldElementAnalyzer.FieldAnalyzer#analyze(javax.lang.model.element.VariableElement, javax.lang.model.type.TypeMirror, javax.lang.model.element.TypeElement, org.netbeans.api.java.source.CompilationInfo, java.util.List, java.util.concurrent.atomic.AtomicBoolean)
     */
    @Override
    public void analyze( VariableElement element, TypeMirror elementType,
            TypeElement parent, AtomicBoolean cancel,
            CdiAnalysisResult result )
    {
        CompilationInfo compInfo = result.getInfo();
        if (!AnnotationUtil.hasAnnotation(element, compInfo, DELEGATE_FQN_JAKARTA, DELEGATE_FQN))
        {
            return;
        }
        result.requireCdiEnabled(element);
        if (!AnnotationUtil.hasAnnotation(element, compInfo, INJECT_FQN_JAKARTA, INJECT_FQN))
        {
            result.addError(element,  NbBundle.getMessage(
                            DelegateFieldAnalizer.class,
                            "ERR_DelegateHasNoInject"));        // NOI18N
        }
        Element clazz = element.getEnclosingElement();
        if (! AnnotationUtil.hasAnnotation(clazz, compInfo, DECORATOR_JAKARTA, DECORATOR))
        {
            result.addError(element,  NbBundle.getMessage(
                            DelegateFieldAnalizer.class,
                            "ERR_DelegateIsNotInDecorator"));   // NOI18N
        }
        EditorAnnotationsHelper helper = EditorAnnotationsHelper.getInstance(
                result);
        if ( helper != null ){
            helper.addDelegate(result, element );
        }
        if ( cancel.get()){
            return;
        }
        checkDelegateType(element, elementType, parent, result );
    }

    private void checkDelegateType( VariableElement element,
            TypeMirror elementType, TypeElement parent,
            CdiAnalysisResult result  )
    {
        Collection<TypeMirror> decoratedTypes = getDecoratedTypes( parent ,
                result.getInfo() );
        for (TypeMirror decoratedType : decoratedTypes) {
            if ( !result.getInfo().getTypes().isSubtype( elementType,decoratedType )){
                result.addError(element, NbBundle.getMessage(
                        DelegateFieldAnalizer.class,
                        "ERR_DelegateTypeHasNoDecoratedType"));   // NOI18N
                return;
            }
        }
    }

    public static Collection<TypeMirror> getDecoratedTypes( TypeElement element ,
            CompilationInfo info )
    {
        TypeElement serializable = info.getElements().getTypeElement(
                Serializable.class.getCanonicalName());
        Collection<TypeMirror> result = new LinkedList<>();
        collectDecoratedTypes( element.asType() , result , serializable, info );
        return result;
    }

    private static void collectDecoratedTypes( TypeMirror type,
            Collection<TypeMirror> result, TypeElement serializable,
            CompilationInfo info)
    {
        List<? extends TypeMirror> directSupertypes = info.getTypes().
            directSupertypes(type);
        for (TypeMirror superType : directSupertypes) {
            Element element = info.getTypes().asElement(superType);
            if( element == null || element.equals( serializable)  )
            {
                continue;
            }
            if ( element.getKind() == ElementKind.INTERFACE ){
                result.add( superType );
            }
            collectDecoratedTypes(superType, result, serializable, info);
        }
    }

}
