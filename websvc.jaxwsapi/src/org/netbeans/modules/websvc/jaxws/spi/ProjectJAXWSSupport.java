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

package org.netbeans.modules.websvc.jaxws.spi;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.stream.StreamSource;
import org.apache.tools.ant.module.api.support.ActionUtils;
import org.netbeans.api.project.Project;
import org.netbeans.modules.j2ee.dd.api.webservices.WebservicesMetadata;
import org.netbeans.modules.j2ee.metadata.model.api.MetadataModel;
import org.netbeans.modules.websvc.api.jaxws.project.JAXWSVersionProvider;
import org.netbeans.modules.websvc.api.jaxws.project.WSUtils;
import org.netbeans.modules.websvc.api.jaxws.project.config.JaxWsModel;
import org.netbeans.modules.websvc.api.jaxws.project.config.Service;
import org.netbeans.modules.websvc.api.jaxws.project.config.ServiceAlreadyExistsExeption;
import org.netbeans.modules.websvc.api.jaxws.project.config.WsimportOption;
import org.netbeans.modules.websvc.api.jaxws.project.config.WsimportOptions;
import org.netbeans.modules.websvc.jaxws.api.WsdlWrapperGenerator;
import org.netbeans.modules.websvc.jaxws.api.WsdlWrapperHandler;
import org.netbeans.spi.project.support.ant.AntProjectHelper;
import org.netbeans.spi.project.support.ant.GeneratedFilesHelper;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.ErrorManager;
import org.openide.execution.ExecutorTask;
import org.openide.filesystems.FileLock;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileStateInvalidException;
import org.openide.filesystems.FileSystem.AtomicAction;
import org.openide.filesystems.FileUtil;
import org.openide.util.NbBundle;
import org.xml.sax.SAXException;

/** Abstract class that implements most of JAXWSSupportImpl methods
 * Useful to implement instead of JAXWSSupportImpl
 *
 * @author mkuchtiak
 * Created on February 7, 2006, 11:09 AM
 */
public abstract class ProjectJAXWSSupport implements JAXWSSupportImpl {
    
    private static final String[] DEFAULT_WSIMPORT_OPTIONS = {"extension", "verbose", "fork"};  //NOI18N
    private static final String[] DEFAULT_WSIMPORT_VALUES = {"true", "true", "false"};  //NOI18N
    private static final String XNOCOMPILE_OPTION = "xnocompile";  //NOI18N
    private static final String XENDORSED_OPTION = "xendorsed"; //NOI18N
    private static final String TARGET_OPTION = "target"; //NOI18N

    protected static final String JAVA_EE_VERSION_NONE="java-ee-version-none"; //NOI18N
    protected static final String JAVA_EE_VERSION_15="java-ee-version-15"; //NOI18N
    protected static final String JAVA_EE_VERSION_16="java-ee-version-16"; //NOI18N
    protected static final String JAVA_EE_VERSION_17="java-ee-version-17"; //NOI18N
    
    private Project project;
    private AntProjectHelper antProjectHelper;
    private FileObject serviceArtifactsFolder;
    
    /** Creates a new instance of JAXWSSupport */
    public ProjectJAXWSSupport(Project project, AntProjectHelper antProjectHelper) {
        this.project = project;
        this.antProjectHelper = antProjectHelper;
    }
    
    public void removeService(String serviceName) {
        assert serviceName != null;
        JaxWsModel jaxWsModel = project.getLookup().lookup(JaxWsModel.class);
        if (jaxWsModel != null && serviceName != null) {
            Service service = jaxWsModel.findServiceByName(serviceName);
            if (service!=null) {
                // remove the service element as well as the implementation class
                jaxWsModel.removeService(serviceName);
                writeJaxWsModel(jaxWsModel);
                WSUtils.removeImplClass(project,service.getImplementationClass());
            }
        }
    }
    /**
     * Notification when Service (created from java) is removed from jax-ws.xml
     * (JAXWSSupport needs to react when @WebService annotation is removed
     * or when impl.class is removed (manually from project)
     * Default implementation does nothing.
     */
    public void serviceFromJavaRemoved(String serviceName) {}
    
    public boolean isFromWSDL(String serviceName) {
        JaxWsModel jaxWsModel = project.getLookup().lookup(JaxWsModel.class);
        Service service = jaxWsModel.findServiceByName(serviceName);
        if (service!=null && service.getWsdlUrl()!=null) return true;
        else return false;
    }
    
    /**
     * Returns the name of the implementation class
     * given the service (ide) name
     */
    public String getServiceImpl(String serviceName) {
        JaxWsModel jaxWsModel = project.getLookup().lookup(JaxWsModel.class);
        if (jaxWsModel!=null) {
            Service service = jaxWsModel.findServiceByName(serviceName);
            return service==null?null:service.getImplementationClass();
        }
        return null;
    }
    
