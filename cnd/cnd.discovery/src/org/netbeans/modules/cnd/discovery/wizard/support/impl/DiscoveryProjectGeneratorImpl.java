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

package org.netbeans.modules.cnd.discovery.wizard.support.impl;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.api.progress.ProgressHandleFactory;
import org.netbeans.api.project.Project;
import org.netbeans.modules.cnd.discovery.api.ItemProperties;
import org.netbeans.modules.cnd.discovery.api.ItemProperties.LanguageKind;
import org.netbeans.modules.cnd.discovery.buildsupport.CompileSupport;
import org.netbeans.modules.cnd.discovery.projectimport.ImportProject;
import org.netbeans.modules.cnd.discovery.wizard.api.DiscoveryDescriptor;
import org.netbeans.modules.cnd.discovery.wizard.api.FileConfiguration;
import org.netbeans.modules.cnd.discovery.wizard.api.ProjectConfiguration;
import org.netbeans.modules.cnd.discovery.wizard.api.support.ProjectBridge;
import org.netbeans.modules.cnd.discovery.wizard.checkedtree.AbstractRoot;
import org.netbeans.modules.cnd.discovery.wizard.checkedtree.UnusedFactory;
import org.netbeans.modules.cnd.makeproject.api.configurations.CCCCompilerConfiguration;
import org.netbeans.modules.cnd.makeproject.api.configurations.CCCompilerConfiguration;
import org.netbeans.modules.cnd.makeproject.api.configurations.CCompilerConfiguration;
import org.netbeans.modules.cnd.makeproject.api.configurations.ConfigurationDescriptorProvider;
import org.netbeans.modules.cnd.makeproject.api.configurations.Folder;
import org.netbeans.modules.cnd.makeproject.api.configurations.Item;
import org.netbeans.modules.cnd.makeproject.api.configurations.MakeConfiguration;
import org.netbeans.modules.cnd.makeproject.api.configurations.MakeConfigurationDescriptor;
import org.netbeans.modules.cnd.utils.CndPathUtilities;
import org.netbeans.modules.cnd.utils.MIMENames;
import org.openide.filesystems.FileObject;
import org.openide.util.NbBundle;
import org.openide.util.Utilities;

/**
 *
 */
public class DiscoveryProjectGeneratorImpl {
    private static final boolean DEBUG = Boolean.getBoolean("cnd.discovery.trace.project_update"); // NOI18N
    private static final boolean TRUNCATE_BEGINNING_PATH = true;

    /**
     * Old IDE behavior is random user include paths after consolidation.
     * Since 7.3 consolidation preserve paths order.
     * It can enlarge project's metadata. To forbid preserving use flag:
     * <pre>
     * -J-Dcnd.discovery.can_violate_paths_order=true
     * </pre>
     */
    private final ProjectBridge projectBridge;
    private final DiscoveryDescriptor wizard;
    private final String baseFolder;
    //private String level;

    /** Creates a new instance of PrjectGenerator */
    public DiscoveryProjectGeneratorImpl(DiscoveryDescriptor wizard) throws IOException {
        this.wizard = wizard;
        baseFolder = wizard.getRootFolder();
        Project project = wizard.getProject();
        if (project != null) {
            projectBridge = new ProjectBridge(project);
        } else {
            projectBridge = new ProjectBridge(baseFolder);
        }
    }

    private void storeCompileLines(List<ProjectConfiguration> projectConfigurations) {
        Project project = wizard.getProject();
        if (project != null) {
            ConfigurationDescriptorProvider pdp = project.getLookup().lookup(ConfigurationDescriptorProvider.class);
            if (pdp != null) {
                MakeConfigurationDescriptor makeConfigurationDescriptor = pdp.getConfigurationDescriptor();
                if (makeConfigurationDescriptor != null) {
                    MakeConfiguration activeConfiguration = makeConfigurationDescriptor.getActiveConfiguration();
                    if (activeConfiguration != null) {
                        List<FileConfiguration> confs = new ArrayList<>();
                        for (ProjectConfiguration config: projectConfigurations){
                            confs.addAll(config.getFiles());
                        }
                        final Iterator<FileConfiguration> iterator = confs.iterator();
                        CompileSupport support = new CompileSupport();
                        Iterator<String> it = new Iterator<String>() {

                            @Override
                            public boolean hasNext() {
                                return iterator.hasNext();
                            }

                            @Override
                            public String next() {
                                FileConfiguration next = iterator.next();
                                if (next.getCompileLine() != null) {
                                    return next.getFilePath()+"="+next.getCompilePath()+"#"+next.getCompileLine(); // NOI18N
                                }
                                return ""; // NOI18N
                            }

                            @Override
                            public void remove() {
                                throw new UnsupportedOperationException();
                            }
                        };
                        support.putOptions(makeConfigurationDescriptor, activeConfiguration, it);
                    }
                }
            }
        }

    }

