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

import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.type.TypeVariable;
import javax.lang.model.type.WildcardType;
import javax.lang.model.util.ElementFilter;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;

import org.netbeans.modules.j2ee.metadata.model.api.support.annotation.AnnotationModelHelper;
import org.netbeans.modules.web.beans.analysis.analyzer.AnnotationUtil;
import org.netbeans.modules.web.beans.api.model.BeansModel;
import org.netbeans.modules.web.beans.api.model.CdiException;
import org.netbeans.modules.web.beans.api.model.DependencyInjectionResult;
import org.netbeans.modules.web.beans.impl.model.results.ErrorImpl;
import org.netbeans.modules.web.beans.impl.model.results.InjectableResultImpl;
import org.netbeans.modules.web.beans.impl.model.results.InjectablesResultImpl;
import org.netbeans.modules.web.beans.impl.model.results.ResolutionErrorImpl;
import org.netbeans.modules.web.beans.impl.model.results.ResultImpl;
import org.openide.util.NbBundle;


/**
 * @author ads
 *
 */
class EnableBeansFilter {

    private static final String EXTENSION = "javax.enterprise.inject.spi.Extension";// NOI18N
    private static final String EXTENSION_JAKARTA = "jakarta.enterprise.inject.spi.Extension";// NOI18N

    private static final Set<String> predefinedBeans = Set.of(
            // Java Servlet
            "javax.servlet.http.HttpServletRequest",//NOI18N
            "javax.servlet.http.HttpSession",//NOI18N
            "javax.servlet.ServletContext",//NOI18N
            // Java Message Service
            "javax.jms.JMSContext",//NOI18N
            // Java CDI
            EventInjectionPointLogic.EVENT_INTERFACE,
            AnnotationUtil.INJECTION_POINT,
            "javax.enterprise.inject.spi.BeanManager",//NOI18N
            // Jakarta Transaction
            "javax.transaction.UserTransaction",//NOI18N
            // Jakarta Security (javax package)
            "java.security.Principal",//NOI18N
            "java.security.enterprise.SecurityContext",//NOI18N
            // Java Bean Validation
            "javax.validation.Validator",//NOI18N
            "javax.validation.ValidatorFactory",//NOI18N
            // JSF
            "javax.faces.application.Application",//NOI18N
            "javax.faces.application.ResourceHandler",//NOI18N
            "javax.faces.component.UIViewRoot",//NOI18N
            "javax.faces.context.ExternalContext",//NOI18N
            "javax.faces.context.FacesContext",//NOI18N
            "javax.faces.context.Flash",//NOI18N
            // Jakarta Servlet
            "jakarta.servlet.http.HttpServletRequest",//NOI18N
            "jakarta.servlet.http.HttpSession",//NOI18N
            "jakarta.servlet.ServletContext",//NOI18N
            // Jakarta Messaging
            "jakarta.jms.JMSContext",//NOI18N
            // Jakarta CDI
            EventInjectionPointLogic.EVENT_INTERFACE_JAKARTA,
            AnnotationUtil.INJECTION_POINT_JAKARTA,
            "jakarta.enterprise.inject.spi.BeanManager",//NOI18N
            // Jakarta Transaction
            "jakarta.transaction.UserTransaction",//NOI18N
            // Jakarta Security (jakarta package)
            "jakarta.security.enterprise.SecurityContext",//NOI18N
            // Jakarta Validation
            "jakarta.validation.Validator",//NOI18N
            "jakarta.validation.ValidatorFactory",//NOI18N
            // Jakarta Faces
            "jakarta.faces.application.Application",//NOI18N
            "jakarta.faces.application.ResourceHandler",//NOI18N
            "jakarta.faces.component.UIViewRoot",//NOI18N
            "jakarta.faces.context.ExternalContext",//NOI18N
            "jakarta.faces.context.FacesContext",//NOI18N
            "jakarta.faces.context.Flash",//NOI18N
            "jakarta.faces.flow.Flow"//NOI18N
    );

