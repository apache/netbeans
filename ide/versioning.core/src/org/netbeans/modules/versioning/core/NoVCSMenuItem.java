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

package org.netbeans.modules.versioning.core;

import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.event.MenuEvent;
import javax.swing.event.MenuListener;
import org.openide.awt.Mnemonics;
import org.openide.util.NbBundle;

/**
 *
 * @author Tomas Stupka
 */
class NoVCSMenuItem extends JMenu {

    private NoVCSMenuItem() { }

    static JMenu createNoVcsMenu(String name) {
        return create(name, NbBundle.getMessage(VersioningMainMenu.class, "LBL_NoneAvailable"));
    }
    
    static JMenu createInitializingMenu(String name) {
        return create(name, NbBundle.getMessage(VersioningMainMenu.class, "CTL_MenuItem_Initializing"));
    }
    
    private static JMenu create(String name, final String itemName) {
        final NoVCSMenuItem menu = new NoVCSMenuItem();
        Mnemonics.setLocalizedText(menu, name);  // NOI18N         
        menu.addMenuListener(new MenuListener() {
            @Override
            public void menuSelected(MenuEvent e) {
                if (menu.getItemCount() != 0) return;
                JMenuItem item = new JMenuItem();
                Mnemonics.setLocalizedText(item, itemName);  // NOI18N                                 
                item.setEnabled(false);
                menu.add(item);
            }
            @Override
            public void menuDeselected(MenuEvent e) { }
            @Override
            public void menuCanceled(MenuEvent e) { }
        });   
        return menu;
    }
    
}
