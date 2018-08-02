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

package org.netbeans.modules.websvc.jaxws.api;

import java.io.IOException;
import java.net.URL;
import java.util.Iterator;
import java.util.List;
import org.netbeans.modules.j2ee.dd.api.webservices.WebservicesMetadata;
import org.netbeans.modules.j2ee.metadata.model.api.MetadataModel;
import org.netbeans.modules.websvc.jaxws.JAXWSSupportAccessor;
import org.netbeans.modules.websvc.jaxws.spi.JAXWSSupportImpl;
import org.netbeans.modules.websvc.jaxws.spi.JAXWSSupportProvider;
import org.netbeans.spi.project.support.ant.AntProjectHelper;
import org.openide.filesystems.FileObject;
import org.openide.util.Lookup;

/** JAXWSSupport should be used to manipulate projects representations
 *  of JAX-WS services.
 * <p>
 * A client may obtain a JAXWSSupport instance using
 * <code>JAXWSSupport.getJAXWSSupport(fileObject)</code> static
 * method, for any FileObject in the project directory structure.
 *
 * @author Peter Williams, Milan Kuchtiak
 */
public final class JAXWSSupport {
    
    private JAXWSSupportImpl impl;
    private static final Lookup.Result<JAXWSSupportProvider> implementations =
    Lookup.getDefault().lookup(new Lookup.Template<JAXWSSupportProvider>(JAXWSSupportProvider.class));
    
    static  {
        JAXWSSupportAccessor.DEFAULT = new JAXWSSupportAccessor() {
            public JAXWSSupport createJAXWSSupport(JAXWSSupportImpl spiWebServicesSupport) {
                return new JAXWSSupport(spiWebServicesSupport);
            }
            
            public JAXWSSupportImpl getJAXWSSupportImpl(JAXWSSupport wss) {
                return wss == null ? null : wss.impl;
            }
        };
    }
    
    private JAXWSSupport(JAXWSSupportImpl impl) {
        if (impl == null)
            throw new IllegalArgumentException();
        this.impl = impl;
    }
    
    /** Find the JAXWSSupport for given file or null if the file does not belong
     * to any module support web services.
     */
    public static JAXWSSupport getJAXWSSupport(FileObject f) {
        if (f == null) {
            throw new NullPointerException("Passed null to JAXWSSupport.getJAXWSSupport(FileObject)"); // NOI18N
        }
        Iterator it = implementations.allInstances().iterator();
        while (it.hasNext()) {
            JAXWSSupportProvider supportProvider = (JAXWSSupportProvider)it.next();
            JAXWSSupport wss = supportProvider.findJAXWSSupport(f);
            if (wss != null) {
                return wss;
            }
        }
        return null;
    }
    
    // Delegated methods from WebServicesSupportImpl
    
    /**
     * Add web service to jax-ws.xml intended for web services from java
     * @param serviceName service display name (name of the node ws will be presented in Netbeans), e.g. "SearchService"
     * @param serviceImpl package name of the implementation class, e.g. "org.netbeans.SerchServiceImpl"
     * @param isJsr109 Indicates if the web service is being created in a project that supports a JSR 109 container
     */
    public void addService(String serviceName, String serviceImpl, boolean isJsr109) {
        impl.addService(serviceName, serviceImpl, isJsr109);
    }
    
    /** Add web service to jax-ws.xml
     * intended for web services from wsdl
     * @param name service display name (name of the node ws will be presented in Netbeans), e.g. "SearchService"
     * @param serviceImpl package name of the implementation class, e.g. "org.netbeans.SerchServiceImpl"
     * @param wsdlUrl url of the local wsdl file, e.g. file:/home/userXY/documents/wsdl/SearchService.wsdl"
     * @param serviceName service name (from service wsdl element), e.g. SearchService
     * @param portName port name (from service:port element), e.g. SearchServicePort
     * @param packageName package name where java artifacts will be generated
     * @param isJsr109 Indicates if the web service is being created in a project that supports a JSR 109 container
     * @param useProvider Indicates if we should generate a Provider implementation
     * @return returns the unique IDE service name
     */
   public String addService(String name, String serviceImpl, String wsdlUrl, 
            String serviceName, String portName, String packageName, boolean isJsr109, boolean useProvider) {
        return impl.addService(name, serviceImpl, wsdlUrl, serviceName, portName, packageName, isJsr109, useProvider);
    }
    /**
     * Returns the list of web services in the project
     * @return list of web services
     */
    public List getServices() {
        return impl.getServices();
    }  
  
