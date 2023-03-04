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
package org.netbeans.modules.websvc.manager;

import org.netbeans.modules.websvc.manager.api.WebServiceDescriptor;
import org.netbeans.modules.websvc.manager.spi.WebServiceManagerExt;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Collection;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.modules.websvc.manager.codegen.Wsdl2Java;

import org.netbeans.modules.websvc.jaxwsmodelapi.WSService;
import org.netbeans.modules.websvc.jaxwsmodelapi.wsdlmodel.WsdlModel;
import org.netbeans.modules.websvc.jaxwsmodelapi.wsdlmodel.WsdlModelProvider;
import org.netbeans.modules.websvc.manager.model.WebServiceListModel;
import org.netbeans.modules.websvc.manager.model.WebServiceData;
import org.netbeans.modules.websvc.manager.model.WebServiceGroup;
import org.netbeans.modules.websvc.saas.spi.websvcmgr.WsdlServiceProxyDescriptor;
import org.netbeans.modules.websvc.saas.util.WsdlUtil;

import org.netbeans.modules.xml.retriever.Retriever;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.filesystems.FileLock;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;

public final class WebServiceManager {

    public static final String WEBSVC_HOME = WebServiceDescriptor.WEBSVC_HOME;
    private int mutatorCount;
    private static final WebServiceManager INSTANCE = new WebServiceManager();

    private WebServiceManager() {
    }

    public static WebServiceManager getInstance() {
        return INSTANCE;
    }

    public void addWebService(WebServiceData wsData) throws IOException {
        addWebService(wsData, false);
    }

    /**
     * Add webservice to the Web Service List Model.
     * @param wsData The WebServiceData to add
     * @param compile true if the client should be compiled immediately; ignored if client has already been compiled
     * 
     * @throws java.io.IOException 
     */
    public void addWebService(WebServiceData wsData, boolean compile) throws IOException {
        try {
            incrementMutatorCount();
            WebServiceListModel listModel = WebServiceListModel.getInstance();

            if (wsData.getState().equals(WebServiceData.State.WSDL_UNRETRIEVED) ||
                    (wsData.getState().equals(WebServiceData.State.WSDL_RETRIEVING) &&
                    !wsData.isResolved())) {
                wsData.setState(WebServiceData.State.WSDL_RETRIEVING);

                File localWsdlFile = null;
                try {
                    localWsdlFile = copyWsdlResources(wsData.getOriginalWsdlUrl());
                } catch (IOException ex) {
                    wsData.setState(WebServiceData.State.WSDL_UNRETRIEVED);
                    throw ex;
                }
                File catalogFile = new File(WEBSVC_HOME, WsdlUtil.getCatalogForWsdl(wsData.getOriginalWsdlUrl()));

                wsData.setWsdlFile(localWsdlFile.getAbsolutePath());
                wsData.setCatalog(catalogFile.getAbsolutePath());
                wsData.setState(WebServiceData.State.WSDL_RETRIEVED);
            }

            assert wsData.getWsdlFile() != null;

            WsdlModel wsdlModel = getWsdlModel(wsData);
            boolean dataInModel = listModel.webServiceExists(wsData);

            if (wsdlModel == null) {
                wsData.setResolved(false);
                removeWebService(wsData, true, false);
                return;
            } else if (wsdlModel.getServices().isEmpty()) {
                // If there are no services in the WSDL, warn the user
                removeWebService(wsData);
                String message = NbBundle.getMessage(WebServiceManager.class, "WS_NO_METHODS_ERROR");
                NotifyDescriptor d = new NotifyDescriptor.Message(message);
                DialogDisplayer.getDefault().notify(d);
                return;
            } else if (dataInModel) {
                // for adding of services already in the model, only add a single service
                boolean assigned = false;
                for (WSService service : wsdlModel.getServices()) {
                    if (service.getName().equals(wsData.getName())) {
                        assigned = true;
                        wsData.setWsdlService(service);
                        wsData.setResolved(true);
                    }
                }

                if (!assigned) {
                    WSService defaultService = wsdlModel.getServices().get(0);
                    wsData.setWsdlService(defaultService);
                    wsData.setName(defaultService.getName());
                    wsData.setResolved(true);
                }

                listModel.getWebServiceGroup(wsData.getGroupId()).modify(wsData.getId());

                try {
                    if (compile && !wsData.getState().equals(WebServiceData.State.WSDL_SERVICE_COMPILED)) {
                        compileService(wsData);
                    }
                } finally {
                    if (wsData.getState().equals(WebServiceData.State.WSDL_SERVICE_COMPILE_FAILED)) {
                        wsData.setResolved(false);
                    }
                }
            } else {
                // Create WebServiceData for each service and add to the model
                boolean first = true;
                for (WSService service : wsdlModel.getServices()) {
                    WebServiceData newData;
                    if (first) {
                        first = false;
                        newData = wsData;
                    } else {
                        newData = new WebServiceData(wsData.getWsdlFile(), wsData.getOriginalWsdlUrl(), wsData.getGroupId());
                    }

                    newData.setWsdlService(service);
                    newData.setResolved(true);
                    newData.setName(service.getName());


                    listModel.addWebService(newData);
                    WebServiceGroup group = listModel.getWebServiceGroup(newData.getGroupId());
                    if (group != null) {
                        group.add(newData.getId());
                    }

                    try {
                        if (compile && !newData.getState().equals(WebServiceData.State.WSDL_SERVICE_COMPILED)) {
                            compileService(newData);
                        }
                    } finally {
                        if (newData.getState().equals(WebServiceData.State.WSDL_SERVICE_COMPILE_FAILED)) {
                            removeWebService(newData);
                        }
                    }
                }
            }
        } finally {
            save();
        }
    }

