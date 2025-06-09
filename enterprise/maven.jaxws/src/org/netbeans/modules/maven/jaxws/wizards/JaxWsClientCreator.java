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
package org.netbeans.modules.maven.jaxws.wizards;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.UnknownHostException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.prefs.Preferences;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.modules.maven.api.execute.RunConfig;
import org.netbeans.modules.maven.api.execute.RunUtils;
import org.netbeans.modules.maven.jaxws.MavenModelUtils;
import org.netbeans.modules.maven.jaxws.MavenWebService;
import org.netbeans.modules.maven.jaxws.WSUtils;
import org.netbeans.modules.websvc.api.support.ClientCreator;
import java.io.IOException;

import java.util.Collections;
import org.netbeans.api.progress.ProgressUtils;
import org.netbeans.api.project.Project;

import org.netbeans.modules.maven.jaxws.MavenJAXWSSupportImpl;
import org.netbeans.modules.maven.model.ModelOperation;
import org.netbeans.modules.maven.model.Utilities;
import org.netbeans.modules.maven.model.pom.POMModel;
import org.netbeans.modules.maven.model.pom.Plugin;
import org.netbeans.modules.websvc.jaxws.light.api.JAXWSLightSupport;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.WizardDescriptor;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.NbBundle;

/**
 *
 * @author Milan Kuchtiak
 */
public class JaxWsClientCreator implements ClientCreator {

    private Project project;
    private WizardDescriptor wiz;
    private boolean isWeb;
    private boolean isEJB;
    private boolean isJakartaEENameSpace;
    
    private static final Logger LOG = Logger.getLogger(JaxWsClientCreator.class.getCanonicalName());

    /**
     * Creates a new instance of WebServiceClientCreator
     */
    public JaxWsClientCreator(Project project, WizardDescriptor wiz) {
        this.project = project;
        this.wiz = wiz;
        this.isWeb = WSUtils.isWeb(project);
        this.isEJB = WSUtils.isEJB(project);
        this.isJakartaEENameSpace = WSUtils.isJakartaEENameSpace(project);
    }

    @Override
    public void createClient() throws IOException {
        JAXWSLightSupport jaxWsSupport = JAXWSLightSupport.getJAXWSLightSupport(project.getProjectDirectory());
        String wsdlUrl = (String)wiz.getProperty(WizardProperties.WSDL_DOWNLOAD_URL);
        String filePath = (String)wiz.getProperty(WizardProperties.WSDL_FILE_PATH);
        //Boolean useDispatch = (Boolean) wiz.getProperty(ClientWizardProperties.USEDISPATCH);
        //if (wsdlUrl==null) wsdlUrl = "file:"+(filePath.startsWith("/")?filePath:"/"+filePath); //NOI18N
        if(wsdlUrl == null) {
            wsdlUrl = FileUtil.toFileObject(FileUtil.normalizeFile(new File(filePath))).toURL().toExternalForm();
        }
        FileObject localWsdlFolder = jaxWsSupport.getWsdlFolder(true);
        
        boolean hasSrcFolder = false;
        File srcFile = new File (FileUtil.toFile(project.getProjectDirectory()),"src"); //NOI18N
        if (srcFile.exists()) {
            hasSrcFolder = true;
        } else {
            hasSrcFolder = srcFile.mkdirs();
        }
        
        if (localWsdlFolder != null) {
            FileObject wsdlFo = retrieveWsdl(wsdlUrl, localWsdlFolder, hasSrcFolder);
            if (wsdlFo != null) {
                final boolean isJaxWsLibrary = MavenModelUtils.hasJaxWsAPI(project, isJakartaEENameSpace);
                final String relativePath = FileUtil.getRelativePath(localWsdlFolder, wsdlFo);
                final String clientName = wsdlFo.getName();

                Preferences prefs = ProjectUtils.getPreferences(project, MavenWebService.class, true);
                if (prefs != null) {
                    // remember original wsdlUrl for Client
                    prefs.put(MavenWebService.CLIENT_PREFIX+WSUtils.getUniqueId(wsdlFo.getName(), jaxWsSupport.getServices()), wsdlUrl);
                }

                if (!isJaxWsLibrary) {
                    try {
                        MavenModelUtils.addMetroLibrary(project);
                        MavenModelUtils.addJavadoc(project);
                    } catch (Exception ex) {
                        LOG.log(Level.INFO, "Cannot add Metro library to pom file", ex); //NOI18N
                    }
                }
                
                final String wsdlLocation = wsdlUrl;
                ModelOperation<POMModel> operation = new ModelOperation<POMModel>() {
                    @Override
                    public void performOperation(POMModel model) {

                        String packageName = (String) wiz.getProperty(WizardProperties.WSDL_PACKAGE_NAME);
                        Plugin plugin = MavenModelUtils.addJaxWSPlugin(model);

                        MavenModelUtils.addWsimportExecution(plugin, clientName, 
                                relativePath,wsdlLocation, packageName);
                        if (isWeb) { // expecting web project
                            MavenModelUtils.addWarPlugin(model, true);
                        } else { // J2SE Project
                            MavenModelUtils.addWsdlResources(model);
                        }
                    }
                };
                
                Utilities.performPOMModelOperations(project.getProjectDirectory().getFileObject("pom.xml"),
                        Collections.singletonList(operation));

                // execute wsimport goal
                RunConfig cfg = RunUtils.createRunConfig(FileUtil.toFile(
                        project.getProjectDirectory()),
                        project,
                        "JAX-WS:wsimport", //NOI18N
                        Collections.singletonList("compile")); //NOI18N
                
                RunUtils.executeMaven(cfg);
            }
        }
    }

