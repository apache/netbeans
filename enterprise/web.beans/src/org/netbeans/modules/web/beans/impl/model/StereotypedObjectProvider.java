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

import java.util.List;
import javax.lang.model.element.TypeElement;

import org.netbeans.modules.j2ee.metadata.model.api.support.annotation.AnnotationModelHelper;


/**
 * Provider for type elements which are annotated with some stereotype.
 * 
 * @author ads
 *
 */
class StereotypedObjectProvider extends AbstractObjectProvider<StereotypedObject>
{
    
    StereotypedObjectProvider( String stereotypeAnnotation ,
            AnnotationModelHelper helper )
    {
        super(List.of(stereotypeAnnotation), helper);
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.web.beans.impl.model.AbstractObjectProvider#createTypeElement(javax.lang.model.element.TypeElement)
     */
    @Override
    protected StereotypedObject createTypeElement( TypeElement element ) {
        for(String annotation : getAnnotation()) {
            if (getHelper().hasAnnotation(element.getAnnotationMirrors(), annotation))  {
                return new StereotypedObject(annotation, getHelper(), element);
            }
        }
        return null;
    }

}
