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
package org.netbeans.modules.web.jsf.api.facesmodel;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.api.annotations.common.CheckForNull;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.api.project.Sources;
import org.netbeans.modules.j2ee.common.ClasspathUtil;
import org.netbeans.modules.j2ee.common.ProjectUtil;
import org.netbeans.modules.j2ee.deployment.common.api.Version;
import org.netbeans.modules.j2ee.deployment.plugins.api.ServerLibrary;
import org.netbeans.modules.web.api.webmodule.WebModule;
import org.netbeans.modules.web.jsf.JSFUtils;
import org.netbeans.modules.web.jsfapi.api.JsfVersion;
import org.netbeans.spi.java.classpath.ClassPathProvider;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Exceptions;
import org.openide.util.Parameters;
import org.openide.util.RequestProcessor;
import org.openide.util.WeakListeners;

/**
 *
 * @author Petr Pisl, ads, Martin Fousek
 */
public final class JsfVersionUtils {

    private static final LinkedHashMap<JsfVersion, String> SPECIFIC_CLASS_NAMES = new LinkedHashMap<>();

    static {
        SPECIFIC_CLASS_NAMES.put(JsfVersion.JSF_4_1, JSFUtils.JSF_4_1__API_SPECIFIC_CLASS);
        SPECIFIC_CLASS_NAMES.put(JsfVersion.JSF_4_0, JSFUtils.JSF_4_0__API_SPECIFIC_CLASS);
        SPECIFIC_CLASS_NAMES.put(JsfVersion.JSF_3_0, JSFUtils.JSF_3_0__API_SPECIFIC_CLASS);
        SPECIFIC_CLASS_NAMES.put(JsfVersion.JSF_2_3, JSFUtils.JSF_2_3__API_SPECIFIC_CLASS);
        SPECIFIC_CLASS_NAMES.put(JsfVersion.JSF_2_2, JSFUtils.JSF_2_2__API_SPECIFIC_CLASS);
        SPECIFIC_CLASS_NAMES.put(JsfVersion.JSF_2_1, JSFUtils.JSF_2_1__API_SPECIFIC_CLASS);
        SPECIFIC_CLASS_NAMES.put(JsfVersion.JSF_2_0, JSFUtils.JSF_2_0__API_SPECIFIC_CLASS);
        SPECIFIC_CLASS_NAMES.put(JsfVersion.JSF_1_2, JSFUtils.JSF_1_2__API_SPECIFIC_CLASS);
        SPECIFIC_CLASS_NAMES.put(JsfVersion.JSF_1_1, JSFUtils.FACES_EXCEPTION);
    }

    private static final Logger LOG = Logger.getLogger(JsfVersion.class.getName());

    // caches for holding JSF version and the project CP listeners
    private static final Map<WebModule, JsfVersion> projectVersionCache = new WeakHashMap<>();
    private static final Map<WebModule, PropertyChangeListener> projectListenerCache = new WeakHashMap<>();

    private JsfVersionUtils() {
    }

    /**
     * Gets the JSF version supported by the WebModule. It seeks for the JSF only on the classpath including the
     * platform classpath.
     *
     * @param webModule WebModule to seek for JSF version
     * @return JSF version if any found on the WebModule compile classpath, {@code null} otherwise
     */
    @CheckForNull
    public static synchronized JsfVersion forWebModule(@NonNull final WebModule webModule) {
        Parameters.notNull("webModule", webModule); //NOI18N
        JsfVersion version = projectVersionCache.get(webModule);
        if (version == null) {
            version = get(webModule, true);
            ClassPath compileCP = getCompileClasspath(webModule);
            if (compileCP == null) {
                return version;
            }
            PropertyChangeListener listener = WeakListeners.propertyChange(new PropertyChangeListener() {
                @Override
                public void propertyChange(PropertyChangeEvent evt) {
                    if (ClassPath.PROP_ROOTS.equals(evt.getPropertyName())) {
                        projectVersionCache.put(webModule, get(webModule, true));
                    }
                }
            }, compileCP);
            compileCP.addPropertyChangeListener(listener);
            projectListenerCache.put(webModule, listener);
            projectVersionCache.put(webModule, get(webModule, true));
        }
        return version;
    }

