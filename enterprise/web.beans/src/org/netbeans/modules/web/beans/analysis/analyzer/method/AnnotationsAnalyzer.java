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

import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.ExecutableType;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.type.TypeVariable;

import org.netbeans.api.java.source.CompilationInfo;
import org.netbeans.modules.web.beans.analysis.analyzer.AnnotationUtil;
import org.netbeans.modules.web.beans.analysis.CdiAnalysisResult;
import org.netbeans.modules.web.beans.analysis.analyzer.MethodElementAnalyzer.MethodAnalyzer;
import org.netbeans.modules.web.beans.hints.EditorAnnotationsHelper;
import org.openide.util.NbBundle;

import static org.netbeans.modules.web.beans.analysis.analyzer.AnnotationUtil.DISPOSES_FQN;
import static org.netbeans.modules.web.beans.analysis.analyzer.AnnotationUtil.DISPOSES_FQN_JAKARTA;
import static org.netbeans.modules.web.beans.analysis.analyzer.AnnotationUtil.INJECT_FQN;
import static org.netbeans.modules.web.beans.analysis.analyzer.AnnotationUtil.INJECT_FQN_JAKARTA;
import static org.netbeans.modules.web.beans.analysis.analyzer.AnnotationUtil.OBSERVES_FQN;
import static org.netbeans.modules.web.beans.analysis.analyzer.AnnotationUtil.OBSERVES_FQN_JAKARTA;
import static org.netbeans.modules.web.beans.analysis.analyzer.AnnotationUtil.PRODUCES_FQN;
import static org.netbeans.modules.web.beans.analysis.analyzer.AnnotationUtil.PRODUCES_FQN_JAKARTA;


/**
 * @author ads
 *
 */
public class AnnotationsAnalyzer implements MethodAnalyzer {

    private static final String EJB = "ejb";            // NOI18N

    /* (non-Javadoc)
     * @see org.netbeans.modules.web.beans.analysis.analyzer.MethodElementAnalyzer.MethodAnalyzer#analyze(javax.lang.model.element.ExecutableElement, javax.lang.model.type.TypeMirror, javax.lang.model.element.TypeElement, java.util.concurrent.atomic.AtomicBoolean, org.netbeans.modules.web.beans.analysis.analyzer.ElementAnalyzer.Result)
     */
    @Override
    public void analyze( ExecutableElement element, TypeMirror returnType,
            TypeElement parent, AtomicBoolean cancel , CdiAnalysisResult result )
    {
        checkProductionObserverDisposerInject( element , parent ,
                cancel , result );
        if ( cancel.get()){
            return;
        }
    }

    private void checkProductionObserverDisposerInject(
            ExecutableElement element, TypeElement parent, AtomicBoolean cancel ,
            CdiAnalysisResult result )
    {
        CompilationInfo compInfo = result.getInfo();
        boolean isProducer = AnnotationUtil.hasAnnotation(element, compInfo, PRODUCES_FQN_JAKARTA, PRODUCES_FQN);
        boolean isInitializer = AnnotationUtil.hasAnnotation(element, compInfo, INJECT_FQN_JAKARTA, INJECT_FQN);
        int observesCount = 0;
        int disposesCount = 0;
        List<? extends VariableElement> parameters = element.getParameters();
        for (VariableElement param : parameters) {
            if ( cancel.get() ){
                return;
            }
            if (AnnotationUtil.hasAnnotation(param, compInfo, OBSERVES_FQN_JAKARTA, OBSERVES_FQN))
            {
                observesCount++;
            }
            if (AnnotationUtil.hasAnnotation(param, compInfo, DISPOSES_FQN_JAKARTA, DISPOSES_FQN))
            {
                disposesCount++;
            }
        }
        if ( observesCount >0 ){
            EditorAnnotationsHelper helper = EditorAnnotationsHelper.getInstance(result);
            if ( helper != null ){
                helper.addObserver( result , element );
            }
        }
        String firstAnnotation = null;
        String secondAnnotation = null;
        if ( isProducer ){
            firstAnnotation = AnnotationUtil.PRODUCES;
            if ( isInitializer ){
                secondAnnotation = AnnotationUtil.INJECT;
            }
            else if ( observesCount >0 ){
                secondAnnotation = AnnotationUtil.OBSERVES;
            }
            else if ( disposesCount >0 ){
                secondAnnotation = AnnotationUtil.DISPOSES;
            }
        }
        else if ( isInitializer ){
            firstAnnotation = AnnotationUtil.INJECT;
            if ( observesCount >0 ){
                secondAnnotation = AnnotationUtil.OBSERVES;
            }
            else if ( disposesCount >0 ){
                secondAnnotation = AnnotationUtil.DISPOSES;
            }
        }
        else if ( observesCount >0 ){
            firstAnnotation = AnnotationUtil.OBSERVES;
            if ( disposesCount >0 ){
                secondAnnotation = AnnotationUtil.DISPOSES;
            }
        }
        if ( firstAnnotation != null && secondAnnotation != null  ){
            result.addError( element, NbBundle.getMessage(
                    AnnotationsAnalyzer.class, "ERR_BothAnnotationsMethod", // NOI18N
                    firstAnnotation, secondAnnotation ));
        }

        // Test quantity of observer parameters
        if ( observesCount > 1){
            result.addError( element, NbBundle.getMessage(
                AnnotationsAnalyzer.class, "ERR_ManyObservesParameter" ));   // NOI18N
        }
        // Test quantity of disposes parameters
        else if ( disposesCount >1 ){
            result.addError( element, NbBundle.getMessage(
                AnnotationsAnalyzer.class, "ERR_ManyDisposesParameter"));    // NOI18N
        }

        // A producer/disposer method must be a non-abstract method .
        checkAbstractMethod(element, result, isProducer,
                disposesCount>0);

        checkBusinessMethod( element , result, isProducer,
                disposesCount >0 , observesCount > 0);

        if ( isInitializer ){
            checkInitializerMethod(element, parent , result );
        }
    }

