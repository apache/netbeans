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

package org.netbeans.modules.xml.xam.dom;

import java.beans.PropertyChangeEvent;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.xml.namespace.QName;
import org.netbeans.modules.xml.xam.AbstractModel;
import org.netbeans.modules.xml.xam.Component;
import org.netbeans.modules.xml.xam.ComponentEvent;
import org.netbeans.modules.xml.xam.ComponentUpdater;
import org.netbeans.modules.xml.xam.EmbeddableRoot;
import org.netbeans.modules.xml.xam.Model.State;
import org.netbeans.modules.xml.xam.ModelSource;
import org.netbeans.modules.xml.xam.spi.DocumentModelAccessProvider;
import org.openide.util.Lookup;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * @author Chris Webster
 * @author Rico
 * @author Nam Nguyen
 */
public abstract class AbstractDocumentModel<T extends DocumentComponent<T>> 
        extends AbstractModel<T> implements DocumentModel<T> {

    /**
     * Do not assign to this field. And do not read from it either. Its initialization is not synchronized
     * by 'this' instance from 6.x, there's no guarantee that the field's value will be visible to the
     * reading thread.
     * <p/>
     * The field will be soon deprecated
     */
    protected DocumentModelAccess access;
    
    private volatile DocumentModelAccess accessPrivate;
    
    /**
     * needsSync contains timestamp of the last dirtying operation. Non-zero
     * value means dirty status.
     */
    private volatile long needsSync;
    private DocumentListener docListener;
    private javax.swing.text.Document swingDocument;
    private final Object getAccessLock = new Object();
    
    public AbstractDocumentModel(ModelSource source) {
        super(source);
	docListener = new DocumentChangeListener();
    }

    public javax.swing.text.Document getBaseDocument() {
        return (javax.swing.text.Document) 
            getModelSource().getLookup().lookup(javax.swing.text.Document.class);
    }
    
    public abstract T createRootComponent(Element root);
    
    @Override
    public boolean areSameNodes(Node n1, Node n2) {
        return getAccess().areSameNodes(n1, n2);
    }
    
    /**
     * Returns QName of elements used in model.  Domain model implementation needs 
     * to override this to be able to embed elements outside of the domain such as
     * child elements of documentation in schema model.
     * @return full set of element QName's or null if there is no needs for distinction 
     * between domain and non-domain elements.
     */
    public Set<QName> getQNames() { 
        return Collections.emptySet(); 
    }
    
    @Override
    protected boolean needsSync() {
	javax.swing.text.Document lastDoc = swingDocument;
	javax.swing.text.Document currentDoc = (javax.swing.text.Document)
		getModelSource().getLookup().lookup(javax.swing.text.Document.class);
    if (currentDoc == null) {
        swingDocument = null;
        setState(State.NOT_SYNCED); // The XAM model isn't synched with text, because there isn't a text at all
        getAccess().unsetDirty();
        return false;
    }
	if (lastDoc == null || currentDoc != lastDoc) {
	    swingDocument = currentDoc;
	    currentDoc.addDocumentListener(new WeakDocumentListener(docListener, currentDoc));
	}
	return needsSync != 0 || !currentDoc.equals(lastDoc);
    }
    
    @Override
    protected void syncStarted() {
        needsSync = 0;
        getAccess().unsetDirty();
    }

    @Override
    protected void syncCompleted() {
        super.syncCompleted();
    }

    
    private void documentChanged() {
	if (!isIntransaction()) {
            DocumentModelAccess acc;
            synchronized (getAccessLock) {
                acc = accessPrivate;
                needsSync = System.currentTimeMillis();
            }
            // if the access was not yet created, the needsSync will keep
            // the dirty information until the model access is initialized; see getAcces().
            if (acc != null) {
                acc.setDirty();
            }
	}
    }

    private static class WeakDocumentListener implements DocumentListener {
	
	public WeakDocumentListener(DocumentListener delegate, 
				    javax.swing.text.Document source) {
	    this.source = source;
	    this.delegate = new WeakReference<DocumentListener>(delegate);
	}
	
	private DocumentListener getDelegate() {
	    DocumentListener l = delegate.get();
	    if (l == null) {
		source.removeDocumentListener(this);
	    }
	    
	    return l;
	}

        @Override
	public void removeUpdate(DocumentEvent e) {
	    DocumentListener l = getDelegate();
	    if (l != null) {
		l.removeUpdate(e);
	    }
	}

        @Override
	public void changedUpdate(DocumentEvent e) {
	    DocumentListener l = getDelegate();
	    if (l != null) {
		l.changedUpdate(e);
	    }
	}
	
        @Override
	public void insertUpdate(DocumentEvent e) {
	    DocumentListener l = getDelegate();
	    if (l != null) {
		l.insertUpdate(e);
	    }
	}
	
	private javax.swing.text.Document source;
	private WeakReference<DocumentListener> delegate;
    }
    
    private class DocumentChangeListener implements DocumentListener {
        @Override
	public void removeUpdate(DocumentEvent e) {
	    documentChanged();
	}
	
        @Override
	public void insertUpdate(DocumentEvent e) {
	    documentChanged();
	}
	
        @Override
	public void changedUpdate(DocumentEvent e) {
	    // ignore these events as these are not changes
	    // to the document text but the document itself
	}
    }

    protected abstract ComponentUpdater<T> getComponentUpdater();
    
    /**
     * Allows match just by tag name, in case full QName is not available.
     */
    private Set<String> elementNames = null;
    public Set<String> getElementNames() {
        if (elementNames == null) {
            elementNames = new HashSet<String>();
            Set<QName> qnames = getQNames();
            for (QName q : qnames) {
                elementNames.add(q.getLocalPart());
            }
        }
        return elementNames;
    }

    /**
     *
     * @param pathToRoot
     * @return
     * @deprecated Use {@link org.netbeans.modules.xml.xam.dom.AbstractDocumentModel#prepareChangeInfo(java.util.List, java.util.List)} instead. It is necessary for fixing bug #166177.
     *
     */
    @Deprecated
    public ChangeInfo prepareChangeInfo(List<Node> pathToRoot) {
        return prepareChangeInfo(pathToRoot, pathToRoot);
    }


    /**
     * Performs intermediate stage of synchronization XDM --> XAM.
     * A new {@link org.netbeans.modules.xml.xam.dom.ChangeInfo} object 
     * is generated here.
     *
     * @param pathToRoot a path of DOM objects from root to changed one.
     * @param nsContextPathToRoot Usually the same path as previous param,
     * but in case of deletion it contains the same path from old model's tree.
     * It is required in case of prefix declaration deletion, because the deleted
     * declaration is present only in old model's tree.
     *
     * Be aware that the method is designed to be called only from XDM
     * {@link org.netbeans.modules.xml.xdm.xam.XDMListener},
     * but it also can be redifined. An example can be found in
     * {@link org.netbeans.modules.xml.wsdl.model.WSDLModel}.
     *
     * @since 1.11
     *
     */
    public ChangeInfo prepareChangeInfo(List<? extends Node> pathToRoot,
            List<? extends Node> nsContextPathToRoot) {
        // we already handle change on root before enter here
        if (pathToRoot.size() < 1) {
            throw new IllegalArgumentException("pathToRoot here should be at least 1");
        }
        if (pathToRoot.get(pathToRoot.size()-1) instanceof Document) {
            pathToRoot.remove(pathToRoot.size()-1);
        }

        if (pathToRoot.size() < 2) {
            throw new IllegalArgumentException("pathToRoot here should be at least 2");
        }
        //
        Node current = null;
        Element parent = null;
        boolean changedIsDomainElement = true;
        Set<QName> qnames = getQNames();
        if (qnames != null && qnames.size() > 0) {
            for (int i=pathToRoot.size()-1; i>=0; i--) {
                //
                Node n = pathToRoot.get(i);
                parent = (Element)current;
                current = n;
                //
                if (! (n instanceof Element)) {
                    changedIsDomainElement = false;
                    break;
                }

                QName currentQName = new QName(getAccess().lookupNamespaceURI(
                        current, nsContextPathToRoot), current.getLocalName());
                if (!(qnames.contains(currentQName))) {
                    changedIsDomainElement =  false;
                    break;
                }
            }
        } else {
            current = pathToRoot.get(0);
            parent = (Element) pathToRoot.get(1);
            changedIsDomainElement = current instanceof Element;
        }

        List<Element> rootToParent = new ArrayList<Element>();
        if (parent != null) {
            for (int i = pathToRoot.indexOf(parent); i<pathToRoot.size(); i++) {
                rootToParent.add(0, (Element)pathToRoot.get(i));
            }
        }

        List<Node> otherNodes = new ArrayList<Node>();
        if (parent != null) {
            int iCurrent = pathToRoot.indexOf(current);
            for (int i=0; i < iCurrent; i++) {
                otherNodes.add(0, pathToRoot.get(i));
            }
        }

        return new ChangeInfo(parent, current, changedIsDomainElement,
                rootToParent, otherNodes);
    }
    
    public SyncUnit prepareSyncUnit(ChangeInfo change, SyncUnit order) {
        if (change.getChangedNode() == null) {
            throw new IllegalStateException("Bad change info");
        }
        AbstractDocumentComponent parentComponent = (AbstractDocumentComponent) change.getParentComponent();
        if (parentComponent == null) {
            parentComponent = (AbstractDocumentComponent) findComponent(change.getRootToParentPath());
        }
        if (parentComponent == null) {
            throw new IllegalArgumentException("Could not find parent component");
        }
        
        DocumentComponent toRemove = null;
        DocumentComponent toAdd = null;
        boolean changed = false;
        
        if (change.isDomainElement()) {
            if (change.isDomainElementAdded()) {
                toAdd = createChildComponent(parentComponent, change.getChangedElement());
            } else {
    			toRemove = parentComponent.findChildComponent(change.getChangedElement());
                if (toRemove == null) {
                    parentComponent.findChildComponentByIdentity(change.getChangedElement());
                }
            }
        } else {
            changed = true;
        }
        
        if (order == null) {
            order = new SyncUnit(parentComponent);
        }
        
        order.addChange(change);
        if (toRemove != null) order.addToRemoveList(toRemove);
        if (toAdd != null) order.addToAddList(toAdd);
        if (changed) order.setComponentChanged(true);
        return order;
    }
    
    protected void firePropertyChangedEvents(SyncUnit unit) {
        firePropertyChangedEvents(unit, null);
    }
    
    protected void firePropertyChangedEvents(SyncUnit unit, Element oldElement) {
        Set<String> propertyNames = new HashSet<>(unit.getRemovedAttributes().keySet());
        propertyNames.addAll(unit.getAddedAttributes().keySet());
        for (String name : propertyNames) {
            Attr oldAttr = unit.getRemovedAttributes().get(name);
            Attr newAttr = unit.getAddedAttributes().get(name);
            super.firePropertyChangeEvent(
                    new PropertyChangeEvent(
                    unit.getTarget(), name,
                    oldAttr == null ? null : oldAttr.getValue(),
                    newAttr == null ? null : newAttr.getValue()));
        }
        if (unit.hasTextContentChanges()) {
            super.firePropertyChangeEvent(
                    new PropertyChangeEvent(
                    unit.getTarget(), DocumentComponent.TEXT_CONTENT_PROPERTY,
                    oldElement == null ? "" : getAccess().getXmlFragment(oldElement),
                    getAccess().getXmlFragment(unit.getTarget().getPeer())));
        }

        for (String tagname : unit.getNonDomainedElementChanges()) {
            List<Element> old = new ArrayList<Element>();
            List<Element> now = new ArrayList<Element>();
            NodeList oldNodes = oldElement.getElementsByTagName(tagname);
            for (int i=0; i<oldNodes.getLength(); i++) {
                Element e = (Element)oldNodes.item(i);
                old.add((Element)e.cloneNode(true));
            }
            NodeList newNodes = unit.getTarget().getPeer().getElementsByTagName(tagname);
            for (int i=0; i<newNodes.getLength(); i++) {
                now.add((Element)newNodes.item(i).cloneNode(true));
            }
            super.firePropertyChangeEvent(
                    new PropertyChangeEvent(unit.getTarget(), toLocalName(tagname), old, now));
        }
    }
    
    protected static String toLocalName(String tagName) {
        String[] parts = tagName.split(":"); //NOI18N
        return parts[parts.length-1];
    }
    
    public void processSyncUnit(SyncUnit syncOrder) {
        AbstractDocumentComponent targetComponent = (AbstractDocumentComponent) syncOrder.getTarget();
        if (targetComponent == null) {
            throw new IllegalArgumentException("sync unit should not be null");
        }
        // skip target component whose some ancestor removed in previous processed syncUnit
        if (! targetComponent.isInDocumentModel()) {
            return;
        }
        
        Element oldElement = syncOrder.getTarget().getPeer();
        syncOrder.updateTargetReference();
        if (syncOrder.isComponentChanged()) {
            ComponentEvent.EventType changeType = ComponentEvent.EventType.VALUE_CHANGED;
            if (! syncOrder.hasWhitespaceChangeOnly()) {
                fireComponentChangedEvent(new ComponentEvent(targetComponent, changeType));
            }
            firePropertyChangedEvents(syncOrder, oldElement);
        }
        
        for (DocumentComponent c : syncOrder.getToRemoveList()) {
            removeChildComponent(c);
        }
        
        for (DocumentComponent c : syncOrder.getToAddList()) {
            Element childElement = (Element) ((AbstractDocumentComponent)c).getPeer();
            int index = targetComponent.findDomainIndex(childElement);
            addChildComponent(targetComponent, c, index);
        }
    }

    private DocumentComponent createChildComponent(DocumentComponent parent, Element e) {
        DocumentModel m = (DocumentModel) parent.getModel();
        if (m == null) {
            throw new IllegalArgumentException("Cannot create child component from a deleted component.");
        }
        return m.createComponent(parent, e);
    }
    
    @Override
    public void addChildComponent(Component target, Component child, int index) {
        AbstractDocumentModel m = (AbstractDocumentModel)target.getModel();
        //assert m != null : "Cannot add child to a deleted component.";
        //Work-around xdm overlapping in firing
        if (m == null) return;
        m.getComponentUpdater().update(target, child, index, ComponentUpdater.Operation.ADD);
    }
    
    @Override
    public void removeChildComponent(Component child) {
        if (child.getParent() == null) return;
        AbstractDocumentModel m = (AbstractDocumentModel) child.getParent().getModel();
        //Work-around xdm overlapping in firing
        //assert m != null : "Cannot remove child from a deleted component.";
        if (m == null) return;
        m.getComponentUpdater().update(child.getParent(), child, ComponentUpdater.Operation.REMOVE);
    }
    
    public DocumentComponent findComponent(Element e) {
        return findComponent((AbstractDocumentComponent) getRootComponent(), e);
    }
    
    private DocumentComponent findComponent(DocumentComponent searchRoot, Element e) {
        if (searchRoot.referencesSameNode(e)) {
            return searchRoot;
        }
        for (Object o : searchRoot.getChildren()) {
            DocumentComponent found = findComponent((DocumentComponent) o, e);
            if (found != null) {
                return found;
            }
        }
        if (searchRoot instanceof EmbeddableRoot.ForeignParent) {
           for (EmbeddableRoot child : ((EmbeddableRoot.ForeignParent)searchRoot).getAdoptedChildren()) {
               if (child instanceof DocumentComponent) {
                   DocumentComponent found = findComponent((DocumentComponent) child, e);
                   if (found != null) {
                       return found;
                   }
               }
           }
       }
       
        return null;
    }
    
    /**
     * Find the component given a path to its element node from root.  All elements, except for
     * the target element should be in the latest version of the xdm tree.  All components on the
     * path will be updated with latest version elements.
     *
     * Note that returned component could be part of an embedded model, which could be of
     * a different type of model.
     *
     * @param pathFromRoot list of elements from model root to backing element of target component.
     * @return component backed by the last element on pathFromRoot or null if not found.
     */
    public DocumentComponent findComponent(List<Element> pathFromRoot) {
        return findComponent((AbstractDocumentComponent)getRootComponent(), pathFromRoot, 0);
    }

    public AbstractDocumentComponent findComponent(AbstractDocumentComponent base, List<Element> pathFromRoot, int current) {
        if (pathFromRoot == null || pathFromRoot.size() <= current) {
            return null;
        }
        Element e = pathFromRoot.get(current);
        if (base.referencesSameNode(e)) {
            if (pathFromRoot.size() == current + 1) {
                base.checkChildrenPopulated(); // make sure children inited
                return base;
            } else {
                for (Object child : base.getChildren()) {
                    AbstractDocumentComponent ac = (AbstractDocumentComponent) child;
                    AbstractDocumentComponent found = findComponent(ac, pathFromRoot, current+1);
                    if (found != null) {
                        return found;
                    }
                }
            }
        }
        return null;
    }
    
    @Override
    public DocumentComponent findComponent(int position) {
        if (getState() != State.VALID) {
            return getRootComponent();
        }
            
        Element e = (Element) getAccess().getContainingElement(position);
        if (e == null) {
            return getRootComponent();
        }
        
        List<Element> pathFromRoot = null;
        try {
            pathFromRoot = getAccess().getPathFromRoot(this.getDocument(), e);
        } catch(UnsupportedOperationException ex) {
            // OK
        }
        if (pathFromRoot == null || pathFromRoot.isEmpty()) {
            return findComponent(e);
        } else {
            return findComponent(pathFromRoot);
        }
    }
    
    @Override
    public String getXPathExpression(DocumentComponent component) {
        Element e = (Element) component.getPeer();
        return getAccess().getXPath(getDocument(), e);
    }

    @Override
    public org.w3c.dom.Document getDocument() {
        return getAccess().getDocumentRoot();
    }
    
    @Override
    public DocumentModelAccess getAccess() {
        DocumentModelAccess acc = accessPrivate;
        if (acc == null) {
            acc = getEffectiveAccessProvider().createModelAccess(this);
            long ts;
            synchronized (getAccessLock) {
                if (accessPrivate != null) {
                    // already loaded and initialized
                    return accessPrivate;
                }
                accessPrivate = access = acc;
                if (! (acc instanceof ReadOnlyAccess)) {
                    acc.addUndoableEditListener(this);
                    setIdentifyingAttributes();
                }
                ts = needsSync;
            }
            if (ts != 0) {
                // delayed setDirty, access was not present when documentChanged was called
                // was called.
                acc.setDirty();
            }
        }
        return acc;
    }

    private DocumentModelAccessProvider getEffectiveAccessProvider() {
	DocumentModelAccessProvider p = (DocumentModelAccessProvider)
	    getModelSource().getLookup().lookup(DocumentModelAccessProvider.class);
	return p == null ? getAccessProvider() : p;
    }
    
    public static DocumentModelAccessProvider getAccessProvider() {
        DocumentModelAccessProvider provider = (DocumentModelAccessProvider) 
            Lookup.getDefault().lookup(DocumentModelAccessProvider.class);
        if (provider == null) {
            return ReadOnlyAccess.Provider.getInstance();
        }
        return provider;
    }
    
    /**
     * Set the identifying attributes for underlying access to merge.
     */
    protected void setIdentifyingAttributes() {
        ElementIdentity eid = getAccess().getElementIdentity();
        eid.addIdentifier("id");
        eid.addIdentifier("name");
        eid.addIdentifier("ref");
    }

    protected boolean isDomainElement(Node e) {
        if (! (e instanceof Element)) {
            return false;
        }
        
        QName q = new QName(e.getNamespaceURI(), e.getLocalName());
        return getQNames().contains(q) || getElementNames().contains(q.getLocalPart());
    }
    
    @Override
    protected void refresh() {
        Document lastStable = null;
        try {
            lastStable = getDocument();
        } catch(Exception ex) {
            // document is not available when underlying model is broken
        }
        if (lastStable != null && lastStable.getDocumentElement() != null) {
            createRootComponent(lastStable.getDocumentElement());
            setState(State.VALID);
        }
    }
    
    /**
     * Returns QName of all attributes with QName value, sorted by containing 
     * element QName.  
     * Note: if domain model implementation return null, namespace
     * consolidation will not attempt namespace prefix refactoring on each 
     * mutation of the underlying XDM DOM tree.
     */
    public Map<QName,List<QName>> getQNameValuedAttributes() {
        return new HashMap<QName,List<QName>>();
    }
}


