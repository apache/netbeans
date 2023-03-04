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
package org.netbeans.test.subversion.operators.actions;

import java.awt.event.KeyEvent;
import org.netbeans.jellytools.MainWindowOperator;
import org.netbeans.jellytools.actions.ActionNoBlock;
import org.netbeans.jemmy.EventTool;
import org.netbeans.jemmy.operators.JMenuItemOperator;

/**
 * Common ancestor for subversion actions.
 *
 * @author Jiri Skrivanek
 */
public class SvnAction extends ActionNoBlock {

    /**
     * "Team" menu item.
     */
    public static final String TEAM_ITEM = "Team";
    /**
     * "Subversion" menu item.
     */
    public static final String SVN_ITEM = "Subversion";

    /**
     *
     */
    protected SvnAction(String menuPath, String popupPath) {
        super(menuPath, popupPath);
    }

    @Override
    public void performMenu() {
        // #221165 - prevent Initializing... menu item shown forever
        JMenuItemOperator[] items;
        do {
            items = MainWindowOperator.getDefault().menuBar().showMenuItems(TEAM_ITEM + "|" + SVN_ITEM);
            // push Escape key to ensure there is no open menu
            MainWindowOperator.getDefault().pushKey(KeyEvent.VK_ESCAPE);
            new EventTool().waitNoEvent(100);
        } while (items != null && items.length > 0 && items[0].getText().equals("Initializing..."));
        super.performMenu();
    }
}
