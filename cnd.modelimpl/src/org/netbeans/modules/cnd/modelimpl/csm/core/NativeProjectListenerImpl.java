/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
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
 * Contributor(s):
 * 
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
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
 */

package org.netbeans.modules.cnd.modelimpl.csm.core;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.modules.cnd.api.project.NativeFileItem;
import org.netbeans.modules.cnd.api.project.NativeProject;
import org.netbeans.modules.cnd.api.project.NativeProjectItemsListener;
import org.netbeans.modules.cnd.modelimpl.debug.TraceFlags;
import org.netbeans.modules.cnd.modelimpl.platform.ModelSupport;
import org.netbeans.modules.cnd.modelimpl.repository.RepositoryUtils;
import org.netbeans.modules.cnd.utils.CndUtils;

/**
 * Implementation of the NativeProjectItemsListener interface
 */
// package-local
class NativeProjectListenerImpl implements NativeProjectItemsListener {
    private static final boolean TRACE;
    private static final Logger LOG = Logger.getLogger("NativeProjectListenerImpl"); // NOI18N
    static {
        TRACE = LOG.isLoggable(Level.FINE);
    }
    
    private final NativeProject nativeProject;
    private final ProjectBase projectBase;
    private volatile boolean enabledEventsHandling = true;

    public NativeProjectListenerImpl(ModelImpl model, NativeProject nativeProject, ProjectBase project) {
        this.nativeProject = nativeProject;
        this.projectBase = project;
        if (TraceFlags.MERGE_EVENTS) {
            CndUtils.assertTrue(false, "NativeProjectListenerImpl shoudl not be used if MERGE_EVENTS flag is set"); //NOI18N
        }

    }

    @Override
    public void filesAdded(List<NativeFileItem> fileItems) {
        if (TRACE) {
            String title = "Native event filesAdded:" + fileItems.size(); // NOI18N
            LOG.log(Level.INFO, title + "\n" +DeepReparsingUtils.toString(fileItems), new Exception(title));
        }
        if (enabledEventsHandling) {
            ArrayList<NativeFileItem> list = new ArrayList<>();
            for (NativeFileItem item : fileItems) {
                if (!item.isExcluded()) {
                    list.add(item);
                }
            }            
            itemsAddedImpl(list);
        } else {
            if (TraceFlags.TIMING) {
                LOG.log(Level.INFO, "skipped filesAdded(list) {0}...", nativeProject.getProjectDisplayName());
            }
        }
    }

    @Override
    public void filesRemoved(List<NativeFileItem> fileItems) {
        if (TRACE) {
            String title = "Native event filesRemoved:" + fileItems.size(); // NOI18N
            LOG.log(Level.INFO, title + "\n" +DeepReparsingUtils.toString(fileItems), new Exception(title));
        }
        if (enabledEventsHandling) {
            itemsRemovedImpl(fileItems);
        } else {
            if (TraceFlags.TIMING) {
                LOG.log(Level.INFO, "skipped filesRemoved(list) {0}...", nativeProject.getProjectDisplayName());
            }
        }
    }

    @Override
    public void fileRenamed(String oldPath, NativeFileItem newFileIetm){
        if (TRACE) {
            LOG.log(Level.INFO, "Native event fileRenamed:\tOld Name:"+oldPath+ "\tNew Name:"+newFileIetm.getAbsolutePath(), new Exception("fileRenamed"));
        }
        if (!enabledEventsHandling) {
            LOG.log(Level.INFO, "UNEXPECTED fileRenamed {0}...", nativeProject.getProjectDisplayName());
        }
	itemRenamedImpl(oldPath, newFileIetm);
    }

    /*package*/
    final void enableListening(boolean enable) {
        if (TraceFlags.TIMING) {
            LOG.log(Level.INFO, "\n%{0} ProjectListeners {1}...", new Object[] {enable ? "enable" : "disable",
                    nativeProject.getProjectDisplayName()});
        }
        enabledEventsHandling = enable;
    }

