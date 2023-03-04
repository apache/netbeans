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

import java.util.List;

import org.netbeans.modules.web.jsf.api.facesmodel.AttributeContainer;
import org.netbeans.modules.web.jsf.api.facesmodel.ConfigAttribute;
import org.netbeans.modules.web.jsf.api.facesmodel.Property;
import org.netbeans.modules.web.jsf.api.facesmodel.PropertyContainer;
import org.w3c.dom.Element;



/**
 * @author ads
 *
 */
abstract class PropertyAttributeContainerImpl extends JSFConfigComponentImpl 
    implements PropertyContainer , AttributeContainer 
{

    PropertyAttributeContainerImpl( JSFConfigModelImpl model,
            Element element )
    {
        super(model, element);
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.web.jsf.api.facesmodel.AttributeContainer#addAttribute(org.netbeans.modules.web.jsf.api.facesmodel.ConfigAttribute)
     */
    public void addAttribute( ConfigAttribute attribute ) {
        appendChild( ATTRIBUTE, attribute);
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.web.jsf.api.facesmodel.AttributeContainer#addAttribute(int, org.netbeans.modules.web.jsf.api.facesmodel.ConfigAttribute)
     */
    public void addAttribute( int index, ConfigAttribute attribute ) {
        insertAtIndex( ATTRIBUTE, attribute, index , ConfigAttribute.class);
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.web.jsf.api.facesmodel.AttributeContainer#getAttributes()
     */
    public List<ConfigAttribute> getAttributes() {
        return getChildren( ConfigAttribute.class );
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.web.jsf.api.facesmodel.AttributeContainer#removeAttribute(org.netbeans.modules.web.jsf.api.facesmodel.ConfigAttribute)
     */
    public void removeAttribute( ConfigAttribute attribute ) {
        removeChild( ATTRIBUTE, attribute);
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.web.jsf.api.facesmodel.PropertyContainer#addProperty(int, org.netbeans.modules.web.jsf.api.facesmodel.Property)
     */
    public void addProperty( int index, Property property ) {
        insertAtIndex( PROPERTY , property, index , Property.class );
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.web.jsf.api.facesmodel.PropertyContainer#addProperty(org.netbeans.modules.web.jsf.api.facesmodel.Property)
     */
    public void addProperty( Property property ) {
        appendChild( PROPERTY,  property );
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.web.jsf.api.facesmodel.PropertyContainer#getProperties()
     */
    public List<Property> getProperties() {
        return getChildren( Property.class );
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.web.jsf.api.facesmodel.PropertyContainer#removePropety(org.netbeans.modules.web.jsf.api.facesmodel.Property)
     */
    public void removePropety( Property property ) {
        removeChild( PROPERTY, property );
    }


}
