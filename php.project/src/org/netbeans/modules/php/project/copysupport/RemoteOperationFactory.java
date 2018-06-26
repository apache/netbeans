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
 * Portions Copyrighted 2009 Sun Microsystems, Inc.
 */
package org.netbeans.modules.php.project.copysupport;

import java.io.File;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.modules.php.api.util.StringUtils;
import org.netbeans.modules.php.project.PhpProject;
import org.netbeans.modules.php.project.PhpVisibilityQuery;
import org.netbeans.modules.php.project.ProjectPropertiesSupport;
import org.netbeans.modules.php.project.connections.RemoteClient;
import org.netbeans.modules.php.project.connections.RemoteConnections;
import org.netbeans.modules.php.project.connections.RemoteException;
import org.netbeans.modules.php.project.connections.transfer.TransferInfo;
import org.netbeans.modules.php.project.connections.spi.RemoteConfiguration;
import org.netbeans.modules.php.project.connections.transfer.TransferFile;
import org.netbeans.modules.php.project.runconfigs.RunConfigRemote;
import org.netbeans.modules.php.project.runconfigs.validation.RunConfigRemoteValidator;
import org.netbeans.modules.php.project.ui.actions.RemoteCommand;
import org.netbeans.modules.php.project.ui.customizer.CompositePanelProviderImpl;
import org.netbeans.modules.php.project.ui.customizer.PhpProjectProperties.RunAsType;
import org.netbeans.modules.php.project.ui.customizer.PhpProjectProperties.UploadFiles;
import org.openide.filesystems.FileEvent;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileRenameEvent;
import org.openide.filesystems.FileUtil;
import org.openide.util.NbBundle;
import org.openide.windows.InputOutput;


/**
 * @author Radek Matous
 */
@org.netbeans.api.annotations.common.SuppressWarnings("NP_BOOLEAN_RETURN_NULL")
final class RemoteOperationFactory extends FileOperationFactory {
    private static final Logger LOGGER = Logger.getLogger(RemoteOperationFactory.class.getName());

    // @GuardedBy(this)
    private RemoteClient remoteClient;

    RemoteOperationFactory(PhpProject project) {
        super(project);
    }

    @Override
    protected boolean isEnabled() {
        return isEnabled(true);
    }

    private boolean isEnabled(boolean verbose) {
        boolean remoteConfigSelected = isRemoteConfigSelected();
        boolean uploadOnSave = false;
        if (remoteConfigSelected) {
            uploadOnSave = isUploadOnSave();
        }
        if (verbose) {
            LOGGER.log(Level.FINE, "REMOTE copying enabled for project {0}: {1}", new Object[] {project.getName(), remoteConfigSelected && uploadOnSave});
            if (!remoteConfigSelected) {
                LOGGER.fine("\t-> remote config not selected");
            }
            if (!uploadOnSave) {
                LOGGER.fine("\t-> upload on save not selected");
            }
        }
        return remoteConfigSelected && uploadOnSave;
    }

    @Override
    protected synchronized void resetInternal() {
        if (remoteClient != null) {
            disconnect(remoteClient, true);
        }
        remoteClient = null;
    }

    static void disconnect(RemoteClient client, boolean force) {
        assert client != null;
        try {
            client.disconnect(force);
        } catch (RemoteException ex) {
            LOGGER.log(Level.INFO, "Error while disconnecting", ex);
        }
    }

    @Override
    Logger getLogger() {
        return LOGGER;
    }

    @Override
    protected Callable<Boolean> createInitHandlerInternal(final FileObject source) {
        LOGGER.log(Level.FINE, "No INIT handler needed for project {0}", project.getName());
        return null;
    }

    @Override
    protected Callable<Boolean> createReinitHandlerInternal(FileObject source) {
        LOGGER.log(Level.FINE, "No REINIT handler needed for project {0}", project.getName());
        return null;
    }

    @Override
    protected Callable<Boolean> createCopyHandlerInternal(final FileObject source, FileEvent fileEvent) {
        LOGGER.log(Level.FINE, "Creating COPY handler for {0} (project {1})", new Object[] {getPath(source), project.getName()});
        return new Callable<Boolean>() {
            @Override
            public Boolean call() throws Exception {
                LOGGER.log(Level.FINE, "Running COPY handler for {0} (project {1})", new Object[] {getPath(source), project.getName()});
                if (!isValid(source)) {
                    return null;
                }
                RemoteClient client = getRemoteClient();
                try {
                    return doCopy(client, source);
                } finally {
                    disconnect(client, false);
                }
            }
        };
    }

