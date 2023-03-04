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
//import org.netbeans.jellytools.ProjectsTabOperator;

/** Used to call "Find" popup menu item, "Edit|Find" main menu item,
 * "org.openide.actions.FindAction" or Ctrl+F shortcut.
 * @see Action
 * @see ActionNoBlock
 * @author <a href="mailto:adam.sotona@sun.com">Adam Sotona</a> */
public class FindAction extends ActionNoBlock {
    private static final String findPopup = Bundle.getStringTrimmed("org.openide.actions.Bundle", "Find");
    private static final String findMenu = Bundle.getStringTrimmed("org.netbeans.core.ui.resources.Bundle", "Menu/Edit")
                                            + "|"
                                            + findPopup;
    private static final KeyStroke keystroke = System.getProperty("os.name").toLowerCase().indexOf("mac") > -1 ?
            KeyStroke.getKeyStroke(KeyEvent.VK_F, KeyEvent.META_MASK) :
            KeyStroke.getKeyStroke(KeyEvent.VK_F, KeyEvent.CTRL_MASK);
 
    /** creates new FindAction instance */
    public FindAction() {
        super(findMenu, findPopup, "org.openide.actions.FindAction", keystroke);
    }
    
    /** Performs action through API. It selects projects node first.
     * @throws UnsupportedOperationException when action does not support API mode */    
    public void performAPI() {
        //new ProjectsTabOperator().tree().selectRow(0);
        super.performAPI();
    }
    
    /** Performs action through shortcut. It selects projects node first.
     * @throws UnsupportedOperationException if no shortcut is defined */
    public void performShortcut() {
        //new ProjectsTabOperator().tree().selectRow(0);
        super.performShortcut();
    }

}
