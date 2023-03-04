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
package org.netbeans.modules.xml.xdm;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import javax.swing.event.UndoableEditEvent;
import javax.swing.event.UndoableEditListener;
import javax.swing.text.BadLocationException;
import javax.swing.undo.CompoundEdit;
import javax.swing.undo.UndoableEdit;
import javax.swing.undo.UndoableEditSupport;
import javax.xml.XMLConstants;
import javax.xml.namespace.QName;
import org.netbeans.api.lexer.Language;
import org.netbeans.api.xml.lexer.XMLTokenId;
import org.netbeans.editor.BaseDocument;
import org.netbeans.modules.xml.xam.ModelSource;
import org.netbeans.modules.xml.xam.dom.ElementIdentity;
import org.netbeans.modules.xml.xdm.diff.DiffFinder;
import org.netbeans.modules.xml.xdm.diff.XDMTreeDiff;
import org.netbeans.modules.xml.xdm.diff.Difference;
import org.netbeans.modules.xml.xdm.diff.Add;
import org.netbeans.modules.xml.xdm.diff.Change;
import org.netbeans.modules.xml.xdm.diff.Delete;
import org.netbeans.modules.xml.xdm.diff.DefaultElementIdentity;
import org.netbeans.modules.xml.xdm.diff.MergeDiff;
import org.netbeans.modules.xml.xdm.diff.NodeIdDiffFinder;
import org.netbeans.modules.xml.xdm.diff.NodeInfo;
import org.netbeans.modules.xml.xdm.diff.SyncPreparation;
import org.netbeans.modules.xml.xdm.nodes.Attribute;
import org.netbeans.modules.xml.xdm.nodes.Document;
import org.netbeans.modules.xml.xdm.nodes.Element;
import org.netbeans.modules.xml.xdm.nodes.Node;
import org.netbeans.modules.xml.xdm.nodes.NodeImpl;
import org.netbeans.modules.xml.xdm.nodes.Text;
import org.netbeans.modules.xml.xdm.nodes.Token;
import org.netbeans.modules.xml.xdm.nodes.XMLSyntaxParser;
import org.netbeans.modules.xml.xdm.visitor.FindVisitor;
import org.netbeans.modules.xml.xdm.visitor.FlushVisitor;
import org.netbeans.modules.xml.xdm.visitor.NamespaceRefactorVisitor;
import org.netbeans.modules.xml.xdm.visitor.PathFromRootVisitor;
import org.netbeans.modules.xml.xdm.visitor.Utils;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.NodeList;

/**
 */
public class XDMModel {
    
    /**
     * @param ms requires an instance of org.netbeans.editor.BaseDocument to be
     * available in the lookup;
     */
    public XDMModel(ModelSource ms) {
        source = ms;
        // assert getSwingDocument() != null; // It can be null, for example if the file is deleted. 
        ues = new UndoableEditSupport(this);
        pcs = new PropertyChangeSupport(this);
        parser = new XMLSyntaxParser();
        setStatus(Status.UNPARSED);
        
        //establish a default element identification mechanism
        //domain models should override this by invoking "setElementIdentity"
        ElementIdentity eID = createElementIdentity();
        setElementIdentity(eID);
    }
    
    public String getIndentation() {
        return currentIndent;
    }
    
    public void setIndentation(String indent) {
        currentIndent = indent;
        indentInitialized = true;
    }
    
    private void setDefaultIndentation() {
        currentIndent = DEFAULT_INDENT;
    }
    
        /*
         * override this method if domain model wants to identify elements
         * using a different mechanism than this default one.
         *
         */
    private ElementIdentity createElementIdentity() {
        //Establish DOM element identities
        ElementIdentity eID = new DefaultElementIdentity();
        //Following values are suitable for Schema and WSDL documents
        //these default values can be reset by eID.reset() call
        eID.addIdentifier( "id" );
        eID.addIdentifier( "name" );
        eID.addIdentifier( "ref" );
        return eID;
    }
    
    /**
     * This api flushes the changes made to the model to the underlying document.
     */
    public synchronized void flush() {
        flushDocument(getDocument());
    }
    
    /**
     * This api syncs the model with the underlying swing document.
     * If its the first time sync is called, swing document is parsed and model
     * is initialized. Otherwise the changes made to swing document are applied
     * to the model using DiffMerger.
     */
    public synchronized void sync() throws IOException {
        if (preparation == null) {
            prepareSync();
        }
        finishSync();
    }
    
    public synchronized void prepareSync() {
        //
        BaseDocument baseDoc = getSwingDocument();
        if (baseDoc == null) {
            // the document can be null, for example, if the file is deleted. 
            IOException ioe = new IOException("Base document not accessible"); // NOI18N
            preparation = new SyncPreparation(ioe);
            return;
        }
        //
        Status oldStat = getStatus();
        try {
            setStatus(Status.PARSING);  // to access in case old broken tree            
            //must set the language for XML lexer to work.
            baseDoc.putProperty(Language.class, XMLTokenId.language());
            Document newDoc = parser.parse(baseDoc);
            Document oldDoc = getCurrentDocument();
            if (oldDoc == null) {
                preparation = new SyncPreparation(newDoc);
            } else {
                newDoc.assignNodeIdRecursively();
                XDMTreeDiff treeDiff = new XDMTreeDiff(eID);
                List<Difference> preparedDiffs = treeDiff.performDiff( this, newDoc );
                preparation = new SyncPreparation(oldDoc, preparedDiffs);
            }
        } catch (BadLocationException ble) {
            preparation = new SyncPreparation(ble);
        } catch (IllegalArgumentException iae) {
            preparation = new SyncPreparation(iae);
        } catch (IOException ioe) {
            preparation = new SyncPreparation(ioe);
        } finally {
            setStatus(oldStat);  // we are not mutating yet, so alway restore
        }
    }
    
    private SyncPreparation preparation = null;
    
