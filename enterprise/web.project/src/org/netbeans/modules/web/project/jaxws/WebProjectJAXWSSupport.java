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

package org.netbeans.modules.web.project.jaxws;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.api.j2ee.core.Profile;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.project.classpath.ProjectClassPathModifier;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.api.project.libraries.Library;
import org.netbeans.api.project.libraries.LibraryManager;
import org.netbeans.api.project.ui.OpenProjects;
import org.netbeans.modules.j2ee.common.dd.DDHelper;
import org.netbeans.modules.j2ee.core.api.support.SourceGroups;
import org.netbeans.modules.j2ee.dd.api.common.NameAlreadyUsedException;
import org.netbeans.modules.j2ee.dd.api.web.DDProvider;
import org.netbeans.modules.j2ee.dd.api.web.Listener;
import org.netbeans.modules.j2ee.dd.api.web.Servlet;
import org.netbeans.modules.j2ee.dd.api.web.ServletMapping;
import org.netbeans.modules.j2ee.dd.api.web.WebApp;
import org.netbeans.modules.j2ee.dd.api.webservices.WebservicesMetadata;
import org.netbeans.modules.j2ee.deployment.devmodules.api.Deployment;
import org.netbeans.modules.j2ee.deployment.devmodules.api.J2eePlatform;
import org.netbeans.modules.j2ee.metadata.model.api.MetadataModel;
import org.netbeans.modules.web.api.webmodule.WebModule;
import org.netbeans.modules.web.project.WebProject;
import org.netbeans.modules.web.project.WebProjectWebServicesSupport;
import org.netbeans.modules.websvc.api.jaxws.project.LogUtils;
import org.netbeans.modules.websvc.api.jaxws.project.WSUtils;
import org.netbeans.modules.websvc.api.jaxws.project.config.Endpoint;
import org.netbeans.modules.websvc.api.jaxws.project.config.Endpoints;
import org.netbeans.modules.websvc.api.jaxws.project.config.EndpointsProvider;
import org.netbeans.modules.websvc.api.jaxws.project.config.JaxWsModel;
import org.netbeans.modules.websvc.api.jaxws.project.config.Service;
import org.netbeans.modules.websvc.jaxws.api.JAXWSSupport;
import org.netbeans.modules.websvc.jaxws.spi.ProjectJAXWSSupport;
import org.netbeans.spi.project.support.ant.AntProjectHelper;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.filesystems.FileLock;
import org.openide.filesystems.FileObject;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;

/**
 *
 * @author mkuchtiak
 */
public class WebProjectJAXWSSupport extends ProjectJAXWSSupport /*implements JAXWSSupportImpl*/ {
    private WebProject project;
    //servlet name and servlet listener for non-JSR 109 web services
    private static final String servletClassName = "com.sun.xml.ws.transport.http.servlet.WSServlet"; //NOI18N
    private static final String servletListener = "com.sun.xml.ws.transport.http.servlet.WSServletContextListener"; //NOI18N

    /** Creates a new instance of JAXWSSupport */
    public WebProjectJAXWSSupport(WebProject project, AntProjectHelper antProjectHelper) {
        super(project,antProjectHelper);
        this.project = project;
    }

    @Override
    public FileObject getWsdlFolder(boolean create) throws java.io.IOException {
        WebModule webModule = WebModule.getWebModule(project.getProjectDirectory());
        if (webModule!=null) {
            FileObject webInfFo = webModule.getWebInf();
            if (webInfFo!=null) {
                FileObject wsdlFo = webInfFo.getFileObject("wsdl"); //NOI18N
                if (wsdlFo!=null) return wsdlFo;
                else if (create) {
                    return webInfFo.createFolder("wsdl"); //NOI18N
                }
            }
        }
        return null;
    }

    /** Get wsdlLocation information
     * Useful for web service from wsdl
     * @param name service "display" name
     */
    @Override
    public String getWsdlLocation(String serviceName) {
        String localWsdl = serviceName+".wsdl"; //NOI18N
        JaxWsModel jaxWsModel = (JaxWsModel)project.getLookup().lookup(JaxWsModel.class);
        if (jaxWsModel!=null) {
            Service service = jaxWsModel.findServiceByName(serviceName);
            if (service!=null) {
                String localWsdlFile = service.getLocalWsdlFile();
                if (localWsdlFile!=null) localWsdl=localWsdlFile;
            }
        }
        String prefix = "WEB-INF/wsdl/"; //NOI18N
        return prefix+serviceName+"/"+localWsdl; //NOI18N
    }

