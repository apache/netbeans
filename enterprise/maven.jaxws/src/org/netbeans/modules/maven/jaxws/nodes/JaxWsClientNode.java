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
package org.netbeans.modules.maven.jaxws.nodes;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.prefs.Preferences;
import javax.swing.Action;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.modules.maven.api.execute.RunConfig;
import org.netbeans.modules.maven.api.execute.RunUtils;
import org.netbeans.modules.maven.jaxws.MavenJAXWSSupportImpl;
import org.netbeans.modules.maven.jaxws.MavenModelUtils;
import org.netbeans.modules.maven.jaxws.MavenWebService;
import org.netbeans.modules.maven.jaxws.WSUtils;
import org.netbeans.modules.maven.jaxws.actions.JaxWsRefreshAction;
import org.netbeans.modules.maven.jaxws.actions.WSEditAttributesAction;
import org.netbeans.modules.maven.jaxws.wizards.JaxWsClientCreator;
import org.netbeans.modules.maven.jaxws.wseditor.EditWSAttributesCookieImpl;
import org.netbeans.modules.maven.model.ModelOperation;
import org.netbeans.modules.maven.model.Utilities;
import org.netbeans.modules.maven.model.pom.POMModel;
import org.netbeans.modules.websvc.api.jaxws.bindings.BindingsHandler;
import org.netbeans.modules.websvc.api.jaxws.bindings.BindingsHandlerChain;
import org.netbeans.modules.websvc.api.jaxws.bindings.BindingsHandlerChains;
import org.netbeans.modules.websvc.api.jaxws.bindings.BindingsHandlerClass;
import org.netbeans.modules.websvc.api.jaxws.bindings.BindingsModel;
import org.netbeans.modules.websvc.api.jaxws.bindings.BindingsModelFactory;
import org.netbeans.modules.websvc.api.jaxws.bindings.DefinitionsBindings;
import org.netbeans.modules.websvc.api.jaxws.bindings.GlobalBindings;
import org.netbeans.modules.websvc.api.jaxws.wsdlmodel.WsdlModel;
import org.netbeans.modules.websvc.api.jaxws.wsdlmodel.WsdlModelListener;
import org.netbeans.modules.websvc.api.jaxws.wsdlmodel.WsdlModeler;
import org.netbeans.modules.websvc.api.jaxws.wsdlmodel.WsdlModelerFactory;
import org.netbeans.modules.websvc.api.support.ConfigureHandlerCookie;
import org.netbeans.modules.websvc.api.support.RefreshClientDialog;
import org.netbeans.modules.websvc.api.support.RefreshCookie;
import org.netbeans.modules.websvc.jaxws.light.api.JAXWSLightSupport;
import org.netbeans.modules.websvc.jaxws.light.api.JaxWsService;
import org.netbeans.modules.websvc.spi.support.ConfigureHandlerAction;
import org.netbeans.modules.websvc.spi.support.MessageHandlerPanel;
import org.netbeans.modules.xml.xam.ModelSource;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.ErrorManager;
import org.openide.NotifyDescriptor;
import org.openide.actions.DeleteAction;
import org.openide.actions.OpenAction;
import org.openide.actions.PropertiesAction;
import org.openide.cookies.EditCookie;
import org.openide.cookies.OpenCookie;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileStateInvalidException;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
import org.openide.nodes.AbstractNode;
import org.openide.util.HelpCtx;
import org.openide.util.ImageUtilities;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;
import org.openide.util.actions.SystemAction;
import org.openide.util.lookup.AbstractLookup;
import org.openide.util.lookup.InstanceContent;
import org.openide.util.lookup.Lookups;

public class JaxWsClientNode extends AbstractNode implements OpenCookie, RefreshCookie, ConfigureHandlerCookie {
    JaxWsService client;
    JAXWSLightSupport jaxWsSupport;
    InstanceContent content;
    private FileObject wsdlFileObject;
    boolean modelGenerationFinished;
    WsdlModel wsdlModel;
    
    public JaxWsClientNode(JAXWSLightSupport jaxWsSupport, JaxWsService client) {
        this(jaxWsSupport, client, new InstanceContent());
    }
    
