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
package org.netbeans.modules.cnd.modelimpl.csm.core;

import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.netbeans.modules.cnd.api.model.CsmFile;
import org.netbeans.modules.cnd.api.project.NativeFileItem;
import org.netbeans.modules.cnd.apt.support.APTDriver;
import org.netbeans.modules.cnd.apt.support.APTFileCacheManager;
import org.netbeans.modules.cnd.apt.support.ClankDriver;
import org.netbeans.modules.cnd.modelimpl.debug.Diagnostic;
import org.netbeans.modules.cnd.modelimpl.debug.DiagnosticExceptoins;
import org.netbeans.modules.cnd.modelimpl.debug.TraceFlags;
import org.netbeans.modules.cnd.modelimpl.platform.FileBufferSnapshot2;
import org.netbeans.modules.cnd.repository.spi.Key;
import org.netbeans.modules.cnd.repository.spi.RepositoryDataInput;
import org.netbeans.modules.cnd.support.Interrupter;
import org.netbeans.modules.parsing.api.Snapshot;
import org.openide.filesystems.FileSystem;
import org.openide.util.RequestProcessor;

/**
 *
 */
public abstract class ProjectBaseWithEditing extends ProjectBase {
    // remove as soon as TraceFlags.USE_PARSER_API becomes always true
    private final Map<CsmFile, EditingTask> editedFiles = new HashMap<>();
    private final ConcurrentHashMap<CsmFile, Boolean> modifiedFiles = new ConcurrentHashMap<>();
    private final static RequestProcessor RP = new RequestProcessor("ProjectImpl RP", 50); // NOI18N

    protected ProjectBaseWithEditing(ModelImpl model, FileSystem fs, Object platformProject, CharSequence name, Key key) {
        super(model, fs, platformProject, name, key);
    }
    public ProjectBaseWithEditing(RepositoryDataInput input) throws IOException {
        super(input);
    }

    @Override
    public final void onFileEditStart(final FileBuffer buf, NativeFileItem nativeFile) {
        if (TraceFlags.DEBUG) {
            Diagnostic.trace("------------------------- onFileEditSTART " + buf.getUrl()); //NOI18N
        }
        final FileImpl impl = getFile(buf.getAbsolutePath(), false);
        if (impl != null) {
            APTDriver.invalidateAPT(buf);
            ClankDriver.invalidate(buf);
            APTFileCacheManager.getInstance(buf.getFileSystem()).invalidate(buf.getAbsolutePath());
            // listener will be triggered immediately, because editor based buffer
            // will be notifies about editing event exactly after onFileEditStart
            final ChangeListener changeListener = new ChangeListener() {
                @Override
                public void stateChanged(ChangeEvent e) {
                    scheduleParseOnEditing(impl);
                }
            };
            synchronized (editedFiles) {
                if (TraceFlags.TRACE_182342_BUG || TraceFlags.TRACE_191307_BUG) {
                    for (CsmFile csmFile : editedFiles.keySet()) {
                        System.err.println("onFileEditStart: edited file " + csmFile);
                    }
                    System.err.println("onFileEditStart: current file " + impl);
                }
                // sync set buffer as well
                impl.setBuffer(buf);
                if (!editedFiles.containsKey(impl)) {
                    // register edited file
                    editedFiles.put(impl, new EditingTask(buf, changeListener));
                }
                scheduleParseOnEditing(impl);
            }
        }
    }
    
    @Override
    public final void onFileEditEnd(FileBuffer buf, NativeFileItem nativeFile, boolean undo) {
        if (TraceFlags.DEBUG) {
            Diagnostic.trace("------------------------- onFileEditEND " + buf.getUrl()); //NOI18N
        }
        FileImpl file = getFile(buf.getAbsolutePath(), false);
        if (file != null) {
            synchronized (editedFiles) {
                if (TraceFlags.TRACE_182342_BUG || TraceFlags.TRACE_191307_BUG) {
                    for (CsmFile csmFile : editedFiles.keySet()) {
                        System.err.println("onFileEditEnd: edited file " + csmFile);
                    }
                    System.err.println("onFileEditEnd: " + (undo ? "undo" : "save") + " current file " + file);
                }
                EditingTask task = editedFiles.remove(file);
                if (task != null) {
                    task.cancelTask();
                } else {
                    // FixUp double file edit end on mounted files
                    return;
                }
                // sync set buffer as well
                file.setBuffer(buf);
            }
//            file.clearStateCache();
            // no need for deep parsing util call here in case of save, because it will be called as external notification change anyway
            if (undo) {
                // but we need to call in case of undo when there are no external modifications
                DeepReparsingUtils.reparseOnUndoEditedFile(this, file);
            }
        }
    }

