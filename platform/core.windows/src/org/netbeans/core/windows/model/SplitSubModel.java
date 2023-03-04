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

package org.netbeans.core.windows.model;



import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.core.windows.*;
import org.openide.util.WeakSet;


/**
 * Sub-model of split (n-branch) tree, which represents structure
 * of split components. It's used in ModesModel as representation
 * of split modes, and also as a representation of editor area,
 * which is in fact the same, just it is inside the enclosed splits.
 *
 * @author  Peter Zavadsky
 */
class SplitSubModel {

    /** Parent model instance. */
    protected final Model parentModel;
    
    /** Maps modes to nodes of this n-branch tree model. */
    private final Set<ModeNode> nodes = new WeakSet<ModeNode>(20);
    
    /** Root <code>Node</code> which represents the split panes structure
     * with modes as leaves. */
    protected Node root;
    
    /** Debugging flag. */
    private static final boolean DEBUG = Debug.isLoggable(SplitSubModel.class);

    
    /** Creates a new instance of SplitModel */
    public SplitSubModel(Model parentModel) {
        this.parentModel = parentModel;
    }

    
    private ModeNode getModeNode(ModeImpl mode) {
        ModeNode res = null;
        synchronized(nodes) {
            for( ModeNode node : nodes ) {
                if( node.getMode().equals( mode ) ) {
                    res = node;
                    break;
                }
            }
            if( null == res ) {
                res = new ModeNode( mode );
                nodes.add( res );
            }
        
            return res;
        }
    }
    
    public SplitConstraint[] getModelElementConstraints(ModelElement element) {
        if(element instanceof Node) {
            Node node = (Node)element;
            if(!isInTree(node)) {
                return null;
            }
            return node.getNodeConstraints();
        }
        
        return null;
    }
    
    public SplitConstraint[] getModeConstraints(ModeImpl mode) {
        ModeNode modeNode = getModeNode(mode);
        return modeNode.getNodeConstraints();
    }
    
    /**
     * Find the side (LEFT/RIGHT/BOTTOM) where the TopComponent from the given
     * mode should slide to. The position is deducted from mode's position relative
     * to the editor mode in the split hierarchy.
     * 
     * @param mode Mode
     * @return The slide side for TopComponents from the given mode.
     */
    public String getSlideSideForMode( ModeImpl mode ) {
        ModeNode modeNode = getModeNode( mode );
        return getPositionRelativeToEditor( modeNode, modeNode.getParent() );
    }
    
    /**
     * Find recursively the position of the given Node relative to editor node.
     * 
     * @param node The Node who's position is being compared with editor node's position.
     * @param parent Node's split parent.
     * @return LEFT/RIGHT/BOTTOM/TOP according to Node's position in the hierarchy.
     */
    private String getPositionRelativeToEditor( Node node, SplitNode parent ) {
        if( null == parent )
            return Constants.LEFT; //fallback - we're at the top level of the hierarchy
        
        Node editorNode = getEditorChildNode( parent );
        if( null != editorNode ) {
            //the split parent contains a node or sub-tree with editor node
            //so let's compare their positions
            int orientation = parent.getOrientation();
            int nodeIndex = parent.getChildIndex( node );
            int editorIndex = parent.getChildIndex( editorNode );
            if( orientation == Constants.VERTICAL ) {
                if( nodeIndex > editorIndex )
                    return Constants.BOTTOM;
                return Constants.TOP; //TODO: is this really OK?
            } else {
                if( nodeIndex < editorIndex )
                    return Constants.LEFT;
                return Constants.RIGHT;
            }
        }
        //try one level up in the tree hierarchy
        return getPositionRelativeToEditor( parent, parent.getParent() );
    }
    
    /**
     * Find the child node that contains the editor node or the child that has the editor
     * node in its sub-tree.
     * 
     * @param split SplitNode searched for editor node.
     * @return Node which is the editor node or which has the editor node in its sub-tree.
     */
    private Node getEditorChildNode( SplitNode split ) {
        List<Node> children = split.getChildren();
        for( Iterator<Node> i=children.iterator(); i.hasNext(); ) {
            Node node = i.next();
            if( node instanceof EditorSplitSubModel.EditorNode 
                ||
                node instanceof SplitNode && null != getEditorChildNode( (SplitNode)node ) ) {
                return node;
            }
        }
        return null;
    }
    
    
    /** Adds mode which is <code>Node</code>
     * with specified constraints designating the path in model.
     * <em>Note: It is important to know that adding of mode can affect the structure
     * the way, it can change constraints of already added modes (they could
     * be moved in that tree)</em> */
    public boolean addMode(ModeImpl mode, SplitConstraint[] constraints) {
        // PENDING do we support empty constraints?
        if(mode == null || constraints == null) {
            Logger.getLogger(SplitSubModel.class.getName()).log(Level.WARNING, null,
                              new java.lang.IllegalArgumentException("Mode=" +
                                                                     mode +
                                                                     " constraints=" +
                                                                     Arrays.toString(constraints)));
            return false;
        }

        
        Node modeNode = getModeNode(mode);
        
        if(DEBUG) {
            debugLog(""); // NOI18N
            debugLog(""); // NOI18N
            debugLog("=========================================="); // NOI18N
            debugLog("Adding mode to tree=" + mode); // NOI18N
            debugLog("constraints=" + Arrays.asList(constraints)); // NOI18N
            debugLog("modeNode=" + modeNode); // NOI18N
        }

        return addNodeToTree(modeNode, constraints);
    }
    
