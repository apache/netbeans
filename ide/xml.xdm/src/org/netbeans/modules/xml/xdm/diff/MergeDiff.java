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

package org.netbeans.modules.xml.xdm.diff;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;
import org.netbeans.modules.xml.xdm.XDMModel;
import org.netbeans.modules.xml.xdm.nodes.Attribute;
import org.netbeans.modules.xml.xdm.nodes.Node;
import org.netbeans.modules.xml.xdm.nodes.NodeImpl;
import org.netbeans.modules.xml.xdm.nodes.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.NodeList;

/*
 * This class is used by XDMTreeDiff (during Sync) to merge changes back to the original
 * xdm model document
 *
 * @author Ayub Khan
 */
public class MergeDiff {
    
    /** Creates a new instance of MergeDiff */
    public MergeDiff() {
    }
    
    public void merge(XDMModel model, List<Difference> deList) {
        this.model = model;
        
        HashMap<Node, Set<Change>> changesByNode = new HashMap<Node, Set<Change>>();
        HashMap<Node, Set<Difference>> childrenDiffsByNode = new HashMap<Node, Set<Difference>>();
        HashMap<Integer,Node> idToChangeds = new HashMap<Integer,Node>();
        
        for ( Difference de: deList ) {
            if (de instanceof Change) {
                Change change = (Change) de;
                if (change.isAttributeChanged() || change.isTokenChanged()) {
                    addDiffToMap(change.getOldNodeInfo().getNode(), change, changesByNode);
                }
                if (change.isPositionChanged()) {
                    addDiffToMap(de.getOldNodeInfo().getParent(), change, childrenDiffsByNode);
                }
            } else {
                addDiffToMap(de.getOldNodeInfo().getParent(), de, childrenDiffsByNode);
            }
        }
        
        for (Map.Entry<Node, Set<Change>> e : changesByNode.entrySet()) {
            Node target = getCurrentNode(e.getKey(), idToChangeds);
            assert (target != null) : "target "+e.getKey().getId()+"is no longer inTree";
            Set<Change> diffs = e.getValue();
            Node changed = applyChanges(diffs, target);
            assert changed.getId() == target.getId() : "changed id should not change";
            idToChangeds.put(changed.getId(), changed);
        }
        
        for (Map.Entry<Node, Set<Difference>> e : childrenDiffsByNode.entrySet()) {
            Node target = getCurrentNode(e.getKey(), idToChangeds);
            assert (target != null) : "target "+e.getKey().getId()+"is no longer inTree";
            Set<Difference> diffs = e.getValue();
            Node processed = applyChildrenDiffs(diffs, target);
            assert processed.getId() == target.getId() : "processed id should not change";
        }
    }
    
    private <T extends Difference>
            void addDiffToMap(Node key, T diff, HashMap<Node, Set<T>> map) {
        Set<T> diffs = map.get(key);
        if (diffs == null) {
            diffs = new HashSet<T>();
            map.put(key, diffs);
        }
        diffs.add(diff);
    }
    
    private Node getCurrentNode(Node target, HashMap<Integer,Node> idToChangeds) {
        Node newTarget = idToChangeds.get(target.getId());
        if (newTarget == null || ! newTarget.isInTree()) {
            List<Node> path = DiffFinder.getPathToRoot(target);
            if (path != null && ! path.isEmpty()) {
                newTarget = path.get(0);
                idToChangeds.put(target.getId(), newTarget);
            }
        }
        return newTarget;
    }
    
    private Node applyChildrenDiffs(final Set<Difference> diffs, Node target) {
        int id = target.getId();
        SortedMap<Integer, Difference> toAddOrReorder = new TreeMap<Integer, Difference>();
        for (Difference d : diffs) {
            if (d instanceof Delete ) {
                delete(d.getOldNodeInfo());
                target = d.getNewParent();
                assert id == target.getId();
            } else if (d instanceof Add) {
                toAddOrReorder.put(d.getNewNodeInfo().getPosition(), d);
            } else if (d instanceof Change) {
                Change change = (Change) d;
                assert (change.isPositionChanged() &&
                        change.getOldNodeInfo().getParent().getId() == target.getId());
                toAddOrReorder.put(change.getNewNodeInfo().getPosition(), change);
            }
        }
        

        target = processAddOrReorder(target, toAddOrReorder);
        assert id == target.getId();
        
        for (Difference d : diffs) {
            d.setNewParent(target);
            assert getReleventDiffNodeInfo(d).getNode().isInTree() : "Processed child not in tree: "+d;
        }
        return target;
    }

