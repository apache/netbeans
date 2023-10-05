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
import org.netbeans.modules.web.jsf.api.metamodel.SystemEventListener;


/**
 * @author ads
 *
 */
class SystemEventListenerImpl extends PersistentObject implements
        Refreshable, SystemEventListener
{

    SystemEventListenerImpl( AnnotationModelHelper helper,
            TypeElement typeElement )
    {
        super(helper, typeElement);
        boolean valid = refresh(typeElement);
        assert valid;
    }

    public boolean refresh( TypeElement type ) {
        Map<String, ? extends AnnotationMirror> types = 
            getHelper().getAnnotationsByType(getHelper().getCompilationController()
                    .getElements().getAllAnnotationMirrors(type));
        AnnotationMirror annotationMirror = types.get(
                "jakarta.faces.event.ListenerFor");                       // NOI18N
        if (annotationMirror == null) {
            annotationMirror = types.get(
                    "javax.faces.event.ListenerFor");        // NOI18N

        }
        if (annotationMirror == null || 
                !ObjectProviders.SystemEventListenerProvider.
                isApplicationSystemEventListener(type, getHelper())) 
        {
            return false;
        }
        
        AnnotationParser parser = AnnotationParser.create(getHelper());
        parser.expectClass( "systemEventClass", null);                  // NOI18N
        parser.expectClass("sourceClass", AnnotationParser.defaultValue(// NOI18N
                Void.class.getCanonicalName()));
        ParseResult parseResult = parser.parse(annotationMirror);
        myEventClass = parseResult.get( "systemEventClass" ,            // NOI18N 
                String.class );
        mySourceClass = parseResult.get( "sourceClass" ,                // NOI18N 
                String.class );
        myClass = type.getQualifiedName().toString();
        return true;
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.web.jsf.api.metamodel.SystemEventListener#getSourceClass()
     */
    public String getSourceClass() {
        return mySourceClass;
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.web.jsf.api.metamodel.SystemEventListener#getSystemEventClass()
     */
    public String getSystemEventClass() {
        return myEventClass;
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.web.jsf.api.metamodel.SystemEventListener#getSystemEventListenerClass()
     */
    public String getSystemEventListenerClass() {
        return myClass;
    }
    
    private String myClass;
    private String myEventClass;
    private String mySourceClass;

}
