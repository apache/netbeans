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

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.math.BigInteger;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.prefs.Preferences;
import javax.swing.SwingUtilities;
import org.netbeans.api.j2ee.core.Profile;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.project.classpath.ProjectClassPathModifier;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.api.project.libraries.Library;
import org.netbeans.api.project.libraries.LibraryManager;
import org.netbeans.api.queries.FileEncodingQuery;
import org.netbeans.modules.j2ee.common.ClasspathUtil;
import org.netbeans.modules.j2ee.common.dd.DDHelper;
import org.netbeans.modules.j2ee.common.ui.BrokenServerLibrarySupport;
import org.netbeans.modules.j2ee.dd.api.common.InitParam;
import org.netbeans.modules.j2ee.dd.api.web.*;
import org.netbeans.modules.j2ee.deployment.common.api.ConfigurationException;
import org.netbeans.modules.j2ee.deployment.devmodules.api.Deployment;
import org.netbeans.modules.j2ee.deployment.devmodules.api.InstanceRemovedException;
import org.netbeans.modules.j2ee.deployment.devmodules.spi.J2eeModuleProvider;
import org.netbeans.modules.j2ee.deployment.plugins.api.ServerLibrary;
import org.netbeans.modules.j2ee.deployment.plugins.api.ServerLibraryDependency;
import org.netbeans.modules.web.api.webmodule.ExtenderController;
import org.netbeans.modules.web.api.webmodule.WebModule;
import org.netbeans.modules.web.jsf.api.ConfigurationUtils;
import org.netbeans.modules.web.jsf.api.JsfComponentUtils;
import org.netbeans.modules.web.jsf.api.facesmodel.Application;
import org.netbeans.modules.web.jsf.api.facesmodel.FacesConfig;
import org.netbeans.modules.web.jsf.api.facesmodel.JSFConfigModel;
import org.netbeans.modules.web.jsf.api.facesmodel.JsfVersionUtils;
import org.netbeans.modules.web.jsf.api.facesmodel.ViewHandler;
import org.netbeans.modules.web.jsf.palette.JSFPaletteUtilities;
import org.netbeans.modules.web.jsf.spi.components.JsfComponentCustomizer;
import org.netbeans.modules.web.jsf.spi.components.JsfComponentImplementation;
import org.netbeans.modules.web.jsf.wizards.JSFConfigurationPanel;
import org.netbeans.modules.web.jsf.wizards.JSFConfigurationPanel.PreferredLanguage;
import org.netbeans.modules.web.jsfapi.api.JsfVersion;
import org.netbeans.modules.web.project.api.WebPropertyEvaluator;
import org.netbeans.modules.web.spi.webmodule.WebFrameworkProvider;
import org.netbeans.modules.web.spi.webmodule.WebModuleExtender;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.filesystems.FileLock;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileSystem;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
import org.openide.text.DataEditorSupport;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;

/**
 *
 * @author Petr Pisl, Po-Ting Wu, Alexey Butenko
 */
public class JSFFrameworkProvider extends WebFrameworkProvider {

    private static final Logger LOGGER = Logger.getLogger(JSFFrameworkProvider.class.getName());

    private static final String HANDLER = "com.sun.facelets.FaceletViewHandler";    //NOI18N

    private static final String J2EE_SERVER_INSTANCE = "j2ee.server.instance";  //NOI18N
    private static final String WELCOME_JSF = "welcomeJSF.jsp";   //NOI18N
    private static final String WELCOME_XHTML = "index.xhtml"; //NOI18N
    private static final String WELCOME_XHTML_TEMPLATE = "/Templates/JSP_Servlet/JSP.xhtml"; //NOI18N
    private static final String FORWARD_JSF = "forwardToJSF.jsp"; //NOI18N
    private static final String RESOURCE_FOLDER = "/org/netbeans/modules/web/jsf/resources/"; //NOI18N
    private static final String DEFAULT_MAPPING = "/faces/*";  //NOI18N

    private boolean createWelcome = true;

    public void setCreateWelcome(boolean set) {
        createWelcome = set;
    }

    private JSFConfigurationPanel panel;
    /** Creates a new instance of JSFFrameworkProvider */
    public JSFFrameworkProvider() {
        super(
                NbBundle.getMessage(JSFFrameworkProvider.class, "JSF_Name"),               // NOI18N
                NbBundle.getMessage(JSFFrameworkProvider.class, "JSF_Description"));       //NOI18N
    }

