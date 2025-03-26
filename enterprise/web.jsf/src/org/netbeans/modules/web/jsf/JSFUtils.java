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

package org.netbeans.modules.web.jsf;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.net.URL;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import org.netbeans.api.j2ee.core.Profile;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.libraries.LibraryManager;
import org.netbeans.modules.j2ee.common.ClasspathUtil;
import org.netbeans.modules.j2ee.common.ProjectUtil;
import org.netbeans.modules.j2ee.dd.api.common.InitParam;
import org.netbeans.modules.j2ee.dd.api.web.DDProvider;
import org.netbeans.modules.j2ee.dd.api.web.WebApp;
import org.netbeans.modules.j2ee.metadata.model.api.MetadataModel;
import org.netbeans.modules.web.api.webmodule.WebModule;
import org.netbeans.modules.web.jsf.api.ConfigurationUtils;
import org.netbeans.modules.web.jsf.api.facesmodel.JsfVersionUtils;
import org.netbeans.modules.web.jsf.api.metamodel.JsfModel;
import org.netbeans.modules.web.jsf.api.metamodel.JsfModelProvider;
import org.netbeans.modules.web.jsfapi.api.JsfVersion;
import org.netbeans.modules.web.jsfapi.api.NamespaceUtils;
import org.netbeans.spi.project.ui.templates.support.Templates;
import org.openide.ErrorManager;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.TemplateWizard;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;
import org.openide.util.Parameters;

/**
 *
 * @author Petr Pisl, Radko Najman, Martin Fousek
 */
public class JSFUtils {

    private static final Logger LOG = Logger.getLogger(JSFUtils.class.getName()); // NOI18N

    private static final String LIB_FOLDER = "lib";         //NOI18N

    // the names of bundled jsf libraries
    public static final String DEFAULT_JSF_1_1_NAME = "jsf1102";  //NOI18N
    public static final String DEFAULT_JSF_1_2_NAME = "jsf12";    //NOI18N
    public static final String DEFAULT_JSF_2_0_NAME = "jsf20";    //NOI18N
    public static final String DEFAULT_JSF_3_0_NAME = "jsf30";    //NOI18N
    public static final String DEFAULT_JSF_4_0_NAME = "jsf40";    //NOI18N
    public static final String DEFAULT_JSF_4_1_NAME = "jsf41";    //NOI18N

    // the name of jstl library
    public static final String DEFAULT_JSTL_1_1_NAME = "jstl11";  //NOI18N

    // fully qualified name of Java classes from the JavaEE API
    public static final String EJB_STATELESS = "javax.ejb.Stateless"; //NOI18N
    public static final String FACES_EXCEPTION = "javax.faces.FacesException"; //NOI18N
    public static final String JAKARTAEE_EJB_STATELESS = "jakarta.ejb.Stateless"; //NOI18N
    public static final String JAKARTAEE_FACES_EXCEPTION = "jakarta.faces.FacesException"; //NOI18N
    public static final String JSF_1_2__API_SPECIFIC_CLASS = "javax.faces.application.StateManagerWrapper"; //NOI18N
    public static final String JSF_2_0__API_SPECIFIC_CLASS = "javax.faces.application.ProjectStage"; //NOI18N
    public static final String JSF_2_1__API_SPECIFIC_CLASS = "javax.faces.component.TransientStateHelper"; //NOI18N
    public static final String JSF_2_2__API_SPECIFIC_CLASS = "javax.faces.flow.Flow"; //NOI18N
    public static final String JSF_2_3__API_SPECIFIC_CLASS = "javax.faces.push.PushContext"; //NOI18N
    public static final String JSF_3_0__API_SPECIFIC_CLASS = "jakarta.faces.push.PushContext"; //NOI18N
    public static final String JSF_4_0__API_SPECIFIC_CLASS = "jakarta.faces.lifecycle.ClientWindowScoped"; //NOI18N
    public static final String JSF_4_1__API_SPECIFIC_CLASS = "jakarta.faces.convert.UUIDConverter"; //NOI18N
    public static final String MYFACES_SPECIFIC_CLASS = "org.apache.myfaces.webapp.StartupServletContextListener"; //NOI18N