    @Override
    protected final void addModifiedFile(FileImpl file) {
        if (!isDisposing()) {
            modifiedFiles.put(file, Boolean.TRUE);
        }
    }

    @Override
    protected final void removeModifiedFile(FileImpl file) {
        modifiedFiles.remove(file);
    }

    @Override
    public final void onFileImplRemoved(Collection<FileImpl> physicallyRemoved, Collection<FileImpl> excluded) {
        try {
            synchronized (editedFiles) {
                if (!editedFiles.isEmpty()) {
                    Set<FileImpl> files = new HashSet<>(physicallyRemoved);
                    files.addAll(excluded);
                    for (FileImpl impl : files) {
                        EditingTask task = editedFiles.remove(impl);
                        if (task != null) {
                            task.cancelTask();
                        }
                    }
                }
            }
        } finally {
            super.onFileImplRemoved(physicallyRemoved, excluded);
        }
    }

    @Override
    protected final void ensureChangedFilesEnqueued() {
        Set<FileImpl> addToParse = new HashSet<>();
        synchronized (editedFiles) {
            super.ensureChangedFilesEnqueued();
            for (Iterator<CsmFile> iter = editedFiles.keySet().iterator(); iter.hasNext();) {
                FileImpl file = (FileImpl) iter.next();
                if (!file.isParsingOrParsed()) {
                    addToParse.add(file);
                }
            }
            for (Iterator<CsmFile> iter = modifiedFiles.keySet().iterator(); iter.hasNext();) {
                FileImpl file = (FileImpl) iter.next();
                if (!file.isParsingOrParsed()) {
                    if (WAIT_PARSE_LOGGER.isLoggable(Level.FINE)) {
                        WAIT_PARSE_LOGGER.fine("### Added modified file " + file);
                    }
                    addToParse.add(file);
                }
            }
        }
        for (FileImpl file : addToParse) {
            ParserQueue.instance().add(file, getPreprocHandlersForParse(file, Interrupter.DUMMY), ParserQueue.Position.TAIL);
        }
        //N.B. don't clear list of editedFiles here.
    }

