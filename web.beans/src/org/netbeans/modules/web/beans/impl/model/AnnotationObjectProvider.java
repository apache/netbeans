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

import java.lang.annotation.Inherited;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;

import org.netbeans.api.java.source.ElementHandle;
import org.netbeans.api.java.source.ClassIndex.SearchKind;
import org.netbeans.api.java.source.ClassIndex.SearchScope;
import org.netbeans.modules.j2ee.metadata.model.api.support.annotation.AnnotationHandler;
import org.netbeans.modules.j2ee.metadata.model.api.support.annotation.AnnotationModelHelper;
import org.netbeans.modules.j2ee.metadata.model.api.support.annotation.AnnotationScanner;
import org.netbeans.modules.j2ee.metadata.model.api.support.annotation.ObjectProvider;


/**
 * This object provider cares about types that directly have annotations,
 * types that inherit annotations ( they extends class or interface that
 * directly have annotation and either 
 * - annotations is @Inherited
 * - type specializes type with annotation ( each hierarchy step has @Specializes
 * annotations ).
 * 
 * So result object ( type ) could not have directly annotation under subject and also 
 * there could be objects ( types ) which don't have even inherited annotation.
 * ( but hey have parents with this annotation and they specializes this parent ). 
 * @author ads
 *
 */
public class AnnotationObjectProvider implements ObjectProvider<BindingQualifier> {
    
    private static final String SPECILIZES_ANNOTATION = 
        "javax.enterprise.inject.Specializes";       // NOI18N
    
    static final Logger LOGGER = Logger.getLogger(
            AnnotationObjectProvider.class.getName());

