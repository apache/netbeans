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
import org.netbeans.jellytools.nodes.Node;

/** Used to call "Window|Documents" main menu item,
 * "org.netbeans.core.windows.actions.DocumentsAction" or
 * shortcut Shift+F4.
 * @see ActionNoBlock
 * @author Jiri.Skrivanek@sun.com
 */
public class DocumentsAction extends ActionNoBlock {

    /** Window main menu item. */
    private static final String windowItem = Bundle.getStringTrimmed("org.netbeans.core.windows.resources.Bundle",
                                                                     "Menu/Window");

    /** "Documents..." main menu item. */
    private static final String menuPath = windowItem
                                            + "|"
                                            + Bundle.getStringTrimmed("org.netbeans.core.windows.actions.Bundle",
                                                                      "CTL_DocumentsAction");

    /** Create new DocumentsAction instance. */
    public DocumentsAction() {
        super(menuPath, null, "org.netbeans.core.windows.actions.DocumentsAction");
    }
 
    /** Throws UnsupportedOperationException because DocumentsAction doesn't have
     * popup representation on nodes.
     * @param nodes array of nodes
     */
    public void performPopup(Node[] nodes) {
        throw new UnsupportedOperationException(
                    "DocumentsAction doesn't have popup representation on nodes.");
    }

    /** Throws UnsupportedOperationException because DocumentsAction doesn't have
     * representation on nodes.
     * @param nodes array of nodes
     */
    public void performAPI(Node[] nodes) {
        throw new UnsupportedOperationException(
                    "DocumentsAction doesn't have popup representation on nodes.");
    }
    
    /** Throws UnsupportedOperationException because DocumentsAction doesn't have
     * representation on nodes.
     * @param nodes array of nodes
     */
    public void performMenu(Node[] nodes) {
        throw new UnsupportedOperationException(
                    "DocumentsAction doesn't have popup representation on nodes.");
    }
    
    /** Throws UnsupportedOperationException because DocumentsAction doesn't have
     * representation on nodes.
     * @param nodes array of nodes
     */
    public void performShortcut(Node[] nodes) {
        throw new UnsupportedOperationException(
                    "DocumentsAction doesn't have popup representation on nodes.");
    }
    
}
