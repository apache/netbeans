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
package org.netbeans.modules.web.beans.analysis.analyzer.type;

import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.util.ElementFilter;

import org.netbeans.api.java.source.CompilationInfo;
import org.netbeans.modules.web.beans.analysis.CdiAnalysisResult;
import org.netbeans.modules.web.beans.analysis.analyzer.AnnotationUtil;
import org.netbeans.modules.web.beans.analysis.analyzer.ClassElementAnalyzer.ClassAnalyzer;
import org.openide.util.NbBundle;
import org.netbeans.spi.editor.hints.Severity;


/**
 * @author ads
 *
 */
public class AnnotationsAnalyzer implements ClassAnalyzer {
    
    /* (non-Javadoc)
     * @see org.netbeans.modules.web.beans.analysis.analyzer.ClassElementAnalyzer.ClassAnalyzer#analyze(javax.lang.model.element.TypeElement, javax.lang.model.element.TypeElement, org.netbeans.api.java.source.CompilationInfo, java.util.List, java.util.concurrent.atomic.AtomicBoolean)
     */
    @Override
    public void analyze( TypeElement element, TypeElement parent,
            AtomicBoolean cancel, CdiAnalysisResult result )
    {
        checkDecoratorInterceptor( element , cancel, result );
    }

    private void checkDecoratorInterceptor( TypeElement element, 
            AtomicBoolean cancel , CdiAnalysisResult result )
    {
        CompilationInfo compInfo = result.getInfo();
        boolean isDecorator = AnnotationUtil.hasAnnotation(element, 
                AnnotationUtil.DECORATOR, compInfo);
        boolean isInterceptor = AnnotationUtil.hasAnnotation(element, 
                AnnotationUtil.INTERCEPTOR, compInfo);
        if ( isDecorator && isInterceptor ){
            result.addError( element, NbBundle.getMessage(
                        AnnotationsAnalyzer.class, "ERR_DecoratorInterceptor"));// NOI18N
        }
        if ( isDecorator || isInterceptor ){
            result.requireCdiEnabled(element);
            if ( cancel.get() ){
                return;
            }
            checkProducerFields( element , isDecorator , result);
            if ( cancel.get() ){
                return;
            }
            checkMethods( element , isDecorator , result);
            if ( cancel.get() ){
                return;
            }
            checkSession( element , result);
            if ( cancel.get() ){
                return;
            }
            checkNamed( element , result );
            if ( cancel.get() ){
                return;
            }
            checkAlternatives(element , result );
            if ( cancel.get() ){
                return;
            }
            checkSpecializes( element , result );
        }
        if ( isDecorator ){
            if ( cancel.get() ){
                return;
            }
            checkDelegateInjectionPoint(element , result);
        }
    }

    private void checkSpecializes( TypeElement element, CdiAnalysisResult result )
    {
        if ( AnnotationUtil.hasAnnotation(element, AnnotationUtil.SPECIALIZES, 
                result.getInfo()) )
        {
            result.addNotification(Severity.WARNING, element, NbBundle.getMessage(
                    AnnotationsAnalyzer.class,  
                    "WARN_SpecializesInterceptorDecorator")); // NOI18N
        }
    }

    private void checkAlternatives( TypeElement element,
            CdiAnalysisResult result )
    {
        if ( AnnotationUtil.hasAnnotation(element, AnnotationUtil.ALTERNATVE, 
                result.getInfo()))
        {
            result.addNotification(Severity.WARNING, element, NbBundle.getMessage(
                    AnnotationsAnalyzer.class,  
                    "WARN_AlternativeInterceptorDecorator")); // NOI18N
        }        
    }

    private void checkNamed( TypeElement element, CdiAnalysisResult result ) {
        if ( AnnotationUtil.hasAnnotation(element, AnnotationUtil.NAMED, 
                result.getInfo()))
        {
            result.addNotification(Severity.WARNING, element, NbBundle.getMessage(
                    AnnotationsAnalyzer.class,  "WARN_NamedInterceptorDecorator")); // NOI18N
        }
    }