    // XXX
    public boolean addModeToSide(ModeImpl mode, ModeImpl attachMode, String side) {
        if(mode == null || mode.getState() == Constants.MODE_STATE_SEPARATED) {
            Logger.getLogger(SplitSubModel.class.getName()).log(Level.WARNING, null,
                              new java.lang.IllegalArgumentException("Mode=" +
                                                                     mode));
            return false;
        }

        Node modeNode = getModeNode(mode);
        Node attachModeNode = getModeNode(attachMode);
        
        if(DEBUG) {
            debugLog(""); // NOI18N
            debugLog(""); // NOI18N
            debugLog("=========================================="); // NOI18N
            debugLog("Adding mode to between=" + mode); // NOI18N
            debugLog("attachMode=" + attachMode); // NOI18N
            debugLog("side=" + side); // NOI18N
        }

        return addNodeToTreeToSide(modeNode, attachModeNode, side);
    }

    public boolean addModeToSideRoot(ModeImpl mode, String side) {
        if(mode == null || mode.getState() == Constants.MODE_STATE_SEPARATED) {
            Logger.getLogger(SplitSubModel.class.getName()).log(Level.WARNING, null,
                              new java.lang.IllegalArgumentException("Mode=" +
                                                                     mode));
            return false;
        }

        Node modeNode = getModeNode(mode);
        
        if(DEBUG) {
            debugLog(""); // NOI18N
            debugLog(""); // NOI18N
            debugLog("=========================================="); // NOI18N
            debugLog("Adding mode to root's side=" + mode); // NOI18N
            debugLog("side=" + side); // NOI18N
        }

        return addNodeToTreeToSide(modeNode, root, side);
    }

    // XXX
    public boolean addModeAround(ModeImpl mode, String side) {
        if(mode == null || mode.getState() == Constants.MODE_STATE_SEPARATED) {
            Logger.getLogger(SplitSubModel.class.getName()).log(Level.WARNING, null,
                              new java.lang.IllegalArgumentException("Mode=" +
                                                                     mode));
            return false;
        }

        Node modeNode = getModeNode(mode);
        
        if(DEBUG) {
            debugLog(""); // NOI18N
            debugLog(""); // NOI18N
            debugLog("=========================================="); // NOI18N
            debugLog("Adding mode to around=" + mode); // NOI18N
            debugLog("side=" + side); // NOI18N
        }

        return addNodeToTreeAround(modeNode, side);
    }
    
    // XXX
    public boolean addModeAroundEditor(ModeImpl mode, String side) {
        if(mode == null || mode.getState() == Constants.MODE_STATE_SEPARATED) {
            Logger.getLogger(SplitSubModel.class.getName()).log(Level.WARNING, null,
                              new java.lang.IllegalArgumentException("Mode=" +
                                                                     mode));
            return false;
        }

        Node modeNode = getModeNode(mode);
        
        if(DEBUG) {
            debugLog(""); // NOI18N
            debugLog(""); // NOI18N
            debugLog("=========================================="); // NOI18N
            debugLog("Adding mode to around=" + mode); // NOI18N
            debugLog("side=" + side); // NOI18N
        }

        return addNodeToTreeAroundEditor(modeNode, side);
    }

    private boolean isInTree(Node descendant) {
        if(root == null) {
            return false;
        }
        
        if(descendant == root) {
            return true;
        }
        
        Node parent = descendant.getParent();
        while(parent != null) {
            if(parent == root) {
                return true;
            }
            parent = parent.getParent();
        }
        
        return false;
    }

