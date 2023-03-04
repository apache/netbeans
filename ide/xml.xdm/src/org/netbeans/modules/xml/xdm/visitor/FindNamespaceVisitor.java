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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.netbeans.modules.xml.xdm.nodes.Attribute;
import org.netbeans.modules.xml.xdm.nodes.Element;
import org.netbeans.modules.xml.xdm.nodes.Node;
import org.netbeans.modules.xml.xdm.nodes.Document;
import org.w3c.dom.NamedNodeMap;

/**
 *
 * @author ajit
 */
public class FindNamespaceVisitor extends ChildVisitor {
    
    /** Creates a new instance of FindNamespaceVisitor */
    public FindNamespaceVisitor(Document root) {
        this.root = root;
    }
    
    public String findNamespace(Node target) {
        if(!(target instanceof Element) && !(target instanceof Attribute)) return null;
        return getNamespaceMap().get(target.getId());
    }
    
    public Map<Integer,String> getNamespaceMap() {
        if(namespaceMap.isEmpty()) {
            nodeCtr = 0;
            visit(root);
        }
        return namespaceMap;
    }
    
    protected void visitNode(Node node) {
        Map<String,String> namespaces = new HashMap<String,String>();
        if((node instanceof Element) || (node instanceof Attribute)) {
            nodeCtr++;
            boolean found = false;
            String prefix = node.getPrefix();
            if(prefix == null) {
                if(node instanceof Attribute) return;
                prefix = "";
            }
            if(node instanceof Element && node.hasAttributes()) {
                NamedNodeMap attrMap = node.getAttributes();
                for (int i=0;i<attrMap.getLength();i++) {
                    Attribute attribute = (Attribute)attrMap.item(i);
                    if(Element.XMLNS.equals(attribute.getPrefix()) || Element.XMLNS.equals(attribute.getName())) {
                        String key = attribute.getPrefix()==null?"":attribute.getLocalName();
                        String value = attribute.getValue();
                        namespaces.put(key,value);
                        if(key.equals(prefix)) {
                            namespaceMap.put(node.getId(),value);
                            found = true;
                        }
                    }
                }
            }
            if(!found) {
                for(Map<String,String> map:ancestorNamespaceMaps) {
                    if(map == null)
                        continue;
                    if(map.containsKey(prefix)) {
                        namespaceMap.put(node.getId(),map.get(prefix));
                        break;
                    }
                }
            }
        }
        if(!namespaces.isEmpty())
            ancestorNamespaceMaps.add(0,namespaces);
        super.visitNode(node);
        ancestorNamespaceMaps.remove(namespaces);
    }

    private Map<Integer,String> namespaceMap = new HashMap<Integer,String>();
    private Document root = null;
    private List<Map<String,String>> ancestorNamespaceMaps = new ArrayList<Map<String,String>>();
    int nodeCtr;
}
