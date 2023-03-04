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
package org.netbeans.modules.web.wizards;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataFolder;
import org.netbeans.api.java.project.JavaProjectConstants;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.api.project.Sources;

public class TargetEvaluator extends Evaluator {

    private List<String> pathItems = null;
    private DeployData deployData = null;
    private String fileName;
    private String className;

    TargetEvaluator(FileType fileType, DeployData deployData) {
        super(fileType);
        this.deployData = deployData;
    }

    String getErrorMessage() {
        return "";
    }

    /**
     * Used to get the deploy data object
     */
    DeployData getDeployData() {
        return deployData;
    }

    /**
     * Used by the various wizard panels to display the classname of
     * the target
     */
    String getClassName() {
        return className;
    }

    /**
     * Used by the various wizard panels to display the classname of
     * the target
     */
    void setClassName(String fileName, String targetFolder) {
        if (targetFolder.length() > 0) {
            className = targetFolder + "." + fileName;
        } else {
            className = fileName;
        }
        this.fileName = fileName;
    }

    /**
     * Used by the DD info panels to generate default names
     */
    String getFileName() {
        return fileName;
    }

    /**
     * Used by the servlet wizard when creating the files
     */
    Iterator<String> getPathItems() {
        return pathItems.iterator();
    }

    String getTargetPath() {
        return super.getTargetPath(pathItems.iterator());
    }

    /**
     * Used by the ObjectNameWizard panel to set the target folder
     * gotten from the system wizard initially. 
     */
    void setInitialFolder(DataFolder selectedFolder, Project p) {
        if (selectedFolder == null) {
            return;
        }
        FileObject targetFolder = selectedFolder.getPrimaryFile();
        Sources sources = ProjectUtils.getSources(p);
        SourceGroup[] groups = sources.getSourceGroups(JavaProjectConstants.SOURCES_TYPE_JAVA);
        String packageName = null;
        for (int i = 0; i < groups.length && packageName == null; i++) {
            packageName = org.openide.filesystems.FileUtil.getRelativePath(groups[i].getRootFolder(), targetFolder);
            deployData.setWebApp(DeployData.getWebAppFor(groups[i].getRootFolder()));
        }
        if (packageName == null) {
            packageName = "";
        }
        setInitialPath(packageName);
    }

    /** 
     * Used by the system wizard to check whether the input so far is valid
     */
    boolean isValid() {
        return true;
    }

    /**
     * Calculates the package name for a new Servlet/Filter/Listener
     * based on the path to the file system relative to the target
     * directory. If the user selected a directory from the web module
     * file system under WEB-INF/classes, then we strip off the
     * WEB-INF/classes portion from the path name. 
     */
    private void setInitialPath(String dirPath) {
        pathItems = new ArrayList<String>();

        String path[] = dirPath.split("/"); //NOI18N
        if (path.length > 0) {
            for (int i = 0; i < path.length; ++i) {
                if (!path[i].equals("")) {
                    pathItems.add(path[i]);
                }
            }
        }
    }
}
