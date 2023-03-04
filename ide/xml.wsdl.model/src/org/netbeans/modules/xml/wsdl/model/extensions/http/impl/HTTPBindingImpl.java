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
package org.netbeans.modules.xml.wsdl.model.extensions.http.impl;

import org.netbeans.modules.xml.wsdl.model.Binding;
import org.netbeans.modules.xml.wsdl.model.WSDLModel;
import org.netbeans.modules.xml.wsdl.model.extensions.http.HTTPBinding;
import org.netbeans.modules.xml.wsdl.model.extensions.http.HTTPBinding.Verb;
import org.netbeans.modules.xml.wsdl.model.extensions.http.HTTPComponent;
import org.netbeans.modules.xml.wsdl.model.extensions.http.HTTPQName;
import org.netbeans.modules.xml.xam.Component;
import org.w3c.dom.Element;

public class HTTPBindingImpl extends HTTPComponentImpl implements HTTPBinding {
    
    public HTTPBindingImpl(WSDLModel model, Element e) {
        super(model, e);
    }
    
    public HTTPBindingImpl(WSDLModel model){
        this(model, createPrefixedElement(HTTPQName.BINDING.getQName(), model));
    }
    
    public void accept(HTTPComponent.Visitor visitor) {
        visitor.visit(this);
    }
    
    public void setVerb(Verb style) {
        setAttribute(VERB_PROPERTY, HTTPAttribute.VERB, style);
    }
    
    public Verb getVerb() {
        String s = getAttribute(HTTPAttribute.VERB);
        return s == null ? null : Verb.valueOf(s.toUpperCase());
    }

    private Verb getVerbValueOf(String s) {
        return s == null ? null : Verb.valueOf(s.toUpperCase());
    }
    
    protected Object getAttributeValueOf(HTTPAttribute attr, String s) {
        if (attr == HTTPAttribute.VERB) {
            return getVerbValueOf(s);
        } else {
            return super.getAttributeValueOf(attr, s);
        }
    }

    @Override
    public boolean canBeAddedTo(Component target) {
        return (target instanceof Binding);
    }
}
