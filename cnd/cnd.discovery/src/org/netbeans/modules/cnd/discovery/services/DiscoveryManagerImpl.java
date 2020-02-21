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
package org.netbeans.modules.cnd.discovery.services;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.logging.Level;
import org.netbeans.api.project.Project;
import org.netbeans.modules.cnd.api.model.CsmListeners;
import org.netbeans.modules.cnd.api.model.CsmModelAccessor;
import org.netbeans.modules.cnd.api.model.CsmProgressAdapter;
import org.netbeans.modules.cnd.api.model.CsmProgressListener;
import org.netbeans.modules.cnd.api.model.CsmProject;
import org.netbeans.modules.cnd.api.project.NativeProject;
import org.netbeans.modules.cnd.discovery.api.DiscoveryExtensionInterface;
import org.netbeans.modules.cnd.discovery.projectimport.ImportProject;
import org.netbeans.modules.cnd.discovery.wizard.DiscoveryWizardAction;
import org.netbeans.modules.cnd.discovery.wizard.api.DiscoveryDescriptor;
import org.netbeans.modules.cnd.discovery.wizard.api.support.DiscoveryProjectGenerator;
import org.netbeans.modules.cnd.makeproject.api.configurations.ConfigurationDescriptorProvider;
import org.netbeans.modules.cnd.makeproject.api.configurations.MakeConfiguration;
import org.netbeans.modules.cnd.makeproject.api.configurations.MakeConfigurationDescriptor;
import org.netbeans.modules.cnd.makeproject.api.ui.wizard.IteratorExtension;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Lookup;
import org.openide.util.RequestProcessor;

/**
 *
 */
public final class DiscoveryManagerImpl {

    public static final String BUILD_LOG_KEY = "build-log"; //NOI18N 
    public static final String BUILD_EXEC_KEY = "exec-log"; //NOI18N 
    private static final RequestProcessor RP = new RequestProcessor("Discovery Manager Worker", 1); //NOI18N
    private static final Map<CsmProject, CsmProgressListener> listeners = new WeakHashMap<>();
    private static final Map<NativeProject, CsmProgressListener> listeners2 = new WeakHashMap<>();

    private DiscoveryManagerImpl() {
    }

    public static void projectBuilt(Project project, Map<String, Object> artifacts, boolean isIncremental) {
        RP.post(new DiscoveryWorker(project, artifacts, isIncremental));
    }

    public static void discoverHeadersByModel(final Project project) {
        RP.post(new Runnable() {

            @Override
            public void run() {
                final NativeProject np = project.getLookup().lookup(NativeProject.class);
                ImportProject.logger.log(Level.INFO, "Post fixing of excluded header files for project {0}", np); //NOI18N
                CsmProgressListener listener = new CsmProgressAdapter() {

                    @Override
                    public void projectParsingFinished(CsmProject aCsmProject) {
                        ImportProject.logger.log(Level.INFO, "Model parsing finished and ready to fixing of excluded header files for project {0}", aCsmProject); //NOI18N
                        final CsmProject csmProject = CsmModelAccessor.getModel().getProject(np);
                        if (csmProject != null && csmProject.equals(aCsmProject)) {
                            CsmListeners.getDefault().removeProgressListener(this);
                            DiscoveryManagerImpl.listeners2.remove(np);
                            DiscoveryProjectGenerator.fixExcludedHeaderFiles(project, ImportProject.logger);
                        }
                    }
                };
                DiscoveryManagerImpl.listeners2.put(np, listener);
                CsmListeners.getDefault().addProgressListener(listener);
            }
        });
    }

    private static final class DiscoveryWorker implements Runnable {

        private final Project project;
        private final Map<String, Object> artifacts;
        private final boolean isIncremental;

        DiscoveryWorker(Project project, Map<String, Object> artifacts, boolean isIncremental) {
            this.project = project;
            this.artifacts = artifacts;
            this.isIncremental = isIncremental;
        }

