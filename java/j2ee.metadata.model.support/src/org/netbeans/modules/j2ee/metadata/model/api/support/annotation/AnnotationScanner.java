/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.netbeans.modules.j2ee.metadata.model.api.support.annotation;

import java.lang.annotation.Inherited;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.DeclaredType;

import org.netbeans.api.java.source.CompilationInfo;
import org.netbeans.api.java.source.ElementHandle;
import org.netbeans.api.java.source.ClassIndex.SearchKind;
import org.netbeans.api.java.source.ClassIndex.SearchScope;
import org.openide.util.Parameters;

/**
 * An utility class that can be used to find elements (types, methods, etc.)
 * annotated with a given annotation.
 *
 * @author Andrei Badea, Tomas Mysik, ads
 */
public class AnnotationScanner {

    // XXX perhaps should be merged with AnnotationModelHelper
    
    /**
     * Defines all the possible kinds for program element types.
     */
    public static final Set<ElementKind> TYPE_KINDS = EnumSet.of(ElementKind.CLASS, ElementKind.INTERFACE, ElementKind.ENUM, ElementKind.ANNOTATION_TYPE);
    
    private static final Logger LOGGER = Logger.getLogger(AnnotationScanner.class.getName());

    private final AnnotationHelper helper;

    AnnotationScanner(AnnotationHelper helper) {
        this.helper = helper;
    }
    
    /**
     * Finds all elements annotated with the given annotation. This methods gets
     * the name of the searched annotation and an instance of the
     * {@link ElementAnnotationHandler} interface which will be used to
     * pass the found annotation elements back to the caller.
     * <p>
     * For finding annotated types {@link #TYPE_KINDS TYPE_KINDS} constant can be useful.
     *
     * @param  searchedTypeName the fully-qualified name of the annotation
     *         to be searched for. Cannot be <code>null</code>.
     * @param  kinds the set of kinds to be searched for.
     *         Cannot be neither <code>null</code> nor empty.
     * @param  handler a {@link ElementAnnotationHandler}. Its <code>elementAnnotation</code>
     *         method will be invoked once for each element annotated with the annotation
     *         passed in the <code>searchedTypeName</code> parameter, with
     *         the <code>element</code> parameter set to the annotated element, and the
     *         <code>annotation</code> parameter set to an {@link AnnotationMirror}
     *         (of type <code>searchedTypeName</code>) which that type is annotated with.
     *         Cannot be null.
     * @throws InterruptedException when the search was interrupted (for 
     *         example because {@link org.netbeans.api.java.source.ClassIndex#getElements}
     *         was interrupted).
     */
    public void findAnnotations(final String searchedTypeName, 
            Set<ElementKind> kinds, final AnnotationHandler handler) 
            throws InterruptedException 
    {
        findAnnotations(searchedTypeName, kinds, handler, false);
    }

    /**
     * Finds all elements annotated with the given annotation. This methods gets
     * the name of the searched annotation and an instance of the
     * {@link ElementAnnotationHandler} interface which will be used to
     * pass the found annotation elements back to the caller.
     * <p>
     * For finding annotated types {@link #TYPE_KINDS TYPE_KINDS} constant can be useful.
     *
     * @param  searchedTypeName the fully-qualified name of the annotation
     *         to be searched for. Cannot be <code>null</code>.
     * @param  kinds the set of kinds to be searched for.
     *         Cannot be neither <code>null</code> nor empty.
     * @param  handler a {@link ElementAnnotationHandler}. Its <code>elementAnnotation</code>
     *         method will be invoked once for each element annotated with the annotation
     *         passed in the <code>searchedTypeName</code> parameter, with
     *         the <code>element</code> parameter set to the annotated element, and the
     *         <code>annotation</code> parameter set to an {@link AnnotationMirror}
     *         (of type <code>searchedTypeName</code>) which that type is annotated with.
     *         Cannot be null.
     * @param  includeDerived include derived types into result ( 
     *          if annotation has @Inherited annotation ) or not   
     * @throws InterruptedException when the search was interrupted (for 
     *         example because {@link org.netbeans.api.java.source.ClassIndex#getElements}
     *         was interrupted).
     */
    public void findAnnotations(final String searchedTypeName, 
            Set<ElementKind> kinds, final AnnotationHandler handler,
            boolean includeDerived) throws InterruptedException 
    {
        Parameters.notNull("searchedTypeName", searchedTypeName); // NOI18N
        Parameters.notNull("kinds", kinds); // NOI18N
        Parameters.notNull("handler", handler); // NOI18N
        LOGGER.log(Level.FINE, "findAnnotations called with {0} for {1}", new Object[] { searchedTypeName, kinds }); // NOI18N
        if (kinds.isEmpty()) {
            LOGGER.log(Level.WARNING, "findAnnotations: no kinds given"); // NOI18N
            return;
        }
        CompilationInfo controller = getHelper().getCompilationInfo();
        TypeElement searchedType = controller.getElements().getTypeElement(searchedTypeName);
        if (searchedType == null) {
            LOGGER.log(Level.FINE, "findAnnotations: could not find type {0}", searchedTypeName); // NOI18N
            return;
        }
        ElementHandle<TypeElement> searchedTypeHandle = ElementHandle.create(searchedType);
        final Set<ElementHandle<TypeElement>> elementHandles = getHelper().
            getCompilationInfo().getClasspathInfo().getClassIndex().getElements(
                searchedTypeHandle,
                EnumSet.of(SearchKind.TYPE_REFERENCES),
                EnumSet.of(SearchScope.SOURCE, SearchScope.DEPENDENCIES));
        if (elementHandles == null) {
            throw new InterruptedException("ClassIndex.getElements() was interrupted"); // NOI18N
        }
        Set<ElementKind> nonTypeKinds = EnumSet.copyOf(kinds);
        nonTypeKinds.removeAll(TYPE_KINDS);
        Set<ElementKind> typeKinds = EnumSet.copyOf(kinds);
        typeKinds.retainAll(TYPE_KINDS);
        boolean followDerived = includeDerived || checkInheritance( searchedType );
        if ( followDerived ){
            followDerived = typeKinds.size() >0 ;
        }
        for (ElementHandle<TypeElement> elementHandle : elementHandles) {
            LOGGER.log(Level.FINE, "found element {0}", elementHandle.getQualifiedName()); // NOI18N
            TypeElement typeElement = elementHandle.resolve(controller);
            if (typeElement == null) {
                continue;
            }
            
            // class etc.
            if (!typeKinds.isEmpty()) {
                handleAnnotation(handler, typeElement, typeElement, 
                        searchedTypeName, typeKinds, followDerived);
            }
            
            // methods & fields
            if (!nonTypeKinds.isEmpty()) {
                for (Element element : typeElement.getEnclosedElements()) {
                    if (nonTypeKinds.contains(element.getKind())) {
                        handleAnnotation(handler, typeElement, element, 
                                searchedTypeName, nonTypeKinds, false);
                    }
                }
            }
        }
    }
    
