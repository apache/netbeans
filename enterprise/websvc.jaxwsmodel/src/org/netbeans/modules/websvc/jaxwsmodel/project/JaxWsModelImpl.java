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

package org.netbeans.modules.websvc.jaxwsmodel.project;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.event.ChangeListener;
import org.netbeans.api.project.Project;
import org.netbeans.modules.schema2beans.BaseBean;
import org.netbeans.modules.websvc.api.jaxws.project.JaxWsBuildScriptExtensionProvider;
import org.netbeans.modules.websvc.api.jaxws.project.WSUtils;
import org.netbeans.modules.websvc.api.jaxws.project.config.Client;
import org.netbeans.modules.websvc.api.jaxws.project.config.ClientAlreadyExistsExeption;
import org.netbeans.modules.websvc.api.jaxws.project.config.JaxWsModel;
import org.netbeans.modules.websvc.api.jaxws.project.config.JaxWsModelProvider;
import org.netbeans.modules.websvc.api.jaxws.project.config.Service;
import org.netbeans.modules.websvc.api.jaxws.project.config.ServiceAlreadyExistsExeption;
import org.netbeans.spi.project.ProjectServiceProvider;
import org.openide.ErrorManager;
import org.openide.filesystems.FileChangeAdapter;
import org.openide.filesystems.FileEvent;
import org.openide.filesystems.FileLock;
import org.openide.filesystems.FileObject;
import org.openide.util.ChangeSupport;
import org.openide.util.NbBundle;

/** Provides information about web services and clients in a project
 * Provides information used for build-impl generation
 * Working over nbproject/jax-ws.xml file
 */
@ProjectServiceProvider(service=JaxWsModel.class, projectType={
    "org-netbeans-modules-j2ee-clientproject",
    "org-netbeans-modules-j2ee-ejbjarproject",
    "org-netbeans-modules-java-j2seproject",
    "org-netbeans-modules-web-project"
})
public final class JaxWsModelImpl implements JaxWsModel {
    private static final String JAX_WS_XML_RESOURCE="/org/netbeans/modules/websvc/jaxwsmodel/resources/jax-ws.xml"; //NOI18N
    private org.netbeans.modules.websvc.jaxwsmodel.project_config1_0.JaxWs jaxws;
    private FileObject fo;
    private Object initLock = new Object();
    private List<ServiceListener> serviceListeners;
    private List<PropertyChangeListener> propertyChangeListeners;
    private List<PropertyChangeListener> cachedListeners;
    private ChangeSupport changeSupport;
    private Project project;

    public JaxWsModelImpl(Project project) {
        this.project = project;
        this.fo = WSUtils.findJaxWsFileObject(project);
        if (fo != null) {
            try {
                jaxws = org.netbeans.modules.websvc.jaxwsmodel.project_config1_0.JaxWs.createGraph(fo.getInputStream());
            } 
            catch (IOException ex) {
                Logger.getLogger(JaxWsModelImpl.class.getName()).log(Level.INFO, 
                        "JaxWsModel creation failed", ex);      // NOI18N
            }
            /*
             *  Fix for BZ#199704 - RuntimeException: 
             *  DOM graph creation failed: org.netbeans.modules.schema2beans.Schema2BeansRuntimeException: 
             *  Failed to create the XML-DOM Document. Check your XML to make sure it is correct. Prematur
             *  
             *  RuntimeException should be changed to the unchecked exception but 
             *  org.netbeans.modules.websvc.jaxwsmodel.project_config1_0.JaxWs is auto-generated
             *  class so it cannot be corrected. 
             */
            catch (RuntimeException ex) {
                Logger.getLogger(JaxWsModelImpl.class.getName()).log(Level.INFO, 
                        "JaxWsModel creation failed", ex);      // NOI18N
            }
            final Project prj = project;
            fo.addFileChangeListener(new FileChangeAdapter() {
                @Override
                public void fileChanged(FileEvent fe) {
                    JaxWsBuildScriptExtensionProvider extProvider = prj.getLookup().lookup(JaxWsBuildScriptExtensionProvider.class);
                    if (extProvider != null) {
                        try {
                            extProvider.handleJaxWsModelChanges(JaxWsModelImpl.this);
                        } catch (java.io.IOException ex) {
                            Logger.getLogger(JaxWsModelImpl.class.getName()).log(Level.INFO, "failed to implement changes in jaxws-build.xml", ex); //NOI18N
                        }
                    }
                }
            });
        }

        propertyChangeListeners = new ArrayList<PropertyChangeListener>();
        cachedListeners = new ArrayList<PropertyChangeListener>();
        serviceListeners = new ArrayList<ServiceListener>();
        changeSupport = new ChangeSupport(this);
    }