    public void process(){
        List<ProjectConfiguration> projectConfigurations = wizard.getConfigurations();
        Folder sourceRoot = projectBridge.getRoot();
        createFolderStructure(projectConfigurations, sourceRoot);
        Set<Item> used = new HashSet<>();
        for (ProjectConfiguration config: projectConfigurations){
            setupCompilerConfiguration(config);
            addConfiguration(sourceRoot, config, used);
        }
        // add other files
        addAdditional(sourceRoot, baseFolder, used);
        if (TRUNCATE_BEGINNING_PATH) {
            packRoot(sourceRoot);
        }
        // move common file configuration to parent
        upConfiguration(sourceRoot, ItemProperties.LanguageKind.CPP);
        upConfiguration(sourceRoot, ItemProperties.LanguageKind.C);
        downConfiguration(sourceRoot, ItemProperties.LanguageKind.CPP);
        downConfiguration(sourceRoot, ItemProperties.LanguageKind.C);
        projectBridge.printStaticstic(sourceRoot, ImportProject.logger);
        storeCompileLines(projectConfigurations);
        projectBridge.save();
        projectBridge.dispose();
    }

    private void packRoot(Folder root) {
        for(Object item : root.getElements()) {
            if (!(item instanceof Folder)) {
                return;
            }
        }
        Map<Folder,Folder> res = new HashMap<>();
        for(Folder folder : root.getFolders()) {
            if (folder.getKind() == Folder.Kind.IMPORTANT_FILES_FOLDER) {
                res.put(folder,folder);
            } else if (folder.isDiskFolder()) {
                // do not change disk folder.
                res.put(folder,folder);
            } else {
                Folder packFolder = packFolder(folder);
                res.put(folder,packFolder);
            }
        }
        boolean isFullNames = false;
        for(int i = 0; i < 3; i++) {
            isFullNames = false;
            Map<String, List<Map.Entry<Folder,Folder>>> names = new HashMap<>();
            for(Map.Entry<Folder,Folder> entry : res.entrySet()) {
                String folderName = entry.getValue().getName();
                List<Map.Entry<Folder,Folder>> list = names.get(folderName);
                if (list == null) {
                    list = new ArrayList<>();
                    names.put(folderName, list);
                }
                list.add(entry);
                if (list.size() > 1) {
                    isFullNames = true;
                }
            }
            if (!isFullNames) {
                break;
            }
            for (Map.Entry<String, List<Map.Entry<Folder,Folder>>> entry : names.entrySet()) {
                if (entry.getValue().size() > 1) {
                    for(Map.Entry<Folder,Folder> e : entry.getValue()) {
                        Folder beg = e.getKey();
                        Folder end = e.getValue();
                        Folder up = end.getParent();
                        if (up != null && up != beg) {
                            res.put(beg, up);
                        } else {
                            // cannot resolve name conflict
                            return;
                        }
                    }
                }
            }
        }
        if (isFullNames) {
            // cannot resolve name conflict
            return;
        }
        List<Object> elements = root.getElements();
        if (res.size() == elements.size()) {
            boolean equals = true;
            for(Map.Entry<Folder,Folder> entry : res.entrySet()) {
                if (elements.indexOf(entry.getValue()) < 0) {
                    equals = false;
                }
            }
            if (equals) {
                for(Map.Entry<Folder,Folder> entry : res.entrySet()) {
                    if (entry.getKey().getKind() == Folder.Kind.IMPORTANT_FILES_FOLDER) {
                        continue;
                    } else if (entry.getValue().isDiskFolder()) {
                        continue;
                    } else {
                        if (entry.getValue().getRoot() == null) {
                            FileObject folderFile = getFolderFile(entry.getValue());
                            if (folderFile != null) {
                                entry.getValue().setRoot(projectBridge.getRelativepath(folderFile.getPath()));
                            }
                        }
                    }
                }
                return;
            }
        }
        root.reset();
        for(Map.Entry<Folder,Folder> entry : res.entrySet()) {
            if (entry.getKey().getKind() == Folder.Kind.IMPORTANT_FILES_FOLDER) {
                root.addFolder(entry.getValue(), true);
                continue;
            } else if (entry.getValue().isDiskFolder()) {
                root.addFolder(entry.getValue(), true);
            } else {
                if (entry.getValue().getRoot() == null) {
                    FileObject folderFile = getFolderFile(entry.getValue());
                    if (folderFile != null) {
                        entry.getValue().setRoot(projectBridge.getRelativepath(folderFile.getPath()));
                    }
                }
                root.addFolder(entry.getValue(), true);
            }
        }
    }

    private FileObject getFolderFile(Folder folder) {
        for(Item item : folder.getItemsAsArray()) {
            FileObject fo = item.getFileObject();
            if (fo != null) {
                FileObject parent = fo.getParent();
                if (parent != null) {
                    return parent;
                }
            }
        }
        for(Folder f : folder.getFolders()) {
            FileObject folderObject = getFolderFile(f);
            if (folderObject != null) {
                FileObject parent = folderObject.getParent();
                if (parent != null) {
                    return parent;
                }
            }
        }
        return null;
    }

    private Folder packFolder(Folder folder) {
        while(true) {
            if (folder.getElements().size() > 1) {
                return folder;
            }
            List<Folder> folders = folder.getFolders();
            if (folders.isEmpty()) {
                return folder;
            }
            folder = folders.get(0);
        }
    }