    /** Adds node into the tree structure if there isn't yet. */
    protected boolean addNodeToTree(Node addingNode, SplitConstraint[] constraints) {
        if(isInTree(addingNode)) {
            return false;
        }
        
        // Find starting split.
        SplitNode splitNode;
        // First solve root.
        if(root == null) {
            if(constraints.length == 0) {
                root = addingNode;
                return true;
            }
            
            // There is nothing, create split.
            splitNode = new SplitNode(constraints[0].orientation);
            root = splitNode;
        } else if(root instanceof SplitNode) {
            splitNode = (SplitNode)root;
        } else {
            // All other nodes (ModeNode, and EditorNode in subclass).
            splitNode = new SplitNode(0); // Default orientation when splitting root?
            splitNode.setChildAt(-1, 0.5D, root);
            root = splitNode;
        }

        // Traverse the structure.
        for(int level = 0; level < constraints.length; level++) {
            int orientation   = constraints[level].orientation;

            // First solve orientation
            if(orientation != splitNode.getOrientation()) {
                // Orientation doesn't fit, create new split.
                SplitNode newSplit = new SplitNode(orientation);
                if(splitNode == root) {
                    // Creating new branch.
                    newSplit.setChildAt(-1, 0.5D, splitNode);
                    root = newSplit;
                } else {
                    SplitNode parent = splitNode.getParent();
                    int   oldIndex       = parent.getChildIndex(splitNode);
                    double oldSplitWeight = parent.getChildSplitWeight(splitNode);
                    // move the original split as child of new one and newSplit put under parent.
                    parent.removeChild(splitNode);

                    // Creating new branch.
                    newSplit.setChildAt(-1, 0.5D, splitNode);
                    parent.setChildAt(oldIndex, oldSplitWeight, newSplit);
                }
                
                splitNode = newSplit;
            }
            
            // Then solve next position (together with splitWeight).
            // But if this is the last iteration, don't do anything the adding will be done after loop.
            if(level < constraints.length - 1) {
                int index         = constraints[level].index;
                double splitWeight = constraints[level].splitWeight;

                Node child = splitNode.getChildAt(index);
                if(child instanceof SplitNode) {
                    // Traverse to split.
                    // Possible wrong orientation solves next iteration (see above).
                    splitNode = (SplitNode)child;
                } else {
                    // There is some leaf node or null, just create new split that way.
                    SplitNode newSplit = new SplitNode(constraints[level + 1].orientation);
                    splitNode.setChildAt(index, splitWeight, newSplit);
                    splitNode = newSplit;
                }
            }
        }
        
        // Finally add the node into tree.
        if(constraints.length == 0) {
            splitNode.setChildAt(-1, 0.5D, addingNode);
        } else {
            splitNode.setChildAt(
                constraints[constraints.length - 1].index,
                constraints[constraints.length - 1].splitWeight,
                addingNode
            );
        }
        
        verifyNode(root);
        
        return true;
    }

    // XXX
    private boolean addNodeToTreeToSide(Node addingNode, Node attachNode, String side) {
        if(isInTree(addingNode)) {
            return false;
        }

        if(!isInTree(attachNode)) {
            return false;
        }
        
        if(DEBUG) {
            debugLog(""); // NOI18N
            debugLog("Inserting to side="+side); // NOI18N
        }

        // Update
        if(attachNode == root) {
            int addingIndex = (side == Constants.TOP || side == Constants.LEFT) ? 0 : -1;
            int oldIndex = addingIndex == 0 ? -1 : 0;
            // Create new branch.
            int orientation = (side == Constants.TOP || side == Constants.BOTTOM) ? Constants.VERTICAL : Constants.HORIZONTAL;
            SplitNode newSplit = new SplitNode(orientation);
            newSplit.setChildAt(addingIndex, Constants.DROP_TO_SIDE_RATIO, addingNode);
            newSplit.setChildAt(oldIndex, 1D - Constants.DROP_TO_SIDE_RATIO, attachNode);
            root = newSplit;
        } else {
            SplitNode parent = attachNode.getParent();
            if(parent == null) {
                return false;
            }

            int attachIndex = parent.getChildIndex(attachNode);
            double attachWeight = parent.getChildSplitWeight(attachNode);
            // Create new branch.
            int orientation = (side == Constants.TOP || side == Constants.BOTTOM) ? Constants.VERTICAL : Constants.HORIZONTAL;
            if( orientation == parent.getOrientation() ) {
                //reuse existing split
                if( side == Constants.BOTTOM || side == Constants.RIGHT )
                    attachIndex++;
                if( parent.getChildren().size() ==1 )
                    parent.setChildSplitWeight( attachNode, Constants.DROP_TO_SIDE_RATIO );
                parent.setChildAt( attachIndex, Constants.DROP_TO_SIDE_RATIO, addingNode );
            } else {
                //split orientation does not match, create a new sub-split
                SplitNode newSplit = new SplitNode(orientation);
                parent.removeChild(attachNode);
                int addingIndex = (side == Constants.TOP || side == Constants.LEFT) ? 0 : -1;
                int oldIndex = addingIndex == 0 ? -1 : 0;
                newSplit.setChildAt(addingIndex, Constants.DROP_TO_SIDE_RATIO, addingNode);
                newSplit.setChildAt(oldIndex, 1D - Constants.DROP_TO_SIDE_RATIO, attachNode);
                parent.setChildAt(attachIndex, attachWeight, newSplit);
            }
        }
        
        return true;
    }
    
