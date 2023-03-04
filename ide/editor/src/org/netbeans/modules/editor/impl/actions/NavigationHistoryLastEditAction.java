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

package org.netbeans.modules.editor.impl.actions;

import java.awt.event.ActionEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.logging.Logger;
import javax.swing.text.JTextComponent;
import org.netbeans.api.editor.EditorActionRegistration;
import org.netbeans.editor.BaseAction;
import org.netbeans.modules.editor.MainMenuAction;
import org.netbeans.api.editor.NavigationHistory;
import org.openide.util.NbBundle;
import org.openide.util.WeakListeners;

/**
 *
 * @author Vita Stejskal
 */
@EditorActionRegistration(
        name = "jump-list-last-edit",
        iconResource = "org/netbeans/modules/editor/resources/last_edit_location_16.png",
        shortDescription = "#NavigationHistoryLastEditAction_Tooltip_simple"
)
public final class NavigationHistoryLastEditAction extends BaseAction implements PropertyChangeListener {
    
    private static final Logger LOG = Logger.getLogger(NavigationHistoryLastEditAction.class.getName());
    
    public NavigationHistoryLastEditAction() {
        update();
        NavigationHistory nav = NavigationHistory.getEdits();
        nav.addPropertyChangeListener(WeakListeners.propertyChange(this, nav));
    }
    
    public void actionPerformed(ActionEvent evt, JTextComponent target) {
        NavigationHistory nav = NavigationHistory.getEdits();
        
        NavigationHistory.Waypoint wpt = nav.getCurrentWaypoint();
        if (wpt != null) {
            if (isStandingThere(target, wpt)) {
                wpt = nav.navigateBack();
            } else {
                wpt = null;
            }
        }
        
        if (wpt == null) {
            wpt = nav.navigateLast();
        }
        
        if (wpt != null) {
            NavigationHistoryBackAction.show(wpt);
        }
    }

    public void propertyChange(PropertyChangeEvent evt) {
        update();
    }
    
    private void update() {
        NavigationHistory nav = NavigationHistory.getEdits();
        setEnabled(nav.hasNextWaypoints() || nav.hasPreviousWaypoints() || null != nav.getCurrentWaypoint());
    }
 
    private boolean isStandingThere(JTextComponent target, NavigationHistory.Waypoint wpt) {
        return target == wpt.getComponent() && target.getCaret().getDot() == wpt.getOffset();
    }
    
    /** Back action in Go To main menu, wrapper for BaseKit.jumpListPrevAction
     */ 
    public static final class MainMenu extends MainMenuAction {
        public MainMenu () {
            super(true, null);
            postSetMenu();
        }
        
        protected String getMenuItemText () {
            return NbBundle.getBundle(NavigationHistoryLastEditAction.class).getString(
                "jump_back_main_menu_item-main-menu"); //NOI18N
        }

        protected String getActionName () {
            return "jump-list-last-edit"; //NOI18N
        }
        
//        protected KeyStroke getDefaultAccelerator () {
//            return KeyStroke.getKeyStroke(KeyEvent.VK_K, InputEvent.ALT_MASK);
//        }
        
    } // End of MainMenu class
    
}
