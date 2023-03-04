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
import java.beans.DefaultPersistenceDelegate;
import java.beans.Encoder;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.netbeans.modules.websvc.manager.model.WebServiceData;
import org.netbeans.modules.websvc.manager.model.WebServiceGroup;
import org.netbeans.modules.websvc.manager.model.WebServiceListModel;
import java.beans.XMLDecoder;
import java.beans.XMLEncoder;

import java.io.*;
import java.io.BufferedOutputStream;
import java.util.*;

import java.beans.ExceptionListener;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.modules.websvc.jaxwsmodelapi.WSService;
import org.netbeans.modules.websvc.jaxwsmodelapi.wsdlmodel.WsdlModel;
import org.netbeans.modules.websvc.manager.util.ManagerUtil;
import org.netbeans.modules.websvc.saas.model.SaasGroup;
import org.netbeans.modules.websvc.saas.model.SaasServicesModel;
import org.netbeans.modules.websvc.saas.model.WsdlSaas;
import org.netbeans.modules.websvc.saas.util.SaasUtil;
import org.netbeans.modules.websvc.saas.util.WsdlUtil;
import org.openide.ErrorManager;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.NbBundle;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.NodeList;

/**
 * WebServicePersistenceManager.java
 * @author  Winston Prakash, quynguyen
 */
public class WebServicePersistenceManager implements ExceptionListener {

    private File websvcDir = new File(WebServiceManager.WEBSVC_HOME);
    private File websvcRefFile = new File(websvcDir, "websvc_ref.xml");
    private List<WebServiceDescriptor> descriptorsToWrite = null;
    private boolean imported = true;

    public void setImported(boolean v) {
        imported = v;
    }

    public void load() {
        if (websvcRefFile.exists()) {
            try {
                SaasServicesModel model = SaasServicesModel.getInstance();
                XMLDecoder decoder = new XMLDecoder(new BufferedInputStream(new FileInputStream(websvcRefFile)));
                List<WebServiceData> wsDatas = new ArrayList<WebServiceData>();

                List<String> partnerServices = WebServiceListModel.getInstance().getPartnerServices();
                Object firstObject = decoder.readObject();
                int wsDataNums;

                if (firstObject instanceof List) {
                    List<String> loadedServices = (List<String>) firstObject;
                    for (String url : loadedServices) {
                        partnerServices.add(url);
                    }
                    wsDataNums = ((Integer) decoder.readObject());
                } else {
                    wsDataNums = ((Integer) firstObject);
                }

                for (int i = 0; i < wsDataNums; i++) {
                    WebServiceData wsData = null;
                    try {
                        wsData = (WebServiceData) decoder.readObject();
                    } catch (Exception exc) {
                        ErrorManager.getDefault().notify(exc);
                        decoder.close();
                    }

                    wsDatas.add(wsData);
                }
                int wsGroupSize = ((Integer) decoder.readObject());
                Map<String, WebServiceGroup> groupByIds = new HashMap<String, WebServiceGroup>();
                for (int i = 0; i < wsGroupSize; i++) {
                    try {
                        WebServiceGroup group = (WebServiceGroup) decoder.readObject();
                        groupByIds.put(group.getId(), group);
                        if (group.getName() == null) {
                            continue;
                        }

                        /**
                         * For import services created from 6.0
                         * Note: we only need to read old group from imported user dir
                         * New group information are not managed by this persistence.
                         */
                        String trimmed = translateGroupName(group);
                        if (!imported &&
                                model.getRootGroup().getChildGroup(group.getName()) == null &&
                                model.getRootGroup().getChildGroup(trimmed) == null &&
                                !WebServiceListModel.DEFAULT_GROUP.equals(group.getName())) {
                            model.createTopGroup(group.getName());
                        }
                    } catch (Exception exc) {
                        ErrorManager.getDefault().notify(exc);
                        decoder.close();
                    }
                }
                decoder.close();

                for (WebServiceData wsData : wsDatas) {
                    if (imported) { // we don't need to import generated artifacts
                        if (wsData.getJaxRpcDescriptorPath() != null) {
                            wsData.setJaxRpcDescriptor(loadDescriptorFile(websvcDir + File.separator + wsData.getJaxRpcDescriptorPath()));
                        }

                        if (wsData.getJaxWsDescriptorPath() != null) {
                            wsData.setJaxWsDescriptor(loadDescriptorFile(websvcDir + File.separator + wsData.getJaxWsDescriptorPath()));
                        }

                        try {
                            WsdlModel wsdlModel = WebServiceManager.getInstance().getWsdlModel(wsData);
                            wsData.setWsdlService(wsdlModel.getServiceByName(wsData.getName()));
                        } catch (IOException ex) {
                            Logger.getGlobal().log(Level.INFO, ex.getLocalizedMessage(), ex);
                        }
                    } else {
                        wsData.reset();
                        WebServiceGroup group = groupByIds.get(wsData.getGroupId());
                        SaasGroup parent = null;
                        if (group.getName() == null) {
                            parent = model.getRootGroup();
                        } else {
                            parent = model.getRootGroup().getChildGroup(translateGroupName(group));
                        }
                        if (parent == null) {
                            parent = model.getRootGroup();
                        }
                        String url = wsData.getOriginalWsdlUrl();
                        if (SaasUtil.getServiceByUrl(parent, url) != null) {
                            continue;
                        }
                        String display = WsdlUtil.getServiceDirName(url);
                        WsdlSaas service = new WsdlSaas(parent, display, url, wsData.getPackageName());
                        parent.addService(service);
                        service.save();
                    }
                    WebServiceListModel.getInstance().addWebService(wsData);
                }

                try {
                    WebServiceGroup defaultGroup = groupByIds.get(WebServiceListModel.DEFAULT_GROUP);
                    if (defaultGroup != null) {
                        WebServiceListModel.getInstance().addWebServiceGroup(defaultGroup);
                    }
                } catch (Exception ex) {
                    ErrorManager.getDefault().notify(ex);
                }

            } catch (Exception exc) {
                exc.printStackTrace();
            }
        }

        loadPartnerServices();
    }

