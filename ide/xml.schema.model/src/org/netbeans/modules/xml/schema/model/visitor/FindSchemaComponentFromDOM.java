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

package org.netbeans.modules.xml.schema.model.visitor;

import org.netbeans.modules.xml.schema.model.Schema;
import org.netbeans.modules.xml.schema.model.SchemaComponent;
import org.netbeans.modules.xml.schema.model.impl.SchemaComponentImpl;
import org.netbeans.modules.xml.schema.model.impl.SchemaModelImpl;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.Element;

/**
 *
 * @author ajit
 */
public class FindSchemaComponentFromDOM extends DeepSchemaVisitor {

    /** Creates a new instance of XMLModelMapperVisitor */
    public FindSchemaComponentFromDOM() {
    }
    
    public static <T extends SchemaComponent> T find(Class<T> type, SchemaComponent root, String xpath) {
        SchemaComponent ret = new FindSchemaComponentFromDOM().findComponent(root, xpath);
        return type.cast(ret);
    }
    
    public SchemaComponent findComponent(SchemaComponent root, Element xmlNode) {
        assert root instanceof Schema;
        assert xmlNode != null;
        
        this.xmlNode = xmlNode;
        result = null;
        root.accept(this);
        return result;
    }
    
    public SchemaComponent findComponent(SchemaComponent root, String xpath) {
        Document doc = getDocument(root);
        if (doc == null) {
            return null;
        }
        
        Node result = ((SchemaModelImpl)root.getModel()).getAccess().findNode(doc, xpath);
        if (result instanceof Element) {
            return findComponent(root, (Element) result);
        } else {
            return null;
        }
    }

    private Document getDocument(SchemaComponent root) {
        return (Document)root.getModel().getDocument();
    }

    private Element getElement(SchemaComponent c) {
        return (Element) c.getPeer();
    }
    
    public String getXPathForComponent(SchemaComponent root, SchemaComponent target) {
        Document doc = getDocument(root);
        Element element = getElement(target);
        if (doc == null || element == null) {
            return null;
        }
        return ((SchemaModelImpl)root.getModel()).getAccess().getXPath(doc, element);
    }

    protected void visitChildren(SchemaComponent component) {
        if(result != null) return;
        if (component.referencesSameNode(xmlNode)) {
            result = component;
        } else {
            super.visitChildren(component);
        }
    }
    
    private SchemaComponent result;
    private Element xmlNode;

}