        @Override
        public void run() {
            final DiscoveryExtensionInterface extension = (DiscoveryExtensionInterface) Lookup.getDefault().lookup(IteratorExtension.class);
            if (extension == null) {
                return;
            }
            String artifact = (String) artifacts.get(BUILD_EXEC_KEY);
            if (artifact != null) {
                final Map<String, Object> map = new HashMap<>();
                DiscoveryDescriptor.ROOT_FOLDER.toMap(map, findRoot());
                DiscoveryDescriptor.EXEC_LOG_FILE.toMap(map, artifact);
                DiscoveryDescriptor.RESOLVE_SYMBOLIC_LINKS.toMap(map, resolveSymbolicLinks());
                DiscoveryDescriptor.INCREMENTAL.toMap(map, isIncremental);
                if (extension.canApply(map, project, null)) {
                    try {
                        postModelTask();
                        extension.apply(map, project, null);
                    } catch (IOException ex) {
                        ex.printStackTrace(System.err);
                    }
                    saveArtifact(artifact, "-exec"); // NOI18N
                    artifact = (String) artifacts.get(BUILD_LOG_KEY);
                    if (artifact != null) {
                        saveArtifact(artifact, "-build"); // NOI18N
                    }
                }
                return;
            }
            artifact = (String) artifacts.get(BUILD_LOG_KEY);
            if (artifact != null) {
                final Map<String, Object> map = new HashMap<>();
                DiscoveryDescriptor.ROOT_FOLDER.toMap(map, findRoot());
                DiscoveryDescriptor.LOG_FILE.toMap(map, artifact);
                DiscoveryDescriptor.RESOLVE_SYMBOLIC_LINKS.toMap(map, resolveSymbolicLinks());
                DiscoveryDescriptor.INCREMENTAL.toMap(map, isIncremental);
                if (extension.canApply(map, project, null)) {
                    try {
                        postModelTask();
                        extension.apply(map, project, null);
                    } catch (IOException ex) {
                        ex.printStackTrace(System.err);
                    }
                    saveArtifact(artifact, "-build"); // NOI18N
                    artifact = (String) artifacts.get(BUILD_EXEC_KEY);
                    if (artifact != null) {
                        saveArtifact(artifact, "-exec"); // NOI18N
                    }
                }
            }
        }

        private void saveArtifact(String artifact, String suffix) {
            if (!isIncremental) {
                FileObject log = FileUtil.toFileObject(new File(artifact));
                if (log != null && log.isValid()) {
                    MakeConfiguration activeConfiguration = getActiveConfiguration();
                    if (activeConfiguration != null) {
                        try {
                            FileObject dest = project.getProjectDirectory().getFileObject("nbproject/private/"+activeConfiguration.getName()+suffix+"."+log.getExt()); // NOI18N
                            if (dest != null) {
                                dest.delete();
                            }
                            FileUtil.copyFile(log, project.getProjectDirectory().getFileObject("nbproject/private"), // NOI18N
                                    activeConfiguration.getName()+suffix);
                        } catch (IOException ex) {
                            ex.printStackTrace(System.err);
                        }
                    }
                }
            }
        }
        
        private MakeConfiguration getActiveConfiguration() {
            ConfigurationDescriptorProvider cdp = project.getLookup().lookup(ConfigurationDescriptorProvider.class);
            MakeConfigurationDescriptor cd = cdp.getConfigurationDescriptor();
            if (cd != null) {
                return cd.getActiveConfiguration();
            }
            return null;
        }

        private boolean resolveSymbolicLinks() {
            MakeConfiguration activeConfiguration = getActiveConfiguration();
            if (activeConfiguration != null) {
                return activeConfiguration.getCodeAssistanceConfiguration().getResolveSymbolicLinks().getValue();
            }
            return false;
        }

        private String findRoot() {
            return DiscoveryWizardAction.findSourceRoot(project);
        }

        private void postModelTask() {
            final NativeProject np = project.getLookup().lookup(NativeProject.class);
            final CsmProject csmProject = CsmModelAccessor.getModel().getProject(np);
            if (csmProject == null) {
                ImportProject.logger.log(Level.INFO, "Can not post fix excluded header files; no associated CsmProject for {0}", np); //NOI18N
                return;
            }
            ImportProject.logger.log(Level.INFO, "Post fixing of excluded header files for project {0}", csmProject); //NOI18N
            CsmProgressListener listener = new CsmProgressAdapter() {

                @Override
                public void projectParsingFinished(CsmProject aCsmProject) {
                    ImportProject.logger.log(Level.INFO, "Model parsing finished and ready to fixing of excluded header files for model project {0}", aCsmProject); //NOI18N
                    if (csmProject.equals(aCsmProject)) {
                        CsmListeners.getDefault().removeProgressListener(this);
                        DiscoveryManagerImpl.listeners.remove(aCsmProject);
                        DiscoveryProjectGenerator.fixExcludedHeaderFiles(DiscoveryWorker.this.project, ImportProject.logger);
                    }
                }
            };
            DiscoveryManagerImpl.listeners.put(csmProject,listener);
            CsmListeners.getDefault().addProgressListener(listener);
        }
    }
}
