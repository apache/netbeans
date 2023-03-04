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
package org.netbeans.modules.php.project.copysupport;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.util.List;
import java.util.Queue;
import java.util.Stack;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.modules.php.api.util.StringUtils;
import org.netbeans.modules.php.project.PhpProject;
import org.netbeans.modules.php.project.PhpProjectValidator;
import org.netbeans.modules.php.project.PhpVisibilityQuery;
import org.netbeans.modules.php.project.ProjectPropertiesSupport;
import org.netbeans.modules.php.project.connections.RemoteConnections;
import org.netbeans.modules.php.project.ui.actions.support.CommandUtils;
import org.netbeans.modules.php.project.util.PhpProjectUtils;
import org.netbeans.spi.project.ui.ProjectOpenedHook;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.filesystems.FileAttributeEvent;
import org.openide.filesystems.FileChangeAdapter;
import org.openide.filesystems.FileChangeListener;
import org.openide.filesystems.FileEvent;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileRenameEvent;
import org.openide.filesystems.FileStateInvalidException;
import org.openide.filesystems.FileSystem;
import org.openide.filesystems.FileUtil;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;
import org.openide.util.WeakListeners;

/**
 *
 * @author Radek Matous
 */
public final class CopySupport extends FileChangeAdapter implements PropertyChangeListener, FileChangeListener, ChangeListener {
    static final Logger LOGGER = Logger.getLogger(CopySupport.class.getName());

    public static final boolean ALLOW_BROKEN = Boolean.getBoolean(CopySupport.class.getName() + ".allowBroken"); // NOI18N

    private static final RequestProcessor COPY_SUPPORT_RP = new RequestProcessor("PHP file change handler (copy support)"); // NOI18N
    private static final int FILE_CHANGE_DELAY = 300; // ms
    private static final int PROPERTY_CHANGE_DELAY = 500; // ms
    private static final int PROGRESS_INITIAL_DELAY = 1000; // ms

    static final Queue<Callable<Boolean>> OPERATIONS_QUEUE = new ConcurrentLinkedQueue<>();
    static final RequestProcessor.Task COPY_TASK = createCopyTask();

    final PhpProject project;
    final PhpVisibilityQuery phpVisibilityQuery;

    private final RequestProcessor.Task initTask;
    // process property changes just once
    private final RequestProcessor.Task reinitTask;

    volatile boolean projectOpened = false;
    // #187060
    final AtomicInteger opened = new AtomicInteger();
    final AtomicInteger closed = new AtomicInteger();
    final Stack<Exception> callStack = new Stack<>();

    final AtomicBoolean sourcesValid = new AtomicBoolean(true);

    private final ProxyOperationFactory proxyOperationFactory;
    // @GuardedBy(this)
    private FileSystem fileSystem;
    // @GuardedBy(this)
    private FileChangeListener fileChangeListener;


    private CopySupport(final PhpProject project) {
        assert project != null;

        this.project = project;
        phpVisibilityQuery = PhpVisibilityQuery.forProject(project);
        proxyOperationFactory = new ProxyOperationFactory(project);

        initTask = COPY_SUPPORT_RP.create(new Runnable() {
            @Override
            public void run() {
                init(false);
            }
        });
        reinitTask = COPY_SUPPORT_RP.create(new Runnable() {
            @Override
            public void run() {
                init(true);
            }
        });
    }

    public static CopySupport getInstance(PhpProject project) {
        CopySupport copySupport = new CopySupport(project);
        ProjectPropertiesSupport.addWeakPropertyEvaluatorListener(project, copySupport);
        // XXX could be done in projectOpened() and projectClosed() but see #187060
        RemoteConnections remoteConnections = RemoteConnections.get();
        remoteConnections.addChangeListener(WeakListeners.change(copySupport, remoteConnections));
        return copySupport;
    }

    private static RequestProcessor.Task createCopyTask() {
        return COPY_SUPPORT_RP.create(new Runnable() {
            @Override
            public void run() {
                Callable<Boolean> operation = OPERATIONS_QUEUE.poll();
                while (operation != null) {
                    try {
                        operation.call();
                    } catch (Exception ex) {
                        LOGGER.log(Level.WARNING, null, ex);
                    }
                    operation = OPERATIONS_QUEUE.poll();
                }
                LOGGER.finest("COPY_TASK_FINISHED");
            }
        }, true);
    }