    public JaxWsModelImpl(org.netbeans.modules.websvc.jaxwsmodel.project_config1_0.JaxWs jaxws) {
        this(jaxws,null);
    }

    public JaxWsModelImpl(org.netbeans.modules.websvc.jaxwsmodel.project_config1_0.JaxWs jaxws, FileObject fo) {
        this.jaxws=jaxws;
        this.fo=fo;
        propertyChangeListeners = new ArrayList<PropertyChangeListener>();
        serviceListeners = new ArrayList<ServiceListener>();
        changeSupport = new ChangeSupport(this);
    }
    
    @Override
    public Service[] getServices() {
        if (jaxws == null) return new Service[] {};
        org.netbeans.modules.websvc.jaxwsmodel.project_config1_0.Services services = jaxws.getServices();
        if (services==null) return new Service[] {};
        else {
            org.netbeans.modules.websvc.jaxwsmodel.project_config1_0.Service[] org = services.getService();
            if (org==null) return new Service[]{};
            Service[] newServices = new Service[org.length];
            for (int i=0;i<org.length;i++) {
                newServices[i] = JaxWsModelProvider.getDefault().createService(org[i]);
            }
            return newServices;
        }
    }
    
    @Override
    public void setJsr109(Boolean jsr109) {
        if (jaxws != null) jaxws.setJsr109(jsr109);
    }
    
    @Override
    public Boolean getJsr109() {
        return jaxws == null ? null : jaxws.getJsr109();
    }
    
    @Override
    public Service findServiceByName(String name) {
        org.netbeans.modules.websvc.jaxwsmodel.project_config1_0.Service service = findService(name);
        return service==null ? null : JaxWsModelProvider.getDefault().createService(service);
    }
    
    @Override
    public Service findServiceByImplementationClass(String wsClassName) {
        org.netbeans.modules.websvc.jaxwsmodel.project_config1_0.Service service = _findServiceByClass(wsClassName);
        return service==null ? null : JaxWsModelProvider.getDefault().createService(service);
    }
    
    private org.netbeans.modules.websvc.jaxwsmodel.project_config1_0.Service findService(String name) {
        if (jaxws == null) return null;
        org.netbeans.modules.websvc.jaxwsmodel.project_config1_0.Services services = jaxws.getServices();
        if (services==null) return null;
        else {
            org.netbeans.modules.websvc.jaxwsmodel.project_config1_0.Service[] org = services.getService();
            if (org==null) return null;
            for (int i=0;i<org.length;i++) {
                if (name.equals(org[i].getName())) return org[i];
            }
            return null;
        }
    }
    
    private org.netbeans.modules.websvc.jaxwsmodel.project_config1_0.Service _findServiceByClass(String wsClassName) {
        if (jaxws == null) return null;
        org.netbeans.modules.websvc.jaxwsmodel.project_config1_0.Services services = jaxws.getServices();
        if (services==null) return null;
        else {
            org.netbeans.modules.websvc.jaxwsmodel.project_config1_0.Service[] org = services.getService();
            if (org==null) return null;
            for (int i=0;i<org.length;i++) {
                if (wsClassName.equals(org[i].getImplementationClass())) return org[i];
            }
            return null;
        }
    }
    
