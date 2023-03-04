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

/** Object representing JAX-WS service or service reference (client).
 *
 * @author mkuchtiak
 */
public class JaxWsService {
    private String serviceName;
    private String portName;
    private String wsdlUrl;
    private String localWsdl;
    private String wsdlLocation;
    private String implementationClass;
    private boolean serviceProvider = true;
    private String handlerBindingFile;
    private String id;

    /** Constructor useful to create web service based on @WebService annotation (WS from Java).
     *
     * @param serviceName service name, usually java class name
     * @param implementationClass path.to implementation class, e.g. org.mycompany.services.HelloService
     */
    public JaxWsService(String serviceName, String implementationClass) {
        this.serviceName = serviceName;
        this.implementationClass = implementationClass;
    }
    /** Constructor useful to create web service reference (WS Client), or web service based on WSDL (WS from WSDL).
     *
     * @param localWsdl reference to wsdl file, relative to local wsdl folder (can vary in different project types)
     * @param serviceProvider true for service providers, false for clients (WS Reference)
     * @see JAXWSLightSupport#getLocalWsdlFolder
     */
    public JaxWsService(String localWsdl, boolean serviceProvider) {
        this.localWsdl = localWsdl;
        this.serviceProvider = serviceProvider;
    }
    /** returns true if service(service provider), false if client(service consumer).
     *
     * @return true if service(service provider), false if client(service consumer)
     */
    public boolean isServiceProvider() {
        return serviceProvider;
    }
    /** sets information if object is service provider or consumer.
     *
     * @param serviceProvider true if service(service provider), false if client(service consumer)
     */
    public void setServiceProvider(boolean serviceProvider) {
        this.serviceProvider = serviceProvider;
    }
    /** Returns original URL of WSDL file (useful for Refresh action).
     *
     * @return original URL of WSDL file
     */
    public String getWsdlUrl() {
        return wsdlUrl;
    }
    /** Sets the original URL of WSDL file.
     *
     * @param wsdlUrl URL of WSDL file
     */
    public void setWsdlUrl(String wsdlUrl) {
        this.wsdlUrl = wsdlUrl;
    }
    /** Returns path to local wsdl file (after WSDL file is downloaded to project).
     * @see JAXWSLightSupport#getLocalWsdlFolder
     * @return path to wsdl file, relative to local wsdl folder
     */
    public String getLocalWsdl() {
        return localWsdl;
    }
    /** Sets path to local WSDL file.
     *
     * @param localWsdl path to wsdl file, relative to local wsdl folder
     * @see JAXWSLightSupport#getLocalWsdlFolder
     */
    public void setLocalWsdl(String localWsdl) {
        this.localWsdl = localWsdl;
    }
    /** Returns package name of WS implementation class.
     *
     * @return package name of WS implementation class
     */
    public String getImplementationClass() {
        return implementationClass;
    }
    /** Sets package name for implementation class.
     *
     * @param implementationClass package name of implementation class
     */
    public void setImplementationClass(String implementationClass) {
        this.implementationClass = implementationClass;
    }
    /** Returns WSDL port name of the web service, useful for WS from WSDL
     * to identify port name for which web service is implemented.
     *
     * @return WSDL port name
     */
    public String getPortName() {
        return portName;
    }
    /** Sets WSDL port name for web service, useful for WS from WSDL
     * to specify port name for which web service is implemented.
     *
     * @param portName WSDL port name
     */
    public void setPortName(String portName) {
        this.portName = portName;
    }
    /** Returns WSDL service name of the web service, useful for WS from WSDL
     * to identify service name for which web service is implemented.
     *
     * @return WSDL service name
     */
    public String getServiceName() {
        return serviceName;
    }
    /** Sets WSDL service name for web service, useful for WS from WSDL
     * to specify service name for which web service is implemented.
     *
     * @param serviceName WSDL service name
     */
    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }
    /** Returns wsdlLocation information of WSDL file in jar/war file (WEB-INF/wsdl/HelloService.wsdl).
     *
     * @return wsdl location of the web service in jar/war file
     */
    public String getWsdlLocation() {
        return wsdlLocation;
    }
    /** Sets wsdlLocation information of WSDL file in jar/war file  (WEB-INF/wsdl/HelloService.wsdl).
     *
     * @param wsdlLocation wsdl location of the web service in jar/war file
     */
    public void setWsdlLocation(String wsdlLocation) {
        this.wsdlLocation = wsdlLocation;
    }

    /** Returns handler binding file information.
     *
     * @return handler binding file path
     */
    public String getHandlerBindingFile() {
        return handlerBindingFile;
    }

    /** Sets handler binding file information.
     *
     * @param handlerBindingFile handler binding file
     */
    public void setHandlerBindingFile(String handlerBindingFile) {
        this.handlerBindingFile = handlerBindingFile;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

}
