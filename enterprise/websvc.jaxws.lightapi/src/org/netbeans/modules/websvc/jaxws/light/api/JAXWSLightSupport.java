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

package org.netbeans.modules.websvc.jaxws.light.api;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.net.URL;
import java.util.List;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock.ReadLock;
import java.util.concurrent.locks.ReentrantReadWriteLock.WriteLock;

import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.modules.j2ee.dd.api.webservices.WebservicesMetadata;
import org.netbeans.modules.j2ee.metadata.model.api.MetadataModel;
import org.netbeans.modules.websvc.jaxws.light.JAXWSLightSupportAccessor;
import org.netbeans.modules.websvc.jaxws.light.spi.JAXWSLightSupportImpl;
import org.netbeans.modules.websvc.jaxws.light.spi.JAXWSLightSupportProvider;
import org.openide.filesystems.FileObject;

/** JAXWSLightSupport should be used to manipulate projects representations
 *  of JAX-WS services.
 * <p>
 * A client may obtain a JAXWSLightSupport instance using
 * <code>JAXWSLightSupport.getJAXWSLightSupport(fileObject)</code> static
 * method, for any FileObject in the project directory structure.
 *
 * @author Milan Kuchtiak
 */
public final class JAXWSLightSupport {

    /**used to notify property change listeners when JAX-WS service is added. */
    public static final String PROPERTY_SERVICE_ADDED = "service-added"; //NOI18N

    /**used to notify property change listeners when JAX-WS service is removed. */
    public static final String PROPERTY_SERVICE_REMOVED = "service-removed"; //NOI18N

    private JAXWSLightSupportImpl impl;
    private PropertyChangeSupport propertyChangeSupport;
    private ReentrantReadWriteLock myLock;
    private ReadLock myReadLock;
    private WriteLock myWriteLoick; 

    static  {
        JAXWSLightSupportAccessor.DEFAULT = new JAXWSLightSupportAccessor() {
            public JAXWSLightSupport createJAXWSSupport(JAXWSLightSupportImpl spiWebServicesSupport) {
                return new JAXWSLightSupport(spiWebServicesSupport);
            }

            public JAXWSLightSupportImpl getJAXWSSupportImpl(JAXWSLightSupport wss) {
                return wss == null ? null : wss.impl;
            }
        };
    }

    private JAXWSLightSupport(JAXWSLightSupportImpl impl) {

        if (impl == null) {
            throw new IllegalArgumentException();
        }
        this.impl = impl;
        propertyChangeSupport = new PropertyChangeSupport(this);
        myLock = new ReentrantReadWriteLock();
        myReadLock = myLock.readLock();
        myWriteLoick = myLock.writeLock();
    }

    /** Returns instance of JAXWSLightSupport from project's lookup, or null if not present.
     *
     * @param f sole file object in project
     * @return JAXWSLightSupport object
     */
    public static JAXWSLightSupport getJAXWSLightSupport(FileObject f) {

        Project project = FileOwnerQuery.getOwner(f);
        if (project != null) {
            JAXWSLightSupportProvider provider =
                    (JAXWSLightSupportProvider) project.getLookup().lookup(JAXWSLightSupportProvider.class);
            if (provider != null) {
                return provider.findJAXWSSupport();
            }
        }
        return null;
    }

    // Delegated methods from WebServicesSupportImpl

    /** Add JAX-WS service/client to project.
     *
     * @param service service or client
     */
    public void addService(JaxWsService service) {
        checkLock();
        impl.addService(service);
        propertyChangeSupport.firePropertyChange(PROPERTY_SERVICE_ADDED, null, service);
    }

    /** Returns the list of JAX-WS services and clients.
     *
     * @return list of web services
     */
    public List<JaxWsService> getServices() {
        myReadLock.lock();
        try {
            return impl.getServices();
        }
        finally {
            myReadLock.unlock();
        }
    }

    /** Remove JAX-WS service from project.
     *
     * @param service service
     */
    public void removeService(JaxWsService service) {
        checkLock();
        impl.removeService(service);
        propertyChangeSupport.firePropertyChange(PROPERTY_SERVICE_REMOVED, service, null);
    }
    
    public void runAtomic( Runnable runnable ){
        myWriteLoick.lock();
        try {
            runnable.run();
        }
        finally {
            myWriteLoick.unlock();
        }
    }

    /** Get deployment descriptor folder for the project (folder containing configuration files, like web.xml).
     *
     *  @return the folder where xml configuration files are located
     */
    public FileObject getDeploymentDescriptorFolder() {
        return impl.getDeploymentDescriptorFolder();
    }

    /** Get folder for local WSDL and XML artifacts for given service.
     *
     * This is the location where wsdl/xml files are downloaded to the project.
     * JAX-WS java artifacts will be generated from these local files instead of remote.
     * @param createFolder if (createFolder==true) the folder will be created (if not created before)
     * @return the file object (folder) where wsdl files are located in project
     */
    public FileObject getWsdlFolder(boolean createFolder) {
        return impl.getWsdlFolder(createFolder);
    }

    /** Get folder for local jaxb binding (xml) files for given service.
     *
     *  This is the location where external jaxb binding files are downloaded to the project.
     *  JAX-WS java artifacts will be generated using these local binding files instead of remote.
     *
     * @param createFolder if (createFolder==true) the folder will be created (if not created before)
     * @return the file object (folder) where jaxb binding files are located in project
     */
    public FileObject getBindingsFolder(boolean createFolder) {
        return impl.getBindingsFolder(createFolder);
    }

    /** Get EntityCatalog for local copies of wsdl and schema files.
     * @return URL for catalog file
     */
    public URL getCatalog() {
        return impl.getCatalog();
    }

    /** Get metadata model of a webservices deployment descriptor.
     *
     * @return metadata model of a webservices deployment descriptor
     */
    public MetadataModel<WebservicesMetadata> getWebservicesMetadataModel() {
        return impl.getWebservicesMetadataModel();
    }
    /** Register property change listener to JAX-WS support
     *
     * @param pcl
     */
    public void addPropertyChangeListener(PropertyChangeListener pcl) {
        propertyChangeSupport.addPropertyChangeListener(pcl);
    }
    /** Unregister property change listener from JAX-WS support
     *
     * @param pcl
     */
    public void removePropertyChangeListener(PropertyChangeListener pcl) {
        propertyChangeSupport.removePropertyChangeListener(pcl);
    }
    
    private void checkLock() {
        if ( !myWriteLoick.isHeldByCurrentThread() ){
            throw new IllegalStateException("Trying to invoke mutable operation " +
                    "outside of transaction. Atomic access should be used " +
                    "for mutable opertations");
        }
    }
}
