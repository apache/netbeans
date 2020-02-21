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
package org.netbeans.modules.cnd.discovery.wizard.api.support;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.api.progress.ProgressHandleFactory;
import org.netbeans.api.project.Project;
import org.netbeans.modules.cnd.api.model.CsmFile;
import org.netbeans.modules.cnd.api.model.CsmModel;
import org.netbeans.modules.cnd.api.model.CsmModelAccessor;
import org.netbeans.modules.cnd.api.model.CsmModelState;
import org.netbeans.modules.cnd.api.model.CsmProject;
import org.netbeans.modules.cnd.api.model.services.CsmFileInfoQuery;
import org.netbeans.modules.cnd.api.model.xref.CsmIncludeHierarchyResolver;
import org.netbeans.modules.cnd.api.project.NativeFileItem;
import org.netbeans.modules.cnd.api.project.NativeProject;
import org.netbeans.modules.cnd.api.project.NativeProjectRegistry;
import org.netbeans.modules.cnd.discovery.api.DiscoveryUtils;
import org.netbeans.modules.cnd.discovery.projectimport.ImportProject;
import org.netbeans.modules.cnd.discovery.wizard.api.DiscoveryDescriptor;
import org.netbeans.modules.cnd.discovery.wizard.support.impl.DiscoveryProjectGeneratorImpl;
import org.netbeans.modules.cnd.makeproject.api.configurations.ConfigurationDescriptorProvider;
import org.netbeans.modules.cnd.makeproject.api.configurations.ConfigurationDescriptorProvider.SnapShot;
import org.netbeans.modules.cnd.makeproject.api.configurations.Folder;
import org.netbeans.modules.cnd.makeproject.api.configurations.Item;
import org.netbeans.modules.cnd.makeproject.api.configurations.MakeConfiguration;
import org.netbeans.modules.cnd.makeproject.api.configurations.MakeConfigurationDescriptor;
import org.netbeans.modules.cnd.utils.MIMENames;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironment;
import org.netbeans.modules.remote.spi.FileSystemProvider;
import org.openide.util.NbBundle;
import org.openide.util.Utilities;

/**
 *
 */
public final class DiscoveryProjectGenerator {

    private DiscoveryProjectGenerator() {
    }

    public static void generateProjects(DiscoveryDescriptor descriptor) throws IOException {
        DiscoveryProjectGeneratorImpl impl = new DiscoveryProjectGeneratorImpl(descriptor);
        impl.process();
    }

