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

package org.netbeans.modules.websvc.spi.jaxws.client;

import java.io.IOException;
import java.net.URL;
import java.util.List;
import org.netbeans.modules.websvc.api.jaxws.project.config.Client;
import org.netbeans.spi.project.support.ant.AntProjectHelper;

import org.openide.filesystems.FileObject;
import org.openide.nodes.Node;

/**
 *
 * @author Peter Williams, Milan Kuchtiak
 */

/** SPI for JAXWSClientSupport
 */
public interface JAXWSClientSupportImpl {

    public static final String XML_RESOURCES_FOLDER="xml-resources"; //NOI18N
    public static final String CLIENTS_LOCAL_FOLDER="web-service-references"; //NOI18N
    public static final String CATALOG_FILE="catalog.xml"; //NOI18N


    /** Add JAX-WS Client to project.
     *  <ul>
     *  <li> add client element to jax-ws.xml (Netbeans specific configuration file)
     *  <li> download the wsdl file(s) and all related XML artifacts to the project
     *  <li> generate JAX-WS artifacts for web service specified by wsdlUrl.
     *  <li> this can be achieved by creating specific target in build-impl.xml, that calls wsimport task.
     *  </ul>
     * @param clientName proposed name for the client (the web service reference node display name)
     * @param wsdlURL URL for web service WSDL file
     * @param isJsr109 flag indicating the need to add JAX-WS libraries to project:
     *        if (isJsr109==false) JAX-WS libraries should be added to the project classpath
     * @return unique name for WS Client in the project(can be different than requested clientName)
     */
    public String addServiceClient(String clientName, String wsdlUrl, String packageName, boolean isJsr109);

    /** Get WSDL folder for the project (folder containing wsdl files)
     *  The folder is used to save remote or local wsdl files to be available within the jar/war files.
     *  it is usually META-INF/wsdl folder (or WEB-INF/wsdl for web application)
     *  @param createFolder if (createFolder==true) the folder will be created (if not created before)
     *  @return the file object (folder) where wsdl files are located in project
     */
    public FileObject getWsdlFolder(boolean createFolder) throws IOException;

    /** Get folder for local WSDL and XML artifacts for given client
     * This is the location where wsdl/xml files are downloaded to the project.
     * JAX-WS java artifacts will be generated from these local files instead of remote.
     * @param clientName client name (the web service reference node display name)
     * @param createFolder if (createFolder==true) the folder will be created (if not created before)
     * @return the file object (folder) where wsdl files are located in project
     */
    public FileObject getLocalWsdlFolderForClient(String clientName, boolean createFolder);

    /** Get folder for local jaxb binding (xml) files for given client
     *  This is the location where external jaxb binding files are downloaded to the project.
     *  JAX-WS java artifacts will be generated using these local binding files instead of remote.
     * @param clientName client name (the web service reference node display name)
     * @param createFolder if (createFolder==true) the folder will be created (if not created before)
     * @return the file object (folder) where jaxb binding files are located in project
     */
    public FileObject getBindingsFolderForClient(String clientName, boolean createFolder);

    /** Remove JAX-WS Client from project.
     * <ul>
     *  <li> remove client element from jax-ws.xml (Netbeans specific configuration file)
     *  <li> remove all WSDL/XML artifacts related to this client
     *  <li> remove all JAX-WS java artifacts generated for this client
     * </ul>
     * @param clientName client name (the web service reference node display name)
     */
    public void removeServiceClient(String serviceName);

    /** Get list of all JAX-WS Clients in project
     * @param clientName client name (the web service reference node display name)
     * @return a list of Clients
     */
    public List<Client> getServiceClients();

    /** gets the URL of catalog.xml file
     *  (the catalog is used by wsimport to locate local wsdl/xml resources)
     * @return URL url of the car
     */
    public URL getCatalog();

    /** intended to be used to obtain service-ref name for given web service reference
     *  (currently not used in projects)
     */
    public String getServiceRefName(Node clientNode);

    /**
     * Get the AntProjectHelper from the project
     */
    public AntProjectHelper getAntProjectHelper();

}
