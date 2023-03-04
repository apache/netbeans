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

import org.netbeans.modules.web.jsf.api.facesmodel.Facet;
import org.netbeans.modules.web.jsf.api.facesmodel.JSFConfigVisitor;
import org.w3c.dom.Element;


/**
 * @author ads
 *
 */
class FacetImpl extends IdentifiableDescriptionGroupImpl implements Facet {

    FacetImpl( JSFConfigModelImpl model, Element element ) {
        super(model, element);
    }
    
    FacetImpl( JSFConfigModelImpl model  ) {
        this(model, createElementNS(model, JSFConfigQNames.FACET));
    }

    /**
     * Gets facet-name of the faces-config-facetType.
     * @return trimmed facet-name if any, {@code null} otherwise
     */
    public String getFacetName() {
        String facetName = getChildElementText(JSFConfigQNames.FACET_NAME.getQName(getNamespaceURI()));
        return ElementTypeHelper.pickJavaIdentifierType(facetName);
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.web.jsf.api.facesmodel.Facet#setFacetName(java.lang.String)
     */
    public void setFacetName( String name ) {
        setChildElementText(FACET_NAME, name, 
                JSFConfigQNames.FACET_NAME.getQName(getNamespaceURI()));
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
    
    protected static final List<String> SORTED_ELEMENTS = new ArrayList<String>(4);
    static {
        SORTED_ELEMENTS.addAll( DESCRIPTION_GROUP_SORTED_ELEMENTS);
        SORTED_ELEMENTS.add( FACET_NAME);
    }

}
