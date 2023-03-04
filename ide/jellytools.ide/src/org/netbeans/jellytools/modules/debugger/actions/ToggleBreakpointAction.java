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
 * Used to call "Debug|Toggle Line Breakpoint" main menu item, "Toggle Line
 * Breakpoint" popup menu item or CTRL+F8 shortcut.
 *
 * @see org.netbeans.jellytools.actions.Action
 * @author Jiri Skrivanek
 */
public class ToggleBreakpointAction extends Action {

    // "Toggle Line Breakpoint"
    private static final String toggleBreakpointItem = Bundle.getStringTrimmed(
            "org.netbeans.modules.debugger.ui.actions.Bundle",
            "CTL_Toggle_breakpoint");
    // "Debug"
    private static final String debugItem = Bundle.getStringTrimmed("org.netbeans.modules.project.ui.Bundle", "Menu/RunProject");
    // "Debug|Toggle Line Breakpoint"
    private static final String mainMenuPath = debugItem + "|" + toggleBreakpointItem;
    private static final KeyStroke keystroke = System.getProperty("os.name").toLowerCase().indexOf("mac") > -1
            ? KeyStroke.getKeyStroke(KeyEvent.VK_F8, KeyEvent.META_MASK)
            : KeyStroke.getKeyStroke(KeyEvent.VK_F8, KeyEvent.CTRL_MASK);

    /**
     * Creates new ToggleBreakpointAction instance.
     */
    public ToggleBreakpointAction() {
        super(mainMenuPath, toggleBreakpointItem, keystroke);
    }
}