    /**
     * This is to support non-JSR 109 containers. In this case, a regular jaxws web service
     * is created and the deployment descriptor is updated with the jaxws-ri servlet and
     * listener.
     */
    private void addServiceEntriesToDD(String serviceName) {
        //add servlet entry to web.xml
        String servletName = serviceName;

        WebApp webApp = getWebApp();
        if(webApp != null){
            Servlet servlet;
            try{
                servlet = (Servlet)webApp.addBean("Servlet", new String[]{"ServletName","ServletClass"},
                        new Object[]{servletName,servletClassName}, "ServletName");
                servlet.setLoadOnStartup(new java.math.BigInteger("1"));
                webApp.addBean("ServletMapping", new String[]{"ServletName","UrlPattern"},
                        new Object[]{servletName, "/" + serviceName}, "ServletName");

                if(!webAppHasListener(webApp, servletListener)){
                    webApp.addBean("Listener", new String[]{"ListenerClass"},
                            new Object[]{servletListener}, "ListenerClass");
                }
                // This also saves server specific configuration, if necessary.
                webApp.write(getDeploymentDescriptor());
            } catch (ClassNotFoundException exc) {
                Logger.getLogger("global").log(Level.INFO, exc.getLocalizedMessage());
            } catch (NameAlreadyUsedException exc) {
                Logger.getLogger("global").log(Level.INFO, exc.getLocalizedMessage());
            } catch (IOException exc) {
                Logger.getLogger("global").log(Level.INFO, exc.getLocalizedMessage());
            }
        }
    }

    @Override
    protected void addServletElement(Project project, String wsName, String serviceImpl) throws IOException {
        WebApp webApp = getWebApp();
        if(webApp != null) {
            boolean addServletEntry = false;
            JAXWSSupport wss = JAXWSSupport.getJAXWSSupport(project.getProjectDirectory());
            if (wss != null) {
                Map properties = wss.getAntProjectHelper().getStandardPropertyEvaluator().getProperties();
                if (properties != null) {
                    String serverInstance = (String)properties.get("j2ee.server.instance"); //NOI18N
                    if (serverInstance != null) {
                        J2eePlatform j2eePlatform = Deployment.getDefault().getJ2eePlatform(serverInstance);
                        if (j2eePlatform != null) {
                            if (j2eePlatform.isToolSupported("JaxWs-in-j2ee14-supported")) addServletEntry = true;
                        }
                    }
                }
            }
            if (addServletEntry) {
                try{
                    Servlet servlet = (Servlet)webApp.addBean("Servlet", new String[]{"ServletName","ServletClass"},
                            new Object[]{wsName,serviceImpl}, "ServletName");
                    servlet.setLoadOnStartup(new java.math.BigInteger("1"));
                    webApp.addBean("ServletMapping", new String[]{"ServletName","UrlPattern"},
                            new Object[]{wsName, "/" + wsName}, "UrlPattern");
                    // This also saves server specific configuration, if necessary.
                    webApp.write(getDeploymentDescriptor());
                } catch (ClassNotFoundException exc) {
                    Logger.getLogger("global").log(Level.INFO, exc.getLocalizedMessage());
                } catch (NameAlreadyUsedException exc) {
                    Logger.getLogger("global").log(Level.INFO, exc.getLocalizedMessage());
                }
            }
        }
    }
    /**
     * Remove the web.xml entries for the non-JSR 109 web service.
     *
     * @param serviceName Name of the web service to be removed
     */
    private void removeServiceEntriesFromDD(String serviceName) {
        WebApp webApp = getWebApp();
        if (webApp != null) {
            boolean changed = removeNonJsr109ServletsFromDD(webApp, serviceName);

            //determine if there are other web services in the project
            //if none, remove the listener
            boolean hasMoreWebServices = false;
            Servlet[] remainingServlets = webApp.getServlet();
            for(int i = 0; i < remainingServlets.length; i++){
                if(remainingServlets[i].getServletClass().equals(servletClassName)){
                    hasMoreWebServices = true;
                    break;
                }
            }
            if(!hasMoreWebServices){
                Listener[] listeners = webApp.getListener();
                for(int i = 0; i < listeners.length; i++){
                    Listener listener = listeners[i];
                    if(listener.getListenerClass().equals(servletListener)){
                        webApp.removeListener(listener);
                        changed = true;
                        break;
                    }
                }
            }
            if(changed){
                try{
                    webApp.write(getDeploymentDescriptor());
                } catch(IOException e){
                    Exceptions.printStackTrace(e);
                }
            }
        }
    }