    @Override
    public boolean removeService(String name) {
        org.netbeans.modules.websvc.jaxwsmodel.project_config1_0.Service service = findService(name);
        if (name==null) return false;
        else {
            fireServiceRemoved(name);
            jaxws.getServices().removeService(service);
            return true;
        }
    }
    
    @Override
    public boolean removeServiceByClassName(String webserviceClassName) {
        if (webserviceClassName != null) {
            org.netbeans.modules.websvc.jaxwsmodel.project_config1_0.Service service = _findServiceByClass(webserviceClassName);
            if (service != null) {
                fireServiceRemoved(service.getName());
                jaxws.getServices().removeService(service);            
                return true;
            }
        }
        return false;
    }
    
    @Override
    public Service addService(String name, String implementationClass)
    throws ServiceAlreadyExistsExeption {
        if (jaxws == null && project != null) {
            try {
                WSUtils.createJaxWsFileObject(project);
            } catch (IOException ex) {
                Logger.getLogger(JaxWsModelImpl.class.getName()).log(Level.INFO, "failed to create jax-ws.xml", ex); //NOI18N
            }
        }
        if (findService(name)!=null) throw new ServiceAlreadyExistsExeption(name);
        org.netbeans.modules.websvc.jaxwsmodel.project_config1_0.Service service = jaxws.getServices().newService();
        service.setName(name);
        service.setImplementationClass(implementationClass);
        jaxws.getServices().addService(service);
        fireServiceAdded(name, implementationClass);
        return JaxWsModelProvider.getDefault().createService(service);
    }
    
    @Override
    public Service addService(String name, String implementationClass, String wsdlUrl, String serviceName, String portName, String packageName)
    throws ServiceAlreadyExistsExeption {
        if (jaxws == null && project != null) {
            try {
                WSUtils.createJaxWsFileObject(project);
            } catch (IOException ex) {
                Logger.getLogger(JaxWsModelImpl.class.getName()).log(Level.INFO, "failed to create jax-ws.xml", ex); //NOI18N
            }
        }
        if (findService(name)!=null) throw new ServiceAlreadyExistsExeption(name);
        org.netbeans.modules.websvc.jaxwsmodel.project_config1_0.Service service = jaxws.getServices().newService();
        service.setName(name);
        service.setImplementationClass(implementationClass);
        service.setWsdlUrl(wsdlUrl);
        service.setServiceName(serviceName);
        service.setPortName(portName);
        service.setPackageName(packageName);
        jaxws.getServices().addService(service);
        return JaxWsModelProvider.getDefault().createService(service);
    }
    
    @Override
    public Client[] getClients() {
        if (jaxws == null) return new Client[] {};
        org.netbeans.modules.websvc.jaxwsmodel.project_config1_0.Clients clients = jaxws.getClients();
        if (clients==null) return new Client[] {};
        else {
            org.netbeans.modules.websvc.jaxwsmodel.project_config1_0.Client[] org = clients.getClient();
            if (org==null) return new Client[]{};
            Client[] newClients = new Client[org.length];
            for (int i=0;i<org.length;i++) {
                newClients[i] = JaxWsModelProvider.getDefault().createClient(org[i]);
            }
            return newClients;
        }
    }
    
    @Override
    public Client findClientByName(String name) {
        org.netbeans.modules.websvc.jaxwsmodel.project_config1_0.Client client = findClient(name);
        return client==null ? null : JaxWsModelProvider.getDefault().createClient(client);
    }
    
    @Override
    public Client findClientByWsdlUrl(String wsdlUrl) {
        if (jaxws == null) return null;
        org.netbeans.modules.websvc.jaxwsmodel.project_config1_0.Clients clients = jaxws.getClients();
        if (clients==null) return null;
        else {
            org.netbeans.modules.websvc.jaxwsmodel.project_config1_0.Client[] org = clients.getClient();
            if (org==null) return null;
            for (int i=0;i<org.length;i++) {
                if (wsdlUrl.equals(org[i].getWsdlUrl())) return JaxWsModelProvider.getDefault().createClient(org[i]);
            }
            return null;
        }
    }
    