    // not named extend() so as to avoid implementing WebFrameworkProvider.extend()
    // better to move this to JSFConfigurationPanel
    public Set extendImpl(WebModule webModule, TreeMap<String, JsfComponentCustomizer> jsfComponentCustomizers) {
        Set<FileObject> result = new HashSet<>();
        Library jsfLibrary = null;
        Library jstlLibrary = null;

        JSFConfigurationPanel.LibraryType libraryType = panel.getLibraryType();
        if (libraryType == JSFConfigurationPanel.LibraryType.NEW) {
            // create new jsf library
            String libraryName = panel.getNewLibraryName();
            File installResource = panel.getInstallResource();
            if (installResource != null && libraryName != null) {
                try {
                    JSFUtils.createJSFUserLibrary(installResource, libraryName);
                    jsfLibrary = LibraryManager.getDefault().getLibrary(libraryName);
                } catch (IOException exception) {
                    LOGGER.log(Level.WARNING, "Exception during extending an web project", exception); //NOI18N
                }
            }
        } else {
            if (libraryType == JSFConfigurationPanel.LibraryType.USED) {
                //use a selected library
                jsfLibrary = panel.getLibrary();
                // if the selected library is a default one, add also JSTL library
                if (jsfLibrary.getName().equals(JSFUtils.DEFAULT_JSF_1_2_NAME)
                        || jsfLibrary.getName().equals(JSFUtils.DEFAULT_JSF_2_0_NAME)
                        || jsfLibrary.getName().equals(JSFUtils.DEFAULT_JSF_1_1_NAME)) {
                    jstlLibrary = LibraryManager.getDefault().getLibrary(JSFUtils.DEFAULT_JSTL_1_1_NAME);
                }
            }
        }

        try {
            FileObject fileObject = webModule.getDocumentBase();
            FileObject[] javaSources = webModule.getJavaSources();
            if (jsfLibrary != null  && javaSources.length > 0) {
                Library[] libraries;
                if (jstlLibrary != null) {
                    libraries = new Library[]{jsfLibrary, jstlLibrary};
                }
                else {
                    libraries = new Library[]{jsfLibrary};
                }
                // This is a way how to add libraries to the project classpath and
                // packed them to the war file by default.  classpath/compile_only (for scope provided)
                boolean modified = false;
                Boolean isMaven = (Boolean)panel.getController().getProperties().getProperty("maven");  //NOI18N
                if (isMaven!=null && isMaven) {
                    Project prj = FileOwnerQuery.getOwner(webModule.getDocumentBase());
                    J2eeModuleProvider provider = prj.getLookup().lookup(J2eeModuleProvider.class);
                    if (provider != null) {
                        String serverInstanceId = provider.getServerInstanceID();
                        if ( serverInstanceId == null || "".equals(serverInstanceId) || "DEV-NULL".equals(serverInstanceId)) {    //NOI18N
                            if (!panel.packageJars()) {
                                //Add to pom with scope provided
                                ProjectClassPathModifier.addLibraries(libraries, javaSources[0], "classpath/compile_only"); //NOI18N
                                modified = true;
                            }
                        }
                    }

                }
                if (!modified) {
                    ProjectClassPathModifier.addLibraries(libraries, javaSources[0], ClassPath.COMPILE);
                }
            }

            boolean isMyFaces = false;
            if (jsfLibrary != null) {
                // find out whether the added library is myfaces jsf implementation
                List<URL> content = jsfLibrary.getContent("classpath"); //NOI18N
                isMyFaces = ClasspathUtil.containsClass(content, JSFUtils.MYFACES_SPECIFIC_CLASS);
            } else {
                // find out whether the target server has myfaces jsf implementation on the classpath
                ClassPath cp = ClassPath.getClassPath(fileObject, ClassPath.COMPILE);
                if (cp != null) {
                    isMyFaces = cp.findResource(JSFUtils.MYFACES_SPECIFIC_CLASS.replace('.', '/') + ".class") != null; //NOI18N
                }
            }

            FileObject webInf = webModule.getWebInf();
            if (webInf == null) {
                webInf = FileUtil.createFolder(webModule.getDocumentBase(), "WEB-INF"); //NOI18N
            }
            assert webInf != null;

            // configure server library
            ServerLibrary serverLibrary = panel.getServerLibrary();
            if (serverLibrary != null) {
                String implementationTitle = serverLibrary.getImplementationTitle();
                isMyFaces = implementationTitle != null && implementationTitle.contains("MyFaces"); // NOI18N
                Project prj = FileOwnerQuery.getOwner(webInf);
                if (prj != null) {
                    String libraryName = serverLibrary.getName();
                    J2eeModuleProvider provider = prj.getLookup().lookup(J2eeModuleProvider.class);
                    if (provider != null && libraryName != null) {
                        provider.getConfigSupport().configureLibrary(
                                ServerLibraryDependency.minimalVersion(libraryName,
                                    serverLibrary.getSpecificationVersion(),
                                    serverLibrary.getImplementationVersion()));

                        Preferences prefs = ProjectUtils.getPreferences(prj, ProjectUtils.class, true);
                        prefs.put(BrokenServerLibrarySupport.OFFER_LIBRARY_DEPLOYMENT, Boolean.TRUE.toString());
                    }
                }
            }

            FileSystem fileSystem = webInf.getFileSystem();

            fileSystem.runAtomicAction(new CreateFacesConfig(webModule, isMyFaces));

            // extending for JSF component libraries
            StringBuilder jsfSuitesSB = new StringBuilder();
            for (JsfComponentImplementation jsfComponentDescriptor : panel.getEnabledJsfDescriptors()) {
                // extend webmodule about the JSF component library
                result.addAll(jsfComponentDescriptor.extend(
                        webModule, jsfComponentCustomizers.get(jsfComponentDescriptor.getName())));

                // track JSF suite for USG statistics
                jsfSuitesSB.append(jsfComponentDescriptor.getName()).append("|");
            }
            String statsString = jsfSuitesSB.toString();
            if (!statsString.isEmpty()) {
                statsString = statsString.substring(0, statsString.length() - 1);
                JSFUtils.logUsage(JSFFrameworkProvider.class, "USG_JSF_INCLUDED_SUITE", new Object[]{statsString});
            }

            FileObject welcomeFile = panel.isEnableFacelets() ? webModule.getDocumentBase().getFileObject(WELCOME_XHTML):
                                                                webModule.getDocumentBase().getFileObject(WELCOME_JSF);
            if (welcomeFile != null) {
                result.add(welcomeFile);
            }
        } catch (IOException | ConfigurationException exception) {
           LOGGER.log(Level.WARNING, "Exception during extending an web project", exception); //NOI18N
        }
        createWelcome = true;

        return result;
    }

