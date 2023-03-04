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

import org.netbeans.modules.web.jsf.api.facesmodel.ComponentExtension;
import org.netbeans.modules.web.jsf.api.facesmodel.FacesComponent;
import org.netbeans.modules.web.jsf.api.facesmodel.Facet;
import org.netbeans.modules.web.jsf.api.facesmodel.JSFConfigVisitor;
import org.w3c.dom.Element;


/**
 * @author ads
 *
 */
class ComponentImpl extends IdentifiableDescriptionGroupImpl implements
        FacesComponent
{

    ComponentImpl( JSFConfigModelImpl model, Element element ) {
        super(model, element);
    }
    
    ComponentImpl( JSFConfigModelImpl model) {
        super(model, createElementNS(model, JSFConfigQNames.COMPONENT));
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.web.jsf.api.facesmodel.FacesComponent#addComponentExtension(org.netbeans.modules.web.jsf.api.facesmodel.ComponentExtension)
     */
    public void addComponentExtension( ComponentExtension extension ) {
        appendChild(COMPONENT_EXTENSION, extension);
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.web.jsf.api.facesmodel.FacesComponent#addComponentExtension(int, org.netbeans.modules.web.jsf.api.facesmodel.ComponentExtension)
     */
    public void addComponentExtension( int index, ComponentExtension extension )
    {
        insertAtIndex(COMPONENT_EXTENSION, extension, index, ComponentExtension.class);
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.web.jsf.api.facesmodel.FacesComponent#addFacet(org.netbeans.modules.web.jsf.api.facesmodel.Facet)
     */
    public void addFacet( Facet facet ) {
        appendChild(FACET, facet );
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.web.jsf.api.facesmodel.FacesComponent#addFacet(int, org.netbeans.modules.web.jsf.api.facesmodel.Facet)
     */
    public void addFacet( int index, Facet facet ) {
        insertAtIndex(FACET, facet, index, Facet.class);
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.web.jsf.api.facesmodel.FacesComponent#getComponentExtensions()
     */
    public List<ComponentExtension> getComponentExtensions() {
        return getChildren( ComponentExtension.class);
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.web.jsf.api.facesmodel.FacesComponent#getFacets()
     */
    public List<Facet> getFacets() {
        return getChildren( Facet.class);
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.web.jsf.api.facesmodel.FacesComponent#removeComponentExtension(org.netbeans.modules.web.jsf.api.facesmodel.ComponentExtension)
     */
    public void removeComponentExtension( ComponentExtension extension ) {
        removeChild( COMPONENT_EXTENSION, extension );
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.web.jsf.api.facesmodel.FacesComponent#removeFacet(org.netbeans.modules.web.jsf.api.facesmodel.Facet)
     */
    public void removeFacet( Facet facet ) {
        removeChild( FACET, facet );
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.web.jsf.api.facesmodel.FacesComponent#setComponentClass(java.lang.String)
     */
    public void setComponentClass( String clazz ) {
        setChildElementText( COMPONENT_CLASS, clazz, 
                JSFConfigQNames.COMPONENT_CLASS.getQName(getNamespaceURI()));
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.web.jsf.api.facesmodel.FacesComponent#setComponentType(java.lang.String)
     */
    public void setComponentType( String type ) {
        setChildElementText(COMPONENT_TYPE, type, 
                JSFConfigQNames.COMPONENT_TYPE.getQName(getNamespaceURI()));
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.web.jsf.api.facesmodel.JSFConfigComponent#accept(org.netbeans.modules.web.jsf.api.facesmodel.JSFConfigVisitor)
     */
    public void accept( JSFConfigVisitor visitor ) {
        visitor.visit( this );
    }

    /**
     * Gets component-class of the faces-config-componentType.
     * @return trimmed component-class if any, {@code null} otherwise
     */
    public String getComponentClass() {
        String componentClass = getChildElementText(JSFConfigQNames.COMPONENT_CLASS.getQName(getNamespaceURI()));
        return ElementTypeHelper.pickFullyQualifiedClassType(componentClass);
    }

    /**
     * Gets component-type of the faces-config-componentType.
     * @return trimmed component-type if any, {@code null} otherwise
     */
    public String getComponentType() {
        String componentType = getChildElementText(JSFConfigQNames.COMPONENT_TYPE.getQName(getNamespaceURI()));
        return ElementTypeHelper.pickString(componentType);
    }
    
    protected List<String> getSortedListOfLocalNames(){
        return SORTED_ELEMENTS;
    }
    
    protected static final List<String> SORTED_ELEMENTS 
        = new ArrayList<String>(9);
    static { 
        SORTED_ELEMENTS.addAll( DESCRIPTION_GROUP_SORTED_ELEMENTS );
        SORTED_ELEMENTS.add(JSFConfigQNames.COMPONENT_TYPE.getLocalName());
        SORTED_ELEMENTS.add(JSFConfigQNames.COMPONENT_CLASS.getLocalName());
        SORTED_ELEMENTS.add(JSFConfigQNames.FACET.getLocalName());
        SORTED_ELEMENTS.add(JSFConfigQNames.ATTRIBUTE.getLocalName());
        SORTED_ELEMENTS.add(JSFConfigQNames.PROPERTY.getLocalName());
        SORTED_ELEMENTS.add(JSFConfigQNames.COMPONENT_EXTENSION.getLocalName());
    }

}
