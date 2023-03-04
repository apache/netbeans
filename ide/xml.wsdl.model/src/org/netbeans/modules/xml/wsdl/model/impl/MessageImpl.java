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

import java.util.Collection;
import org.netbeans.modules.xml.wsdl.model.Message;
import org.netbeans.modules.xml.wsdl.model.Part;
import org.netbeans.modules.xml.wsdl.model.WSDLModel;
import org.netbeans.modules.xml.wsdl.model.visitor.WSDLVisitor;
import org.w3c.dom.Element;

/**
 *
 * @author nn136682
 */
public class MessageImpl extends NamedImpl implements Message {
    
    /** Creates a new instance of MessageImpl */
    public MessageImpl(WSDLModel model, Element e) {
        super(model, e);
    }
    public MessageImpl(WSDLModel model) {
        this(model, createNewElement(WSDLQNames.MESSAGE.getQName(), model));
    }
    
    public Collection<Part> getParts() {
        return getChildren(Part.class);
    }

    public void removePart(Part part) {
        removeChild(PART_PROPERTY, part);
    }

    public void addPart(Part part) {
        appendChild(PART_PROPERTY, part);
    }

    public void accept(WSDLVisitor visitor) {
        visitor.visit(this);
    }
}
