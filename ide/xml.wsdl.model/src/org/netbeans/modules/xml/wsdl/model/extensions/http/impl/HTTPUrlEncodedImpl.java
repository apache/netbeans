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

import org.netbeans.modules.xml.wsdl.model.BindingInput;
import org.netbeans.modules.xml.wsdl.model.BindingOutput;
import org.netbeans.modules.xml.wsdl.model.WSDLModel;
import org.netbeans.modules.xml.wsdl.model.extensions.http.HTTPComponent.Visitor;
import org.netbeans.modules.xml.wsdl.model.extensions.http.HTTPQName;
import org.netbeans.modules.xml.wsdl.model.extensions.http.HTTPUrlReplacement;
import org.netbeans.modules.xml.xam.Component;
import org.w3c.dom.Element;

public class HTTPUrlEncodedImpl extends HTTPComponentImpl implements HTTPUrlReplacement {

    public HTTPUrlEncodedImpl(WSDLModel model, Element e) {
        super(model, e);
    }
    
    public HTTPUrlEncodedImpl(WSDLModel model){
        this(model, createPrefixedElement(HTTPQName.URLREPLACEMENT.getQName(), model));
    }
    
    public void accept(Visitor visitor) {
        visitor.visit(this);
    }

    @Override
    public boolean canBeAddedTo(Component target) {
        return (target instanceof BindingInput || target instanceof BindingOutput);
    }
}
