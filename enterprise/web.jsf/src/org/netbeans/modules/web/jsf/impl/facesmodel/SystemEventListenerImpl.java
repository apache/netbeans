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
package org.netbeans.modules.web.jsf.impl.facesmodel;

import java.util.ArrayList;
import java.util.List;

import org.netbeans.modules.web.jsf.api.facesmodel.FacesSystemEventListener;
import org.netbeans.modules.web.jsf.api.facesmodel.JSFConfigVisitor;
import org.w3c.dom.Element;


/**
 * @author ads
 *
 */
class SystemEventListenerImpl extends IdentifiableComponentImpl implements
        FacesSystemEventListener
{

    SystemEventListenerImpl( JSFConfigModelImpl model,
            Element element )
    {
        super(model, element);
    }
    
    SystemEventListenerImpl( JSFConfigModelImpl model)
    {
        this(model, createElementNS(model, JSFConfigQNames.SYSTEM_EVENT_LISTENER));
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.web.jsf.api.facesmodel.FacesSystemEventListener#setSourceClass(java.lang.String)
     */
    public void setSourceClass( String clazz ) {
        setChildElementText(SOURCE_CLASS, clazz, 
                JSFConfigQNames.SOURCE_CLASS.getQName(getNamespaceURI()));
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.web.jsf.api.facesmodel.FacesSystemEventListener#setSystemEventClass(java.lang.String)
     */
    public void setSystemEventClass( String clazz ) {
        setChildElementText(SYSTEM_EVENT_CLASS, clazz, 
                JSFConfigQNames.SYSTEM_EVENT_CLASS.getQName(getNamespaceURI()));
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.web.jsf.api.facesmodel.FacesSystemEventListener#setSystemEventListenerClass(java.lang.String)
     */
    public void setSystemEventListenerClass( String clazz ) {
        setChildElementText(SYSTEM_EVENT_LISTENER_CLASS, clazz, 
                JSFConfigQNames.SYSTEM_EVENT_LISTENER_CLASS.getQName(getNamespaceURI()));
    }

    /**
     * Gets source-class of the faces-config-system-event-listenerType.
     * @return trimmed source-class if any, {@code null} otherwise
     */
    public String getSourceClass() {
        String sourceClass = getChildElementText(JSFConfigQNames.SOURCE_CLASS.getQName(getNamespaceURI()));
        return ElementTypeHelper.pickFullyQualifiedClassType(sourceClass);
    }

    /**
     * Gets system-event-class of the faces-config-system-event-listenerType.
     * @return trimmed system-event-class if any, {@code null} otherwise
     */
    public String getSystemEventClass() {
        String systemEventClass = getChildElementText(JSFConfigQNames.SYSTEM_EVENT_CLASS.getQName(getNamespaceURI()));
        return ElementTypeHelper.pickFullyQualifiedClassType(systemEventClass);
    }

    /**
     * Gets system-event-listener-class of the faces-config-system-event-listenerType.
     * @return trimmed system-event-listener-class if any, {@code null} otherwise
     */
    public String getSystemEventListenerClass() {
        String systemEventListenerClass = getChildElementText(JSFConfigQNames.SYSTEM_EVENT_LISTENER_CLASS.getQName(getNamespaceURI()));
        return ElementTypeHelper.pickFullyQualifiedClassType(systemEventListenerClass);
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.web.jsf.api.facesmodel.JSFConfigComponent#accept(org.netbeans.modules.web.jsf.api.facesmodel.JSFConfigVisitor)
     */
    public void accept( JSFConfigVisitor visitor ) {
        visitor.visit( this );
    }
    
    protected List<String> getSortedListOfLocalNames(){
        return SORTED_ELEMENTS;
    }
    
    protected static final List<String> SORTED_ELEMENTS = new ArrayList<String>(3);
    
    static {
        SORTED_ELEMENTS.add( SYSTEM_EVENT_LISTENER_CLASS);
        SORTED_ELEMENTS.add( SYSTEM_EVENT_CLASS);
        SORTED_ELEMENTS.add( SOURCE_CLASS );  
    }

}