    public void projectOpened() {
        assert assertProjectOpened();

        LOGGER.log(Level.FINE, "Opening Copy support for project {0}", project.getName());

        projectOpened = true;
        proxyOperationFactory.reset();

        initTask.schedule(PROPERTY_CHANGE_DELAY);
    }

    public void projectClosed() {
        assert assertProjectClosed();

        LOGGER.log(Level.FINE, "Closing Copy support for project {0}", project.getName());

        projectOpened = false;
        proxyOperationFactory.reset();

        unregisterFileChangeListener();
    }

    // runs only under assertions
    private boolean assertProjectOpened() {
        opened.incrementAndGet();
        if (projectOpened) {
            throwProjectOpenedClosedError();
        }
        callStack.push(new Exception());
        return true;
    }

    // runs only under assertions
    private boolean assertProjectClosed() {
        closed.incrementAndGet();
        if (!projectOpened) {
            throwProjectOpenedClosedError();
        }
        callStack.pop();
        return true;
    }

    private void throwProjectOpenedClosedError() {
        int hooks = project.getLookup().lookupAll(ProjectOpenedHook.class).size();
        LOGGER.log(Level.INFO, "Number of ProjectOpenedHook classes in project lookup: {0}", hooks);

        LOGGER.log(Level.INFO, "Copy Support incorrectly opened/closed (opened: {0}, closed: {1})", new Object[] {opened.get(), closed.get()});
        Exception previous = callStack.peek();
        // #220893 - log the exception itself because the stacktrace is not in the log file (?!)
        LOGGER.log(Level.WARNING, "Stack trace of the previous call", previous);
        throw new IllegalStateException(previous);
    }

    private void prepareOperation(Callable<Boolean> callable) {
        if (callable != null) {
            OPERATIONS_QUEUE.offer(callable);
            COPY_TASK.schedule(FILE_CHANGE_DELAY);
        }
    }

    synchronized void init(boolean reinit) {
        String phase = reinit ? "REINIT" : "INIT"; // NOI18N
        LOGGER.log(Level.FINE, "Copy support {0} for project {1}", new Object[] {phase, project.getName()});

        // invalidate factories, e.g. remote client (it's better to simply create a new client)
        proxyOperationFactory.reset();

        if (proxyOperationFactory.isEnabled()) {
            Callable<Boolean> handler;
            if (reinit) {
                handler = proxyOperationFactory.createReinitHandler(getSources());
            } else {
                handler = proxyOperationFactory.createInitHandler(getSources());
            }
            prepareOperation(handler);
            registerFileChangeListener();
        } else {
            unregisterFileChangeListener();
        }
    }

    private void registerFileChangeListener() {
        LOGGER.log(Level.FINE, "Copy support REGISTERING FS listener for project {0}", project.getName());
        assert Thread.holdsLock(this);

        if (fileChangeListener != null) {
            LOGGER.log(Level.FINE, "\t-> not needed for project {0} (already registered)", project.getName());
            return;
        }
        if (ALLOW_BROKEN) {
            try {
                fileSystem = getSources().getFileSystem();
                fileChangeListener = FileUtil.weakFileChangeListener(this, fileSystem);
                fileSystem.addFileChangeListener(fileChangeListener);
                LOGGER.log(Level.FINE, "\t-> NON-RECURSIVE listener registered for project {0}", project.getName());
            } catch (FileStateInvalidException ex) {
                LOGGER.log(Level.WARNING, null, ex);
            }
        } else {
            fileChangeListener = new SourcesFileChangeListener(this);
            FileUtil.addRecursiveListener(fileChangeListener, FileUtil.toFile(getSources()), new Callable<Boolean>() {
                @Override
                public Boolean call() throws Exception {
                    boolean cancel = !projectOpened;
                    if (cancel) {
                        LOGGER.log(Level.INFO, "Adding of recursive listener interrupted for project {0}", project.getName());
                    }
                    return cancel;
                }
            });
            LOGGER.log(Level.FINE, "\t-> RECURSIVE listener registered for project {0}", project.getName());
        }
    }

