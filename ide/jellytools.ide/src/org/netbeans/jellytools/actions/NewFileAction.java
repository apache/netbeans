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
 * Used to call "File|New File..." main menu item, "New|Other..." popup menu
 * item, "org.netbeans.modules.project.ui.actions.NewFile" action or Ctrl+N
 * shortcut.<br> Usage:
 * <pre>
 *    new NewFileAction().performMenu();
 *    new NewFileAction().performPopup();
 *    new NewFileAction().performShortcut();
 * </pre>
 *
 * @see Action
 * @see ActionNoBlock
 * @author tb115823
 */
public class NewFileAction extends ActionNoBlock {

    /**
     * "New" popup menu item.
     */
    private static final String popupPathNew = Bundle.getStringTrimmed("org.netbeans.modules.project.ui.actions.Bundle", "LBL_NewFileAction_PopupName");
    /**
     * "Other..." popup menu sub item.
     */
    private static final String popupSubPath = Bundle.getStringTrimmed("org.netbeans.modules.project.ui.actions.Bundle", "LBL_NewFileAction_File_PopupName");
    /**
     * File|New File..." main menu path.
     */
    private static final String menuPathNewFile = Bundle.getStringTrimmed("org.netbeans.core.ui.resources.Bundle", "Menu/File")
            + "|"
            + Bundle.getStringTrimmed("org.netbeans.modules.project.ui.actions.Bundle", "LBL_NewFileAction_Name");
    private static final KeyStroke keystroke = System.getProperty("os.name").toLowerCase().indexOf("mac") > -1
            ? KeyStroke.getKeyStroke(KeyEvent.VK_N, KeyEvent.META_MASK)
            : KeyStroke.getKeyStroke(KeyEvent.VK_N, KeyEvent.CTRL_MASK);

    /**
     * Creates new NewFileAction instance.
     */
    public NewFileAction() {
        super(menuPathNewFile, popupPathNew + "|" + popupSubPath, "org.netbeans.modules.project.ui.actions.NewFile", keystroke);
    }

    /**
     * Create new NewFileAction instance with name of template for popup
     * operation (only popup mode allowed).
     *
     * @param templateName name of template shown in sub menu (e.g. "Java Main
     * Class")
     */
    public NewFileAction(String templateName) {
        super(null, popupPathNew + "|" + templateName);
    }
}
