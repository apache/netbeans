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
package org.netbeans.modules.j2ee.sun.dd.api.services;

import java.io.File;

import org.netbeans.modules.j2ee.sun.dd.api.common.MessageSecurityBinding;


/** Service interface that defines capabilities for third party modules to manipulate
 *  the security bindings present in the SJSAS server specific deployment descriptors.
 * 
 * @author Peter Williams
 */
public interface MessageSecurityProvider {
    
    /* Retrieve current MessageSecurityBinding data for the specified endpoint
     *
     * @param sunDD File reference to the primary sun deployment descriptor file for this
     *  j2ee/javaEE module.
     * @param endpointName Value from the webservice-description-name field in webservices.xml
     *  or equivalent annotation for the pertinent webservice endpoint.
     * @param portName Value from the port-component-name field in webservices.xml or
     *  equivalent annotation for the pertinent port-component of the endpoint.
     *
     * @return Copy of the MessageSecurityBinding data for the specified endpoint or null 
     *  if the endpoint cannot be located, or if there is not currently any binding information.
     *
     * @throws IllegalArgumentException if sunDD does not refer to the proper descriptor file
     *  or if either the endpointName or portName fields are empty (null or "").
     * @throws IllegalStateException if the SJSAS configuration has not been initialized.
     *  This could occur if SJSAS is not the current selected server for the project.
     *
     * FIXME How to differentiate the errors "service not found", "port not found", and 
     *   "no binding" from each other?  Currently implementation does not distinguish them.
     */
    public MessageSecurityBinding getEndpointBinding(File sunDD, String endpointName, String portName);

    
    /* Set new MessageSecurityBinding data for the specified endpoint.
     *
     *  - If the configuration file is not open in an editor in any form, this new data will
     *  be merged and saved.
     *  - If the configuration file is open in the XML view, an attempt will be made to merge
     *  the graphs.  If the file was previously unchanged, it will remain so and the new data
     *  will be saved.  If there were unsaved changes, the file will remain dirty and the changes
     *  will not be saved.  If the merge fails, the user will be notified of the problem.  The
     *  security settings may need to be fixed manually if this happens.
     *  - If the configuration file is open in the GUI view, the data will be merged and the
     *  editor will be marked dirty.  If the user subsequently closes the file without saving
     *  the changes will be lost.
     *
     * @param sunDD File reference to the primary sun deployment descriptor file for this
     *  j2ee/javaEE module.
     * @param endpointName Value from the webservice-description-name field in webservices.xml
     *  or equivalent annotation for the pertinent webservice endpoint.
     * @param portName Value from the port-component-name field in webservices.xml or
     *  equivalent annotation for the pertinent port-component of the endpoint.
     * @param binding Binding data to save.
     *
     * @returns true if succcessful, false on failure.
     *
     * @throws IllegalArgumentException if sunDD does not refer to the proper descriptor file
     *  or if either the endpointName or portName fields are empty (null or "") or if there is
     *  no defined endpoint with the specified portName.
     * @throws IllegalStateException if the SJSAS configuration has not been initialized.
     *  This could occur if SJSAS is not the current selected server for the project.
     */
    public boolean setEndpointBinding(File sunDD, String endpointName, String portName, MessageSecurityBinding binding);


