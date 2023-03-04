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

import java.util.ArrayList;
import java.util.List;

import org.netbeans.modules.web.beans.xml.AlternativeElement;
import org.netbeans.modules.web.beans.xml.Alternatives;
import org.netbeans.modules.web.beans.xml.Stereotype;
import org.netbeans.modules.web.beans.xml.WebBeansComponent;
import org.netbeans.modules.web.beans.xml.WebBeansVisitor;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;


/**
 * @author ads
 *
 */
class AlternativesImpl extends BaseClassContainerImpl implements
        Alternatives
{
    private final WebBeansModelImpl model;

    AlternativesImpl( WebBeansModelImpl model, Element e ) {
        super(model, e);
        this.model = model;
    }
    
    AlternativesImpl( WebBeansModelImpl model) {
        this(model, createNewElement( ALTERNATIVES , model ));
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.web.beans.xml.Alternatives#addElement(org.netbeans.modules.web.beans.xml.AlternativeElement)
     */
    public void addElement( AlternativeElement element ) {
        appendChild(AlternativeElement.ALTERNATIVE_ELEMENT, element);
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.web.beans.xml.Alternatives#addElement(int, org.netbeans.modules.web.beans.xml.AlternativeElement)
     */
    public void addElement( int index, AlternativeElement element ) {
        insertAtIndex(AlternativeElement.ALTERNATIVE_ELEMENT, element, index);
    }


    /* (non-Javadoc)
     * @see org.netbeans.modules.web.beans.xml.Alternatives#getElements()
     */
    public List<AlternativeElement> getElements() {
        return getChildren( AlternativeElement.class);
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.web.beans.xml.Alternatives#getSterrotypes()
     */
    public List<String> getSterrotypes() {
        NodeList nl = getPeer().getElementsByTagName(Stereotype.STEREOTYPE);
        List<String> result = new ArrayList<String>( nl.getLength());
        if (nl != null) {
            for (int i=0; i<nl.getLength(); i++) {
                if (WebBeansElements.STEREOTYPE.getQName(model).equals(
                        getQName(nl.item(i)))) 
                {
                    result.add(getText((Element) nl.item(i)));
                }
            }
        }
        return result;
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.web.beans.xml.Alternatives#removeElement(org.netbeans.modules.web.beans.xml.AlternativeElement)
     */
    public void removeElement( AlternativeElement element ) {
        removeChild(AlternativeElement.ALTERNATIVE_ELEMENT, element);
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.web.beans.xml.WebBeansComponent#accept(org.netbeans.modules.web.beans.xml.WebBeansVisitor)
     */
    public void accept( WebBeansVisitor visitor ) {
        visitor.visit(this );
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.web.beans.xml.WebBeansComponent#getComponentType()
     */
    public Class<? extends WebBeansComponent> getComponentType() {
        return Alternatives.class;
    }

}