    private JaxWsClientNode(JAXWSLightSupport jaxWsSupport, JaxWsService client, InstanceContent content) {
        super(new JaxWsClientChildren(jaxWsSupport, client), new AbstractLookup(content));
        this.jaxWsSupport=jaxWsSupport;
        this.client=client;
        this.content = content;
        content.add(this);
        content.add(client);
        content.add(jaxWsSupport);
        final WsdlModeler modeler = getWsdlModeler();
        if (modeler!=null) {
            changeIcon();
            modeler.generateWsdlModel(new WsdlModelListener(){
                @Override
                public void modelCreated(WsdlModel model) {
                    modelGenerationFinished=true;
                    changeIcon();
                    if (modeler.getCreationException() == null && model != null) {
                        wsdlModel = model;
                    }
                }
            });
        }
//        if (wsdlFileObject != null) {
//            setName(wsdlFileObject.getName());
//            setDisplayName(wsdlFileObject.getName());
//        }
          content.add(new EditWSAttributesCookieImpl(this));
//        setValue("wsdl-url",client.getWsdlUrl());
    }
    
    public WsdlModel getWsdlModel(){
        return this.getWsdlModeler().getAndWaitForWsdlModel();
    }

    @Override
    public String getName() {
        //return wsdlFileObject.getName();
        return client.getId();
    }
    
    @Override
    public String getDisplayName() {
        //return wsdlFileObject.getName();
        return client.getId();
    }
    
    @Override
    public String getShortDescription() {
        return client.getLocalWsdl();
    }
    
    private static final String WAITING_BADGE = "org/netbeans/modules/maven/jaxws/resources/waiting.png"; // NOI18N
    private static final String ERROR_BADGE = "org/netbeans/modules/maven/jaxws/resources/error-badge.gif"; //NOI18N
    private static final String SERVICE_BADGE = "org/netbeans/modules/maven/jaxws/resources/XMLServiceDataIcon.png"; //NOI18N

    private java.awt.Image cachedWaitingBadge;
    private java.awt.Image cachedErrorBadge;
    private java.awt.Image cachedServiceBadge;
    
    @Override
    public java.awt.Image getIcon(int type) {
        if (wsdlModel != null) {
            return getServiceImage();
        } else {
            WsdlModeler wsdlModeler = getWsdlModeler();
            if (wsdlModeler!=null && wsdlModeler.getCreationException()==null) {
                if (modelGenerationFinished)
                    return getServiceImage();
                else
                    return ImageUtilities.mergeImages(getServiceImage(), getWaitingBadge(), 15, 8);
            } else {
                java.awt.Image dirtyNodeImage = ImageUtilities.mergeImages(getServiceImage(), getErrorBadge(), 6, 6);
                if (modelGenerationFinished)
                    return dirtyNodeImage;
                else
                    return ImageUtilities.mergeImages(dirtyNodeImage, getWaitingBadge(), 15, 8);
            }
        }
    }
    
    private java.awt.Image getServiceImage() {
        if (cachedServiceBadge == null) {
            cachedServiceBadge = ImageUtilities.loadImage(SERVICE_BADGE);
        }            
        return cachedServiceBadge;        
    }
    private java.awt.Image getErrorBadge() {
        if (cachedErrorBadge == null) {
            cachedErrorBadge = ImageUtilities.loadImage(ERROR_BADGE);
        }            
        return cachedErrorBadge;        
    }
    private java.awt.Image getWaitingBadge() {
        if (cachedWaitingBadge == null) {
            cachedWaitingBadge = ImageUtilities.loadImage(WAITING_BADGE);
        }            
        return cachedWaitingBadge;        
    }
    
    @Override
    public java.awt.Image getOpenedIcon(int type){
        return getIcon( type);
    }
    
    @Override
    public void open() {
        EditCookie ec = getEditCookie();
        if (ec != null) {
            ec.edit();
        }
    }
    
    void changeIcon() {
        fireIconChange();
    }

    private EditCookie getEditCookie() {
        try {
            FileObject wsdlFolder = jaxWsSupport.getWsdlFolder(false);
            if ( wsdlFolder == null ){
                return null;
            }
            FileObject wsdlFo =
                    wsdlFolder.getFileObject(client.getLocalWsdl());
            if (wsdlFo!=null) {
                DataObject dObj = DataObject.find(wsdlFo);
                return (EditCookie)dObj.getCookie(EditCookie.class);
            }
        } catch (java.io.IOException ex) {
            ErrorManager.getDefault().log(ex.getLocalizedMessage());
            return null;
        }
        return null;
    }
    
    @Override
    public Action getPreferredAction() {
        return SystemAction.get(OpenAction.class);
    }
    
    // Create the popup menu:
    @Override
    public Action[] getActions(boolean context) {
        ArrayList<Action> actions = new ArrayList<Action>(Arrays.asList(
            SystemAction.get(OpenAction.class),
            SystemAction.get(JaxWsRefreshAction.class),
//            null,
            SystemAction.get(WSEditAttributesAction.class),
            SystemAction.get(ConfigureHandlerAction.class),
            null,
            SystemAction.get(DeleteAction.class),
            null,
            SystemAction.get(PropertiesAction.class)));
        addFromLayers(actions, "WebServices/Clients/Actions");
        return actions.toArray(new Action[0]);
    }
    