    private void finishSync() throws IOException {
        if (preparation == null) {
            return; // unprepared or other thread has stealth the sync
        }
        
        if (preparation.hasErrors()) {
            IOException error = preparation.getError();
            preparation = null;
            setStatus(Status.BROKEN);
            throw error;
        }
        
        Status savedStatus = getStatus();
        setStatus(Status.PARSING);
        Document oldDoc = getCurrentDocument();
        try {
            if (preparation.getNewDocument() != null) {
                Document newDoc = preparation.getNewDocument();
                newDoc.addedToTree(this);
                setDocument(newDoc);
            } else {
                assert preparation.getOldDocument() != null : "Invalid preparation oldDoc is null";
                if (oldDoc != preparation.getOldDocument()) {
                    // other thread has completed the sync before me
                    setStatus(savedStatus);
                    return;
                }
                List<Difference> diffs = preparation.getDifferences();
                if (diffs != null && diffs.size() != 0) {
                    mergeDiff(diffs);
                    //diffs = DiffFinder.filterWhitespace(diffs);
                    fireDiffEvents(diffs);
                    if (getCurrentDocument() != oldDoc) {
                        fireUndoableEditEvent(getCurrentDocument(), oldDoc);
                    }
                }
            }
            setStatus(Status.STABLE);
        } catch (IllegalArgumentException iae) {
            if (getStatus() != Status.STABLE) {
                IOException ioe = new IOException();
                ioe.initCause(iae);
                throw ioe;
            } else {
                // XAM will review the mutation and veto by it wrapped IOException
                if (iae.getCause() instanceof IOException) {
                    setStatus(Status.BROKEN);
                    throw (IOException) iae.getCause();
                } else {
                    throw iae;
                }
            }
        } finally {
            if(getStatus() != Status.STABLE) {
                setStatus(Status.BROKEN);
                setDocument(oldDoc);
            }
            preparation = null;
        }
    }
    
    public void mergeDiff(List<Difference> diffs) {
        setStatus(Status.PARSING);
        new MergeDiff().merge(this, diffs);
        // exception in event firing should not put tree out-of-sync with buffer
        setStatus(Status.STABLE);
    }
    
    private void fireDiffEvents(final List<Difference> deList) {
        for ( Difference de:deList ) {
            NodeInfo.NodeType nodeType = de.getNodeType();
//	    if ( nodeType == NodeInfo.NodeType.WHITE_SPACE ) continue;//skip if WS
            if ( de instanceof Add ) {
                NodeInfo newNodeInfo = ((Add)de).getNewNodeInfo();
                assert newNodeInfo != null;
                pcs.firePropertyChange( PROP_ADDED, null, newNodeInfo );
            } else if ( de instanceof Delete ) {
                NodeInfo OldNodeInfo = ((Delete)de).getOldNodeInfo();
                assert OldNodeInfo != null;
                pcs.firePropertyChange( PROP_DELETED, OldNodeInfo, null );
            } else if ( de instanceof Change ) {
                NodeInfo oldNodeInfo = ((Change)de).getOldNodeInfo();
                assert oldNodeInfo != null;
                
                NodeInfo newNodeInfo = ((Change)de).getNewNodeInfo();
                assert newNodeInfo != null;
                
                //fire delete and add events for position change of element/text
                if ( ((Change)de).isPositionChanged() ) {
                    pcs.firePropertyChange( PROP_DELETED, oldNodeInfo, null );
                    pcs.firePropertyChange( PROP_ADDED, null, newNodeInfo );
                } else if ( de.getNodeType() == NodeInfo.NodeType.TEXT ) { //text change only
                    pcs.firePropertyChange( PROP_MODIFIED, oldNodeInfo, newNodeInfo );
                } else if ( de.getNodeType() == NodeInfo.NodeType.ELEMENT ) {
                    List<Node> path1 = new ArrayList<Node>(oldNodeInfo.getOriginalAncestors());
                    path1.add(0, oldNodeInfo.getNode());
                    List<Node> path2 = new ArrayList<Node>(newNodeInfo.getNewAncestors());
                    path2.add(0, newNodeInfo.getNode());
                    //fire attribute change events
                    List<Change.AttributeDiff> attrChanges = ((Change)de).getAttrChanges();
                    for (Change.AttributeDiff attrDiff:attrChanges) {
                        Node oldAttr = attrDiff.getOldAttribute();
                        Node newAttr = attrDiff.getNewAttribute();
                        if(attrDiff instanceof Change.AttributeAdd) {
                            assert newAttr != null;
                            pcs.firePropertyChange( PROP_ADDED, null,
                                    new NodeInfo(newAttr, -1, path1, path2));
                        } else if(attrDiff instanceof Change.AttributeDelete) {
                            assert oldAttr != null;
                            pcs.firePropertyChange(
                                    PROP_DELETED, new NodeInfo(oldAttr, -1, path1, path2), null );
                        } else if(attrDiff instanceof Change.AttributeChange) {
                            assert oldAttr != null;
                            assert newAttr != null;                            
                            NodeInfo old = new NodeInfo(oldAttr, -1, path1, path2);
                            NodeInfo now = new NodeInfo(newAttr, -1, path1, path2);
                            pcs.firePropertyChange( PROP_MODIFIED, old, now);
                        }
                    }
                }
            }
        }
    }
    
    private interface Updater {
        void update(Node parent, Node oldNode, Node newNode);
    }
    
    private List<Node> getPathToRoot(Node node, Document root) {
        PathFromRootVisitor pathVisitor = new PathFromRootVisitor();
        List<Node> path = pathVisitor.findPath(root, node);
        if (path==null || path.isEmpty()) {
            throw new IllegalArgumentException("old node must be in the tree");
        }
        return path;
    }
    
    private static String classifyMutationType(Node oldNode, Node newNode) {
        if (newNode == null && oldNode == null ||
                newNode != null && oldNode != null) {
            return PROP_MODIFIED;
        } else if (newNode != null) {
            return PROP_ADDED;
        } else {
            return PROP_DELETED;
        }
    }
    
    private enum MutationType { CHILDREN, ATTRIBUTE, BOTH }
    
    private List<Node> mutate(Node parent, Node oldNode, Node newNode, Updater updater) {
        return mutate(parent, oldNode, newNode, updater, null);
    }