    private synchronized void unregisterFileChangeListener() {
        LOGGER.log(Level.FINE, "Copy support UNREGISTERING FS listener for project {0}", project.getName());
        if (fileChangeListener == null) {
            LOGGER.log(Level.FINE, "\t-> not needed for project {0} (not registered)", project.getName());
        } else {
            if (ALLOW_BROKEN) {
                fileSystem.removeFileChangeListener(fileChangeListener);
                LOGGER.log(Level.FINE, "\t-> NON-RECURSIVE listener unregistered for project {0}", project.getName());
            } else {
                assert fileChangeListener instanceof SourcesFileChangeListener : "FS listener of incorrect type: " + fileChangeListener.getClass().getName();
                FileObject sources = getSources();
                if (sources == null) {
                    // broken project
                    unregisterFileChangeListenerFromOriginalSources();
                    return;
                }
                // #172777
                try {
                    FileUtil.removeRecursiveListener(fileChangeListener, FileUtil.toFile(sources));
                    LOGGER.log(Level.FINE, "\t-> RECURSIVE listener unregistered for project {0}", project.getName());
                } catch (IllegalArgumentException ex) {
                    LOGGER.log(Level.INFO,
                            "If this happens to you reliably, report issue with steps to reproduce and attach IDE log (http://netbeans.org/community/issues.html).", ex);
                    FileObject originalSources = ((SourcesFileChangeListener) fileChangeListener).getSources();
                    LOGGER.log(Level.INFO,
                            "registered sources (valid): {0} ({1}), current sources (valid): {2} ({3}), equals: {4}",
                            new Object[] {originalSources, originalSources.isValid(), sources, sources.isValid(), originalSources.equals(sources)});
                    unregisterFileChangeListenerFromOriginalSources();
                }
            }
            fileSystem = null;
            fileChangeListener = null;
        }
    }

    private void unregisterFileChangeListenerFromOriginalSources() {
        assert Thread.holdsLock(this);
        assert fileChangeListener instanceof SourcesFileChangeListener : "FS listener of incorrect type: " + fileChangeListener.getClass().getName();
        FileObject originalSources = ((SourcesFileChangeListener) fileChangeListener).getSources();
        assert originalSources != null : "Original sources should be found";
        File origSources = FileUtil.toFile(originalSources);
        try {
            FileUtil.removeRecursiveListener(fileChangeListener, origSources);
        } catch (IllegalArgumentException ex) {
            LOGGER.log(Level.FINE, null, ex);
        }
    }

    /**
     * @return {@literal true} if copying finished or user wants to continue
     */
    public boolean waitFinished() {
        return waitFinished(NbBundle.getMessage(CopySupport.class, "MSG_CopySupportRunning"), 200);
    }

    /**
     * @return {@literal true} if copying finished or user wants to continue
     */
    public boolean waitFinished(String message, long timeout, Object... additionalOptions) {
        try {
            if (!proxyOperationFactory.isEnabled()) {
                return true;
            }
            if (COPY_TASK.waitFinished(timeout)) {
                return true;
            }
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            return true;
        }
        NotifyDescriptor descriptor = new NotifyDescriptor.Confirmation(
                message,
                NotifyDescriptor.YES_NO_OPTION);
        if (additionalOptions != null) {
            descriptor.setAdditionalOptions(additionalOptions);
        }
        return DialogDisplayer.getDefault().notify(descriptor) == NotifyDescriptor.YES_OPTION;
    }

    @Override
    public void fileFolderCreated(FileEvent fe) {
        FileObject source = getValidProjectSource(fe, true);
        if (source == null) {
            return;
        }
        LOGGER.log(Level.FINE, "Processing event FOLDER CREATED for project {0}", project.getName());
        prepareOperation(proxyOperationFactory.createCopyHandler(source, fe));
    }

    @Override
    public void fileDataCreated(FileEvent fe) {
        FileObject source = getValidProjectSource(fe);
        if (source == null) {
            return;
        }
        LOGGER.log(Level.FINE, "Processing event DATA CREATED for project {0}", project.getName());
        prepareOperation(proxyOperationFactory.createCopyHandler(source, fe));
    }

    @Override
    public void fileChanged(FileEvent fe) {
        FileObject source = getValidProjectSource(fe);
        if (source == null) {
            return;
        }
        LOGGER.log(Level.FINE, "Processing event FILE CHANGED for project {0}", project.getName());
        prepareOperation(proxyOperationFactory.createCopyHandler(source, fe));
    }

    @Override
    public void fileDeleted(FileEvent fe) {
        FileObject source = getValidProjectSource(fe);
        if (source == null) {
            return;
        }
        LOGGER.log(Level.FINE, "Processing event FILE DELETED for project {0}", project.getName());
        prepareOperation(proxyOperationFactory.createDeleteHandler(source, fe));
    }

