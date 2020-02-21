/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2012 Oracle and/or its affiliates. All rights reserved.
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
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2012 Sun Microsystems, Inc.
 */
package org.netbeans.modules.cnd.remote.actions.base;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.MissingResourceException;
import javax.swing.Action;
import javax.swing.JComponent;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JSeparator;
import org.netbeans.modules.cnd.api.remote.ServerList;
import org.netbeans.modules.cnd.api.remote.ui.ServerListUI;
import org.netbeans.modules.cnd.api.remote.ServerRecord;
import org.netbeans.modules.cnd.remote.actions.OpenRemoteProjectAction;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironment;
import org.openide.awt.DynamicMenuContent;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.lookup.Lookups;

/**
 *
 */
public abstract class RemoteActionPerformer implements ActionListener, DynamicMenuContent {
    protected RemoteOpenActionBase presenter;
    private JMenuItem lastPresenter;
    
    protected abstract void actionPerformedRemote(ExecutionEnvironment env, ActionEvent e);
    
    protected final Action findAction(String path, String id) {
        Lookup lookup = Lookups.forPath(path);  
        Lookup.Template<Action> template = new Lookup.Template<>(Action.class, id, null);
        Lookup.Item<Action> item = lookup.lookupItem(template);
        if (item != null) {
            return item.getInstance();
        }
        return null;
    }

    @Override
    public final void actionPerformed(ActionEvent e) {
        if (e != null && RemoteOpenActionBase.ACTIVATED_PSEUDO_ACTION_COMAND.equals(e.getActionCommand())) {
            presenter = (RemoteOpenActionBase) e.getSource();
            return;
        }
        if (e!= null && (e.getSource() instanceof JMenuItem)) {
            JMenuItem item = (JMenuItem) e.getSource();
            Object property = item.getClientProperty(RemoteOpenActionBase.ENV_KEY);
            if (property instanceof ExecutionEnvironment) {
                actionPerformedRemote((ExecutionEnvironment)property, e);
            } else {
                ServerListUI.showServerListDialog();
            }
            return;
        } else if (e!= null && (e.getSource() instanceof Lookup.Provider)) {
            ExecutionEnvironment env = ((Lookup.Provider) e.getSource()).getLookup().lookup(ExecutionEnvironment.class);
            if (env != null) {
                actionPerformedRemote(env, e);
                return;
            }
        }
        actionPerformedRemote(ServerList.getDefaultRecord().getExecutionEnvironment(), e);
    }

    @Override
    public JComponent[] getMenuPresenters() {
        lastPresenter = createSubMenu();
        return new JComponent[] { lastPresenter };
    }

    @Override
    public JComponent[] synchMenuPresenters(JComponent[] jcs) {
        lastPresenter = createSubMenu();
        return new JComponent[] { lastPresenter };
    }
    
    private JMenuItem createSubMenu() throws MissingResourceException {
        String label = presenter.getSubmenuTitle();
        JMenu subMenu = new JMenu(label);
        subMenu.setIcon(presenter.getIcon());
        for (ServerRecord record : ServerList.getRecords()) {
            if (record.isRemote()) {
                String text = presenter.getItemTitle(record.getDisplayName());
                JMenuItem item = new JMenuItem(text);
                item.putClientProperty(RemoteOpenActionBase.ENV_KEY, record.getExecutionEnvironment());
                item.addActionListener(this);
                subMenu.add(item);
            }
        }
        if (subMenu.getItemCount() > 0) {
            subMenu.add(new JSeparator());
        }
        JMenuItem item = new JMenuItem(NbBundle.getMessage(OpenRemoteProjectAction.class, "LBL_ManagePlatforms_Name")); // NOI18N
        item.putClientProperty(RemoteOpenActionBase.ENV_KEY, null);
        item.addActionListener(this);
        subMenu.add(item);
        return subMenu;
    }
    
}
