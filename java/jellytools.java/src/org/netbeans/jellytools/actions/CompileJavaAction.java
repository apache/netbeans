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
import org.netbeans.jellytools.nodes.Node;

/** Used to call "Build|Compile File" main menu item, "Compile File" popup menu or F9 shortcut.
 * @see Action
 * @author <a href="mailto:adam.sotona@sun.com">Adam Sotona</a>
 */
public class CompileJavaAction extends Action {

    // Build|Compile
    private static final String compileItem = Bundle.getStringTrimmed("org.netbeans.modules.project.ui.Bundle", "Menu/BuildProject");
    private static final String compileMenu = compileItem+"|"
                                            +Bundle.getStringTrimmed("org.netbeans.modules.project.ui.actions.Bundle", "LBL_CompileSingleAction_Name");
    private static final KeyStroke keystroke = KeyStroke.getKeyStroke(KeyEvent.VK_F9, 0);
    // Compile File
    private static final String compilePopup = Bundle.getString("org.netbeans.modules.project.ui.actions.Bundle", "LBL_CompileSingleAction_Name");
    
    /** creates new CompileAction instance */    
    public CompileJavaAction() {
        super(compileMenu, compilePopup, keystroke);
    }

    public @Override void performMenu(Node node) {
        this.menuPath = compileItem + "|" +
                Bundle.getStringTrimmed("org.netbeans.modules.project.ui.actions.Bundle",
                                        "LBL_CompileSingleAction_Name",
                                        new Object[] {Integer.valueOf(1), node.getText()});
        super.performMenu(node);
    }

    public @Override void performPopup(Node node) {
        this.popupPath =
                Bundle.getStringTrimmed("org.netbeans.modules.project.ui.actions.Bundle",
                                        "LBL_CompileSingleAction_Name",
                                        new Object[] {Integer.valueOf(1), node.getText()});
        super.performPopup(node);
    }
}
