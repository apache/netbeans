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
package org.netbeans.modules.jakarta.web.beans.impl.model.results;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.AnnotationValue;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeMirror;

import org.netbeans.api.java.source.CompilationController;
import org.netbeans.modules.j2ee.metadata.model.api.support.annotation.AnnotationModelHelper;
import org.netbeans.modules.j2ee.metadata.model.api.support.annotation.parser.AnnotationParser;
import org.netbeans.modules.j2ee.metadata.model.api.support.annotation.parser.ArrayValueHandler;
import org.netbeans.modules.jakarta.web.beans.api.model.InterceptorsResult;
import org.netbeans.modules.jakarta.web.beans.impl.model.StereotypeChecker;
import org.netbeans.modules.jakarta.web.beans.impl.model.WebBeansModelProviderImpl;


/**
 * @author ads
 *
 */
public class InterceptorsResultImpl implements InterceptorsResult {

    static final String INTERCEPTORS = "jakarta.interceptor.Interceptors";    // NOI18N

    public InterceptorsResultImpl( Element element ,
            List<TypeElement> enabledInterceptors,
            Set<TypeElement> disabledIntercaptors,
            AnnotationModelHelper helper )
    {
        mySubjectElement = element;
        myHelper = helper;
        myEnabledInterceptors = enabledInterceptors;
        myDisabledInterceptors = disabledIntercaptors;
        initDeclaredInterceptors();
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.jakarta.web.beans.api.model.Result#getAllStereotypes(javax.lang.model.element.Element)
     */
    @Override
    public List<AnnotationMirror> getAllStereotypes( Element element ) {
        return WebBeansModelProviderImpl.getAllStereotypes(element,
                getHelper().getHelper());
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.jakarta.web.beans.api.model.Result#getStereotypes(javax.lang.model.element.Element)
     */
    @Override
    public List<AnnotationMirror> getStereotypes( Element element ) {
        return getStereotypes(element, getHelper() );
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.jakarta.web.beans.api.model.BeansResult#isDisabled(javax.lang.model.element.Element)
     */
    @Override
    public boolean isDisabled( Element element ) {
        return myDisabledInterceptors.contains(element);
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.jakarta.web.beans.api.model.InterceptorsResult#getElement()
     */
    @Override
    public Element getElement() {
        return mySubjectElement;
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.jakarta.web.beans.api.model.InterceptorsResult#getResolvedInterceptors()
     */
    @Override
    public List<TypeElement> getResolvedInterceptors() {
        int enabledSize = myEnabledInterceptors.size();
        int disabledSize = myDisabledInterceptors.size();
        ArrayList<TypeElement> result = new ArrayList<TypeElement>( enabledSize +
                disabledSize );
        result.addAll( myEnabledInterceptors );
        result.addAll( myDisabledInterceptors );
        return result;
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.jakarta.web.beans.api.model.InterceptorsResult#getDeclaredInterceptors()
     */
    @Override
    public List<TypeElement> getDeclaredInterceptors() {
        return myDeclaredInterceptors;
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.jakarta.web.beans.api.model.InterceptorsResult#getAllInterceptors()
     */
    @Override
    public List<TypeElement> getAllInterceptors() {
        int enabledSize = myEnabledInterceptors.size();
        int disabledSize = myDisabledInterceptors.size();
        int declaredSize = myDeclaredInterceptors.size();
        ArrayList<TypeElement> result = new ArrayList<TypeElement>( enabledSize +
                disabledSize +declaredSize);
        result.addAll( myEnabledInterceptors );
        result.addAll( myDeclaredInterceptors );
        result.addAll( myDisabledInterceptors );
        return result;
    }


    private void initDeclaredInterceptors() {
        final LinkedHashSet<TypeElement> result = new LinkedHashSet<TypeElement>();
        AnnotationParser parser = AnnotationParser.create( getHelper());
        parser.expectClassArray("value", new ArrayValueHandler() {

            @Override
            public Object handleArray( List<AnnotationValue> arrayMembers ) {
                for (AnnotationValue arrayMember : arrayMembers) {
                    TypeMirror typeMirror = (TypeMirror) arrayMember.getValue();
                    Element element = getController().getTypes().
                        asElement( typeMirror );
                    if ( element instanceof TypeElement ){
                        result.add( (TypeElement)element );
                    }
                }
                return null;
            }
        }, null);
        Element subjectElement = getElement();
        if ( subjectElement instanceof ExecutableElement ){
            TypeElement enclosingType = getController().getElementUtilities().
                enclosingTypeElement( subjectElement);
            fillDeclaredAnnotations(parser, enclosingType);
        }
        fillDeclaredAnnotations(parser, subjectElement);
        myDeclaredInterceptors = new ArrayList<TypeElement>( result );
    }

    private void fillDeclaredAnnotations( AnnotationParser parser,
            Element subjectElement )
    {
        List<? extends AnnotationMirror> annotationMirrors =
            getController().getElements().getAllAnnotationMirrors( subjectElement );
        AnnotationMirror annotationMirror = getHelper().getAnnotationsByType(
                annotationMirrors).get(INTERCEPTORS);
        if ( annotationMirror != null ){
            parser.parse(annotationMirror);
        }
    }

    static List<AnnotationMirror> getStereotypes( Element element ,
            AnnotationModelHelper helper)
    {
        List<AnnotationMirror> result = new LinkedList<AnnotationMirror>();
        List<? extends AnnotationMirror> annotationMirrors =
            helper.getCompilationController().getElements().
                getAllAnnotationMirrors( element );
        StereotypeChecker checker = new StereotypeChecker( helper.getHelper());
        for (AnnotationMirror annotationMirror : annotationMirrors) {
            TypeElement annotationElement = (TypeElement)annotationMirror.
                getAnnotationType().asElement();
            if ( annotationElement!= null &&
                    WebBeansModelProviderImpl.isStereotype( annotationElement,
                            checker ) )
            {
                result.add( annotationMirror );
            }
        }
        return result;
    }

    private AnnotationModelHelper getHelper(){
        return myHelper;
    }

    private CompilationController getController(){
        return getHelper().getCompilationController();
    }

    private Element mySubjectElement;
    private List<TypeElement> myEnabledInterceptors;
    private Collection<TypeElement> myDisabledInterceptors;
    private List<TypeElement> myDeclaredInterceptors;
    private AnnotationModelHelper myHelper;
}