    private String translateGroupName(WebServiceGroup group) {
        String name = group.getName();
        if (name.endsWith(" Services")) {
            return name.substring(0, name.length() - 9);
        }
        return name;
    }

    public void save() {
        WebServiceListModel model = WebServiceListModel.getInstance();
        if (!model.isInitialized()) {
            return;
        }

        if (!websvcDir.exists()) {
            websvcDir.mkdirs();
        }
        if (websvcRefFile.exists()) {
            websvcRefFile.delete();
        }
        XMLEncoder encoder = null;
        try {
            encoder = new XMLEncoder(new BufferedOutputStream(new FileOutputStream(websvcRefFile)));
            encoder.setExceptionListener(this);

            DefaultPersistenceDelegate delegate = new WebServiceDataPersistenceDelegate();
            encoder.setPersistenceDelegate(WSService.class, delegate);
            encoder.setPersistenceDelegate(WebServiceDescriptor.class, delegate);

            encoder.writeObject(model.getPartnerServices());

            List<WebServiceData> wsDataSet = model.getWebServiceSet();
            encoder.writeObject(wsDataSet.size());

            synchronized (wsDataSet) {
                for (WebServiceData wsData : wsDataSet) {
                    encoder.writeObject(wsData);
                }
            }

            List<WebServiceGroup> wsGroupSet = model.getWebServiceGroupSet();
            encoder.writeObject(wsGroupSet.size());

            synchronized (wsGroupSet) {
                for (WebServiceGroup group : wsGroupSet) {
                    encoder.writeObject(group);
                }
            }

            encoder.flush();

            delegate = new DefaultPersistenceDelegate();
            encoder.setPersistenceDelegate(WSService.class, delegate);
            encoder.setPersistenceDelegate(WebServiceDescriptor.class, delegate);

            if (descriptorsToWrite != null) {
                for (WebServiceDescriptor descriptor : descriptorsToWrite) {
                    saveWebServiceDescriptor(descriptor);
                }
                descriptorsToWrite.clear();
            }
        } catch (Exception exc) {
            ErrorManager.getDefault().notify(exc);
        } finally {
            if (encoder != null) {
                encoder.close();
            }
        }
    }

    private WebServiceDescriptor loadDescriptorFile(String descriptorPath) {
        if (descriptorPath == null || descriptorPath.length() == 0) {
            return null;
        } else {
            XMLDecoder decoder = null;
            try {
                decoder = new java.beans.XMLDecoder(new java.io.BufferedInputStream(new java.io.FileInputStream(descriptorPath)));
                return (WebServiceDescriptor) decoder.readObject();
            } catch (Exception ex) {
                exceptionThrown(ex);
                return null;
            } finally {
                if (decoder != null) {
                    decoder.close();
                }
            }
        }

    }

    /**
     * Loads (or reloads if the services already exist) a set of partner services
     * 
     * @param serviceFolder the folder location of the component definitions in the system filesystem
     * @param partnerName optional partner name for the web service group folder name, should be null 
     *        or identical to existing group name if overwriting an existing component's folder
     */
    public static void loadPartnerService(String serviceFolder, String partnerName) {
        FileObject folder = FileUtil.getConfigFile(serviceFolder);

        loadPartnerFromFolder(folder, partnerName, true);
    }

    public static void loadPartnerServices() {
        FileObject f = FileUtil.getConfigFile("RestComponents"); // NOI18N

        if (f != null && f.isFolder()) {
            Enumeration<? extends FileObject> en = f.getFolders(false);
            while (en.hasMoreElements()) {
                FileObject nextFolder = en.nextElement();
                String groupName = ManagerUtil.getLocalizedName(nextFolder);
                loadPartnerFromFolder(nextFolder, groupName, false);
            }
        }
    }

