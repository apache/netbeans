/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.netbeans.modules.xml.xam.dom;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.w3c.dom.Attr;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

/**
 *
 * @author Nam Nguyen
 */
public class ChangeInfo {
    
        private Node changed;
        private Element parent;
        private DocumentComponent parentComponent;
        private boolean domainElement;
        private boolean added;
        private List<Element> rootToParent;
        /**
         * List of non-domain element nodes beside the changed node.
         * The order is increasing distance from root.
         */
        private List<Node> otherNonDomainElementNodes;
        
        /**
         * Creates change info.
         *
         * @param parent parent node of changed
         * @param changed added/removed domain element or first non-domain change node.
         * @param isDomainElement is the changed node a domain element.
         * @param rootToParent path from root to parent node, inclusively.
         * @param otherNodes list of other nodes that are not domain elements beside the changed nodes.
         */
        public ChangeInfo(Element parent, Node changed, boolean isDomainElement, List<Element> rootToParent, List<Node> otherNodes) {
            this.parent = parent;
            this.changed = changed;
            domainElement = isDomainElement;
            if (! domainElement) {
                otherNonDomainElementNodes = otherNodes;
            }
            this.rootToParent = rootToParent;
        }
        public Element getParent() { return parent; }
        public Node getChangedNode() { return changed; }
        public Element getChangedElement() {
            if (changed instanceof Element) {
                return (Element) changed;
            }
            return null;
        }
        public boolean isDomainElement() { return domainElement; }
        public void setDomainElement(boolean v) { domainElement = v; }
        public void setRootToParentPath(List<Element> path) {
            rootToParent = path;
        }
        public List<Element> getRootToParentPath() {
            return rootToParent;
        }
        public List<Element> getParentToRootPath() {
            ArrayList<Element> ret = new ArrayList<>(rootToParent);
            Collections.reverse(ret);
            return ret;
        }
        public boolean isDomainElementAdded() {
            return domainElement && added;
        }
        public void setAdded(boolean v) {
            added = v;
        }
        public boolean isAdded() {
            return added;
        }
        public void markParentAsChanged() {
            assert parent != null;
            changed = parent;

            assert rootToParent.size() > 1;
            assert parent == rootToParent.get(rootToParent.size()-1);
            rootToParent.remove(rootToParent.size()-1);
            parent = rootToParent.get(rootToParent.size()-1);
        }
        public void setParentComponent(DocumentComponent component) {
            parentComponent = component;
        }
        public DocumentComponent getParentComponent() {
            return parentComponent;
        }
        public List<Node> getOtherNonDomainElementNodes() {
            return otherNonDomainElementNodes;
        }
        public Node getActualChangedNode() {
            if (isDomainElement()) {
                return changed;
            } else {
                if (otherNonDomainElementNodes == null || otherNonDomainElementNodes.isEmpty()) {
                    return changed;
                } else {
                    return otherNonDomainElementNodes.get(otherNonDomainElementNodes.size()-1);
                }
            }
        }
        public void markNonDomainChildAsChanged() {
            assert otherNonDomainElementNodes != null && otherNonDomainElementNodes.size() > 0;
            assert(changed instanceof Element);
            rootToParent.add((Element) changed);
            parent = (Element) changed;
            changed = otherNonDomainElementNodes.remove(0);
            parentComponent = null;
        }
        
        public String toString() {
            String op = added ? "ADD: " : "REMOVE: ";
            if (changed instanceof Element) {
                return op + ((Element)changed).getTagName();
            } else if (changed instanceof Attr) {
                return op + ((Attr)changed).getNodeName()+"="+((Attr)changed).getNodeValue();
            } else {
                return op + changed.getNodeValue();
            }
        }
}