    private void downConfiguration(Folder folder, ItemProperties.LanguageKind lang) {
        CCCCompilerConfiguration cccc = projectBridge.getFolderConfiguration(lang, folder);
        if (cccc != null) {
            List<String> commonFoldersIncludes = cccc.getIncludeDirectories().getValue();
            List<String> commonFoldersFiles = cccc.getIncludeFiles().getValue();
            List<String> commonFoldersMacros = cccc.getPreprocessorConfiguration().getValue();
            List<String> commonFoldersUndefs = cccc.getUndefinedPreprocessorConfiguration().getValue();
            projectBridge.setupProject(commonFoldersIncludes, commonFoldersFiles, commonFoldersMacros, commonFoldersUndefs, lang);
            projectBridge.setupFolder(Collections.<String>emptyList(), true, Collections.<String>emptyList(), true,
                    Collections.<String>emptyList(), true,
                    Collections.<String>emptyList(), true, lang, folder);
            downConfiguration(folder, lang, commonFoldersIncludes, commonFoldersFiles, commonFoldersMacros, commonFoldersUndefs);
        }
    }

    private void downConfiguration(Folder folder, ItemProperties.LanguageKind lang, List<String> commonFoldersIncludes, List<String> commonFoldersFiles,
            List<String> commonFoldersMacros, List<String> commonFoldersUndefs) {
        for(Folder subFolder : folder.getFoldersAsArray()){
            CCCCompilerConfiguration cccc = projectBridge.getFolderConfiguration(lang, subFolder);
            if (cccc == null) {
                continue;
            }
            List<String> aCommonFoldersIncludes = new ArrayList<>(commonFoldersIncludes);
            List<String> aCommonFoldersFiles = new ArrayList<>(commonFoldersFiles);
            List<String> aCommonFoldersMacros = new ArrayList<>(commonFoldersMacros);
            List<String> aCommonFoldersUndefs = new ArrayList<>(commonFoldersUndefs);
            List<String> foldersIncludes = new ArrayList<>();
            List<String> foldersFiles = new ArrayList<>();
            List<String> foldersMacros = new ArrayList<>();
            List<String> foldersUndefs = new ArrayList<>();
            for(String s : cccc.getIncludeDirectories().getValue()){
                if (!aCommonFoldersIncludes.contains(s)) {
                    foldersIncludes.add(s);
                    aCommonFoldersIncludes.add(s);
                }
            }
            for(String s : cccc.getIncludeFiles().getValue()){
                if (!aCommonFoldersFiles.contains(s)) {
                    foldersFiles.add(s);
                    aCommonFoldersFiles.add(s);
                }
            }
            for(String s : cccc.getPreprocessorConfiguration().getValue()){
                if (!aCommonFoldersMacros.contains(s)) {
                    foldersMacros.add(s);
                    aCommonFoldersMacros.add(s);
                }
            }
            for(String s : cccc.getUndefinedPreprocessorConfiguration().getValue()){
                if (!aCommonFoldersUndefs.contains(s)) {
                    foldersUndefs.add(s);
                    aCommonFoldersUndefs.add(s);
                }
            }
            projectBridge.setupFolder(foldersIncludes, true, foldersFiles, true, foldersMacros, true, foldersUndefs, true, lang, subFolder);
            downConfiguration(subFolder, lang, aCommonFoldersIncludes, aCommonFoldersFiles, aCommonFoldersMacros, aCommonFoldersUndefs);
        }
    }

    private Folder getOrCreateFolder(Folder folder, String name, AbstractRoot used) {
        Folder added = null;
        Folder[] folders = folder.getFoldersAsArray();
        for (Folder folder1 : folders) {
            String root = folder1.getAbsolutePath();
            String orphan = used.getFolder();
            if (root != null && orphan != null && orphan.startsWith(root)) {
                String[] splitRoot = root.split("\\/"); // NOI18N
                String[] splitOrphan = orphan.split("\\/"); // NOI18N
                int lastEquals = -1;
                for(int j = 0; j < splitRoot.length && j < splitOrphan.length; j++) {
                    if (splitRoot[j].equals(splitOrphan[j])) {
                        lastEquals = j;
                    } else {
                        break;
                    }
                }
                if (lastEquals == splitRoot.length - 1) {
                    // ophan is subfolder of root
                    added = folder1;
                    for(int j = lastEquals + 1; j < splitOrphan.length; j++) {
                        Folder found = null;
                        for(Folder current : added.getFoldersAsArray()) {
                            if (current.getName().equals(splitOrphan[j])) {
                                found = current;
                                break;
                            }
                        }
                        if (found == null) {
                            found = projectBridge.createFolder(added, splitOrphan[j]);
                            added.addFolder(found, true);
                        }
                        added = found;
                    }
                    break;
                }
            }
            if (name != null && name.equals(folder1.getName())) {
                added = folder1;
                break;
            }
        }
        if (added == null) {
            added = projectBridge.createFolder(folder, name);
            //if (!folder.isDiskFolder()) {
            //    String additionalPath = used.getFolder();
            //    added.setRoot(CndPathUtilities.toRelativePath(folder.getConfigurationDescriptor().getBaseDir(), additionalPath));
            //    projectBridge.addSourceRoot(additionalPath);
            //}
            folder.addFolder(added, true);
        } else {
            if (added.isDiskFolder()) {
                String additionalPath = used.getFolder();
                String folderPath = CndPathUtilities.toAbsolutePath(folder.getConfigurationDescriptor().getBaseDirFileObject(), added.getRootPath());
                Folder logicalCandidate = null;
                if (!additionalPath.equals(folderPath)) {
                    for (Folder candidate : folder.getFolders()) {
                        if (candidate.isDiskFolder()) {
                            folderPath = CndPathUtilities.toAbsolutePath(folder.getConfigurationDescriptor().getBaseDirFileObject(), candidate.getRootPath());
                            if (additionalPath.equals(folderPath)) {
                                added = candidate;
                                break;
                            }
                        } else if (logicalCandidate == null && candidate.getName().equals(name)) {
                            logicalCandidate = candidate;
                        }
                    }
                }
                if (!additionalPath.equals(folderPath)) {
                    if (logicalCandidate == null) {
                        added = projectBridge.createFolder(folder, name);
                        //added.setRoot(CndPathUtilities.toRelativePath(folder.getConfigurationDescriptor().getBaseDir(), additionalPath));
                        //projectBridge.addSourceRoot(additionalPath);
                        folder.addFolder(added, true);
                    } else {
                        added = logicalCandidate;
                    }
                }
            }
        }
        return added;
    }