    private NodeInfo getReleventDiffNodeInfo (Difference d) {
        if (d instanceof Add) {
            return d.getNewNodeInfo();
        } else if (d instanceof Change) {
            Change c = (Change) d;
            if (c.isAttributeChanged() || c.isTokenChanged()) {
                return c.getNewNodeInfo();
            } else {
                return c.getOldNodeInfo();
            }
        } else if (d instanceof Delete) {
            return d.getOldNodeInfo();
        }
        throw new IllegalArgumentException("Invald diff type");
    }
    
    private List<Node> getChildrenNodes(Node target) {
        NodeList cList = target.getChildNodes();
        ArrayList<Node> nodes = new ArrayList<Node>();
        for (int i=0; i<cList.getLength(); i++) {
            nodes.add((Node) cList.item(i));
        }
        return nodes;
    }

    // position change node info needs to be get from new node info because
    // reordering processing should be after attribute and token changes processed.
    private NodeInfo getReorderNodeInfo(Change change) {
        assert change.isPositionChanged();
        if (change.isAttributeChanged() || change.isTokenChanged()) {
            return change.getNewNodeInfo();
        } else {
            return change.getOldNodeInfo();
        }
    }
    
    private Node processAddOrReorder(Node target, SortedMap<Integer, Difference> toAddOrReorder) {
        int id = target.getId();
        List<Node> worksheet = getChildrenNodes(target);
        
        // for accurace, first remove the nodes to be reordered from the worksheet
        for (Entry<Integer,Difference> e : toAddOrReorder.entrySet()) {
            Difference diff = e.getValue();
            if (diff instanceof Change) {
                Node toReorder = getReorderNodeInfo((Change)diff).getNode();
                if (! worksheet.remove(toReorder)) {
                    for (Iterator<Node> i = worksheet.iterator(); i.hasNext();) {
                        Node n = i.next();
                        if (n.getId() == toReorder.getId()) {
                            i.remove();
                            break;
                        }
                    }
                }
            }
        }
        // calculate the final ordering on the worksheet
        for (Entry<Integer,Difference> e : toAddOrReorder.entrySet()) {
            Difference diff = e.getValue();
            if (diff instanceof Change) {
                Node toReorder = getReorderNodeInfo((Change)diff).getNode();
                int index = diff.getNewNodeInfo().getPosition();
                worksheet.add(index, toReorder);
            } else if (diff instanceof Add) {
                // actual add of new child nodes
                add(target, (Add) diff);
                target = diff.getNewParent();
                assert id == target.getId();

                Node added = ((Add)diff).getNewNodeInfo().getNode();
                int index =  ((Add)diff).getNewNodeInfo().getPosition();
                worksheet.add(index, added);
            }
        }
        //calculate array for the reorder permutation
        assert target.getChildNodes().getLength() == worksheet.size() : "Failed "+toAddOrReorder.values();
        int[] permutation = new int[worksheet.size()];
        NodeList cList = target.getChildNodes();
        for (int i=0; i<cList.getLength(); i++) {
            Node m  = (Node) cList.item(i);
            int j = -1;
            for (int k=0; k<worksheet.size(); k++) {
                Node n = worksheet.get(k);
                if (m == n || n.isEquivalentNode(m)) {
                    j = k;
                    break;
                }
            }
            assert j > -1 : "current item "+i+" is not on worksheet";
            permutation[i] = j;
        }
        
        target = reorder(target, permutation);
        
        // update pure position change newNodeInfo because is used by event firing.
        for (Entry<Integer,Difference> e : toAddOrReorder.entrySet()) {
            Difference diff = e.getValue();
            if (diff instanceof Change) {
                Change c = (Change)diff;
                if (c.isPositionChanged() && 
                    ! c.isAttributeChanged() && ! c.isTokenChanged()) 
                {
                    c.getNewNodeInfo().setNode(c.getOldNodeInfo().getNode());
                }
            }
            
        }
        
        return target;
    }

    private Node applyChanges(Set<Change> diffs, Node target) {
        Node parent = null;
        for (Change change : diffs) {
            target = applyChange(change, target);
            parent = change.getNewParent();
        }
        // updating the diffs as this will be use by children change processing
        for (Change change : diffs) {
            change.getNewNodeInfo().setNode(target);
            change.setNewParent(parent);
        }
        return target;
    }
    