    private void addFromLayers(ArrayList<Action> actions, String path) {
        Lookup look = Lookups.forPath(path);
        for (Object next : look.lookupAll(Object.class)) {
            if (next instanceof Action) {
                actions.add((Action) next);
            } else if (next instanceof javax.swing.JSeparator) {
                actions.add(null);
            }
        }
    }

    @Override
    public HelpCtx getHelpCtx() {
        return HelpCtx.DEFAULT_HELP;
    }
    
    // Handle deleting:
    @Override
    public boolean canDestroy() {
        return true;
    }

    @Override
    public void destroy() throws java.io.IOException {
        FileObject wsdlFolder = jaxWsSupport.getWsdlFolder(false);
        if (wsdlFolder != null) {
            Project project = FileOwnerQuery.getOwner(wsdlFolder);
            if (project != null) {
                final String clientId = client.getId();

                // remove entry from wsimport configuration
                final ModelOperation<POMModel> oper = new ModelOperation<POMModel>() {
                    @Override
                    public void performOperation(POMModel model) {
                        MavenModelUtils.removeWsimportExecution(model, clientId);
                    }
                };
                final FileObject pom = project.getProjectDirectory().getFileObject("pom.xml"); //NOI18N
                RequestProcessor.getDefault().post(new Runnable() {

                    @Override
                    public void run() {
                        Utilities.performPOMModelOperations(pom, Collections.singletonList(oper));
                    }

                });

                // remove wsdl file
                if (wsdlFileObject != null) {
                    // check if there are other clients/services with the same wsdl
                    boolean hasOtherServices = false;
                    List<JaxWsService> services = jaxWsSupport.getServices();
                    for (JaxWsService s : services) {
                        if (clientId != null && !clientId.equals(s.getId()) && client.getLocalWsdl().equals(s.getLocalWsdl())) {
                            hasOtherServices = true;
                            break;
                        }
                    }
                    if (!hasOtherServices) {
                        // remove wsdl file
                        wsdlFileObject.delete();
                    }
                }

                // remove stale file
                try {
                    removeStaleFile(project, clientId);
                } catch (IOException ex) {
                    Logger.getLogger(JaxWsClientNode.class.getName()).log(
                            Level.FINE, "Cannot remove stale file", ex); //NOI18N
                }
            }
            super.destroy();
        }
    }
    
//    private void removeWsdlFolderContents(){
//        FileObject wsdlFolder = getJAXWSClientSupport().getLocalWsdlFolderForClient(getName(), false);
//        if(wsdlFolder != null){
//            FileLock lock = null;
//            
//            FileObject[] files = wsdlFolder.getChildren();
//            for(int i = 0; i < files.length; i++){
//                try{
//                    FileObject file = files[i];
//                    lock = file.lock();
//                    file.delete(lock);
//                }catch(IOException e){
//                    ErrorManager.getDefault().notify(e);
//                } 
//                finally{
//                    if(lock != null){
//                        lock.releaseLock();
//                        lock = null;
//                    }
//                }
//            }
//        }
//    }
    
    
    @Override
    public void configureHandler() {
        Project project = FileOwnerQuery.getOwner(wsdlFileObject);
        ArrayList<String> handlerClasses = new ArrayList<String>();
        BindingsModel bindingsModel = getBindingsModel();
        if(bindingsModel != null){  //if there is an existing bindings file, load it
            GlobalBindings gb = bindingsModel.getGlobalBindings();
            if(gb != null){
                DefinitionsBindings db = gb.getDefinitionsBindings();
                if(db != null){
                    BindingsHandlerChains handlerChains = db.getHandlerChains();
                    //there is only one handler chain
                    BindingsHandlerChain handlerChain =
                            handlerChains.getHandlerChains().iterator().next();
                    Collection<BindingsHandler> handlers = handlerChain.getHandlers();
                    for(BindingsHandler handler : handlers){
                        BindingsHandlerClass handlerClass = handler.getHandlerClass();
                        handlerClasses.add(handlerClass.getClassName());
                    }
                }
            }
        }
        final MessageHandlerPanel panel = new MessageHandlerPanel(project,
                handlerClasses, true, client.getServiceName());
        String title = NbBundle.getMessage(JaxWsNode.class,"TTL_MessageHandlerPanel");
        DialogDescriptor dialogDesc = new DialogDescriptor(panel, title);
        dialogDesc.setButtonListener(new ClientHandlerButtonListener(panel,
                bindingsModel, client, this));
        DialogDisplayer.getDefault().notify(dialogDesc);
    }
    