    /**
     * Remove the web service entries from the project properties
     * @param serviceName service IDE name 
     * project.xml files
     */
    public void removeService(String serviceName) {
        impl.removeService(serviceName);
    }
    
    /**
     * Notification when Service (created from java) is removed from jax-ws.xml
     * (JAXWSSupport needs to react when @WebService annotation is removed 
     * or when impl.class is removed (manually from project)
     * @param serviceName service IDE name 
     */
    public void serviceFromJavaRemoved(String serviceName) {
        impl.serviceFromJavaRemoved(serviceName);
    }
    
    /** Get the name of the implementation class
     * given the service (ide) name
     * @param serviceName service IDE name 
     * @return service implementation class package name
     */
    public String getServiceImpl(String serviceName) {
        return impl.getServiceImpl(serviceName);
    }
    
    /** Determine if the web service was created from WSDL
     * @param serviceName service name 
     */
    public boolean isFromWSDL(String serviceName) {
        return impl.isFromWSDL(serviceName);
    }
    
    /** Get WSDL folder for the project (folder containing wsdl files)
     *  The folder is used to save remote or local wsdl files to be available within the jar/war files.
     *  it is usually META-INF/wsdl folder (or WEB-INF/wsdl for web application)
     *  @param createFolder if (createFolder==true) the folder will be created (if not created before)
     *  @return the file object (folder) where wsdl files are located in project 
     */
    public FileObject getWsdlFolder(boolean create) throws java.io.IOException {
        return impl.getWsdlFolder(create);
    }
    
    /** Get folder for local WSDL and XML artifacts for given service
     * This is the location where wsdl/xml files are downloaded to the project.
     * JAX-WS java artifacts will be generated from these local files instead of remote.
     * @param serviceName service IDE name (the web service node display name)
     * @param createFolder if (createFolder==true) the folder will be created (if not created before)
     * @return the file object (folder) where wsdl files are located in project 
     */
    public FileObject getLocalWsdlFolderForService(String serviceName, boolean createFolder) {
        return impl.getLocalWsdlFolderForService(serviceName,createFolder);
    }
    
    /** Get folder for local jaxb binding (xml) files for given service
     *  This is the location where external jaxb binding files are downloaded to the project.
     *  JAX-WS java artifacts will be generated using these local binding files instead of remote.
     * @param serviceName service IDE name (the web service node display name)
     * @param createFolder if (createFolder==true) the folder will be created (if not created before)
     * @return the file object (folder) where jaxb binding files are located in project 
     */
    public FileObject getBindingsFolderForService(String serviceName, boolean createFolder) {
        return impl.getBindingsFolderForService(serviceName,createFolder);
    }
    
    /**
     * Get the AntProjectHelper from the project
     */
    public AntProjectHelper getAntProjectHelper() {
        return impl.getAntProjectHelper();
    }
    
    /** Get EntityCatalog for local copies of wsdl and schema files
     */
    public URL getCatalog() {
        return impl.getCatalog();
    }
    
    /** Get wsdlLocation information
     * Useful for web service from wsdl (the @WebService wsdlLocation attribute)
     * @param serviceName service "display" name
     */
    public String getWsdlLocation(String serviceName) {
        return impl.getWsdlLocation(serviceName);
    }
    
    /**
     * Remove all entries associated with a non-JSR 109 entries
     * This may include entries in the module's deployment descriptor,
     * and entries in the implementation-specific descriptor file, sun-jaxws.xml.
     * This is provided as a service so that the node can also use it for cleanup.
     */
    public void removeNonJsr109Entries(String serviceName) throws IOException{
        impl.removeNonJsr109Entries(serviceName);
    }
    /**
     * Returns the directory that contains the deployment descriptor in the project
     */    
    public FileObject getDeploymentDescriptorFolder(){
        return impl.getDeploymentDescriptorFolder();
    }
    /**
     * Returns a metadata model of a webservices deployment descriptor
     *
     * @return metadata model of a webservices deployment descriptor
     */
    public MetadataModel<WebservicesMetadata> getWebservicesMetadataModel() {
        return impl.getWebservicesMetadataModel();
    }
}