    private static final Set<String> predefinedMapAnnotations = Set.of(
            // JSF
            "javax.faces.annotation.ApplicationMap", //NOI18N
            "javax.faces.annotation.FlowMap", //NOI18N
            "javax.faces.annotation.HeaderMap", //NOI18N
            "javax.faces.annotation.HeaderValuesMap", //NOI18N
            "javax.faces.annotation.InitParameterMap", //NOI18N
            "javax.faces.annotation.RequestCookieMap", //NOI18N
            "javax.faces.annotation.RequestMap", //NOI18N
            "javax.faces.annotation.RequestParameterMap", //NOI18N
            "javax.faces.annotation.RequestParameterValuesMap", //NOI18N
            "javax.faces.annotation.SessionMap", //NOI18N
            "javax.faces.annotation.ViewMap", //NOI18N
            // Jakarta Faces
            "jakarta.faces.annotation.ApplicationMap", //NOI18N
            "jakarta.faces.annotation.FlowMap", //NOI18N
            "jakarta.faces.annotation.HeaderMap", //NOI18N
            "jakarta.faces.annotation.HeaderValuesMap", //NOI18N
            "jakarta.faces.annotation.InitParameterMap", //NOI18N
            "jakarta.faces.annotation.RequestCookieMap", //NOI18N
            "jakarta.faces.annotation.RequestMap", //NOI18N
            "jakarta.faces.annotation.RequestParameterMap", //NOI18N
            "jakarta.faces.annotation.RequestParameterValuesMap", //NOI18N
            "jakarta.faces.annotation.SessionMap", //NOI18N
            "jakarta.faces.annotation.ViewMap" //NOI18N
    );

    private static final Map<String, String> predefinedBeanAnnotationPairs = Map.of(
            // JSF
            "javax.faces.flow.builder.FlowBuilder", "javax.faces.flow.builder.FlowBuilderParameter",//NOI18N
            // Jakarta Faces
            "jakarta.faces.flow.builder.FlowBuilder", "jakarta.faces.flow.builder.FlowBuilderParameter"//NOI18N
    );

    private static final Set<String> managedPropertyAnnotations = Set.of(
            // JSF
            "javax.faces.annotation.ManagedProperty", //NOI18N
            // Jakarta Faces
            "jakarta.faces.annotation.ManagedProperty" //NOI18N
    );

    private Set<Element> myAlternatives;
    private Set<Element> myEnabledAlternatives;
    private final ResultImpl myResult;
    private final AnnotationModelHelper myHelper;
    private final BeansModel myBeansModel;
    private final WebBeansModelImplementation myModel;
    private final boolean isProgrammatic;

    EnableBeansFilter(ResultImpl result, WebBeansModelImplementation model ,
            boolean programmatic )
    {
        myResult = result;
        myHelper = model.getHelper();
        myBeansModel = model.getBeansModel();
        myModel = model;
        isProgrammatic = programmatic;
    }

