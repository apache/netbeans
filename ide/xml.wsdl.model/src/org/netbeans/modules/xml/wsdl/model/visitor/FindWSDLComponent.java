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
 * XMLModelMapperVisitor.java
 *
 * Created on October 28, 2005, 3:17 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.netbeans.modules.xml.wsdl.model.visitor;

import org.netbeans.modules.xml.wsdl.model.WSDLComponent;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

/**
 *
 * @author ajit
 */
public class FindWSDLComponent extends ChildVisitor {
    
    /** Creates a new instance of XMLModelMapperVisitor */
    public FindWSDLComponent() {
    }
    
    public static <T extends WSDLComponent> T findComponent(Class<T> type, WSDLComponent root, String xpath) {
        WSDLComponent ret = new FindWSDLComponent().findComponent(root, xpath);
        if (ret == null) {
            return null;
        } else {
            return type.cast(ret);
        }
    }
    
    public WSDLComponent findComponent(WSDLComponent root, Element xmlNode) {
        assert xmlNode != null;
        
        this.xmlNode = xmlNode;
        result = null;
        root.accept(this);
        return result;
    }
    
    public WSDLComponent findComponent(WSDLComponent root, String xpath) {
        Document doc = (Document) root.getModel().getDocument();
        if (doc == null) {
            return null;
        }
        
        Node result = root.getModel().getAccess().findNode(doc, xpath);
        if (result instanceof Element) {
            return findComponent(root, (Element) result);
        } else {
            return null;
        }
    }

    protected void visitComponent(WSDLComponent component) {
        if (result != null) return;
        if (component.referencesSameNode(xmlNode)) {
            result = component;
            return;
        } else {
            super.visitComponent(component);
        }
    }
    
    private WSDLComponent result;
    private Element xmlNode;
}