    // TODO: Describe result value
    private List<Node> mutate(Node parent, Node oldNode, Node newNode, Updater updater, MutationType type) {
        checkStableOrParsingState();
        if (newNode != null) checkNodeInTree(newNode);
        
        Document currentDocument = getDocument();
        List<Node> ancestors;
        if (parent == null) {
            assert(oldNode != null);
            ancestors = getPathToRoot(oldNode, currentDocument);
            oldNode = ancestors.remove(0);
        } else {
            if (oldNode != null) {
                assert parent.getIndexOfChild(oldNode) > -1;
                ancestors = getPathToRoot(oldNode, currentDocument);
                assert oldNode.getId() == ancestors.get(0).getId();
                oldNode = ancestors.remove(0);
                assert parent.getId() == ancestors.get(0).getId();
            } else {
                ancestors = getPathToRoot(parent, currentDocument);
            }
        }
        
        final Node oldParent = ancestors.remove(0);
        Node newParent;
        if (type == null) {
            if (oldNode instanceof Attribute || newNode instanceof Attribute ||
                    (oldNode == null && newNode == null)) {
                type = MutationType.ATTRIBUTE;
            } else if (ancestors.size() == 1) {
                assert (oldParent instanceof Element);
                //might have to add namespace declaration to root
                type = MutationType.BOTH;
            } else {
                type = MutationType.CHILDREN;
            }
        }
        switch(type) {
            case ATTRIBUTE:
                newParent = (Node)oldParent.clone(true,true,false);
                break;
            case CHILDREN:
                newParent = (Node)oldParent.clone(true,false,true);
                break;
            default:
                newParent = (Node)oldParent.clone(true,true,true);
        }
        
        if (oldNode != null && oldNode.getNodeType() != Node.TEXT_NODE && newNode == null) { // pure remove
            undoPrettyPrint(newParent, oldNode, oldParent);
        }
        updater.update(newParent, oldNode, newNode);
        if (oldNode == null && newNode != null && newNode.getNodeType() != Node.TEXT_NODE ) { // pure add
            doPrettyPrint(newParent, newNode, oldParent);
        }
        
        List<Node> newAncestors = updateAncestors(ancestors, newParent, oldParent);
        if(getStatus() != Status.PARSING && newNode instanceof Element) {
            consolidateNamespaces(newAncestors, newParent, (Element)newNode);
        }
        Document d = (Document) (!newAncestors.isEmpty() ?
            newAncestors.get(newAncestors.size()-1) : newParent);
        d.addedToTree(this);
        setDocument(d);
        ancestors.add(0, oldParent);
        newAncestors.add(0, newParent);
        if(getStatus() != Status.PARSING) { // not merging
            fireUndoableEditEvent(d, currentDocument);
            String mutationType = classifyMutationType(oldNode, newNode);
            //TODO seems missing delete/change; also, who are listening to these xdm mutation events
            NodeInfo newNodeInfo = new NodeInfo( newNode, -1, ancestors, newAncestors );
            pcs.firePropertyChange(mutationType, null, newNodeInfo);
        }
        return newAncestors;
    }
    
    private void consolidateNamespaces(List<Node> ancestors, Node parent, Element newNode) {
        if (parent instanceof Document) return; // no actions if newNode is root itself
        assert ancestors.size() > 0;
        Element root = (Element) (ancestors.size() == 1 ?
            parent : ancestors.get(ancestors.size()-2));
        List<Node> parentAndAncestors = new ArrayList<>(ancestors);
        parentAndAncestors.add(0, parent);
        consolidateAttributePrefix(parentAndAncestors, newNode);
        NamedNodeMap nnm = newNode.getAttributes();
        for (int i=0; i<nnm.getLength(); i++) {
            if (nnm.item(i) instanceof Attribute) {
                Attribute attr = (Attribute) nnm.item(i);
                consolidateNamespace(root, parentAndAncestors, newNode, attr);
            }
        }
        
        // use parent node prefix
        String parentPrefix = parent.getPrefix();
        String parentNS = NodeImpl.lookupNamespace(parent, ancestors);
        if (parentNS != null && ! parentNS.equals(XMLConstants.NULL_NS_URI)) {
            new NamespaceRefactorVisitor(this).refactor(
                        newNode, parentNS, parentPrefix, parentAndAncestors);
        }
    }
    
    private void consolidateAttributePrefix(List<Node> parentAndAncestors, Element newNode) {
        NamedNodeMap nnm = newNode.getAttributes();
        for (int i=0; i<nnm.getLength(); i++) {
            if (! (nnm.item(i) instanceof Attribute))  continue;
            Attribute attr = (Attribute) nnm.item(i);
            String prefix = attr.getPrefix();
            if (prefix != null && ! attr.isXmlnsAttribute()) {
                String namespace = newNode.lookupNamespaceURI(prefix);
                if (namespace == null) continue;
                prefix = NodeImpl.lookupPrefix(namespace, parentAndAncestors);
                  if (prefix != null) {
                    attr.setPrefix(prefix);
                }
            }
        }
    }
    
    /**
     * Consolidate new node top-leveled namespaces with parent's.
     * Note: this assume #consolidateAttributePrefix has been called
     */
    private void consolidateNamespace(Element root, List<Node> parentAndAncestors,
            Element newNode, Attribute attr) {
        if (attr.isXmlnsAttribute()) {
            String prefix = attr.getLocalName();
            if (XMLConstants.XMLNS_ATTRIBUTE.equals(prefix)) {
                prefix = XMLConstants.DEFAULT_NS_PREFIX;
            }
            String namespace = attr.getValue();
            assert (namespace != null);
            
            Node parent = parentAndAncestors.get(0);
            String parentNS = NodeImpl.lookupNamespace(parent, parentAndAncestors);
            
            String existingNS = NodeImpl.lookupNamespace(prefix, parentAndAncestors);
            String existingPrefix = NodeImpl.lookupPrefix(namespace, parentAndAncestors);
            
            // 1. prefix is free (existingNS == null) and namespace is never declared (existingPrefix == null)
            // 2. prefix is used and for the same namespace
            // 3. namespace is declared by different prefix
            // 4. prefix is used and for different namespace
            
            if (existingNS == null && existingPrefix == null) { // case 1.
                newNode.removeAttributeNode(attr);
                root.appendAttribute(attr);
            } else if (namespace.equals(existingNS) && prefix.equals(existingPrefix)) { // case 2
                assert prefix.equals(existingPrefix) : "prefix='"+prefix+"' existingPrefix='"+existingPrefix+"'";
                newNode.removeAttributeNode(attr);
            } else if (existingPrefix != null) { // case 3.
                // skip attribute redeclaring namespace of parent element
                // we will refactor to parent prefix after processing all xmlns-attributes
                if (! namespace.equals(parentNS)) {
                    new NamespaceRefactorVisitor(this).refactor(
                        newNode, namespace, existingPrefix, parentAndAncestors);
                }
            } else { // existingNS != null && existingPrefix == null
                // case 4 just leave prefix as overriding with different namespace
            }
        }
    }
    
