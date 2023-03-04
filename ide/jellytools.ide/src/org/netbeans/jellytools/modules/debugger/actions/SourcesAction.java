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
package org.netbeans.jellytools.modules.debugger.actions;

import java.awt.event.KeyEvent;
import javax.swing.KeyStroke;
import org.netbeans.jellytools.Bundle;
import org.netbeans.jellytools.actions.Action;

/**
 * Used to call "Window|Debugging|Sources" main menu item or Alt+Shift+8
 * shortcut.
 *
 * @see org.netbeans.jellytools.actions.Action
 * @author Jiri Skrivanek
 */
public class SourcesAction extends Action {

    private static final String menuPathSources =
            Bundle.getStringTrimmed("org.netbeans.core.windows.resources.Bundle", "Menu/Window")
            + "|" + Bundle.getStringTrimmed(
            "org.netbeans.modules.debugger.resources.Bundle",
            "CTL_Debugging_workspace")
            + "|" + Bundle.getStringTrimmed(
            "org.netbeans.modules.debugger.ui.actions.Bundle",
            "CTL_SourcesAction");
    private static final KeyStroke keystroke = KeyStroke.getKeyStroke(
            KeyEvent.VK_8,
            KeyEvent.VK_ALT + KeyEvent.VK_SHIFT);

    /**
     * Creates new SourcesAction instance.
     */
    public SourcesAction() {
        super(menuPathSources, null, keystroke);
    }
}