    public static boolean fixExcludedHeaderFiles(Project makeProject, Logger logger) {
        if (CsmModelAccessor.getModelState() != CsmModelState.ON) {
            if (logger != null) {
                logger.log(Level.INFO, "#model is not ON: {0}", CsmModelAccessor.getModelState()); // NOI18N
            }
        }
        CsmModel model = CsmModelAccessor.getModel();
        if (!(model != null && makeProject != null)) {
            if (logger != null) {
                logger.log(Level.INFO, "Failed fixing of excluded header files for project {0}", makeProject); // NOI18N
            }
            return false;
        }
        final NativeProject np = makeProject.getLookup().lookup(NativeProject.class);
        if (np == null) {
            if (logger != null) {
                logger.log(Level.INFO, "Not found make project for fixing of excluded header files for project {0}", makeProject); // NOI18N
            }
            return false;
        }
        final CsmProject p = model.getProject(np);
        if (p == null || !p.isValid()) {
            if (logger != null) {
                logger.log(Level.INFO, "Not found model project for fixing of excluded header files for project {0}", np); // NOI18N
            }
            return false;
        }
        if (logger != null) {
            logger.log(Level.INFO, "Start fixing of excluded header files for project {0}", p); // NOI18N
        }
        final AtomicBoolean interrupter = new AtomicBoolean(false);
        PropertyChangeListener listener = new PropertyChangeListener() {

            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                Collection<NativeProject> projects = (Collection<NativeProject>) evt.getNewValue();
                if (!projects.contains(np)) {
                    interrupter.set(true);
                }
            }
        };
        if (!NativeProjectRegistry.getDefault().getOpenProjects().contains(np)) {
            return false;
        }
        NativeProjectRegistry.getDefault().addPropertyChangeListener(listener);
        try {
            if (!NativeProjectRegistry.getDefault().getOpenProjects().contains(np)) {
                return false;
            }
            ConfigurationDescriptorProvider pdp = makeProject.getLookup().lookup(ConfigurationDescriptorProvider.class);
            SnapShot delta = pdp.startModifications();
            boolean isChanged = false;
            Set<String> needCheck = new HashSet<>();
            Set<String> needAdd = new HashSet<>();
            ExecutionEnvironment env = FileSystemProvider.getExecutionEnvironment(pdp.getConfigurationDescriptor().getBaseDirFileSystem());
            boolean resolveLinks = false;
            MakeConfiguration activeConfiguration = pdp.getConfigurationDescriptor().getActiveConfiguration();
            if (activeConfiguration != null) {
                resolveLinks = activeConfiguration.getCodeAssistanceConfiguration().getResolveSymbolicLinks().getValue();
            }
            ProgressHandle handle = ProgressHandleFactory.createHandle(NbBundle.getMessage(DiscoveryProjectGenerator.class, "FixInclude.Progress.AnalyzeRoot")); // NOI18N
            handle.start();
            try {
                Map<String, Item> normalizedItems = DiscoveryProjectGenerator.initNormalizedNames(makeProject);
                for (CsmFile impl : p.getAllFiles()) {
                    if (interrupter.get()) {
                        return false;
                    }
                    if (impl != null) {
                        NativeFileItem item = CsmFileInfoQuery.getDefault().getNativeFileItem(impl);
                        if (item == null) {
                            String path = impl.getAbsolutePath().toString();
                            item = normalizedItems.get(path);
                        }
                        boolean isLineDirective = false;
                        if (item != null && item.getLanguage() == NativeFileItem.Language.C_HEADER) {
                            Collection<CsmFile> parentFiles = CsmIncludeHierarchyResolver.getDefault().getFiles(impl);
                            if (parentFiles.isEmpty()) {
                                isLineDirective = true;
                            }
                        }
                        if (item != null && np.equals(item.getNativeProject())) {
                            if (item instanceof Item) {
                                if (isLineDirective || !impl.isSourceFile()) {
                                    if (logger != null) {
                                        logger.log(Level.FINE, "#fix excluded as header for file {0}", impl.getAbsolutePath()); // NOI18N
                                    }
                                    isChanged |= ProjectBridge.setHeaderTool((Item) item);
                                    isChanged |= ProjectBridge.setExclude((Item) item, true);
                                    needCheck.add(item.getAbsolutePath());
                                }
                            }
                        } else if (item == null) {
                            // It should be in project?
                            if (impl.isHeaderFile()) {
                                String path = impl.getAbsolutePath().toString();
                                boolean added = false;
                                if (resolveLinks) {
                                    String resolvedSymbolicLink = DiscoveryUtils.resolveSymbolicLink(FileSystemProvider.getFileSystem(env), path);
                                    if (resolvedSymbolicLink != null) {
                                        needAdd.add(resolvedSymbolicLink);
                                        added = true;
                                    }
                                }
                                if (!added) {
                                    needAdd.add(path);
                                }
                            }
                        }
                    }
                }
                if (needCheck.size() > 0 || needAdd.size() > 0) {
                    if (interrupter.get()) {
                        return false;
                    }
                    ProjectBridge bridge = new ProjectBridge(makeProject);
                    if (bridge.isValid()) {
                        if (needAdd.size() > 0) {
                            Map<String, Folder> prefferedFolders = bridge.prefferedFolders();
                            for (String path : needAdd) {
                                if (interrupter.get()) {
                                    return false;
                                }
                                String name = path;
                                if (Utilities.isWindows()) {
                                    path = path.replace('\\', '/'); // NOI18N
                                }
                                int i = path.lastIndexOf('/'); // NOI18N
                                if (i >= 0) {
                                    String folderPath = path.substring(0, i);
                                    Folder prefferedFolder = prefferedFolders.get(folderPath);
                                    if (prefferedFolder == null) {
                                        LinkedList<String> mkFolder = new LinkedList<>();
                                        while (true) {
                                            i = folderPath.lastIndexOf('/'); // NOI18N
                                            if (i > 0) {
                                                mkFolder.addLast(folderPath.substring(i + 1));
                                                folderPath = folderPath.substring(0, i);
                                                prefferedFolder = prefferedFolders.get(folderPath);
                                                if (prefferedFolder != null) {
                                                    break;
                                                }
                                            } else {
                                                break;
                                            }
                                        }
                                        if (prefferedFolder != null) {
                                            while (true) {
                                                if (mkFolder.isEmpty()) {
                                                    break;
                                                }
                                                String segment = mkFolder.pollLast();
                                                prefferedFolder = prefferedFolder.addNewFolder(segment, segment, true, (Folder.Kind) null);
                                                folderPath += "/" + segment; // NOI18N
                                                prefferedFolders.put(folderPath, prefferedFolder);
                                            }
                                        }
                                    }
                                    if (prefferedFolder != null) {
                                        String relPath = bridge.getRelativepath(name);
                                        Item item = bridge.getProjectItem(relPath);
                                        if (item == null) {
                                            item = bridge.createItem(name);
                                            item = prefferedFolder.addItem(item);
                                        }
                                        if (item != null) {
                                            isChanged |= ProjectBridge.setHeaderTool(item);
                                            if (!MIMENames.isCppOrCOrFortran(item.getMIMEType())) {
                                                needCheck.add(path);
                                            }
                                            isChanged |= ProjectBridge.setExclude(item, true);
                                            isChanged |= ProjectBridge.excludeItemFromOtherConfigurations(item);
                                        }
                                    }
                                }
                            }
                        }
                        if (needCheck.size() > 0) {
                            isChanged |= bridge.checkForNewExtensions(needCheck);
                        }
                    }
                }
                if (isChanged) {
                    saveMakeConfigurationDescriptor(makeProject, delta);
                } else {
                    pdp.endModifications(delta, false, null);
                }
            } finally {
                handle.finish();
            }
            return isChanged;
        } finally {
            NativeProjectRegistry.getDefault().removePropertyChangeListener(listener);
        }
    }

    public static void saveMakeConfigurationDescriptor(Project lastSelectedProject, SnapShot delta) {
        ConfigurationDescriptorProvider pdp = lastSelectedProject.getLookup().lookup(ConfigurationDescriptorProvider.class);
        final MakeConfigurationDescriptor makeConfigurationDescriptor = pdp.getConfigurationDescriptor();
        if (makeConfigurationDescriptor != null) {
            makeConfigurationDescriptor.setModified();
            makeConfigurationDescriptor.save();
            if (delta != null) {
                pdp.endModifications(delta, true, ImportProject.logger);
            } else {
                makeConfigurationDescriptor.checkForChangedItems(lastSelectedProject, null, null);
            }
        }
    }

    public static HashMap<String, Item> initNormalizedNames(Project makeProject) {
        HashMap<String, Item> normalizedItems = new HashMap<>();
        ConfigurationDescriptorProvider pdp = makeProject.getLookup().lookup(ConfigurationDescriptorProvider.class);
        if (pdp != null) {
            MakeConfigurationDescriptor makeConfigurationDescriptor = pdp.getConfigurationDescriptor();
            if (makeConfigurationDescriptor != null) {
                for (Item item : makeConfigurationDescriptor.getProjectItems()) {
                    normalizedItems.put(item.getNormalizedPath(), item);
                }
            }
        }
        return normalizedItems;
    }
}
