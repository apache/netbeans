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

package org.netbeans.modules.groovy.grailsproject.ui.wizards.impl;

import java.util.List;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.modules.groovy.grailsproject.SourceCategory;
import org.netbeans.modules.groovy.grailsproject.SourceCategoriesFactory;
import org.netbeans.modules.groovy.grailsproject.SourceCategoryType;
import org.netbeans.modules.groovy.grailsproject.ui.TemplatesImpl;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

/**
 *
 * @author Martin Adamek
 */
public class GrailsArtifacts {

    public static SourceCategoryType getCategoryTypeForFolder(FileObject projectRoot, FileObject fileObject, SourceCategoriesFactory sourceCategoriesFactory) {
        String dirName = null;
        if (projectRoot != null && fileObject.isFolder()) {
            dirName = FileUtil.getRelativePath(projectRoot, fileObject);
        }
        if (dirName == null) {
            return null;
        }

        if (sourceCategoriesFactory.getSourceCategory( SourceCategoryType.GRAILSAPP_CONF).getRelativePath().equals(dirName)) {
            return SourceCategoryType.GRAILSAPP_CONF;
        } else if (sourceCategoriesFactory.getSourceCategory( SourceCategoryType.GRAILSAPP_CONTROLLERS).getRelativePath().equals(dirName)) {
            return SourceCategoryType.GRAILSAPP_CONTROLLERS;
        } else if (sourceCategoriesFactory.getSourceCategory( SourceCategoryType.GRAILSAPP_DOMAIN).getRelativePath().equals(dirName)) {
            return SourceCategoryType.GRAILSAPP_DOMAIN;
        } else if (sourceCategoriesFactory.getSourceCategory( SourceCategoryType.TEST_INTEGRATION).getRelativePath().equals(dirName)) {
            return SourceCategoryType.TEST_INTEGRATION;
        } else if (sourceCategoriesFactory.getSourceCategory( SourceCategoryType.LIB).getRelativePath().equals(dirName)) {
            return SourceCategoryType.LIB;
        } else if (sourceCategoriesFactory.getSourceCategory( SourceCategoryType.GRAILSAPP_I18N).getRelativePath().equals(dirName)) {
            return SourceCategoryType.GRAILSAPP_I18N;
        } else if (sourceCategoriesFactory.getSourceCategory( SourceCategoryType.SCRIPTS).getRelativePath().equals(dirName)) {
            return SourceCategoryType.SCRIPTS;
        } else if (sourceCategoriesFactory.getSourceCategory( SourceCategoryType.GRAILSAPP_SERVICES).getRelativePath().equals(dirName)) {
            return SourceCategoryType.GRAILSAPP_SERVICES;
        } else if (sourceCategoriesFactory.getSourceCategory( SourceCategoryType.SRC_GROOVY).getRelativePath().equals(dirName)) {
            return SourceCategoryType.SRC_GROOVY;
        } else if (sourceCategoriesFactory.getSourceCategory( SourceCategoryType.SRC_JAVA).getRelativePath().equals(dirName)) {
            return SourceCategoryType.SRC_JAVA;
        } else if (sourceCategoriesFactory.getSourceCategory( SourceCategoryType.SRC_GWT).getRelativePath().equals(dirName)) {
            return SourceCategoryType.SRC_GWT;
        } else if (sourceCategoriesFactory.getSourceCategory( SourceCategoryType.GRAILSAPP_TAGLIB).getRelativePath().equals(dirName)) {
            return SourceCategoryType.GRAILSAPP_TAGLIB;
        } else if (sourceCategoriesFactory.getSourceCategory( SourceCategoryType.TEST_UNIT).getRelativePath().equals(dirName)) {
            return SourceCategoryType.TEST_UNIT;
        } else if (sourceCategoriesFactory.getSourceCategory( SourceCategoryType.GRAILSAPP_UTILS).getRelativePath().equals(dirName)) {
            return SourceCategoryType.GRAILSAPP_UTILS;
        } else if (sourceCategoriesFactory.getSourceCategory( SourceCategoryType.GRAILSAPP_VIEWS).getRelativePath().equals(dirName)) {
            return SourceCategoryType.GRAILSAPP_VIEWS;
        } else if (sourceCategoriesFactory.getSourceCategory( SourceCategoryType.WEBAPP).getRelativePath().equals(dirName)) {
            return SourceCategoryType.WEBAPP;
        }
        return null;
    }

    public static SourceCategoryType getCategoryTypeForTemplate(FileObject template) {
        String templatePath = template.getPath();
        if (TemplatesImpl.CONTROLLER.equals(templatePath)) { // NOI18N
            return SourceCategoryType.GRAILSAPP_CONTROLLERS;
        } else if (TemplatesImpl.DOMAIN_CLASS.equals(templatePath)) {
            return SourceCategoryType.GRAILSAPP_DOMAIN;
        } else if (TemplatesImpl.GANT_SCRIPT.equals(templatePath)) {
            return SourceCategoryType.SCRIPTS;
        } else if (TemplatesImpl.GROOVY_CLASS.equals(templatePath)) {
            return SourceCategoryType.SRC_GROOVY;
        } else if (TemplatesImpl.GROOVY_SCRIPT.equals(templatePath)) {
            return SourceCategoryType.SCRIPTS;
        } else if (TemplatesImpl.GSP.equals(templatePath)) {
            return SourceCategoryType.GRAILSAPP_VIEWS;
        } else if (TemplatesImpl.INTEGRATION_TEST.equals(templatePath)) {
            return SourceCategoryType.TEST_INTEGRATION;
        } else if (TemplatesImpl.SERVICE.equals(templatePath)) {
            return SourceCategoryType.GRAILSAPP_SERVICES;
        } else if (TemplatesImpl.TAG_LIB.equals(templatePath)) {
            return SourceCategoryType.GRAILSAPP_TAGLIB;
        } else if (TemplatesImpl.UNIT_TEST.equals(templatePath)) {
            return SourceCategoryType.TEST_UNIT;
        }
        return null;
    }

    public static SourceGroup getSourceGroupForCategory(Project project, List<SourceGroup> groups, SourceCategory category) {
        FileObject projectRoot = project.getProjectDirectory();
        for (SourceGroup group : groups) {
            FileObject folder = group.getRootFolder();
            if (category != null) {
                FileObject categoryFolder = projectRoot.getFileObject(category.getRelativePath());
                if (folder.equals(categoryFolder)) {
                    return group;
                }
            }
        }

        return null;
    }
}
