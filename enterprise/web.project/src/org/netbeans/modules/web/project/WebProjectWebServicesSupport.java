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

package org.netbeans.modules.web.project;

import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.modules.j2ee.dd.api.common.NameAlreadyUsedException;
import org.netbeans.modules.websvc.api.client.WebServicesClientConstants;
import org.netbeans.modules.websvc.spi.webservices.WebServicesSupportImpl;
import java.io.IOException;
import java.util.Arrays;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.api.java.project.JavaProjectConstants;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.api.project.Sources;
import org.netbeans.api.project.ui.OpenProjects;
import org.netbeans.modules.j2ee.dd.api.webservices.ServiceImplBean;
import org.netbeans.spi.project.support.ant.AntProjectHelper;
import org.netbeans.spi.project.support.ant.EditableProperties;
import org.netbeans.spi.project.support.ant.PropertyUtils;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.NbBundle;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.netbeans.modules.j2ee.dd.api.web.Servlet;
import org.netbeans.modules.j2ee.dd.api.web.ServletMapping;
import org.netbeans.modules.j2ee.dd.api.web.WebApp;
import org.netbeans.modules.j2ee.dd.api.web.DDProvider;
import org.netbeans.modules.web.project.classpath.ClassPathProviderImpl;
import org.netbeans.modules.web.project.ui.customizer.WebProjectProperties;
import static org.netbeans.modules.websvc.spi.webservices.WebServicesConstants.*;
import org.netbeans.modules.websvc.api.webservices.WsCompileEditorSupport;
import org.netbeans.modules.websvc.api.webservices.StubDescriptor;
import org.netbeans.spi.java.classpath.ClassPathFactory;
import org.netbeans.spi.java.classpath.support.ClassPathSupport;
import org.netbeans.spi.java.project.classpath.support.ProjectClassPathSupport;
import org.netbeans.spi.project.support.ant.ReferenceHelper;

/**
 *
 * @author  rico
 * Implementation of WebServicesSupportImpl
 */
public class WebProjectWebServicesSupport implements WebServicesSupportImpl {
    private WebProject project;
    private AntProjectHelper helper;
    private ReferenceHelper referenceHelper;
    private ClassPath projectSourcesClassPath;
    
    /** Creates a new instance of WebProjectWebServicesSupport */
    public WebProjectWebServicesSupport(WebProject project, AntProjectHelper helper, ReferenceHelper referenceHelper) {
        this.project = project;
        this.helper = helper;
        this.referenceHelper = referenceHelper;
    }
    
