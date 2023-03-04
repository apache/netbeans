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
 * IorSecurityConfig.java
 *
 * Created on November 17, 2004, 5:16 PM
 */

package org.netbeans.modules.j2ee.sun.dd.api.ejb;

/**
 *
 * @author  Nitya Doraisamy
 */
public interface IorSecurityConfig extends org.netbeans.modules.j2ee.sun.dd.api.CommonDDBean {
    public static final String TRANSPORT_CONFIG = "TransportConfig";	// NOI18N
    public static final String AS_CONTEXT = "AsContext";	// NOI18N
    public static final String SAS_CONTEXT = "SasContext";	// NOI18N

    /** Setter for transport-config property
     * @param value property value
     */
    public void setTransportConfig(TransportConfig value);
    /** Getter for transport-config property.
     * @return property value
     */
    public TransportConfig getTransportConfig(); 
        
    public TransportConfig newTransportConfig();
    /** Setter for as-context property
     * @param value property value
     */
    public void setAsContext(AsContext value);
    /** Getter for as-context property.
     * @return property value
     */
    public AsContext getAsContext();  
    
    public AsContext newAsContext();
    /** Setter for sas-context property
     * @param value property value
     */
    public void setSasContext(SasContext value); 
    /** Getter for sas-context property.
     * @return property value
     */
    public SasContext getSasContext(); 
        
    public SasContext newSasContext();
}
