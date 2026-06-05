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

package org.netbeans.modules.web.jsf.api.editor;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.project.JavaProjectConstants;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.modules.j2ee.core.api.support.SourceGroups;
import org.netbeans.modules.j2ee.metadata.model.api.MetadataModel;
import org.netbeans.modules.web.el.spi.ResourceBundle;
import org.netbeans.modules.web.jsf.JSFUtils;
import org.netbeans.modules.web.jsf.api.facesmodel.Application;
import org.netbeans.modules.web.jsf.api.metamodel.JsfModel;
import org.openide.filesystems.FileObject;

/**
 *
 * @author marekfukala
 */
public class JSFResourceBundlesProvider {

    private static final Logger LOGGER = Logger.getLogger(JSFResourceBundlesProvider.class.getName());

    public static List<ResourceBundle> getResourceBundles(final Project project) {
        MetadataModel<JsfModel> model = JSFUtils.getModel(project);
        if (model == null) {
            return Collections.emptyList();
        }
        try {
            return model.runReadAction(metadata -> {
                List<Application> applications = metadata.getElements(Application.class);

                if (applications.isEmpty()) {
                    return Collections.emptyList();
                }

                List<SourceGroup> allSourceGroups = new ArrayList<>();
                Collections.addAll(allSourceGroups, SourceGroups.getJavaSourceGroups(project));
                Collections.addAll(allSourceGroups, ProjectUtils.getSources(project).getSourceGroups(JavaProjectConstants.SOURCES_TYPE_RESOURCES));

                ClassPath compileClassPath = null;
                if (!allSourceGroups.isEmpty()) {
                    compileClassPath = ClassPath.getClassPath(allSourceGroups.get(0).getRootFolder(), ClassPath.COMPILE);
                }

                List<ResourceBundle> result = new ArrayList<>();
                for (Application application : applications) {
                    for (org.netbeans.modules.web.jsf.api.facesmodel.ResourceBundle bundle : application.getResourceBundles()) {
                        String baseName = bundle.getBaseName();
                        if (baseName == null) {
                            continue;
                        }
                        Set<FileObject> fileSet = new LinkedHashSet<>();
                        int lastDelim = baseName.lastIndexOf("."); 
                        String bundleSimpleName = (lastDelim <= 0) ? baseName : baseName.substring(lastDelim + 1);
                        String packagePath = (lastDelim <= 0) ? "" : baseName.replace(".", "/").substring(0, lastDelim);

                        for (SourceGroup sourceGroup : allSourceGroups) {
                            FileObject root = sourceGroup.getRootFolder();
                            FileObject folder = (lastDelim <= 0) ? root : root.getFileObject(packagePath);
                            addBundleFilesInFolder(fileSet, folder, bundleSimpleName);
                        }

                        if (fileSet.isEmpty() && compileClassPath != null) {
                            addBundleFilesInCompileClasspath(fileSet, compileClassPath, bundleSimpleName, packagePath);
                        }

                        result.add(new ResourceBundle(bundle.getBaseName(), bundle.getVar(), new ArrayList<>(fileSet)));
                    }
                }
                return result;
            });
        } catch (IOException | IllegalStateException ex) {
            LOGGER.log(Level.INFO, "Failed to read resource bundles for " + project, ex);
        }
        return Collections.emptyList();
    }

    private static void addBundleFilesInFolder(Set<FileObject> files, FileObject folder, String bundleName) {
        if (folder != null && folder.isValid() && folder.isFolder()) {
            for (FileObject fo : folder.getChildren()) {
                if ("properties".equals(fo.getExt())
                        && (fo.getName().equals(bundleName) || fo.getName().startsWith(bundleName + "_"))) {
                    files.add(fo);
                }
            }
        }
    }

    private static void addBundleFilesInCompileClasspath(Set<FileObject> files, ClassPath cp, String bundleName, String packagePath) {
        List<FileObject> folders = cp.findAllResources(packagePath);
        for (FileObject folder : folders) {
            addBundleFilesInFolder(files, folder, bundleName);
        }
    }

}