    DependencyInjectionResult filter(AtomicBoolean cancel){
        myAlternatives = new HashSet<>();
        myEnabledAlternatives = new HashSet<>();

        PackagingFilter filter = new PackagingFilter(getWebBeansModel());
        Set<TypeElement> typeElements = getResult().getTypeElements();

        TypeElement firstElement = !typeElements.isEmpty() ? typeElements.iterator().next() : null;

        // remove elements defined in compile class path which doesn't have beans.xml
        filter.filter( typeElements, cancel );
        for (TypeElement typeElement : typeElements) {
            if ( getResult().isAlternative(typeElement)){
                myAlternatives.add( typeElement );
                addEnabledAlternative( typeElement , typeElement);
            }
        }
        // remove elements defined in compile class path which doesn't have beans.xml
        Set<Element> productions = packagedFilterProductions ( );

        for (Element element : productions) {
            TypeElement enclosingTypeElement = myHelper.getCompilationController().
                getElementUtilities().enclosingTypeElement(element);
            if ( getResult().isAlternative(element)){
                myAlternatives.add( element );
                addEnabledAlternative( enclosingTypeElement , element );
            }
        }

        Set<Element> enabledTypeElements = new HashSet<>( typeElements );
        Set<Element> enabledProductions = new HashSet<>( productions );
        myAlternatives.removeAll(myEnabledAlternatives);
        // now myAlternative contains only disabled alternatives.
        enabledProductions.removeAll( myAlternatives );
        enabledTypeElements.removeAll( myAlternatives );

        int typesSize = enabledTypeElements.size();
        int productionsSize = enabledProductions.size();

        // filter enabled/disabled beans
        Set<Element> enabledTypes = findEnabledTypes( enabledTypeElements );
        findEnabledProductions( enabledProductions);
        int commonSize = enabledTypes.size() + enabledProductions.size();
        if ( commonSize == 1 ){
            Element injectable = enabledTypes.isEmpty() ?
                    enabledProductions.iterator().next():
                        enabledTypes.iterator().next();
            enabledTypes.addAll( enabledProductions);
            return new InjectableResultImpl( getResult(), injectable, enabledTypes );
        }
        if ( commonSize ==0 ){
            //no implementation on classpath/sources or it's fileterd by common logic(for usual beans)
            //first check if we have a class in white list (i.e. must be implemented in ee7 environment)
            String nm = myResult.getVariableType().toString();
            if (nm.startsWith("javax.") || nm.startsWith("java.") || nm.startsWith("jakarta.") //NOI18N
                    || hasManagedPropertyAnnotation(getResult().getVariable())) {
                InjectableResultImpl res = handleEESpecificImplementations(getResult(), firstElement, enabledTypes);
                if (res != null) {
                    return res;
                }
            }
            //
            if ( typeElements.isEmpty() && productions.isEmpty() ){
                return new ErrorImpl(getResult().getVariable(),
                        getResult().getVariableType(), NbBundle.getMessage(
                                EnableBeansFilter.class, "ERR_NoFound"));   // NOI18N
            }
            if ( typesSize==0 && productionsSize == 0 )
            {
                /* no elements was eliminated after check for "enabling"
                 * ( by the spec ). So they are all alternatives that
                 * was not turned on in beans.xml.
                 */
                return new ResolutionErrorImpl(getResult(), NbBundle.getMessage(
                        EnableBeansFilter.class, "ERR_AlternativesOnly"));  // NOI18N
            }
            return new ResolutionErrorImpl( getResult(),  NbBundle.getMessage(
                    EnableBeansFilter.class, "ERR_NoEnabledBeans"));        // NOI18N
        }
        Set<Element> allElements = new HashSet<>( enabledTypes );
        allElements.addAll( enabledProductions );
        allElements.retainAll( myEnabledAlternatives );
        boolean hasSingleAlternative = allElements.size() == 1;
        if ( hasSingleAlternative ){
            /*
             * Spec : When an ambiguous dependency exists, the container attempts
             * to resolve the ambiguity:
             * - If any matching beans are alternatives, the container
             * eliminates all matching beans that are not alternatives.
             * If there is exactly one bean remaining, the container will select
             * this bean, and the ambiguous dependency is called resolvable.
             */
            enabledTypes.addAll( enabledProductions);
            return new InjectableResultImpl( getResult(),
                    allElements.iterator().next(), enabledTypes );
        }

        enabledTypes.addAll( enabledProductions);
        if ( isProgrammatic ){
            return new InjectablesResultImpl(getResult() , enabledTypes );
        }
        else {
            String message = NbBundle.getMessage(EnableBeansFilter.class,
                    "ERR_UnresolvedAmbiguousDependency");           // NOI81N
            return new ResolutionErrorImpl(getResult(), message, enabledTypes);
        }
    }

