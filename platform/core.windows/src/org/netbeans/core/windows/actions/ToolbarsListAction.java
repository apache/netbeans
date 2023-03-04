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

package org.netbeans.core.windows.actions;

import java.awt.EventQueue;
import org.netbeans.core.windows.view.ui.toolbars.ToolbarConfiguration;
import org.openide.awt.Mnemonics;
import org.openide.awt.ToolbarPool;
import org.openide.util.NbBundle;
import org.openide.util.Utilities;
import org.openide.util.actions.Presenter;

import javax.swing.*;


/** Action that lists toolbars of current toolbar config in a submenu, the
 * same like a popup menu on toolbars area.
 *
 * @author Dafe Simonek
 */
public class ToolbarsListAction extends AbstractAction
                                implements Presenter.Menu {
    
    public ToolbarsListAction() {
        putValue(NAME,NbBundle.getMessage(ToolbarsListAction.class, "CTL_ToolbarsListAction"));
        putValue("noIconInMenu", Boolean.TRUE); // NOI18N
    }
    
    /** Perform the action. Tries the performer and then scans the ActionMap
     * of selected topcomponent.
     */
    public void actionPerformed(java.awt.event.ActionEvent ev) {
        // no operation
    }
    
    public JMenuItem getMenuPresenter() {
        String label = NbBundle.getMessage(ToolbarsListAction.class, "CTL_ToolbarsListAction");
        final JMenu menu = new JMenu(label);
        Mnemonics.setLocalizedText(menu, label);
        if (EventQueue.isDispatchThread()) {
            return ToolbarConfiguration.getToolbarsMenu(menu);
        } else {
            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    ToolbarConfiguration.getToolbarsMenu(menu);
                }
            });
            return menu;
        }
    }

}

