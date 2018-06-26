/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2012 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2012 Sun Microsystems, Inc.
 */
package org.netbeans.modules.php.project.ui.actions;

import java.util.logging.Logger;
import org.netbeans.modules.php.project.PhpProject;
import org.netbeans.modules.php.project.ProjectPropertiesSupport;
import org.netbeans.modules.php.project.connections.RemoteClient;
import org.netbeans.modules.php.project.connections.spi.RemoteConfiguration;
import org.netbeans.modules.php.project.connections.sync.SyncController;
import org.netbeans.modules.php.project.connections.sync.SyncController.SyncResult;
import org.netbeans.modules.php.project.connections.sync.SyncController.SyncResultProcessor;
import org.netbeans.modules.php.project.runconfigs.RunConfigRemote;
import org.netbeans.modules.php.project.ui.actions.support.CommandUtils;
import org.netbeans.modules.php.project.ui.actions.support.Displayable;
import org.openide.filesystems.FileObject;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.windows.InputOutput;

/**
 * Synchronize remote and local files.
 */
public class SyncCommand extends RemoteCommand implements Displayable {

    static final Logger LOGGER = Logger.getLogger(SyncCommand.class.getName());

    public static final String ID = "synchronize"; // NOI18N
    @NbBundle.Messages("SyncCommand.label=Synchronize...")
    public static final String DISPLAY_NAME = Bundle.SyncCommand_label();


    public SyncCommand(PhpProject project) {
        super(project);
    }

    @Override
    public String getCommandId() {
        return ID;
    }

    @Override
    public String getDisplayName() {
        return DISPLAY_NAME;
    }

    @Override
    public boolean isFileSensitive() {
        return true;
    }

    @Override
    protected Runnable getContextRunnable(Lookup context) {
        final FileObject[] files = CommandUtils.filesForContext(context, ProjectPropertiesSupport.getSourcesDirectory(getProject()));
        return new Runnable() {
            @Override
            public void run() {
                synchronize(files);
            }
        };
    }

    void synchronize(FileObject[] files) {
        RemoteConfiguration remoteConfiguration = RunConfigRemote.forProject(getProject()).getRemoteConfiguration();
        InputOutput remoteLog = getRemoteLog(remoteConfiguration.getDisplayName());
        RemoteClient remoteClient = getRemoteClient(remoteLog);
        SyncController syncController;
        if (files == null) {
            // whole project
            syncController = SyncController.forProject(getProject(), remoteClient, remoteConfiguration);
        } else {
            // just one file
            syncController = SyncController.forFiles(files, getProject(), remoteClient, remoteConfiguration);
        }
        syncController.synchronize(new PrintSyncResultProcessor(remoteLog));
    }

    //~ Inner classes

    private static final class PrintSyncResultProcessor implements SyncResultProcessor {

        private final InputOutput remoteLog;


        public PrintSyncResultProcessor(InputOutput remoteLog) {
            this.remoteLog = remoteLog;
        }

        @NbBundle.Messages({
            "SyncCommand.download.title=Download",
            "SyncCommand.upload.title=Upload",
            "SyncCommand.localDelete.title=Local Delete",
            "SyncCommand.remoteDelete.title=Remote Delete"
        })
        @Override
        public void process(SyncResult result) {
            remoteLog.select();
            processTransferInfo(result.getDownloadTransferInfo(), remoteLog, Bundle.SyncCommand_download_title());
            processTransferInfo(result.getUploadTransferInfo(), remoteLog, Bundle.SyncCommand_upload_title());
            processTransferInfo(result.getLocalDeleteTransferInfo(), remoteLog, Bundle.SyncCommand_localDelete_title());
            processTransferInfo(result.getRemoteDeleteTransferInfo(), remoteLog, Bundle.SyncCommand_remoteDelete_title());
        }

    }

}
