/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
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

import org.netbeans.modules.web.beans.xml.WebBeansComponent;
import org.netbeans.modules.xml.xam.dom.AbstractDocumentComponent;
import org.netbeans.modules.xml.xam.dom.Attribute;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;


/**
 * @author ads
 *
 */
abstract class WebBeansComponentImpl extends 
    AbstractDocumentComponent<WebBeansComponent> implements WebBeansComponent
{
    WebBeansComponentImpl( WebBeansModelImpl model, Element e ) {
        super(model, e);
    }
    
    /* (non-Javadoc)
     * @see org.netbeans.modules.xml.xam.dom.AbstractDocumentComponent#getModel()
     */
    @Override
    public WebBeansModelImpl getModel() {
        return (WebBeansModelImpl)super.getModel();
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.xml.xam.dom.AbstractDocumentComponent#getAttributeValueOf(org.netbeans.modules.xml.xam.dom.Attribute, java.lang.String)
     */
    @Override
    protected Object getAttributeValueOf( Attribute attr, String stringValue ) {
        return null;
    }
    
    protected static Element createNewElement(String name, WebBeansModelImpl model){
        String ns = WebBeansComponent.WEB_BEANS_NAMESPACE;
        if(model.getRootComponent() instanceof AbstractDocumentComponent) {
            ns = ((AbstractDocumentComponent)model.getRootComponent()).getQName().getNamespaceURI();
        }
        return model.getDocument().createElementNS( ns, name );
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.xml.xam.dom.AbstractDocumentComponent#populateChildren(java.util.List)
     */
    @Override
    protected void populateChildren( List<WebBeansComponent> children ) {
        NodeList nl = getPeer().getChildNodes();
        if (nl != null) {
            for (int i = 0; i < nl.getLength(); i++) {
                Node n = nl.item(i);
                if (n instanceof Element) {
                    WebBeansComponent comp = getModel().getFactory().createComponent((Element) n, this);
                    if (comp != null) {
                        children.add(comp);
                    }
                }
            }
        }
    }

}