    private void checkDelegateInjectionPoint( TypeElement element,
            CdiAnalysisResult result )
    {
        CompilationInfo compInfo = result.getInfo();
        List<? extends Element> enclosedElements = element.getEnclosedElements();
        int count = 0;
        for (Element child : enclosedElements) {
            if ( child.getKind() == ElementKind.CONSTRUCTOR )
            {
                count +=delegateInjectionPointCount(child, compInfo);
            }
            else if ( ! AnnotationUtil.hasAnnotation(child, AnnotationUtil.INJECT_FQN, 
                    compInfo ))
            {
                continue;
            }
            if ( child.getKind() == ElementKind.FIELD && AnnotationUtil.
                    hasAnnotation(child, AnnotationUtil.DELEGATE_FQN, compInfo ))
            {
                count++;
            }
            else if (  child.getKind() ==ElementKind.METHOD )
            {
                count+=delegateInjectionPointCount(child, compInfo);
            }
        }
        if ( count != 1){
            result.addError( element, NbBundle.getMessage(
                AnnotationsAnalyzer.class,  "ERR_IncorrectDelegateCount")); // NOI18N
        }
    }
    
    private int delegateInjectionPointCount(Element element , 
            CompilationInfo compInfo)
    {
        int result=0;
        ExecutableElement method = (ExecutableElement)element;
        List<? extends VariableElement> parameters = method.getParameters();
        for (VariableElement par : parameters) {
            if ( AnnotationUtil.hasAnnotation(par, AnnotationUtil.DELEGATE_FQN, 
                    compInfo))
            {
                result++;
            }
        }
        return result;
    }

    private void checkSession( TypeElement element,
            CdiAnalysisResult result )
    {
        if ( AnnotationUtil.isSessionBean(element, result.getInfo()) )
        {
            result.addError( element,  NbBundle.getMessage(
                        AnnotationsAnalyzer.class,  "ERR_SesssionBeanID")); // NOI18N
        }
    }

    private void checkMethods( TypeElement element, boolean isDecorator,
            CdiAnalysisResult result )
    {
        CompilationInfo compInfo = result.getInfo();
        List<ExecutableElement> methods = ElementFilter.methodsIn( 
                element.getEnclosedElements());
        for (ExecutableElement method : methods) {
            boolean isProducer = AnnotationUtil.hasAnnotation(method, 
                    AnnotationUtil.PRODUCES_FQN, compInfo);     
            boolean isDisposer = false;
            boolean isObserver = false;
            List<? extends VariableElement> parameters = method.getParameters();
            for (VariableElement param : parameters) {
                if ( AnnotationUtil.hasAnnotation( param , AnnotationUtil.DISPOSES_FQN, 
                        compInfo))
                {
                    isDisposer = true;
                    break;
                }
                if ( AnnotationUtil.hasAnnotation( param , AnnotationUtil.OBSERVES_FQN, 
                        compInfo))
                {
                    isObserver = true;
                    break;
                }
            }
            if ( isProducer || isDisposer || isObserver ){
                result.addError( element, NbBundle.getMessage(
                    AnnotationsAnalyzer.class, getMethodErrorKey(isDecorator, 
                            isProducer, isDisposer) , 
                            method.getSimpleName().toString()));
                break;
            }
        }
    }
    
    private String getMethodErrorKey(boolean isDecorator, boolean isProducer,
            boolean isDisposer )
    {
        String key= null;
        if ( isDecorator ){
            if ( isProducer ){
                key = "ERR_DecoratorHasProducerMethod";             //  NOI18N
            }
            else if ( isDisposer ){
                key = "ERR_DecoratorHasDisposerMethod";             //  NOI18N
            }
            else {
                key = "ERR_DecoratorHasObserverMethod";             //  NOI18N
            }
        }
        else {
            if ( isProducer ){
                key = "ERR_InterceptorHasProducerMethod";             //  NOI18N
            }
            else if ( isDisposer ){
                key = "ERR_InterceptorHasDisposerMethod";             //  NOI18N
            }
            else {
                key = "ERR_InterceptorHasObserverMethod";             //  NOI18N
            }
        }
        return key;
    }

    private void checkProducerFields( TypeElement element, boolean isDecorator,
            CdiAnalysisResult result )
    {
        List<VariableElement> fields = ElementFilter.fieldsIn( 
                element.getEnclosedElements() );
        for (VariableElement field : fields) {
            if ( AnnotationUtil.hasAnnotation(field, AnnotationUtil.PRODUCES_FQN, 
                    result.getInfo()))
            {
                String key= isDecorator ? "ERR_DecoratorHasProducerField":
                    "ERR_IntrerceptorHasProducerField";                 // NOI18N
                result.addError( element, NbBundle.getMessage(
                        AnnotationsAnalyzer.class, key , field.getSimpleName().toString()));
                break;
            }
        }
    }

}
