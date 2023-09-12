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

import java.util.Collections;
import java.util.List;
import java.util.Set;

import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeMirror;

import org.netbeans.api.java.source.CompilationController;
import org.netbeans.modules.j2ee.metadata.model.api.support.annotation.AnnotationModelHelper;
import org.netbeans.modules.jakarta.web.beans.api.model.DependencyInjectionResult;
import org.netbeans.modules.jakarta.web.beans.impl.model.WebBeansModelProviderImpl;


/**
 * @author ads
 *
 */
public class ResultImpl extends BaseResult implements DependencyInjectionResult.ResolutionResult {

    private static final String ALTERNATIVE =
        "jakarta.enterprise.inject.Alternative";   // NOI18N

    public ResultImpl( VariableElement var, TypeMirror elementType ,
            Set<TypeElement> declaredTypes,
            Set<Element> productionElements,
            AnnotationModelHelper helper )
    {
        super( var, elementType );
        myDeclaredTypes = declaredTypes;
        myProductions = productionElements;
        myHelper = helper;
    }

    public ResultImpl( VariableElement var, TypeMirror elementType ,
            TypeElement declaredType, AnnotationModelHelper helper )
    {
        super( var, elementType );
        myDeclaredTypes =Collections.singleton( declaredType );
        myProductions = Collections.emptySet();
        myHelper = helper;
    }

    public ResultImpl( VariableElement var, TypeMirror elementType ,
            AnnotationModelHelper helper )
    {
        super( var, elementType );
        myDeclaredTypes =Collections.emptySet();
        myProductions = Collections.emptySet();
        myHelper = helper;
    }

    public Set<TypeElement> getTypeElements() {
        return myDeclaredTypes;
    }

    public Set<Element> getProductions() {
        return myProductions;
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.jakarta.web.beans.api.model.Result#getKind()
     */
    @Override
    public ResultKind getKind() {
        return null;
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.jakarta.web.beans.api.model.Result.InjectableResult#getStereotypes(javax.lang.model.element.Element)
     */
    @Override
    public List<AnnotationMirror> getAllStereotypes( Element element ) {
        return WebBeansModelProviderImpl.getAllStereotypes(element,
                getHelper().getHelper());
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.jakarta.web.beans.api.model.Result.InjectableResult#getStereotypes(javax.lang.model.element.Element)
     */
    @Override
    public List<AnnotationMirror> getStereotypes( Element element ) {
        return InterceptorsResultImpl.getStereotypes(element, getHelper() );
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.jakarta.web.beans.api.model.Result.InjectableResult#isAlternative(javax.lang.model.element.Element)
     */
    @Override
    public boolean isAlternative( Element element ) {
        if (hasAlternative(element)){
            return true;
        }
        for (AnnotationMirror annotationMirror : getAllStereotypes(element)) {
            DeclaredType annotationType = annotationMirror.getAnnotationType();
            if ( hasAlternative( annotationType.asElement()) ){
                return true;
            }
        }
        return false;
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.jakarta.web.beans.api.model.Result.ResolutionResult#hasAlternative(javax.lang.model.element.Element)
     */
    @Override
    public boolean hasAlternative( Element element ){
        List<? extends AnnotationMirror> annotations = getController().
            getElements().getAllAnnotationMirrors(element);
        return getHelper().hasAnnotation(annotations, ALTERNATIVE);
    }

    AnnotationModelHelper  getHelper(){
        return myHelper;
    }

    private CompilationController getController(){
        return getHelper().getCompilationController();
    }

    private Set<TypeElement> myDeclaredTypes;
    private Set<Element> myProductions;
    private final AnnotationModelHelper myHelper;
}
