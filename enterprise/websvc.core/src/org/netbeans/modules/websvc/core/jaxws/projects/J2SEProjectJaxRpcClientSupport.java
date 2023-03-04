/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.netbeans.modules.websvc.core.jaxws.projects;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.netbeans.modules.websvc.api.client.ClientStubDescriptor;
import org.netbeans.modules.websvc.api.client.WebServicesClientConstants;
import org.netbeans.modules.websvc.api.client.WsCompileClientEditorSupport;
import org.netbeans.modules.websvc.api.jaxws.project.WSUtils;
import org.netbeans.modules.websvc.spi.client.WebServicesClientSupportImpl;
import java.io.IOException;
import java.util.Arrays;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.api.project.ui.OpenProjects;
import org.netbeans.spi.project.AuxiliaryConfiguration;
import org.netbeans.spi.project.support.ant.AntProjectHelper;
import org.netbeans.spi.project.support.ant.EditableProperties;
import org.netbeans.spi.project.support.ant.PropertyUtils;
import org.openide.DialogDisplayer;
import org.openide.ErrorManager;
import org.openide.NotifyDescriptor;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Mutex.Action;
import org.openide.util.NbBundle;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.w3c.dom.Node;


/**
 *
 * @author  rico
 * Implementation of WebServicesSupportImpl and WebServicesClientSupportImpl
 */
public class J2SEProjectJaxRpcClientSupport implements WebServicesClientSupportImpl{

    private static final String JAX_RPC_NAMESPACE="http://www.netbeans.org/ns/j2se-project/jax-rpc"; //NOI18N
    private Project project;
    private String proxyHost,proxyPort;

    public static final String WSDL_FOLDER = "wsdl"; //NOI18N
            
    /** Creates a new instance of J2SEProjectWebServicesSupport */
    public J2SEProjectJaxRpcClientSupport(Project project) {
        this.project = project;
    }

    // Implementation of WebServiceClientSupportImpl
    public void addServiceClient(String serviceName, String packageName, String sourceUrl, FileObject configFile, ClientStubDescriptor stubDescriptor) {
        this.addServiceClient(serviceName, packageName, sourceUrl, configFile, stubDescriptor, null);
    }
    
