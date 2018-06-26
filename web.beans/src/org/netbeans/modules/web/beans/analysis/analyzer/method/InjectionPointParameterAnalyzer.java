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
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.ExecutableType;
import javax.lang.model.type.TypeMirror;

import org.netbeans.api.java.source.CompilationController;
import org.netbeans.api.java.source.ElementHandle;
import org.netbeans.modules.j2ee.metadata.model.api.support.annotation.AnnotationHelper;
import org.netbeans.modules.web.beans.analysis.CdiEditorAnalysisFactory;
import org.netbeans.modules.web.beans.analysis.analyzer.AbstractDecoratorAnalyzer;
import org.netbeans.modules.web.beans.analysis.analyzer.AnnotationUtil;
import org.netbeans.modules.web.beans.analysis.analyzer.MethodModelAnalyzer.MethodAnalyzer;
import org.netbeans.modules.web.beans.analysis.analyzer.ModelAnalyzer.Result;
import org.netbeans.modules.web.beans.analysis.analyzer.field.InjectionPointAnalyzer;
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
public class InjectionPointParameterAnalyzer 
   extends AbstractDecoratorAnalyzer<ExecutableElement> implements MethodAnalyzer 
{

    /* (non-Javadoc)
     * @see org.netbeans.modules.web.beans.analysis.analyzer.MethodModelAnalyzer.MethodAnalyzer#analyze(javax.lang.model.element.ExecutableElement, javax.lang.model.type.TypeMirror, javax.lang.model.element.TypeElement, org.netbeans.modules.web.beans.api.model.WebBeansModel, java.util.concurrent.atomic.AtomicBoolean, org.netbeans.modules.web.beans.analysis.analyzer.ModelAnalyzer.Result)
     */
    @Override
    public void analyze( ExecutableElement element, TypeMirror returnType,
            TypeElement parent, WebBeansModel model , 
            AtomicBoolean cancel , Result result )
    {
        for (VariableElement var : element.getParameters()) {
            if (cancel.get()) {
                return;
            }
            try {
                if (model.isInjectionPoint(var)) {
                    boolean isDelegate = false;
                    result.requireCdiEnabled(element, model);
                    if ( cancel.get() ){
                        return;
                    }
                    if (!model.isDynamicInjectionPoint(var)) {
                        isDelegate =AnnotationUtil.isDelegate(var, parent, model);
                        if (!checkBuiltInBeans(var,
                                getParameterType(var, element, parent,
                                        model.getCompilationController()),
                                model, cancel))
                        {
                            DependencyInjectionResult res = model
                                    .lookupInjectables(var,
                                            (DeclaredType) parent.asType(), cancel);
                            checkResult(res, element, var, model, result);
                            if (isDelegate) {
                                analyzeDecoratedBeans(res, var, element,
                                        parent, model, result);
                            }
                        }
                    }
                    if ( cancel.get()){
                        return;
                    }
                    checkName(element, var, model,result );
                    if ( cancel.get()){
                        return;
                    }
                    checkInjectionPointMetadata( var, element, parent , model , 
                            cancel , result );
                    if ( isDelegate || AnnotationUtil.hasAnnotation(element, 
                            AnnotationUtil.DELEGATE_FQN, model.getCompilationController()))
                    {
                        return;
                    }
                    else {
                        VariableElement param = CdiEditorAnalysisFactory.
                            resolveParameter(var, element, result.getInfo());
                        EditorAnnotationsHelper helper = EditorAnnotationsHelper.
                            getInstance(result);
                        if ( helper != null ){
                            helper.addInjectionPoint(result, param);
                        }
                    }
            }
            }
            catch( InjectionPointDefinitionError e ){
                result.requireCdiEnabled(element, model);
                informInjectionPointDefError(e, element, model, result );
            }
        }

    }
    
    /* (non-Javadoc)
     * @see org.netbeans.modules.web.beans.analysis.analyzer.AbstractDecoratorAnalyzer#addClassError(javax.lang.model.element.VariableElement, java.lang.Object, javax.lang.model.element.TypeElement, org.netbeans.modules.web.beans.api.model.WebBeansModel, org.netbeans.api.java.source.CompilationInfo, java.util.List)
     */
    @Override
    protected void addClassError( VariableElement element, ExecutableElement method,
            TypeElement decoratedBean, WebBeansModel model,
            Result result )
    {
        result.addError( element , method, model,  
                    NbBundle.getMessage(InjectionPointParameterAnalyzer.class, 
                            "ERR_FinalDecoratedBean",                   // NOI18N
                            decoratedBean.getQualifiedName().toString()));
    }
    
    /* (non-Javadoc)
     * @see org.netbeans.modules.web.beans.analysis.analyzer.AbstractDecoratorAnalyzer#addMethodError(javax.lang.model.element.VariableElement, java.lang.Object, javax.lang.model.element.TypeElement, javax.lang.model.element.Element, org.netbeans.modules.web.beans.api.model.WebBeansModel, org.netbeans.modules.web.beans.analysis.analyzer.ModelAnalyzer.Result)
     */
    @Override
    protected void addMethodError( VariableElement element,
            ExecutableElement method, TypeElement decoratedBean,
            Element decoratedMethod, WebBeansModel model, Result result )
    {
        result.addError(
                element, method, model,  NbBundle.getMessage(
                        InjectionPointParameterAnalyzer.class,
                        "ERR_FinalMethodDecoratedBean", // NOI18N
                        decoratedBean.getQualifiedName().toString(),
                        decoratedMethod.getSimpleName().toString()));
    }
    
    private TypeMirror getParameterType( VariableElement var,
            ExecutableElement element, TypeElement parent , 
            CompilationController controller )
    {
        ExecutableType method = (ExecutableType)controller.getTypes().asMemberOf(
                (DeclaredType)parent.asType(), element);
        List<? extends TypeMirror> parameterTypes = method.getParameterTypes();
        List<? extends VariableElement> parameters = element.getParameters();
        int paramIndex = parameters.indexOf(var);
        return parameterTypes.get(paramIndex);
    }
    
    private void checkInjectionPointMetadata( VariableElement var,
            ExecutableElement method, TypeElement parent, WebBeansModel model,
            AtomicBoolean cancel , Result result )
    {
        TypeElement injectionPointType = model.getCompilationController()
                .getElements().getTypeElement(AnnotationUtil.INJECTION_POINT);
        if (injectionPointType == null) {
            return;
        }
        Element varElement = model.getCompilationController().getTypes()
                .asElement(var.asType());
        if (!injectionPointType.equals(varElement)) {
            return;
        }
        if (cancel.get()) {
            return;
        }
        List<AnnotationMirror> qualifiers = model.getQualifiers(varElement,
                true);
        AnnotationHelper helper = new AnnotationHelper(
                model.getCompilationController());
        Map<String, ? extends AnnotationMirror> qualifiersFqns = helper
                .getAnnotationsByType(qualifiers);
        boolean hasDefault = model.hasImplicitDefaultQualifier(varElement);
        if (!hasDefault
                && qualifiersFqns.keySet().contains(AnnotationUtil.DEFAULT_FQN))
        {
            hasDefault = true;
        }
        if (!hasDefault || cancel.get()) {
            return;
        }
        try {
            String scope = model.getScope(parent);
            if (scope != null && !AnnotationUtil.DEPENDENT.equals(scope)) {
                result.addError(var, method, model, 
                        NbBundle.getMessage(InjectionPointParameterAnalyzer.class,"ERR_WrongQualifierInjectionPointMeta")); // NOI18N
            }
        }
        catch (CdiException e) {
            // this exception will be handled in the appropriate scope analyzer
            return;
        }
    }

    private void checkName( ExecutableElement element, VariableElement var,
            WebBeansModel model, Result result)
    {
        AnnotationMirror annotation = AnnotationUtil.getAnnotationMirror( 
                var , AnnotationUtil.NAMED, model.getCompilationController());
        if ( annotation!= null){
            result.addNotification( Severity.WARNING , var, element , model,  
                        NbBundle.getMessage(InjectionPointAnalyzer.class, 
                                "WARN_NamedInjectionPoint"));                       // NOI18N
            if ( annotation.getElementValues().size() == 0 ){
                result.addError(var, element,  model, 
                        NbBundle.getMessage( InjectionPointParameterAnalyzer.class, 
                                "ERR_ParameterNamedInjectionPoint"));        // NOI18N
            }
        }
    }

    private void checkResult( DependencyInjectionResult res ,
            ExecutableElement method , VariableElement element, WebBeansModel model,
            Result result )
    {
        if ( res instanceof DependencyInjectionResult.Error ){
            ResultKind kind = res.getKind();
            Severity severity = Severity.WARNING;
            if ( kind == DependencyInjectionResult.ResultKind.DEFINITION_ERROR){
                severity = Severity.ERROR;
            }
            String message = ((DependencyInjectionResult.Error)res).getMessage();
            result.addNotification(severity, element , method , 
                        model,  message);
        }
    }

    private void informInjectionPointDefError(InjectionPointDefinitionError exception , 
            Element element, WebBeansModel model, 
            Result result )
    {
        result.addError(element, model, exception.getMessage());
    }

}
