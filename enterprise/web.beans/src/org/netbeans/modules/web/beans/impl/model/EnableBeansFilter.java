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

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.TypeElement;
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
// @todo: Support JakartaEE
class EnableBeansFilter {

    static final String DECORATOR = "javax.decorator.Decorator";            // NOI18N

    static final String EXTENSION = "javax.enterprise.inject.spi.Extension";// NOI18N

    static String SCOPE = "javax.inject.Scope";                             // NOI18N

     private final HashSet<String> predefinedBeans;
     {
         predefinedBeans = new HashSet<>();
         predefinedBeans.add(EventInjectionPointLogic.EVENT_INTERFACE);
         predefinedBeans.add("javax.servlet.http.HttpServletRequest");//NOI18N
         predefinedBeans.add("javax.servlet.http.HttpSession");//NOI18N
         predefinedBeans.add("javax.servlet.ServletContext");//NOI18N
         predefinedBeans.add("javax.jms.JMSContext");//NOI18N
         predefinedBeans.add(AnnotationUtil.INJECTION_POINT);//NOI18N
         predefinedBeans.add("javax.enterprise.inject.spi.BeanManager");//NOI18N
         predefinedBeans.add("javax.transaction.UserTransaction");//NOI18N
         predefinedBeans.add("java.security.Principal");//NOI18N
         predefinedBeans.add("javax.validation.ValidatorFactory");//NOI18N
         predefinedBeans.add("javax.faces.application.Application");//NOI18N
         predefinedBeans.add("javax.faces.annotation.ApplicationMap");//NOI18N
         predefinedBeans.add("javax.faces.annotation.FlowMap");//NOI18N
         predefinedBeans.add("javax.faces.annotation.HeaderMap");//NOI18N
         predefinedBeans.add("javax.faces.annotation.HeaderValuesMap");//NOI18N
         predefinedBeans.add("javax.faces.annotation.InitParameterMap");//NOI18N
         predefinedBeans.add("javax.faces.annotation.ManagedProperty");//NOI18N
         predefinedBeans.add("javax.faces.annotation.RequestCookieMap");//NOI18N
         predefinedBeans.add("javax.faces.annotation.RequestMap");//NOI18N
         predefinedBeans.add("javax.faces.annotation.RequestParameterMap");//NOI18N
         predefinedBeans.add("javax.faces.annotation.RequestParameterValuesMap");//NOI18N
         predefinedBeans.add("javax.faces.annotation.SessionMap");//NOI18N
         predefinedBeans.add("javax.faces.annotation.ViewMap");//NOI18N
         predefinedBeans.add("javax.faces.context.ExternalContext");//NOI18N
         predefinedBeans.add("javax.faces.context.FacesContext");//NOI18N
    };

     private final HashMap<String, String> predefinedBeanAnnotationPairs;
     {
         predefinedBeanAnnotationPairs = new HashMap<>();
         predefinedBeanAnnotationPairs.put("javax.faces.flow.builder.FlowBuilder","javax.faces.flow.builder.FlowBuilderParameter");//NOI18N
     };

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
        myAlternatives = new HashSet<Element>();
        myEnabledAlternatives = new HashSet<Element>();

        PackagingFilter filter = new PackagingFilter(getWebBeansModel());
        Set<TypeElement> typeElements = getResult().getTypeElements();

        TypeElement firstElement = typeElements.size()>0 ? typeElements.iterator().next() : null;

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

