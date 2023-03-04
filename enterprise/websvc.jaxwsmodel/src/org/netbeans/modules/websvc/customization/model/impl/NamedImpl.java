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
 * NamedImpl.java
 *
 * Created on February 6, 2006, 4:41 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.netbeans.modules.websvc.customization.model.impl;


import org.netbeans.modules.xml.wsdl.model.WSDLComponent;
import org.netbeans.modules.xml.wsdl.model.WSDLModel;
import org.netbeans.modules.xml.wsdl.model.visitor.WSDLVisitor;
import org.netbeans.modules.xml.xam.Nameable;
import org.w3c.dom.Element;

/**
 *
 * @author Roderico Cruz
 */
public abstract class NamedImpl extends CustomizationComponentImpl
   implements Nameable<WSDLComponent>{
    
    /** Creates a new instance of NamedImpl */
    public NamedImpl(WSDLModel model, Element e) {
        super(model, e);
    }

    public void setName(String name) {
        setAttribute(NAME_PROPERTY, CustomizationAttribute.NAME, name);
    }

    public String getName() {
        return getAttribute(CustomizationAttribute.NAME);
    }
}