    private boolean upConfiguration(Folder folder, ItemProperties.LanguageKind lang) {
        Set<String> commonFoldersIncludes = new LinkedHashSet<>();
        Set<String> commonFoldersFiles = new LinkedHashSet<>();
        MacroMap commonFolderMacroMap = new MacroMap();
        Set<String> commonFoldersUndefs = new HashSet<>();
        boolean haveSubFolders = false;
        for (Folder subFolder : folder.getFolders()) {
            if (!upConfiguration(subFolder, lang)){
                continue;
            }
            if (!haveSubFolders) {
                CCCCompilerConfiguration cccc = projectBridge.getFolderConfiguration(lang, subFolder);
                if (cccc != null) {
                    commonFoldersIncludes.addAll(cccc.getIncludeDirectories().getValue());
                    commonFoldersFiles.addAll(cccc.getIncludeFiles().getValue());
                    commonFolderMacroMap.addAll(cccc.getPreprocessorConfiguration().getValue());
                    commonFoldersUndefs.addAll(cccc.getUndefinedPreprocessorConfiguration().getValue());
                    haveSubFolders = true;
                }
            } else {
                if (commonFoldersIncludes.size() > 0) {
                    CCCCompilerConfiguration cccc = projectBridge.getFolderConfiguration(lang, subFolder);
                    if (cccc != null) {
                        List<String> itemPaths = cccc.getIncludeDirectories().getValue();
                        int min = Math.min(commonFoldersIncludes.size(), itemPaths.size());
                        Iterator<String> it1 = commonFoldersIncludes.iterator();
                        Iterator<String> it2 = itemPaths.iterator();
                        int last = min;
                        for(int i = 0; i < min; i++) {
                            String next1 = it1.next();
                            String next2 = it2.next();
                            if (!next1.equals(next2)) {
                                last = i;
                                break;
                            }
                        }
                        commonFoldersIncludes = new LinkedHashSet<>();
                        if (last > 0) {
                            for(int i = 0; i < last; i++) {
                                commonFoldersIncludes.add(itemPaths.get(i));
                            }
                        }
                    }
                }
                if (commonFoldersFiles.size() > 0) {
                    CCCCompilerConfiguration cccc = projectBridge.getFolderConfiguration(lang, subFolder);
                    if (cccc != null) {
                        List<String> itemPaths = cccc.getIncludeFiles().getValue();
                        int min = Math.min(commonFoldersFiles.size(), itemPaths.size());
                        Iterator<String> it1 = commonFoldersFiles.iterator();
                        Iterator<String> it2 = itemPaths.iterator();
                        int last = min;
                        for(int i = 0; i < min; i++) {
                            String next1 = it1.next();
                            String next2 = it2.next();
                            if (!next1.equals(next2)) {
                                last = i;
                                break;
                            }
                        }
                        commonFoldersFiles = new LinkedHashSet<>();
                        if (last > 0) {
                            for(int i = 0; i < last; i++) {
                                commonFoldersFiles.add(itemPaths.get(i));
                            }
                        }
                    }
                }
                if (commonFolderMacroMap.size() > 0) {
                    CCCCompilerConfiguration cccc = projectBridge.getFolderConfiguration(lang, subFolder);
                    if (cccc != null) {
                        commonFolderMacroMap.retainAll(cccc.getPreprocessorConfiguration().getValue());
                    }
                }
                if (commonFoldersUndefs.size() > 0) {
                    CCCCompilerConfiguration cccc = projectBridge.getFolderConfiguration(lang, subFolder);
                    if (cccc != null) {
                        commonFoldersUndefs.retainAll(cccc.getUndefinedPreprocessorConfiguration().getValue());
                    }
                }
            }
        }
        Set<String> commonFilesIncludes = new LinkedHashSet<>();
        Set<String> commonFilesFiles = new HashSet<>();
        MacroMap commonFilesMacroMap  = new MacroMap();
        Set<String> commonFilesUndefs = new HashSet<>();
        boolean first = true;
        if (haveSubFolders) {
            commonFilesIncludes = new LinkedHashSet<>(commonFoldersIncludes);
            commonFilesFiles = new HashSet<>(commonFoldersFiles);
            commonFilesMacroMap = new MacroMap(commonFolderMacroMap);
            commonFilesUndefs = new HashSet<>(commonFoldersUndefs);
            first = false;
        }
        for (Item item : folder.getItemsAsArray()) {
            if (ProjectBridge.getExclude(item)){
                continue;
            }
            CCCCompilerConfiguration cccc = projectBridge.getItemConfiguration(item);
            if (lang == ItemProperties.LanguageKind.CPP) {
                if (!(cccc instanceof CCCompilerConfiguration)) {
                    continue;
                }
            } else if (lang == ItemProperties.LanguageKind.C) {
                if (!(cccc instanceof CCompilerConfiguration)) {
                    continue;
                }
            } else {
                continue;
            }
            if (first) {
                commonFilesIncludes.addAll(cccc.getIncludeDirectories().getValue());
                commonFilesFiles.addAll(cccc.getIncludeFiles().getValue());
                commonFilesMacroMap.addAll(cccc.getPreprocessorConfiguration().getValue());
                commonFilesUndefs.addAll(cccc.getUndefinedPreprocessorConfiguration().getValue());
                first = false;
            } else {
                if (commonFilesIncludes.size() > 0) {
                    List<String> itemPaths = cccc.getIncludeDirectories().getValue();
                    int min = Math.min(commonFilesIncludes.size(), itemPaths.size());
                    Iterator<String> it1 = commonFilesIncludes.iterator();
                    Iterator<String> it2 = itemPaths.iterator();
                    int last = min;
                    for(int i = 0; i < min; i++) {
                        String next1 = it1.next();
                        String next2 = it2.next();
                        if (!next1.equals(next2)) {
                            last = i;
                            break;
                        }
                    }
                    commonFilesIncludes = new LinkedHashSet<>();
                    if (last > 0) {
                        for(int i = 0; i < last; i++) {
                            commonFilesIncludes.add(itemPaths.get(i));
                        }
                    }
                }
                if (commonFilesFiles.size() > 0) {
                    List<String> itemPaths = cccc.getIncludeFiles().getValue();
                    int min = Math.min(commonFilesFiles.size(), itemPaths.size());
                    Iterator<String> it1 = commonFilesFiles.iterator();
                    Iterator<String> it2 = itemPaths.iterator();
                    int last = min;
                    for(int i = 0; i < min; i++) {
                        String next1 = it1.next();
                        String next2 = it2.next();
                        if (!next1.equals(next2)) {
                            last = i;
                            break;
                        }
                    }
                    commonFilesFiles = new LinkedHashSet<>();
                    if (last > 0) {
                        for(int i = 0; i < last; i++) {
                            commonFilesFiles.add(itemPaths.get(i));
                        }
                    }
                }
                if (commonFilesMacroMap.size() > 0) {
                    commonFilesMacroMap.retainAll(cccc.getPreprocessorConfiguration().getValue());
                }
                if (commonFilesUndefs.size() > 0) {
                    commonFilesUndefs.retainAll(cccc.getUndefinedPreprocessorConfiguration().getValue());
                }
            }
        }
        if (commonFilesIncludes.size() > 0 || commonFilesFiles.size() > 0 || commonFilesMacroMap.size() > 0 || commonFilesUndefs.size() > 0) {
            for (Item item : folder.getItemsAsArray()) {
                CCCCompilerConfiguration cccc = projectBridge.getItemConfiguration(item);
                if (lang == ItemProperties.LanguageKind.CPP) {
                    if (!(cccc instanceof CCCompilerConfiguration)) {
                        continue;
                    }
                } else if (lang == ItemProperties.LanguageKind.C) {
                    if (!(cccc instanceof CCompilerConfiguration)) {
                        continue;
                    }
                } else {
                    continue;
                }
                if (commonFilesIncludes.size() > 0) {
                    List<String> list = new ArrayList<>(cccc.getIncludeDirectories().getValue());
                    list.removeAll(commonFilesIncludes);
                    cccc.getIncludeDirectories().setValue(list);
                }
                if (commonFilesFiles.size() > 0) {
                    List<String> list = new ArrayList<>(cccc.getIncludeFiles().getValue());
                    list.removeAll(commonFilesFiles);
                    cccc.getIncludeFiles().setValue(list);
                }
                if (commonFilesMacroMap.size() > 0) {
                    List<String> list = new ArrayList<>(cccc.getPreprocessorConfiguration().getValue());
                    list = commonFilesMacroMap.removeCommon(list);
                    cccc.getPreprocessorConfiguration().setValue(list);
                }
                if (commonFilesUndefs.size() > 0) {
                    List<String> list = new ArrayList<>(cccc.getUndefinedPreprocessorConfiguration().getValue());
                    list.removeAll(commonFilesUndefs);
                    cccc.getUndefinedPreprocessorConfiguration().setValue(list);
                }
            }
        }
        CCCCompilerConfiguration cccc = projectBridge.getFolderConfiguration(lang, folder);
        if (cccc != null) {
            if (commonFilesIncludes.size() > 0) {
                cccc.getIncludeDirectories().setValue(new ArrayList<>(commonFilesIncludes));
            }
            if (commonFilesFiles.size() > 0) {
                cccc.getIncludeFiles().setValue(new ArrayList<>(commonFilesFiles));
            }
            if (commonFilesMacroMap.size() > 0) {
                cccc.getPreprocessorConfiguration().setValue(commonFilesMacroMap.convertToList());
            }
            if (commonFilesUndefs.size() > 0) {
                cccc.getUndefinedPreprocessorConfiguration().setValue(new ArrayList<>(commonFilesUndefs));
            }
        }
        return !first;
    }