    AnnotationObjectProvider( AnnotationModelHelper helper , String annotation) {
        myHelper = helper;
        myAnnotationName = annotation;
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.j2ee.metadata.model.api.support.annotation.ObjectProvider#createInitialObjects()
     */
    @Override
    public List<BindingQualifier> createInitialObjects() throws InterruptedException {
        final List<BindingQualifier> result = new LinkedList<BindingQualifier>();
        final Set<TypeElement> set = new HashSet<TypeElement>(); 
        getHelper().getAnnotationScanner().findAnnotations(getAnnotationName(), 
                AnnotationScanner.TYPE_KINDS, 
                new AnnotationHandler() {
                    @Override
                    public void handleAnnotation(TypeElement type, 
                            Element element, AnnotationMirror annotation) 
                    {
                        if ( !set.contains( type )){
                            result.add( new BindingQualifier( getHelper(), type , 
                                getAnnotationName()));
                        }
                        set.add( type );
                        if ( !getHelper().hasAnnotation( annotation.
                                getAnnotationType().asElement().
                                getAnnotationMirrors(), 
                                Inherited.class.getCanonicalName()))
                        {
                            /*
                             *  if annotation is inherited then method 
                             *  findAnnotations()
                             *  method will return types with this annotation.
                             *  Otherwise there could be implementors which 
                             *  specialize this type.
                             */
                            collectSpecializedImplementors( type , set, result );
                        }
                    }

        } );
        return new ArrayList<BindingQualifier>( result );
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.j2ee.metadata.model.api.support.annotation.ObjectProvider#createObjects(javax.lang.model.element.TypeElement)
     */
    @Override
    public List<BindingQualifier> createObjects( TypeElement type ) {
        final List<BindingQualifier> result = new ArrayList<BindingQualifier>();
        Map<String, ? extends AnnotationMirror> annotationsByType = 
            getHelper().getAnnotationsByType(getHelper().getCompilationController().
                getElements().getAllAnnotationMirrors( type ));
        AnnotationMirror annotationMirror = annotationsByType.get( 
                getAnnotationName());
        if (annotationMirror != null ) {
            result.add( new BindingQualifier(getHelper(), type, getAnnotationName()));
        }
        if ( annotationMirror == null || !getHelper().hasAnnotation( annotationMirror.
                getAnnotationType().asElement().
                getAnnotationMirrors(), 
                Inherited.class.getCanonicalName()))
        {
            if ( checkSuper( type , getAnnotationName() , getHelper())!= null ){
                result.add( new BindingQualifier( getHelper(), type, getAnnotationName()) );
            }
        }
        return result;
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.j2ee.metadata.model.api.support.annotation.ObjectProvider#modifyObjects(javax.lang.model.element.TypeElement, java.util.List)
     */
    @Override
    public boolean modifyObjects( TypeElement type, List<BindingQualifier> bindings ) {
        /*
         * Type element couldn't have the same annotation twice.
         * Provider based on single annotation ( its FQN  ).
         * So each type could have only one annotation at most.
         */
        assert bindings.size() ==1;
        BindingQualifier binding = bindings.get(0);
        assert binding!= null;
        if ( ! binding.refresh(type)){
            bindings.remove(0);
            return true;
        }
        return false;
    }
    
    static void visitSpecializes( TypeElement type, AnnotationModelHelper helper,
            SpecializeVisitor visitor ) 
    {
        if ( !hasSpecializes( type, helper )){
            return;
        }
        
        TypeElement superClass = helper.getSuperclass(type);
        if ( superClass != null ){
            if ( visitor.visit( superClass ) ){
                return;
            }
            visitSpecializes(superClass, helper, visitor);
        }
        
        /* interfaces could not be injectables , but let's inspect them as possible 
         * injectables for notifying user about error if any.
         */   
        
        List<? extends TypeMirror> interfaces = type.getInterfaces();
        for (TypeMirror typeMirror : interfaces) {
            Element el = helper.getCompilationController().getTypes().
                asElement(typeMirror);
            if ( el instanceof TypeElement ){
                TypeElement interfaceElement = (TypeElement) el;
                visitSpecializes( interfaceElement , helper , visitor );
            }
        }
    }
    
    static TypeElement checkSuper( TypeElement type , final  String annotationName, 
            final AnnotationModelHelper helper ) 
    {
        final TypeElement result[] = new TypeElement[1];
        SpecializeVisitor visitor = new SpecializeVisitor(){

            @Override
            public boolean visit( TypeElement element ) {
                if ( FieldInjectionPointLogic.DEFAULT_QUALIFIER_ANNOTATION.equals( 
                        annotationName))
                {
                    if ( checkSpecializedDefault( element, helper )){
                        result[0] = element;
                        return true;
                    }
                }
                if ( hasAnnotation( element , annotationName, helper)){
                    result[0] = element;
                    return true;
                }
                return false;
            }

            @Override
            public boolean visit( ExecutableElement element ) {
                return false;
            }
            
        };
        visitSpecializes(type, helper, visitor);
        return result[0];
    }
    
    /*
     * This method is called only for parent which are specialized.
     * In this case @Default is not "inherited" by child from parents. 
     */
    static boolean checkSpecializedDefault( Element element , AnnotationModelHelper helper){
        return helper.hasAnnotation( helper.getCompilationController().
                getElements().getAllAnnotationMirrors(element), 
                WebBeansModelProviderImpl.DEFAULT_QUALIFIER_ANNOTATION);
    }
    
    static  boolean checkDefault( Element element , AnnotationModelHelper helper){
        Set<String> qualifierNames = getQualifiers(element, helper , false );
        if ( qualifierNames.contains(
                WebBeansModelProviderImpl.DEFAULT_QUALIFIER_ANNOTATION))
        {
            return true;
        }
        qualifierNames.remove( ParameterInjectionPointLogic.NAMED_QUALIFIER_ANNOTATION);
        qualifierNames.remove( ParameterInjectionPointLogic.ANY_QUALIFIER_ANNOTATION);
        if ( qualifierNames.size() == 0 ){
            return true;
        }
        return false;
    }
    
    static Set<String> getQualifiers(Element element , 
            AnnotationModelHelper helper , boolean event )
    {
        final Set<String> result = new HashSet<String>();
        AnnotationHandleStrategy strategy = new AnnotationHandleStrategy(){

            @Override
            public void handleAnnotation( AnnotationMirror annotationMirror , 
                    TypeElement annotationElement) 
            {
                result.add(annotationElement.getQualifiedName().toString());
            }
        };
        findQualifiers(element, helper, event, strategy);
        return result;
    }
    
    static void findQualifiers(Element element , 
            AnnotationModelHelper helper , boolean event , 
            AnnotationHandleStrategy strategy )
    {
        List<? extends AnnotationMirror> allAnnotationMirrors = 
            helper.getCompilationController().getElements().
            getAllAnnotationMirrors( element );
        for (AnnotationMirror annotationMirror : allAnnotationMirrors) {
            DeclaredType annotationType = annotationMirror
                    .getAnnotationType();
            if ( annotationType == null || annotationType.getKind() == TypeKind.ERROR){
                continue;
            }
            TypeElement annotationElement = (TypeElement) annotationType
                    .asElement();
            if (annotationElement!= null && isQualifier(annotationElement, 
                    helper , event )) 
            {
                strategy.handleAnnotation(annotationMirror , annotationElement );
            }
        }
    }
    
    static boolean isQualifier( TypeElement annotationElement , 
            AnnotationModelHelper helper, boolean event ) 
    {
        QualifierChecker checker = QualifierChecker.get( event );
        checker.init(annotationElement, helper );
        return checker.check();
    }
    
    public static boolean hasSpecializes( Element element , 
            AnnotationModelHelper helper )
    {
        return hasAnnotation(element , SPECILIZES_ANNOTATION , helper );
    }
    
    static boolean hasAnnotation( Element element, String annotation, 
            AnnotationModelHelper helper )
    {
        List<? extends AnnotationMirror> allAnnotationMirrors = 
            helper.getCompilationController().getElements().
            getAllAnnotationMirrors(element);
        return helper.hasAnnotation(allAnnotationMirrors, 
                annotation );
    }
    
    private String getAnnotationName(){
        return myAnnotationName;
    }
    
    private AnnotationModelHelper getHelper(){
        return myHelper;
    }
    
    private void collectSpecializedImplementors( TypeElement type, Set<TypeElement> set, 
            List<BindingQualifier> bindings ) 
    {
        Set<TypeElement> result = new HashSet<TypeElement>();
        Set<TypeElement> toProcess = new HashSet<TypeElement>();
        toProcess.add(type);
        while (toProcess.size() > 0) {
            TypeElement element = toProcess.iterator().next();
            toProcess.remove(element);
            Set<TypeElement> implementors = doCollectSpecializedImplementors(
                    element,bindings);
            if (implementors.size() == 0) {
                continue;
            }
            result.addAll(implementors);
            for (TypeElement impl : implementors) {
                toProcess.add(impl);
            }
        }
        for (TypeElement derivedElement : result) {
            if (!hasSpecializes(derivedElement, getHelper())) {
                continue;
            }
            handleSuper(type, derivedElement, bindings, set);
        }
    }
    
    private Set<TypeElement> doCollectSpecializedImplementors( TypeElement type, 
            List<BindingQualifier> bindings )
    {
        Set<TypeElement> result = new HashSet<TypeElement>();
        ElementHandle<TypeElement> handle = ElementHandle.create(type);
        final Set<ElementHandle<TypeElement>> handles = getHelper()
                .getClasspathInfo().getClassIndex().getElements(
                        handle,
                        EnumSet.of(SearchKind.IMPLEMENTORS),
                        EnumSet
                                .of(SearchScope.SOURCE,
                                        SearchScope.DEPENDENCIES));
        if (handles == null) {
            LOGGER.log(Level.WARNING,
                    "ClassIndex.getElements() was interrupted"); // NOI18N
            return Collections.emptySet();
        }
        for (ElementHandle<TypeElement> elementHandle : handles) {
            LOGGER.log(Level.FINE, "found derived element {0}", elementHandle
                    .getQualifiedName()); // NOI18N
            TypeElement derivedElement = elementHandle.resolve(getHelper().
                    getCompilationController());
            if (derivedElement == null) {
                continue;
            }
            result.add(derivedElement);
        }
        return result;
    }
    
    private boolean  handleInterface( TypeElement element, TypeElement child,
            Set<TypeElement> collectedElements , Set<TypeElement>  bindingTypes )
    {
        /* interfaces could not be injectables , but let's inspect them as possible 
         * injectables for notifying user about error if any.
         */ 
        List<? extends TypeMirror> interfaces = child.getInterfaces();
        for (TypeMirror typeMirror : interfaces) {
            if ( getHelper().getCompilationController().getTypes().isSameType(
                    element.asType(), typeMirror) )
            {
                return true;
            }
            if ( getHelper().getCompilationController().getTypes().
                    isAssignable( typeMirror, element.asType()))
            {
                Element el = getHelper().getCompilationController().
                    getTypes().asElement( typeMirror );
                if ( !( el instanceof TypeElement )){
                    return false;
                }
                TypeElement interfaceElement = (TypeElement)el;
                if ( bindingTypes.contains( interfaceElement) ){
                    return true;
                }
                collectedElements.add( interfaceElement);
                if ( !hasSpecializes( interfaceElement , getHelper() ) ){
                    return false;
                }
                else {
                    return handleInterface(element, interfaceElement, 
                            collectedElements, bindingTypes );
                }
            }
        }  
        
        return false;
    }

    private void handleSuper(TypeElement type ,TypeElement child, 
            List<BindingQualifier> bindings, Set<TypeElement> set) 
    {
        if ( !getHelper().getCompilationController().getTypes().isAssignable( 
                child.asType(), type.asType()))
        {
            return;
        }
        List<? extends TypeElement> superclasses = getHelper().getSuperclasses(
                child);
        Set<TypeElement> collectedSuper = new HashSet<TypeElement>();
        collectedSuper.add( child );
        boolean specializes = true;
        TypeElement previous = child;
        for (TypeElement superElement : superclasses) {
            if (superElement.equals(type) || set.contains( superElement)) {
                break;
            }
            if ( getHelper().getCompilationController().getTypes().
                    isAssignable( superElement.asType(), type.asType()))
            {
                previous = superElement;
            }
            else {
                if ( !hasSpecializes(superElement, getHelper())) {
                    specializes = false;
                    break;
                }
                collectedSuper.add(superElement);
                specializes = handleInterface(type, previous, collectedSuper, set );
                break;
            }
        }
        if (specializes) {
            for (TypeElement superElement : collectedSuper) {
                if (!set.contains(superElement)) {
                    set.add(superElement);
                    bindings.add(new BindingQualifier(getHelper(), superElement,
                            getAnnotationName()));
                }
            }
        }
    }
    
    static interface AnnotationHandleStrategy {
        void handleAnnotation( AnnotationMirror mirror , TypeElement annotation );
    }
    
    static interface SpecializeVisitor {
        boolean visit( TypeElement superElement );
        boolean visit( ExecutableElement overridenElement );
    }
    
    private AnnotationModelHelper myHelper;
    private String myAnnotationName;

}
