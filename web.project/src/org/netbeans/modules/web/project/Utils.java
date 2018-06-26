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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
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

package org.netbeans.modules.web.project;

import java.awt.Color;
import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.List;
import java.util.ResourceBundle;
import java.util.Vector;

import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import javax.swing.*;
import org.netbeans.api.annotations.common.NonNull;

import org.netbeans.api.j2ee.core.Profile;
import org.openide.filesystems.FileUtil;

import org.netbeans.spi.project.support.ant.PropertyUtils;
import org.netbeans.spi.project.support.ant.AntProjectHelper;
import org.netbeans.spi.project.support.ant.EditableProperties;
import org.netbeans.api.java.platform.JavaPlatform;
import org.netbeans.api.java.platform.JavaPlatformManager;
import org.netbeans.api.project.libraries.Library;
import org.netbeans.api.project.libraries.LibraryChooser;
import org.netbeans.modules.java.api.common.classpath.ClassPathSupport;
import org.netbeans.modules.java.api.common.project.ProjectProperties;
import org.netbeans.modules.web.api.webmodule.WebModule;
import org.openide.filesystems.FileObject;
import org.openide.util.NbBundle;
import org.openide.util.Parameters;

public class Utils {

    private static final Logger UI_LOGGER = Logger.getLogger("org.netbeans.ui.web.project"); // NOI18N
    public static final String USG_LOGGER_NAME = "org.netbeans.ui.metrics.web.project"; // NOI18N
    private static final Logger USG_LOGGER = Logger.getLogger(USG_LOGGER_NAME); // NOI18N

    
    private static final String PLATFORM_ANT_NAME = "platform.ant.name"; //NOI18N
    public static final String SPECIFICATION_J2SE = "j2se";              //NOI18N

    /** Create a valid default for context path from project name.
     */
    public static String createDefaultContext(String projectName) {
        return "/" + PropertyUtils.getUsablePropertyName(projectName);
    }
    
    /**
     * Updates property file at given location of ant based project
     * @param h helper of the project
     * @param path a relative URI in the project directory
     * @param ep new or updated properties
     */
    public static void updateProperties(AntProjectHelper h, String path, EditableProperties ep) {
        EditableProperties properties = h.getProperties(path);
        properties.putAll(ep);
        h.putProperties(path, properties);
    }

    /**
     * Recursively checks whether the file lies underneath or equals the folder
     * @param folder the root of folders hierarchy to search in 
     * @param file the file to search for
     * @return <code>true</code>, if <code>file</code> lies somewhere underneath or equals the <code>folder</code>,
     * <code>false</code> otherwise
     */
    public static boolean isParentOrEqual(File folder, File file) {
        if(folder != null || file != null) {
            folder = FileUtil.normalizeFile(folder);
            file = FileUtil.normalizeFile(file);
            while(file != null) {
                if(file.equals(folder)) {
                    return true;
                }
                file = file.getParentFile();
            }
        }
        return false;
    }

    /**
     * Searches Java platform according to platform name
     * Specification of the platform has to be J2SE
     * @param platformName
     * @return related JavaPlatform object if found, otherwise null
     */
    public static JavaPlatform findJ2seJavaPlatform(String platformName) {
        return findJavaPlatform(platformName, SPECIFICATION_J2SE);
    }

    /**
     * Searches Java platform according to platform name
     * The platform sepecification does not need to be J2SE
     * @param platformName
     * @return related JavaPlatform object if found, otherwise null
     */
    public static JavaPlatform findJavaPlatform(String platformName) {
        return findJavaPlatform(platformName, null);
    }
    
    /**
     * Get the default value of the <tt>debug.classpath</tt> property.
     * @return the default value of the <tt>debug.classpath</tt> property.
     */
    public static String getDefaultDebugClassPath() {
        return "${" + ProjectProperties.BUILD_CLASSES_DIR + "}:${" + ProjectProperties.JAVAC_CLASSPATH + "}"; // NOI18N
    }
    
    /**
     * Correct given classpath, that means remove obsolete properties, add missing ones etc.
     * If the given parameter is <code>null</code> or empty, the default debug classpath is returned.
     * @return corrected classpath, never <code>null</code>.
     * @see #getDefaultClassPath()
     */
    public static String correctDebugClassPath(String debugClassPath) {

        if (debugClassPath == null || debugClassPath.length() == 0) {
            // should not happen
            return Utils.getDefaultDebugClassPath();
        }
        
        // "invalid" strings
        final String buildEarWebDir = "${build.ear.web.dir}"; // NOI18N
        final String buildEarClassesDir = "${build.ear.classes.dir}"; // NOI18N
        final String buildEarPrefix = "${build.ear."; // NOI18N

        if (!debugClassPath.contains(buildEarPrefix)) {
            return debugClassPath;
        }

        StringBuilder buffer = new StringBuilder(debugClassPath.length());
        for (String token : PropertyUtils.tokenizePath(debugClassPath)) {
            // check NB 5.5.x obsolete properties
            if (!buildEarWebDir.equals(token)
                    && !buildEarClassesDir.equals(token)) {
                if (buffer.length() > 0) {
                    buffer.append(":"); // NOI18N
                }
                buffer.append(token);
            }
        }

        return buffer.toString();
    }