    /* Retrieve current MessageSecurityBinding data for the specified webservice client.
     *
     * NOTE: Temporarily, this API does not allow the user to specify which port they
     *  want the binding for.  If the client defines multiple ports, then only the first
     *  MessageSecurityBinding will be returned.  Be sure to note corollary in set method.
     *
     * @param sunDD File reference to the primary sun deployment descriptor file for this
     *  j2ee/javaEE module.
     * @param serviceRefName Value from the service-ref-name field in sun-web.xml, sun-ejb-jar.xml,
     *  sun-application-client.xml, or equivalent annotation for the pertinent webservice client.
     *
     * @deprecated
     */
    public MessageSecurityBinding getServiceRefBinding(File sunDD, String serviceRefName);
    
    
    /* Retrieve current MessageSecurityBinding data for the specified wsdl-port (determined
     * by the namespaceURI and localpart fields) of the specified webservice client.
     *
     * @param sunDD File reference to the primary sun deployment descriptor file for this
     *  j2ee/javaEE module.
     * @param serviceRefName Value from the service-ref-name field in sun-web.xml, sun-ejb-jar.xml,
     *  sun-application-client.xml, or equivalent annotation for the pertinent webservice client.
     * @param namespaceURI namespace URI designation for the wsdl-port this binding will be associated
     *  with.  This is typically the target namespace, but depends on the WSDL file.  This parameter
     *  cannot be null or empty.
     * @param localpart name of the wsdl-port as specified in the WSDL file under the service
     *  entry.  This parameter cannot be null or empty.
     *
     * @throws IllegalArgumentException if sunDD does not refer to the proper descriptor file
     *  or if the serviceRefName, namespaceURI, or localpart fields are empty (null or "").
     * @throws IllegalStateException if the SJSAS configuration has not been initialized.
     *  This could occur if SJSAS is not the current selected server for the project.
     *
     * FIXME How to differentiate the errors "service ref found", "wsdl-port" not found, and 
     * "no binding" from each other?  Currently implementation does not distinguish them.
     */
    public MessageSecurityBinding getServiceRefBinding(File sunDD, String serviceRefName, 
            String namespaceURI, String localpart);
    
    
    /* Set the MessageSecurityBinding data for the specified webservice client.  The
     * current implementation applies this binding data to all configured ports on this
     * client.
     *
     * Note that the binding instance passed in is cloned for all ports it is configured to
     * so subsequent modification of that instance after this call returns will not affect
     * the data that was configured by this call.  A subsequent call to setServiceRefBinding()
     * would be required to apply new binding data.
     *
     * @param sunDD File reference to the primary sun deployment descriptor file for this
     *  j2ee/javaEE module.
     * @param serviceRefName Value from the service-ref-name field in sun-web.xml, sun-ejb-jar.xml,
     *  sun-application-client.xml, or equivalent annotation for the pertinent webservice client.
     * @param binding The MessageSecurityBinding data to apply to all ports.
     *
     * @returns true if succcessful, false on failure.
     *
     * @deprecated
     */
    public boolean setServiceRefBinding(File sunDD, String serviceRefName, MessageSecurityBinding binding);

    
    /* Set the MessageSecurityBinding data for the specified wsdl-port (determined
     * by the namespaceURI and localpart fields) of the specified webservice client.
     * If a matching wsdl-port is not found, a new one will be created and initialized
     * with the binding data.
     *
     * Note that the binding instance passed in is cloned so subsequent modification of
     * that instance after this call returns will not affect the data that was configured
     * by this call.  A subsequent call to setServiceRefBinding() would be required to
     * apply new binding data.
     *
     * @param sunDD File reference to the primary sun deployment descriptor file for this
     *  j2ee/javaEE module.
     * @param serviceRefName Value from the service-ref-name field in sun-web.xml, sun-ejb-jar.xml,
     *  sun-application-client.xml, or equivalent annotation for the pertinent webservice client.
     * @param namespaceURI namespace URI designation for the wsdl-port this binding will be associated
     *  with.  This is typically the target namespace, but depends on the WSDL file.  This parameter
     *  cannot be null or empty.
     * @param localpart name of the wsdl-port as specified in the WSDL file under the service
     *  entry.  This parameter cannot be null or empty.
     * @param binding The MessageSecurityBinding data to apply to all ports.
     *
     * @returns true if succcessful, false on failure.
     *
     * @throws IllegalArgumentException if sunDD does not refer to the proper descriptor file
     *  or if the serviceRefName, namespaceURI, or localpart fields are empty (null or "").
     * @throws IllegalStateException if the SJSAS configuration has not been initialized.
     *  This could occur if SJSAS is not the current selected server for the project.
     * @throws UnsupportedOperationException if called for an EJB jar project and a
     *  service-ref of the correct name does not already exist.  (Need to know ejb-name
     *  to create assocated service-ref.)
     */
    public boolean setServiceRefBinding(File sunDD, String serviceRefName, String namespaceURI, 
            String localpart, MessageSecurityBinding binding);

        
    /* Creates a new MessageSecurityBinding instance appropriate for the sun 
     * deployment descriptor specified.  Requires that Sun Deployment Configuration 
     * subsystem be initialized for this project, which implies that SJSAS is
     * the current selected server.
     *
     * @param sunDD File refering to the primary sun deployment descriptor.  The
     *  actual configuration file does not have to exist.
     *
     * @return a new empty instance of a MessageSecurityBinding object versioned for the
     *  current configuration.
     *
     * @throws IllegalArgumentException if sunDD does not refer to the proper descriptor file.
     * @throws IllegalStateException if the configuration has not been initialized
     *  yet.  This can happen if the selected server for the project is not SJSAS.
     */
    public MessageSecurityBinding newMessageSecurityBinding(File sunDD);
    

}
