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

package org.netbeans.modules.j2ee.ejbjarproject;

import com.sun.source.tree.AnnotationTree;
import com.sun.source.tree.ClassTree;
import com.sun.source.tree.ExpressionTree;
import com.sun.source.tree.IdentifierTree;
import com.sun.source.tree.MemberSelectTree;
import com.sun.source.tree.MethodTree;
import com.sun.source.tree.ModifiersTree;
import com.sun.source.tree.Tree;
import com.sun.source.tree.Tree.Kind;
import com.sun.source.tree.TypeParameterTree;
import com.sun.source.tree.VariableTree;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.io.IOException;
import javax.lang.model.element.Modifier;
import java.util.Collections;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.modules.j2ee.core.api.support.java.SourceUtils;
import org.netbeans.modules.websvc.api.client.WebServicesClientConstants;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.netbeans.spi.project.support.ant.AntProjectHelper;
import org.netbeans.modules.websvc.spi.webservices.WebServicesSupportImpl;
import org.netbeans.spi.project.support.ant.EditableProperties;
import org.netbeans.spi.project.support.ant.PropertyUtils;
import org.netbeans.modules.j2ee.dd.api.ejb.DDProvider;
import org.netbeans.modules.j2ee.dd.api.ejb.EjbJar;
import org.netbeans.modules.j2ee.dd.api.ejb.EnterpriseBeans;
import org.netbeans.modules.j2ee.dd.api.ejb.Session;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.api.project.Sources;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.NbBundle;
import org.netbeans.modules.j2ee.dd.api.webservices.ServiceImplBean;
import org.netbeans.api.java.project.JavaProjectConstants;
import org.netbeans.api.java.source.CancellableTask;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.JavaSource.Phase;
import org.netbeans.api.java.source.TreeMaker;
import org.netbeans.api.java.source.WorkingCopy;
import org.netbeans.modules.java.api.common.classpath.ClassPathProviderImpl;
import org.netbeans.modules.j2ee.ejbjarproject.ui.customizer.EjbJarProjectProperties;
import org.netbeans.modules.websvc.spi.webservices.WebServicesConstants;
import org.netbeans.modules.websvc.api.webservices.WsCompileEditorSupport;
import org.netbeans.modules.websvc.api.webservices.StubDescriptor;
import org.netbeans.spi.java.classpath.ClassPathFactory;
import org.netbeans.spi.project.support.ant.ReferenceHelper;
import static org.netbeans.modules.websvc.spi.webservices.WebServicesConstants.*;
import org.netbeans.spi.java.classpath.support.ClassPathSupport;
import org.netbeans.spi.java.project.classpath.support.ProjectClassPathSupport;
import org.openide.ErrorManager;

/**
 *
 * @author  rico
 * Implementation of WebServicesSupportImpl
 */
public class EjbJarWebServicesSupport implements WebServicesSupportImpl{
    
    private EjbJarProject project;
    private AntProjectHelper helper;
    private ReferenceHelper referenceHelper;
    private ClassPath projectSourcesClassPath;
    
    /** Creates a new instance of EjbJarWebServicesSupport */
    public EjbJarWebServicesSupport(EjbJarProject project, AntProjectHelper helper, ReferenceHelper referenceHelper) {
        this.project = project;
        this.helper = helper;
        this.referenceHelper = referenceHelper;
    }
    
    //implementation of WebServicesSupportImpl
    public String generateImplementationBean(String wsName, FileObject pkg, Project project, String delegateData)throws java.io.IOException {
        //TODO: RETOUCHE waiting for ejbcore
        //        SessionGenerator sessionGenerator = new SessionGenerator();
        //        return sessionGenerator.generateWebServiceImplBean(wsName, pkg, project, delegateData);
        return null;
    }
    
    public void addServiceImpl(String serviceName, FileObject configFile, boolean fromWSDL) {
        this.addServiceImpl(serviceName, configFile, fromWSDL,null);
    }
    
