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

package org.netbeans.modules.websvc.spi.jaxws.client;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.SwingUtilities;
import org.apache.tools.ant.module.api.support.ActionUtils;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.modules.websvc.api.jaxws.project.CatalogUtils;
import org.netbeans.modules.websvc.api.jaxws.project.JAXWSVersionProvider;
import org.netbeans.modules.websvc.api.jaxws.project.config.Client;
import org.netbeans.modules.websvc.api.jaxws.project.config.ClientAlreadyExistsExeption;
import org.netbeans.modules.websvc.api.jaxws.project.config.JaxWsModel;
import org.netbeans.modules.websvc.api.jaxws.project.WSUtils;
import org.netbeans.modules.websvc.api.jaxws.project.config.WsimportOption;
import org.netbeans.modules.websvc.api.jaxws.project.config.WsimportOptions;
import org.netbeans.modules.websvc.api.jaxws.wsdlmodel.WsdlModel;
import org.netbeans.modules.websvc.api.jaxws.wsdlmodel.WsdlModelListener;
import org.netbeans.modules.websvc.api.jaxws.wsdlmodel.WsdlModeler;
import org.netbeans.modules.websvc.api.jaxws.wsdlmodel.WsdlModelerFactory;
import org.netbeans.modules.websvc.api.jaxws.wsdlmodel.WsdlService;
import org.netbeans.spi.project.support.ant.AntProjectHelper;
import org.netbeans.spi.project.support.ant.GeneratedFilesHelper;
import org.openide.DialogDisplayer;
import org.openide.ErrorManager;
import org.openide.NotifyDescriptor;
import org.openide.execution.ExecutorTask;
import org.openide.filesystems.FileLock;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileStateInvalidException;
import org.openide.filesystems.FileUtil;
import org.openide.nodes.Node;
import org.openide.util.Mutex;
import org.openide.util.MutexException;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;
import org.openide.windows.TopComponent;
import org.openide.windows.WindowManager;

/**
 *
 * @author mkuchtiak
 */
public abstract class ProjectJAXWSClientSupport implements JAXWSClientSupportImpl {
    
    private static final String[] DEFAULT_WSIMPORT_OPTIONS = {"extension", "verbose", "fork"};  //NOI18N
    private static final String[] DEFAULT_WSIMPORT_VALUES = {"true", "true", "false"};  //NOI18N
    private static final String XNOCOMPILE_OPTION = "xnocompile"; //NOI18N
    private static final String XENDORSED_OPTION = "xendorsed"; //NOI18N
    private static final String PACKAGE_OPTION = "package"; //NOI18N
    private static final String WSDL_LOCATION_OPTION = "wsdlLocation"; //NOI18N
    private static final String TARGET_OPTION = "target"; //NOI18N
    protected static final String JAVA_EE_VERSION_NONE="java-ee-version-none"; //NOI18N
    protected static final String JAVA_EE_VERSION_15="java-ee-version-15"; //NOI18N
    protected static final String JAVA_EE_VERSION_16="java-ee-version-16"; //NOI18N
    protected static final String JAVA_EE_VERSION_17="java-ee-version-17"; //NOI18N
    
    Project project;
    private AntProjectHelper helper;
    private FileObject clientArtifactsFolder;
    
    /** Creates a new instance of WebProjectJAXWSClientSupport */
    public ProjectJAXWSClientSupport(Project project, AntProjectHelper helper ) {
        this.project=project;
        this.helper = helper;
    }
    
    public void removeServiceClient(String serviceName) {
        JaxWsModel jaxWsModel = project.getLookup().lookup(JaxWsModel.class);
        if (jaxWsModel!=null && jaxWsModel.removeClient(serviceName)) {
            writeJaxWsModel(jaxWsModel);
        }
    }
    
    public AntProjectHelper getAntProjectHelper() {
        return helper;
    }
    
    public String getWsdlUrl(String serviceName) {
        JaxWsModel jaxWsModel = project.getLookup().lookup(JaxWsModel.class);
        if (jaxWsModel!=null) {
            Client client = jaxWsModel.findClientByName(serviceName);
            if (client!=null) return client.getWsdlUrl();
        }
        return null;
    }
    