    public static String readResource(InputStream is, String encoding) throws IOException {
        // read the config from resource first
        StringBuilder sbuffer = new StringBuilder();
        String lineSep = System.getProperty("line.separator");//NOI18N
        try (BufferedReader br = new BufferedReader(new InputStreamReader(is, encoding))) {
            String line = br.readLine();
            while (line != null) {
                sbuffer.append(line);
                sbuffer.append(lineSep);
                line = br.readLine();
            }
        }
        return sbuffer.toString();
    }

    @Override
    public java.io.File[] getConfigurationFiles(org.netbeans.modules.web.api.webmodule.WebModule wm) {
        // The JavaEE 5 introduce web modules without deployment descriptor. In such wm can not be jsf used.
        if (wm != null) {
            FileObject dd = wm.getDeploymentDescriptor();
            if (dd != null){
                FileObject[] filesFO = ConfigurationUtils.getFacesConfigFiles(wm);
                File[] files = new File[filesFO.length];
                for (int i = 0; i < filesFO.length; i++)
                    files[i] = FileUtil.toFile(filesFO[i]);
                if (files.length > 0)
                    return files;
            }
        }
        return null;
    }

    @Override
    public WebModuleExtender createWebModuleExtender(WebModule webModule, ExtenderController controller) {
        boolean isFrameworkAddition = (webModule == null || !isInWebModule(webModule));
        boolean isMaven = webModule == null ? false : JsfComponentUtils.isMavenBased(webModule);
        controller.getProperties().setProperty("maven", isMaven);
        if (webModule != null && webModule.getDocumentBase() != null) {
            FileObject docBase = webModule.getDocumentBase();
            Project project = FileOwnerQuery.getOwner(docBase);
            JsfPreferences preferences = JsfPreferences.forProject(project);
            if (preferences.getPreferredLanguage() == null) { //NOI18N
                ClassPath cp  = ClassPath.getClassPath(docBase, ClassPath.COMPILE);
                if (JSFUtils.isFaceletsPresent(cp)) {
                    preferences.setPreferredLanguage(PreferredLanguage.Facelets);    //NOI18N
                }
            }
            panel = new JSFConfigurationPanel(this, controller, isFrameworkAddition, preferences, webModule);
        } else {
            if (webModule != null && webModule.getDocumentBase() == null) {
                controller.getProperties().setProperty("NoDocBase", true);  //NOI18N
            }
            panel = new JSFConfigurationPanel(this, controller, isFrameworkAddition);
        }
        panel.setCreateExamples(createWelcome);
        if (!isFrameworkAddition && webModule != null) {
            // get configuration panel with values from the wm
            Servlet servlet = ConfigurationUtils.getFacesServlet(webModule);
            if (servlet != null) {
                panel.setServletName(servlet.getServletName());
            }
            String mapping = ConfigurationUtils.getFacesServletMapping(webModule);
            if (mapping == null) {
                mapping = DEFAULT_MAPPING;   //NOI18N
            }
            panel.setURLPattern(mapping);
            FileObject dd = webModule.getDeploymentDescriptor();
            panel.setValidateXML(JSFConfigUtilities.validateXML(dd));
            panel.setVerifyObjects(JSFConfigUtilities.verifyObjects(dd));

            //Facelets
            panel.setDebugFacelets(JSFUtils.debugFacelets(dd));
            panel.setSkipComments(JSFUtils.skipCommnets(dd));
        }

        return panel;
    }