    /*
     * This method should filter production elements which are defined
     * in the classes inside compile class path without beans.xml.
     * But NB doesn't perform indexing and search for fields and methods
     * inside compile class path at all so there will be no production
     * elements inside compile class path.
     * So I commented out this block of logic to avoid wasting time .
     */
    private Set<Element> packagedFilterProductions() {
        return getResult().getProductions();
        /*Map<Element, List<DeclaredType>> productions =
            getResult().getAllProductions();
        List<Element> filtered = new ArrayList<Element>( productions.size());
        for (Entry<Element, List<DeclaredType>> entry : productions.entrySet()) {
            Element element = entry.getKey();
            List<DeclaredType> list = entry.getValue();
            int size = list.size();
            PackagingFilter filter = new PackagingFilter(myModel);
            filter.filterTypes( list );
            if ( list.size() == 0 ){
                filtered.add( element );
            }
        }
        for( Element element : filtered ){
            productions.remove( element );
        }
        return productions.keySet();*/
    }

    private void findEnabledProductions(Set<Element> productions )
    {
        /*
         * This is partial implementation of the spec :
         * A bean is said to be enabled if:
         * - it is not a producer method or field of a disabled bean
         * Full check for enabled/disabled bean is very complicated.
         * Here is check only for enabled alternatives if any.
         */
        for (Iterator<Element> iterator =  productions.iterator();
            iterator.hasNext(); )
        {
            Element element = iterator.next();
            TypeElement enclosingTypeElement = getHelper().
                getCompilationController().getElementUtilities().
                enclosingTypeElement(element);
            if ( getResult().isAlternative(enclosingTypeElement)){
                String name = enclosingTypeElement.getQualifiedName().toString();
                if ( getResult().hasAlternative(enclosingTypeElement) ){
                    if ( !getModel().getAlternativeClasses().contains( name ) ){
                        iterator.remove();
                    }
                }
                if ( !alternativeStereotypesEnabled(enclosingTypeElement) ){
                    iterator.remove();
                }
            }
        }
    }

    private Set<Element> findEnabledTypes(Set<Element> elements) {
        LinkedList<Element> types = new LinkedList<>( elements );
        Set<Element> result = new HashSet<>( elements );
        while( !types.isEmpty() ) {
            TypeElement typeElement = (TypeElement)types.removeFirst();
            if ( !checkClass( typeElement )){
                result.remove( typeElement );
                continue;
            }
            checkProxyability( typeElement , result );
            checkSpecializes(typeElement, types, result ,  elements );
        }
        return result;
    }

    private boolean checkClass( TypeElement element ){
        if ( element.getKind() != ElementKind.CLASS ){
            return false;
        }
        Set<Modifier> modifiers = element.getModifiers();

        Element enclosing = element.getEnclosingElement();
        if ( !( enclosing instanceof PackageElement) ){
            /*
             * If class is inner class then it should be static.
             */
            if ( !modifiers.contains( Modifier.STATIC ) ){
                return false;
            }
        }
        Elements elements = getHelper().getCompilationController().getElements();
        Types types = getHelper().getCompilationController().getTypes();

        List<? extends AnnotationMirror> allAnnotations = elements.
            getAllAnnotationMirrors(element);

        if ( modifiers.contains( Modifier.ABSTRACT ) 
                && !getHelper().hasAnnotation(allAnnotations, AnnotationUtil.DECORATOR )
                && !getHelper().hasAnnotation(allAnnotations, AnnotationUtil.DECORATOR_JAKARTA ))
        {
            /*
             * If class is abstract it should be Decorator.
             */
            return false;
        }
        TypeElement extensionElement = elements.getTypeElement(EXTENSION_JAKARTA);
        if (extensionElement == null) {
            extensionElement = elements.getTypeElement(EXTENSION);
        }
        if ( extensionElement!= null ){
            TypeMirror extensionType = extensionElement.asType();
            /*
             * Class doesn't implement Extension
             */
            if ( types.isAssignable( element.asType(), extensionType )){
                return false;
            }
        }
        /*
         * There should be either no parameters CTOR or CTOR is annotated with @Inject
         */
        List<ExecutableElement> constructors = ElementFilter.constructorsIn(
                element.getEnclosedElements());
        boolean foundCtor = constructors.isEmpty();
        for (ExecutableElement ctor : constructors) {
            if ( ctor.getParameters().isEmpty() ){
                foundCtor = true;
                break;
            }
            List<? extends AnnotationMirror> ctorAnnotations = ctor.getAnnotationMirrors();
            if ( getHelper().hasAnnotation(ctorAnnotations, FieldInjectionPointLogic.INJECT_ANNOTATION)
                    || getHelper().hasAnnotation(ctorAnnotations, FieldInjectionPointLogic.INJECT_ANNOTATION_JAKARTA))
            {
                foundCtor = true;
                break;
            }
        }
        return foundCtor;
    }