    private static void loadPartnerFromFolder(FileObject folder, String groupName, boolean reloadIfExists) {
        if (folder == null || !folder.isFolder()) {
            return;
        }

        Map<String, String> currentUrls = new HashMap<String, String>(); //url->name

        List<String> partnerUrls = WebServiceListModel.getInstance().getPartnerServices();

        FileObject[] contents = folder.getChildren();
        for (int i = 0; i < contents.length; i++) {
            try {
                DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
                DocumentBuilder db = dbf.newDocumentBuilder();
                Document doc = db.parse(contents[i].getInputStream());
                NodeList nodes = doc.getElementsByTagName("method"); // NOI18N

                for (int j = 0; j < nodes.getLength(); j++) {
                    NamedNodeMap attributes = nodes.item(j).getAttributes();
                    String type = attributes.getNamedItem("type").getNodeValue(); // NOI18N
                    String url = attributes.getNamedItem("url").getNodeValue(); // NOI18N

                    boolean addUrl = !currentUrls.containsKey(url) && (reloadIfExists ||
                            (!reloadIfExists && !partnerUrls.contains(url)));

                    if ("http://schemas.xmlsoap.org/wsdl/".equals(type) && addUrl) { // NOI18N
                        String serviceName = attributes.getNamedItem("serviceName").getNodeValue(); //NOI18N
                        currentUrls.put(url, serviceName);
                    }
                }
            } catch (Exception ex) {
                String msg = NbBundle.getMessage(WebServicePersistenceManager.class, "MSG_BadContent", contents[i].getPath());
                Throwable t = ErrorManager.getDefault().annotate(ex, msg);
                ErrorManager.getDefault().notify(t);
            }
        }

        if (currentUrls.size() > 0) {
            WebServiceGroup newGroup = null;
            List<WebServiceGroup> webServiceGroups = WebServiceListModel.getInstance().getWebServiceGroupSet();
            for (WebServiceGroup group : webServiceGroups) {
                if (!group.isUserDefined() && group.getName().equals(groupName)) {
                    newGroup = group;
                    break;
                }
            }

            if (newGroup == null) {
                newGroup = new WebServiceGroup(WebServiceListModel.getInstance().getUniqueWebServiceGroupId());
                newGroup.setName(groupName);
                newGroup.setUserDefined(false);
            }

            for (Map.Entry<String, String> entry : currentUrls.entrySet()) {
                String url = entry.getKey();

                // !reloadIfExists -> !partnerUrls.contains(url)
                if (!reloadIfExists || !partnerUrls.contains(url)) {
                    // Add a new web service
                    partnerUrls.add(url);
                    WebServiceData wsData = new WebServiceData(url, newGroup.getId());
                    WebServiceListModel.getInstance().addWebService(wsData);

                    newGroup.add(wsData.getId(), true);
                } else {
                    // reset an existing service
                    WebServiceData existingData = null;
                    List<WebServiceData> wsDatas = WebServiceListModel.getInstance().getWebServiceSet();
                    for (WebServiceData wsData : wsDatas) {
                        if (wsData.getOriginalWsdlUrl().equals(url)) {
                            existingData = wsData;
                            break;
                        }
                    }

                    if (existingData != null) {
                        WebServiceManager.getInstance().resetWebService(existingData);
                        existingData.setName(entry.getValue());
                    } else {
                        WebServiceData wsData = new WebServiceData(url, newGroup.getId());
                        WebServiceListModel.getInstance().addWebService(wsData);

                        newGroup.add(wsData.getId(), true);
                    }
                }
            }

            WebServiceListModel.getInstance().addWebServiceGroup(newGroup);
        }
    }

    public void saveDescriptor(WebServiceDescriptor descriptor) throws IOException {
        XMLEncoder encoder = new XMLEncoder(new BufferedOutputStream(new FileOutputStream(descriptor.getXmlDescriptor())));
        encoder.setExceptionListener(this);
        DefaultPersistenceDelegate delegate = new WebServiceDataPersistenceDelegate();
        encoder.setPersistenceDelegate(WSService.class, delegate);
        encoder.writeObject(descriptor);

        encoder.flush();
        encoder.close();
    }

    public void saveWebServiceDescriptor(WebServiceDescriptor descriptor) {
        try {
            saveDescriptor(descriptor);
        } catch (IOException ex) {
            exceptionThrown(ex);
        }
    }

    public void exceptionThrown(Exception exc) {
        ErrorManager.getDefault().notify(exc);
    }

    public static class WebServiceDataPersistenceDelegate extends DefaultPersistenceDelegate {

        /**
         * Suppress the writing of 
         * org.netbeans.modules.websvc.api.jaxws.wsdlmodel.WsdlService
         * It will be created from the WSDL file
         */
        @Override
        public void writeObject(Object oldInstance, Encoder out) {
            if (oldInstance instanceof WSService) {
                return;
            } else if (oldInstance instanceof WebServiceDescriptor) {
                //RESOLVE: I am taking out saving the descriptors during saving
                // of the model since it is already done separately. This will
                // also make saving more efficient.
//                if (descriptorsToWrite == null) {
//                    descriptorsToWrite = new ArrayList<WebServiceDescriptor>();
//                }
//                descriptorsToWrite.add((WebServiceDescriptor)oldInstance);
                return;
            } else {
                super.writeObject(oldInstance, out);
            }
        }
    }
}
