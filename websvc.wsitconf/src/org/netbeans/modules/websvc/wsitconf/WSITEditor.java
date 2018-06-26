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
 * Software is Sun Microsystems, Inc. Portions Copyright 2006 Sun
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

package org.netbeans.modules.websvc.wsitconf;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Collection;
import javax.swing.undo.UndoManager;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.modules.websvc.api.jaxws.client.JAXWSClientSupport;
import org.netbeans.modules.websvc.api.jaxws.project.config.Client;
import org.netbeans.modules.websvc.api.jaxws.project.config.JaxWsModel;
import org.netbeans.modules.websvc.api.jaxws.project.config.Service;
import org.netbeans.modules.websvc.jaxws.api.JAXWSSupport;
import org.netbeans.modules.websvc.wsitconf.ui.ErrorTopComponent;
import org.netbeans.modules.websvc.wsitconf.ui.service.ServiceTopComponent;
import org.netbeans.modules.websvc.wsitconf.ui.client.ClientTopComponent;
import org.netbeans.modules.websvc.wsitconf.util.UndoManagerHolder;
import org.netbeans.modules.websvc.wsitconf.wsdlmodelext.WSITModelSupport;
import org.netbeans.modules.xml.retriever.catalog.Utilities;
import org.netbeans.modules.xml.wsdl.model.*;
import org.netbeans.modules.xml.xam.locator.CatalogModelException;
import org.openide.cookies.SaveCookie;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;
import org.openide.nodes.Node;
import org.openide.util.NbBundle;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.LinkedList;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JComponent;
import org.netbeans.api.java.project.JavaProjectConstants;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.api.project.Sources;
import org.netbeans.modules.websvc.api.wseditor.WSEditor;
import org.netbeans.modules.websvc.wsitconf.spi.WsitProvider;
import org.netbeans.modules.xml.xam.ModelSource;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.filesystems.FileLock;
import org.openide.loaders.DataObjectNotFoundException;

/**
 *
 * @author Martin Grebac
 */
public class WSITEditor implements WSEditor, UndoManagerHolder {

    private static final Logger logger = Logger.getLogger(WSITEditor.class.getName());
    private JaxWsModel jaxWsModel;
    private UndoManager undoManager;
    private Collection<FileObject> createdFiles = new LinkedList<FileObject>();
            
    /**
     * Creates a new instance of WSITEditor
     */
    public WSITEditor(JaxWsModel jaxWsModel) {
        this.jaxWsModel = jaxWsModel;
    }

    public String getTitle() {
        return NbBundle.getMessage(WSITEditor.class, "QOS_EDITOR_TITLE"); //NOI18N
    }

