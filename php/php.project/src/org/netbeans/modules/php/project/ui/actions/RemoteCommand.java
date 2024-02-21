/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.netbeans.modules.php.project.ui.actions;

import java.awt.Color;
import java.awt.EventQueue;
import java.io.File;
import java.io.IOException;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Deque;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.modules.php.project.PhpProject;
import org.netbeans.modules.php.project.PhpVisibilityQuery;
import org.netbeans.modules.php.project.ProjectPropertiesSupport;
import org.netbeans.modules.php.project.connections.RemoteClient;
import org.netbeans.modules.php.project.connections.RemoteClient.Operation;
import org.netbeans.modules.php.project.connections.sync.TimeStamps;
import org.netbeans.modules.php.project.connections.transfer.TransferFile;
import org.netbeans.modules.php.project.connections.transfer.TransferInfo;
import org.netbeans.modules.php.project.runconfigs.RunConfigRemote;
import org.netbeans.modules.php.project.runconfigs.validation.RunConfigRemoteValidator;
import org.netbeans.modules.php.project.ui.actions.support.RememberAsSyncPanel;
import org.netbeans.modules.php.project.ui.customizer.PhpProjectProperties;
import org.netbeans.modules.php.project.util.PhpProjectUtils;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;
import org.openide.windows.IOColorLines;
import org.openide.windows.IOProvider;
import org.openide.windows.InputOutput;
import org.openide.windows.OutputWriter;

/**
 * @author Radek Matous
 */
public abstract class RemoteCommand extends Command {

    private static final boolean IGNORE_REMEMBER_SYNC = Boolean.getBoolean("nb.php.remote.sync.remember.ignore"); // NOI18N
    private static final char SEP_CHAR = '='; // NOI18N
    private static final int MAX_TYPE_SIZE = getFileTypeLabelMaxSize() + 2;
    private static final Color COLOR_SUCCESS = Color.GREEN.darker().darker();
    private static final Color COLOR_IGNORE = Color.ORANGE.darker();

    private static final RequestProcessor RP = new RequestProcessor("Remote connection", 1); // NOI18N
    private static final Queue<Runnable> RUNNABLES = new ConcurrentLinkedQueue<>();
    private static final RequestProcessor.Task TASK = RP.create(new Runnable() {
        @Override
        public void run() {
            Runnable toRun = RUNNABLES.poll();
            while (toRun != null) {
                toRun.run();
                toRun = RUNNABLES.poll();
            }
        }
    }, true);

    private static final AtomicBoolean ASK_REMEMBER_SYNC = new AtomicBoolean(true);
    private static final AtomicBoolean DO_SYNC = new AtomicBoolean(true);


    public RemoteCommand(PhpProject project) {
        super(project);

    }

    @Override
    public boolean isFileSensitive() {
        return true;
    }

    @Override
    public final void invokeActionInternal(Lookup context) {
        assert getConfigAction().getClass().getSimpleName().equals("ConfigActionRemote") : "Remote config action expected but found: " + getConfigAction().getClass().getSimpleName();
        if (RunConfigRemoteValidator.validateRemoteTransfer(RunConfigRemote.forProject(getProject())) != null) {
            PhpProjectUtils.openCustomizerRun(getProject());
            return;
        }
        RUNNABLES.add(getContextRunnable(context));
        TASK.schedule(0);
    }

    @Override
    public final boolean isActionEnabledInternal(Lookup context) {
        // WARNING context can be null, see RunCommand.invokeAction()
        return isRemoteConfigSelected() && TASK.isFinished();
    }

    protected abstract Runnable getContextRunnable(Lookup context);

    @Override
    public final boolean asyncCallRequired() {
        return false;
    }

    public static InputOutput getRemoteLog(String displayName) {
        return getRemoteLog(displayName, true);
    }

    public static InputOutput getRemoteLog(String displayName, boolean select) {
        InputOutput io = IOProvider.getDefault().getIO(NbBundle.getMessage(Command.class, "LBL_RemoteLog", displayName), false);
        if (select) {
            io.select();
        }
        try {
            io.getOut().reset();
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }
        return io;
    }

    protected RemoteClient getRemoteClient(InputOutput io) {
        RunConfigRemote runConfig = RunConfigRemote.forProject(getProject());
        return new RemoteClient(runConfig.getRemoteConfiguration(), new RemoteClient.AdvancedProperties()
                .setInputOutput(io)
                .setAdditionalInitialSubdirectory(runConfig.getUploadDirectory())
                .setPreservePermissions(runConfig.arePermissionsPreserved())
                .setUploadDirectly(runConfig.getUploadDirectly())
                .setPhpVisibilityQuery(PhpVisibilityQuery.forProject(getProject())));
    }

