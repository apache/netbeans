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
/*
 * Contributor(s): Craig MacKay
 */

package org.netbeans.modules.spring.webmvc;

import java.io.File;
import java.io.IOException;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.project.classpath.ProjectClassPathModifier;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.api.project.libraries.Library;
import org.netbeans.modules.j2ee.common.dd.DDHelper;
import org.netbeans.modules.j2ee.core.api.support.SourceGroups;
import org.netbeans.modules.j2ee.dd.api.common.CommonDDBean;
import org.netbeans.modules.j2ee.dd.api.common.CreateCapability;
import org.netbeans.modules.j2ee.dd.api.common.InitParam;
import org.netbeans.modules.j2ee.dd.api.web.DDProvider;
import org.netbeans.modules.j2ee.dd.api.web.Listener;
import org.netbeans.modules.j2ee.dd.api.web.Servlet;
import org.netbeans.modules.j2ee.dd.api.web.ServletMapping;
import org.netbeans.modules.j2ee.dd.api.web.ServletMapping25;
import org.netbeans.modules.j2ee.dd.api.web.WebApp;
import org.netbeans.modules.j2ee.dd.api.web.WelcomeFileList;
import org.netbeans.modules.spring.api.SpringUtilities;
import org.netbeans.modules.spring.api.beans.ConfigFileGroup;
import org.netbeans.modules.spring.api.beans.ConfigFileManager;
import org.netbeans.modules.spring.api.beans.SpringScope;
import org.netbeans.modules.spring.webmvc.utils.SpringWebFrameworkUtils;
import org.netbeans.modules.web.api.webmodule.ExtenderController;
import org.netbeans.modules.web.api.webmodule.WebModule;
import org.netbeans.modules.web.spi.webmodule.WebModuleExtender;
import org.openide.DialogDisplayer;
import org.openide.ErrorManager;
import org.openide.NotifyDescriptor.Message;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileSystem;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataFolder;
import org.openide.loaders.DataObject;
import org.openide.util.ChangeSupport;
import org.openide.util.Exceptions;
import org.openide.util.HelpCtx;
import org.openide.util.Mutex.ExceptionAction;
import org.openide.util.MutexException;
import org.openide.util.NbBundle;

/**
 * The WebModuleExtender implementation for Spring Web MVC.
 *
 * @author Craig MacKay et al.
 */

public class SpringWebModuleExtender extends WebModuleExtender implements ChangeListener {
    private static final Logger LOGGER = Logger.getLogger(SpringWebModuleExtender.class.getName());

    private final ChangeSupport changeSupport = new ChangeSupport(this);
    private final SpringWebFrameworkProvider framework;
    private final ExtenderController controller;
    private final boolean customizer;
    private final WebModule webModule;
    private SpringConfigPanelVisual component;
    private String dispatcherName = "dispatcher"; // NOI18N
    private String dispatcherMapping = "*.htm"; // NOI18N
    private boolean includeJstl = true;

    /**
     * Creates a new instance of SpringWebModuleExtender
     * @param framework
     * @param controller an instance of org.netbeans.modules.web.api.webmodule.ExtenderController
     * @param webModule the web module to extend, or {@code null} for new projects.
     * @param customizer
     */
    public SpringWebModuleExtender(SpringWebFrameworkProvider framework, ExtenderController controller, WebModule webModule, boolean customizer) {
        this.framework = framework;
        this.controller = controller;
        this.webModule = webModule;
        this.customizer = customizer;
    }

    public ExtenderController getController() {
        return controller;
    }

    public String getDispatcherName() {
        return dispatcherName;
    }

    public String getDispatcherMapping() {
        return dispatcherMapping;
    }

    public boolean getIncludeJstl() {
        return includeJstl;
    }

    @Override
    public synchronized SpringConfigPanelVisual getComponent() {
        if (component == null) {
            component = new SpringConfigPanelVisual(this);
            component.setEnabled(!customizer);
        }
        return component;
    }

