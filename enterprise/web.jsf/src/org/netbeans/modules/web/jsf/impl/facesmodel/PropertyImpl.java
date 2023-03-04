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

import org.netbeans.modules.web.jsf.api.facesmodel.JSFConfigVisitor;
import org.netbeans.modules.web.jsf.api.facesmodel.Property;
import org.w3c.dom.Element;


/**
 * @author ads
 *
 */
class PropertyImpl extends IdentifiableDescriptionGroupImpl implements Property
{

    PropertyImpl( JSFConfigModelImpl model, Element element ) {
        super(model, element);
    }
    
    PropertyImpl( JSFConfigModelImpl model ) {
        this(model, createElementNS(model, JSFConfigQNames.PROPERTY));
    }

    /**
     * Gets property-class of the faces-config-propertyType.
     * @return trimmed property-class if any, {@code null} otherwise
     */
    public String getPropertyClass() {
        String propertyClass = getChildElementText(JSFConfigQNames.PROPERTY_CLASS.getQName(getNamespaceURI()));
        return ElementTypeHelper.pickJavaTypeType(propertyClass);
    }

    /**
     * Gets property-class of the faces-config-propertyType.
     * @return trimmed property-class if any, {@code null} otherwise
     */
    public String getPropertyName() {
        String propertyName = getChildElementText(JSFConfigQNames.PROPERTY_NAME.getQName(getNamespaceURI()));
        return ElementTypeHelper.pickString(propertyName);
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.web.jsf.api.facesmodel.Property#setPropertyClass(java.lang.String)
     */
    public void setPropertyClass( String clazz ) {
        setChildElementText(PROPERTY_CLASS, clazz, 
                JSFConfigQNames.PROPERTY_CLASS.getQName(getNamespaceURI()));
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.web.jsf.api.facesmodel.Property#setPropertyName(java.lang.String)
     */
    public void setPropertyName( String name ) {
        setChildElementText(PROPERTY_NAME, name, 
                JSFConfigQNames.PROPERTY_NAME.getQName(getNamespaceURI()));
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.web.jsf.api.facesmodel.JSFConfigComponent#accept(org.netbeans.modules.web.jsf.api.facesmodel.JSFConfigVisitor)
     */
    public void accept( JSFConfigVisitor visitor ) {
        visitor.visit(this );
    }
    
    protected List<String> getSortedListOfLocalNames(){
        return SORTED_ELEMENTS;
    }
    
    protected static final List<String> SORTED_ELEMENTS = new ArrayList<String>(5);
    static {
        SORTED_ELEMENTS.addAll( DESCRIPTION_GROUP_SORTED_ELEMENTS);
        SORTED_ELEMENTS.add( PROPERTY_NAME);
        SORTED_ELEMENTS.add( PROPERTY_CLASS );  
    }

}
