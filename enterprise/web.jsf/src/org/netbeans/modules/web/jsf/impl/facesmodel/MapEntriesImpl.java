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
import org.netbeans.modules.web.jsf.api.facesmodel.MapEntries;
import org.w3c.dom.Element;


/**
 * @author ads
 *
 */
class MapEntriesImpl extends IdentifiableComponentImpl implements MapEntries {

    MapEntriesImpl( JSFConfigModelImpl model, Element element ) {
        super(model, element);
    }
    
    MapEntriesImpl( JSFConfigModelImpl model) {
        super(model, createElementNS(model, JSFConfigQNames.MAP_ENTRIES));
    }

    /**
     * Gets key-class of the faces-config-map-entriesType.
     * @return trimmed key-class if any, {@code null} otherwise
     */
    public String getKeyClass() {
        String keyClass = getChildElementText(JSFConfigQNames.KEY_CLASS.getQName(getNamespaceURI()));
        return ElementTypeHelper.pickFullyQualifiedClassType(keyClass);
    }

    /**
     * Gets value-class of the faces-config-map-entriesType.
     * @return trimmed value-class if any, {@code null} otherwise
     */
    public String getValueClass() {
        String valueClass = getChildElementText(JSFConfigQNames.VALUE_CLASS.getQName(getNamespaceURI()));
        return ElementTypeHelper.pickFacesConfigValueClassType(valueClass);
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.web.jsf.api.facesmodel.MapEntries#setKeyClass(java.lang.String)
     */
    public void setKeyClass( String clazz ) {
        setChildElementText(KEY_CLASS, clazz, 
                JSFConfigQNames.KEY_CLASS.getQName(getNamespaceURI()));
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.web.jsf.api.facesmodel.MapEntries#setValueClass(java.lang.String)
     */
    public void setValueClass( String clazz ) {
        setChildElementText(VALUE_CLASS, clazz, 
                JSFConfigQNames.VALUE_CLASS.getQName(getNamespaceURI()));
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
        SORTED_ELEMENTS.add( KEY_CLASS);
        SORTED_ELEMENTS.add( VALUE_CLASS );
    }

}
