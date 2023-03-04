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
 * MdbResourceAdapter.java
 *
 * Created on November 17, 2004, 5:19 PM
 */

package org.netbeans.modules.j2ee.sun.dd.api.ejb;

/**
 *
 * @author  Nitya Doraisamy
 */
public interface MdbResourceAdapter extends org.netbeans.modules.j2ee.sun.dd.api.CommonDDBean {

    public static final String RESOURCE_ADAPTER_MID = "ResourceAdapterMid";	// NOI18N
    public static final String ACTIVATION_CONFIG = "ActivationConfig";	// NOI18N

    /** Setter for resource-adapter-mid property
     * @param value property value
     */
    public void setResourceAdapterMid(java.lang.String value);
    /** Getter for resource-adapter-mid property.
     * @return property value
     */
    public java.lang.String getResourceAdapterMid();
    /** Setter for activation-config property
     * @param value property value
     */
    public void setActivationConfig(ActivationConfig value);
    /** Getter for activation-config property.
     * @return property value
     */
    public ActivationConfig getActivationConfig(); 
    
    public ActivationConfig newActivationConfig();
}
