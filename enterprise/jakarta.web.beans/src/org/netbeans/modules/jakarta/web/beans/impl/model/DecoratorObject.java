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

import java.util.List;
import java.util.Map;

import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.TypeElement;

import org.netbeans.modules.j2ee.metadata.model.api.support.annotation.AnnotationModelHelper;
import org.netbeans.modules.j2ee.metadata.model.api.support.annotation.PersistentObject;
import org.netbeans.modules.jakarta.web.beans.impl.model.AbstractObjectProvider.Refreshable;


/**
 * @author ads
 *
 */
class DecoratorObject extends PersistentObject implements Refreshable {

    DecoratorObject( AnnotationModelHelper helper,
            TypeElement typeElement )
    {
        super(helper, typeElement);
        boolean valid = refresh(typeElement);
        assert valid;
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.jakarta.web.beans.impl.model.AbstractObjectProvider.Refreshable#refresh(javax.lang.model.element.TypeElement)
     */
    @Override
    public boolean refresh( TypeElement type ) {
        List<? extends AnnotationMirror> allAnnotationMirrors = 
            getHelper().getCompilationController().getElements().
                getAllAnnotationMirrors(type);
        Map<String, ? extends AnnotationMirror> annotationsByType = 
                getHelper().getAnnotationsByType( allAnnotationMirrors );
        return annotationsByType.get( EnableBeansFilter.DECORATOR) != null ;
    }

}