    private void handleAnnotation(final AnnotationHandler handler, 
            final TypeElement typeElement, final Element element, 
            final String searchedTypeName, Set<ElementKind> kinds, 
            boolean includeDerived) throws InterruptedException 
    {
        
        List<? extends AnnotationMirror> fieldAnnotationMirrors = element.getAnnotationMirrors();
        for (AnnotationMirror annotationMirror : fieldAnnotationMirrors) {
            DeclaredType annotationType = annotationMirror.getAnnotationType();
            String annotationTypeName = getHelper().getAnnotationTypeName(annotationType);
            // issue 110819: need to compare the real type names, since the annotation can be @<any>
            if (searchedTypeName.equals(annotationTypeName)) {
                LOGGER.log(
                        Level.FINE,
                        "notifying type {0}, element {1}, annotation {2}", // NOI18N
                        new Object[] { typeElement.getQualifiedName(), 
                                element.getSimpleName(), annotationMirror });
                // this check was originally missed for Types........
                if ( kinds.contains( element.getKind())){
                    handler.handleAnnotation(typeElement, element, annotationMirror);
                }
                if ( includeDerived && element.getKind() == ElementKind.CLASS ){
                    discoverHierarchy( typeElement , annotationMirror , 
                            handler, kinds);
                }
            } else {
                LOGGER.log(
                        Level.FINE,
                        "type name mismatch, ignoring type {0}, element {1}, annotation {2}", // NOI18N
                        new Object[] { typeElement.getQualifiedName(), element.getSimpleName(), annotationMirror });
            }
        }
    }
    
    private void discoverHierarchy( TypeElement typeElement, 
            AnnotationMirror annotationMirror, AnnotationHandler handler,
            Set<ElementKind> kinds) throws InterruptedException
    {
        Set<TypeElement> result = new HashSet<TypeElement>();
        result.add(typeElement);
        
        Set<TypeElement> toProcess = new HashSet<TypeElement>();
        toProcess.add(typeElement);
        while ( toProcess.size() >0 ){
            TypeElement element = toProcess.iterator().next();
            toProcess.remove( element );
            Set<TypeElement> set = doDiscoverHierarchy(element, annotationMirror,
                    handler , kinds );
            if ( set.size() == 0 ){
                continue;
            }
            result.addAll( set );
            for (TypeElement impl : set) {
                toProcess.add(impl);
            }
        }
        result.remove( typeElement );
        for (TypeElement derivedElement : result) {
            if ( kinds.contains( derivedElement.getKind())){
                handler.handleAnnotation(derivedElement, derivedElement, 
                        annotationMirror);
            }
        }
    }

    private Set<TypeElement> doDiscoverHierarchy( TypeElement typeElement,
            AnnotationMirror annotationMirror, AnnotationHandler handler,
            Set<ElementKind> kinds) throws InterruptedException
    {
        Set<TypeElement> result = new HashSet<TypeElement>();
        ElementHandle<TypeElement> handle = ElementHandle.create(
                typeElement);
        final Set<ElementHandle<TypeElement>> handles = getHelper().
            getCompilationInfo().getClasspathInfo().getClassIndex().getElements(
                handle,
                EnumSet.of(SearchKind.IMPLEMENTORS),
                EnumSet.of(SearchScope.SOURCE, SearchScope.DEPENDENCIES));
        if (handles == null) {
            throw new InterruptedException("ClassIndex.getElements() was interrupted"); // NOI18N
        }
        for (ElementHandle<TypeElement> elementHandle : handles) {
            LOGGER.log(Level.FINE, "found derived element {0}", 
                    elementHandle.getQualifiedName()); // NOI18N
            TypeElement derivedElement = elementHandle.resolve(
                    getHelper().getCompilationInfo());
            if (derivedElement == null) {
                continue;
            }
            result.add( derivedElement );
        }
        return result;
    }
    
    private boolean checkInheritance( TypeElement annotationType ) {
        return getHelper().hasAnnotation( annotationType.getAnnotationMirrors(), 
                Inherited.class.getCanonicalName());
    }
    
    private AnnotationHelper getHelper(){
        return helper;
    }
}