    public JComponent createWSEditorComponent(Node node) {

        WSDLModel clientWsdlModel;
        WSDLModel wsdlModel;

        //is it a client node?
        Client client = node.getLookup().lookup(Client.class);
        //is it a service node?
        Service service = node.getLookup().lookup(Service.class);
        
        final Project p;
        if (jaxWsModel != null) {
            p = FileOwnerQuery.getOwner(jaxWsModel.getJaxWsFile());
        } else {
            p = null;
        }
        
        if (client != null){ //its a client
            if (p != null) {
                final JAXWSClientSupport wscs = JAXWSClientSupport.getJaxWsClientSupport(p.getProjectDirectory());
                if (wscs != null) {
                    PropertyChangeListener jaxWsClientListener = new PropertyChangeListener() {
                        public void propertyChange(PropertyChangeEvent evt) {
                            if (evt != null) {
                                Object newV = evt.getNewValue();
                                Object oldV = evt.getOldValue();
                                if ((oldV != null) && (newV == null)) {  //being removed
                                    if (oldV instanceof Client) {
                                        Client c = (Client)oldV;
                                        
                                    }
                                }
                            }
                        }
                    };
                    jaxWsModel.addPropertyChangeListener(jaxWsClientListener);
                    try {
                        clientWsdlModel = WSITModelSupport.getModel(node, jaxWsModel, this, true, createdFiles);
                        if (clientWsdlModel == null) {
                            return new ErrorTopComponent(NbBundle.getMessage(WSITEditor.class, "TXT_WSIT_ClientWsdlNotFound"));
                        }
                        wsdlModel = WSITModelSupport.getServiceModelForClient(wscs, client);
                        return new ClientTopComponent(client, jaxWsModel, clientWsdlModel, wsdlModel, node);
                    } catch (Exception e) {
                        logger.log(Level.SEVERE, null, e);
                    }
                }
            }
        } else {
            if (p != null) {
                final JAXWSSupport wss = JAXWSSupport.getJAXWSSupport(p.getProjectDirectory());
                if (wss != null) {
                    JaxWsModel.ServiceListener jaxWsServiceListener = new JaxWsModel.ServiceListener() {
                        public void serviceAdded(String name, String implementationClass) {}
                        public void serviceRemoved(String name) {
                            if (!wss.isFromWSDL(name)) {
                                JaxWsModel jaxWsModel = p.getLookup().lookup(JaxWsModel.class);
                                Service s = jaxWsModel.findServiceByName(name);
                                String implClass = s.getImplementationClass();
                                String configWsdlName = WSITModelSupport.CONFIG_WSDL_SERVICE_PREFIX + implClass;
                                if ((implClass != null) && (implClass.length() > 0)) {
                                    try {
                                        if (wss.getWsdlFolder(true) != null) {
                                            FileObject wsdlFO = wss.getWsdlFolder(
                                                    true).getParent().getFileObject(configWsdlName, WSITModelSupport.CONFIG_WSDL_EXTENSION);
                                            if ((wsdlFO != null) && (wsdlFO.isValid())) {   //NOI18N
                                                FileLock lock = null;
                                                try {
                                                    lock = wsdlFO.lock();
                                                    wsdlFO.delete(lock);
                                                } finally {
                                                    if (lock != null) {
                                                        lock.releaseLock();
                                                    }
                                                }
                                            }
                                        }
                                    } catch (IOException e) {
                                        // burn
                                    }
                                }
                            }
                        }
                    };
                    jaxWsModel.addServiceListener(jaxWsServiceListener);                    
                    try {
                        wsdlModel = WSITModelSupport.getModel(node, jaxWsModel, this, true, createdFiles);
                        return new ServiceTopComponent(service, jaxWsModel, wsdlModel, node, getUndoManager());
                    } catch(Exception e){
                        logger.log(Level.SEVERE, null, e);
                    }
                }
            }
        }
        return new ErrorTopComponent(NbBundle.getMessage(WSITEditor.class, "TXT_WSIT_NotSupported"));
    }

    @Override
    public void save(Node node) {
        if (node == null) {
            return;
        }
        try {
            WSDLModel model = WSITModelSupport.getModel(node, jaxWsModel, this, false, createdFiles);
            if (model != null) {
                WSITModelSupport.save(model);
            }
            else {
                NotifyDescriptor descriptor = new NotifyDescriptor.Message(
                        NbBundle.getMessage(WSITEditor.class, "TXT_NO_WSDL_FILE"),  // NOI18N
                        NotifyDescriptor.ERROR_MESSAGE);
                DialogDisplayer.getDefault().notify( descriptor );
            }
        } catch (Exception e){
            logger.log(Level.SEVERE, null, e);
        }
    }

