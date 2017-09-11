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

package org.netbeans.modules.git.ui.menu;

import javax.swing.Action;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import org.netbeans.modules.git.Annotator;
import static org.netbeans.modules.git.Annotator.ACTIONS_PATH_PREFIX;
import org.netbeans.modules.git.ui.actions.ContextHolder;
import org.netbeans.modules.git.ui.clone.CloneAction;
import org.netbeans.modules.git.ui.fetch.FetchAction;
import org.netbeans.modules.git.ui.fetch.FetchFromUpstreamAction;
import org.netbeans.modules.git.ui.fetch.PullAction;
import org.netbeans.modules.git.ui.fetch.PullFromUpstreamAction;
import org.netbeans.modules.git.ui.history.SearchIncomingAction;
import org.netbeans.modules.git.ui.history.SearchIncomingWithContextAction;
import org.netbeans.modules.git.ui.history.SearchOutgoingAction;
import org.netbeans.modules.git.ui.history.SearchOutgoingWithContextAction;
import org.netbeans.modules.git.ui.push.PushAction;
import org.netbeans.modules.git.ui.push.PushToUpstreamAction;
import org.netbeans.modules.versioning.spi.VCSAnnotator.ActionDestination;
import org.netbeans.modules.versioning.spi.VCSContext;
import org.netbeans.modules.versioning.util.SystemActionBridge;
import org.netbeans.modules.versioning.util.Utils;
import org.openide.awt.Actions;
import org.openide.filesystems.FileUtil;
import org.openide.util.ContextAwareAction;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.actions.SystemAction;
import org.openide.util.lookup.Lookups;

/**
 * Container menu for export actions.
 *
 * @author Ondra
 */
public final class RemoteMenu extends DynamicMenu {
    private static final String CLONE_ACTION = "org-netbeans-modules-git-ui-clone-CloneAction.instance"; //NOI18N
    private final ActionDestination dest;
    private final Lookup lkp;
    private final VCSContext ctx;

    @NbBundle.Messages({
        "CTL_MenuItem_RemoteMenu=R&emote",
        "CTL_MenuItem_RemoteMenu.popup=Remote"
    })
    public RemoteMenu (ActionDestination dest, Lookup lkp, VCSContext ctx) {
        super(dest.equals(ActionDestination.MainMenu) ? Bundle.CTL_MenuItem_RemoteMenu() : Bundle.CTL_MenuItem_RemoteMenu_popup());
        this.dest = dest;
        this.lkp = lkp;
        this.ctx = ctx;
    }