    // XXX
    private boolean addNodeToTreeAround(Node addingNode, String side) {
        Node top = root;
        
        if(top instanceof SplitNode) {
            SplitNode parent = (SplitNode)top;
            
            if((parent.getOrientation() == Constants.VERTICAL
                && (side == Constants.TOP || side == Constants.BOTTOM))
            || (parent.getOrientation() == Constants.HORIZONTAL
                && (side == Constants.LEFT || side == Constants.RIGHT))) {
                    // Has the needed orientation (no new branch).
                double splitWeights = 0D;
                for(Iterator it = parent.getChildren().iterator(); it.hasNext(); ) {
                    Node next = (Node)it.next();
                    splitWeights += parent.getChildSplitWeight(next);
                }

                double addingSplitWeight = splitWeights * Constants.DROP_AROUND_RATIO;
                int index = (side == Constants.TOP || side == Constants.LEFT) ? 0 : -1;
                
                parent.setChildAt(index, addingSplitWeight, addingNode);
                if(addingSplitWeight > 1D) {
                    double ratio = 1D/addingSplitWeight;
                    parent.normalizeWeights(ratio);
                }
                return true;
            } else {
                // Create new branch.
                int orientation = (side == Constants.TOP || side == Constants.BOTTOM) ? Constants.VERTICAL : Constants.HORIZONTAL;
                SplitNode newSplit = new SplitNode(orientation);
                int addingIndex = (side == Constants.TOP || side == Constants.LEFT) ? 0 : -1;
                int oldIndex = addingIndex == 0 ? -1 : 0;
                newSplit.setChildAt(addingIndex, Constants.DROP_AROUND_RATIO, addingNode);
                newSplit.setChildAt(oldIndex, 1D - Constants.DROP_AROUND_RATIO, parent);
                root = newSplit;
                return true;
            }
        }
        
        SplitConstraint[] newConstraints; // Adding constraint to new mode.
        if(side == Constants.TOP) {
            newConstraints = new SplitConstraint[] {new SplitConstraint(Constants.VERTICAL, 0, Constants.DROP_AROUND_RATIO)};
        } else if(side == Constants.BOTTOM) {
            newConstraints = new SplitConstraint[] {new SplitConstraint(Constants.VERTICAL, -1, Constants.DROP_AROUND_RATIO)};
        } else if(side == Constants.LEFT) {
            newConstraints = new SplitConstraint[] {new SplitConstraint(Constants.HORIZONTAL, 0, Constants.DROP_AROUND_RATIO)};
        } else if(side == Constants.RIGHT) {
            newConstraints = new SplitConstraint[] {new SplitConstraint(Constants.HORIZONTAL, -1, Constants.DROP_AROUND_RATIO)};
        } else {
            // XXX wrong side
            return false;
        }

        return addNodeToTree(addingNode, newConstraints);
    }
    
    // XXX
    protected boolean addNodeToTreeAroundEditor(Node addingNode, String side) {
        // XXX No op here, it's impelmented in editor split subclass.
        return false;
    }

    
    /** Removes specified mode as <code>Node</code> from this model. */
    public boolean removeMode(ModeImpl mode) {
        if(mode == null) {
            throw new NullPointerException("Cannot remove null mode!");
        }

        return removeNodeFromTree(getModeNode(mode));
    }

    /** Removes node from this tree. */
    protected boolean removeNodeFromTree(Node node) {
        if(!isInTree(node)) {
            return false;
        }
        
        SplitNode parent = node.getParent();
        if(parent == null && node != root) {
            // PENDING incorrect state?
            return false;
        } 

        if(node == root) {
            root = null;
        } else {
            parent.removeChild(node);
            
            List children = parent.getChildren();

            if(children.isEmpty()) {
                // Parent split is empty, remove it too.
                if(parent == root) {
                    root = null;
                } else {                
                    SplitNode grandParent = parent.getParent();
                    grandParent.removeChild(parent);
                }
            } else if( children.size() == 1 ) {
                //the parent has only one child left - move the orphan to its grand-parent
                Node orphan = (Node)children.get( 0 );
                if( parent == root ) {
                    orphan.setParent( null );
                    root = orphan;
                } else {
                    SplitNode grandParent = parent.getParent();
                    int index = grandParent.getChildIndex( parent );
                    double weight = grandParent.getChildSplitWeight( parent );
                    grandParent.removeChild( parent );
                    grandParent.setChildAt( index, weight, orphan );
                }
            }
        }

        verifyNode(root);
        
        return true;
    }

