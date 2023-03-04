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

package org.netbeans.modules.debugger.ui.models;

import java.awt.Dialog;
import java.awt.event.ActionEvent;
import javax.swing.*;
import javax.swing.KeyStroke;

import org.netbeans.api.debugger.DebuggerManager;

import org.netbeans.api.debugger.Watch;
import org.netbeans.modules.debugger.ui.actions.AddWatchAction;
import org.netbeans.modules.debugger.ui.WatchPanel;
import org.netbeans.spi.viewmodel.Models;
import org.netbeans.spi.viewmodel.TreeModel;
import org.netbeans.spi.viewmodel.NodeActionsProvider;
import org.netbeans.spi.viewmodel.ModelListener;
import org.netbeans.spi.viewmodel.UnknownTypeException;
import org.openide.DialogDisplayer;
import org.openide.awt.Actions;
import org.openide.util.NbBundle;
import org.openide.util.HelpCtx;
import org.openide.util.actions.Presenter;


/**
 * @author   Jan Jancura
 */
public class WatchesActionsProvider implements NodeActionsProvider {
    
    static final Action NEW_WATCH_ACTION = new AbstractAction
        (NbBundle.getBundle(WatchesActionsProvider.class).getString("CTL_WatchAction_AddNew")) {
            public void actionPerformed (ActionEvent e) {
                ((AddWatchAction) AddWatchAction.findObject(AddWatchAction.class, true)).actionPerformed(null);
            }
    };
    private static final Action SHOW_PINNED_WATCHES_ACTION = new CheckBoxAction
        (NbBundle.getBundle(WatchesActionsProvider.class).getString("CTL_WatchAction_ShowPinned"),
         "showPinned") {
            @Override protected boolean isSelected() {
                return WatchesTreeModel.showPinnedWatches.isShowPinnedWatches();
            }

            @Override protected void setSelected(boolean selected) {
                WatchesTreeModel.showPinnedWatches.setShowPinnedWatches(selected);
            }
    };
    private static final Action DELETE_ALL_ACTION = new AbstractAction 
        (NbBundle.getBundle(WatchesActionsProvider.class).getString("CTL_WatchAction_DeleteAll")) {
            public void actionPerformed (ActionEvent e) {
                DebuggerManager.getDebuggerManager ().removeAllWatches ();
            }
    };
    private static final Action DELETE_ACTION = Models.createAction (
        NbBundle.getBundle(WatchesActionsProvider.class).getString("CTL_WatchAction_Delete"),
        new Models.ActionPerformer () {
            public boolean isEnabled (Object node) {
                return true;
            }
            public void perform (Object[] nodes) {
                int i, k = nodes.length;
                for (i = 0; i < k; i++)
                    ((Watch) nodes [i]).remove ();
            }
        },
        Models.MULTISELECTION_TYPE_ANY
    );
    static { 
        DELETE_ACTION.putValue (
            Action.ACCELERATOR_KEY,
            KeyStroke.getKeyStroke ("DELETE")
        );
    };
    private static final Action CUSTOMIZE_ACTION = Models.createAction (
        NbBundle.getBundle(WatchesActionsProvider.class).getString("CTL_WatchAction_Customize"),
        new Models.ActionPerformer () {
            public boolean isEnabled (Object node) {
                return true;
            }
            public void perform (Object[] nodes) {
                if (nodes[0] instanceof WatchesTreeModel.EmptyWatch) {
                    customize ((WatchesTreeModel.EmptyWatch) nodes[0]);
                } else {
                    customize ((Watch) nodes[0]);
                }
            }
        },
        Models.MULTISELECTION_TYPE_EXACTLY_ONE
    );
    
    public Action[] getActions (Object node) throws UnknownTypeException {
        if (node == TreeModel.ROOT) 
            return new Action [] {
                NEW_WATCH_ACTION,
                SHOW_PINNED_WATCHES_ACTION,
                null,
                DELETE_ALL_ACTION
            };
        if (node instanceof Watch)
            return new Action [] {
                NEW_WATCH_ACTION,
                SHOW_PINNED_WATCHES_ACTION,
                null,
                DELETE_ACTION,
                DELETE_ALL_ACTION,
                null,
                CUSTOMIZE_ACTION
            };
        if (node instanceof WatchesTreeModel.EmptyWatch) {
            return new Action [] {
                CUSTOMIZE_ACTION
            };
        }
        throw new UnknownTypeException (node);
    }
    
    public void performDefaultAction (Object node) throws UnknownTypeException {
        if (node == TreeModel.ROOT) 
            return;
        if (node instanceof Watch) {
            customize ((Watch) node);
            return;
        }
        if (node instanceof WatchesTreeModel.EmptyWatch) {
            customize ((WatchesTreeModel.EmptyWatch) node);
            return;
        }
        throw new UnknownTypeException (node);
    }

    public void addModelListener (ModelListener l) {
    }

    public void removeModelListener (ModelListener l) {
    }

    private static void customize (Watch w) {

        WatchPanel wp = new WatchPanel(w.getExpression());
        JComponent panel = wp.getPanel();

        org.openide.DialogDescriptor dd = new org.openide.DialogDescriptor(
            panel,
            NbBundle.getMessage(WatchesActionsProvider.class, "CTL_WatchDialog_Title", // NOI18N 
                                           w.getExpression())
        );
        dd.setHelpCtx(new HelpCtx("debug.add.watch"));
        Dialog dialog = DialogDisplayer.getDefault().createDialog(dd);
        dialog.setVisible(true);
        dialog.dispose();

        if (dd.getValue() != org.openide.DialogDescriptor.OK_OPTION) return;
        w.setExpression(wp.getExpression());
    }

    private static void customize (WatchesTreeModel.EmptyWatch w) {

        WatchPanel wp = new WatchPanel("");
        JComponent panel = wp.getPanel();

        org.openide.DialogDescriptor dd = new org.openide.DialogDescriptor(
            panel,
            NbBundle.getMessage(WatchesActionsProvider.class, "CTL_WatchDialog_Title", "")
        );
        dd.setHelpCtx(new HelpCtx("debug.add.watch"));
        Dialog dialog = DialogDisplayer.getDefault().createDialog(dd);
        dialog.setVisible(true);
        dialog.dispose();

        if (dd.getValue() != org.openide.DialogDescriptor.OK_OPTION) return;
        w.setExpression(wp.getExpression());
    }

    private abstract static class CheckBoxAction extends AbstractAction implements Presenter.Popup {

        private JCheckBoxMenuItem popupItem;

        CheckBoxAction(String name, String id) {
            super(name);
            putValue("WatchActionId", id);  // NOI18N
        }

        @Override
        public final void actionPerformed(ActionEvent e) {
            setSelected(!isSelected());
        }

        @Override
        public JMenuItem getPopupPresenter() {
            if (popupItem == null) {
                popupItem = new JCheckBoxMenuItem();
                popupItem.setSelected(isSelected());
                Actions.connect(popupItem, this, true);
            }
            return popupItem;
        }

        protected abstract boolean isSelected();

        protected abstract void setSelected(boolean selected);

    }

}