    /**
     * This api replaces given old node with given new node.
     * The old node passed must be in tree, the new node must not be in tree,
     * and new node must be clone of old node.
     * @param oldValue The old node to be replaced.
     * @param newValue The new node.
     * @return The new parent
     */
    public synchronized List<Node> modify(Node oldValue, Node newValue) {
        if (oldValue.getId() != newValue.getId()) {
            throw new IllegalArgumentException("newValue must be a clone of oldValue");
        }
        
        if (oldValue instanceof Document) {
            assert newValue instanceof Document;
            Document oldDoc = (Document) oldValue;
            Document newDoc = (Document) newValue;
            newDoc.addedToTree(this);
            setDocument(newDoc);
            if (getStatus() != Status.PARSING) { // not merging
                fireUndoableEditEvent(oldDoc, currentDocument);
                String mutationType = classifyMutationType(oldDoc, newDoc);
                ArrayList<Node> ancestors = new ArrayList<Node>();
                NodeInfo oldNodeInfo = new NodeInfo( oldDoc, -1, ancestors, ancestors );
                NodeInfo newNodeInfo = new NodeInfo( newDoc, -1, ancestors, ancestors );
                pcs.firePropertyChange(mutationType, oldNodeInfo, newNodeInfo);
            }
            return new ArrayList<Node>();
        }
        
        Updater modifier = new Updater() {
            public void update(Node newParent, Node oldNode, Node newNode) {
                if (oldNode instanceof Attribute) {
                    ((Element)newParent).replaceAttribute((Attribute)newNode,(Attribute)oldNode);
                } else {
                    newParent.replaceChild(newNode, oldNode);
                }
            }
        };
        return mutate(null, oldValue, newValue, modifier);
    }
    
    /**
     * This api adds given node to given parent at given index.
     * The added node will be part of childnodes of the parent,
     * and its index will be the given index. If the given index
     * is out of the parents childnodes range, the node will be
     * appended.
     * @param parent The parent node to which the node is to be added.
     * @param node The node which is to be added.
     * @param offset The index at which the node is to be added.
     * @return The parent node resulted by addition of this node
     */
    public synchronized List<Node> add(Node parent, Node node, final int offset) {
        if (offset<0) throw new IndexOutOfBoundsException();
        Updater adder = new Updater() {
            public void update(Node newParent, Node oldNode, Node newNode) {
                if (newParent instanceof Element && newNode instanceof Attribute) {
                    Element newElement = (Element)newParent;
                    if (offset>newElement.getAttributes().getLength())
                        throw new IndexOutOfBoundsException();
                    newElement.addAttribute((Attribute)newNode,offset);
                } else {
                    if (offset>newParent.getChildNodes().getLength())
                        throw new IndexOutOfBoundsException();
                    if(offset<newParent.getChildNodes().getLength()) {
                        Node refChild = (Node)newParent.getChildNodes().item(offset);
                        newParent.insertBefore(newNode,refChild);
                    } else {
                        newParent.appendChild(newNode);
                    }
                }
            }
        };
        return mutate(parent, null, node, adder);
    }
    
    /**
     * This api adds given node to given parent before given ref node.
     * The inserted node will be part of childnodes of the parent,
     * and will appear before ref node.
     * @param parent The parent node to which the node is to be added.
     * @param node The node which is to be added.
     * @param refChild The ref node (child) of parent node,
     *                  before which the node is to be added.
     * @return The parent node resulted by inserion of this node.
     */
    public synchronized List<Node> insertBefore(Node parent, Node node, Node refChild) {
        final Node ref = refChild;
        Updater updater = new Updater() {
            public void update(Node newParent, Node oldNode, Node newNode) {
                newParent.insertBefore(newNode, ref);
            }
        };
        
        return mutate(parent, null, node, updater);
    }
    
    /**
     * This api adds given node to given parent at the end.
     * The added node will be part of childnodes of the parent,
     * and it will be the last node.
     * @param parent The parent node to which the node is to be appended.
     * @param node The node which is to be appended.
     * @return The parent node resulted by addition of this node
     */
    public synchronized List<Node> append(Node parent, Node node) {
        Updater appender = new Updater() {
            public void update(Node parent, Node oldNode, Node newNode) {
                parent.appendChild(newNode);
            }
        };
        return mutate(parent, null, node, appender);
    }
    
    /**
     * This api deletes given node from a tree.
     * @param node The node  to be deleted.
     * @return The parent node resulted by deletion of this node.
     */
    public synchronized List<Node> delete(Node n) {
        Updater remover = new Updater() {
            public void update(Node newParent, Node oldNode, Node newNode) {
                newParent.removeChild(oldNode);
            }
        };
        return mutate(null, n, null, remover);
    }
    
    /**
     * This api changes index of the given node.
     * @param nodes The nodes to be moved.
     * @param indexes the new indexes of the nodes.
     * @return The parent node resulted by deletion of this node.
     */
    public synchronized List<Node> reorder(Node parent, Node n, final int index) {
        if (index < 0) throw new IndexOutOfBoundsException("index="+index);
        Updater u = new Updater() {
            public void update(Node newParent, Node oldNode, Node newNode) {
                if (newParent instanceof Element && newNode instanceof Attribute) {
                    Element parent = (Element) newParent;
                    int i = index;
                    if (index > parent.getAttributes().getLength()) {
                        i = parent.getAttributes().getLength();
                    }
                    parent.reorderAttribute((Attribute) oldNode, i);
                } else {
                    int i = index;
                    if (index > newParent.getChildNodes().getLength()) {
                        i = newParent.getChildNodes().getLength();
                    }
                    ((NodeImpl)newParent).reorderChild(oldNode, i);
                }
            }
        };
        return mutate(parent, n, null, u);
    }
    
    /**
     * This api changes indexes of the given node children.
     * @param nodes The nodes to be moved.
     * @param indexes the new indexes of the nodes.
     * @return The parent node resulted by deletion of this node.
     */
    public synchronized List<Node> reorderChildren(Node parent, final int[] permutation) {
        Updater u = new Updater() {
            public void update(Node newParent, Node oldNode, Node newNode) {
                ((NodeImpl)newParent).reorderChildren(permutation);
            }
        };
        return mutate(parent, null, null, u, MutationType.CHILDREN);
    }
    