        Set<Element> enabledTypeElements = new HashSet<Element>( typeElements );
        Set<Element> enabledProductions = new HashSet<Element>( productions );
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
            Element injectable = enabledTypes.size() ==0 ?
                    enabledProductions.iterator().next():
                        enabledTypes.iterator().next();
            enabledTypes.addAll( enabledProductions);
            return new InjectableResultImpl( getResult(), injectable, enabledTypes );
        }
        if ( commonSize ==0 ){
            //no implementation on classpath/sources or it's fileterd by common logic(for usual beans)
            //first check if we have a class in white list (i.e. must be implemented in ee7 environment)
            String nm = myResult.getVariableType().toString();
            if(nm.startsWith("javax.") || nm.startsWith("java.")) {//NOI18N
                InjectableResultImpl res = handleEESpecificImplementations(getResult(), firstElement, enabledTypes);
                if(res != null) {
                    return res;
                }
            }
            //
            if ( typeElements.size() == 0 && productions.size() == 0 ){
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
        Set<Element> allElements = new HashSet<Element>( enabledTypes );
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
        LinkedList<Element> types = new LinkedList<Element>( elements );
        Set<Element> result = new HashSet<Element>( elements );
        while( types.size() != 0 ) {
            TypeElement typeElement = (TypeElement)types.remove();
            if ( !checkClass( typeElement )){
                result.remove( typeElement );
                continue;
            }
            checkProxyability( typeElement , types, result );
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

        if ( modifiers.contains( Modifier.ABSTRACT ) &&
                !getHelper().hasAnnotation(allAnnotations, DECORATOR ) )
        {
            /*
             * If class is abstract it should be Decorator.
             */
            return false;
        }
        TypeElement extensionElement = elements.getTypeElement( EXTENSION );
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
        boolean foundCtor = constructors.size() ==0;
        for (ExecutableElement ctor : constructors) {
            if ( ctor.getParameters().size() == 0 ){
                foundCtor = true;
                break;
            }
            if ( getHelper().hasAnnotation(allAnnotations,
                    FieldInjectionPointLogic.INJECT_ANNOTATION))
            {
                foundCtor = true;
                break;
            }
        }
        return foundCtor;
    }

    private void checkProxyability( TypeElement typeElement,
            LinkedList<Element> types , Set<Element> elements)
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
            if ( scopeElement == null ||
                    getHelper().hasAnnotation( elementsUtil.getAllAnnotationMirrors(
                    scopeElement), SCOPE) )
            {
                return;
            }
        }
        catch (CdiException e) {
            types.remove( typeElement );
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
            types.remove(typeElement);
            elements.remove( typeElement );
            return;
        }
        checkFinalMethods(typeElement, types, elements);

        List<ExecutableElement> constructors = ElementFilter.constructorsIn(
                typeElement.getEnclosedElements()) ;
        boolean appropriateCtor = false;
        for (ExecutableElement constructor : constructors) {
            if ( hasModifier(constructor, Modifier.PRIVATE)){
                continue;
            }
            if ( constructor.getParameters().size() == 0 ){
                appropriateCtor = true;
                break;
            }
        }

        if ( !appropriateCtor){
            types.remove(typeElement);
            elements.remove( typeElement );
        }
    }

    private void checkFinalMethods( TypeElement typeElement,
            LinkedList<Element> types, Set<Element> elements )
    {
        TypeMirror variableType = getResult().getVariableType();
        DeclaredType beanType = getDeclaredType( variableType );
        if ( beanType == null ){
            return;
        }
        Element beanElement = beanType.asElement();
        if ( !( beanElement instanceof TypeElement )){
            return;
        }
        List<ExecutableElement> methods = ElementFilter.methodsIn(
                getHelper().getCompilationController().getElements().getAllMembers(
                        (TypeElement)beanElement)) ;
        TypeElement objectElement = getHelper().getCompilationController().
            getElements().getTypeElement(Object.class.getCanonicalName());
        for (ExecutableElement executableElement : methods) {
            // Skip Object methods , Fix for BZ#201825 - suspicious messages for @Injection
            if ( executableElement.getEnclosingElement().equals( objectElement ) ){
                continue;
            }
            if ( hasModifier(executableElement, Modifier.FINAL)){
                types.remove(typeElement);
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
                types.remove(typeElement);
                elements.remove( typeElement );
                return;
            }
        }
    }

    private DeclaredType getDeclaredType( TypeMirror type ){
        if ( type instanceof DeclaredType && type.getKind()!= TypeKind.ERROR){
            return (DeclaredType)type;
        }
        if ( type instanceof TypeVariable ){
            TypeMirror upperBound = ((TypeVariable)type).getUpperBound();
            return getDeclaredType( upperBound );
        }
        else if ( type instanceof WildcardType ){
            TypeMirror extendsBound = ((WildcardType)type).getExtendsBound();
            return getDeclaredType( extendsBound );
        }
        return null;
    }

    private boolean hasModifier ( Element element , Modifier mod){
        Set<Modifier> modifiers = element.getModifiers();
        for (Modifier modifier : modifiers) {
            if (modifier == mod) {
                return true;
            }
        }
        return false;
    }

    private void checkSpecializes( TypeElement typeElement,
            LinkedList<Element> beans, Set<Element> resultElementSet,
            Set<Element> originalElements)
    {
        TypeElement current = typeElement;
        while( current != null ){
            TypeMirror superClass = current.getSuperclass();
            if (!(superClass instanceof DeclaredType)) {
                break;
            }
            if (!AnnotationObjectProvider.hasSpecializes(current, getHelper())) {
                break;
            }
            TypeElement superElement = (TypeElement) ((DeclaredType) superClass)
                .asElement();
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

    private Set<Element> myAlternatives;
    private Set<Element> myEnabledAlternatives;
    private ResultImpl myResult;
    private final AnnotationModelHelper myHelper;
    private final BeansModel myBeansModel;
    private WebBeansModelImplementation myModel;
    private boolean isProgrammatic;



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
            String ann = predefinedBeanAnnotationPairs.get(nm);
            if(ann != null) {//NOI18N
                for(AnnotationMirror am:result.getVariable().getAnnotationMirrors()) {
                    if(ann.equals(am.getAnnotationType().toString())) {//NOI18N
                        return new InjectableResultImpl( getResult(), firstElement, enabledTypes );
                    }
                }
            }
        }
        return null;
    }
}