    private FileObject retrieveWsdl( final String wsdlUrl,
            final FileObject localWsdlFolder, final boolean hasSrcFolder )
    {
        final FileObject[] wsdlFo = new FileObject[1];
        Runnable runnable = new Runnable() {
            
            @Override
            public void run() {
                try {
                    wsdlFo[0] = WSUtils.retrieveResource(
                            localWsdlFolder,
                            (hasSrcFolder ? 
                                    new URI(MavenJAXWSSupportImpl.CATALOG_PATH) : 
                                        new URI("jax-ws-catalog.xml")), //NOI18N
                                            new URI(wsdlUrl));
                } catch (URISyntaxException ex) {
                    //ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, ex);
                    String mes = NbBundle.getMessage(JaxWsClientCreator.class, 
                            "ERR_IncorrectURI", wsdlUrl); // NOI18N
                    NotifyDescriptor desc = new NotifyDescriptor.Message(mes, 
                            NotifyDescriptor.Message.ERROR_MESSAGE);
                    DialogDisplayer.getDefault().notify(desc);
                } catch (UnknownHostException ex) {
                    //ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, ex);
                    String mes = NbBundle.getMessage(JaxWsClientCreator.class, 
                            "ERR_UnknownHost", ex.getMessage()); // NOI18N
                    NotifyDescriptor desc = new NotifyDescriptor.Message(mes, 
                            NotifyDescriptor.Message.ERROR_MESSAGE);
                    DialogDisplayer.getDefault().notify(desc);
                } catch (IOException ex) {
                    //ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, ex);
                    String mes = NbBundle.getMessage(JaxWsClientCreator.class, 
                            "ERR_WsdlRetrieverFailure", wsdlUrl); // NOI18N
                    NotifyDescriptor desc = new NotifyDescriptor.Message(mes, 
                            NotifyDescriptor.Message.ERROR_MESSAGE);
                    DialogDisplayer.getDefault().notify(desc);
                }                
            }
        };
        AtomicBoolean isCancelled = new AtomicBoolean();
        ProgressUtils.runOffEventDispatchThread(runnable, NbBundle.getMessage(
                JaxWsClientCreator.class, "LBL_RetrieveWSDL"), isCancelled, false);    // NOI18N
        
        if ( isCancelled.get() ){
            return null;
        }
        return wsdlFo[0];
    }
    
}