    private org.netbeans.modules.websvc.jaxwsmodel.project_config1_0.Client findClient(String name) {
        if (jaxws == null) return null;
        org.netbeans.modules.websvc.jaxwsmodel.project_config1_0.Clients clients = jaxws.getClients();
        if (clients==null) return null;
        else {
            org.netbeans.modules.websvc.jaxwsmodel.project_config1_0.Client[] org = clients.getClient();
            if (org==null) return null;
            for (int i=0;i<org.length;i++) {
                if (name.equals(org[i].getName())) return org[i];
            }
            return null;
        }
    }
    
    @Override
    public boolean removeClient(String name) {
        org.netbeans.modules.websvc.jaxwsmodel.project_config1_0.Client client = findClient(name);
        if (client == null) return false;
        else {
            jaxws.getClients().removeClient(client);
            return true;
        }
    }
    
    @Override
    public Client addClient(String name, String wsdlUrl, String packageName)
    throws ClientAlreadyExistsExeption {
        if (jaxws == null && project != null) {
            try {
                WSUtils.createJaxWsFileObject(project);
            } catch (IOException ex) {
                Logger.getLogger(JaxWsModelImpl.class.getName()).log(Level.INFO, "failed to create jax-ws.xml", ex); //NOI18N
            }
        }
        if (findClient(name) != null) throw new ClientAlreadyExistsExeption(name);
        org.netbeans.modules.websvc.jaxwsmodel.project_config1_0.Client client = jaxws.getClients().newClient();
        client.setName(name);
        client.setWsdlUrl(wsdlUrl);
        if (packageName!=null) {
            client.setPackageName(packageName);
            client.setPackageNameForceReplace("true");
        }
        jaxws.getClients().addClient(client);
        return JaxWsModelProvider.getDefault().createClient(client);
    }
    
    @Override
    public synchronized void addPropertyChangeListener(PropertyChangeListener l) {
        if (jaxws == null) {
            cachedListeners.add(l);
        } else {
            JaxWsPCL jaxWsPcl = new JaxWsPCL(l);
            propertyChangeListeners.add(jaxWsPcl);
            jaxws.addPropertyChangeListener(jaxWsPcl);
        }
    }
    
    @Override
    public synchronized void removePropertyChangeListener(PropertyChangeListener l) {
        if (jaxws == null) {
            cachedListeners.remove(l);
        } else {
            for (PropertyChangeListener pcl:propertyChangeListeners) {
                if (l == ((JaxWsPCL)pcl).getOriginalListener()) {
                    jaxws.removePropertyChangeListener(pcl);
                    propertyChangeListeners.remove(pcl);
                    break;
                }
            }
        }
    }
    
    @Override
    public void merge(JaxWsModel newJaxWs) {
        if (jaxws != null) {
            JaxWsModelImpl impl = (JaxWsModelImpl)newJaxWs;
            if (impl.jaxws!=null)
                jaxws.merge(((JaxWsModelImpl)newJaxWs).jaxws,BaseBean.MERGE_UPDATE);
        }
    }
    
    @Override
    public void write(OutputStream os) throws IOException {
        if (jaxws != null) jaxws.write(os);
    }
    
    @Override
    public FileObject getJaxWsFile() {
        return fo;
    }
    
    @Override
    public void setJaxWsFile(FileObject fo) {
        this.fo = fo;
        if (fo != null && project !=null) {
            final JaxWsBuildScriptExtensionProvider extProvider = project.getLookup().lookup(JaxWsBuildScriptExtensionProvider.class);
            if (extProvider != null) {
                try {
                    jaxws = org.netbeans.modules.websvc.jaxwsmodel.project_config1_0.JaxWs.createGraph(fo.getInputStream());
                } catch (IOException ex) {
                }
                final Project prj = project;
                fo.addFileChangeListener(new FileChangeAdapter() {
                    @Override
                    public void fileChanged(FileEvent fe) {
                        JaxWsBuildScriptExtensionProvider extProvider = prj.getLookup().lookup(JaxWsBuildScriptExtensionProvider.class);
                        if (extProvider != null) {
                            try {
                                extProvider.handleJaxWsModelChanges(JaxWsModelImpl.this);
                            } catch (java.io.IOException ex) {
                                Logger.getLogger(JaxWsModelImpl.class.getName()).log(Level.INFO, "failed to implement changes in jaxws-build.xml", ex); //NOI18N
                            }
                        }
                    }
                });
                if (jaxws != null) {
                    for (PropertyChangeListener l : cachedListeners) {
                        addPropertyChangeListener(l);
                    }
                    cachedListeners.clear();
                }
            }
        }
        changeSupport.fireChange();
    }
    