    public WsdlModel getWsdlModel(WebServiceData wsData) throws IOException {
        File localWsdlFile = new File(wsData.getWsdlFile());
        URL wsdlUrl = localWsdlFile.toURI().toURL();

        WsdlModelProvider wsdlModelProvider = null;
        Collection<? extends WsdlModelProvider> providers = Lookup.getDefault().
            lookupAll(WsdlModelProvider.class);
        boolean notAccepted = false;
        if (providers != null) {
            for (WsdlModelProvider provider : providers) {
                if (provider.canAccept(wsdlUrl)) {
                    wsdlModelProvider = provider;
                    break;
                } else {
                    notAccepted = true;
                }
            }
        }
        WsdlModel wsdlModel = null;
        Throwable exc = null;

        if (wsdlModelProvider != null) {
            String packageName = wsData.getPackageName();
            if(packageName == null || packageName.trim().length() == 0)
                packageName = wsdlModelProvider.getEffectivePackageName();
            File catalogFile = new File(wsData.getCatalog());
            URL catalogUrl = catalogFile.toURI().toURL();

            try {
                wsdlModel = wsdlModelProvider.getWsdlModel(wsdlUrl, packageName, 
                        catalogUrl, true );
            } catch (Exception ex) {
                Throwable e = ex.getCause();
                exc = (e == null ? ex : e);
            }
        }
       
        if (wsdlModel == null) {
            wsData.setResolved(false);
            removeWebService(wsData, true, false);
            if (wsdlModelProvider == null && notAccepted) {
                DialogDisplayer.getDefault().notify(
                    new DialogDescriptor.Message(NbBundle.getMessage(WebServiceManager.class, "JAX_RPC_MODELER_ERROR")));
            } else if (wsdlModelProvider != null) {
                Throwable ex = wsdlModelProvider.getCreationException();
                if (ex != null) {
                    DialogDisplayer.getDefault().notify(
                        new DialogDescriptor.Message(NbBundle.getMessage(WebServiceManager.class, "JAX_WS_MODEL_CREATION_ERROR", ex.getLocalizedMessage())));
                } else if (exc != null) {
                    Logger.getLogger(WebServiceManager.class.getName()).log(Level.WARNING,
                        NbBundle.getMessage(WebServiceManager.class, "JAX_WS_MODELER_ERROR"), exc);
                } else {
                    String message = NbBundle.getMessage(WebServiceManager.class, "WS_MODELER_ERROR");
                    exc = new IllegalStateException(message);
                    Exceptions.printStackTrace(exc);
                }
            } else {
                String message = NbBundle.getMessage(WebServiceManager.class, "WS_MODELER_ERROR");
                exc = new IllegalStateException(message);
                Exceptions.printStackTrace(exc);
            }
        }
        
        return wsdlModel;
    }
    
    public

     void refreshWebService(WebServiceData wsData) throws IOException {
        removeWebService(wsData, false, true);
        wsData.setWsdlFile(null);
        wsData.setState(WebServiceData.State.WSDL_UNRETRIEVED);
        wsData.setCatalog(null);
        wsData.setWsdlService(null);
        wsData.setJaxRpcDescriptorPath(null);
        wsData.setJaxRpcDescriptor(null);
        wsData.setJaxWsDescriptor(null);
        wsData.setJaxWsDescriptorPath(null);

        addWebService(wsData, true);
    }

    public void resetWebService(WebServiceData wsData) {
        removeWebService(wsData, false, true);

        wsData.setWsdlFile(null);
        wsData.setState(WebServiceData.State.WSDL_UNRETRIEVED);
        wsData.setCatalog(null);
        wsData.setWsdlService(null);
        wsData.setJaxRpcDescriptorPath(null);
        wsData.setJaxRpcDescriptor(null);
        wsData.setJaxWsDescriptor(null);
        wsData.setJaxWsDescriptorPath(null);
    }

