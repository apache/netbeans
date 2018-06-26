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
        boolean isProducer = AnnotationUtil.hasAnnotation(element, 
                AnnotationUtil.PRODUCES_FQN, compInfo);
        boolean isInitializer = AnnotationUtil.hasAnnotation(element, 
                AnnotationUtil.INJECT_FQN, compInfo);
        int observesCount = 0;
        int disposesCount = 0;
        List<? extends VariableElement> parameters = element.getParameters();
        for (VariableElement param : parameters) {
            if ( cancel.get() ){
                return;
            }
            if ( AnnotationUtil.hasAnnotation( param, AnnotationUtil.OBSERVES_FQN, 
                    compInfo))
            {
                observesCount++;
            }
            if ( AnnotationUtil.hasAnnotation( param, AnnotationUtil.DISPOSES_FQN, 
                    compInfo))
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
            if ( typeVariables != null && typeVariables.size() > 0 ){
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
