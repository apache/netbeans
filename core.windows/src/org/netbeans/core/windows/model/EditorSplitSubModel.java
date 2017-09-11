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

