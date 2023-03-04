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
import org.netbeans.modules.web.jsf.api.facesmodel.ViewParam;
import org.w3c.dom.Element;


/**
 * @author ads
 *
 */
class ViewParamImpl extends IdentifiableComponentImpl implements ViewParam {

    ViewParamImpl( JSFConfigModelImpl model, Element element ) {
        super(model, element);
    }

    ViewParamImpl( JSFConfigModelImpl model) {
        super(model, createElementNS(model, JSFConfigQNames.VIEW_PARAM));
    }
    
    /**
     * Gets name of the faces-config-redirect-viewParamType.
     * @return trimmed name if any, {@code null} otherwise
     */
    public String getName() {
        String name = getChildElementText(JSFConfigQNames.NAME.getQName(getNamespaceURI()));
        return ElementTypeHelper.pickString(name);
    }

    /**
     * Gets value of the faces-config-redirect-viewParamType.
     * @return trimmed value if any, {@code null} otherwise
     */
    public String getValue() {
        String value = getChildElementText(JSFConfigQNames.VALUE.getQName(getNamespaceURI()));
        return ElementTypeHelper.pickString(value);
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.web.jsf.api.facesmodel.ViewParam#setName(java.lang.String)
     */
    public void setName( String name ) {
        setChildElementText(NAME, name, 
                JSFConfigQNames.NAME.getQName(getNamespaceURI()));
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.web.jsf.api.facesmodel.ViewParam#setValue(java.lang.String)
     */
    public void setValue( String value ) {
        setChildElementText(VALUE, value, 
                JSFConfigQNames.VALUE.getQName(getNamespaceURI()));
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
        SORTED_ELEMENTS.add( NAME);
        SORTED_ELEMENTS.add( VALUE );
    }

}