    // Implementation of WebServiceClientSupportImpl
    public void addServiceClient(final String serviceName, final String packageName, final String sourceUrl, final FileObject configFile, final ClientStubDescriptor stubDescriptor, final String[] wscompileFeatures) {
        // It seems like it ought to be implemented via the AuxiliaryConfiguration interface.
        boolean needToSave = ProjectManager.mutex().writeAccess(new Action<Boolean>() {
            public Boolean run() {
                boolean needsSave = false;
                boolean modifiedProjectProperties = false;
                boolean modifiedPrivateProperties = false;

                /** Locate root of web service client node structure in project,xml, creating it
                 *  if it's not found.
                 */
                //Element data = helper.getPrimaryConfigurationData(true);
                AuxiliaryConfiguration aux = ProjectUtils.getAuxiliaryConfiguration(project);
                Element clientElements = aux.getConfigurationFragment(WebServicesClientConstants.WEB_SERVICE_CLIENTS,
                                            JAX_RPC_NAMESPACE, true);
                if (clientElements==null) {
                    Document doc = createNewDocument();
                    Element root = doc.createElementNS(JAX_RPC_NAMESPACE, WebServicesClientConstants.WEB_SERVICE_CLIENTS);
                    aux.putConfigurationFragment(root, true);
                    clientElements = aux.getConfigurationFragment(WebServicesClientConstants.WEB_SERVICE_CLIENTS,
                                            JAX_RPC_NAMESPACE, true);
                }
                Document doc = clientElements.getOwnerDocument();

                /** Make sure this service is not already registered in project.xml
                 */
                boolean serviceAlreadyAdded = false;
                NodeList clientNameList = clientElements.getElementsByTagNameNS(JAX_RPC_NAMESPACE, WebServicesClientConstants.WEB_SERVICE_CLIENT_NAME);
                for(int i = 0; i < clientNameList.getLength(); i++ ) {
                    Element clientNameElement = (Element) clientNameList.item(i);
                    NodeList nl = clientNameElement.getChildNodes();
                    if(nl.getLength() >= 1) {
                        org.w3c.dom.Node n = nl.item(0);
                        if(Node.TEXT_NODE == n.getNodeType() ) {
                            if(serviceName.equalsIgnoreCase(n.getNodeValue())) {
                                serviceAlreadyAdded = true;

                                // !PW FIXME should force stub type to match value passed in
                                // in case someone is overwriting a current service with a different
                                // stub type.
                            }
                        }
                    }
                }

                /** Add entry for the client to project.xml and regenerate build-impl.xml.
                 */
                if(!serviceAlreadyAdded) {
                    Element clientElement = doc.createElementNS(JAX_RPC_NAMESPACE, WebServicesClientConstants.WEB_SERVICE_CLIENT);
                    clientElements.appendChild(clientElement);
                    Element clientElementName = doc.createElementNS(JAX_RPC_NAMESPACE, WebServicesClientConstants.WEB_SERVICE_CLIENT_NAME);
                    clientElement.appendChild(clientElementName);
                    clientElementName.appendChild(doc.createTextNode(serviceName));
                    Element clientElementStubType = doc.createElementNS(JAX_RPC_NAMESPACE, WebServicesClientConstants.WEB_SERVICE_STUB_TYPE);
                    clientElement.appendChild(clientElementStubType);
                    clientElementStubType.appendChild(doc.createTextNode(stubDescriptor.getName()));
                    Element clientElementSourceUrl = doc.createElementNS(JAX_RPC_NAMESPACE, WebServicesClientConstants.CLIENT_SOURCE_URL);
                    clientElement.appendChild(clientElementSourceUrl);
                    clientElementSourceUrl.appendChild(doc.createTextNode(sourceUrl));
                    aux.putConfigurationFragment(clientElements, true);
                    //helper.putPrimaryConfigurationData(data, true);
                    needsSave = true;
                }
                EditableProperties projectProperties = null;
                EditableProperties privateProperties = null;
                try {
                    projectProperties = WSUtils.getEditableProperties(project, AntProjectHelper.PROJECT_PROPERTIES_PATH);
                    privateProperties = WSUtils.getEditableProperties(project, AntProjectHelper.PRIVATE_PROPERTIES_PATH);
                } catch (IOException ex) {

                }
                assert projectProperties!=null && privateProperties!=null;
                // Add property for wscompile features
                {
                    String featurePropertyName = "wscompile.client." + serviceName + ".features"; // NOI18N
                    String defaultFeatures = "wsi, strict"; // NOI18N -- defaults if stub descriptor is bad type (should never happen?)
                    if(stubDescriptor instanceof JAXRPCClientStubDescriptor) {
                        JAXRPCClientStubDescriptor stubDesc = (JAXRPCClientStubDescriptor) stubDescriptor;
                        if (wscompileFeatures!=null) stubDesc.setDefaultFeatures(wscompileFeatures);
                        defaultFeatures = stubDesc.getDefaultFeaturesAsArgument();
                    } else {
                        // !PW FIXME wrong stub type -- log error message.
                    }
                    String oldFeatures = projectProperties.getProperty(featurePropertyName);
                    if(!defaultFeatures.equals(oldFeatures)) {
                        projectProperties.put(featurePropertyName, defaultFeatures);
                        modifiedProjectProperties = true;
                    }
                }

                // Add package name property
                {
                    String packagePropertyName = "wscompile.client." + serviceName + ".package"; // NOI18N
                    String oldPackageName = projectProperties.getProperty(packagePropertyName);
                    if(!packageName.equals(oldPackageName)) {
                        projectProperties.put(packagePropertyName, packageName);
                        modifiedProjectProperties = true;
                    }
                }

                // Add http.proxyHost, http.proxyPort and http.nonProxyHosts JVM options
                // create wscompile:httpproxy property
                if (proxyHost!=null && proxyHost.length()>0) {
                    boolean modif = addJVMProxyOptions(projectProperties,proxyHost,proxyPort);
                    if (modif) modifiedProjectProperties = true;
                    String proxyProperty = "wscompile.client." + serviceName + ".proxy"; // NOI18N
                    String oldProxyProperty = privateProperties.getProperty(proxyProperty);
                    if(!proxyProperty.equals(oldProxyProperty)) {
                        privateProperties.put(proxyProperty, proxyHost+":"+(proxyPort==null?"8080":proxyPort)); //NOI18N
                        modifiedPrivateProperties = true;
                    }
                }

                if(modifiedProjectProperties) {
                    try {
                        WSUtils.storeEditableProperties(project, AntProjectHelper.PROJECT_PROPERTIES_PATH, projectProperties);
                        needsSave = true;
                    } catch (IOException ex) {
                    }
                }
                if(modifiedPrivateProperties) {
                    //helper.putProperties(AntProjectHelper.PRIVATE_PROPERTIES_PATH, privateProperties);
                    try {
                        WSUtils.storeEditableProperties(project, AntProjectHelper.PRIVATE_PROPERTIES_PATH, privateProperties);
                        needsSave = true;
                    } catch (IOException ex) {
                    }

                }

                // Update wscompile related properties.  boolean return indicates whether
                // any changes were made.
                if(updateWsCompileProperties(serviceName)) {
                    needsSave = true;
                }
                return needsSave;
            }
        });
        
        // !PW Lastly, save the project if we actually made any changes to any
        // properties or the build script.
        if(needToSave) {
            try {
                ProjectManager.getDefault().saveProject(project);
            } catch(IOException ex) {
                NotifyDescriptor desc = new NotifyDescriptor.Message(
                NbBundle.getMessage(J2SEProjectJaxRpcClientSupport.class,"MSG_ErrorSavingOnWSClientAdd", serviceName, ex.getMessage()), // NOI18N
                NotifyDescriptor.ERROR_MESSAGE);
                DialogDisplayer.getDefault().notify(desc);
            }
        }
    }
    
