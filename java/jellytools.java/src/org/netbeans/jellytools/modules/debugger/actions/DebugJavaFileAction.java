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
 * Used to call "Debug|Debug File" main menu item, "Debug File" popup menu
 * item or CTRL+Shift+F5 shortcut.
 *
 * @see org.netbeans.jellytools.actions.Action
 * @author Jiri Skrivanek
 */
public class DebugJavaFileAction extends Action {

    // "Debug"
    private static final String DEBUG_ITEM = Bundle.getStringTrimmed("org.netbeans.modules.project.ui.Bundle", "Menu/RunProject");
    // "Debug File"
    private static final String POPUP_PATH =
            Bundle.getStringTrimmed("org.netbeans.modules.debugger.ui.actions.Bundle", "LBL_DebugSingleAction_Name", new Object[]{0});
    private static final KeyStroke keystroke = System.getProperty("os.name").toLowerCase().indexOf("mac") > -1
            ? KeyStroke.getKeyStroke(KeyEvent.VK_F5, KeyEvent.META_MASK | KeyEvent.SHIFT_MASK)
            : KeyStroke.getKeyStroke(KeyEvent.VK_F5, KeyEvent.CTRL_MASK | KeyEvent.SHIFT_MASK);

    /** Creates new DebugAction instance. */
    public DebugJavaFileAction() {
        super(DEBUG_ITEM + "|" + POPUP_PATH, POPUP_PATH, keystroke);
    }
}
