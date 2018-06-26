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
                else if ( isDelegate || AnnotationUtil.hasAnnotation(element, 
                        AnnotationUtil.DELEGATE_FQN, model.getCompilationController()))
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
        if ( AnnotationUtil.hasAnnotation(element, AnnotationUtil.NAMED, 
                model.getCompilationController()) )
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
        TypeElement injectionPointType = model.getCompilationController().
            getElements().getTypeElement(AnnotationUtil.INJECTION_POINT);
        if ( injectionPointType == null ){
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
        if ( !hasDefault && qualifiersFqns.keySet().contains(AnnotationUtil.DEFAULT_FQN)){
            hasDefault = true;
        }
        if ( !hasDefault || cancel.get() ){
            return;
        }
        try {
            String scope = model.getScope( parent );
            if ( scope != null && !AnnotationUtil.DEPENDENT.equals( scope )){
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