    public void addInfrastructure(String implBeanClass, FileObject pkg) {
        //nothing to do here, there are no infrastructure elements
    }
    
    public FileObject getDeploymentDescriptor() {
        return null;
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
        if(globalProperties.getProperty(WebServicesClientConstants.WSCOMPILE_TOOLS_CLASSPATH) == null) {
            globalProperties.setProperty(WebServicesClientConstants.WSCOMPILE_TOOLS_CLASSPATH, "${java.home}\\..\\lib\\tools.jar"); // NOI18N
            
            try {
                PropertyUtils.putGlobalProperties(globalProperties);
            } catch(IOException ex) {
                NotifyDescriptor desc = new NotifyDescriptor.Message(
                NbBundle.getMessage(J2SEProjectJaxRpcClientSupport.class,"MSG_ErrorSavingGlobalProperties", serviceName, ex.getMessage()), // NOI18N
                NotifyDescriptor.ERROR_MESSAGE);
                DialogDisplayer.getDefault().notify(desc);
            }
            
            globalPropertiesChanged = true;
        }
        
        
        boolean projectPropertiesChanged = false;
        //EditableProperties projectProperties = helper.getProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH);
        EditableProperties projectProperties = null;
        try {
            projectProperties = WSUtils.getEditableProperties(project, AntProjectHelper.PROJECT_PROPERTIES_PATH);
        } catch (IOException ex) {
            
        }        
        { // Block that adjusts wscompile.client.classpath as necessary.
            //HashSet wscJars = new HashSet();
            boolean newWscJars = false;
            String wscClientClasspath = projectProperties.getProperty(WebServicesClientConstants.WSCOMPILE_CLASSPATH);
            if (wscClientClasspath == null) {
                wscClientClasspath = "${" + WebServicesClientConstants.WSCOMPILE_TOOLS_CLASSPATH + "}" + ":${javac.classpath}";
                projectProperties.put(WebServicesClientConstants.WSCOMPILE_CLASSPATH, wscClientClasspath);
                projectPropertiesChanged = true;
            }
            
//            for(int i = 0; i < WSCOMPILE_JARS.length; i++) {
//                if(!wscJars.contains(WSCOMPILE_JARS[i])) {
//                    wscJars.add(WSCOMPILE_JARS[i]);
//                    newWscJars = true;
//                }
//            }
            
//            if(newWscJars) {
//                StringBuffer newClasspathBuf = new StringBuffer(256);
//                for(Iterator iter = wscJars.iterator(); iter.hasNext(); ) {
//                    newClasspathBuf.append(iter.next().toString());
//                    if(iter.hasNext()) {
//                        newClasspathBuf.append(":");
//                    }
//                }
//                projectProperties.put(WSCOMPILE_CLASSPATH, newClasspathBuf.toString());
//                projectPropertiesChanged = true;
//            }
        }
        
        // set tools.jar property if not set
        if(projectProperties.getProperty(WebServicesClientConstants.WSCOMPILE_TOOLS_CLASSPATH) == null) {
            projectProperties.setProperty(WebServicesClientConstants.WSCOMPILE_TOOLS_CLASSPATH, "${java.home}\\..\\lib\\tools.jar"); // NOI18N
            projectPropertiesChanged = true;
        }
        
        if(projectPropertiesChanged) {
            //helper.putProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH, projectProperties);
            try {
                WSUtils.storeEditableProperties(project, AntProjectHelper.PROJECT_PROPERTIES_PATH, projectProperties);
            } catch (IOException ex) {
            }
        }
        
        return globalPropertiesChanged || projectPropertiesChanged;
    }
    