    private void checkProxyability( TypeElement typeElement, Set<Element> elements)
    {
        try {
            String scope = ParameterInjectionPointLogic.getScope(typeElement,
                    getWebBeansModel().getHelper());
            Elements elementsUtil = getHelper().getCompilationController().
                getElements();
            TypeElement scopeElement = elementsUtil.getTypeElement(scope);
            /*
             * Client proxies are never required for a bean whose
             * scope is a pseudo-scope such as @Dependent.
             */
            if ( scopeElement == null
                    || getHelper().hasAnnotation( elementsUtil.getAllAnnotationMirrors(scopeElement), AnnotationUtil.SCOPE_FQN)
                    || getHelper().hasAnnotation( elementsUtil.getAllAnnotationMirrors(scopeElement), AnnotationUtil.SCOPE_FQN_JAKARTA))
            {
                return;
            }
        }
        catch (CdiException e) {
            elements.remove( typeElement);
            return;
        }
        /*
         * Certain legal bean types cannot be proxied by the container:
         * - classes which don't have a non-private constructor with no parameters,
         * - classes which are declared final or have final methods,
         * - primitive types,
         * -  and array types.
         */
        if ( hasModifier(typeElement, Modifier.FINAL)){
            elements.remove( typeElement );
            return;
        }
        checkFinalMethods(typeElement, elements);

        List<ExecutableElement> constructors = ElementFilter.constructorsIn(
                typeElement.getEnclosedElements()) ;
        boolean appropriateCtor = constructors.stream()
                .anyMatch(ctor -> !hasModifier(ctor, Modifier.PRIVATE)
                && ctor.getParameters().isEmpty());

        if ( !appropriateCtor){
            elements.remove( typeElement );
        }
    }

    private void checkFinalMethods( TypeElement typeElement, Set<Element> elements )
    {
        TypeMirror variableType = getResult().getVariableType();
        DeclaredType beanType = getDeclaredType( variableType );
        if ( beanType == null ){
            return;
        }
        Element beanElement = beanType.asElement();
        if ( !( beanElement instanceof TypeElement te )){
            return;
        }
        List<ExecutableElement> methods = ElementFilter.methodsIn(
                getHelper().getCompilationController().getElements().getAllMembers(te)) ;
        TypeElement objectElement = getHelper().getCompilationController().
            getElements().getTypeElement(Object.class.getCanonicalName());
        for (ExecutableElement executableElement : methods) {
            // Skip Object methods , Fix for BZ#201825 - suspicious messages for @Injection
            if ( executableElement.getEnclosingElement().equals( objectElement ) ){
                continue;
            }
            if ( hasModifier(executableElement, Modifier.FINAL)){
                elements.remove( typeElement );
                return;
            }
            Element overloaded = getHelper().getCompilationController().
                getElementUtilities().getImplementationOf(executableElement,
                        typeElement);
            if ( overloaded == null ){
                continue;
            }
            if ( hasModifier(overloaded, Modifier.FINAL)){
                elements.remove( typeElement );
                return;
            }
        }
    }

    private DeclaredType getDeclaredType(TypeMirror type) {
        return switch (type) {
            case null -> null;
            case DeclaredType dt when type.getKind() != TypeKind.ERROR -> dt;
            case TypeVariable tv -> getDeclaredType(tv.getUpperBound());
            case WildcardType wt -> getDeclaredType(wt.getExtendsBound());
            default -> null;
        };
    }

    private boolean hasModifier ( Element element , Modifier mod){
        return element.getModifiers().contains(mod);
    }

