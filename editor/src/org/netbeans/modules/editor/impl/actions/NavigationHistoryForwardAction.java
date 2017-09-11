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

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.util.List;
import java.util.logging.Logger;
import javax.swing.Action;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JPopupMenu;
import javax.swing.text.JTextComponent;
import javax.swing.text.TextAction;
import org.netbeans.editor.BaseKit;
import org.netbeans.api.editor.NavigationHistory;
import org.openide.awt.DropDownButtonFactory;
import org.openide.util.ContextAwareAction;
import org.openide.util.ImageUtilities;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.WeakListeners;
import org.openide.util.actions.Presenter;

/**
 *
 * @author Vita Stejskal
 */
public final class NavigationHistoryForwardAction extends TextAction implements ContextAwareAction, Presenter.Toolbar,  PropertyChangeListener {
    
    private static final Logger LOG = Logger.getLogger(NavigationHistoryForwardAction.class.getName());
    
    private final Reference<JTextComponent> componentRef;
    private final NavigationHistory.Waypoint waypoint;
    private final JPopupMenu popupMenu;
    private boolean updatePopupMenu = false;
    
    public NavigationHistoryForwardAction() {
        this(null, null, null);
    }

    private NavigationHistoryForwardAction(JTextComponent component, NavigationHistory.Waypoint waypoint, String actionName) {
        super(BaseKit.jumpListNextAction);
        
        this.componentRef = new WeakReference<>(component);
        this.waypoint = waypoint;
        
        putValue("menuText", NbBundle.getMessage(NavigationHistoryBackAction.class,
                "NavigationHistoryForwardAction_Tooltip_simple")); //NOI18N

        if (waypoint != null) {
            putValue(NAME, actionName);
            putValue(SHORT_DESCRIPTION, NbBundle.getMessage(NavigationHistoryBackAction.class, 
                "NavigationHistoryForwardAction_Tooltip", actionName)); //NOI18N
            this.popupMenu = null;
        } else if (component != null) {
            putValue(SMALL_ICON, ImageUtilities.loadImageIcon("org/netbeans/modules/editor/resources/navigate_forward_16.png", false)); //NOI18N
            this.popupMenu = new JPopupMenu() {

                @Override
                public int getComponentCount() {
                    if (updatePopupMenu) {
                        updatePopupMenu = false;    
                        popupMenu.removeAll();

                        int count = 0;
                        String lastFileName = null;
                        NavigationHistory.Waypoint lastWpt = null;
                        List<NavigationHistory.Waypoint> waypoints = NavigationHistory.getNavigations().getNextWaypoints();
                        for (int i = 0; i < waypoints.size(); i++) {
                            NavigationHistory.Waypoint wpt = waypoints.get(i);
                            String fileName = NavigationHistoryBackAction.getWaypointName(wpt);

                            if (fileName == null) {
                                continue;
                            }

                            if (lastFileName == null || !fileName.equals(lastFileName)) {
                                JTextComponent c = componentRef.get();
                                if (lastFileName != null && c != null) {
                                    popupMenu.add(new NavigationHistoryForwardAction(c, lastWpt,
                                            count > 1 ? lastFileName + ":" + count : lastFileName)); //NOI18N
                                }
                                lastFileName = fileName;
                                lastWpt = wpt;
                                count = 1;
                            } else {
                                count++;
                            }
                        }

                        JTextComponent c = componentRef.get();
                        if (lastFileName != null && c != null) {
                            popupMenu.add(new NavigationHistoryForwardAction(c, lastWpt,
                                    count > 1 ? lastFileName + ":" + count : lastFileName)); //NOI18N
                        }
                    }
                    return super.getComponentCount(); //To change body of generated methods, choose Tools | Templates.
                }  
            };
            update();
            NavigationHistory nav = NavigationHistory.getNavigations();
            nav.addPropertyChangeListener(WeakListeners.propertyChange(this, nav));
        } else {
            this.popupMenu = null;
            putValue(SHORT_DESCRIPTION, NbBundle.getMessage(NavigationHistoryForwardAction.class, 
                "NavigationHistoryForwardAction_Tooltip_simple")); //NOI18N
        }
    }
    
    @Override
    public Action createContextAwareInstance(Lookup actionContext) {
        JTextComponent c = NavigationHistoryBackAction.findComponent(actionContext);
        return new NavigationHistoryForwardAction(c, null, null);
    }

    @Override
    public void actionPerformed(ActionEvent evt) {
        NavigationHistory history = NavigationHistory.getNavigations();
        NavigationHistory.Waypoint wpt = waypoint != null ? 
            history.navigateTo(waypoint) : history.navigateForward();
        
        if (wpt != null) {
            NavigationHistoryBackAction.show(wpt);
        }
    }

    @Override
    public Component getToolbarPresenter() {
        if (popupMenu != null) {
            JButton button = DropDownButtonFactory.createDropDownButton(
                (ImageIcon) getValue(SMALL_ICON), 
                popupMenu
            );
            button.putClientProperty("hideActionText", Boolean.TRUE); //NOI18N
            button.setAction(this);
            return button;
        } else {
            return new JButton(this);
        }
    }
    
    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        update();
    }
    
    private void update() {
        List<NavigationHistory.Waypoint> waypoints = NavigationHistory.getNavigations().getNextWaypoints();

        // Update popup menu
        if (popupMenu != null) {
            updatePopupMenu = true;
        }
        
        // Set the short description
        if (!waypoints.isEmpty()) {
            NavigationHistory.Waypoint wpt = waypoints.get(0);
            String fileName = NavigationHistoryBackAction.getWaypointName(wpt);
            if (fileName != null) {
                putValue(SHORT_DESCRIPTION, NbBundle.getMessage(NavigationHistoryForwardAction.class, 
                    "NavigationHistoryForwardAction_Tooltip", fileName)); //NOI18N
            } else {
                putValue(SHORT_DESCRIPTION, NbBundle.getMessage(NavigationHistoryForwardAction.class, 
                    "NavigationHistoryForwardAction_Tooltip_simple")); //NOI18N
            }
            setEnabled(true);
        } else {
            putValue(SHORT_DESCRIPTION, NbBundle.getMessage(NavigationHistoryForwardAction.class, 
                "NavigationHistoryForwardAction_Tooltip_simple")); //NOI18N
            setEnabled(false);
        }
    }
}