    public void removeServiceClient(final String serviceName) {
        // 2. Remove service from project.xml
        //    Side effect: Regenerate build-impl.xsl
        //    Optional - if last service, remove properties we generated.
        
        boolean needToSave = ProjectManager.mutex().writeAccess(new Action<Boolean>() {
            public Boolean run() {
                boolean needsSave = false;
                boolean needsSave1 = false;

                /** Remove properties from project.properties
                 */
                String featureProperty = "wscompile.client." + serviceName + ".features"; // NOI18N
                String packageProperty = "wscompile.client." + serviceName + ".package"; // NOI18N
                String proxyProperty = "wscompile.client." + serviceName + ".proxy"; //NOI18N

                EditableProperties ep = null;
                EditableProperties ep1 =  null;
                try {
                    ep = WSUtils.getEditableProperties(project, AntProjectHelper.PROJECT_PROPERTIES_PATH);
                    ep1 = WSUtils.getEditableProperties(project, AntProjectHelper.PRIVATE_PROPERTIES_PATH);
                } catch (IOException ex) {
                    ErrorManager.getDefault().notify(ex);
                }
                assert ep!=null && ep1!=null;

                if(ep.getProperty(featureProperty) != null) {
                    ep.remove(featureProperty);
                    needsSave = true;
                }

                if(ep.getProperty(packageProperty) != null) {
                    ep.remove(packageProperty);
                    needsSave = true;
                }

                if(ep1.getProperty(proxyProperty) != null) {
                    ep1.remove(proxyProperty);
                    needsSave1 = true;
                }

                if(needsSave) {
                    //helper.putProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH, ep);
                    try {
                        WSUtils.storeEditableProperties(project, AntProjectHelper.PROJECT_PROPERTIES_PATH, ep);
                    } catch (IOException ex) {
                        ErrorManager.getDefault().notify(ex);
                    }
                }

                if(needsSave1) {
                    //helper.putProperties(AntProjectHelper.PRIVATE_PROPERTIES_PATH, ep1);
                    try {
                        WSUtils.storeEditableProperties(project, AntProjectHelper.PRIVATE_PROPERTIES_PATH, ep1);
                    } catch (IOException ex) {
                        ErrorManager.getDefault().notify(ex);
                    }
                }

                /** Locate root of web service client node structure in project,xml
                 */
                AuxiliaryConfiguration aux = ProjectUtils.getAuxiliaryConfiguration(project);
                Element clientElements = aux.getConfigurationFragment(WebServicesClientConstants.WEB_SERVICE_CLIENTS,
                                            JAX_RPC_NAMESPACE, true);
                if (clientElements!=null) {
                    NodeList clientNameList = clientElements.getElementsByTagNameNS(JAX_RPC_NAMESPACE, WebServicesClientConstants.WEB_SERVICE_CLIENT_NAME);
                    for(int i = 0; i < clientNameList.getLength(); i++ ) {
                        Element clientNameElement = (Element) clientNameList.item(i);
                        NodeList nl = clientNameElement.getChildNodes();
                        if(nl.getLength() == 1) {
                            org.w3c.dom.Node n = nl.item(0);
                            if(n.getNodeType() == org.w3c.dom.Node.TEXT_NODE) {
                                if(serviceName.equalsIgnoreCase(n.getNodeValue())) {
                                    // Found it!  Now remove it.
                                    org.w3c.dom.Node clientNode = clientNameElement.getParentNode();
                                    clientElements.removeChild(clientNode);
                                    //helper.putPrimaryConfigurationData(data, true);
                                    aux.putConfigurationFragment(clientElements, true);
                                    needsSave = true;
                                }
                            }
                        }
                    }        
                }
                return needsSave || needsSave1;
            }
        });
      
        // !PW Lastly, save the project if we actually made any changes to any
        // properties or the build script.
        if(needToSave) {
            try {
                ProjectManager.getDefault().saveProject(project);
            } catch(IOException ex) {
                NotifyDescriptor desc = new NotifyDescriptor.Message(
                NbBundle.getMessage(J2SEProjectJaxRpcClientSupport.class,"MSG_ErrorSavingOnWSClientRemove", serviceName, ex.getMessage()), // NOI18N
                NotifyDescriptor.ERROR_MESSAGE);
                DialogDisplayer.getDefault().notify(desc);
            }
        }
    }
    
