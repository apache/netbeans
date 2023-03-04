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
/*
 * PortInfo.java
 *
 * Created on November 18, 2004, 9:46 AM
 */

package org.netbeans.modules.j2ee.sun.dd.api.common;

import org.netbeans.modules.j2ee.sun.dd.api.VersionNotSupportedException;

/**
 *
 * @author  Nitya Doraisamy
 */
public interface PortInfo extends org.netbeans.modules.j2ee.sun.dd.api.CommonDDBean {

    public static final String VERSION_SERVER_8_0 = "Server 8.0";

    public static final String SERVICE_ENDPOINT_INTERFACE = "ServiceEndpointInterface";	// NOI18N
    public static final String WSDL_PORT = "WsdlPort";	// NOI18N
    public static final String STUB_PROPERTY = "StubProperty";	// NOI18N
    public static final String CALL_PROPERTY = "CallProperty";	// NOI18N
    public static final String MESSAGE_SECURITY_BINDING = "MessageSecurityBinding";	// NOI18N

        
    /** Setter for service-endpoint-interface property
     * @param value property value
     */
    public void setServiceEndpointInterface(java.lang.String value);
    /** Getter for service-endpoint-interface property.
     * @return property value
     */
    public java.lang.String getServiceEndpointInterface();
    /** Setter for wsdl-port property
     * @param value property value
     */
    public void setWsdlPort(WsdlPort value);
    /** Getter for wsdl-port property.
     * @return property value
     */
    public WsdlPort getWsdlPort(); 
    public WsdlPort newWsdlPort(); 
    
    public StubProperty[] getStubProperty(); 
    public StubProperty getStubProperty(int index);
    public void setStubProperty(StubProperty[] value);
    public void setStubProperty(int index, StubProperty value);
    public int addStubProperty(StubProperty value);
    public int removeStubProperty(StubProperty value);
    public int sizeStubProperty();
    public StubProperty newStubProperty();
    
    public CallProperty[] getCallProperty(); 
    public CallProperty getCallProperty(int index);
    public void setCallProperty(CallProperty[] value);
    public void setCallProperty(int index, CallProperty value);
    public int addCallProperty(CallProperty value);
    public int removeCallProperty(CallProperty value);
    public int sizeCallProperty();
    public CallProperty newCallProperty();
    
    //For AppServer 8.1 & 9.0
    /** Setter for message-security-binding property
     * @param value property value
     */
    public void setMessageSecurityBinding(MessageSecurityBinding value) throws VersionNotSupportedException; 
    /** Getter for message-security-binding property.
     * @return property value
     */
    public MessageSecurityBinding getMessageSecurityBinding() throws VersionNotSupportedException; 
    
    public MessageSecurityBinding newMessageSecurityBinding() throws VersionNotSupportedException; 
}
