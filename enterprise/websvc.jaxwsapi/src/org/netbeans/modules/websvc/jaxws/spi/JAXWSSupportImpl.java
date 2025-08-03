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

package org.netbeans.modules.websvc.jaxws.spi;

import java.io.IOException;
import java.net.URL;
import java.util.List;
import org.netbeans.modules.j2ee.dd.api.webservices.WebservicesMetadata;
import org.netbeans.modules.j2ee.metadata.model.api.MetadataModel;
import org.netbeans.spi.project.support.ant.AntProjectHelper;
import org.openide.filesystems.FileObject;

/**
 * SPI for JAXWSSupport
 * @author Milan Kuchtiak
 */ 
public interface JAXWSSupportImpl {
    
    public static final String XML_RESOURCES_FOLDER="xml-resources"; //NOI18N
    public static final String SERVICES_LOCAL_FOLDER="web-services"; //NOI18N
    public static final String CATALOG_FILE="catalog.xml"; //NOI18N
    
    /**
     * Add web service to jax-ws.xml intended for web services from java
     * @param serviceName service display name (name of the node ws will be presented in Netbeans), e.g. "SearchService"
     * @param serviceImpl package name of the implementation class, e.g. "org.netbeans.SerchServiceImpl"
     * @param isJsr109 Indicates if the web service is being created in a project that supports a JSR 109 container
     */
    public void addService(String serviceName, String serviceImpl, boolean isJsr109);
    
    /** Add web service to jax-ws.xml
     * intended for web services from wsdl
     * @param name service display name (name of the node ws will be presented in Netbeans), e.g. "SearchService"
     * @param serviceImpl package name of the implementation class, e.g. "org.netbeans.SerchServiceImpl"
     * @param wsdlUrl url of the local wsdl file, e.g. file:/home/userXY/documents/wsdl/SearchService.wsdl"
     * @param serviceName service name (from service wsdl element), e.g. SearchService
     * @param portName port name (from service:port element), e.g. SearchServicePort
     * @param packageName package name where java artifacts will be generated
     * @param isJsr109 Indicates if the web service is being created in a project that supports a JSR 109 container
     * @param useProvider indicates if the generated impl is a Provider
     * @return returns the unique IDE service name
     */
    public String addService(String name, String serviceImpl, String wsdlUrl, String serviceName, 
            String portName, String packageName, boolean isJsr109,  boolean useProvider);
    
    /**
     * Returns the list of web services in the project
     * @return list of web services
     */
    public List/*Service*/ getServices();    
    
    /**
     * Remove the web service entries from the project properties
     * @param serviceName service IDE name 
     * project.xml files
     */
    public void removeService(String serviceName);
    
    /**
     * Notification when Service (created from java) is removed from jax-ws.xml
     * (JAXWSSupport needs to react when @WebService annotation is removed 
     * or when impl.class is removed (manually from project)
     * @param serviceName service IDE name 
     */
    public void serviceFromJavaRemoved(String serviceName);
    
    /** Get the name of the implementation class
     * given the service (ide) name
     * @param serviceName service IDE name 
     * @return service implementation class package name
     */
    public String getServiceImpl(String serviceName);
    
    /** Determine if the web service was created from WSDL
     * @param serviceName service name 
     */
    public boolean isFromWSDL(String serviceName);
    

    /** Get WSDL folder for the project (folder containing wsdl files)
     *  The folder is used to save remote or local wsdl files to be available within the jar/war files.
     *  it is usually META-INF/wsdl folder (or WEB-INF/wsdl for web application)
     *  @param createFolder if (createFolder==true) the folder will be created (if not created before)
     *  @return the file object (folder) where wsdl files are located in project 
     */
    public FileObject getWsdlFolder(boolean create) throws java.io.IOException;
    
    /** Get folder for local WSDL and XML artifacts for given service
     * This is the location where wsdl/xml files are downloaded to the project.
     * JAX-WS java artifacts will be generated from these local files instead of remote.
     * @param serviceName service IDE name (the web service node display name)
     * @param createFolder if (createFolder==true) the folder will be created (if not created before)
     * @return the file object (folder) where wsdl files are located in project 
     */
    public FileObject getLocalWsdlFolderForService(String serviceName, boolean createFolder);
    
    /** Get folder for local jaxb binding (xml) files for given service
     *  This is the location where external jaxb binding files are downloaded to the project.
     *  JAX-WS java artifacts will be generated using these local binding files instead of remote.
     * @param serviceName service IDE name (the web service node display name)
     * @param createFolder if (createFolder==true) the folder will be created (if not created before)
     * @return the file object (folder) where jaxb binding files are located in project 
     */
    public FileObject getBindingsFolderForService(String serviceName, boolean createFolder);
    
    /**
     * Get the AntProjectHelper from the project
     */
    public AntProjectHelper getAntProjectHelper();
    
    /** Get EntityCatalog for local copies of wsdl and schema files
     */
    public URL getCatalog();
    
    /** Get wsdlLocation information
     * Useful for web service from wsdl (the @WebService wsdlLocation attribute)
     * @param serviceName service "display" name
     */
    public String getWsdlLocation(String serviceName);
   
    /**
     * Remove all entries associated with a non-JSR 109 entries
     * This may include entries in the module's deployment descriptor,
     * and entries in the implementation-specific descriptor file, sun-jaxws.xml.
     * This is provided as a service so that the node can also use it for cleanup.
     */
    public void removeNonJsr109Entries(String serviceName) throws IOException;
    
    /**
     * Returns the directory that contains the deployment descriptor in the project
     */
    public FileObject getDeploymentDescriptorFolder();

    /**
     * Returns a metadata model of a webservices deployment descriptor
     *
     * @return metadata model of a webservices deployment descriptor
     */
    public MetadataModel<WebservicesMetadata> getWebservicesMetadataModel();
    
}
