/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.netbeans.modules.cnd.makeproject.ui;

import java.awt.event.ActionEvent;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import javax.swing.Action;
import javax.swing.SwingUtilities;
import org.netbeans.modules.cnd.makeproject.api.configurations.Folder;
import org.netbeans.modules.cnd.makeproject.api.configurations.MakeConfigurationDescriptor;
import org.openide.actions.DeleteAction;
import org.openide.actions.RenameAction;
import org.openide.cookies.SaveCookie;
import org.openide.loaders.DataObject;
import org.openide.nodes.Node;
import org.openide.util.Exceptions;
import org.openide.util.HelpCtx;
import org.openide.util.Lookup;
import org.openide.util.actions.CallbackSystemAction;
import org.openide.util.actions.NodeAction;
import org.openide.util.actions.SystemAction;
import org.openide.util.lookup.AbstractLookup;
import org.openide.util.lookup.InstanceContent;

/**
 *
 */
final class NodeActionFactory {
    private static final boolean DOWNLOAD_ACTION = Boolean.getBoolean("cnd.remote.download.project.action"); // NOI18N
    private static StandardNodeAction renameAction = null;
    private static StandardNodeAction deleteAction = null;

    static StandardNodeAction createRenameAction() {
        if (renameAction == null) {
            renameAction = new RenameNodeAction();
        }
        return renameAction;
    }

    static StandardNodeAction createDeleteAction() {
        if (deleteAction == null) {
            deleteAction = new DeleteNodeAction();
        }
        return deleteAction;
    }

    static void addSyncActions(List<Action> actions) {
        actions.add(RemoteSyncActions.createUploadAction());
        if (DOWNLOAD_ACTION) {
            actions.add(RemoteSyncActions.createDownloadAction());
        }
    }

    static Action[] insertSyncActions(Action[] actions, Class<?> insertAfter) {
        Action[] result = actions;
        if (DOWNLOAD_ACTION) {
            result = insertAfter(result, new Action[]{RemoteSyncActions.createDownloadAction()}, insertAfter);
        }
        result = insertAfter(result, new Action[]{RemoteSyncActions.createUploadAction()}, insertAfter);
        return result;
    }

    static Action[] insertAfter(Action[] actions, Action[] actionsToInsert, Class<?> insertAfter) {
        if (actionsToInsert == null || actionsToInsert.length == 0) {
            return actions;
        }
        int insertPos = - 1;
        for (int i = 0; i < actions.length; i++) {
            if (actions[i] != null) {
                if (actions[i].getClass().equals(insertAfter)) {
                    insertPos = i + 1;
                    break;
                }
            }
        }
        if (insertPos < 0) {
            return actions;
        } else {
            Action[] newActions = new Action[actions.length + actionsToInsert.length];
            System.arraycopy(actions, 0, newActions, 0, insertPos);
            int rest = actions.length - insertPos;
            int newIndex = insertPos;
            for (Action action : actionsToInsert) {
                newActions[newIndex++] = action;
            }
            if (rest > 0) {
                System.arraycopy(actions, insertPos, newActions, newIndex, rest);
            }
            return newActions;
        }
    }

    static Action[] insertAfter(Action[] actions, Action[] actionsToInsert) {
        if (actionsToInsert == null || actionsToInsert.length == 0) {
            return actions;
        }
        Action[] newActions = new Action[actions.length + actionsToInsert.length];
        System.arraycopy(actions, 0, newActions, 0, actions.length);
        System.arraycopy(actionsToInsert, 0, newActions, actions.length, actionsToInsert.length);
        return newActions;
    }

    // this class should be static, because successors are shared classes
    // and accessing MakeLogicalViewProvider.this would use wrong one!
    static class StandardNodeAction extends NodeAction {

        private final SystemAction systemAction;

        public StandardNodeAction(SystemAction systemAction) {
            this.systemAction = systemAction;
        }

        @Override
        protected void performAction(Node[] activatedNodes) {
            if (activatedNodes.length == 0) {
                return;
            }
            final List<MakeConfigurationDescriptor> projects = new ArrayList<>();
            final AtomicBoolean isItem = new AtomicBoolean(false);
            for (Node activatedNode : activatedNodes) {
                Folder folder = activatedNodes[0].getLookup().lookup(Folder.class);
                if (folder == null) {
                    ViewItemNode vin = activatedNode.getLookup().lookup(ViewItemNode.class);
                    if (vin == null) {
                        return;
                    }
                    folder = vin.getFolder();
                    if (folder == null) {
                        return;
                    }
                    isItem.set(true);
                }
                MakeConfigurationDescriptor mcd = folder.getConfigurationDescriptor();
                if (mcd == null) {
                    return;
                }
                if (!projects.contains(mcd)) {
                    projects.add(mcd);
                }
            }
            for(MakeConfigurationDescriptor mcd : projects) {
                if (!mcd.okToChange()) {
                    return;
                }
            }
            InstanceContent ic = new InstanceContent();
            for (Node activatedNode : activatedNodes) {
                ic.add(activatedNode);
                if (saveBefore()) {
                    DataObject dobj = activatedNode.getLookup().lookup(DataObject.class);
                    SaveCookie sc = dobj.getLookup().lookup(SaveCookie.class);
                    if (sc != null) {
                        try {
                            sc.save();
                        } catch (IOException ex) {
                            Exceptions.printStackTrace(ex);
                        }
                    }
                }
            }
            Lookup actionContext = new AbstractLookup(ic);
            final Action a;
            if (systemAction instanceof NodeAction) {
                a = ((NodeAction) systemAction).createContextAwareInstance(actionContext);
            } else if (systemAction instanceof CallbackSystemAction) {
                a = ((CallbackSystemAction) systemAction).createContextAwareInstance(actionContext);
            } else {
                assert false;
                return;
            }
            SwingUtilities.invokeLater(() -> {
                a.actionPerformed(new ActionEvent(StandardNodeAction.this, 0, null));
                projects.forEach((mcd) -> {
                    if (isItem.get()) {
                        ViewItemNode.getRP().post(() -> {
                            mcd.save();
                        });
                    } else {
                        MakeLogicalViewProvider provider = mcd.getProject().getLookup().lookup(MakeLogicalViewProvider.class);
                        provider.getAnnotationRP().post(mcd::save);
                    }
                });
            });
        }

        @Override
        protected boolean enable(Node[] activatedNodes) {
            return true;
        }

        @Override
        public HelpCtx getHelpCtx() {
            return systemAction.getHelpCtx();
        }

        @Override
        public String getName() {
            return systemAction.getName();
        }
        
        protected boolean saveBefore() {
            return false;
        }
    }

    static final class RenameNodeAction extends StandardNodeAction {

        public RenameNodeAction() {
            super(SystemAction.get(RenameAction.class));
        }

        @Override
        protected boolean enable(Node[] activatedNodes) {
            return activatedNodes.length == 1;
        }
    }

    static final class DeleteNodeAction extends StandardNodeAction {

        public DeleteNodeAction() {
            super(SystemAction.get(DeleteAction.class));
        }

        @Override
        protected boolean saveBefore() {
            return true;
        }
        
    }
}
