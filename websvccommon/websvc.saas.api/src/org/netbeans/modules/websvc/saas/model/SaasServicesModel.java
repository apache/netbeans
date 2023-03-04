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
package org.netbeans.modules.websvc.saas.model;

import java.beans.PropertyChangeEvent;
import org.netbeans.modules.websvc.saas.model.jaxb.Group;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.File;
import java.io.IOException;
import java.util.Enumeration;
import java.util.List;
import org.netbeans.modules.websvc.saas.model.jaxb.SaasServices;
import org.netbeans.modules.websvc.saas.util.SaasUtil;
import org.netbeans.modules.websvc.saas.util.WsdlUtil;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;

/**
 *
 * @author nam
 */
public class SaasServicesModel {

    public static final String PROP_GROUPS = "groups";
    public static final String PROP_SERVICES = "services";
    public static final String ROOT_GROUP = "Root";
    public static final String WEBSVC_HOME = System.getProperty("netbeans.user") +
            File.separator + "config" + File.separator + "WebServices"; // NOI18N

    public static final String SERVICE_GROUP_XML = "service-groups.xml";
    public static final String PROFILE_PROPERTIES_FILE = "profile.properties";
    private SaasGroup rootGroup;
    private State state = State.UNINITIALIZED;
    private PropertyChangeSupport pps = new PropertyChangeSupport(this);

    public static enum State {

        UNINITIALIZED, INITIALIZING, READY
    }
    private static SaasServicesModel instance;

    public static synchronized SaasServicesModel getInstance() {
        if (instance == null) {
            instance = new SaasServicesModel();
        }
        return instance;
    }

    private SaasServicesModel() {
    }

    /**
     * @return low-cost preliminary root group, might not fully populated
     */
    public SaasGroup getInitialRootGroup() {
        if (rootGroup == null) {
            loadUserDefinedGroups();
        }
        return rootGroup;
    }

    public synchronized void initRootGroup() {
        if (state == State.READY) {
            return;
        }
        setState(State.INITIALIZING);
        getInitialRootGroup();
        loadFromDefaultFileSystem();
        loadFromWebServicesHome();
        WsdlUtil.ensureImportExisting60Services();
        setState(State.READY);
    }

    private void loadUserDefinedGroups() {
        FileObject input = FileUtil.toFileObject(new File(WEBSVC_HOME, SERVICE_GROUP_XML));
        try {
            if (input != null) {
                rootGroup = SaasUtil.loadSaasGroup(input);
            }
        } catch (Exception e) {
            Exceptions.printStackTrace(e);
        }
        if (rootGroup == null) {
            Group g = new Group();
            g.setName(ROOT_GROUP);
            rootGroup = new SaasGroup((SaasGroup) null, g);
        }
    }

    private void loadFromDefaultFileSystem() {
        FileObject f = FileUtil.getConfigFile("SaaSServices"); // NOI18N

        if (f != null && f.isFolder()) {
            Enumeration<? extends FileObject> en = f.getFolders(false);
            while (en.hasMoreElements()) {
                FileObject groupFolder = en.nextElement();
                for (FileObject fo : groupFolder.getChildren()) {
                    if (fo.isFolder()) {
                        continue;
                    }
                    if (PROFILE_PROPERTIES_FILE.equals(fo.getNameExt())) {
                        continue;
                    }
                    loadSaasServiceFile(fo, false);
                }
                SaasGroup g = rootGroup.getChildGroup(groupFolder.getName());
                if (g != null) {
                    g.setIcon16Path((String) groupFolder.getAttribute("icon16"));
                    g.setIcon32Path((String) groupFolder.getAttribute("icon32"));
                }
            }
        }
    }

    public void saveRootGroup() {
        try {
            SaasUtil.saveSaasGroup(rootGroup, new File(WEBSVC_HOME, SERVICE_GROUP_XML));
        } catch (Exception ex) {
            Exceptions.printStackTrace(ex);
        }
    }

    public static FileObject getWebServiceHome() {
        File websvcDir = new File(WEBSVC_HOME);
        if (!websvcDir.isFile()) {
            websvcDir.mkdirs();
        }
        return FileUtil.toFileObject(websvcDir);
    }

