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
package org.netbeans.modules.web.beans.impl.model;

import java.util.List;
import java.util.Map;

import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.TypeElement;

import org.netbeans.modules.j2ee.metadata.model.api.support.annotation.AnnotationModelHelper;
import org.netbeans.modules.j2ee.metadata.model.api.support.annotation.PersistentObject;


/**
 * @author ads
 *
 */
class BindingQualifier extends PersistentObject {

    BindingQualifier( AnnotationModelHelper helper, TypeElement typeElement, 
            String annotation ) 
    {
        super(helper, typeElement);
        myAnnotation = annotation;
        refresh( typeElement);
    }
    
    String getAnnotationName(){
        return myAnnotation;
    }
    
    boolean refresh( TypeElement type ) {
        List<? extends AnnotationMirror> allAnnotationMirrors = 
            getHelper().getCompilationController().getElements().
                getAllAnnotationMirrors(type);
        Map<String, ? extends AnnotationMirror> annotationsByType = 
                getHelper().getAnnotationsByType( allAnnotationMirrors );
        if ( annotationsByType.get( getAnnotationName()) != null ){
            return true;
        }
        return  AnnotationObjectProvider.checkSuper(type, getAnnotationName(), 
                getHelper())!= null;
    }
    
    /* (non-Javadoc)
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals( Object obj ) {
        if ( obj instanceof BindingQualifier ){
            return ((BindingQualifier)obj).getTypeElement().equals( getTypeElement()); 
        }
        else {
            return false;
        }
    }
    
    /* (non-Javadoc)
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        return getTypeElement().hashCode();
    }
    
    private String myAnnotation;

}
