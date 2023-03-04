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

package org.netbeans.modules.xml.wsdl.model.impl;

import org.netbeans.modules.xml.wsdl.model.Definitions;
import org.netbeans.modules.xml.wsdl.model.Import;
import org.netbeans.modules.xml.wsdl.model.ReferenceableWSDLComponent;
import org.netbeans.modules.xml.wsdl.model.WSDLModel;
import org.netbeans.modules.xml.wsdl.model.spi.WSDLComponentBase;
import org.netbeans.modules.xml.wsdl.model.visitor.FindReferencedVisitor;
import org.netbeans.modules.xml.xam.dom.AbstractNamedComponentReference;
import org.netbeans.modules.xml.xam.dom.NamedComponentReference;
import org.netbeans.modules.xml.xam.locator.CatalogModelException;

/**
 *
 * @author Nam Nguyen
 * @author rico
 */
public class GlobalReferenceImpl<T extends ReferenceableWSDLComponent> 
        extends AbstractNamedComponentReference<T> implements NamedComponentReference<T> {
    
    /** Creates a new instance of GlobalReferenceImpl */
    //for use by factory, create from scratch
    public GlobalReferenceImpl(
            T referenced, 
            Class<T> type, 
            WSDLComponentBase parent) {
        super(referenced, type, parent);
    }
    
    //for use by resolve methods
    public GlobalReferenceImpl(
            Class<T> type, 
            WSDLComponentBase parent, 
            String refString){
        super(type, parent, refString);
    }
    
    protected Definitions getDefinitions() {
        WSDLComponentBase wparent = WSDLComponentBase.class.cast(getParent());
        return wparent.getModel().getDefinitions();
    }
    
    public T get() {
        WSDLComponentBase wparent = WSDLComponentBase.class.cast(getParent());
        if (super.getReferenced() == null) {
            String localName = getLocalName();
            String namespace = getEffectiveNamespace();
            WSDLModel model = wparent.getWSDLModel();
            T target = null;
            String targetNamespace = model.getDefinitions().getTargetNamespace();
            if ((namespace == null && targetNamespace == null) ||
                (namespace != null && namespace.equals(targetNamespace))) {
                target = new FindReferencedVisitor<T>(model.getDefinitions()).find(localName, getType());
            }
            if (target == null) {
                for (Import i : wparent.getWSDLModel().getDefinitions().getImports()) {
                    if (! i.getNamespace().equals(namespace)) {
                        continue;
                    }
                    try {
                        model = i.getImportedWSDLModel();
                    } catch(CatalogModelException ex) {
                        continue;
                    }
                    target = new FindReferencedVisitor<T>(model.getDefinitions()).find(localName, getType());
                    if (target != null) {
                        break;
                    }
                }
            }
            setReferenced(target);
        }
        return getReferenced();
    }
    
    public WSDLComponentBase getParent() {
        return (WSDLComponentBase) super.getParent();
    }
    
    public String getEffectiveNamespace() {
        if (getReferenced() != null) {
            return getReferenced().getModel().getDefinitions().getTargetNamespace();
        } else {
            assert refString != null;
            return getParent().lookupNamespaceURI(getPrefix());
        }
    }
}
