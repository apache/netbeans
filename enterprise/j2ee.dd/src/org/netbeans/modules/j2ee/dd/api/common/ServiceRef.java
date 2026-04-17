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

package org.netbeans.modules.j2ee.dd.api.common;
/**
 * Generated interface for ServiceRef element.
 *
 *<p><b><span style="color:red"><em>Important note: Do not provide an implementation of this interface unless you are a DD API provider!</em></span></b>
 *</p>
 */
public interface ServiceRef extends ComponentInterface {

        public static final String SERVICE_REF_NAME = "ServiceRefName";	// NOI18N
	public static final String SERVICE_INTERFACE = "ServiceInterface";	// NOI18N
	public static final String WSDL_FILE = "WsdlFile";	// NOI18N
	public static final String JAXRPC_MAPPING_FILE = "JaxrpcMappingFile";	// NOI18N
	public static final String SERVICE_QNAME = "ServiceQname";	// NOI18N
	public static final String PORT_COMPONENT_REF = "PortComponentRef";	// NOI18N
	public static final String HANDLER = "Handler";	// NOI18N
        /** Setter for service-ref-name property.
         * @param value property value
         */
	public void setServiceRefName(String value);
        /** Getter for service-ref-name property.
         * @return property value 
         */
	public String getServiceRefName();
        /** Setter for service-interface property.
         * @param value property value
         */
	public void setServiceInterface(String value);
        /** Getter for service-interface property.
         * @return property value 
         */
	public String getServiceInterface();
        /** Setter for wsdl-file property.
         * @param value property value
         */
	public void setWsdlFile(java.net.URI value);
        /** Getter for wsdl-file property.
         * @return property value 
         */
	public java.net.URI getWsdlFile();
        /** Setter for jaxrpc-mapping-file property.
         * @param value property value
         */
	public void setJaxrpcMappingFile(String value);
        /** Getter for jaxrpc-mapping-file property.
         * @return property value 
         */
	public String getJaxrpcMappingFile();
        /** Setter for service-qname property.
         * @param value property value
         */
	public void setServiceQname(String value);
        /** Getter for service-qname property.
         * @return property value 
         */
	public String getServiceQname();
        /** Setter for port-component-ref element.
         * @param index position in the array of elements
         * @param valueInterface port-component-ref element (PortComponentRef object)
         */
	public void setPortComponentRef(int index, PortComponentRef valueInterface);
        /** Getter for port-component-ref element.
         * @param index position in the array of elements
         * @return port-component-ref element (PortComponentRef object)
         */
	public PortComponentRef getPortComponentRef(int index);
        /** Setter for port-component-ref elements.
         * @param value array of port-component-ref elements (PortComponentRef objects)
         */
	public void setPortComponentRef(PortComponentRef[] value);
        /** Getter for port-component-ref elements.
         * @return array of port-component-ref elements (PortComponentRef objects)
         */
	public PortComponentRef[] getPortComponentRef();
        /** Returns size of port-component-ref elements.
         * @return number of port-component-ref elements 
         */
	public int sizePortComponentRef();
        /** Adds port-component-ref element.
         * @param valueInterface port-component-ref element (PortComponentRef object)
         * @return index of new port-component-ref
         */
	public int addPortComponentRef(PortComponentRef valueInterface);
        /** Removes port-component-ref element.
         * @param valueInterface port-component-ref element (PortComponentRef object)
         * @return index of the removed port-component-ref
         */
	public int removePortComponentRef(PortComponentRef valueInterface);
        /** Setter for handler element.
         * @param index position in the array of elements
         * @param valueInterface handler element (SeviceRefHandler object)
         */
	public void setHandler(int index, ServiceRefHandler valueInterface);
        /** Getter for handler element.
         * @param index position in the array of elements
         * @return handler element (SeviceRefHandler object)
         */
	public ServiceRefHandler getHandler(int index);
        /** Setter for handler elements.
         * @param value array of handler elements (SeviceRefHandler objects)
         */
	public void setHandler(ServiceRefHandler[] value);
        /** Getter for handler elements.
         * @return array of handler elements (SeviceRefHandler objects)
         */
	public ServiceRefHandler[] getHandler();
        /** Returns size of handler elements.
         * @return number of handler elements 
         */
	public int sizeHandler();
        /** Adds handler element.
         * @param valueInterface handler element (SeviceRefHandler object)
         * @return index of new handler
         */
	public int addHandler(ServiceRefHandler valueInterface);
        /** Removes handler element.
         * @param valueInterface handler element (SeviceRefHandler object)
         * @return index of the removed handler
         */
	public int removeHandler(ServiceRefHandler valueInterface);

        // Java EE 5
        
	void setMappedName(String value) throws VersionNotSupportedException;
	String getMappedName() throws VersionNotSupportedException;
	void setHandlerChains(ServiceRefHandlerChains valueInterface) throws VersionNotSupportedException;
	ServiceRefHandlerChains getHandlerChains() throws VersionNotSupportedException;
	PortComponentRef newPortComponentRef() throws VersionNotSupportedException;
	ServiceRefHandler newServiceRefHandler() throws VersionNotSupportedException;
	ServiceRefHandlerChains newServiceRefHandlerChains() throws VersionNotSupportedException;

}
