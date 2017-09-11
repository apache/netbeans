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