    public FileObject getWsdlFolder(boolean create) throws IOException {

        //EditableProperties ep =  helper.getProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH);
        EditableProperties ep = null;
        try {
            ep = WSUtils.getEditableProperties(project, AntProjectHelper.PROJECT_PROPERTIES_PATH);
        } catch (IOException ex) {
            
        }
        
        String srcDir = "src"; //NOI18N
        if (ep != null) {
            String srcDirProp = ep.getProperty("src.dir"); //NOI18N
            if (srcDirProp != null) {
                srcDir = srcDirProp;
            }
        }
        
        String wsdlFolderStr = srcDir + "/META-INF/" + WSDL_FOLDER; // NOI18N
        FileObject wsdlFolder = project.getProjectDirectory().getFileObject(wsdlFolderStr);
        if (wsdlFolder == null && create) {
            wsdlFolder = FileUtil.createFolder(project.getProjectDirectory(), wsdlFolderStr);
        }
        return wsdlFolder;
    }
    
    public List<ClientStubDescriptor> getStubDescriptors() {
        List<ClientStubDescriptor> stubs = new ArrayList<ClientStubDescriptor>(2);
        stubs.add(jaxrpcClientStub);
        return stubs;
    }
    
    private boolean isProjectOpened() {
        Project[] projects = OpenProjects.getDefault().getOpenProjects();
        for (int i = 0; i < projects.length; i++) {
            if (projects[i].equals(project))
                return true;
        }
        return false;
    }
    
    /** !PW This method is exposed in the client support API.  Though it's
     *  implementation makes more sense here than anywhere else, perhaps this
     *  and the other project.xml/project.properties related methods in this
     *  object should be refactored into another object that this one delegates
     *  to.  That way, this method would be directly available within the web
     *  web module, as it is needed, and remain missing from the API (where it
     *  probably does not belong at this time.
     */
    private static final String [] WSCOMPILE_CLIENT_FEATURES = {
        "datahandleronly", // - portable
        //        "documentliteral", // SEI ONLY
        //        "rpcliteral", // SEI ONLY
        "explicitcontext",
        //        "infix:<name>", // difficult to implement.
        "jaxbenumtype",
        "nodatabinding", //  - portable
        "noencodedtypes",
        "nomultirefs",
        "norpcstructures", //  - portable
        "novalidation", //  - portable
        "resolveidref",
        "searchschema", //  - portable
        "serializeinterfaces",
        "strict", //  - portable
        //        "useonewayoperations", // SEI ONLY
        "wsi", // - portable
        "unwrap",// - portable
        "donotoverride", // - portable
        "donotunwrap", // - portable
    };
    
    private static final List allClientFeatures = Arrays.asList(WSCOMPILE_CLIENT_FEATURES);
    
    private static final String [] WSCOMPILE_KEY_CLIENT_FEATURES = {
        "wsi",
        "strict",
        "norpcstructures",
        "unwrap",
        "donotunwrap",
        "donotoverride",
        "datahandleronly",
        "nodatabinding",
        "novalidation",
        "searchschema",
    };
    
    private static final List importantClientFeatures = Arrays.asList(WSCOMPILE_KEY_CLIENT_FEATURES);
    
