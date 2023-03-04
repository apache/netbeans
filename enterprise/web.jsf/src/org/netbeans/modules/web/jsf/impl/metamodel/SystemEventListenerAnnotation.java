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
package org.netbeans.modules.web.jsf.impl.metamodel;

import org.netbeans.modules.web.jsf.api.metamodel.SystemEventListener;


/**
 * @author ads
 *
 */
class SystemEventListenerAnnotation implements SystemEventListener {
    
    SystemEventListenerAnnotation( String clazz, String sourceClass , 
            String eventClass )
    {
        myClass = clazz;
        mySourceClass = sourceClass;
        myEventClass = eventClass;
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
    private String mySourceClass;
    private String myEventClass;

}
