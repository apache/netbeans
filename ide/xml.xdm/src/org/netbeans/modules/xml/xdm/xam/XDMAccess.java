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

package org.netbeans.modules.xml.xdm.xam;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import javax.swing.event.UndoableEditListener;
import javax.xml.namespace.QName;
import org.netbeans.modules.xml.xam.dom.AbstractDocumentComponent;
import org.netbeans.modules.xml.xam.dom.DocumentModel;
import org.netbeans.modules.xml.xam.dom.DocumentModelAccess;
import org.netbeans.modules.xml.xam.dom.AbstractDocumentModel;
import org.netbeans.modules.xml.xam.dom.DocumentModelAccess2;
import org.netbeans.modules.xml.xam.dom.ElementIdentity;
import org.netbeans.modules.xml.xdm.XDMModel;
import org.netbeans.modules.xml.xdm.diff.NodeInfo;
import org.netbeans.modules.xml.xdm.nodes.Attribute;
import org.netbeans.modules.xml.xdm.nodes.Document;
import org.netbeans.modules.xml.xdm.nodes.Element;
import org.netbeans.modules.xml.xdm.nodes.Node;
import org.netbeans.modules.xml.xdm.nodes.NodeImpl;
import org.netbeans.modules.xml.xdm.nodes.Token;
import org.netbeans.modules.xml.xdm.visitor.EndPositionFinderVisitor;
import org.netbeans.modules.xml.xdm.visitor.NodeByPositionVisitor;
import org.netbeans.modules.xml.xdm.visitor.PathFromRootVisitor;
import org.netbeans.modules.xml.xdm.visitor.PositionFinderVisitor;
import org.netbeans.modules.xml.xdm.visitor.XPathFinder;
import org.w3c.dom.NamedNodeMap;

/**
 *
 * @author nn136682
 */
public class XDMAccess extends DocumentModelAccess2 {
    private final XDMModel xdmModel;
    private final AbstractDocumentModel model;
    private final XDMListener xdmListener;
    
    public XDMAccess(AbstractDocumentModel model) {
        xdmModel = new XDMModel(model.getModelSource());
        xdmModel.setPretty(true);
        this.model = model;
        xdmListener = new XDMListener(this.model);
        xdmModel.setQNameValuedAttributes(model.getQNameValuedAttributes());
    }
    
    @Override
    public org.w3c.dom.Document getDocumentRoot() {
        return getReferenceModel().getCurrentDocument();
    }
    
    @Override
    public void removeUndoableEditListener(UndoableEditListener listener) {
        xdmModel.removeUndoableEditListener(listener);
    }
    
    @Override
    public void addUndoableEditListener(UndoableEditListener listener) {
        xdmModel.addUndoableEditListener(listener);
    }
    
    @Override
    public AbstractDocumentModel getModel() { return model; }
    public XDMModel getReferenceModel() {
        return xdmModel;
    }
    
    @Override
    public void flush() {
        xdmModel.flush();
    }
    
    @Override
    public void prepareForUndoRedo() {
        xdmListener.startSync();
    }
    
    @Override
    public void finishUndoRedo() {
        xdmListener.endSync(true);
    }
    
    @Override
    public void prepareSync() {
        xdmModel.prepareSync();
    }
    
    @Override
    public DocumentModel.State sync() throws IOException {
        if (model.getRootComponent() == null) {
            xdmModel.sync();
            if(xdmModel.getStatus() == XDMModel.Status.STABLE){
                Element root = Element.class.cast(xdmModel.getDocument().getDocumentElement());
                if (root == null) {
                    throw new IOException("Cannot create model from non-XML document");
                }
                if (model.createRootComponent(root) == null) {
                    throw new IOException("Cannot create model with "+
                            new QName(root.getNamespaceURI(), root.getLocalName()));
                }
            }
        } else {
            boolean error = true;
            try {
                xdmListener.startSync();
                synchronized (xdmModel) {
                    Document oldDoc = xdmModel.getCurrentDocument();
                    xdmModel.sync();
                    if (!xdmListener.xamModelHasRoot()) {
                        xdmModel.setDocument(oldDoc);
                        return DocumentModel.State.NOT_WELL_FORMED;
                    }
                }
                error = false;
            } catch(IllegalArgumentException ex) {
                IOException ioe = new IOException();
                ioe.initCause(ex);
                throw ioe;
            } finally {
                xdmListener.endSync(!error);
            }
        }
        
        return xdmModel.getStatus() == XDMModel.Status.STABLE ? DocumentModel.State.VALID : DocumentModel.State.NOT_WELL_FORMED;
    }
    