    public AntProjectHelper getAntProjectHelper() {
        return antProjectHelper;
    }
    
    public void addService(String serviceName, String serviceImpl, boolean isJsr109) {  
        if(!isJsr109 ){
            try {
                addJaxwsArtifacts(project, serviceName, serviceImpl);
            } catch(Exception e){
                ErrorManager.getDefault().notify(e); //TODO handle this
            }
        } else {
            try {
                addServletElement(project, serviceName, serviceImpl);
            } catch(IOException e){
                ErrorManager.getDefault().notify(e); //TODO handle this
            }
        }
    }
    
    protected abstract void addJaxwsArtifacts(Project project, String wsName,
            String serviceImpl) throws Exception;
    
    protected void addServletElement(Project project, String wsName, String serviceImpl) throws IOException {        
    }
    
    /*
     * Add web service to jax-ws.xml
     * intended for web services from wsdl
     * @return returns the unique IDE service name
     */
    
    public String addService(String name, String serviceImpl, String wsdlUrl, String serviceName,
            String portName, String packageName, boolean isJsr109, boolean useProvider) {
        JaxWsModel jaxWsModel = project.getLookup().lookup(JaxWsModel.class);
        if (jaxWsModel!=null) {
            String finalServiceName = WSUtils.findProperServiceName(name, jaxWsModel);
            boolean serviceAdded=false;
            
            FileObject localWsdl=null;
            try {
                // download resources to xml-resources
                FileObject xmlResorcesFo = getLocalWsdlFolderForService(finalServiceName,true);
                localWsdl = WSUtils.retrieveResource(
                        xmlResorcesFo,
                        new URI(wsdlUrl));
                if (localWsdl!=null) {
                    
                    WsdlWrapperHandler handler = null;
                    try {
                        handler = WsdlWrapperGenerator.parse(localWsdl.getURL().toExternalForm());
                    } catch (ParserConfigurationException ex) {
                        ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL,ex);
                    } catch (SAXException ex) {
                        ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL,ex);
                    } catch (IOException ex) {
                        ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL,ex);
                    }
                    if (!handler.isServiceElement()) {
                        StreamSource source = new StreamSource(localWsdl.getURL().toExternalForm());
                        try {
                            File wrapperWsdlFile = new File(FileUtil.toFile(localWsdl.getParent()), WsdlWrapperGenerator.getWrapperName(localWsdl.getURL())); //NOI18N

                            if(!wrapperWsdlFile.exists()) {
                                try {
                                    wrapperWsdlFile.createNewFile();
                                } catch(IOException ex) {
                                    ErrorManager.getDefault().notify(ErrorManager.EXCEPTION,ex);
                                }
                            }
                            if (wrapperWsdlFile.exists()) {
                                WsdlWrapperGenerator.generateWrapperWSDLContent(wrapperWsdlFile, source, handler.getTargetNsPrefix(),localWsdl.getNameExt());
                                localWsdl=FileUtil.toFileObject(wrapperWsdlFile);
                            }
                        } catch (IOException ex) {
                            ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL,ex);
                        }
                    }

                    Boolean value = jaxWsModel.getJsr109();
                    if (value == null) {
                        jaxWsModel.setJsr109(isJsr109);
                    } else if (Boolean.TRUE.equals(value) && !isJsr109) {
                        jaxWsModel.setJsr109(Boolean.FALSE);
                    } else if (Boolean.FALSE.equals(value) && isJsr109) {
                        jaxWsModel.setJsr109(Boolean.TRUE);
                    }
                    Service service=null;
                    try {
                        service = jaxWsModel.addService(finalServiceName, serviceImpl, wsdlUrl, serviceName, portName, packageName);
                    } catch (ServiceAlreadyExistsExeption ex) {
                        ErrorManager.getDefault().notify(ex);
                    }
                    service.setUseProvider(useProvider);
                    String localWsdlUrl = FileUtil.getRelativePath(xmlResorcesFo, localWsdl);
                    service.setLocalWsdlFile(localWsdlUrl);
                    FileObject catalog = getCatalogFileObject();
                    if (catalog!=null) service.setCatalogFile(CATALOG_FILE);
                    
