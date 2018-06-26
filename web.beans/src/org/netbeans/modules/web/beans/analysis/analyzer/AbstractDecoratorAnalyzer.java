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
package org.netbeans.modules.web.beans.analysis.analyzer;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.ElementFilter;

import org.netbeans.modules.j2ee.metadata.model.api.support.annotation.AnnotationHelper;
import org.netbeans.modules.web.beans.analysis.analyzer.ModelAnalyzer.Result;
import org.netbeans.modules.web.beans.analysis.analyzer.field.DelegateFieldAnalizer;
import org.netbeans.modules.web.beans.api.model.DependencyInjectionResult;
import org.netbeans.modules.web.beans.api.model.WebBeansModel;


/**
 * @author ads
 *
 */
public abstract class AbstractDecoratorAnalyzer<T> {

    protected void analyzeDecoratedBeans( DependencyInjectionResult res,
            VariableElement element, T t, TypeElement decorator , 
            WebBeansModel model, Result result )
    {
        Set<TypeElement> decoratedBeans = null;
        if ( res instanceof DependencyInjectionResult.ApplicableResult ){
            DependencyInjectionResult.ApplicableResult appResult = 
                (DependencyInjectionResult.ApplicableResult) res;
            decoratedBeans = appResult.getTypeElements();
        }
        else if ( res instanceof DependencyInjectionResult.InjectableResult ){
            Element decorated = ((DependencyInjectionResult.InjectableResult)res).
                getElement();
            if ( decorated instanceof TypeElement ){
                decoratedBeans = Collections.singleton( (TypeElement)decorated);
            }
        }
        if ( decoratedBeans == null ){
            return;
        }
        for( TypeElement decorated : decoratedBeans ){
            Set<Modifier> modifiers = decorated.getModifiers();
            if ( modifiers.contains(Modifier.FINAL)){
                addClassError( element , t , decorated, model , result );
                return;
            }
        }
        if ( decoratedBeans.isEmpty() ){
            return;
        }
        /*
         *  The rule : "If a decorator matches a managed bean with a non-static, 
         *  non-private, final method, the decorator shouldn't also implement that method."
         *  is actually nonsense. 
         *  Each Java class has final wait(). Decorator is also java class 
         *  so it also has a method wait() . So it always implements 
         *  non-static, non-private final method.
         *  I believe one need to care about ONLY methods in decorated types : 
         *  all methods that are defined in interfaces ( decorated types )
         *  and which are implemented in the decorator and decorated bean.
         *  
         *  Here is implementation of this requirement. 
         */
        Collection<TypeMirror> decoratedTypes = DelegateFieldAnalizer
                .getDecoratedTypes(decorator, model.getCompilationController());
        for (TypeMirror typeMirror : decoratedTypes) {
            Element decoratedTypeElement = model.getCompilationController()
                    .getTypes().asElement(typeMirror);
            if (!(decoratedTypeElement instanceof TypeElement)) {
                continue;
            }
            TypeElement iface = (TypeElement) decoratedTypeElement;
            List<ExecutableElement> methods = ElementFilter.methodsIn(iface
                    .getEnclosedElements());
            for (ExecutableElement method : methods) {
                Element decoratorMethod = model.getCompilationController()
                        .getElementUtilities()
                        .getImplementationOf(method, decorator);
                if (decoratorMethod == null) {
                    continue;
                }
                if (decoratorMethod.getModifiers().contains(Modifier.ABSTRACT))
                {
                    continue;
                }
                for (TypeElement decorated : decoratedBeans) {
                    Element decoratedMethod = model.getCompilationController()
                            .getElementUtilities()
                            .getImplementationOf(method, decorated);
                    if (decoratedMethod == null) {
                        continue;
                    }
                    if (decoratedMethod.getModifiers().contains(Modifier.FINAL))
                    {
                        addMethodError( element , t, decorated, decoratedMethod,
                                model , result );
                    }
                }
            }
        }
    }
    
    protected boolean checkBuiltInBeans( VariableElement element,
            TypeMirror elementType, WebBeansModel model, AtomicBoolean cancel )
    {
        TypeElement context = model.getCompilationController().getElements().
            getTypeElement(AnnotationUtil.CONTEXT);
        if ( context != null && context.equals(model.getCompilationController().
                getTypes().asElement(elementType)))
        {
            /* This is built-in javax.enterprise.context.spi.Context bean 
             * provided by container for each scope
             */
            return true;
        }
        if ( cancel.get()){
            return true;
        }
        
        Element varElement = model.getCompilationController().getTypes().
            asElement(elementType);
        if ( varElement instanceof TypeElement ){
            if ( !((TypeElement)varElement).getQualifiedName().contentEquals( 
                    AnnotationUtil.CONVERSATION))
            {
                return false;
            }
        }
        else {
            return false;
        }
        
        if ( model.hasImplicitDefaultQualifier( element ) ){
            return true;
        }
        List<AnnotationMirror> qualifiers = model.getQualifiers(element, true);
        AnnotationHelper helper = new AnnotationHelper(model.getCompilationController());
        Map<String, ? extends AnnotationMirror> qualifiersFqns = helper.
            getAnnotationsByType(qualifiers);
        boolean hasOnlyDefault = false;
        if ( qualifiersFqns.keySet().contains(AnnotationUtil.DEFAULT_FQN)){
            HashSet<String> fqns = new HashSet<String>(qualifiersFqns.keySet());
            fqns.remove( AnnotationUtil.NAMED );
            fqns.remove( AnnotationUtil.ANY );
            hasOnlyDefault = fqns.size() == 1;
        }
        return hasOnlyDefault;
    }
    
    protected abstract void addMethodError( VariableElement element, T t,
            TypeElement decorated, Element decoratedMethod,
            WebBeansModel model, Result result );

    protected abstract void addClassError( VariableElement element , T t, 
            TypeElement decoratedBean, WebBeansModel model, Result result );
    
}