    /**
     * Remove the web.xml servlets for the non-JSR 109 web service.
     *
     * @param serviceName Name of the web service to be removed
     */
    private boolean removeNonJsr109ServletsFromDD(WebApp webApp, String serviceName) {
        boolean changed = false;
        //first remove the servlet
        Servlet[] servlets = webApp.getServlet();
        for(int i = 0; i < servlets.length; i++){
            Servlet servlet = servlets[i];
            if(servlet.getServletName().equals(serviceName)){
                webApp.removeServlet(servlet);
                changed = true;
                break;
            }
        }
        //remove the servlet mapping
        ServletMapping[] mappings = webApp.getServletMapping();
        for(int i = 0; i < mappings.length; i++){
            ServletMapping mapping = mappings[i];
            if(mapping.getServletName().equals(serviceName)){
                webApp.removeServletMapping(mapping);
                changed = true;
            }
        }
        return changed;
    }

    private boolean webAppHasListener(WebApp webApp, String listenerClass){
        Listener[] listeners = webApp.getListener();
        for(int i = 0; i < listeners.length; i++){
            Listener listener = listeners[i];
            if(listenerClass.equals(listener.getListenerClass())){
                return true;
            }
        }
        return false;
    }
    /**
     * Returns the directory that contains the deployment descriptor in the project
     */
    @Override
    public FileObject getDeploymentDescriptorFolder() {
        WebModule webModule = WebModule.getWebModule(project.getProjectDirectory());
        if(webModule != null) {
            return webModule.getWebInf();
        }
        return null;
    }

    private WebApp getWebApp() {
        try {
            FileObject deploymentDescriptor = getDeploymentDescriptor();
            if(deploymentDescriptor != null) {
                return DDProvider.getDefault().getDDRoot(deploymentDescriptor);
            }
        } catch (java.io.IOException e) {
            Logger.getLogger("global").log(Level.INFO, e.getLocalizedMessage());
        }
        return null;
    }

    private boolean isProjectOpened() {
        // XXX workaround: OpenProjects.getDefault() can be null
        // when called from ProjectOpenedHook.projectOpened() upon IDE startup
        if (OpenProjects.getDefault() == null) {
            return true;
        }

        Project[] projects = OpenProjects.getDefault().getOpenProjects();
        for (int i = 0; i < projects.length; i++) {
            if (projects[i].equals(project)) {
                return true;
            }
        }
        return false;
    }

    private FileObject getDeploymentDescriptor() throws IOException {
        WebModule webModule = WebModule.getWebModule(project.getProjectDirectory());
        if (webModule != null) {
            FileObject ddFo = webModule.getDeploymentDescriptor();
            if (ddFo == null) {
                FileObject webInfFo = webModule.getWebInf();
                if (webInfFo != null) {
                    ddFo = DDHelper.createWebXml(webModule.getJ2eeProfile(), webInfFo);
                }
            }
            return ddFo;
        } else {
            DialogDisplayer.getDefault().notify(
                    new NotifyDescriptor.Message(NbBundle.getMessage(WebProjectWebServicesSupport.class,"MSG_WebInfCorrupted"), // NOI18N
                    NotifyDescriptor.ERROR_MESSAGE));
            return null;
        }
    }

    @Override
    protected void addJaxwsArtifacts(Project project, String wsName, String serviceImpl) throws Exception {

        // check if the wsimport class is already present - this means we don't need to add the library
        SourceGroup[] sgs = SourceGroups.getJavaSourceGroups(project);
        if (sgs.length > 0) {
            ClassPath classPath = ClassPath.getClassPath(sgs[0].getRootFolder(),ClassPath.COMPILE);
            if (classPath != null) {
                FileObject wsimportFO = classPath.findResource("com/sun/tools/ws/ant/WsImport.class"); // NOI18N

                if (wsimportFO == null) {
                    //Add the Metro library to the project to be packed with the archive
                    Library metroLib = LibraryManager.getDefault().getLibrary("metro"); //NOI18N
                    if (metroLib != null) {
                        try {
                            ProjectClassPathModifier.addLibraries(
                                    new Library[] {metroLib},
                                    sgs[0].getRootFolder(),
                                    ClassPath.COMPILE);
                        }catch(IOException e){
                            throw new Exception("Unable to add Metro library", e);
                        }
                    } else {
                        throw new Exception("Unable to add Metro Library"); //NOI18N
                    }
                }
            }
        }

        //Add the servlet and listener entry in web.xml
        addServiceEntriesToDD(wsName);

        //create the sun-jaxws deployment descriptor
        FileObject ddFolder = getDeploymentDescriptorFolder();
        if(ddFolder != null){
            FileObject sunjaxwsFile = ddFolder.getFileObject("sun-jaxws.xml");
            if(sunjaxwsFile == null){
                WSUtils.generateSunJaxwsFile(ddFolder);
            }

            sunjaxwsFile = ddFolder.getFileObject("sun-jaxws.xml");
            Endpoints endpoints = EndpointsProvider.getDefault().getEndpoints(sunjaxwsFile);
            Endpoint endpoint = endpoints.newEndpoint();
            endpoint.setEndpointName(wsName);
            endpoint.setImplementation(serviceImpl);
            endpoint.setUrlPattern("/" + wsName);
            endpoints.addEnpoint(endpoint);
            FileLock lock = null;
            OutputStream os = null;
            synchronized (this) {
                try{
                    lock = sunjaxwsFile.lock();
                    os = sunjaxwsFile.getOutputStream(lock);
                    endpoints.write(os);
                }finally{
                    if(lock != null) {
                        lock.releaseLock();
                    }

                    if(os != null) {
                        os.close();
                    }
                }
            }
        }else{
            String mes = NbBundle.getMessage(WebProjectJAXWSSupport.class, "MSG_CannotFindWEB-INF"); // NOI18N
            NotifyDescriptor desc = new NotifyDescriptor.Message(mes, NotifyDescriptor.Message.ERROR_MESSAGE);
            DialogDisplayer.getDefault().notify(desc);
        }
    }

