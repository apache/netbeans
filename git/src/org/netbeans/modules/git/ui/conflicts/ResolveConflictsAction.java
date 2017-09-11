/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2010 Sun Microsystems, Inc.
 */

package org.netbeans.modules.git.ui.conflicts;

import java.io.File;
import org.netbeans.modules.git.ui.actions.*;
import java.util.EnumSet;
import org.netbeans.modules.git.FileInformation.Status;
import org.netbeans.modules.git.Git;
import org.netbeans.modules.git.client.GitProgressSupport;
import org.netbeans.modules.versioning.spi.VCSContext;
import org.openide.awt.ActionID;
import org.openide.awt.ActionRegistration;
import org.openide.nodes.Node;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor.Task;

/**
 *
 * @author Tomas Stupka
 */
@ActionID(id = "org.netbeans.modules.git.ui.conflicts.ResolveConflictsAction", category = "Git")
@ActionRegistration(displayName = "#LBL_ResolveConflictsAction_Name")
@NbBundle.Messages({
    "LBL_ResolveConflictsAction_Name=Resolve Con&flicts"
})
public class ResolveConflictsAction extends MultipleRepositoryAction {

    private static final String ICON_RESOURCE = "org/netbeans/modules/git/resources/icons/conflict-resolve.png"; //NOI18N

    public ResolveConflictsAction () {
        super(ICON_RESOURCE);
    }

    @Override
    protected String iconResource () {
        return ICON_RESOURCE;
    }
    
    @Override
    protected boolean enableFull (Node[] activatedNodes) {
        VCSContext context = getCurrentContext(activatedNodes);
        return Git.getInstance().getFileStatusCache().containsFiles(context, EnumSet.of(Status.IN_CONFLICT), false);
    }

    @Override
    protected Task performAction (File repository, File[] roots, VCSContext context) {
        final File[] files = Git.getInstance().getFileStatusCache().listFiles(roots, EnumSet.of(Status.IN_CONFLICT));
        if (files.length > 0) {
            GitProgressSupport supp = new ResolveConflictsExecutor(files);;
            supp.start(Git.getInstance().getRequestProcessor(repository), repository, NbBundle.getMessage(ResolveConflictsAction.class, "MSG_PreparingMerge")); //NOI18N
            return supp.getTask();
        } else {
            return null;
        }
    }
}