    protected boolean isRemoteConfigSelected() {
        PhpProjectProperties.RunAsType runAs = ProjectPropertiesSupport.getRunAs(getProject());
        return PhpProjectProperties.RunAsType.REMOTE.equals(runAs);
    }

    protected static void processTransferInfo(TransferInfo transferInfo, InputOutput io) {
        processTransferInfo(transferInfo, io, NbBundle.getMessage(RemoteCommand.class, "LBL_RemoteSummary"));
    }

    protected static void processTransferInfo(TransferInfo transferInfo, InputOutput io, String title) {
        OutputWriter out = io.getOut();
        OutputWriter err = io.getErr();

        out.println();
        out.println(title);
        StringBuilder sep = new StringBuilder(20);
        for (int i = 0; i < sep.capacity(); i++) {
            sep.append(SEP_CHAR);
        }
        out.println(sep.toString());

        int maxRelativePath = getRelativePathMaxSize(transferInfo);
        long size = 0;
        int files = 0;
        if (transferInfo.hasAnyTransfered()) {
            printSuccess(io, NbBundle.getMessage(RemoteCommand.class, "LBL_RemoteSucceeded"));
            ArrayList<TransferFile> sorted = new ArrayList<>(transferInfo.getTransfered());
            sorted.sort(TransferFile.TRANSFER_FILE_COMPARATOR);
            for (TransferFile file : sorted) {
                printSuccess(io, maxRelativePath, file);
                if (file.isFile()) {
                    size += file.getSize();
                    files++;
                }
            }
        }

        if (transferInfo.hasAnyFailed()) {
            err.println(NbBundle.getMessage(RemoteCommand.class, "LBL_RemoteFailed"));
            Map<TransferFile, String> sorted = new TreeMap<>(TransferFile.TRANSFER_FILE_COMPARATOR);
            sorted.putAll(transferInfo.getFailed());
            for (Map.Entry<TransferFile, String> entry : sorted.entrySet()) {
                printError(err, maxRelativePath, entry.getKey(), entry.getValue());
            }
        }

        if (transferInfo.hasAnyPartiallyFailed()) {
            err.println(NbBundle.getMessage(RemoteCommand.class, "LBL_RemotePartiallyFailed"));
            Map<TransferFile, String> sorted = new TreeMap<>(TransferFile.TRANSFER_FILE_COMPARATOR);
            sorted.putAll(transferInfo.getPartiallyFailed());
            for (Map.Entry<TransferFile, String> entry : sorted.entrySet()) {
                printError(err, maxRelativePath, entry.getKey(), entry.getValue());
            }
        }

        if (transferInfo.hasAnyIgnored()) {
            printIgnore(io, NbBundle.getMessage(RemoteCommand.class, "LBL_RemoteIgnored"));
            Map<TransferFile, String> sorted = new TreeMap<>(TransferFile.TRANSFER_FILE_COMPARATOR);
            sorted.putAll(transferInfo.getIgnored());
            for (Map.Entry<TransferFile, String> entry : sorted.entrySet()) {
                printIgnore(io, maxRelativePath, entry.getKey(), entry.getValue());
            }
        }

        // summary
        long runtime = transferInfo.getRuntime();
        String timeUnit = NbBundle.getMessage(RemoteCommand.class, "LBL_TimeUnitMilisecond");
        if (runtime > 1000) {
            runtime /= 1000;
            timeUnit = NbBundle.getMessage(RemoteCommand.class, "LBL_TimeUnitSecond");
        }
        double s = size / 1024.0;
        String sizeUnit = NbBundle.getMessage(RemoteCommand.class, "LBL_SizeUnitKilobyte");
        if (s > 1024) {
            s /= 1024.0;
            sizeUnit = NbBundle.getMessage(RemoteCommand.class, "LBL_SizeUnitMegabyte");
        }
        Object[] params = new Object[] {
            runtime,
            timeUnit,
            files,
            s,
            sizeUnit,
        };
        out.println(NbBundle.getMessage(RemoteCommand.class, "MSG_RemoteRuntimeAndSize", params));
    }

    private static void print(InputOutput io, String message, Color color) {
        try {
            IOColorLines.println(io, message, color);
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }
    }

    private static void printSuccess(InputOutput io, String message) {
        print(io, message.trim(), COLOR_SUCCESS);
    }

    private static void printSuccess(InputOutput io, int maxRelativePath, TransferFile file) {
        String message = String.format("%-" + MAX_TYPE_SIZE + "s %-" + maxRelativePath + "s", getFileTypeLabel(file), file.getRemotePath());
        printSuccess(io, message);
    }

