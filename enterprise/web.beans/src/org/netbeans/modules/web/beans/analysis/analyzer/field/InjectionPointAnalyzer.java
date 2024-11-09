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

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.TypeMirror;
import javax.swing.text.Document;

import org.netbeans.api.java.source.ElementHandle;
import org.netbeans.modules.j2ee.metadata.model.api.support.annotation.AnnotationHelper;
import org.netbeans.modules.web.beans.analysis.analyzer.AbstractDecoratorAnalyzer;
import org.netbeans.modules.web.beans.analysis.analyzer.AnnotationUtil;
import org.netbeans.modules.web.beans.analysis.analyzer.FieldModelAnalyzer.FieldAnalyzer;
import org.netbeans.modules.web.beans.analysis.analyzer.ModelAnalyzer.Result;
import org.netbeans.modules.web.beans.api.model.CdiException;
import org.netbeans.modules.web.beans.api.model.DependencyInjectionResult;
import org.netbeans.modules.web.beans.api.model.DependencyInjectionResult.ResultKind;
import org.netbeans.modules.web.beans.api.model.InjectionPointDefinitionError;
import org.netbeans.modules.web.beans.api.model.WebBeansModel;
import org.netbeans.modules.web.beans.hints.EditorAnnotationsHelper;
import org.netbeans.spi.editor.hints.Severity;
import org.openide.util.NbBundle;


/**
 * @author ads
 *
 */
public class InjectionPointAnalyzer extends AbstractDecoratorAnalyzer<Void> implements FieldAnalyzer {
    
    /* (non-Javadoc)
     * @see org.netbeans.modules.web.beans.analysis.analyzer.FieldModelAnalyzer.FieldAnalyzer#analyze(javax.lang.model.element.VariableElement, javax.lang.model.type.TypeMirror, javax.lang.model.element.TypeElement, org.netbeans.modules.web.beans.api.model.WebBeansModel, java.util.List, org.netbeans.api.java.source.CompilationInfo, java.util.concurrent.atomic.AtomicBoolean)
     */
    @Override
    public void analyze( final VariableElement element, TypeMirror elementType,
            TypeElement parent, WebBeansModel model,
            AtomicBoolean cancel , 
            Result result )
    {
        try {
            if (model.isInjectionPoint(element) ){
                boolean isDelegate = false;
                result.requireCdiEnabled(element, model);
                checkInjectionPointMetadata( element, elementType , parent, model , 
                        cancel , result );
                checkNamed( element, model , cancel, result);
                if ( cancel.get() ){
                    return;
                }
                if ( !model.isDynamicInjectionPoint(element)) {
                    isDelegate = AnnotationUtil.isDelegate(element, parent, model);
                    if (!checkBuiltInBeans(element, elementType, model, cancel))
                    {
                        DependencyInjectionResult res = model
                                .lookupInjectables(element, null, cancel);
                        checkResult(res, element, model, result);
                        if (isDelegate) {
                            analyzeDecoratedBeans(res, element, null, parent,
                                    model, result);
                        }
                    }
                }
                boolean isEvent = model.isEventInjectionPoint(element);
                if ( isEvent ){
                    ElementHandle<VariableElement> modelHandle = ElementHandle.create(element);
                    EditorAnnotationsHelper helper = EditorAnnotationsHelper.getInstance(
                            result);
                    if ( helper != null ){
                        helper.addEventInjectionPoint( result, 
                                modelHandle.resolve(result.getInfo()));
                    }
                }
                else if ( isDelegate
                        || AnnotationUtil.hasAnnotation(element, AnnotationUtil.DELEGATE_FQN, model.getCompilationController())
                        || AnnotationUtil.hasAnnotation(element, AnnotationUtil.DELEGATE_FQN_JAKARTA, model.getCompilationController())
                )
                {
                    return;
                }
                else {
                    ElementHandle<VariableElement> modelHandle = ElementHandle.create(element);
                    EditorAnnotationsHelper helper = EditorAnnotationsHelper.getInstance(
                            result);
                    if  (helper != null ){
                        helper.addInjectionPoint( result, 
                                modelHandle.resolve(result.getInfo()));
                    }
                }
            }
        }
        catch (InjectionPointDefinitionError e) {
            result.requireCdiEnabled(element, model);
            informInjectionPointDefError(e, element, model, result );
        }
    }
    