    @Override
    public boolean isInWebModule(org.netbeans.modules.web.api.webmodule.WebModule webModule) {
        // The JavaEE 5 introduce web modules without deployment descriptor. In such wm can not be jsf used.
//        FileObject dd = webModule.getDeploymentDescriptor();
//        return (dd != null && ConfigurationUtils.getFacesServlet(webModule) != null);
        long time = System.currentTimeMillis();
        try {
            FileObject fo = webModule.getDocumentBase();
            if (fo != null) {
                return JSFConfigUtilities.hasJsfFramework(fo);
            }
            return false;
        } finally {
            LOGGER.log(Level.INFO, "Total time spent={0} ms", (System.currentTimeMillis() - time));
        }
    }

    @Override
    public String getServletPath(FileObject file){
        String url = null;
        if (file == null) return url;

        WebModule wm = WebModule.getWebModule(file);
        if (wm != null){
            url = FileUtil.getRelativePath(wm.getDocumentBase(), file);
            if (url == null) {
                return null;
            }
            if (url.charAt(0)!='/')
                url = "/" + url;
            String mapping = ConfigurationUtils.getFacesServletMapping(wm);
            if (mapping != null && !"".equals(mapping)){
                if (mapping.endsWith("/*")){
                    mapping = mapping.substring(0, mapping.length()-2);
                    url = mapping + url;
                }
            }
        }
        return url;
    }