    /**
     * This api deletes given node from a given parent node.
     * @param parent The parent node from which the node is to be deleted.
     * @param child The node  to be deleted.
     * @return The parent node resulted by deletion of this node.
     */
    public synchronized List<Node> remove(final Node parent, Node child) {
        Updater remover = new Updater() {
            public void update(Node newParent, Node oldNode, Node newNode) {
                assert parent.isEquivalentNode(newParent);
                newParent.removeChild(oldNode);
            }
        };
        return mutate(parent, child, null, remover);
    }
    
    /**
     * This api deletes given node from a given parent node.
     * @param parent The parent node from which the node is to be deleted.
     * @param toRemove collection of node to be deleted.
     * @return The parent node resulted by deletion of this node.
     */
    public synchronized List<Node> removeChildNodes(final Node parent, final Collection<Node> toRemove) {
        Updater remover = new Updater() {
            public void update(Node newParent, Node oldNode, Node newNode) {
                assert parent.isEquivalentNode(newParent);
                for (Node n : toRemove) {
                    newParent.removeChild(n);
                }
            }
        };
        return mutate(parent, null, null, remover, MutationType.CHILDREN);
    }
    
    public synchronized List<Node> replaceChild(final Node parent, Node child, Node newChild) {
        Updater updater = new Updater() {
            public void update(Node newParent, Node oldNode, Node newNode) {
                assert newParent.isEquivalentNode(parent);
                newParent.replaceChild(newNode, oldNode);
            }
        };
        return mutate(null, child, newChild, updater);
    }
    
    /**
     * This api sets an attribute given name and value of a given element node.
     * If an attribute with given name already present in element, it will only
     * set the value. Otherwise a new attribute node, with given name and value,
     * will be appended to the attibute list of the element node.
     * @param element The element of which the attribute to be set.
     * @param name The name of the attribute to be set.
     * @param value The value of the attribute to be set.
     * @return The element resulted by setting of attribute.
     */
    public synchronized List<Node> setAttribute(Element element, final String name, final String value) {
        Updater updater = new Updater() {
            public void update(Node newParent, Node oldNode, Node newNode) {
                ((Element)newParent).setAttribute(name,value);
            }
        };
        return mutate(element, null, null, updater);
    }
    
    /**
     * This api removes an attribute given name and value of a given element node.
     * @param element The element of which the attribute to be removed.
     * @param name The name of the attribute to be removed.
     * @return The element resulted by removed of attribute.
     */
    public synchronized List<Node> removeAttribute(Element element, final String name) {
        Updater updater = new Updater() {
            public void update(Node newParent, Node oldNode, Node newNode) {
                ((Element)newParent).removeAttribute(name);
            }
        };
        return mutate(element, null, null, updater);
    }
    
    private interface CheckIOExceptionUpdater extends Updater {
        public IOException getError();
    }
    
    public synchronized List<Node> setXmlFragmentText(Element node, final String value) throws IOException {
        CheckIOExceptionUpdater updater = new CheckIOExceptionUpdater() {
            public void update(Node newParent, Node oldNode, Node newNode) {
                try {
                    ((Element)newParent).setXmlFragmentText(value);
                } catch(IOException ioe) {
                    error = ioe;
                }
            }
            public IOException getError() {
                return error;
            }
            private IOException error;
        };
        List<Node> retPath = mutate(node, null, null, updater, MutationType.CHILDREN);
        if (updater.getError() != null) {
            throw updater.getError();
        } else {
            return retPath;
        }
    }
    
    public synchronized List<Node> setTextValue(Node node, String value) {
        Node text = (Node) currentDocument.createTextNode(value);
        Updater updater = new Updater() {
            public void update(Node newParent, Node oldNode, Node newNode) {
                while(newParent.hasChildNodes()) {
                    newParent.removeChild(newParent.getLastChild());
                }
                newParent.appendChild(newNode);
            }
        };
        return mutate(node, null, text, updater);
    }
    
    /**
     * This is utility method which updates all the ancestors in the given
     * ancestor list of given originalNode. The list returned represents
     * the ancestors of given modified node.
     * @param ancestors the list of ancestors starting from parent
     * @param modifiedNode The modified node for which the new list is to be created
     * @param originalNode The original node which ancestors are given
     * @return The list of new ancestors starting parent for the modified node
     */
    private List<Node> updateAncestors(List<Node> ancestors, Node modifiedNode, Node originalNode) {
        assert ancestors != null && modifiedNode != null && originalNode != null;
        List<Node> newAncestors = new ArrayList<Node>(ancestors.size());
        Node currentModifiedNode = modifiedNode;
        Node currentOrigNode = originalNode;
        for(Node parentNode: ancestors) {
            Node newParentNode = (Node)parentNode.clone(false,true,true);
            newParentNode.replaceChild(currentModifiedNode, currentOrigNode);
            newAncestors.add(newParentNode);
            currentOrigNode = parentNode;
            currentModifiedNode = newParentNode;
        }
        return newAncestors;
    }
    
    /**
     * This api returns the latest stable document in the model.
     * An IllegalStateException can be thrown in case the model
     * isn't in a STABLE or PARSING state.
     * @return The latest stable document in the model.
     */
    public synchronized Document getDocument() {
        checkStableOrParsingState();
        return currentDocument;
    }
    
    /**
     * This api returns the current document in the model, regardless of the state.
     * @return The latest stable document in the model.
     */
    public synchronized Document getCurrentDocument() {
        return currentDocument;
    }
    
    /**
     * Reset document to provided known version and cause events to be fired.
     * Note caller are responsible to handle exception and decide which version
     * to keep after exception and do proper cleanup.
     */
    synchronized void resetDocument(Document newDoc) {
        try {
            fireUndoEvents = false;
            List<Difference> diffs = new NodeIdDiffFinder().findDiff(getCurrentDocument(), newDoc);
            List<Difference> filtered = DiffFinder.filterWhitespace(diffs);
            //flushDocument(newDoc);
            setDocument(newDoc);
            setStatus(Status.STABLE);
            if ( filtered != null && !filtered.isEmpty() ) {
                fireDiffEvents(filtered);
            }
        } finally {
            fireUndoEvents = true;
        }
    }
    
