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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
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

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import javax.swing.text.BadLocationException;
import javax.xml.namespace.QName;
import org.netbeans.modules.xml.xam.ModelAccess;
import org.w3c.dom.CDATASection;
import org.w3c.dom.Comment;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.ProcessingInstruction;

/**
 * Access to the underlying structure of the model.
 *
 * @author Nam Nguyen
 */

public abstract class DocumentModelAccess extends ModelAccess {
    
    public interface NodeUpdater {
        void updateReference(Element node);
        <T extends Node> void updateReference(List<T> pathToRoot);
    }
    
    public abstract Document getDocumentRoot();
    
    /**
     *  Returns the associated document model.
     *  Subclass should override.
     */
    public AbstractDocumentModel getModel() {
        return null;
    }
    
    public abstract boolean areSameNodes(Node n1, Node n2);
    
    /**
     * @return child element index in the children list of given parent.
     */
    public abstract int getElementIndexOf(Node parent, Element child);
    
    public abstract void setAttribute(Element element, String name, String value, NodeUpdater updater);
    
    public abstract void removeAttribute(Element element, String name, NodeUpdater updater);
    
    public abstract void appendChild(Node node, Node newChild, NodeUpdater updater);
    
    public abstract void insertBefore(Node node, Node newChild, Node refChild, NodeUpdater updater);
    
    public abstract void removeChild(Node node, Node child, NodeUpdater updater);
    
    public void removeChildren(Node node, Collection<Node> children, NodeUpdater updater) {
        throw new UnsupportedOperationException();
    }

    public abstract void replaceChild(Node node, Node child, Node newChild, NodeUpdater updater);
    
    public abstract void setText(Element element, String val, NodeUpdater updater);
    
    public abstract void setPrefix(org.w3c.dom.Element node, String prefix);

    public abstract int findPosition(org.w3c.dom.Node node);
    
    public abstract Element getContainingElement(int position);
    
    public abstract Element duplicate(Element element);

    /**
     * Reorder children list of an element.
     * @param element the parent element with children to be reordered
     * @param newIndexes array indexed by existing indexes, with values representing reordered indexes
     * @param updater the component wrapper of the given element.
     */
    public void reorderChildren(Element element, int[] newIndexes, NodeUpdater updater) {
        throw new UnsupportedOperationException("Not support yet by this DocumentModelAccess");
    }

    public String getXmlFragmentInclusive(Element element) {
        if (getModel() == null) {
            throw new UnsupportedOperationException("Unavailable because access does not support getModel()");
        }
        DocumentComponent component = getModel().findComponent(element);
        if (component == null) {
            throw new IllegalArgumentException("Know nothing about '"+element.getTagName()+"'");
        }
        Node parent = component.getParent() == null ? 
            getModel().getDocument() : ((DocumentComponent)component.getParent()).getPeer();
        
        int end = -1;
        int start = findPosition(element);
        assert start > -1 : "Negative start position";
        Node next = element.getNextSibling();
        try {
            javax.swing.text.Document doc = getModel().getBaseDocument();
            StringBuilder sb = new StringBuilder(doc.getText(0, doc.getLength()));
            if (parent instanceof Document) {
                assert ((Document)parent).getDocumentElement() == element;
                end = sb.lastIndexOf(">") + 1;
            } else if (next == null) { // use parent end tag
                end = sb.indexOf(parent.getNodeName(), start)-2;
            } else if (next instanceof Element) {
                end = findPosition(next);
            } else {
                while (next != null && 
                        ! (next instanceof Element) &&
                        ! (next instanceof CDATASection) &&
                        ! (next instanceof ProcessingInstruction) &&
                        ! (next instanceof Comment)) 
                {
                    next = next.getNextSibling();
                }
                if (next instanceof Element) {
                    end = findPosition(next);
                } else if (next instanceof CDATASection || next instanceof Comment || 
                           next instanceof ProcessingInstruction) 
                {
                    end = sb.indexOf(next.getNodeValue(), start);
                } else {
                    end = sb.indexOf("</"+parent.getNodeName(), start);
                }
            } 

            String result = sb.substring(start, end);
            end = result.lastIndexOf("</"+element.getNodeName());
            if (end < 0) { // self-closing
                end = result.indexOf(">") + 1;
            } else {
                end = result.indexOf(">", end) + 1;
            }
            return result.substring(0, end);
        } catch(BadLocationException ble) {
            assert false : "start="+start+" end="+end;
            return "";
        }
    }
    