    @Override
    public void filesPropertiesChanged(final List<NativeFileItem> fileItems) {
        if (TRACE) {
            String title = "Native event filesPropertiesChanged:" + fileItems.size(); // NOI18N
            LOG.log(Level.INFO, title + "\n" +DeepReparsingUtils.toString(fileItems), new Exception(title));
        }
        if (enabledEventsHandling) {
            itemsPropertiesChangedImpl(fileItems, false);
        } else {
            if (TraceFlags.TIMING) {
                LOG.log(Level.INFO, "skipped filesPropertiesChanged(list) {0}...", nativeProject.getProjectDisplayName());
            }
        }
    }

    @Override
    public void filesPropertiesChanged(NativeProject nativeProject) {
        List<NativeFileItem> allFiles = nativeProject.getAllFiles();
        if (TRACE) {
            String title = "Native event projectPropertiesChanged:" + allFiles.size(); // NOI18N
            LOG.log(Level.INFO, title, new Exception(title));
        }
        if (enabledEventsHandling) {
            ArrayList<NativeFileItem> list = new ArrayList<>();
            for(NativeFileItem item : allFiles){
                if (!item.isExcluded()) {
                    switch(item.getLanguage()){
                        case C:
                        case CPP:
                        case FORTRAN:
                            list.add(item);
                            break;
                        default:
                            break;
                    }
                }
            }
            if (false/*part of AllFiles*/) list.addAll(nativeProject.getStandardHeadersIndexers());
            itemsPropertiesChangedImpl(list, true);
        } else {
            if (TraceFlags.TIMING) {
                LOG.log(Level.INFO, "skipped filesPropertiesChanged {0}...", nativeProject.getProjectDisplayName());
            }
        }
    }

    @Override
    public void projectDeleted(NativeProject nativeProject) {
        if (TRACE) {
            LOG.log(Level.INFO, "projectDeleted {0}", nativeProject);  // NOI18N
        }
	RepositoryUtils.onProjectDeleted(nativeProject);
    }

    private void itemsAddedImpl(final List<NativeFileItem> items) {
        if (!items.isEmpty()){
            ModelImpl.instance().enqueueModelTask(new Runnable() {

                @Override
                public void run() {
                    try {
                        projectBase.onFileItemsAdded(items);
                    } catch( Exception e ) {
                        e.printStackTrace(System.err);
                    }
                }
            }, "Applying add items"); // NOI18N         
        }
    }
    
    private void itemsRemovedImpl(final List<NativeFileItem> items) {
        if (!items.isEmpty()) {
            ModelImpl.instance().enqueueModelTask(new Runnable() {

                @Override
                public void run() {
                    try {
                        projectBase.onFileItemsRemoved(items);
                    } catch( Exception e ) {
                        e.printStackTrace(System.err);
                    }
                }
            }, "Applying remove items"); // NOI18N                
        }
    }

    private void itemRenamedImpl(final String oldPath, final NativeFileItem newFileIetm) {
        ModelImpl.instance().enqueueModelTask(new Runnable() {

            @Override
            public void run() {
                try {
                    projectBase.onFileItemRenamed(oldPath, newFileIetm);
                } catch( Exception e ) {
                    //TODO: FIX (most likely in Makeproject: path == null in this situation,
                    //this cause NPE
                    e.printStackTrace(System.err);
                }
            }
        }, "Applying rename item"); // NOI18N          
    }
    
    private void itemsPropertiesChangedImpl(final List<NativeFileItem> items, final boolean invalidateLibraries) {
        if (!items.isEmpty()) {
            ModelImpl.instance().enqueueModelTask(new Runnable() {

                @Override
                public void run() {
                    try {
                        projectBase.onFileItemsPropertyChanged(items, invalidateLibraries);
                    } catch (Exception e) {
                        e.printStackTrace(System.err);
                    }
                }
            }, "Applying property changes"); // NOI18N            
        }
    }

    @Override
    public void fileOperationsStarted(NativeProject nativeProject) {
        if (TRACE) {
            String title = "fileOperationsStarted:" + nativeProject.getProjectDisplayName(); // NOI18N
            LOG.log(Level.INFO, title, new Exception(title));
        }        
        ModelSupport.instance().suspendDeleteEvents();
    }

    @Override
    public void fileOperationsFinished(NativeProject nativeProject) {
        if (TRACE) {
            String title = "fileOperationsFinished:" + nativeProject.getProjectDisplayName(); // NOI18N
            LOG.log(Level.INFO, title, new Exception(title));
        }
        ModelSupport.instance().resumeDeleteEvents();
    }
}
