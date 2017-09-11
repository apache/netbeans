/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
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
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2009 Sun Microsystems, Inc.
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
