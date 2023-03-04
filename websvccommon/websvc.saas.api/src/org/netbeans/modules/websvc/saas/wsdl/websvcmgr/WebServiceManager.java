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
package org.netbeans.modules.websvc.saas.wsdl.websvcmgr;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;


import org.netbeans.modules.websvc.jaxwsmodelapi.WSService;

import org.netbeans.modules.websvc.saas.model.SaasServicesModel;
import org.netbeans.modules.websvc.saas.model.wsdl.impl.WsdlModel;
import org.netbeans.modules.websvc.saas.model.wsdl.impl.WsdlModeler;
import org.netbeans.modules.websvc.saas.model.wsdl.impl.WsdlModelerFactory;
import org.netbeans.modules.websvc.saas.spi.websvcmgr.WsdlData;
import org.netbeans.modules.websvc.saas.util.WsdlUtil;

import org.netbeans.modules.xml.retriever.Retriever;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.filesystems.FileLock;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;

public final class WebServiceManager {

    private static WebServiceManager wsMgr;
    public static String WEBSVC_HOME = SaasServicesModel.WEBSVC_HOME;

    private WebServiceManager() {
    }

    public static WebServiceManager getInstance() {
        if (wsMgr == null) {
            wsMgr = new WebServiceManager();
        }
        return wsMgr;
    }

    public void addWebService(WsdlDataImpl wsData) throws IOException {
        addWebService(wsData, false);
    }

    /**
     * Add webservice to the Web Service List Model.
     * @param wsData The WebServiceData to add
     * @param compile true if the client should be compiled immediately; ignored if client has already been compiled
     * 
     * @throws java.io.IOException 
     */
    public void addWebService(WsdlDataImpl wsData, boolean compile) throws IOException {

        if (wsData.getStatus().equals(WsdlData.Status.WSDL_UNRETRIEVED) ||
                (wsData.getStatus().equals(WsdlData.Status.WSDL_RETRIEVING) && !wsData.isReady())) {
            wsData.setStatus(WsdlData.Status.WSDL_RETRIEVING);

            File localWsdlFile = null;
            try {
                localWsdlFile = copyWsdlResources(wsData.getOriginalWsdlUrl());
            } catch (IOException ex) {
                wsData.setStatus(WsdlData.Status.WSDL_UNRETRIEVED);
                throw ex;
            }

            wsData.setWsdlFile(localWsdlFile.getAbsolutePath());
            wsData.setStatus(WsdlData.Status.WSDL_RETRIEVED);
        }

        assert wsData.getWsdlFile() != null;
        File localWsdlFile = new File(wsData.getWsdlFile());
        URL wsdlUrl = localWsdlFile.toURI().toURL();

        WsdlModeler wsdlModeler = WsdlModelerFactory.getDefault().getWsdlModeler(wsdlUrl);

        WsdlModel model = wsdlModeler.getAndWaitForWsdlModel();

        boolean dataInModel = WebServiceListManager.getInstance().wsdlDataExistsFor(wsData.getOriginalWsdlUrl());

        if (model == null) {
            removeWebService(wsData, true, false);

            Throwable exc = wsdlModeler.getCreationException();
            String message = NbBundle.getMessage(WebServiceManager.class, "WS_MODELER_ERROR");
            if (exc != null) {
                String cause = exc.getLocalizedMessage();
                String excString = exc.getClass().getName() + " - " + cause;
                message += "\n\n" + excString; // NOI18N

                Exceptions.printStackTrace(Exceptions.attachLocalizedMessage(exc, message));
            } else {
                exc = new IllegalStateException(message);
                Exceptions.printStackTrace(exc);
            }

            return;
        } else if (model.getServices().isEmpty()) {
            // If there are no services in the WSDL, warn the user
            removeWebService(wsData, true, true);
            String message = NbBundle.getMessage(WebServiceManager.class, "WS_NO_METHODS_ERROR");
            NotifyDescriptor d = new NotifyDescriptor.Message(message);
            DialogDisplayer.getDefault().notify(d);
            return;
        } else if (dataInModel) {
            // for adding of services already in the model, only add a single service
            boolean assigned = false;
            for (WSService service : model.getServices()) {
                if (service.getName().equals(wsData.getName())) {
                    assigned = true;
                    wsData.setWsdlService(service);
                    wsData.setResolved(true);
                    break;
                }
            }

            if (!assigned) {
                WSService defaultService = model.getServices().get(0);
                wsData.setWsdlService(defaultService);
                wsData.setName(defaultService.getName());
                wsData.setResolved(true);
            }

        } else {
            boolean first = true;
            for (WSService service : model.getServices()) {
                WsdlDataImpl newData;
                if (first) {
                    first = false;
                    newData = wsData;
                } else {
                    newData = new WsdlDataImpl(wsData.getOriginalWsdlUrl());
                }
                newData.setWsdlService(service);
                newData.setName(service.getName());
                newData.setResolved(true);
                WebServiceListManager.getInstance().addWsdlData(newData);
            }
        }
    }

