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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
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
package org.netbeans.modules.performance.utilities;

import org.netbeans.jellytools.MainWindowOperator;
import org.netbeans.modules.performance.guitracker.ActionTracker;

import org.netbeans.jellytools.nodes.Node;

import org.netbeans.jemmy.operators.JPopupMenuOperator;
import org.netbeans.jemmy.operators.ComponentOperator;
import org.netbeans.jemmy.operators.JTreeOperator;

/**
 * Common test case for test of context menu invocation on various nodes in the
 * tree views.
 *
 * @author mmirilovic@netbeans.org
 */
public abstract class ValidatePopupMenuOnNodes extends PerformanceTestCase {

    protected Node dataObjectNode;

    /**
     * Creates a new instance of ValidatePopupMenuOnNodes
     *
     * @param testName test name
     */
    public ValidatePopupMenuOnNodes(String testName) {
        super(testName);
        expectedTime = UI_RESPONSE;
        WAIT_AFTER_OPEN = 300;
        track_mouse_event = ActionTracker.TRACK_MOUSE_PRESS;
    }

    /**
     * Creates a new instance of ValidatePopupMenuOnNodes
     *
     * @param testName test name
     * @param performanceDataName perf name
     */
    public ValidatePopupMenuOnNodes(String testName, String performanceDataName) {
        super(testName, performanceDataName);
        expectedTime = UI_RESPONSE;
        WAIT_AFTER_OPEN = 300;
        track_mouse_event = ActionTracker.TRACK_MOUSE_PRESS;
    }

    /**
     * Selects node whose popup menu will be tested.
     */
    @Override
    public void prepare() {
        MainWindowOperator.getDefault().pushKey(java.awt.event.KeyEvent.VK_ESCAPE);
        dataObjectNode.select();
    }

    /**
     * Directly sends mouse events causing popup menu displaying to the selected
     * node.
     * <p>
     * Using Jemmy/Jelly to call popup can cause reselecting of node and more
     * events than is desirable for this case.
     *
     * @return JPopupMenuOperator instance
     */
    @Override
    public ComponentOperator open() {
        /* it stopped to work after a while, see issue 58790
         java.awt.Point p = dataObjectNode.tree().getPointToClick(dataObjectNode.getTreePath());
         JPopupMenu menu = callPopup(dataObjectNode.tree(), p.x, p.y, java.awt.event.InputEvent.BUTTON3_MASK);
         return new JPopupMenuOperator(menu);
         */

        java.awt.Point point = dataObjectNode.tree().getPointToClick(dataObjectNode.getTreePath());
        int button = JTreeOperator.getPopupMouseButton();
        dataObjectNode.tree().clickMouse(point.x, point.y, 1, button);
        return new JPopupMenuOperator();
    }

    /**
     * Closes the popup by sending ESC key event.
     */
    @Override
    public void close() {
        //testedComponentOperator.pressKey(java.awt.event.KeyEvent.VK_ESCAPE);
        // Above sometimes fails in QUEUE mode waiting to menu become visible.
        // This pushes Escape on underlying JTree which should be always visible
        dataObjectNode.tree().pushKey(java.awt.event.KeyEvent.VK_ESCAPE);
    }
}