    @Override
    protected Callable<Boolean> createRenameHandlerInternal(final FileObject source, final String oldName, FileRenameEvent fileRenameEvent) {
        LOGGER.log(Level.FINE, "Creating RENAME handler for {0} (project {1})", new Object[] {getPath(source), project.getName()});
        return new Callable<Boolean>() {
            @Override
            public Boolean call() throws Exception {
                LOGGER.log(Level.FINE, "Running RENAME handler for {0} (project {1})", new Object[] {getPath(source), project.getName()});
                if (!isValid(source)) {
                    return null;
                }
                RemoteClient client = getRemoteClient();
                try {
                    return doRename(client, source, oldName);
                } finally {
                    disconnect(client, false);
                }
            }
        };
    }

    @Override
    protected Callable<Boolean> createDeleteHandlerInternal(final FileObject source, FileEvent fileEvent) {
        LOGGER.log(Level.FINE, "Creating DELETE handler for {0} (project {1})", new Object[] {getPath(source), project.getName()});
        return new Callable<Boolean>() {
            @Override
            public Boolean call() throws Exception {
                LOGGER.log(Level.FINE, "Running DELETE handler for {0} (project {1})", new Object[] {getPath(source), project.getName()});
                if (!isValid(source)) {
                    return null;
                }
                RemoteClient client = getRemoteClient();
                try {
                    return doDelete(client, source);
                } finally {
                    disconnect(client, false);
                }
            }
        };
    }

    private boolean isValid(FileObject source) {
        LOGGER.log(Level.FINE, "Validating source {0} for {1}", new Object[] {getPath(source), project.getName()});
        if (!isRemoteConfigValid()) {
            LOGGER.fine("\t-> invalid (invalid config)");
            return false;
        }
        if (!isSourceFileValid(source)) {
            LOGGER.fine("\t-> invalid (invalid source)");
            return false;
        }
        return true;
    }

    @NbBundle.Messages({
        "# {0} - project name",
        "# {1} - reason",
        "RemoteOperationFactory.error=Upload Files On Save cannot continue for project {0}: {1}\nDo you want to open Project Properties dialog now?"
    })
    protected boolean isRemoteConfigValid() {
        if (!isEnabled(false)) {
            LOGGER.log(Level.FINE, "REMOTE copying not enabled for project {0}", project.getName());
            return false;
        }
        if (isInvalid()) {
            LOGGER.log(Level.FINE, "REMOTE copying invalid for project {0}", project.getName());
            return false;
        }
        if (getSources() == null) {
            LOGGER.log(Level.WARNING, "REMOTE copying disabled for project {0}. Reason: source root is null", project.getName());
            return false;
        }
        String error = RunConfigRemoteValidator.validateRemoteTransfer(RunConfigRemote.forProject(project));
        if (error != null) {
            LOGGER.log(Level.INFO, "REMOTE copying disabled for project {0}. Reason: {1}", new Object[] {project.getName(), error});
            if (askUser(Bundle.RemoteOperationFactory_error(project.getName(), error))) {
                showCustomizer(CompositePanelProviderImpl.RUN);
            }
            invalidate();
            return false;
        }
        return true;
    }

    protected synchronized RemoteClient getRemoteClient() {
        if (remoteClient == null) {
            InputOutput remoteLog = RemoteCommand.getRemoteLog(
                    NbBundle.getMessage(RemoteOperationFactory.class, "LBL_RemoteSynchronizationLog", project.getName(),
                    false));
            remoteClient = new RemoteClient(getRemoteConfiguration(), new RemoteClient.AdvancedProperties()
                    .setAdditionalInitialSubdirectory(ProjectPropertiesSupport.getRemoteDirectory(project))
                    .setPreservePermissions(ProjectPropertiesSupport.areRemotePermissionsPreserved(project))
                    .setUploadDirectly(ProjectPropertiesSupport.isRemoteUploadDirectly(project))
                    .setInputOutput(remoteLog)
                    .setPhpVisibilityQuery(PhpVisibilityQuery.forProject(project)));
        }
        return remoteClient;
    }