    private void flushDocument(Document newDoc) {
        checkStableState();
	UndoableEditListener uel = null;
	BaseDocument d = getSwingDocument();
        if (d == null) {
            return; // Destination document doesn't exist. For example, because the file is deleted. 
        }
	final CompoundEdit ce = new CompoundEdit();
        try {
            FlushVisitor flushvisitor = new FlushVisitor();
            String newXMLText = flushvisitor.flushModel(newDoc);
	    uel = new UndoableEditListener() {
		public void undoableEditHappened(UndoableEditEvent e) {
		    ce.addEdit(e.getEdit());
		}
	    };
	    d.addUndoableEditListener(uel);
            Utils.replaceDocument(d, newXMLText);
        } catch (BadLocationException ble) {
            throw new IllegalStateException("It is possible that model source file is locked", ble);
        } finally {
	    if (uel != null) {
		d.removeUndoableEditListener(uel);
	    }
	    ce.end();
	    for (UndoableEditListener l : ues.getUndoableEditListeners()) {
		l.undoableEditHappened(new UndoableEditEvent(this,ce));
	    }
	}
    }
    
    public synchronized String getCurrentDocumentText() {
        return new FlushVisitor().flushModel(getCurrentDocument());
    }
    
    private BaseDocument getSwingDocument() {
        BaseDocument bd = (BaseDocument)
        source.getLookup().lookup(BaseDocument.class);
        return bd;
    }
    
    public synchronized void setDocument(Document newDoc) {
        currentDocument = newDoc;
    }
    
    /**
     * This returns the statuc of the model.
     * @return the status.
     * @see #Status
     */
    public synchronized Status getStatus() {
        return status;
    }
    
    /**
     * This api adds an undoable edit listener.
     * @param l The undoable edit listener to be added.
     */
    public synchronized void addUndoableEditListener(UndoableEditListener l) {
        ues.addUndoableEditListener(l);
    }
    
    /**
     * This api removes an undoable edit listener.
     * @param l The undoable edit listener to be removed.
     */
    public synchronized void removeUndoableEditListener(UndoableEditListener l) {
        ues.addUndoableEditListener(l);
    }
    
    /**
     * This api adds a property change listener.
     * @param pcl The property change listener to be added.
     */
    public synchronized void addPropertyChangeListener(PropertyChangeListener pcl) {
        pcs.addPropertyChangeListener(pcl);
    }
    
    /**
     * This api removes a property change listener.
     * @param pcl The property change listener to be removed.
     */
    public synchronized void removePropertyChangeListener(PropertyChangeListener pcl) {
        pcs.removePropertyChangeListener(pcl);
    }
    
    /**
     * Find the node with same id in the current tree.
     */
    private synchronized Node findNode(int id) {
        FindVisitor fv = new FindVisitor();
        return fv.find(getDocument(), id);
    }
    
    /**
     * This represents the status of the XDM Model.
     * Status STABLE means the latest attempt to parse was successful
     * Status BROKEN means that the latest attempt to parse was unsuccessful.
     * Status UNPARSED means the document has not been parsed yet.
     * Status PARSING means the document is being parsed.
     */
    //TODO Last Parsed status
    public enum Status {BROKEN, STABLE, UNPARSED, PARSING;}
    
    private void fireUndoableEditEvent(Document newDoc, Document oldDoc) {
        if (fireUndoEvents) {
            assert newDoc != oldDoc;
            UndoableEdit ee = new XDMModelUndoableEdit(oldDoc, newDoc, this);
            UndoableEditEvent ue = new UndoableEditEvent(this, ee);
            for (UndoableEditListener l:ues.getUndoableEditListeners()) {
                l.undoableEditHappened(ue);
            }
        }
    }
    
    private void checkNodeInTree(Node n) {
        if (n.isInTree()) {
            throw new IllegalArgumentException("newValue must not have been added to model"); // NOI18N
        }
    }
    
    private void checkStableState() {
        if (getStatus() != Status.STABLE ) {
            throw new IllegalStateException("flush can only be called from STABLE STATE"); //NOI18N
        }
    }
    
    private void checkStableOrParsingState() {
        if (getStatus() != Status.STABLE && getStatus() != Status.PARSING) {
            throw new IllegalStateException("The model is not initialized or is broken."); //NOI18N
        }
    }
    
    private void setStatus(Status s) {
        status = s;
    }
    
    /**
     * This api keeps track of the nodes created in this model.
     * @return the id of the next node to be created.
     */
    public int getNextNodeId() {
        int nodeId = nodeCount;
        nodeCount++;
        return nodeId;
    }
    
    private boolean isPretty() {
        return pretty;
    }
    
    public void setPretty(boolean print) {
        pretty = print;
    }
    
    private void doPrettyPrint(Node newParent, Node newNode, Node oldParent) {
        if ((getStatus() != Status.PARSING) && isPretty()) {
            if(isSimpleContent(newParent)) {//skip if simpleContent
                /*
                 * <test name="test1">A new text node</test>
                 */
                return;
            }
            if(!indentInitialized)
                initializeIndent(oldParent);
            String parentIndent = calculateNodeIndent(oldParent);
            if(!isPretty(newParent, newNode)) {//skip if already pretty
                int offset = 1;
                if(oldParent.getChildNodes().getLength() == 0) {//old parent did not have prettyprint before
                    /*
                     * before
                     *
                     * <test name="test1"></test>
                     *
                     * after
                     *
                     * <test name="test1">
                     *     <c name="c1">
                     * </test>
                     */
                    newParent.insertBefore(createPrettyText(
                            parentIndent+getIndentation()), newNode);
                    offset++;
                }
                int index = ((NodeImpl)newParent).getIndexOfChild(newNode);
                if(index > 0) {
                    Node oldText = (Node)newParent.getChildNodes().item(index-1);
                    if(checkPrettyText(oldText)) {
                        /*
                         * before
                         *
                         * <test name="test1">
                         *     <a name="a1">
                         *     <b name="b1">
                         * <c name="c1">
                         * </test>
                         *
                         * after
                         *
                         * <test name="test1">
                         *     <a name="a1">
                         *     <b name="b1">
                         *     <c name="c1">
                         * </test>
                         */
                        Text newText = createPrettyText(
                                parentIndent+getIndentation());
                        newParent.replaceChild(newText, oldText);
                    } else {
                        /*
                         * before
                         *
                         * <test name="test1">
                         *     <a name="a1">
                         *     <b name="b1"><c name="c1">
                         * </test>
                         *
                         * after
                         *
                         * <test name="test1">
                         *     <a name="a1">
                         *     <b name="b1">
                         *     <c name="c1">
                         * </test>
                         */
                        newParent.insertBefore(createPrettyText(
                                parentIndent+getIndentation()), newNode);
                        offset++;
                    }
                }
                Node ref = null;
                if((index+offset) < newParent.getChildNodes().getLength())
                    ref = (Node)newParent.getChildNodes().item((index+offset));
                if(ref != null) {
                    if(!checkPrettyText(ref)) {
                        /*
                         * before
                         *
                         * <test name="test1">
                         *     <a name="a1">
                         *     <b name="b1">
                         *     <c name="c1"><d name="d1">
                         * </test>
                         *
                         * after
                         *
                         * <test name="test1">
                         *     <a name="a1">
                         *     <b name="b1">
                         *     <c name="c1">
                         *     <d name="d1">
                         * </test>
                         */
                        newParent.insertBefore(createPrettyText(
                                parentIndent+getIndentation()), ref);
                    }
                } else {
                    /*
                     * before
                     *
                     * <test name="test1">
                     *     <a name="a1">
                     *     <b name="b1">
                     *     <c name="c1"></test>
                     *
                     * after
                     *
                     * <test name="test1">
                     *     <a name="a1">
                     *     <b name="b1">
                     *     <c name="c1">
                     * </test>
                     */
                    newParent.appendChild(createPrettyText(parentIndent));
                }
            }
            
            //recurse pretty print
            doPrettyPrintRecursive(newNode, parentIndent, newParent);//for children of node
        }
    }
    