    public void addServiceImpl(String serviceName, FileObject configFile, boolean fromWSDL, String[] wscompileFeatures) {
        
        //Add properties to project.properties file
        EditableProperties ep =  helper.getProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH);
        String packageName = getPackageName(configFile);
        ep.put(serviceName + CONFIG_PROP_SUFFIX, packageName +
                (packageName.equals("") ? "" : "/") + configFile.getNameExt()); //NOI18N
        ep.put(serviceName + MAPPING_PROP_SUFFIX, serviceName + MAPPING_FILE_SUFFIX); //NOI18N
        // Add property for wscompile
        String featurePropertyName = "wscompile.service." + serviceName + ".features"; // NOI18N
        JAXRPCStubDescriptor stubDesc = null;
        if (fromWSDL) {
            if (wscompileFeatures!=null) {
                stubDesc = new JAXRPCStubDescriptor(StubDescriptor.WSDL_SERVICE_STUB,
                        NbBundle.getMessage(EjbJarWebServicesSupport.class,"LBL_WSDLServiceStub"),
                        wscompileFeatures);
            } else {
                stubDesc = wsdlServiceStub;
            }
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
        if(nodes.getLength() == 0){
            webservices = doc.createElementNS(EjbJarProjectType.PROJECT_CONFIGURATION_NAMESPACE, WEB_SERVICES); //NOI18N
            NodeList insertBefore = data.getElementsByTagNameNS(EjbJarProjectType.PROJECT_CONFIGURATION_NAMESPACE, WebServicesClientConstants.WEB_SERVICE_CLIENTS);
            if (insertBefore.getLength() <= 0) {
                insertBefore = data.getElementsByTagNameNS(EjbJarProjectType.PROJECT_CONFIGURATION_NAMESPACE, "source-roots"); // NOI18N
                assert insertBefore.getLength() == 1 : "Invalid project.xml file."; // NOI18N
            }
            data.insertBefore(webservices, insertBefore.item(0));
        } else{
            webservices = (Element)nodes.item(0);
        }
        Element webservice = doc.createElementNS(EjbJarProjectType.PROJECT_CONFIGURATION_NAMESPACE, WEB_SERVICE); //NOI18N
        webservices.appendChild(webservice);
        Element webserviceName = doc.createElementNS(EjbJarProjectType.PROJECT_CONFIGURATION_NAMESPACE, WEB_SERVICE_NAME); //NOI18N
        webservice.appendChild(webserviceName);
        webserviceName.appendChild(doc.createTextNode(serviceName));
        if(fromWSDL) {
            Element fromWSDLElem = doc.createElementNS(EjbJarProjectType.PROJECT_CONFIGURATION_NAMESPACE, "from-wsdl");
            webservice.appendChild(fromWSDLElem);
        }
        helper.putPrimaryConfigurationData(data, true);
        
        // Update wscompile related properties.  boolean return indicates whether
        // any changes were made.
        updateWsCompileProperties(serviceName);
        
        try {
            ProjectManager.getDefault().saveProject(project);
        }catch(java.io.IOException ioe){
            ErrorManager.getDefault().notify(ioe);
        }
    }
    
    public  void addServiceEntriesToDD(String serviceName, String serviceEndpointInterface, String servantClassName) {
        //add service endpoint entry to ejb-jar.xml
        DDProvider provider = DDProvider.getDefault();
        org.netbeans.modules.j2ee.api.ejbjar.EjbJar ejbJarModule = org.netbeans.modules.j2ee.api.ejbjar.EjbJar.getEjbJars(project)[0];
        org.netbeans.modules.j2ee.dd.api.ejb.EjbJar ejbJar = null;
        try {
            ejbJar = provider.getDDRoot(ejbJarModule.getDeploymentDescriptor());
        } catch(java.io.IOException e) {
            ErrorManager.getDefault().notify(e);
        }
        
        if (ejbJar == null) {
            Logger.getLogger("global").log(Level.SEVERE, NbBundle.getMessage(EjbJarWebServicesSupport.class, "MSG_MissingMetadata"));
            return;
        }
        
        EjbJarProvider pwm = project.getLookup().lookup(EjbJarProvider.class);
        pwm.getConfigSupport().ensureConfigurationReady();
        EnterpriseBeans beans = ejbJar.getEnterpriseBeans();
        Session s = null;
        if(beans == null) {
            beans = ejbJar.newEnterpriseBeans();
            ejbJar.setEnterpriseBeans(beans);
        }
        s = beans.newSession();
        s.setEjbName(serviceName);
        s.setDisplayName(serviceName + "SB"); // NOI18N
        s.setEjbClass(servantClassName);
        try {
            s.setServiceEndpoint(serviceEndpointInterface);
        } catch(org.netbeans.modules.j2ee.dd.api.common.VersionNotSupportedException e) {
            ErrorManager.getDefault().notify(e);
        }
        s.setSessionType("Stateless"); // NOI18N
        s.setTransactionType("Container"); // NOI18N
        beans.addSession(s);
        try {
            // This also saves server specific configuration, if necessary.
            ejbJar.write(ejbJarModule.getDeploymentDescriptor());
        } catch(java.io.IOException e) {
            ErrorManager.getDefault().notify(e);
        }
    }
    