    @Override
    public boolean areSameNodes(org.w3c.dom.Node node1, org.w3c.dom.Node node2) {
        if (! (node1 instanceof NodeImpl && node2 instanceof NodeImpl)) {
            return false;
        }
        NodeImpl n1 = (NodeImpl) node1;
        NodeImpl n2 = (NodeImpl) node2;
		boolean areSameNodes = n1.isEquivalentNode(n2);
        
        /*
        // keep the original fail-fast version and make additional 
        // comparisons version only for sync usage
		if (! areSameNodes) {
            areSameNodes = compareTokens(n1, n2);
		}*/
        return areSameNodes;
    }
    
    /**
     * @Returns true if both nodes have same list of tokens and attributes.
     * Since children list are not used, only use this comparison in specific
     * context.
     */
    private boolean compareTokens(NodeImpl n1, NodeImpl n2) {
        List<Token> n1Tokens = n1.getTokens();
        List<Token> n2Tokens = n2.getTokens();
        if (n1Tokens.size() != n2Tokens.size()) {
            return false;
        }
         
        for( int i=0;i<n1Tokens.size();i++) {
            if (! n1Tokens.get(i).getValue().equals(n2Tokens.get(i).getValue())) {
                return false;
            }
        }
			
        NamedNodeMap n1Attrs = n1.getAttributes();
        NamedNodeMap n2Attrs = n2.getAttributes();
        if (n1Attrs.getLength() != n2Attrs.getLength()) {
            return false;
        }

        for(int i=0;i<n1Attrs.getLength();i++) {
            List<Token> n1AttrTokens = ((NodeImpl)n1Attrs.item(i)).getTokens();
            List<Token> n2AttrTokens = ((NodeImpl)n2Attrs.item(i)).getTokens();						
            if (n1AttrTokens.size() != n2AttrTokens.size()) {
                return false;
            }
             
            for (int j=0;j<n1AttrTokens.size();j++) {
                if (! n1AttrTokens.get(j).getValue().equals(n2AttrTokens.get(j).getValue())) {
                    return false;
                }
            }
        }
        
        return true;
    }
    
    @Override
    public int getElementIndexOf(org.w3c.dom.Node parent, org.w3c.dom.Element child) {
        if (child == null) return -1;
        int elementIndex = -1;
        for (int i = 0; i < parent.getChildNodes().getLength(); i++) {
            org.w3c.dom.Node n = parent.getChildNodes().item(i);
            if (! (n instanceof Element)) continue;
            elementIndex++;
            if (areSameNodes(n, child)) {
                return elementIndex;
            }
        }
        return -1;
    }
    
    private boolean noMutations() {
        return model.inSync() && ! model.startedFiringEvents() || model.inUndoRedo();
    }
    
    @Override
    public void setAttribute(org.w3c.dom.Element element, String name, String value, NodeUpdater updater) {
        if (noMutations()) return;
        if(element instanceof Node) {
            Element xdmElem = (Element)element;
            if(xdmElem.isInTree()) {
                updater.updateReference(xdmModel.setAttribute(xdmElem,name,value));
            } else {
                xdmElem.setAttribute(name,value);
            }
        } else {
            throw new IllegalArgumentException();
        }
    }
    
    @Override
    public void removeAttribute(org.w3c.dom.Element element, String name, NodeUpdater updater) {
        if (noMutations()) return;
        if(element instanceof Node) {
            Element xdmElem = (Element)element;
            if(xdmElem.isInTree()) {
                updater.updateReference(xdmModel.removeAttribute(xdmElem,name));
            } else {
                xdmElem.removeAttribute(name);
            }
        } else {
            throw new IllegalArgumentException();
        }
    }
    
    @Override
    public void appendChild(org.w3c.dom.Node node, org.w3c.dom.Node newChild, NodeUpdater updater) {
        if (noMutations()) return;
        if(node instanceof Node && newChild instanceof Node) {
            Node xdmNode = (Node)node;
            if (xdmNode.isInTree()) {
                updater.updateReference(xdmModel.append(xdmNode,(Node)newChild));
            } else {
                xdmNode.appendChild(newChild);
            }
        } else {
            throw new IllegalArgumentException();
        }
    }
    
    @Override
    public void insertBefore(org.w3c.dom.Node node, org.w3c.dom.Node newChild, org.w3c.dom.Node refChild, NodeUpdater updater) {
        if (noMutations()) return;
        if (node instanceof Node && newChild instanceof Node && refChild instanceof Node) {
            Node xdmNode = (Node)node;
            if(xdmNode.isInTree()) {
                updater.updateReference(xdmModel.insertBefore(xdmNode,(Node)newChild,(Node)refChild));
            } else {
                xdmNode.insertBefore(newChild,refChild);
            }
        } else {
            throw new IllegalArgumentException();
        }
    }
    
