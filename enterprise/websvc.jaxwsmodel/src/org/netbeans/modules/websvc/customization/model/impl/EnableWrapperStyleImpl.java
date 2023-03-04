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
/*
 * EnableWrapperStyleImpl.java
 *
 * Created on February 3, 2006, 3:28 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.netbeans.modules.websvc.customization.model.impl;



import org.netbeans.modules.websvc.api.customization.model.JAXWSQName;
import org.netbeans.modules.websvc.api.customization.model.EnableWrapperStyle;
import org.netbeans.modules.xml.wsdl.model.WSDLModel;
import org.netbeans.modules.xml.wsdl.model.visitor.WSDLVisitor;
import org.w3c.dom.Element;

/**
 *
 * @author Roderico Cruz
 */
public class EnableWrapperStyleImpl extends CustomizationComponentImpl
        implements EnableWrapperStyle{

    /** Creates a new instance of EnableWrapperStyleImpl */
    public EnableWrapperStyleImpl(WSDLModel model, Element e) {
        super(model, e);
    }
    
    public EnableWrapperStyleImpl(WSDLModel model){
        this(model, createPrefixedElement(JAXWSQName.ENABLEWRAPPERSTYLE.getQName(), model));
    }
    
    public boolean isEnabled() {
        String value = getText().trim();
        if(value.equals("true")){
            return true;
        }
        return false;
    }
    
    public void setEnabled(boolean enable) {
        setText(ENABLE_WRAPPER_STYLE_VALUE_PROPERTY, Boolean.toString(enable));
    }

    public void accept(WSDLVisitor visitor) {
        visitor.visit(this);
    }
    
}
