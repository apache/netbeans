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

package org.netbeans.modules.apisupport.project.ui.wizard.html;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.api.project.Project;
import org.netbeans.api.templates.TemplateRegistration;
import org.netbeans.modules.apisupport.project.api.UIUtil;
import org.netbeans.modules.apisupport.project.api.Util;
import org.netbeans.modules.apisupport.project.spi.NbModuleProvider;
import org.netbeans.modules.apisupport.project.ui.wizard.common.BasicWizardIterator;
import org.netbeans.modules.apisupport.project.ui.wizard.common.CreatedModifiedFiles;
import org.openide.WizardDescriptor;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileSystem;
import org.openide.filesystems.FileUtil;
import org.openide.modules.SpecificationVersion;
import org.openide.util.NbBundle.Messages;

/**
 * Wizard for creating new HTML based UI.
 *
 * @author Jaroslav Tulach
 */
@TemplateRegistration(
    folder=UIUtil.TEMPLATE_FOLDER,
    id="newHTML",
    position=191,
    displayName="#template_html",
    iconBase="org/netbeans/modules/apisupport/project/ui/wizard/html/newHTML.png",
    description="newHTML.html",
    category=UIUtil.TEMPLATE_CATEGORY
)
@Messages("template_html=Portable HTML UI")
public final class HTMLIterator extends BasicWizardIterator {

    private HTMLIterator.DataModel data;
    
    @Override
    public Set instantiate() throws IOException {
        CreatedModifiedFiles cmf = data.getCreatedModifiedFiles();
        cmf.run();
        return getCreatedFiles(cmf, data.getProject());
    }
    
    @Override
    protected BasicWizardIterator.Panel[] createPanels(WizardDescriptor wiz) {
        data = new HTMLIterator.DataModel(wiz);
        return new BasicWizardIterator.Panel[] {
            new NameAndLocationPanel(wiz, data)
        };
    }
    
    public @Override void uninitialize(WizardDescriptor wiz) {
        super.uninitialize(wiz);
        data = null;
    }
    
    static final class DataModel extends BasicWizardIterator.BasicDataModel {
        private String name;
        private String icon;
        private String mode;
        private boolean opened = false;
        private boolean keepPrefSize = false;
        private boolean slidingNotAllowed = false;
        private boolean closingNotAllowed = false;
        private boolean draggingNotAllowed = false;
        private boolean undockingNotAllowed = false;
        private boolean maximizationNotAllowed = false;
        private Map<String,String> newModes;
        private Set<String> existingModes;
        private boolean ignorePreviousRun = true;
        
        private CreatedModifiedFiles files;

        FileSystem sfs;
        
        DataModel(WizardDescriptor wiz) {
            super(wiz);
        }
        
        public CreatedModifiedFiles getCreatedModifiedFiles() {
            return getFiles();
        }
        
        public void setCreatedModifiedFiles(CreatedModifiedFiles files) {
            this.setFiles(files);
        }
        
        public String getName() {
            return name;
        }
        
        public void setName(String name) {
            this.name = name;
        }
        
        public CreatedModifiedFiles getFiles() {
            return files;
        }
        
        public void setFiles(CreatedModifiedFiles files) {
            this.files = files;
        }

        public String getIcon() {
            return icon;
        }

        public void setIcon(String icon) {
            this.icon = icon;
        }

        public String getMode() {
            return mode;
        }
        
        public void defineMode(String name, String definition) {
            if (newModes == null) {
                newModes = new TreeMap<String, String>();
            }
            newModes.put(name, definition);
        }
        void existingMode(String name) {
            if (existingModes == null) {
                existingModes = new TreeSet<String>();
            }
            existingModes.add(name);
        }
        boolean isExistingMode(String name) {
            return existingModes != null && existingModes.contains(name);
        }
    
        Map<String,String> getNewModes() {
            if (newModes == null) {
                return null;
            }
            TreeMap<String,String> copy = new TreeMap<String,String>(newModes);
            if (existingModes != null) {
                copy.keySet().removeAll(existingModes);
            }
            return copy.isEmpty() ? null : copy;
        }

        public boolean isIgnorePreviousRun() {
            return ignorePreviousRun;
        }

        public void setIgnorePreviousRun(boolean ignorePreviousRun) {
            this.ignorePreviousRun = ignorePreviousRun;
        }

        public void setMode(String mode) {
            this.mode = mode;
        }

        public boolean isOpened() {
            return opened;
        }

        public void setOpened(boolean opened) {
            this.opened = opened;
        }
        
        public boolean isKeepPrefSize() {
            return keepPrefSize;
        }

        public void setKeepPrefSize(boolean keepPrefSize) {
            this.keepPrefSize = keepPrefSize;
        }

        public boolean isClosingNotAllowed() {
            return closingNotAllowed;
        }

        public void setClosingNotAllowed(boolean closingNotAllowed) {
            this.closingNotAllowed = closingNotAllowed;
        }

        public boolean isDraggingNotAllowed() {
            return draggingNotAllowed;
        }

