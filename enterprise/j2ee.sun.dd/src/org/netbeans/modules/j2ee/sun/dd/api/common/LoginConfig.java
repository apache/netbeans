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
 * LoginConfig.java
 *
 * Created on November 18, 2004, 10:27 AM
 */

package org.netbeans.modules.j2ee.sun.dd.api.common;

import org.netbeans.modules.j2ee.sun.dd.api.VersionNotSupportedException;

/**
 *
 * @author  Nitya Doraisamy
 */
public interface LoginConfig extends org.netbeans.modules.j2ee.sun.dd.api.CommonDDBean {
    
    public static final String VERSION_SERVER_8_1 = "Server 8.1";
    public static final String VERSION_SERVER_9_0 = "Server 9.0";
    
    public static final String AUTH_METHOD = "AuthMethod";	// NOI18N
    public static final String REALM = "Realm";	// NOI18N
    
    /** Setter for auth-method property
     * @param value property value
     */
    public void setAuthMethod(java.lang.String value);
    public java.lang.String getAuthMethod();
    
    // This property supported for AS 9.0+ EJB hosted endpoints only.
    public void setRealm(java.lang.String value) throws VersionNotSupportedException;
    public java.lang.String getRealm() throws VersionNotSupportedException;
    
}
