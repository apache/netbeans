/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.netbeans.jellytools.actions;

import java.awt.event.KeyEvent;
import javax.swing.KeyStroke;
import org.netbeans.jellytools.Bundle;
import org.netbeans.jellytools.TopComponentOperator;
import org.netbeans.jellytools.nodes.Node;
import org.netbeans.jemmy.operators.ComponentOperator;

/**
 * Used to call "File|Save" main menu item,
 * "org.openide.actions.SaveAction" or Ctrl+S shortcut.
 *
 * @see Action
 * @author Jiri Skrivanek
 */
public class SaveAction extends Action {

    private static final String savePopup = Bundle.getStringTrimmed(
            "org.openide.actions.Bundle", "Save");
    private static final String saveMenu = Bundle.getStringTrimmed(
            "org.netbeans.core.ui.resources.Bundle", "Menu/File")
            + "|" + savePopup;
    private static final KeyStroke keystroke = System.getProperty("os.name").toLowerCase().indexOf("mac") > -1
            ? KeyStroke.getKeyStroke(KeyEvent.VK_S, KeyEvent.META_MASK)
            : KeyStroke.getKeyStroke(KeyEvent.VK_S, KeyEvent.CTRL_MASK);

    /** Creates new SaveAction instance. */
    public SaveAction() {
        super(saveMenu, savePopup, "org.openide.actions.SaveAction", keystroke);
    }

    /** Performs popup action Save on given component operator
     * which is activated before the action. It only accepts TopComponentOperator
     * as parameter.
     * @param compOperator operator which should be activated and saved
     * @deprecated Save menu item removed from popup on tab. Use TopComponentOperator.save() instead.
     */
    @Override
    @Deprecated
    public void performPopup(ComponentOperator compOperator) {
        throw new UnsupportedOperationException("Save menu item removed from popup on tab. Use TopComponentOperator.save() instead.");
    }

    /** Performs popup action Save on given top component operator
     * which is activated before the action. It only accepts TopComponentOperator
     * as parameter.
     * @param tco top component operator which should be activated and saved
     * @deprecated Save menu item removed from popup on tab. Use TopComponentOperator.save() instead.
     */
    @Deprecated
    public void performPopup(TopComponentOperator tco) {
        throw new UnsupportedOperationException("Save menu item removed from popup on tab. Use TopComponentOperator.save() instead.");
    }

    /** Throws UnsupportedOperationException because SaveAction doesn't have
     * popup representation on nodes.
     * @param nodes array of nodes
     */
    @Override
    public void performPopup(Node[] nodes) {
        throw new UnsupportedOperationException("SaveAction doesn't have popup representation on node.");
    }

    /** Throws UnsupportedOperationException because SaveAction doesn't have
     * popup representation on node.
     * @param node a node
     */
    @Override
    public void performPopup(Node node) {
        throw new UnsupportedOperationException("SaveAction doesn't have popup representation on node.");
    }
}