    private static void printError(OutputWriter writer, int maxRelativePath, TransferFile file, String reason) {
        String msg = String.format("%-" + MAX_TYPE_SIZE + "s %-" + maxRelativePath + "s   %s", getFileTypeLabel(file), file.getRemotePath(), reason);
        writer.println(msg);
    }

    private static void printIgnore(InputOutput io, String message) {
        print(io, message, COLOR_IGNORE);
    }

    private static void printIgnore(InputOutput io, int maxRelativePath, TransferFile file, String reason) {
        String msg = String.format("%-" + MAX_TYPE_SIZE + "s %-" + maxRelativePath + "s   %s", getFileTypeLabel(file), file.getRemotePath(), reason);
        printIgnore(io, msg);
    }

    private static String getFileTypeLabel(TransferFile file) {
        String type = null;
        if (file.isDirectory()) {
            type = "LBL_TypeDirectory"; // NOI18N
        } else if (file.isFile()) {
            type = "LBL_TypeFile"; // NOI18N
        } else if (file.isLink()) {
            type = "LBL_TypeLink"; // NOI18N
        } else {
            type = "LBL_TypeUnknown"; // NOI18N
        }
        return NbBundle.getMessage(RemoteCommand.class, type);
    }

    private static int getFileTypeLabelMaxSize() {
        int max = 0;
        for (String label : Arrays.asList("LBL_TypeDirectory", "LBL_TypeFile", "LBL_TypeLink", "LBL_TypeUnknown")) { // NOI18N
            int length = NbBundle.getMessage(RemoteCommand.class, label).length();
            if (max < length) {
                max = length;
            }
        }
        return max;
    }

    private static int getRelativePathMaxSize(TransferInfo transferInfo) {
        int max = getRelativePathMaxSize(transferInfo.getTransfered());
        int size = getRelativePathMaxSize(transferInfo.getFailed().keySet());
        if (size > max) {
            max = size;
        }
        size = getRelativePathMaxSize(transferInfo.getPartiallyFailed().keySet());
        if (size > max) {
            max = size;
        }
        size = getRelativePathMaxSize(transferInfo.getIgnored().keySet());
        if (size > max) {
            max = size;
        }
        return max + 2;
    }

    private static int getRelativePathMaxSize(Collection<TransferFile> files) {
        int max = 0;
        for (TransferFile file : files) {
            int length = file.getRemotePath().length();
            if (length > max) {
                max = length;
            }
        }
        return max;
    }

    // # 161620
    protected final boolean sourcesFilesOnly(FileObject sources, FileObject[] selectedFiles) {
        for (FileObject file : selectedFiles) {
            if (!FileUtil.isParentOf(sources, file) && !sources.equals(file)) {
                DialogDisplayer.getDefault().notify(new NotifyDescriptor.Message(
                        NbBundle.getMessage(RemoteCommand.class, "MSG_TransferSourcesOnly"),
                        NotifyDescriptor.ERROR_MESSAGE));
                return false;
            }
        }
        return true;
    }

    // #142955 - but remember only if one of the selected files is source directory
    //  (otherwise it would make no sense, consider this scenario: upload just one file -> remember timestamp
    //  -> upload another file or the whole project [timestamp is irrelevant])
    protected static boolean isSourcesSelected(FileObject sources, FileObject[] selectedFiles) {
        for (FileObject fo : selectedFiles) {
            if (sources.equals(fo)) {
                return true;
            }
        }
        return false;
    }

    // #250343 remember it also as last sync
    @NbBundle.Messages("RemoteCommand.sync.remember.title=Remote Synchronization")
    protected static void storeLastSync(final PhpProject project, final RemoteClient remoteClient, final FileObject sources, boolean possiblyAskUser) {
        if (IGNORE_REMEMBER_SYNC) {
            // #257391
            return;
        }
        if (!DO_SYNC.get()) {
            return;
        }
        if (possiblyAskUser
                && ASK_REMEMBER_SYNC.get()) {
            EventQueue.invokeLater(new Runnable() {
                @Override
                public void run() {
                    RememberAsSyncPanel panel = new RememberAsSyncPanel();
                    NotifyDescriptor descriptor = new NotifyDescriptor(
                            panel,
                            Bundle.RemoteCommand_sync_remember_title(),
                            NotifyDescriptor.YES_NO_OPTION,
                            NotifyDescriptor.QUESTION_MESSAGE,
                            new Object[] {NotifyDescriptor.YES_OPTION, NotifyDescriptor.NO_OPTION},
                            NotifyDescriptor.NO_OPTION);
                    boolean store = DialogDisplayer.getDefault().notify(descriptor) == NotifyDescriptor.YES_OPTION;
                    if (panel.isDoNotAskAgain()) {
                        ASK_REMEMBER_SYNC.set(false);
                        DO_SYNC.set(store);
                    }
                    if (store) {
                        RUNNABLES.add(new Runnable() {
                            @Override
                            public void run() {
                                storeLastSyncInternal(project, remoteClient, sources);
                            }
                        });
                        TASK.schedule(0);
                    }
                }
            });
            return;
        }
        storeLastSyncInternal(project, remoteClient, sources);
    }

