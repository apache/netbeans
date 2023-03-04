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

package org.netbeans.modules.xml.wsdl.model.extensions.soap12.impl;

import java.util.Collection;

import org.netbeans.modules.xml.wsdl.model.Binding;
import org.netbeans.modules.xml.wsdl.model.BindingOperation;
import org.netbeans.modules.xml.wsdl.model.WSDLComponent;
import org.netbeans.modules.xml.wsdl.model.WSDLModel;
import org.netbeans.modules.xml.wsdl.model.extensions.soap12.SOAP12Binding.Style;
import org.netbeans.modules.xml.wsdl.model.extensions.soap12.SOAP12Binding;
import org.netbeans.modules.xml.wsdl.model.extensions.soap12.SOAP12Component;
import org.netbeans.modules.xml.wsdl.model.extensions.soap12.SOAP12Operation;
import org.netbeans.modules.xml.wsdl.model.extensions.soap12.SOAP12QName;
import org.netbeans.modules.xml.xam.Component;
import org.w3c.dom.Element;

/**
 * @author Sujit Biswas
 *
 */
public class SOAP12OperationImpl extends SOAP12ComponentImpl implements SOAP12Operation {
    
    /** Creates a new instance of SOAPOperationImpl */
    public SOAP12OperationImpl(WSDLModel model, Element e) {
        super(model, e);
    }
    
    public SOAP12OperationImpl(WSDLModel model){
        this(model, createPrefixedElement(SOAP12QName.OPERATION.getQName(), model));
    }
    
    public void accept(SOAP12Component.Visitor visitor) {
        visitor.visit(this);
    }

    public void setSoapAction(String soapActionURI) {
        setAttribute(SOAP_ACTION_PROPERTY, SOAP12Attribute.SOAP_ACTION, soapActionURI);
    }

    public String getSoapAction() {
        return getAttribute(SOAP12Attribute.SOAP_ACTION);
    }
    
    public void setSoapActionRequired(String soapActionRequired) {
        setAttribute(SOAP_ACTION_REQUIRED_PROPERTY, SOAP12Attribute.SOAP_ACTION_REQUIRED, soapActionRequired);
    }

    public String getSoapActionRequired() {
        return getAttribute(SOAP12Attribute.SOAP_ACTION_REQUIRED);
    }
   
    public void setStyle(Style v) {
        setAttribute(STYLE_PROPERTY, SOAP12Attribute.STYLE, v);
    }

    public Style getStyle() {
        String s = getAttribute(SOAP12Attribute.STYLE);
        if (s == null) {
            WSDLComponent ancestor = getParent() == null? null : getParent().getParent();
            if (ancestor instanceof Binding) {
                Binding b = (Binding) ancestor;
                Collection<SOAP12Binding> sbs = b.getExtensibilityElements(SOAP12Binding.class);
                if (sbs.size() > 0) {
                    SOAP12Binding sb = sbs.iterator().next();
                    Style sbStyle = sb.getStyle();
                    if (sbStyle != null) {
                        return sbStyle;
                    }
                }
            }
            return Style.DOCUMENT;
        }

        return Style.valueOf(s.toUpperCase());

    }

    @Override
    public boolean canBeAddedTo(Component target) {
        if (target instanceof BindingOperation) {
            return true;
        }
        return false;
    }
}