    /**
     * Add webservice to the Web Service List Model.
     * 
     * @param wsdl the wsdl URL
     * @param packageName the package for the webservice java classes
     * @param groupId the id of the group the webservice belongs to
     * 
     * @throws java.io.IOException if the web service could not be added
     */
    public WebServiceData addWebService(String wsdl, String packageName, String groupId) throws IOException {
        WebServiceData wsData = new WebServiceData(wsdl, groupId);
        wsData.setPackageName(packageName);
        wsData.setResolved(false);

        addWebService(wsData, true);
        return wsData;
    }

    /**
     * Removes a webservice from the Web Service List Model.
     * Client jars and other data are deleted from the filesystem.
     * 
     * @param wsData the WebService to remove
     */
    public void removeWebService(WebServiceData wsData) {
        removeWebService(wsData, true, true);
    }

    private void removeWebService(WebServiceData wsData, boolean removeFromModel, boolean deleteWsdl) {
        try {
            incrementMutatorCount();

            if (removeFromModel) {
                WebServiceListModel.getInstance().removeWebService(wsData.getId());
            }
            Collection<? extends WebServiceManagerExt> extensions = Lookup.getDefault().lookupAll(WebServiceManagerExt.class);
            WebServiceDescriptor jaxRpcDescriptor = wsData.getJaxRpcDescriptor();
            WebServiceDescriptor jaxWsDescriptor = wsData.getJaxWsDescriptor();

            for (WebServiceManagerExt extension : extensions) {
                if (jaxRpcDescriptor != null) {
                    extension.wsServiceRemovedExt(jaxRpcDescriptor);
                }
                if (jaxWsDescriptor != null) {
                    extension.wsServiceRemovedExt(jaxWsDescriptor);
                }
            }

            deleteWsArtifacts(jaxRpcDescriptor);
            deleteWsArtifacts(jaxWsDescriptor);

            // remove w/s directory
            if (wsData.getName() != null) {
                new File(WEBSVC_HOME, wsData.getName()).delete();
            }

            if (wsData.getWsdlFile() == null) {
                return;
            }

            if (!deleteWsdl) {
                return;
            }

            WebServiceListModel model = WebServiceListModel.getInstance();
            for (WebServiceData data : model.getWebServiceSet()) {
                if (data != wsData && wsData.getWsdlFile().equals(data.getWsdlFile())) {
                    deleteWsdl = false;
                    break;
                }
            }

            if (deleteWsdl) {
                // remove the top-level wsdl file
                rmDir(new File(wsData.getWsdlFile()));

                if (wsData.getCatalog() != null) {
                    File catalogFile = new File(wsData.getCatalog());
                    if (catalogFile.exists()) {
                        rmDir(catalogFile.getParentFile());
                    }
                }
            }
        } finally {
            save();
        }
    }

