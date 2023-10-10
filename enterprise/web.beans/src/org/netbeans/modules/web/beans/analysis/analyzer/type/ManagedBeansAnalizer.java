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
package org.netbeans.modules.web.beans.analysis.analyzer.type;

import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.ElementFilter;

import org.netbeans.api.java.source.ElementHandle;
import org.netbeans.modules.web.beans.analysis.analyzer.AnnotationUtil;
import org.netbeans.modules.web.beans.analysis.analyzer.ClassModelAnalyzer.ClassAnalyzer;
import org.netbeans.modules.web.beans.analysis.analyzer.ModelAnalyzer.Result;
import org.netbeans.modules.web.beans.api.model.WebBeansModel;
import org.netbeans.modules.web.beans.hints.EditorAnnotationsHelper;
import org.netbeans.spi.editor.hints.Severity;
import org.openide.util.NbBundle;


/**
 * @author ads
 *
 */
// @todo: Support JakartaEE
public class ManagedBeansAnalizer implements ClassAnalyzer {
    
    private static final String EXTENSION = "javax.enterprise.inject.spi.Extension";  //NOI18N
    
    /* (non-Javadoc)
     * @see org.netbeans.modules.web.beans.analysis.analyzer.ClassModelAnalyzer.ClassAnalyzer#analyze(javax.lang.model.element.TypeElement, javax.lang.model.element.TypeElement, org.netbeans.modules.web.beans.api.model.WebBeansModel, java.util.List, org.netbeans.api.java.source.CompilationInfo, java.util.concurrent.atomic.AtomicBoolean)
     */
    @Override
    public void analyze( TypeElement element, TypeElement parent,
            WebBeansModel model, AtomicBoolean cancel,
            Result result )
    {
        boolean cdiManaged = model.getQualifiers( element,  true ).size()>0;
        if ( !cdiManaged ){
                return;
        }
        result.requireCdiEnabled(element, model);
        if (cancel.get()) {
            return;
        }
        checkCtor(element, model, result );
        if (cancel.get()) {
            return;
        }
        checkInner(element, parent, model, result);
        if (cancel.get()) {
            return;
        }
        checkAbstract(element, model, result);
        if (cancel.get()) {
            return;
        }
        checkImplementsExtension(element, model, result);
        if (cancel.get()) {
            return;
        }
        checkDecorators( element , model , result );
    }

    private void checkDecorators( TypeElement element, WebBeansModel model,
            Result result )
    {
        Collection<TypeElement> decorators = model.getDecorators(element);
        if ( decorators!= null && decorators.size() >0 ){
            EditorAnnotationsHelper helper = EditorAnnotationsHelper.getInstance(result);
            ElementHandle<TypeElement> handle = ElementHandle.create(element);
            if ( helper != null ){
                helper.addDecoratedBean( result , handle.resolve( result.getInfo() ));
            }
        }
    }

    private void checkImplementsExtension( TypeElement element,
            WebBeansModel model, Result result )
    {
        TypeElement extension = model.getCompilationController().getElements().
            getTypeElement(EXTENSION);
        if ( extension == null ){
            return;
        }
        TypeMirror elementType = element.asType();
        if ( model.getCompilationController().getTypes().isSubtype( 
                elementType,  extension.asType())){
            result.addNotification(Severity.WARNING, element, 
                        model,  NbBundle.getMessage( ManagedBeansAnalizer.class, 
                                "WARN_QualifiedElementExtension"));     // NOI18N
        }
    }

    private void checkAbstract( TypeElement element,
            WebBeansModel model, Result result )
    {
        Set<Modifier> modifiers = element.getModifiers();
        if ( modifiers.contains( Modifier.ABSTRACT )){
            if ( AnnotationUtil.hasAnnotation(element, 
                    AnnotationUtil.DECORATOR, model.getCompilationController()) ){
                return;
            }
                
            // element is abstract and has no Decorator annotation
            result.addNotification( Severity.WARNING, element, model,
                        NbBundle.getMessage(ManagedBeansAnalizer.class, 
                                "WARN_QualifierAbstractClass"));        // NOI18N
        }        
    }

    private void checkInner( TypeElement element, TypeElement parent,
            WebBeansModel model, Result result  )
    {
        if ( parent == null ){
            return;
        }
        Set<Modifier> modifiers = element.getModifiers();
        if ( !modifiers.contains( Modifier.STATIC )){
            result.addError(element, model, 
                    NbBundle.getMessage(ManagedBeansAnalizer.class, 
                    "ERR_NonStaticInnerType")); // NOI18N
        }
    }

    private void checkCtor( TypeElement element, WebBeansModel model,
           Result result )
    {
        List<ExecutableElement> ctors = ElementFilter.constructorsIn( 
                element.getEnclosedElements());
        for (ExecutableElement ctor : ctors) {
            Set<Modifier> modifiers = ctor.getModifiers();
            if ( modifiers.contains( Modifier.PRIVATE )){
                continue;
            }
            List<? extends VariableElement> parameters = ctor.getParameters();
            if ( parameters.size() ==0 ){
                return;
            }
            if ( AnnotationUtil.hasAnnotation(ctor, AnnotationUtil.INJECT_FQN, 
                    model.getCompilationController()))
            {
                return;
            }
        }
        // there is no non-private ctors without params or annotated with @Inject
        result.addNotification( Severity.WARNING, element, model, 
                NbBundle.getMessage(ManagedBeansAnalizer.class, 
                "WARN_QualifierNoCtorClass")); // NOI18N
    }

}