    @Override
    public void write() throws IOException {
        if (fo!=null) {
            FileLock lock=null;
            OutputStream os = null;
            try {
                lock = fo.lock();
                os = fo.getOutputStream(lock);
                write(os);
                os.close();
            } 
            finally {
                if (lock!=null) lock.releaseLock();
                if (os != null) os.close();
            }
        } 
        else {
            throw new IOException("No FileObject for writing specified"); //NOI18N
        }
    }
    
    @Override
    public synchronized void addServiceListener(ServiceListener listener) {
        if (listener!=null)
            serviceListeners.add(listener);
    }
    
    @Override
    public synchronized void removeServiceListener(ServiceListener listener) {
        serviceListeners.remove(listener);
    }
    
    void fireServiceAdded(String name, String implementationClass) {
        Iterator<ServiceListener> it = serviceListeners.iterator();
        synchronized (this) {
            while (it.hasNext()) it.next().serviceAdded(name, implementationClass);
        }
    }
    
    void fireServiceRemoved(String name) {
        Iterator<ServiceListener> it = serviceListeners.iterator();
        synchronized (this) {
            while (it.hasNext()) it.next().serviceRemoved(name);
        }
    }
    
    private static class JaxWsPCL implements PropertyChangeListener {

        PropertyChangeListener originalListener;
        JaxWsPCL(PropertyChangeListener originalListener) {
            this.originalListener = originalListener;       
        }

        @Override
        public void propertyChange(PropertyChangeEvent evt) {
           Object oldValue = evt.getOldValue();
           JaxWsModelProvider factory = JaxWsModelProvider.getDefault();
           if (oldValue != null) {
               if (oldValue instanceof org.netbeans.modules.websvc.jaxwsmodel.project_config1_0.Client) {
                   oldValue = factory.createClient(oldValue);
               }
               if (oldValue instanceof org.netbeans.modules.websvc.jaxwsmodel.project_config1_0.Service) {
                   oldValue = factory.createService(oldValue);
               }
           }
           Object newValue = evt.getNewValue();
           if (newValue != null) {
               if (newValue instanceof org.netbeans.modules.websvc.jaxwsmodel.project_config1_0.Client) {
                   newValue = factory.createClient(newValue);
               }
               if (newValue instanceof org.netbeans.modules.websvc.jaxwsmodel.project_config1_0.Service) {
                   newValue = factory.createService(newValue);
               }
           }
           originalListener.propertyChange(new PropertyChangeEvent(evt.getSource(), evt.getPropertyName(), oldValue, newValue));
        }
        
        PropertyChangeListener getOriginalListener() {
            return originalListener;
        }
        
    }
    
    /** Registers ChangeListener for JaxWsModel object.
     *  The listener fires the ChangeEvent when FileObject is set for JaxWsModel
     *  (For projects, this occurs when jax-ws.xml is physicaly created in nbproject directory)   
     * 
     * @param listener ChangeListener instance
     */
    @Override
    public void addChangeListener(ChangeListener listener) {
        changeSupport.addChangeListener(listener);
    }
    /** Unregisters ChangeListener from JaxWsModel object.
     * 
     * @param listener ChangeListener instance
     */
    @Override
    public void removeChangeListener(ChangeListener listener) {
        changeSupport.removeChangeListener(listener);
    }
    
}
