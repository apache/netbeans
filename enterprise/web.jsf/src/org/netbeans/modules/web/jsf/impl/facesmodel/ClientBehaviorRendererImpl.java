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

import org.netbeans.modules.web.jsf.api.facesmodel.FacesClientBehaviorRenderer;
import org.netbeans.modules.web.jsf.api.facesmodel.JSFConfigVisitor;
import org.w3c.dom.Element;


/**
 * @author ads
 *
 */
class ClientBehaviorRendererImpl extends JSFConfigComponentImpl implements
        FacesClientBehaviorRenderer
{
    ClientBehaviorRendererImpl( JSFConfigModelImpl model, Element element )
    {
        super(model, element);
    }
    
    ClientBehaviorRendererImpl( JSFConfigModelImpl model )
    {
        this(model, createElementNS(model, JSFConfigQNames.CLIENT_BEHAVIOR_RENDERER));
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.web.jsf.api.facesmodel.FacesClientBehaviorRenderer#setClientBehaviorRendererClass(java.lang.String)
     */
    public void setClientBehaviorRendererClass( String clazz ) {
        setChildElementText(CLIENT_BEHAVIOR_RENDERER_CLASS, clazz, 
                JSFConfigQNames.CLIENT_BEHAVIOR_RENDERER_CLASS.getQName(getNamespaceURI()));
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.web.jsf.api.facesmodel.FacesClientBehaviorRenderer#setClientBehaviorRendererType(java.lang.String)
     */
    public void setClientBehaviorRendererType( String type ) {
        setChildElementText(CLIENT_BEHAVIOR_RENDERER_TYPE, type, 
                JSFConfigQNames.CLIENT_BEHAVIOR_RENDERER_TYPE.getQName(getNamespaceURI()));
    }

    /**
     * Gets client-behavior-renderer-class of the faces-config-client-behavior-rendererType.
     * @return trimmed client-behavior-renderer-class if any, {@code null} otherwise
     */
    public String getClientBehaviorRendererClass() {
        String rendererClass = getChildElementText(JSFConfigQNames.CLIENT_BEHAVIOR_RENDERER_CLASS.getQName(getNamespaceURI()));
        return ElementTypeHelper.pickFullyQualifiedClassType(rendererClass);
    }

    /**
     * Gets client-behavior-renderer-type of the faces-config-client-behavior-rendererType.
     * @return trimmed client-behavior-renderer-type if any, {@code null} otherwise
     */
    public String getClientBehaviorRendererType() {
        String rendererType = getChildElementText(JSFConfigQNames.CLIENT_BEHAVIOR_RENDERER_TYPE.getQName(getNamespaceURI()));
        return ElementTypeHelper.pickString(rendererType);
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
    
    protected static final List<String> SORTED_ELEMENTS = new ArrayList<String>(2);
    
    static {
        SORTED_ELEMENTS.add( CLIENT_BEHAVIOR_RENDERER_TYPE);
        SORTED_ELEMENTS.add( CLIENT_BEHAVIOR_RENDERER_CLASS);
    }

}
