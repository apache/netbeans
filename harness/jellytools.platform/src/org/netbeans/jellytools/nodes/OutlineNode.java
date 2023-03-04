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
package org.netbeans.jellytools.nodes;

import java.awt.Point;
import javax.swing.tree.TreePath;
import org.netbeans.jellytools.OutlineOperator;
import org.netbeans.jellytools.actions.Action;
import org.netbeans.jellytools.actions.ActionNoBlock;
import org.netbeans.jemmy.operators.JPopupMenuOperator;

/**
 * Handles nodes of the Outline component.
 *
 * Warning: Do not use yet unless really necessary!! Incomplete, under
 * development and most probably still buggy!
 *
 * @author Vojtech.Sigler@sun.com
 */
public final class OutlineNode {

    private OutlineOperator _outline;
    private TreePath _treePath;

    public OutlineNode(OutlineOperator irOutlineOp, TreePath irTreePath) {
        if (irOutlineOp == null) {
            throw new IllegalArgumentException("OutlineOperator argument cannot be null.");
        }

        if (irTreePath == null) {
            throw new IllegalArgumentException("TreePath argument cannot be null.");
        }

        _outline = irOutlineOp;
        _treePath = irTreePath;
    }

    public OutlineNode(OutlineNode irParentNode, String isPath) {
        _outline = irParentNode.getOutline();
        _treePath = getOutline().findPath(irParentNode.getTreePath(), isPath);
    }

    public OutlineNode(OutlineOperator irOutline, String isPath) {
        _outline = irOutline;
        _treePath = getOutline().findPath(isPath);
    }

    /**
     * Gets the underlying OutlineOperator.
     *
     * @return
     */
    public OutlineOperator getOutline() {
        return _outline;
    }

    /**
     * Gets tree path in the Outline tree.
     *
     * @return
     */
    public TreePath getTreePath() {
        return _treePath;
    }

    /**
     * Calls popup menu on this node.
     *
     * @return
     */
    public JPopupMenuOperator callPopup() {
        Point lrPopupPoint = getOutline().getLocationForPath(getTreePath());

        //y is for row, x for column
        return new JPopupMenuOperator(getOutline().callPopupOnCell(lrPopupPoint.y, lrPopupPoint.x));
    }

    /**
     * Expands the node.
     */
    public void expand() {
        getOutline().expandPath(getTreePath());
        getOutline().waitExpanded(getTreePath());
    }

    /**
     * Selects the node.
     */
    public void select() {
        getOutline().selectPath(getTreePath());
    }

    /**
     * Tests whether the node is a leaf.
     *
     * @return
     */
    public boolean isLeaf() {
        Object lrLastElem = getTreePath().getLastPathComponent();
        return getOutline().getOutline().getOutlineModel().getChildCount(lrLastElem) < 1;
    }

    /**
     * performs action on node through main menu
     *
     * @param menuPath main menu path of action
     */
    public void performMenuAction(String menuPath) {
        new Action(menuPath, null).performMenu(this);
    }

    /**
     * performs action on node through popup menu
     *
     * @param popupPath popup menu path of action
     */
    public void performPopupAction(String popupPath) {
        new Action(null, popupPath).performPopup(this);
    }

    /**
     * performs action on node through API menu
     *
     * @param systemActionClass String class name of SystemAction (use null
     * value if API mode is not supported)
     */
    public void performAPIAction(String systemActionClass) {
        new Action(null, null, systemActionClass).performAPI(this);
    }

    /**
     * performs action on node through main menu
     *
     * @param menuPath main menu path of action
     */
    public void performMenuActionNoBlock(String menuPath) {
        new ActionNoBlock(menuPath, null).performMenu(this);
    }

    /**
     * performs action on node through popup menu
     *
     * @param popupPath popup menu path of action
     */
    public void performPopupActionNoBlock(String popupPath) {
        new ActionNoBlock(null, popupPath).performPopup(this);
    }

    /**
     * performs action on node through API menu
     *
     * @param systemActionClass String class name of SystemAction (use null
     * value if API mode is not supported)
     */
    public void performAPIActionNoBlock(String systemActionClass) {
        new ActionNoBlock(null, null, systemActionClass).performAPI(this);
    }
}
