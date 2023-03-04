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

package org.netbeans.modules.editor.actions;

import java.awt.event.ActionEvent;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.text.JTextComponent;
import org.netbeans.spi.editor.AbstractEditorAction;
import org.netbeans.api.editor.EditorActionRegistration;
import org.netbeans.api.editor.EditorActionRegistrations;
import org.netbeans.api.editor.settings.SimpleValueNames;
import org.netbeans.api.editor.EditorActionNames;

/**
 * Toggle toolbar/lines visibility.
 *
 * @author Miloslav Metelka
 */
public final class ToggleAction extends AbstractEditorAction {
    /* Use eager initialization for these actions. Otherwise the selection ("checked") state of menu
    items will not properly track the respective Preferences value (NETBEANS-5726). */
    @EditorActionRegistrations({
        @EditorActionRegistration(
            name = EditorActionNames.toggleToolbar,
            menuPath = "View",
            menuPosition = 800,
            menuText = "#" + EditorActionNames.toggleToolbar + "_menu_text",
            preferencesKey = SimpleValueNames.TOOLBAR_VISIBLE_PROP
        ),
        @EditorActionRegistration(
            name = EditorActionNames.toggleLineNumbers,
            menuPath = "View",
            menuPosition = 850,
            menuText = "#" + EditorActionNames.toggleLineNumbers + "_menu_text",
            preferencesKey = SimpleValueNames.LINE_NUMBER_VISIBLE
        ),
        @EditorActionRegistration(
            name = EditorActionNames.toggleNonPrintableCharacters,
            menuPath = "View",
            menuPosition = 870,
            menuText = "#" + EditorActionNames.toggleNonPrintableCharacters + "_menu_text",
            preferencesKey = SimpleValueNames.NON_PRINTABLE_CHARACTERS_VISIBLE
        )
    })
    public static ToggleAction create(Map<String,?> attrs) {
        return new ToggleAction(attrs);
    }

    private static final Logger LOG = Logger.getLogger(ToggleAction.class.getName());

    private static final long serialVersionUID = 1L;

    public ToggleAction() {
    }

    private ToggleAction(Map<String,?> attrs) {
        super(attrs);
    }

    @Override
    public void actionPerformed(ActionEvent evt, JTextComponent component) {
        // Leave empty - AlwaysEnabledAction toggles state in preferences by default
        if (LOG.isLoggable(Level.FINE)) {
            LOG.fine("actionPerformed: actionName=" + actionName());
        }
    }

}