    @Override
    public boolean isValid() {
        if (dispatcherName == null || dispatcherName.trim().length() == 0){
            controller.setErrorMessage(NbBundle.getMessage(SpringConfigPanelVisual.class, "MSG_DispatcherNameIsEmpty")); // NOI18N
            return false;
        }

        if (!SpringWebFrameworkUtils.isDispatcherServletConfigFilenameValid(dispatcherName)){
            controller.setErrorMessage(NbBundle.getMessage(SpringConfigPanelVisual.class, "MSG_DispatcherServletConfigFilenameIsNotValid"));
            return false;
        }

        if (dispatcherMapping == null || dispatcherMapping.trim().length() == 0) {
            controller.setErrorMessage(NbBundle.getMessage(SpringConfigPanelVisual.class, "MSG_DispatcherMappingPatternIsEmpty")); // NOI18N
            return false;
        }
        if (!SpringWebFrameworkUtils.isDispatcherMappingPatternValid(dispatcherMapping)){
            controller.setErrorMessage(NbBundle.getMessage(SpringConfigPanelVisual.class, "MSG_DispatcherMappingPatternIsNotValid")); // NOI18N
            return false;
        }
        if (webModule != null && !isWebXmlValid(webModule)) {
            controller.setErrorMessage(NbBundle.getMessage(SpringConfigPanelVisual.class, "MSG_WebXmlIsNotValid")); // NOI18N
            return false;
        }
        if (getComponent().getSpringLibrary() == null) {
            controller.setErrorMessage(NbBundle.getMessage(SpringConfigPanelVisual.class, "MSG_NoValidSpringLibraryFound")); // NOI18N
            return false;
        }

        controller.setErrorMessage(null);
        return true;
    }

    @Override
    public HelpCtx getHelp() {
        return new HelpCtx(SpringWebModuleExtender.class);
    }

    @Override
    public final void addChangeListener(ChangeListener l) {
        changeSupport.addChangeListener(l);
    }

    @Override
    public final void removeChangeListener(ChangeListener l) {
        changeSupport.removeChangeListener(l);
    }

    @Override
    public void stateChanged(ChangeEvent e) {
        dispatcherName = getComponent().getDispatcherName();
        dispatcherMapping = getComponent().getDispatcherMapping();
        includeJstl = getComponent().getIncludeJstl();
        changeSupport.fireChange();
    }

    @Override
    public void update() {
    // not used yet
    }

    @Override
    public Set<FileObject> extend(WebModule webModule) {
        CreateSpringConfig createSpringConfig = new CreateSpringConfig(webModule);
        FileObject webInf = webModule.getWebInf();
        if (webInf == null) {
            try {
                FileObject documentBase = webModule.getDocumentBase();
                if (documentBase == null) {
                    LOGGER.log(Level.INFO, "WebModule does not have valid documentBase");
                    return Collections.<FileObject>emptySet();
                }
                webInf = FileUtil.createFolder(documentBase, "WEB-INF"); //NOI18N
            } catch (IOException ex) {
                LOGGER.log(Level.WARNING, "Exception during creating WEB-INF directory", ex);
            }
        }
        if (webInf != null) {
            try {
                FileSystem fs = webInf.getFileSystem();
                fs.runAtomicAction(createSpringConfig);
            } catch (IOException e) {
                Logger.getLogger("global").log(Level.INFO, null, e);
                return Collections.<FileObject>emptySet();
            }
        }
        return createSpringConfig.getFilesToOpen();
    }

    private boolean isWebXmlValid(WebModule webModule) {
        FileObject webXml = webModule.getDeploymentDescriptor();
        if (webXml == null) {
            return true;
        }
        WebApp webApp = null;
        try {
            webApp = DDProvider.getDefault().getDDRoot(webXml);
        } catch (IOException ex) {
            LOGGER.log(Level.INFO, "Can't read web.xml file: " + webXml.getPath(), ex);
        }
        return webApp != null && webApp.getStatus() == WebApp.STATE_VALID;
    }

    private class CreateSpringConfig implements FileSystem.AtomicAction {

