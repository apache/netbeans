/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2007 Sun Microsystems, Inc.
 */

package org.netbeans.modules.groovy.grailsproject.classpath;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import org.netbeans.api.queries.VisibilityQuery;
import org.netbeans.modules.groovy.grailsproject.GrailsProject;
import org.netbeans.modules.groovy.grailsproject.GrailsSources;
import org.netbeans.modules.groovy.grailsproject.SourceCategory;
import org.netbeans.modules.groovy.grailsproject.SourceCategoriesFactory;
import org.netbeans.modules.groovy.grailsproject.SourceCategoryType;
import org.netbeans.modules.groovy.grailsproject.plugins.GrailsPlugin;
import org.netbeans.modules.groovy.grailsproject.plugins.GrailsPluginSupport;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Exceptions;
import org.openide.util.Utilities;

/**
 *
 * @author Martin Adamek
 */
public class SourceRoots {

    private final FileObject projectRoot;

    private final GrailsProject project;

    public SourceRoots(GrailsProject project, FileObject projectRoot) {
        this.projectRoot = projectRoot;
        this.project = project;
    }

    public FileObject[] getRoots() {
        // FIXME optimize this
        List<FileObject> result = new ArrayList<>();
        addGrailsSourceRoots(projectRoot, result);

        if (project != null) {
            GrailsPluginSupport pluginSupport = GrailsPluginSupport.forProject(project);
            if (pluginSupport != null) {
                result.addAll(addPluginRoots(project.getBuildConfig().getProjectPluginsDir(), pluginSupport.getProjectPluginFilter()));
            }
            result.addAll(addPluginRoots(project.getBuildConfig().getGlobalPluginsDir(), null));

            // in-place plugins
            for (GrailsPlugin plugin : project.getBuildConfig().getLocalPlugins()) {
                if (plugin.getPath() != null) {
                    FileObject fo = FileUtil.toFileObject(FileUtil.normalizeFile(plugin.getPath()));
                    if (fo != null) {
                        addGrailsSourceRoots(fo, result);
                    }
                }
            }
        }

        return result.toArray(new FileObject[result.size()]);
    }

    public List<URL> getRootURLs() {
        List<URL> urls = new ArrayList<>();
        try {
            for (FileObject fileObject : getRoots()) {
                urls.add(Utilities.toURI(FileUtil.toFile(fileObject)).toURL());
            }
        } catch (MalformedURLException murle) {
            Exceptions.printStackTrace(murle);
        }
        return urls;
    }

    private List<FileObject> addPluginRoots(File pluginsDirFile, GrailsPluginSupport.FolderFilter filter) {
        if (pluginsDirFile == null) {
            return Collections.emptyList();
        }

        final FileObject pluginsDir = FileUtil.toFileObject(FileUtil.normalizeFile(pluginsDirFile));
        if (pluginsDir != null) {
            List<FileObject> result = new ArrayList<>();
            for(Enumeration<? extends FileObject> subfolders = pluginsDir.getFolders(false);
                    subfolders.hasMoreElements();) {

                FileObject subFolder = subfolders.nextElement();
                if (filter == null || filter.accept(subFolder.getNameExt())) {
                    addGrailsSourceRoots(subFolder, result);
                }
            }
            return result;
        }
        return Collections.emptyList();
    }

    private void addGrailsSourceRoots(FileObject projectRoot, List<FileObject> result) {
        SourceCategoriesFactory sourceCategoriesFactory = project.getSourceCategoriesFactory();
        addRoot(projectRoot, sourceCategoriesFactory.getSourceCategory(SourceCategoryType.GRAILSAPP_CONF), result);
        addRoot(projectRoot, sourceCategoriesFactory.getSourceCategory(SourceCategoryType.GRAILSAPP_CONTROLLERS), result);
        addRoot(projectRoot, sourceCategoriesFactory.getSourceCategory(SourceCategoryType.GRAILSAPP_DOMAIN), result);
        addRoot(projectRoot, sourceCategoriesFactory.getSourceCategory(SourceCategoryType.GRAILSAPP_SERVICES), result);
        addRoot(projectRoot, sourceCategoriesFactory.getSourceCategory(SourceCategoryType.GRAILSAPP_TAGLIB), result);
        addRoot(projectRoot, sourceCategoriesFactory.getSourceCategory(SourceCategoryType.GRAILSAPP_UTILS), result);
        addRoot(projectRoot, sourceCategoriesFactory.getSourceCategory(SourceCategoryType.SCRIPTS), result);
        addRoot(projectRoot, sourceCategoriesFactory.getSourceCategory(SourceCategoryType.SRC_GROOVY), result);
        addRoot(projectRoot, sourceCategoriesFactory.getSourceCategory(SourceCategoryType.SRC_JAVA), result);
        addRoot(projectRoot, sourceCategoriesFactory.getSourceCategory(SourceCategoryType.SRC_GWT), result);
        addRoot(projectRoot, sourceCategoriesFactory.getSourceCategory(SourceCategoryType.TEST_INTEGRATION), result);
        addRoot(projectRoot, sourceCategoriesFactory.getSourceCategory(SourceCategoryType.TEST_UNIT), result);

        File pluginsDirFile = project == null ? null : project.getBuildConfig().getProjectPluginsDir();
        FileObject pluginsDir = pluginsDirFile == null ? null : FileUtil.toFileObject(
                FileUtil.normalizeFile(pluginsDirFile));
        File globalPluginsDirFile = project == null ? null : project.getBuildConfig().getGlobalPluginsDir();
        FileObject globalPluginsDir = globalPluginsDirFile == null ? null : FileUtil.toFileObject(
                FileUtil.normalizeFile(globalPluginsDirFile));

        for (FileObject child : projectRoot.getChildren()) {
            if (child.isFolder()
                    && VisibilityQuery.getDefault().isVisible(child)
                    && !GrailsSources.KNOWN_FOLDERS.contains(child.getName())
                    && child != pluginsDir
                    && child != globalPluginsDir) {
                result.add(child);
            }
        }

        addUnknownRoots(GrailsSources.KNOWN_FOLDERS_IN_GRAILS_APP, projectRoot, result, "grails-app");
        addUnknownRoots(GrailsSources.KNOWN_OR_IGNORED_FOLDERS_IN_TEST, projectRoot, result, "test");
    }

    private static void addUnknownRoots(Collection<String> alreadyKnown,
            FileObject projectRoot, List<FileObject> result, String relativePath) {

        FileObject folder = projectRoot.getFileObject(relativePath);
        if (folder != null) {
            for (FileObject child : folder.getChildren()) {
                if (child.isFolder()
                        && VisibilityQuery.getDefault().isVisible(child)
                        && !alreadyKnown.contains(child.getName())) {
                    result.add(child);
                }
            }
        }
    }

    private static void addRoot(FileObject projectRoot, SourceCategory category, List<FileObject> roots) {
        FileObject root = projectRoot.getFileObject(category.getRelativePath());
        if (root != null) {
            roots.add(root);
        }
    }

}