    @Override
    public void removeChild(org.w3c.dom.Node node, org.w3c.dom.Node child, NodeUpdater updater) {
        if (noMutations()) return;
        if(node instanceof Node && child instanceof Node) {
            Node xdmNode = (Node)node;
            if(xdmNode.isInTree()) {
                updater.updateReference(xdmModel.remove(xdmNode,(Node)child));
            } else {
                xdmNode.removeChild(child);
            }
        } else {
            throw new IllegalArgumentException();
        }
    }
    
    @Override
    public void removeChildren(org.w3c.dom.Node node, Collection<org.w3c.dom.Node> children, NodeUpdater updater) {
        if (noMutations()) return;
        if(node instanceof Node) {
            ArrayList<Node> nodes = new ArrayList<Node>();
            for (org.w3c.dom.Node n : children) {
                if (n instanceof Node) {
                    nodes.add((Node)n);
                } else {
                    throw new IllegalArgumentException();
                }
            }
            Node xdmNode = (Node)node;
            if(xdmNode.isInTree()) {
                updater.updateReference(xdmModel.removeChildNodes(xdmNode, nodes));
            } else {
                for (Node child : nodes) {
                    xdmNode.removeChild(child);
                }
            }
        } else {
            throw new IllegalArgumentException();
        }
    }
    
    @Override
    public void replaceChild(org.w3c.dom.Node node, org.w3c.dom.Node child, org.w3c.dom.Node newChild, NodeUpdater updater) {
        if (noMutations()) return;
        Node xdmNode = (Node)node;
        if(xdmNode.isInTree()) {
            updater.updateReference(xdmModel.replaceChild(xdmNode, (Node)child, (Node)newChild));
        } else {
            xdmNode.replaceChild(newChild, child);
        }
    }
    
    /**
     * Replace children content with single text node having string value.
     */
    @Override
    public void setText(org.w3c.dom.Element element, String val, NodeUpdater updater) {
        if (noMutations()) return;
        Element xdmElem = (Element)element;
        if(xdmElem.isInTree()) {
            updater.updateReference(xdmModel.setTextValue(xdmElem,val));
        } else {
            while(xdmElem.hasChildNodes()) {
                xdmElem.removeChild(xdmElem.getLastChild());
            }
            xdmElem.appendChild(xdmModel.getCurrentDocument().createTextNode(val));
        }
    }
    
    @Override
    public String getXmlFragment(org.w3c.dom.Element element) {
        if (element instanceof Element) {
            Element xdmElem = (Element)element;
            return xdmElem.getXmlFragmentText();
        } else {
            throw new IllegalArgumentException();
        }
    }
    
    /**
     * Replace element children with result from parsing of given xml fragment text.
     */
    @Override
    public void setXmlFragment(org.w3c.dom.Element element, String val, NodeUpdater updater) throws IOException {
        if (noMutations()) return;
        Element xdmElem = (Element)element;
        if(xdmElem.isInTree()) {
            updater.updateReference(xdmModel.setXmlFragmentText(xdmElem, val));
        } else {
            xdmElem.setXmlFragmentText(val);
        }
    }
    
    @Override
    public void setPrefix(org.w3c.dom.Element element, String prefix) {
        if (noMutations()) return;
        Element xdmElement = (Element)element;
        if (! xdmElement.isInTree()) {
            xdmElement.setPrefix(prefix);
        }
    }
    
    @Override
    public int findPosition(org.w3c.dom.Node node){
        return (new PositionFinderVisitor()).findPosition(xdmModel.getDocument(), (Node)node);
    }
    
    @Override
    public int findEndPosition(org.w3c.dom.Node node) {
        return (new EndPositionFinderVisitor()).findPosition(xdmModel.getDocument(), (Node)node);
    }
    
    @Override
    public Element getContainingElement(int position){
        try {
            return (new NodeByPositionVisitor(xdmModel.getDocument())).getContainingElement(position);
        } catch(Exception ex) {
            return null;
        }
    }
    
    @Override
    public org.w3c.dom.Element duplicate(org.w3c.dom.Element element){
        return (org.w3c.dom.Element) ((Element)element).copy();
    }
    
    @Override
    public Map<QName,String> getAttributeMap(org.w3c.dom.Element element) {
        Map<QName,String> qValues = new AttributeMap<QName,String>();
        NamedNodeMap attributes = element.getAttributes();
        for (int i=0; i<attributes.getLength(); i++) {
            Attribute attr = (Attribute) attributes.item(i);
            if (attr.isXmlnsAttribute()) {
                continue;
            }
            QName q = AbstractDocumentComponent.getQName(attr);
			((AttributeMap)qValues).addKey(q);
            qValues.put(q, attr.getValue());
        }
        return qValues;
    }
    