    //constants for web.xml (Java EE)
    protected static final String FACELETS_SKIPCOMMNETS = "javax.faces.FACELETS_SKIP_COMMENTS";
    protected static final String FACELETS_DEVELOPMENT = "facelets.DEVELOPMENT";
    protected static final String FACELETS_DEFAULT_SUFFIX = "javax.faces.DEFAULT_SUFFIX";
    public static final String FACES_PROJECT_STAGE = "javax.faces.PROJECT_STAGE";
    
    //constants for web.xml (Jakarta EE)
    protected static final String FACELETS_SKIPCOMMNETS_JAKARTAEE = "jakarta.faces.FACELETS_SKIP_COMMENTS";
    protected static final String FACELETS_DEFAULT_SUFFIX_JAKARTAEE = "jakarta.faces.DEFAULT_SUFFIX";
    public static final String FACES_PROJECT_STAGE_JAKARTAEE = "jakarta.faces.PROJECT_STAGE";

    // usages logger
    private static final Logger USG_LOGGER = Logger.getLogger("org.netbeans.ui.metrics.web.jsf"); // NOI18N

    /** This method finds out, whether the input file is a folder that contains
     * a jsf implementation or if file given if it contains required javax.faces.FacesException
     * class directly.
     *
     * @return null if the folder or file contains a JSF implemention or an error message
     */
    public static String isJSFLibraryResource(File resource) {
        String result = null;
        boolean isJSF = false;

        // path doesn't exist
        if (!resource.exists()) {
            result = NbBundle.getMessage(JSFUtils.class, "ERROR_IS_NOT_VALID_PATH", resource.getPath()); //NOI18N
        }

        if (resource.isDirectory()) {
            // Case of JSF version 2.1.2 and older - JSF library is created from packed directory
            File libFolder = new File(resource, LIB_FOLDER);
            if (libFolder.exists()) {
                File[] files = libFolder.listFiles(new FileFilter() {
                    @Override
                    public boolean accept(File pathname) {
                        boolean accepted = false;
                        if (pathname.getName().endsWith(".jar")) { //NOI18N
                            accepted = true;
                        }
                        return accepted;
                    }
                });
                try {
                    List<File> list = Arrays.asList(files);
                    isJSF = ClasspathUtil.containsClass(list, FACES_EXCEPTION);
                } catch (IOException exception) {
                    Exceptions.printStackTrace(exception);
                }
            } else {
                result = NbBundle.getMessage(JSFUtils.class, "ERROR_THERE_IS_NOT_LIB_FOLDER", resource.getPath()); //NOI18N
            }
        } else {
            // Case of JSF version 2.1.3+ - JSF library is delivered as a single JAR file
            try {
                isJSF = ClasspathUtil.containsClass(Collections.singletonList(resource), FACES_EXCEPTION);
            } catch (IOException ex) {
                Exceptions.printStackTrace(ex);
            }
        }

        // jsf wasn't found (in the directory or inside selected JAR file)
        if (!isJSF) {
            result = NbBundle.getMessage(JSFUtils.class, "ERROR_IS_NOT_JSF_API", resource.getPath()); //NOI18N
        }

        return result;
    }

    public static boolean createJSFUserLibrary(File resource, String libraryName) throws IOException {
        if (!resource.exists()) {
            return false;
        }

        List<URL> urls = new ArrayList<URL>();
        if (resource.isDirectory()) {
            // JSF version 2.1.2-
            // find all jars in the folder/lib
            File libFolder = new File(resource, LIB_FOLDER);
            if (libFolder.isDirectory()) {
                File[] jars = libFolder.listFiles(new FileFilter() {
                    @Override
                    public boolean accept(File pathname) {
                        return pathname.getName().endsWith(".jar"); //NOI18N
                    }
                });

                // obtain URLs of the jar file
                for (int i = 0; i < jars.length; i++) {
                    URL url = FileUtil.urlForArchiveOrDir(jars[i]);
                    if (url != null) {
                        urls.add(url);
                    }
                }
            }
        } else {
            // JSF version 2.1.3+
            urls.add(FileUtil.urlForArchiveOrDir(resource));
        }

        // create new library and regist in the Library Manager.
        Map<String, List<URL>> content = new HashMap<>();
        content.put("classpath", urls); //NOI18N
        LibraryManager.getDefault().createLibrary("j2se", libraryName, libraryName, libraryName, content); //NOI18N
        return true;
    }

