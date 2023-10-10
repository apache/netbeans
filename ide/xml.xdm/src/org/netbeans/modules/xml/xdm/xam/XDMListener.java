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

package org.netbeans.modules.xml.xdm.xam;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.netbeans.modules.xml.xam.Component;
import org.netbeans.modules.xml.xam.Model;
import org.netbeans.modules.xml.xam.dom.AbstractDocumentComponent;
import org.netbeans.modules.xml.xam.dom.AbstractDocumentModel;
import org.netbeans.modules.xml.xam.dom.ChangeInfo;
import org.netbeans.modules.xml.xam.dom.DocumentComponent;
import org.netbeans.modules.xml.xam.dom.SyncUnit;
import org.netbeans.modules.xml.xdm.XDMModel;
import org.netbeans.modules.xml.xdm.diff.NodeInfo;
import org.netbeans.modules.xml.xdm.nodes.Node;
import org.netbeans.modules.xml.xdm.nodes.Element;
import org.netbeans.modules.xml.xdm.nodes.Document;

/**
 * @author Nam Nguyen
 * @author ajit
 */
public class XDMListener implements PropertyChangeListener {
    
    private AbstractDocumentModel model;
    private boolean inSync;
    private Document oldDocument;
    private boolean xamModelHasRoot;

    /** Creates a new instance of XDMListener */
    public XDMListener(AbstractDocumentModel model) {
        this.model = model;
    }
    
    public boolean xamModelHasRoot() {
        return xamModelHasRoot;
    }

    @Override
    public void propertyChange(PropertyChangeEvent event) {
        if (!inSync) return;

        NodeInfo oldInfo = (NodeInfo) event.getOldValue();
        NodeInfo newInfo = (NodeInfo) event.getNewValue();

        Node old = oldInfo!=null?(Node) oldInfo.getNode():null;
        Node now = newInfo!=null?(Node) newInfo.getNode():null;

        if (old != null) {
//            processEvent(old, oldInfo.getNewAncestors(), false);
            processEvent(old, oldInfo, false);
        }

        if (now != null) {
            processEvent(now, newInfo, true);
        }
    }

    private XDMModel getXDMModel() {
        return ((XDMAccess) model.getAccess()).getXDMModel();
    }
    
    void startSync() {
        inSync = true;
        xamModelHasRoot = true;
        syncUnits.clear();
        oldDocument = getXDMModel().getCurrentDocument();
        getXDMModel().addPropertyChangeListener(this);
    }
    
    Document getOldDocument() {
        return oldDocument;
    }
    
    /**
     * After sync processing.
     * @param processing means that the changes have to be processed, 
     * which are collected in syncUnits. Usually they have to be discarded 
     * in case of an error.
     */
    void endSync(boolean processing) {
        getXDMModel().removePropertyChangeListener(this);
        try {
            if (processing) {
                for (SyncUnit unit : syncUnits.values()) {
                    model.processSyncUnit(unit);
                }
            }
        } finally {
            syncUnits.clear();
            inSync = false;
        }
    }
    
    private Map<Integer, SyncUnit> syncUnits = new HashMap<Integer, SyncUnit>();
    private static Integer getID(ChangeInfo change) {
        if (change.getParent() == null) {
            return Integer.valueOf(0);
        } else {
            Element parent = (Element) change.getParent();
            return Integer.valueOf(parent.getId());
        }
    }
    private static Integer getID(DocumentComponent c) {
        if (c == null) {
            return Integer.valueOf(0);
        }
        Element xdmElement = (Element) ((AbstractDocumentComponent)c).getPeer();
        return Integer.valueOf(xdmElement.getId());
    }
    