        public static final String CONTEXT_LOADER = "org.springframework.web.context.ContextLoaderListener"; // NOI18N
        public static final String DISPATCHER_SERVLET = "org.springframework.web.servlet.DispatcherServlet"; // NOI18N
        public static final String ENCODING = "UTF-8"; // NOI18N
        
        private final Set<FileObject> filesToOpen = new LinkedHashSet<>();
        private final WebModule webModule;

        public CreateSpringConfig(WebModule webModule) {
            this.webModule = webModule;
        }

        @NbBundle.Messages({
            "CreateSpringConfig.msg.invalid.dd=Deployment descriptor cointains errors, Spring framework has to be manually configured there!"
        })
        @Override
        public void run() throws IOException {
            // MODIFY WEB.XML
            FileObject dd = webModule.getDeploymentDescriptor();
            //we need deployment descriptor, create if null
            if(dd==null)
            {
                dd = DDHelper.createWebXml(webModule.getJ2eeProfile(), webModule.getWebInf());
            }
            if (dd != null) {
                WebApp ddRoot = DDProvider.getDefault().getDDRoot(dd);
                if (ddRoot.getError() != null) {
                    Message message = new Message(Bundle.CreateSpringConfig_msg_invalid_dd(), Message.ERROR_MESSAGE);
                    DialogDisplayer.getDefault().notifyLater(message);
                } else {
                    addContextParam(ddRoot, "contextConfigLocation", "/WEB-INF/applicationContext.xml"); // NOI18N
                    addListener(ddRoot, CONTEXT_LOADER);
                    addServlet(ddRoot, getComponent().getDispatcherName(), DISPATCHER_SERVLET, getComponent().getDispatcherMapping(), "2"); // NOI18N
                    WelcomeFileList welcomeFiles = ddRoot.getSingleWelcomeFileList();
                    if (welcomeFiles == null) {
                        try {
                            welcomeFiles = (WelcomeFileList) ddRoot.createBean("WelcomeFileList"); // NOI18N
                            ddRoot.setWelcomeFileList(welcomeFiles);
                        } catch (ClassNotFoundException ex) {
                            Exceptions.printStackTrace(ex);
                        }
                    }
                    if (welcomeFiles.sizeWelcomeFile() == 0) {
                        welcomeFiles.addWelcomeFile("redirect.jsp"); // NOI18N
                    }
                    ddRoot.write(dd);
                }
            }
            
            // ADD JSTL LIBRARY IF ENABLED AND SPRING LIBRARY
            List<Library> libraries = new ArrayList<>(3);
            Library springLibrary = component.getSpringLibrary();
            String version = component.getSpringLibraryVersion();
            Library webMVCLibrary = null;
            if (springLibrary != null) {
                libraries.add(springLibrary);
                if (SpringUtilities.isSpringWebMVCLibrary(springLibrary)) {
                    webMVCLibrary = springLibrary;
                }
            } else {
                LOGGER.log(Level.WARNING, null, new Error("No Spring Framework library found."));
            }
            if (webMVCLibrary == null) {
                webMVCLibrary = SpringUtilities.findSpringWebMVCLibrary(version);
                if (webMVCLibrary !=null) {
                    libraries.add(webMVCLibrary);
                } else {
                    LOGGER.log(Level.WARNING, null, new Error("No Spring Web MVC library with version "+version+" found."));
                }
            }
//            Library webMVCLibrary = SpringUtilities.findSpringWebMVCLibrary();
//            Library springLibrary = null;
//            if (webMVCLibrary != null) {
//                libraries.add(webMVCLibrary);
//                if (SpringUtilities.isSpringLibrary(webMVCLibrary)) {
//                    // In case this is an user library with a monolithic Spring.
//                    springLibrary = webMVCLibrary;
//                }
//            } else {
//                LOGGER.log(Level.WARNING, null, new Error("No Spring Web MVC library found."));
//            }
//            if (springLibrary == null) {
//                springLibrary = SpringUtilities.findSpringLibrary();
//                if (springLibrary != null){
//                    libraries.add(springLibrary);
//                } else {
//                    LOGGER.log(Level.WARNING, null, new Error("No Spring Framework library found."));
//                }
//            }
            if (includeJstl) {
                Library jstlLibrary = SpringUtilities.findJSTLibrary();
                if (jstlLibrary != null) {
                    libraries.add(jstlLibrary);
                } else {
                    LOGGER.log(Level.WARNING, null, new Error("No JSTL library found."));
                }
            }
            if (!libraries.isEmpty()) {
                addLibrariesToWebModule(libraries, webModule);
            }

            // CREATE WEB-INF/JSP FOLDER
            FileObject webInf = webModule.getWebInf();

            FileObject jsp = FileUtil.createFolder(webInf, "jsp");
            // COPY TEMPLATE SPRING RESOURCES (JSP, XML, PROPERTIES)
            DataFolder webInfDO = DataFolder.findFolder(webInf);
            final List<File> newConfigFiles = new ArrayList<>(2);
            HashMap<String, Object> params = new HashMap<>();
            
            String appContextTemplateName = "applicationContext-4.xml"; //NOI18N
            String dispServletTemplateName = "dispatcher-servlet-4.xml"; //NOI18N
            if (version.startsWith("3.")) {    //NOI18N
                appContextTemplateName = "applicationContext-3.xml"; //NOI18N
                dispServletTemplateName = "dispatcher-servlet-3.xml"; //NOI18N
            }
            
            FileObject configFile = createFromTemplate(appContextTemplateName, webInfDO, "applicationContext",params); // NOI18N
            addFileToOpen(configFile);
            newConfigFiles.add(FileUtil.toFile(configFile));
            String fullIndexUrl = SpringWebFrameworkUtils.instantiateDispatcherMapping(dispatcherMapping, "index"); // NOI18N
            String simpleIndexUrl = SpringWebFrameworkUtils.getSimpleDispatcherURL(fullIndexUrl);
            Map<String, ?> indexUrlParams = Collections.singletonMap("index", Collections.singletonMap("url", simpleIndexUrl)); // NOI18N
            params.putAll(indexUrlParams);
            configFile = createFromTemplate(dispServletTemplateName, webInfDO, getComponent().getDispatcherName() + "-servlet", params); // NOI18N
            addFileToOpen(configFile);
            newConfigFiles.add(FileUtil.toFile(configFile));
            addFileToOpen(createFromTemplate("index.jsp", DataFolder.findFolder(jsp), "index")); // NOI18N

            // MODIFY EXISTING REDIRECT.JSP
            indexUrlParams = Collections.singletonMap("index", Collections.singletonMap("url", fullIndexUrl)); // NOI18N
            FileObject documentBase = webModule.getDocumentBase();
            FileObject redirectJsp = documentBase.getFileObject("redirect.jsp"); // NOI18N
            if (redirectJsp != null) {
                redirectJsp.delete();
            }
            DataFolder documentBaseDO = DataFolder.findFolder(documentBase);
            addFileToOpen(createFromTemplate("redirect.jsp", documentBaseDO, "redirect", indexUrlParams));

            SpringScope scope = SpringScope.getSpringScope(configFile);
            if (scope != null) {
                final ConfigFileManager manager = scope.getConfigFileManager();
                try {
                    manager.mutex().writeAccess((ExceptionAction<Void>) () -> {
                        List<File> files = manager.getConfigFiles();
                        files.addAll(newConfigFiles);
                        List<ConfigFileGroup> groups = manager.getConfigFileGroups();
                        String groupName = NbBundle.getMessage(SpringWebModuleExtender.class, "LBL_DefaultGroup");
                        ConfigFileGroup newGroup = ConfigFileGroup.create(groupName, newConfigFiles);
                        groups.add(newGroup);
                        manager.putConfigFilesAndGroups(files, groups);
                        manager.save();
                        return null;
                    });
                } catch (MutexException e) {
                    throw (IOException)e.getException();
                }
            } else {
                LOGGER.log(Level.WARNING, "Could not find a SpringScope for file {0}", configFile);
            }
        }