    // PENDING Currently verifies parent-child links only.
    /** Verifies the tree structure. */
    private /*static*/ void verifyNode(Node node) {
        if(node instanceof SplitNode) {
            SplitNode splitNode = (SplitNode)node;
            for(Iterator it = splitNode.getChildren().iterator(); it.hasNext(); ) {
                Node child = (Node)it.next();

                if(child.getParent() != splitNode) {
                    Logger.getLogger(SplitSubModel.class.getName()).log(Level.WARNING, null,
                                      new java.lang.IllegalStateException("Node->" +
                                                                          child +
                                                                          " has wrong parent->" +
                                                                          child.getParent() +
                                                                          " is has to be->" +
                                                                          splitNode +
                                                                          " \nModel: " +
                                                                          toString()));
                    // Repair model.
                    child.setParent(splitNode);
                }

                verifyNode(child);
            }
        }
    }
    
    /** Resets model. Removes all nodes. */
    public void reset() {
        detachNodes(root);
        root = null;
    }

    /** Detaches nodes tree from itself. */
    private static void detachNodes(Node node) {
        if(node instanceof SplitNode) {
            SplitNode splitNode = (SplitNode)node;
            
            for(Iterator it = splitNode.getChildren().iterator(); it.hasNext(); ) {
                Node child = (Node)it.next();
                splitNode.removeChild(child);
                detachNodes(child);
            }
        }
    }

    public boolean setSplitWeights( ModelElement[] snapshots, double[] splitWeights ) {
        if( 0 == snapshots.length )
            return false;
        for( int i=0; i<snapshots.length; i++ ) {
            Node node = (Node)snapshots[i];
            if( null == node || null == node.getParent() )
                return false;
        }
        Node firstNode = (Node)snapshots[0];
        SplitNode parent = firstNode.getParent();
        
        if(parent == null || !isInTree(parent)) {
            return false;
        }
       
        boolean res = true;
        for( int i=0; i<snapshots.length; i++ ) {
            Node node = (Node)snapshots[ i ];
            double weight = splitWeights[ i ];
            SplitNode parentNode = node.getParent();
            if( null == parentNode || !isInTree( parentNode ) ) {
                res = false;
            } else {
                parentNode.setChildSplitWeight( node, weight );
            }
        }
        
        return res;
    }
    
    /** */
    public ModeStructureSnapshot.ElementSnapshot createSplitSnapshot() {
        return root == null ? null : root.createSnapshot();
    }
    
    public Set<ModeStructureSnapshot.ModeSnapshot> createSeparateSnapshots() {
        return findSeparateModeSnapshots(root);
    }
    
    private Set<ModeStructureSnapshot.ModeSnapshot> findSeparateModeSnapshots(Node node) {
        Set<ModeStructureSnapshot.ModeSnapshot> s = 
                new HashSet<ModeStructureSnapshot.ModeSnapshot>();
        if(node instanceof ModeNode) {
            ModeNode modeNode = (ModeNode)node;
            if(modeNode.isVisibleSeparate()) {
                s.add((ModeStructureSnapshot.ModeSnapshot)modeNode.createSnapshot());
            }
        } else if(node instanceof SplitNode) {
            SplitNode splitNode = (SplitNode)node;
            for(Iterator it = splitNode.getChildren().iterator(); it.hasNext(); ) {
                Node child = (Node)it.next();
                s.addAll(findSeparateModeSnapshots(child));
            }
        }
        
        return s;
    }
    
    /** Overrides superclass method, adds dump of this model tree. */
    public String toString() {
        // PENDING Better method name, some refinements possible of the dump.
        return dumpNode(root, 0, null);
    }
    