    /**
     * Gets the JSF version supported by the project. It seeks for the JSF only on the compile classpath.
     *
     * @param project project to seek for JSF version
     * @return JSF version if any found on the project compile classpath, {@code null} otherwise
     * @since 1.65
     */
    @CheckForNull
    public static synchronized JsfVersion forProject(@NonNull final Project project) {
        Parameters.notNull("project", project); //NOI18N
        WebModule webModule = WebModule.getWebModule(project.getProjectDirectory());
        if (webModule != null) {
            return forWebModule(webModule);
        } else {
            ClassPathProvider cpp = project.getLookup().lookup(ClassPathProvider.class);
            Sources sources = ProjectUtils.getSources(project);
            if (sources == null) {
                return null;
            }
            SourceGroup[] sourceGroups = sources.getSourceGroups("java"); //NOII18N
            if (sourceGroups.length > 0) {
                ClassPath compileClasspath = cpp.findClassPath(sourceGroups[0].getRootFolder(), ClassPath.COMPILE);
                List<URL> cpUrls = new ArrayList<>();
                for (ClassPath.Entry entry : compileClasspath.entries()) {
                    cpUrls.add(entry.getURL());
                }
                return forClasspath(cpUrls);
            } else {
                return null;
            }
        }
    }

    /**
     * Gets the highest JSF version found on the classpath. This method can
     * be slow and sholdn't be called within AWT EDT.
     *
     * @param classpath consists of jar files and folders containing classes
     * @return JSF version if any found on the classpath, {@code null} otherwise
     * @since 1.46
     */
    @CheckForNull
    public static synchronized JsfVersion forClasspath(@NonNull Collection<File> classpath) {
        Parameters.notNull("classpath", classpath); //NOI18N
        try {
            return ClasspathUtil.containsClass(classpath, SPECIFIC_CLASS_NAMES);
        } catch (IOException ex) {
            LOG.log(Level.INFO, null, ex);
        }
        return null;
    }

    /**
     * Gets the highest JSF version found on the classpath. This method can
     * be slow and sholdn't be called within AWT EDT.
     *
     * @param classpath consists of jar files and folders containing classes
     * @return JSF version if any found on the classpath, {@code null} otherwise
     * @since 1.46
     */
    @CheckForNull
    public static JsfVersion forClasspath(@NonNull List<URL> classpath) {
        Parameters.notNull("classpath", classpath); //NOI18N
        try {
            return ClasspathUtil.containsClass(classpath, SPECIFIC_CLASS_NAMES);
        } catch (IOException ex) {
            LOG.log(Level.INFO, null, ex);
        }
        return null;
    }

    /**
     * Gets the JSF version of the server library if any.
     *
     * @param lib server library to detect
     * @return JSF version if valid JSF server library, {@code null} otherwise
     * @since 1.46
     */
    @CheckForNull
    public static JsfVersion forServerLibrary(@NonNull ServerLibrary lib) {
        Parameters.notNull("serverLibrary", lib); //NOI18N
        if ("JavaServer Faces".equals(lib.getSpecificationTitle())) { // NOI18N
            if (Version.fromJsr277NotationWithFallback("4.1").equals(lib.getSpecificationVersion())) { //NOI18N
                return JsfVersion.JSF_4_1;
            } else if (Version.fromJsr277NotationWithFallback("4.0").equals(lib.getSpecificationVersion())) { //NOI18N
                return JsfVersion.JSF_4_0;
            } else if (Version.fromJsr277NotationWithFallback("3.0").equals(lib.getSpecificationVersion())) { //NOI18N
                return JsfVersion.JSF_3_0;
            } else if (Version.fromJsr277NotationWithFallback("2.3").equals(lib.getSpecificationVersion())) { //NOI18N
                return JsfVersion.JSF_2_3;
            } else if (Version.fromJsr277NotationWithFallback("2.2").equals(lib.getSpecificationVersion())) { //NOI18N
                return JsfVersion.JSF_2_2;
            } else if (Version.fromJsr277NotationWithFallback("2.1").equals(lib.getSpecificationVersion())) { //NOI18N
                return JsfVersion.JSF_2_1;
            } else if (Version.fromJsr277NotationWithFallback("2.0").equals(lib.getSpecificationVersion())) { // NOI18N
                return JsfVersion.JSF_2_0;
            } else if (Version.fromJsr277NotationWithFallback("1.2").equals(lib.getSpecificationVersion())) { // NOI18N
                return JsfVersion.JSF_1_2;
            } else if (Version.fromJsr277NotationWithFallback("1.1").equals(lib.getSpecificationVersion())) { // NOI18N
                return JsfVersion.JSF_1_1;
            } else {
                LOG.log(Level.INFO, "Unknown JSF version {0}", lib.getSpecificationVersion());
            }
        }
        return null;
    }