    public void addServiceImplLinkEntry(ServiceImplBean serviceImplBean, String wsName) {
        serviceImplBean.setEjbLink(wsName);
    }
    
    /**
     * Get the webservices.xml file object
     */
    public FileObject getWebservicesDD() {
        FileObject metaInfFo = getMetaInf();
        if (metaInfFo==null) {
            return null;
        }
        return getMetaInf().getFileObject(WEBSERVICES_DD, "xml");
    }
    
    /**
     *  Returns the directory that contains webservices.xml in the project
     */
    public FileObject getWsDDFolder() {
        return getMetaInf();
    }
    
    /**
     * Returns the name of the directory that contains the webservices.xml in
     * the archive
     */
    public String getArchiveDDFolderName() {
        return "META-INF"; // NOI18N
    }
    
    /**
     * Returns the name of the implementation bean class
     * given the ejb-link name
     */
    public String getImplementationBean(String linkName) {
        EjbJar ejbJar = getEjbJar();
        EnterpriseBeans beans = ejbJar.getEnterpriseBeans();
        Session[] sessionBeans = beans.getSession();
        for(int i = 0; i < sessionBeans.length; i++) {
            Session sessionBean = sessionBeans[i];
            if(sessionBean.getEjbName().equals(linkName)) {
                return sessionBean.getEjbClass();
            }
            
        }
        return null;
    }
    