    /*
     * initialized only once
     *
     */
    private void initializeIndent(final Node n) {
        String parentIndent = calculateNodeIndent(n);
        List<Node> pathToRoot = new PathFromRootVisitor().findPath(getDocument(), n);
        if(parentIndent.length() > 0 && pathToRoot.size()-2 > 0) {
            //exclude Document and the root from path for indent step calculation
            double step = Math.floor(parentIndent.length() / (double) (pathToRoot.size()-2));
            StringBuffer sb = new StringBuffer();
            for(int i=0;i<step;i++)
                sb.append(" ");
            String indentString = sb.toString();
            if(indentString.length() > 0)
                setIndentation(indentString);
            else
                setDefaultIndentation();
        } else
            setDefaultIndentation();
    }
    
    private String calculateNodeIndent(final Node n) {
        String indent = "";
        Node parent = (Node) n.getParentNode();
        if(parent != null) {
            int index = parent.getIndexOfChild(n);
            if(index > 0) {
                Node txt = (Node) parent.getChildNodes().item(index-1);
                if(checkPrettyText(txt)) {
                    String wsValue = ((NodeImpl)txt).getTokens().get(0).getValue();
                    int ndx = wsValue.lastIndexOf("\n");
                    if(ndx != -1 && (ndx+1) < wsValue.length())
                        indent = wsValue.substring(ndx+1);
                }
            }
        }
        return indent;
    }
    
    private void doPrettyPrintRecursive(Node n, String indent, Node parent) {
        if ((getStatus() != Status.PARSING) && isPretty()) {
            if(isSimpleContent(n))
                return; //skip if simpleContent
            else if(n instanceof Element && isPretty(n)) {//adjust for pretty length difference
                fixPrettyForCopiedNode(n, indent, parent);
            } else {
                List<Node> childList = new ArrayList<Node>();
                List<Node> visitList = new ArrayList<Node>();
                NodeList childs = n.getChildNodes();
                for(int i=0;i<childs.getLength();i++) {
                    childList.add((Node)childs.item(i));
                    if(childs.item(i) instanceof Element)
                        visitList.add((Node)childs.item(i));
                }
                String parentIndent = indent+getIndentation();
                if(childList.size() > 0)
                    n.appendChild(createPrettyText(parentIndent));
                String childIndent = parentIndent+getIndentation();
                for(int i=childList.size()-1;i>=0;i--) {
                    Node ref = (Node)childList.get(i);
                    Text postText = createPrettyText(childIndent);
                    n.insertBefore(postText, ref);
                }
                childList.clear(); //no need to keep it beyond here
                for(int i=0;i<visitList.size();i++) {
                    doPrettyPrintRecursive((Node)visitList.get((i)), parentIndent, n);
                }
                visitList.clear(); //no need to keep it beyond here
            }
        }
    }

    /*
     * This function will fix the pretty text of nodes that are cut or copied and
     * pasted to xdm tree
     */
    private void fixPrettyForCopiedNode(Node n, String indent, Node parent) {
        NodeList childs = n.getChildNodes();
        if(childs.getLength() == 0)
            return;
        Text nlastChild = (Text)childs.item(childs.getLength()-1);
        String lc = ((NodeImpl)nlastChild).getTokens().get(0).getValue();
        NodeImpl pfirstChild = (NodeImpl)parent.getChildNodes().item(0);
        String fc = pfirstChild.getTokens().get(0).getValue();
        
        if(fc.length() == lc.length()) {//return if already pretty
            return;
        } else {
            String parentIndent = indent+getIndentation();
            String childIndent = parentIndent+getIndentation();
            List<Node> childList = new ArrayList<Node>();
            for(int i=0;i<childs.getLength();i++) {
                childList.add((Node)childs.item(i));
            }                    
            for(int i=0;i<childList.size();i++) {
                Node txt = (Node)n.getChildNodes().item(i);
                if(checkPrettyText(txt)) {
                    String newIndent = childIndent+getIndentation();
                    if(i==0) {
                        newIndent = childIndent; 
                    } else if(i == childList.size()-1) {
                        newIndent = parentIndent;
                    }
                    n.replaceChild(createPrettyText(newIndent), txt);
                }
            }
            for(int i=0;i<childList.size();i++) {
                fixPrettyForCopiedNode((Node)n.getChildNodes().item(i), 
                        childIndent, n);
            }
            childList.clear(); //no need to keep it beyond here
        }        
    }
    
    private Text createPrettyText(String indent) {
        String textChars = "\n"+indent;
        Text txt = (Text)this.getDocument().createTextNode(textChars);
        return txt;
    }
    
