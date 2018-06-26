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
