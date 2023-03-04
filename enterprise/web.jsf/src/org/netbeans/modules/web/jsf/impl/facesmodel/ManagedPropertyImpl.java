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

import org.netbeans.modules.web.jsf.api.facesmodel.FacesManagedProperty;
import org.netbeans.modules.web.jsf.api.facesmodel.JSFConfigVisitor;
import org.w3c.dom.Element;


/**
 * @author ads
 *
 */
class ManagedPropertyImpl extends IdentifiableDescriptionGroupImpl
        implements FacesManagedProperty
{

    ManagedPropertyImpl( JSFConfigModelImpl model, Element element )
    {
        super(model, element);
    }
    
    ManagedPropertyImpl( JSFConfigModelImpl model )
    {
        this(model, createElementNS(model, JSFConfigQNames.MANAGED_PROPERTY));
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.web.jsf.api.facesmodel.FacesManagedProperty#setPropertyClass(java.lang.String)
     */
    public void setPropertyClass( String clazz ) {
        setChildElementText(PROPERT_CLASS, clazz, 
                JSFConfigQNames.PROPERTY_CLASS.getQName(getNamespaceURI()));
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.web.jsf.api.facesmodel.FacesManagedProperty#setPropertyName(java.lang.String)
     */
    public void setPropertyName( String name ) {
        setChildElementText(PROPERT_NAME, name, 
                JSFConfigQNames.PROPERTY_NAME.getQName(getNamespaceURI()));
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.web.jsf.api.facesmodel.JSFConfigComponent#accept(org.netbeans.modules.web.jsf.api.facesmodel.JSFConfigVisitor)
     */
    public void accept( JSFConfigVisitor visitor ) {
        visitor.visit( this );
    }

    /**
     * Gets property-class of the faces-config-managed-propertyType.
     * @return trimmed property-class if any, {@code null} otherwise
     */
    public String getPropertyClass() {
        String propertyClass = getChildElementText(JSFConfigQNames.PROPERTY_CLASS.getQName(getNamespaceURI()));
        return ElementTypeHelper.pickJavaTypeType(propertyClass);
    }

    /**
     * Gets property-name of the faces-config-managed-propertyType.
     * @return trimmed property-name if any, {@code null} otherwise
     */
    public String getPropertyName() {
        String propertyName = getChildElementText(JSFConfigQNames.PROPERTY_NAME.getQName(getNamespaceURI()));
        return ElementTypeHelper.pickString(propertyName);
    }
    
    protected List<String> getSortedListOfLocalNames(){
        return SORTED_ELEMENTS;
    }
    
    protected static final List<String> SORTED_ELEMENTS = new ArrayList<String>(5);
    static {
        SORTED_ELEMENTS.addAll( DESCRIPTION_GROUP_SORTED_ELEMENTS);
        SORTED_ELEMENTS.add( PROPERT_NAME);
        SORTED_ELEMENTS.add( PROPERT_CLASS );  
    }

}