                    WsimportOptions wsimportOptions = service.getWsImportOptions();
                    if (wsimportOptions != null) {
                        int i=0;
                        for (String option:DEFAULT_WSIMPORT_OPTIONS) {
                            WsimportOption wsimportOption = wsimportOptions.newWsimportOption();
                            wsimportOption.setWsimportOptionName(option);
                            wsimportOption.setWsimportOptionValue(DEFAULT_WSIMPORT_VALUES[i++]); //NOI18N
                            wsimportOptions.addWsimportOption(wsimportOption);
                        }
                        if (isXnocompile(project)) {
                            WsimportOption wsimportOption = wsimportOptions.newWsimportOption();
                            wsimportOption.setWsimportOptionName(XNOCOMPILE_OPTION);
                            wsimportOption.setWsimportOptionValue("true"); //NOI18N
                            wsimportOptions.addWsimportOption(wsimportOption);
                        }
                        if (isXendorsed(project)) {
                            WsimportOption wsimportOption = wsimportOptions.newWsimportOption();
                            wsimportOption.setWsimportOptionName(XENDORSED_OPTION);
                            wsimportOption.setWsimportOptionValue("true"); //NOI18N
                            wsimportOptions.addWsimportOption(wsimportOption);
                        }
                        if (JAVA_EE_VERSION_15.equals(getProjectJavaEEVersion())) {
                            WsimportOption wsimportOption = wsimportOptions.newWsimportOption();
                            wsimportOption.setWsimportOptionName(TARGET_OPTION);
                            wsimportOption.setWsimportOptionValue("2.1"); //NOI18N
                            wsimportOptions.addWsimportOption(wsimportOption);
                        }
                    }
                    
