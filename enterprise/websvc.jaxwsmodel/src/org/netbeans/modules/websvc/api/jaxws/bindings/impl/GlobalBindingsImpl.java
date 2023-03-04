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
import org.netbeans.modules.websvc.api.jaxws.bindings.BindingsModel;
import org.netbeans.modules.websvc.api.jaxws.bindings.DefinitionsBindings;
import org.netbeans.modules.websvc.api.jaxws.bindings.GlobalBindings;

import org.w3c.dom.Element;

/**
 *
 * @author Roderico Cruz
 */
public class GlobalBindingsImpl extends BindingsComponentImpl 
          implements GlobalBindings{
    
    /** Creates a new instance of GlobalBindingsImpl */
    public GlobalBindingsImpl(BindingsModelImpl model, Element e) {
        super(model, e);
    }
    
    public GlobalBindingsImpl(BindingsModelImpl model){
        this(model, createPrefixedElement(BindingsQName.BINDINGS.getQName(), model));
    }

    public void setWsdlLocation(String wsdlLocation) {
        setAttribute(WSDL_LOCATION_PROPERTY, BindingsAttribute.WSDL_LOCATION, wsdlLocation);
    }

    public void setDefinitionsBindings(DefinitionsBindings bindings) {
        java.util.List<Class<? extends BindingsComponent>> classes = Collections.emptyList();
        setChild(DefinitionsBindings.class,
                DEFINITIONS_BINDINGS_PROPERTY, bindings, classes);
    }
    
    public DefinitionsBindings getDefinitionsBindings() {
        return getChild(DefinitionsBindings.class);
    }

    public String getWsdlLocation() {
        return getAttribute(BindingsAttribute.WSDL_LOCATION);
    }

    protected String getNamespaceURI() {
        return BindingsQName.JAXWS_NS_URI;
    }

    
    
}
