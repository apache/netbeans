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
package org.netbeans.modules.jakarta.web.beans.analysis.analyzer.annotation;

import java.lang.annotation.ElementType;
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

import org.netbeans.modules.j2ee.metadata.model.api.support.annotation.AnnotationHelper;
import org.netbeans.modules.jakarta.web.beans.analysis.analyzer.AbstractScopedAnalyzer;
import org.netbeans.modules.jakarta.web.beans.analysis.analyzer.AnnotationModelAnalyzer.AnnotationAnalyzer;
import org.netbeans.modules.jakarta.web.beans.analysis.analyzer.AnnotationUtil;
import org.netbeans.modules.jakarta.web.beans.analysis.analyzer.ModelAnalyzer.Result;
import org.netbeans.modules.jakarta.web.beans.api.model.WebBeansModel;
import org.netbeans.modules.jakarta.web.beans.impl.model.WebBeansModelProviderImpl;
import org.openide.util.NbBundle;
import org.netbeans.spi.editor.hints.Severity;


/**
 * @author ads
 *
 */
public class StereotypeAnalyzer extends AbstractScopedAnalyzer implements AnnotationAnalyzer {
    
    
    /* (non-Javadoc)
     * @see org.netbeans.modules.jakarta.web.beans.analysis.analyzer.AnnotationModelAnalyzer.AnnotationAnalyzer#analyze(javax.lang.model.element.TypeElement, org.netbeans.modules.jakarta.web.beans.api.model.WebBeansModel, java.util.List, org.netbeans.api.java.source.CompilationInfo, java.util.concurrent.atomic.AtomicBoolean)
     */
    @Override
    public void analyze( TypeElement element, WebBeansModel model ,
            AtomicBoolean cancel , 
            Result result)
    {
        boolean isStereotype = AnnotationUtil.hasAnnotation(element, 
                AnnotationUtil.STEREOTYPE_FQN, model.getCompilationController());
        if ( !isStereotype ){
            return;
        }
        result.requireCdiEnabled(element, model);
        if ( cancel.get() ){
            return;
        }
        analyzeScope((Element)element, model, cancel, result );
        if (cancel.get() ){
            return;
        }
        checkName( element, model, result );
        if ( cancel.get() ){
            return;
        }
        Set<ElementType> targets = checkDefinition( element , model, result );
        if ( cancel.get() ){
            return;
        }
        checkInterceptorBindings( element , targets, model , result );
        if ( cancel.get() ){
            return;
        }
        checkTransitiveStereotypes( element , targets, model , result );
        if ( cancel.get() ){
            return;
        }
        checkTyped( element , model , result );
        if ( cancel.get() ){
            return;
        }
        checkQualifers( element , model, result );
    }

    private void checkQualifers( TypeElement element, WebBeansModel model,
            Result result )
    {
        List<AnnotationMirror> qualifiers = model.getQualifiers(element, true);
        for (AnnotationMirror annotationMirror : qualifiers) {
            Element annotation = annotationMirror.getAnnotationType().asElement();
            if ( annotation instanceof TypeElement && 
                    ((TypeElement)annotation).getQualifiedName().contentEquals( 
                            AnnotationUtil.NAMED))
            {
                continue;
            }
            else {
                result.addNotification( Severity.WARNING , element, model,  
                        NbBundle.getMessage(StereotypeAnalyzer.class, 
                                "WARN_QualifiedStereotype"));            // NOI18N
                break;
            }
        }        
    }

    private void checkTyped( TypeElement element, WebBeansModel model,
            Result result )
    {
        AnnotationMirror typed = AnnotationUtil.getAnnotationMirror(element, 
                model.getCompilationController(), AnnotationUtil.TYPED);
        if ( typed != null ){
            result.addNotification( Severity.WARNING , element, model,  
                    NbBundle.getMessage(StereotypeAnalyzer.class, 
                            "WARN_TypedStereotype"));            // NOI18N
        }
    }