        public List getServiceClients() {
        
        List<WsCompileClientEditorSupport.ServiceSettings> serviceNames = new ArrayList<WsCompileClientEditorSupport.ServiceSettings>();
        AuxiliaryConfiguration aux = ProjectUtils.getAuxiliaryConfiguration(project);
        Element clientElements = aux.getConfigurationFragment(WebServicesClientConstants.WEB_SERVICE_CLIENTS,
                                    JAX_RPC_NAMESPACE, true);
        if (clientElements!=null) {
            NodeList clientNameList = clientElements.getElementsByTagNameNS(
                    JAX_RPC_NAMESPACE, WebServicesClientConstants.WEB_SERVICE_CLIENT_NAME);

            for(int i = 0; i < clientNameList.getLength(); i++ ) {
                Element clientNameElement = (Element) clientNameList.item(i);
                NodeList nl = clientNameElement.getChildNodes();
                if(nl.getLength() == 1) {
                    org.w3c.dom.Node n = nl.item(0);
                    if(n.getNodeType() == org.w3c.dom.Node.TEXT_NODE) {
                        EditableProperties projectProperties = null;
                        try {
                            projectProperties = WSUtils.getEditableProperties(project, AntProjectHelper.PROJECT_PROPERTIES_PATH);
                        } catch (IOException ex) {
                            
                        }
                        // this may happen when deleting client
                        if (projectProperties == null) continue;
                        
                        String serviceName = n.getNodeValue();
                        String currentFeatures = projectProperties.getProperty("wscompile.client." + serviceName + ".features"); //NOI18N
                        if(currentFeatures == null) {
                            // !PW should probably retrieve default features for stub type.
                            // For now, this will work because this is the same value we'd get doing that.
                            //
                            // Defaults if we can't find any feature property for this client
                            // Mostly for upgrading EA1, EA2 projects which did not have
                            // this property, but also useful if the user deletes it from
                            // project.properties.
                            currentFeatures = "wsi, strict";
                        }
                        ClientStubDescriptor stubType = getClientStubDescriptor(clientNameElement.getParentNode());
                        boolean propVerbose = "true".equalsIgnoreCase( //NOI18N
                                projectProperties.getProperty("wscompile.client." + serviceName + ".verbose")); //NOI18N
                        boolean propDebug = "true".equalsIgnoreCase( //NOI18N
                                projectProperties.getProperty("wscompile.client." + serviceName + ".debug")); //NOI18N                
                        boolean propPrintStackTrace = "true".equalsIgnoreCase( //NOI18N
                                projectProperties.getProperty("wscompile.client." + serviceName + ".xPrintStackTrace")); //NOI18N
                        boolean propExtensible = "true".equalsIgnoreCase( //NOI18N
                                projectProperties.getProperty("wscompile.client." + serviceName + ".xSerializable")); //NOI18N
                        boolean propOptimize = "true".equalsIgnoreCase( //NOI18N
                                projectProperties.getProperty("wscompile.client." + serviceName + ".optimize")); //NOI18N
                        boolean[] options = new boolean[] { //NOI18N
                            propVerbose,propDebug,propPrintStackTrace,propExtensible,propOptimize
                        };
                        WsCompileClientEditorSupport.ServiceSettings settings = new WsCompileClientEditorSupport.ServiceSettings(
                        serviceName, stubType, options, currentFeatures, allClientFeatures, importantClientFeatures);
                        serviceNames.add(settings);
                        
                        
                    } else {
                        // !PW FIXME node is wrong type?! - log message or trace?
                    }
                } else {
                    // !PW FIXME no name for this service entry - notify user
                }
            }      
        }
        
        return serviceNames;
    }
    
    private ClientStubDescriptor getClientStubDescriptor(org.w3c.dom.Node parentNode) {
        ClientStubDescriptor result = null;
        
        if(parentNode instanceof Element) {
            Element parentElement = (Element) parentNode;
            NodeList clientNameList = parentElement.getElementsByTagNameNS(
            JAX_RPC_NAMESPACE, WebServicesClientConstants.WEB_SERVICE_STUB_TYPE);
            if(clientNameList.getLength() == 1) {
                Element clientStubElement = (Element) clientNameList.item(0);
                NodeList nl = clientStubElement.getChildNodes();
                if(nl.getLength() == 1) {
                    org.w3c.dom.Node n = nl.item(0);
                    if(n.getNodeType() == org.w3c.dom.Node.TEXT_NODE) {
                        String stubName = n.getNodeValue();
                        if (ClientStubDescriptor.JAXRPC_CLIENT_STUB.equals(stubName)) {
                            result = jaxrpcClientStub;
                        }
                    }
                }
            }
        }
        
        return result;
    }
    
