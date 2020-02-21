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
package org.netbeans.modules.cnd.modelimpl.platform;

import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.modules.cnd.api.model.CsmFile;
import org.netbeans.modules.cnd.api.model.CsmProject;
import org.netbeans.modules.cnd.modelimpl.csm.core.FileImpl;
import org.netbeans.modules.cnd.modelimpl.csm.core.ModelImpl;
import org.netbeans.modules.cnd.modelimpl.csm.core.ProjectBase;
import org.netbeans.modules.cnd.modelimpl.debug.TraceFlags;
import org.netbeans.modules.cnd.modelutil.CsmUtilities;
import org.netbeans.modules.cnd.utils.CndPathUtilities;
import org.netbeans.modules.cnd.utils.CndUtils;
import org.netbeans.modules.cnd.utils.FSPath;
import org.netbeans.modules.cnd.utils.MIMENames;
import org.netbeans.modules.dlight.libs.common.InvalidFileObjectSupport;
import org.openide.filesystems.FileChangeAdapter;
import org.openide.filesystems.FileEvent;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileRenameEvent;
import org.openide.filesystems.FileUtil;

/**
 *
 */
/*package*/final class ExternalUpdateListener extends FileChangeAdapter implements Runnable {
    /*package*/static final Logger LOG = Logger.getLogger("ExternalUpdateListener"); // NOI18N
    private final ModelSupport modelSupport;
    private enum EventKind {
        CREATED,
        CHANGED,
        DELETED
    }

    private static final class Pair {
        private final EventKind kind;
        private final FileEvent fe;

        public Pair(EventKind kind, FileEvent fe) {
            this.kind = kind;
            this.fe = fe;
        }

        @Override
        public String toString() {
            return "Pair{" + "kind=" + kind + ", fe=" + fe + '}'; // NOI18N
        }
    }
    private volatile LinkedList<Pair> events = new LinkedList<>();
    private final Object eventsLock = new Object();
    
    ExternalUpdateListener(final ModelSupport outer) {
        this.modelSupport = outer;
        if (TraceFlags.MERGE_EVENTS) {
            CndUtils.assertTrue(false, "ExternalUpdateListener shoudl not be used if MERGE_EVENTS flag is set"); //NOI18N
        }
    }

    /** FileChangeListener implementation. Fired when a file is changed. */
    @Override
    public void fileChanged(FileEvent fe) {
        if (TraceFlags.TRACE_EXTERNAL_CHANGES) {
            LOG.log(Level.INFO, "External updates: try to register fileChanged {0}", fe);
        }
        register(fe, EventKind.CHANGED);
    }

    @Override
    public void fileDataCreated(FileEvent fe) {
        if (TraceFlags.TRACE_EXTERNAL_CHANGES) {
            LOG.log(Level.INFO, "External updates: try to register fileDataCreated {0}", fe);
        }
        register(fe, EventKind.CREATED);
    }

    @Override
    public void fileRenamed(FileRenameEvent fe) {
        if (TraceFlags.TRACE_EXTERNAL_CHANGES) {
            LOG.log(Level.INFO, "External updates: try to register fileRenamed {0}", fe);
        }     
        final ModelImpl model = modelSupport.getModel();
        if (model != null) {
            final FileObject fo = fe.getFile();
            if (isCOrCpp(fo)) {
                FSPath newPath = FSPath.toFSPath(fo);
                String strPrevExt = (fe.getExt() == null || fe.getExt().isEmpty()) ? "" : "." + fe.getExt(); // NOI18N
                String strPrevPath = CndPathUtilities.getDirName(newPath.getPath()) + '/' + fe.getName() + strPrevExt; // NOI18N
                FSPath prevPath = new FSPath(newPath.getFileSystem(), strPrevPath);        
                FileObject removedFO = InvalidFileObjectSupport.getInvalidFileObject(prevPath.getFileSystem(), prevPath.getPath());
                FileEvent deleteFE = new FileEvent((FileObject) fe.getSource(), removedFO, fe.isExpected(), fe.getTime());
                synchronized (eventsLock) {
                    if (TraceFlags.TRACE_EXTERNAL_CHANGES) {
                        LOG.log(Level.INFO, "External updates: registered fileRenamed {0}", fe);
                    }
                    events.addLast(new Pair(EventKind.DELETED, deleteFE));
                    events.addLast(new Pair(EventKind.CREATED, fe));
                }
            }
        }
    }

    
    @Override
    public void fileDeleted(FileEvent fe) {
        if (TraceFlags.TRACE_EXTERNAL_CHANGES) {
            LOG.log(Level.INFO, "External updates: try to register fileDeleted {0}", fe);
        }
        register(fe, EventKind.DELETED); 
    }

    @Override
    public void run() {
        if (TraceFlags.TRACE_EXTERNAL_CHANGES) {
            LOG.info("External updates: running update task");
        }
        while (true) {
            LinkedList<Pair> curEvents;
            synchronized (eventsLock) {
                if (events.isEmpty()) {
                    if (TraceFlags.TRACE_EXTERNAL_CHANGES) {
                        LOG.info("External updates: empty queue");
                    }
                    return;
                }
                curEvents = events;
                events = new LinkedList<>();
            }            
            for (Pair pair : curEvents) {
                ModelImpl model = modelSupport.getModel();
                if (model == null) {
                    return;
                }
                FileObject fo = pair.fe.getFile();
                if (fo != null) {
                    EventKind curKind = pair.kind;
                    if (TraceFlags.TRACE_EXTERNAL_CHANGES) {
                        LOG.log(Level.INFO, "External updates: Updating for {0} {1}", new Object[]{curKind, fo});
                    }
                    CsmFile[] files = model.findFiles(FSPath.toFSPath(fo), false, false);
                    Set<ProjectBase> handledProjects = new HashSet<>();
                            
                    for (int i = 0; i < files.length; ++i) {
                        FileImpl file = (FileImpl) files[i];
                        ProjectBase project = file.getProjectImpl(true);
                        if (project != null) {
                            handledProjects.add(project);
                            if (curKind == EventKind.DELETED) {
                                if (TraceFlags.TRACE_EXTERNAL_CHANGES) {
                                    LOG.log(Level.INFO, "External updates: project {0} found for deleted {1}", new Object[]{project, file});
                                }
                                project.checkForRemoved();
                            } else if (curKind == EventKind.CHANGED) {
                                if (TraceFlags.TRACE_EXTERNAL_CHANGES) {
                                    LOG.log(Level.INFO, "External updates: project {0} found for changed {1}", new Object[]{project, file});
                                }
                                project.onFileImplExternalChange(file);
                            } else {
                                if (TraceFlags.TRACE_EXTERNAL_CHANGES) {
                                    LOG.log(Level.INFO, "External updates: project {0} found for {1}", new Object[]{project, fo});
                                }
                                project.onFileObjectExternalCreate(fo);                            
                            }
                        }
                    }                        
                    if (curKind == EventKind.CREATED) {
                        Collection<CsmProject> ownerCsmProjects = CsmUtilities.getOwnerCsmProjects(fo);
                        for (CsmProject prj : ownerCsmProjects) {
                            if (prj instanceof ProjectBase) {
                                ProjectBase project = (ProjectBase) prj;
                                if (!handledProjects.contains(project)) {
                                    if (TraceFlags.TRACE_EXTERNAL_CHANGES) {
                                        LOG.log(Level.INFO, "External updates: project {0} found for {1}", new Object[]{project, fo});
                                    }
                                    project.onFileObjectExternalCreate(fo);
                                }
                            }
                        }
                        if (TraceFlags.TRACE_EXTERNAL_CHANGES && ownerCsmProjects.isEmpty()) {
                            LOG.log(Level.INFO, "External updates: No CsmProject found for {0}", fo);
                        }
                    }
                }
            }
        }
    }

    private boolean isCOrCpp(FileObject fo) {
        String mime = fo.getMIMEType();
        if (mime == null) {
            mime = FileUtil.getMIMEType(fo);
            if (TraceFlags.TRACE_EXTERNAL_CHANGES) {
                LOG.log(Level.INFO, "External updates: MIME resolved: {0}", mime);
            }
        }
        return MIMENames.isFortranOrHeaderOrCppOrC(mime);
    }

    private void register(FileEvent fe, EventKind kind) {
        final ModelImpl model = modelSupport.getModel();
        if (model != null) {
            final FileObject fo = fe.getFile();
            if (!fo.isValid() || isCOrCpp(fo)) {
                synchronized (eventsLock) {
                    if (TraceFlags.TRACE_EXTERNAL_CHANGES) {
                        LOG.log(Level.INFO, "External updates: registered {0} {1}", new Object[]{kind, fe});
                    }                    
                    events.addLast(new Pair(kind, fe));
                }
                model.enqueueModelTask(this, "External File Updater"); // NOI18N
            }
        }
    }
}
