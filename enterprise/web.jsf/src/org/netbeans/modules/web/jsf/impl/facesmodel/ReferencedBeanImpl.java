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
import org.netbeans.modules.web.jsf.api.facesmodel.ReferencedBean;
import org.w3c.dom.Element;


/**
 * @author ads
 *
 */
class ReferencedBeanImpl extends IdentifiableDescriptionGroupImpl implements
        ReferencedBean
{

    ReferencedBeanImpl( JSFConfigModelImpl model, Element element ) {
        super(model, element);
    }
    
    ReferencedBeanImpl( JSFConfigModelImpl model ) {
        super(model, createElementNS(model, JSFConfigQNames.REFERENCED_BEAN));
    }

    /**
     * Gets referenced-bean-class of the faces-config-referenced-beanType.
     * @return trimmed referenced-bean-class if any, {@code null} otherwise
     */
    public String getReferencedBeanClass() {
        String beanClass = getChildElementText(JSFConfigQNames.REFERENCED_BEAN_CLASS.getQName(getNamespaceURI()));
        return ElementTypeHelper.pickFullyQualifiedClassType(beanClass);
    }

    /**
     * Gets referenced-bean-name of the faces-config-referenced-beanType.
     * @return trimmed referenced-bean-name if any, {@code null} otherwise
     */
    public String getReferencedBeanName() {
        String beanName = getChildElementText(JSFConfigQNames.REFERENCED_BEAN_NAME.getQName(getNamespaceURI()));
        return ElementTypeHelper.pickJavaIdentifierType(beanName);
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.web.jsf.api.facesmodel.ReferencedBean#setReferencedBeanClass(java.lang.String)
     */
    public void setReferencedBeanClass( String clazz ) {
        setChildElementText(REFERENCED_BEAN_CLASS, clazz, 
                JSFConfigQNames.REFERENCED_BEAN_CLASS.getQName(getNamespaceURI()));
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.web.jsf.api.facesmodel.ReferencedBean#setReferencedBeanName(java.lang.String)
     */
    public void setReferencedBeanName( String name ) {
        setChildElementText(REFERENCED_BEAN_NAME, name, 
                JSFConfigQNames.REFERENCED_BEAN_NAME.getQName(getNamespaceURI()));
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
            DESCRIPTION_GROUP_SORTED_ELEMENTS.size() +2 );
    
    static {
        SORTED_ELEMENTS.add( REFERENCED_BEAN_NAME);
        SORTED_ELEMENTS.add( REFERENCED_BEAN_CLASS);
    }

}
