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

import org.netbeans.modules.web.jsf.api.facesmodel.ConfigAttribute;
import org.netbeans.modules.web.jsf.api.facesmodel.JSFConfigVisitor;
import org.w3c.dom.Element;


/**
 * @author ads
 *
 */
class AttributeImpl extends IdentifiableDescriptionGroupImpl implements ConfigAttribute {

    AttributeImpl( JSFConfigModelImpl model, Element element ) {
        super(model, element);
    }
    
    AttributeImpl( JSFConfigModelImpl model) {
        this(model, createElementNS(model, JSFConfigQNames.ATTRIBUTE));
    }

    /**
     * Gets attribute-class of the attributeType.
     * @return trimmed attribute-class if any, {@code null} otherwise
     */
    public String getAttributeClass() {
        String attributeClass = getChildElementText(JSFConfigQNames.ATTRIBUTE_CLASS.getQName(getNamespaceURI()));
        return ElementTypeHelper.pickFullyQualifiedClassType(attributeClass);
    }

    /**
     * Gets attribute-name of the attributeType.
     * @return trimmed attribute-name if any, {@code null} otherwise
     */
    public String getAttributeName() {
        String attributeName = getChildElementText(JSFConfigQNames.ATTRIBUTE_NAME.getQName(getNamespaceURI()));
        return ElementTypeHelper.pickString(attributeName);
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.web.jsf.api.facesmodel.ConfigAttribute#setAttributeClass(java.lang.String)
     */
    public void setAttributeClass( String clazz ) {
        setChildElementText( ATTRIBUTE_CLASS, clazz, 
                JSFConfigQNames.ATTRIBUTE_CLASS.getQName(getNamespaceURI()));
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.web.jsf.api.facesmodel.ConfigAttribute#setAttributeName(java.lang.String)
     */
    public void setAttributeName( String name ) {
        setChildElementText( ATTRIBUTE_NAME, name, 
                JSFConfigQNames.ATTRIBUTE_NAME.getQName(getNamespaceURI()));
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.web.jsf.api.facesmodel.JSFConfigComponent#accept(org.netbeans.modules.web.jsf.api.facesmodel.JSFConfigVisitor)
     */
    public void accept( JSFConfigVisitor visitor ) {
        visitor.visit( this);
    }
    
    protected List<String> getSortedListOfLocalNames(){
        return SORTED_ELEMENTS;
    }
    
    protected static final List<String> SORTED_ELEMENTS = new ArrayList<String>(5);
    static {
        SORTED_ELEMENTS.addAll( DESCRIPTION_GROUP_SORTED_ELEMENTS);
        SORTED_ELEMENTS.add( ATTRIBUTE_NAME);
        SORTED_ELEMENTS.add( ATTRIBUTE_CLASS );  
    }

}
