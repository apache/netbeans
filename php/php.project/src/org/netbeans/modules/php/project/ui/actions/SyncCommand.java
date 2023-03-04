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
