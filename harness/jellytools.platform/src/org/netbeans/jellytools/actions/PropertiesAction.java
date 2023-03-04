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

/** Used to call "Properties" popup menu item, "Window|IDE Tools|Properties" main menu item,
 * "org.openide.actions.PropertiesAction" or Ctrl+Shift+7 shortcut.
 * @see Action
 * @author <a href="mailto:adam.sotona@sun.com">Adam Sotona</a> */
public class PropertiesAction extends Action {

    private static final String propertiesPopup = Bundle.getStringTrimmed("org.openide.actions.Bundle", "Properties");
    private static final String propertiesMenu = Bundle.getStringTrimmed("org.netbeans.core.windows.resources.Bundle", "Menu/Window")
                                                + "|IDE Tools"
                                                + "|" + propertiesPopup;
    private static final KeyStroke keystroke = System.getProperty("os.name").toLowerCase().indexOf("mac") > -1 ?
            KeyStroke.getKeyStroke(KeyEvent.VK_7, KeyEvent.META_MASK|KeyEvent.SHIFT_MASK) :
            KeyStroke.getKeyStroke(KeyEvent.VK_7, KeyEvent.CTRL_MASK|KeyEvent.SHIFT_MASK);
    
    /** creates new PropertiesAction instance */    
    public PropertiesAction() {
        super(propertiesMenu, propertiesPopup, "org.openide.actions.PropertiesAction", keystroke);
    }
}