    public void addServiceImpl(String serviceName, FileObject configFile, boolean fromWSDL) {
        this.addServiceImpl(serviceName, configFile, fromWSDL,null);
    }
    //implementation of WebServicesSupportImpl 
    public void addServiceImpl(String serviceName, FileObject configFile, boolean fromWSDL, String[] wscompileFeatures) {
        //Add properties to project.properties file
        EditableProperties ep =  helper.getProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH);
        String packageName = getPackageName(configFile);
        ep.put(serviceName + CONFIG_PROP_SUFFIX, packageName +
        (packageName.equals("") ? "" : "/") + configFile.getNameExt()); //NOI18N
        ep.put(serviceName + MAPPING_PROP_SUFFIX, serviceName + MAPPING_FILE_SUFFIX); //NOI18N
        // Add property for wscompile
        String featurePropertyName = "wscompile.service." + serviceName + ".features"; // NOI18N
        getWebservicesDD();
        JAXRPCStubDescriptor stubDesc = null;
        if (fromWSDL) {
            if (wscompileFeatures!=null) 
                stubDesc = new JAXRPCStubDescriptor(StubDescriptor.WSDL_SERVICE_STUB,
                                NbBundle.getMessage(WebProjectWebServicesSupport.class,"LBL_WSDLServiceStub"),
                                wscompileFeatures);
            else stubDesc = wsdlServiceStub;
        } else {
            stubDesc = seiServiceStub;
        }
        String defaultFeatures = stubDesc.getDefaultFeaturesAsArgument();
            ep.put(featurePropertyName, defaultFeatures);
            helper.putProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH, ep);
            //Add web-services information in project.xml
            Element data = helper.getPrimaryConfigurationData(true);
            Document doc = data.getOwnerDocument();
            NodeList nodes = data.getElementsByTagName(WEB_SERVICES); //NOI18N
            Element webservices = null;
            if (nodes.getLength() == 0){
                webservices = doc.createElementNS(WebProjectType.PROJECT_CONFIGURATION_NAMESPACE, WEB_SERVICES); //NOI18N
                NodeList insertBefore = data.getElementsByTagNameNS(WebProjectType.PROJECT_CONFIGURATION_NAMESPACE, WebServicesClientConstants.WEB_SERVICE_CLIENTS);
                if (insertBefore.getLength() <= 0) {
                    insertBefore = data.getElementsByTagNameNS(WebProjectType.PROJECT_CONFIGURATION_NAMESPACE, "source-roots"); // NOI18N
                    assert insertBefore.getLength() == 1 : "Invalid project.xml file."; // NOI18N
                }
                data.insertBefore(webservices, insertBefore.item(0));
            } else {
                webservices = (Element)nodes.item(0);
            }
            Element webservice = doc.createElementNS(WebProjectType.PROJECT_CONFIGURATION_NAMESPACE, WEB_SERVICE); //NOI18N
            webservices.appendChild(webservice);
            Element webserviceName = doc.createElementNS(WebProjectType.PROJECT_CONFIGURATION_NAMESPACE, WEB_SERVICE_NAME); //NOI18N
            webservice.appendChild(webserviceName);
            webserviceName.appendChild(doc.createTextNode(serviceName));
            if(fromWSDL) {
                Element fromWSDLElem = doc.createElementNS(WebProjectType.PROJECT_CONFIGURATION_NAMESPACE, "from-wsdl");
                webservice.appendChild(fromWSDLElem);
            }
            helper.putPrimaryConfigurationData(data, true);
            // Update wscompile related properties.  boolean return indicates whether
            // any changes were made.
            updateWsCompileProperties(serviceName);
            try {
                ProjectManager.getDefault().saveProject(project);
            }catch(java.io.IOException ioe){
                throw new RuntimeException(ioe.getMessage());
            }
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
    
    public  void addServiceEntriesToDD(String serviceName, String serviceEndpointInterface, String servantClassName) {
        //add servlet entry to web.xml
        String servletName = WebServiceServlet_PREFIX + serviceName;
        WebApp webApp = getWebApp();
        if(webApp != null){
            Servlet servlet = null;
            try{
                servlet = (Servlet)webApp.addBean("Servlet", new String[]{"ServletName","ServletClass"},
                new Object[]{servletName,servantClassName}, "ServletName");
                servlet.setLoadOnStartup(new java.math.BigInteger("1"));
                ServletMapping servletMapping = (ServletMapping)
                webApp.addBean("ServletMapping", new String[]{"ServletName","UrlPattern"},
                new Object[]{servletName, "/" + serviceName}, "ServletName");
                
                // This also saves server specific configuration, if necessary.
                webApp.write(getDeploymentDescriptor());
            } catch (ClassNotFoundException exc) {
                exc.printStackTrace();
                throw new RuntimeException(exc.getMessage());
            } catch (NameAlreadyUsedException exc) {
                exc.printStackTrace();
                throw new RuntimeException(exc.getMessage());
            } catch (IOException exc) {
                exc.printStackTrace();
                throw new RuntimeException(exc.getMessage());
            }
        }
    }
    
    
    /**
     * Get the webservices.xml file object
     * descriptive in interface, e.g., getWebserviceDD
     */
    public FileObject getWebservicesDD() {
        FileObject webInfFo = getWebInf();
        if (webInfFo==null) {
            if (isProjectOpened()) {
                DialogDisplayer.getDefault().notify(
                new NotifyDescriptor.Message(NbBundle.getMessage(WebProjectWebServicesSupport.class,"MSG_WebInfCorrupted"),
                NotifyDescriptor.ERROR_MESSAGE));
            }
            return null;
        }
        return getWebInf().getFileObject(WEBSERVICES_DD, "xml");
    }
    
    /**
     *  Returns the directory that contains webservices.xml in the project
     */
    public FileObject getWsDDFolder() {
        return getWebInf();
    }
    
    /**
     * Returns the name of the directory that contains the webservices.xml in
     * the archive
     */
    public String getArchiveDDFolderName() {
        return "WEB-INF"; // NOI18N
    }
    
    /**
     * Returns the name of the implementation bean class
     * given the servlet-link name
     */
    public String getImplementationBean(String linkName) {
        WebApp webApp = getWebApp();
        org.netbeans.modules.j2ee.dd.api.web.Servlet[] servlets = webApp.getServlet();
        for(int i = 0; i < servlets.length; i++) {
            if(servlets[i].getServletName().equals(linkName)) {
                return servlets[i].getServletClass();
            }
        }
        return null;
    }
    
    public boolean isFromWSDL(String serviceName) {
        Element data = helper.getPrimaryConfigurationData(true);
        NodeList nodes = data.getElementsByTagNameNS(WebProjectType.PROJECT_CONFIGURATION_NAMESPACE,
        WEB_SERVICES); //NOI18N
        Element webservices = null;
        Element wsNameNode = null;
        if(nodes.getLength() == 1){
            webservices = (Element)nodes.item(0);
            NodeList wsNodes = webservices.getElementsByTagNameNS(WebProjectType.PROJECT_CONFIGURATION_NAMESPACE,
            WEB_SERVICE); //NOI18N
            for(int j = 0; j < wsNodes.getLength(); j++) {
                Element wsNode = (Element)wsNodes.item(j);
                NodeList wsNameNodes = wsNode.getElementsByTagNameNS(WebProjectType.PROJECT_CONFIGURATION_NAMESPACE,
                WEB_SERVICE_NAME); //NOI18N
                if(wsNameNodes.getLength() == 1) {
                    wsNameNode = (Element)wsNameNodes.item(0);
                    NodeList nl = wsNameNode.getChildNodes();
                    if(nl.getLength() == 1) {
                        Node n = nl.item(0);
                        if(n.getNodeType() == Node.TEXT_NODE) {
                            if(serviceName.equals(n.getNodeValue())) {
                                NodeList fromWSDLNodes = wsNode.getElementsByTagNameNS(WebProjectType.PROJECT_CONFIGURATION_NAMESPACE,
                                WEB_SERVICE_FROM_WSDL); //NOI18N
                                if(fromWSDLNodes.getLength() == 1) {
                                    return true;
                                }
                            }
                        }
                    }
                }
            }
        }
        return false;
    }
    
    public void removeProjectEntries(String serviceName) {
        boolean needsSave = false;
        
        //Remove entries in the project.properties file
        //FIX-ME:we should move this to websvc
        EditableProperties ep =  helper.getProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH);
        String configProperty = serviceName + CONFIG_PROP_SUFFIX;
        String mappingProperty = serviceName + MAPPING_PROP_SUFFIX;
        if(ep.getProperty(configProperty) != null) {
            ep.remove(configProperty);
            needsSave = true;
        }
        if(ep.getProperty(mappingProperty) != null) {
            ep.remove(mappingProperty);
            needsSave = true;
        }
        String featureProperty = "wscompile.service." + serviceName + ".features"; // NOI18N
        if(ep.getProperty(featureProperty) != null) {
            ep.remove(featureProperty);
            needsSave = true;
        }
        if(needsSave){
            helper.putProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH, ep);
        }
        //Remove entry in the project.xml file (we should move this to websvc)
        Element data = helper.getPrimaryConfigurationData(true);
        NodeList nodes = data.getElementsByTagNameNS(WebProjectType.PROJECT_CONFIGURATION_NAMESPACE,
        WEB_SERVICES); //NOI18N
        Element webservices = null;
        Element wsNameNode = null;
        if(nodes.getLength() == 1){
            webservices = (Element)nodes.item(0);
            NodeList wsNodes = webservices.getElementsByTagNameNS(WebProjectType.PROJECT_CONFIGURATION_NAMESPACE,
            WEB_SERVICE); //NOI18N
            for(int j = 0; j < wsNodes.getLength(); j++) {
                Element wsNode = (Element)wsNodes.item(j);
                NodeList wsNameNodes = wsNode.getElementsByTagNameNS(WebProjectType.PROJECT_CONFIGURATION_NAMESPACE,
                WEB_SERVICE_NAME); //NOI18N
                if(wsNameNodes.getLength() == 1) {
                    wsNameNode = (Element)wsNameNodes.item(0);
                    NodeList nl = wsNameNode.getChildNodes();
                    if(nl.getLength() == 1) {
                        Node n = nl.item(0);
                        if(n.getNodeType() == Node.TEXT_NODE) {
                            if(serviceName.equals(n.getNodeValue())) {
                                webservices.removeChild(wsNode);
                                //if there are no more children, remove the web-services node
                                NodeList children = webservices.getChildNodes();
                                if(children.getLength() == 0) {
                                    data.removeChild(webservices);
                                }
                                needsSave = true;
                                break;
                            }
                        }
                    }
                }
            }
        }
        if(needsSave) {
            helper.putPrimaryConfigurationData(data, true);
            try {
                ProjectManager.getDefault().saveProject(project);
            } catch(IOException ex) {
                String mes = NbBundle.getMessage(this.getClass(), "MSG_ErrorSavingOnWSRemove") + serviceName // NOI18N
                + "'\r\n" + ex.getMessage();
                NotifyDescriptor desc = new NotifyDescriptor.
                Message(mes, NotifyDescriptor.Message.ERROR_MESSAGE);
                DialogDisplayer.getDefault().notify(desc);			}
        }
    }
    
    public void removeServiceEntry(String linkName) {
        //remove servlet entry in web.xml
        WebApp webApp = getWebApp();
        Servlet[] servlets = webApp.getServlet();
        for(int i = 0; i < servlets.length; i++) {
            Servlet servlet = servlets[i];
            if(servlet.getServletName().equals(linkName)) {
                webApp.removeServlet(servlet);
                break;
            }
        }
        ServletMapping[] mappings = webApp.getServletMapping();
        for(int j = 0; j < mappings.length; j++ ) {
            ServletMapping mapping = mappings[j];
            if(mapping.getServletName().equals(linkName)) {
                webApp.removeServletMapping(mapping);
            }
        }
        try {
            // This also saves server specific configuration, if necessary.
            webApp.write(getDeploymentDescriptor());
        }
        catch(java.io.IOException e) {
            NotifyDescriptor ndd =
            new NotifyDescriptor.Message(NbBundle.getMessage(this.getClass(), "MSG_Unable_WRITE_WS_DD"), // NOI18N
            NotifyDescriptor.ERROR_MESSAGE);
            DialogDisplayer.getDefault().notify(ndd);
        }
    }
    
    public AntProjectHelper getAntProjectHelper() {
        return helper;
    }
    
    public String generateImplementationBean(String wsName, FileObject pkg, Project project, String delegateData)
    throws java.io.IOException {
        return null;
        //FIX-ME: move impl bean generation here
    }
    
    public void addServiceImplLinkEntry(ServiceImplBean serviceImplBean, String wsName) {
        serviceImplBean.setServletLink(WebServiceServlet_PREFIX + wsName);
    }
    
    public ReferenceHelper getReferenceHelper(){
        return referenceHelper;
    }
    
    /** !PW This method is exposed in the service support API.  Though it's
     *  implementation makes more sense here than anywhere else, perhaps this
     *  and the other project.xml/project.properties related methods in this
     *  object should be refactored into another object that this one delegates
     *  to.  That way, this method would be directly available within the web or
     *  ejb module, as it is needed, and remain missing from the API (where it
     *  probably does not belong at this time.
     */
    private static final String [] WSCOMPILE_SEI_SERVICE_FEATURES = {
        //        "datahandleronly", // WSDL - portable
        "documentliteral", // SEI ONLY - portable
        "rpcliteral", // SEI ONLY - portable
        //        "explicitcontext", // WSDL - portable
        //        "infix:<name>", // difficult handle with current API
        //        "jaxbenumtype", // WSDL
        //        "nodatabinding", // WSDL - portable
        "noencodedtypes",
        "nomultirefs",
        //        "norpcstructures", // import only - portable
        //        "novalidation", // WSDL - portable
        //        "resolveidref", // WSDL
        //        "searchschema", // WSDL - portable
        "serializeinterfaces",
        "strict", // - portable
        "useonewayoperations", // SEI ONLY - portable
        //        "wsi", // WSDL - portable
        //        "unwrap", // WSDL - portable
        "donotoverride", // - portable
        //        "donotunwrap", // WSDL - portable
    };
    
    private static final List<String> allSeiServiceFeatures = Arrays.asList(WSCOMPILE_SEI_SERVICE_FEATURES);
    
    private static final String [] WSCOMPILE_KEY_SEI_SERVICE_FEATURES = {
        "documentliteral",
        "rpcliteral",
        "strict",
        "useonewayoperations",
        "donotoverride"
    };
    
    private static final List<String> importantSeiServiceFeatures = Arrays.asList(WSCOMPILE_KEY_SEI_SERVICE_FEATURES);
    
    private static final String [] WSCOMPILE_WSDL_SERVICE_FEATURES = {
        "datahandleronly", // WSDL - portable
        //        "documentliteral", // SEI ONLY - portable
        //        "rpcliteral", // SEI ONLY - portable
        "explicitcontext", // WSDL - portable
        //        "infix:<name>", // difficult handle with current API
        "jaxbenumtype", // WSDL
        "nodatabinding", // WSDL - portable
        "noencodedtypes",
        "nomultirefs",
        "norpcstructures", // import only - portable
        "novalidation", // WSDL - portable
        "resolveidref", // WSDL
        "searchschema", // WSDL - portable
        "serializeinterfaces",
        "strict", // - portable
        //        "useonewayoperations", // SEI ONLY - portable
        "wsi", // WSDL - portable
        "unwrap", // WSDL - portable
        "donotoverride", // - portable
        "donotunwrap", // WSDL - portable
    };
    
    private static final List<String> allWsdlServiceFeatures = Arrays.asList(WSCOMPILE_WSDL_SERVICE_FEATURES);
    
    private static final String [] WSCOMPILE_KEY_WSDL_SERVICE_FEATURES = {
        "wsi",
        "strict",
        "unwrap",
        "donotunwrap",
        "donotoverride",
        "datahandleronly",
        "nodatabinding",
        "novalidation",
        "searchschema",
        "explicitcontext",
    };
    
    private static final List<String> importantWsdlServiceFeatures = Arrays.asList(WSCOMPILE_KEY_WSDL_SERVICE_FEATURES);
    
    public List<WsCompileEditorSupport.ServiceSettings> getServices() {
        List<WsCompileEditorSupport.ServiceSettings> serviceList = new ArrayList<WsCompileEditorSupport.ServiceSettings>();
        
        Element data = helper.getPrimaryConfigurationData(true);
        NodeList nodes = data.getElementsByTagName(WEB_SERVICES);
        EditableProperties projectProperties = helper.getProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH);
        
        if(nodes.getLength() != 0) {
            Element serviceElements = (Element) nodes.item(0);
            NodeList serviceNameList = serviceElements.getElementsByTagNameNS(
            WebProjectType.PROJECT_CONFIGURATION_NAMESPACE, WEB_SERVICE_NAME);
            for(int i = 0; i < serviceNameList.getLength(); i++ ) {
                Element serviceNameElement = (Element) serviceNameList.item(i);
                NodeList nl = serviceNameElement.getChildNodes();
                if(nl.getLength() == 1) {
                    org.w3c.dom.Node n = nl.item(0);
                    if(n.getNodeType() == org.w3c.dom.Node.TEXT_NODE) {
                        String serviceName = n.getNodeValue();
                        String currentFeatures = projectProperties.getProperty("wscompile.service." + serviceName + ".features"); // NOI18N
                        StubDescriptor stubType = getServiceStubDescriptor(serviceNameElement.getParentNode());
                        WsCompileEditorSupport.ServiceSettings settings;
                        
                        // !PW The logic for managing wscompile options needs refactoring badly.
                        if(seiServiceStub == stubType) {
                            if(currentFeatures == null) {
                                // default for SEI generation
                                currentFeatures = seiServiceStub.getDefaultFeaturesAsArgument();
                            }
                            settings = new WsCompileEditorSupport.ServiceSettings(
                            serviceName, stubType, currentFeatures, allSeiServiceFeatures, importantSeiServiceFeatures);
                        } else { // Should only ever be wsdl node here.)
                            if(currentFeatures == null) {
                                // default for WSDL generation
                                currentFeatures = wsdlServiceStub.getDefaultFeaturesAsArgument();
                            }
                            settings = new WsCompileEditorSupport.ServiceSettings(
                            serviceName, stubType, currentFeatures, allWsdlServiceFeatures, importantWsdlServiceFeatures);
                        }
                        serviceList.add(settings);
                    } else {
                        // !PW FIXME node is wrong type?! - log message or trace?
                    }
                } else {
                    // !PW FIXME no name for this service entry - notify user
                }
            }
        }
        
        return serviceList;
    }
    
    private StubDescriptor getServiceStubDescriptor(org.w3c.dom.Node parentNode) {
        StubDescriptor result = null;
        
        if(parentNode instanceof Element) {
            Element parentElement = (Element) parentNode;
            NodeList fromWsdlList = parentElement.getElementsByTagNameNS(
            WebProjectType.PROJECT_CONFIGURATION_NAMESPACE, WEB_SERVICE_FROM_WSDL);
            if(fromWsdlList.getLength() == 1) {
                result = wsdlServiceStub;
            } else {
                result = seiServiceStub;
            }
        }
        
        return result;
    }
    
    private String getPackageName(FileObject file){
        FileObject parent = file.getParent();
        Sources sources = ProjectUtils.getSources(project);
        SourceGroup[] groups = sources.getSourceGroups(JavaProjectConstants.SOURCES_TYPE_JAVA);
        String packageName = null;
        for (int i = 0; i < groups.length && packageName == null; i++) {
            packageName = FileUtil.getRelativePath(groups[i].getRootFolder(), parent);
            if (packageName != null) {
                packageName = groups[i].getName() + "/" + packageName;
            }
        }
        return packageName + "";
    }
    
    public void addInfrastructure(String implBeanClass, FileObject pkg) {
        //nothing to do here, there are no infrastructure elements
    }
    
    public FileObject getDeploymentDescriptor() {
        FileObject webInfFo = getWebInf();
        if (webInfFo==null) {
            if (isProjectOpened()) {
                DialogDisplayer.getDefault().notify(
                new NotifyDescriptor.Message(NbBundle.getMessage(WebProjectWebServicesSupport.class,"MSG_WebInfCorrupted"), // NOI18N
                NotifyDescriptor.ERROR_MESSAGE));
            }
            return null;
        }
        return getWebInf().getFileObject(ProjectWebModule.FILE_DD);
    }
    
    public FileObject getWebInf() {
        return getFileObject("webinf.dir"); // NOI18N
        
//        FileObject documentBase = getDocumentBase();
//        if (documentBase != null)
//            return documentBase.getFileObject(ProjectWebModule.FOLDER_WEB_INF);
//        else
//            return null;
    }
    
    public FileObject getDocumentBase() {
        return getFileObject("web.docbase.dir"); // NOI18N
    }
    
    private FileObject getFileObject(String propname) {
        String prop = helper.getStandardPropertyEvaluator().getProperty(propname);
        if (prop != null) {
            return helper.resolveFileObject(prop);
        } else {
            return null;
        }
    }
    
    private boolean updateWsCompileProperties(String serviceName) {
        /** Ensure wscompile.classpath and wscompile.tools.classpath are
         *  properly defined.
         *
         *  wscompile.classpath goes in project properties and includes
         *  jaxrpc and qname right now.
         *
         *  wscompile.tools.classpath is for tools.jar which is needed when
         *  running under the Sun JDK to invoke javac.  It is placed in
         *  user.properties so that if we compute it incorrectly (say on a mac)
         *  the user can change it and we will not blow away the change.
         *  Hopefully we can do this better for release.
         */
        boolean globalPropertiesChanged = false;
        
        EditableProperties globalProperties = PropertyUtils.getGlobalProperties();
        if(globalProperties.getProperty(WSCOMPILE_TOOLS_CLASSPATH) == null) {
            globalProperties.setProperty(WSCOMPILE_TOOLS_CLASSPATH, "${java.home}\\..\\lib\\tools.jar"); // NOI18N
            
            try {
                PropertyUtils.putGlobalProperties(globalProperties);
            } catch(IOException ex) {
                NotifyDescriptor desc = new NotifyDescriptor.Message(
                NbBundle.getMessage(WebProjectWebServicesSupport.class,"MSG_ErrorSavingGlobalProperties", serviceName, ex.getMessage()), // NOI18N
                NotifyDescriptor.ERROR_MESSAGE);
                DialogDisplayer.getDefault().notify(desc);
            }
            
            globalPropertiesChanged = true;
        }
        
        
        boolean projectPropertiesChanged = false;
        EditableProperties projectProperties = helper.getProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH);
        
        { // Block that adjusts wscompile.client.classpath as necessary.
            HashSet<String> wscJars = new HashSet<String>();
            boolean newWscJars = false;
            String wscClientClasspath = projectProperties.getProperty(WSCOMPILE_CLASSPATH);
            if(wscClientClasspath != null) {
                String [] libs = PropertyUtils.tokenizePath(wscClientClasspath);
                for(int i = 0; i < libs.length; i++) {
                    wscJars.add(libs[i]);
                }
            }
            
            for(int i = 0; i < WSCOMPILE_JARS.length; i++) {
                if(!wscJars.contains(WSCOMPILE_JARS[i])) {
                    wscJars.add(WSCOMPILE_JARS[i]);
                    newWscJars = true;
                }
            }
            
            if(newWscJars) {
                StringBuffer newClasspathBuf = new StringBuffer(256);
                for(Iterator iter = wscJars.iterator(); iter.hasNext(); ) {
                    newClasspathBuf.append(iter.next());
                    if(iter.hasNext()) {
                        newClasspathBuf.append(':');
                    }
                }
                projectProperties.put(WSCOMPILE_CLASSPATH, newClasspathBuf.toString());
                projectPropertiesChanged = true;
            }
        }
        
        // set tools.jar property if not set
        if(projectProperties.getProperty(WSCOMPILE_TOOLS_CLASSPATH) == null) {
            projectProperties.setProperty(WSCOMPILE_TOOLS_CLASSPATH, "${java.home}\\..\\lib\\tools.jar"); // NOI18N
            projectPropertiesChanged = true;
        }
        
        if(projectPropertiesChanged) {
            helper.putProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH, projectProperties);
        }
        
        return globalPropertiesChanged || projectPropertiesChanged;
    }
        
    public FileObject getWsdlFolder(boolean create) throws IOException {
        FileObject wsdlFolder = null;
        FileObject webInf = getWebInf();
        
        if(webInf != null) {
            wsdlFolder = webInf.getFileObject(WSDL_FOLDER);
            if(wsdlFolder == null && create) {
                wsdlFolder = webInf.createFolder(WSDL_FOLDER);
            }
        } else if(create) {
            // Create was specified, but no WEB-INF was found, so how do we create it?
            // Expect an NPE if we return null for this case, but log it anyway.
            Logger.getLogger("global").log(Level.INFO, NbBundle.getMessage(WebProjectWebServicesSupport.class, "MSG_WebInfNotFoundForWsdlFolder"));
        }
        
        return wsdlFolder;
    }
    
    private boolean isProjectOpened() {
        // XXX workaround: OpenProjects.getDefault() can be null 
        // when called from ProjectOpenedHook.projectOpened() upon IDE startup
        if (OpenProjects.getDefault() == null)
            return true;
        
        Project[] projects = OpenProjects.getDefault().getOpenProjects();
        for (int i = 0; i < projects.length; i++) {
            if (projects[i].equals(project))
                return true;
        }
        return false;
    }

    public ClassPath getClassPath() {
        synchronized (this) {
            if (projectSourcesClassPath == null) {
                ClassPathProviderImpl cpProvider = project.getClassPathProvider();
                projectSourcesClassPath = ClassPathSupport.createProxyClassPath(new ClassPath[] {
                    cpProvider.getProjectSourcesClassPath(ClassPath.SOURCE),
                    getJ2eePlatformClassPath(),
                });
            }
            return projectSourcesClassPath;
        }
    }
    
    public synchronized ClassPath getJ2eePlatformClassPath() {
        if (platformClassPath == null) {
            platformClassPath = ClassPathFactory.createClassPath(ProjectClassPathSupport.createPropertyBasedClassPathImplementation(
                    FileUtil.toFile(project.getProjectDirectory()), project.evaluator(), new String[] {WebProjectProperties.J2EE_PLATFORM_CLASSPATH }));
        }
        return platformClassPath;
    }
    
    private ClassPath platformClassPath = null;
        
    // Service stub descriptors
    private static final JAXRPCStubDescriptor seiServiceStub = new JAXRPCStubDescriptor(
    StubDescriptor.SEI_SERVICE_STUB,
    NbBundle.getMessage(WebProjectWebServicesSupport.class,"LBL_SEIServiceStub"),
    new String [] { "documentliteral", "strict", "useonewayoperations" });
    
    private static final JAXRPCStubDescriptor wsdlServiceStub = new JAXRPCStubDescriptor(
    StubDescriptor.WSDL_SERVICE_STUB,
    NbBundle.getMessage(WebProjectWebServicesSupport.class,"LBL_WSDLServiceStub"),
    new String [] { "wsi", "strict" });
        
    /** Stub descriptor for services and clients supported by this project type.
     */
    private static class JAXRPCStubDescriptor extends StubDescriptor {
        
        private String [] defaultFeatures;
        
        public JAXRPCStubDescriptor(String name, String displayName, String [] defaultFeatures) {
            super(name, displayName);
            
            this.defaultFeatures = defaultFeatures;
        }
        
        public String [] getDefaultFeatures() {
            return defaultFeatures;
        }
        
        public String getDefaultFeaturesAsArgument() {
            StringBuffer buf = new StringBuffer(defaultFeatures.length*32);
            for(int i = 0; i < defaultFeatures.length; i++) {
                if(i > 0) {
                    buf.append(',');
                }
                
                buf.append(defaultFeatures[i]);
            }
            return buf.toString();
        }
    }
}
