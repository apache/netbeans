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

/** Used to call "Cut" popup menu item, "Edit|Cut" main menu item,
 * "org.openide.actions.CutAction" or Ctrl+X shortcut.
 * @see Action
 * @author <a href="mailto:adam.sotona@sun.com">Adam Sotona</a> */
public class CutAction extends Action {

    private static final String cutPopup = Bundle.getStringTrimmed("org.openide.actions.Bundle", "Cut");
    private static final String cutMenu = Bundle.getStringTrimmed("org.netbeans.core.ui.resources.Bundle", "Menu/Edit")
                                            + "|" + cutPopup;
    private static final KeyStroke keystroke = System.getProperty("os.name").toLowerCase().indexOf("mac") > -1 ?
            KeyStroke.getKeyStroke(KeyEvent.VK_X, KeyEvent.META_MASK) :
            KeyStroke.getKeyStroke(KeyEvent.VK_X, KeyEvent.CTRL_MASK);

    /** creates new CutAction instance */    
    public CutAction() {
        super(cutMenu, cutPopup, "org.openide.actions.CutAction", keystroke);
    }
}
