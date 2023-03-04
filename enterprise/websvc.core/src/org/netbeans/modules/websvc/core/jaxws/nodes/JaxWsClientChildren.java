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
package org.netbeans.modules.websvc.core.jaxws.nodes;

/** WSDL children (Service elements)
 *
 * @author mkuchtiak
 */
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.tools.ant.module.api.support.ActionUtils;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.modules.j2ee.deployment.devmodules.spi.J2eeModuleProvider;
import org.netbeans.modules.websvc.api.jaxws.client.JAXWSClientSupport;
import org.netbeans.modules.websvc.api.jaxws.project.CatalogUtils;
import org.netbeans.modules.websvc.api.jaxws.project.config.Client;
import org.netbeans.modules.websvc.api.jaxws.wsdlmodel.WsdlModel;
import org.netbeans.modules.websvc.api.jaxws.wsdlmodel.WsdlModelListener;
import org.netbeans.modules.websvc.api.jaxws.wsdlmodel.WsdlModeler;
import org.netbeans.modules.websvc.api.jaxws.wsdlmodel.WsdlService;
import org.netbeans.modules.websvc.api.jaxws.project.WSUtils;
import org.netbeans.modules.websvc.core.JaxWsUtils;
import org.netbeans.modules.websvc.spi.jaxws.client.JAXWSClientSupportImpl;
import org.netbeans.spi.project.support.ant.GeneratedFilesHelper;
import org.openide.DialogDisplayer;
import org.openide.ErrorManager;
import org.openide.execution.ExecutorTask;
import org.openide.filesystems.FileLock;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.util.NbBundle;

public class JaxWsClientChildren extends Children.Keys<WsdlService> {

    Client client;
    WsdlModel wsdlModel;
    FileObject srcRoot;

    public JaxWsClientChildren(Client client, FileObject srcRoot) {
        this.client = client;
        this.srcRoot = srcRoot;
    }

    @Override
    protected void addNotify() {
        final WsdlModeler wsdlModeler = ((JaxWsClientNode) getNode()).getWsdlModeler();
        if (wsdlModeler != null) {
            JAXWSClientSupport clientSupport = JAXWSClientSupport.getJaxWsClientSupport(srcRoot);
            if (clientSupport != null) {
                wsdlModeler.setCatalog(clientSupport.getCatalog());
            }
            wsdlModel = wsdlModeler.getWsdlModel();
            if (wsdlModel == null) {
                wsdlModeler.generateWsdlModel(new WsdlModelListener() {

                    public void modelCreated(WsdlModel model) {
                        wsdlModel = model;
                        ((JaxWsClientNode) getNode()).changeIcon();
                        if (model == null) {
                            DialogDisplayer.getDefault().notify(
                                    new JaxWsUtils.WsImportClientFailedMessage(wsdlModeler.getCreationException()));
                        }
                        updateKeys();
                    }
                });
            } else {
                updateKeys();
            }
        }
    }

    @Override
    protected void removeNotify() {
        setKeys(Collections.<WsdlService>emptySet());
    }

    private void updateKeys() {
        List<WsdlService> keys = null;
        if (wsdlModel != null) {
            keys = wsdlModel.getServices();
        }
        setKeys(keys == null ? Collections.<WsdlService>emptyList() : keys);
    }

    protected Node[] createNodes(WsdlService key) {
        return new Node[]{new ServiceNode(key)};
    }

    void refreshKeys(boolean downloadWsdl) {
        this.refreshKeys(downloadWsdl, "");
    }