    /** Find the value of the facelets.DEVELOPMENT context parameter in the deployment descriptor.
     */
    public static boolean debugFacelets(FileObject dd) {
        boolean value = false;  // the default value of the facelets.DEVELOPMENT
        if (dd != null){
            try{
                WebApp webApp = DDProvider.getDefault().getDDRoot(dd);
                InitParam param = null;
                if (webApp != null)
                    param = (InitParam)webApp.findBeanByName("InitParam", "ParamName", "facelets.DEVELOPMENT"); //NOI18N
                if (param != null)
                    value =   "true".equals(param.getParamValue().trim()); //NOI18N
            } catch (java.io.IOException e) {
                ErrorManager.getDefault().notify(e);
            }
        }
        return value;
    }

    /** Find the value of the facelets.SKIP_COMMENTS context parameter in the deployment descriptor.
     */
    public static boolean skipCommnets(FileObject dd) {
        boolean value = false;  // the default value of the facelets.SKIP_COMMENTS
        if (dd != null){
            try{
                WebApp webApp = DDProvider.getDefault().getDDRoot(dd);
                InitParam param = null;
                if (webApp != null)
                    param = (InitParam)webApp.findBeanByName("InitParam", "ParamName", "facelets.SKIP_COMMENTS"); //NOI18N
                if (param != null)
                    value =   "true".equals(param.getParamValue().trim()); //NOI18N
            } catch (java.io.IOException e) {
                ErrorManager.getDefault().notify(e);
            }
        }
        return value;
    }
    /**
     * Returns relative path from one file to another file.
     */
    public static String getRelativePath (FileObject fromFO, FileObject toFO) {
        StringBuilder path = new StringBuilder("./"); //NOI18N
        FileObject parent = fromFO.getParent();
        String tmpPath = null;
        while (parent != null && (tmpPath = FileUtil.getRelativePath(parent, toFO)) == null) {
            parent = parent.getParent();
            path.append("../"); //NOI18N
        }

        return (tmpPath != null ? (path.append(tmpPath)).toString() : null);
    }

    /**
     * Use {@link JsfVersionUtils#get(org.netbeans.modules.web.api.webmodule.WebModule, boolean) } instead.
     */
    @Deprecated
    public static boolean isJSF12Plus(WebModule webModule, boolean includingPlatformCP) {
        return isJSFPlus(webModule, includingPlatformCP, JSF_1_2__API_SPECIFIC_CLASS);
    }

    /**
     * Use {@link JsfVersionUtils#get(org.netbeans.modules.web.api.webmodule.WebModule, boolean) } instead.
     */
    @Deprecated
    public static boolean isJSF20Plus(WebModule webModule, boolean includingPlatformCP) {
        return isJSFPlus(webModule, includingPlatformCP, JSF_2_0__API_SPECIFIC_CLASS);
    }

    /**
     * Use {@link JsfVersionUtils#get(org.netbeans.modules.web.api.webmodule.WebModule, boolean) } instead.
     */
    @Deprecated
    public static boolean isJSF21Plus(WebModule webModule, boolean includingPlatformCP) {
        return isJSFPlus(webModule, includingPlatformCP, JSF_2_1__API_SPECIFIC_CLASS);
    }

    /**
     * Use {@link JsfVersionUtils#get(org.netbeans.modules.web.api.webmodule.WebModule, boolean) } instead.
     */
    @Deprecated
    public static boolean isJSF22Plus(WebModule webModule, boolean includingPlatformCP) {
        return isJSFPlus(webModule, includingPlatformCP, JSF_2_2__API_SPECIFIC_CLASS);
    }

    /**
     * Use {@link JsfVersionUtils#get(org.netbeans.modules.web.api.webmodule.WebModule, boolean) } instead.
     */
    @Deprecated
    private static boolean isJSFPlus(WebModule webModule, boolean includingPlatformCP, String versionSpecificClass) {
        if (webModule == null) {
            return false;
        }

        final ClassPath compileCP = ClassPath.getClassPath(webModule.getDocumentBase(), ClassPath.COMPILE);
        if (compileCP == null) {
            return false;
        }

        if (includingPlatformCP) {
            return compileCP.findResource(versionSpecificClass.replace('.', '/') + ".class") != null; //NOI18N
        } else {
            Project project = FileOwnerQuery.getOwner(getFileObject(webModule));
            if (project == null) {
                return false;
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
                return ClasspathUtil.containsClass(projectDeps, versionSpecificClass); //NOI18N
            } catch (IOException ex) {
                Exceptions.printStackTrace(ex);
            }
        }
        return false;
    }

