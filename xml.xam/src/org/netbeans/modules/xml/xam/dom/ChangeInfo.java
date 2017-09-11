/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
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
            ArrayList<Element> ret = new ArrayList(rootToParent);
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
