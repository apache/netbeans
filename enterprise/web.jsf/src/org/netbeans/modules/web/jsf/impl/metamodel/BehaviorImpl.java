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
package org.netbeans.modules.web.jsf.impl.metamodel;

import java.util.Map;

import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.TypeElement;

import org.netbeans.modules.j2ee.metadata.model.api.support.annotation.AnnotationModelHelper;
import org.netbeans.modules.j2ee.metadata.model.api.support.annotation.PersistentObject;
import org.netbeans.modules.j2ee.metadata.model.api.support.annotation.parser.AnnotationParser;
import org.netbeans.modules.j2ee.metadata.model.api.support.annotation.parser.ParseResult;
import org.netbeans.modules.web.jsf.api.metamodel.Behavior;


/**
 * @author ads
 *
 */
class BehaviorImpl extends PersistentObject implements Behavior, Refreshable {

    BehaviorImpl( AnnotationModelHelper helper, TypeElement typeElement )
    {
        super(helper, typeElement);
        boolean valid = refresh(typeElement);
        assert valid;
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.web.jsf.api.metamodel.Behavior#getBehaviorClass()
     */
    public String getBehaviorClass() {
        return myClass;
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.web.jsf.api.metamodel.Behavior#getBehaviorId()
     */
    public String getBehaviorId() {
        return myId;
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.web.jsf.impl.metamodel.Refreshable#refresh(javax.lang.model.element.TypeElement)
     */
    public boolean refresh( TypeElement type ) {
        Map<String, ? extends AnnotationMirror> types = 
            getHelper().getAnnotationsByType(getHelper().getCompilationController().
                    getElements().getAllAnnotationMirrors(type));
        AnnotationMirror annotationMirror = types.get("jakarta.faces.component.behavior.FacesBehavior"); //NOI18N
        if (annotationMirror == null) {
            annotationMirror = types.get("javax.faces.component.behavior.FacesBehavior"); // NOI18N
        }
        if (annotationMirror == null) {
            return false;
        }
        AnnotationParser parser = AnnotationParser.create(getHelper());
        parser.expectString( "value", null);                            // NOI18N
        ParseResult parseResult = parser.parse(annotationMirror);
        myId = parseResult.get( "value" , String.class );               // NOI18N
        myClass = type.getQualifiedName().toString();
        return true;
    }
    
    private String myId;
    private String myClass;

}
