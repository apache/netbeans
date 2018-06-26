/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2015 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2015 Sun Microsystems, Inc.
 */
package org.netbeans.modules.javascript.nodejs.util;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.netbeans.api.annotations.common.CheckForNull;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.modules.javascript.nodejs.exec.NodeExecutable;
import org.netbeans.modules.javascript.nodejs.exec.NpmExecutable;
import org.netbeans.modules.javascript.nodejs.file.PackageJson;
import org.netbeans.modules.javascript.nodejs.options.NodeJsOptions;
import org.netbeans.modules.javascript.nodejs.platform.NodeJsSupport;
import org.netbeans.modules.javascript.nodejs.preferences.NodeJsPreferences;
import org.netbeans.modules.web.clientproject.api.WebClientProjectConstants;
import org.netbeans.modules.web.common.api.UsageLogger;
import org.netbeans.modules.web.common.api.Version;
import org.netbeans.spi.project.ui.support.ProjectConvertors;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
import org.openide.modules.Places;
import org.openide.util.Lookup;
import org.openide.util.Pair;
import org.openide.util.Utilities;

public final class NodeJsUtils {

    public static final String START_FILE_NODE_PREFIX = "node "; // NOI18N

    private static final String NODEJS_DIR_NAME = "nodejs"; // NOI18N
    private static final String USAGE_LOGGER_NAME = "org.netbeans.ui.metrics.javascript.nodejs"; // NOI18N
    private static final UsageLogger NPM_INSTALL_USAGE_LOGGER = new UsageLogger.Builder(USAGE_LOGGER_NAME)
            .message(NodeJsUtils.class, "USG_NPM_INSTALL") // NOI18N
            .create();
    private static final UsageLogger NPM_RUN_SCRIPT_USAGE_LOGGER = new UsageLogger.Builder(USAGE_LOGGER_NAME)
            .message(NodeJsUtils.class, "USG_NPM_RUN_SCRIPT") // NOI18N
            .firstMessageOnly(false)
            .create();
    private static final UsageLogger NPM_LIBRARY_USAGE_LOGGER = new UsageLogger.Builder(USAGE_LOGGER_NAME)
            .message(NodeJsUtils.class, "USG_NPM_LIBRARY") // NOI18N
            .firstMessageOnly(false)
            .create();


    private NodeJsUtils() {
    }

    public static void logUsageNpmInstall() {
        NPM_INSTALL_USAGE_LOGGER.log();
    }

    public static void logUsageNpmRunScript(String script) {
        NPM_RUN_SCRIPT_USAGE_LOGGER.log(script);
    }

    public static void logUsageNpmLibrary(String type, String name, String version) {
        NPM_LIBRARY_USAGE_LOGGER.log(type, name, version);
    }

    public static String getProjectDisplayName(Project project) {
        return ProjectUtils.getInformation(project).getDisplayName();
    }

    @CheckForNull
    public static String getNode() {
        if (GraalVmUtils.isRunningOn()) {
            return GraalVmUtils.getNode();
        }
        List<String> files = FileUtils.findFileOnUsersPath(NodeExecutable.NODE_NAMES);
        if (!files.isEmpty()) {
            return files.get(0);
        }
        return null;
    }

    @CheckForNull
    public static String getNpm() {
        if (GraalVmUtils.isRunningOn()) {
            return GraalVmUtils.getNpm(true);
        }
        List<String> files = FileUtils.findFileOnUsersPath(NpmExecutable.NPM_NAME);
        if (!files.isEmpty()) {
            return files.get(0);
        }
        return null;
    }

    public static boolean isJsLibrary(Project project) {
        return getSiteRoots(project).isEmpty();
    }

    @CheckForNull
    public static File getSourceRoot(Project project) {
        for (File root : getSourceRoots(project)) {
            return root;
        }
        return null;
    }

    @CheckForNull
    public static File getSiteRoot(Project project) {
        for (File root : getSiteRoots(project)) {
            return root;
        }
        return null;
    }

    @CheckForNull
    public static File getTestRoot(Project project) {
        for (File root : getTestRoots(project)) {
            return root;
        }
        return null;
    }

    public static List<File> getSourceRoots(Project project) {
        return getRoots(project, WebClientProjectConstants.SOURCES_TYPE_HTML5);
    }

    public static List<File> getSiteRoots(Project project) {
        return getRoots(project, WebClientProjectConstants.SOURCES_TYPE_HTML5_SITE_ROOT);
    }

    public static List<File> getTestRoots(Project project) {
        return getRoots(project, WebClientProjectConstants.SOURCES_TYPE_HTML5_TEST);
    }

