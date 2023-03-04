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

/** Used to call "Float" popup menu item or
 * "org.netbeans.core.windows.actions.UndockWindowAction". 
 *
 * @see Action
 * @see org.netbeans.jellytools.TopComponentOperator
 * @author Vojtech Sigler
 * @author Jiri Skrivanek
 */
public class UndockWindowAction extends Action {

    /** Window main menu item. */
    private static final String windowItem = Bundle.getStringTrimmed("org.netbeans.core.windows.resources.Bundle",
            "Menu/Window");
    private static final String configureWindowItem = Bundle.getStringTrimmed("org.netbeans.core.windows.resources.Bundle",
            "Menu/Window/ConfigureWindow");
    /** "Window|Configure Window|Float" main menu item. */
    private static final String floatMenuPath = windowItem
            + "|"
            + configureWindowItem
            + "|"
            + Bundle.getStringTrimmed("org.netbeans.core.windows.actions.Bundle",
            "CTL_UndockWindowAction");
    /** "Undock Window" popup menu item. */
    private static final String floatPopupPath = Bundle.getStringTrimmed("org.netbeans.core.windows.actions.Bundle",
            "CTL_UndockWindowAction");

    /** Create new UndockWindowAction instance. */
    public UndockWindowAction() {
        super(floatMenuPath, floatPopupPath, "org.netbeans.core.windows.actions.UndockWindowAction");
    }

    /** Performs popup action "Undock Window" on given component operator
     * which is activated before the action. It only accepts TopComponentOperator
     * as parameter.
     * @param compOperator operator which should be activated and undocked
     */
    @Override
    public void performPopup(ComponentOperator compOperator) {
        if (compOperator instanceof TopComponentOperator) {
            performPopup((TopComponentOperator) compOperator);
        } else {
            throw new UnsupportedOperationException(
                    "UndockWindowAction can only be called on TopComponentOperator.");
        }
    }

    /** Performs popup action "Undock Window" on given top component operator
     * which is activated before the action. It only accepts TopComponentOperator
     * as parameter.
     * @param tco top component operator which should be activated and undocked
     */
    public void performPopup(TopComponentOperator tco) {
        tco.pushMenuOnTab(floatPopupPath);
    }

    /** Throws UnsupportedOperationException because UndockWindowAction doesn't have
     * popup representation on nodes.
     * @param nodes array of nodes
     */
    @Override
    public void performPopup(Node[] nodes) {
        throw new UnsupportedOperationException(
                "UndockWindowAction doesn't have popup representation on nodes.");
    }

    /** Throws UnsupportedOperationException because UndockWindowAction doesn't have
     * representation on nodes.
     * @param nodes array of nodes
     */
    @Override
    public void performAPI(Node[] nodes) {
        throw new UnsupportedOperationException(
                "UndockWindowAction doesn't have popup representation on nodes.");
    }

    /** Throws UnsupportedOperationException because UndockWindowAction doesn't have
     * representation on nodes.
     * @param nodes array of nodes
     */
    @Override
    public void performMenu(Node[] nodes) {
        throw new UnsupportedOperationException(
                "UndockWindowAction doesn't have popup representation on nodes.");
    }

    /** Throws UnsupportedOperationException because UndockWindowAction doesn't have
     * representation on nodes.
     * @param nodes array of nodes
     */
    @Override
    public void performShortcut(Node[] nodes) {
        throw new UnsupportedOperationException(
                "UndockWindowAction doesn't have popup representation on nodes.");
    }
}
