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

package org.netbeans.modules.apisupport.project.ui.wizard.project;

import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import javax.imageio.ImageIO;
import javax.swing.Icon;
import javax.swing.JLabel;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.api.project.Sources;
import org.netbeans.api.queries.SharabilityQuery;
import org.netbeans.api.queries.VisibilityQuery;
import org.netbeans.api.templates.TemplateRegistration;
import org.netbeans.modules.apisupport.project.api.LayerHandle;
import org.netbeans.modules.apisupport.project.api.ManifestManager;
import org.netbeans.modules.apisupport.project.api.UIUtil;
import org.netbeans.modules.apisupport.project.api.Util;
import org.netbeans.modules.apisupport.project.spi.LayerUtil;
import org.netbeans.modules.apisupport.project.spi.NbModuleProvider;
import org.netbeans.modules.apisupport.project.ui.wizard.common.BasicWizardIterator;
import org.netbeans.modules.apisupport.project.ui.wizard.common.CreatedModifiedFiles;
import org.openide.WizardDescriptor;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileSystem;
import org.openide.filesystems.FileUtil;
import org.openide.modules.SpecificationVersion;
import org.openide.util.Exceptions;

/**
 * Wizard for creating new project templates.
 *
 * @author Milos Kleint
 */
@TemplateRegistration(folder = UIUtil.TEMPLATE_FOLDER, position = 1000, displayName = "#Templates/NetBeansModuleDevelopment/newProject", iconBase = "org/netbeans/modules/apisupport/project/ui/wizard/project/newProject.png", description = "newProject.html", category = UIUtil.TEMPLATE_CATEGORY)
public final class NewProjectIterator extends BasicWizardIterator {
    
    private NewProjectIterator.DataModel data;
    
    public static final String[] MODULES = {
        "org.openide.filesystems", // NOI18N
        "org.openide.loaders", // NOI18N
        "org.openide.dialogs", // NOI18N
        "org.openide.util", // NOI18N
        "org.openide.util.ui", // NOI18N
        "org.openide.util.lookup", // NOI18N
        "org.netbeans.api.templates", // NOI18N
        "org.netbeans.modules.projectuiapi", // NOI18N
        "org.netbeans.modules.projectapi", // NOI18N
        "org.openide.awt", // NOI18N
    };
    
    @Override
    public Set instantiate() throws IOException {
        CreatedModifiedFiles cmf = data.getCreatedModifiedFiles();
        cmf.run();
        return getCreatedFiles(cmf, data.getProject());
    }
    
    @Override
    protected BasicWizardIterator.Panel[] createPanels(WizardDescriptor wiz) {
        data = new NewProjectIterator.DataModel(wiz);
        return new BasicWizardIterator.Panel[] {
            new SelectProjectPanel(wiz, data),
            new NameAndLocationPanel(wiz, data)
        };
    }
    
    public @Override void uninitialize(WizardDescriptor wiz) {
        super.uninitialize(wiz);
        data = null;
    }
    
    static final class DataModel extends BasicWizardIterator.BasicDataModel {
        
        private Project template;
        private String name;
        private String displayName;
        private String category;
        
        private CreatedModifiedFiles files;
        
        DataModel(WizardDescriptor wiz) {
            super(wiz);
        }
        
        public CreatedModifiedFiles getCreatedModifiedFiles() {
            return getFiles();
        }
        
        public void setCreatedModifiedFiles(CreatedModifiedFiles files) {
            this.setFiles(files);
        }
        
        public Project getTemplate() {
            return template;
        }
        
        public void setTemplate(Project template) {
            this.template = template;
        }
        
        public String getName() {
            return name;
        }
        
        public void setName(String name) {
            this.name = name;
        }
        
        public String getDisplayName() {
            return displayName;
        }
        
        public void setDisplayName(String displayName) {
            this.displayName = displayName;
        }
        
        public String getCategory() {
            return category;
        }
        
        public void setCategory(String category) {
            this.category = category;
        }
        
        public CreatedModifiedFiles getFiles() {
            return files;
        }
        
        public void setFiles(CreatedModifiedFiles files) {
            this.files = files;
        }
        
    }
    