    public Set<Project> makeProject(){
        ProgressHandle handle = ProgressHandleFactory.createHandle(NbBundle.getMessage(DiscoveryProjectGeneratorImpl.class, "UpdateCodeAssistance"));
        handle.start();
        try {
            if (projectBridge.isValid() && wizard.getConfigurations() != null && wizard.getConfigurations().size() > 0) {
                projectBridge.startModifications();
                process();
                return projectBridge.getResult();
            }
            return Collections.<Project>emptySet();
        } finally {
            handle.finish();
        }
    }

    private Set<String> getSourceFolders(){
        Set<String> used = new HashSet<>();
        List<ProjectConfiguration> projectConfigurations = wizard.getConfigurations();
        for (ProjectConfiguration conf : projectConfigurations) {
            for (FileConfiguration file : conf.getFiles()){
                String path = file.getFilePath();
                if (Utilities.isWindows()) {
                    path = path.replace('\\', '/');
                }
                int i = path.lastIndexOf('/');
                if (i > 0) {
                    path = path.substring(0,i+1);
                }
                used.add(path);
            }
        }
        used.addAll(compureRoots(used));
        return used;
    }

    private Set<String> compureRoots(Set<String> roots) {
        Set<String> res = new HashSet<>();
        ArrayList<String> root = null;
        for(String s : roots) {
            if (root == null) {
                root = new ArrayList<>();
                root.addAll(Arrays.asList(s.split("/"))); // NOI18N
                continue;
            }
            int i = 0;
            for(String segment : s.split("/")) { // NOI18N
                if (i < root.size()) {
                    if (!segment.equals(root.get(i))) {
                        while(root.size() > i) {
                            root.remove(root.size()-1);
                        }
                    }
                } else {
                    break;
                }
                i++;
            }
        }
        if (root != null && root.size() > 1) {
            StringBuilder buf = new StringBuilder();
            for(String s : root) {
                buf.append(s);
                buf.append('/');
            }
            res.add(buf.toString());
        }
        return res;
    }

