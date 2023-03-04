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
import org.openide.util.Utilities;
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
            JOptionPane.showMessageDialog(Utilities.findDialogParent(),
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