        public void setDraggingNotAllowed(boolean draggingNotAllowed) {
            this.draggingNotAllowed = draggingNotAllowed;
        }

        public boolean isMaximizationNotAllowed() {
            return maximizationNotAllowed;
        }

        public void setMaximizationNotAllowed(boolean maximizationNotAllowed) {
            this.maximizationNotAllowed = maximizationNotAllowed;
        }

        public boolean isSlidingNotAllowed() {
            return slidingNotAllowed;
        }

        public void setSlidingNotAllowed(boolean slidingNotAllowed) {
            this.slidingNotAllowed = slidingNotAllowed;
        }

        public boolean isUndockingNotAllowed() {
            return undockingNotAllowed;
        }

        public void setUndockingNotAllowed(boolean undockingNotAllowed) {
            this.undockingNotAllowed = undockingNotAllowed;
        }
        void setSFS(FileSystem sfs) {
            this.sfs = sfs;
        }
    }
    
    static void generateFileChanges(final DataModel model) {
        CreatedModifiedFiles fileChanges = new CreatedModifiedFiles(model.getProject());
        Project project = model.getProject();
        NbModuleProvider moduleInfo = model.getModuleInfo();
        final String name = model.getName();
        final String packageName = model.getPackageName();
        final String mode = model.getMode();

        try {
            SpecificationVersion current = model.getModuleInfo().getDependencyVersion("org.netbeans.api.htmlui");
        } catch (IOException ex) {
            Logger.getLogger(HTMLIterator.class.getName()).log(Level.INFO, null, ex);
        }

        if (name != null) {
            Map<String,String> replaceTokens = new HashMap<String,String>();
            replaceTokens.put("TEMPLATENAME", name);//NOI18N
            replaceTokens.put("PACKAGENAME", packageName);//NOI18N

            // 0. move icon file if necessary
            String icon = model.getIcon();
            File fil = null;
            if (icon != null) {
                fil = new File(icon);
                if (!fil.exists()) {
                    fil = null;
                }
            }
            // XXX this should be using addCreateIconOperation
            String relativeIconPath = null;
            if (fil != null) {
                FileObject fo = FileUtil.toFileObject(fil);
                if (!FileUtil.isParentOf(Util.getResourceDirectory(project), fo)) {
                    String iconPath = getRelativePath(moduleInfo.getResourceDirectoryPath(false), packageName, 
                                                    "", fo.getNameExt()); //NOI18N
                    fileChanges.add(fileChanges.createFile(iconPath, fo));
                    relativeIconPath = packageName.replace('.', '/') + "/" + fo.getNameExt(); // NOI18N
                } else {
                    relativeIconPath = FileUtil.getRelativePath(Util.getResourceDirectory(project), fo);
                }
                replaceTokens.put("ICONPATH", relativeIconPath);//NOI18N
                replaceTokens.put("COMMENTICON", "");//NOI18N

            } else {
                replaceTokens.put("ICONPATH", "SET/PATH/TO/ICON/HERE"); //NOI18N
                replaceTokens.put("COMMENTICON", "//");//NOI18N
            }


            // 2. update project dependencies
            replaceTokens.put("MODULENAME", moduleInfo.getCodeNameBase()); // NOI18N
            String specVersion = moduleInfo.getSpecVersion();
            replaceTokens.put("SPECVERSION", specVersion != null ? specVersion : "0"); // NOI18N
            fileChanges.add(fileChanges.addModuleDependency("org.netbeans.api.htmlui")); //NOI18N
            fileChanges.add(fileChanges.addModuleDependency("net.java.html")); //NOI18N
            fileChanges.add(fileChanges.addModuleDependency("net.java.html.json")); //NOI18N
            fileChanges.add(fileChanges.addModuleDependency("net.java.html.js")); //NOI18N
            fileChanges.add(fileChanges.addModuleDependency("org.openide.util")); //NOI18N
            fileChanges.add(fileChanges.addModuleDependency("org.openide.awt")); //NOI18N

            // x. generate java classes
            final String tcName = getRelativePath(moduleInfo.getSourceDirectoryPath(), packageName,
                    name, "Cntrl.java"); //NOI18N
            FileObject template = CreatedModifiedFiles.getTemplate(
                "HTML.java"
            );
            fileChanges.add(fileChanges.createFileWithSubstitutions(tcName, template, replaceTokens));
            // x. generate java classes
            final String tcFormName = getRelativePath(moduleInfo.getSourceDirectoryPath(), packageName,
                    name, ".html"); //NOI18N
            template = CreatedModifiedFiles.getTemplate("HTML.html");//NOI18N
            fileChanges.add(fileChanges.createFileWithSubstitutions(tcFormName, template, replaceTokens));
        }
        
        model.setCreatedModifiedFiles(fileChanges);
    }

    private static String getRelativePath(String rootpath, String fullyQualifiedPackageName,
            String prefix, String postfix) {
        StringBuilder sb = new StringBuilder();
        
        sb.append(rootpath).append('/').append(fullyQualifiedPackageName.replace('.','/'))
                        .append('/').append(prefix).append(postfix);
        
        return sb.toString();
    }
    
}
