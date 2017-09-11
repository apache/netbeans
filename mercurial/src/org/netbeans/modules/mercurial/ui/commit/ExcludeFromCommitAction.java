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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2009 Sun
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

package org.netbeans.modules.mercurial.ui.commit;

import java.io.*;
import java.util.Set;
import java.util.List;
import java.util.ArrayList;

import org.netbeans.modules.mercurial.HgModuleConfig;
import org.netbeans.modules.mercurial.HgProgressSupport;
import org.netbeans.modules.mercurial.Mercurial;
import org.netbeans.modules.mercurial.ui.actions.ContextAction;
import org.netbeans.modules.mercurial.util.HgUtils;
import org.openide.util.RequestProcessor;
import org.netbeans.modules.versioning.spi.VCSContext;
import org.openide.nodes.*;
import org.openide.util.NbBundle;

/**
 *
 * @author Petr Kuzel
 */
@NbBundle.Messages({
    "CTL_MenuItem_IncludeInCommit=In&clude in Commit",
    "CTL_MenuItem_ExcludeFromCommit=Ex&clude from Commit"
})
public final class ExcludeFromCommitAction extends ContextAction {

    public static final int UNDEFINED = -1;
    public static final int EXCLUDING = 1;
    public static final int INCLUDING = 2;

    @Override
    protected boolean enable(Node[] nodes) {
        VCSContext context = HgUtils.getCurrentContext(nodes);
        Set<File> ctxFiles = context != null? context.getRootFiles(): null;
        if (!HgUtils.isFromHgRepository(context) || ctxFiles == null || ctxFiles.size() == 0) {
            return false;
        }
        return !HgUtils.onlyProjects(nodes) && !HgUtils.onlyFolders(ctxFiles);
    }

    protected String getBaseName(Node[] nodes) {
        VCSContext ctx = HgUtils.getCurrentContext(nodes);
        int actionStatus = getActionStatus(ctx);
        switch (actionStatus) {
        case UNDEFINED:
        case EXCLUDING:
            return "CTL_MenuItem_ExcludeFromCommit";                    //NOI18N
        case INCLUDING:
            return "CTL_MenuItem_IncludeInCommit";                      //NOI18N
        default:
            throw new RuntimeException("Invalid action status: " + actionStatus); // NOI18N
        }
    }
    
    public int getActionStatus(VCSContext ctx) {
        HgModuleConfig config = HgModuleConfig.getDefault();
        int status = UNDEFINED;
        if (ctx == null) {
            ctx = HgUtils.getCurrentContext(null);
        }
        Set<File> files = ctx.getRootFiles();
        for (File file : files) {
            if (config.isExcludedFromCommit(file.getAbsolutePath())) {
                if (status == EXCLUDING) {
                    return UNDEFINED;
                }
                status = INCLUDING;
            } else {
                if (status == INCLUDING) {
                    return UNDEFINED;
                }
                status = EXCLUDING;
            }
        }
        return status;
    }

    @Override
    protected void performContextAction(Node[] nodes) {
        final VCSContext ctx = HgUtils.getCurrentContext(nodes);
        RequestProcessor rp = Mercurial.getInstance().getRequestProcessor();
        HgProgressSupport support = new HgProgressSupport() {
            public void perform() {
                HgModuleConfig config = HgModuleConfig.getDefault();
                int status = getActionStatus(ctx);
                Set<File> files = ctx.getRootFiles();
                List<String> paths = new ArrayList<String>(files.size());
                for (File file : files) {
                    paths.add(file.getAbsolutePath());
                }
                if (isCanceled()) return;
                if (status != INCLUDING) {
                    config.addExclusionPaths(paths);
                } else {
                    config.removeExclusionPaths(paths);
                }
            }
        };
        support.start(rp, "");                                          //NOI18N
    }
}
