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
 * ServiceRef.java
 *
 * Created on November 17, 2004, 5:09 PM
 */

package org.netbeans.modules.j2ee.sun.dd.api.common;

/**
 *
 * @author  Nitya Doraisamy
 */
public interface ServiceRef extends org.netbeans.modules.j2ee.sun.dd.api.CommonDDBean {

    public static final String SERVICE_REF_NAME = "ServiceRefName";	// NOI18N
    public static final String PORT_INFO = "PortInfo";	// NOI18N
    public static final String CALL_PROPERTY = "CallProperty";	// NOI18N
    public static final String WSDL_OVERRIDE = "WsdlOverride";	// NOI18N
    public static final String SERVICE_IMPL_CLASS = "ServiceImplClass";	// NOI18N
    public static final String SERVICE_QNAME = "ServiceQname";	// NOI18N
        
    /** Setter for service-ref-name property
     * @param value property value
     */
    public void setServiceRefName(java.lang.String value);
    /** Getter for service-ref-name property.
     * @return property value
     */
    public java.lang.String getServiceRefName();
    
    public PortInfo[] getPortInfo(); 
    public PortInfo getPortInfo(int index);
    public void setPortInfo(PortInfo[] value);
    public void setPortInfo(int index, PortInfo value);
    public int addPortInfo(PortInfo value);
    public int removePortInfo(PortInfo value);
    public int sizePortInfo();
    public PortInfo newPortInfo();
    
    public CallProperty[] getCallProperty(); 
    public CallProperty getCallProperty(int index);
    public void setCallProperty(CallProperty[] value);
    public void setCallProperty(int index, CallProperty value);
    public int addCallProperty(CallProperty value);
    public int removeCallProperty(CallProperty value);
    public int sizeCallProperty(); 
    public CallProperty newCallProperty(); 
    
    public void setWsdlOverride(java.lang.String value);
    public java.lang.String getWsdlOverride();
    
    public void setServiceImplClass(java.lang.String value);
    public java.lang.String getServiceImplClass();
    
    public void setServiceQname(ServiceQname value);
    public ServiceQname getServiceQname();
    public ServiceQname newServiceQname();
}