    public void cancel(Node node) {
        if (node == null) return;
        WSDLModel model = null;
        
        // remove imports from main config file if a new config file was imported
        FileObject srcRoot = node.getLookup().lookup(FileObject.class);
        if (srcRoot != null) {
            Project p = FileOwnerQuery.getOwner(srcRoot);
            if (p != null) {
                FileObject clientConfigFolder = getClientConfigFolder(p);
                WSDLModel mainModel = WSITModelSupport.getMainClientModel(clientConfigFolder);
                if (mainModel != null) {
                    Collection<Import> imports = mainModel.getDefinitions().getImports();
                    for (Import i : imports) {
                        try {
                            WSDLModel importedModel = i.getImportedWSDLModel();
                            ModelSource importedms = importedModel.getModelSource();
                            FileObject importedfo = org.netbeans.modules.xml.retriever.catalog.Utilities.getFileObject(importedms);
                            mainModel.startTransaction();
                            if (createdFiles.contains(importedfo)) {
                                mainModel.getDefinitions().removeImport(i);
                            }
                            mainModel.endTransaction();
                            FileObject mainFO = Utilities.getFileObject(mainModel.getModelSource());
                            if (mainFO == null) {
                                logger.log(Level.INFO, "Cannot find fileobject in lookup for: " + mainModel.getModelSource());
                            }
                            try {
                                DataObject mainDO = DataObject.find(mainFO);
                                if ((mainDO != null) && (mainDO.isModified())) {
                                    SaveCookie wsdlSaveCookie = mainDO.getCookie(SaveCookie.class);
                                    if(wsdlSaveCookie != null){
                                        wsdlSaveCookie.save();
                                    }
                                    mainDO.setModified(false);
                                }
                            } catch (IOException ioe) {
                                // ignore - just don't do anything
                            }
                        } catch (IllegalStateException ex) {
                            logger.log(Level.SEVERE, null, ex);
                        } catch (CatalogModelException ex) {
                            logger.log(Level.SEVERE, null, ex);
                        }
                    }
                }
            }
        }
        
        // now first undo all edits in the files
        try {
            model = WSITModelSupport.getModel(node, jaxWsModel, this, false, createdFiles);
        } catch (MalformedURLException ex) {
            ex.printStackTrace();
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        if (model != null) {
            try {
                if (getUndoManager() != null) {
                    while (getUndoManager().canUndo()) {
                        getUndoManager().undo();
                    }
                }
            } catch (Exception e){
                logger.log(Level.INFO, null, e);
            }
            FileObject fo = org.netbeans.modules.xml.retriever.catalog.Utilities.getFileObject(model.getModelSource());
            DataObject dO = null;
            try {
                dO = DataObject.find(fo);
            } catch (DataObjectNotFoundException ex) {
                logger.log(Level.SEVERE, null, ex);
            }
            if (dO != null) {
                try {
                    model.sync();
                } catch (IOException ex) {
                    logger.log(Level.SEVERE, null, ex);
                }
                dO.setModified(false);dO.setModified(true);dO.setModified(false);
            }
        }
        
        // and remove all created files during this run
        if ((createdFiles != null) && (createdFiles.size() > 0)) {
            for (FileObject fo : createdFiles) {
                if (fo != null) {
                    try {
                        DataObject dO = DataObject.find(fo);
                        if (dO != null) {
                            dO.delete();
                        }
                    } catch (IOException ex) {
                        logger.log(Level.SEVERE, null, ex);
                    }
                }
            }
        }
    }

    public static FileObject getClientConfigFolder(Project p) {

        FileObject folder = null;

        WsitProvider wsitProvider = p.getLookup().lookup(WsitProvider.class);
        if (wsitProvider != null) {
            folder = wsitProvider.getConfigFilesFolder(true);
        }

        if (folder == null) {
            // proceed with default folder (META-INF) if the provider is not found
            Sources sources = ProjectUtils.getSources(p);
            if (sources == null) return null;
            SourceGroup[] sourceGroups = sources.getSourceGroups(JavaProjectConstants.SOURCES_TYPE_JAVA);
            if ((sourceGroups != null) && (sourceGroups.length > 0)) {
                folder = sourceGroups[0].getRootFolder();
                if (folder != null) {
                    folder = folder.getFileObject("META-INF");
                }
                if ((folder == null) || (!folder.isValid())) {
                    try {
                        folder = sourceGroups[0].getRootFolder().createFolder("META-INF");
                    } catch (IOException ex) {
                        logger.log(Level.SEVERE, null, ex);
                    }
                }
            }
        }
        return folder;
    }
    
    public UndoManager getUndoManager() {
        return undoManager;
    }

    public void setUndoManager(UndoManager undoManager) {
        this.undoManager = undoManager;
    }

    public String getDescription() {
        return NbBundle.getMessage(WSITEditor.class, "WSIT_CONFIG_DESC");
    }
}
