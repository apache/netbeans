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

package org.netbeans.modules.xml.wsdl.model.extensions.soap.impl;

import java.util.Collection;
import org.netbeans.modules.xml.wsdl.model.Binding;
import org.netbeans.modules.xml.wsdl.model.BindingOperation;
import org.netbeans.modules.xml.wsdl.model.WSDLComponent;
import org.netbeans.modules.xml.wsdl.model.WSDLModel;
import org.netbeans.modules.xml.wsdl.model.extensions.soap.SOAPBinding;
import org.netbeans.modules.xml.wsdl.model.extensions.soap.SOAPOperation;
import org.netbeans.modules.xml.wsdl.model.extensions.soap.SOAPBinding.Style;
import org.netbeans.modules.xml.wsdl.model.extensions.soap.SOAPComponent;
import org.netbeans.modules.xml.wsdl.model.extensions.soap.SOAPQName;
import org.netbeans.modules.xml.xam.Component;
import org.w3c.dom.Element;

/**
 *
 * @author Nam Nguyen
 */
public class SOAPOperationImpl extends SOAPComponentImpl implements SOAPOperation {
    
    /** Creates a new instance of SOAPOperationImpl */
    public SOAPOperationImpl(WSDLModel model, Element e) {
        super(model, e);
    }
    
    public SOAPOperationImpl(WSDLModel model){
        this(model, createPrefixedElement(SOAPQName.OPERATION.getQName(), model));
    }
    
    public void accept(SOAPComponent.Visitor visitor) {
        visitor.visit(this);
    }

    public void setSoapAction(String soapActionURI) {
        setAttribute(SOAP_ACTION_PROPERTY, SOAPAttribute.SOAP_ACTION, soapActionURI);
    }

    public String getSoapAction() {
        return getAttribute(SOAPAttribute.SOAP_ACTION);
    }
   
    public void setStyle(Style v) {
        setAttribute(STYLE_PROPERTY, SOAPAttribute.STYLE, v);
    }

    public Style getStyle() {
        String s = getAttribute(SOAPAttribute.STYLE);
        if (s == null) {
            WSDLComponent ancestor = getParent() == null? null : getParent().getParent();
            if (ancestor instanceof Binding) {
                Binding b = (Binding) ancestor;
                Collection<SOAPBinding> sbs = b.getExtensibilityElements(SOAPBinding.class);
                if (sbs.size() > 0) {
                    SOAPBinding sb = sbs.iterator().next();
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
