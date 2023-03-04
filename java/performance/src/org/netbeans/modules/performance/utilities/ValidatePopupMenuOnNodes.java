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