    private void addAdditional(Folder folder, String base, Set<Item> usedItems){
        Set<String> folders = getSourceFolders();
        Set<String> used = new HashSet<>();
        Set<String> needAdd = new HashSet<>();
        Set<String> needCheck = new HashSet<>();
        List<String> list = wizard.getIncludedFiles();
        Map<String,Folder> preffered = projectBridge.prefferedFolders();
        for (String name : list){
            used.add(name);
            String path = projectBridge.getRelativepath(name);
            Item item = projectBridge.getProjectItem(path);
            if (item == null){
                path = name;
                if (Utilities.isWindows()) {
                    path = path.replace('\\', '/');
                }
                boolean isNeedAdd = false;
                if (path.startsWith(base)){
                    isNeedAdd = true;
                } else {
                    for(String dir : folders){
                        if (path.startsWith(dir)){
                            isNeedAdd = true;
                            break;
                        }
                    }
                }
                if (isNeedAdd){
                    int i = path.lastIndexOf('/');
                    if (i >= 0){
                        String folderPath = path.substring(0,i);
                        Folder prefferedFolder = preffered.get(folderPath);
                        if (prefferedFolder != null) {
                            item = projectBridge.createItem(name);
                            item = prefferedFolder.addItem(item);
                            ProjectBridge.setHeaderTool(item);
                            if(!MIMENames.isCppOrCOrFortran(item.getMIMEType())){
                                needCheck.add(path);
                            } else {
                                if (DEBUG) {System.err.println("Source is header:"+item.getAbsPath());} // NOI18N
                            }
                            ProjectBridge.setExclude(item, true);
                            ProjectBridge.excludeItemFromOtherConfigurations(item);
                            isNeedAdd = false;
                        }
                    }
                }
                if (isNeedAdd){
                    needCheck.add(path);
                    needAdd.add(name);
                }
            } else {
                if (!usedItems.contains(item)) {
                    ProjectBridge.setExclude(item,true);
                    ProjectBridge.setHeaderTool(item);
                } else {
                    if(!MIMENames.isCppOrCOrFortran(item.getMIMEType())){
                        needCheck.add(path);
                    }
                }
            }
        }
        if (needAdd.size()>0) {
            AbstractRoot additional = UnusedFactory.createRoot(needAdd, projectBridge.getBaseFolderFileSystem());
            if (additional.getName().isEmpty()) {
                for(AbstractRoot aRoot : additional.getChildren()) {
                    addAdditionalPreferedFolder(folder, aRoot);
                }
            } else {
                addAdditionalPreferedFolder(folder, additional);
            }
        }
        // remove unused
        List<ProjectConfiguration> projectConfigurations = wizard.getConfigurations();
        for (ProjectConfiguration conf : projectConfigurations) {
            for (FileConfiguration file : conf.getFiles()){
                used.add(file.getFilePath());
            }
        }
        Set<String> relatives = new HashSet<>();
        for (String name : used){
            relatives.add(projectBridge.getRelativepath(name));
        }
        TreeMap<String,Item> sorted = new TreeMap<>();
        for (Item item : projectBridge.getAllSources()){
            if (!usedItems.contains(item)) {
                sorted.put(item.getPath(),item);
            }
        }
        for (Map.Entry<String,Item> entry : sorted.entrySet()){
            String path = entry.getKey();
            Item item = entry.getValue();
            String canonicalPath = item.getNormalizedPath();
            if (!(relatives.contains(path) || used.contains(path) ||
                  relatives.contains(canonicalPath) || used.contains(canonicalPath))) {
                // remove item;
                if (DEBUG) {System.out.println("Exclude Item "+path);} // NOI18N
                ProjectBridge.setExclude(item,true);
            }
        }
        if (needCheck.size()>0) {
            projectBridge.checkForNewExtensions(needCheck);
        }
    }

