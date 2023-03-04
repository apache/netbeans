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

package org.netbeans.modules.xml.xdm.visitor;

import org.netbeans.modules.xml.xdm.nodes.Attribute;
import org.netbeans.modules.xml.xdm.nodes.Document;
import org.netbeans.modules.xml.xdm.nodes.Element;
import org.netbeans.modules.xml.xdm.nodes.Node;
import org.netbeans.modules.xml.xdm.nodes.Text;
import org.w3c.dom.NodeList;


/**
 *
 * @author Chris Webster
 */
public class PrintVisitor extends ChildVisitor {
    
    public void visit(Attribute attr) {
        System.out.printf("attr: %s\n", attr.getValue());
    }
    
    public void visit(Document doc) {
        super.visit(doc);
    }
    
    public void visit(Element e) {
        System.out.printf("element %s\n", e.getLocalName());
        super.visit(e);
    }
    
    public void visit(Text txt) {
        System.out.printf("text %s\n", txt.getText());
	System.out.printf("node type %s\n", txt.getNodeName());
	System.out.printf("node value %s\n", txt.getNodeValue());
    }
    
}
