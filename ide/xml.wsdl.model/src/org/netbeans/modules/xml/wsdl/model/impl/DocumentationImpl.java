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

import java.io.IOException;
import org.netbeans.modules.xml.wsdl.model.Documentation;
import org.netbeans.modules.xml.wsdl.model.WSDLModel;
import org.netbeans.modules.xml.wsdl.model.spi.WSDLComponentBase;
import org.netbeans.modules.xml.wsdl.model.visitor.WSDLVisitor;
import org.w3c.dom.Element;

/**
 *
 * @author rico
 */
public class DocumentationImpl extends WSDLComponentBase implements Documentation{
    
    /** Creates a new instance of DocumentationImpl */
    public DocumentationImpl(WSDLModel model, Element e) {
        super(model, e);
    }
    public DocumentationImpl(WSDLModel model){
        this(model, createNewElement(WSDLQNames.DOCUMENTATION.getQName(), model));
    }

    public void accept(WSDLVisitor visitor) {
        visitor.visit(this);
    }

    // Documentation cannot have Documentation
    public void setDocumentation(Documentation doc) {}
    public Documentation getDocumentation() {  return null;  }
    
    public void setTextContent(String content) {
        super.setText(CONTENT_PROPERTY, content);
    }

    public String getTextContent() {
        return getText();
    }

    public Element getDocumentationElement() {
        return Element.class.cast(getPeer().cloneNode(true));
    }
    
    public void setDocumentationElement(Element documentationElement) {
        super.updatePeer(CONTENT_PROPERTY, documentationElement);
    }

    public String getContentFragment() {
        return super.getXmlFragment();
    }
    
    public void setContentFragment(String text) throws IOException {
        super.setXmlFragment(CONTENT_PROPERTY, text);
    }
}
