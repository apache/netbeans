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

import java.util.Collections;
import org.netbeans.modules.websvc.api.jaxws.bindings.BindingsComponent;
import org.netbeans.modules.websvc.api.jaxws.bindings.BindingsHandlerChains;
import org.netbeans.modules.websvc.api.jaxws.bindings.BindingsModel;
import org.netbeans.modules.websvc.api.jaxws.bindings.DefinitionsBindings;

import org.w3c.dom.Element;

/**
 *
 * @author Roderico Cruz
 */
public class DefinitionsBindingsImpl extends BindingsComponentImpl 
             implements DefinitionsBindings{
    
    /** Creates a new instance of DefinitionsBindingsImpl */
    public DefinitionsBindingsImpl(BindingsModelImpl model, Element e) {
        super(model, e);
    }
    
    public DefinitionsBindingsImpl(BindingsModelImpl model){
        this(model, createPrefixedElement(BindingsQName.BINDINGS.getQName(), model));
    }

    public void setNode(String node) {
        setAttribute(NODE_PROPERTY, BindingsAttribute.NODE, node);
    }

    public void setHandlerChains(BindingsHandlerChains handlerChains) {
        java.util.List<Class<? extends BindingsComponent>> classes = Collections.emptyList();
        setChild(BindingsHandlerChains.class,
                HANDLER_CHAINS_PROPERTY, handlerChains, classes);
    }

    public String getNode() {
        return getAttribute(BindingsAttribute.NODE);
    }

    protected String getNamespaceURI() {
        return BindingsQName.JAXWS_NS_URI;
    }

    public BindingsHandlerChains getHandlerChains() {
        return getChild(BindingsHandlerChains.class);
    }
    
}