    @Override
    protected JMenu createMenu () {
        JMenu menu = new JMenu(this);
        JMenuItem item;
        if (dest.equals(ActionDestination.MainMenu)) {
            Action action = Utils.getAcceleratedAction(Annotator.ACTIONS_PATH_PREFIX + CLONE_ACTION); //NOI18N
            if(action instanceof ContextAwareAction) {
                action = ((ContextAwareAction)action).createContextAwareInstance(Lookups.singleton(new ContextHolder(null)));
            }
            if (action != null) {
                item = new JMenuItem();
                Actions.connect(item, action, false);
                menu.add(item);
                menu.addSeparator();
            }
            
            
            item = new JMenuItem();
            action = (Action) SystemAction.get(FetchFromUpstreamAction.class);
            Utils.setAcceleratorBindings(Annotator.ACTIONS_PATH_PREFIX, action);
            Actions.connect(item, action, false);
            menu.add(item);
            
            item = new JMenuItem();
            action = (Action) SystemAction.get(FetchAction.class);
            Utils.setAcceleratorBindings(Annotator.ACTIONS_PATH_PREFIX, action);
            Actions.connect(item, action, false);
            menu.add(item);
            
            item = new JMenuItem();
            action = (Action) SystemAction.get(PullFromUpstreamAction.class);
            Utils.setAcceleratorBindings(Annotator.ACTIONS_PATH_PREFIX, action);
            Actions.connect(item, action, false);
            menu.add(item);
            
            item = new JMenuItem();
            action = (Action) SystemAction.get(PullAction.class);
            Utils.setAcceleratorBindings(Annotator.ACTIONS_PATH_PREFIX, action);
            Actions.connect(item, action, false);
            menu.add(item);
            
            menu.addSeparator();
            
            item = new JMenuItem();
            action = (Action) SystemAction.get(PushToUpstreamAction.class);
            Utils.setAcceleratorBindings(Annotator.ACTIONS_PATH_PREFIX, action);
            Actions.connect(item, action, false);
            menu.add(item);
            
            item = new JMenuItem();
            action = (Action) SystemAction.get(PushAction.class);
            Utils.setAcceleratorBindings(Annotator.ACTIONS_PATH_PREFIX, action);
            Actions.connect(item, action, false);
            menu.add(item);
            
            menu.addSeparator();
            
            item = new JMenuItem();
            action = (Action) SystemAction.get(SearchIncomingAction.class);
            Utils.setAcceleratorBindings(Annotator.ACTIONS_PATH_PREFIX, action);
            Actions.connect(item, action, false);
            menu.add(item);
            
            item = new JMenuItem();
            action = (Action) SystemAction.get(SearchOutgoingAction.class);
            Utils.setAcceleratorBindings(Annotator.ACTIONS_PATH_PREFIX, action);
            Actions.connect(item, action, false);
            menu.add(item);
            
            menu.addSeparator();
            
            item = new JMenuItem();
            action = (Action) SystemAction.get(SearchIncomingWithContextAction.class);
            Utils.setAcceleratorBindings(Annotator.ACTIONS_PATH_PREFIX, action);
            Actions.connect(item, action, false);
            menu.add(item);
            
            item = new JMenuItem();
            action = (Action) SystemAction.get(SearchOutgoingWithContextAction.class);
            Utils.setAcceleratorBindings(Annotator.ACTIONS_PATH_PREFIX, action);
            Actions.connect(item, action, false);
            menu.add(item);
        } else {
            // or use Actions.forID
            Action action = (Action) FileUtil.getConfigObject(ACTIONS_PATH_PREFIX + CLONE_ACTION, Action.class);
            if (action != null) {
                item = menu.add(SystemActionBridge.createAction(action, NbBundle.getMessage(CloneAction.class, "LBL_CloneAction_PopupName"), Lookups.singleton(new ContextHolder(ctx)))); //NOI18N
                org.openide.awt.Mnemonics.setLocalizedText(item, item.getText());
                menu.addSeparator();
            }
            
            item = menu.add(SystemActionBridge.createAction(SystemAction.get(FetchFromUpstreamAction.class), NbBundle.getMessage(FetchFromUpstreamAction.class, "LBL_FetchFromUpstreamAction_PopupName"), lkp)); //NOI18N
            org.openide.awt.Mnemonics.setLocalizedText(item, item.getText());
            item = menu.add(SystemActionBridge.createAction(SystemAction.get(FetchAction.class), NbBundle.getMessage(FetchAction.class, "LBL_FetchAction_PopupName"), lkp)); //NOI18N
            org.openide.awt.Mnemonics.setLocalizedText(item, item.getText());
            item = menu.add(SystemActionBridge.createAction(SystemAction.get(PullFromUpstreamAction.class), NbBundle.getMessage(PullFromUpstreamAction.class, "LBL_PullFromUpstreamAction_PopupName"), lkp)); //NOI18N
            org.openide.awt.Mnemonics.setLocalizedText(item, item.getText());
            item = menu.add(SystemActionBridge.createAction(SystemAction.get(PullAction.class), NbBundle.getMessage(PullAction.class, "LBL_PullAction_PopupName"), lkp)); //NOI18N
            org.openide.awt.Mnemonics.setLocalizedText(item, item.getText());
            
            menu.addSeparator();
            item = menu.add(SystemActionBridge.createAction(SystemAction.get(PushToUpstreamAction.class), NbBundle.getMessage(PushToUpstreamAction.class, "LBL_PushToUpstreamAction_PopupName"), lkp)); //NOI18N
            org.openide.awt.Mnemonics.setLocalizedText(item, item.getText());
            item = menu.add(SystemActionBridge.createAction(SystemAction.get(PushAction.class), NbBundle.getMessage(PushAction.class, "LBL_PushAction_PopupName"), lkp)); //NOI18N
            org.openide.awt.Mnemonics.setLocalizedText(item, item.getText());
            
            menu.addSeparator();
            item = menu.add(SystemActionBridge.createAction(SystemAction.get(SearchIncomingAction.class), NbBundle.getMessage(SearchIncomingAction.class, "LBL_SearchIncomingAction_PopupName"), lkp)); //NOI18N
            org.openide.awt.Mnemonics.setLocalizedText(item, item.getText());
            item = menu.add(SystemActionBridge.createAction(SystemAction.get(SearchOutgoingAction.class), NbBundle.getMessage(SearchOutgoingAction.class, "LBL_SearchOutgoingAction_PopupName"), lkp)); //NOI18N
            org.openide.awt.Mnemonics.setLocalizedText(item, item.getText());
            
            menu.addSeparator();
            item = menu.add(SystemActionBridge.createAction(SystemAction.get(SearchIncomingWithContextAction.class), NbBundle.getMessage(SearchIncomingWithContextAction.class, "LBL_SearchIncomingWithContextAction_PopupName"), lkp)); //NOI18N
            org.openide.awt.Mnemonics.setLocalizedText(item, item.getText());
            item = menu.add(SystemActionBridge.createAction(SystemAction.get(SearchOutgoingWithContextAction.class), NbBundle.getMessage(SearchOutgoingWithContextAction.class, "LBL_SearchOutgoingWithContextAction_PopupName"), lkp)); //NOI18N
            org.openide.awt.Mnemonics.setLocalizedText(item, item.getText());
        }        
        return menu;
    }
}