    private void loadFromWebServicesHome() {
        for (FileObject fo : getWebServiceHome().getChildren()) {
            if (!fo.isFolder()) {
                continue;
            }
            for (FileObject file : fo.getChildren()) {
                if (!file.getNameExt().endsWith("-saas.xml")) { //NOI18N

                    continue;
                }
                try {
                    loadSaasServiceFile(file, true);
                } catch (Exception ex) {
                    Exceptions.printStackTrace(ex);
                }
            }
        }
    }

    private Saas createService(SaasGroup parent, SaasServices jaxbServiceObject) {
        if (Saas.NS_WADL.equals(jaxbServiceObject.getType()) 
                ||Saas.NS_WADL_09.equals(jaxbServiceObject.getType())) 
        {
            return new WadlSaas(parent, jaxbServiceObject);
        } else if (Saas.NS_WSDL.equals(jaxbServiceObject.getType())) {
            if (WsdlUtil.hasWsdlSupport())
                return new WsdlSaas(parent, jaxbServiceObject);
        } else {
            return new CustomSaas(parent, jaxbServiceObject); //custom
        }
        return null;
    }

    private void loadSaasServiceFile(FileObject saasFile, boolean userDefined) {
        try {
            SaasServices ss = SaasUtil.loadSaasServices(saasFile);
            Group g = ss.getSaasMetadata().getGroup();
            SaasGroup parent = rootGroup;

            Saas service = null;
            if (g == null || g.getName() == null || g.getName().trim().length() == 0) {
                service = createService(parent, ss);
                if(service != null) {
                    service.setUserDefined(userDefined);
                    parent.addService(service);
                }
            } else {
                while (g != null) {
                    SaasGroup child = parent.getChildGroup(g.getName());
                    if (child == null) {
                        child = new SaasGroup(parent, g);
                        parent.addChildGroup(child);
                    }

                    child.setUserDefined(userDefined);

                    if (g.getGroup().size() == 0) {
                        service = createService(child, ss);
                        if(service != null) {
                            service.setUserDefined(userDefined);
                            child.addService(service);
                        }
                        break;
                    } else {
                        // 'group' element part of straight path, has only single child
                        g = g.getGroup().get(0);
                        parent = child; // march on towards tip

                    }
                }
            }
            if(service != null)
                service.upgrade();
        } catch (Throwable ex) {
            Exceptions.printStackTrace(Exceptions.attachMessage(ex, "Error loading saas file: " + saasFile.getPath()));
        }
    }

    public SaasGroup getRootGroup() {
        initRootGroup();
        return rootGroup;
    }

    public State getState() {
        return state;
    }

    private synchronized void setState(State state) {
        this.state = state;
        if (state == State.READY) {
            fireChange(PROP_GROUPS, rootGroup, null, rootGroup.getChildrenGroups());
            fireChange(PROP_SERVICES, rootGroup, null, rootGroup.getServices());
        }
    }

    public void addPropertyChangeListener(PropertyChangeListener l) {
        pps.addPropertyChangeListener(l);
    }

    public void removePropertyChangeListener(PropertyChangeListener l) {
        pps.removePropertyChangeListener(l);
    }

    protected void fireChange(String propertyName, Object source, Object old, Object neu) {
        PropertyChangeEvent pce = new PropertyChangeEvent(source, propertyName, old, neu);
        pps.firePropertyChange(pce);
    }

    List<SaasGroup> getGroups() {
        return getRootGroup().getChildrenGroups();
    }

    public SaasGroup getTopGroup(String name) {
        return getRootGroup().getChildGroup(name);
    }

    public Saas getTopService(String name) {
        return getRootGroup().getChildService(name);
    }

    /**
     * Model mutation: add group from UI
     * 
     * @param parent
     * @param child
     */
    public synchronized SaasGroup createTopGroup(String groupName) {
        return createGroup(getInitialRootGroup(), groupName);
    }

    public synchronized SaasGroup createGroup(SaasGroup parent, String groupName) {
        initRootGroup();
        SaasGroup group = parent.createGroup(groupName);
        parent.addChildGroup(group);
        saveRootGroup();
        fireChange(PROP_GROUPS, parent, null, group);
        return group;
    }

