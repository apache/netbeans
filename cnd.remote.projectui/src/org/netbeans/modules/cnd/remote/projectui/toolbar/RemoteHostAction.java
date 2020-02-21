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

package org.netbeans.modules.cnd.remote.projectui.toolbar;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.HierarchyEvent;
import java.awt.event.HierarchyListener;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JComboBox;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import org.netbeans.modules.cnd.remote.actions.base.RemoteOpenActionBase;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionRegistration;
import org.openide.util.ContextAwareAction;
import org.openide.util.HelpCtx;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.actions.CallableSystemAction;
import org.openide.util.actions.Presenter;
import org.openide.util.lookup.Lookups;

/**
 * Action permitting selection of a default remote host.
 * 
 */
@ActionID(id = "org.netbeans.modules.cnd.remote.projectui.toolbar.RemoteHostAction", category = "Project")
@ActionRegistration(displayName = "#RemoteHostAction.label", lazy = false)
@ActionReference(path = "Toolbars/Remote", position = 550)
public class RemoteHostAction extends CallableSystemAction implements ContextAwareAction {

    private ActionListener performer;
    JComboBox hostListCombo;

    @SuppressWarnings("LeakingThisInConstructor")
    public RemoteHostAction() {
        putValue("noIconInMenu", true); // NOI18N
    }

    @Override
    public Component getToolbarPresenter() {
        // Do not return combo box directly; looks bad.
        JPanel toolbarPanel = new JPanel(new GridBagLayout());
        toolbarPanel.setOpaque(false); // don't interrupt JToolBar background
        toolbarPanel.setMaximumSize(new Dimension(150, 80));
        toolbarPanel.setMinimumSize(new Dimension(150, 0));
        toolbarPanel.setPreferredSize(new Dimension(150, 23));
        hostListCombo = new JComboBox();
        // XXX top inset of 2 looks better w/ small toolbar, but 1 seems to look better for large toolbar (the default):
        toolbarPanel.add(hostListCombo, new GridBagConstraints(0, 0, 1, 1, 1.0, 1.0, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new Insets(1, 6, 1, 5), 0, 0));
        
        toolbarPanel.addHierarchyListener(new HierarchyListener() {

            @Override
            public void hierarchyChanged(HierarchyEvent e) {
                if ((e.getChangeFlags() & HierarchyEvent.SHOWING_CHANGED) != 0) {
                    if (e.getChanged().isShowing()){
                        initPerformer();
                    }
                }
            }

        });
        
        return toolbarPanel;
    }

    private void initPerformer() {
        assert SwingUtilities.isEventDispatchThread();
        if (performer == null) {
            performer = Lookups.forPath("CND/Toobar/Services/RemoteHost").lookup(ActionListener.class); // NOI18N
            performer.actionPerformed(new ActionEvent(RemoteHostAction.this, 0, RemoteOpenActionBase.ACTIVATED_PSEUDO_ACTION_COMAND));
        }
    }
    
    public @Override
    HelpCtx getHelpCtx() {
        return new HelpCtx("org.netbeans.modules.cnd.remote.projectui.toolbar.RemoteHostAction"); // NOI18N
    }

    public @Override
    String getName() {
        return NbBundle.getMessage(RemoteHostAction.class, "RemoteHostAction.label"); // NOI18N
    }

    public @Override
    void performAction() {
        Toolkit.getDefaultToolkit().beep();
    }

    @Override
    public JMenuItem getMenuPresenter() {
        initPerformer();
        return ((Presenter.Menu)performer).getMenuPresenter();
    }

    @Override
    public Action createContextAwareInstance(final Lookup actionContext) {
        @SuppressWarnings("serial")
        class MyAction extends AbstractAction implements Presenter.Popup {

            public @Override
            void actionPerformed(ActionEvent e) {
                assert false;
            }

            public @Override
            JMenuItem getPopupPresenter() {
                return RemoteHostAction.this.getMenuPresenter();
            }
        }
        return new MyAction();
    }
}
