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