    /**
     * Gets version of the JSF on the project classpath. You can specify whether the classpath should include
     * platform's classpath too. If you don't need to exclude platform classpath use the
     * {@link #forWebModule(org.netbeans.modules.web.api.webmodule.WebModule)} which caches its results per project.
     *
     * @param webModule webModule
     * @param includingPlatformCP whether to include platform into the JSF version investigation of not
     * @return JSF version
     */
    @CheckForNull
    public static JsfVersion get(@NonNull WebModule webModule, boolean includingPlatformCP) {
        Parameters.notNull("webModule", webModule); //NOI18N
        if (webModule.getDocumentBase() == null) {
            return null;
        }

        ClassPath compileCP = ClassPath.getClassPath(webModule.getDocumentBase(), ClassPath.COMPILE);
        if (compileCP == null) {
            return null;
        }

        if (includingPlatformCP) {
            for (Map.Entry<JsfVersion, String> entry : SPECIFIC_CLASS_NAMES.entrySet()) {
                String className = entry.getValue();
                if (compileCP.findResource(className.replace('.', '/') + ".class") != null) { //NOI18N
                    return entry.getKey();
                }
            }
            return null;
        } else {
            Project project = FileOwnerQuery.getOwner(JSFUtils.getFileObject(webModule));
            if (project == null) {
                return null;
            }
            List<File> platformClasspath = Arrays.asList(ClasspathUtil.getJ2eePlatformClasspathEntries(project, ProjectUtil.getPlatform(project)));
            List<URL> projectDeps = new ArrayList<URL>();
            for (ClassPath.Entry entry : compileCP.entries()) {
                File archiveOrDir = FileUtil.archiveOrDirForURL(entry.getURL());
                if (archiveOrDir == null || !platformClasspath.contains(archiveOrDir)) {
                    projectDeps.add(entry.getURL());
                }
            }
            try {
                return ClasspathUtil.containsClass(projectDeps, SPECIFIC_CLASS_NAMES);
            } catch (IOException ex) {
                Exceptions.printStackTrace(ex);
            }
        }
        return null;
    }

    private static ClassPath getCompileClasspath(WebModule webModule) {
        FileObject projectFile = JSFUtils.getFileObject(webModule);
        if (projectFile == null) {
            return null;
        }

        Project project = FileOwnerQuery.getOwner(projectFile);
        if (project == null) {
            return null;
        }

        ClassPathProvider cpp = project.getLookup().lookup(ClassPathProvider.class);
        if (webModule.getDocumentBase() != null) {
            return cpp.findClassPath(webModule.getDocumentBase(), ClassPath.COMPILE);
        } else {
            Sources sources = ProjectUtils.getSources(project);
            if (sources == null) {
                return null;
            }

            SourceGroup[] sourceGroups = sources.getSourceGroups("java"); //NOII18N
            if (sourceGroups.length > 0) {
                return cpp.findClassPath(sourceGroups[0].getRootFolder(), ClassPath.COMPILE);
            }
        }
        return null;
    }
}
