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

/** Used to call "Paste" popup menu item, "Edit|Paste" main menu item,
 * "org.openide.actions.PasteAction" or Ctrl+V shortcut.
 * @see Action
 * @author <a href="mailto:adam.sotona@sun.com">Adam Sotona</a> */
public class PasteAction extends Action {

    protected static final String POPUP = Bundle.getStringTrimmed("org.openide.actions.Bundle", "Paste");
    protected static final String MENU = Bundle.getStringTrimmed("org.netbeans.core.ui.resources.Bundle", "Menu/Edit")
                                            + "|" + POPUP;
    protected static final KeyStroke KEYSTROKE = System.getProperty("os.name").toLowerCase().indexOf("mac") > -1 ?
            KeyStroke.getKeyStroke(KeyEvent.VK_V, KeyEvent.META_MASK) :
            KeyStroke.getKeyStroke(KeyEvent.VK_V, KeyEvent.CTRL_MASK);
    protected static final String SYSTEMCLASS = "org.openide.actions.PasteAction";

    /** creates new PasteAction instance */    
    public PasteAction() {
        super(MENU, POPUP, SYSTEMCLASS, KEYSTROKE);
    }
}