    /**
     *  A producer/disposer/observer non-static method of a session bean class
     *  should be a business method of the session bean.
     */
    private void checkBusinessMethod( ExecutableElement element,
            CdiAnalysisResult result ,boolean isProducer, boolean isDisposer, boolean isObserver )
    {
        CompilationInfo compInfo = result.getInfo();
        if ( !isProducer && !isDisposer && !isObserver ){
            return;
        }
        Set<Modifier> modifiers = element.getModifiers();
        if ( modifiers.contains(Modifier.STATIC) ){
            return;
        }
        TypeElement containingClass = compInfo.getElementUtilities().
            enclosingTypeElement(element);
        if ( !AnnotationUtil.isSessionBean( containingClass, compInfo) ){
            return;
        }
        String methodName = element.getSimpleName().toString();
        boolean isBusinessMethod = true;
        if ( methodName.startsWith(EJB)){
            isBusinessMethod = false;
        }
        if (AnnotationUtil.isLifecycleCallback(element, compInfo)){
            isBusinessMethod = false;
        }
        if ( modifiers.contains(Modifier.FINAL) ||
                !modifiers.contains( Modifier.PUBLIC) )
        {
            isBusinessMethod = false;
        }
        if ( !isBusinessMethod ){
            String key = null;
            if ( isProducer ){
                key = "ERR_ProducerNotBusiness";         // NOI18N
            }
            else if ( isDisposer ){
                key = "ERR_DisposerNotBusiness";         // NOI18N
            }
            else if ( isObserver ){
                key = "ERR_ObserverNotBusiness";         // NOI18N
            }
            result.addError( element, NbBundle.getMessage(
                AnnotationsAnalyzer.class, key));
        }
    }

    private void checkInitializerMethod( ExecutableElement element,
            TypeElement parent, CdiAnalysisResult result )
    {
        Set<Modifier> modifiers = element.getModifiers();
        boolean isAbstract = modifiers.contains( Modifier.ABSTRACT );
        boolean isStatic = modifiers.contains( Modifier.STATIC );
        if (  isAbstract || isStatic ){
            String key = isAbstract? "ERR_AbstractInitMethod":
                "ERR_StaticInitMethod";           // NOI18N
            result.addError( element, NbBundle.getMessage(
                AnnotationsAnalyzer.class, key ));
        }
        TypeMirror method = result.getInfo().getTypes().asMemberOf(
                (DeclaredType)parent.asType() , element);
        if ( method instanceof ExecutableType ){
            List<? extends TypeVariable> typeVariables =
                ((ExecutableType)method).getTypeVariables();
            if (typeVariables != null && !typeVariables.isEmpty()) {
                result.addError( element, NbBundle.getMessage(
                            AnnotationsAnalyzer.class, "ERR_GenericInitMethod" ));// NOI18N
            }
        }
    }

    private void checkAbstractMethod( ExecutableElement element,
            CdiAnalysisResult result ,boolean isProducer, boolean isDisposer )
    {
        if ( isProducer || isDisposer ){
            String key = isProducer? "ERR_AbstractProducerMethod":
                "ERR_AbstractDisposerMethod";           // NOI18N
            Set<Modifier> modifiers = element.getModifiers();
            if ( modifiers.contains( Modifier.ABSTRACT )){
                result.addError( element, NbBundle.getMessage(
                    AnnotationsAnalyzer.class, key ));
            }
        }
    }

}