    protected void processEvent(Node eventNode, NodeInfo nodeInfo, boolean isAdded) {
        List<Node> pathToRoot = nodeInfo.getNewAncestors();
        if (pathToRoot.size() == 1) {
            processRootRelatedEvent(eventNode, pathToRoot, isAdded);
        } else {
            if (eventNode.getId() == pathToRoot.get(0).getId()) {
                throw new IllegalArgumentException("Event node has same id as parent");
            }
            //
            pathToRoot = new ArrayList(pathToRoot);
            pathToRoot.add(0, eventNode);
            //
            // Another path to root which is used for looking for ns prefixes.
            List<Node> namespacePathToRoot = null;
            if (isAdded) { 
                namespacePathToRoot = pathToRoot;
            } else {
                // The old tree has to be used for prefix search after deletion.
                // See issue #166177
                //
                namespacePathToRoot = nodeInfo.getOriginalAncestors();
                namespacePathToRoot = new ArrayList(namespacePathToRoot);
                namespacePathToRoot.add(0, eventNode);
                assert namespacePathToRoot.size() == pathToRoot.size();
            }
            //
            ChangeInfo change = model.prepareChangeInfo(pathToRoot, namespacePathToRoot);
            change.setAdded(isAdded);
            processChange(change);
        }
    }

    protected void processRootRelatedEvent(Node eventNode, List<Node> pathToRoot, boolean isAdded) {
        assert pathToRoot.get(0) instanceof Document;
        if (! (eventNode instanceof Element)) {
            return;
        }
        //assert eventNode.getId() == 1;
        if (! isAdded) {
            // It's implied that the root component is deleted here.
            // model.removeRootComponent();
            xamModelHasRoot = false;
            return;
        }
        Component rootComponent = null;
        String errorMessage = null;
        try {
            rootComponent = model.createRootComponent((Element) eventNode);
        } catch(IllegalArgumentException e) {
            errorMessage = e.getMessage();
        }
        if (rootComponent == null) {
            errorMessage = errorMessage != null ? errorMessage :
                "Unexpected root element "+AbstractDocumentComponent.getQName(eventNode);
            throw new IllegalArgumentException(new IOException(errorMessage));
        }
        xamModelHasRoot = true;
        model.firePropertyChangeEvent(new PropertyChangeEvent(model,
            Model.STATE_PROPERTY, Model.State.NOT_SYNCED, Model.State.VALID));
    }
    
    protected void processChange(ChangeInfo change) {
        Integer unitID = getID(change);
        SyncUnit existing = syncUnits.get(unitID);
        SyncUnit su = model.prepareSyncUnit(change, existing);
        if (su == null) {
            return;
        }
        Integer reviewedID = getID(su.getTarget());
        existing = syncUnits.get(reviewedID);
        if (existing == null) {
            if (unitID.equals(reviewedID)) {
                // normal new sync order
                syncUnits.put(reviewedID, su);
            } else {
                existing = syncUnits.get(reviewedID);
                if (existing != null) {
                    existing.merge(su);
                } else {
                    syncUnits.put(reviewedID, su);
                }
            }
        } else {
            if (existing != su) {
                if (unitID.equals(reviewedID)) {
                    existing.merge(su);
                } else {
                    // existing sync unit replaced by reviewed one on ancestor of original target
                    syncUnits.remove(unitID);
                    existing = syncUnits.get(reviewedID);
                    if (existing != null) {
                        existing.merge(su);
                    } else {
                        syncUnits.put(reviewedID, su);
                    }
                }
            }
        }
    }

    static List<org.w3c.dom.Node> toDomNodes(List<Node> nodes) {
        List<org.w3c.dom.Node> domNodes = new ArrayList<org.w3c.dom.Node>();
        for (Node n : nodes) {
            domNodes.add((org.w3c.dom.Node) n);
        }
        return domNodes;
    }

    /**
     * @deprecated this methos was added to 6.9 release by mistake and
     * it is temporary kept here for the sake of binary compatibility.
     * This implementaion do nothing. The correct location of the method is
     * {@link org.netbeans.modules.xml.xam.dom.AbstractDocumentModel#prepareChangeInfo(java.util.List, java.util.List)}
     */
    @Deprecated
    protected ChangeInfo prepareChangeInfo(List<? extends Node> pathToRoot,
            List<? extends Node> nsContextPathToRoot) {
        return null;
    }
    
 }
