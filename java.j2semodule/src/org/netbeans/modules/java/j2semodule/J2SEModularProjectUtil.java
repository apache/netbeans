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
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
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
 */

package org.netbeans.modules.java.j2semodule;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Map;
import java.util.Map.Entry;
import java.util.logging.Logger;
import org.netbeans.api.annotations.common.CheckForNull;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.project.Project;
import org.netbeans.modules.java.api.common.SourceRoots;
import org.netbeans.modules.java.api.common.project.ProjectProperties;
import org.netbeans.spi.project.support.ant.GeneratedFilesHelper;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.modules.SpecificationVersion;
import org.openide.util.Parameters;

/**
 * Miscellaneous utilities for the J2SE modular projects.
 * @author  Jiri Rechtacek
 */
public class J2SEModularProjectUtil {

    public static final SpecificationVersion MIN_SOURCE_LEVEL = new SpecificationVersion("9");  //NOI18N

    private static final String MODULE_INFO_JAVA = "module-info.java"; //NOI18N
    private static final String[] BREAKABLE_PROPERTIES = {
        ProjectProperties.JAVAC_CLASSPATH,
        ProjectProperties.RUN_CLASSPATH,
        ProjectProperties.DEBUG_CLASSPATH,
        ProjectProperties.JAVAC_TEST_CLASSPATH,
        ProjectProperties.RUN_TEST_CLASSPATH,
        ProjectProperties.DEBUG_TEST_CLASSPATH,

        ProjectProperties.ENDORSED_CLASSPATH,

        ProjectProperties.JAVAC_MODULEPATH,
        ProjectProperties.RUN_MODULEPATH,
        ProjectProperties.DEBUG_MODULEPATH,
        ProjectProperties.JAVAC_TEST_MODULEPATH,
        ProjectProperties.RUN_TEST_MODULEPATH,
        ProjectProperties.DEBUG_TEST_MODULEPATH
    };

    private static final Logger LOG = Logger.getLogger(J2SEModularProjectUtil.class.getName());

    private J2SEModularProjectUtil () {}
    
    /**
     * Returns the property value evaluated by J2SEModularProject's PropertyEvaluator.
     *
     * @param p project
     * @param value of property
     * @return evaluated value of given property or null if the property not set or
     * if the project doesn't provide AntProjectHelper
     */    
    public static Object getEvaluatedProperty(Project p, String value) {
        if (value == null) {
            return null;
        }
        J2SEModularProject j2seprj = p.getLookup().lookup(J2SEModularProject.class);
        if (j2seprj != null) {
            return j2seprj.evaluator().evaluate(value);
        } else {
            return null;
        }
    }
    
    
    /**
     * Creates an URL of a classpath or sourcepath root
     * For the existing directory it returns the URL obtained from {@link File#toUri()}
     * For archive file it returns an URL of the root of the archive file
     * For non existing directory it fixes the ending '/'
     * @param root the file of a root
     * @param offset a path relative to the root file or null (eg. src/ for jar:file:///lib.jar!/src/)" 
     * @return an URL of the root
     * @throws MalformedURLException if the URL cannot be created
     */
    public static URL getRootURL (File root, String offset) throws MalformedURLException {
        URL url = FileUtil.urlForArchiveOrDir(root);
        if (url == null) {
            throw new IllegalArgumentException(root.getAbsolutePath());
        }
        if (offset != null) {
            assert offset.endsWith("/");    //NOI18N
            url = new URL(url.toExternalForm() + offset); // NOI18N
        }
        return url;
    }
    
    
    public static String getBuildXmlName (final J2SEModularProject project) {
        assert project != null;
        String buildScriptPath = project.evaluator().getProperty(ProjectProperties.BUILD_SCRIPT);
        if (buildScriptPath == null) {
            buildScriptPath = GeneratedFilesHelper.BUILD_XML_PATH;
        }
        return buildScriptPath;
    }
    
    public static FileObject getBuildXml (final J2SEModularProject project) {
        return project.getProjectDirectory().getFileObject (getBuildXmlName(project));
    }

    public static boolean isCompileOnSaveSupported(final J2SEModularProject project) {
        Parameters.notNull("project", project);
        final Map<String,String> props = project.evaluator().getProperties();
        if (props == null) {
            LOG.warning("PropertyEvaluator mapping could not be computed (e.g. due to a circular definition)");  //NOI18N
        }
        else {
            for (Entry<String, String> e : props.entrySet()) {
                if (e.getKey().startsWith(ProjectProperties.COMPILE_ON_SAVE_UNSUPPORTED_PREFIX)) {
                    if (e.getValue() != null && Boolean.valueOf(e.getValue())) {
                        return false;
                    }
                }
            }                    
        }
        return true;
    }

    public static boolean isCompileOnSaveEnabled(final J2SEModularProject project) {
        String compileOnSaveProperty = project.evaluator().getProperty(ProjectProperties.COMPILE_ON_SAVE);

        return (compileOnSaveProperty != null && Boolean.valueOf(compileOnSaveProperty)) && J2SEModularProjectUtil.isCompileOnSaveSupported(project);
    }

    /**
     * Returns true when value is ant true|on|yes
     * @param value to be checked
     * @return boolean
     */
    public static boolean isTrue(final String param) {
        return "true".equalsIgnoreCase(param) ||    //NOI18N
               "yes".equalsIgnoreCase(param) ||     //NOI18N
               "on".equalsIgnoreCase(param);        //NOI18N
    }

    /**
     * Creates reference to property.
     * @param propertyName the name of property
     * @param lastEntry if true, the path separator is not added
     * @return the reference
     */
    public static String ref(
            @NonNull final String propertyName,
            final boolean lastEntry) {
        return String.format(
                "${%s}%s",  //NOI18N
                propertyName,
                lastEntry ? "" : ":");  //NOI18N
    }

    /**
     * Returns a list of property names which need to be tested for broken references.
     * @param project to return property names for
     * @return the list of breakable properties
     */
    public static String[] getBreakableProperties(
            @NonNull final SourceRoots sourceRoots,
            @NonNull final SourceRoots testRoot) {
        final String[] srcRootProps = sourceRoots.getRootProperties();
        final String[] testRootProps = testRoot.getRootProperties();
        final String[] result = new String [BREAKABLE_PROPERTIES.length + srcRootProps.length + testRootProps.length];
        System.arraycopy(BREAKABLE_PROPERTIES, 0, result, 0, BREAKABLE_PROPERTIES.length);
        System.arraycopy(srcRootProps, 0, result, BREAKABLE_PROPERTIES.length, srcRootProps.length);
        System.arraycopy(testRootProps, 0, result, BREAKABLE_PROPERTIES.length + srcRootProps.length, testRootProps.length);
        return result;
    }

    public static boolean hasModuleInfo(@NonNull final SourceRoots roots) {
        ClassPath scp = null;
        for (FileObject root : roots.getRoots()) {
            FileObject mInfo = J2SEModularProjectUtil.getModuleInfo(root);
            if (mInfo != null) {
                if (scp == null) {
                    scp = ClassPath.getClassPath(root, ClassPath.SOURCE);
                }
                if (scp.contains(mInfo)) {
                    return true;
                }
            }
        }
        return false;
    }

    @NonNull
    public static File getModuleInfo(@NonNull final File root) {
        return new File(root, MODULE_INFO_JAVA);
    }

    @CheckForNull
    public static FileObject getModuleInfo(@NonNull final FileObject root) {
        return root.getFileObject(MODULE_INFO_JAVA);
    }
}
