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

import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.modules.cnd.api.model.CsmFile;
import org.netbeans.modules.cnd.api.model.CsmModel;
import org.netbeans.modules.cnd.api.model.CsmModelAccessor;
import org.netbeans.modules.cnd.api.model.CsmProgressListener;
import org.netbeans.modules.cnd.api.model.CsmProject;
import org.netbeans.modules.cnd.modelimpl.csm.core.ModelImpl;
import org.netbeans.modules.cnd.modelimpl.csm.core.ProjectBase;
import org.netbeans.modules.cnd.modelimpl.debug.TraceFlags;
import org.netbeans.modules.cnd.utils.UIGesturesSupport;
import org.openide.util.Cancellable;

/**
 *
 */
@org.openide.util.lookup.ServiceProvider(service = org.netbeans.modules.cnd.api.model.CsmProgressListener.class)
public class ProgressListenerImpl implements CsmProgressListener {

    private final Map<CsmProject, ParsingProgress> handles = new HashMap<>();
    private static final Logger LOG = Logger.getLogger(ProgressListenerImpl.class.getName());
    
    private synchronized ParsingProgress getHandle(final CsmProject project, boolean createIfNeed) {
        ParsingProgress handle = handles.get(project);
        if (handle == null && createIfNeed) {
            Cancellable cancellable = null;
            // disable by default, because cancel is called on IDE exit and 
            // code model is left in confusing "turned off" state for project being parsed
            if (Boolean.getBoolean("cnd.cancellable.parse")) { // NOI18N
                cancellable = new Cancellable() {
                    @Override
                    public boolean cancel() {
                        UIGesturesSupport.submit("USG_CND_CANCEL_PARSE"); //NOI18N
                        LOG.log(Level.INFO, "Cancel Parse requst for project {0}", project); //NOI18N
                        CsmModel model = CsmModelAccessor.getModel();
                        if (model instanceof ModelImpl && project instanceof ProjectBase) {
                            final ModelImpl modelImpl = (ModelImpl) model;
                            modelImpl.enqueueModelTask(new Runnable() {
                                @Override
                                public void run() {
                                    LOG.log(Level.INFO, "Enqueue disabling Code Assistance for project {0}", project); //NOI18N
                                    modelImpl.disableProjectBase((ProjectBase) project);
                                }
                            }, "disable " + project.getDisplayName()); // NOI18N

                            return true;
                        }
                        return false;
                    }
                };
            }
            handle = new ParsingProgress(project, cancellable);
            handles.put(project, handle);
        }
        return handle;
    }

    @Override
    public void projectParsingStarted(CsmProject project) {
        if (TraceFlags.TRACE_PARSER_QUEUE) {
            System.err.println("ProgressListenerImpl.projectParsingStarted " + project.getName());
        }
        getHandle(project, true).start();
    }

    @Override
    public void projectFilesCounted(CsmProject project, int filesCount) {
        if (TraceFlags.TRACE_PARSER_QUEUE) {
            System.err.println("ProgressListenerImpl.projectFilesCounted " + project.getName() + ' ' + filesCount);
        }
        getHandle(project, true).switchToDeterminate(filesCount);
    }

    @Override
    public void projectParsingFinished(CsmProject project) {
        if (TraceFlags.TRACE_PARSER_QUEUE) {
            System.err.println("ProgressListenerImpl.projectParsingFinished " + project.getName());
        }
        done(project);
    }

    @Override
    public void projectLoaded(CsmProject project) {
        if (TraceFlags.TRACE_PARSER_QUEUE) {
            System.err.println("ProgressListenerImpl.projectLoaded " + project.getName());
        }
    }

    @Override
    public void projectParsingCancelled(CsmProject project) {
        if (TraceFlags.TRACE_PARSER_QUEUE) {
            System.err.println("ProgressListenerImpl.projectParsingCancelled " + project.getName());
        }
        done(project);
    }

    private void done(CsmProject project) {
        getHandle(project, true).finish();
        synchronized (this) {
            handles.remove(project);
        }
    }

    @Override
    public void fileInvalidated(CsmFile file) {
    }

    @Override
    public void fileAddedToParse(CsmFile file) {
        CsmProject project = file.getProject();
        ParsingProgress handle = getHandle(project, false);
        if (handle != null) {
            handle.addedToParse(file);
        } else if (project.isArtificial()) {
            for (CsmProject p : CsmModelAccessor.getModel().projects()){
                if (!p.isArtificial()) {
                    if (p.getLibraries().contains(project)){
                        handle = getHandle(p, false);
                        if (handle != null) {
                            handle.addedToParse(file);
                        }
                    }
                }
            }
        }
    }

    @Override
    public void fileParsingStarted(CsmFile file) {
        if (TraceFlags.TRACE_PARSER_QUEUE) {
            System.err.println("  ProgressListenerImpl.fileParsingStarted " + file.getAbsolutePath());
        }
        CsmProject project = file.getProject();
        ParsingProgress handle = getHandle(project, false);
        if (handle != null) {
            handle.nextCsmFile(file);
        } else if (project.isArtificial()) {
            for (CsmProject p : CsmModelAccessor.getModel().projects()){
                if (!p.isArtificial()) {
                    if (p.getLibraries().contains(project)){
                        handle = getHandle(p, false);
                        if (handle != null) {
                            handle.nextCsmFile(file);
                        }
                    }
                }
            }
        }
    }

    @Override
    public void fileParsingFinished(CsmFile file) {
        if (TraceFlags.TRACE_PARSER_QUEUE) {
            System.err.println("  ProgressListenerImpl.fileParsingFinished " + file.getAbsolutePath());
        }
    }

    @Override
    public void parserIdle() {
        if (TraceFlags.TRACE_PARSER_QUEUE) {
            System.err.println("  ProgressListenerImpl.parserIdle");
        }
    }

    @Override
    public void fileRemoved(CsmFile file) {
        if (TraceFlags.TRACE_PARSER_QUEUE) {
            System.err.println("  ProgressListenerImpl.fileRemoved " + file.getAbsolutePath());
        }
    }
}
