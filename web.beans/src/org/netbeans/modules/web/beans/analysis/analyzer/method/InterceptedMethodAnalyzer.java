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

import java.lang.annotation.ElementType;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Types;
import org.netbeans.api.java.source.CompilationController;

import org.netbeans.api.java.source.ElementHandle;
import org.netbeans.modules.j2ee.metadata.model.api.support.annotation.AnnotationHelper;
import org.netbeans.modules.web.beans.analysis.analyzer.AbstractInterceptedElementAnalyzer;
import org.netbeans.modules.web.beans.analysis.analyzer.AnnotationUtil;
import org.netbeans.modules.web.beans.analysis.analyzer.MethodModelAnalyzer.MethodAnalyzer;
import org.netbeans.modules.web.beans.analysis.analyzer.ModelAnalyzer.Result;
import org.netbeans.modules.web.beans.analysis.analyzer.annotation.TargetAnalyzer;
import org.netbeans.modules.web.beans.api.model.InterceptorsResult;
import org.netbeans.modules.web.beans.api.model.WebBeansModel;
import org.netbeans.modules.web.beans.hints.EditorAnnotationsHelper;
import org.netbeans.spi.editor.hints.Severity;
import org.openide.util.NbBundle;


/**
 * @author ads
 *
 */
public class InterceptedMethodAnalyzer extends AbstractInterceptedElementAnalyzer 
    implements MethodAnalyzer
{

    /* (non-Javadoc)
     * @see org.netbeans.modules.web.beans.analysis.analyzer.MethodModelAnalyzer.MethodAnalyzer#analyze(javax.lang.model.element.ExecutableElement, javax.lang.model.type.TypeMirror, javax.lang.model.element.TypeElement, org.netbeans.modules.web.beans.api.model.WebBeansModel, java.util.List, org.netbeans.api.java.source.CompilationInfo, java.util.concurrent.atomic.AtomicBoolean)
     */
    @Override
    public void analyze( ExecutableElement element, TypeMirror returnType,
            TypeElement parent, WebBeansModel model,
            AtomicBoolean cancel , Result result )
    {
        boolean hasInterceptorBindings = hasInterceptorBindings(element, model);
        if ( hasInterceptorBindings ){
            result.requireCdiEnabled(element, model);
            EditorAnnotationsHelper helper = EditorAnnotationsHelper.getInstance(result);
            ElementHandle<ExecutableElement> handle = ElementHandle.create(element);
            if ( helper != null ){
                helper.addInterceptedMethod(result, handle.resolve( result.getInfo()));
            }
        }
        if (AnnotationUtil.isLifecycleCallback(element, model.getCompilationController() )) {
            if (hasInterceptorBindings) {
                result.addNotification( Severity.WARNING, element, model,  
                        NbBundle.getMessage(InterceptedMethodAnalyzer.class,
                            "WARN_CallbackInterceptorBinding")); // NOI18N
            }
            if (cancel.get()) {
                return;
            }
            InterceptorsResult interceptorResult = model
                    .getInterceptors(element);
            List<TypeElement> interceptors = interceptorResult
                    .getResolvedInterceptors();
            AnnotationHelper helper = null;
            if ( interceptors.size() >0 ){
                helper = new AnnotationHelper(model.getCompilationController());
            }
            for (TypeElement interceptor : interceptors) {
                if (isLifecycleCallbackInterceptor(interceptor, model.getCompilationController())) {
                    Collection<AnnotationMirror> interceptorBindings = model
                        .getInterceptorBindings(interceptor);
                    for (AnnotationMirror annotationMirror : interceptorBindings) {
                        Element iBinding = model.getCompilationController().getTypes().
                            asElement( annotationMirror.getAnnotationType() );
                        if ( !( iBinding instanceof TypeElement )) {
                            continue;
                        }
                        Set<ElementType> declaredTargetTypes = TargetAnalyzer.
                            getDeclaredTargetTypes(helper, (TypeElement)iBinding);
                        if ( declaredTargetTypes.size() != 1 ||
                                !declaredTargetTypes.contains(ElementType.TYPE))
                        {
                            result.addError(element, model,
                                        NbBundle.getMessage(InterceptedMethodAnalyzer.class,
                                        "ERR_LifecycleInterceptorTarget" ,      // NOI18N
                                        interceptor.getQualifiedName().toString(),
                                        ((TypeElement)iBinding).getQualifiedName().toString()));
                        }
                    }
                }
            }
        }
        if (cancel.get()) {
            return;
        }
                
        Set<Modifier> modifiers = element.getModifiers();
        if ( modifiers.contains( Modifier.STATIC ) || 
                modifiers.contains( Modifier.PRIVATE))
        {
            return;
        }
        boolean finalMethod = modifiers.contains( Modifier.FINAL );
        boolean finalClass = parent.getModifiers().contains( Modifier.FINAL);
        if ( !finalMethod && !finalClass ){
            return;
        }
        if ( cancel.get() ){
            return;
        }
        if ( hasInterceptorBindings){
            if ( finalMethod ){
                result.addError(element, model,  
                            NbBundle.getMessage(
                            InterceptedMethodAnalyzer.class,
                        "ERR_FinalInterceptedMethod")); // NOI18N
            }
            if ( finalClass && !AnnotationUtil.hasAnnotation(parent, 
                    AnnotationUtil.INTERCEPTOR, model.getCompilationController()))
            {
                result.addError(element, model,   
                            NbBundle.getMessage(
                            InterceptedMethodAnalyzer.class,
                        "ERR_FinalInterceptedClass")); // NOI18N
            }
        }
    }
    
    /* (non-Javadoc)
     * @see org.netbeans.modules.web.beans.analysis.analyzer.AbstractInterceptedElementAnalyzer#getInterceptorBindings(javax.lang.model.element.Element, org.netbeans.modules.web.beans.api.model.WebBeansModel)
     */
    @Override
    protected Set<AnnotationMirror> getInterceptorBindings( Element element,
            WebBeansModel model )
    {
        Set<AnnotationMirror> iBindings = super.getInterceptorBindings(element, model);
        List<? extends AnnotationMirror> annotations = model
                .getCompilationController().getElements()
                .getAllAnnotationMirrors(element);
        iBindings.retainAll(annotations);
        return iBindings;
    }

    private static boolean isLifecycleCallbackInterceptor(TypeElement interceptor, CompilationController info) {
        for (Element e : interceptor.getEnclosedElements()) {
            if (e.getKind() == ElementKind.METHOD && e instanceof ExecutableElement) {
                if (AnnotationUtil.isLifecycleCallback((ExecutableElement) e, info)) {
                    return true;
                }
            }
        }
        TypeMirror tm = interceptor.getSuperclass();
        if (tm.getKind() == TypeKind.DECLARED) {
            Element e = info.getTypes().asElement(tm);
            if (e instanceof TypeElement) {
                return isLifecycleCallbackInterceptor((TypeElement) e, info);
            }
        }

        return false;
    }
}