    private static JavaPlatform findJavaPlatform(String platformName, String specFilter) {
        if(platformName != null) {
            JavaPlatform[] platforms = JavaPlatformManager.getDefault().getInstalledPlatforms();
            for(int i = 0; i < platforms.length; i++) {
                JavaPlatform platform = platforms[i];
                String antName = (String)platform.getProperties().get(PLATFORM_ANT_NAME);
                if (antName != null && antName.equals(platformName)) {
                    if(specFilter == null || specFilter.equalsIgnoreCase(platform.getSpecification().getName())) {
                        return platform;
                    }
                }
            }
        }
        return null;
    }

    /** Returns a slash-delimited resource path for the servlet generated from 
     * JSP, given a resource path of the original JSP.
     * Note: does not handle tag files yet, only JSP files.
     */
    static String getGeneratedJavaResource(String jspUri) {
        return getServletResourcePath(null, jspUri);
    }
    
    public static Color getErrorColor() {
        // inspired by org.openide.WizardDescriptor
        Color c = UIManager.getColor("nb.errorForeground"); //NOI18N
        return c == null ? new Color(89,79,191) : c;
    }
    
    /**
     * Logs the UI gesture.
     *
     * @param bundle resource bundle to use for message
     * @param message message key
     * @param params message parameters, may be <code>null</code>
     */
    public static void logUI(ResourceBundle bundle,String message, Object[] params) {
        Parameters.notNull("message", message);
        Parameters.notNull("bundle", bundle);

        LogRecord logRecord = new LogRecord(Level.INFO, message);
        logRecord.setLoggerName(UI_LOGGER.getName());
        logRecord.setResourceBundle(bundle);
        if (params != null) {
            logRecord.setParameters(params);
        }
        UI_LOGGER.log(logRecord);
    }

    /**
     * Logs usage data.
     *
     * @param srcClass source class
     * @param message message key
     * @param params message parameters, may be <code>null</code>
     */
    public static void logUsage(Class srcClass, String message, Object[] params) {
        Parameters.notNull("message", message); // NOI18N

        LogRecord logRecord = new LogRecord(Level.INFO, message);
        logRecord.setLoggerName(USG_LOGGER.getName());
        logRecord.setResourceBundle(NbBundle.getBundle(srcClass));
        logRecord.setResourceBundleName(srcClass.getPackage().getName() + ".Bundle"); // NOI18N
        if (params != null) {
            logRecord.setParameters(params);
        }
        USG_LOGGER.log(logRecord);
    }
    
    @NonNull
    public static String getServletName(FileObject docBase, FileObject jsp) {
        String jspRelativePath = FileUtil.getRelativePath(docBase, jsp);
        return getServletResourcePath(null, jspRelativePath);
    }
    
    @NonNull
    public static String getServletResourcePath(String moduleContextPath, String jspResourcePath) {
        return getServletPackageName(jspResourcePath).replace('.', '/') + '/' +
            getServletClassName(jspResourcePath) + ".java";
    }

    // After Apache code donation, should use org.apache.jasper utilities in
    // JspUtil and JspCompilationContext
    @NonNull
    private static String getServletPackageName(String jspUri) {
        String jspBasePackageName = "org/apache/jsp";//NOI18N
        int iSep = jspUri.lastIndexOf('/');
        String packageName = (iSep > 0) ? jspUri.substring(0, iSep) : "";//NOI18N
        if (packageName.length() == 0) {
            return jspBasePackageName;
        }
        return jspBasePackageName + "/" + packageName.substring(1);//NOI18N

    }

    // After Apache code donation, should use org.apache.jasper utilities in
    // JspUtil and JspCompilationContext
    @NonNull
    private static String getServletClassName(String jspUri) {
        int iSep = jspUri.lastIndexOf('/') + 1;
        String className = jspUri.substring(iSep);
        StringBuilder modClassName = new StringBuilder("");//NOI18N
        for (int i = 0; i < className.length(); i++) {
            char c = className.charAt(i);
            if (c == '.') {
                modClassName.append('_');
            } else {
                modClassName.append(c);
            }
        }
        return modClassName.toString();
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
        if (offset != null) {
            assert offset.endsWith("/");    //NOI18N
            url = new URL(url.toExternalForm() + offset); // NOI18N
        }
        return url;
    }
    
    public static LibraryChooser.Filter getFilter(WebProject p) {
        LibraryChooser.Filter filter = null;
        WebModule wm = WebModule.getWebModule(p.getProjectDirectory());
        if (wm != null && Profile.J2EE_13.equals(wm.getJ2eeProfile())) { // NOI18N
            filter = new LibraryChooser.Filter() {
                @Override
                public boolean accept(Library library) {
                    if ("javascript".equals(library.getType())) { //NOI18N
                        return false;
                    }
                    try {
                        library.getContent("classpath"); //NOI18N
                    } catch (IllegalArgumentException ex) {
                        return false;
                    }
                    return !library.getName().matches("jstl11|jaxrpc16|Spring|jaxws20|jaxb20|struts|jsf"); // NOI18N
                }
            };
        }
        return filter;
    }

    /**
     * Is this library contains classes folder instead of a jar?
     */
    public static boolean isLibraryDirectoryBased(ClassPathSupport.Item item) {
        assert item.getType() == ClassPathSupport.Item.TYPE_LIBRARY : item;
        Library l = item.getLibrary();
        if (l == null) {
            return false;
        }
        List<URL> cp = l.getContent("classpath"); // NOI18N
        if (cp.size() > 0 && cp.get(0).toString().startsWith("file:")) { // NOI18N
            return true;
        }
        return false;
    }
        
    
}