    private Node applyChange(final Change de, Node target) {
        //Apply token change first as attribute changes will update newNodeInfo node.
        Node oldNode = de.getOldNodeInfo().getNode();
        assert target.getId() == oldNode.getId() : "change target node id != old node id";
        Node curNode = de.getNewNodeInfo().getNode();
        
        if (de.isTokenChanged()) {
            NodeImpl newNode = createClone(target);
            newNode.copyTokens( curNode );
            de.setNewParent(modify(oldNode, newNode));
            target = newNode;
        }
        
        if (de.isAttributeChanged()) {
            assert oldNode.getLocalName().equals(curNode.getLocalName());
            applyAttrTokenChange(target, de);
        } else if (de.isTokenChanged()) {
            de.getNewNodeInfo().setNode(target);
        }
        return de.getNewNodeInfo().getNode();
    }
    
    private void applyAttrTokenChange(Node target, Change de) {
        Node curNode = de.getNewNodeInfo().getNode();
        List<Node> ancestors2 = DiffFinder.getPathToRoot(target);
        
        // get new positions
        NamedNodeMap nm2 = curNode.getAttributes();
        HashMap<String, Integer> nodeToPosition = new HashMap<String, Integer>();
        for ( int i=0; i < nm2.getLength(); i++ ) {
            Attribute newAttr = (Attribute) nm2.item(i);
            assert newAttr.getName() != null;
            nodeToPosition.put( newAttr.getName(), Integer.valueOf( i ) );
        }
        
        // to ensure accurate order, do delete or modify first, spare adds to the end
        List<Change.AttributeDiff> attrChanges = ((Change)de).getAttrChanges();
        SortedMap<Integer, Node> positionToNode = new TreeMap<Integer, Node>();
        for (Change.AttributeDiff attrDiff:attrChanges) {
            Attribute oldAttr = attrDiff.getOldAttribute();
            Attribute currAttr = attrDiff.getNewAttribute();
            if ( oldAttr != null ) {
                if ( currAttr == null ) {
                    ancestors2 = model.delete( oldAttr );
                } else {
                    NodeImpl cloneAttr = createClone(oldAttr);
                    cloneAttr.copyTokens(currAttr);
                    ancestors2 = model.modify(oldAttr, cloneAttr);
                }
            } else if ( currAttr != null ) {
                Integer pos = nodeToPosition.get(currAttr.getName());
                assert pos != null : "Attribute "+currAttr.getName() + " \n" + de + nodeToPosition + " \n" + positionToNode;
                positionToNode.put(pos, currAttr);
            }
        }
        
        for (Entry<Integer,Node> e : positionToNode.entrySet()) {
            Node copy = createCopy(e.getValue());
            curNode = (Element) ancestors2.get(0);
            ancestors2 = model.add(curNode, copy, e.getKey());
        }
        curNode = (Element) ancestors2.get(0);
        
        // save
        de.getNewNodeInfo().setNode(curNode);
        assert ancestors2.get(1).isInTree() : "new parent not intree";
        de.setNewParent(ancestors2.get(1));
    }
    
    private NodeImpl createCopy(final Node currNode) {
        NodeImpl newNode = (NodeImpl) ((NodeImpl)currNode).cloneNode(true, false);
        return newNode;
    }
    
    private NodeImpl createClone(final Node oldNode) {
        NodeImpl newNode = (NodeImpl) ((NodeImpl)oldNode).clone( false, false, false );
        return newNode;
    }
    
    private void add(Node parent, Difference diff) {
        NodeInfo newInfo = diff.getNewNodeInfo();
        int pos = newInfo.getPosition();
        assert pos <= parent.getChildNodes().getLength();
        Node newNode = null;
        if (diff instanceof Change) {
            newNode = createClone(newInfo.getNode());
        } else {
            newNode = createCopy(newInfo.getNode());
        }
        diff.setNewParent(model.add(parent, newNode, pos).get(0));
        newInfo.setNode(newNode);
    }
    
    private Node reorder(Node parent, int[] permutation) {
        return model.reorderChildren(parent, permutation).get(0);
    }
    
    private void delete(NodeInfo oldInfo) {
        oldInfo.setNewParent(model.delete(oldInfo.getNode()).get(0));
    }
    
    private Node modify(Node oldNode, Node newNode) {
        List<Node> ancestors = model.modify( oldNode, newNode );
        return ancestors.isEmpty() ? null : ancestors.get(0);
    }
    
    ////////////////////////////////////////////////////////////////////////////////
    // Member variables
    ////////////////////////////////////////////////////////////////////////////////
    
    private XDMModel model;
}
