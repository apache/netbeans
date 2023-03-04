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

package org.netbeans.modules.refactoring.api.impl;

import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ui.OpenProjects;
import org.netbeans.modules.refactoring.spi.impl.CopyAction;
import org.netbeans.modules.refactoring.spi.ui.ActionsImplementationProvider;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor.Message;
import org.openide.filesystems.FileObject;
import org.openide.nodes.Node;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;

/**
 * @author Jan Becicka
 */
public final class ActionsImplementationFactory {

    private static final Logger LOG = Logger.getLogger(ActionsImplementationFactory.class.getName());
    
    private ActionsImplementationFactory(){}
    
    private static final Lookup.Result<ActionsImplementationProvider> implementations =
        Lookup.getDefault().lookupResult(ActionsImplementationProvider.class);

    public static boolean canRename(Lookup lookup) {
        for (ActionsImplementationProvider rafi: implementations.allInstances()) {
            if (rafi.canRename(lookup)) {
                return true;
            }
        }
        return false;
    }
    
    public static void doRename(Lookup lookup) {
        for (ActionsImplementationProvider rafi: implementations.allInstances()) {
            boolean canRename = rafi.canRename(lookup);

            if (LOG.isLoggable(Level.FINE)) {
                LOG.fine(String.format("canRename: %s, %s", rafi, canRename));
            }

            if (canRename) {
                rafi.doRename(lookup);
                return;
            }
        }
        notifyOutOfContext("LBL_RenameRefactoring", lookup); // NOI18N
    }

    public static boolean canFindUsages(Lookup lookup) {
        for (ActionsImplementationProvider rafi: implementations.allInstances()) {
            if (rafi.canFindUsages(lookup)) {
                return true;
            }
        }
        return false;
    }
    
    public static void doFindUsages(Lookup lookup) {
        for (ActionsImplementationProvider rafi: implementations.allInstances()) {
            boolean canFindUsages = rafi.canFindUsages(lookup);

            if (LOG.isLoggable(Level.FINE)) {
                LOG.fine(String.format("canFindUsages: %s, %s", rafi, canFindUsages));
            }

            if (canFindUsages) {
                rafi.doFindUsages(lookup);
                return;
            }
        }
        notifyOutOfContext("LBL_FindUsagesRefactoring", lookup); // NOI18N
    }
    public static boolean canDelete(Lookup lookup) {
        for (ActionsImplementationProvider rafi: implementations.allInstances()) {
            if (rafi.canDelete(lookup)) {
                return true;
            }
        }
        return false;
    }
    
    public static void doDelete(Lookup lookup) {
        for (ActionsImplementationProvider rafi: implementations.allInstances()) {
            boolean canDelete = rafi.canDelete(lookup);

            if (LOG.isLoggable(Level.FINE)) {
                LOG.fine(String.format("canDelete: %s, %s", rafi, canDelete));
            }

            if (canDelete) {
                rafi.doDelete(lookup);
                return;
            }
        }
        notifyOutOfContext("LBL_SafeDeleteRefactoring", lookup); // NOI18N
    }
    
    public static void doMove(Lookup lookup) {
        for (ActionsImplementationProvider rafi: implementations.allInstances()) {
            boolean canMove = rafi.canMove(lookup);

            if (LOG.isLoggable(Level.FINE)) {
                LOG.fine(String.format("canMove: %s, %s", rafi, canMove));
            }

            if (canMove) {
                rafi.doMove(lookup);
                return;
            }
        }
        notifyOutOfContext("LBL_MoveRefactoring", lookup); // NOI18N
    }
    
    public static boolean canMove(Lookup lookup) {
        for (ActionsImplementationProvider rafi: implementations.allInstances()) {
            if (rafi.canMove(lookup)) {
                return true;
            }
        }
        return false;
    }

    public static void doCopy(Lookup lookup) {
        for (ActionsImplementationProvider rafi: implementations.allInstances()) {
            boolean canCopy = rafi.canCopy(lookup);

            if (LOG.isLoggable(Level.FINE)) {
                LOG.fine(String.format("canCopy: %s, %s", rafi, canCopy));
            }

            if (canCopy) {
                rafi.doCopy(lookup);
                return;
            }
        }
        notifyOutOfContext("LBL_CopyRefactoring", lookup); // NOI18N
    }
    
    public static boolean canCopy(Lookup lookup) {
        for (ActionsImplementationProvider rafi: implementations.allInstances()) {
            if (rafi.canCopy(lookup)) {
                return true;
            }
        }
        return false;
    }

    private static void notifyOutOfContext(String refactoringNameKey, Lookup context) {
        for (Node node : context.lookupAll(Node.class)) {
            for (FileObject file : node.getLookup().lookupAll(FileObject.class)) {
                if (!isFileInOpenProject(file)) {
                    DialogDisplayer.getDefault().notify(new Message(
                            NbBundle.getMessage(CopyAction.class, "ERR_ProjectNotOpened", file.getNameExt())));
                    return;
                }
            }
        }
        String refactoringName = NbBundle.getMessage(CopyAction.class, refactoringNameKey);
        DialogDisplayer.getDefault().notify(new Message(
                NbBundle.getMessage(CopyAction.class, "MSG_CantApplyRefactoring", refactoringName)));
    }

    private static boolean isFileInOpenProject(FileObject file) {
        assert file != null;
        Project p = FileOwnerQuery.getOwner(file);
        if (p == null) {
            return false;
        }
        return isOpenProject(p);
    }

    private static boolean isOpenProject(Project p) {
        return OpenProjects.getDefault().isProjectOpen(p);
    }

    
}