    /** Recursively dump tree content */
    private static String dumpNode(Node node, int ind, String state) {
        ind++;
        if (node == null) {
            return "NULL NODE\n";
        }
        StringBuffer buffer = new StringBuffer();
        if(state == null) {
            buffer.append("\n");
        }
        StringBuffer sb = getOffset(ind);
        if(node instanceof ModeNode) {
            buffer.append(sb);
            buffer.append("<mode-node"); // NOI18N
            buffer.append(" [" + Integer.toHexString(System.identityHashCode(node)) + "]"); // NOI18N
            buffer.append(" index=\""); // NOI18N
            if (node.getParent() != null) {
                buffer.append(node.getParent().getChildIndex(node));
            }
            buffer.append(" splitWeight="); // NOI18N
            if (node.getParent() != null) {
                buffer.append(node.getParent().getChildSplitWeight(node));
            }
            buffer.append("\""); // NOI18N
            buffer.append(" state=\""); // NOI18N
            buffer.append(state);
            buffer.append("\""); // NOI18N
            buffer.append(" name=\"" + ((ModeNode)node).getMode().getName() + "\""); // NOI18N
            buffer.append(" parent="); // NOI18N
            buffer.append(node.getParent() == null ? null : "["+Integer.toHexString(node.getParent().hashCode())+"]");
            buffer.append(" constraints=\'" + java.util.Arrays.asList(node.getNodeConstraints()) + "\"");
            buffer.append("</mode-node>\n"); // NOI18N
        } else if(node instanceof SplitNode) {
            buffer.append(sb);
            buffer.append("<split-node"); // NOI18N
            buffer.append(" [" + Integer.toHexString(System.identityHashCode(node)) + "]"); // NOI18N
            buffer.append(" index=\""); // NOI18N
            if (node.getParent() != null) {
                buffer.append(node.getParent().getChildIndex(node));
            }
            buffer.append(" splitWeight="); // NOI18N
            if (node.getParent() != null) {
                buffer.append(node.getParent().getChildSplitWeight(node));
            }
            buffer.append("\""); // NOI18N
            SplitNode split = (SplitNode) node;
            buffer.append(" state=\""); // NOI18N
            buffer.append(state);
            buffer.append("\" orientation=\""); // NOI18N
            buffer.append(split.getOrientation());
            buffer.append("\">\n");
            int j = 0;
            for(Iterator it = split.getChildren().iterator(); it.hasNext(); j++ ) {
                Node child = (Node)it.next();
                buffer.append(dumpNode(child, ind, "child["+j+"]"));
            }
            buffer.append(sb);
            buffer.append("</split-node>\n"); // NOI18N
        } else {
            // supposing it's editor mode.
            buffer.append(sb);
            buffer.append("<editor-node"); // NOI18N
            buffer.append(" [" + Integer.toHexString(System.identityHashCode(node)) + "]"); // NOI18N
            buffer.append(" index=\""); // NOI18N
            if (node.getParent() != null) {
                buffer.append(node.getParent().getChildIndex(node));
            }
            buffer.append("\""); // NOI18N
            buffer.append(" splitWeight="); // NOI18N
            if (node.getParent() != null) {
                buffer.append(node.getParent().getChildSplitWeight(node));
            }
            buffer.append(" parent="); // NOI18N
            buffer.append(node.getParent() == null ? null : "["+Integer.toHexString(node.getParent().hashCode())+"]");
            buffer.append("</editor-node>\n"); // NOI18N
        }
        return buffer.toString();
    }
    