                    writeJaxWsModel(jaxWsModel);
                    serviceAdded=true;
                }
                
            } catch (URISyntaxException ex) {
                ErrorManager.getDefault().notify(ErrorManager.EXCEPTION,ex);
            } catch (UnknownHostException ex) {
                ErrorManager.getDefault().notify(ErrorManager.EXCEPTION,ex);
            } catch (IOException ex) {
                ErrorManager.getDefault().notify(ErrorManager.EXCEPTION,ex);
            }
            
            if (serviceAdded) {
                if(!isJsr109 ){
                    try{
                        addJaxwsArtifacts(project, serviceName, serviceImpl);
                    } catch(Exception e){
                        ErrorManager.getDefault().notify(e); //TODO handle this
                    }
                } else {
                    try {
                        addServletElement(project, serviceName, serviceImpl);
                    } catch (IOException ex) {
                        ErrorManager.getDefault().notify(ex); //TODO handle this
                    }
                }
                FileObject buildImplFo = project.getProjectDirectory().getFileObject(GeneratedFilesHelper.BUILD_XML_PATH);
                try {
                    Properties props = WSUtils.identifyWsimport(antProjectHelper);
                    ExecutorTask wsimportTask =
                            ActionUtils.runTarget(buildImplFo,
                                    new String[]{"wsimport-service-"+finalServiceName}, //NOI18N
                                    props); 
                    wsimportTask.waitFinished();
                } catch (IOException ex) {
                    ErrorManager.getDefault().log(ex.getLocalizedMessage());
                } catch (IllegalArgumentException ex) {
                    ErrorManager.getDefault().log(ex.getLocalizedMessage());
                }
                return finalServiceName;
            }
        }
        return null;
    }
    
    /**
     * Returns the list of web services in the project
     */
    public List<Service> getServices() {
        JaxWsModel jaxWsModel = project.getLookup().lookup(JaxWsModel.class);
        if (jaxWsModel!=null) {
            Service[] services = jaxWsModel.getServices();
            if (services!=null) {
                List<Service> list = new ArrayList<Service>();
                for (int i=0;i<services.length;i++) {
                    list.add(services[i]);
                }
                return list;
            }
        }
        return new ArrayList<Service>();
    }

    private void writeJaxWsModel(final JaxWsModel jaxWsModel) {
        try {
            final FileObject jaxWsFo = project.getProjectDirectory().getFileObject("nbproject/jax-ws.xml"); //NOI18N
            if (jaxWsFo != null) {
                jaxWsFo.getFileSystem().runAtomicAction(new AtomicAction() {
                    public void run() {
                        FileLock lock=null;
                        OutputStream os=null;
                        try {
                            lock = jaxWsFo.lock();
                            os = jaxWsFo.getOutputStream(lock);
                            jaxWsModel.write(os);
                            os.close();
                        } catch (java.io.IOException ex) {
                            ErrorManager.getDefault().notify(ex);
                        } finally {
                            if (os!=null) {
                                try {
                                    os.close();
                                } catch (IOException ex) {}
                            }
                            if (lock!=null) lock.releaseLock();
                        }
                    }
                });
            }
        } catch (IOException ex) {
            ErrorManager.getDefault().notify(ex);
        }
    }
    
    /**
     *  return folder for local wsdl artifacts
     */
    public FileObject getLocalWsdlFolderForService(String serviceName, boolean createFolder) {
        return getArtifactsFolder(serviceName, createFolder, true);
    }
    /**
     *  return folder for local wsdl bindings
     */
    public FileObject getBindingsFolderForService(String serviceName, boolean createFolder) {
        return getArtifactsFolder(serviceName, createFolder, false);
    }
    
    private FileObject getArtifactsFolder(String serviceName, boolean createFolder, boolean forWsdl) {
        String folderName = forWsdl?"wsdl":"bindings"; //NOI18N
        FileObject root = getXmlArtifactsRoot();
        if (root==null) {
            DialogDisplayer.getDefault().notify(
                new DialogDescriptor.Message(NbBundle.getMessage(ProjectJAXWSSupport.class, "MSG_MISSING_SRC_CONF")));
            return null;
        }
        FileObject wsdlLocalFolder = root.getFileObject(XML_RESOURCES_FOLDER+"/"+SERVICES_LOCAL_FOLDER+"/"+serviceName+"/"+folderName); //NOI18N
        if (wsdlLocalFolder==null && createFolder) {
            try {
                FileObject xmlLocalFolder = root.getFileObject(XML_RESOURCES_FOLDER);
                if (xmlLocalFolder==null) xmlLocalFolder = root.createFolder(XML_RESOURCES_FOLDER);
                FileObject servicesLocalFolder = xmlLocalFolder.getFileObject(SERVICES_LOCAL_FOLDER);
                if (servicesLocalFolder==null) servicesLocalFolder = xmlLocalFolder.createFolder(SERVICES_LOCAL_FOLDER);
                FileObject serviceLocalFolder = servicesLocalFolder.getFileObject(serviceName);
                if (serviceLocalFolder==null) serviceLocalFolder = servicesLocalFolder.createFolder(serviceName);
                wsdlLocalFolder=serviceLocalFolder.getFileObject(folderName);
                if (wsdlLocalFolder==null) wsdlLocalFolder = serviceLocalFolder.createFolder(folderName);
            } catch (IOException ex) {
                return null;
            }
        }
        return wsdlLocalFolder;
    }
    
    /** return root folder for xml artifacts
     */
    protected FileObject getXmlArtifactsRoot() {
        return project.getProjectDirectory();
    }
    
    private FileObject getCatalogFileObject() {
        return project.getProjectDirectory().getFileObject(CATALOG_FILE);
    }
    
    public URL getCatalog() {
        try {
            FileObject catalog = getCatalogFileObject();
            return catalog==null?null:catalog.getURL();
        } catch (FileStateInvalidException ex) {
            return null;
        }
        
    }
    
    private FileObject getWsdlFolderForService(String name) throws IOException {
        FileObject globalWsdlFolder = getWsdlFolder(true);
        FileObject oldWsdlFolder = globalWsdlFolder.getFileObject(name);
        if (oldWsdlFolder!=null) {
            FileLock lock = oldWsdlFolder.lock();
            try {
                oldWsdlFolder.delete(lock);
            } finally {
                lock.releaseLock();
            }
        }
        return globalWsdlFolder.createFolder(name);
    }
    
    private static boolean isXnocompile(Project project){
        JAXWSVersionProvider jvp = project.getLookup().lookup(JAXWSVersionProvider.class);
        if (jvp != null) {
            String version = jvp.getJAXWSVersion();
            if (version != null) {
                return isVersionSatisfied(version, "2.1.3");
            }
        }
        // Defaultly return true
        return true;
    }
    
    private static boolean isXendorsed(Project project){
        JAXWSVersionProvider jvp = project.getLookup().lookup(JAXWSVersionProvider.class);
        if (jvp != null) {
            String version = jvp.getJAXWSVersion();
            if (version != null) {
                return isVersionSatisfied(version, "2.1.1"); //NOI18N
            }
        }
        // Defaultly return false
        return false;
    }
    
    private static boolean isVersionSatisfied(String version, String requiredVersion) {
        int len1 = version.length();
        int len2 = requiredVersion.length();
        for (int i=0;i<Math.min(len1, len2);i++) {
            if (version.charAt(i) < requiredVersion.charAt(i)) {
                return false;
            } else if (version.charAt(i) > requiredVersion.charAt(i)) {
                return true;
            }
        }
        if (len1 > len2) return true;
        else if (len1 < len2) return false;
        return true;
    }
    
    public abstract FileObject getWsdlFolder(boolean create) throws java.io.IOException;
    
    /** Get wsdlLocation information
     * Useful for web service from wsdl
     * @param name service "display" name
     */
    public abstract String getWsdlLocation(String serviceName);

    /**
     * Returns a metadata model of a webservices deployment descriptor
     *
     * @return metadata model of a webservices deployment descriptor
     */
    public abstract MetadataModel<WebservicesMetadata> getWebservicesMetadataModel();

    protected String getProjectJavaEEVersion() {
        return JAVA_EE_VERSION_NONE;
    }
}