    private void addAdditionalPreferedFolder(Folder folder, AbstractRoot additional){
        Folder rootCandidate = null;
        String root = additional.getFolder();
        if (Utilities.isWindows()) {
            root = root.replace('\\', '/');
        }
        int i = root.lastIndexOf('/');
        if (i > 0) {
            Map<String, Folder> prefferedFolders = projectBridge.prefferedFolders();
            root = root.substring(0,i);
            rootCandidate = prefferedFolders.get(root);
        }
        if (rootCandidate == null) {
            rootCandidate = folder;
        }
        addAdditionalFolder(rootCandidate, additional);
    }

    private void addAdditionalFolder(Folder folder, AbstractRoot used){
        String name = used.getName();
        Folder added = getOrCreateFolder(folder, name, used);
        if (added == null) {
            added = projectBridge.createFolder(folder, name);
            folder.addFolder(added, true);
        }
        for(AbstractRoot sub : used.getChildren()){
            addAdditionalFolder(added, sub);
        }
        List<String> files = used.getFiles();
        if (files != null) {
            for(String file : files){
                String path = projectBridge.getRelativepath(file);
                Item item =  projectBridge.getProjectItem(path);
                if (item!=null) {
                    if (item.getFolder() != added){
                        Object old = projectBridge.getAuxObject(item);
                        item.getFolder().removeItem(item);
                        item = added.addItem(item);
                        if (old != null) {
                            projectBridge.setAuxObject(item, old);
                        }
                    }
                } else {
                    item = projectBridge.createItem(file);
                    item = added.addItem(item);
                    ProjectBridge.excludeItemFromOtherConfigurations(item);
                }
                ProjectBridge.setExclude(item, true);
                ProjectBridge.setHeaderTool(item);
            }
        }
    }

    private void setupCompilerConfiguration(ProjectConfiguration config){
        // cleanup project configuration
        projectBridge.setupProject(Collections.<String>emptyList(), Collections.<String>emptyList(), Collections.<String>emptyList(), Collections.<String>emptyList(), config.getLanguageKind());
    }

    private List<String> buildMacrosString(final Map<String, String> map) {
        List<String> vector = new ArrayList<>();
        for(Map.Entry<String,String> entry : map.entrySet()){
            if (entry.getValue()!=null) {
                vector.add(entry.getKey()+"="+entry.getValue()); // NOI18N
            } else {
                vector.add(entry.getKey());
            }
        }
        return vector;
    }

    private void setupFile(FileConfiguration config, Item item, ItemProperties.LanguageKind lang) {
        ProjectBridge.setSourceTool(item,lang, config.getLanguageStandard(), wizard.isIncrementalMode());
        LinkedHashSet<String> set = new LinkedHashSet<>();
        LinkedHashSet<String> fileSet = new LinkedHashSet<>();
        Map<String,String> macros = new HashMap<>();
        reConsolidatePaths(set, config);
        reConsolidateFiles(fileSet, config, set);
        macros.putAll(config.getUserMacros());
        projectBridge.setupFile(config.getCompilePath(), new ArrayList<>(set), !config.overrideIncludes(),  new ArrayList<>(fileSet), !config.overrideFiles(),
                buildMacrosString(macros), !config.overrideMacros(),
                new ArrayList<>(config.getUndefinedMacros()), !config.overrideUndefinedMacros(), item, config.getImportantFlags());
    }

    private void reConsolidatePaths(Set<String> set, FileConfiguration file){
        projectBridge.convertIncludePaths(set, file.getUserInludePaths(), file.getCompilePath(), file.getFilePath());
    }

    private void reConsolidateFiles(Set<String> set, FileConfiguration file, Set<String> paths){
        projectBridge.convertIncludeFiles(set, file.getUserInludeFiles(), file.getCompilePath(), paths);
    }

    private void createFolderStructure(List<ProjectConfiguration> projectConfigurations, Folder sourceRoot ){
        Map<String,Set<Pair>> configurationStructure = new HashMap<>();
        for (ProjectConfiguration config: projectConfigurations){
            analyzeConfigurationStructure(config.getFiles(), config.getLanguageKind(), configurationStructure);
        }
        List<Pair> orphan = detectOrphan(configurationStructure, null);
        if (orphan.size() > 0) {
            createOrphan(sourceRoot, orphan, null);
        }
    }

    private void addConfiguration(Folder sourceRoot, ProjectConfiguration conf, Set<Item> used){
        ItemProperties.LanguageKind lang = conf.getLanguageKind();
        Map<String,Set<Pair>> configurationStructure = new HashMap<>();
        analyzeConfigurationStructure(conf.getFiles(), lang, configurationStructure);
        List<Pair> orphan = detectOrphan(configurationStructure, lang);
        if (orphan.size() > 0) {
            createOrphan(sourceRoot, orphan, lang);
        }
        // cleanup folder configurations
        Set<Folder> folders = new HashSet<>();
        for(Map.Entry<String,Set<Pair>> entry : configurationStructure.entrySet()){
            Set<Pair> files = entry.getValue();
            for(Pair pair : files){
                if (pair.item != null) {
                    Folder folder = pair.item.getFolder();
                    folders.add(folder);
                }
            }
        }
        for(Folder folder : folders){
            projectBridge.setupFolder(Collections.<String>emptyList(), true, Collections.<String>emptyList(), true,
                    Collections.<String>emptyList(), true, Collections.<String>emptyList(), true, conf.getLanguageKind(), folder);
        }
        for(Set<Pair> set : configurationStructure.values()){
            for(Pair pair : set){
                if (pair.item != null){
                    used.add(pair.item);
                }
            }
        }
    }

