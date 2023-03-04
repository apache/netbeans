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
package org.netbeans.jellytools.actions;

import org.netbeans.jellytools.Bundle;
import org.netbeans.jellytools.TopComponentOperator;
import org.netbeans.jellytools.nodes.Node;
import org.netbeans.jemmy.QueueTool;
import org.netbeans.jemmy.operators.ComponentOperator;

// it is tested in MaximizeWindowActionTest
/**
 * Used to call "Maximize" popup menu item, "Window|Configure Window|Maximize" main
 * menu item, shortcut or restore window by IDE API. There is the same menu item
 * to maximize and restore window, just check box informs you about current status.
 *
 * @see Action
 * @see org.netbeans.jellytools.TopComponentOperator
 * @author Jiri Skrivanek
 */
public class RestoreWindowAction extends Action {

    /**
     * "Window" main menu item.
     */
    private static final String windowItem =
            Bundle.getStringTrimmed("org.netbeans.core.windows.resources.Bundle", "Menu/Window");
    /**
     * "Configure Window"
     */
    private static final String configureWindowItem =
            Bundle.getStringTrimmed("org.netbeans.core.windows.resources.Bundle", "Menu/Window/ConfigureWindow");
    /**
     * "Window|Configure Window|Maximize"
     */
    private static final String windowMaximizePath =
            windowItem + "|"
            + configureWindowItem + "|"
            + Bundle.getStringTrimmed("org.netbeans.core.windows.actions.Bundle",
            "CTL_MaximizeWindowAction");
    /**
     * "Maximize"
     */
    private static final String popupPathMaximize =
            Bundle.getStringTrimmed("org.netbeans.core.windows.actions.Bundle", "CTL_MaximizeWindowAction");

    /** Creates new instance of RestoreWindowAction. */
    public RestoreWindowAction() {
        // action MaximizeWindowAction is no called but it is used to get shortcut
        super(windowMaximizePath, popupPathMaximize, "org.netbeans.core.windows.actions.MaximizeWindowAction");
    }

    /** Performs popup action Restore Window on given component operator 
     * which is activated before the action. It only accepts TopComponentOperator
     * as parameter.
     * @param compOperator operator which should be activated and restored
     */
    @Override
    public void performPopup(ComponentOperator compOperator) {
        if (compOperator instanceof TopComponentOperator) {
            performPopup((TopComponentOperator) compOperator);
        } else {
            throw new UnsupportedOperationException("RestoreWindowAction can only be called on TopComponentOperator.");
        }
    }

    /** Performs popup action Restore Window on given top component operator 
     * which is activated before the action.
     * @param tco top component operator which should be activated and maximized
     */
    public void performPopup(TopComponentOperator tco) {
        tco.pushMenuOnTab(popupPathMaximize);
    }

    /** Restore active top component by IDE API.*/
    @Override
    public void performAPI() {
        // run in dispatch thread
        new QueueTool().invokeSmoothly(new Runnable() {

            @Override
            public void run() {
                AttachWindowAction.callWindowManager("switchMaximizedMode", new Object[]{null});
            }
        });
    }

    /** Throws UnsupportedOperationException because RestoreWindowAction doesn't have
     * representation on nodes.
     * @param nodes array of nodes
     */
    @Override
    public void performAPI(Node[] nodes) {
        throw new UnsupportedOperationException(
                "RestoreWindowAction doesn't have popup representation on nodes.");
    }

    /** Throws UnsupportedOperationException because RestoreWindowAction doesn't have
     * representation on nodes.
     * @param nodes array of nodes
     */
    @Override
    public void performMenu(Node[] nodes) {
        throw new UnsupportedOperationException("RestoreWindowAction doesn't have popup representation on nodes.");
    }

    /** Throws UnsupportedOperationException because RestoreWindowAction doesn't have
     * popup representation on nodes.
     * @param nodes array of nodes
     */
    @Override
    public void performPopup(Node[] nodes) {
        throw new UnsupportedOperationException("RestoreWindowAction doesn't have popup representation on nodes.");
    }

    /** Throws UnsupportedOperationException because RestoreWindowAction doesn't have
     * representation on nodes.
     * @param nodes array of nodes
     */
    @Override
    public void performShortcut(Node[] nodes) {
        throw new UnsupportedOperationException("RestoreWindowAction doesn't have popup representation on nodes.");
    }
}
