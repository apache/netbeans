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
package org.netbeans.modules.cnd.lsp.compilationdb;

import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.modules.cnd.api.project.NativeFileItem;
import org.netbeans.modules.cnd.api.project.NativeProject;
import org.netbeans.modules.cnd.api.project.NativeProjectItemsListener;
import org.netbeans.modules.cnd.makeproject.api.MakeProject;
import org.netbeans.modules.cnd.makeproject.api.support.MakeProjectEvent;
import org.netbeans.modules.cnd.makeproject.api.support.MakeProjectListener;
import org.netbeans.spi.project.ui.ProjectOpenedHook;
import org.openide.util.RequestProcessor;

/**
 * ClangCDBSupport is responsible for detecting and generating a JSON
 * compilation database, as defined in
 * https://clang.llvm.org/docs/JSONCompilationDatabase.html
 *
 * When "ProjectOpenedHook.projectOpened" is fired, i.e., when the project is
 * opened, we start listening to NativeProjectItemsListener and
 * MakeProjectListener.
 *
 * We remove these listeners when the project is closed or deleted.
 *
 * The compilation database is regenerated after COALESCING_DELAY_MS
 * milliseconds. Requests are coalesced using a "pendingCauses" blocking queue.
 */
public final class ClangCDBSupport
        extends ProjectOpenedHook
        implements
        NativeProjectItemsListener, MakeProjectListener {

    private static final Logger LOG = Logger.getLogger(ClangCDBSupport.class.getName());
    private static final RequestProcessor CLANG_CDB_PROCESSOR = new RequestProcessor(ClangCDBSupport.class.getName());
    private static final long COALESCING_DELAY_MS = 1000;
    private final MakeProject makeProject;
    private final BlockingQueue<ClangCDBGenerationCause> pendingCauses;
    private boolean projectOpen;

    public ClangCDBSupport(final MakeProject makeProject) {
        this.makeProject = makeProject;
        this.projectOpen = false;
        this.pendingCauses = new LinkedBlockingQueue<>();
    }

    /**
     * Called when multiple files are added to the project.
     *
     * @param fileItems the list of file items that was added.
     */
    @Override
    public void filesAdded(List<NativeFileItem> fileItems) {
        updateCompilationDatabase(ClangCDBGenerationCause.FILES_ADDED);
    }

    /**
     * Called when multiple files are removed from the project.
     *
     * @param fileItems the list of file items that was added.
     */
    @Override
    public void filesRemoved(List<NativeFileItem> fileItems) {
        updateCompilationDatabase(ClangCDBGenerationCause.FILES_REMOVED);
    }

    /**
     * Called when include paths or macro definitions have changed (and files
     * needs to be re-parsed) for multiple files.
     *
     * @param fileItems the list of file items that has changed.
     */
    @Override
    public void filesPropertiesChanged(List<NativeFileItem> fileItems) {
        updateCompilationDatabase(ClangCDBGenerationCause.INCLUDES_MACROS_CHANGED);
    }

    /**
     * Called when include paths or macro definitions have changed (and files
     * needs to be re-parsed) for all files in project.
     *
     * @param nativeProject project whose properties have changed
     */
    @Override
    public void filesPropertiesChanged(NativeProject nativeProject) {
        updateCompilationDatabase(ClangCDBGenerationCause.INCLUDES_MACROS_CHANGED);
    }

    @Override
    public void fileRenamed(String oldPath, NativeFileItem newFileIetm) {
        updateCompilationDatabase(ClangCDBGenerationCause.FILES_RENAMED);
    }

    /**
     * notifies about intensive file operations to be started. We ignore these.
     *
     * @param nativeProject
     */
    @Override
    public void fileOperationsStarted(NativeProject nativeProject) {
        LOG.log(Level.FINE, "File operations started.");
    }

    /**
     * notifies about intensive file operations finished. We ignore these
     *
     * @param nativeProject
     */
    @Override
    public void fileOperationsFinished(NativeProject nativeProject) {
        LOG.log(Level.FINE, "File operations finished.");
    }

    @Override
    public void configurationXmlChanged(MakeProjectEvent ev) {
        updateCompilationDatabase(ClangCDBGenerationCause.PROJECT_CONFIGURATION_CHANGED);
    }

    /**
     * Ignored. See javadoc
     * @param ev 
     */
    @Override
    public void propertiesChanged(MakeProjectEvent ev) {
        LOG.log(Level.FINE, "Some project properties changed {0}",
                ev.getPath());
    }

    @Override
    protected void projectOpened() {
        LOG.log(Level.FINE, "Project opened");
        projectOpen = true;
        makeProject.getHelper().addMakeProjectListener(this);
        NativeProject nativeProject = makeProject.getLookup().lookup(NativeProject.class);
        if (nativeProject != null) {
            nativeProject.addProjectItemsListener(this);
        } else {
            LOG.log(Level.FINE, "Cannot attach to native project events.");
        }
        updateCompilationDatabase(ClangCDBGenerationCause.PROJECT_OPENED);
    }

    @Override
    protected void projectClosed() {
        LOG.log(Level.FINE, "Project closed");
        removeListenersOnCloseOrDelete();
    }

    @Override
    public void projectDeleted(NativeProject nativeProject) {
        LOG.log(Level.FINE, "Project deleted");
        removeListenersOnCloseOrDelete();
    }

    private void removeListenersOnCloseOrDelete() {
        projectOpen = false;
        makeProject.getHelper().removeMakeProjectListener(this);
        NativeProject nativeProject = makeProject.getLookup().lookup(NativeProject.class);
        if (nativeProject != null) {
            nativeProject.removeProjectItemsListener(this);
        } else {
            LOG.log(Level.FINE, "Cannot detach from native project events.");
        }
    }

    /**
     * Updates the compilation database "[project]/compile_commands.json". If a
     * task is already running then the request is silently ignored.
     *
     * @param cause The cause that originated the need to recreate the
     * compilation database.
     */
    private void updateCompilationDatabase(ClangCDBGenerationCause cause) {
        LOG.log(Level.FINE, "Update compilation database project is open: {0}", projectOpen);
        if (projectOpen) {
            pendingCauses.add(cause);
            ClangCDBGenerationTask task = null;
            try {
                task = new ClangCDBGenerationTask(this, pendingCauses);
            } catch (IllegalArgumentException iae) {
                LOG.log(Level.SEVERE, "Unable to create ClangCDBGenerationTask: {0}", iae.getMessage());
                return;
            }
            // We fire the regeneration task after a COALESCING_DELAY_MS milliseconds, to
            // coalesce requests (for instance, when many files are added or removed).
            CLANG_CDB_PROCESSOR.schedule(task, COALESCING_DELAY_MS, TimeUnit.MILLISECONDS);
        }
    }

    MakeProject getMakeProject() {
        return makeProject;
    }

    boolean isOpen() {
        return projectOpen;
    }

}
