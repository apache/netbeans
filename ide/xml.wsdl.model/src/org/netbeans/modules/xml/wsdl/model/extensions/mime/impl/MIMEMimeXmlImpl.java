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

package org.netbeans.modules.xml.wsdl.model.extensions.mime.impl;

import org.netbeans.modules.xml.wsdl.model.BindingInput;
import org.netbeans.modules.xml.wsdl.model.BindingOutput;
import org.netbeans.modules.xml.wsdl.model.Part;
import org.netbeans.modules.xml.wsdl.model.WSDLModel;
import org.netbeans.modules.xml.wsdl.model.extensions.mime.MIMEComponent;
import org.netbeans.modules.xml.wsdl.model.extensions.mime.MIMEMimeXml;
import org.netbeans.modules.xml.wsdl.model.extensions.mime.MIMEQName;
import org.netbeans.modules.xml.xam.Component;
import org.netbeans.modules.xml.xam.Reference;
import org.w3c.dom.Element;

/**
 *
 * @author jyang
 */
public class MIMEMimeXmlImpl extends MIMEComponentImpl implements MIMEMimeXml {

    public MIMEMimeXmlImpl(WSDLModel model, Element e) {
        super(model, e);
    }

    public MIMEMimeXmlImpl(WSDLModel model) {
        this(model, createPrefixedElement(MIMEQName.MIME_XML.getQName(), model));
    }

    /**
     * Set the part for this MIME content.
     *
     * @param part the desired part
     */
    public void setPart(String part) {
        setAttribute(PART_PROPERTY, MIMEAttribute.PART, part);
    }

    /**
     * Get the part for this MIME content.
     */
    public String getPart() {
        return getAttribute(MIMEAttribute.PART);
    }

    @Override
    public boolean canBeAddedTo(Component target) {
        if (target instanceof BindingInput || target instanceof BindingOutput) {
            return true;
        }
        return false;
    }

    public void accept(MIMEComponent.Visitor visitor) {
        visitor.visit(this);
    }

    public Reference<Part> getPartRef() {
        String v = getPart();
        return v == null ? null : new PartReference(this, v);
    }

    public void setTypeRef(Reference<Part> partRef) {
        String v = partRef == null ? null : partRef.getRefString();
        setAttribute(PART_PROPERTY, MIMEAttribute.PART, v);
    }
}
    
    

