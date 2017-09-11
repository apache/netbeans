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
package org.netbeans.modules.mercurial.ui.diff;

import org.netbeans.modules.versioning.spi.VCSContext;
import org.netbeans.modules.versioning.util.Utils;

import javax.swing.*;
import java.io.File;
import java.util.Set;
import org.netbeans.modules.mercurial.FileInformation;
import org.netbeans.modules.mercurial.FileStatus;

import org.netbeans.modules.mercurial.Mercurial;
import org.netbeans.modules.mercurial.OutputLogger;
import org.netbeans.modules.mercurial.util.HgUtils;
import org.netbeans.modules.mercurial.ui.actions.ContextAction;
import org.netbeans.modules.mercurial.ui.log.HgLogMessage.HgRevision;
import org.openide.nodes.Node;
import org.openide.util.NbBundle;
import static org.netbeans.modules.mercurial.ui.diff.Bundle.*;

/**
 * Diff action for mercurial: 
 * hg diff - diff repository (or selected files)
 * 
 * @author John Rice
 */
@NbBundle.Messages({
    "CTL_MenuItem_Diff=&Diff To Base",
    "CTL_PopupMenuItem_Diff=Diff To Base",
    "# {0} - context name", "CTL_DiffPanel_Title={0} [ Diff ]"
})
public class DiffAction extends ContextAction {
    private static final String ICON_RESOURCE = "org/netbeans/modules/mercurial/resources/icons/diff.png"; //NOI18N
    
    public DiffAction () {
        super(ICON_RESOURCE);
    }
    
    @Override
    protected boolean enable(Node[] nodes) {
        VCSContext context = HgUtils.getCurrentContext(nodes);
        Set<File> ctxFiles = context != null? context.getRootFiles(): null;
        if(!HgUtils.isFromHgRepository(context) || ctxFiles == null || ctxFiles.isEmpty())
            return false;
        return true;
    }

    @Override
    protected String getBaseName(Node[] nodes) {
        return "CTL_MenuItem_Diff"; // NOI18N
    }

    @Override
    protected String iconResource () {
        return ICON_RESOURCE;
    }

    @Override
    protected void performContextAction(Node[] nodes) {
        VCSContext context = HgUtils.getCurrentContext(nodes);
        String contextName = Utils.getContextDisplayName(context);
                
        File [] files = context.getRootFiles().toArray(new File[context.getRootFiles().size()]);
        boolean bNotManaged = !HgUtils.isFromHgRepository(context) || ( files == null || files.length == 0);

        if (bNotManaged) {
            OutputLogger logger = Mercurial.getInstance().getLogger(Mercurial.MERCURIAL_OUTPUT_TAB_TITLE);
            logger.outputInRed( NbBundle.getMessage(DiffAction.class,"MSG_DIFF_TITLE")); // NOI18N
            logger.outputInRed( NbBundle.getMessage(DiffAction.class,"MSG_DIFF_TITLE_SEP")); // NOI18N
            logger.outputInRed(
                    NbBundle.getMessage(DiffAction.class, "MSG_DIFF_NOT_SUPPORTED_INVIEW_INFO")); // NOI18N
            logger.output(""); // NOI18N
            logger.closeLog();
            JOptionPane.showMessageDialog(null,
                    NbBundle.getMessage(DiffAction.class, "MSG_DIFF_NOT_SUPPORTED_INVIEW"),// NOI18N
                    NbBundle.getMessage(DiffAction.class, "MSG_DIFF_NOT_SUPPORTED_INVIEW_TITLE"),// NOI18N
                    JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        diff(context, Setup.DIFFTYPE_LOCAL, contextName);
    }

    public static void diff(VCSContext ctx, int type, String contextName) {

        MultiDiffPanel panel = new MultiDiffPanel(ctx, type, contextName); // spawns background DiffPrepareTask
        DiffTopComponent tc = new DiffTopComponent(panel);
        tc.setName(CTL_DiffPanel_Title(contextName));
        tc.open();
        tc.requestActive();
    }

    public static void diff (File file1, HgRevision rev1, File file2, HgRevision rev2) {
        diff(file1, rev1, file2, rev2, -1);
    }

    public static void diff (File file1, HgRevision rev1, File file2, HgRevision rev2, int lineNumber) {
        MultiDiffPanel panel = new MultiDiffPanel(file2, rev1, rev2,
                new FileInformation(FileInformation.STATUS_VERSIONED_UPTODATE, new FileStatus(file2, file1), false),
                false, lineNumber); // spawns background DiffPrepareTask
        DiffTopComponent tc = new DiffTopComponent(panel);
        tc.setName(CTL_DiffPanel_Title(file2.getName()));
        tc.open();
        tc.requestActive();
    }

    public void diff (File[] roots, HgRevision rev1, HgRevision rev2, String contextName,
            boolean fixedRevisions, boolean displayUnversionedFiles) {
        // spawns background DiffPrepareTask
        MultiDiffPanel panel = new MultiDiffPanel(roots, rev1, rev2, fixedRevisions, displayUnversionedFiles);
        DiffTopComponent tc = new DiffTopComponent(panel);
        tc.setName(CTL_DiffPanel_Title(contextName));
        tc.open();
        tc.requestActive();
    }
}