    protected boolean isUploadOnSave() {
        return UploadFiles.ON_SAVE.equals(ProjectPropertiesSupport.getRemoteUpload(project));
    }

    protected boolean isRemoteConfigSelected() {
        return RunAsType.REMOTE.equals(ProjectPropertiesSupport.getRunAs(project));
    }

    protected RemoteConfiguration getRemoteConfiguration() {
        String configName = ProjectPropertiesSupport.getRemoteConnection(project);
        assert StringUtils.hasText(configName) : "Remote configuration name must be selected for project " + project.getName();
        return RemoteConnections.get().remoteConfigurationForName(configName);
    }

    Boolean doCopy(RemoteClient client, FileObject source) throws RemoteException {
        LOGGER.log(Level.FINE, "Uploading file {0} for project {1}", new Object[] {getPath(source), project.getName()});
        FileObject sourceRoot = getSources();
        Set<TransferFile> transferFiles = client.prepareUpload(sourceRoot, source);
        if (transferFiles.size() > 0) {
            TransferInfo transferInfo = client.upload(transferFiles);
            if (!transferInfo.hasAnyFailed()
                    && !transferInfo.hasAnyPartiallyFailed()
                    && !transferInfo.hasAnyIgnored()) {
                LOGGER.fine("\t-> success");
                return true;
            }
            LOGGER.fine("\t-> failure");
            LOGGER.log(Level.INFO, "Upload failed: {0}", transferInfo);
            return false;
        }
        LOGGER.fine("\t-> nothing to upload?!");
        return null;
    }

    Boolean doRename(RemoteClient client, FileObject source, String oldName) throws RemoteException {
        FileObject sourceRoot = getSources();
        String baseDirectory = FileUtil.toFile(sourceRoot).getAbsolutePath();
        File sourceFile = FileUtil.toFile(source);
        TransferFile toTransferFile = TransferFile.fromFileObject(client.createRemoteClientImplementation(baseDirectory), null, source);
        TransferFile fromTransferFile = TransferFile.fromFile(client.createRemoteClientImplementation(baseDirectory), null, new File(sourceFile.getParentFile(), oldName));
        LOGGER.log(Level.FINE, "Renaming file {0} -> {1} for project {2}", new Object[] {fromTransferFile.getRemotePath(), toTransferFile.getRemotePath(), project.getName()});
        if (client.exists(fromTransferFile)) {
            if (client.rename(fromTransferFile, toTransferFile)) {
                LOGGER.fine("\t-> success");
                return true;
            } else {
                LOGGER.fine("\t-> failure");
                return false;
            }
        }
        // file not exist remotely => simply upload it
        LOGGER.fine("\t-> does not exist -> uploading");
        return doCopy(client, source);
    }

    Boolean doDelete(RemoteClient client, FileObject source) throws RemoteException {
        LOGGER.log(Level.FINE, "Deleting file {0} for project {1}", new Object[] {getPath(source), project.getName()});
        Boolean success = null;
        Set<TransferFile> transferFiles = client.prepareDelete(getSources(), source);
        for (TransferFile file : transferFiles) {
            LOGGER.log(Level.FINE, "Deleting remote file {0}", file.getRemotePath());
            if (!client.exists(file)) {
                LOGGER.fine("\t-> does not exist -> ignoring");
            } else {
                TransferInfo transferInfo = client.delete(file);
                if (transferInfo.hasAnyTransfered()) {
                    LOGGER.fine("\t-> success");
                } else {
                    LOGGER.fine("\t-> failure");
                    LOGGER.log(Level.INFO, "Remote delete failed: {0}", transferInfo);
                    success = false;
                }
            }
        }
        return success;
    }

    @Override
    protected boolean isValid(FileEvent fileEvent) {
        boolean valid = !fileEvent.firedFrom(RemoteClient.DOWNLOAD_ATOMIC_ACTION);
        if (valid) {
            // #202673
            LOGGER.log(Level.FINE, "FS event fired from thread: {0}", Thread.currentThread().getName());
        }
        return valid;
    }

}