    private void undoPrettyPrint(Node newParent, Node oldNode, Node oldParent) {
        if ((getStatus() != Status.PARSING) && isPretty()) {
            String parentIndent = calculateNodeIndent(oldParent);
            int piLength = parentIndent != null ? parentIndent.length() : 0;
            int index = ((NodeImpl)oldParent).getIndexOfChild(oldNode);
            Node txtBefore = null;
            if(index > 0) {//remove pre pretty print node
                txtBefore = (Node)oldParent.getChildNodes().item(index-1);
                if(checkPrettyText(txtBefore) &&
                        piLength <= getLength((Text) txtBefore)) {
                                        /*
                                         * before
                                         *
                                         * <test name="test1">
                                         *     <a name="a1">
                                         *     <b name="b1">
                                         *     <c name="c1">
                                         * </test>
                                         *
                                         * after
                                         *
                                         * <test name="test1">
                                         *     <a name="a1">
                                         *     <b name="b1"><c name="c1">
                                         * </test>
                                         */
                    newParent.removeChild(txtBefore);
                }
            }
            if(newParent.getChildNodes().getLength() == 2 &&
                    index+1 < oldParent.getChildNodes().getLength()) {//remove post pretty print node
                Node txtAfter = (Node)oldParent.getChildNodes().item(index+1);
                if(checkPrettyText(txtAfter) &&
                        piLength <= getLength((Text) txtAfter)) {
                                        /*
                                         * before
                                         *
                                         * <test name="test1">
                                         *     <a name="a1">
                                         *     <b name="b1"><c name="c1">
                                         * </test>
                                         *
                                         * after
                                         *
                                         * <test name="test1">
                                         *     <a name="a1">
                                         *     <b name="b1"><c name="c1"></test>
                                         */
                    newParent.removeChild(txtAfter);
                }
            }
        }
    }
    
    private int getLength(Text n) {
        int len = 0;
        for(Token token:((NodeImpl)n).getTokens())
            len += token.getValue().length();
        return len;
    }
    
    private boolean checkPrettyText(Node txt) {
        if (txt instanceof Text) {
            if ((((NodeImpl)txt).getTokens().size() == 1) &&
                    isWhitespaceOnly(((NodeImpl)txt).getTokens().get(0).getValue())) {
                return true;
            }
        }
        return false;
    }
    
    private boolean isPossibleWhiteSpace(String text) {
        return text.length() > 0 &&
                Character.isWhitespace(text.charAt(0)) &&
                Character.isWhitespace(text.charAt(text.length()-1));
    }
    
    private boolean isWhitespaceOnly(String tn) {
        return isPossibleWhiteSpace(tn) &&
                tn.trim().length() == 0;
    }
    
    public ElementIdentity getElementIdentity() {
        return eID;
        
        
    }
    
    public void setElementIdentity(ElementIdentity eID) {
        this.eID = eID;
    }
    
    private boolean isSimpleContent(Node newParent) {
        NodeList childs = newParent.getChildNodes();
        for(int i=0;i<childs.getLength();i++)
            if(!(childs.item(i) instanceof Text))
                return false;
        return true;
    }
    
    private boolean isPretty(Node newParent) {
        return isPretty(newParent, null);
    }
    
    private boolean isPretty(Node newParent, Node newNode) {
        boolean parentPretty = false;
        NodeList childs = newParent.getChildNodes();
        int len = childs.getLength();
        
                /*
                 * <test name="test1"></test>  parentPretty = true
                 *
                 * <test name="test1"> parentPretty = true
                 * </test>
                 *
                 * <test name="test1">  parentPretty = true
                 *     <c name="c1">
                 * </test>
                 *
                 * <test name="test1"><c name="c1">  parentPretty = false
                 * </test>
                 */
        if(len == 0)
            parentPretty = true;
        else if(len == 1 && childs.item(0) instanceof Text)
            parentPretty = true;
        else if(len > 2 &&
                checkPrettyText((Node) childs.item(0)) && checkPrettyText((Node) childs.item(len-1)))
            parentPretty = true;
        
        if(!parentPretty)
            return false;
        
        if(newNode != null) {
            //now check newNode pretty
            Node preText = null;
            Node postText = null;
            int index = ((NodeImpl)newParent).getIndexOfChild(newNode);
            if(index > 0)
                preText = (Node)newParent.getChildNodes().item(index-1);
            if((index+1) < newParent.getChildNodes().getLength())
                postText = (Node)newParent.getChildNodes().item((index+1));
            
                        /*
                         * <test name="test1">
                         *     <a name="a1"/>
                         *     <b name="b1"/>
                         *     <c name="c1"/>  'c' pretty = true
                         * </test>
                         *
                         * <test name="test1">
                         *     <a name="a1"/>
                         *     <b name="b1"/><c name="c1"/>  'c' pretty = false
                         * </test>
                         *
                         */
            if(checkPrettyText(preText) && checkPrettyText(postText))
                return true;
        } else
            return parentPretty;
        
        return false;
    }

    /**
     * Set/get mapping of QName-valued attributes by element.
     * Key of the mapping is QName of the element.
     * Value of the mapping is list QName's of the attributes.
     * If the mapping is set, it will be used to identify the 
     * attribute values affected by namespace prefix refactoring 
     * during namespace consolidation.  If not set, namespace consolidation
     * would skip prefix rename refactoring case.
     */
    public void setQNameValuedAttributes(Map<QName,List<QName>> attrsByElement) {
        qnameValuedAttributesByElementMap = attrsByElement;
    }
    public Map<QName,List<QName>> getQNameValuedAttributes() {
        return qnameValuedAttributesByElementMap;
    }
    
    /**
     * The xml syntax parser
     */
    private XMLSyntaxParser parser;
    
    /**
     * The current stable document represented by the model
     */
    private Document currentDocument;
    
    /**
     * Property change support
     */
    private PropertyChangeSupport pcs;
    
    /**
     * The underlying model source
     */
    private ModelSource source;
    
    /**
     * Current status of the model
     */
    private Status status;
    
    private boolean pretty = false;
    
    /**
     * Undoable edit support
     */
    private UndoableEditSupport ues;
    
    /**
     * whether to fire undo events
     */
    private boolean fireUndoEvents = true;
    
    /**
     * The names of property change events fired
     */
    /**
     * Indicates node modified
     */
    public static final String PROP_MODIFIED = "modified";
    /**
     * Indicates node deleted
     */
    public static final String PROP_DELETED = "deleted";
    /**
     * Indicates node added
     */
    public static final String PROP_ADDED = "added";
    
    public static final String DEFAULT_INDENT = "    ";
    
    /**
     * current node count
     */
    private int nodeCount = 0;
    
    private ElementIdentity eID;
    
    private String currentIndent = "";
    
    private boolean indentInitialized = false;
    
    private Map<QName,List<QName>> qnameValuedAttributesByElementMap;
}
