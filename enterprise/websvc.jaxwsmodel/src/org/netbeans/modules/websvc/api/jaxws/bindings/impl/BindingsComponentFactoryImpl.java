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

import javax.xml.namespace.QName;
import org.netbeans.modules.websvc.api.jaxws.bindings.BindingsComponent;
import org.netbeans.modules.websvc.api.jaxws.bindings.BindingsComponentFactory;
import org.netbeans.modules.websvc.api.jaxws.bindings.BindingsHandler;
import org.netbeans.modules.websvc.api.jaxws.bindings.BindingsHandlerChain;
import org.netbeans.modules.websvc.api.jaxws.bindings.BindingsHandlerChains;
import org.netbeans.modules.websvc.api.jaxws.bindings.BindingsHandlerClass;
import org.netbeans.modules.websvc.api.jaxws.bindings.BindingsHandlerName;
import org.netbeans.modules.websvc.api.jaxws.bindings.BindingsModel;
import org.netbeans.modules.websvc.api.jaxws.bindings.DefinitionsBindings;
import org.netbeans.modules.websvc.api.jaxws.bindings.GlobalBindings;

import org.w3c.dom.Element;

/**
 *
 * @author Roderico Cruz
 */
public class BindingsComponentFactoryImpl 
          implements BindingsComponentFactory{
    
    private BindingsModelImpl model;
    /** Creates a new instance of BindingsComponentFactoryImpl */
    public BindingsComponentFactoryImpl(BindingsModel model) {
        if (model instanceof BindingsModelImpl) {
            this.model = (BindingsModelImpl) model;
        } else {
            throw new IllegalArgumentException("Excpect BindingsModelImpl");
        }
    }

    public BindingsComponent create(Element e, BindingsComponent parent) {
        //TODO implement Visitor to get rid of this humongous if-else block
        QName childQName = new QName(e.getNamespaceURI(), e.getLocalName());
        if(childQName.equals(BindingsQName.BINDINGS.getQName())){
            if(parent instanceof GlobalBindings){
                return new DefinitionsBindingsImpl(model, e);
            }
            else{
                return new GlobalBindingsImpl(model, e);
            }
        }
        if(childQName.equals(BindingsQName.HANDLER_CHAINS.getQName())){
            return new BindingsHandlerChainsImpl(model, e);
        }
        else if (childQName.equals(BindingsQName.HANDLER_CHAIN.getQName())){
            return new BindingsHandlerChainImpl(model, e);
        }
        else if (childQName.equals(BindingsQName.HANDLER.getQName())){
            return new BindingsHandlerImpl(model, e);
        }
        else if (childQName.equals(BindingsQName.HANDLER_CLASS.getQName())){
            return new BindingsHandlerClassImpl(model, e);
        }
        else if (childQName.equals(BindingsQName.HANDLER_NAME.getQName())){
            return new BindingsHandlerNameImpl(model, e);
        }
        return null;
    }

    public BindingsHandlerClass createHandlerClass() {
        return new BindingsHandlerClassImpl(model);
    }
    public BindingsHandlerName createHandlerName() {
        return new BindingsHandlerNameImpl(model);
    }

    public BindingsHandlerChains createHandlerChains() {
        return new BindingsHandlerChainsImpl(model);
    }

    public BindingsHandlerChain createHandlerChain() {
        return new BindingsHandlerChainImpl(model);
    }

    public GlobalBindings createGlobalBindings() {
        return new GlobalBindingsImpl(model);
    }

    public DefinitionsBindings createDefinitionsBindings() {
        return new DefinitionsBindingsImpl(model);
    }

    public BindingsHandler createHandler() {
        return new BindingsHandlerImpl(model);
    }
    
}
