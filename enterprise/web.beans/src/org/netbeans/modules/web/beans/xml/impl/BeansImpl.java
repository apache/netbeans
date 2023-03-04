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
package org.netbeans.modules.web.beans.xml.impl;

import java.util.List;

import org.netbeans.modules.web.beans.xml.Beans;
import org.netbeans.modules.web.beans.xml.BeansElement;
import org.netbeans.modules.web.beans.xml.WebBeansComponent;
import org.netbeans.modules.web.beans.xml.WebBeansVisitor;
import org.w3c.dom.Element;


/**
 * @author ads
 *
 */
class BeansImpl extends WebBeansComponentImpl implements Beans {

    BeansImpl( WebBeansModelImpl  model, Element e ) {
        super(model, e);
    }
    
    BeansImpl( WebBeansModelImpl  model) {
        this(model, createNewElement( BEANS, model));
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.web.beans.xml.Beans#addElement(org.netbeans.modules.web.beans.xml.BeansElement)
     */
    public void addElement( BeansElement element ) {
        appendChild(BEANS_ELEMENT,  element );
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.web.beans.xml.Beans#addElement(int, org.netbeans.modules.web.beans.xml.BeansElement)
     */
    public void addElement( int index, BeansElement element ) {
        insertAtIndex( BEANS_ELEMENT, element, index);
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.web.beans.xml.Beans#getElements()
     */
    public List<BeansElement> getElements() {
        return getChildren( BeansElement.class );
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.web.beans.xml.Beans#removeElement(org.netbeans.modules.web.beans.xml.BeansElement)
     */
    public void removeElement( BeansElement element ) {
        removeChild( BEANS_ELEMENT,  element );
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.web.beans.xml.WebBeansComponent#accept(org.netbeans.modules.web.beans.xml.WebBeansVisitor)
     */
    public void accept( WebBeansVisitor visitor ) {
        visitor.visit( this );
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.web.beans.xml.WebBeansComponent#getComponentType()
     */
    public Class<? extends WebBeansComponent> getComponentType() {
        return Beans.class;
    }

}