    public String getWsdlSource(String serviceName) {
//        Element data = helper.getPrimaryConfigurationData(true);
//        Document doc = data.getOwnerDocument();
        String wsdlSource = null;
//        
//        Element clientElement = getWebServiceClientNode(data, serviceName);
//        if(clientElement != null) {
//            NodeList fromWsdlList = clientElement.getElementsByTagNameNS(
//            J2SEProjectType.PROJECT_CONFIGURATION_NAMESPACE, WebServicesClientConstants.CLIENT_SOURCE_URL);
//            if(fromWsdlList.getLength() == 1) {
//                Element fromWsdlElement = (Element) fromWsdlList.item(0);
//                NodeList nl = fromWsdlElement.getChildNodes();
//                if(nl.getLength() == 1) {
//                    org.w3c.dom.Node n = nl.item(0);
//                    if(n.getNodeType() == org.w3c.dom.Node.TEXT_NODE) {
//                        wsdlSource = n.getNodeValue();
//                    }
//                }
//            }
//        }
        
        return wsdlSource;
    }
    
    public void setWsdlSource(String serviceName, String wsdlSource) {
//        Element data = helper.getPrimaryConfigurationData(true);
//        Document doc = data.getOwnerDocument();
//        boolean needsSave = false;
//        
//        Element clientElement = getWebServiceClientNode(data, serviceName);
//        if(clientElement != null) {
//            NodeList fromWsdlList = clientElement.getElementsByTagNameNS(
//            J2SEProjectType.PROJECT_CONFIGURATION_NAMESPACE, WebServicesClientConstants.CLIENT_SOURCE_URL);
//            if(fromWsdlList.getLength() > 0) {
//                Element fromWsdlElement = (Element) fromWsdlList.item(0);
//                NodeList nl = fromWsdlElement.getChildNodes();
//                if(nl.getLength() > 0) {
//                    org.w3c.dom.Node n = nl.item(0);
//                    n.setNodeValue(wsdlSource);
//                } else {
//                    fromWsdlElement.appendChild(doc.createTextNode(wsdlSource));
//                }
//            } else {
//                Element clientElementSourceUrl = doc.createElementNS(J2SEProjectType.PROJECT_CONFIGURATION_NAMESPACE, WebServicesClientConstants.CLIENT_SOURCE_URL);
//                clientElement.appendChild(clientElementSourceUrl);
//                clientElementSourceUrl.appendChild(doc.createTextNode(wsdlSource));
//            }
//            
//            needsSave = true;
//        }
//        
//        // !PW Save the project if we were able to make the change.
//        if(needsSave) {
//            try {
//                ProjectManager.getDefault().saveProject(project);
//            } catch(IOException ex) {
//                NotifyDescriptor desc = new NotifyDescriptor.Message(
//                org.openide.util.NbBundle.getMessage(J2SEProjectJaxRpcClientSupport.class,"MSG_ErrorSavingOnWSClientAdd", serviceName, ex.getMessage()), // NOI18N
//                NotifyDescriptor.ERROR_MESSAGE);
//                DialogDisplayer.getDefault().notify(desc);
//            }
//        }
    }
    
    private Element getWebServiceClientNode(Element data, String serviceName) {
        Element clientElement = null;
//        NodeList nodes = data.getElementsByTagName(WebServicesClientConstants.WEB_SERVICE_CLIENTS);
//        
//        if(nodes.getLength() != 0) {
//            Element clientElements = (Element) nodes.item(0);
//            NodeList clientNameList = clientElements.getElementsByTagNameNS(
//            J2SEProjectType.PROJECT_CONFIGURATION_NAMESPACE, WebServicesClientConstants.WEB_SERVICE_CLIENT_NAME);
//            for(int i = 0; i < clientNameList.getLength(); i++ ) {
//                Element clientNameElement = (Element) clientNameList.item(i);
//                NodeList nl = clientNameElement.getChildNodes();
//                if(nl.getLength() == 1) {
//                    org.w3c.dom.Node n = nl.item(0);
//                    if(n.getNodeType() == org.w3c.dom.Node.TEXT_NODE) {
//                        String name = n.getNodeValue();
//                        if(serviceName.equals(name)) {
//                            org.w3c.dom.Node node = clientNameElement.getParentNode();
//                            clientElement = (node instanceof Element) ? (Element) node : null;
//                            break;
//                        }
//                    } else {
//                        // !PW FIXME node is wrong type?! - log message or trace?
//                    }
//                }
//            }
//        }
        
        return clientElement;
    }
    