    /**
     * Model mutation: remove group from UI
     * 
     * @param group group to remove
     */
    public synchronized void removeGroup(SaasGroup group) {
        initRootGroup();
        SaasGroup parent = group.getParent();
        if (parent == null || !parent.removeChildGroup(group)) {
            throw new IllegalArgumentException("Can't remove group " + group.getName()); //NOI18N

        }
        saveRootGroup();
        fireChange(PROP_GROUPS, parent, group, null);
    }

    /**
     * Model mutation: add saas service from UI
     * 
     * @param parent group
     * @param displayName name
     * @param url URL pointing to a WSDL or WADL
     * @param packageName package name used in codegen; if null, value will be derived.
     */
    public synchronized WsdlSaas createWsdlService(SaasGroup parent, String displayName, String url, String packageName) {
        initRootGroup();
        WsdlSaas service = new WsdlSaas(parent, url, displayName, packageName);
        service.setUserDefined(true);
        parent.addService(service);
        service.save();
        fireChange(PROP_SERVICES, parent, null, service);
        return service;
    }

    public Saas createSaasService(SaasGroup parent, String url, String packageName) {
        String saasType = SaasUtil.getSaasType(url);
        
        if (Saas.NS_WSDL.equals(saasType)) {
            if (WsdlUtil.hasWsdlSupport()) {
                return createWsdlService(parent, url, packageName);
            } else {
                throw new RuntimeException(NbBundle.getMessage(SaasServicesModel.class, "MSG_WsdlServiceNotSupported"));
            }
        } else if (Saas.NS_WADL.equals(saasType)||Saas.NS_WADL_09.equals(saasType)) {
            return createWadlService(parent, url, packageName);
        } else {
            throw new RuntimeException(NbBundle.getMessage(SaasServicesModel.class, "MSG_UnknownSaasType"));
        }
    }
    
    private WsdlSaas createWsdlService(SaasGroup parent, String url, String packageName) {
        return createWsdlService(parent, WsdlUtil.getServiceDirName(url), url, packageName);
    }

    private WadlSaas createWadlService(SaasGroup parent, String url, String packageName) {
        initRootGroup();
        String displayName = SaasUtil.getWadlServiceDirName(url);
        WadlSaas service = new WadlSaas(parent, url, displayName, packageName);
        service.toStateReady(false);
        service.setUserDefined(true);
        parent.addService(service);
        service.save();
        fireChange(PROP_SERVICES, parent, null, service);
        return service;
    }

    /**
     * Model mutation: remve service from parent group, delete file, fire event
     * @param service to remove.
     */
    public synchronized void removeService(Saas service) {
        initRootGroup();
        SaasGroup parent = service.getParentGroup();
        parent.removeService(service);
        _removeService(service);
        fireChange(PROP_SERVICES, parent, service, null);
    }

    void _removeService(Saas service) {
        try {
            if (service instanceof WsdlSaas) {
                WsdlSaas saas = (WsdlSaas) service;
                WsdlUtil.removeWsdlData(saas.getDelegate().getUrl());
            }
            FileObject saasFolder = service.getSaasFolder();
            if (saasFolder != null) {
                saasFolder.delete();
            }
            service.setState(Saas.State.REMOVED);
        } catch (IOException e) {
            Exceptions.printStackTrace(e);
        }
    }

    public synchronized void renameGroup(SaasGroup group, String newName) {
        String oldName = group.getName();
        group.setName(newName);
        saveRootGroup();
        saveAllSaas(group);
        fireChange(SaasGroup.PROP_GROUP_NAME, group, oldName, newName);
    }

    private void saveAllSaas(SaasGroup group) {
        for (Saas s : group.getServices()) {
            s.save();
        }
        for (SaasGroup sg : group.getChildrenGroups()) {
            saveAllSaas(sg);
        }
    }

    public synchronized void refreshService(Saas saas) {
        saas.refresh();
    }

    void reset() {
        rootGroup = null;
        state = State.UNINITIALIZED;
    }
}