    @Override
    public void fileRenamed(FileRenameEvent fe) {
        FileObject source = getValidProjectSource(fe);
        if (source == null) {
            return;
        }
        LOGGER.log(Level.FINE, "Processing event FILE RENAMED for project {0}", project.getName());
        String originalName = fe.getName();
        String ext = fe.getExt();
        if (StringUtils.hasText(ext)) {
            originalName += "." + ext; // NOI18N
        }
        prepareOperation(proxyOperationFactory.createRenameHandler(source, originalName, fe));
    }

    @Override
    public void propertyChange(final PropertyChangeEvent propertyChangeEvent) {
        if (projectOpened) {
            LOGGER.log(Level.FINE, "Processing event PROPERTY CHANGE for opened project {0}", project.getName());
            reinitTask.schedule(PROPERTY_CHANGE_DELAY);
        }
    }

    @Override
    public void stateChanged(ChangeEvent e) {
        if (projectOpened) {
            LOGGER.log(Level.FINE, "Processing event STATE CHANGE (remote connections) for opened project {0}", project.getName());
            reinitTask.schedule(PROPERTY_CHANGE_DELAY);
        }
    }

    private FileObject getValidProjectSource(FileEvent fileEvent) {
        return getValidProjectSource(fileEvent, false);
    }

    private FileObject getValidProjectSource(FileEvent fileEvent, boolean folderCreated) {
        if (!isSourceRootValid()) {
            LOGGER.log(Level.INFO, "Source root not valid for project {0} -> ignoring FS event {1}", new Object[] {project.getName(), fileEvent});
            if (sourcesValid.compareAndSet(true, false)) {
                warnInvalidSourceRoot();
            }
            return null;
        }
        FileObject source = fileEvent.getFile();
        if (sourcesValid.compareAndSet(false, true)) {
            boolean ignoreEvent = false;
            if (folderCreated) {
                File oldSources = FileUtil.toFile(getSources());
                if (FileUtil.toFile(source).equals(oldSources)) {
                    // ignore this event since the sources were just restored (otherwise all files would be copied to the server)
                    ignoreEvent = true;
                }
            }
            // need to get file object once more since the current one is invalid
            project.getSourceRoots().refresh();
            if (ignoreEvent) {
                LOGGER.log(Level.INFO, "Previously invalid source root restored for project {0} -> to avoid copying all files to the server ignoring FS event {1}",
                        new Object[] {project.getName(), fileEvent});
                return null;
            }
        }
        LOGGER.log(Level.FINEST, "Getting source file for project {0} from {1}", new Object[] {project.getName(), fileEvent});
        if (!PhpProjectUtils.isVisible(phpVisibilityQuery, source)) {
            LOGGER.finest("\t-> null (invisible source)");
            return null;
        }
        if (!CommandUtils.isUnderSources(project, source)) {
            LOGGER.finest("\t-> null (invalid source)");
            return null;
        }
        LOGGER.log(Level.FINE, "Got source file for project {0} from {1}", new Object[] {project.getName(), fileEvent});
        return source;
    }

    // #212495 - project files deleted on server when network drive is unmapped
    private boolean isSourceRootValid() {
        FileObject sources = getSources();
        if (sources == null) {
            // #220803
            return false;
        }
        File sourceFiles = FileUtil.toFile(sources);
        return sourceFiles != null && sourceFiles.isDirectory();
    }

    FileObject getSources() {
        return ProjectPropertiesSupport.getSourcesDirectory(project);
    }

    @NbBundle.Messages({
        "# {0} - project name",
        "CopySupport.warn.invalidSources=<html>Source Files of project \"{0}\" do not exist, file changes are not propagated to the server.<br><br>"
            + "Use \"Resolve Project Problems...\" action to repair the project."
    })
    private void warnInvalidSourceRoot() {
        NotifyDescriptor descriptor = new NotifyDescriptor.Message(
                Bundle.CopySupport_warn_invalidSources(project.getName()),
                NotifyDescriptor.WARNING_MESSAGE);
        DialogDisplayer.getDefault().notifyLater(descriptor);
    }


    private static class ProxyOperationFactory extends FileOperationFactory {

        static final int SHOW_FAILED_FILES_DELAY = 3000;

        final FileOperationFactory localFactory;
        final FileOperationFactory remoteFactory;
        final List<String> localFailedFiles = new CopyOnWriteArrayList<>();
        final List<String> remoteFailedFiles = new CopyOnWriteArrayList<>();
        final RequestProcessor.Task showFailedFilesTask;

        // failed files (local and remote copying)
        volatile boolean userAskedLocalCopying = false;
        volatile boolean userAskedRemoteCopying = false;