    public static void generateFileChanges(final DataModel model) {
        CreatedModifiedFiles fileChanges = new CreatedModifiedFiles(model.getProject());
        final Project project = model.getProject();
        final NbModuleProvider moduleInfo = model.getModuleInfo();
        final String category = model.getCategory();
        final String displayName = model.getDisplayName();
        final String name = model.getName();
        final String packageName = model.getPackageName();
        
        Map<String,Object> replaceTokens = new HashMap<String,Object>();
        replaceTokens.put("CATEGORY", category);//NOI18N
        replaceTokens.put("DISPLAYNAME", displayName);//NOI18N
        replaceTokens.put("TEMPLATENAME", name);//NOI18N
        replaceTokens.put("PACKAGENAME", packageName);//NOI18N
        
        
        // 1. create project description file
        final String descName = getRelativePath(moduleInfo.getResourceDirectoryPath(false), packageName,
                name, "Description.html"); //NOI18N
        FileObject template = CreatedModifiedFiles.getTemplate("templateDescription.html");//NOI18N
        fileChanges.add(fileChanges.createFileWithSubstitutions(descName, template, replaceTokens));
        
        // 2. update project dependencies
        for (int i = 0; i < MODULES.length; i++) {
            fileChanges.add(fileChanges.addModuleDependency(MODULES[i]));
        }

        // XXX use @Messages where available
        String bundlePath = getRelativePath(moduleInfo.getResourceDirectoryPath(false), packageName, "", "Bundle.properties");//NOI18N
        fileChanges.add(fileChanges.bundleKey(bundlePath, "LBL_CreateProjectStep",  "Name and Location")); // NOI18N
        
        // 3. create sample template
        boolean useTR = false;
        try {
            SpecificationVersion v = moduleInfo.getDependencyVersion("org.openide.loaders");
            if (v != null && v.compareTo(new SpecificationVersion("7.29")) >= 0) {
                useTR = true;
            }
        } catch (IOException x) {
            Exceptions.printStackTrace(x);
        }
        if (useTR) {
            replaceTokens.put("useTR", true);
            fileChanges.add(new CreatedModifiedFiles.AbstractOperation(project) {
                final String zipPath;
                final String iconPath;
                {
                    zipPath = getRelativePath(moduleInfo.getResourceDirectoryPath(false), packageName, name, "Project.zip");
                    iconPath = getRelativePath(moduleInfo.getResourceDirectoryPath(false), packageName, name, ".png");
                    addCreatedOrModifiedPath(zipPath, false);
                    addCreatedOrModifiedPath(iconPath, false);
                }
                @Override public void run() throws IOException {
                    FileObject zip = FileUtil.createData(project.getProjectDirectory(), zipPath);
                    OutputStream os = zip.getOutputStream();
                    try {
                        createProjectZip(os, model.getTemplate());
                    } finally {
                        os.close();
                    }
                    FileObject icon = FileUtil.createData(project.getProjectDirectory(), iconPath);
                    os = icon.getOutputStream();
                    try {
                        writeIcon(os, model.getTemplate());
                    } finally {
                        os.close();
                    }
                }
            });
        } else {
        fileChanges.add(fileChanges.bundleKeyDefaultBundle(category + "/" + name +  "Project.zip", displayName)); // NOI18N
        FileObject xml = LayerHandle.forProject(project).getLayerFile();
        FileObject parent = xml != null ? xml.getParent() : null;
        // XXX this is not fully accurate since if two ops would both create the same file,
        // really the second one would automatically generate a uniquified name... but close enough!
        Set<String> externalFiles = Collections.singleton(LayerUtil.findGeneratedName(parent, name + "Project.zip")); // NOI18N
        fileChanges.add(fileChanges.layerModifications(
                new LayerCreateProjectZipOperation(model.getTemplate(), name, packageName,
                category, ManifestManager.getInstance(Util.getManifest(moduleInfo.getManifestFile()), false)),externalFiles));
        }
        
        // x. generate java classes
        final String iteratorName = getRelativePath(moduleInfo.getSourceDirectoryPath(), packageName,
                name, "WizardIterator.java"); //NOI18N
        template = CreatedModifiedFiles.getTemplate("templateWizardIterator.java");//NOI18N
        fileChanges.add(fileChanges.createFileWithSubstitutions(iteratorName, template, replaceTokens));
        final String panelName = getRelativePath(moduleInfo.getSourceDirectoryPath(), packageName,
                name, "WizardPanel.java"); //NOI18N
        template = CreatedModifiedFiles.getTemplate("templateWizardPanel.java");//NOI18N
        fileChanges.add(fileChanges.createFileWithSubstitutions(panelName, template, replaceTokens));
        
        final String formName = getRelativePath(moduleInfo.getSourceDirectoryPath(), packageName,
                name, "PanelVisual.form"); //NOI18N
        template = CreatedModifiedFiles.getTemplate("templatePanelVisual.form");//NOI18N
        fileChanges.add(fileChanges.createFileWithSubstitutions(formName, template, replaceTokens));
        
        final String panelVisName = getRelativePath(moduleInfo.getSourceDirectoryPath(), packageName,
                name, "PanelVisual.java"); //NOI18N
        template = CreatedModifiedFiles.getTemplate("templatePanelVisual.java");//NOI18N
        fileChanges.add(fileChanges.createFileWithSubstitutions(panelVisName, template, replaceTokens));
        
        
        model.setCreatedModifiedFiles(fileChanges);
    }
    
    private static String getRelativePath(String rootPath, String fullyQualifiedPackageName,
            String prefix, String postfix) {
        StringBuilder sb = new StringBuilder();
        sb.append(rootPath).append('/').
                append(fullyQualifiedPackageName.replace('.','/')).
                append('/').append(prefix).append(postfix);
        return sb.toString();
    }
    