    public String addServiceClient(String clientName, String wsdlUrl, String packageName, boolean isJsr109) {
        
        // create jax-ws.xml if necessary
        FileObject fo = WSUtils.findJaxWsFileObject(project);
        if (fo==null) {
            try {
                WSUtils.createJaxWsFileObject(project);
            } catch (IOException ex) {
                ErrorManager.getDefault().notify(ex);
            }
        }
        
        final JaxWsModel jaxWsModel = project.getLookup().lookup(JaxWsModel.class);
        String finalClientName=clientName;
        boolean clientAdded=false;
        if (jaxWsModel!=null) {
            
            if(!isJsr109){
                try{
                    addJaxWs20Library();
                } catch(Exception e){  //TODO handle this
                    ErrorManager.getDefault().notify(e);
                }
            }
            
            Client client=null;
            finalClientName = findProperClientName(clientName, jaxWsModel);
            FileObject xmlResourcesFo = getLocalWsdlFolderForClient(finalClientName,true);                      
            FileObject localWsdl=null;
            try {
                localWsdl = WSUtils.retrieveResource(
                        xmlResourcesFo,
                        new URI(wsdlUrl));
            } catch (URISyntaxException ex) {
                ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, ex);
                String mes = NbBundle.getMessage(ProjectJAXWSClientSupport.class, "ERR_IncorrectURI", wsdlUrl); // NOI18N
                NotifyDescriptor desc = new NotifyDescriptor.Message(mes, NotifyDescriptor.Message.ERROR_MESSAGE);
                DialogDisplayer.getDefault().notify(desc);
            } catch (UnknownHostException ex) {
                ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, ex);
                String mes = NbBundle.getMessage(ProjectJAXWSClientSupport.class, "ERR_UnknownHost", ex.getMessage()); // NOI18N
                NotifyDescriptor desc = new NotifyDescriptor.Message(mes, NotifyDescriptor.Message.ERROR_MESSAGE);
                DialogDisplayer.getDefault().notify(desc);
            } catch (IOException ex) {
                ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, ex);
                String mes = NbBundle.getMessage(ProjectJAXWSClientSupport.class, "ERR_WsdlRetrieverFailure", wsdlUrl); // NOI18N
                NotifyDescriptor desc = new NotifyDescriptor.Message(mes, NotifyDescriptor.Message.ERROR_MESSAGE);
                DialogDisplayer.getDefault().notify(desc);
            }
            
            if (localWsdl!=null) {
                try {
                    String localWsdlUrl = FileUtil.getRelativePath(xmlResourcesFo, localWsdl);
                    client = jaxWsModel.addClient(finalClientName, wsdlUrl, packageName);
                    client.setLocalWsdlFile(localWsdlUrl);
                    if (packageName == null) {
                        // compute package name from namespace
                        client.setPackageName(
                                WSUtils.getPackageNameForWsdl(FileUtil.toFile(localWsdl)));
                    }
                } catch (ClientAlreadyExistsExeption ex) {
                    //this shouldn't happen
                }

                FileObject catalog = getCatalogFileObject();
                if (catalog!=null) client.setCatalogFile(CATALOG_FILE);
                
                WsimportOptions wsimportOptions = client.getWsImportOptions();
                WsimportOption wsimportOption = null;
                if (wsimportOptions != null) {
                    int i=0;
                    for (String option:DEFAULT_WSIMPORT_OPTIONS) {
                        wsimportOption = wsimportOptions.newWsimportOption();
                        wsimportOption.setWsimportOptionName(option);
                        wsimportOption.setWsimportOptionValue(DEFAULT_WSIMPORT_VALUES[i++]); //NOI18N
                        wsimportOptions.addWsimportOption(wsimportOption);
                    }
                    wsimportOption = wsimportOptions.newWsimportOption();
                    wsimportOption.setWsimportOptionName(WSDL_LOCATION_OPTION);
                    wsimportOption.setWsimportOptionValue(wsdlUrl);
                    wsimportOptions.addWsimportOption(wsimportOption);
                    if (isXnocompile(project)) {
                        wsimportOption = wsimportOptions.newWsimportOption();
                        wsimportOption.setWsimportOptionName(XNOCOMPILE_OPTION);
                        wsimportOption.setWsimportOptionValue("true"); //NOI18N
                        wsimportOptions.addWsimportOption(wsimportOption);
                    }
                    if (isXendorsed(project)) {
                        wsimportOption = wsimportOptions.newWsimportOption();
                        wsimportOption.setWsimportOptionName(XENDORSED_OPTION);
                        wsimportOption.setWsimportOptionValue("true"); //NOI18N
                        wsimportOptions.addWsimportOption(wsimportOption);
                    }
                    if (packageName != null) {
                        wsimportOption = wsimportOptions.newWsimportOption();
                        wsimportOption.setWsimportOptionName(PACKAGE_OPTION);
                        wsimportOption.setWsimportOptionValue(packageName); //NOI18N
                        wsimportOptions.addWsimportOption(wsimportOption);
                    }
                    if (JAVA_EE_VERSION_15.equals(getProjectJavaEEVersion())) {
                        wsimportOption = wsimportOptions.newWsimportOption();
                        wsimportOption.setWsimportOptionName(TARGET_OPTION);
                        wsimportOption.setWsimportOptionValue("2.1"); //NOI18N
                        wsimportOptions.addWsimportOption(wsimportOption);
                    }
                }
                writeJaxWsModel(jaxWsModel);
                clientAdded=true;
                // get jax-ws-catalog.xml
                if (catalog != null) {
                    try {
                        FileObject webInfWsdl = getWsdlFolder(true);
                        if (webInfWsdl != null) {
                            FileObject jaxWsCatalog = webInfWsdl.getParent().getFileObject("jax-ws-catalog.xml");
                            if (jaxWsCatalog == null) {
                                jaxWsCatalog = FileUtil.copyFile(catalog, webInfWsdl.getParent(), "jax-ws-catalog"); //NOI18N
                                // update system elements in jax-ws-catalog.xml
                                CatalogUtils.updateCatalogEntriesForClient(jaxWsCatalog, clientName);
                            } else {
                                // copy, and modify catalog entries from catalog.xml to jax-ws-catalog.xml
                                CatalogUtils.copyCatalogEntriesForClient(catalog, jaxWsCatalog, clientName);
                            }
                            // copy files
                            WSUtils.copyFiles(xmlResourcesFo, webInfWsdl);
                        }

                    } catch (IOException ex) {
                        ex.printStackTrace();
                    }
                }

                // generate wsdl model immediately
                final String clientName2 = finalClientName;
                try {
                    final WsdlModeler modeler = WsdlModelerFactory.getDefault().getWsdlModeler(localWsdl.getURL());
                    if (modeler!=null) {
                        modeler.setPackageName(packageName);
                        if (catalog != null) {
                            modeler.setCatalog(catalog.getURL());
                        }
                        modeler.generateWsdlModel(new WsdlModelListener() {
                            public void modelCreated(WsdlModel model) {
                                if (model==null) {
                                    RequestProcessor.getDefault().post(new Runnable() {
                                       public void run() {
                                           DialogDisplayer.getDefault().notify(new WsImportFailedMessage(modeler.getCreationException()));
                                       }
                                    });
                                    
                                } else {
                                    Client client = jaxWsModel.findClientByName(clientName2);
                                    String packName = client.getPackageName();                               
                                    // this shuldn't normally happen
                                    // this applies only for case when package name cannot be resolved for namespace
                                    if(packName == null) {
                                        if (model.getServices().size() > 0) {
                                            WsdlService service = model.getServices().get(0);
                                            String javaName = service.getJavaName();
                                            int index = javaName.lastIndexOf(".");
                                            if (index != -1){
                                                packName = javaName.substring(0,index );
                                            } else {
                                                packName = javaName;
                                            }                                 
                                            client.setPackageName(packName);
                                            writeJaxWsModel(jaxWsModel);
                                        }
                                    }
                                    
                                    runWsimport(clientName2);
                                }
                            }
                        });
                    }
                } catch (IOException ex) {
                    ErrorManager.getDefault().log(ex.getLocalizedMessage());
                }
            }
            return finalClientName;
        }
        return null;
    }
    
    private void runWsimport(String finalClientName){
        final FileObject buildImplFo = project.getProjectDirectory().getFileObject(GeneratedFilesHelper.BUILD_XML_PATH);
        final String finalName = finalClientName;

        if (SwingUtilities.isEventDispatchThread()) {
            openOutputWindow();
        } else {
            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    openOutputWindow();
                }
            });            
        }

        try {
            ProjectManager.mutex().readAccess(new Mutex.ExceptionAction<Boolean>() {
                public Boolean run() throws IOException {
                    Properties props = WSUtils.identifyWsimport(helper);
                    ExecutorTask wsimportTask =
                            ActionUtils.runTarget(buildImplFo,new String[]{"wsimport-client-"+finalName},props); //NOI18N
                    return Boolean.TRUE;
                }
            }).booleanValue();
        } catch (MutexException e) {
            ErrorManager.getDefault().notify(e);
        }
    }
    
    private void openOutputWindow() {
        TopComponent outputTc = WindowManager.getDefault().findTopComponent("output"); //NOI18N
        if (outputTc != null) {
            outputTc.open();
        }       
    }
    
    private String findProperClientName(String name, JaxWsModel jaxWsModel) {
        String firstName=name.length()==0?NbBundle.getMessage(ProjectJAXWSClientSupport.class,"LBL_defaultClientName"):name;
        if (jaxWsModel.findClientByName(firstName)==null) return firstName;
        for (int i = 1;; i++) {
            String finalName = firstName + "_" + i; // NOI18N
            if (jaxWsModel.findClientByName(finalName)==null)
                return finalName;
        }
    }
    
    private void writeJaxWsModel(JaxWsModel jaxWsModel) {
        try {
            jaxWsModel.write();
        } catch (IOException ex) {
            Logger.getLogger(this.getClass().getName()).log(Level.FINE, "failed to save jax-ws.xml", ex); //NOI18N
        }
    }
    
    public List getServiceClients() {
        List<Client> jaxWsClients = new ArrayList<Client>();
        JaxWsModel jaxWsModel = project.getLookup().lookup(JaxWsModel.class);
        if (jaxWsModel!=null) {
            Client[] clients = jaxWsModel.getClients();
            for (int i=0;i<clients.length;i++) jaxWsClients.add(clients[i]);
        }
        return jaxWsClients;
    }
    
    /**
     *  return root folder for wsdl artifacts
     */
    public FileObject getLocalWsdlFolderForClient(String clientName, boolean createFolder) {
        return getArtifactsFolder(clientName, createFolder, true);
    }
    
    /**
     *  return folder for local wsdl bindings
     */
    public FileObject getBindingsFolderForClient(String clientName, boolean createFolder) {
        return getArtifactsFolder(clientName, createFolder, false);
    }
    
    private FileObject getArtifactsFolder(String clientName, boolean createFolder, boolean forWsdl) {
        String folderName = forWsdl?"wsdl":"bindings"; //NOI18N
        FileObject root = getXmlArtifactsRoot();
        assert root!=null;
        FileObject wsdlLocalFolder = root.getFileObject(XML_RESOURCES_FOLDER+"/"+CLIENTS_LOCAL_FOLDER+"/"+clientName+"/"+folderName); //NOI18N
        if (wsdlLocalFolder==null && createFolder) {
            try {
                FileObject xmlLocalFolder = root.getFileObject(XML_RESOURCES_FOLDER);
                if (xmlLocalFolder==null) xmlLocalFolder = root.createFolder(XML_RESOURCES_FOLDER);
                FileObject servicesLocalFolder = xmlLocalFolder.getFileObject(CLIENTS_LOCAL_FOLDER);
                if (servicesLocalFolder==null) servicesLocalFolder = xmlLocalFolder.createFolder(CLIENTS_LOCAL_FOLDER);
                FileObject serviceLocalFolder = servicesLocalFolder.getFileObject(clientName);
                if (serviceLocalFolder==null) serviceLocalFolder = servicesLocalFolder.createFolder(clientName);
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
    
    protected abstract void addJaxWs20Library() throws Exception;
    
    public abstract FileObject getWsdlFolder(boolean create) throws IOException;
    
    public String getServiceRefName(Node clientNode) {
        WsdlService service = clientNode.getLookup().lookup(WsdlService.class);
        String serviceName = service.getName();
        return "service/" + serviceName;
    }
    
    private class WsImportFailedMessage extends NotifyDescriptor.Message {
        public WsImportFailedMessage(Throwable ex) {
            super(NbBundle.getMessage(ProjectJAXWSClientSupport.class,"TXT_CannotGenerateClient",ex.getLocalizedMessage()),
                    NotifyDescriptor.ERROR_MESSAGE);
        }
        
    }
    
    /** folder where xml client artifacts should be saved, e.g. WEB-INF/wsdl/client/SampleClient
     */
    protected FileObject getWsdlFolderForClient(String name) throws IOException {
        FileObject globalWsdlFolder = getWsdlFolder(true);
        FileObject oldWsdlFolder = globalWsdlFolder.getFileObject("client/"+name); //NOI18N
        if (oldWsdlFolder!=null) {
            FileLock lock = oldWsdlFolder.lock();
            try {
                oldWsdlFolder.delete(lock);
            } finally {
                lock.releaseLock();
            }
        }
        FileObject clientWsdlFolder = globalWsdlFolder.getFileObject("client"); //NOI18N
        if (clientWsdlFolder==null) clientWsdlFolder = globalWsdlFolder.createFolder("client"); //NOI18N
        return clientWsdlFolder.createFolder(name);
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

    protected String getProjectJavaEEVersion() {
        return JAVA_EE_VERSION_NONE;
    }
    
}
