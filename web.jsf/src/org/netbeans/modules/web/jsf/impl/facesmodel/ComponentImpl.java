/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
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