    /**
     * Utility method to remove the web service and delete any generated artifacts
     * @param proxyDescriptor the WebServiceDescriptor representing a set of proxy jars
     */
    private void deleteWsArtifacts(WebServiceDescriptor proxyDescriptor) {
        if (proxyDescriptor == null) {
            return;
        }

        File proxyRoot = proxyDescriptor.getXmlDescriptorFile().getParentFile();
        assert proxyRoot != null;

        // delete all registered jar files
        for (WebServiceDescriptor.JarEntry jar : proxyDescriptor.getJars()) {
            File jarFile = new File(proxyRoot, jar.getName());
            jarFile.delete();
        }

        // delete xml descriptor
        proxyDescriptor.getXmlDescriptorFile().delete();

        // remove the empty directory
        proxyRoot.delete();
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

            FileUtil.createFolder(new File(WEBSVC_HOME));
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

    public static synchronized void compileService(WebServiceData wsData) {
        boolean compileAttempted = false;
        try {
            if (WebServiceListModel.getInstance().getWebService(wsData.getId()) == null ||
                    wsData.getState().equals(WebServiceData.State.WSDL_SERVICE_COMPILED) ||
                    wsData.getState().equals(WebServiceData.State.WSDL_SERVICE_COMPILING)) {
                return;
            }
            wsData.setState(WebServiceData.State.WSDL_SERVICE_COMPILING);
            compileAttempted = true;

            Collection<? extends WebServiceManagerExt> extensions = Lookup.getDefault().lookupAll(WebServiceManagerExt.class);
            WSService svc = wsData.getWsdlService();

            // compile the WSDL and create the proxy jars
            Wsdl2Java wsdl2Java = new Wsdl2Java(wsData);
            boolean success = wsdl2Java.createProxyJars();
            if (!success) {
                return;
            }
            URL wsdl = new File(wsData.getWsdlFile()).toURI().toURL();
            String packageName = wsData.getEffectivePackageName();
            if (wsData.isJaxRpcEnabled()) {
                WebServiceDescriptor jaxRpcDescriptor = new WebServiceDescriptor(wsData.getName(), packageName, WebServiceDescriptor.JAX_RPC_TYPE, wsdl, new File(WEBSVC_HOME, wsData.getJaxRpcDescriptorPath()), svc);
                jaxRpcDescriptor.addJar(wsData.getName() + ".jar", WebServiceDescriptor.JarEntry.PROXY_JAR_TYPE);
                jaxRpcDescriptor.addJar(wsData.getName() + "-src.jar", WebServiceDescriptor.JarEntry.SRC_JAR_TYPE);

                wsData.setJaxRpcDescriptor(jaxRpcDescriptor);
            }
            if (wsData.isJaxWsEnabled()) {
                WebServiceDescriptor jaxWsDescriptor = new WebServiceDescriptor(wsData.getName(), packageName, WebServiceDescriptor.JAX_WS_TYPE, wsdl, new File(WEBSVC_HOME, wsData.getJaxWsDescriptorPath()), svc);
                jaxWsDescriptor.addJar(wsData.getName() + ".jar", WebServiceDescriptor.JarEntry.PROXY_JAR_TYPE);
                jaxWsDescriptor.addJar(wsData.getName() + "-src.jar", WebServiceDescriptor.JarEntry.SRC_JAR_TYPE);

                wsData.setJaxWsDescriptor(jaxWsDescriptor);
            }

            // create additional classes and jars from registered consumers
            // (e.g. designtime API implementations, client beans, dataproviders, etc.)
            boolean hasJaxWsConsumer = false;
            boolean hasJaxRpcConsumer = false;
            for (WebServiceManagerExt extension : extensions) {
                if (wsData.getJaxRpcDescriptor() != null) {
                    if (extension.wsServiceAddedExt(wsData.getJaxRpcDescriptor())) {
                        hasJaxRpcConsumer = true;
                    }
                }
                if (wsData.getJaxWsDescriptor() != null) {
                    if (extension.wsServiceAddedExt(wsData.getJaxWsDescriptor())) {
                        hasJaxWsConsumer = true;
                    }
                }
            }

            // delete everything if no consumer could be created, otherwise
            // only delete the artifacts of the failed ws type
            if (!hasJaxWsConsumer && !hasJaxRpcConsumer) {
                WebServiceManager.getInstance().removeWebService(wsData);
            } else if (!hasJaxWsConsumer && wsData.getJaxWsDescriptor() != null) {
                WebServiceManager.getInstance().deleteWsArtifacts(wsData.getJaxWsDescriptor());
                wsData.setJaxWsDescriptor(null);
                wsData.setJaxWsDescriptorPath(null);
                wsData.setJaxWsEnabled(false);
            } else if (!hasJaxRpcConsumer && wsData.getJaxRpcDescriptor() != null) {
                WebServiceManager.getInstance().deleteWsArtifacts(wsData.getJaxRpcDescriptor());
                wsData.setJaxRpcDescriptor(null);
                wsData.setJaxRpcDescriptorPath(null);
                wsData.setJaxRpcEnabled(false);
            }

            if (hasJaxWsConsumer || hasJaxRpcConsumer) {
                wsData.setState(WebServiceData.State.WSDL_SERVICE_COMPILED);
                wsData.setCompiled(true);
            }
        } catch (IOException ex) {
            Logger.getGlobal().log(Level.INFO, ex.getLocalizedMessage(), ex);
        } finally {
            if (!wsData.getState().equals(WebServiceData.State.WSDL_SERVICE_COMPILED) && compileAttempted) {
                wsData.setState(WebServiceData.State.WSDL_SERVICE_COMPILE_FAILED);
            } else {
                saveDescriptor(wsData);
            }
        }
    }

    private synchronized void incrementMutatorCount() {
        mutatorCount++;
    //System.out.println("incrementMutatorCount() mutatorCount = " + mutatorCount);
    }

    private synchronized void save() {
        if (--mutatorCount <= 0) {
            if (WebServiceListModel.getInstance().isDirty()) {
                //System.out.println("saving ws model");
                new WebServicePersistenceManager().save();
                WebServiceListModel.getInstance().setDirty(false);
            }
        }
    //System.out.println("save() mutatorCount = " + mutatorCount);
    }

    private static void saveDescriptor(WebServiceData data) {
        WebServicePersistenceManager mgr = new WebServicePersistenceManager();
        try {
            WebServiceDescriptor desc = data.getJaxWsDescriptor();
            mgr.saveDescriptor(desc);
            desc = data.getJaxRpcDescriptor();
            mgr.saveDescriptor(desc);
        } catch (Exception ex) {
        }
    }
}
