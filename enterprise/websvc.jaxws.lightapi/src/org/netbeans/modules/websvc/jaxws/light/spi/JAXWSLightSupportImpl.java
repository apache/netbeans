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

package org.netbeans.modules.websvc.jaxws.light.spi;

import java.net.URL;
import java.util.List;
import org.netbeans.modules.j2ee.dd.api.webservices.WebservicesMetadata;
import org.netbeans.modules.j2ee.metadata.model.api.MetadataModel;
import org.netbeans.modules.websvc.jaxws.light.api.JaxWsService;
import org.openide.filesystems.FileObject;

/** SPI for JAXWSSupport.
 *
 * @author Milan Kuchtiak
 */

public interface JAXWSLightSupportImpl {

    /** Add JAX-WS service/client to project.
     *
     * @param service service or client
     */
    void addService(JaxWsService service);

    /** Returns the list of JAX-WS services and clients.
     *
     * @return list of web services
     */
    List<JaxWsService> getServices();

    /** Remove JAX-WS service from project.
     *
     * @param service service
     */
    void removeService(JaxWsService service);

    /** Get deployment descriptor folder for the project (folder containing configuration files, like web.xml).
     *
     *  @return the folder where xml configuration files are located
     */
    FileObject getDeploymentDescriptorFolder();

    /** Get folder for local WSDL and XML artifacts for given service.
     *
     * This is the location where wsdl/xml files are downloaded to the project.
     * JAX-WS java artifacts will be generated from these local files instead of remote.
     * @param createFolder if (createFolder==true) the folder will be created (if not created before)
     * @return the file object (folder) where wsdl files are located in project
     */
    FileObject getWsdlFolder(boolean createFolder);

    /** Get folder for local jaxb binding (xml) files for given service.
     *
     *  This is the location where external jaxb binding files are downloaded to the project.
     *  JAX-WS java artifacts will be generated using these local binding files instead of remote.
     *
     * @param createFolder if (createFolder==true) the folder will be created (if not created before)
     * @return the file object (folder) where jaxb binding files are located in project
     */
    FileObject getBindingsFolder(boolean createFolder);

    /** Get EntityCatalog for local copies of wsdl and schema files.
     * @return URL for catalog file
     */
    URL getCatalog();

    /** Get metadata model of a webservices deployment descriptor.
     *
     * @return metadata model of a webservices deployment descriptor
     */
    MetadataModel<WebservicesMetadata> getWebservicesMetadataModel();

}
