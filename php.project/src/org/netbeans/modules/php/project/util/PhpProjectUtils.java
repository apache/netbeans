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

package org.netbeans.modules.php.project.util;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.api.project.Sources;
import org.netbeans.modules.php.api.PhpConstants;
import org.netbeans.modules.php.api.util.StringUtils;
import org.netbeans.modules.php.project.PhpProject;
import org.netbeans.modules.php.project.PhpVisibilityQuery;
import org.netbeans.modules.php.project.classpath.CommonPhpSourcePath;
import org.netbeans.modules.php.project.ui.actions.support.CommandUtils;
import org.netbeans.modules.php.project.ui.customizer.CompositePanelProviderImpl;
import org.netbeans.modules.php.project.ui.customizer.CustomizerProviderImpl;
import org.netbeans.modules.php.spi.framework.PhpFrameworkProvider;
import org.netbeans.spi.project.ui.support.ProjectConvertors;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.nodes.Node;
import org.openide.util.NbBundle;
import org.openide.util.Pair;

/**
 * Utility methods.
 * @author Tomas Mysik
 */
@SuppressWarnings("ClassWithMultipleLoggers")
public final class PhpProjectUtils {

    public static final String USAGE_LOGGER_NAME = "org.netbeans.ui.metrics.php"; // NOI18N

    private static final Logger LOGGER = Logger.getLogger(PhpProjectUtils.class.getName());
    private static final Logger USG_LOGGER = Logger.getLogger(USAGE_LOGGER_NAME);

    private PhpProjectUtils() {
    }

    /**
     * Get a PHP project for the given node.
     * @return a PHP project or <code>null</code>.
     */
    public static PhpProject getPhpProject(Node node) {
        return getPhpProject(CommandUtils.getFileObject(node));
    }

    /**
     * Get a PHP project for the given FileObject.
     * @return a PHP project or <code>null</code>.
     */
    public static PhpProject getPhpProject(FileObject fo) {
        assert fo != null;

        Project project = FileOwnerQuery.getOwner(fo);
        if (project == null) {
            return null;
        }
        return project.getLookup().lookup(PhpProject.class);
    }

    // #137230, #165918
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

    public static SourceGroup[] getSourceGroups(Project phpProject) {
        Sources sources = ProjectUtils.getSources(phpProject);
        return sources.getSourceGroups(PhpConstants.SOURCES_TYPE_PHP);
    }

    public static FileObject[] getSourceObjects(Project phpProject) {
        SourceGroup[] groups = getSourceGroups(phpProject);

        FileObject[] fileObjects = new FileObject[groups.length];
        for (int i = 0; i < groups.length; i++) {
            fileObjects[i] = groups[i].getRootFolder();
        }
        return fileObjects;
    }

    /**
     * "Deep" check whether file is visible or not. It is a work around for #172571.
     * @param phpVisibilityQuery PHP visibility query
     * @param fileObject file object to check
     * @return <code>true</code> if file object is visible, <code>false</code> otherwise
     */
    public static boolean isVisible(PhpVisibilityQuery phpVisibilityQuery, FileObject fileObject) {
        assert phpVisibilityQuery != null;
        assert fileObject != null;

        FileObject fo = fileObject;
        while (fo != null) {
            if (!phpVisibilityQuery.isVisible(fo)) {
                return false;
            }
            fo = fo.getParent();
        }
        return true;
    }

    /**
     * Resolve enum from the given {@code value}. If the enum cannot be resolved,
     * the {@code defaultValue} is returned.
     * @param <T> enum type
     * @param enumClass enum class
     * @param value value to be resolved, can be {@code null}
     * @param defaultValue default value, can be {@code null}
     * @return enum from the given {@code value} or the {@code defaultValue} if enum cannot be resolved
     */
    public static <T extends Enum<T>> T resolveEnum(Class<T> enumClass, String value, T defaultValue) {
        if (value == null) {
            return defaultValue;
        }
        try {
            return Enum.valueOf(enumClass, value);
        } catch (Exception exc) {
            return defaultValue;
        }
    }

    /**
     * Resolve local file.
     * @param parentDir parent directory
     * @param relativeFilePath relative path ("/" expected as a separator), can be {@link StringUtils#hasText(String) empty}
     * @return resolved file
     */
    public static File resolveFile(File parentDir, String relativeFilePath) {
        if (parentDir == null) {
            throw new NullPointerException("Parameter 'parentDir' must be set");
        }
        if (StringUtils.hasText(relativeFilePath)) {
            return new File(parentDir, relativeFilePath.replace('/', File.separatorChar)); // NOI18N
        }
        return parentDir;
    }

    /**
     * Open project customizer, Run Configuration category.
     */
    public static void openCustomizerRun(Project project) {
        openCustomizer(project, CompositePanelProviderImpl.RUN);
    }

    /**
     * Open project customizer.
     */
    public static void openCustomizer(Project project, String category) {
        project.getLookup().lookup(CustomizerProviderImpl.class).showCustomizer(category, null);
    }

    /**
     * Get number intervals for the given numbers.
     * <p>
     * For example, for numbers [2, 1, 3, 102, 5, 77, 103, 4], these intervals are returned:
     * [[1, 5], [77, 77], [102, 103]].
     * @param numbers numbers to get number intervals for
     * @return number intervals for the given numbers, never {@code null}
     */
    public static List<Pair<Integer, Integer>> getIntervals(List<Integer> numbers) {
        if (numbers.isEmpty()) {
            return Collections.emptyList();
        }
        if (numbers.size() == 1) {
            Integer number = numbers.get(0);
            return Collections.singletonList(Pair.of(number, number));
        }
        Collections.sort(numbers);
        int start = -1;
        int end = -1;
        int current;
        List<Pair<Integer, Integer>> intervals = new ArrayList<>();
        for (Integer index : numbers) {
            current = index;
            if (start == -1) {
                start = index;
            }
            if (end == -1) {
                end = index;
            } else if (current - end == 1) {
                end = current;
            } else {
                intervals.add(Pair.of(start, end));
                start = current;
                end = current;
            }
        }
        intervals.add(Pair.of(start, end));
        return intervals;
    }

    public static boolean isInternalFile(FileObject file) {
        for (FileObject dir : CommonPhpSourcePath.getInternalPath()) {
            if (dir.equals(file)
                    || FileUtil.isParentOf(dir, file)) {
                return true;
            }
        }
        return false;
    }

    // http://wiki.netbeans.org/UsageLoggingSpecification
    /**
     * Logs usage data.
     *
     * @param srcClass source class
     * @param message message key
     * @param params message parameters, may be <code>null</code>
     */
    public static void logUsage(Class<?> srcClass, String message, List<? extends Object> params) {
        assert message != null;

        LogRecord logRecord = new LogRecord(Level.INFO, message);
        logRecord.setLoggerName(USG_LOGGER.getName());
        logRecord.setResourceBundle(NbBundle.getBundle(srcClass));
        logRecord.setResourceBundleName(srcClass.getPackage().getName() + ".Bundle"); // NOI18N
        if (params != null) {
            logRecord.setParameters(params.toArray(new Object[params.size()]));
        }
        USG_LOGGER.log(logRecord);
    }

    public static String getFrameworksForUsage(Collection<PhpFrameworkProvider> frameworks) {
        assert frameworks != null;
        StringBuilder buffer = new StringBuilder(200);
        for (PhpFrameworkProvider provider : frameworks) {
            if (buffer.length() > 0) {
                buffer.append("|"); // NOI18N
            }
            buffer.append(provider.getIdentifier());
        }
        return buffer.toString();
    }
}