    private void checkSpecializes( TypeElement typeElement,
            LinkedList<Element> beans, Set<Element> resultElementSet,
            Set<Element> originalElements)
    {
        TypeElement current = typeElement;
        while( current != null ){
            TypeMirror superClass = current.getSuperclass();
            if (!(superClass instanceof DeclaredType dt)) {
                break;
            }
            if (!AnnotationObjectProvider.hasSpecializes(current, getHelper())) {
                break;
            }
            TypeElement superElement = (TypeElement) dt.asElement();
            if (originalElements.contains(superElement)) {
                resultElementSet.remove(superElement);
            }
            beans.remove( superElement );
            if ( !getResult().getTypeElements().contains( superElement)){
                break;
            }
            current = superElement;
        }
    }

    private void addEnabledAlternative( TypeElement typeElement , Element element) {
        String name = typeElement.getQualifiedName().toString();
        if ( getResult().hasAlternative(element) ){
            if ( !getModel().getAlternativeClasses().contains( name ) ){
                return;
            }
            /*
             * I have commented the code below but I'm not sure is it
             * correct. Specification doesn't mention the case
             * when @Alternative annotation presents along with
             * alternative Stereotypes.
             *
             * if ( getModel().getAlternativeClasses().contains( name ) ){
             *  myEnabledAlternatives.add( element );
                return;
            }
             */
        }
        if ( alternativeStereotypesEnabled(element)){
            myEnabledAlternatives.add( element );
        }
    }

    private boolean alternativeStereotypesEnabled( Element element ){
        List<AnnotationMirror> stereotypes = getResult().getStereotypes(element);
        for (AnnotationMirror annotationMirror : stereotypes) {
            DeclaredType annotationType = annotationMirror.getAnnotationType();
            TypeElement annotationTypeElement = (TypeElement)annotationType.asElement();
            if ( getResult().isAlternative(annotationTypeElement) ){
                if ( getResult().hasAlternative(annotationTypeElement) ){
                    String name = annotationTypeElement.getQualifiedName().toString();
                    if ( !getModel().getAlternativeStereotypes().contains(name) ){
                        return false;
                    }
                }
                else if ( !alternativeStereotypesEnabled(annotationTypeElement) ){
                        return false;
                }
            }
        }
        return true;
    }

    private ResultImpl getResult(){
        return myResult;
    }

    private BeansModel getModel(){
        return myBeansModel;
    }

    private AnnotationModelHelper getHelper(){
        return myHelper;
    }

    private WebBeansModelImplementation getWebBeansModel(){
        return myModel;
    }

    private InjectableResultImpl handleEESpecificImplementations(ResultImpl result, TypeElement firstElement, Set<Element> enabledTypes) {
        if(result.getVariable() != null) {
            String nm = result.getVariable().asType().toString();
            int c = nm.indexOf('<');
            if(c>0) {
                nm = nm.substring(0,c);
            }
            if(predefinedBeans.contains(nm)) {
                        return new InjectableResultImpl( getResult(), firstElement, enabledTypes );
            }
            if (Objects.equals("java.util.Map", nm)) { //NOI18N
                if (getHelper().hasAnyAnnotation(result.getVariable().getAnnotationMirrors(), predefinedMapAnnotations)) {
                    return new InjectableResultImpl(getResult(), firstElement, enabledTypes);
                }
            }
            String ann = predefinedBeanAnnotationPairs.get(nm);
            if(ann != null) {
                if(getHelper().hasAnnotation(result.getVariable().getAnnotationMirrors(), ann)) {
                    return new InjectableResultImpl( getResult(), firstElement, enabledTypes );
                }
            }
            if (hasManagedPropertyAnnotation(result.getVariable())) {
                return new InjectableResultImpl( getResult(), firstElement, enabledTypes );
            }
        }
        return null;
    }

    private boolean hasManagedPropertyAnnotation(VariableElement variable) {
        return variable != null && getHelper().hasAnyAnnotation(variable.getAnnotationMirrors(), managedPropertyAnnotations);
    }

}
