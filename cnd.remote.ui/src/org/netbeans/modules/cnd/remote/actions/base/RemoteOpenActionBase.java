/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2011 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2011 Sun Microsystems, Inc.
 */

package org.netbeans.modules.cnd.remote.actions.base;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.HierarchyEvent;
import java.awt.event.HierarchyListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import javax.swing.AbstractAction;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JMenuItem;
import javax.swing.SwingUtilities;
import org.netbeans.modules.cnd.api.remote.ServerList;
import org.netbeans.modules.cnd.api.remote.ui.ServerListUI;
import org.openide.awt.Actions;
import org.openide.awt.DynamicMenuContent;
import org.openide.util.actions.Presenter;
import org.openide.util.lookup.Lookups;

/**
 *
 */
public abstract class RemoteOpenActionBase extends AbstractAction implements DynamicMenuContent, Presenter.Toolbar, PropertyChangeListener {

    static final String ENV_KEY = "org.netbeans.modules.cnd.remote.actions.ENV"; // NOI18N
    public static final String ACTIVATED_PSEUDO_ACTION_COMAND = "performerActivated"; // NOI18N
    
    private JButton lastToolbarPresenter;
    private boolean isEnabledToolbarAction = true;
    private ActionListener peformer;
    
    public RemoteOpenActionBase(String name) {
        super(name);
        ServerList.addPropertyChangeListener(this);
        updateToolTip();
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        if (ServerList.PROP_DEFAULT_RECORD.equals(evt.getPropertyName())) {
            defaultRemoteHostChanged();
        }
    }

    protected void defaultRemoteHostChanged() {
        SwingUtilities.invokeLater(() -> updateToolTip());
    }

    protected void updateToolTip() {
    }

    @Override
    public JComponent[] getMenuPresenters() {
        initPerformer();
        return ((DynamicMenuContent)getPerformer()).getMenuPresenters();
    }

    @Override
    public JComponent[] synchMenuPresenters(JComponent[] jcs) {
        initPerformer();
        return ((DynamicMenuContent)getPerformer()).synchMenuPresenters(jcs);
    }
    
    @Override
    public JButton getToolbarPresenter() {
        lastToolbarPresenter = new JButton() {

            @Override
            public void setEnabled(boolean b) {
                super.setEnabled(isEnabledToolbarAction);
            }

            @Override
            public boolean isEnabled() {
                return isEnabledToolbarAction;
            }
        };
        lastToolbarPresenter.addHierarchyListener(new HierarchyListener() {

            @Override
            public void hierarchyChanged(HierarchyEvent e) {
                if ((e.getChangeFlags() & HierarchyEvent.SHOWING_CHANGED) != 0) {
                    if (e.getChanged().isShowing()){
                        initPerformer();
                    }
                }
            }
        });
        Actions.connect(lastToolbarPresenter, this);
        return lastToolbarPresenter;
    }

    private ActionListener getPerformer() {
        if (peformer == null) {
            initPerformer();
        }
        return peformer;
    }

    private void initPerformer() {
        assert SwingUtilities.isEventDispatchThread();
        if (peformer == null) {
            peformer = Lookups.forPath(getPerformerID()).lookup(ActionListener.class);
            peformer.actionPerformed(new ActionEvent(RemoteOpenActionBase.this, 0, ACTIVATED_PSEUDO_ACTION_COMAND));
        }
    }
    
    @Override
    public final void setEnabled(boolean enabled) {
        isEnabledToolbarAction = enabled;
        if (lastToolbarPresenter != null) {
            lastToolbarPresenter.setEnabled(enabled);
        }
    }

    @Override
    public final void actionPerformed(ActionEvent e) {
        if (e.getSource() instanceof JMenuItem) {
            JMenuItem item = (JMenuItem) e.getSource();
            Object env = item.getClientProperty(ENV_KEY);
            if (env == null) {
                ServerListUI.showServerListDialog();
            } else {
                getPerformer().actionPerformed(e);
            }
        } else {
            getPerformer().actionPerformed(e);
        }
    }

    protected abstract Icon getIcon();
    protected abstract String getPerformerID();
    protected abstract String getSubmenuTitle();
    protected abstract String getItemTitle(String record);
}