    void refreshKeys(boolean downloadWsdl, String newWsdlUrl) {
        super.addNotify();
        // copy to local wsdl first
        JAXWSClientSupport support = JAXWSClientSupport.getJaxWsClientSupport(srcRoot);
        final JaxWsClientNode clientNode = (JaxWsClientNode) getNode();
        if (downloadWsdl) {
            try {
                String clientName = clientNode.getName();
                String oldWsdlUrl = client.getWsdlUrl();
                boolean jaxWsModelChanged = false;
                FileObject localWsdl = null;
                if (newWsdlUrl.length() > 0 && !oldWsdlUrl.equals(newWsdlUrl)) {
                    localWsdl = WSUtils.retrieveResource(
                            support.getLocalWsdlFolderForClient(clientName, true),
                            new URI(newWsdlUrl));
                    jaxWsModelChanged = true;
                } else {
                    WSUtils.retrieveResource(
                            support.getLocalWsdlFolderForClient(clientName, true),
                            new URI(oldWsdlUrl));
                }

                if (jaxWsModelChanged) {
                    client.setWsdlUrl(newWsdlUrl);
                    FileObject xmlResourcesFo = support.getLocalWsdlFolderForClient(clientName, false);
                    if (xmlResourcesFo != null) {
                        String localWsdlUrl = FileUtil.getRelativePath(xmlResourcesFo, localWsdl);
                        client.setLocalWsdlFile(localWsdlUrl);
                    }

                    clientNode.getJaxWsModel().write();
                }
                // copy resources to WEB-INF[META-INF]/wsdl/
                FileObject sourceRoot = getNode().getLookup().lookup(FileObject.class);
                Project project = FileOwnerQuery.getOwner(srcRoot);
                FileObject xmlResorcesFo = support.getLocalWsdlFolderForClient(clientName, false);
                if (xmlResorcesFo != null) {
                    FileObject webInfWsdlFolder = support.getWsdlFolder(true);
                    if (webInfWsdlFolder != null) {
                        try {
                            FileObject jaxWsCatalog = webInfWsdlFolder.getParent().getFileObject("jax-ws-catalog.xml"); //NOI18N
                            FileObject catalog = project.getProjectDirectory().getFileObject(JAXWSClientSupportImpl.CATALOG_FILE);
                            if (jaxWsCatalog != null && catalog != null) {
                                CatalogUtils.copyCatalogEntriesForClient(catalog, jaxWsCatalog, clientName);
                            }
                            WSUtils.copyFiles(xmlResorcesFo, webInfWsdlFolder);
                        } catch (IOException ex) {
                            Logger.getLogger(JaxWsClientChildren.class.getName()).log(Level.INFO, "Cannot copy files to "+webInfWsdlFolder, ex);
                        }
                    }
                }
            } catch (URISyntaxException ex) {
                ErrorManager.getDefault().notify(ex);
            } catch (UnknownHostException ex) {
                ErrorManager.getDefault().annotate(ex,
                        NbBundle.getMessage(JaxWsClientChildren.class, "MSG_ConnectionProblem"));
                return;
            } catch (IOException ex) {
                ErrorManager.getDefault().annotate(ex,
                        NbBundle.getMessage(JaxWsClientChildren.class, "MSG_ConnectionProblem"));
                return;
            }

        }

        final WsdlModeler wsdlModeler = clientNode.getWsdlModeler();
        clientNode.setModelGenerationFinished(false);
        clientNode.changeIcon();
        if (wsdlModeler != null) {
            wsdlModeler.generateWsdlModel(new WsdlModelListener() {

                public void modelCreated(WsdlModel model) {
                    wsdlModel = model;
                    clientNode.setModelGenerationFinished(true);
                    clientNode.changeIcon();
                    if (model == null) {
                        DialogDisplayer.getDefault().notify(
                                new JaxWsUtils.WsImportClientFailedMessage(wsdlModeler.getCreationException()));
                    }
                    updateKeys();

                    if (model != null) {
                        Client client = clientNode.getJaxWsModel().findClientByName(clientNode.getName());
                        if (client != null) {
                            WsdlService wsdlService = null;
                            boolean jaxWsModelChanged = false;
                            List<WsdlService> wsdlServices = model.getServices();
                            if (wsdlServices != null && wsdlServices.size() > 0) {
                                wsdlService = wsdlServices.get(0);
                            }

                            // test if package name for java artifacts hasn't changed
                            String oldPkgName = client.getPackageName();
                            if (wsdlService != null && !client.isPackageNameForceReplace()) {
                                String javaName = wsdlService.getJavaName();
                                int dotPosition = javaName.lastIndexOf(".");
                                if (dotPosition >= 0) {
                                    String newPkgName = javaName.substring(0, dotPosition);
                                    if ((oldPkgName == null && newPkgName != null) || (!oldPkgName.equals(newPkgName))) {
                                        client.setPackageName(newPkgName);
                                        jaxWsModelChanged = true;
                                    }
                                }
                            }

                            // save jax-ws model
                            if (jaxWsModelChanged) {
                                try {
                                    clientNode.getJaxWsModel().write();
                                } catch (IOException ex) {
                                    ErrorManager.getDefault().notify(ErrorManager.ERROR, ex);
                                }
                            }
                        }
                    }
                }
            });
        }
        // re-generate java artifacts
        //FileObject sourceRoot = getNode().getLookup().lookup(FileObject.class);
        Project project = FileOwnerQuery.getOwner(srcRoot);
        if (project != null) {
            FileObject buildImplFo = project.getProjectDirectory().getFileObject(GeneratedFilesHelper.BUILD_XML_PATH);
            try {
                String name = client.getName();
                ExecutorTask wsimportTask =
                        ActionUtils.runTarget(buildImplFo,
                        new String[]{"wsimport-client-clean-" + name, "wsimport-client-generate"}, null); //NOI18N
                wsimportTask.waitFinished();
            } catch (IOException ex) {
                ErrorManager.getDefault().log(ex.getLocalizedMessage());
            } catch (IllegalArgumentException ex) {
                ErrorManager.getDefault().log(ex.getLocalizedMessage());
            }
        }
    }

    WsdlModel getWsdlModel() {
        return wsdlModel;
    }

//    private FileObject getWsdlFolderForClient(JAXWSClientSupport support, String name) throws IOException {
//        FileObject globalWsdlFolder = support.getWsdlFolder(true);
//        FileObject oldWsdlFolder = globalWsdlFolder.getFileObject("client/" + name);
//        if (oldWsdlFolder != null) {
//            FileLock lock = oldWsdlFolder.lock();
//            try {
//                oldWsdlFolder.delete(lock);
//            } finally {
//                lock.releaseLock();
//            }
//        }
//        FileObject clientWsdlFolder = globalWsdlFolder.getFileObject("client"); //NOI18N
//        if (clientWsdlFolder == null) {
//            clientWsdlFolder = globalWsdlFolder.createFolder("client");
//        } //NOI18N
//        return clientWsdlFolder.createFolder(name);
//    }
}
