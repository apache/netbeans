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
package org.netbeans.modules.jakarta.web.beans.impl.model;

import java.util.Collections;
import java.util.EnumSet;
import java.util.LinkedList;
import java.util.List;

import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.TypeElement;

import org.netbeans.modules.j2ee.metadata.model.api.support.annotation.AnnotationHandler;
import org.netbeans.modules.j2ee.metadata.model.api.support.annotation.AnnotationModelHelper;
import org.netbeans.modules.j2ee.metadata.model.api.support.annotation.ObjectProvider;


/**
 * @author ads
 *
 */
abstract class AbstractObjectProvider<T extends AbstractObjectProvider.Refreshable> 
    implements ObjectProvider<T> 
{
    
    AbstractObjectProvider(String annotation , AnnotationModelHelper helper)
    {
        myHelper = helper;
        myAnnotationName = annotation;
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.j2ee.metadata.model.api.support.annotation.ObjectProvider#createInitialObjects()
     */
    @Override
    public List<T> createInitialObjects() throws InterruptedException {
        final List<T> result = new LinkedList<T>();
        getHelper().getAnnotationScanner().findAnnotations(
                getAnnotation(), 
                EnumSet.of(ElementKind.CLASS, ElementKind.INTERFACE), 
                new AnnotationHandler() {
                        @Override
                        public void handleAnnotation(TypeElement type, 
                                Element element, AnnotationMirror annotation) 
                        {
                            result.add(createTypeElement(type));
                        }
        });
        return result;
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.j2ee.metadata.model.api.support.annotation.ObjectProvider#createObjects(javax.lang.model.element.TypeElement)
     */
    @Override
    public List<T> createObjects( TypeElement type ) {
        if ((type.getKind() == ElementKind.CLASS || type.getKind() == ElementKind.INTERFACE)
                && getHelper().hasAnnotation(type.getAnnotationMirrors(), 
                getAnnotation())) 
        {
            return Collections.singletonList(createTypeElement(type));
        }
        return Collections.emptyList();
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.j2ee.metadata.model.api.support.annotation.ObjectProvider#modifyObjects(javax.lang.model.element.TypeElement, java.util.List)
     */
    @Override
    public boolean modifyObjects( TypeElement type, List<T> objects ) {
        assert objects.size() ==1;
        T object = objects.get(0);
        assert object!= null;
        if ( ! object.refresh(type)){
            objects.remove(0);
            return true;
        }
        return false;
    }
    
    protected abstract T createTypeElement( TypeElement element );
    
    public static List<Element> getAnnotatedMembers( final String annotationName,
            final AnnotationModelHelper helper )
    {
        final List<Element> result = new LinkedList<Element>();
        try {
            helper.getAnnotationScanner().findAnnotations(
                    annotationName, 
                    EnumSet.of(ElementKind.FIELD, ElementKind.METHOD), 
                    new AnnotationHandler() {
                            @Override
                            public void handleAnnotation(TypeElement type, 
                                    Element element, AnnotationMirror annotation) 
                            {
                                result.add(element);
                            }
            });
        }
        catch (InterruptedException e) {
            FieldInjectionPointLogic.LOGGER.warning("Finding annotation "+
                    annotationName+" was interrupted"); // NOI18N
        }
        return result;
    }
    
    protected AnnotationModelHelper getHelper(){
        return myHelper;
    }
    
    protected String getAnnotation(){
        return myAnnotationName;
    }
    
    public static List<Element> getNamedMembers( AnnotationModelHelper helper )
    {
         List<Element> namedMembers = getAnnotatedMembers(
                 FieldInjectionPointLogic.NAMED_QUALIFIER_ANNOTATION, helper);
         return namedMembers;
    }
    
    static interface Refreshable {
        boolean refresh( TypeElement type );
    }
    
    private AnnotationModelHelper myHelper;
    private String myAnnotationName;

}
