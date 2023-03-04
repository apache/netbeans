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
package org.netbeans.modules.websvc.api.jaxws.bindings.impl;

import java.util.List;
import javax.xml.namespace.QName;
import org.netbeans.modules.websvc.api.jaxws.bindings.BindingsComponent;
import org.netbeans.modules.websvc.api.jaxws.bindings.BindingsModel;
import org.netbeans.modules.xml.xam.dom.Attribute;
import org.netbeans.modules.xml.xam.dom.AbstractDocumentComponent;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/**
 *
 * @author Roderico Cruz
 */
public abstract class BindingsComponentImpl extends AbstractDocumentComponent<BindingsComponent>
        implements BindingsComponent{
    
    /** Creates a new instance of BindingsComponentImpl */
    public BindingsComponentImpl(BindingsModelImpl model, Element e) {
        super(model, e);
    }
    
    protected Object getAttributeValueOf(Attribute attr, String stringValue) {
        return stringValue;
    }

    protected void populateChildren(List<BindingsComponent> children) {
        NodeList nl = getPeer().getChildNodes();
        if (nl != null){
            for (int i = 0; i < nl.getLength(); i++) {
                org.w3c.dom.Node n = nl.item(i);
                if (n instanceof Element) {
                    BindingsModel bindingsModel = getModel();
                    BindingsComponentImpl comp = (BindingsComponentImpl) bindingsModel.getFactory().create((Element)n,this);
                    if (comp != null) {
                        children.add(comp);
                    }
                }
            }
        }
    }

    protected abstract String getNamespaceURI(); 

    public BindingsModelImpl getModel() {
        return (BindingsModelImpl) super.getModel();
    }
    
    protected static org.w3c.dom.Element createNewElement(QName qName, BindingsModel model){
        return model.getDocument().createElementNS(
                qName.getNamespaceURI(),
                qName.getLocalPart());
    }
    
    protected static org.w3c.dom.Element createPrefixedElement(QName qName, BindingsModel model){
        org.w3c.dom.Element e = createNewElement(qName, model);
        e.setPrefix(qName.getPrefix());
        return e;
    }
    
}