    static void storeLastSyncInternal(PhpProject project, RemoteClient remoteClient, FileObject sources) {
        assert !EventQueue.isDispatchThread();
        assert DO_SYNC.get();
        TimeStamps timeStamps = new TimeStamps(project);
        timeStamps.setSyncTimestamp(getRemoteRoot(remoteClient, sources));
    }

    static TransferFile getRemoteRoot(RemoteClient remoteClient, FileObject sources) {
        File sourceDir = FileUtil.toFile(sources);
        return TransferFile.fromFile(remoteClient.createRemoteClientImplementation(sourceDir.getAbsolutePath()),
                null, sourceDir);
    }

    //~ Inner classes

    /**
     * Default operation monitor for file upload and download.
     */
    public static final class DefaultOperationMonitor implements RemoteClient.OperationMonitor {

        private static final Logger LOGGER = Logger.getLogger(DefaultOperationMonitor.class.getName());

        private final Deque<Operation> operations = new ArrayDeque<>();
        private final ProgressHandle progressHandle;

        private int workUnits = 0;
        private int workUnit = 0;
        private boolean sizeIssueLogged = false;


        public DefaultOperationMonitor(ProgressHandle progressHandle, Set<TransferFile> forFiles) {
            if (progressHandle == null) {
                throw new IllegalStateException("Progress handle must be set");
            }
            this.progressHandle = progressHandle;
            workUnits = getWorkUnits(forFiles);
            // #237847
            if (workUnits < 0) {
                LOGGER.log(Level.WARNING, "Negative number of workunits {0} for transfer files {1}", new Object[] {workUnits, forFiles});
                workUnits = forFiles.size();
            }
        }

        @Override
        public void operationStart(Operation operation, Collection<TransferFile> forFiles) {
            if (operations.isEmpty()) {
                assert workUnits >= 0 : workUnits + " :: " + forFiles;
                progressHandle.start(workUnits);
            }
            operations.offerFirst(operation);
            if (operation == Operation.LIST) {
                progressHandle.progress(NbBundle.getMessage(RemoteCommand.class, "LBL_ListingFiles", forFiles.iterator().next().getName()));
                progressHandle.switchToIndeterminate();
            }
        }

        @Override
        public void operationProcess(Operation operation, TransferFile forFile) {
            int size = getSize(forFile);
            switch (operation) {
                case LIST:
                    if (size > 0) {
                        workUnits += size;
                    }
                    break;
                case UPLOAD:
                case DOWNLOAD:
                    if (size > 0) {
                        String processMessageKey = operation == Operation.DOWNLOAD ? "LBL_Downloading" : "LBL_Uploading"; // NOI18N
                        progressHandle.progress(NbBundle.getMessage(DefaultOperationMonitor.class, processMessageKey, forFile.getName()), workUnit);
                        workUnit += size;
                    }
                    break;
                default:
                    throw new IllegalStateException("Unsupported operation: " + operation);
            }
        }

        @Override
        public void operationFinish(Operation operation, Collection<TransferFile> forFiles) {
            operations.pollFirst();
            if (operation == Operation.LIST) {
                progressHandle.switchToDeterminate(workUnits);
                progressHandle.progress(workUnit);
            }
            if (operations.isEmpty()) {
                progressHandle.finish();
            }
        }

        @Override
        public void addUnits(Collection<TransferFile> files) {
            progressHandle.switchToIndeterminate();
            workUnits += getWorkUnits(files);
            progressHandle.switchToDeterminate(workUnits);
        }

        private int getWorkUnits(Collection<TransferFile> forFiles) {
            int size = 0;
            for (TransferFile file : forFiles) {
                size += getSize(file);
            }
            return size;
        }

        // #237847
        private int getSize(TransferFile file) {
            long size = file.getSize();
            if (size < 0) {
                if (!sizeIssueLogged) {
                    LOGGER.log(Level.WARNING, "Negative size {0} of transfer file {1}", new Object[] {size, file});
                    sizeIssueLogged = true;
                }
                return 1;
            }
            return (int) (size / 1024);
        }

    }

}