    public static void createFile(FileObject target, String content, String encoding) throws IOException{
        try (FileLock lock = target.lock();
                BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(target.getOutputStream(lock), encoding))) {
            bw.write(content);
        }
    }

    private class  CreateFacesConfig implements FileSystem.AtomicAction{
        private static final String FACES_SERVLET_CLASS = "javax.faces.webapp.FacesServlet";  //NOI18N
        private static final String FACES_SERVLET_CLASS_JAKARTAEE = "jakarta.faces.webapp.FacesServlet";  //NOI18N
        private static final String FACES_SERVLET_NAME = "Faces Servlet";                     //NOI18N
        private static final String MYFACES_STARTUP_LISTENER_CLASS = "org.apache.myfaces.webapp.StartupServletContextListener";//NOI18N

        WebModule webModule;
        boolean isMyFaces;

        public CreateFacesConfig(WebModule webModule, boolean isMyFaces){
            this.webModule = webModule;
            this.isMyFaces = isMyFaces;
        }

        @Override
        public void run() throws IOException {
            // Enter servlet into the deployment descriptor
            FileObject dd = webModule.getDeploymentDescriptor();
            //we need deployment descriptor, create if null
            if(dd == null) {
                dd = DDHelper.createWebXml(webModule.getJ2eeProfile(), webModule.getWebInf());
            }
            //faces servlet mapping
            String facesMapping =  panel == null ? DEFAULT_MAPPING : panel.getURLPattern();//"/*";

            Library jsfLibrary = null;
            if (panel.getLibraryType() == JSFConfigurationPanel.LibraryType.USED) {
                jsfLibrary = panel.getLibrary();
            } else if (panel.getLibraryType() == JSFConfigurationPanel.LibraryType.NEW) {
                jsfLibrary = LibraryManager.getDefault().getLibrary(panel.getNewLibraryName());
            }

            JsfVersion jsfVersion;
            if (jsfLibrary != null) {
                List<URL> content = jsfLibrary.getContent("classpath"); //NOI18N
                jsfVersion = JsfVersionUtils.forClasspath(content);
            } else {
                if (panel.getLibraryType() == JSFConfigurationPanel.LibraryType.SERVER && panel.getServerLibrary() != null) {
                    jsfVersion = JsfVersionUtils.forServerLibrary(panel.getServerLibrary());
                } else {
                    jsfVersion = JsfVersionUtils.forWebModule(webModule);
                }
            }

            WebApp ddRoot = null;
            // issue #244100 - obviously the web project's profile can be unrecognized and the DD needn't still exist
            if (dd != null) {
                ddRoot = DDProvider.getDefault().getDDRoot(dd);
            }

            //Add Faces Servlet and servlet-mapping into web.xml
            if (ddRoot != null && ddRoot.getStatus() == WebApp.STATE_VALID) {
                boolean shouldAddMappings = shouldAddMappings(webModule);
                try{
                    if (shouldAddMappings || !DEFAULT_MAPPING.equals(facesMapping)) {
                        boolean servletDefined = false;
                        Servlet servlet;

                        if (ConfigurationUtils.getFacesServlet(webModule)!=null) {
                            servletDefined = true;
                        }

                        if (!servletDefined) {
                            servlet = (Servlet)ddRoot.createBean("Servlet"); //NOI18N
                            String servletName = (panel == null) ? FACES_SERVLET_NAME : panel.getServletName();
                            servlet.setServletName(servletName);
                            if (jsfVersion.isAtLeast(JsfVersion.JSF_3_0)) {
                                servlet.setServletClass(FACES_SERVLET_CLASS_JAKARTAEE);
                            } else {
                                servlet.setServletClass(FACES_SERVLET_CLASS);
                            }
                            servlet.setLoadOnStartup(new BigInteger("1"));//NOI18N
                            ddRoot.addServlet(servlet);

                            ServletMapping25 mapping = (ServletMapping25)ddRoot.createBean("ServletMapping"); //NOI18N
                            mapping.setServletName(servletName);//NOI18N
//                            facesMapping = panel == null ? "faces/*" : panel.getURLPattern();
                            mapping.setUrlPatterns(new String[]{facesMapping});
                            ddRoot.addServletMapping(mapping);
                        }
                    }
                    boolean faceletsEnabled = panel.isEnableFacelets();

                    if (jsfVersion != null && jsfVersion.isAtLeast(JsfVersion.JSF_2_0)) {
                        InitParam contextParam = (InitParam) ddRoot.createBean("InitParam");    //NOI18N
                        if (jsfVersion.isAtLeast(JsfVersion.JSF_3_0)) {
                            contextParam.setParamName(JSFUtils.FACES_PROJECT_STAGE_JAKARTAEE);
                        } else {
                            contextParam.setParamName(JSFUtils.FACES_PROJECT_STAGE);
                        }
                        contextParam.setParamValue("Development"); //NOI18N
                        ddRoot.addContextParam(contextParam);
                    }
                    if (isMyFaces) {
                        boolean listenerDefined = false;
                        Listener listeners[] = ddRoot.getListener();
                        for (int i = 0; i < listeners.length; i++) {
                            if (MYFACES_STARTUP_LISTENER_CLASS.equals(listeners[i].getListenerClass().trim())) {
                                listenerDefined = true;
                                break;
                            }
                        }
                        if (!listenerDefined) {
                            Listener facesListener = (Listener) ddRoot.createBean("Listener");  //NOI18N
                            facesListener.setListenerClass(MYFACES_STARTUP_LISTENER_CLASS);
                            ddRoot.addListener(facesListener);
                        }
                    }
                    // add welcome file
                    WelcomeFileList welcomeFiles = ddRoot.getSingleWelcomeFileList();
                    List<String> welcomeFileList = new ArrayList<String>();

                    // add the welcome file only if there isn't any
                    if (!faceletsEnabled) {
                        if (welcomeFiles == null) {
                            if (facesMapping.charAt(0) == '/') {
                                // if the mapping start with '/' (like /faces/*), then the welcome file can be the mapping
                                if (webModule.getDocumentBase().getFileObject(WELCOME_JSF) != null || createWelcome) {
                                    welcomeFileList.add(ConfigurationUtils.translateURI(facesMapping, WELCOME_JSF));
                                }
                            } else {
                                // if the mapping doesn't start '/' (like *.jsf), then the welcome file has to be
                                // a helper file, which will foward the request to the right url
                                welcomeFileList.add(FORWARD_JSF);
                                //copy forwardToJSF.jsp
                                if (facesMapping.charAt(0) != '/' && canCreateNewFile(webModule.getDocumentBase(), FORWARD_JSF)) { //NOI18N
                                    String content = readResource(getClass().getResourceAsStream(RESOURCE_FOLDER + FORWARD_JSF), "UTF-8"); //NOI18N
                                    content = content.replace("__FORWARD__", ConfigurationUtils.translateURI(facesMapping, WELCOME_JSF));
                                    Charset encoding = FileEncodingQuery.getDefaultEncoding();
                                    content = content.replace("__ENCODING__", encoding.name());
                                    FileObject target = FileUtil.createData(webModule.getDocumentBase(), FORWARD_JSF);//NOI18N
                                    createFile(target, content, encoding.name());  //NOI18N
                                    DataObject dob = DataObject.find(target);
                                    if (dob != null) {
                                        JSFPaletteUtilities.reformat(dob);
                                    }

                                }
                            }
                        }
                    } else {
                        // Add the welcome file into the list if no such list exist or if it doesn't contain it yet
                        String welcomeFileUri = ConfigurationUtils.translateURI(facesMapping, WELCOME_XHTML);
                        if (welcomeFiles == null || !Arrays.asList(welcomeFiles.getWelcomeFile()).contains(welcomeFileUri)) {
                            welcomeFileList.add(welcomeFileUri);
                        }
                    }
                    if (welcomeFiles != null) {
                        // Copy already existing entries
                        welcomeFileList.addAll(Arrays.asList(welcomeFiles.getWelcomeFile()));
                    }
                    if (!welcomeFileList.isEmpty()) {
                        // If not empty generate the welcome file list
                        welcomeFiles = (WelcomeFileList) ddRoot.createBean("WelcomeFileList"); //NOI18N
                        ddRoot.setWelcomeFileList(welcomeFiles);
                        for (String fileName : welcomeFileList) {
                            welcomeFiles.addWelcomeFile(fileName);
                        }
                    }
                    ddRoot.write(dd);

                } catch (ClassNotFoundException cnfe){
                    LOGGER.log(Level.WARNING, "Exception in JSFMoveClassPlugin", cnfe); //NOI18N
                }
            }  else {
                SwingUtilities.invokeLater(new Runnable() {

                    @Override
                    public void run() {
                        NotifyDescriptor warningDialog = new NotifyDescriptor.Message(
                            NbBundle.getMessage(JSFFrameworkProvider.class, "WARN_UnknownDeploymentDescriptorText"), //NOI18N
                            NotifyDescriptor.WARNING_MESSAGE);
                        DialogDisplayer.getDefault().notify(warningDialog);
                    }
                });
            }

            // copy faces-config.xml
            File fileConfig = new File(FileUtil.toFile(webModule.getWebInf()), "faces-config.xml"); // NOI18N
            boolean createFacesConfig = false;
            if (!fileConfig.exists()) {
                // Fix Issue#105180, new project wizard lets me select both jsf and visual jsf.
                // The new faces-config.xml template contains no elements;
                // it's better the framework don't replace user's original one if exist.
                String facesConfigTemplate = JSFCatalog.RES_FACES_CONFIG_DEFAULT;
                if (ddRoot != null) {
                    Profile profile = webModule.getJ2eeProfile();
                    if (profile != null && profile.isAtLeast(Profile.JAVA_EE_5) && jsfVersion != null) {
                        if (jsfVersion.isAtLeast(JsfVersion.JSF_4_1)) {
                            facesConfigTemplate = JSFCatalog.RES_FACES_CONFIG_4_1;
                        } else if (jsfVersion.isAtLeast(JsfVersion.JSF_4_0)) {
                            facesConfigTemplate = JSFCatalog.RES_FACES_CONFIG_4_0;
                        } else if (jsfVersion.isAtLeast(JsfVersion.JSF_3_0)) {
                            facesConfigTemplate = JSFCatalog.RES_FACES_CONFIG_3_0;
                        } else if (jsfVersion.isAtLeast(JsfVersion.JSF_2_3)) {
                            facesConfigTemplate = JSFCatalog.RES_FACES_CONFIG_2_3;
                        } else if (jsfVersion.isAtLeast(JsfVersion.JSF_2_2)) {
                            facesConfigTemplate = JSFCatalog.RES_FACES_CONFIG_2_2;
                        } else if (jsfVersion.isAtLeast(JsfVersion.JSF_2_1)) {
                            facesConfigTemplate = JSFCatalog.RES_FACES_CONFIG_2_1;
                        } else if (jsfVersion.isAtLeast(JsfVersion.JSF_2_0)) {
                            facesConfigTemplate = JSFCatalog.RES_FACES_CONFIG_2_0;
                        } else {
                            facesConfigTemplate = JSFCatalog.RES_FACES_CONFIG_1_2;
                        }
                    }
                    if (profile != null && !profile.isAtLeast(Profile.JAVA_EE_6_WEB)
                            && (jsfVersion == null || !jsfVersion.isAtLeast(JsfVersion.JSF_2_0))) {
                        createFacesConfig = true;
                    }
                }
                if (createFacesConfig) {
                    String content = readResource(getClass().getResourceAsStream(RESOURCE_FOLDER + facesConfigTemplate), "UTF-8"); //NOI18N
                    FileObject target = FileUtil.createData(webModule.getWebInf(), "faces-config.xml");//NOI18N
                    createFile(target, content, "UTF-8"); //NOI18N
                }
            }

            //If Facelets enabled need to add view-handler
            if (panel.isEnableFacelets()) {
                FileObject files[] = ConfigurationUtils.getFacesConfigFiles(webModule);
                if (files != null && files.length > 0) {
                    JSFConfigModel model = ConfigurationUtils.getConfigModel(files[0], true);
                    FacesConfig jsfConfig = model.getRootComponent();
                    if (jsfConfig != null){
                        Application application = null;
                        boolean newApplication = false;

                        List<Application> applications = jsfConfig.getApplications();
                        if (applications != null && applications.size() > 0){
                            List<ViewHandler> handlers = applications.get(0).getViewHandlers();
                            boolean alreadyDefined = false;
                            if (handlers != null){
                                for (ViewHandler viewHandler : handlers) {
                                    if (HANDLER.equals(viewHandler.getFullyQualifiedClassType().trim())){
                                        alreadyDefined = true;
                                        break;
                                    }
                                }
                            }
                            if (!alreadyDefined){
                                application = applications.get(0);
                            }
                        } else {
                            application = model.getFactory().createApplication();
                            newApplication = true;
                        }
                        if (application != null){
                            model.startTransaction();
                            if (newApplication) {
                                jsfConfig.addApplication(application);
                            }
                            //In JSF2.0 no need to add HANDLER need to change version of faces-config instead
                            if (jsfVersion != null && !jsfVersion.isAtLeast(JsfVersion.JSF_2_0) && !isMyFaces) {
                                ViewHandler viewHandler = model.getFactory().createViewHandler();
                                viewHandler.setFullyQualifiedClassType(HANDLER);
                                application.addViewHandler(viewHandler);
                            }
//                            // A component library may require a render kit
//                            if (isJSF20Plus && panel.getJsfComponentDescriptor() != null) {
//                                String drki = panel.getJsfComponentDescriptor().getDefaultRenderKitId();
//                                if (drki != null) {
//                                    List<DefaultRenderKitId> drkits = application.getDefaultRenderKitIds();
//                                    boolean alreadyDefined = false;
//                                    if (drkits != null){
//                                        for (DefaultRenderKitId drkit : drkits) {
//                                            if (drki.equals(drkit.getText().trim())){
//                                                alreadyDefined = true;
//                                                break;
//                                            }
//                                        }
//                                    }
//                                    if (!alreadyDefined){
//                                        DefaultRenderKitId newdrki = model.getFactory().createDefaultRenderKitId();
//                                        newdrki.setText(drki);
//                                        application.addDefaultRenderKitId(newdrki);
//                                    }
//                                }
//                            }
                            ClassPath cp = ClassPath.getClassPath(webModule.getDocumentBase(), ClassPath.COMPILE);
                            // FIXME icefaces on server
                            if (panel.getLibrary()!=null && panel.getLibrary().getName().indexOf("facelets-icefaces") != -1 //NOI18N
                                    && cp != null && cp.findResource("com/icesoft/faces/facelets/D2DFaceletViewHandler.class") != null){    //NOI18N
                                ViewHandler iceViewHandler = model.getFactory().createViewHandler();
                                iceViewHandler.setFullyQualifiedClassType("com.icesoft.faces.facelets.D2DFaceletViewHandler");  //NOI18N
                                application.addViewHandler(iceViewHandler);
                            }
                            try {
                                model.endTransaction();
                                model.sync();
                            } catch (IllegalStateException ex) {
                                IOException io = new IOException("Cannot update faces-config.xml", ex);
                                throw Exceptions.attachLocalizedMessage(io,
                                        NbBundle.getMessage(JSFFrameworkProvider.class, "ERR_WRITE_FACES_CONFIG", Exceptions.findLocalizedMessage(ex)));
                            }
                            DataEditorSupport editorSupport =
                                    DataObject.find(files[0]).getLookup().lookup(DataEditorSupport.class);
                            editorSupport.saveDocument();
                        }
                    }
                }

            }

            // generate the faces-config from template
            if (panel.isEnableFacelets() && panel.isCreateExamples()) {
                if (webModule.getDocumentBase().getFileObject(WELCOME_XHTML) == null) {
                    FileObject target = FileUtil.createData(webModule.getDocumentBase(), WELCOME_XHTML);
                    FileObject template = FileUtil.getConfigRoot().getFileObject(WELCOME_XHTML_TEMPLATE);
                    HashMap<String, Object> params = new HashMap<>();
                    if (jsfVersion != null) {
                        if (jsfVersion.isAtLeast(JsfVersion.JSF_4_1)) {
                            params.put("isJSF41", Boolean.TRUE);    //NOI18N
                        } if (jsfVersion.isAtLeast(JsfVersion.JSF_4_0)) {
                            params.put("isJSF40", Boolean.TRUE);    //NOI18N
                        } else if (jsfVersion.isAtLeast(JsfVersion.JSF_3_0)) {
                            params.put("isJSF30", Boolean.TRUE);    //NOI18N
                        } else if (jsfVersion.isAtLeast(JsfVersion.JSF_2_2)) {
                            params.put("isJSF22", Boolean.TRUE);    //NOI18N
                        } else if (jsfVersion.isAtLeast(JsfVersion.JSF_2_0)) {
                            params.put("isJSF20", Boolean.TRUE);    //NOI18N
                        }
                    }
                    JSFPaletteUtilities.expandJSFTemplate(template, params, target);
                }
            }

            //copy Welcome.jsp
            if (!panel.isEnableFacelets() && createWelcome && canCreateNewFile(webModule.getDocumentBase(), WELCOME_JSF)) {
                String content = readResource(getClass().getResourceAsStream(RESOURCE_FOLDER + WELCOME_JSF), "UTF-8"); //NOI18N
                Charset encoding = FileEncodingQuery.getDefaultEncoding();
                content = content.replace("__ENCODING__", encoding.name());
                FileObject target = FileUtil.createData(webModule.getDocumentBase(), WELCOME_JSF);
                createFile(target, content, encoding.name());
                DataObject dob = DataObject.find(target);
                if (dob != null) {
                    JSFPaletteUtilities.reformat(dob);
                }
            }
        }
        private boolean shouldAddMappings(WebModule webModule) {
            assert webModule != null;
            JsfVersion jsfVersion = JsfVersionUtils.forWebModule(webModule);
            FileObject projectFO = JSFUtils.getFileObject(webModule);
            if (jsfVersion != null && projectFO != null) {
                Project project = FileOwnerQuery.getOwner(projectFO);
                WebPropertyEvaluator evaluator = project.getLookup().lookup(WebPropertyEvaluator.class);
                if (evaluator != null) {
                    String serverInstanceID = evaluator.evaluator().getProperty(J2EE_SERVER_INSTANCE);
                    if (jsfVersion.isAtLeast(JsfVersion.JSF_2_0)
                            && isGlassFishv3(serverInstanceID)
                            && JSFConfigUtilities.hasJsfFramework(webModule.getDocumentBase())) {
                        return false;
                    }
                }
            }
            return true;
        }

        private boolean isGlassFishv3(String serverInstanceID) {
            if (serverInstanceID == null || "".equals(serverInstanceID)) {
                return false;
            }
            String shortName;
            try {
                shortName = Deployment.getDefault().getServerInstance(serverInstanceID).getServerID();
                if ("gfv800ee11".equals(shortName) || "gfv700ee10".equals(shortName) 
                        || "gfv610ee9".equals(shortName) || "gfv6ee9".equals(shortName) 
                        || "gfv510ee8".equals(shortName) || "gfv5ee8".equals(shortName) 
                        || "gfv5".equals(shortName) || "gfv4ee7".equals(shortName) 
                        || "gfv4".equals(shortName) || "gfv3ee6".equals(shortName) 
                        || "gfv3".equals(shortName)) {
                    return true;
                }
            } catch (InstanceRemovedException ex) {
                LOGGER.log(Level.INFO, "Server Instance was removed", ex); //NOI18N
            }
            return false;
        }

        private boolean canCreateNewFile(FileObject parent, String name){
            File fileToBe = new File(FileUtil.toFile(parent), name);
            boolean create = true;
            if (fileToBe.exists()){
                DialogDescriptor dialog = new DialogDescriptor(
                        NbBundle.getMessage(JSFFrameworkProvider.class, "MSG_OverwriteFile", fileToBe.getAbsolutePath()),
                        NbBundle.getMessage(JSFFrameworkProvider.class, "TTL_OverwriteFile"),
                        true, DialogDescriptor.YES_NO_OPTION, DialogDescriptor.NO_OPTION, null);
                java.awt.Dialog d = org.openide.DialogDisplayer.getDefault().createDialog(dialog);
                d.setVisible(true);
                create = (dialog.getValue() == org.openide.DialogDescriptor.NO_OPTION);
            }
            return create;
        }
    }
}