    WsdlModeler getWsdlModeler() {
        if (getLocalWsdl()!=null) {
            WsdlModeler modeler = WsdlModelerFactory.getDefault().getWsdlModeler(wsdlFileObject.toURL());
            if (modeler!=null) {
//              String packageName = client.getPackageName();
//              if (packageName!=null && client.isPackageNameForceReplace()) {
//                  // set the package name for the modeler
//                  modeler.setPackageName(packageName);
//              } else {
//                  modeler.setPackageName(null);
//              }
                modeler.setCatalog(jaxWsSupport.getCatalog());
//              setBindings(modeler);
                return modeler;
            }
        } else {
            ErrorManager.getDefault().log(ErrorManager.ERROR, NbBundle.getMessage(JaxWsNode.class,"ERR_missingLocalWsdl"));
        }
        return null;
    }
    
    FileObject getLocalWsdl() {
        if (wsdlFileObject==null) {
            FileObject localWsdlocalFolder = jaxWsSupport.getWsdlFolder(false);
            if (localWsdlocalFolder!=null) {
                String relativePath = client.getLocalWsdl();
                if (relativePath != null) {
                    wsdlFileObject=localWsdlocalFolder.getFileObject(relativePath);
                }
            }
        }
        return wsdlFileObject;
    }
    
    void setModelGenerationFinished(boolean value) {
        modelGenerationFinished=value;
    }

    @Override
    public void refreshService(boolean replaceLocalWsdl) {
        if (replaceLocalWsdl) {
            String wsdlUrl = client.getWsdlUrl();
            if (wsdlUrl == null) {
                if (wsdlFileObject != null) {
                    Project project = FileOwnerQuery.getOwner(wsdlFileObject);
                    Preferences prefs = ProjectUtils.getPreferences(project, MavenWebService.class,true);
                    if (prefs != null) {
                        wsdlUrl = prefs.get(MavenWebService.CLIENT_PREFIX+client.getId(), null);
                        if (wsdlUrl != null) {
                            client.setWsdlUrl(wsdlUrl);
                        }
                    }
                }
            }
            RefreshClientDialog.Result result = RefreshClientDialog.open(true, wsdlUrl);
            if (RefreshClientDialog.Result.CLOSE.equals(result)) {
                return;
            } else if (RefreshClientDialog.Result.REFRESH_ONLY.equals(result)) {
                updateNode();               
            } else {
                // replace local wsdl with downloaded version
                FileObject localWsdlFolder = jaxWsSupport.getWsdlFolder(true);
                if (localWsdlFolder != null) {
                    String newWsdlUrl = result.getWsdlUrl();
                    boolean wsdlUrlChanged = false;
                    if (newWsdlUrl.length() > 0 && !newWsdlUrl.equals(wsdlUrl)) {
                        wsdlUrlChanged = true;
                    }
                    FileObject wsdlFo = null;
                    try {
                        wsdlFo = WSUtils.retrieveResource(
                                localWsdlFolder,
                                new URI(MavenJAXWSSupportImpl.CATALOG_PATH),
                                new URI(newWsdlUrl));
                    } catch (URISyntaxException ex) {
                        //ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, ex);
                        String mes = NbBundle.getMessage(JaxWsClientCreator.class, "ERR_IncorrectURI", wsdlUrl); // NOI18N
                        NotifyDescriptor desc = new NotifyDescriptor.Message(mes, NotifyDescriptor.Message.ERROR_MESSAGE);
                        DialogDisplayer.getDefault().notify(desc);
                    } catch (UnknownHostException ex) {
                        //ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, ex);
                        String mes = NbBundle.getMessage(JaxWsClientCreator.class, "ERR_UnknownHost", ex.getMessage()); // NOI18N
                        NotifyDescriptor desc = new NotifyDescriptor.Message(mes, NotifyDescriptor.Message.ERROR_MESSAGE);
                        DialogDisplayer.getDefault().notify(desc);
                    } catch (IOException ex) {
                        //ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, ex);
                        String mes = NbBundle.getMessage(JaxWsClientCreator.class, "ERR_WsdlRetrieverFailure", wsdlUrl); // NOI18N
                        NotifyDescriptor desc = new NotifyDescriptor.Message(mes, NotifyDescriptor.Message.ERROR_MESSAGE);
                        DialogDisplayer.getDefault().notify(desc);
                    }
                    if (wsdlFo != null) {
                        final String relativePath = FileUtil.getRelativePath(localWsdlFolder, wsdlFo);
                        Project project = FileOwnerQuery.getOwner(wsdlFo);

                        final String oldId = client.getId();
                        List<JaxWsService> servicesToCheck = new ArrayList<JaxWsService>();
                        for (JaxWsService s : jaxWsSupport.getServices()) {
                            String serviceId = s.getId();
                            if (serviceId != null && !serviceId.equals(oldId)) {
                                servicesToCheck.add(s);
                            }
                        }
                        final String newId = WSUtils.getUniqueId(wsdlFo.getName(), servicesToCheck);

                        // update wsdl URL property
                        if (wsdlUrlChanged) {
                            wsdlUrl = newWsdlUrl;
                            client.setWsdlUrl(wsdlUrl);
                            Preferences prefs = ProjectUtils.getPreferences(project, MavenWebService.class,true);
                            if (prefs != null) {
                                prefs.remove(MavenWebService.CLIENT_PREFIX+oldId);
                                prefs.put(MavenWebService.CLIENT_PREFIX+newId, newWsdlUrl);
                            }
                        }

                        if (!relativePath.equals(client.getLocalWsdl())) {
                            wsdlFileObject = wsdlFo;
                            // update project's pom.xml
                            ModelOperation<POMModel> oper = new ModelOperation<POMModel>() {
                                @Override
                                public void performOperation(POMModel model) {
                                    MavenModelUtils.renameWsdlFile(model, oldId, newId, client.getLocalWsdl(), relativePath);
                                }
                            };
                            FileObject pom = project.getProjectDirectory().getFileObject("pom.xml"); //NOI18N
                            Utilities.performPOMModelOperations(pom, Collections.singletonList(oper));
                            // remove stale file
                            try {
                                removeStaleFile(project, oldId);
                            } catch (IOException ex) {
                                Logger.getLogger(JaxWsClientNode.class.getName()).log(
                                        Level.FINE, "Cannot remove stale file", ex); //NOI18N
                            }
                        }
                    } // endif
                    updateNode();
                } // endif
            } //end if-else
        } else {
            DialogDisplayer.getDefault().notify(new NotifyDescriptor.Message(
                    NbBundle.getMessage(JaxWsClientNode.class, "MSG_RefreshClient")));        
            updateNode();
        }
        
        if (wsdlFileObject != null) {
            // execute wsimport goal
            Project project = FileOwnerQuery.getOwner(wsdlFileObject);
            RunConfig cfg = RunUtils.createRunConfig(FileUtil.toFile(
                    project.getProjectDirectory()),
                    project,
                    "JAX-WS:wsimport", //NOI18N
                    Collections.singletonList("compile")); //NOI18N

            RunUtils.executeMaven(cfg);
        }
    }
    