        ProxyOperationFactory(final PhpProject project) {
            super(project);
            this.localFactory = new LocalOperationFactory(project);
            this.remoteFactory = new RemoteOperationFactory(project);
            showFailedFilesTask = COPY_SUPPORT_RP.create(new Runnable() {
                @Override
                public void run() {
                    if (!localFailedFiles.isEmpty()) {
                        FailedFilesPanel.local(ProjectUtils.getInformation(project).getDisplayName(), localFailedFiles);
                        localFailedFiles.clear();
                    }
                    if (!remoteFailedFiles.isEmpty()) {
                        FailedFilesPanel.remote(ProjectUtils.getInformation(project).getDisplayName(), remoteFailedFiles);
                        remoteFailedFiles.clear();
                    }
                }
            }, true);
        }

        @Override
        protected void resetInternal() {
            localFactory.reset();
            remoteFactory.reset();
            userAskedLocalCopying = false;
            userAskedRemoteCopying = false;
        }

        @Override
        Logger getLogger() {
            return LOGGER;
        }

        @Override
        protected boolean isEnabled() {
            if (PhpProjectValidator.isFatallyBroken(project)) {
                LOGGER.log(Level.INFO, "Copy support disabled for project without sources ({0})", project.getName());
                return false;
            }
            return localFactory.isEnabled()
                    || remoteFactory.isEnabled();
        }

        @Override
        protected Callable<Boolean> createInitHandlerInternal(FileObject source) {
            return createHandler(source, localFactory.createInitHandler(source), remoteFactory.createInitHandler(source));
        }

        @Override
        protected Callable<Boolean> createReinitHandlerInternal(FileObject source) {
            return createHandler(source, localFactory.createReinitHandler(source), remoteFactory.createReinitHandler(source));
        }

        @Override
        protected Callable<Boolean> createCopyHandlerInternal(FileObject source, FileEvent fileEvent) {
            return createHandler(source, localFactory.createCopyHandler(source, fileEvent), remoteFactory.createCopyHandler(source, fileEvent));
        }

        @Override
        protected Callable<Boolean> createRenameHandlerInternal(FileObject source, String oldName, FileRenameEvent fileRenameEvent) {
            return createHandler(source, localFactory.createRenameHandler(source, oldName, fileRenameEvent), remoteFactory.createRenameHandler(source, oldName, fileRenameEvent));
        }

        @Override
        protected Callable<Boolean> createDeleteHandlerInternal(FileObject source, FileEvent fileEvent) {
            return createHandler(source, localFactory.createDeleteHandler(source, fileEvent), remoteFactory.createDeleteHandler(source, fileEvent));
        }

        private Callable<Boolean> createHandler(FileObject source, Callable<Boolean> localHandler, Callable<Boolean> remoteHandler) {
            if (localHandler == null && remoteHandler == null) {
                LOGGER.fine("No handler given");
                return null;
            }
            return new ProxyHandler(source, localHandler, remoteHandler);
        }

        @Override
        protected boolean isValid(FileEvent fileEvent) {
            return true;
        }

        private final class ProxyHandler implements Callable<Boolean> {

            private final FileObject source;
            private final Callable<Boolean> localHandler;
            private final Callable<Boolean> remoteHandler;


            public ProxyHandler(FileObject source, Callable<Boolean> localHandler, Callable<Boolean> remoteHandler) {
                this.source = source;
                this.localHandler = localHandler;
                this.remoteHandler = remoteHandler;
            }

            @org.netbeans.api.annotations.common.SuppressWarnings("NP_BOOLEAN_RETURN_NULL")
            @Override
            public Boolean call() throws Exception {
                Boolean localRetval = callLocal();
                Boolean remoteRetval = callRemote();
                showFailedFilesTask.schedule(SHOW_FAILED_FILES_DELAY);
                if (localRetval == null && remoteRetval == null) {
                    return null;
                }
                if (localRetval != null && !localRetval) {
                    return false;
                }
                if (remoteRetval != null && !remoteRetval) {
                    return false;
                }
                return true;
            }