    public static boolean isJavaEE5(TemplateWizard wizard) {
        Project project = Templates.getProject(wizard);
        WebModule wm = WebModule.getWebModule(project.getProjectDirectory());
        if (wm != null) {
            Profile profile = wm.getJ2eeProfile();
            return (profile == Profile.JAVA_EE_5);
        }
        return false;
    }
    
    public static boolean isJakartaEE9Plus(TemplateWizard wizard) {
        Project project = Templates.getProject(wizard);
        WebModule wm = WebModule.getWebModule(project.getProjectDirectory());
        if (wm != null) {
            Profile profile = wm.getJ2eeProfile();
            return profile.isAtLeast(Profile.JAKARTA_EE_9_WEB);
        }
        return false;
    }

     /**
     * Logs usage statistics data.
     *
     * @param srcClass source class
     * @param message USG message key
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

    /**
     * Gets any fileObject inside the given web module.
     *
     * @param module web module to be scanned
     * @return fileObject if any found, {@code null} otherwise
     */
    public static FileObject getFileObject(WebModule module) {
        FileObject fileObject = module.getDocumentBase();
        if (fileObject != null) {
            return fileObject;
        }
        fileObject = module.getDeploymentDescriptor();
        if (fileObject != null) {
            return fileObject;
        }
        fileObject = module.getWebInf();
        if (fileObject != null) {
            return fileObject;
        }

        FileObject[] facesConfigFiles = ConfigurationUtils.getFacesConfigFiles(module);
        if (facesConfigFiles != null && facesConfigFiles.length > 0) {
            return facesConfigFiles[0];
        }

        FileObject[] fileObjects = module.getJavaSources();
        if (fileObjects != null) {
            for (FileObject source : fileObjects) {
                if (source != null) {
                    return source;
                }
            }
        }
        return null;
    }

    /**
     * Whether the given classpath contains support for Facelets.
     *
     * @param cp examined classpath
     * @return {@code true} if the facelets classes are present on the classpath, {@code false} otherwise
     */
    public static boolean isFaceletsPresent(ClassPath cp) {
        if (cp == null) {
            return false;
        }
        return cp.findResource(JSFUtils.MYFACES_SPECIFIC_CLASS.replace('.', '/') + ".class") != null || //NOI18N
                cp.findResource("com/sun/facelets/Facelet.class") != null || //NOI18N
                cp.findResource("com/sun/faces/facelets/Facelet.class") != null || // NOI18N
                cp.findResource("javax/faces/view/facelets/FaceletContext.class") != null || //NOI18N
                cp.findResource("jakarta/faces/view/facelets/FaceletContext.class") != null; //NOI18N
    }

    /**
     * Gets JSF metaModel of the Project.
     * @param project project
     * @return model if found, {@code null} otherwise
     */
    public static MetadataModel<JsfModel> getModel(Project project) {
        JsfModelProvider modelProvider = project.getLookup().lookup(JsfModelProvider.class);
        if (modelProvider == null) {
            return null;
        }
        return modelProvider.getModel();
    }

    /**
     * Gets domain name of the namespace according to the JSF version included on the web module classpath.
     * @param webModule web module, can be null
     * @return {@link NamespaceUtils#SUN_COM_LOCATION} if JSF not found or JSF version is lower than 2.2,
     *         {@link NamespaceUtils#JCP_ORG_LOCATION} otherwise
     */
    public static String getNamespaceDomain(WebModule webModule) {
        JsfVersion version = webModule != null ? JsfVersionUtils.forWebModule(webModule) : null;
        String nsLocation = NamespaceUtils.SUN_COM_LOCATION;
        if (version != null && version.isAtLeast(JsfVersion.JSF_4_0)) {
            nsLocation = NamespaceUtils.JAKARTA_ORG_LOCATION;
        } else if (version != null && version.isAtLeast(JsfVersion.JSF_2_2)) {
            nsLocation = NamespaceUtils.JCP_ORG_LOCATION;
        }
        return nsLocation;
    }
}