    private static void createProjectZip(OutputStream target, Project source) throws IOException {
        Sources srcs = ProjectUtils.getSources(source); // #63247: don't use lookup directly
        // assuming we got 1-sized array, should be enforced by UI.
        SourceGroup[] grps = srcs.getSourceGroups(Sources.TYPE_GENERIC);
        SourceGroup group = grps[0];
        Collection<FileObject> files = new ArrayList<FileObject>();
        collectFiles(group.getRootFolder(), files,
                SharabilityQuery.getSharability(group.getRootFolder()));
        createZipFile(target, group.getRootFolder(), files);
    }

    private static void writeIcon(OutputStream target, Project source) throws IOException {
        Icon icon = ProjectUtils.getInformation(source).getIcon();
        BufferedImage image = new BufferedImage(icon.getIconWidth(), icon.getIconHeight(), BufferedImage.TYPE_INT_ARGB);
        Graphics g = image.getGraphics();
        icon.paintIcon(new JLabel(), g, 0, 0);
        g.dispose();
        ImageIO.write(image, "png", target);
    }
    
    private static void collectFiles(FileObject parent, Collection<FileObject> accepted, SharabilityQuery.Sharability parentSharab) {
        for (FileObject fo : parent.getChildren()) {
            if (!VisibilityQuery.getDefault().isVisible(fo)) {
                // #66765: ignore invisible files/folders, like CVS subdirectory
                continue;
            }
            SharabilityQuery.Sharability sharab;
            if (parentSharab == SharabilityQuery.Sharability.UNKNOWN || parentSharab == SharabilityQuery.Sharability.MIXED) {
                sharab = SharabilityQuery.getSharability(fo);
            } else {
                sharab = parentSharab;
            }
            if (sharab == SharabilityQuery.Sharability.NOT_SHARABLE) {
                continue;
            }
            if (fo.isData() && !fo.isVirtual()) {
                accepted.add(fo);
            } else if (fo.isFolder()) {
                accepted.add(fo);
                collectFiles(fo, accepted, sharab);
            }
        }
    }
    
    private static void createZipFile(OutputStream target, FileObject root, Collection /* FileObject*/ files) throws IOException {
        ZipOutputStream str = null;
        try {
            str = new ZipOutputStream(target);
            Iterator it = files.iterator();
            while (it.hasNext()) {
                FileObject fo = (FileObject)it.next();
                String path = FileUtil.getRelativePath(root, fo);
                if (fo.isFolder() && !path.endsWith("/")) {
                    path = path + "/";
                }
                ZipEntry entry = new ZipEntry(path);
                str.putNextEntry(entry);
                if (fo.isData()) {
                    InputStream in = null;
                    try {
                        in = fo.getInputStream();
                        FileUtil.copy(in, str);
                    } finally {
                        if (in != null) {
                            in.close();
                        }
                    }
                }
                str.closeEntry();
            }
        } finally {
            if (str != null) {
                str.close();
            }
        }
    }
    
    private static class LayerCreateProjectZipOperation implements CreatedModifiedFiles.LayerOperation {
        
        private final String name;
        private final String packageName;
        private final Project templateProject;
        private final String category;
        private final ManifestManager manifestManager;
        
        LayerCreateProjectZipOperation(Project template, String name, String packageName,
                String category, ManifestManager manifestManager) {
            this.packageName = packageName;
            this.name = name;
            this.category = category;
            this.manifestManager = manifestManager;
            templateProject = template;
        }
        
        @Override
        public void run(FileSystem layer) throws IOException {
            FileObject folder = layer.getRoot().getFileObject(category);// NOI18N
            if (folder == null) {
                folder = FileUtil.createFolder(layer.getRoot(), category); // NOI18N
            }
            FileObject file = folder.createData(name + "Project", "zip"); // NOI18N
            createProjectZip(file.getOutputStream(), templateProject);
            // XXX use writeIcon
            String bundlePath = manifestManager.getLocalizingBundle();
            String suffix = ".properties"; // NOI18N
            if (bundlePath != null && bundlePath.endsWith(suffix)) {
                bundlePath = bundlePath.substring(0, bundlePath.length() - suffix.length()).replace('/', '.');
                file.setAttribute("displayName", "bundlevalue:" + bundlePath + "#" // NOI18N
                        + category + "/" + name +  "Project.zip");
            } else {
                // XXX what?
            }            
            file.setAttribute("template", Boolean.TRUE); // NOI18N            
            URL descURL = new URL("nbresloc:/" + packageName.replace('.', '/') + "/" + name + "Description.html"); // NOI18N
            file.setAttribute("instantiatingWizardURL", descURL); // NOI18N
            file.setAttribute("instantiatingIterator", "methodvalue:" + packageName + "." + name + "WizardIterator.createIterator"); // NOI18N
        }        
    }
    
}