            private Boolean callLocal() {
                Boolean localRetval = null;
                Exception localExc = null;

                if (localHandler != null) {
                    LOGGER.log(Level.FINE, "Processing LOCAL copying handler for project {0}", project.getName());

                    ProgressHandle progress = ProgressHandle.createHandle(NbBundle.getMessage(CopySupport.class, "LBL_LocalSynchronization"));
                    progress.setInitialDelay(PROGRESS_INITIAL_DELAY);
                    try {
                        progress.start();
                        localRetval = localHandler.call();
                    } catch (Exception exc) {
                        LOGGER.log(Level.INFO, "LOCAL copying fail: ", exc);
                        localRetval = false;
                        localExc = exc;
                    } finally {
                        progress.finish();
                    }
                }
                if (localRetval != null && !localRetval) {
                    String pathInfo = getPathInfo(source);
                    if (pathInfo != null) {
                        localFailedFiles.add(pathInfo);
                    }
                    if (!userAskedLocalCopying) {
                        userAskedLocalCopying = true;
                        if (askUser(NbBundle.getMessage(CopySupport.class, "LBL_Copy_Support_Fail", project.getName()))) {
                            localFactory.invalidate();
                            localFailedFiles.clear();
                            LOGGER.log(Level.INFO, String.format("LOCAL copying for project %s disabled by user", project.getName()), localExc);
                        } else {
                            LOGGER.log(Level.INFO, String.format("LOCAL copying for project %s failed but not disabled by user => resetting", project.getName()), localExc);
                            localFactory.reset();
                        }
                    }
                }
                return localRetval;
            }

            private Boolean callRemote() {
                Boolean remoteRetval = null;
                Exception remoteExc = null;

                if (remoteHandler != null) {
                    LOGGER.log(Level.FINE, "Processing REMOTE copying handler for project {0}", project.getName());

                    ProgressHandle progress = ProgressHandle.createHandle(NbBundle.getMessage(CopySupport.class, "LBL_RemoteSynchronization"));
                    progress.setInitialDelay(PROGRESS_INITIAL_DELAY);
                    try {
                        progress.start();
                        remoteRetval = remoteHandler.call();
                    } catch (Exception exc) {
                        LOGGER.log(Level.INFO, "REMOTE copying fail: ", exc);
                        remoteRetval = false;
                        remoteExc = exc;
                    } finally {
                        progress.finish();
                    }
                    if (remoteRetval != null && !remoteRetval) {
                        String pathInfo = getPathInfo(source);
                        if (pathInfo != null) {
                            remoteFailedFiles.add(pathInfo);
                        }
                        if (!userAskedRemoteCopying) {
                            userAskedRemoteCopying = true;
                            // disconnect remote client
                            remoteFactory.reset();
                            if (askUser(NbBundle.getMessage(CopySupport.class, "LBL_Remote_On_Save_Fail", project.getName()))) {
                                // invalidate factory
                                remoteFactory.invalidate();
                                remoteFailedFiles.clear();
                                LOGGER.log(Level.INFO, String.format("REMOTE copying for project %s disabled by user", project.getName()), remoteExc);
                            } else {
                                LOGGER.log(Level.INFO, String.format("REMOTE copying for project %s failed but not disabled by user => resetting", project.getName()), remoteExc);
                            }
                        }
                    }
                }
                return remoteRetval;
            }

            private String getPathInfo(FileObject file) {
                FileObject sources = ProjectPropertiesSupport.getSourcesDirectory(project);
                if (sources == null) {
                    // broken project
                    return null;
                }
                String relativePath = FileUtil.getRelativePath(sources, file);
                if (relativePath != null) {
                    return relativePath;
                }
                assert false : "Should be able to get relative path for copied file";
                return file.getNameExt();
            }

        }
    }

    // #172777
    private static class SourcesFileChangeListener implements FileChangeListener {
        private final CopySupport copySupport;
        private final FileObject sources;

        public SourcesFileChangeListener(CopySupport copySupport) {
            this.copySupport = copySupport;
            this.sources = copySupport.getSources();
        }

        public FileObject getSources() {
            return sources;
        }

        @Override
        public void fileFolderCreated(FileEvent fe) {
            copySupport.fileFolderCreated(fe);
        }

        @Override
        public void fileDataCreated(FileEvent fe) {
            copySupport.fileDataCreated(fe);
        }

        @Override
        public void fileChanged(FileEvent fe) {
            copySupport.fileChanged(fe);
        }

        @Override
        public void fileDeleted(FileEvent fe) {
            copySupport.fileDeleted(fe);
        }

        @Override
        public void fileRenamed(FileRenameEvent fe) {
            copySupport.fileRenamed(fe);
        }

        @Override
        public void fileAttributeChanged(FileAttributeEvent fe) {
            copySupport.fileAttributeChanged(fe);
        }
    }
}
