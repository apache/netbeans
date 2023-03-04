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

import java.util.List;
import org.netbeans.modules.xml.wsdl.model.BindingInput;
import org.netbeans.modules.xml.wsdl.model.BindingOutput;
import org.netbeans.modules.xml.wsdl.model.WSDLModel;
import org.netbeans.modules.xml.wsdl.model.extensions.mime.MIMEComponent;
import org.netbeans.modules.xml.wsdl.model.extensions.mime.MIMEMultipartRelated;
import org.netbeans.modules.xml.wsdl.model.extensions.mime.MIMEPart;
import org.netbeans.modules.xml.xam.Component;
import org.w3c.dom.Element;

/**
 *
 * @author jyang
 */
public class MIMEMultipartRelatedImpl extends MIMEComponentImpl
        implements MIMEMultipartRelated {

    public MIMEMultipartRelatedImpl(WSDLModel model, Element e) {
        super(model, e);
    }

    public void addMIMEPart(MIMEPart mimepart) {
        appendChild(PART_PROPERTY, mimepart);
    }

    public List<MIMEPart> getMIMEParts() {
        return getChildren(MIMEPart.class);
    }

    public void removeMIMEPart(MIMEPart mimepart) {
        removeChild(PART_PROPERTY, mimepart);
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
}
