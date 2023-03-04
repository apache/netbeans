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

package org.netbeans.modules.websvc.api.jaxws.project.config;

import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.io.OutputStream;
import javax.swing.event.ChangeListener;
import org.openide.filesystems.FileObject;

/** Provides information about web services and clients in a project
 * Provides information used for build-impl generation
 * Working over nbproject/jax-ws.xml file
 */
public interface JaxWsModel {
    
    public Service[] getServices();
    
    public void setJsr109(Boolean jsr109);
    
    public Boolean getJsr109();
    
    public Service findServiceByName(String name);
    
    public Service findServiceByImplementationClass(String wsClassName);

    
    public boolean removeService(String name);
    
    public boolean removeServiceByClassName(String webserviceClassName);
    
    public Service addService(String name, String implementationClass)
    throws ServiceAlreadyExistsExeption;
    
    public Service addService(String name, String implementationClass, String wsdlUrl, String serviceName, String portName, String packageName)
    throws ServiceAlreadyExistsExeption;
    
    public Client[] getClients();
    
    public Client findClientByName(String name);
    
    public Client findClientByWsdlUrl(String wsdlUrl);

    public boolean removeClient(String name) ;
    
    public Client addClient(String name, String wsdlUrl, String packageName)
    throws ClientAlreadyExistsExeption;
    
    public void addPropertyChangeListener(PropertyChangeListener l);
    
    public void removePropertyChangeListener(PropertyChangeListener l);
    
    public void merge(JaxWsModel newJaxWs);
    
    public void write(OutputStream os) throws IOException;
    
    public FileObject getJaxWsFile();
    
    public void setJaxWsFile(FileObject fo);
    
    public void write() throws IOException;
    
    public void addServiceListener(ServiceListener listener);
    
    public void removeServiceListener(ServiceListener listener);
    
    public static interface ServiceListener {
        
        public void serviceAdded(String name, String implementationClass);
        
        public void serviceRemoved(String name);
        
    }
    
    /** Registers ChangeListener for JaxWsModel object.
     *  The listener fires the ChangeEvent when FileObject is set for JaxWsModel
     *  (For projects, this occurs when jax-ws.xml is physicaly created in nbproject directory)   
     * 
     * @param listener ChangeListener instance
     */
    public void addChangeListener(ChangeListener listener);
    
    /** Unregisters ChangeListener from JaxWsModel object.
     * 
     * @param listener ChangeListener instance
     */
    public void removeChangeListener(ChangeListener listener);
    
}
