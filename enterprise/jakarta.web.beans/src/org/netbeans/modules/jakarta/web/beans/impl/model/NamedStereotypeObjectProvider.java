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


/**
 * @author ads
 *
 */
class NamedStereotypeObjectProvider extends AbstractObjectProvider<NamedStereotype> {
    
    NamedStereotypeObjectProvider(AnnotationModelHelper helper){
        super( StereotypeChecker.STEREOTYPE, helper );
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.jakarta.web.beans.impl.model.AbstractObjectProvider#createInitialObjects()
     */
    @Override
    public List<NamedStereotype> createInitialObjects()
            throws InterruptedException
    {
        final List<NamedStereotype> result = new LinkedList<NamedStereotype>();
        getHelper().getAnnotationScanner().findAnnotations(
                getAnnotation(), 
                EnumSet.of(ElementKind.ANNOTATION_TYPE), 
                new AnnotationHandler() {
                        @Override
                        public void handleAnnotation(TypeElement type, 
                                Element element, AnnotationMirror annotation) 
                        {
                            if ( hasNamed( type , getHelper() )) {
                                result.add(createTypeElement(type));
                            }
                        }
        });
        return result;
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.jakarta.web.beans.impl.model.AbstractObjectProvider#createObjects(javax.lang.model.element.TypeElement)
     */
    @Override
    public List<NamedStereotype> createObjects( TypeElement type ) {
        if (type.getKind() == ElementKind.ANNOTATION_TYPE &&
                getHelper().hasAnnotation(type.getAnnotationMirrors(), 
                getAnnotation())) 
        {
            if ( hasNamed(type, getHelper())){
                return Collections.singletonList(createTypeElement(type));
            }
        }
        return Collections.emptyList();
    }
    
    /* (non-Javadoc)
     * @see org.netbeans.modules.jakarta.web.beans.impl.model.AbstractObjectProvider#createTypeElement(javax.lang.model.element.TypeElement)
     */
    @Override
    protected NamedStereotype createTypeElement( TypeElement element ) {
        return new NamedStereotype(getHelper(), element);
    }

    static  boolean hasNamed( TypeElement type , AnnotationModelHelper helper ) {
        if (AnnotationObjectProvider.hasAnnotation(type, 
                FieldInjectionPointLogic.NAMED_QUALIFIER_ANNOTATION, helper))
        {
            return true; 
        }
        List<AnnotationMirror> stereotypes = WebBeansModelProviderImpl.
            getAllStereotypes(type, helper.getHelper());
        for (AnnotationMirror annotationMirror : stereotypes) {
            TypeElement annotation = (TypeElement)annotationMirror.
                getAnnotationType().asElement();
            if (annotation!= null && AnnotationObjectProvider.hasAnnotation(annotation, 
                    FieldInjectionPointLogic.NAMED_QUALIFIER_ANNOTATION, helper))
            {
                return true; 
            }
        }
        
        return false;
    }
    
}