    private void checkTransitiveStereotypes( TypeElement element,
            final Set<ElementType> targets, WebBeansModel model,
            Result result )
    {
        AnnotationHelper helper = new AnnotationHelper(
                model.getCompilationController());
        List<AnnotationMirror> stereotypes = WebBeansModelProviderImpl
                .getAllStereotypes(element, helper);
        for (AnnotationMirror stereotypeAnnotation : stereotypes) {
            Element annotationElement = stereotypeAnnotation
                    .getAnnotationType().asElement();
            if (annotationElement instanceof TypeElement) {
                TypeElement stereotype = (TypeElement) annotationElement;
                Set<ElementType> declaredTargetTypes = TargetAnalyzer
                        .getDeclaredTargetTypes(helper, stereotype);
                if (declaredTargetTypes != null
                        && declaredTargetTypes.size() == 1
                        && declaredTargetTypes.contains(ElementType.TYPE))
                {
                    if (targets.size() == 1
                            && targets.contains(ElementType.TYPE))
                    {
                        continue;
                    }
                    else {
                        String fqn = stereotype.getQualifiedName().toString();
                        result.addError(element, model,  
                                        NbBundle.getMessage(
                                                StereotypeAnalyzer.class,
                                                "ERR_IncorrectTransitiveTarget",    // NOI18N
                                                fqn));
                    }
                }
            }
        }
    }

    private void checkInterceptorBindings( TypeElement element,
            Set<ElementType> targets, WebBeansModel model,
            Result result )
    {
        if (targets == null) {
            return;
        }
        if (targets.size() == 1 && targets.contains(ElementType.TYPE)) {
            return;
        }
        int interceptorsCount = model.getInterceptorBindings(element).size();
        if (interceptorsCount != 0) {
            result.addError(element,model,  
                            NbBundle.getMessage(StereotypeAnalyzer.class,
                                    "ERR_IncorrectTargetWithInterceptorBindings")); // NOI18N
        }
    }

    private Set<ElementType> checkDefinition( TypeElement element,
            WebBeansModel model , Result result )
    {
        StereotypeTargetAnalyzer analyzer = new StereotypeTargetAnalyzer(element, 
                model, result );
        if ( !analyzer.hasRuntimeRetention()){
            result.addError( element, model,   
                    NbBundle.getMessage(StereotypeAnalyzer.class, 
                            INCORRECT_RUNTIME));
        }
        if ( !analyzer.hasTarget()){
            result.addError( element, model,   
                        NbBundle.getMessage(StereotypeAnalyzer.class, 
                                "ERR_IncorrectStereotypeTarget"));                // NOI18N
            return null;
        }
        else {
            return analyzer.getDeclaredTargetTypes();
        }
    }

    private void checkName( TypeElement element, WebBeansModel model,
            Result result  )
    {
        AnnotationMirror named = AnnotationUtil.getAnnotationMirror(element, 
                AnnotationUtil.NAMED , model.getCompilationController());
        if ( named == null ){
            return;
        }
        Map<? extends ExecutableElement, ? extends AnnotationValue> members = 
            named.getElementValues();
        for (Entry<? extends ExecutableElement, ? extends AnnotationValue> entry: 
            members.entrySet()) 
        {
            ExecutableElement member = entry.getKey();
            if ( member.getSimpleName().contentEquals(AnnotationUtil.VALUE)){ 
                result.addError( element, model,  
                    NbBundle.getMessage(StereotypeAnalyzer.class, 
                            "ERR_NonEmptyNamedStereotype"));            // NOI18N
            }
        }
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.jakarta.web.beans.analysis.analyzer.AbstractScopedAnalyzer#checkScope(javax.lang.model.element.TypeElement, javax.lang.model.element.Element, org.netbeans.modules.jakarta.web.beans.api.model.WebBeansModel, java.util.concurrent.atomic.AtomicBoolean, org.netbeans.modules.jakarta.web.beans.analysis.analyzer.ModelAnalyzer.Result)
     */
    @Override
    protected void checkScope( TypeElement scopeElement, Element element,
            WebBeansModel model, AtomicBoolean cancel , Result result )
    {
    }

    private static class StereotypeTargetAnalyzer extends CdiAnnotationAnalyzer{

        StereotypeTargetAnalyzer( TypeElement element, WebBeansModel model,
                Result result )
        {
            super(element, model, result );
        }

        /* (non-Javadoc)
         * @see org.netbeans.modules.jakarta.web.beans.analysis.analizer.annotation.CdiAnnotationAnalyzer#getCdiMetaAnnotation()
         */
        @Override
        protected String getCdiMetaAnnotation() {
            return AnnotationUtil.STEREOTYPE;
        }

        /* (non-Javadoc)
         * @see org.netbeans.modules.jakarta.web.beans.analysis.analizer.annotation.TargetAnalyzer#getTargetVerifier()
         */
        @Override
        protected TargetVerifier getTargetVerifier() {
            return StereotypeVerifier.getInstance();
        }
        
    }
    
}