    public static List<File> getRoots(Project project, String type) {
        SourceGroup[] sourceGroups = ProjectUtils.getSources(project).getSourceGroups(type);
        List<File> roots = new ArrayList<>(sourceGroups.length);
        for (SourceGroup sourceGroup : sourceGroups) {
            FileObject rootFolder = sourceGroup.getRootFolder();
            File root = FileUtil.toFile(rootFolder);
            assert root != null : rootFolder;
            roots.add(root);
        }
        return roots;
    }

    public static Pair<String, String> parseStartFile(String line) {
        assert line != null;
        String data = line.trim();
        if (data.startsWith(START_FILE_NODE_PREFIX)) {
            data = data.substring(START_FILE_NODE_PREFIX.length());
        }
        String[] params = Utilities.parseParameters(data);
        String startFile = null;
        StringBuilder startArgsBuilder = new StringBuilder();
        for (String param : params) {
            if (startFile == null) {
                if (param.startsWith("-")) { // NOI18N
                    // node param
                    continue;
                }
                startFile = param;
            } else {
                // args
                if (startArgsBuilder.length() > 0) {
                    startArgsBuilder.append(" "); // NOI18N
                }
                startArgsBuilder.append(param);
            }
        }
        return Pair.of(startFile, startArgsBuilder.toString());
    }

    @CheckForNull
    public static PackageJson getPackageJson(Lookup context) {
        return getProjectAndPackageJson(context).second();
    }

    @CheckForNull
    public static Project getPackageJsonProject(Lookup context) {
        return getProjectAndPackageJson(context).first();
    }

    public static Pair<Project, PackageJson> getProjectAndPackageJson(Lookup context) {
        Project project = context.lookup(Project.class);
        PackageJson packageJson = null;
        if (project != null) {
            // project action
            packageJson = new PackageJson(project.getProjectDirectory());
        } else {
            // package.json directly
            FileObject file = context.lookup(FileObject.class);
            if (file == null) {
                DataObject dataObject = context.lookup(DataObject.class);
                if (dataObject != null) {
                    file = dataObject.getPrimaryFile();
                }
            }
            if (file != null) {
                packageJson = new PackageJson(file.getParent());
                project = FileOwnerQuery.getOwner(file);
            }
        }
        if (project == null) {
            return Pair.of(null, null);
        }
        if (packageJson == null) {
            return Pair.of(null, null);
        }
        if (!packageJson.exists()) {
            return Pair.of(null, null);
        }
        assert project != null;
        assert packageJson != null;
        return Pair.of(project, packageJson);
    }

    @CheckForNull
    public static File getNodeSources(Project project) {
        NodeJsPreferences preferences = NodeJsSupport.forProject(project).getPreferences();
        if (preferences.isDefaultNode()) {
            // default node
            String nodeSources = NodeJsOptions.getInstance().getNodeSources();
            if (nodeSources != null) {
                return new File(nodeSources);
            }
            NodeExecutable node = NodeExecutable.getDefault(project, false);
            if (node == null) {
                return null;
            }
            Version version = node.getVersion();
            if (version == null) {
                return null;
            }
            return getNodeSources(version);
        }
        // custom node
        String nodeSources = preferences.getNodeSources();
        if (nodeSources != null) {
            return new File(nodeSources);
        }
        NodeExecutable node = NodeExecutable.forProject(project, false);
        if (node == null) {
            return null;
        }
        Version version = node.getVersion();
        if (version == null) {
            return null;
        }
        return getNodeSources(version);
    }

    public static File getNodeSources() {
        return Places.getCacheSubdirectory(NODEJS_DIR_NAME);
    }

    public static boolean hasNodeSources(Version version) {
        assert version != null;
        return getNodeSources(version).isDirectory();
    }

    public static boolean hasNodeSources(String version) {
        assert version != null;
        return getNodeSources(version).isDirectory();
    }

    static File getNodeSources(Version version) {
        assert version != null;
        return getNodeSources(version.toString());
    }

    private static File getNodeSources(String version) {
        assert version != null;
        return new File(getNodeSources(), version);
    }

    /**
     * Checks whether the given folder is already a project.
     * <p>
     * This method ignores ProjectConvertor projects.
     * @param folder folder to be checked
     * @return {@code true} if the given folder is already a project, {@code false} otherwise
     */
    public static boolean isProject(File folder) {
        Project prj = null;
        boolean foundButBroken = false;
        try {
            prj = ProjectManager.getDefault().findProject(FileUtil.toFileObject(FileUtil.normalizeFile(folder)));
        } catch (IOException ex) {
            foundButBroken = true;
        } catch (IllegalArgumentException ex) {
            // noop
        }
        if (prj != null
                && !ProjectConvertors.isConvertorProject(prj)) {
            return true;
        }
        return foundButBroken;
    }

}