    private void checkNamed( VariableElement element, WebBeansModel model,
            AtomicBoolean cancel, Result result )
    {
        if( cancel.get() ){
            return;
        }
        if (
                AnnotationUtil.hasAnnotation(element, AnnotationUtil.NAMED, model.getCompilationController())
                || AnnotationUtil.hasAnnotation(element, AnnotationUtil.NAMED_JAKARTA, model.getCompilationController())
        )
        {
            result.addNotification( Severity.WARNING , element, model,  
                    NbBundle.getMessage(InjectionPointAnalyzer.class, 
                            "WARN_NamedInjectionPoint"));                       // NOI18N
        }
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.web.beans.analysis.analyzer.AbstractDecoratorAnalyzer#addClassError(javax.lang.model.element.VariableElement, java.lang.Object, javax.lang.model.element.TypeElement, org.netbeans.modules.web.beans.api.model.WebBeansModel, org.netbeans.modules.web.beans.analysis.analyzer.ModelAnalyzer.Result)
     */
    @Override
    protected void addClassError( VariableElement element, Void fake, 
            TypeElement decoratedBean, WebBeansModel model, Result result  )
    {
        result.addError( element , model,  
                    NbBundle.getMessage(InjectionPointAnalyzer.class, 
                            "ERR_FinalDecoratedBean",                       // NOI18N
                            decoratedBean.getQualifiedName().toString()));
    }
    
    /* (non-Javadoc)
     * @see org.netbeans.modules.web.beans.analysis.analyzer.AbstractDecoratorAnalyzer#addMethodError(javax.lang.model.element.VariableElement, java.lang.Object, javax.lang.model.element.TypeElement, javax.lang.model.element.Element, org.netbeans.modules.web.beans.api.model.WebBeansModel, org.netbeans.modules.web.beans.analysis.analyzer.ModelAnalyzer.Result)
     */
    @Override
    protected void addMethodError( VariableElement element, Void fake,
            TypeElement decoratedBean, Element decoratedMethod,
            WebBeansModel model, Result result  )
    {
        result.addError(
                element, model,  NbBundle.getMessage(
                        InjectionPointAnalyzer.class,
                        "ERR_FinalMethodDecoratedBean", // NOI18N
                        decoratedBean.getQualifiedName().toString(),
                        decoratedMethod.getSimpleName().toString()));
    }

    private void checkInjectionPointMetadata( VariableElement element,
            TypeMirror elementType , TypeElement parent, WebBeansModel model,
            AtomicBoolean cancel , Result result )
    {
        TypeElement injectionPointType = model.getCompilationController().getElements().getTypeElement(AnnotationUtil.INJECTION_POINT_JAKARTA);
        if (injectionPointType == null) {
            injectionPointType = model.getCompilationController().getElements().getTypeElement(AnnotationUtil.INJECTION_POINT);
        }
        if (injectionPointType == null) {
            return;
        }
        Element varElement = model.getCompilationController().getTypes().asElement( 
                elementType );
        if ( !injectionPointType.equals(varElement)){
            return;
        }
        if ( cancel.get()){
            return;
        }
        List<AnnotationMirror> qualifiers = model.getQualifiers(element, true);
        AnnotationHelper helper = new AnnotationHelper(model.getCompilationController());
        Map<String, ? extends AnnotationMirror> qualifiersFqns = helper.
            getAnnotationsByType(qualifiers);
        boolean hasDefault = model.hasImplicitDefaultQualifier( element );
        if ( !hasDefault && (qualifiersFqns.containsKey(AnnotationUtil.DEFAULT_FQN) || qualifiersFqns.containsKey(AnnotationUtil.DEFAULT_FQN_JAKARTA))){
            hasDefault = true;
        }
        if ( !hasDefault || cancel.get() ){
            return;
        }
        try {
            String scope = model.getScope( parent );
            if ( scope != null && !AnnotationUtil.DEPENDENT.equals( scope ) && !AnnotationUtil.DEPENDENT_JAKARTA.equals( scope )){
                result.addError(element , model,  
                        NbBundle.getMessage(
                        InjectionPointAnalyzer.class, "ERR_WrongQualifierInjectionPointMeta"));            // NOI18N
            }
        }
        catch (CdiException e) {
            // this exception will be handled in the appropriate scope analyzer
            return;
        }
    }

    private void checkResult( DependencyInjectionResult res ,
            VariableElement var, WebBeansModel model,
            Result result  )
    {
        if ( res instanceof DependencyInjectionResult.Error ){
            ResultKind kind = res.getKind();
            Severity severity = Severity.WARNING;
            if ( kind == DependencyInjectionResult.ResultKind.DEFINITION_ERROR){
                severity = Severity.ERROR;
            }
            String message = ((DependencyInjectionResult.Error)res).getMessage();
            result.addNotification(severity, var , model, message);
        }
    }

    private void informInjectionPointDefError(InjectionPointDefinitionError exception , 
            Element element, WebBeansModel model, 
            Result result )
    {
        result.addError(element, model, exception.getMessage());
    }
}