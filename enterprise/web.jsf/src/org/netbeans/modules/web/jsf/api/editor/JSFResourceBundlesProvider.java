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
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
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
                List<ResourceBundle> result = new ArrayList<>();
                for (Application application : applications) {
                    for (org.netbeans.modules.web.jsf.api.facesmodel.ResourceBundle bundle : application.getResourceBundles()) {
                        if (bundle.getBaseName() == null) {
                            continue;
                        }
                        List<FileObject> files = new ArrayList<>();
                        // java source source groups
                        for (SourceGroup sourceGroup : SourceGroups.getJavaSourceGroups(project)) {
                            FileObject bundleFile = getBundleFileInSourceGroup(sourceGroup, bundle);
                            if (bundleFile != null) {
                                files.add(bundleFile);
                            }
                        }
                        // resource source groups
                        for (SourceGroup sourceGroup : ProjectUtils.getSources(project).getSourceGroups(JavaProjectConstants.SOURCES_TYPE_RESOURCES)) {
                            FileObject bundleFile = getBundleFileInSourceGroup(sourceGroup, bundle);
                            if (bundleFile != null) {
                                files.add(bundleFile);
                            }
                        }
                        
                        result.add(new ResourceBundle(bundle.getBaseName(), bundle.getVar(), files));
                    }
                }
                return result;
            });
        } catch (IOException | IllegalStateException ex) {
            LOGGER.log(Level.INFO, "Failed to read resource bundles for " + project, ex);
        }
        return Collections.emptyList();
    }

    private static FileObject getBundleFileInSourceGroup(SourceGroup sourceGroup, org.netbeans.modules.web.jsf.api.facesmodel.ResourceBundle bundle) {
        int lastDelim = bundle.getBaseName().lastIndexOf("/"); //NOI18N
        String bundleName = bundle.getBaseName().substring(lastDelim + 1);
        if (lastDelim <= 0) {
            // in root folder or default package
            return getBundleInFolder(sourceGroup.getRootFolder(), bundleName);
        } else {
            // in the subfolder or non-default package
            String parentFolder = bundle.getBaseName().replace(".", "/").substring(0, lastDelim); //NOI18N
            return getBundleInFolder(sourceGroup.getRootFolder().getFileObject(parentFolder), bundleName);
        }
    }

    private static FileObject getBundleInFolder(FileObject folder, String bundleName) {
        if (folder != null && folder.isValid() && folder.isFolder()) {
            for (FileObject fo : folder.getChildren()) {
                if (fo.getName().startsWith(bundleName) && "properties".equals(fo.getExt())) { //NOI18N
                    return fo;
                }
            }
        }
        return null;
    }

}