        public void addFileToOpen(FileObject file) {
            filesToOpen.add(file);
        }

        public Set<FileObject> getFilesToOpen() {
            return filesToOpen;
        }

        private FileObject createFromTemplate(String templateName, DataFolder targetDO, String fileName, Map<String, ?> params) throws IOException {
            FileObject templateFO = FileUtil.getConfigFile("SpringFramework/Templates/" + templateName);
            DataObject templateDO = DataObject.find(templateFO);
            return templateDO.createFromTemplate(targetDO, fileName, params).getPrimaryFile();
        }

        private FileObject createFromTemplate(String templateName, DataFolder targetDO, String fileName) throws IOException {
            FileObject templateFO = FileUtil.getConfigFile("SpringFramework/Templates/" + templateName);
            DataObject templateDO = DataObject.find(templateFO);
            return templateDO.createFromTemplate(targetDO, fileName).getPrimaryFile();
        }

        protected boolean addLibrariesToWebModule(List<Library> libraries, WebModule webModule) throws IOException, UnsupportedOperationException {
            FileObject fileObject = webModule.getDocumentBase();
            Project project = FileOwnerQuery.getOwner(fileObject);
            if (project == null) {
                return false;
            }
            boolean addLibraryResult = false;
            try {
                SourceGroup[] groups = SourceGroups.getJavaSourceGroups(project);
                if (groups.length == 0) {
                    return false;
                }
                addLibraryResult = ProjectClassPathModifier.addLibraries(libraries.toArray(new Library[0]), groups[0].getRootFolder(), ClassPath.COMPILE);
            } catch (IOException e) {
                LOGGER.log(Level.WARNING, "Libraries required for the Spring MVC project not added", e); // NOI18N
            } catch (UnsupportedOperationException uoe) {
                LOGGER.log(Level.WARNING, "This project does not support adding these types of libraries to the classpath", uoe); // NOI18N
            }
            return addLibraryResult;
        }