    private static final JAXRPCClientStubDescriptor jaxrpcClientStub = new JAXRPCClientStubDescriptor(
        ClientStubDescriptor.JAXRPC_CLIENT_STUB, org.openide.util.NbBundle.getMessage(J2SEProjectJaxRpcClientSupport.class,"LBL_JAXRPCStaticClientStub"),
        new String [] { "wsi", "strict" });

    public void addServiceClientReference(String serviceName, String fqServiceName, String relativeWsdlPath, String relativeMappingPath, String[] portSEIInfo) {
        // nothing to do in J2se
    }
    
    /** Stub descriptor for services and clients supported by this project type.
     */
    private static class JAXRPCClientStubDescriptor extends ClientStubDescriptor {
        
        private String [] defaultFeatures;
        
        public JAXRPCClientStubDescriptor(String name, String displayName, String [] defaultFeatures) {
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
        
        void setDefaultFeatures(String[] defaultFeatures) {
            this.defaultFeatures=defaultFeatures;
        }
    }
    
    public void setProxyJVMOptions(String proxyHost, String proxyPort) {
        this.proxyHost=proxyHost;
        this.proxyPort=proxyPort;
    }
    
    private static final String PROXY_HOST_OPTION="-Dhttp.proxyHost"; //NOI18N
    private static final String PROXY_PORT_OPTION="-Dhttp.proxyPort"; //NOI18N
    private static final String NON_PROXY_HOSTS_OPTION="-Dhttp.nonProxyHosts"; //NOI18N
    private static final String RUN_JVM_ARGS = "run.jvmargs"; // NOI18N
    
    @org.netbeans.api.annotations.common.SuppressWarnings("DE_MIGHT_IGNORE")
    private boolean addJVMProxyOptions(EditableProperties prop, String proxyHost, String proxyPort) {
        String jvmOptions = prop.getProperty(RUN_JVM_ARGS);
        boolean modif=false;
        String localHosts = "localhost"; //NOI18N 
        try {
            localHosts = java.net.InetAddress.getLocalHost().getCanonicalHostName();
        } catch (java.net.UnknownHostException ex) {}
        if (!"localhost".equals(localHosts)) localHosts="\""+localHosts+"|localhost\""; //NOI18N
        if (jvmOptions==null || jvmOptions.length()==0) {
            jvmOptions = PROXY_HOST_OPTION+"="+proxyHost+ //NOI18N
                    " "+PROXY_PORT_OPTION+"="+proxyPort+ //NOI18N
                    " "+NON_PROXY_HOSTS_OPTION+"="+localHosts; //NOI18N
            modif=true;
        } else {
            if (jvmOptions.indexOf(PROXY_HOST_OPTION)<0) {
                jvmOptions+=" "+PROXY_HOST_OPTION+"="+proxyHost; //NOI18N
                modif=true;
            }
            if (jvmOptions.indexOf(PROXY_PORT_OPTION)<0) {
                jvmOptions+=" "+PROXY_PORT_OPTION+"="+proxyPort; //NOI18N
                modif=true;
            }
            if (jvmOptions.indexOf(NON_PROXY_HOSTS_OPTION)<0) {
                jvmOptions+=" "+NON_PROXY_HOSTS_OPTION+"="+localHosts; //NOI18N
                modif=true;
            }
        }
        if (modif) prop.setProperty(RUN_JVM_ARGS,jvmOptions);
        return modif;
    }

    public String getServiceRefName(String serviceName) {
        //noop
        return null;
    }
    
    private static final DocumentBuilder db;
    static {
        try {
            db = DocumentBuilderFactory.newInstance().newDocumentBuilder();
        } catch (ParserConfigurationException e) {
            throw new AssertionError(e);
        }
    }
    private static Document createNewDocument() {
        // #50198: for thread safety, use a separate document.
        // Using XMLUtil.createDocument is much too slow.
        synchronized (db) {
            return db.newDocument();
        }
    }
    
}
