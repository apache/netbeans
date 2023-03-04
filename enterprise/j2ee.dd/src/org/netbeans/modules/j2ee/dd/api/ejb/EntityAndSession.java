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

package org.netbeans.modules.j2ee.dd.api.ejb;

import org.netbeans.modules.j2ee.dd.api.common.SecurityRoleRef;

/**
 *
 * @author  Nitya Doraisamy
 */
public interface EntityAndSession extends Ejb {
    //Entity & Session Only
    public static final String HOME = "Home";	// NOI18N
    public static final String REMOTE = "Remote";	// NOI18N
    public static final String LOCAL_HOME = "LocalHome";	// NOI18N
    public static final String LOCAL = "Local";	// NOI18N
    public static final String SECURITY_ROLE_REF = "SecurityRoleRef";	// NOI18N

    // entity & session only
    public String getHome();
    
    public void setHome(String value);
    
    public String getRemote();
    
    public void setRemote(String value);
    
    public String getLocal();
    
    public void setLocal(String value);
    
    public String getLocalHome();
    
    public void setLocalHome(String value);
    
    public void setSecurityRoleRef(int index,SecurityRoleRef value);
    
    public SecurityRoleRef getSecurityRoleRef(int index);
    
    public void setSecurityRoleRef(SecurityRoleRef[] value);
    
    public SecurityRoleRef[] getSecurityRoleRef();
    
    public int sizeSecurityRoleRef();
    
    public int removeSecurityRoleRef(org.netbeans.modules.j2ee.dd.api.common.SecurityRoleRef value);
    
    public int addSecurityRoleRef(org.netbeans.modules.j2ee.dd.api.common.SecurityRoleRef value);
    
    public SecurityRoleRef newSecurityRoleRef();
    
}
