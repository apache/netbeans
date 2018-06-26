/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
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
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
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