    @Override
    public List<org.w3c.dom.Element> getPathFromRoot(org.w3c.dom.Document root, org.w3c.dom.Element node) {
        List<Node> pathToRoot = new PathFromRootVisitor().findPath(root, node);
        if(pathToRoot == null)
            return null;
        List<org.w3c.dom.Element> pathFromRoot = new ArrayList<org.w3c.dom.Element>();
        for (Node n : pathToRoot) {
            if (! (n instanceof Element)) {
                break;
            }
            pathFromRoot.add(0, (Element) n);
        }
        return pathFromRoot;
    }
    
    @Override
    public String getXPath(org.w3c.dom.Document root, org.w3c.dom.Element node) {
        return XPathFinder.getXpath((Document)root, (Node)node);
    }
    
    @Override
    public org.w3c.dom.Node findNode(org.w3c.dom.Document root, String xpath) {
        return new XPathFinder().findNode((Document)root, xpath);
    }
    
    @Override
    public List<org.w3c.dom.Node> findNodes(org.w3c.dom.Document root, String xpath) {
        return XDMListener.toDomNodes(new XPathFinder().findNodes((Document)root, xpath));
    }
    
    public XDMModel getXDMModel() {
        return xdmModel;
    }
    
    @Override
    public ElementIdentity getElementIdentity() {
        return getXDMModel().getElementIdentity();
    }

    @Override
    public void addMergeEventHandler(PropertyChangeListener l) {
        xdmModel.addPropertyChangeListener(l);
    }

    @Override
    public void removeMergeEventHandler(PropertyChangeListener l) {
        xdmModel.removePropertyChangeListener(l);
    }

    @Override
    public org.w3c.dom.Node getOldEventParentNode(PropertyChangeEvent event) {
        NodeInfo oldInfo = (NodeInfo) event.getOldValue();
        return oldInfo!=null?(Node) oldInfo.getParent():null;
    }

    @Override
    public org.w3c.dom.Node getOldEventNode(PropertyChangeEvent event) {
        NodeInfo oldInfo = (NodeInfo) event.getOldValue();
        return oldInfo!=null?(Node) oldInfo.getNode():null;
    }

    @Override
    public org.w3c.dom.Node getNewEventParentNode(PropertyChangeEvent event) {
        NodeInfo newInfo = (NodeInfo) event.getNewValue();
        return newInfo!=null?(Node) newInfo.getParent():null;
    }

    @Override
    public org.w3c.dom.Node getNewEventNode(PropertyChangeEvent event) {
        NodeInfo newInfo = (NodeInfo) event.getNewValue();
        return newInfo!=null?(Node) newInfo.getNode():null;
    }

    public String getIndentation(){
        return xdmModel.getIndentation();
    }
    
    public void setIndentation(String indentation){
        xdmModel.setIndentation(indentation);
    }

	// To fix the attribute order issue when using getAttributeMap().keySet().iterator()

	// To fix the attribute order issue when using getAttributeMap().keySet().iterator()
    public class AttributeMap<K,V> extends HashMap<K,V> {
		List<K> keys = new ArrayList<K>();

        @Override
		public AttributeKeySet<K> keySet() {
			return new AttributeKeySet(keys);
		}		

		private void addKey(K q) {
			keys.add(q);
		}
	}
	
    public class AttributeKeySet<E> extends HashSet<E> {
		List<E> keys = new ArrayList<E>();
		
		public AttributeKeySet(List<E> keys) {
			this.keys = keys;
		}

        @Override
		public boolean isEmpty() {
            return keys.isEmpty();
		}	
        
        @Override
		public boolean contains(Object key) {
            return keys.contains(key);
		}
        
        @Override
		public Iterator iterator() {
             return keys.iterator();
		}	
        
        @Override
		public int size() {
            return keys.size();
		}
	}

    @Override
    public void reorderChildren(org.w3c.dom.Element element, int[] permutation,
                                NodeUpdater updater) {
        if (noMutations()) return;
        Element xdmElem = (Element)element;
        if(xdmElem.isInTree()) {
            updater.updateReference(xdmModel.reorderChildren(xdmElem, permutation));
        } else {
            xdmElem.reorderChildren(permutation);
        }
    }
    
    @Override
    public String getCurrentDocumentText() {
        return xdmModel.getCurrentDocumentText();
    }

    @Override
    public void addQNameValuedAttributes(Map<QName, List<QName>> attributesMap) {
        Map<QName, List<QName>> map = new HashMap<QName, List<QName>>(xdmModel.getQNameValuedAttributes());
        map.putAll(attributesMap);
        xdmModel.setQNameValuedAttributes(map);
    }
}
