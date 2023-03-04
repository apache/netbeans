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
import org.netbeans.jemmy.JemmyException;

/** Used to invoke help using "Help|Help Contents" menu or the F1 shortcut.
 * Can also invoke help on a property sheet from popup menu.
 *
 * @see Action
 * @author <a href="mailto:adam.sotona@sun.com">Adam Sotona</a> */
public class HelpAction extends Action {

    // String used in property sheets
    private static final String popupPath = Bundle.getString("org.openide.explorer.propertysheet.Bundle", "CTL_Help");    
    private static final KeyStroke keystroke = KeyStroke.getKeyStroke(KeyEvent.VK_F1, 0);
    private static final String helpMenu = getHelpMenu();


    /**
     * Gets the bundle message for the "Help Contents" menu item. In case it is
     * unavailable, it falls back to a hard-coded message.
     *
     * @return String message for "Help Contents" menu item
     */
    public static String getHelpMenu()
    {
        String lsRetVal;

        try
        {
            lsRetVal = Bundle.getStringTrimmed("org.netbeans.core.ui.resources.Bundle", "Menu/Help")
                                         + "|"
                                         + Bundle.getStringTrimmed("org.netbeans.modules.usersguide.Bundle",
                                         "Menu/Help/org-netbeans-modules-usersguide-master.xml");
        }
        catch (JemmyException e)
        {
            System.err.println("Warning: " + e.getMessage() + " Falling back to hard-coded message!");
            lsRetVal = "Help &Contents";
        }

        return lsRetVal;
    }

    /** Creates new HelpAction instance for master help set (Help|Contents)
     * or for generic use e.g. in property sheets.
     */
    public HelpAction() {
        super(helpMenu, popupPath, keystroke);
    }

}