    public void refreshWebService(WsdlDataImpl wsData) throws IOException {
        removeWebService(wsData, false, true);
        wsData.setWsdlFile(null);
        wsData.setStatus(WsdlData.Status.WSDL_RETRIEVING);
        wsData.setWsdlService(null);
        addWebService(wsData, true);
    }

    /**
     * Removes a webservice from the Web Service List Model.
     * Client jars and other data are deleted from the filesystem.
     * 
     * @param wsData the WebService to remove
     */
    public void removeWebService(WsdlDataImpl wsData) {
        removeWebService(wsData, true, true);
    }

    private void removeWebService(WsdlDataImpl wsData, boolean removeFromModel, boolean deleteWsdl) {

        if (removeFromModel) {
            WebServiceListManager mgr = WebServiceListManager.getInstance();
            if (mgr.wsdlDataExistsFor(wsData.getOriginalWsdlUrl())) {
                mgr.removeWsdlData(wsData);
            }
        }

        if (wsData.getWsdlFile() == null) {
            return;
        }

        if (deleteWsdl) {
            // remove the top-level wsdl file
            rmDir(new File(wsData.getWsdlFile()));
        }
    }

    private static void rmDir(File dir) {
        if (dir == null) {
            return;
        }

        FileObject fo = FileUtil.toFileObject(dir);
        if (fo != null) {
            FileLock lock = null;
            try {
                lock = fo.lock();
                fo.delete(lock);
            } catch (IOException ex) {
                Exceptions.printStackTrace(ex);
            } finally {
                if (lock != null) {
                    lock.releaseLock();
                }
            }
        }
    }

    static File copyWsdlResources(String wsdlUrl) throws IOException {
        File userDirFile = new File(WEBSVC_HOME);
        File catalogFile = new File(userDirFile, WsdlUtil.getCatalogForWsdl(wsdlUrl));
        File dir = catalogFile.getParentFile();

        boolean success = false;
        dir = catalogFile.getParentFile();
        try {
            FileObject dirFO = FileUtil.createFolder(dir);
            URI catalog = catalogFile.toURI();
            URI wsdlUri = new URL(wsdlUrl).toURI();

            Retriever retriever = Retriever.getDefault();
            FileObject wsdlFO = retriever.retrieveResource(dirFO, catalog, wsdlUri);

            if (wsdlFO == null) {
                throw new IOException(NbBundle.getMessage(WebServiceManager.class, "WSDL_COPY_ERROR"));
            }
            File result = FileUtil.toFile(wsdlFO);
            success = true;
            return result;
        } catch (URISyntaxException ex) {
            throw new IOException(ex.getLocalizedMessage());
        } finally {
            if (catalogFile.exists() && !success) {
                rmDir(catalogFile.getParentFile());
            }
        }
    }
}
