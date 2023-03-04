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
 * JavaParameterImpl.java
 *
 * Created on February 4, 2006, 4:50 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.netbeans.modules.websvc.customization.model.impl;


import org.netbeans.modules.websvc.api.customization.model.JAXWSQName;
import org.netbeans.modules.websvc.api.customization.model.JavaParameter;
import org.netbeans.modules.xml.wsdl.model.WSDLModel;
import org.netbeans.modules.xml.wsdl.model.visitor.WSDLVisitor;
import org.w3c.dom.Element;

/**
 *
 * @author Roderico Cruz
 */
public class JavaParameterImpl extends NamedImpl
   implements JavaParameter{
    
    /** Creates a new instance of JavaParameterImpl */
    public JavaParameterImpl(WSDLModel model, Element e) {
        super(model, e);
    }
    
    public JavaParameterImpl(WSDLModel model){
        this(model, createPrefixedElement(JAXWSQName.PARAMETER.getQName(), model));
    }

    public void setPart(String part) {
        setAttribute(PART_PROPERTY, CustomizationAttribute.PART, part);
    }

    public void setChildElementName(String child) {
        setAttribute(CHILD_ELEMENT_NAME_PROPERTY, CustomizationAttribute.CHILDELEMENTNAME, child);
    }

    public String getPart() {
       return getAttribute(CustomizationAttribute.PART);
    }

    public String getChildElementName() {
        return getAttribute(CustomizationAttribute.CHILDELEMENTNAME);
    }

    public void accept(WSDLVisitor visitor) {
        visitor.visit(this);
    }
    
}