    public boolean isFromWSDL(String serviceName) {
        Element data = helper.getPrimaryConfigurationData(true);
        NodeList nodes = data.getElementsByTagNameNS(EjbJarProjectType.PROJECT_CONFIGURATION_NAMESPACE,
                WEB_SERVICES); //NOI18N
        Element webservices = null;
        Element wsNameNode = null;
        if(nodes.getLength() == 1){
            webservices = (Element)nodes.item(0);
            NodeList wsNodes = webservices.getElementsByTagNameNS(EjbJarProjectType.PROJECT_CONFIGURATION_NAMESPACE,
                    WEB_SERVICE); //NOI18N
            for(int j = 0; j < wsNodes.getLength(); j++) {
                Element wsNode = (Element)wsNodes.item(j);
                NodeList wsNameNodes = wsNode.getElementsByTagNameNS(EjbJarProjectType.PROJECT_CONFIGURATION_NAMESPACE,
                        WEB_SERVICE_NAME); //NOI18N
                if(wsNameNodes.getLength() == 1) {
                    wsNameNode = (Element)wsNameNodes.item(0);
                    NodeList nl = wsNameNode.getChildNodes();
                    if(nl.getLength() == 1) {
                        Node n = nl.item(0);
                        if(n.getNodeType() == Node.TEXT_NODE) {
                            if(serviceName.equals(n.getNodeValue())) {
                                NodeList fromWSDLNodes = wsNode.getElementsByTagNameNS(EjbJarProjectType.PROJECT_CONFIGURATION_NAMESPACE,
                                        WebServicesConstants.WEB_SERVICE_FROM_WSDL); //NOI18N
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
        NodeList nodes = data.getElementsByTagNameNS(EjbJarProjectType.PROJECT_CONFIGURATION_NAMESPACE,
                WEB_SERVICES); //NOI18N
        Element webservices = null;
        Element wsNameNode = null;
        if(nodes.getLength() == 1){
            webservices = (Element)nodes.item(0);
            NodeList wsNodes = webservices.getElementsByTagNameNS(EjbJarProjectType.PROJECT_CONFIGURATION_NAMESPACE,
                    WEB_SERVICE); //NOI18N
            for(int j = 0; j < wsNodes.getLength(); j++) {
                Element wsNode = (Element)wsNodes.item(j);
                NodeList wsNameNodes = wsNode.getElementsByTagNameNS(EjbJarProjectType.PROJECT_CONFIGURATION_NAMESPACE,
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
            } catch(java.io.IOException ex) {
                String mes = NbBundle.getMessage(this.getClass(), "MSG_ErrorSavingOnWSRemove") + serviceName // NOI18N
                        + "'\r\n" + ex.getMessage(); // NOI18N
                NotifyDescriptor desc = new NotifyDescriptor.
                        Message(mes, NotifyDescriptor.Message.ERROR_MESSAGE);
                DialogDisplayer.getDefault().notify(desc);			}
        }
    }
    
    public void removeServiceEntry(String linkName) {
        //remove ejb  entry in ejb-jar.xml
        org.netbeans.modules.j2ee.api.ejbjar.EjbJar ejbJarModule = org.netbeans.modules.j2ee.api.ejbjar.EjbJar.getEjbJars(project)[0];
        EjbJar ejbJar = getEjbJar();
        EnterpriseBeans beans = ejbJar.getEnterpriseBeans();
        Session[] sessionBeans = beans.getSession();
        for(int i = 0; i < sessionBeans.length; i++) {
            Session sessionBean = sessionBeans[i];
            if(sessionBean.getEjbName().equals(linkName)) {
                EjbJarProvider pwm = project.getLookup().lookup(EjbJarProvider.class);
                pwm.getConfigSupport().ensureConfigurationReady();
                beans.removeSession(sessionBean);
                break;
            }
        }
        try {
            ejbJar.write(ejbJarModule.getDeploymentDescriptor());
        } catch(java.io.IOException e) {
            NotifyDescriptor ndd =
                    new NotifyDescriptor.Message(NbBundle.getMessage(this.getClass(), "MSG_Unable_WRITE_EJB_DD"), // NOI18N
                    NotifyDescriptor.ERROR_MESSAGE);
            DialogDisplayer.getDefault().notify(ndd);
        }
    }
    
    public AntProjectHelper getAntProjectHelper() {
        return helper;
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
        "datahandleronly",
        "explicitcontext",
        "nodatabinding",
        "novalidation",
        "searchschema",
        "strict",
        "wsi",
        "unwrap",
        "donotoverride",
        "donotunwrap"
    };
    
    private static final List<String> importantWsdlServiceFeatures = Arrays.asList(WSCOMPILE_KEY_WSDL_SERVICE_FEATURES);
    
    public List<WsCompileEditorSupport.ServiceSettings> getServices() {
        List<WsCompileEditorSupport.ServiceSettings> serviceList = new ArrayList<WsCompileEditorSupport.ServiceSettings>();
        
        // Implementation from getServiceClients() -- FIXME
        Element data = helper.getPrimaryConfigurationData(true);
        NodeList nodes = data.getElementsByTagName(WebServicesConstants.WEB_SERVICES);
        EditableProperties projectProperties = helper.getProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH);
        
        if(nodes.getLength() != 0) {
            Element serviceElements = (Element) nodes.item(0);
            NodeList serviceNameList = serviceElements.getElementsByTagNameNS(
                    EjbJarProjectType.PROJECT_CONFIGURATION_NAMESPACE, WebServicesConstants.WEB_SERVICE_NAME);
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
                        } else {
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
                    EjbJarProjectType.PROJECT_CONFIGURATION_NAMESPACE, WebServicesConstants.WEB_SERVICE_FROM_WSDL);
            if(fromWsdlList.getLength() == 1) {
                result = wsdlServiceStub;
            } else {
                result = seiServiceStub;
            }
        }
        
        return result;
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
            globalProperties.setProperty(WSCOMPILE_TOOLS_CLASSPATH, "${java.home}\\..\\lib\\tools.jar");
            
            try {
                PropertyUtils.putGlobalProperties(globalProperties);
            } catch(java.io.IOException ex) {
                String mes = "Error saving global properties when adding wscompile.tools.classpath for service '" + serviceName + "'\r\n" + ex.getMessage();
                NotifyDescriptor desc = new NotifyDescriptor.Message(mes, NotifyDescriptor.Message.ERROR_MESSAGE);
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
                String[] libs = PropertyUtils.tokenizePath(wscClientClasspath);
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
                for(Iterator<String> iter = wscJars.iterator(); iter.hasNext(); ) {
                    newClasspathBuf.append(iter.next().toString());
                    if(iter.hasNext()) {
                        newClasspathBuf.append(":");
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
    
    public void addInfrastructure(String implBeanClass, FileObject pkg){
        FileObject implClassFo = pkg.getFileObject(implBeanClass, "java");
        if(implClassFo == null) {
            return;
        }
        JavaSource targetSource = JavaSource.forFileObject(implClassFo);
        if (targetSource == null) {
            return;
        }
        final CancellableTask<WorkingCopy> modificationTask = new CancellableTask<WorkingCopy>() {
            public void run(WorkingCopy workingCopy) throws IOException {
                workingCopy.toPhase(Phase.RESOLVED);
                TreeMaker make = workingCopy.getTreeMaker();
                ClassTree javaClass = SourceUtils.getPublicTopLevelTree(workingCopy);
                IdentifierTree id = make.Identifier("javax.ejb.SessionBean");
                ClassTree modifiedClass = make.addClassImplementsClause(javaClass, id);
                
                List<? extends Tree> implClauses = javaClass.getImplementsClause();
                for(Tree implClause: implClauses){
                    if(implClause.getKind() == Kind.MEMBER_SELECT){
                        if (((MemberSelectTree)implClause).getIdentifier().contentEquals("Remote") ){
                            modifiedClass = make.removeClassImplementsClause(modifiedClass, implClause);
                            break;
                        }
                    }
                }
                
                VariableTree var = make.Variable(make.Modifiers(Collections.<Modifier>singleton(Modifier.PRIVATE),
                        Collections.<AnnotationTree>emptyList()), "context", make.Identifier("javax.ejb.SessionContext"), null);
                modifiedClass = make.insertClassMember(modifiedClass, 0, var);
                
                ModifiersTree modifiersTree = make.Modifiers(
                        Collections.<Modifier>singleton(Modifier.PUBLIC),
                        Collections.<AnnotationTree>emptyList()
                        );
                List<VariableTree> params = new ArrayList<VariableTree>();
                params.add(make.Variable(
                        make.Modifiers(
                        Collections.<Modifier>emptySet(),
                        Collections.<AnnotationTree>emptyList()
                        ),
                        "aContext",
                        make.Identifier("javax.ejb.SessionContext"), // parameter type
                        null // initializer - does not make sense in parameters.
                        ));
                MethodTree infMethod = make.Method(modifiersTree,
                        "setSessionContext",
                        make.Identifier("void"),
                        Collections.<TypeParameterTree>emptyList(),
                        params,
                        Collections.<ExpressionTree>emptyList(),
                        "{context = aContext;}",
                        null
                        );
                modifiedClass = make.addClassMember(modifiedClass, infMethod);
                
                params.clear();
                infMethod = make.Method(modifiersTree,
                        "ejbActivate",
                        make.Identifier("void"),
                        Collections.<TypeParameterTree>emptyList(),
                        params,
                        Collections.<ExpressionTree>emptyList(),
                        "{}",
                        null);
                modifiedClass = make.addClassMember(modifiedClass, infMethod);
                
                infMethod = make.Method(modifiersTree,
                        "ejbPassivate",
                        make.Identifier("void"),
                        Collections.<TypeParameterTree>emptyList(),
                        params,
                        Collections.<ExpressionTree>emptyList(),
                        "{}",
                        null);
                modifiedClass = make.addClassMember(modifiedClass, infMethod);
                
                infMethod = make.Method(modifiersTree,
                        "ejbRemove",
                        make.Identifier("void"),
                        Collections.<TypeParameterTree>emptyList(),
                        params,
                        Collections.<ExpressionTree>emptyList(),
                        "{}",
                        null);
                modifiedClass = make.addClassMember(modifiedClass, infMethod);
                
                infMethod = make.Method(modifiersTree,
                        "ejbCreate",
                        make.Identifier("void"),
                        Collections.<TypeParameterTree>emptyList(),
                        params,
                        Collections.<ExpressionTree>emptyList(),
                        "{}",
                        null);
                modifiedClass = make.addClassMember(modifiedClass, infMethod);
                workingCopy.rewrite(javaClass, modifiedClass);
            }
            public void cancel() {}
        };
        
        try{
            targetSource.runModificationTask(modificationTask).commit();
        }catch(IOException e){
            ErrorManager.getDefault().notify(e);
        }
        //TODO: RETOUCHE webservices
        //        boolean rollbackFlag = true; // rollback the transaction by default
        //        JavaModel.getJavaRepository().beginTrans(true);
        //        try {
        //            JavaModel.setClassPath(pkg);
        //            JavaMetamodel.getManager().waitScanFinished();
        //            JavaClass clazz = Utils.findClass(implBeanClass);
        //
        //            if (clazz == null) {
        //                ErrorManager.getDefault().log(ErrorManager.INFORMATIONAL,
        //                        "EjbJarWSSupport.addInfrastructure: Class not found: " + implBeanClass + " for package: " + pkg); //NOI18N
        //                return;
        //            }
        //
        //            //remove java.rmi.Remote interface
        //            List interfaces = clazz.getInterfaceNames();
        //            for (Iterator it = interfaces.iterator(); it.hasNext();) {
        //               MultipartId interfaceId = (MultipartId) it.next();
        //               if (interfaceId.getElement().getName().equals("java.rmi.Remote")) {
        //                   interfaces.remove(interfaceId);
        //                   break;
        //               }
        //            }
        //
        //            MultipartId id = JavaModelUtil.resolveImportsForClass(clazz, Utils.findClass("javax.ejb.SessionBean"));
        //            if (id!=null) interfaces.add(id);
        //
        //            JavaModelPackage jmp = (JavaModelPackage) clazz.refImmediatePackage();
        //
        //            //add javax.ejb.SessionContext field
        //            JavaClass sessionCtx = Utils.findClass("javax.ejb.SessionContext");
        //            Field field = jmp.getField().createField();
        //            field.setType(sessionCtx);
        //            field.setName("context");
        //            clazz.getContents().add(0,field);
        //
        //            //add setSessionContext(javax.ejb.SessionContext aContext) method
        //            Method sessionCtxMethod = jmp.getMethod().createMethod();
        //            sessionCtxMethod.setName("setSessionContext");
        //            Parameter ctxParam = jmp.getParameter().createParameter(
        //                    "aContext",
        //                    Collections.EMPTY_LIST,
        //                    false,
        //                    jmp.getMultipartId().createMultipartId(sessionCtx.getName(), null, null), // type name
        //                    0,
        //                    false);
        //            sessionCtxMethod.getParameters().add(ctxParam);
        //            sessionCtxMethod.setType(Utils.resolveType("void"));
        //            sessionCtxMethod.setModifiers(Modifier.PUBLIC);
        //            sessionCtxMethod.setBodyText("context = aContext;");
        //            sessionCtxMethod.setJavadocText("@see javax.ejb.SessionBean#setSessionContext(javax.ejb.SessionContext)");
        //            clazz.getContents().add(sessionCtxMethod);
        //
        //            //add ejbActivate method
        //            Method ejbActivateMethod = jmp.getMethod().createMethod();
        //            ejbActivateMethod.setName("ejbActivate");
        //            ejbActivateMethod.setType(Utils.resolveType("void"));
        //            ejbActivateMethod.setModifiers(Modifier.PUBLIC);
        //            ejbActivateMethod.setJavadocText("@see javax.ejb.SessionBean#ejbActivate()");
        //            clazz.getContents().add(ejbActivateMethod);
        //
        //            //add ejbPassivate method
        //            Method ejbPassivateMethod = jmp.getMethod().createMethod();
        //            ejbPassivateMethod.setName("ejbPassivate");
        //            ejbPassivateMethod.setType(Utils.resolveType("void"));
        //            ejbPassivateMethod.setModifiers(Modifier.PUBLIC);
        //            ejbPassivateMethod.setJavadocText("@see javax.ejb.SessionBean#ejbPassivate()");
        //            clazz.getContents().add(ejbPassivateMethod);
        //
        //            //add ejbRemove method
        //            Method ejbRemoveMethod = jmp.getMethod().createMethod();
        //            ejbRemoveMethod.setName("ejbRemove");
        //            ejbRemoveMethod.setType(Utils.resolveType("void"));
        //            ejbRemoveMethod.setModifiers(Modifier.PUBLIC);
        //            ejbRemoveMethod.setJavadocText("@see javax.ejb.SessionBean#ejbRemove()");
        //            clazz.getContents().add(ejbRemoveMethod);
        //
        //            //add ejbCreate method
        //            Method ejbCreateMethod = jmp.getMethod().createMethod();
        //            ejbCreateMethod.setName("ejbCreate");
        //            ejbCreateMethod.setType(Utils.resolveType("void"));
        //            ejbCreateMethod.setModifiers(Modifier.PUBLIC);
        //            ejbCreateMethod.setJavadocText("See section 7.10.3 of the EJB 2.0 specification\nSee section 7.11.3 of the EJB 2.1 specification");
        //            clazz.getContents().add(ejbCreateMethod);
        //
        //            rollbackFlag=false;
        //        } finally {
        //            JavaModel.getJavaRepository().endTrans(rollbackFlag);
        //        }
    }
    
    private EjbJar getEjbJar() {
        try {
            // TODO: first one API EjbJar from project is taken... this should be fixed
            return DDProvider.getDefault().getDDRoot(org.netbeans.modules.j2ee.api.ejbjar.EjbJar.getEjbJars(project)[0].getDeploymentDescriptor());
        } catch (java.io.IOException e) {
            Logger.getLogger("global").log(Level.INFO, e.getLocalizedMessage());
        }
        return null;
    }
    
    public FileObject getMetaInf() {
        EjbJarProvider provider = project.getLookup().lookup(EjbJarProvider.class);
        return provider.getMetaInf();
    }
    
    public FileObject getDeploymentDescriptor() {
        FileObject metaInfFo = getMetaInf();
        if (metaInfFo==null) {
            return null;
        }
        return metaInfFo.getFileObject(EjbJarProvider.FILE_DD);
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
    
    private FileObject getFileObject(String propname) {
        String prop = helper.getStandardPropertyEvaluator().getProperty(propname);
        if (prop != null) {
            return helper.resolveFileObject(prop);
        }
        
        return null;
    }
    
    public FileObject getWsdlFolder(boolean create) throws IOException {
        FileObject wsdlFolder = null;
        FileObject metaInf = getMetaInf();
        
        if(metaInf != null) {
            wsdlFolder = metaInf.getFileObject(WSDL_FOLDER);
            if(wsdlFolder == null && create) {
                wsdlFolder = metaInf.createFolder(WSDL_FOLDER);
            }
        } else if(create) {
            // Create was specified, but no META-INF was found, so how do we create it?
            // Expect an NPE if we return null for this case, but log it anyway.
            Logger.getLogger("global").log(Level.INFO, NbBundle.getMessage(EjbJarWebServicesSupport.class, "MSG_MetaInfNotFoundForWsdlFolder"));
        }
        
        return wsdlFolder;
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
                    FileUtil.toFile(project.getProjectDirectory()), project.evaluator(), new String[] {EjbJarProjectProperties.J2EE_PLATFORM_CLASSPATH }));
        }
        return platformClassPath;
    }
    
    private ClassPath platformClassPath = null;
        
    // Service stub descriptors
    private static final JAXRPCStubDescriptor seiServiceStub = new JAXRPCStubDescriptor(
            StubDescriptor.SEI_SERVICE_STUB,
            NbBundle.getMessage(EjbJarWebServicesSupport.class,"LBL_SEIServiceStub"), // NOI18N
            new String [] {"documentliteral", "strict", "useonewayoperations"});
    
    private static final JAXRPCStubDescriptor wsdlServiceStub = new JAXRPCStubDescriptor(
            StubDescriptor.WSDL_SERVICE_STUB,
            NbBundle.getMessage(EjbJarWebServicesSupport.class,"LBL_WSDLServiceStub"), // NOI18N
            new String [] { "wsi", "strict" }); // NOI18N
    
    /** Stub descriptor for services supported by this project type.
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
                    buf.append(",");
                }
                
                buf.append(defaultFeatures[i]);
            }
            return buf.toString();
        }
    }
}