    /** return root folder for xml artifacts
     */
    @Override
    protected FileObject getXmlArtifactsRoot() {
        return project.getWebModule().getConfDir();
    }

    /**
     * Notification when Service (created from java) is removed from jax-ws.xml
     * (JAXWSSupport needs to react when @WebService annotation is removed
     * or when impl.class is removed (manually from project)
     * Default implementation does nothing.
     */
    @Override
    public void serviceFromJavaRemoved(String serviceName) {
        JaxWsModel jaxWsModel = project.getLookup().lookup(JaxWsModel.class);
        Boolean isJsr109 = jaxWsModel.getJsr109();
        if(isJsr109!=null && !isJsr109){
            try{
                removeNonJsr109Entries(serviceName);
            }catch(IOException e){
                Exceptions.printStackTrace(e);
            }
        } else {
            try{
                removeJsr109Entries(serviceName);
            }catch(IOException e){
                Exceptions.printStackTrace(e);
            }
        }

    }

    /**
     * Removes the servlet entry from web.xml and
     * the endpoint entry from the sun-jaxws.xml file
     */
    @Override
    public void removeNonJsr109Entries(String serviceName) throws IOException {
        //delete web.xml entry
        removeServiceEntriesFromDD(serviceName);
        //delete entry in sun-jaxws.xml file.
        FileObject ddFolder = getDeploymentDescriptorFolder();
        if(ddFolder != null){
            FileObject sunjaxwsFile = ddFolder.getFileObject("sun-jaxws.xml");
            if(sunjaxwsFile != null){
                FileLock lock = null;
                //if there are no more services, delete the file
                JaxWsModel jaxWsModel = project.getLookup().lookup(JaxWsModel.class);
                if(jaxWsModel.getServices().length == 0) {
                    synchronized(this) {
                        try{
                            lock = sunjaxwsFile.lock();
                            sunjaxwsFile.delete(lock);
                        } finally{
                            if(lock != null){
                                lock.releaseLock();
                            }
                        }
                    }
                } else{
                    //remove the entry from the sunjaxwsFile
                    Endpoints endpoints = EndpointsProvider.getDefault().getEndpoints(sunjaxwsFile);
                    Endpoint endpoint = endpoints.findEndpointByName(serviceName);
                    if(endpoint != null){
                        endpoints.removeEndpoint(endpoint);
                        OutputStream os = null;
                        synchronized(this) {
                            try{
                                lock = sunjaxwsFile.lock();
                                os = sunjaxwsFile.getOutputStream(lock);
                                endpoints.write(os);
                            }finally{
                                if(lock != null){
                                    lock.releaseLock();
                                }
                                if(os != null){
                                    os.close();
                                }
                            }
                        }
                    }
                }
            }
        }else{
            String mes = NbBundle.getMessage(WebProjectJAXWSSupport.class, "MSG_CannotFindDDDirectory"); // NOI18N
            NotifyDescriptor desc = new NotifyDescriptor.Message(mes, NotifyDescriptor.Message.ERROR_MESSAGE);
            DialogDisplayer.getDefault().notify(desc);
        }

        //TODO if no more web services, remove the jaxws21 library
    }

