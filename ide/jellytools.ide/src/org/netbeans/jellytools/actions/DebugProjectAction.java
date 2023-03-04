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

import java.awt.event.KeyEvent;
import javax.swing.KeyStroke;
import org.netbeans.jellytools.Bundle;

/**
 * Used to call "Debug" popup menu item on project's root node, "Debug|Debug
 * Main Project" main menu item or Ctrl+F5 shortcut.
 *
 * @see Action
 * @see org.netbeans.jellytools.nodes.ProjectRootNode
 * @author Adam Sotona
 * @author Jiri Skrivanek
 */
public class DebugProjectAction extends Action {

    // "Debug"
    private static final String debugProjectPopup =
            Bundle.getStringTrimmed("org.netbeans.modules.debugger.ui.actions.Bundle",
            "LBL_DebugProjectActionOnProject_Name");
    // "Debug|Debug Main Project"
    private static final String debugProjectMenu =
            Bundle.getStringTrimmed("org.netbeans.modules.project.ui.Bundle", "Menu/RunProject")
            + "|"
            + Bundle.getStringTrimmed("org.netbeans.modules.debugger.ui.actions.Bundle", "LBL_DebugMainProjectAction_Name");
    private static final KeyStroke KEYSTROKE = System.getProperty("os.name").toLowerCase().indexOf("mac") > -1
            ? KeyStroke.getKeyStroke(KeyEvent.VK_F5, KeyEvent.META_MASK)
            : KeyStroke.getKeyStroke(KeyEvent.VK_F5, KeyEvent.CTRL_MASK);

    /**
     * creates new DebugProjectAction instance
     */
    public DebugProjectAction() {
        super(debugProjectMenu, debugProjectPopup, KEYSTROKE);
    }
}
