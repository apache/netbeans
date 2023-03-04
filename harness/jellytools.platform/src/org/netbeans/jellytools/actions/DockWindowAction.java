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
import org.netbeans.jemmy.operators.ComponentOperator;

/** Used to call "Dock Window" popup menu item, "Window|Dock Window" main menu item or
 * "org.netbeans.core.windows.actions.UnDockWindowAction".
 *
 * @see Action
 * @see org.netbeans.jellytools.TopComponentOperator
 * @author Vojtech.Sigler@sun.com
 */
public class DockWindowAction extends Action {

    /** Window main menu item. */
    private static final String windowItem = Bundle.getStringTrimmed("org.netbeans.core.windows.resources.Bundle",
                                                                    "Menu/Window");
    private static final String configureWindowItem = Bundle.getStringTrimmed("org.netbeans.core.windows.resources.Bundle",
            "Menu/Window/ConfigureWindow");
    /** "Window|Dock Window"" main menu item. */
    private static final String menuPath = windowItem
                                            + "|"
                                            + configureWindowItem
                                            + "|"
                                            + Bundle.getStringTrimmed("org.netbeans.core.windows.actions.Bundle",
                                            "CTL_UndockWindowAction_Dock");

    /** "Dock Window" popup menu item. */
    private static final String popupPath = Bundle.getStringTrimmed("org.netbeans.core.windows.actions.Bundle",
                                            "CTL_UndockWindowAction_Dock");

    /** Create new DockWindowAction instance. */
    public DockWindowAction() {
        super(menuPath, popupPath, "org.netbeans.core.windows.actions.UndockWindowAction");
    }

    /** Performs popup action "Dock Window" on given component operator
     * which is activated before the action. It only accepts TopComponentOperator
     * as parameter.
     * @param compOperator operator which should be activated and docked
     */
    public void performPopup(ComponentOperator compOperator) {
        if(compOperator instanceof TopComponentOperator) {
            performPopup((TopComponentOperator)compOperator);
        } else {
            throw new UnsupportedOperationException(
                    "DockWindowAction can only be called on TopComponentOperator.");
        }
    }

    /** Performs popup action "Dock Window" on given top component operator
     * which is activated before the action. It only accepts TopComponentOperator
     * as parameter.
     * @param tco top component operator which should be activated and docked
     */
    public void performPopup(TopComponentOperator tco) {
        tco.pushMenuOnTab(popupPath);
    }

    /** Throws UnsupportedOperationException because DockWindowAction doesn't have
     * popup representation on nodes.
     * @param nodes array of nodes
     */
    public void performPopup(Node[] nodes) {
        throw new UnsupportedOperationException(
                    "DockWindowAction doesn't have popup representation on nodes.");
    }

    /** Throws UnsupportedOperationException because DockWindowAction doesn't have
     * representation on nodes.
     * @param nodes array of nodes
     */
    public void performAPI(Node[] nodes) {
        throw new UnsupportedOperationException(
                    "DockWindowAction doesn't have popup representation on nodes.");
    }

    /** Throws UnsupportedOperationException because DockWindowAction doesn't have
     * representation on nodes.
     * @param nodes array of nodes
     */
    public void performMenu(Node[] nodes) {
        throw new UnsupportedOperationException(
                    "DockWindowAction doesn't have popup representation on nodes.");
    }

    /** Throws UnsupportedOperationException because DockWindowAction doesn't have
     * representation on nodes.
     * @param nodes array of nodes
     */
    public void performShortcut(Node[] nodes) {
        throw new UnsupportedOperationException(
                    "DockWindowAction doesn't have popup representation on nodes.");
    }

}
