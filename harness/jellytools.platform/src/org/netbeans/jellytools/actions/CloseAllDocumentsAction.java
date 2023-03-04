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

/** Used to call "Close All Documents" popup menu item, "Window|Close All Documents"
 * main menu, "org.netbeans.core.windows.actions.CloseAllDocumentsAction"
 * or Ctrl+Shift+F4 shortcut.
 * @see Action
 * @author Jiri.Skrivanek@sun.com
 */
public class CloseAllDocumentsAction extends Action {
    
    /** Window main menu item. */
    private static final String windowItem = Bundle.getStringTrimmed("org.netbeans.core.windows.resources.Bundle", 
                                                                    "Menu/Window");
    /** "Close All Documents" popup menu item. */
    private static final String popupPath = Bundle.getStringTrimmed("org.netbeans.core.windows.actions.Bundle",
                                                                    "LBL_CloseAllDocumentsAction");
    /** "Windows|Close All Documents" main menu item */
    private static final String menuPath = windowItem+"|"+
                            Bundle.getStringTrimmed("org.netbeans.core.windows.actions.Bundle",
                                                    "CTL_CloseAllDocumentsAction");
    
    /** Create new CloseAllDocumentsAction instance. */
    public CloseAllDocumentsAction() {
        super(menuPath, popupPath, "org.netbeans.core.windows.actions.CloseAllDocumentsAction");
    }

    /** Performs popup action Close All Documents on given component operator 
     * which is activated before the action. It only accepts TopComponentOperator
     * as parameter.
     * @param compOperator operator which should be activated
     */
    public void performPopup(ComponentOperator compOperator) {
        if(compOperator instanceof TopComponentOperator) {
            performPopup((TopComponentOperator)compOperator);
        } else {
            throw new UnsupportedOperationException(
                    "CloseAllDocumentsAction can only be called on TopComponentOperator.");
        }
    }

    /** Performs popup action Close All Documents on given top component operator 
     * which is activated before the action.
     * @param tco operator which should be activated
     */
    public void performPopup(TopComponentOperator tco) {
        tco.pushMenuOnTab(popupPath);
    }
    
    /** Throws UnsupportedOperationException because CloseAllDocumentsAction doesn't have
     * popup representation on nodes.
     * @param nodes array of nodes
     */
    public void performPopup(Node[] nodes) {
        throw new UnsupportedOperationException(
                    "CloseAllDocumentsAction doesn't have popup representation on nodes.");
    }

    /** Throws UnsupportedOperationException because CloseAllDocumentsAction doesn't have
     * representation on nodes.
     * @param nodes array of nodes
     */
    public void performAPI(Node[] nodes) {
        throw new UnsupportedOperationException(
                    "CloseAllDocumentsAction doesn't have popup representation on nodes.");
    }
    
    /** Throws UnsupportedOperationException because CloseAllDocumentsAction doesn't have
     * representation on nodes.
     * @param nodes array of nodes
     */
    public void performMenu(Node[] nodes) {
        throw new UnsupportedOperationException(
                    "CloseAllDocumentsAction doesn't have popup representation on nodes.");
    }
    
    /** Throws UnsupportedOperationException because CloseAllDocumentsAction doesn't have
     * representation on nodes.
     * @param nodes array of nodes
     */
    public void performShortcut(Node[] nodes) {
        throw new UnsupportedOperationException(
                    "CloseAllDocumentsAction doesn't have popup representation on nodes.");
    }
    
}
