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
package org.netbeans.modules.websvc.jaxws.catalog.impl;

import java.util.List;
import org.netbeans.modules.websvc.jaxws.catalog.CatalogQNames;
import org.netbeans.modules.websvc.jaxws.catalog.CatalogComponent;
import org.netbeans.modules.websvc.jaxws.catalog.CatalogModel;
import org.netbeans.modules.xml.xam.dom.AbstractDocumentComponent;
import org.netbeans.modules.xml.xam.dom.Attribute;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

public abstract class CatalogComponentImpl extends AbstractDocumentComponent<CatalogComponent> 
            implements CatalogComponent {
    
    public CatalogComponentImpl(CatalogModelImpl model, Element element) {
        super(model, element);
    }
    
    public CatalogModelImpl getModel() {
        return (CatalogModelImpl) super.getModel();
    }

    public static Element createElementNS(CatalogModel model, CatalogQNames rq) {
        return model.getDocument().createElementNS(rq.getQName().getNamespaceURI(), rq.getQualifiedName());
    }
    
    protected Object getAttributeValueOf(Attribute attr, String stringValue) {
        return stringValue;
    }

    protected void populateChildren(List<CatalogComponent> children) {
        NodeList nl = getPeer().getChildNodes();
        if (nl != null){
            for (int i = 0; i < nl.getLength(); i++) {
                org.w3c.dom.Node n = nl.item(i);
                if (n instanceof Element) {
                    CatalogModel model = getModel();
                    CatalogComponent comp = (CatalogComponent) model.getFactory().create((Element)n, this);
                    if (comp != null) {
                        children.add(comp);
                    }
                }
            }
        }
    }

    
}