    private void createOrphan(Folder sourceRoot, List<Pair> orphan, ItemProperties.LanguageKind lang){
        Map<String,Pair> folders = new HashMap<>();
        for(Pair pair : orphan){
            String path = pair.fileConfiguration.getFilePath();
            folders.put(path,pair);
        }
        AbstractRoot additional = UnusedFactory.createRoot(folders.keySet(), projectBridge.getBaseFolderFileSystem());
        if (additional.getName().isEmpty()) {
            for(AbstractRoot aRoot : additional.getChildren()) {
                addFolder(sourceRoot, aRoot, folders, lang);
            }
        } else {
            addFolder(sourceRoot, additional, folders, lang);
        }
    }

    private void addFolder(Folder folder, AbstractRoot additional, Map<String,Pair> folders, ItemProperties.LanguageKind lang){
        String name = additional.getName();
        Folder added = getOrCreateFolder(folder, name, additional);
        for(AbstractRoot sub : additional.getChildren()){
            addFolder(added, sub, folders, lang);
        }
        for(String file : additional.getFiles()){
            Pair pair = folders.get(file);
            if (pair != null) {
                String path = projectBridge.getRelativepath(file);
                Item item = projectBridge.getProjectItem(path);
                if (item == null){
                    item = projectBridge.createItem(file);
                    item = added.addItem(item);
                    if (item != null) {
                        ProjectBridge.excludeItemFromOtherConfigurations(item);
                    }
                } else {
                    if (DEBUG) {System.err.println("Orphan pair found by path "+file);} // NOI18N
                }
                pair.item = item;
                if (lang != null) {
                    setupFile(pair.fileConfiguration, pair.item, lang);
                }
            } else {
                if (DEBUG) {System.err.println("Cannot find pair by path "+file);} // NOI18N
            }
        }
    }


    private List<Pair> detectOrphan(final Map<String, Set<Pair>> configurationStructure, ItemProperties.LanguageKind lang) {
        Map<String,Folder> preffered = projectBridge.prefferedFolders();
        List<Pair> orphan = new ArrayList<>();
        for(Map.Entry<String,Set<Pair>> entry : configurationStructure.entrySet()){
            Set<Pair> files = entry.getValue();
            Folder folder = null;
            List<Pair> list = new ArrayList<>();
            for(Pair pair : files){
                Item item = pair.item;
                if (item != null){
                    if (folder != null) {
                        folder = item.getFolder();
                    }
                } else {
                    String prefferedFolder = pair.fileConfiguration.getFilePath();
                    if (Utilities.isWindows()) {
                        prefferedFolder = prefferedFolder.replace('\\', '/'); // NOI18N
                    }
                    int i = prefferedFolder.lastIndexOf('/'); // NOI18N
                    if (i >= 0){
                        prefferedFolder = prefferedFolder.substring(0,i);
                        folder = preffered.get(prefferedFolder);
                    }
                    //if (folder == null) {
                        list.add(pair);
                    //}
                }
            }
            if (folder != null) {
                for(Pair pair : list){
                    String relPath = projectBridge.getRelativepath(pair.fileConfiguration.getFilePath());
                    Item item = projectBridge.getProjectItem(relPath);
                    if (item == null){
                        item = projectBridge.createItem(pair.fileConfiguration.getFilePath());
                        pair.item = item;
                        item = folder.addItem(item);
                        if (item != null) {
                            ProjectBridge.excludeItemFromOtherConfigurations(item);
                        }
                    }
                    if (lang != null) {
                        setupFile(pair.fileConfiguration, item, lang);
                    }
                }
            } else {
                for(Pair pair : list){
                    orphan.add(pair);
                }
            }
        }
        return orphan;
    }

    private void analyzeConfigurationStructure(List<FileConfiguration> files, ItemProperties.LanguageKind lang, Map<String,Set<Pair>> folders){
        for (FileConfiguration file : files){
            analyzeConfigurationStructure(file.getFilePath(), folders, file, lang);
        }
    }
    private void analyzeConfigurationStructure(String aPath, Map<String, Set<Pair>> folders, FileConfiguration file, LanguageKind lang) {
        String path = Utilities.isWindows() ? aPath.replace('\\', '/') : aPath;
        int i = path.lastIndexOf('/');
        if (i >= 0) {
            String folder = path.substring(0, i);
            Set<Pair> set = folders.get(folder);
            if (set == null) {
                set = new HashSet<>();
                folders.put(folder, set);
            }
            String relPath = projectBridge.getRelativepath(path);
            Item item = projectBridge.getProjectItem(relPath);
            if (item != null && lang != null) {
                setupFile(file, item, lang);
            }
            set.add(new Pair(file, item));
        }
    }

    private static class Pair{
        private FileConfiguration fileConfiguration;
        private Item item;
        private Pair(FileConfiguration fileConfiguration, Item item){
            this.fileConfiguration = fileConfiguration;
            this.item = item;
        }
    }

}
