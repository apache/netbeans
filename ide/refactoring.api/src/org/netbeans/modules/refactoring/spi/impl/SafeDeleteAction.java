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
package org.netbeans.modules.refactoring.spi.impl;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.SwingUtilities;
import org.netbeans.modules.refactoring.api.impl.ActionsImplementationFactory;
import org.netbeans.modules.refactoring.api.ui.ExplorerContext;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionReferences;
import org.openide.awt.ActionRegistration;
import org.openide.nodes.Node;
import org.openide.util.HelpCtx;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.explorer.ExtendedDelete;
import org.openide.filesystems.FileSystem;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
import org.openide.util.Exceptions;
import org.openide.util.lookup.Lookups;
import org.openide.util.lookup.ProxyLookup;

/**
 * @author Jan Becicka
 */
@ActionID(id = "org.netbeans.modules.refactoring.api.ui.SafeDeleteAction", category = "Refactoring")
@ActionRegistration(displayName = "#LBL_SafeDel_Action", lazy=false)
@ActionReferences({
    @ActionReference(path = "Menu/Refactoring", name = "SafeDeleteAction", position = 350),
    @ActionReference(path = "Shortcuts", name = "O-DELETE")
})
@NbBundle.Messages({
    "# {0} - name",
    "MSG_ConfirmDeleteObject=Are you sure you want to delete {0}?",
    "# {0} - number of objects",
    "MSG_ConfirmDeleteObjects=Are you sure you want to delete these {0} items?",
    "MSG_ConfirmDeleteObjectTitle=Confirm Object Deletion",
    "MSG_ConfirmDeleteObjectsTitle=Confirm Multiple Object Deletion"})
@org.openide.util.lookup.ServiceProvider(service = org.openide.explorer.ExtendedDelete.class)
public class SafeDeleteAction extends RefactoringGlobalAction implements ExtendedDelete {

    private static final Logger LOGGER = Logger.getLogger(SafeDeleteAction.class.getName());

    /**
     * Creates a new instance of SafeDeleteAction
     */
    public SafeDeleteAction() {
        super(NbBundle.getMessage(SafeDeleteAction.class, "LBL_SafeDel_Action"), null);
        putValue("noIconInMenu", Boolean.TRUE); // NOI18N
    }

    @Override
    public final void performAction(Lookup context) {
        ActionsImplementationFactory.doDelete(context);
        if (LOGGER.isLoggable(Level.FINEST)) {
            LOGGER.log(Level.FINEST, "SafeDeleteAction.performAction", new Exception());
        }
    }

    @Override
    public org.openide.util.HelpCtx getHelpCtx() {
        return HelpCtx.DEFAULT_HELP;
    }

    @Override
    protected boolean asynchronous() {
        return false;
    }

    @Override
    protected boolean enable(Lookup context) {
        return true;
    }

    @Override
    protected Lookup getLookup(Node[] n) {
        Lookup l = super.getLookup(n);
        if (regularDelete) {
            ExplorerContext con = l.lookup(ExplorerContext.class);
            if (con != null) {
                con.setDelete(true);
            } else {
                con = new ExplorerContext();
                con.setDelete(true);
                return new ProxyLookup(l, Lookups.singleton(con));
            }
        }
        return l;
    }
    private boolean regularDelete = false;

    @Override
    public boolean delete(final Node[] nodes) {
        if (nodes.length < 2 && ActionsImplementationFactory.canDelete(getLookup(nodes))) {
            if (java.awt.EventQueue.isDispatchThread()) {
                regularDelete = true;
                performAction(nodes);
                regularDelete = false;
            } else {
                SwingUtilities.invokeLater(new Runnable() {
                    @Override
                    public void run() {
                        regularDelete = true;
                        performAction(nodes);
                        regularDelete = false;
                    }
                });
            }
            return true;
        } else {
            // #172199: maybe this should be somewhere (where?) else, essentially
            // it's a bridge between explorer API and parsing API. Prior fixing #134716
            // SafeDelete was performed even on multiselection and if the user opted for
            // performing delete in the safe way all nodes/files were deleted in a single
            // batch operation from RepositoryUpdater's perspective.
            boolean delete = true;
            for (int i = 0; i < nodes.length; i++) {
                if (nodes[i].getLookup().lookup(DataObject.class) == null) {
                    // not file-based node
                    delete = false;
                    break;
                }
            }
            if (delete) {
                if (doConfirm(nodes)) {
                    try {
                        FileUtil.runAtomicAction(new FileSystem.AtomicAction() {
                            @Override
                            public void run() throws IOException {
                                for (int i = 0; i < nodes.length; i++) {
                                    try {
                                        nodes[i].destroy();
                                    } catch (IOException ioe) {
                                        Exceptions.printStackTrace(ioe);
                                    }
                                }
                            }
                        });
                    } catch (IOException ioe) {
                        Exceptions.printStackTrace(ioe);
                    }
                }
                return true;
            }
        }
        return false;
    }

    // #173549 - ideally we should somehow reuse this from ExplorerActionsImpl or
    //  EAI should wrap destroying nodes in FileSystem.AtomicAction the same way as we did
    //  for #172199
    private boolean doConfirm(Node[] sel) {
        String message;
        String title;
        boolean customDelete = true;

        for (int i = 0; i < sel.length; i++) {
            if (!Boolean.TRUE.equals(sel[i].getValue("customDelete"))) { // NOI18N
                customDelete = false;

                break;
            }
        }

        if (customDelete) {
            return true;
        }

        if (sel.length == 1) {
            message = NbBundle.getMessage(SafeDeleteAction.class, "MSG_ConfirmDeleteObject", sel[0].getDisplayName() //NOI18N
                    );
            title = NbBundle.getMessage(SafeDeleteAction.class, "MSG_ConfirmDeleteObjectTitle"); //NOI18N
        } else {
            message = NbBundle.getMessage(SafeDeleteAction.class, "MSG_ConfirmDeleteObjects", Integer.valueOf(sel.length) //NOI18N
                    );
            title = NbBundle.getMessage(SafeDeleteAction.class, "MSG_ConfirmDeleteObjectsTitle"); //NOI18N
        }

        NotifyDescriptor desc = new NotifyDescriptor.Confirmation(message, title, NotifyDescriptor.YES_NO_OPTION);

        return NotifyDescriptor.YES_OPTION.equals(DialogDisplayer.getDefault().notify(desc));
    }

    @Override
    protected boolean applicable(Lookup context) {
        return ActionsImplementationFactory.canDelete(context);
    }
}