    /**
     * @return XML fragment text of the given element content.
     */
    public abstract String getXmlFragment(Element element);
    
    public String getCurrentDocumentText() {
        throw new IllegalArgumentException();
    }
    
    /**
     * Sets the XML fragment text for given element content.
     * The XML fragment will be parsed and the resulting nodes will
     * replace the current children of this documentation element.
     * @param element element to set content to.
     * @param text XML fragment text.
     * @exception IOException if the fragment text is not well-form.
     */
    public abstract void setXmlFragment(Element element, String text, NodeUpdater updater) throws IOException;
    
    /**
     * Returns map of attribute names and string values.
     */
    public abstract Map<QName,String> getAttributeMap(Element element);

    /**
     * Returns path from given element to given root; or null if the node is not in tree.
     */
    public abstract List<Element> getPathFromRoot(Document root, Element node);
    
    /**
     * Returns xpath expression of given element.
     */
    public abstract String getXPath(Document root, Element node);
    
    /**
     * Provide a uniform return value for undefined attribute values. 
     * XDM supports full fidelty so this deviates slightly from the DOM
     * specification in that the return value for an undefined attribute
     * is null instead of "". This method normalizes the return value
     * for an undefined element to null. 
     */
    public String normalizeUndefinedAttributeValue(String value) {
	return value;
    }

    /**
     * Returns node from given xpath expression 
     */
    public abstract Node findNode(Document root, String xpath);
    
    /**
     * Returns nodes from given xpath expression 
     */
    public abstract List<Node> findNodes(Document root, String xpath);
    
    /**
     * Returns element identity helper.
     */
    public abstract ElementIdentity getElementIdentity();
    
    /**
     * Add/remove merge property change listener.
     */
    public abstract void addMergeEventHandler(PropertyChangeListener l);

    public abstract void removeMergeEventHandler(PropertyChangeListener l);
    
    public abstract Node getOldEventNode(PropertyChangeEvent evt);

    public abstract Node getOldEventParentNode(PropertyChangeEvent evt);
    
    public abstract Node getNewEventNode(PropertyChangeEvent evt);

    public abstract Node getNewEventParentNode(PropertyChangeEvent evt);
    
    public String lookupNamespaceURI(Node node, List<? extends Node> pathToRoot) {
        String prefix = node.getPrefix();
        if (prefix == null) prefix = ""; //NOI18N
        String namespace = node.lookupNamespaceURI(prefix);
        if (namespace == null) {
            boolean skipDeeperNodes = true;
            for (Node n : pathToRoot) {
                if (skipDeeperNodes) {
                    // The target node has to be inside of pathToRoot. 
                    // But it can be not a top element of the list. 
                    // It's necessary to skip items until the target node 
                    // isn't found in the list.
                    if (areSameNodes(n, node)) {
                        skipDeeperNodes = false;
                    }
                } else {
                    namespace = n.lookupNamespaceURI(prefix);
                    if (namespace != null) {
                        break;
                    }
                }
            }
        }
        return namespace;
    }
    
    private long dirtyTimeMillis = 0;

    @Override
    public long dirtyIntervalMillis() {
        if (dirtyTimeMillis == 0) return 0;
        return System.currentTimeMillis() - dirtyTimeMillis;
    }
    
    public void setDirty() {
        dirtyTimeMillis = System.currentTimeMillis();
    }
    
    @Override
    public void unsetDirty() {
        dirtyTimeMillis = 0;
    }

    /**
     * A chance for extensible model to register attributes from extension that have
     * QName values.  This will help with refactoring of namespace prefixes that happen
     * during namespace consolidation when a new component is added to model.
     * Note: should be overridden by implementation as necessary.
     */
    public void addQNameValuedAttributes(Map<QName, List<QName>> attributesMap) {
    }
}