    /**
     * Removes the servlet entry from web.xml and
     * the endpoint entry from the sun-jaxws.xml file
     */
    public void removeJsr109Entries(String serviceName) throws IOException {
        WebApp webApp = getWebApp();
        if (webApp != null) {
            JaxWsModel jaxWsModel = project.getLookup().lookup(JaxWsModel.class);
            if (jaxWsModel != null) {
                Service service = jaxWsModel.findServiceByName(serviceName);
                if (service != null) {
                    boolean changed = removeJsr109ServletsFromDD(webApp, service);
                    if(changed){
                        try{
                            webApp.write(getDeploymentDescriptor());
                        } catch(IOException e){
                            Exceptions.printStackTrace(e);
                        }
                    }
                }
            }
        }
    }

    /**
     * Remove the web.xml servlets for the non-JSR 109 web service.
     *
     * @param serviceName Name of the web service to be removed
     */
    private boolean removeJsr109ServletsFromDD(WebApp webApp, Service service) {
        boolean changed = false;
        //first remove the servlet
        String implClass = service.getImplementationClass();
        if (implClass != null) {
            String servletName = null;
            for(Servlet servlet:webApp.getServlet()){
                if(implClass.equals(servlet.getServletClass())) {
                    webApp.removeServlet(servlet);
                    servletName = servlet.getServletName();
                    changed = true;
                    break;
                }
            }
            //remove the servlet mapping
            if (servletName != null) {
                for(ServletMapping mapping:webApp.getServletMapping()) {
                    if(mapping.getServletName().equals(servletName)){
                        webApp.removeServletMapping(mapping);
                        changed = true;
                        break;
                    }
                }
            }
        }
        return changed;
    }

    @Override
    public String addService(String name, String serviceImpl, String wsdlUrl, String serviceName,
            String portName, String packageName, boolean isJsr109, boolean useProvider) {
        // create jax-ws.xml if necessary
        FileObject fo = WSUtils.findJaxWsFileObject(project);
        if (fo==null) {
            try {
                WSUtils.createJaxWsFileObject(project);
                // logging first service creation
                logWsDetected();
            } catch (IOException ex) {
                Exceptions.printStackTrace(ex);
            }
        }
        return super.addService(name, serviceImpl, wsdlUrl, serviceName, portName, packageName, isJsr109, useProvider);
    }

    @Override
    public void addService(String serviceName, String serviceImpl, boolean isJsr109) {
        // create jax-ws.xml if necessary
        FileObject fo = WSUtils.findJaxWsFileObject(project);
        if (fo==null) {
            try {
                WSUtils.createJaxWsFileObject(project);
                // logging first service creation
                logWsDetected();
            } catch (IOException ex) {
                Exceptions.printStackTrace(ex);
            }
        }
        super.addService(serviceName, serviceImpl, isJsr109);
    }

    @Override
    public MetadataModel<WebservicesMetadata> getWebservicesMetadataModel() {
        return project.getWebModule().getWebservicesMetadataModel();
    }

    private void logWsDetected() {
        Object[] params = new Object[3];
        params[0] = LogUtils.WS_STACK_JAXWS;
        params[1] = project.getClass().getName();
        params[2] = "SERVICE"; // NOI18N
        LogUtils.logWsDetect(params);
    }

    @Override
    protected String getProjectJavaEEVersion() {
        WebModule webModule = WebModule.getWebModule(project.getProjectDirectory());
        if (webModule != null) {
            switch (webModule.getJ2eeProfile()) {
                case JAVA_EE_6_WEB:
                case JAVA_EE_6_FULL:
                    return JAVA_EE_VERSION_16;
                case JAVA_EE_7_WEB:
                case JAVA_EE_7_FULL:
                    return JAVA_EE_VERSION_17;
                case JAVA_EE_8_WEB:
                case JAVA_EE_8_FULL:
                    return JAVA_EE_VERSION_18;
                case JAKARTA_EE_8_WEB:
                case JAKARTA_EE_8_FULL:
                    return JAKARTA_EE_VERSION_8;
                case JAKARTA_EE_9_WEB:
                case JAKARTA_EE_9_FULL:
                    return JAKARTA_EE_VERSION_9;
                case JAKARTA_EE_9_1_WEB:
                case JAKARTA_EE_9_1_FULL:
                    return JAKARTA_EE_VERSION_91;
                case JAKARTA_EE_10_WEB:
                case JAKARTA_EE_10_FULL:
                    return JAKARTA_EE_VERSION_10;
                case JAKARTA_EE_11_WEB:
                case JAKARTA_EE_11_FULL:
                    return JAKARTA_EE_VERSION_11;
                case JAVA_EE_5:
                    return JAVA_EE_VERSION_15;
                default:
                    break;
            }
        }
        return JAVA_EE_VERSION_NONE;
    }

}
