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
package org.netbeans.modules.web.beans.impl.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.ExecutableType;
import javax.lang.model.type.TypeMirror;

import org.netbeans.api.java.source.CompilationController;
import org.netbeans.modules.j2ee.metadata.model.api.support.annotation.AnnotationModelHelper;
import org.netbeans.modules.web.beans.api.model.CdiException;
import org.netbeans.modules.web.beans.api.model.DependencyInjectionResult;
import org.netbeans.modules.web.beans.impl.model.results.DefinitionErrorResult;
import org.netbeans.modules.web.beans.impl.model.results.ResultImpl;
import org.netbeans.modules.web.beans.model.spi.WebBeansModelProvider;
import org.openide.util.NbBundle;


/**
 * @author ads
 *
 */
// @todo: Support JakartaEE
abstract class ParameterInjectionPointLogic extends FieldInjectionPointLogic 
    implements WebBeansModelProvider 
{
    
    static final String CONTEXT_DEPENDENT_ANNOTATION = 
        "javax.enterprise.context.Dependent";                       // NOI18N

    static final String DISPOSES_ANNOTATION = 
            "javax.enterprise.inject.Disposes";                     // NOI18N
    
    static final String OBSERVES_ANNOTATION = 
            "javax.enterprise.event.Observes";                      // NOI18N
    
    
    ParameterInjectionPointLogic( WebBeansModelImplementation model ) {
        super( model );
    }
    
    protected DependencyInjectionResult findParameterInjectable( VariableElement element , 
            DeclaredType parentType , ResultLookupStrategy strategy, AtomicBoolean cancel ) 
    {
        DeclaredType parent = parentType;
        try {
            parent = getParent(element, parentType);
        }
        catch (DefinitionError e) {
            TypeElement type = e.getElement();
            return new DefinitionErrorResult(element,  parentType, 
                    NbBundle.getMessage(WebBeansModelProviderImpl.class, 
                            "ERR_BadParent", element.getSimpleName(),
                             type!= null? type.toString(): null));
        }
        
        ExecutableElement parentMethod = (ExecutableElement)element.
            getEnclosingElement();
        ExecutableType methodType = (ExecutableType)getCompilationController().
            getTypes().asMemberOf(parent, parentMethod );
        List<? extends TypeMirror> parameterTypes = methodType.getParameterTypes();
        
        boolean isInjectionPoint = false;
        /*
         * Check if method has parameters as injection points.
         * F.e. disposer method has only one parameter with @Disposes annotation.
         * All other its parameters are injection points. 
         */
        List<? extends VariableElement> parameters = parentMethod.getParameters();
        int index =0;
        for (int i=0; i<parameters.size() ; i++ ) {
            VariableElement variableElement = parameters.get(i);
            if ( variableElement.equals( element )){
                index = i;
            }
            else if ( AnnotationObjectProvider.hasAnnotation(variableElement,
                    DISPOSES_ANNOTATION, getModel().getHelper()) ||
                    AnnotationObjectProvider.hasAnnotation(variableElement,
                            OBSERVES_ANNOTATION, getModel().getHelper()) )
            {
                isInjectionPoint = true;
            }
        }
        TypeMirror elementType = strategy.getType( getModel(), parameterTypes.get(index));
        
        DependencyInjectionResult result = null;
        boolean disposes = AnnotationObjectProvider.hasAnnotation( element, 
                DISPOSES_ANNOTATION, getModel().getHelper());
        boolean observes = AnnotationObjectProvider.hasAnnotation( element, 
                OBSERVES_ANNOTATION, getModel().getHelper());
        if ( isInjectionPoint || AnnotationObjectProvider.hasAnnotation( parentMethod, 
                INJECT_ANNOTATION, getModel().getHelper()) ||
                AnnotationObjectProvider.hasAnnotation( parentMethod, 
                        PRODUCER_ANNOTATION, getModel().getHelper()) || disposes||
                        observes)
        {
            result = doFindVariableInjectable(element, elementType , false, cancel );
            isInjectionPoint = true;
        }
        if ( disposes ){
            if( result instanceof ResultImpl ){
                ((ResultImpl) result).getTypeElements().clear();
                Set<Element> productions = ((ResultImpl) result).getProductions();
                TypeElement enclosingTypeElement = getCompilationController().
                    getElementUtilities().enclosingTypeElement(element);
                for (Iterator<Element> iterator = productions.iterator(); 
                    iterator.hasNext(); ) 
                {
                    Element injectable = iterator.next();
                    if ( !(injectable instanceof ExecutableElement) ||
                            !getCompilationController().getElementUtilities().
                                isMemberOf( injectable, enclosingTypeElement))
                    {
                        iterator.remove();
                    }
                }
            }
            else {
                return result;
            }
        }

        if ( isInjectionPoint ){
            return strategy.getResult(getModel(), result, cancel );
        }
        else {
            return new DefinitionErrorResult(element, elementType, 
                    NbBundle.getMessage( WebBeansModelProviderImpl.class, 
                            "ERR_NoInjectPoint" , element.getSimpleName()));
        }
    }
    
    /* (non-Javadoc)
     * @see org.netbeans.modules.web.beans.model.spi.WebBeansModelProvider#isDynamicInjectionPoint(javax.lang.model.element.VariableElement)
     */
    @Override
    public boolean isDynamicInjectionPoint( VariableElement element ) {
        TypeMirror type = getParameterType(element, null, INSTANCE_INTERFACE);
        if ( type != null ){
            try {
                return isInjectionPoint(element);
            }
            catch ( org.netbeans.modules.web.beans.api.model.
                    InjectionPointDefinitionError e )
            {
                return false;
            }
        }
        return false;
    }
    
    /* (non-Javadoc)
     * @see org.netbeans.modules.web.beans.api.model.Result.ResolutionResult#getScope(javax.lang.model.element.Element)
     */
    @Override
    public String getScope( Element element ) throws CdiException {
        return getScope(element , getModel().getHelper());
    }
    
    public static String getScope( Element element, AnnotationModelHelper helper )
            throws CdiException
    {
        String scope = getDeclaredScope(element, helper);
        if (scope != null) {
            return scope;
        }
        List<AnnotationMirror> stereotypes = WebBeansModelProviderImpl
                .getAllStereotypes(element, helper.getHelper());
        for (AnnotationMirror annotationMirror : stereotypes) {
            DeclaredType annotationType = annotationMirror.getAnnotationType();
            Element annotationElement = annotationType.asElement();
            if ( annotationElement == null ){
                continue;
            }
            String declaredScope = getDeclaredScope(annotationElement, helper);
            if (declaredScope == null) {
                continue;
            }
            if (scope == null) {
                scope = declaredScope;
            }
            else if (!scope.equals(declaredScope)) {
                throw new CdiException(NbBundle.getMessage(ParameterInjectionPointLogic.class,
                        "ERR_DefaultScopeCollision", scope, declaredScope)); // NOI18N
            }
        }
        if (scope != null) {
            return scope;
        }
        return CONTEXT_DEPENDENT_ANNOTATION;
    }
    
    static String getDeclaredScope( Element element , 
            AnnotationModelHelper helper ) throws CdiException
    {
        List<? extends AnnotationMirror> annotationMirrors = element.getAnnotationMirrors();
        ScopeChecker scopeChecker = ScopeChecker.get();
        NormalScopeChecker normalScopeChecker = NormalScopeChecker.get();
        String scope = getDeclaredScope(helper, annotationMirrors, scopeChecker, 
                normalScopeChecker, true);
        if ( scope != null ){
            return scope;
        }
        
        annotationMirrors = helper.getCompilationController().getElements().
            getAllAnnotationMirrors( element );
        return getDeclaredScope(helper, annotationMirrors, scopeChecker, 
                normalScopeChecker, false );
    }

    private static String getDeclaredScope( AnnotationModelHelper helper,
            List<? extends AnnotationMirror> annotationMirrors,
            ScopeChecker scopeChecker , NormalScopeChecker normalScopeChecker ,
            boolean singleScopeRequired ) throws CdiException
    {
        List<? extends AnnotationMirror> annotations = annotationMirrors;
        if ( !singleScopeRequired ){
            annotations = new ArrayList<AnnotationMirror>( 
                    annotationMirrors);
            Collections.reverse( annotations );
        }
        String scope = null;
        for (AnnotationMirror annotationMirror : annotations ) {
            String declaredScope = null;
            DeclaredType annotationType = annotationMirror.getAnnotationType();
            Element annotationElement = annotationType.asElement();
            if ( annotationElement instanceof TypeElement ){
                TypeElement annotation = (TypeElement)annotationElement;
                scopeChecker.init(annotation, helper );
                if ( scopeChecker.check() ){
                    declaredScope = annotation.getQualifiedName().toString();
                }
                normalScopeChecker.init( annotation, helper );
                if ( normalScopeChecker.check() ){
                    declaredScope = annotation.getQualifiedName().toString();
                }
                if ( declaredScope != null ){
                    if ( !singleScopeRequired ){
                        return declaredScope;
                    }
                    if ( scope != null ){
                        throw new CdiException(NbBundle.getMessage(
                                ParameterInjectionPointLogic.class, 
                                "ERR_SeveralScopes"));                      // NOI18N
                    }
                    else {
                        scope = declaredScope;
                    }
                }
            }
        }
        return scope;
    }
    
    protected TypeMirror getParameterType( Element element , DeclaredType parentType, 
            String... interfaceFqns) 
    {
        return getParameterType(getCompilationController(),
                element, parentType, interfaceFqns);
    }
    
    static TypeMirror getParameterType( CompilationController controller, 
            Element element , DeclaredType parentType, String... interfaceFqns) 
    {
        TypeMirror elementType = null;
        if ( parentType == null ) {
            elementType = element.asType();
        }
        else {
            elementType = controller.getTypes().asMemberOf(parentType, element);
        }
        return getParameterType(elementType,interfaceFqns);
    }

    static TypeMirror getParameterType( TypeMirror elementType, 
            String... interfaceFqns )
    {
        if ( elementType instanceof DeclaredType ){
            DeclaredType declaredType = (DeclaredType)elementType;
            Element elementDeclaredType = declaredType.asElement();
            if ( elementDeclaredType!= null && 
                    elementDeclaredType.getKind() == ElementKind.INTERFACE )
            {
                String typeFqn = ((TypeElement)elementDeclaredType).
                    getQualifiedName().toString();
                for (String interfaceFqn : interfaceFqns) {
                    if (interfaceFqn.equals(typeFqn)) {
                        List<? extends TypeMirror> typeArguments = declaredType
                                .getTypeArguments();
                        if (typeArguments.size() > 0) {
                            return typeArguments.get(0);
                        }
                    }
                }
            }
        }
        return null;
    }
}