        protected Listener addListener(WebApp webApp, String classname) throws IOException {
            Listener listener = (Listener) createBean(webApp, "Listener"); // NOI18N
            listener.setListenerClass(classname);
            webApp.addListener(listener);
            return listener;
        }

        protected Servlet addServlet(WebApp webApp, String name, String classname, String pattern, String loadOnStartup) throws IOException {
            Servlet servlet = (Servlet) createBean(webApp, "Servlet"); // NOI18N
            servlet.setServletName(name);
            servlet.setServletClass(classname);
            if (loadOnStartup != null) {
                servlet.setLoadOnStartup(new BigInteger(loadOnStartup));
            }
            webApp.addServlet(servlet);
            if (pattern != null) {
                addServletMapping(webApp, name, pattern);
            }
            return servlet;
        }

        protected ServletMapping addServletMapping(WebApp webApp, String name, String pattern) throws IOException {
            ServletMapping25 mapping = (ServletMapping25) createBean(webApp, "ServletMapping"); // NOI18N
            mapping.setServletName(name);
            mapping.addUrlPattern(pattern);
            webApp.addServletMapping(mapping);
            return mapping;
        }

        protected InitParam addContextParam(WebApp webApp, String name, String value) throws IOException {
            InitParam initParam = (InitParam) createBean(webApp, "InitParam"); // NOI18N
            initParam.setParamName(name);
            initParam.setParamValue(value);
            webApp.addContextParam(initParam);
            return initParam;
        }

        protected CommonDDBean createBean(CreateCapability creator, String beanName) throws IOException {
            CommonDDBean bean = null;
            try {
                bean = creator.createBean(beanName);
            } catch (ClassNotFoundException ex) {
                ErrorManager.getDefault().notify(ErrorManager.EXCEPTION, ex);
                throw new IOException("Error creating bean with name:" + beanName); // NOI18N
            }
            return bean;
        }
    }
}
