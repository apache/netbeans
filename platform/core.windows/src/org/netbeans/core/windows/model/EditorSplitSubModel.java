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


import org.netbeans.core.windows.Constants;
import org.netbeans.core.windows.ModeStructureSnapshot;
import org.netbeans.core.windows.SplitConstraint;


/**
 * Model whidh represents sub model of split modes. It adds special notion
 * of positioning of editor area. The editor area itself is represented
 * by exact instance of SplitSubModel.
 *
 * @author  Peter Zavadsky
 */
final class EditorSplitSubModel extends SplitSubModel {

    /** Only instance of EditorNode representing position
     * of editor area in this sub model. */
    private final EditorNode editorNode;


    public EditorSplitSubModel(Model parentModel, SplitSubModel editorArea) {
        super(parentModel);
        
        this.editorNode = new EditorNode(editorArea);
        
        // XXX The editor node has to be always present.
        addNodeToTree(editorNode, new SplitConstraint[0]);
    }
    

    /** Overrides superclass method to prevent removing of editor node. */
    @Override
    protected boolean removeNodeFromTree(Node node) {
        if(node == editorNode) {
            // XXX Prevents removing of editor node.
            return false;
        }
        
        return super.removeNodeFromTree(node);
    }

    public boolean setEditorNodeConstraints(SplitConstraint[] editorNodeConstraints) {
        super.removeNodeFromTree(editorNode);
        return addNodeToTree(editorNode, editorNodeConstraints);
    }
    
    public SplitConstraint[] getEditorNodeConstraints() {
        return editorNode.getNodeConstraints();
    }
    
    public SplitSubModel getEditorArea() {
        return editorNode.getEditorArea();
    }

    @Override
    public boolean setSplitWeights( ModelElement[] snapshots, double[] splitWeights) {
        if( super.setSplitWeights( snapshots, splitWeights ) ) {
            return true;
        }
        
        return getEditorArea().setSplitWeights( snapshots, splitWeights );
    }
    
    
    /** Class which represents editor area position in EditorSplitSubModel. */
    static class EditorNode extends SplitSubModel.Node {
        /** Ref to editor area. */
        private final SplitSubModel editorArea;
        
        /** Creates a new instance of EditorNode */
        public EditorNode(SplitSubModel editorArea) {
            this.editorArea = editorArea;
        }

        @Override
        public boolean isVisibleInSplit() {
            return true;
        }
        
        public SplitSubModel getEditorArea() {
            return editorArea;
        }
        
        @Override
        public double getResizeWeight() {
            return 1D;
        }
        
        @Override
        public ModeStructureSnapshot.ElementSnapshot createSnapshot() {
            return new ModeStructureSnapshot.EditorSnapshot(this, null,
                editorArea.createSplitSnapshot(), getResizeWeight());
        }
    } // End of nested EditorNode class.
    
    
    // XXX
    @Override
    protected boolean addNodeToTreeAroundEditor(Node addingNode, String side) {
        // Update
        double dropRatio = Constants.DROP_AROUND_EDITOR_RATIO;
        Node attachNode = editorNode;
        // Update
        if(attachNode == root) {
            int addingIndex = (side == Constants.TOP || side == Constants.LEFT) ? 0 : -1;
            int oldIndex = addingIndex == 0 ? -1 : 0;
            // Create new branch.
            int orientation = (side == Constants.TOP || side == Constants.BOTTOM) ? Constants.VERTICAL : Constants.HORIZONTAL;
            SplitNode newSplit = new SplitNode(orientation);
            newSplit.setChildAt(addingIndex, dropRatio, addingNode);
            newSplit.setChildAt(oldIndex, 1D - dropRatio, attachNode);
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
                parent.setChildAt( attachIndex, dropRatio, addingNode );
            } else {
                //split orientation does not match, create a new sub-split
                SplitNode newSplit = new SplitNode(orientation);
                parent.removeChild(attachNode);
                int addingIndex = (side == Constants.TOP || side == Constants.LEFT) ? 0 : -1;
                int oldIndex = addingIndex == 0 ? -1 : 0;
                newSplit.setChildAt(addingIndex, dropRatio, addingNode);
                newSplit.setChildAt(oldIndex, 1D - dropRatio, attachNode);
                parent.setChildAt(attachIndex, attachWeight, newSplit);
            }
        }
        
        return true;
    }

}

