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

import org.netbeans.modules.web.jsf.api.facesmodel.FacesRenderer;
import org.netbeans.modules.web.jsf.api.facesmodel.Facet;
import org.netbeans.modules.web.jsf.api.facesmodel.JSFConfigVisitor;
import org.w3c.dom.Element;


/**
 * @author ads
 *
 */
class RendererImpl extends IdentifiableDescriptionGroupImpl implements
        FacesRenderer
{

    RendererImpl( JSFConfigModelImpl model, Element element ) {
        super(model, element);
    }
    
    RendererImpl( JSFConfigModelImpl model) {
        this(model, createElementNS(model, JSFConfigQNames.RENDERER));
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.web.jsf.api.facesmodel.FacesRenderer#addFacet(org.netbeans.modules.web.jsf.api.facesmodel.Facet)
     */
    public void addFacet( Facet facet ) {
        appendChild( FACET, facet);
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.web.jsf.api.facesmodel.FacesRenderer#addFacet(int, org.netbeans.modules.web.jsf.api.facesmodel.Facet)
     */
    public void addFacet( int index, Facet facet ) {
        insertAtIndex( FACET, facet, index, Facet.class);
    }

    /**
     * Gets component-family of the faces-config-rendererType.
     * @return trimmed component-family if any, {@code null} otherwise
     */
    public String getComponentFamily() {
        String componentFamily = getChildElementText(JSFConfigQNames.COMPONENT_FAMILY.getQName(getNamespaceURI()));
        return ElementTypeHelper.pickString(componentFamily);
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.web.jsf.api.facesmodel.FacesRenderer#getFacets()
     */
    public List<Facet> getFacets() {
        return getChildren( Facet.class );
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.web.jsf.api.facesmodel.FacesRenderer#removeFacet(org.netbeans.modules.web.jsf.api.facesmodel.Facet)
     */
    public void removeFacet( Facet facet ) {
        removeChild( FACET, facet);
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.web.jsf.api.facesmodel.FacesRenderer#setComponentFamily(java.lang.String)
     */
    public void setComponentFamily( String family ) {
        setChildElementText(COMPONENT_FAMILY, family, 
                JSFConfigQNames.COMPONENT_FAMILY.getQName(getNamespaceURI()));
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.web.jsf.api.facesmodel.FacesRenderer#setRendererClass(java.lang.String)
     */
    public void setRendererClass( String clazz ) {
        setChildElementText(RENDERER_CLASS, clazz, 
                JSFConfigQNames.RENDERER_CLASS.getQName(getNamespaceURI()));
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.web.jsf.api.facesmodel.FacesRenderer#setRendererType(java.lang.String)
     */
    public void setRendererType( String type ) {
        setChildElementText(RENDERER_TYPE, type, 
                JSFConfigQNames.RENDERER_TYPE.getQName(getNamespaceURI()));
    }

    /**
     * Gets renderer-class of the faces-config-rendererType.
     * @return trimmed renderer-class if any, {@code null} otherwise
     */
    public String getRendererClass() {
        String rendererClass = getChildElementText(JSFConfigQNames.RENDERER_CLASS.getQName(getNamespaceURI()));
        return ElementTypeHelper.pickFullyQualifiedClassType(rendererClass);
    }

    /**
     * Gets renderer-type of the faces-config-rendererType.
     * @return trimmed renderer-type if any, {@code null} otherwise
     */
    public String getRendererType() {
        String rendererType = getChildElementText(JSFConfigQNames.RENDERER_TYPE.getQName(getNamespaceURI()));
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
    
    protected static final List<String> SORTED_ELEMENTS = new ArrayList<String>(
            DESCRIPTION_GROUP_SORTED_ELEMENTS.size() + 6);
    
    static {
        SORTED_ELEMENTS.addAll( DESCRIPTION_GROUP_SORTED_ELEMENTS );
        SORTED_ELEMENTS.add( COMPONENT_FAMILY);
        SORTED_ELEMENTS.add( RENDERER_TYPE);
        SORTED_ELEMENTS.add( RENDERER_CLASS  );  
        SORTED_ELEMENTS.add( FACET );
        SORTED_ELEMENTS.add( ATTRIBUTE  );
        //SORTED_ELEMENTS.add( RENDERER_EXTENSION  );
    }

}
