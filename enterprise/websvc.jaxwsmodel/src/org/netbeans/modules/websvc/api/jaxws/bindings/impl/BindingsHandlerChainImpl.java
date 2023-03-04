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

import java.util.Collection;
import org.netbeans.modules.websvc.api.jaxws.bindings.BindingsHandler;
import org.netbeans.modules.websvc.api.jaxws.bindings.BindingsHandlerChain;
import org.netbeans.modules.websvc.api.jaxws.bindings.BindingsModel;

import org.w3c.dom.Element;

/**
 *
 * @author Roderico Cruz
 */
public class BindingsHandlerChainImpl extends 
        BindingsComponentImpl implements BindingsHandlerChain{
    
    /**
     * Creates a new instance of BindingsHandlerChainImpl
     */
    public BindingsHandlerChainImpl(BindingsModelImpl model, Element e) {
        super(model, e);
    }
    
    public BindingsHandlerChainImpl(BindingsModelImpl model){
        this(model, createPrefixedElement(BindingsQName.HANDLER_CHAIN.getQName(), model));
    }

    public void removeHandler(BindingsHandler handler) {
        removeChild(HANDLER_PROPERTY, handler);
    }

    public void addHandler(BindingsHandler handler) {
        appendChild(HANDLER_PROPERTY, handler);
    }

    public Collection<BindingsHandler> getHandlers() {
        return getChildren(BindingsHandler.class);
    }

    protected String getNamespaceURI() {
        return BindingsQName.JAVAEE_NS_URI;
    }
}
