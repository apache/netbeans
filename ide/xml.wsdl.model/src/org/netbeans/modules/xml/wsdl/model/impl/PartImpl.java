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

package org.netbeans.modules.xml.wsdl.model.impl;

import org.netbeans.modules.xml.schema.model.GlobalElement;
import org.netbeans.modules.xml.schema.model.GlobalType;
import org.netbeans.modules.xml.wsdl.model.Part;
import org.netbeans.modules.xml.wsdl.model.WSDLModel;
import org.netbeans.modules.xml.wsdl.model.visitor.WSDLVisitor;
import org.netbeans.modules.xml.xam.dom.NamedComponentReference;
import org.w3c.dom.Element;

/**
 *
 * @author Nam Nguyen
 */
public class PartImpl extends NamedImpl implements Part {
    
    /** Creates a new instance of PartImpl */
    public PartImpl(WSDLModel model, Element e) {
        super(model, e);
    }
    public PartImpl(WSDLModel model) {
        this(model, createNewElement(WSDLQNames.PART.getQName(), model));
    }
    
    public void accept(WSDLVisitor visitor) {
        visitor.visit(this);
    }
    
    public void setType(NamedComponentReference<GlobalType> typeRef) {
        setAttribute(TYPE_PROPERTY, WSDLAttribute.TYPE, typeRef);
    }
    
     public NamedComponentReference<GlobalType> getType() {
        return resolveSchemaReference(GlobalType.class, WSDLAttribute.TYPE);
    }
    
     public void setElement(NamedComponentReference<GlobalElement> elementRef){
        setAttribute(ELEMENT_PROPERTY, WSDLAttribute.ELEMENT, elementRef);
    }
    
     public NamedComponentReference<GlobalElement> getElement() {
         return resolveSchemaReference(GlobalElement.class, WSDLAttribute.ELEMENT);
    }
    
}