    private static StringBuffer getOffset (int ind) {
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < ind - 1; i++) {
            sb.append("\t"); // NOI18N
        }
        return sb;
    }

    ///////////////////////////////
    // Controller updates >>
    
    public ModeImpl getModeForOriginator(ModelElement originator) {
        if(originator instanceof ModeNode) {
            return ((ModeNode)originator).getMode();
        }
        
        return null;
    }
    
    // Controller updates <<
    ///////////////////////////////
    
    private static void debugLog(String message) {
        Debug.log(SplitSubModel.class, message);
    }
    
    ////////////////////////////////////////
    /// Nodes of this tree model
    ////////////////////////////////////////
    /** Class representing one node in SplitSubModel.  */
    protected abstract static class Node implements ModelElement {
        /** Reference to parent node. */
        private SplitNode parent;

        /** Creates a new instance of TreeNode. */
        public Node() {
        }

        /** Overrides superclass method, adds info about parent node. */
        public String toString() {
            return super.toString()
                + "[parent=" + (parent == null // NOI18N
                    ? null
                    : (parent.getClass() + "@" // NOI18N
                            + Integer.toHexString(parent.hashCode())))
                + "]"; // NOI18N
        }

        /** Setter of parent property. */
        public void setParent(SplitNode parent) {
            if(this.parent == parent) {
                return;
            }

            this.parent = parent;
        }

        /** Getter of parent property. */
        public SplitNode getParent() {
            return parent;
        }

        public abstract double getResizeWeight();
        
        /** Gets constraints of this <code>Node</code>, designating
         * the path in the model */
        public SplitConstraint[] getNodeConstraints() {
            Node node = this;
            List<SplitConstraint> conList = new ArrayList<SplitConstraint>(5);
            do {
                SplitConstraint item = getConstraintForNode(node);
                if(item != null) {
                    conList.add(item);
                }
                
                node = node.getParent();
            } while(node != null);

            Collections.reverse(conList);
            return conList.toArray(new SplitConstraint[0]);
        }

        /** Gets constraint of this <code>Node</code> from parent. */
        private static SplitConstraint getConstraintForNode(Node node) {
            SplitNode parent = node.getParent();
            if(parent != null) {
                return  new SplitConstraint(
                    parent.getOrientation(),
                    parent.getChildIndex(node),
                    parent.getChildSplitWeight(node)
                );
            }

            return null;
        }
        
        //////////////////////////////////////////////////////////////////////////////
        //////////////////////////////////////////////////////////////////////////////

        /** Indicates whether component represented by this node is visible or not. */
        public boolean isVisibleInSplit() {
            return false;
        }

        /** Indicates whether there is at least one visible descendant. */
        public boolean hasVisibleDescendant() {
            return isVisibleInSplit();
        }

        /** Creates snapshot of this node. */
        public abstract ModeStructureSnapshot.ElementSnapshot createSnapshot();
        
    } // End of nested Node class.

    
    /** Class representing one split in SplitSubModel. The split is n-branched, i.e.
     * it can have more than two children. */
    protected static class SplitNode extends Node {

        /** Constraint of first node (VERTICAL or HORIZONTAL). */
        private final int orientation;
        
        // XXX some better structure needed? List is not enough since the indices may
        // not be continuous (like 0, 1, 2, 3) but even like (0, 3, 8, 9).
        /** Maps index to child node, while keeps ordering according to keys (indices). */
        private final TreeMap<Integer, Node> index2child = new TreeMap<Integer, Node>();

        /** Maps child node to its splitWeight. */
        private final Map<Node, Double> child2splitWeight = new HashMap<Node, Double>();

        /** Creates a new instance of SplitNode */
        public SplitNode(int orientation) {
            this.orientation = orientation;
        }


        /** Overrides superclass method. Adds info about dividePos, orientation,
         * first and second sub-nodes. */
        @Override
        public String toString() {
            StringBuffer sb = new StringBuffer();
            sb.append(super.toString());
            
            for(Iterator it = index2child.keySet().iterator(); it.hasNext(); ) {
                Integer index = (Integer)it.next();
                Node child = (Node)index2child.get(index);
                sb.append("child[" + index.intValue() +"]=" + child.getClass()
                    + "@" + Integer.toHexString(child.hashCode())); // NOI18N
            }
            
            return sb.toString();
        }

        /** Getter of orientation property. */
        public int getOrientation() {
            return orientation;
        }
        
        public void setChildAt(int index, double splitWeight, Node child) {
            // XXX -1 means, put it at the end.
            if(index == -1) {
                if(index2child.isEmpty()) {
                    index = 0;
                } else {
                    index = ((Integer)index2child.lastKey()).intValue() + 1;
                }
            }
            
            Integer ind = Integer.valueOf(index);
            
            Node oldChild = (Node)index2child.get(ind);
            // There are some other nodes at the index, shift them first.
            for(int i = ind.intValue() + 1; oldChild != null; i++) {
                oldChild = (Node)index2child.put(Integer.valueOf(i), oldChild);
            }

            // Finally add the new node.
            index2child.put(ind, child);
            // Also add it to child2splitWeight map
            setChildSplitWeightImpl(child, splitWeight);
            child.setParent(this);
            
            verifyChildren();
        }
        
        public Node getChildAt(int index) {
            return (Node)index2child.get(Integer.valueOf(index));
        }
        
        private void verifyChildren() {
            for(Iterator it = index2child.values().iterator(); it.hasNext(); ) {
                Node child = (Node)it.next();
                if(child.getParent() != this) {
                    Logger.getLogger(SplitSubModel.class.getName()).log(Level.WARNING, null,
                                      new java.lang.IllegalStateException("Node " +
                                                                          child +
                                                                          " is a child in split " +
                                                                          this +
                                                                          " but his parent is " +
                                                                          child.getParent() +
                                                                          ". Repairing")); // NOI18N
                    // Repair.
                    child.setParent(this);
                }
            }
        }

        public double getChildSplitWeight(Node child) {
            Double db = child2splitWeight.get(child);
            if(db != null) {
                return db.doubleValue();
            }
            
            return -1D;
        }
        
        public void setChildSplitWeight(Node child, double weight) {
            if(child == null || !child2splitWeight.containsKey(child)) {
                return;
            }
            
            setChildSplitWeightImpl(child, weight);
        }
        
        private void setChildSplitWeightImpl(Node child, double weight) {
            child2splitWeight.put(child, Double.valueOf(weight));
        }
        
        
        private void normalizeWeights(double ratio) {
            for(Map.Entry<Node, Double> entry: child2splitWeight.entrySet()) {
                double w = entry.getValue().doubleValue();
                w = ratio * w;
                entry.setValue(Double.valueOf(w));
            }
        }
        
        public int getChildIndex(Node child) {
            for(Iterator it = index2child.keySet().iterator(); it.hasNext(); ) {
                Object key = it.next();
                if(child == index2child.get(key)) {
                    return ((Integer)key).intValue();
                }
            }
            
            return -1;
        }
        
        public List<Node> getChildren() {
            return new ArrayList<Node>(index2child.values());
        }
        
        public List<Node> getVisibleChildren() {
            List<Node> l = getChildren();
            for(Iterator<Node> it = l.iterator(); it.hasNext(); ) {
                Node node = it.next();
                if(!node.hasVisibleDescendant()) {
                    it.remove();
                }
            }
            
            return l;
        }
        
        protected boolean removeChild(Node child) {
            boolean result = index2child.values().remove(child);
            child2splitWeight.remove(child);
            child.setParent(null);
            
            return result;
        }

        /** Indicates whether component represented by this node is visible or not. */
        @Override
        public boolean isVisibleInSplit() {
            int count = 0;
            for(Iterator it = index2child.values().iterator(); it.hasNext(); ) {
                Node node = (Node)it.next();
                if(node.hasVisibleDescendant()) {
                    count++;
                    // At leas two are needed so the split is showing.
                    if(count >= 2) {
                        return true;
                    }
                }
            }
            
            return false;
        }

        /** Indicates whether there is at least one visible descendant. */
        @Override
        public boolean hasVisibleDescendant() {
            for(Iterator it = index2child.values().iterator(); it.hasNext(); ) {
                Node node = (Node)it.next();
                if(node.hasVisibleDescendant()) {
                    return true;
                }
            }
            
            return false;
        }
        
        public double getResizeWeight() {
            List children = getVisibleChildren();
            double max = 0D;
            for(Iterator it = children.iterator(); it.hasNext(); ) {
                double resizeWeight = ((Node)it.next()).getResizeWeight();
                max = Math.max(max, resizeWeight);
            }
            
            return max;
        }

        public ModeStructureSnapshot.ElementSnapshot createSnapshot() {
            List<ModeStructureSnapshot.ElementSnapshot> childSnapshots = 
                    new ArrayList<ModeStructureSnapshot.ElementSnapshot>();
            Map<ModeStructureSnapshot.ElementSnapshot,Double> childSnapshot2splitWeight = 
                    new HashMap<ModeStructureSnapshot.ElementSnapshot, Double>();
            for(Node child: getChildren()) {
                ModeStructureSnapshot.ElementSnapshot childSnapshot = child.createSnapshot();
                childSnapshots.add(childSnapshot);
                childSnapshot2splitWeight.put(childSnapshot, child2splitWeight.get(child));
            }
            
            ModeStructureSnapshot.SplitSnapshot splitSnapshot = new ModeStructureSnapshot.SplitSnapshot(this, null,
                getOrientation(), childSnapshots, childSnapshot2splitWeight, getResizeWeight());
            
            // Set parent for children.
            for(ModeStructureSnapshot.ElementSnapshot snapshot: childSnapshots) {
                snapshot.setParent(splitSnapshot);
            }
            
            return splitSnapshot;
        }
    } // End of nested SplitNode class.


    /** Class representing leaf node in SplitSubModel which corresponds to Mode. */
    private static class ModeNode extends Node {

        private final ModeImpl mode;
        

        /** Creates a new instance of ModeNode */
        public ModeNode(ModeImpl mode) {
            this.mode = mode;
        }

        public ModeImpl getMode() {
            return mode;
        }

        @Override
        public boolean isVisibleInSplit() {
            if(mode.getOpenedTopComponents().isEmpty()) {
                return false;
            }

            if(mode.getState() == Constants.MODE_STATE_SEPARATED) {
                return false;
            }
            
            if(mode.getKind() == Constants.MODE_KIND_EDITOR ) {
                WindowManagerImpl wm = WindowManagerImpl.getInstance();
                if( null != wm.getEditorMaximizedMode() && wm.getEditorMaximizedMode() != mode )
                    return false;
            }

            return true;
        }
        
        public boolean isVisibleSeparate() {
            if(mode.getOpenedTopComponents().isEmpty()) {
                return false;
            }
            
            if(mode.getState() == Constants.MODE_STATE_JOINED) {
                return false;
            }
            
            return true;
        }
        
        public double getResizeWeight() {
            return 0D;
        }
        
        public ModeStructureSnapshot.ElementSnapshot createSnapshot() {
            return new ModeStructureSnapshot.ModeSnapshot(this, null, mode, getResizeWeight());
        }
    } // End of nested ModeNode class.


}