    private void updateNode() {
        final WsdlModeler wsdlModeler = getWsdlModeler();
        if (wsdlModeler != null) {
            wsdlModeler.generateWsdlModel(new WsdlModelListener() {

                @Override
                public void modelCreated(WsdlModel model) {
                    wsdlModel = model;
                    setModelGenerationFinished(true);
                    changeIcon();
                    if (model == null) {
                        DialogDisplayer.getDefault().notify(
                                new WsImportFailedMessage(false, wsdlModeler.getCreationException()));
                    }
                    ((JaxWsClientChildren)getChildren()).setWsdlModel(wsdlModel);
                    ((JaxWsClientChildren)getChildren()).updateKeys();
                }
            });
        }
    }

    private BindingsModel getBindingsModel(){
        String handlerBindingFile = client.getHandlerBindingFile();
        BindingsModel bindingsModel = null;

        //if there is an existing handlerBindingFile, load it
        try{
            if(handlerBindingFile != null){
                FileObject bindingsFolder = jaxWsSupport.getBindingsFolder(false);
                if(bindingsFolder != null){
                    FileObject handlerBindingFO = bindingsFolder.getFileObject(handlerBindingFile);
                    if(handlerBindingFO != null){
                        ModelSource ms = org.netbeans.modules.xml.retriever.catalog.Utilities.getModelSource(handlerBindingFO, true);
                        bindingsModel =  BindingsModelFactory.getDefault().getModel(ms);
                    }
                }
            }
        } catch(Exception e){
            ErrorManager.getDefault().notify(e);
            return null;
        }
        return bindingsModel;
    }

    private void removeStaleFile(Project prj, String name) throws IOException {
        FileObject staleFile = prj.getProjectDirectory().getFileObject("target/jaxws/stale/"+name+".stale");
        if (staleFile != null) {
            staleFile.delete();
        }
    }
 
}