    @Override
    protected final boolean hasChangedFiles(CsmFile skipFile) {
        if (skipFile == null) {
            return false;
        }
        synchronized (editedFiles) {
            for (Iterator iter = editedFiles.keySet().iterator(); iter.hasNext();) {
                FileImpl file = (FileImpl) iter.next();
                if ((skipFile != file) && !file.isParsingOrParsed()) {
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    protected final boolean hasEditedFiles() {
        synchronized (editedFiles) {
            return !editedFiles.isEmpty();
        }
    }

    @Override
    protected void onDispose() {
        super.onDispose();
        editedFiles.clear();
        modifiedFiles.clear();
    }

    @Override
    public final void onSnapshotChanged(FileImpl file, Snapshot snapshot) {
        //file.markReparseNeeded(false);
        FileBufferSnapshot2 fb = new FileBufferSnapshot2(snapshot, System.currentTimeMillis());
        file.setBuffer(fb);
        DeepReparsingUtils.reparseOnEditingFile(this, file);
    }

    @Override
    public void setDisposed() {
        super.setDisposed();
        synchronized (editedFiles) {
            for (EditingTask task : editedFiles.values()) {
                task.cancelTask();
            }
            editedFiles.clear();
        }
    }

    ////////////////////////////////////////////////////////////////////////////
    private void scheduleParseOnEditing(final FileImpl file) {
        RequestProcessor.Task task;
        int delay;
        synchronized (editedFiles) {
            if (TraceFlags.TRACE_182342_BUG || TraceFlags.TRACE_191307_BUG) {
                new Exception("scheduleParseOnEditing " + file).printStackTrace(System.err); // NOI18N
            }
            EditingTask pair = editedFiles.get(file);
            if (pair == null) {
                // we were removed between rescheduling and finish of edit
                if (TraceFlags.TRACE_182342_BUG || TraceFlags.TRACE_191307_BUG) {
                    System.err.println("scheduleParseOnEditing: file was removed " + file);
                }
                return;
            }
            if (!pair.updateLastModified()) {
                // no need to schedule the second parse
                if (TraceFlags.TRACE_182342_BUG || TraceFlags.TRACE_191307_BUG) {
                    System.err.println("scheduleParseOnEditing: no updates " + file + " : " + pair.lastModified);
                }
                return;
            }

            // markReparseNeeded have to be called synchroniously
            // otherwise it will be delayed till DeepReparsingUtils.reparseOnEditingFile is called from task
            // but task is delayed (or even turned off), so this could never happen and client
            // using CsmFile.scheduleParsing(true) get file without wait for reparsing, because
            // file is still in state PARSED if delayed till task starts execution
            // see #203526 - Code completion is empty if typing too fast
            file.markReparseNeeded(false);
            task = pair.getTask();
            if (task == null) {
                if (TraceFlags.TRACE_182342_BUG || TraceFlags.TRACE_191307_BUG) {
                    for (CsmFile csmFile : editedFiles.keySet()) {
                        System.err.println("scheduleParseOnEditing: edited file " + csmFile);
                    }
                    System.err.println("scheduleParseOnEditing: current file " + file);
                }
                task = RP.create(new Runnable() {

                    @Override
                    public void run() {
                        try {
                            if (TraceFlags.TRACE_182342_BUG || TraceFlags.TRACE_191307_BUG) {
                                System.err.printf("scheduleParseOnEditing: RUN scheduleParseOnEditing task for %s%n", file);
                            }
                            if (isDisposing()) {
                                return;
                            }
                            DeepReparsingUtils.reparseOnEditingFile(ProjectBaseWithEditing.this, file);
                        } catch (AssertionError ex) {
                            DiagnosticExceptoins.register(ex);
                        } catch (Exception ex) {
                            DiagnosticExceptoins.register(ex);
                        }
                    }
                }, true);
                task.setPriority(Thread.MIN_PRIORITY);
                pair.setTask(task);
            } else {
                if (TraceFlags.TRACE_182342_BUG || TraceFlags.TRACE_191307_BUG) {
                    for (CsmFile csmFile : editedFiles.keySet()) {
                        System.err.println("reschedule in scheduleParseOnEditing: edited file " + csmFile);
                    }
                    System.err.println("reschedule in scheduleParseOnEditing: current file " + file);
                }
            }
            delay = TraceFlags.REPARSE_DELAY;
            if (TraceFlags.REPARSE_ON_DOCUMENT_CHANGED) {
                if (file.getLastParseTime() / (delay+1) > 2) {
                    delay = Math.max(delay, file.getLastParseTime()+2000);
                }
            } else {
                delay = Integer.MAX_VALUE;
            }
        }
        // to prevent frequent re-post
        if (task.getDelay() < Math.max(100, delay - 100)) {
            task.schedule(delay);
        }
    }

    // remove as soon as TraceFlags.USE_PARSER_API becomes always true
    private final static class EditingTask {
        // field is synchronized by editedFiles lock
        private RequestProcessor.Task task;
        private final ChangeListener bufListener;
        private final FileBuffer buf;
        private long lastModified = -1;

        public EditingTask(final FileBuffer buf, ChangeListener bufListener) {
            assert (bufListener != null);
            this.bufListener = bufListener;
            assert (buf != null);
            this.buf = buf;
            this.buf.addChangeListener(bufListener);
        }

        public boolean updateLastModified() {
            long lm = this.buf.lastModified();
            if (this.lastModified == lm) {
                return false;
            }
            if (TraceFlags.TRACE_182342_BUG || TraceFlags.TRACE_191307_BUG) {
                System.err.printf("EditingTask.updateLastModified: set lastModified from %d to %d%n", this.lastModified, lm);// NOI18N
            }
            this.lastModified = lm;
            return true;
        }

        public void setTask(RequestProcessor.Task task) {
            if (TraceFlags.TRACE_182342_BUG || TraceFlags.TRACE_191307_BUG) {
                System.err.printf("EditingTask.setTask: set new EditingTask %d for %s%n", task.hashCode(), buf.getUrl());
            }
            this.task = task;
        }

        public void cancelTask() {
            if (this.task != null) {
                if (TraceFlags.TRACE_182342_BUG || TraceFlags.TRACE_191307_BUG) {
                    if (!task.isFinished()) {
                        new Exception("EditingTask.cancelTask: cancelling previous EditingTask " + task.hashCode()).printStackTrace(System.err); // NOI18N
                    } else {
                        new Exception("EditingTask.cancelTask: cancelTask where EditingTask was finished " + task.hashCode()).printStackTrace(System.err); // NOI18N
                    }
                }
                try {
                    this.task.cancel();
                } catch (Throwable ex) {
                    System.err.println("EditingTask.cancelTask: cancelled with exception:");
                    ex.printStackTrace(System.err);
                }
            }
            this.buf.removeChangeListener(bufListener);
        }

        private RequestProcessor.Task getTask() {
            return this.task;
        }
    }
}
