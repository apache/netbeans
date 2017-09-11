/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
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
